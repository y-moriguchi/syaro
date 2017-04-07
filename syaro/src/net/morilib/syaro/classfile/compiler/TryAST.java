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

import java.util.ArrayList;
import java.util.List;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantClass;
import net.morilib.syaro.classfile.ExceptionTable;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.ALoad;
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.Jsr;
import net.morilib.syaro.classfile.code.Ret;

/**
 * @author Yuichiro MORIGUCHI
 *
 */
public class TryAST implements SAST {

	private SAST body;
	private List<CatchEntry> catches;
	private SAST _finally;

	public TryAST(SAST body, List<CatchEntry> catches, SAST _finally) {
		this.body = body;
		this.catches = new ArrayList<CatchEntry>(catches);
		this._finally = _finally;
	}

	@Override
	public void putCode(FunctionSpace functions, LocalVariableSpace space,
			Code code, List<Integer> breakIndices, int continueAddress,
			List<Integer> continueIndices,
			List<Integer> loopFinallyAddresses,
			List<Integer> returnFinallyAddresses) {
		List<Integer> nloop, nreturn;
		List<Goto> gotoList;
		List<Integer> indexList;
		ExceptionTable etable;
		Goto gt1, gt2, gtl;
		Jsr j1, j2;
		int startpc, endpc, gi1, gi2, gj, index;

		if(catches.size() > 0 && _finally == null) {
			startpc = code.getCurrentAddress();
			body.putCode(functions, space, code, breakIndices, continueAddress,
					continueIndices, loopFinallyAddresses, returnFinallyAddresses);
			endpc = code.getCurrentAddress();
			gt1 = new Goto();
			gi1 = code.addCode(gt1);
			for(CatchEntry ce : catches) {
				etable = new ExceptionTable();
				etable.setStartPc((short)startpc);
				etable.setEndPc((short)endpc);
				etable.setHandlerPc((short)code.getCurrentAddress());
				etable.setCatchType(ConstantClass.getInstance(ce.getType().getClassName(
						functions)));
				index = space.getIndex(ce.getVarialeName());
				if(index < 0) {
					throw new SemanticsException(
							"variable " + ce.getVarialeName() + " is not defined");
				}
				code.addCode(new AStore(index));
				ce.getAst().putCode(functions, space, code, breakIndices, continueAddress,
						continueIndices, loopFinallyAddresses, returnFinallyAddresses);
				code.addExceptionTable(etable);
			}
			gt1.setOffset(code.getCurrentOffset(gi1));
		} else {
			if(catches.size() == 0 && _finally == null) {
				throw new SemanticsException("invalid try-catch-finally");
			}
			gt2 = new Goto();
			gi2 = code.addCode(gt2);
			gj = code.getCurrentAddress();
			_finally.putCode(functions, space, code, breakIndices, continueAddress,
					continueIndices, loopFinallyAddresses, returnFinallyAddresses);
			code.addCode(new AStore(space.getIndex(null)));
			code.addCode(new Ret(space.getIndex(null)));
			gt2.setOffset(code.getCurrentOffset(gi2));

			nloop = new ArrayList<Integer>(loopFinallyAddresses);
			nreturn = new ArrayList<Integer>(returnFinallyAddresses);
			nloop.add(gj);
			nreturn.add(gj);
			startpc = code.getCurrentAddress();
			body.putCode(functions, space, code, breakIndices, continueAddress,
					continueIndices, nloop, nreturn);
			endpc = code.getCurrentAddress();

			gotoList = new ArrayList<Goto>();
			indexList = new ArrayList<Integer>();
			if(catches.size() > 0) {
				gtl = new Goto();
				gotoList.add(gtl);
				indexList.add(code.addCode(gtl));
				for(CatchEntry ce : catches) {
					etable = new ExceptionTable();
					etable.setStartPc((short)startpc);
					etable.setEndPc((short)endpc);
					etable.setHandlerPc((short)code.getCurrentAddress());
					etable.setCatchType(ConstantClass.getInstance(
							ce.getType().getClassName(functions)));
					index = space.getIndex(ce.getVarialeName());
					if(index < 0) {
						throw new SemanticsException(
								"variable " + ce.getVarialeName() + " is not defined");
					}
					code.addCode(new AStore(index));
					ce.getAst().putCode(functions, space, code, breakIndices,
							continueAddress, continueIndices,
							loopFinallyAddresses, returnFinallyAddresses);
					gtl = new Goto();
					gotoList.add(gtl);
					indexList.add(code.addCode(gtl));
					code.addExceptionTable(etable);
				}
				for(int i = 0; i < gotoList.size(); i++) {
					gotoList.get(i).setOffset(code.getCurrentOffset(indexList.get(i)));
				}
			}

			code.addCode(new Jsr(gj - code.getCurrentAddress()));
			gt1 = new Goto();
			gi1 = code.addCode(gt1);
			etable = new ExceptionTable();
			etable.setStartPc((short)startpc);
			etable.setEndPc((short)endpc);
			etable.setHandlerPc((short)code.getCurrentAddress());
			etable.setCatchType(null);
			code.addCode(new AStore(space.getMax()));
			code.addCode(new Jsr(gj - code.getCurrentAddress()));
			code.addCode(new ALoad(space.getMax()));
			code.addCode(Mnemonic.ATHROW);
			code.addExceptionTable(etable);
			gt1.setOffset(code.getCurrentOffset(gi1));
		}
	}

}
