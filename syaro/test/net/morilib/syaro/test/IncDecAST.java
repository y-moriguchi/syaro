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
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.IStore;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class IncDecAST implements AST {

	private boolean isInc;
	private boolean isPre;
	private AST node;

	public IncDecAST(boolean isInc, boolean isPre, AST node) {
		this.isInc = isInc;
		this.isPre = isPre;
		this.node = node;
	}

	public AST getNode() {
		return node;
	}

	private int getLocalIndex(LocalVariableSpace space, AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return space.getIndex(((SymbolAST)ast).getName());
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.AST#putCode(net.morilib.syaro.classfile.Code)
	 */
	@Override
	public void putCode(LocalVariableSpace space, Code code) {
		Mnemonic val = new IConst(isInc ? 1 : -1);

		if(isPre) {
			node.putCode(space, code);
			code.addCode(val);
			code.addCode(Mnemonic.IADD);
			code.addCode(Mnemonic.DUP);
			code.addCode(new IStore(getLocalIndex(space, node)));
		} else {
			node.putCode(space, code);
			code.addCode(Mnemonic.DUP);
			code.addCode(val);
			code.addCode(Mnemonic.IADD);
			code.addCode(new IStore(getLocalIndex(space, node)));
		}
	}

}
