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

	/**
	 * The instruction fadd.
	 */
	public static Mnemonic FADD = new Single(98);

	/**
	 * The instruction fsub.
	 */
	public static Mnemonic FSUB = new Single(102);

	/**
	 * The instruction fmul.
	 */
	public static Mnemonic FMUL = new Single(106);

	/**
	 * The instruction fdiv.
	 */
	public static Mnemonic FDIV = new Single(110);

	/**
	 * The instruction frem.
	 */
	public static Mnemonic FREM = new Single(114);

	/**
	 * The instruction fneg.
	 */
	public static Mnemonic FNEG = new Single(118);

	/**
	 * The instruction i2f.
	 */
	public static Mnemonic I2F = new Single(134);

	/**
	 * The instruction f2d.
	 */
	public static Mnemonic F2D = new Single(141);

	/**
	 * The instruction fcmpg.
	 */
	public static Mnemonic FCMPG = new Single(150);

	/**
	 * The instruction fcmpl.
	 */
	public static Mnemonic FCMPL = new Single(149);

	/**
	 * The instruction d2f.
	 */
	public static Mnemonic D2F = new Single(144);

	/**
	 * The instruction d2l.
	 */
	public static Mnemonic D2L = new Single(143);

	/**
	 * The instruction f2i.
	 */
	public static Mnemonic F2I = new Single(139);

	/**
	 * The instruction f2l.
	 */
	public static Mnemonic F2L = new Single(140);

	/**
	 * The instruction freturn.
	 */
	public static Mnemonic FRETURN = new Single(174);

	/**
	 * The instruction pop2.
	 */
	public static Mnemonic POP2 = new Single(88);

	/**
	 * The instruction baload.
	 */
	public static Mnemonic BALOAD = new Single(51);

	/**
	 * The instruction saload.
	 */
	public static Mnemonic SALOAD = new Single(53);

	/**
	 * The instruction iaload.
	 */
	public static Mnemonic IALOAD = new Single(46);

	/**
	 * The instruction laload.
	 */
	public static Mnemonic LALOAD = new Single(47);

	/**
	 * The instruction faload.
	 */
	public static Mnemonic FALOAD = new Single(48);

	/**
	 * The instruction daload.
	 */
	public static Mnemonic DALOAD = new Single(49);

	/**
	 * The instruction aaload.
	 */
	public static Mnemonic AALOAD = new Single(50);

	/**
	 * The instruction aconst_null.
	 */
	public static Mnemonic ACONST_NULL = new Single(1);

	/**
	 * The instruction dup2_x2.
	 */
	public static Mnemonic DUP2_X2 = new Single(94);

	/**
	 * The instruction bastore.
	 */
	public static Mnemonic BASTORE = new Single(84);

	/**
	 * The instruction sastore.
	 */
	public static Mnemonic SASTORE = new Single(86);

	/**
	 * The instruction iastore.
	 */
	public static Mnemonic IASTORE = new Single(79);

	/**
	 * The instruction lastore.
	 */
	public static Mnemonic LASTORE = new Single(80);

	/**
	 * The instruction fastore.
	 */
	public static Mnemonic FASTORE = new Single(81);

	/**
	 * The instruction dastore.
	 */
	public static Mnemonic DASTORE = new Single(82);

	/**
	 * The instruction aastore.
	 */
	public static Mnemonic AASTORE = new Single(83);

	/**
	 * The instruction areturn.
	 */
	public static Mnemonic ARETURN = new Single(176);

	/**
	 * The instruction lreturn.
	 */
	public static Mnemonic LRETURN = new Single(173);

	/**
	 * The instruction i2b.
	 */
	public static Mnemonic I2B = new Single(145);

	/**
	 * The instruction i2s.
	 */
	public static Mnemonic I2S = new Single(147);

	/**
	 * The instruction i2l.
	 */
	public static Mnemonic I2L = new Single(133);

	/**
	 * The instruction l2i.
	 */
	public static Mnemonic L2I = new Single(136);

	/**
	 * The instruction l2f.
	 */
	public static Mnemonic L2F = new Single(137);

	/**
	 * The instruction l2d.
	 */
	public static Mnemonic L2D = new Single(138);

	/**
	 * The instruction ladd.
	 */
	public static Mnemonic LADD = new Single(97);

	/**
	 * The instruction lsub.
	 */
	public static Mnemonic LSUB = new Single(101);

	/**
	 * The instruction lmul.
	 */
	public static Mnemonic LMUL = new Single(105);

	/**
	 * The instruction ldiv.
	 */
	public static Mnemonic LDIV = new Single(109);

	/**
	 * The instruction lrem.
	 */
	public static Mnemonic LREM = new Single(113);

	/**
	 * The instruction lneg.
	 */
	public static Mnemonic LNEG = new Single(117);

	/**
	 * The instruction lor.
	 */
	public static Mnemonic LOR = new Single(129);

	/**
	 * The instruction lxor.
	 */
	public static Mnemonic LXOR = new Single(131);

	/**
	 * The instruction land.
	 */
	public static Mnemonic LAND = new Single(127);

	/**
	 * The instruction lshl.
	 */
	public static Mnemonic LSHL = new Single(121);

	/**
	 * The instruction lshr.
	 */
	public static Mnemonic LSHR = new Single(123);

	/**
	 * The instruction lcmp.
	 */
	public static Mnemonic LCMP = new Single(148);

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
