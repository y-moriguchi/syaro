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

import java.io.FileOutputStream;
import java.util.ArrayList;

import net.morilib.syaro.classfile.Classfile;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.compiler.FunctionSpace;
import net.morilib.syaro.classfile.compiler.MethodCompiler;
import net.morilib.syaro.classfile.compiler.NameAndType;
import net.morilib.syaro.classfile.compiler.Primitive;

/**
 * Test code for MethodCompiler.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Test01 {

	public static void main(String[] args) throws Exception {
		Classfile cf = new Classfile();
		FunctionSpace fn = new FunctionSpace(args[0]);

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(ConstantClass.getInstance(args[0]));
		cf.setSuperClass(ConstantClass.getInstance("Ljava/lang/Object;"));
		MethodCompiler.compile(cf, "test", Primitive.INT,
				new ArrayList<NameAndType>(), fn,
				"return 1+2*3;");

		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(args[0] + ".class");
			cf.generateClassFile(fs);
		} finally {
			if(fs != null) {
				fs.close();
			}
		}
	}

}
