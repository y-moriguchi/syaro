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
 * An abstract syntax tree of binary operators.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class BinaryAST implements AST {

	/**
	 * the type of binary operator.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Type implements OperationMnemonics {
		OR(null, null, null, null, OP_LOGICAL),
		AND(null, null, null, null, OP_LOGICAL),
		EQ(null, null, null, null, COMPARISON),
		NE(null, null, null, null, COMPARISON),
		LT(null, null, null, null, COMPARISON),
		GE(null, null, null, null, COMPARISON),
		GT(null, null, null, null, COMPARISON),
		LE(null, null, null, null, COMPARISON),
		XOR(Mnemonic.IXOR, Mnemonic.LXOR, null, null, OP_VALUE),
		SHR(Mnemonic.ISHR, Mnemonic.LSHR, null, null, OP_VALUE),
		SHL(Mnemonic.ISHL, Mnemonic.LSHL, null, null, OP_VALUE),
		ADD(Mnemonic.IADD, Mnemonic.LADD, Mnemonic.DADD, Mnemonic.FADD, OP_VALUE),
		SUB(Mnemonic.ISUB, Mnemonic.LSUB, Mnemonic.DSUB, Mnemonic.FSUB, OP_VALUE),
		MUL(Mnemonic.IMUL, Mnemonic.LMUL, Mnemonic.DMUL, Mnemonic.FMUL, OP_VALUE),
		DIV(Mnemonic.IDIV, Mnemonic.LDIV, Mnemonic.DDIV, Mnemonic.FDIV, OP_VALUE),
		REM(Mnemonic.IREM, Mnemonic.LREM, Mnemonic.DREM, Mnemonic.FREM, OP_VALUE);

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

	/**
	 * creates the AST of binary operator.
	 * 
	 * @param type the type of this AST
	 * @param left the AST of left value
	 * @param right the AST of right value
	 */
	public BinaryAST(Type type, AST left, AST right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	/**
	 * gets the type of this AST.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * gets the AST of left value.
	 */
	public AST getLeft() {
		return left;
	}

	/**
	 * gets the AST of right value.
	 */
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
			case OR:
				putCodeLogical(this, functions, space, code, Type.OR, If.Cond.NE);
				break;
			case AND:
				putCodeLogical(this, functions, space, code, Type.AND, If.Cond.EQ);
				break;
			case EQ:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.EQ,
						IfAcmp.Cond.EQ);
				break;
			case NE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.NE,
						IfAcmp.Cond.NE);
				break;
			case LT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LT, null);
				break;
			case LE:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.LE, null);
				break;
			case GT:
				putCodeCompare(this, functions, space, code, IfIcmp.Cond.GT, null);
				break;
			case GE:
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
