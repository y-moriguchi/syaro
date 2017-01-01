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

import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.If;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class IfAST implements SAST {

	private AST condition;
	private SAST ifClause;
	private SAST elseClause;

	public IfAST(AST cond, SAST _if, SAST _else) {
		condition = cond;
		ifClause = _if;
		elseClause = _else;
	}

	@Override
	public void putCode(LocalVariableSpace space, Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices) {
		int ifa, gta;
		If _if;
		Goto _gt;

		if(elseClause != null) {
			condition.putCode(space, code);
			_if = new If(If.Cond.EQ);
			ifa = code.addCode(_if);
			ifClause.putCode(space, code, breakIndices, continueAddress, continueIndices);
			_gt = new Goto();
			gta = code.addCode(_gt);
			_if.setOffset(code.getCurrentOffset(ifa));
			elseClause.putCode(space, code, breakIndices, continueAddress, continueIndices);
			_gt.setOffset(code.getCurrentOffset(gta));
		} else {
			condition.putCode(space, code);
			_if = new If(If.Cond.EQ);
			ifa = code.addCode(_if);
			ifClause.putCode(space, code, breakIndices, continueAddress, continueIndices);
			_if.setOffset(code.getCurrentOffset(ifa));
		}
	}

}
