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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a constant pool of Utf8 string.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstantUtf8 extends ConstantPool {

	private static Map<String, ConstantUtf8> pool =
			new HashMap<String, ConstantUtf8>();
	private String string;

	private ConstantUtf8(String str) {
		super(CONSTANT_Utf8);
		this.string = str;
	}

	/**
	 * gets an instance of a constant pool of Utf8 string.
	 */
	public static ConstantUtf8 getInstance(String str) {
		ConstantUtf8 res;

		res = pool.get(str);
		if(res == null) {
			res = new ConstantUtf8(str);
			pool.put(str, res);
		}
		return res;
	}

	/**
	 * get the string.
	 */
	public String getString() {
		return string;
	}

	private byte[] toUtf8() {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		char c;

		for(int i = 0; i < string.length(); i++) {
			c = string.charAt(i);
			if(c >= 0x0001 && c <= 0x007f) {
				ous.write((byte)c);
			} else if(c == 0x0000 || (c >= 0x0080 && c <= 0x07ff)) {
				ous.write((byte)(0xc0 | (c >> 6)));
				ous.write((byte)(0x80 | (c & 0x3f)));
			} else if(c >= 0x0800 && c <= 0xffff) {
				ous.write((byte)(0xe0 | (c >> 12)));
				ous.write((byte)(0x80 | ((c >> 6) & 0x3f)));
				ous.write((byte)(0x80 | (c & 0x3f)));
			} else {
				throw new RuntimeException();
			}
		}
		return ous.toByteArray();
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(this);
	}

	@Override
	protected void generatePoolCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		byte[] ba = toUtf8();

		ous.writeShort(ba.length);
		ous.write(ba);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ConstantUtf8) {
			return obj != null && string.equals(((ConstantUtf8)obj).string);
		}
		return false;
	}

}
