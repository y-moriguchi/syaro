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
 * This class represents a constant pool of field reference.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantFieldref extends ConstantPool {

	private ConstantClass classInfo;
	private ConstantNameAndType nameAndTypeInfo;

	/**
	 * constructs a constant pool of field reference.
	 */
	public ConstantFieldref() {
		super(CONSTANT_Fieldref);
	}

	/**
	 * constructs a constant pool of field reference.
	 * 
	 * @param classname class name
	 * @param fieldname field name
	 * @param type descriptor
	 */
	public ConstantFieldref(String classname, String fieldname, String type) {
		super(CONSTANT_Fieldref);
		classInfo = new ConstantClass(classname);
		nameAndTypeInfo = new ConstantNameAndType(fieldname, type);
	}

	/**
	 * gets the class info.
	 */
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	/**
	 * sets the class info.
	 */
	public void setClassInfo(ConstantClass classInfo) {
		this.classInfo = classInfo;
	}

	/**
	 * gets the name and type info.
	 */
	public ConstantNameAndType getNameAndTypeInfo() {
		return nameAndTypeInfo;
	}

	/**
	 * sets the name and type info.
	 */
	public void setNameAndTypeInfo(ConstantNameAndType nameAndTypeInfo) {
		this.nameAndTypeInfo = nameAndTypeInfo;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
		classInfo.gatherConstantPool(gathered);
		nameAndTypeInfo.gatherConstantPool(gathered);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generatePoolCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(gathered.getIndex(classInfo));
		ous.writeShort(gathered.getIndex(nameAndTypeInfo));
	}

}
