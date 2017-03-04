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
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.FStore;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Invokespecial;
import net.morilib.syaro.classfile.code.Invokevirtual;
import net.morilib.syaro.classfile.code.LStore;
import net.morilib.syaro.classfile.code.New;
import net.morilib.syaro.classfile.code.Putfield;
import net.morilib.syaro.test.BinaryAST.Type;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class Utils {

	public static void putConversionLong(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2L);
		}
	}

	public static void putConversionFloat(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2F);
		} else if(type.equals(Primitive.LONG)) {
			code.addCode(Mnemonic.L2F);
		}
	}

	public static void putConversionDouble(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2D);
		} else if(type.equals(Primitive.LONG)) {
			code.addCode(Mnemonic.L2D);
		} else if(type.equals(Primitive.FLOAT)) {
			code.addCode(Mnemonic.F2D);
		}
	}

	public static String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	public static void putCodeRef(VariableType v, Code code) {
		if(v.isPrimitive()) {
			if(v.equals(Primitive.BYTE)) {
				code.addCode(Mnemonic.BALOAD);
			} else if(v.equals(Primitive.CHAR)) {
				code.addCode(Mnemonic.CALOAD);
			} else if(v.equals(Primitive.SHORT)) {
				code.addCode(Mnemonic.SALOAD);
			} else if(v.equals(Primitive.INT)) {
				code.addCode(Mnemonic.IALOAD);
			} else if(v.equals(Primitive.LONG)) {
				code.addCode(Mnemonic.LALOAD);
			} else if(v.equals(Primitive.FLOAT)) {
				code.addCode(Mnemonic.FALOAD);
			} else if(v.equals(Primitive.DOUBLE)) {
				code.addCode(Mnemonic.DALOAD);
			}
		} else {
			code.addCode(Mnemonic.AALOAD);
		}
	}

	public static void putCodeArrayRef(AST left,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		ArrayIndexAST a;

		if(!(left instanceof ArrayIndexAST)) {
			return;
		} else {
			a = (ArrayIndexAST)left;
			while(true) {
				a.getArray().putCode(functions, space, code);
				a.getArrayIndex().putCode(functions, space, code);
				if(a.getArray() instanceof ArrayIndexAST) {
					code.addCode(Mnemonic.AALOAD);
					a = (ArrayIndexAST)a.getArray();
				} else {
					break;
				}
			}
		}
	}

	private static int getLocalIndex(LocalVariableSpace space, AST ast) {
		return space.getIndex(Utils.getVarName(ast));
	}

	private static void setVarNotRef(AST node,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType type;
		String name;
		int idx;

		idx = getLocalIndex(space, node);
		if(idx >= 0) {
			if(node.getASTType(functions, space).isConversible(Primitive.INT)) {
				code.addCode(new IStore(idx));
			} else if(node.getASTType(functions, space).equals(Primitive.LONG)) {
				code.addCode(new LStore(idx));
			} else if(node.getASTType(functions, space).equals(Primitive.FLOAT)) {
				code.addCode(new FStore(idx));
			} else if(node.getASTType(functions, space).equals(Primitive.DOUBLE)) {
				code.addCode(new DStore(idx));
			} else {
				code.addCode(new AStore(idx));
			}
		} else {
			code.addCode(new ALoad(0));
			if(node.getASTType(functions, space).equals(Primitive.DOUBLE)) {
				code.addCode(Mnemonic.DUP_X2);
				code.addCode(Mnemonic.POP);
			} else {
				code.addCode(Mnemonic.SWAP);
			}
			name = getVarName(node);
			type = functions.getGlobal(name);
			code.addCode(new Putfield(ConstantFieldref.getInstance(
					functions.getClassname(), name, type.getDescriptor())));
		}
	}

	public static void setVar(AST node,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType v;

		if(node instanceof ArrayIndexAST) {
			v = node.getASTType(functions, space);
			if(v.isPrimitive()) {
				if(v.equals(Primitive.BYTE)) {
					code.addCode(Mnemonic.BASTORE);
				} else if(v.equals(Primitive.CHAR)) {
					code.addCode(Mnemonic.CASTORE);
				} else if(v.equals(Primitive.SHORT)) {
					code.addCode(Mnemonic.SASTORE);
				} else if(v.equals(Primitive.INT)) {
					code.addCode(Mnemonic.IASTORE);
				} else if(v.equals(Primitive.LONG)) {
					code.addCode(Mnemonic.LASTORE);
				} else if(v.equals(Primitive.FLOAT)) {
					code.addCode(Mnemonic.FASTORE);
				} else if(v.equals(Primitive.DOUBLE)) {
					code.addCode(Mnemonic.DASTORE);
				}
			} else {
				code.addCode(Mnemonic.AASTORE);
			}
		} else {
			setVarNotRef(node, functions, space, code);
		}
	}

	public static void putDup(AST node, Code code) {
		if(node instanceof ArrayIndexAST) {
			code.addCode(Mnemonic.DUP_X2);
		} else {
			code.addCode(Mnemonic.DUP);
		}
	}

	public static void putDup2(AST node, Code code) {
		if(node instanceof ArrayIndexAST) {
			code.addCode(Mnemonic.DUP2_X2);
		} else {
			code.addCode(Mnemonic.DUP2);
		}
	}

	public static void operatePrimitive(AST left, AST right,
			OperationMnemonics type,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Primitive lp, rp;

		lp = (Primitive)left.getASTType(functions, space);
		rp = (Primitive)right.getASTType(functions, space);
		if(lp.isConversible(Primitive.INT) && rp.isConversible(Primitive.INT)) {
			left.putCode(functions, space, code);
			right.putCode(functions, space, code);
			code.addCode(type.getMnemonic());
		} else if(type.getMnemonicLong() == null) {
			throw new RuntimeException("type mismatch");
		} else if(lp.isConversible(Primitive.LONG) &&
				rp.isConversible(Primitive.INT)) {
			left.putCode(functions, space, code);
			Utils.putConversionLong(lp, code);
			right.putCode(functions, space, code);
			if(type.getMnemonicLong().equals(Mnemonic.LSHL) ||
					type.getMnemonicLong().equals(Mnemonic.LSHR)) {
				code.addCode(type.getMnemonicLong());
			} else {
				Utils.putConversionLong(rp, code);
				code.addCode(type.getMnemonicLong());
			}
		} else if(lp.isConversible(Primitive.LONG) &&
				rp.isConversible(Primitive.LONG)) {
			if(type.getMnemonicLong().equals(Mnemonic.LSHL) ||
					type.getMnemonicLong().equals(Mnemonic.LSHR)) {
				throw new RuntimeException("type mismatch");
			}
			left.putCode(functions, space, code);
			Utils.putConversionLong(lp, code);
			right.putCode(functions, space, code);
			Utils.putConversionLong(rp, code);
			code.addCode(type.getMnemonicLong());
		} else if(type.getMnemonicFloat() == null) {
			throw new RuntimeException("type mismatch");
		} else if(lp.isConversible(Primitive.FLOAT) &&
				rp.isConversible(Primitive.FLOAT)) {
			left.putCode(functions, space, code);
			Utils.putConversionFloat(lp, code);
			right.putCode(functions, space, code);
			Utils.putConversionFloat(rp, code);
			code.addCode(type.getMnemonicFloat());
		} else if(type.getMnemonicDouble() == null) {
			throw new RuntimeException("type mismatch");
		} else {
			left.putCode(functions, space, code);
			Utils.putConversionDouble(lp, code);
			right.putCode(functions, space, code);
			Utils.putConversionDouble(rp, code);
			code.addCode(type.getMnemonicDouble());
		}
	}

	private static void operateAddAppend(AST node, VariableType type,
			FunctionSpace functions, LocalVariableSpace space, Code code) {
		if(type.isPrimitive()) {
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/lang/StringBuffer", "append",
					"(" + type.getDescriptor() + ")Ljava/lang/StringBuffer;")));
		} else {
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/lang/StringBuffer", "append",
					"(Ljava/lang/Object;)Ljava/lang/StringBuffer;")));
		}
	}

	private static void operateAddLeft(AST node, FunctionSpace functions,
			LocalVariableSpace space, Code code) {
		VariableType nv;
		BinaryAST ba;

		if(!(node instanceof BinaryAST) ||
				!((BinaryAST)node).getType().equals(Type.IADD)) {
			nv = node.getASTType(functions, space);
			node.putCode(functions, space, code);
			operateAddAppend(node, nv, functions, space, code);
		} else {
			ba = (BinaryAST)node;
			operateAddLeft(ba.getLeft(), functions, space, code);
			nv = ba.getRight().getASTType(functions, space);
			ba.getRight().putCode(functions, space, code);
			operateAddAppend(ba.getRight(), nv, functions, space, code);
		}
	}

	public static void operateAdd(AST left, AST right, FunctionSpace functions,
			LocalVariableSpace space, Code code) {
		VariableType lv, rv, nv;

		lv = left.getASTType(functions, space);
		rv = right.getASTType(functions, space);
		if(lv.isPrimitive() && rv.isPrimitive()) {
			operatePrimitive(left, right, Type.IADD, functions, space, code);
		} else {
			code.addCode(new New(ConstantClass.getInstance("java/lang/StringBuffer")));
			code.addCode(Mnemonic.DUP);
			code.addCode(new Invokespecial(ConstantMethodref.getInstance(
					"java/lang/StringBuffer", "<init>", "()V")));
			operateAddLeft(left, functions, space, code);
			nv = right.getASTType(functions, space);
			right.putCode(functions, space, code);
			operateAddAppend(right, nv, functions, space, code);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/lang/StringBuffer", "toString", "()Ljava/lang/String;")));
		}
	}

}
