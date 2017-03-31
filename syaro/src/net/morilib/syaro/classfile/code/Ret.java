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
 * This class represents a Java VM instruction ret.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Ret extends Mnemonic {

	private short index;

	/**
	 * constructs an ret instruction.
	 * The index must be between 0 and 255.
	 * 
	 * @param index variable index
	 */
	public Ret(int index) {
		super(169);
		if(index > 255) {
			throw new IllegalArgumentException("argument too large");
		}
		this.index = (short)index;
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
		ous.writeByte((byte)index);
	}

	@Override
	protected int getByteLength() {
		return 2;
	}

}