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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class represents class file attributes.
 * 
 * @author Yuichiro MORIGUCHI
 */
public abstract class Attribute implements ClassInfo {

	private ConstantUtf8 attributeName;

	/**
	 * constructs an attribute.
	 * 
	 * @param str the attribute name
	 */
	protected Attribute(String str) {
		attributeName = new ConstantUtf8(str);
	}

	/**
	 * get the attribute name.
	 */
	public ConstantUtf8 getAttributeName() {
		return attributeName;
	}

	/**
	 * gathers constant pools of this attribute.
	 * 
	 * @param gathered container of constant pools
	 */
	protected abstract void gatherConstantPoolAttribute(GatheredConstantPool gathered);

	/**
	 * gathers constant pools.
	 * 
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public final void gatherConstantPool(GatheredConstantPool gathered) {
		attributeName.gatherConstantPool(gathered);
		gatherConstantPoolAttribute(gathered);
	}

	/**
	 * generates a part of classfile about this attributes.
	 * 
	 * @param gathered container of constant pools
	 * @param ous an output stream
	 * @throws IOException
	 */
	protected abstract void generateAttributeCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException;

	/**
	 * generates a part of classfile.
	 * 
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	public final void generateCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		byte[] ba;

		generateAttributeCode(gathered, new DataOutputStream(bo));
		ba = bo.toByteArray();
		ous.writeShort(gathered.getIndex(attributeName));
		ous.writeInt(ba.length);
		ous.write(ba);
	}

}
