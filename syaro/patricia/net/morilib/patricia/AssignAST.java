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
package net.morilib.patricia;

import java.util.List;

import net.morilib.syaro.classfile.Code;

/**
 * An abstract syntax tree for statements of assignment.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class AssignAST implements SAST {

	private AST left;
	private AST right;

	/**
	 * creates statement AST of assignment.
	 * 
	 * @param name the name of variable
	 * @param right the AST of right value
	 */
	public AssignAST(String name, AST right) {
		this.left = new SymbolAST(name);
		this.right = right;
	}

	private void putCodeReference(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Utils.putCodeArrayRef(left, functions, space, code);
		right.putCode(functions, space, code);
		Utils.setVar(left, functions, space, code);
	}

	private void putCodePrimitive(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Primitive lp, rp;

		lp = (Primitive)left.getASTType(functions, space);
		rp = (Primitive)right.getASTType(functions, space);
		Utils.putCodeArrayRef(left, functions, space, code);
		right.putCode(functions, space, code);
		if(!rp.isConversible(lp)) {
			throw new RuntimeException("type mismatch");
		} else if(lp.isConversible(Primitive.BOOLEAN)) {
			// do nothing
		} else if(lp.isConversible(Primitive.INT)) {
			// do nothing
		} else if(lp.equals(Primitive.LONG)) {
			Utils.putConversionLong(rp, code);
		} else if(lp.equals(Primitive.FLOAT)) {
			Utils.putConversionFloat(rp, code);
		} else {
			Utils.putConversionDouble(rp, code);
		}
		Utils.setVar(left, functions, space, code);
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code, List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices) {
		VariableType l, r;

		l = left.getASTType(functions, space);
		r = right.getASTType(functions, space);
		if(!l.isConversible(r)) {
			throw new RuntimeException("type mismatch");
		} else if(!l.isPrimitive() || !r.isPrimitive()) {
			putCodeReference(functions, space, code);
		} else {
			putCodePrimitive(functions, space, code);
		}
	}

}
