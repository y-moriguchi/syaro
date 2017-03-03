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
 * This class represents an Java VM instruction tableswitch.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class TableSwitch extends Mnemonic {

	private int byteLength;
	private int low;
	private int high;
	private int[] offsets;
	private int defaultAddress;

	/**
	 * constructs a tableswitch instruction.
	 */
	public TableSwitch(int low, int high) {
		super(170);
		if(high < low) {
			throw new IllegalArgumentException();
		}
		this.low = low;
		this.high = high;
		this.offsets = new int[high - low + 1];
	}

	/**
	 * puts offset.
	 * 
	 * @param key key
	 * @param offset offset
	 * @throws IllegalArgumentException throw if key is not exist
	 */
	public void putOffset(int key, int offset) {
		if(key < low || key > high) {
			throw new IndexOutOfBoundsException();
		}
		offsets[key - low] = offset;
	}

	/**
	 * set default offset.
	 * 
	 * @param offset offset
	 */
	public void setDefaultOffset(int offset) {
		defaultAddress = offset;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		for(int i = 0; i < (byteLength - 1) % 4; i++) {
			ous.writeByte(0);
		}
		ous.writeInt(defaultAddress);
		ous.writeInt(low);
		ous.writeInt(high);
		for(int i = 0; i < offsets.length; i++) {
			ous.writeInt(offsets[i]);
		}
	}

	@Override
	protected int getByteLength() {
		if(byteLength < 0) {
			throw new IllegalStateException();
		}
		return byteLength;
	}

	@Override
	protected int computeByteLength(int current) {
		int pad;

		pad = (4 - ((current + 1) % 4)) % 4;
		byteLength = offsets.length * 4 + 12 + 1 + pad;
		return byteLength;
	}

}
