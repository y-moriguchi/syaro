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
 * This class represents a Java VM instruction lstore.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class LStore extends Mnemonic {

	private short index;

	/**
	 * constructs an lstore instruction.
	 * If the index is greater then 255 then the wide instruction is
	 * automatically added.
	 * 
	 * @param cond condition
	 * @param offset offset address to execute
	 */
	public LStore(int index) {
		super(getOpcode(index));
		this.index = (short)index;
	}

	private static int getOpcode(int index) {
		if(index < 0 || index > 0xffff) {
			throw new IllegalArgumentException("index out of range");
		}
		return index < 256 ? 55 : 196;
	}

	/**
	 * gets the offset address.
	 */
	public short getIndex() {
		return index;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		if(index < 256) {
			ous.writeByte((byte)index);
		} else {
			ous.writeByte((byte)55);
			ous.writeShort(index);
		}
	}

	@Override
	protected int getByteLength() {
		return index < 256 ? 2 : 4;
	}

}