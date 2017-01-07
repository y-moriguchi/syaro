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

import java.util.ArrayList;
import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.DConst;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.If;
import net.morilib.syaro.classfile.code.IfIcmp;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class BinaryAST implements AST {

	public static enum Type {
		ILOR(null, null),
		ILAND(null, null),
		IEQ(null, null),
		INE(null, null),
		ILT(null, null),
		IGE(null, null),
		IGT(null, null),
		ILE(null, null),
		IBOR(Mnemonic.IOR, null),
		IBXOR(Mnemonic.IXOR, null),
		IBAND(Mnemonic.IAND, null),
		ISHR(Mnemonic.ISHR, null),
		ISHL(Mnemonic.ISHL, null),
		IADD(Mnemonic.IADD, Mnemonic.DADD),
		ISUB(Mnemonic.ISUB, Mnemonic.DSUB),
		IMUL(Mnemonic.IMUL, Mnemonic.DMUL),
		IDIV(Mnemonic.IDIV, Mnemonic.DDIV),
		IREM(Mnemonic.IREM, Mnemonic.DREM);
		private Mnemonic mnemonic;
		private Mnemonic mnemonicDouble;
		private Type(Mnemonic m, Mnemonic d) {
			mnemonic = m;
			mnemonicDouble = d;
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

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		if(type.mnemonic != null) {
			if(left.getASTType(functions, space).equals(Primitive.INT) &&
					right.getASTType(functions, space).equals(Primitive.INT)) {
				left.putCode(functions, space, code);
				right.putCode(functions, space, code);
				code.addCode(type.mnemonic);
			} else if(type.mnemonicDouble == null) {
				throw new RuntimeException("type mismatch");
			} else {
				left.putCode(functions, space, code);
				if(left.getASTType(functions, space).equals(Primitive.INT)) {
					code.addCode(Mnemonic.I2D);
				}
				right.putCode(functions, space, code);
				if(right.getASTType(functions, space).equals(Primitive.INT)) {
					code.addCode(Mnemonic.I2D);
				}
				code.addCode(type.mnemonicDouble);
			}
		} else {
			switch(type) {
			case ILOR:
				putCodeLogical(this, functions, space, code, Type.ILOR, If.Cond.NE);
				break;
			case ILAND:
				putCodeLogical(this, functions, space, code, Type.ILAND, If.Cond.EQ);
				break;
			case IEQ:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.EQ);
				break;
			case INE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.NE);
				break;
			case ILT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LT);
				break;
			case ILE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LE);
				break;
			case IGT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.GT);
				break;
			case IGE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.GE);
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	private static void putCodeLogical(AST bnode,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code, Type tp, If.Cond cond) {
		List<Integer> labels = new ArrayList<Integer>();
		AST node = bnode;
		int caddr;
		If xif;

		while(true) {
			if(!(node instanceof BinaryAST)) {
				break;
			} else if(!((BinaryAST)node).type.equals(tp)) {
				break;
			}
			((BinaryAST)node).left.putCode(functions, space, code);
			if(!((BinaryAST)node).left.equals(Primitive.INT)) {
				code.addCode(new DConst(0.0));
				code.addCode(Mnemonic.DCMPG);
			}
			code.addCode(Mnemonic.DUP);
			labels.add(code.addCode(new If(cond)));
			code.addCode(Mnemonic.POP);
			node = ((BinaryAST)node).right;
		}
		node.putCode(functions, space, code);
		if(!node.equals(Primitive.INT)) {
			code.addCode(new DConst(0.0));
			code.addCode(Mnemonic.DCMPG);
		}
		caddr = code.getCurrentAddress();
		for(int idx : labels) {
			xif = (If)code.getCode(idx);
			xif.setOffset(caddr - code.getAddress(idx));
		}
	}

	private static void putCodeCompare(BinaryAST bnode,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			IfIcmp.Cond cond) {
		int ifa, gta;
		IfIcmp _if;
		Goto _gt;

		if(bnode.left.getASTType(functions, space).equals(Primitive.INT) &&
				bnode.right.getASTType(functions, space).equals(Primitive.INT)) {
			bnode.left.putCode(functions, space, code);
			bnode.right.putCode(functions, space, code);
		} else {
			bnode.left.putCode(functions, space, code);
			if(bnode.left.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(Mnemonic.I2D);
			}
			bnode.right.putCode(functions, space, code);
			if(bnode.right.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(Mnemonic.I2D);
			}
			code.addCode(Mnemonic.DCMPG);
			code.addCode(new IConst(0));
		}
		_if = new IfIcmp(cond);
		ifa = code.addCode(_if);
		code.addCode(new IConst(0));
		_gt = new Goto();
		gta = code.addCode(_gt);
		_if.setOffset(code.getCurrentOffset(ifa));
		code.addCode(new IConst(1));
		_gt.setOffset(code.getCurrentOffset(gta));
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		if(type.mnemonic == null && type.mnemonicDouble == null) {
			return Primitive.INT;
		} else if(left.getASTType(functions, space).equals(Primitive.INT) &&
				right.getASTType(functions, space).equals(Primitive.INT)) {
			return Primitive.INT;
		} else {
			return Primitive.DOUBLE;
		}
	}

}
