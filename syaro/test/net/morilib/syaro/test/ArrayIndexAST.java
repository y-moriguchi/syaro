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

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class ArrayIndexAST implements AST {

	private AST array;
	private AST arrayIndex;

	public ArrayIndexAST(AST a, AST i) {
		array = a;
		arrayIndex = i;
	}

	public AST getArray() {
		return array;
	}

	public AST getArrayIndex() {
		return arrayIndex;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		VariableType v;

		v = getASTType(functions, space);
		array.putCode(functions, space, code);
		arrayIndex.putCode(functions, space, code);
		Utils.putCodeRef(v, code);
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		VariableType v, w;

		v = array.getASTType(functions, space);
		w = arrayIndex.getASTType(functions, space);
		if(!w.equals(Primitive.INT)) {
			throw new RuntimeException("type of arrayindex is integer");
		} else if(v instanceof ArrayType) {
			return ((ArrayType)v).getElement();
		} else {
			throw new RuntimeException("type of array is not array");
		}
	}

}
