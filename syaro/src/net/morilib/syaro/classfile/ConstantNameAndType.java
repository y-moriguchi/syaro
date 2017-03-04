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
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a constant pool of name and type.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantNameAndType extends ConstantPool {

	private static Map<ConstantNameAndType, ConstantNameAndType> flyweight =
			new HashMap<ConstantNameAndType, ConstantNameAndType>();
	private ConstantUtf8 name;
	private ConstantUtf8 descriptor;

	private ConstantNameAndType(String name, String desc) {
		super(CONSTANT_NameAndType);
		this.name = ConstantUtf8.getInstance(name);
		this.descriptor = ConstantUtf8.getInstance(desc);
	}

	/**
	 * gets a constant pool of name and type.
	 * 
	 * @param name name
	 * @param desc descriptor
	 */
	public static ConstantNameAndType getInstance(String name, String desc) {
		ConstantNameAndType res, obj;

		obj = new ConstantNameAndType(name, desc);
		if((res = flyweight.get(obj)) == null) {
			res = obj;
			flyweight.put(res, res);
		}
		return res;
	}

	/**
	 * gets the name.
	 */
	public ConstantUtf8 getName() {
		return name;
	}

	/**
	 * gets the descriptor.
	 */
	public ConstantUtf8 getDescriptor() {
		return descriptor;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
		name.gatherConstantPool(gathered);
		descriptor.gatherConstantPool(gathered);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generatePoolCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(gathered.getIndex(name));
		ous.writeShort(gathered.getIndex(descriptor));
	}

	@Override
	public int hashCode() {
		int h = 17;

		h = 37 * name.hashCode() + h;
		h = 37 * descriptor.hashCode() + h;
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		ConstantNameAndType r;

		if(obj != null && obj instanceof ConstantNameAndType) {
			r = (ConstantNameAndType)obj;
			return name.equals(r.name) &&
					descriptor.equals(r.descriptor);
		}
		return false;
	}

}
