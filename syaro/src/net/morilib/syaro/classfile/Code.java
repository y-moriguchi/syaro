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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class Code extends Attribute {

	private short maxStack;
	private short maxLocals;
	private List<Mnemonic> code = new ArrayList<Mnemonic>();
	private List<ExceptionTable> exceptionTable = new ArrayList<ExceptionTable>();
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * 
	 */
	public Code() {
		super("Code");
	}

	public short getMaxStack() {
		return maxStack;
	}

	public void setMaxStack(int maxStack) {
		this.maxStack = (short)maxStack;
	}

	public short getMaxLocals() {
		return maxLocals;
	}

	public void setMaxLocals(int maxLocals) {
		this.maxLocals = (short)maxLocals;
	}

	public int addCode(Mnemonic code) {
		this.code.add(code);
		return this.code.size();
	}

	public void addExceptionTable(ExceptionTable ex) {
		exceptionTable.add(ex);
	}

	public void addAttribute(Attribute attr) {
		attributes.add(attr);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	protected void gatherConstantPoolAttribute(GatheredConstantPool gathered) {
		for(Mnemonic m : code) {
			m.gatherConstantPool(gathered);
		}
		for(ExceptionTable t : exceptionTable) {
			t.gatherConstantPool(gathered);
		}
		for(Attribute a : attributes) {
			a.gatherConstantPool(gathered);
		}
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.Attribute#generateAttributeCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	protected void generateAttributeCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException {
		ByteArrayOutputStream bo;
		DataOutputStream os;
		byte[] ba;

		bo = new ByteArrayOutputStream();
		os = new DataOutputStream(bo);
		for(Mnemonic m : code) {
			m.generateCode(gathered, os);
		}
		ba = bo.toByteArray();
		ous.writeShort(maxStack);
		ous.writeShort(maxLocals);
		ous.writeInt(ba.length);
		ous.write(ba);
		ous.writeShort(exceptionTable.size());
		for(ExceptionTable t : exceptionTable) {
			t.generateCode(gathered, ous);
		}
		ous.writeShort(attributes.size());
		for(Attribute a : attributes) {
			a.generateCode(gathered, ous);
		}
	}

}
