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
 * This class represents an Java VM instruction if_acmp&gt;cond&lt;.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class IfAcmp extends Mnemonic {

	/**
	 * This enum represents a condition of the instruction if_acmp.
	 * 
	 * @author Yuichiro MORIGUCHI
	 */
	public static enum Cond {

		/**
		 * represents equals.
		 */
		EQ(165),

		/**
		 * represents not equals.
		 */
		NE(166);
		private int opcode;
		private Cond(int c) {
			opcode = c;
		}
	}

	private short offset;

	/**
	 * constructs an if_icmp&gt;cond&lt; instruction.
	 * 
	 * @param cond condition
	 * @param offset offset address to execute
	 */
	public IfAcmp(Cond cond) {
		super(cond.opcode);
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