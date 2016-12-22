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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This interface represents an information about a classfile.
 * 
 * @author Yuichiro MORIGUCHI
 */
public interface ClassInfo {

	/**
	 * gathers constant pools in this information.
	 * 
	 * @param gathered a container of constant pool
	 */
	public void gatherConstantPool(GatheredConstantPool gathered);

	/**
	 * generates a part of the classfile about this information.
	 * 
	 * @param gathered a container of constant pool
	 * @param ous an output stream
	 * @throws IOException
	 */
	public void generateCode(GatheredConstantPool gathered,
			DataOutputStream ous) throws IOException;

}
