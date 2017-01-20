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
 * This class represents an Java VM instruction ifnonnull.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class IfNonnull extends Mnemonic {

	private short offset;

	/**
	 * constructs an ifnonnull instruction.
	 */
	public IfNonnull() {
		super(199);
	}

	/**
	 * gets the offset address.
	 */
	public short getOffset() {
		return offset;
	}

	/**
	 * sets the offset address.
	 */
	public void setOffset(int offset) {
		this.offset = (short)offset;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeShort(offset);
	}

	@Override
	protected int getByteLength() {
		return 3;
	}

}