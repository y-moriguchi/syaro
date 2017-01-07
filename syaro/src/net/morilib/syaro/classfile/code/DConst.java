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
 * This class represents a Java VM instruction dconst.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class DConst extends Mnemonic {

	/**
	 * constructs a dconst instruction.
	 * 
	 * @param cond condition
	 * @param value double value which must be 0.0 or 1.0
	 */
	public DConst(double value) {
		super(getOpcode(value));
	}

	private static int getOpcode(double v) {
		if(v != 0.0 && v != 1.0) {
			throw new IllegalArgumentException("value must be 0.0 or 1.0");
		} else if(v == 0.0) {
			return 14;
		} else {
			return 15;
		}
	}

	/**
	 * gets the value.
	 */
	public double getValue() {
		return getOpcode() == 14 ? 0.0 : 1.0;
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