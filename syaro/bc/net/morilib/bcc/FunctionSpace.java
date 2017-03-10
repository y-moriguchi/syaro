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
package net.morilib.bcc;

import java.util.HashMap;
import java.util.Map;

/**
 * An class represents a namespace of functions and global variables.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class FunctionSpace {

	private String classname;
	private Map<String, Integer> space = new HashMap<String, Integer>();

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
	 * puts a function arity to this namespace.
	 * 
	 * @param name the name of the function
	 * @param arity the arity of the function
	 */
	public void putSpace(String name, int arity) {
		space.put(name, arity);
	}

	/**
	 * gets a function arity from this namespace by the given name.
	 * 
	 * @param name the name
	 * @return a arity
	 */
	public int getDefinition(String name) {
		if(!space.containsKey(name)) {
			throw new RuntimeException("function " + name + " is not defined");
		}
		return space.get(name);
	}

}
