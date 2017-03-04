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
 * This class represents a constant pool of field reference.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantFieldref extends ConstantPool {

	private static Map<ConstantFieldref, ConstantFieldref> flyweight =
			new HashMap<ConstantFieldref, ConstantFieldref>();
	private ConstantClass classInfo;
	private ConstantNameAndType nameAndTypeInfo;

	private ConstantFieldref(String classname, String fieldname, String type) {
		super(CONSTANT_Fieldref);
		classInfo = ConstantClass.getInstance(classname);
		nameAndTypeInfo = ConstantNameAndType.getInstance(fieldname, type);
	}

	/**
	 * gets a constant pool of field reference.
	 * 
	 * @param classname class name
	 * @param fieldname field name
	 * @param type descriptor
	 */
	public static ConstantFieldref getInstance(String classname,
			String fieldname, String type) {
		ConstantFieldref res, obj;

		obj = new ConstantFieldref(classname, fieldname, type);
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

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
		classInfo.gatherConstantPool(gathered);
		nameAndTypeInfo.gatherConstantPool(gathered);
	}

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
		ConstantFieldref r;

		if(obj != null && obj instanceof ConstantFieldref) {
			r = (ConstantFieldref)obj;
			return classInfo.equals(r.classInfo) &&
					nameAndTypeInfo.equals(r.nameAndTypeInfo);
		}
		return false;
	}

}
