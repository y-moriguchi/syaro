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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Field representation of this program.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SyaroField {

	private String name;
	private VariableType type;
	private boolean _static;

	/**
	 * constructs a field by parameter.
	 * 
	 * @param name field name
	 * @param type type representation of this program
	 * @param isStatic true if this definition is static
	 */
	public SyaroField(String name, VariableType type, boolean isStatic) {
		this.name = name;
		this.type = type;
		this._static = isStatic;
	}

	/**
	 * constructs a field by reflection.
	 * 
	 * @param field reflection field object
	 */
	public SyaroField(Field field) {
		this.name = field.getName();
		this.type = Utils.convertType(field.getType());
		this._static = Modifier.isStatic(field.getModifiers());
	}

	/**
	 * gets the name of this definition.
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the type of this definition.
	 */
	public VariableType getType() {
		return type;
	}

	/**
	 * returns true if this is static.
	 */
	public boolean isStatic() {
		return _static;
	}

}
