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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class MethodInfo implements ClassInfo {

	public static final short ACC_PUBLIC = 0x0001;
	public static final short ACC_PRIVATE = 0x0002;
	public static final short ACC_PROTECTED = 0x0004;
	public static final short ACC_STATIC = 0x0008;
	public static final short ACC_FINAL = 0x0010;
	public static final short ACC_SYNCHRONIZED = 0x0020;
	public static final short ACC_BRIDGE = 0x0040;
	public static final short ACC_VARARGS = 0x0080;
	public static final short ACC_NATIVE = 0x0100;
	public static final short ACC_ABSTRACT = 0x0400;
	public static final short ACC_STRICT = 0x0800;
	public static final short ACC_SYNTHETIC = 0x1000;

	private short accessFlags;
	private ConstantUtf8 name;
	private ConstantUtf8 descriptor;
	private List<Attribute> attributes = new ArrayList<Attribute>();

	public MethodInfo(String name, String desc) {
		this.name = new ConstantUtf8(name);
		this.descriptor = new ConstantUtf8(desc);
	}

	public short getAccessFlags() {
		return accessFlags;
	}

	public void setAccessFlags(int accessFlags) {
		this.accessFlags = (short)accessFlags;
	}

	public ConstantUtf8 getName() {
		return name;
	}

	public ConstantUtf8 getDescriptor() {
		return descriptor;
	}

	public void addAttribute(Attribute attr) {
		attributes.add(attr);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		name.gatherConstantPool(gathered);
		descriptor.gatherConstantPool(gathered);
		for(Attribute a : attributes) {
			a.gatherConstantPool(gathered);
		}
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	public void generateCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(accessFlags);
		ous.writeShort(gathered.getIndex(name));
		ous.writeShort(gathered.getIndex(descriptor));
		ous.writeShort(attributes.size());
		for(Attribute a : attributes) {
			a.generateCode(gathered, ous);
		}
	}

}
