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
package net.morilib.syaro.test;

import java.util.ArrayList;
import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.If;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class DoAST implements SAST {

	private AST condition;
	private SAST statement;

	public DoAST(AST cond, SAST stmt) {
		condition = cond;
		statement = stmt;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices) {
		List<Integer> brk = new ArrayList<Integer>();
		int addr;
		If _if;

		addr = code.getCurrentAddress();
		statement.putCode(functions, space, code, brk, addr, null);
		condition.putCode(functions, space, code);
		_if = new If(If.Cond.NE);
		_if.setOffset(addr - code.getCurrentAddress());
		code.addCode(_if);
		for(int x : brk) {
			((Goto)code.getCode(x)).setOffset(code.getCurrentOffset(x));
		}
	}

}
