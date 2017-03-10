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

/**
 * An interface of code generator of binary operator.
 * 
 * @author Yuichiro MORIGUCHI
 */
public interface BinaryOperator {

	/**
	 * generates codes of binary operator.
	 * 
	 * @param left  left AST node
	 * @param right right AST node
	 * @param functions function namespace
	 * @param space local variable namespace
	 * @param code container of instructions
	 */
	public void putCode(AST left, AST right, FunctionSpace functions,
			LocalVariableSpace space, Code code);

}
