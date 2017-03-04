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
 * This class represents a constant pool of string value.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantString extends ConstantPool {

	private ConstantUtf8 value;

	/**
	 * constructs a constant pool of string value.
	 */
	public ConstantString(String value) {
		super(CONSTANT_String);
		this.value = ConstantUtf8.getInstance(value);
	}

	/**
	 * gets the string value.
	 */
	public String getValue() {
		return value.getString();
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
		value.gatherConstantPool(gathered);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ConstantPool#generatePoolCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generatePoolCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeShort(gathered.getIndex(value));
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ConstantString) {
			return obj != null && value.equals(((ConstantString)obj).value);
		}
		return false;
	}

}
