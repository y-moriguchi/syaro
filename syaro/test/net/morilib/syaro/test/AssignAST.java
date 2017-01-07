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
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Putfield;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class AssignAST implements AST {

	public static enum Type {
		BOR(Mnemonic.IOR, null),
		BXOR(Mnemonic.IXOR, null),
		BAND(Mnemonic.IAND, null),
		SHR(Mnemonic.ISHR, null),
		SHL(Mnemonic.ISHL, null),
		ADD(Mnemonic.IADD, Mnemonic.DADD),
		SUB(Mnemonic.ISUB, Mnemonic.DSUB),
		MUL(Mnemonic.IMUL, Mnemonic.DMUL),
		DIV(Mnemonic.IDIV, Mnemonic.DDIV),
		REM(Mnemonic.IREM, Mnemonic.DREM);
		private Mnemonic mnemonic;
		private Mnemonic mnemonicDouble;
		private Type(Mnemonic m, Mnemonic d) {
			mnemonic = m;
			mnemonicDouble = d;
		}
	}

	private Mnemonic operate;
	private Mnemonic operateDouble;
	private AST left, right;

	public AssignAST(Mnemonic operate, AST left, AST right) {
		this.operate = operate;
		this.left = left;
		this.right = right;
	}

	public AssignAST(Type op, AST left, AST right) {
		this.operate = op.mnemonic;
		this.operateDouble = op.mnemonicDouble;
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

	private String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	private int getLocalIndex(LocalVariableSpace space, AST ast) {
		return space.getIndex(getVarName(ast));
	}

	private void setVar(FunctionSpace functions, LocalVariableSpace space,
			int idx, Code code) {
		VariableType type;
		String name;

		if(idx >= 0) {
			if(left.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(new IStore(idx));
			} else {
				code.addCode(new DStore(idx));
			}
		} else {
			code.addCode(new ALoad(0));
			if(left.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(Mnemonic.SWAP);
			} else {
				code.addCode(Mnemonic.DUP_X2);
				code.addCode(Mnemonic.POP);
			}
			name = getVarName(left);
			type = functions.getGlobal(name);
			code.addCode(new Putfield(new ConstantFieldref(
					functions.getClassname(), name, type.getDescriptor())));
		}
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		int idx;

		idx = getLocalIndex(space, left);
		if(operate == null) {
			right.putCode(functions, space, code);
		} else {
			if(left.getASTType(functions, space).equals(Primitive.INT) &&
					right.getASTType(functions, space).equals(Primitive.INT)) {
				left.putCode(functions, space, code);
				right.putCode(functions, space, code);
				code.addCode(operate);
			} else if(operateDouble == null) {
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
				code.addCode(operateDouble);
			}
		}
		if(left.getASTType(functions, space).equals(Primitive.INT)) {
			if(right.getASTType(functions, space).equals(Primitive.DOUBLE)) {
				throw new RuntimeException("double value can not assign to int");
			}
			code.addCode(Mnemonic.DUP);
		} else {
			if(right.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(Mnemonic.I2D);
			}
			code.addCode(Mnemonic.DUP2);
		}
		setVar(functions, space, idx, code);
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return left.getASTType(functions, space);
	}

}
