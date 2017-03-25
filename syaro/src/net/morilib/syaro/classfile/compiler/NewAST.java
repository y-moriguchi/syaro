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
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Invokespecial;
import net.morilib.syaro.classfile.code.New;

/**
 * An abstract syntax tree of new operator.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class NewAST implements AST {

	private VariableType type;
	private List<AST> arguments = new ArrayList<AST>();

	/**
	 * constructs an AST for new operator.
	 * 
	 * @param callee method AST
	 */
	public NewAST(VariableType type) {
		this.type = type;
	}

	/**
	 * gets the type of new operator.
	 */
	public VariableType getType() {
		return type;
	}

	/**
	 * gets the arguments of new operator.
	 */
	public List<AST> getArguments() {
		return new ArrayList<AST>(arguments);
	}

	/**
	 * adds a argument.
	 * 
	 * @param arg argument
	 */
	public void addArgument(AST arg) {
		arguments.add(arg);
	}

	private SyaroConstructor findConstructor(SyaroClass cls,
			FunctionSpace functions,
			LocalVariableSpace space) {
		SyaroConstructor con;
		List<VariableType> vtyp;

		vtyp = new ArrayList<VariableType>();
		for(AST ast : arguments) {
			vtyp.add(ast.getASTType(functions, space));
		}
		con = cls.findConstructor(vtyp);
		if(con == null) {
			throw new RuntimeException("constructor not found");
		}
		return con;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		SyaroConstructor con;
		String name;

		name = type.getClassName(functions);
		if(name == null) {
			throw new RuntimeException("cannot instantiate");
		}
		con = findConstructor(functions.getClass(type), functions, space);
		code.addCode(new New(ConstantClass.getInstance(name)));
		code.addCode(Mnemonic.DUP);
		for(AST a : arguments) {
			a.putCode(functions, space, code);
		}
		code.addCode(new Invokespecial(ConstantMethodref.getInstance(
				name, "<init>", con.getDescriptor(functions))));
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return type;
	}

}
