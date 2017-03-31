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
	 * If the given type is null, this handler catches all exceptions.
	 */
	public void setCatchType(ConstantClass catchType) {
		this.catchType = catchType;
	}

	@Override
	public void gatherConstantPool(GatheredConstantPool gathered) {
		if(catchType != null) {
			catchType.gatherConstantPool(gathered);
		}
	}

	@Override
	public void generateCode(GatheredConstantPool gathered, DataOutputStream ous)
			throws IOException {
		ous.writeShort(startPc);
		ous.writeShort(endPc);
		ous.writeShort(handlerPc);
		if(catchType != null) {
			ous.writeShort(gathered.getIndex(catchType));
		} else {
			ous.writeShort(0);
		}
	}

}
