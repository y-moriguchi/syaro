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
package net.morilib.syaro.classfile.compiler;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * An abstract syntax tree for statements of assignment.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class AssignAST implements AST, OperationMnemonics {

	/**
	 * the type of assignment operator.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Type {
		BOR(Mnemonic.IOR, Mnemonic.LOR, null, null),
		BXOR(Mnemonic.IXOR, Mnemonic.LXOR, null, null),
		BAND(Mnemonic.IAND, Mnemonic.LAND, null, null),
		SHR(Mnemonic.ISHR, Mnemonic.LSHR, null, null),
		SHL(Mnemonic.ISHL, Mnemonic.LSHL, null, null),
		ADD(Mnemonic.IADD, Mnemonic.LADD, Mnemonic.DADD, Mnemonic.FADD),
		SUB(Mnemonic.ISUB, Mnemonic.LSUB, Mnemonic.DSUB, Mnemonic.FSUB),
		MUL(Mnemonic.IMUL, Mnemonic.LMUL, Mnemonic.DMUL, Mnemonic.FMUL),
		DIV(Mnemonic.IDIV, Mnemonic.LDIV, Mnemonic.DDIV, Mnemonic.FDIV),
		REM(Mnemonic.IREM, Mnemonic.LREM, Mnemonic.DREM, Mnemonic.FREM);
		private Mnemonic mnemonic;
		private Mnemonic mnemonicLong;
		private Mnemonic mnemonicDouble;
		private Mnemonic mnemonicFloat;
		private Type(Mnemonic m, Mnemonic l, Mnemonic d, Mnemonic f) {
			mnemonic = m;
			mnemonicLong = l;
			mnemonicDouble = d;
			mnemonicFloat = f;
		}
	}

	private Mnemonic mnemonic;
	private Mnemonic mnemonicLong;
	private Mnemonic mnemonicDouble;
	private Mnemonic mnemonicFloat;
	private AST left, right;

	/**
	 * creates AST of assignment.
	 * 
	 * @param mnemonic operator, if mnemonic is null this represents simple assignment
	 * @param name the name of variable
	 * @param right the AST of right value
	 */
	public AssignAST(Mnemonic mnemonic, AST left, AST right) {
		this.mnemonic = mnemonic;
		this.left = left;
		this.right = right;
	}

	/**
	 * creates AST of assignment.
	 * 
	 * @param op operator, if op is null this represents simple assignment
	 * @param name the name of variable
	 * @param right the AST of right value
	 */
	public AssignAST(Type op, AST left, AST right) {
		if(op != null) {
			this.mnemonic = op.mnemonic;
			this.mnemonicLong = op.mnemonicLong;
			this.mnemonicDouble = op.mnemonicDouble;
			this.mnemonicFloat = op.mnemonicFloat;
		} else {
			this.mnemonic = null;
			this.mnemonicLong = null;
			this.mnemonicDouble = null;
			this.mnemonicFloat = null;
		}
		this.left = left;
		this.right = right;
	}

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

	private void putCodeReference(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		if(mnemonic != null) {
			throw new SemanticsException("not mnemonic instance");
		} else {
			Utils.putCodeArrayRef(left, functions, space, code);
			right.putCode(functions, space, code);
			Utils.putDup(left, code);
			Utils.setVar(left, functions, space, code);
		}
	}

	private void putCodePrimitive(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Primitive lp, rp;

		lp = (Primitive)left.getASTType(functions, space);
		rp = (Primitive)right.getASTType(functions, space);
		Utils.putCodeArrayRef(left, functions, space, code);
		if(mnemonic == null) {
			right.putCode(functions, space, code);
		} else {
			Utils.operatePrimitive(left, right, this, functions, space, code);
		}
		if(!rp.isConversible(lp)) {
			throw new SemanticsException("type mismatch");
		} else if(lp.isConversible(Primitive.INT)) {
			Utils.putDup(left, code);
		} else if(lp.equals(Primitive.LONG)) {
			Utils.putConversionLong(rp, code);
			Utils.putDup2(left, code);
		} else if(lp.equals(Primitive.FLOAT)) {
			Utils.putConversionFloat(rp, code);
			Utils.putDup(left, code);
		} else {
			Utils.putConversionDouble(rp, code);
			Utils.putDup2(left, code);
		}
		Utils.setVar(left, functions, space, code);
	}

	private void putCodeString(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Utils.putCodeArrayRef(left, functions, space, code);
		if(mnemonic == null) {
			right.putCode(functions, space, code);
		} else if(mnemonic.equals(Type.ADD)) {
			Utils.operateAdd(left, right, functions, space, code);
		} else {
			throw new SemanticsException("cannot operate string except +");
		}
		Utils.putDup(left, code);
		Utils.setVar(left, functions, space, code);
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType l, r;

		l = left.getASTType(functions, space);
		r = right.getASTType(functions, space);
		if(l.equals(QuasiPrimitive.STRING)) {
			putCodeString(functions, space, code);
		} else if(!l.isConversible(r)) {
			throw new SemanticsException("type mismatch");
		} else if(!l.isPrimitive() || !r.isPrimitive()) {
			putCodeReference(functions, space, code);
		} else {
			putCodePrimitive(functions, space, code);
		}
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return left.getASTType(functions, space);
	}

}
