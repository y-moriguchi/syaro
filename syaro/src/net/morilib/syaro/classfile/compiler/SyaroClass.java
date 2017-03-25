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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representation of this program.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class SyaroClass {

	private String name;
	private boolean _interface;
	private List<SyaroMethod> methods;
	private List<SyaroConstructor> constructors;
	private Map<String, SyaroField> fields;

	/**
	 * constructs a class by parameter.
	 * 
	 * @param name Java class name
	 * @param isInterface true if this definition is interface
	 * @param methods methods
	 * @param constructors constructors
	 * @param fields fields
	 */
	public SyaroClass(String name, boolean isInterface,
			List<SyaroMethod> methods,
			List<SyaroConstructor> constructors,
			List<SyaroField> fields) {
		this.name = name;
		this._interface = isInterface;
		this.methods = new ArrayList<SyaroMethod>(methods);
		this.constructors = new ArrayList<SyaroConstructor>(constructors);
		this.fields = new HashMap<String, SyaroField>();
		for(SyaroField fld : fields) {
			this.fields.put(fld.getName(), fld);
		}
	}

	/**
	 * constructs a class by reflection.
	 * 
	 * @param classe reflection class object
	 */
	public SyaroClass(Class<?> classe) {
		int mod;

		this.name = classe.getCanonicalName().replace('.', '/');
		this._interface = classe.isInterface();
		this.methods = new ArrayList<SyaroMethod>();
		for(Method mth : classe.getMethods()) {
			mod = mth.getModifiers();
			if(!Modifier.isPrivate(mod)) {
				this.methods.add(new SyaroMethod(mth));
			}
		}
		this.constructors = new ArrayList<SyaroConstructor>();
		for(Constructor<?> con : classe.getConstructors()) {
			mod = con.getModifiers();
			if(!Modifier.isPrivate(mod)) {
				this.constructors.add(new SyaroConstructor(con));
			}
		}
		this.fields = new HashMap<String, SyaroField>();
		for(Field fld : classe.getFields()) {
			mod = fld.getModifiers();
			if(!Modifier.isPrivate(mod)) {
				this.fields.put(fld.getName(), new SyaroField(fld));
			}
		}
	}

	/**
	 * gets the name of this definition.
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns true if this is an interface.
	 */
	public boolean isInterface() {
		return _interface;
	}

	/**
	 * gets the methods of this definition.
	 */
	public List<SyaroMethod> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	/**
	 * gets the fields of this definition.
	 */
	public List<SyaroField> getFields() {
		return new ArrayList<SyaroField>(fields.values());
	}

	/**
	 * gets the field by the name.
	 */
	public SyaroField getField(String name) {
		return fields.get(name);
	}

	private boolean isConversible(List<VariableType> args,
			List<VariableType> prms) {
		for(int i = 0; i < args.size(); i++) {
			if(!args.get(i).isConversible(prms.get(i))) {
				return false;
			}
		}
		return true;
	}

	private boolean isConversible(List<VariableType> args,
			SyaroMethod mth) {
		List<VariableType> prms = mth.getArgumentTypes();

		for(int i = 0; i < args.size(); i++) {
			if(!args.get(i).isConversible(prms.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * finds the method by name and types of parameters.
	 * 
	 * This returns a method whose arguments are the nearest conversible.
	 * If conversible arguments are not found, this attempts to return
	 * a method whose name and arity of arguments are equal.
	 * If such method is not found or found more than two,
	 * this returns null.
	 */
	public SyaroMethod findMethod(String name, List<VariableType> args) {
		List<VariableType> prms;
		SyaroMethod resn = null;  // returns if arguments is not conversible
		SyaroMethod res = null;   // the nearest conversible
		int count = 0;

		for(SyaroMethod mth : methods) {
			if(mth.getName().equals(name)) {
				prms = mth.getArgumentTypes();
				if(args.size() == prms.size()) {
					if(!isConversible(args, mth)) {
						resn = mth;
						count++;
						continue;
					}
					if(res == null || isConversible(mth.getArgumentTypes(), res)) {
						res = mth;
					}
				}
			}
		}
		return res != null ? res : count == 1 ? resn : null;
	}

	/**
	 * finds the constructor by name and types of parameters.
	 * 
	 * This returns a constructor whose arguments are the nearest conversible.
	 * If conversible arguments are not found, this attempts to return
	 * a method whose name and arity of arguments are equal.
	 * If such method is not found or found more than two,
	 * this returns null.
	 */
	public SyaroConstructor findConstructor(List<VariableType> args) {
		List<VariableType> prms;
		SyaroConstructor resn = null;  // returns if arguments is not conversible
		SyaroConstructor res = null;   // the nearest conversible
		int count = 0;

		for(SyaroConstructor con : constructors) {
			prms = con.getArgumentTypes();
			if(args.size() == prms.size()) {
				if(!isConversible(args, con.getArgumentTypes())) {
					resn = con;
					count++;
					continue;
				}
				if(res == null || isConversible(con.getArgumentTypes(),
						res.getArgumentTypes())) {
					res = con;
				}
			}
		}
		return res != null ? res : count == 1 ? resn : null;
	}

}
