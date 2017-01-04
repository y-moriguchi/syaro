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

import java.util.HashMap;
import java.util.Map;


/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class FunctionSpace {

	private String classname;
	private Map<String, FunctionDefinition> space =
			new HashMap<String, FunctionDefinition>();
	private Map<String, VariableType> global = new HashMap<String, VariableType>();

	public FunctionSpace(String classname) {
		this.classname = classname;
	}

	public String getClassname() {
		return classname;
	}

	public void putSpace(String name, FunctionDefinition def) {
		space.put(name, def);
	}

	public FunctionDefinition getDefinition(String name) {
		if(!space.containsKey(name)) {
			throw new RuntimeException("function " + name + " is not defined");
		}
		return space.get(name);
	}

	public void putGlobal(String name, VariableType type) {
		global.put(name, type);
	}

	public VariableType getGlobal(String name) {
		if(!global.containsKey(name)) {
			throw new RuntimeException("variable " + name + "is not defined");
		}
		return global.get(name);
	}

	public Map<String, VariableType> getGlobalMap() {
		return new HashMap<String, VariableType>(global);
	}

}
