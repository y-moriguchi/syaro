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
package net.morilib.syaro.classfile.compiler;

/**
 * Type information which gives by name.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SymbolType implements VariableType {

	private String name;

	/**
	 * constructs this type by the given name.
	 * 
	 * @param name name of type
	 */
	public SymbolType(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * gets the name of this type.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String getDescriptor() {
		return "L" + name + ";";
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isConversible(VariableType type) {
		if(type instanceof SymbolType) {
			return name.equals(((SymbolType)type).name);
		}
		return false;
	}

	@Override
	public boolean isCastable(VariableType type) {
		return type instanceof SymbolType;
	}

}
