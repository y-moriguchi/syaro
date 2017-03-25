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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constructor representation of this program.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SyaroConstructor {

	private String name;
	private List<VariableType> argumentTypes;

	/**
	 * constructs a constructor by parameter.
	 * 
	 * @param name field name
	 * @param argumentTypes types of argument
	 */
	public SyaroConstructor(String name,
			List<VariableType> argumentTypes) {
		this.name = name;
		this.argumentTypes = new ArrayList<VariableType>(argumentTypes);
	}

	/**
	 * constructs a constructor by reflection.
	 * 
	 * @param method reflection method object
	 */
	public SyaroConstructor(Constructor<?> method) {
		this.name = method.getName();
		this.argumentTypes = new ArrayList<VariableType>();
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
	 * gets the argument types of this definition.
	 */
	public List<VariableType> getArgumentTypes() {
		return Collections.unmodifiableList(argumentTypes);
	}

	/**
	 * returns the descriptor of this method.
	 * 
	 * @return descriptor
	 */
	public String getDescriptor(FunctionSpace functions) {
		return Utils.getDescriptor(functions, Primitive.VOID, argumentTypes);
	}

}
