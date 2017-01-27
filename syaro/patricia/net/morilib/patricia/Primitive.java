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
package net.morilib.patricia;

/**
 * An enum represents primitive types.
 * 
 * @author Yuichiro MORIGUCHI
 */
public enum Primitive implements VariableType {

	VOID("V", "java/lang/Void", -1),
	BOOLEAN("B", "java/lang/Boolean", 0),
	BYTE("B", "java/lang/Byte", 1),
	CHAR("C", "java/lang/Character", 2),
	SHORT("S", "java/lang/Short", 2),
	INT("I", "java/lang/Integer", 3),
	LONG("J", "java/lang/Long", 4),
	FLOAT("F", "java/lang/Float", 5),
	DOUBLE("D", "java/lang/Double", 6);

	private String descriptor;
	private String wrapper;
	private int level;

	private Primitive(String desc, String w, int l) {
		this.descriptor = desc;
		this.wrapper = w;
		this.level = l;
	}

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
			} else if(level == 0 || p.level == 0) {
				return equals(t);
			} else if(level == 2 && p.level == 2) {
				return equals(t);
			} else {
				return level <= p.level;
			}
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

	/**
	 * gets the name of the wrapper class.
	 */
	public String getWrapperClass() {
		return wrapper;
	}

	/**
	 * gets the descriptor of the wrapper class.
	 */
	public String getWrapperDescriptor() {
		return "L" + wrapper + ";";
	}

}
