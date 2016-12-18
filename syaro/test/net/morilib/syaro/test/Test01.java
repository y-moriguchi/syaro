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

import java.io.FileOutputStream;

import net.morilib.syaro.classfile.Classfile;
import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.ConstantInteger;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.MethodInfo;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.Getstatic;
import net.morilib.syaro.classfile.code.Invokevirtual;
import net.morilib.syaro.classfile.code.LdcW;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class Test01 {

	public static void main(String[] args) throws Exception {
		Classfile cf = new Classfile();
		MethodInfo mi = new MethodInfo("main", "([Ljava/lang/String;)V");
		Code cd = new Code();

		cf.setMajorVersion(45);
		cf.setMinorVersion(3);
		cf.setAccessFlag(Classfile.ACC_PUBLIC);
		cf.setThisClass(new ConstantClass(args[0]));
		cf.setSuperClass(new ConstantClass("Ljava/lang/Object;"));
		mi.setAccessFlags(MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC);
		cd.setMaxStack(1024);
		cd.setMaxLocals(1);
		cd.addCode(new Getstatic(new ConstantFieldref(
				"java/lang/System", "out", "Ljava/io/PrintStream;")));
		cd.addCode(new LdcW(new ConstantInteger(765)));
		cd.addCode(new Invokevirtual(new ConstantMethodref(
				"java/io/PrintStream", "println", "(I)V")));
		cd.addCode(Mnemonic.RETURN);
		mi.addAttribute(cd);
		cf.addMethod(mi);

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
