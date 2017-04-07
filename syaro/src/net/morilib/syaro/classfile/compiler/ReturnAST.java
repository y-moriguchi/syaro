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

import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.DLoad;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.FLoad;
import net.morilib.syaro.classfile.code.FStore;
import net.morilib.syaro.classfile.code.ILoad;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Jsr;
import net.morilib.syaro.classfile.code.LLoad;
import net.morilib.syaro.classfile.code.LStore;

/**
 * An abstract syntax tree for return statement.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class ReturnAST implements SAST {

	private AST expr;

	/**
	 * constructs AST for return statement.
	 * 
	 * @param expr return value
	 */
	public ReturnAST(AST expr) {
		this.expr = expr;
	}

	private void putFinally(Code code, LocalVariableSpace space,
			List<Integer> returnFinallyAddresses) {
		VariableType rettype = space.getThisReturnType();

		if(returnFinallyAddresses.size() > 0) {
			if(rettype.isConversible(Primitive.INT)) {
				code.addCode(new IStore(space.getMax()));
			} else if(rettype.equals(Primitive.LONG)) {
				code.addCode(new LStore(space.getMax()));
			} else if(rettype.equals(Primitive.FLOAT)) {
				code.addCode(new FStore(space.getMax()));
			} else if(rettype.equals(Primitive.DOUBLE)) {
				code.addCode(new DStore(space.getMax()));
			} else {
				code.addCode(new AStore(space.getMax()));
			}
			for(int addr : returnFinallyAddresses) {
				code.addCode(new Jsr(addr - code.getCurrentAddress()));
			}
			if(rettype.isConversible(Primitive.INT)) {
				code.addCode(new ILoad(space.getMax()));
			} else if(rettype.equals(Primitive.LONG)) {
				code.addCode(new LLoad(space.getMax()));
			} else if(rettype.equals(Primitive.FLOAT)) {
				code.addCode(new FLoad(space.getMax()));
			} else if(rettype.equals(Primitive.DOUBLE)) {
				code.addCode(new DLoad(space.getMax()));
			} else {
				code.addCode(new ALoad(space.getMax()));
			}
		}
	}

	private void putFinally(Code code, List<Integer> returnFinallyAddresses) {
		for(int addr : returnFinallyAddresses) {
			code.addCode(new Jsr(addr - code.getCurrentAddress()));
		}
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code,
			List<Integer> breakIndices,
			int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses) {
		VariableType t;
		Primitive ep;

		if(expr != null) {
			expr.putCode(functions, space, code);
			putFinally(code, space, returnFinallyAddresses);
			t = expr.getASTType(functions, space);
			if(!t.isConversible(space.getThisReturnType())) {
				throw new SemanticsException("type mismatch");
			} else if(space.getThisReturnType().isPrimitive()) {
				ep = (Primitive)space.getThisReturnType();
				if(ep.isConversible(Primitive.INT)) {
					code.addCode(Mnemonic.IRETURN);
				} else if(ep.equals(Primitive.LONG)) {
					Utils.putConversionLong(ep, code);
					code.addCode(Mnemonic.LRETURN);
				} else if(ep.equals(Primitive.FLOAT)) {
					Utils.putConversionFloat(ep, code);
					code.addCode(Mnemonic.FRETURN);
				} else if(ep.equals(Primitive.DOUBLE)) {
					Utils.putConversionDouble(ep, code);
					code.addCode(Mnemonic.DRETURN);
				} else {
					throw new SemanticsException("subroutine must not return value");
				}
			} else {
				code.addCode(Mnemonic.ARETURN);
			}
		} else {
			putFinally(code, returnFinallyAddresses);
			code.addCode(Mnemonic.RETURN);
		}
	}

}
