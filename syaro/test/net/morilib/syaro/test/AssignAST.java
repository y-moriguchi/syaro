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
public class AssignAST implements AST {

	public static enum Type {
		BOR(Mnemonic.IOR, null, null),
		BXOR(Mnemonic.IXOR, null, null),
		BAND(Mnemonic.IAND, null, null),
		SHR(Mnemonic.ISHR, null, null),
		SHL(Mnemonic.ISHL, null, null),
		ADD(Mnemonic.IADD, Mnemonic.DADD, Mnemonic.FADD),
		SUB(Mnemonic.ISUB, Mnemonic.DSUB, Mnemonic.FSUB),
		MUL(Mnemonic.IMUL, Mnemonic.DMUL, Mnemonic.FMUL),
		DIV(Mnemonic.IDIV, Mnemonic.DDIV, Mnemonic.FDIV),
		REM(Mnemonic.IREM, Mnemonic.DREM, Mnemonic.FREM);
		private Mnemonic mnemonic;
		private Mnemonic mnemonicDouble;
		private Mnemonic mnemonicFloat;
		private Type(Mnemonic m, Mnemonic d, Mnemonic f) {
			mnemonic = m;
			mnemonicDouble = d;
			mnemonicFloat = f;
		}
	}

	private Mnemonic operate;
	private Mnemonic operateDouble;
	private Mnemonic operateFloat;
	private AST left, right;

	public AssignAST(Mnemonic operate, AST left, AST right) {
		this.operate = operate;
		this.left = left;
		this.right = right;
	}

	public AssignAST(Type op, AST left, AST right) {
		if(op != null) {
			this.operate = op.mnemonic;
			this.operateDouble = op.mnemonicDouble;
			this.operateDouble = op.mnemonicFloat;
		} else {
			this.operate = null;
			this.operateDouble = null;
			this.operateFloat = null;
		}
		this.left = left;
		this.right = right;
	}

	public Mnemonic getOperate() {
		return operate;
	}

	public AST getLeft() {
		return left;
	}

	public AST getRight() {
		return right;
	}

	private void putCodeReference(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		if(operate != null) {
			throw new RuntimeException("not operate instance");
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
		if(operate == null) {
			right.putCode(functions, space, code);
		} else {
			if(lp.isConversible(Primitive.INT) && rp.isConversible(Primitive.INT)) {
				left.putCode(functions, space, code);
				right.putCode(functions, space, code);
				code.addCode(operate);
			} else if(operateFloat == null) {
				throw new RuntimeException("type mismatch");
			} else if(lp.isConversible(Primitive.FLOAT) &&
					rp.isConversible(Primitive.FLOAT)) {
				left.putCode(functions, space, code);
				Utils.putConversionFloat(lp, code);
				right.putCode(functions, space, code);
				Utils.putConversionFloat(rp, code);
				code.addCode(operateFloat);
			} else if(operateDouble == null) {
				throw new RuntimeException("type mismatch");
			} else {
				left.putCode(functions, space, code);
				Utils.putConversionDouble(lp, code);
				right.putCode(functions, space, code);
				Utils.putConversionDouble(rp, code);
				code.addCode(operateDouble);
			}
		}
		if(!rp.isConversible(lp)) {
			throw new RuntimeException("type mismatch");
		} else if(lp.equals(Primitive.INT)) {
			Utils.putDup(left, code);
		} else if(lp.equals(Primitive.FLOAT)) {
			Utils.putConversionFloat(rp, code);
			Utils.putDup(left, code);
		} else {
			Utils.putConversionDouble(rp, code);
			Utils.putDup2(left, code);
		}
		Utils.setVar(left, functions, space, code);
	}


	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType l, r;

		l = left.getASTType(functions, space);
		r = right.getASTType(functions, space);
		if(!l.isConversible(r)) {
			throw new RuntimeException("type mismatch");
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
