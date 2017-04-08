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
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.DLoad;
import net.morilib.syaro.classfile.code.FLoad;
import net.morilib.syaro.classfile.code.ILoad;
import net.morilib.syaro.classfile.code.LLoad;

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
		VariableType type;
		int idx = space.getIndex(name);

		if(name.equals("null")) {
			code.addCode(Mnemonic.ACONST_NULL);
		} else if(name.equals("this")) {
			if(space.isStatic()) {
				throw new SemanticsException("this method is static");
			}
			code.addCode(new ALoad(0));
		} else if(functions.isConstant(name)) {
			code.addCode(Mnemonic.pushInt(functions.getConstant(name)));
		} else if(idx >= 0) {
			type = space.getType(name);
			if(type.isConversible(Primitive.INT)) {
				code.addCode(new ILoad(idx));
			} else if(type.equals(Primitive.LONG)) {
				code.addCode(new LLoad(idx));
			} else if(type.equals(Primitive.FLOAT)) {
				code.addCode(new FLoad(idx));
			} else if(type.equals(Primitive.DOUBLE)) {
				code.addCode(new DLoad(idx));
			} else {
				code.addCode(new ALoad(idx));
			}
		} else {
			throw new UndefinedSymbolException(name);
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		int idx = space.getIndex(name);

		if(name.equals("null")) {
			return QuasiPrimitive.OBJECT;
		} else if(name.equals("this")) {
			if(space.isStatic()) {
				throw new SemanticsException("this method is static");
			}
			return new SymbolType(functions.getClassname());
		} else if(functions.isConstant(name)) {
			return Primitive.INT;
		} else if(idx >= 0) {
			return space.getType(name);
		} else {
			throw new UndefinedSymbolException(name);
		}
	}

}
