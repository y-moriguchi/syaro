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

	VOID("V", -1),
	BYTE("B", 1),
	SHORT("S", 2),
	INT("I", 3),
	LONG("J", 4),
	FLOAT("F", 5),
	DOUBLE("D", 6);

	private String descriptor;
	private int level;

	private Primitive(String desc, int l) {
		this.descriptor = desc;
		this.level = l;
	}

	/* (non-Javadoc)
	 * @see net.morilib.syaro.test.VariableType#getDescriptor()
	 */
	@Override
	public String getDescriptor() {
		return descriptor;
	}

	public boolean isConversible(VariableType t) {
		Primitive p;

		if(t instanceof Primitive) {
			p = (Primitive)t;
			if(level < 0 || p.level < 0) {
				throw new IllegalArgumentException();
			}
			return level <= p.level;
		} else {
			return false;
		}
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isCastable(VariableType type) {
		return type instanceof Primitive;
	}

}
