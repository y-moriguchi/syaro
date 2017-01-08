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
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.DConst;
import net.morilib.syaro.classfile.code.FConst;
import net.morilib.syaro.classfile.code.IConst;

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

	private int getLocalIndex(LocalVariableSpace space, AST ast) {
		return space.getIndex(Utils.getVarName(ast));
	}

	@Override
	public void putCode(FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		Mnemonic val;
		int idx;

		if(node.getASTType(functions, space).equals(Primitive.INT)) {
			val = new IConst(isInc ? 1 : -1);
		} else if(node.getASTType(functions, space).equals(Primitive.INT)) {
			val = new FConst(1);
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
				Utils.setVar(node, functions, space, idx, code);
			} else {
				node.putCode(functions, space, code);
				code.addCode(val);
				if(node.getASTType(functions, space).equals(Primitive.FLOAT)) {
					code.addCode(isInc ? Mnemonic.FADD : Mnemonic.FSUB);
					code.addCode(Mnemonic.DUP);
				} else {
					code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
					code.addCode(Mnemonic.DUP2);
				}
				Utils.setVar(node, functions, space, idx, code);
			}
		} else {
			if(node.getASTType(functions, space).equals(Primitive.INT)) {
				node.putCode(functions, space, code);
				code.addCode(Mnemonic.DUP);
				code.addCode(val);
				code.addCode(Mnemonic.IADD);
				Utils.setVar(node, functions, space, idx, code);
			} else {
				node.putCode(functions, space, code);
				if(node.getASTType(functions, space).equals(Primitive.FLOAT)) {
					code.addCode(Mnemonic.DUP);
					code.addCode(val);
					code.addCode(isInc ? Mnemonic.FADD : Mnemonic.FSUB);
				} else {
					code.addCode(Mnemonic.DUP2);
					code.addCode(val);
					code.addCode(isInc ? Mnemonic.DADD : Mnemonic.DSUB);
				}
				Utils.setVar(node, functions, space, idx, code);
			}
		}
	}

	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		return node.getASTType(functions, space);
	}

}
