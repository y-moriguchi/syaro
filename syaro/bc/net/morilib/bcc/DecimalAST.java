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
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.ConstantString;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Invokespecial;
import net.morilib.syaro.classfile.code.LdcW;
import net.morilib.syaro.classfile.code.New;

/**
 * An abstract syntax tree for decimal constant.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class DecimalAST implements AST {

	private String value;

	/**
	 * creates an AST for the value.
	 * 
	 * @param value the value
	 */
	public DecimalAST(String value) {
		this.value = value;
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		code.addCode(new New(ConstantClass.getInstance("java/math/BigDecimal")));
		code.addCode(Mnemonic.DUP);
		code.addCode(new LdcW(ConstantString.getInstance(value)));
		code.addCode(new Invokespecial(ConstantMethodref.getInstance(
				"java/math/BigDecimal", "<init>", "(Ljava/lang/String;)V")));
	}

}
