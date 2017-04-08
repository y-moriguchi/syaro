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

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.code.Instanceof;

/**
 * An abstract syntax tree for instanceof.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class InstanceofAST implements AST {

	private AST left;
	private VariableType right;

	/**
	 * creates an AST for instanceof.
	 * 
	 * @param left  left node of AST
	 * @param right type to check
	 */
	public InstanceofAST(AST left, VariableType right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		String tname;

		left.putCode(functions, space, code);
		if(right instanceof ArrayType) {
			tname = right.getDescriptor(functions);
		} else {
			tname = right.getClassName(functions);
		}
		code.addCode(new Instanceof(ConstantClass.getInstance(tname)));
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return Primitive.INT;
	}

}
