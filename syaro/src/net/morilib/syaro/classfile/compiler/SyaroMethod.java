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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Method representation of this program.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SyaroMethod {

	private String name;
	private VariableType returnType;
	private List<VariableType> argumentTypes;
	private boolean _static;

	/**
	 * constructs a method by parameter.
	 * 
	 * @param name field name
	 * @param returnType return type representation of this program
	 * @param argumentTypes types of argument
	 * @param isStatic true if this definition is static
	 */
	public SyaroMethod(String name, VariableType returnType,
			List<VariableType> argumentTypes,
			boolean isStatic) {
		this.name = name;
		this.returnType = returnType;
		this.argumentTypes = new ArrayList<VariableType>(argumentTypes);
		this._static = isStatic;
	}

	/**
	 * constructs a method by reflection.
	 * 
	 * @param method reflection method object
	 */
	public SyaroMethod(Method method) {
		this.name = method.getName();
		this.returnType = Utils.convertType(method.getReturnType());
		this.argumentTypes = new ArrayList<VariableType>();
		this._static = Modifier.isStatic(method.getModifiers());
		for(Class<?> cls : method.getParameterTypes()) {
			this.argumentTypes.add(Utils.convertType(cls));
		}
	}

	/**
	 * gets the name of this definition.
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the return type of this definition.
	 */
	public VariableType getReturnType() {
		return returnType;
	}

	/**
	 * gets the argument types of this definition.
	 */
	public List<VariableType> getArgumentTypes() {
		return Collections.unmodifiableList(argumentTypes);
	}

	/**
	 * returns true if this is static.
	 */
	public boolean isStatic() {
		return _static;
	}

	/**
	 * returns the descriptor of this method.
	 * 
	 * @return descriptor
	 */
	public String getDescriptor() {
		return Utils.getDescriptor(returnType, argumentTypes);
	}

}
