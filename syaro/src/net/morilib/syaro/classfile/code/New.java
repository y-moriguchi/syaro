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

import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.GatheredConstantPool;
import net.morilib.syaro.classfile.Mnemonic;

/**
 * This class represents an Java VM instruction new.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class New extends Mnemonic {

	private ConstantClass klasse;

	/**
	 * constructs a new.
	 * 
	 * @param method method reference
	 */
	public New(ConstantClass klasse) {
		super(187);
		this.klasse = klasse;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		klasse.gatherConstantPool(gathered);
	}

	@Override
	protected void generateMnemonicCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ous.writeShort(gathered.getIndex(klasse));
	}

	@Override
	protected int getByteLength() {
		return 3;
	}

}