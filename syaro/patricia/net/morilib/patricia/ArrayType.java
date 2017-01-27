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
 * A type class represents an array.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ArrayType implements VariableType {

	private VariableType element;

	/**
	 * creates a type of array.
	 * 
	 * @param el element of the array
	 */
	public ArrayType(VariableType el) {
		element = el;
	}

	/**
	 * gets the type of the element of array.
	 */
	public VariableType getElement() {
		return element;
	}

	@Override
	public String getDescriptor() {
		return element.getDescriptor() + "[";
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isConversible(VariableType type) {
		if(type instanceof ArrayType) {
			return element.isConversible(((ArrayType)type).element);
		} else {
			return false;
		}
	}

	@Override
	public boolean isCastable(VariableType type) {
		return isConversible(type);
	}

}
