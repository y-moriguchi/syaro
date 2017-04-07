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

import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.FStore;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Invokeinterface;
import net.morilib.syaro.classfile.code.Invokespecial;
import net.morilib.syaro.classfile.code.Invokestatic;
import net.morilib.syaro.classfile.code.Invokevirtual;
import net.morilib.syaro.classfile.code.LStore;
import net.morilib.syaro.classfile.code.New;
import net.morilib.syaro.classfile.code.Putfield;
import net.morilib.syaro.classfile.code.Putstatic;
import net.morilib.syaro.classfile.compiler.BinaryAST.Type;

/**
 * An utility class of ASTs.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Utils {

	public static final SyaroClass STRING = new SyaroClass(String.class);

	public static final SyaroClass OBJECT = new SyaroClass(Object.class);

	/**
	 * puts the codes of converting to long.
	 * 
	 * @param type the type
	 * @param code the container of codes
	 */
	public static void putConversionLong(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2L);
		}
	}

	/**
	 * puts the codes of converting to float.
	 * 
	 * @param type the type
	 * @param code the container of codes
	 */
	public static void putConversionFloat(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2F);
		} else if(type.equals(Primitive.LONG)) {
			code.addCode(Mnemonic.L2F);
		}
	}

	/**
	 * puts the codes of converting to double.
	 * 
	 * @param type the type
	 * @param code the container of codes
	 */
	public static void putConversionDouble(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2D);
		} else if(type.equals(Primitive.LONG)) {
			code.addCode(Mnemonic.L2D);
		} else if(type.equals(Primitive.FLOAT)) {
			code.addCode(Mnemonic.F2D);
		}
	}

	/**
	 * gets the symbol name of the AST.
	 * 
	 * @param ast the AST
	 */
	public static String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new SemanticsException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	/**
	 * puts the codes of referring an array.
	 * 
	 * @param v the type
	 * @param code the container of codes.
	 */
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

	/**
	 * puts the codes of referring an array.
	 * 
	 * @param v the type
	 * @param code the container of codes.
	 */
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
		SyaroClass cls;
		SyaroField fld;
		AST al, ar;
		String name;
		int idx;

		if(node instanceof DotAST) {
			al = ((DotAST)node).getLeft();
			ar = ((DotAST)node).getRight();
			if(!(ar instanceof SymbolAST)) {
				throw new SemanticsException("illegal field specifition");
			}
			name = ((SymbolAST)ar).getName();
			if(isInstance(al, functions, space)) {
				al.putCode(functions, space, code);
				if(node.getASTType(functions, space).equals(Primitive.DOUBLE)) {
					code.addCode(Mnemonic.DUP_X2);
					code.addCode(Mnemonic.POP);
				} else {
					code.addCode(Mnemonic.SWAP);
				}
				cls = functions.getClass(al.getASTType(functions, space));
				if((fld = cls.getField(name)) == null) {
					throw new SemanticsException("field " + name + " is not found");
				} else if(fld.isStatic()) {
					throw new SemanticsException("field " + name + " is static");
				}
				type = fld.getType();
				code.addCode(new Putfield(ConstantFieldref.getInstance(
						cls.getName(), name, type.getDescriptor(functions))));
			} else {
				cls = functions.getClass(Utils.getTypeFromName(
						Utils.getName(al)));
				if((fld = cls.getField(name)) == null) {
					throw new SemanticsException("field " + name + " is not found");
				} else if(!fld.isStatic()) {
					throw new SemanticsException("field " + name + " is not static");
				}
				type = fld.getType();
				code.addCode(new Putstatic(ConstantFieldref.getInstance(
						cls.getName(), name, type.getDescriptor(functions))));
			}
		} else {
			name = Utils.getVarName(node);
			if(functions.isConstant(name)) {
				throw new SemanticsException("symbol " + name + " is constant");
			}
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
				name = getVarName(node);
				throw new SemanticsException(
						"local variable " + name + " is not defined");
			}
		}
	}

	/**
	 * puts the codes of storing value to a variable or an element of an array.
	 * 
	 * @param node the node
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 */
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

	/**
	 * puts dup instruction corresponds to the AST.
	 * 
	 * @param node the AST
	 * @param code the container of codes
	 */
	public static void putDup(AST node, Code code) {
		if(node instanceof ArrayIndexAST) {
			code.addCode(Mnemonic.DUP_X2);
		} else {
			code.addCode(Mnemonic.DUP);
		}
	}

	/**
	 * puts dup2 instruction corresponds to the AST.
	 * 
	 * @param node the AST
	 * @param code the container of codes
	 */
	public static void putDup2(AST node, Code code) {
		if(node instanceof ArrayIndexAST) {
			code.addCode(Mnemonic.DUP2_X2);
		} else {
			code.addCode(Mnemonic.DUP2);
		}
	}

	/**
	 * puts the code of the binary operator.
	 * 
	 * @param left the left value
	 * @param right the right value
	 * @param type the type of the binary operator
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 */
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
			throw new SemanticsException("type mismatch");
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
				throw new SemanticsException("type mismatch");
			}
			left.putCode(functions, space, code);
			Utils.putConversionLong(lp, code);
			right.putCode(functions, space, code);
			Utils.putConversionLong(rp, code);
			code.addCode(type.getMnemonicLong());
		} else if(type.getMnemonicFloat() == null) {
			throw new SemanticsException("type mismatch");
		} else if(lp.isConversible(Primitive.FLOAT) &&
				rp.isConversible(Primitive.FLOAT)) {
			left.putCode(functions, space, code);
			Utils.putConversionFloat(lp, code);
			right.putCode(functions, space, code);
			Utils.putConversionFloat(rp, code);
			code.addCode(type.getMnemonicFloat());
		} else if(type.getMnemonicDouble() == null) {
			throw new SemanticsException("type mismatch");
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
					"(" + type.getDescriptor(functions) + ")Ljava/lang/StringBuffer;")));
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

	/**
	 * puts the code of addition.
	 * 
	 * @param left the left value
	 * @param right the right value
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 */
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

	/**
	 * convert reflection type to type representation of this program.
	 * 
	 * @param cls reflection type
	 * @return type representation of this program
	 */
	public static VariableType convertType(Class<?> cls) {
		if(cls.equals(Byte.TYPE)) {
			return Primitive.BYTE;
		} else if(cls.equals(Character.TYPE)) {
			return Primitive.CHAR;
		} else if(cls.equals(Double.TYPE)) {
			return Primitive.DOUBLE;
		} else if(cls.equals(Float.TYPE)) {
			return Primitive.FLOAT;
		} else if(cls.equals(Integer.TYPE)) {
			return Primitive.INT;
		} else if(cls.equals(Long.TYPE)) {
			return Primitive.LONG;
		} else if(cls.equals(Short.TYPE)) {
			return Primitive.SHORT;
		} else if(cls.equals(Object.class)) {
			return QuasiPrimitive.OBJECT;
		} else if(cls.equals(String.class)) {
			return QuasiPrimitive.STRING;
		} else {
			return new SymbolType(cls.getSimpleName());
		}
	}

	/**
	 * gets the name of symbol AST.
	 * 
	 * @param ast AST
	 * @return the name
	 */
	public static String getName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new SemanticsException("not a function");
		}
		return ((SymbolAST)ast).getName();
	}

	private static void putCodeInvokeArgument(List<VariableType> fvar,
			List<AST> arguments,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Primitive ap, fp;
		VariableType at;
		AST a;

		for(int i = 0; i < arguments.size(); i++) {
			a = arguments.get(i);
			at = arguments.get(i).getASTType(functions, space);
			a.putCode(functions, space, code);
			if(!at.isConversible(fvar.get(i))) {
				throw new SemanticsException("type mismatch");
			} else if(fvar.get(i).isPrimitive()) {
				ap = (Primitive)at;
				fp = (Primitive)fvar.get(i);
				if(fp.isConversible(Primitive.INT)) {
					// do nothing
				} else if(fp.isConversible(Primitive.LONG)) {
					Utils.putConversionLong(ap, code);
				} else if(fp.isConversible(Primitive.FLOAT)) {
					Utils.putConversionFloat(ap, code);
				} else {
					Utils.putConversionDouble(ap, code);
				}
			}
		}
	}

	/**
	 * puts Java VM codes for invoke a static method.
	 * 
	 * @param typeName type name of the class which has method to invoke
	 * @param methodName method name
	 * @param methodDescriptor method descriptor
	 * @param fvar type definitions of method
	 * @param arguments ASTs of arguments
	 * @param functions function name space
	 * @param space local variable name space
	 * @param code instruction container
	 */
	public static void putCodeInvokeStaic(String typeName,
			String methodName,
			String methodDescriptor,
			List<VariableType> fvar,
			List<AST> arguments,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;
		String name, desc;

		if(fvar.size() != arguments.size()) {
			throw new SemanticsException("arity is not the same");
		}
		putCodeInvokeArgument(fvar, arguments, functions, space, code);
		code.addCode(new Invokestatic(ConstantMethodref.getInstance(
				typeName, methodName, methodDescriptor)));
	}

	/**
	 * puts Java VM codes for invoke a non static method.
	 * 
	 * @param typeName type name of the class which has method to invoke
	 * @param methodName method name
	 * @param methodDescriptor method descriptor
	 * @param fvar type definitions of method
	 * @param arguments ASTs of arguments
	 * @param functions function name space
	 * @param space local variable name space
	 * @param code instruction container
	 */
	public static void putCodeInvokeVirtual(String typeName,
			String methodName,
			String methodDescriptor,
			List<VariableType> fvar,
			List<AST> arguments,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;
		String name;
		Primitive ap, fp;
		VariableType at;
		AST a;

		if(fvar.size() != arguments.size()) {
			throw new SemanticsException("arity is not the same");
		}
		putCodeInvokeArgument(fvar, arguments, functions, space, code);
		code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
				typeName, methodName, methodDescriptor)));
	}

	/**
	 * puts Java VM codes for invoke a interface method.
	 * 
	 * @param typeName type name of the class which has method to invoke
	 * @param methodName method name
	 * @param methodDescriptor method descriptor
	 * @param fvar type definitions of method
	 * @param arguments ASTs of arguments
	 * @param functions function name space
	 * @param space local variable name space
	 * @param code instruction container
	 */
	public static void putCodeInvokeInterface(String typeName,
			String methodName,
			String methodDescriptor,
			List<VariableType> fvar,
			List<AST> arguments,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;
		String name;
		Primitive ap, fp;
		VariableType at;
		AST a;

		if(fvar.size() != arguments.size()) {
			throw new SemanticsException("arity is not the same");
		}
		putCodeInvokeArgument(fvar, arguments, functions, space, code);
		code.addCode(new Invokeinterface(ConstantMethodref.getInstance(
				typeName, methodName, methodDescriptor)));
	}

	static String getDescriptor(FunctionSpace functions,
			VariableType returnType,
			List<VariableType> argumentTypes) {
		StringBuilder b = new StringBuilder();

		b.append("(");
		for(VariableType v : argumentTypes) {
			b.append(v.getDescriptor(functions));
		}
		b.append(")");
		b.append(returnType.getDescriptor(functions));
		return b.toString();
	}

	static VariableType getTypeFromName(String name) {
		if(name.equals("String")) {
			return QuasiPrimitive.STRING;
		} else if(name.equals("Object")) {
			return QuasiPrimitive.OBJECT;
		} else {
			return new SymbolType(name);
		}
	}

	/**
	 * returns true if the ast is an instance.
	 * 
	 * @param ast ast
	 * @param functions function space
	 * @param space local variable namespace
	 * @return
	 */
	public static boolean isInstance(AST ast, FunctionSpace functions,
			LocalVariableSpace space) {
		try {
			ast.getASTType(functions, space);
			return true;
		} catch(UndefinedSymbolException e) {
			if(ast instanceof SymbolAST) {
				return false;
			} else {
				throw e;
			}
		}
	}

}
