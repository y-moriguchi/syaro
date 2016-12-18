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
package net.morilib.syaro.classfile;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public abstract class Mnemonic implements ClassInfo {

	private static class Single extends Mnemonic {

		public Single(int code) {
			super(code);
		}

		/* (non-Javadoc)
		 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
		 */
		@Override
		public void gatherConstantPool(GatheredConstantPool gathered) {
		}

		/* (non-Javadoc)
		 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
		 */
		@Override
		protected void generateMnemonicCode(GatheredConstantPool gathered,
				DataOutputStream ous) throws IOException {
		}

	}

	public static Mnemonic RETURN = new Single(177);
	public static Mnemonic IOR = new Single(128);
	public static Mnemonic IXOR = new Single(130);
	public static Mnemonic IAND = new Single(126);
	public static Mnemonic ISHL = new Single(120);
	public static Mnemonic ISHR = new Single(122);
	public static Mnemonic IADD = new Single(96);
	public static Mnemonic ISUB = new Single(100);
	public static Mnemonic IMUL = new Single(104);
	public static Mnemonic IDIV = new Single(108);
	public static Mnemonic IREM = new Single(112);
	public static Mnemonic INEG = new Single(116);

	private byte opcode;

	public Mnemonic(int opcode) {
		this.opcode = (byte)opcode;
	}

	protected abstract void generateMnemonicCode(
			GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException;

	@Override
	public final void generateCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeByte(opcode);
		generateMnemonicCode(gathered, ous);
	}

}
