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
 * This class represents a constant pool of double value.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantFloat extends ConstantPool {

	private float value;

	/**
	 * constructs a constant pool of double value.
	 */
	public ConstantFloat(float value) {
		super(CONSTANT_Float);
		this.value = value;
	}

	/**
	 * gets the double value.
	 */
	public float getValue() {
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
		ous.writeInt(Float.floatToRawIntBits(value));
	}

}
