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
 * This class represents a field info.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class FieldInfo implements ClassInfo {

	public static final short ACC_PUBLIC = 0x0001;
	public static final short ACC_PRIVATE = 0x0002;
	public static final short ACC_PROTECTED = 0x0004;
	public static final short ACC_STATIC = 0x0008;
	public static final short ACC_FINAL = 0x0010;
	public static final short ACC_VOLATILE = 0x0040;
	public static final short ACC_TRANSIENT = 0x0080;
	public static final short ACC_SYNTHETIC = 0x1000;
	public static final short ACC_ENUM = 0x4000;

	private short accessFlags;
	private ConstantUtf8 name;
	private ConstantUtf8 descriptor;
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * constructs a field info.
	 * 
	 * @param name field name
	 * @param desc field descriptor
	 */
	public FieldInfo(String name, String desc) {
		this.name = ConstantUtf8.getInstance(name);
		this.descriptor = ConstantUtf8.getInstance(desc);
	}

	/**
	 * gets the access flags.
	 */
	public short getAccessFlags() {
		return accessFlags;
	}

	/**
	 * sets the access flags.
	 */
	public void setAccessFlags(int accessFlags) {
		this.accessFlags = (short)accessFlags;
	}

	/**
	 * gets the field name.
	 */
	public ConstantUtf8 getName() {
		return name;
	}

	/**
	 * gets the field descriptor.
	 */
	public ConstantUtf8 getDescriptor() {
		return descriptor;
	}

	/**
	 * adds an attribute.
	 * 
	 * @param attr an attribute
	 */
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
