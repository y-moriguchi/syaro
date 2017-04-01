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

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantInteger;
import net.morilib.syaro.classfile.code.LdcW;

/**
 * An abstract syntax tree for integer constant.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class IntegerAST implements AST {

	private int value;

	/**
	 * creates an AST for the value.
	 * 
	 * @param value the value
	 */
	public IntegerAST(int value) {
		this.value = value;
	}

	/**
	 * gets the value.
	 */
	public int getValue() {
		return value;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		code.addCode(new LdcW(ConstantInteger.getInstance(value)));
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return Primitive.INT;
	}

}
