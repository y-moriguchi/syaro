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
 * This class represents a Java VM instruction bipush.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Bipush extends Mnemonic {

	private byte value;

	/**
	 * constructs an bipush instruction.
	 * 
	 * @param value byte value
	 */
	public Bipush(byte value) {
		super(16);
		this.value = value;
	}

	/**
	 * gets the byte value.
	 */
	public int getValue() {
		return value;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeByte(value);
	}

	@Override
	protected int getByteLength() {
		return 2;
	}

}