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
package net.morilib.patricia;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.LookupSwitch;

/**
 * An abstract syntax tree for case.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class CaseAST implements SAST {

	private AST expr;
	private List<SAST> caseStatement;
	private List<List<Integer>> caseValue;
	private SAST caseElse = null;

	/**
	 * constructs case AST.
	 * 
	 * @param expr value expression
	 */
	public CaseAST(AST expr) {
		this.expr = expr;
		caseStatement = new ArrayList<SAST>();
		caseValue = new ArrayList<List<Integer>>();
	}

	public void addCase(List<Integer> values, SAST stmt) {
		caseValue.add(new ArrayList<Integer>(values));
		caseStatement.add(stmt);
	}

	public void setElse(SAST stmt) {
		caseElse = stmt;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space, Code code,
			List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices) {
		SortedMap<Integer, Integer> npairs;
		LookupSwitch ls;
		List<Integer> val;
		List<Integer> end = new ArrayList<Integer>();
		int lsidx;

		expr.putCode(functions, space, code);
		npairs = new TreeMap<Integer, Integer>();
		for(int i = 0; i < caseValue.size(); i++) {
			val = caseValue.get(i);
			for(int j = 0; j < val.size(); j++) {
				if(npairs.containsKey(val.get(j))) {
					throw new SemanticsException("duplicated case");
				}
				npairs.put(val.get(j), -1);
			}
		}
		ls = new LookupSwitch(npairs, -1);
		lsidx = code.addCode(ls);
		for(int i = 0; i < caseValue.size(); i++) {
			val = caseValue.get(i);
			for(int j = 0; j < val.size(); j++) {
				ls.putOffset(val.get(j), code.getCurrentOffset(lsidx));
			}
			caseStatement.get(i).putCode(functions, space, code,
					breakIndices, continueAddress, continueIndices);
			end.add(code.addCode(new Goto()));
		}
		ls.setDefaultOffset(code.getCurrentOffset(lsidx));
		if(caseElse != null) {
			caseElse.putCode(functions, space, code,
					breakIndices, continueAddress, continueIndices);
		}
		for(int l : end) {
			((Goto)code.getCode(l)).setOffset(code.getCurrentOffset(l));
		}
	}

}
