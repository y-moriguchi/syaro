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
import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.If;

/**
 * An abstract syntax tree for for statement.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ForAST implements SAST {

	private AST initialize;
	private AST condition;
	private AST inclement;
	private SAST statement;

	/**
	 * constructs AST for for statement.
	 * 
	 * @param init initialize expression
	 * @param cond condition expression
	 * @param incl step expression
	 * @param stmt stamtement to loop
	 */
	public ForAST(AST init, AST cond, AST incl, SAST stmt) {
		initialize = init;
		condition = cond;
		inclement = incl;
		statement = stmt;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses) {
		List<Integer> brk = new ArrayList<Integer>();
		List<Integer> cnt = new ArrayList<Integer>();
		int addr, ifa;
		If _if;
		Goto _gt;

		if(initialize != null) {
			initialize.putCode(functions, space, code);
			code.addCode(Mnemonic.POP);
		}
		addr = code.getCurrentAddress();
		condition.putCode(functions, space, code);
		_if = new If(If.Cond.EQ);
		ifa = code.addCode(_if);
		statement.putCode(functions, space, code, brk, -1, cnt,
				new ArrayList<Integer>(), returnFinallyAddresses);
		for(int x : cnt) {
			((Goto)code.getCode(x)).setOffset(code.getCurrentOffset(x));
		}
		inclement.putCode(functions, space, code);
		code.addCode(Mnemonic.POP);
		_gt = new Goto();
		_gt.setOffset(addr - code.getCurrentAddress());
		code.addCode(_gt);
		_if.setOffset(code.getCurrentOffset(ifa));
		for(int x : brk) {
			((Goto)code.getCode(x)).setOffset(code.getCurrentOffset(x));
		}
	}

}
