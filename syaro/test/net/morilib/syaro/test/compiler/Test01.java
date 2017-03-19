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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.morilib.syaro.classfile.Classfile;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.MethodInfo;
import net.morilib.syaro.classfile.compiler.FunctionSpace;
import net.morilib.syaro.classfile.compiler.MethodCompiler;
import net.morilib.syaro.classfile.compiler.NameAndType;
import net.morilib.syaro.classfile.compiler.Primitive;
import net.morilib.syaro.classfile.compiler.QuasiPrimitive;
import net.morilib.syaro.classfile.compiler.VariableType;

/**
 * Test code for MethodCompiler.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Test01 extends TestCase {

	static Object execclass(String code,
			FunctionSpace fn,
			VariableType returnType,
			List<NameAndType> fargs,
			List<NameAndType> flocals,
			Class<?>[] clc,
			Object[] args) throws Exception {
		Classfile cf = new Classfile();

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance("Test01"));
		cf.setSuperClass(ConstantClass.getInstance("java/lang/Object"));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("test", returnType),
				fargs,
				flocals,
				fn,
				code);

		ByteArrayOutputStream fs = new ByteArrayOutputStream();
		cf.generateClassFile(fs);

		ByteArrayClassLoader cl = new ByteArrayClassLoader();
		cl.addClass("Test01", fs.toByteArray());
		Class<?> classe = Class.forName("Test01", true, cl);
		Method method = classe.getMethod("test", clc);
		return method.invoke(null, args);
	}

	public void testA0001() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;

		obj = execclass("return 1+2*3;",
				fn, Primitive.INT, fa, fl,
				new Class<?>[] {},
				new Object[] {});
		assertEquals(7, ((Integer)obj).intValue());
	}

	public void testA0002() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;

		fa.add(new NameAndType("a", Primitive.INT));
		obj = execclass("return 1+2*a;",
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { Integer.TYPE },
				new Object[] { 5 });
		assertEquals(11, ((Integer)obj).intValue());
	}

	public void testA0003() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "b = a; b++; return b;";
		fa.add(new NameAndType("a", Primitive.INT));
		fl.add(new NameAndType("b", Primitive.INT));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { Integer.TYPE },
				new Object[] { 72 });
		assertEquals(73, ((Integer)obj).intValue());
	}

	public void testA0004() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return Math.sqrt(a);";
		fa.add(new NameAndType("a", Primitive.DOUBLE));
		fn.importClass(Math.class);
		obj = execclass(code,
				fn, Primitive.DOUBLE, fa, fl,
				new Class<?>[] { Double.TYPE },
				new Object[] { 841.0 });
		assertEquals(29.0, ((Double)obj).doubleValue());
	}

	public void testA0005() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return a.length();";
		fa.add(new NameAndType("a", QuasiPrimitive.STRING));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { String.class },
				new Object[] { "1234567890" });
		assertEquals(10, ((Integer)obj).intValue());
	}

	public void testA0006() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return Integer.MAX_VALUE;";
		fn.importClass(Integer.class);
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] {},
				new Object[] {});
		assertEquals(Integer.MAX_VALUE, ((Integer)obj).intValue());
	}

}
