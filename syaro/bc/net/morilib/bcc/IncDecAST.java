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
 * An abstract syntax tree for increment/decrement.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class IncDecAST implements AST {

	private boolean isInc;
	private boolean isPre;
	private AST node;

	/**
	 * creates an AST of increment/decrement.
	 * 
	 * @param isInc true if the AST is increment
	 * @param isPre true if prefix
	 * @param node  operand
	 */
	public IncDecAST(boolean isInc, boolean isPre, AST node) {
		this.isInc = isInc;
		this.isPre = isPre;
		this.node = node;
	}

	/**
	 * gets the node of operand.
	 */
	public AST getNode() {
		return node;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		if(isPre) {
			node.putCode(functions, space, code);
			Utils.putConstDecimal(code, true);
			Utils.putDecimalMethod(code, isInc ? "add" : "subtract");
			code.addCode(Mnemonic.DUP);
		} else {
			node.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP);
			Utils.putConstDecimal(code, true);
			Utils.putDecimalMethod(code, isInc ? "add" : "subtract");
		}
		Utils.setVar(node, functions, space, code);
	}

}
