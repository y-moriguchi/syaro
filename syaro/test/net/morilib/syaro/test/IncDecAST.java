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

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantFieldref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.DConst;
import net.morilib.syaro.classfile.code.DStore;
import net.morilib.syaro.classfile.code.IConst;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.Putfield;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class IncDecAST implements AST {

	private boolean isInc;
	private boolean isPre;
	private AST node;

	public IncDecAST(boolean isInc, boolean isPre, AST node) {
		this.isInc = isInc;
		this.isPre = isPre;
		this.node = node;
	}

	public AST getNode() {
		return node;
	}

	private String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	private int getLocalIndex(LocalVariableSpace space, AST ast) {
		return space.getIndex(getVarName(ast));
	}

	private void setVar(FunctionSpace functions, LocalVariableSpace space,
			int idx, Code code) {
		VariableType type;
		String name;

		if(idx >= 0) {
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(new IStore(idx));
			} else {
				code.addCode(new DStore(idx));
			}
		} else {
			code.addCode(new ALoad(0));
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				code.addCode(Mnemonic.SWAP);
			} else {
				code.addCode(Mnemonic.DUP_X2);
				code.addCode(Mnemonic.POP);
			}
			name = getVarName(node);
			type = functions.getGlobal(name);
			code.addCode(new Putfield(new ConstantFieldref(
					functions.getClassname(), name, type.getDescriptor())));
		}
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Mnemonic val;
		int idx;

		if(node.getASTType(functions, space).equals(Primitive.INT)) {
			val = new IConst(isInc ? 1 : -1);
		} else {
			val = new DConst(1);
		}
		idx = getLocalIndex(space, node);
		if(isPre) {
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				node.putCode(functions, space, code);
				code.addCode(val);
				code.addCode(Mnemonic.IADD);
				code.addCode(Mnemonic.DUP);
				setVar(functions, space, idx, code);
			} else {
				node.putCode(functions, space, code);
				code.addCode(val);
				code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
				code.addCode(Mnemonic.DUP2);
				setVar(functions, space, idx, code);
			}
		} else {
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				node.putCode(functions, space, code);
				code.addCode(Mnemonic.DUP);
				code.addCode(val);
				code.addCode(Mnemonic.IADD);
				setVar(functions, space, idx, code);
			} else {
				node.putCode(functions, space, code);
				code.addCode(Mnemonic.DUP2);
				code.addCode(val);
				code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
				setVar(functions, space, idx, code);
			}
		}
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return node.getASTType(functions, space);
	}

}
