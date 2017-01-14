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
import net.morilib.syaro.classfile.code.DConst;
import net.morilib.syaro.classfile.code.FConst;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.LConst;

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

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType t;
		Mnemonic val;

		t = node.getASTType(functions, space);
		if(t.isConversible(Primitive.INT)) {
			val = new IConst(isInc ? 1 : -1);
		} else if(t.equals(Primitive.LONG)) {
			val = new LConst(1);
		} else if(t.equals(Primitive.FLOAT)) {
			val = new FConst(1);
		} else if(t.equals(Primitive.DOUBLE)) {
			val = new DConst(1);
		} else {
			throw new RuntimeException("type mismatch");
		}
		Utils.putCodeArrayRef(node, functions, space, code);
		if(isPre) {
			node.putCode(functions, space, code);
			code.addCode(val);
			if(t.isConversible(Primitive.INT)) {
				code.addCode(Mnemonic.IADD);
				Utils.putDup(node, code);
			} else if(t.equals(Primitive.LONG)) {
				code.addCode(isInc ? Mnemonic.LADD : Mnemonic.LSUB);
				Utils.putDup2(node, code);
			} else if(t.equals(Primitive.FLOAT)) {
				code.addCode(isInc ? Mnemonic.FADD : Mnemonic.FSUB);
				Utils.putDup(node, code);
			} else {
				code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
				Utils.putDup2(node, code);
			}
		} else {
			node.putCode(functions, space, code);
			if(t.isConversible(Primitive.INT)) {
				Utils.putDup(node, code);
				code.addCode(val);
				code.addCode(Mnemonic.IADD);
			} else if(t.equals(Primitive.LONG)) {
				Utils.putDup2(node, code);
				code.addCode(val);
				code.addCode(isInc ? Mnemonic.LADD : Mnemonic.LSUB);
			} else if(t.equals(Primitive.FLOAT)) {
				Utils.putDup(node, code);
				code.addCode(val);
				code.addCode(isInc ? Mnemonic.FADD : Mnemonic.FSUB);
			} else {
				Utils.putDup2(node, code);
				code.addCode(val);
				code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
			}
		}
		Utils.setVar(node, functions, space, code);
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return node.getASTType(functions, space);
	}

}
