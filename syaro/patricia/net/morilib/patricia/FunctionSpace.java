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

import java.util.HashMap;
import java.util.Map;


/**
 * An class represents a namespace of functions and global variables.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class FunctionSpace {

	private String classname;
	private Map<String, FunctionDefinition> space =
			new HashMap<String, FunctionDefinition>();
	private Map<String, VariableType> global = new HashMap<String, VariableType>();

	/**
	 * creates a namespace.
	 * 
	 * @param classname the class name to which this namespace is belong
	 */
	public FunctionSpace(String classname) {
		this.classname = classname;
	}

	/**
	 * gets the class name to which this namespace is belong.
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * puts a function to this namespace.
	 * 
	 * @param name the name of the function
	 * @param def the definition of the function
	 */
	public void putSpace(String name, FunctionDefinition def) {
		space.put(name, def);
	}

	/**
	 * gets a function from this namespace by the given name.
	 * 
	 * @param name the name
	 * @return a function
	 */
	public FunctionDefinition getDefinition(String name) {
		if(!space.containsKey(name)) {
			throw new SemanticsException("function " + name + " is not defined");
		}
		return space.get(name);
	}

	/**
	 * puts a global variable definition.
	 * 
	 * @param name the name of the variable
	 * @param type the type of the variable
	 */
	public void putGlobal(String name, VariableType type) {
		global.put(name, type);
	}

	/**
	 * gets a global variable from this namespace by the given name.
	 * 
	 * @param name the name
	 * @return the definition of the global variable
	 */
	public VariableType getGlobal(String name) {
		if(!global.containsKey(name)) {
			throw new SemanticsException("variable " + name + "is not defined");
		}
		return global.get(name);
	}

	/**
	 * gets a map of definitions of global variables.
	 */
	public Map<String, VariableType> getGlobalMap() {
		return new HashMap<String, VariableType>(global);
	}

}
