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
import java.util.HashMap;
import java.util.List;
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
	private Map<String, SyaroClass> classes = new HashMap<String, SyaroClass>();

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
			throw new RuntimeException("function " + name + " is not defined");
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
			throw new RuntimeException("variable " + name + "is not defined");
		}
		return global.get(name);
	}

	/**
	 * gets a map of definitions of global variables.
	 */
	public Map<String, VariableType> getGlobalMap() {
		return new HashMap<String, VariableType>(global);
	}

	/**
	 * imports a static method by reflection.
	 * 
	 * @param classe class object
	 * @param name the name of method
	 */
	public void importMethod(Class<?> classe, String name) {
		Method[] mths;
		Method mth = null;
		List<VariableType> vt;

		mths = classe.getMethods();
		for(int i = 0; i < mths.length; i++) {
			if(mths[i].getName().equals(name) &&
					Modifier.isStatic(mths[i].getModifiers())) {
				if(mth != null) {
					throw new IllegalArgumentException("method name is ambiguous");
				}
				mth = mths[i];
			}
		}
		if(mth == null) {
			throw new IllegalArgumentException("method not found");
		}
		vt = new ArrayList<VariableType>();
		for(Class<?> prm : mth.getParameterTypes()) {
			vt.add(Utils.convertType(prm));
		}
		putSpace(mth.getName(), new FunctionDefinition(
				classe.getCanonicalName().replace('.', '/'),
				mth.getName(), Utils.convertType(mth.getReturnType()),
				vt));
	}

	/**
	 * imports a class by reflection.
	 * 
	 * @param classe class object
	 */
	public void importClass(Class<?> classe) {
		if(classe.isArray()) {
			throw new IllegalArgumentException("array can not be impoted");
		} else if(classe.isAnnotation()) {
			throw new IllegalArgumentException("annotation can not be impoted");
		} else if(classes.containsKey(classe.getSimpleName())) {
			throw new IllegalArgumentException("class has been already imported");
		}
		classes.put(classe.getSimpleName(), new SyaroClass(classe));
	}

	/**
	 * adds a class representation of this program.
	 * 
	 * @param classe class object
	 */
	public void addClass(String name, SyaroClass classe) {
		if(classes.containsKey(name)) {
			throw new IllegalArgumentException("class has been already defined");
		}
		classes.put(name, classe);
	}

	/**
	 * gets a class by variable type
	 * 
	 * @param type variable type
	 */
	public SyaroClass getClass(VariableType type) {
		String name;

		if(type.equals(QuasiPrimitive.OBJECT)) {
			return Utils.OBJECT;
		} else if(type.equals(QuasiPrimitive.STRING)) {
			return Utils.STRING;
		} else if(type instanceof SymbolType) {
			name = ((SymbolType)type).getName();
			if(classes.containsKey(name)) {
				return classes.get(name);
			} else {
				throw new RuntimeException("class " + name + " not defined");
			}
		} else {
			throw new RuntimeException("illegal type");
		}
	}

}
