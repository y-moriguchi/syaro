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

/**
 * This class represents a classfile.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Classfile {

	/**
	 * The magic number of Java classfile.
	 */
	public static final int MAGIC = 0xcafebabe;

	/**
	 * The public access flag.
	 */
	public static final short ACC_PUBLIC    = 0x0001;

	/**
	 * The final access flag.
	 */
	public static final short ACC_FINAL     = 0x0010;

	/**
	 * The super access flag.
	 */
	public static final short ACC_SUPER     = 0x0020;

	/**
	 * The interface access flag.
	 */
	public static final short ACC_INTERFACE = 0x0200;

	/**
	 * The abstract access flag.
	 */
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

	/**
	 * gets the magic number.
	 */
	public long getMagic() {
		return magic;
	}

	/**
	 * gets the major version.
	 */
	public short getMajorVersion() {
		return majorVersion;
	}

	/**
	 * sets the major version.
	 */
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = (short)majorVersion;
	}

	/**
	 * gets the minor version.
	 */
	public short getMinorVersion() {
		return minorVersion;
	}

	/**
	 * sets the minor version.
	 */
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = (short)minorVersion;
	}

	/**
	 * gets the access flag.
	 */
	public short getAccessFlag() {
		return accessFlag;
	}

	/**
	 * sets the access flag.
	 */
	public void setAccessFlag(int accessFlag) {
		this.accessFlag = (short)accessFlag;
	}

	/**
	 * gets the class info represents this class.
	 */
	public ConstantClass getThisClass() {
		return thisClass;
	}

	/**
	 * sets the class info represents this class.
	 */
	public void setThisClass(ConstantClass thisClass) {
		this.thisClass = thisClass;
	}

	/**
	 * gets the class info represents super class.
	 */
	public ConstantClass getSuperClass() {
		return superClass;
	}

	/**
	 * sets the class info represents super class.
	 */
	public void setSuperClass(ConstantClass superClass) {
		this.superClass = superClass;
	}

	/**
	 * gets the method info.
	 */
	public void addMethod(MethodInfo method) {
		methods.add(method);
	}

	/**
	 * generates the classfile binary code.
	 * 
	 * @param ous the output stream
	 * @throws IOException
	 */
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
