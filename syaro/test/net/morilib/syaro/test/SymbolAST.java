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
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.DLoad;
import net.morilib.syaro.classfile.code.Getfield;
import net.morilib.syaro.classfile.code.ILoad;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class SymbolAST implements AST {

	private String name;

	public SymbolAST(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		VariableType type;
		int idx = space.getIndex(name);

		if(idx >= 0) {
			type = space.getType(name);
			if(type.equals(Primitive.INT)) {
				code.addCode(new ILoad(idx));
			} else {
				code.addCode(new DLoad(idx));
			}
		} else {
			type = functions.getGlobal(name);
			code.addCode(new ALoad(0));
			code.addCode(new Getfield(new ConstantFieldref(
					functions.getClassname(), name, type.getDescriptor())));
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		int idx = space.getIndex(name);

		if(idx >= 0) {
			return space.getType(name);
		} else {
			return functions.getGlobal(name);
		}
	}

}
