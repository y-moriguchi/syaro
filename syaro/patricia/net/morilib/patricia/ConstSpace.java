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
 * A namespace of constants.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ConstSpace {

	private Map<String, AST> constMap = new HashMap<String, AST>();

	/**
	 * returns true if the name is in the namespace.
	 * 
	 * @param name name
	 */
	public boolean containsKey(String name) {
		return constMap.containsKey(name);
	}

	/**
	 * finds the constant value by the name.
	 * 
	 * @param name name of constant
	 * @return value
	 */
	public AST find(String name) {
		if(!constMap.containsKey(name)) {
			throw new SemanticsException("undefined constant: " + name);
		}
		return constMap.get(name);
	}

	/**
	 * defines the constant.
	 * 
	 * @param name name of constant
	 * @param val  value of constant
	 */
	public void bind(String name, Number val) {
		if(constMap.containsKey(name)) {
			throw new SemanticsException("the constant has been already defined: " + name);
		} else if(val instanceof Integer) {
			constMap.put(name, new IntegerAST(val.intValue()));
		} else if(val instanceof Float) {
			constMap.put(name, new FloatAST(val.floatValue()));
		} else if(val instanceof Double) {
			constMap.put(name, new DoubleAST(val.doubleValue()));
		} else {
			throw new SemanticsException("type mismatch");
		}
	}

	/**
	 * defines negation of the constant.
	 * 
	 * @param name name of constant
	 * @param val  value of constant
	 */
	public void bindNegate(String name, Number val) {
		if(constMap.containsKey(name)) {
			throw new SemanticsException("the constant has been already defined: " + name);
		} else if(val instanceof Integer) {
			constMap.put(name, new IntegerAST(-val.intValue()));
		} else if(val instanceof Float) {
			constMap.put(name, new FloatAST(-val.floatValue()));
		} else if(val instanceof Double) {
			constMap.put(name, new DoubleAST(-val.doubleValue()));
		} else {
			throw new SemanticsException("type mismatch");
		}
	}

	/**
	 * defines the constant.
	 * 
	 * @param name name of constant
	 * @param val  value of constant
	 */
	public void bind(String name, AST val) {
		if(constMap.containsKey(name)) {
			throw new SemanticsException("the constant has been already defined: " + name);
		}
		constMap.put(name, val);
	}

	/**
	 * defines negation of the constant.
	 * 
	 * @param name name of constant
	 * @param val  value of constant
	 */
	public void bindNegate(String name, AST val) {
		if(constMap.containsKey(name)) {
			throw new SemanticsException("the constant has been already defined: " + name);
		} else if(val instanceof IntegerAST) {
			constMap.put(name, new IntegerAST(-((IntegerAST)val).getValue()));
		} else if(val instanceof FloatAST) {
			constMap.put(name, new FloatAST(-((FloatAST)val).getValue()));
		} else if(val instanceof DoubleAST) {
			constMap.put(name, new DoubleAST(-((DoubleAST)val).getValue()));
		} else {
			throw new SemanticsException("the constant cannot be negated");
		}
	}

}
