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
 * This class represents an exception table.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ExceptionTable implements ClassInfo {

	private short startPc;
	private short endPc;
	private short handlerPc;
	private ConstantClass catchType;

	/**
	 * gets the start pc.
	 */
	public short getStartPc() {
		return startPc;
	}

	/**
	 * sets the start pc.
	 */
	public void setStartPc(short startPc) {
		this.startPc = startPc;
	}

	/**
	 * gets the end pc.
	 */
	public short getEndPc() {
		return endPc;
	}

	/**
	 * sets the end pc.
	 */
	public void setEndPc(short endPc) {
		this.endPc = endPc;
	}

	/**
	 * gets the handler pc.
	 */
	public short getHandlerPc() {
		return handlerPc;
	}

	/**
	 * sets the handler pc.
	 */
	public void setHandlerPc(short handlerPc) {
		this.handlerPc = handlerPc;
	}

	/**
	 * gets the class info of catch type.
	 */
	public ClassInfo getCatchType() {
		return catchType;
	}

	/**
	 * sets the class info of catch type.
	 */
	public void setCatchType(ConstantClass catchType) {
		this.catchType = catchType;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#gatherConstantPool(net.morilib.syaro.classfile.GatheredConstantPool)
	 */
	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		gathered.putConstantPool(catchType);
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.classfile.ClassInfo#generateCode(net.morilib.syaro.classfile.GatheredConstantPool, java.io.DataOutputStream)
	 */
	@Override
	public void generateCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(startPc);
		ous.writeShort(endPc);
		ous.writeShort(handlerPc);
		ous.writeShort(gathered.getIndex(catchType));
	}

}
