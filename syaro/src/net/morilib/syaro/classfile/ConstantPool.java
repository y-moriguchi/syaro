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
 * This class represents a constant pool.
 * 
 * @author Yuichiro MORIGUCHI
 */
public abstract class ConstantPool implements ClassInfo {

	public static final byte CONSTANT_Class = 7;
	public static final byte CONSTANT_Fieldref = 9;
	public static final byte CONSTANT_Methodref = 10;
	public static final byte CONSTANT_InterfaceMethodref = 11;
	public static final byte CONSTANT_String = 8;
	public static final byte CONSTANT_Integer = 3;
	public static final byte CONSTANT_Float = 4;
	public static final byte CONSTANT_Long = 5;
	public static final byte CONSTANT_Double = 6;
	public static final byte CONSTANT_NameAndType = 12;
	public static final byte CONSTANT_Utf8 = 1;
	public static final byte CONSTANT_MethodHandle = 15;
	public static final byte CONSTANT_MethodType = 16;
	public static final byte CONSTANT_InvokeDynamic = 18;

	private byte tag;

	/**
	 * constructs a constant pool.
	 * 
	 * @param tag tag number
	 */
	protected ConstantPool(byte tag) {
		this.tag = tag;
	}

	/**
	 * gets the tag number.
	 */
	public byte getTag() {
		return tag;
	}

	/**
	 * generates a part of classfile about this constant pool.
	 * 
	 * @param gathered container of constant pools
	 * @param ous the output stream
	 * @throws IOException
	 */
	protected abstract void generatePoolCode(
			GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException;

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	public final void generateCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeByte(tag);
		generatePoolCode(gathered, ous);
	}

}
