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
 * @author Yuichiro MORIGUCHI
 *
 */
public class CatchEntry {

	private VariableType type;
	private String variableName;
	private SAST ast;

	public CatchEntry(VariableType type, String variableName, SAST ast) {
		this.type = type;
		this.variableName = variableName;
		this.ast = ast;
	}

	public VariableType getType() {
		return type;
	}

	public String getVarialeName() {
		return variableName;
	}

	public SAST getAst() {
		return ast;
	}

}
