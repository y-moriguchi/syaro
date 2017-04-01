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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An class represents a namesapce which has local variables.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class LocalVariableSpace {

	private Map<String, Integer> space = new HashMap<String, Integer>();
	private Map<String, VariableType> types = new HashMap<String, VariableType>();
	private List<VariableType> typeList = new ArrayList<VariableType>();
	private VariableType thisReturnType;
	private boolean isStatic;
	private int max;

	/**
	 * creates a namespace of local variables.
	 * 
	 * @param rettype the type of a class which has this namespace
	 * @param isStatic the type which has this namespace is static
	 */
	public LocalVariableSpace(VariableType rettype, boolean isStatic) {
		this.thisReturnType = rettype;
		this.isStatic = isStatic;
		this.max = isStatic ? 0 : 1;
	}

	/**
	 * puts definition of integer.
	 * 
	 * @param var the name
	 */
	public void putVariable(String var) {
		putVariable(var, Primitive.INT);
	}

	/**
	 * puts definition of a variable.
	 * 
	 * @param var the name of the variable
	 * @param type the type of the variable
	 */
	public void putVariable(String var, VariableType type) {
		if(!space.containsKey(var)) {
			types.put(var, type);
			if(type.equals(Primitive.DOUBLE) || type.equals(Primitive.LONG)) {
				space.put(var, max);
				max += 2;
				typeList.add(type);
				typeList.add(null);
			} else {
				space.put(var, max++);
				typeList.add(type);
			}
		}
	}

	/**
	 * gets the maximum index.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * gets the index of the variable which indicates by the name.
	 * 
	 * @param var the name
	 */
	public int getIndex(String var) {
		Integer res = space.get(var);

		return res != null ? res : -1;
	}

	/**
	 * gets the type of the variable which indicates by the name.
	 * 
	 * @param var the name
	 */
	public VariableType getType(String var) {
		return types.get(var);
	}

	/**
	 * gets the type of the variable by the index.
	 * 
	 * @param idx the index
	 */
	public VariableType getType(int idx) {
		return typeList.get(idx - (isStatic ? 0 : 1));
	}

	/**
	 * gets the return type of the owner class.
	 */
	public VariableType getThisReturnType() {
		return thisReturnType;
	}

}
