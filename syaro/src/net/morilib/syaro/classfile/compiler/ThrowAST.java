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

import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class ThrowAST implements SAST {

	private AST expr;

	public ThrowAST(AST expr) {
		this.expr = expr;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code, List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses) {
		expr.putCode(functions, space, code);
		code.addCode(Mnemonic.ATHROW);
	}

}
