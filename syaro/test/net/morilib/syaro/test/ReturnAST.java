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
import net.morilib.syaro.classfile.Mnemonic;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class ReturnAST implements SAST {

	private AST expr;

	public ReturnAST(AST expr) {
		this.expr = expr;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices) {
		Primitive ep;

		if(expr != null) {
			expr.putCode(functions, space, code);
			if(space.getThisReturnType().isPrimitive()) {
				ep = (Primitive)space.getThisReturnType();
				if(ep.equals(Primitive.INT)) {
					if(!ep.isConversible(Primitive.INT)) {
						throw new RuntimeException("type mismatch");
					}
					code.addCode(Mnemonic.IRETURN);
				} else if(ep.equals(Primitive.FLOAT)) {
					if(!ep.isConversible(Primitive.FLOAT)) {
						throw new RuntimeException("type mismatch");
					}
					Utils.putConversionFloat(ep, code);
					code.addCode(Mnemonic.FRETURN);
				} else if(ep.equals(Primitive.DOUBLE)) {
					Utils.putConversionDouble(ep, code);
					code.addCode(Mnemonic.DRETURN);
				} else {
					throw new RuntimeException("subroutine must not return value");
				}
			}
		} else {
			code.addCode(Mnemonic.RETURN);
		}
	}

}
