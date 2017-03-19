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
package net.morilib.syaro.test.compiler;

/**
 * A class loader which loaded from byte array.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ByteArrayClassLoader extends ClassLoader {

	private java.util.Map<String, byte[]> classMap;

	/**
	 * constructs a class loader.
	 */
	public ByteArrayClassLoader() {
		classMap = new java.util.HashMap<>();
	}

	/**
	 * adds a class described by byte array.
	 * 
	 * @param name class name
	 * @param b a class described by byte array
	 */
	public void addClass(String name, byte[] b) {
		classMap.put(name, b);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b = classMap.get(name);
		if (b == null) {
			throw new ClassNotFoundException(name);
		}
		return defineClass(name, b, 0, b.length);
    }

}
