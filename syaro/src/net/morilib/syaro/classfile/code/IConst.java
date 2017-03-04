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
package net.morilib.syaro.classfile.code;

import java.io.DataOutputStream;
import java.io.IOException;

import net.morilib.syaro.classfile.GatheredConstantPool;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * This class represents a Java VM instruction iconst.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class IConst extends Mnemonic {

	/**
	 * constructs an iconst instruction.
	 * 
	 * @param value int value between -1 and 5
	 */
	public IConst(int value) {
		super(getOpcode(value));
	}

	private static int getOpcode(int v) {
		if(v < -1 || v > 5) {
			throw new IllegalArgumentException("value must be between -1 and 5");
		}
		return 3 + v;
	}

	/**
	 * gets the offset address.
	 */
	public int getValue() {
		return getOpcode() - 3;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
	}

	@Override
	protected int getByteLength() {
		return 1;
	}

}