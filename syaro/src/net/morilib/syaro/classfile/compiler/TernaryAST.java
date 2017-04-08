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

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.If;

/**
 * An abstract syntax tree of the ternary operator.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class TernaryAST implements AST {

	private AST cond;
	private AST iftrue;
	private AST iffalse;

	/**
	 * creates the AST of ternary operator.
	 * 
	 * @param cond the AST of condition value
	 * @param iftrue the AST of value which returns if true
	 * @param iffalse the AST of value which returns if false
	 */
	public TernaryAST(AST cond, AST iftrue, AST iffalse) {
		this.cond = cond;
		this.iftrue = iftrue;
		this.iffalse = iffalse;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code) {
		VariableType tt, tf;
		int ifa, gta;
		If _if;
		Goto _gt;

		tt = iftrue.getASTType(functions, space);
		tf = iffalse.getASTType(functions, space);
		cond.putCode(functions, space, code);
		_if = new If(If.Cond.EQ);
		ifa = code.addCode(_if);
		iftrue.putCode(functions, space, code);
		if(tt.isConversible(tf)) {
			Utils.putConversion(tt, code, tf);
		}
		_gt = new Goto();
		gta = code.addCode(_gt);
		_if.setOffset(code.getCurrentOffset(ifa));
		iffalse.putCode(functions, space, code);
		if(tf.isConversible(tt)) {
			Utils.putConversion(tf, code, tt);
		}
		_gt.setOffset(code.getCurrentOffset(gta));
	}

	@Override
	public VariableType getASTType(FunctionSpace functions,
			LocalVariableSpace space) {
		VariableType tt, tf;

		tt = iftrue.getASTType(functions, space);
		tf = iffalse.getASTType(functions, space);
		if(tt.isConversible(tf)) {
			return tf;
		} else if(tf.isConversible(tt)) {
			return tt;
		} else {
			throw new SemanticsException("type of result values is not conversible");
		}
	}

}
