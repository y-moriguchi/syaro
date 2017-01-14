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
package net.morilib.syaro.classfile.code;

import java.io.DataOutputStream;
import java.io.IOException;

import net.morilib.syaro.classfile.GatheredConstantPool;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * This class represents an Java VM instruction newarray.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class NewArray extends Mnemonic {

	public static enum Type {
		BOOLEAN(4),
		CHAR(5),
		FLOAT(6),
		DOUBLE(7),
		BYTE(8),
		SHORT(9),
		INT(10),
		LONG(11);
		private byte code;
		private Type(int c) {
			code = (byte)c;
		}
	}

	private Type type;

	/**
	 * constructs a newarray.
	 * 
	 * @param method method reference
	 */
	public NewArray(Type type) {
		super(188);
		this.type = type;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeByte(type.code);
	}

	@Override
	protected int getByteLength() {
		return 2;
	}

}