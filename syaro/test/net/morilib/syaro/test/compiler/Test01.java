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

import java.awt.GridBagConstraints;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.morilib.syaro.classfile.Classfile;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.FieldInfo;
import net.morilib.syaro.classfile.MethodInfo;
import net.morilib.syaro.classfile.compiler.ArrayType;
import net.morilib.syaro.classfile.compiler.FunctionSpace;
import net.morilib.syaro.classfile.compiler.MethodCompiler;
import net.morilib.syaro.classfile.compiler.NameAndType;
import net.morilib.syaro.classfile.compiler.Primitive;
import net.morilib.syaro.classfile.compiler.QuasiPrimitive;
import net.morilib.syaro.classfile.compiler.SyaroClass;
import net.morilib.syaro.classfile.compiler.SyaroConstructor;
import net.morilib.syaro.classfile.compiler.SyaroField;
import net.morilib.syaro.classfile.compiler.SyaroMethod;
import net.morilib.syaro.classfile.compiler.SymbolType;
import net.morilib.syaro.classfile.compiler.VariableType;

/**
 * Test code for MethodCompiler.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Test01 extends TestCase {

	static Object invokeTestClass(Classfile cf, String methodName,
			Class<?>[] clc, Object[] args) throws Exception {
		ByteArrayOutputStream fs = new ByteArrayOutputStream();
		cf.generateClassFile(fs);

		ByteArrayClassLoader cl = new ByteArrayClassLoader();
		cl.addClass("Test01", fs.toByteArray());
		Class<?> classe = Class.forName("Test01", true, cl);
		Method method = classe.getMethod(methodName, clc);
		return method.invoke(null, args);
	}

	static Class<?> createTestClass(Classfile cf) throws Exception {
		ByteArrayOutputStream fs = new ByteArrayOutputStream();
		cf.generateClassFile(fs);

		ByteArrayClassLoader cl = new ByteArrayClassLoader();
		cl.addClass("Test01", fs.toByteArray());
		return Class.forName("Test01", true, cl);
	}

	static Object invokeTestClass(Class<?> classe, String methodName,
			int val) throws Exception {
		Method method = classe.getMethod(methodName, new Class<?>[] { Integer.TYPE });
		return method.invoke(null, new Object[] { val });
	}

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
		return invokeTestClass(cf, "test", clc, args);
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

	public void testA0007() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return Math.max(1, 2);";
		fn.importClass(Math.class);
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] {},
				new Object[] {});
		assertEquals(2, ((Integer)obj).intValue());
	}

	public void testA0008() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return Math.max(1, 2.1f);";
		fn.importClass(Math.class);
		obj = execclass(code,
				fn, Primitive.FLOAT, fa, fl,
				new Class<?>[] {},
				new Object[] {});
		assertEquals(2.1f, ((Float)obj).floatValue());
	}

	public void testA0009() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return Math.max(2.3, 2.3f);";
		fn.importClass(Math.class);
		obj = execclass(code,
				fn, Primitive.DOUBLE, fa, fl,
				new Class<?>[] {},
				new Object[] {});
		assertEquals(2.3, ((Double)obj).doubleValue());
	}

	public void testA0010() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return a.add(b);";
		fn.importClass(BigInteger.class);
		fa.add(new NameAndType("a", new SymbolType("BigInteger")));
		fa.add(new NameAndType("b", new SymbolType("BigInteger")));
		obj = execclass(code,
				fn, new SymbolType("BigInteger"), fa, fl,
				new Class<?>[] { BigInteger.class, BigInteger.class },
				new Object[] { new BigInteger("765"), new BigInteger("346") });
		assertEquals(new BigInteger("1111"), obj);
	}

	public void testA0011() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return ((BigInteger)a).add(b);";
		fn.importClass(Number.class);
		fn.importClass(BigInteger.class);
		fa.add(new NameAndType("a", new SymbolType("Number")));
		fa.add(new NameAndType("b", new SymbolType("BigInteger")));
		obj = execclass(code,
				fn, new SymbolType("BigInteger"), fa, fl,
				new Class<?>[] { Number.class, BigInteger.class },
				new Object[] { new BigInteger("765"), new BigInteger("346") });
		assertEquals(new BigInteger("1111"), obj);
	}

	public void testA0012() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "return new BigInteger(a);";
		fn.importClass(BigInteger.class);
		fa.add(new NameAndType("a", QuasiPrimitive.STRING));
		obj = execclass(code,
				fn, new SymbolType("BigInteger"), fa, fl,
				new Class<?>[] { String.class },
				new Object[] { "765" });
		assertEquals(new BigInteger("765"), obj);
	}

	public void testA0013() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		int[] a;
		Object obj;
		String code;

		code = "return a.length;";
		fn.importClass(BigInteger.class);
		fa.add(new NameAndType("a", new ArrayType(Primitive.INT)));
		a = new int[10];
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { int[].class },
				new Object[] { a });
		assertEquals(10, ((Integer)obj).intValue());
	}

	public void testA0014() throws Exception {
		Classfile cf = new Classfile();
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa;
		List<NameAndType> fl;
		FieldInfo fi;
		Object obj;

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance("Test01"));
		cf.setSuperClass(ConstantClass.getInstance("java/lang/Object"));

		fn.addClass("Test01", new SyaroClass("Test01",
				false,
				Arrays.asList(new SyaroMethod[0]),
				Arrays.asList(new SyaroConstructor[0]),
				Arrays.asList(new SyaroField[] {
						new SyaroField("field", Primitive.INT, true)
				})));

		fi = new FieldInfo("field", "I");
		fi.setAccessFlags(FieldInfo.ACC_PUBLIC | FieldInfo.ACC_STATIC);
		cf.addField(fi);

		fa = new ArrayList<NameAndType>();
		fl = new ArrayList<NameAndType>();
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("test", Primitive.INT),
				fa,
				fl,
				fn,
				"Test01.field = 765; return Test01.field;");
		obj = invokeTestClass(cf, "test",
				new Class<?>[] { },
				new Object[] { });
		assertEquals(765, ((Integer)obj).intValue());
	}

	public void testA0015() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "a = new GridBagConstraints(); a.gridx = 765; return a.gridx;";
		fn.importClass(GridBagConstraints.class);
		fl.add(new NameAndType("a", new SymbolType("GridBagConstraints")));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(765, ((Integer)obj).intValue());
	}

	public void testA0016() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "try {" +
				" throw new RuntimeException();" +
				" return 961;" +
				"} catch(RuntimeException e) {" +
				" return 765;" +
				"}";
		fn.importClass(RuntimeException.class);
		fl.add(new NameAndType("e", new SymbolType("RuntimeException")));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(765, ((Integer)obj).intValue());
	}

	public void testA0017() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		System.setProperty("syaro.test", "961");
		code = "try {" +
				" a = 765; return a;" +
				"} finally {" +
				" System.setProperty(\"syaro.test\", \"346\");" +
				"}";
		fn.importClass(System.class);
		fl.add(new NameAndType("a", Primitive.INT));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(765, ((Integer)obj).intValue());
		assertEquals("346", System.getProperty("syaro.test"));
	}

	public void testA0018() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "for(a = 1; a < 10; a++) {" +
				" try {" +
				"  if(a == 5) break;" +
				" } finally {" +
				"  b += 1000;" +
				" }" +
				"}" +
				"return b;";
		fl.add(new NameAndType("a", Primitive.INT));
		fl.add(new NameAndType("b", Primitive.INT));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(5000, ((Integer)obj).intValue());
	}

	public void testA0019() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "b = 0;" +
				"try {" +
				" for(a = 10; a >= 0; a--) {" +
				"  try {" +
				"   10 / a;" +
				"  } finally {" +
				"   b += 10 - a;" +
				"  }" +
				" }" +
				"} catch(ArithmeticException e) { b += 1000; }" +
				"return b;";
		fn.importClass(ArithmeticException.class);
		fl.add(new NameAndType("a", Primitive.INT));
		fl.add(new NameAndType("b", Primitive.INT));
		fl.add(new NameAndType("e", new SymbolType("ArithmeticException")));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(1055, ((Integer)obj).intValue());
	}

	public void testA0020() throws Exception {
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		code = "try {" +
				" a = 0; a = 1 / 0;" +
				"} catch(ArithmeticException e) {" +
				" a += 765;" +
				"} finally {" +
				" a += 346;" +
				"}" +
				"return a;";
		fn.importClass(ArithmeticException.class);
		fl.add(new NameAndType("a", Primitive.INT));
		fl.add(new NameAndType("e", new SymbolType("ArithmeticException")));
		obj = execclass(code,
				fn, Primitive.INT, fa, fl,
				new Class<?>[] { },
				new Object[] { });
		assertEquals(1111, ((Integer)obj).intValue());
	}

	public void testA0021() throws Exception {
		Classfile cf = new Classfile();
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa = new ArrayList<NameAndType>();
		List<NameAndType> fl = new ArrayList<NameAndType>();
		Object obj;
		String code;

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance("Test01"));
		cf.setSuperClass(ConstantClass.getInstance("java/lang/Object"));

		code = "switch(a) {" +
				" case 72: r = \"chihaya\"; break;" +
				" case 85: r = \"ritsuko\"; break;" +
				" case 89:" +
				" case 90: r = \"takane\"; break;" +
				" default: r = \"unknown\"; break;" +
				"}" +
				"return r;";
		fa.add(new NameAndType("a", Primitive.INT));
		fl.add(new NameAndType("r", QuasiPrimitive.STRING));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("test", QuasiPrimitive.STRING),
				fa,
				fl,
				fn,
				code);

		Class<?> cls = createTestClass(cf);
		obj = invokeTestClass(cls, "test", 72);
		assertEquals("chihaya", obj);
		obj = invokeTestClass(cls, "test", 85);
		assertEquals("ritsuko", obj);
		obj = invokeTestClass(cls, "test", 89);
		assertEquals("takane", obj);
		obj = invokeTestClass(cls, "test", 90);
		assertEquals("takane", obj);
		obj = invokeTestClass(cls, "test", 70);
		assertEquals("unknown", obj);
	}

	static final String TO_DECIMAL =
			"BBASE2 = BigInteger.valueOf(100);\n" +
			"t = new byte[b.toString().length()];\n" +
			"x = b;\n" +
			"i = 0;\n" +
			"\n" +
			"while((y = x.divide(BBASE2)).signum() > 0) {\n" +
			"	t[i++] = x.mod(BBASE2).byteValue();\n" +
			" 	x = y;\n" +
			"}\n" +
			"t[i++] = x.byteValue();\n" +
			"\n" +
			"r = new byte[i];\n" +
			"for(j = 0; j < i; j++) {\n" +
			"	r[i - j - 1] = t[j];\n" +
			"}\n" +
			"return r;";

	static final String TO_INT =
			"if(b < 0) { return (b & 127) + 128; } else { return b; }";

	static final String SQRT =
			"BASE = 10;\n" +
			"BBASE = BigInteger.valueOf(10);\n" +
			"BBASE2 = BigInteger.valueOf(100);\n" +
			"i = 0;\n" +
			"x = 0;\n" +
			"\n" +
			"t = s = r = BigInteger.ZERO;\n" +
			"a = Test01.toDecimal(q);\n" +
			"for(; a[i] == 0; i++) {}\n" +
			"for(; i < a.length; i++) {\n" +
			"	x = 0;\n" +
			"	if(s.signum() == 0) {\n" +
			"		t = BigInteger.valueOf(Test01.toInt(a[i]));\n" +
			"		for(x = 0; x < BASE; x++) {\n" +
			"			if(x * x > t.intValue())  break;\n" +
			"		}\n" +
			"		s = BigInteger.valueOf(--x);\n" +
			"	} else {\n" +
			"		v = s;\n" +
			"		t = t.multiply(BBASE2).add(BigInteger.valueOf(\n" +
			"				Test01.toInt(a[i])));\n" +
			"\n" +
			"		do {\n" +
			"			s = v.multiply(BBASE).add(BigInteger.valueOf(x));\n" +
			"		} while(s.multiply(\n" +
			"				BigInteger.valueOf(x++)).compareTo(t) <= 0);\n" +
			"		x -= 2;\n" +
			"		s = v.multiply(BBASE).add(BigInteger.valueOf(x));\n" +
			"	}\n" +
			"	t = t.subtract(s.multiply(BigInteger.valueOf(x)));\n" +
			"	s = s.add(BigInteger.valueOf(x));\n" +
			"	r = r.multiply(BBASE).add(BigInteger.valueOf(x));\n" +
			"}\n" +
			"return r;";

	public void testSqrtToDecimal() throws Exception {
		Classfile cf = new Classfile();
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa;
		List<NameAndType> fl;
		byte[] res;

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance("Test01"));
		cf.setSuperClass(ConstantClass.getInstance("java/lang/Object"));

		fn.importClass(BigInteger.class);

		fa = new ArrayList<NameAndType>();
		fl = new ArrayList<NameAndType>();
		fa.add(new NameAndType("b", new SymbolType("BigInteger")));
		fl.add(new NameAndType("t", new ArrayType(Primitive.BYTE)));
		fl.add(new NameAndType("x", new SymbolType("BigInteger")));
		fl.add(new NameAndType("y", new SymbolType("BigInteger")));
		fl.add(new NameAndType("i", Primitive.INT));
		fl.add(new NameAndType("j", Primitive.INT));
		fl.add(new NameAndType("r", new ArrayType(Primitive.BYTE)));
		fl.add(new NameAndType("BBASE2", new SymbolType("BigInteger")));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("toDecimal", new ArrayType(Primitive.BYTE)),
				fa,
				fl,
				fn,
				TO_DECIMAL);
		res = (byte[])invokeTestClass(cf, "toDecimal",
				new Class<?>[] { BigInteger.class },
				new Object[] { new BigInteger("765") });
		assertEquals(res.length, 2);
		assertEquals(res[0], 7);
		assertEquals(res[1], 65);
	}

	public void testSqrt() throws Exception {
		Classfile cf = new Classfile();
		FunctionSpace fn = new FunctionSpace("Test01");
		List<NameAndType> fa;
		List<NameAndType> fl;
		Object obj;

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance("Test01"));
		cf.setSuperClass(ConstantClass.getInstance("java/lang/Object"));

		fn.importClass(BigInteger.class);
		fn.addClass("Test01", new SyaroClass("Test01",
				false,
				Arrays.asList(new SyaroMethod[] {
						new SyaroMethod("toDecimal",
								new ArrayType(Primitive.BYTE),
								Arrays.asList(new VariableType[] {
										new SymbolType("BigInteger")
										}),
								true),
						new SyaroMethod("toInt",
								Primitive.INT,
								Arrays.asList(new VariableType[] {
										Primitive.BYTE
										}),
								true)
				}),
				Arrays.asList(new SyaroConstructor[0]),
				Arrays.asList(new SyaroField[0])));

		fa = new ArrayList<NameAndType>();
		fl = new ArrayList<NameAndType>();
		fa.add(new NameAndType("b", Primitive.BYTE));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("toInt", Primitive.INT),
				fa,
				fl,
				fn,
				TO_INT);

		fa = new ArrayList<NameAndType>();
		fl = new ArrayList<NameAndType>();
		fa.add(new NameAndType("b", new SymbolType("BigInteger")));
		fl.add(new NameAndType("t", new ArrayType(Primitive.BYTE)));
		fl.add(new NameAndType("x", new SymbolType("BigInteger")));
		fl.add(new NameAndType("y", new SymbolType("BigInteger")));
		fl.add(new NameAndType("i", Primitive.INT));
		fl.add(new NameAndType("j", Primitive.INT));
		fl.add(new NameAndType("r", new ArrayType(Primitive.BYTE)));
		fl.add(new NameAndType("BBASE2", new SymbolType("BigInteger")));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("toDecimal", new ArrayType(Primitive.BYTE)),
				fa,
				fl,
				fn,
				TO_DECIMAL);

		fa = new ArrayList<NameAndType>();
		fl = new ArrayList<NameAndType>();
		fa.add(new NameAndType("q", new SymbolType("BigInteger")));
		fl.add(new NameAndType("a", new ArrayType(Primitive.BYTE)));
		fl.add(new NameAndType("t", new SymbolType("BigInteger")));
		fl.add(new NameAndType("s", new SymbolType("BigInteger")));
		fl.add(new NameAndType("v", new SymbolType("BigInteger")));
		fl.add(new NameAndType("r", new SymbolType("BigInteger")));
		fl.add(new NameAndType("i", Primitive.INT));
		fl.add(new NameAndType("x", Primitive.INT));
		fl.add(new NameAndType("BASE", Primitive.INT));
		fl.add(new NameAndType("BBASE", new SymbolType("BigInteger")));
		fl.add(new NameAndType("BBASE2", new SymbolType("BigInteger")));
		MethodCompiler.compile(cf,
				MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC,
				new NameAndType("sqrt", new SymbolType("BigInteger")),
				fa,
				fl,
				fn,
				SQRT);

		obj = invokeTestClass(cf, "sqrt",
				new Class<?>[] { BigInteger.class },
				new Object[] { new BigInteger("841") });
		assertEquals(new BigInteger("29"), obj);
	}

}
