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
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.FStore;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Putfield;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class Utils {

	public static void putConversionFloat(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2F);
		}
	}

	public static void putConversionDouble(VariableType type, Code code) {
		if(type.equals(Primitive.INT)) {
			code.addCode(Mnemonic.I2D);
		} else if(type.equals(Primitive.FLOAT)) {
			code.addCode(Mnemonic.F2D);
		}
	}

	public static String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	public static void setVar(AST node,
			FunctionSpace functions,
			LocalVariableSpace space,
			int idx, Code code) {
		VariableType type;
		String name;

		if(idx >= 0) {
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(new IStore(idx));
			} else if(node.getASTType(functions, space).equals(Primitive.FLOAT)) {
				code.addCode(new FStore(idx));
			} else if(node.getASTType(functions, space).equals(Primitive.DOUBLE)) {
				code.addCode(new DStore(idx));
			} else {
				throw new RuntimeException();
			}
		} else {
			code.addCode(new ALoad(0));
			if(node.getASTType(functions, space).equals(Primitive.DOUBLE)) {
				code.addCode(Mnemonic.DUP_X2);
				code.addCode(Mnemonic.POP);
			} else {
				code.addCode(Mnemonic.SWAP);
			}
			name = getVarName(node);
			type = functions.getGlobal(name);
			code.addCode(new Putfield(new ConstantFieldref(
					functions.getClassname(), name, type.getDescriptor())));
		}
	}

}
