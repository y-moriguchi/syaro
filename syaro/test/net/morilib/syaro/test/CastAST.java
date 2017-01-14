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

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class CastAST implements AST {

	private VariableType type;
	private AST node;

	public CastAST(VariableType t, AST e) {
		type = t;
		node = e;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		VariableType ntype = node.getASTType(functions, space);

		node.putCode(functions, space, code);
		if(ntype.isCastable(type)) {
			throw new RuntimeException("can not cast");
		} else if(ntype.equals(Primitive.INT)) {
			if(type.equals(Primitive.FLOAT)) {
				code.addCode(Mnemonic.I2F);
			} else if(type.equals(Primitive.DOUBLE)) {
				code.addCode(Mnemonic.I2D);
			}
		} else if(ntype.equals(Primitive.FLOAT)) {
			if(type.equals(Primitive.INT)) {
				code.addCode(Mnemonic.F2I);
			} else if(type.equals(Primitive.DOUBLE)) {
				code.addCode(Mnemonic.F2D);
			}
		} else if(ntype.equals(Primitive.DOUBLE)) {
			if(type.equals(Primitive.INT)) {
				code.addCode(Mnemonic.D2I);
			} else if(type.equals(Primitive.FLOAT)) {
				code.addCode(Mnemonic.D2F);
			}
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return type;
	}

}
