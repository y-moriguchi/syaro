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
package net.morilib.syaro.test;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public enum Primitive implements VariableType {

	INT("I"),
	DOUBLE("D");

	private String descriptor;

	private Primitive(String desc) {
		this.descriptor = desc;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.VariableType#getDescriptor()
	 */
	@Override
	public String getDescriptor() {
		return descriptor;
	}

}
