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
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.code.ANewArray;
import net.morilib.syaro.classfile.code.NewArray;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class NewArrayAST implements AST {

	private ArrayType type;
	private AST size;

	public NewArrayAST(ArrayType type, AST size) {
		this.type = type;
		this.size = size;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		VariableType t;

		if(!size.getASTType(functions, space).isConversible(Primitive.INT)) {
			throw new RuntimeException("size must be int");
		} else if(type.isPrimitive()) {
			throw new RuntimeException("can not new primitives");
		}
		size.putCode(functions, space, code);
		t = ((ArrayType)type).getElement();
		if(t.equals(Primitive.BYTE)) {
			code.addCode(new NewArray(NewArray.Type.BYTE));
		} else if(t.equals(Primitive.CHAR)) {
			code.addCode(new NewArray(NewArray.Type.CHAR));
		} else if(t.equals(Primitive.SHORT)) {
			code.addCode(new NewArray(NewArray.Type.SHORT));
		} else if(t.equals(Primitive.INT)) {
			code.addCode(new NewArray(NewArray.Type.INT));
		} else if(t.equals(Primitive.LONG)) {
			code.addCode(new NewArray(NewArray.Type.LONG));
		} else if(t.equals(Primitive.FLOAT)) {
			code.addCode(new NewArray(NewArray.Type.FLOAT));
		} else if(t.equals(Primitive.DOUBLE)) {
			code.addCode(new NewArray(NewArray.Type.DOUBLE));
		} else {
			code.addCode(new ANewArray(new ConstantClass(t.getDescriptor())));
		}
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return type;
	}

}
