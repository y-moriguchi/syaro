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

import java.util.ArrayList;
import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Getfield;
import net.morilib.syaro.classfile.code.Getstatic;

/**
 * 
 * @author Yuichiro MORIGUCHI
 */
public class DotAST implements AST {

	private AST left;
	private AST right;

	public DotAST(AST left, AST right) {
		this.left = left;
		this.right = right;
	}

	public AST getLeft() {
		return left;
	}

	public AST getRight() {
		return right;
	}

	private boolean isArrayLength(FunctionSpace functions,
			LocalVariableSpace space) {
		VariableType type;
		String name;

		try {
			type = left.getASTType(functions, space);
			if(!(right instanceof SymbolAST)) {
				return false;
			}
			name = Utils.getName(right);
			return type instanceof ArrayType && name.equals("length");
		} catch(UndefinedSymbolException e) {
			return false;
		}
	}

	private SyaroClass getClassdef(FunctionSpace functions,
			LocalVariableSpace space) {
		VariableType type;

		type = left.getASTType(functions, space);
		return functions.getClass(type);
	}

	private SyaroMethod findMethod(SyaroClass cls,
			FunctionSpace functions,
			LocalVariableSpace space) {
		String name;
		SyaroMethod mth;
		List<VariableType> vtyp;

		name = Utils.getName(((CallAST)right).getCallee());
		vtyp = new ArrayList<VariableType>();
		for(AST ast : ((CallAST)right).getArguments()) {
			vtyp.add(ast.getASTType(functions, space));
		}
		mth = cls.findMethod(name, vtyp);
		if(mth == null) {
			throw new RuntimeException("method not found " + name);
		}
		return mth;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		SyaroClass cls;
		SyaroMethod mth;
		SyaroField fld;

		if(right instanceof CallAST) {
			if(Utils.isInstance(left, functions, space)) {
				cls = getClassdef(functions, space);
				mth = findMethod(cls, functions, space);
				left.putCode(functions, space, code);
				if(mth.isStatic()) {
					throw new RuntimeException("method is static");
				} else if(cls.isInterface()) {
					Utils.putCodeInvokeInterface(cls.getName(),
							mth.getName(),
							mth.getDescriptor(functions),
							mth.getArgumentTypes(),
							((CallAST)right).getArguments(),
							functions, space, code);
				} else {
					Utils.putCodeInvokeVirtual(cls.getName(),
							mth.getName(),
							mth.getDescriptor(functions),
							mth.getArgumentTypes(),
							((CallAST)right).getArguments(),
							functions, space, code);
				}
			} else {
				cls = functions.getClass(Utils.getTypeFromName(
						Utils.getName(left)));
				mth = findMethod(cls, functions, space);
				if(mth.isStatic()) {
					Utils.putCodeInvokeStaic(cls.getName(),
							mth.getName(),
							mth.getDescriptor(functions),
							mth.getArgumentTypes(),
							((CallAST)right).getArguments(),
							functions, space, code);
				} else {
					throw new RuntimeException("method is not static");
				}
			}
		} else if(right instanceof SymbolAST) {
			if(isArrayLength(functions, space)) {
				left.putCode(functions, space, code);
				code.addCode(Mnemonic.ARRAYLENGTH);
			} else if(Utils.isInstance(left, functions, space)) {
				cls = getClassdef(functions, space);
				fld = cls.getField(Utils.getName(right));
				left.putCode(functions, space, code);
				if(fld.isStatic()) {
					throw new RuntimeException("method is static");
				} else {
					code.addCode(new Getfield(ConstantFieldref.getInstance(
							cls.getName(), fld.getName(),
							fld.getType().getDescriptor(functions))));
				}
			} else {
				cls = functions.getClass(Utils.getTypeFromName(
						Utils.getName(left)));
				fld = cls.getField(Utils.getName(right));
				if(fld.isStatic()) {
					code.addCode(new Getstatic(ConstantFieldref.getInstance(
							cls.getName(), fld.getName(),
							fld.getType().getDescriptor(functions))));
				} else {
					throw new RuntimeException("method is not static");
				}
			}
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		SyaroClass cls;
		SyaroMethod mth;
		SyaroField fld;

		if(right instanceof CallAST) {
			if(Utils.isInstance(left, functions, space)) {
				cls = getClassdef(functions, space);
			} else {
				cls = functions.getClass(Utils.getTypeFromName(
						Utils.getName(left)));
			}
			mth = findMethod(cls, functions, space);
			return mth.getReturnType();
		} else if(right instanceof SymbolAST) {
			if(isArrayLength(functions, space)) {
				return Primitive.INT;
			} else if(Utils.isInstance(left, functions, space)) {
				cls = getClassdef(functions, space);
			} else {
				cls = functions.getClass(Utils.getTypeFromName(
						Utils.getName(left)));
			}
			fld = cls.getField(Utils.getName(right));
			return fld.getType();
		} else {
			throw new RuntimeException();
		}
	}

}
