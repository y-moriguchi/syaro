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
 * A class storeing variable name and type.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class NameAndType {

	private String name;
	private VariableType type;

	/**
	 * constructs a pair of variable name and type.
	 * 
	 * @param name variable name
	 * @param type variable type
	 */
	public NameAndType(String name, VariableType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * gets the variable name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the variable type.
	 */
	public VariableType getType() {
		return type;
	}

}
