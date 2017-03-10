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
 * An abstract syntax tree of unary operators.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class UnaryAST implements AST {

	/**
	 * The types of unary operators.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Type {
		NEG(Utils.NEG, OP_VALUE),
		LNOT(Utils.LNOT, OP_LOGICAL);

		private UnaryOperator code;
		private Type(UnaryOperator m, int fl) {
			code = m;
		}
	}

	private static final int OP_VALUE = 0;
	private static final int OP_LOGICAL = 1;

	private Type type;
	private AST node;

	/**
	 * creates an AST.
	 * 
	 * @param type the type of a unary operator
	 * @param node the AST of value
	 */
	public UnaryAST(Type type, AST node) {
		this.type = type;
		this.node = node;
	}

	/**
	 * gets the type of this AST.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * gets the node of this AST.
	 */
	public AST getNode() {
		return node;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		type.code.putCode(node, functions, space, code);
	}

}
