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
package net.morilib.patricia;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantInteger;
import net.morilib.syaro.classfile.ConstantLong;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.If;
import net.morilib.syaro.classfile.code.Ldc2W;
import net.morilib.syaro.classfile.code.LdcW;

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
		NEG(Mnemonic.INEG, Mnemonic.LNEG, Mnemonic.DNEG, Mnemonic.FNEG, OP_VALUE),
		NOT(null, null, null, null, OP_LOGICAL);
		private Mnemonic mnemonic;
		private Mnemonic mnemonicLong;
		private Mnemonic mnemonicDouble;
		private Mnemonic mnemonicFloat;
		private Type(Mnemonic m, Mnemonic l, Mnemonic d, Mnemonic f, int fl) {
			mnemonic = m;
			mnemonicLong = l;
			mnemonicDouble = d;
			mnemonicFloat = f;
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
		int lbl0, lbl1;
		VariableType t;

		t = node.getASTType(functions, space);
		if(type.mnemonic != null) {
			node.putCode(functions, space, code);
			if(t.isConversible(Primitive.INT)) {
				code.addCode(type.mnemonic);
			} else if(t.equals(Primitive.LONG)) {
				code.addCode(type.mnemonicLong);
			} else if(t.equals(Primitive.FLOAT)) {
				code.addCode(type.mnemonicFloat);
			} else if(t.equals(Primitive.DOUBLE)) {
				code.addCode(type.mnemonicDouble);
			} else {
				throw new SemanticsException("type mismatch");
			}
		} else {
			switch(type) {
			case NOT:
				node.putCode(functions, space, code);
				if(t.equals(Primitive.BOOLEAN)) {
					lbl0 = code.addCode(new If(If.Cond.NE));
					code.addCode(new IConst(1));
					lbl1 = code.addCode(new Goto());
					((If)code.getCode(lbl0)).setOffset(code.getCurrentOffset(lbl0));
					code.addCode(new IConst(0));
					((Goto)code.getCode(lbl1)).setOffset(code.getCurrentOffset(lbl1));
				} else if(t.isConversible(Primitive.INT)) {
					node.putCode(functions, space, code);
					code.addCode(new LdcW(ConstantInteger.getInstance(0xffffffff)));
					code.addCode(Mnemonic.IXOR);
					break;
				} else if(t.equals(Primitive.LONG)) {
					node.putCode(functions, space, code);
					code.addCode(new Ldc2W(ConstantLong.getInstance(0xffffffffffffffffl)));
					code.addCode(Mnemonic.LXOR);
					break;
				} else {
					throw new RuntimeException("type mismatch");
				}
				break;
			default: throw new RuntimeException();
			}
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return node.getASTType(functions, space);
	}

}
