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
 * This class represents a constant pool of method reference.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantMethodref extends ConstantPool {

	private static Map<ConstantMethodref, ConstantMethodref> flyweight =
			new HashMap<ConstantMethodref, ConstantMethodref>();
	private ConstantClass classInfo;
	private ConstantNameAndType nameAndTypeInfo;

	private ConstantMethodref(String classname, String methodname, String type) {
		super(CONSTANT_Methodref);
		classInfo = ConstantClass.getInstance(classname);
		nameAndTypeInfo = ConstantNameAndType.getInstance(methodname, type);
	}

	/**
	 * gets a constant pool of method reference.
	 * 
	 * @param classname class name
	 * @param methodname method name
	 * @param type method descriptor
	 */
	public static ConstantMethodref getInstance(String classname,
			String methodname, String type) {
		ConstantMethodref res, obj;

		obj = new ConstantMethodref(classname, methodname, type);
		if((res = flyweight.get(obj)) == null) {
			res = obj;
			flyweight.put(res, res);
		}
		return res;
	}

	/**
	 * gets the class info.
	 */
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	/**
	 * gets the name and type info.
	 */
	public ConstantNameAndType getNameAndTypeInfo() {
		return nameAndTypeInfo;
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

	@Override
	public int hashCode() {
		int h = 17;

		h = 37 * classInfo.hashCode() + h;
		h = 37 * nameAndTypeInfo.hashCode() + h;
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		ConstantMethodref r;

		if(obj != null && obj instanceof ConstantMethodref) {
			r = (ConstantMethodref)obj;
			return classInfo.equals(r.classInfo) &&
					nameAndTypeInfo.equals(r.nameAndTypeInfo);
		}
		return false;
	}

}
