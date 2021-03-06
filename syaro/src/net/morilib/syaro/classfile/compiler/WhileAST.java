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
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.If;

/**
 * An abstract syntax tree for while statement.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class WhileAST implements SAST {

	private AST condition;
	private SAST statement;

	/**
	 * creates an AST of while statement.
	 * 
	 * @param cond condition
	 * @param stmt statement
	 */
	public WhileAST(AST cond, SAST stmt) {
		condition = cond;
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
		int addr, ifa;
		If _if;
		Goto _gt;

		addr = code.getCurrentAddress();
		condition.putCode(functions, space, code);
		_if = new If(If.Cond.EQ);
		ifa = code.addCode(_if);
		statement.putCode(functions, space, code, brk, addr, null,
				new ArrayList<Integer>(), returnFinallyAddresses);
		_gt = new Goto();
		_gt.setOffset(addr - code.getCurrentAddress());
		code.addCode(_gt);
		_if.setOffset(code.getCurrentOffset(ifa));
		for(int x : brk) {
			((Goto)code.getCode(x)).setOffset(code.getCurrentOffset(x));
		}
	}

}
