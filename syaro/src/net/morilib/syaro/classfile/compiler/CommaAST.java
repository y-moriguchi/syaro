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
import net.morilib.syaro.classfile.Mnemonic;

/**
 * An abstract syntax tree of comma operator.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class CommaAST implements AST {

	private AST left;
	private AST right;

	/**
	 * creates the AST of comma operator.
	 * 
	 * @param left the AST of left value
	 * @param right the AST of right value
	 */
	public CommaAST(AST left, AST right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		VariableType vl;

		vl = left.getASTType(functions, space);
		left.putCode(functions, space, code);
		if(vl.equals(Primitive.LONG) || vl.equals(Primitive.DOUBLE)) {
			code.addCode(Mnemonic.POP2);
		} else {
			code.addCode(Mnemonic.POP);
		}
		right.putCode(functions, space, code);
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return right.getASTType(functions, space);
	}

}
