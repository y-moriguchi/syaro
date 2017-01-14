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
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.FStore;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Putfield;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class Utils {

	public static void putConversionFloat(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2F);
		}
	}

	public static void putConversionDouble(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2D);
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
			if(v.equals(Primitive.INT)) {
				code.addCode(Mnemonic.IALOAD);
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
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(new IStore(idx));
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
			code.addCode(new Putfield(new ConstantFieldref(
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
				if(v.equals(Primitive.INT)) {
					code.addCode(Mnemonic.IASTORE);
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

}
