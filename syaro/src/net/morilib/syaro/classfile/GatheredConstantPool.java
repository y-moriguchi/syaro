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
package net.morilib.syaro.classfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a container of constant pools.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class GatheredConstantPool {

	private Map<ConstantPool, Integer> poolMap = new HashMap<ConstantPool, Integer>();
	private List<ConstantPool> poolList = new ArrayList<ConstantPool>();
	private int maxIndex = 1;

	/**
	 * adds a constant pool to this container.
	 * 
	 * @param pool a constant pool
	 */
	public void putConstantPool(ConstantPool pool) {
		if(!poolMap.containsKey(pool)) {
			poolList.add(pool);
			poolMap.put(pool, maxIndex++);
		}
	}

	/**
	 * adds a constant pool to this container.
	 * 
	 * @param pool a constant pool
	 */
	public void putConstantPool(ConstantPool pool, int inclement) {
		if(inclement < 0) {
			throw new IllegalArgumentException();
		} else if(!poolMap.containsKey(pool)) {
			poolList.add(pool);
			for(int i = 1; i < inclement; i++) {
				poolList.add(null);
			}
			poolMap.put(pool, maxIndex);
			maxIndex += inclement;
		}
	}

	/**
	 * gets the index of the constant pool.
	 * 
	 * @param pool constant pool
	 * @return the index
	 */
	public int getIndex(ConstantPool pool) {
		if(poolMap.containsKey(pool)) {
			return poolMap.get(pool);
		} else {
			throw new RuntimeException();
		}
	}

	/**
	 * gets the constant pools.
	 */
	public List<ConstantPool> getConstatPools() {
		return Collections.unmodifiableList(poolList);
	}

	/**
	 * gets the maximum index.
	 */
	public int getMaxIndex() {
		return maxIndex;
	}

}
