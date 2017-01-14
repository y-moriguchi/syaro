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
import net.morilib.syaro.classfile.ConstantLong;
import net.morilib.syaro.classfile.code.Ldc2W;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class LongAST implements AST {

	private long value;

	public LongAST(long value) {
		this.value = value;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		code.addCode(new Ldc2W(new ConstantLong(value)));
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return Primitive.LONG;
	}

}
