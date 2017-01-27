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
package net.morilib.patricia;

import java.util.ArrayList;
import java.util.List;

import net.morilib.syaro.classfile.Code;

/**
 * An abstract syntax tree for statement of a block.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class BlockAST implements SAST {

	private List<SAST> block;

	/**
	 * creates statement AST of a block.
	 * 
	 * @param list the list of statements
	 */
	public BlockAST(List<SAST> list) {
		block = new ArrayList<SAST>(list);
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices) {
		for(SAST s : block) {
			s.putCode(functions, space,
					code, breakIndices, continueAddress, continueIndices);
		}
	}

}
