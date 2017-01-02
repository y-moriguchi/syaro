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

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		FunctionDefinition func;
		String name, desc;

		name = getName(callee);
		func = getFunction(functions, callee);
		desc = func.getDescriptor();
		code.addCode(new ALoad(0));
		for(AST a : arguments) {
			a.putCode(functions, space, code);
		}
		code.addCode(new Invokevirtual(new ConstantMethodref(
				functions.getClassname(), name, desc)));
	}

}
