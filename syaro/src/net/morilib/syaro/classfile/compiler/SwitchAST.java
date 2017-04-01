/*
 * Copyright 2016-2017 Yuichiro Moriguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.morilib.syaro.classfile.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.LookupSwitch;
import net.morilib.syaro.classfile.code.TableSwitch;

/**
 * @author Yuichiro MORIGUCHI
 */
public class SwitchAST implements SAST {

	private AST expr;
	private List<SAST> stmtBuffer = null;
	private AST caseBuffer = null;
	private Map<Integer, List<SAST>> statementMap =
			new LinkedHashMap<Integer, List<SAST>>();
	private List<SAST> defaultStmt = null;

	public SwitchAST(AST expr) {
		this.expr = expr;
	}

	private static int getIntegerFromAST(AST ast) {
		return ((IntegerAST)ast).getValue();
	}

	public void endSwitch() {
		int val;

		if(stmtBuffer != null) {
			if(caseBuffer != null) {
				val = getIntegerFromAST(caseBuffer);
				if(statementMap.containsKey(val)) {
					throw new RuntimeException("duplicate case label");
				}
				statementMap.put(val, stmtBuffer);
			} else {
				if(defaultStmt != null) {
					throw new RuntimeException("duplicated default label");
				}
				defaultStmt = stmtBuffer;
			}
		}
	}

	public void addCase(AST ast) {
		if(!(ast instanceof IntegerAST)) {
			throw new RuntimeException("case label must be integer");
		}
		endSwitch();
		stmtBuffer = new ArrayList<SAST>();
		caseBuffer = ast;
	}

	public void addDefault() {
		endSwitch();
		stmtBuffer = new ArrayList<SAST>();
		caseBuffer = null;
	}

	public void addStatement(SAST ast) {
		stmtBuffer.add(ast);
	}

	private boolean isDense(SortedSet<Integer> npairs) {
		for(int i = npairs.first(); i <= npairs.last(); i++) {
			if(!npairs.contains(i)) {
				return false;
			}
		}
		return true;
	}

	private void putCodeLookup(FunctionSpace functions,
			LocalVariableSpace space, Code code,
			List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses,
			SortedSet<Integer> npairs) {
		LookupSwitch ls;
		List<Integer> val;
		int lsidx;

		ls = new LookupSwitch(npairs, -1);
		lsidx = code.addCode(ls);
		for(Map.Entry<Integer, List<SAST>> e : statementMap.entrySet()) {
			ls.putOffset(e.getKey(), code.getCurrentOffset(lsidx));
			for(SAST stmt : e.getValue()) {
				stmt.putCode(functions, space, code, breakIndices, continueAddress,
						continueIndices, loopFinallyAddresses, returnFinallyAddresses);
			}
		}
		ls.setDefaultOffset(code.getCurrentOffset(lsidx));
		if(defaultStmt != null) {
			for(SAST stmt : defaultStmt) {
				stmt.putCode(functions, space, code,
						breakIndices, continueAddress, continueIndices,
						loopFinallyAddresses, returnFinallyAddresses);
			}
		}
	}

	private void putCodeTable(FunctionSpace functions,
			LocalVariableSpace space, Code code,
			List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses,
			SortedSet<Integer> npairs) {
		TableSwitch ls;
		List<Integer> val;
		int lsidx;

		ls = new TableSwitch(npairs.first(), npairs.last());
		lsidx = code.addCode(ls);
		for(Map.Entry<Integer, List<SAST>> e : statementMap.entrySet()) {
			ls.putOffset(e.getKey(), code.getCurrentOffset(lsidx));
			for(SAST stmt : e.getValue()) {
				stmt.putCode(functions, space, code, breakIndices, continueAddress,
						continueIndices, loopFinallyAddresses, returnFinallyAddresses);
			}
		}
		ls.setDefaultOffset(code.getCurrentOffset(lsidx));
		if(defaultStmt != null) {
			for(SAST stmt : defaultStmt) {
				stmt.putCode(functions, space, code,
						breakIndices, continueAddress, continueIndices,
						loopFinallyAddresses, returnFinallyAddresses);
			}
		}
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code, List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices, List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses) {
		List<Integer> brk = new ArrayList<Integer>();
		SortedSet<Integer> npairs;

		if(!expr.getASTType(functions, space).isConversible(Primitive.INT)) {
			throw new RuntimeException("expression of case must be int");
		}

		expr.putCode(functions, space, code);
		npairs = new TreeSet<Integer>(statementMap.keySet());
		if(isDense(npairs)) {
			putCodeTable(functions, space, code, brk,
					continueAddress, continueIndices,
					loopFinallyAddresses, returnFinallyAddresses, npairs);
		} else {
			putCodeLookup(functions, space, code, brk,
					continueAddress, continueIndices,
					loopFinallyAddresses, returnFinallyAddresses, npairs);
		}
		for(int x : brk) {
			((Goto)code.getCode(x)).setOffset(code.getCurrentOffset(x));
		}
	}

}
