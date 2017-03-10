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
package net.morilib.bcc;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.ALoad;

/**
 * An abstract syntax tree of a symbol.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SymbolAST implements AST {

	private String name;

	/**
	 * creates an AST of a symbol.
	 * 
	 * @param name the name of the symbol
	 */
	public SymbolAST(String name) {
		this.name = name;
	}

	/**
	 * gets the name of the symbol.
	 */
	public String getName() {
		return name;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		int idx = space.getIndex(name);

		if(idx >= 0) {
			code.addCode(new ALoad(idx));
		} else {
			throw new RuntimeException("undefined variable: " + name);
		}
	}

}
