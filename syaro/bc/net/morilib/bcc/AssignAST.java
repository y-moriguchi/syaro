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
package net.morilib.bcc;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * An abstract syntax tree for operator of assignment.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class AssignAST implements AST {

	/**
	 * the type of binary operator.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Type implements OperationMnemonics {
		ADD(Utils.ADD),
		SUB(Utils.SUB),
		MUL(Utils.MUL),
		DIV(Utils.DIV),
		REM(Utils.REM),
		POW(Utils.POW);
		private BinaryOperator code;

		public BinaryOperator getCode() {
			return code;
		}

		private Type(BinaryOperator m) {
			code = m;
		}
	}

	private Type type;
	private AST left, right;

	/**
	 * creates the AST of assignment operator.
	 * 
	 * @param type the type of this AST
	 * @param left the AST of left value
	 * @param right the AST of right value
	 */
	public AssignAST(Type op, AST left, AST right) {
		this.type = op;
		this.left = left;
		this.right = right;
	}

	/**
	 * gets the type of this AST.
	 */
	public AST getLeft() {
		return left;
	}

	/**
	 * gets the AST of left value.
	 */
	public AST getRight() {
		return right;
	}

	private void putCodePrimitive(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		if(type == null) {
			right.putCode(functions, space, code);
		} else {
			Utils.operatePrimitive(left, right, type, functions, space, code);
		}
		code.addCode(Mnemonic.DUP);
		Utils.setVar(left, functions, space, code);
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		putCodePrimitive(functions, space, code);
	}

}
