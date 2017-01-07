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
 * This class represents Java VM instructions.
 * 
 * @author Yuichiro MORIGUCHI
 */
public abstract class Mnemonic implements ClassInfo {

	private static class Single extends Mnemonic {

		public Single(int code) {
			super(code);
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

	/**
	 * The instruction return.
	 */
	public static Mnemonic RETURN = new Single(177);

	/**
	 * The instruction ior.
	 */
	public static Mnemonic IOR = new Single(128);

	/**
	 * The instruction ixor.
	 */
	public static Mnemonic IXOR = new Single(130);

	/**
	 * The instruction iand.
	 */
	public static Mnemonic IAND = new Single(126);

	/**
	 * The instruction ishl.
	 */
	public static Mnemonic ISHL = new Single(120);

	/**
	 * The instruction ishr.
	 */
	public static Mnemonic ISHR = new Single(122);

	/**
	 * The instruction iadd.
	 */
	public static Mnemonic IADD = new Single(96);

	/**
	 * The instruction isub.
	 */
	public static Mnemonic ISUB = new Single(100);

	/**
	 * The instruction imul.
	 */
	public static Mnemonic IMUL = new Single(104);

	/**
	 * The instruction idiv.
	 */
	public static Mnemonic IDIV = new Single(108);

	/**
	 * The instruction irem.
	 */
	public static Mnemonic IREM = new Single(112);

	/**
	 * The instruction ineg.
	 */
	public static Mnemonic INEG = new Single(116);

	/**
	 * The instruction nop.
	 */
	public static Mnemonic NOP = new Single(0);

	/**
	 * The instruction dup.
	 */
	public static Mnemonic DUP = new Single(89);

	/**
	 * The instruction pop.
	 */
	public static Mnemonic POP = new Single(87);

	/**
	 * The instruction ireturn.
	 */
	public static Mnemonic IRETURN = new Single(172);

	/**
	 * The instruction swap.
	 */
	public static Mnemonic SWAP = new Single(95);

	/**
	 * The instruction i2d.
	 */
	public static Mnemonic I2D = new Single(135);

	/**
	 * The instruction dadd.
	 */
	public static Mnemonic DADD = new Single(99);

	/**
	 * The instruction dsub.
	 */
	public static Mnemonic DSUB = new Single(103);

	/**
	 * The instruction dmul.
	 */
	public static Mnemonic DMUL = new Single(107);

	/**
	 * The instruction ddiv.
	 */
	public static Mnemonic DDIV = new Single(111);

	/**
	 * The instruction drem.
	 */
	public static Mnemonic DREM = new Single(115);

	/**
	 * The instruction dneg.
	 */
	public static Mnemonic DNEG = new Single(119);

	/**
	 * The instruction dcmpg.
	 */
	public static Mnemonic DCMPG = new Single(152);

	/**
	 * The instruction dcmpl.
	 */
	public static Mnemonic DCMPL = new Single(151);

	/**
	 * The instruction dup2.
	 */
	public static Mnemonic DUP2 = new Single(92);

	/**
	 * The instruction dup_x2.
	 */
	public static Mnemonic DUP_X2 = new Single(91);

	/**
	 * The instruction dreturn.
	 */
	public static Mnemonic DRETURN = new Single(175);

	/**
	 * The instruction d2i.
	 */
	public static Mnemonic D2I = new Single(142);

	private byte opcode;

	/**
	 * constructs an instruction.
	 * 
	 * @param opcode opcode of instruction
	 */
	protected Mnemonic(int opcode) {
		this.opcode = (byte)opcode;
	}

	/**
	 * gets the opcode of this instruction.
	 */
	public byte getOpcode() {
		return opcode;
	}

	/**
	 * generates a part of classfile about this instruction.
	 * 
	 * @param gathered container of constant pools
	 * @param ous the output stream
	 * @throws IOException
	 */
	protected abstract void generateMnemonicCode(
			GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException;

	/**
	 * gets the byte length of this instruction.
	 * 
	 * @return byte length
	 */
	protected abstract int getByteLength();

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	public final void generateCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeByte(opcode);
		generateMnemonicCode(gathered, ous);
	}

}
