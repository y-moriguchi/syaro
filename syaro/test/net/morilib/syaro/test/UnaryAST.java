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
import net.morilib.syaro.classfile.ConstantInteger;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.LdcW;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class UnaryAST implements AST {

	public static enum Type {
		INEG(Mnemonic.INEG),
		IBNOT(null);
		private Mnemonic mnemonic;
		private Type(Mnemonic m) {
			mnemonic = m;
		}
	}

	private Type type;
	private AST node;

	public UnaryAST(Type type, AST node) {
		this.type = type;
		this.node = node;
	}

	public Type getType() {
		return type;
	}

	public AST getNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.AST#putCode(net.morilib.syaro.classfile.Code)
	 */
	@Override
	public void putCode(Code code) {
		if(type.mnemonic != null) {
			node.putCode(code);
			code.addCode(type.mnemonic);
		} else {
			switch(type) {
			case IBNOT:
				node.putCode(code);
				code.addCode(new LdcW(new ConstantInteger(0xffffffff)));
				code.addCode(Mnemonic.IXOR);
				break;
			default: throw new RuntimeException();
			}
		}
	}

}
