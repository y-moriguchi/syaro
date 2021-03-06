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

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinition {

	private VariableType returnType;
	private List<VariableType> argumentTypes;

	public FunctionDefinition(VariableType ret, List<VariableType> args) {
		this.returnType = ret;
		this.argumentTypes = new ArrayList<VariableType>(args);
	}

	public VariableType getReturnType() {
		return returnType;
	}

	public List<VariableType> getArgumentTypes() {
		return argumentTypes;
	}

	public String getDescriptor() {
		StringBuilder b = new StringBuilder();

		b.append("(");
		for(VariableType v : argumentTypes) {
			b.append(v.getDescriptor());
		}
		b.append(")");
		b.append(returnType.getDescriptor());
		return b.toString();
	}

}