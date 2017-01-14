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
import net.morilib.syaro.classfile.code.FConst;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.If;
import net.morilib.syaro.classfile.code.IfAcmp;
import net.morilib.syaro.classfile.code.IfIcmp;
import net.morilib.syaro.classfile.code.LConst;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class BinaryAST implements AST {

	public static enum Type implements OperationMnemonics {
		ILOR(null, null, null, null, OP_LOGICAL),
		ILAND(null, null, null, null, OP_LOGICAL),
		IEQ(null, null, null, null, COMPARISON),
		INE(null, null, null, null, COMPARISON),
		ILT(null, null, null, null, COMPARISON),
		IGE(null, null, null, null, COMPARISON),
		IGT(null, null, null, null, COMPARISON),
		ILE(null, null, null, null, COMPARISON),
		IBOR(Mnemonic.IOR, Mnemonic.LOR, null, null, OP_VALUE),
		IBXOR(Mnemonic.IXOR, Mnemonic.LXOR, null, null, OP_VALUE),
		IBAND(Mnemonic.IAND, Mnemonic.LAND, null, null, OP_VALUE),
		ISHR(Mnemonic.ISHR, Mnemonic.LSHR, null, null, OP_VALUE),
		ISHL(Mnemonic.ISHL, Mnemonic.LSHL, null, null, OP_VALUE),
		IADD(Mnemonic.IADD, Mnemonic.LADD, Mnemonic.DADD, Mnemonic.FADD, OP_VALUE),
		ISUB(Mnemonic.ISUB, Mnemonic.LSUB, Mnemonic.DSUB, Mnemonic.FSUB, OP_VALUE),
		IMUL(Mnemonic.IMUL, Mnemonic.LMUL, Mnemonic.DMUL, Mnemonic.FMUL, OP_VALUE),
		IDIV(Mnemonic.IDIV, Mnemonic.LDIV, Mnemonic.DDIV, Mnemonic.FDIV, OP_VALUE),
		IREM(Mnemonic.IREM, Mnemonic.LREM, Mnemonic.DREM, Mnemonic.FREM, OP_VALUE);

		private Mnemonic mnemonic;
		private Mnemonic mnemonicLong;
		private Mnemonic mnemonicDouble;
		private Mnemonic mnemonicFloat;
		private int type;

		public Mnemonic getMnemonic() {
			return mnemonic;
		}

		public Mnemonic getMnemonicLong() {
			return mnemonicLong;
		}

		public Mnemonic getMnemonicDouble() {
			return mnemonicDouble;
		}

		public Mnemonic getMnemonicFloat() {
			return mnemonicFloat;
		}

		private Type(Mnemonic m, Mnemonic l, Mnemonic d, Mnemonic f, int fl) {
			mnemonic = m;
			mnemonicLong = l;
			mnemonicDouble = d;
			mnemonicFloat = f;
			type = fl;
		}
	}

	private static final int OP_VALUE = 0;
	private static final int OP_LOGICAL = 1;
	private static final int COMPARISON = 2;

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
		if(!left.getASTType(functions, space).isPrimitive() ||
				!right.getASTType(functions, space).isPrimitive()) {
			throw new RuntimeException();
		} else if(type.mnemonic != null) {
			Utils.operatePrimitive(left, right, type, functions, space, code);
		} else {
			switch(type) {
			case ILOR:
				putCodeLogical(this, functions, space, code, Type.ILOR, If.Cond.NE);
				break;
			case ILAND:
				putCodeLogical(this, functions, space, code, Type.ILAND, If.Cond.EQ);
				break;
			case IEQ:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.EQ,
						IfAcmp.Cond.EQ);
				break;
			case INE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.NE,
						IfAcmp.Cond.NE);
				break;
			case ILT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LT, null);
				break;
			case ILE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LE, null);
				break;
			case IGT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.GT, null);
				break;
			case IGE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.GE, null);
				break;
			default:
				throw new RuntimeException();
			}
		}
	}

	private static void putCodeCmp(VariableType t, Code code) {
		if(t.isConversible(Primitive.INT)) {
			// do nothing
		} else if(t.equals(Primitive.LONG)) {
			code.addCode(new LConst(0));
			code.addCode(Mnemonic.LCMP);
		} else if(t.equals(Primitive.FLOAT)) {
			code.addCode(new FConst(0.0));
			code.addCode(Mnemonic.FCMPG);
		} else if(t.equals(Primitive.DOUBLE)) {
			code.addCode(new DConst(0.0));
			code.addCode(Mnemonic.DCMPG);
		}
	}

	private static void putCodeLogical(AST bnode,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code, Type tp, If.Cond cond) {
		List<Integer> labels = new ArrayList<Integer>();
		VariableType t;
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
			t = ((BinaryAST)node).left.getASTType(functions, space);
			if(!t.isPrimitive()) {
				throw new RuntimeException("type mismatch");
			}
			putCodeCmp(t, code);
			code.addCode(Mnemonic.DUP);
			labels.add(code.addCode(new If(cond)));
			code.addCode(Mnemonic.POP);
			node = ((BinaryAST)node).right;
		}
		node.putCode(functions, space, code);
		putCodeCmp(node.getASTType(functions, space), code);
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
			IfIcmp.Cond cond,
			IfAcmp.Cond acond) {
		int ifa, gta;
		IfIcmp _if;
		IfAcmp _af;
		Goto _gt;
		VariableType lp, rp;

		lp = bnode.left.getASTType(functions, space);
		rp = bnode.right.getASTType(functions, space);
		if(lp.isPrimitive() != rp.isPrimitive()) {
			throw new RuntimeException("type mismatch");
		} else if(!lp.isPrimitive() && !rp.isPrimitive() && acond == null) {
			throw new RuntimeException("type mismatch");
		}
		if(lp.isConversible(Primitive.INT) && rp.isConversible(Primitive.INT)) {
			bnode.left.putCode(functions, space, code);
			bnode.right.putCode(functions, space, code);
		} else if(lp.isConversible(Primitive.LONG) &&
				rp.isConversible(Primitive.LONG)) {
			bnode.left.putCode(functions, space, code);
			Utils.putConversionLong(lp, code);
			bnode.right.putCode(functions, space, code);
			Utils.putConversionLong(rp, code);
			code.addCode(Mnemonic.LCMP);
			code.addCode(new IConst(0));
		} else if(lp.isConversible(Primitive.FLOAT) &&
				rp.isConversible(Primitive.FLOAT)) {
			bnode.left.putCode(functions, space, code);
			Utils.putConversionFloat(lp, code);
			bnode.right.putCode(functions, space, code);
			Utils.putConversionFloat(rp, code);
			code.addCode(Mnemonic.FCMPG);
			code.addCode(new IConst(0));
		} else if(lp.isConversible(Primitive.DOUBLE) &&
				rp.isConversible(Primitive.DOUBLE)) {
			bnode.left.putCode(functions, space, code);
			Utils.putConversionDouble(lp, code);
			bnode.right.putCode(functions, space, code);
			Utils.putConversionDouble(rp, code);
			code.addCode(Mnemonic.DCMPG);
			code.addCode(new IConst(0));
		} else {
			bnode.left.putCode(functions, space, code);
			bnode.right.putCode(functions, space, code);
		}
		if(lp.isPrimitive() && rp.isPrimitive()) {
			_if = new IfIcmp(cond);
			ifa = code.addCode(_if);
			code.addCode(new IConst(0));
			_gt = new Goto();
			gta = code.addCode(_gt);
			_if.setOffset(code.getCurrentOffset(ifa));
			code.addCode(new IConst(1));
			_gt.setOffset(code.getCurrentOffset(gta));
		} else {
			_af = new IfAcmp(acond);
			ifa = code.addCode(_af);
			code.addCode(new IConst(0));
			_gt = new Goto();
			gta = code.addCode(_gt);
			_af.setOffset(code.getCurrentOffset(ifa));
			code.addCode(new IConst(1));
			_gt.setOffset(code.getCurrentOffset(gta));
		}
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		Primitive lp, rp;

		if(left.getASTType(functions, space).isPrimitive() &&
				right.getASTType(functions, space).isPrimitive()) {
			lp = (Primitive)left.getASTType(functions, space);
			rp = (Primitive)right.getASTType(functions, space);
			if(type.type == OP_LOGICAL || type.type == COMPARISON) {
				return Primitive.INT;
			} else if(lp.isConversible(Primitive.INT) &&
					rp.isConversible(Primitive.INT)) {
				return Primitive.INT;
			} else if(lp.isConversible(Primitive.LONG) &&
					rp.isConversible(Primitive.LONG)) {
				return Primitive.LONG;
			} else if(lp.isConversible(Primitive.FLOAT) &&
					rp.isConversible(Primitive.FLOAT)) {
				return Primitive.FLOAT;
			} else {
				return Primitive.DOUBLE;
			}
		} else if(type.type == COMPARISON) {
			return Primitive.INT;
		} else {
			throw new RuntimeException("type mismatch");
		}
	}

}
