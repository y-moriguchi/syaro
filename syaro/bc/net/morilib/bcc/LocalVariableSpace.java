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
 * An class represents a namesapce which has local variables.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class LocalVariableSpace {

	private Map<String, Integer> space = new HashMap<String, Integer>();
	private int max = 3;

	/**
	 * puts definition of a variable.
	 * 
	 * @param var the name of the variable
	 */
	public void putVariable(String var) {
		if(!space.containsKey(var)) {
			space.put(var, max++);
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

}
