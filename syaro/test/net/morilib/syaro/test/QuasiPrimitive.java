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

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public enum QuasiPrimitive implements VariableType {

	OBJECT("Ljava/lang/Object;", 0),
	STRING("Ljava/lang/String;", 1);

	private String descriptor;
	private int level;

	private QuasiPrimitive(String d, int l) {
		descriptor = d;
		level = l;
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isConversible(VariableType type) {
		if(level == 0) {
			return true;
		} else if(type instanceof QuasiPrimitive) {
			return level <= ((QuasiPrimitive)type).level;
		} else {
			return false;
		}
	}

	@Override
	public boolean isCastable(VariableType type) {
		return !type.isPrimitive();
	}

}
