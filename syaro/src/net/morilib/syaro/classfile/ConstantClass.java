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
 * This class represents a constant pool of class.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantClass extends ConstantPool {

	private static Map<String, ConstantClass> flyweight =
			new HashMap<String, ConstantClass>();
	private ConstantUtf8 name;

	private ConstantClass(String name) {
		super(CONSTANT_Class);
		this.name = ConstantUtf8.getInstance(name);
	}

	/**
	 * gets a constant pool of class.
	 * 
	 * @param name class name
	 */
	public static ConstantClass getInstance(String name) {
		ConstantClass res;

		if((res = flyweight.get(name)) == null) {
			res = new ConstantClass(name);
			flyweight.put(name, res);
		}
		return res;
	}

	/**
	 * get the class name.
	 */
	public ConstantUtf8 getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
		name.gatherConstantPool(gathered);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generatePoolCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(gathered.getIndex(name));
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ConstantClass) {
			return obj != null && name.equals(((ConstantClass)obj).name);
		}
		return false;
	}

}
