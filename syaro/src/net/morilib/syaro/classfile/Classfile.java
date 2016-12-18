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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Classfile {

	public static final int MAGIC = 0xcafebabe;
	public static final short ACC_PUBLIC    = 0x0001;
	public static final short ACC_FINAL     = 0x0010;
	public static final short ACC_SUPER     = 0x0020;
	public static final short ACC_INTERFACE = 0x0200;
	public static final short ACC_ABSTRACT  = 0x0400;

	private int magic = MAGIC;
	private short majorVersion;
	private short minorVersion;
	private short accessFlag;
	private ConstantClass thisClass;
	private ConstantClass superClass;
//	private List<ConstantClass> interfaces;
//	private List<ConstantPool> fields;
	private List<MethodInfo> methods = new ArrayList<MethodInfo>();
//	private List<Attribute> attributes;

	public long getMagic() {
		return magic;
	}

	public short getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = (short)majorVersion;
	}

	public short getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = (short)minorVersion;
	}

	public short getAccessFlag() {
		return accessFlag;
	}

	public void setAccessFlag(int accessFlag) {
		this.accessFlag = (short)accessFlag;
	}

	public void setThisClass(ConstantClass thisClass) {
		this.thisClass = thisClass;
	}

	public ConstantClass getThisClass() {
		return thisClass;
	}

	public ConstantClass getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ConstantClass superClass) {
		this.superClass = superClass;
	}

	public void addMethod(MethodInfo method) {
		methods.add(method);
	}

	public void generateClassFile(OutputStream ous) throws IOException {
		GatheredConstantPool gathered = new GatheredConstantPool();
		DataOutputStream dos = new DataOutputStream(ous);

		thisClass.gatherConstantPool(gathered);
		superClass.gatherConstantPool(gathered);
		for(MethodInfo m : methods) {
			m.gatherConstantPool(gathered);
		}
		dos.writeInt(magic);
		dos.writeShort(minorVersion);
		dos.writeShort(majorVersion);
		dos.writeShort(gathered.getMaxIndex());
		for(ConstantPool c : gathered.getConstatPools()) {
			c.generateCode(gathered, dos);
		}
		dos.writeShort(accessFlag);
		dos.writeShort(gathered.getIndex(thisClass));
		dos.writeShort(gathered.getIndex(superClass));
		dos.writeShort(0);
		dos.writeShort(0);
		dos.writeShort(methods.size());
		for(MethodInfo m : methods) {
			m.generateCode(gathered, dos);
		}
		dos.writeShort(0);
	}

}
