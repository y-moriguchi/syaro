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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class LocalVariableSpace {

	private Map<String, Integer> space = new HashMap<String, Integer>();
	private Map<String, VariableType> types = new HashMap<String, VariableType>();
	private List<VariableType> typeList = new ArrayList<VariableType>();
	private VariableType thisReturnType;
	private int max = 1;

	public LocalVariableSpace() {
		this(Primitive.INT);
	}

	public LocalVariableSpace(VariableType rettype) {
		this.thisReturnType = rettype;
	}

	public void putVariable(String var) {
		putVariable(var, Primitive.INT);
	}

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

	public int getMax() {
		return max;
	}

	public int getIndex(String var) {
		Integer res = space.get(var);

		return res != null ? res : -1;
	}

	public VariableType getType(String var) {
		return types.get(var);
	}

	public VariableType getType(int idx) {
		return typeList.get(idx - 1);
	}

	public VariableType getThisReturnType() {
		return thisReturnType;
	}

}
