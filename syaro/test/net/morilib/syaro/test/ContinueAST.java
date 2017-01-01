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

import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class ContinueAST implements SAST {

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.AST#putCode(net.morilib.syaro.test.LocalVariableSpace, net.morilib.syaro.classfile.Code)
	 */
	@Override
	public void putCode(LocalVariableSpace space, Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices) {
		Goto _gt;

		if(continueAddress >= 0) {
			_gt = new Goto();
			_gt.setOffset(continueAddress - code.getCurrentAddress());
			code.addCode(_gt);
		} else if(continueIndices != null) {
			continueIndices.add(code.addCode(new Goto()));
		} else {
			throw new RuntimeException("invalid continue");
		}
	}

}
