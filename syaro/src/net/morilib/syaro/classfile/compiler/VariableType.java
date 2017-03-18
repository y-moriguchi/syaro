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

/**
 * An interface represents types.
 * 
 * @author Yuichiro MORIGUCHI
 */
public interface VariableType {

	/**
	 * gets the descriptor of the type.
	 */
	public String getDescriptor();

	/**
	 * gets true if the type is primitive.
	 */
	public boolean isPrimitive();

	/**
	 * gets true if the type is conversible to the give type.
	 * @param type the type to check
	 */
	public boolean isConversible(VariableType type);

	/**
	 * gets true if the type is castable to the give type.
	 * @param type the type to check
	 */
	public boolean isCastable(VariableType type);

}
