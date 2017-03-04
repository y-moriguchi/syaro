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
 * This class represents a constant pool of int value.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantInteger extends ConstantPool {

	private static Map<Integer, ConstantInteger> flyweight =
			new HashMap<Integer, ConstantInteger>();
	private int value;

	private ConstantInteger(int value) {
		super(CONSTANT_Integer);
		this.value = value;
	}

	/**
	 * gets a constant pool of int value.
	 */
	public static ConstantInteger getInstance(int value) {
		ConstantInteger res;

		if((res = flyweight.get(value)) == null) {
			res = new ConstantInteger(value);
			flyweight.put(value, res);
		}
		return res;
	}

	/**
	 * gets the int value.
	 */
	public int getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ConstantPool#generatePoolCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generatePoolCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeInt(value);
	}

}
