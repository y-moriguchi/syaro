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
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.Invokevirtual;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class CallAST implements AST {

	private AST callee;
	private List<AST> arguments = new ArrayList<AST>();

	public CallAST(AST callee) {
		this.callee = callee;
	}

	public void addArgument(AST arg) {
		arguments.add(arg);
	}

	private FunctionDefinition getFunction(FunctionSpace space, AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a function");
		}
		return space.getDefinition(((SymbolAST)ast).getName());
	}

	private String getName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a function");
		}
		return ((SymbolAST)ast).getName();
	}

	public boolean isSubroutine(FunctionSpace functions) {
		FunctionDefinition func;

		func = getFunction(functions, callee);
		return func.getReturnType().equals(Primitive.VOID);
	}

	public void putCodeSimple(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;
		List<VariableType> fvar;
		String name, desc;
		Primitive ap, fp;
		VariableType at;
		AST a;

		name = getName(callee);
		func = getFunction(functions, callee);
		desc = func.getDescriptor();
		code.addCode(new ALoad(0));
		fvar = func.getArgumentTypes();
		if(fvar.size() != arguments.size()) {
			throw new RuntimeException("arity is not the same");
		}
		for(int i = 0; i < arguments.size(); i++) {
			a = arguments.get(i);
			at = arguments.get(i).getASTType(functions, space);
			a.putCode(functions, space, code);
			if(!at.isConversible(fvar.get(i))) {
				throw new RuntimeException("type mismatch");
			} else if(fvar.get(i).isPrimitive()) {
				ap = (Primitive)at;
				fp = (Primitive)fvar.get(i);
				if(fp.isConversible(Primitive.INT)) {
					// do nothing
				} else if(fp.isConversible(Primitive.FLOAT)) {
					Utils.putConversionFloat(ap, code);
				} else {
					Utils.putConversionDouble(ap, code);
				}
			}
		}
		code.addCode(new Invokevirtual(new ConstantMethodref(
				functions.getClassname(), name, desc)));
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;

		func = getFunction(functions, callee);
		if(func.getReturnType().equals(Primitive.VOID)) {
			throw new RuntimeException("cannot call subroutine");
		}
		putCodeSimple(functions, space, code);
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		FunctionDefinition func;

		func = getFunction(functions, callee);
		return func.getReturnType();
	}

}
