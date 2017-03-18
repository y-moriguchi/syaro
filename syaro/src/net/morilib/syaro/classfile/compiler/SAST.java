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

import java.util.List;

import net.morilib.syaro.classfile.Code;

/**
 * An interface represents abstract syntax trees of statement.
 * 
 * @author Yuichiro MORIGUCHI
 */
public interface SAST {

	/**
	 * puts codes corresponds to the statement AST.
	 * 
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 * @param breakIndices code indices to jump by break statement
	 * @param continueAddress
	 * @param continueIndices
	 */
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices);

}
