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
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.If;
import net.morilib.syaro.classfile.code.LdcW;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class UnaryAST implements AST {

	public static enum Type {
		INEG(Mnemonic.INEG),
		IBNOT(null),
		ILNOT(null);
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
	public void putCode(LocalVariableSpace space, Code code) {
		int lbl0, lbl1;

		if(type.mnemonic != null) {
			node.putCode(space, code);
			code.addCode(type.mnemonic);
		} else {
			switch(type) {
			case IBNOT:
				node.putCode(space, code);
				code.addCode(new LdcW(new ConstantInteger(0xffffffff)));
				code.addCode(Mnemonic.IXOR);
				break;
			case ILNOT:
				node.putCode(space, code);
				lbl0 = code.addCode(new If(If.Cond.EQ));
				code.addCode(new IConst(1));
				lbl1 = code.addCode(new Goto());
				((If)code.getCode(lbl0)).setOffset(code.getCurrentOffset(lbl0));
				code.addCode(new IConst(0));
				((Goto)code.getCode(lbl1)).setOffset(code.getCurrentOffset(lbl1));
				break;
			default: throw new RuntimeException();
			}
		}
	}

}
