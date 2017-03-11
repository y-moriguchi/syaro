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
package net.morilib.bcc;

import java.math.BigDecimal;

import net.morilib.syaro.classfile.Code;
import net.morilib.syaro.classfile.ConstantMethodref;
import net.morilib.syaro.classfile.Mnemonic;
import net.morilib.syaro.classfile.code.AStore;
import net.morilib.syaro.classfile.code.Goto;
import net.morilib.syaro.classfile.code.ILoad;
import net.morilib.syaro.classfile.code.IStore;
import net.morilib.syaro.classfile.code.If;
import net.morilib.syaro.classfile.code.Invokestatic;
import net.morilib.syaro.classfile.code.Invokevirtual;
import net.morilib.syaro.classfile.code.LConst;

/**
 * An utility class of ASTs.
 * 
 * @author Yuichiro MORIGUCHI
 */
public class Utils {

	/**
	 * local variable index of variable scale.
	 */
	public static final int SCALE_LOCAL_INDEX = 1;

	/**
	 * local variable index of counter of multiplication.
	 */
	public static final int POW_COUNT_LOCAL_INDEX = 2;

	private static class DecimalMethod implements BinaryOperator {

		private String name;

		private DecimalMethod(String name) {
			this.name = name;
		}

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			left.putCode(functions, space, code);
			right.putCode(functions, space, code);
			putDecimalMethod(code, name);
		}

	};

	/**
	 * code generator for add.
	 */
	public static final BinaryOperator ADD = new DecimalMethod("add");

	/**
	 * code generator for subtract.
	 */
	public static final BinaryOperator SUB = new DecimalMethod("subtract");

	/**
	 * code generator for multiply.
	 */
	public static final BinaryOperator MUL = new BinaryOperator() {

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			left.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP);
			right.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP_X1);
			putDecimalMethod(code, "multiply");
			code.addCode(Mnemonic.DUP_X2);
			code.addCode(Mnemonic.POP);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "scale", "()I")));
			code.addCode(Mnemonic.SWAP);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "scale", "()I")));
			code.addCode(new Invokestatic(ConstantMethodref.getInstance(
					"java/lang/Math", "max", "(II)I")));
			code.addCode(Mnemonic.pushInt(BigDecimal.ROUND_HALF_UP));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "setScale", "(II)Ljava/math/BigDecimal;")));
		}

	};

	/**
	 * code generator for divide.
	 */
	public static final BinaryOperator DIV = new BinaryOperator() {

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			putDecimalDiv(left, right, functions, space, code);
		}

	};

	/**
	 * code generator for remainder.
	 */
	public static final BinaryOperator REM = new BinaryOperator() {

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			left.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP);
			right.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP_X1);
			code.addCode(new ILoad(SCALE_LOCAL_INDEX));
			code.addCode(Mnemonic.pushInt(BigDecimal.ROUND_HALF_UP));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal",
					"divide",
					"(Ljava/math/BigDecimal;II)Ljava/math/BigDecimal;")));
			putDecimalMethod(code, "multiply");
			putDecimalMethod(code, "subtract");
		}

	};

	/**
	 * code generator for power.
	 */
	public static final BinaryOperator POW = new BinaryOperator() {

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			int icd1, icd2;
			If mcd1;
			Goto mcd2;

			right.putCode(functions, space, code);

			// compare the right value is more than or equals 0
			code.addCode(Mnemonic.DUP);
			code.addCode(Mnemonic.pushInt(0));
			code.addCode(Mnemonic.I2L);
			code.addCode(new Invokestatic(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "valueOf", "(J)Ljava/math/BigDecimal;")));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "compareTo", "(Ljava/math/BigDecimal;)I")));
			mcd1 = new If(If.Cond.GE);
			icd1 = code.addCode(mcd1);
			Mnemonic.throwException(code, "java/lang/ArithmeticException");
			mcd1.setOffset(code.getCurrentOffset(icd1));

			// compare the right value is less than 32768
			code.addCode(Mnemonic.DUP);
			code.addCode(Mnemonic.pushInt(32767));
			code.addCode(Mnemonic.I2L);
			code.addCode(new Invokestatic(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "valueOf", "(J)Ljava/math/BigDecimal;")));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "compareTo", "(Ljava/math/BigDecimal;)I")));
			mcd1 = new If(If.Cond.LE);
			icd1 = code.addCode(mcd1);
			Mnemonic.throwException(code, "java/lang/ArithmeticException");
			mcd1.setOffset(code.getCurrentOffset(icd1));

			// check the right value is integer
			code.addCode(Mnemonic.DUP);
			code.addCode(Mnemonic.DUP);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "longValue", "()J")));
			code.addCode(new Invokestatic(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "valueOf", "(J)Ljava/math/BigDecimal;")));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "compareTo", "(Ljava/math/BigDecimal;)I")));
			mcd1 = new If(If.Cond.EQ);
			icd1 = code.addCode(mcd1);
			Mnemonic.throwException(code, "java/lang/ArithmeticException");
			mcd1.setOffset(code.getCurrentOffset(icd1));

			// set to the counter
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "intValue", "()I")));
			code.addCode(new IStore(POW_COUNT_LOCAL_INDEX));
			putConstDecimal(code, true);
			left.putCode(functions, space, code);
			code.addCode(Mnemonic.DUP);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "scale", "()I")));
			code.addCode(Mnemonic.DUP_X2);
			code.addCode(Mnemonic.POP);
			code.addCode(Mnemonic.DUP_X1);

			// loop while the right value is more than 0
			icd2 = code.addCode(new ILoad(POW_COUNT_LOCAL_INDEX));
			code.addCode(Mnemonic.DUP);
			mcd1 = new If(If.Cond.LE);
			icd1 = code.addCode(mcd1);
			code.addCode(Mnemonic.pushInt(1));
			code.addCode(Mnemonic.ISUB);
			code.addCode(new IStore(POW_COUNT_LOCAL_INDEX));
			putDecimalMethod(code, "multiply");
			code.addCode(Mnemonic.SWAP);
			code.addCode(Mnemonic.DUP_X1);
			mcd2 = new Goto();
			mcd2.setOffset(code.getAddress(icd2) - code.getCurrentAddress());
			code.addCode(mcd2);

			// end
			mcd1.setOffset(code.getCurrentOffset(icd1));
			code.addCode(Mnemonic.POP);
			code.addCode(Mnemonic.POP);
			code.addCode(Mnemonic.SWAP);
			code.addCode(Mnemonic.POP);
			code.addCode(Mnemonic.SWAP);
			code.addCode(Mnemonic.pushInt(BigDecimal.ROUND_HALF_UP));
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "setScale", "(II)Ljava/math/BigDecimal;")));
		}

	};

	private static class RelMethod implements BinaryOperator {

		private If.Cond cond;

		private RelMethod(If.Cond cond) {
			this.cond = cond;
		}

		@Override
		public void putCode(AST left, AST right, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			int iif, igt;
			If mif;
			Goto mgt;

			left.putCode(functions, space, code);
			right.putCode(functions, space, code);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "compareTo", "(Ljava/math/BigDecimal;)I")));
			mif = new If(cond);
			iif = code.addCode(mif);
			putConstDecimal(code, false);
			mgt = new Goto();
			igt = code.addCode(mgt);
			mif.setOffset(code.getCurrentOffset(iif));
			putConstDecimal(code, true);
			mgt.setOffset(code.getCurrentOffset(igt));
		}

	};

	/**
	 * code generator for equal.
	 */
	public static final BinaryOperator EQ = new RelMethod(If.Cond.EQ);

	/**
	 * code generator for not equal.
	 */
	public static final BinaryOperator NE = new RelMethod(If.Cond.NE);

	/**
	 * code generator for greater than.
	 */
	public static final BinaryOperator GT = new RelMethod(If.Cond.GT);

	/**
	 * code generator for greater than or equal.
	 */
	public static final BinaryOperator GE = new RelMethod(If.Cond.GE);

	/**
	 * code generator for less than.
	 */
	public static final BinaryOperator LT = new RelMethod(If.Cond.LT);

	/**
	 * code generator for less than or equal.
	 */
	public static final BinaryOperator LE = new RelMethod(If.Cond.LE);

	/**
	 * generate code of invocation to operate.
	 * 
	 * @param code container of instructions
	 * @param name name of method
	 */
	public static void putDecimalMethod(Code code, String name) {
		code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
				"java/math/BigDecimal",
				name,
				"(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;")));
	}

	private static void putDecimalDiv(AST left, AST right, FunctionSpace functions,
			LocalVariableSpace space, Code code) {
		left.putCode(functions, space, code);
		right.putCode(functions, space, code);
		code.addCode(new ILoad(SCALE_LOCAL_INDEX));
		code.addCode(Mnemonic.pushInt(BigDecimal.ROUND_HALF_UP));
		code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
				"java/math/BigDecimal",
				"divide",
				"(Ljava/math/BigDecimal;II)Ljava/math/BigDecimal;")));
	}

	/**
	 * code generator for negate.
	 */
	public static final UnaryOperator NEG = new UnaryOperator() {

		@Override
		public void putCode(AST node, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			node.putCode(functions, space, code);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal",
					"negate",
					"()Ljava/math/BigDecimal;")));
		}

	};

	/**
	 * code generator for logical not.
	 */
	public static final UnaryOperator LNOT = new UnaryOperator() {

		@Override
		public void putCode(AST node, FunctionSpace functions,
				LocalVariableSpace space, Code code) {
			int lbl0, lbl1;

			node.putCode(functions, space, code);
			code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
					"java/math/BigDecimal", "signum", "()I")));
			lbl0 = code.addCode(new If(If.Cond.NE));
			putConstDecimal(code, true);
			lbl1 = code.addCode(new Goto());
			((If)code.getCode(lbl0)).setOffset(code.getCurrentOffset(lbl0));
			putConstDecimal(code, false);
			((Goto)code.getCode(lbl1)).setOffset(code.getCurrentOffset(lbl1));
		}

	};

	/**
	 * generate code to create boolean value.
	 * 
	 * @param code container of instructions
	 * @param val create 1 if val is true, 0 otherwise
	 */
	public static void putConstDecimal(Code code, boolean val) {
		code.addCode(new LConst(val ? 1 : 0));
		code.addCode(new Invokestatic(ConstantMethodref.getInstance(
				"java/math/BigDecimal", "valueOf", "(J)Ljava/math/BigDecimal;")));
	}

	/**
	 * gets the symbol name of the AST.
	 * 
	 * @param ast the AST
	 */
	public static String getVarName(AST ast) {
		if(!(ast instanceof SymbolAST)) {
			throw new RuntimeException("not a lvalue");
		}
		return ((SymbolAST)ast).getName();
	}

	private static int getLocalIndex(LocalVariableSpace space, AST ast) {
		return space.getIndex(Utils.getVarName(ast));
	}

	private static void setVarNotRef(AST node,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		String name;
		int idx;

		name = Utils.getVarName(node);
		idx = getLocalIndex(space, node);
		if(idx >= 0) {
			code.addCode(new AStore(idx));
		} else {
			throw new RuntimeException("undefined variable: " + name);
		}
	}

	/**
	 * puts the codes of storing value to a variable.
	 * 
	 * @param node the node
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 */
	public static void setVar(AST node,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		setVarNotRef(node, functions, space, code);
	}

	/**
	 * puts the code of the binary operator.
	 * 
	 * @param left the left value
	 * @param right the right value
	 * @param type the type of the binary operator
	 * @param functions the namespace of functions
	 * @param space the namespace of local variables
	 * @param code the container of codes
	 */
	public static void operatePrimitive(AST left, AST right,
			OperationMnemonics type,
			FunctionSpace functions,
			LocalVariableSpace space,
			Code code) {
		type.getCode().putCode(left, right, functions, space, code);
	}

}
