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

/**
 * An abstract syntax tree of binary operators.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class BinaryAST implements AST {

	/**
	 * the type of binary operator.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Type implements OperationMnemonics {
		LOR(null, OP_LOGICAL),
		LAND(null, OP_LOGICAL),
		EQ(null, COMPARISON),
		NE(null, COMPARISON),
		LT(null, COMPARISON),
		GE(null, COMPARISON),
		GT(null, COMPARISON),
		LE(null, COMPARISON),
		ADD(Utils.ADD, OP_VALUE),
		SUB(Utils.SUB, OP_VALUE),
		MUL(Utils.MUL, OP_VALUE),
		DIV(Utils.DIV, OP_VALUE),
		REM(Utils.REM, OP_VALUE),
		POW(Utils.POW, OP_VALUE);

		private BinaryOperator code;

		public BinaryOperator getCode() {
			return code;
		}

		private Type(BinaryOperator m, int fl) {
			code = m;
		}
	}

	private static final int OP_VALUE = 0;
	private static final int OP_LOGICAL = 1;
	private static final int COMPARISON = 2;

	private Type type;
	private AST left, right;

	/**
	 * creates the AST of binary operator.
	 * 
	 * @param type the type of this AST
	 * @param left the AST of left value
	 * @param right the AST of right value
	 */
	public BinaryAST(Type type, AST left, AST right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	/**
	 * gets the type of this AST.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * gets the AST of left value.
	 */
	public AST getLeft() {
		return left;
	}

	/**
	 * gets the AST of right value.
	 */
	public AST getRight() {
		return right;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Utils.operatePrimitive(left, right, type, functions, space, code);
	}

}
