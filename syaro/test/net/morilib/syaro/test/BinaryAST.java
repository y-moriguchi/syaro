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
public class BinaryAST implements AST {

	public static enum Type {
		IBOR(Mnemonic.IOR),
		IBXOR(Mnemonic.IXOR),
		IBAND(Mnemonic.IAND),
		ISHR(Mnemonic.ISHR),
		ISHL(Mnemonic.ISHL),
		IADD(Mnemonic.IADD),
		ISUB(Mnemonic.ISUB),
		IMUL(Mnemonic.IMUL),
		IDIV(Mnemonic.IDIV),
		IREM(Mnemonic.IREM);
		private Mnemonic mnemonic;
		private Type(Mnemonic m) {
			mnemonic = m;
		}
	}

	private Type type;
	private AST left, right;

	public BinaryAST(Type type, AST left, AST right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	public Type getType() {
		return type;
	}

	public AST getLeft() {
		return left;
	}

	public AST getRight() {
		return right;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.AST#putCode(net.morilib.syaro.classfile.Code)
	 */
	@Override
	public void putCode(Code code) {
		left.putCode(code);
		right.putCode(code);
		if(type.mnemonic != null) {
			code.addCode(type.mnemonic);
		}
	}

}
