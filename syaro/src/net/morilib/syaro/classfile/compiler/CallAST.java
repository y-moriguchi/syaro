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

/**
 * An abstract syntax tree of method call.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class CallAST implements AST {

	private AST callee;
	private List<AST> arguments = new ArrayList<AST>();

	/**
	 * constructs an AST for method call.
	 * 
	 * @param callee method AST
	 */
	public CallAST(AST callee) {
		this.callee = callee;
	}

	public AST getCallee() {
		return callee;
	}

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

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		throw new RuntimeException("class or instance modifier needed");
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		throw new RuntimeException("class or instance modifier needed");
	}

}
