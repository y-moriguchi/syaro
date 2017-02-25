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
 * This class represents codes of a method.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Code extends Attribute {

	private short maxStack;
	private short maxLocals;
	private List<Mnemonic> code = new ArrayList<Mnemonic>();
	private List<ExceptionTable> exceptionTable = new ArrayList<ExceptionTable>();
	private List<Attribute> attributes = new ArrayList<Attribute>();
	private List<Integer> address = new ArrayList<Integer>();

	/**
	 * constructs a code info.
	 */
	public Code() {
		super("Code");
	}

	/**
	 * gets the size of stack.
	 */
	public short getMaxStack() {
		return maxStack;
	}

	/**
	 * sets the size of stack.
	 */
	public void setMaxStack(int maxStack) {
		this.maxStack = (short)maxStack;
	}

	/**
	 * gets the number of local variables.
	 */
	public short getMaxLocals() {
		return maxLocals;
	}

	/**
	 * sets the number of local variables.
	 */
	public void setMaxLocals(int maxLocals) {
		this.maxLocals = (short)maxLocals;
	}

	/**
	 * adds an instruction.
	 * 
	 * @param code the instruction
	 * @return the index at which the code added in the list
	 */
	public int addCode(Mnemonic code) {
		this.code.add(code);
		if(address.size() > 0) {
			address.add(getCurrentAddress() +
					code.computeByteLength(getCurrentAddress()));
		} else {
			address.add(code.getByteLength());
		}
		return this.code.size() - 1;
	}

	/**
	 * gets the instruction indicated by the index.
	 * 
	 * @param index the index of instructions
	 * @return the instruction
	 */
	public Mnemonic getCode(int index) {
		return code.get(index);
	}

	/**
	 * gets the address of the code indicated by the index.
	 * 
	 * @param index the index at which the code added in the list
	 * @return the address
	 */
	public int getAddress(int index) {
		return address.get(index) - code.get(index).getByteLength();
	}

	/**
	 * gets the offset from address indicated by the index to current address.
	 * 
	 * @param index the index of instruction
	 * @return the offset
	 */
	public int getCurrentOffset(int index) {
		return getCurrentAddress() - getAddress(index);
	}

	/**
	 * gets current address of this codes.
	 */
	public int getCurrentAddress() {
		if(address.size() > 0) {
			return address.get(address.size() - 1);
		} else {
			return 0;
		}
	}

	/**
	 * gets current padding of this codes.
	 */
	public int getCurrentPad() {
		int current = getCurrentAddress();

		return (4 - ((current + 1) % 4)) / 4;
	}

	/**
	 * add an exception table.
	 * 
	 * @param ex exception table.
	 */
	public void addExceptionTable(ExceptionTable ex) {
		exceptionTable.add(ex);
	}

	/**
	 * add an attribute.
	 * 
	 * @param attr attribute
	 */
	public void addAttribute(Attribute attr) {
		attributes.add(attr);
	}

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
