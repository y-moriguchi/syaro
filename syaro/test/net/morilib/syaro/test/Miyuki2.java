/*
 * Copyright (c) 2016, Yuichiro MORIGUCHI
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * * Neither the name of the Yuichiro MORIGUCHI nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Yuichiro MORIGUCHI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * morilib Nina Version: 0.4.16.612
 *
 * Nina homepage:      http://nina.morilib.net/
 * Plugin update site: http://nina.morilib.net/update-site/
 */
package net.morilib.syaro.test;

import java.io.FileOutputStream;
import java.io.StringReader;
import net.morilib.syaro.classfile.*;
import net.morilib.syaro.classfile.code.*;

/**
 * a high order calculator
 * 
 * @author Yuichiro MORIGUCHI
 */
public  class Miyuki2   {

	/* @@@-PARSER-CODE-START-@@@ */
	static class TokenException extends RuntimeException {
	}

	static abstract class Engine {
		abstract int step(int c) throws java.io.IOException;
		abstract boolean accepted();
		abstract boolean isDead();
		abstract boolean isEmptyTransition();
		abstract int execaction(int c);
		abstract boolean isend();
		abstract int recover(Exception e);
		abstract int deadState();
		abstract int stateSize();
		abstract int finallyState();
	}
	static final int INVALIDTOKEN = 0x7fff7fff;
	private static final int NINA_BEGIN = -2;
	private static final int NINA_EOF = -1;
	private static final int NINA_ACCEPT = -8;
	private static final int NINA_FAIL = -9;
	private static final int NINA_HALT_ACCEPT = -91;
	private static final int NINA_HALT_REJECT = -72;
	private static final int NINA_YIELD = -85;
	private static final int NINA_STACKLEN = 72;
	static final int NINA_DISCARDSTATE = 0x40000000;
	static final int INITIAL = 0;
	static final int INDENT = 1;

	private int STATE;
	private int[] __sts = new int[NINA_STACKLEN];
	private Engine[] __stk = new Engine[NINA_STACKLEN];
	private Object[][] __stv = new Object[NINA_STACKLEN][];
	private int __slen = 0;
	private int unread = -1;

	Object _v;
	java.util.List<Object> _l;
	Object yieldObject;
	Throwable exception;

	StringBuffer $buffer;
	StringBuffer $bufferUnicode;
	int $int;
	java.math.BigInteger $bigint;
	Number $num;

	java.util.Stack<java.io.Reader> streamStack =
			new java.util.Stack<java.io.Reader>();

	void _initlist() {
		_l = new java.util.ArrayList<Object>();
	}

	void _addlist(Object x) {
		_l.add(x);
	}

	private int __lookahead_state;
	private int __lookahead_mark = -1;
	private int[] __lookahead = null;
	private int __lookahead_ptr = -1;
	private int[] __lookaheadw = null;
	private int __lookaheadw_ptr = -1;
	private boolean __lookahead_ok = true;

	private int _unreadl = -1;

	void INCLUDE(java.io.Reader rd) {
		if(__lookahead_ptr >= 0) {
			throw new IllegalStateException();
		}
		streamStack.push(rd);
	}

	void INCLUDE(String name) throws java.io.IOException {
		java.io.InputStream ins;

		ins = new java.io.FileInputStream(name);
		INCLUDE(new java.io.InputStreamReader(ins));
	}

	int readComment() throws java.io.IOException {
		return streamStack.peek().read();
	}
	int _read1l() throws java.io.IOException {
		int c;

		while(streamStack.size() > 0) {
			if((c = readComment()) >= 0) {
				return c;
			} else if(streamStack.size() > 1) {
				streamStack.pop().close();
			} else {
				streamStack.pop();
			}
		}
		return NINA_EOF;
	}

	int _read1() throws java.io.IOException {
		int c;

		if(_unreadl != -1) {
			c = _unreadl;
			_unreadl = -1;
		} else if((c = _read1l()) == '\r' && (c = _read1l()) != '\n') {
			_unreadl = c;
			c = '\r';
		}
		return c;
	}

	private int _read() throws java.io.IOException {
		int c;

		while(true) {
			if(unread != -1) {

				c = unread;
				unread = -1;
				__logprint("Read unread: ", c);
			} else if(__lookahead_ptr >= 0) {
				if(__lookahead_ptr < __lookahead.length) {
					c = __lookahead[__lookahead_ptr++];
				} else {
					__lookahead = null;
					__lookahead_ptr = -1;
					c = _read();
				}
				__logprint("Read Lookahead: ", c);
			} else if((c = _read1()) != -1) {
				__logprint("Read: ", c);
			} else {
				__logprint("Read end-of-file");
			}
			return c;
		}
	}

	void UNGET(int c) {
		unread = c;
		__logprint("Set unread: ", c);
	}

	void __sleep(int m) {
		try {
			Thread.sleep(m);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void __logprint(String s, int c) {
	}

	private void __logopen() {
	}

	private void __logprint(String s) {
	}

	private void __logclose() {
	}

	private void __puttrace() {
	}

	void LOOKAHEAD(int c) {
		int[] a;

		if(__lookaheadw == null) {
			__lookahead_state = STATE;
			__lookaheadw = new int[72];
			__lookaheadw_ptr = 0;
			__lookaheadw[__lookaheadw_ptr++] = c;
		} else if(__lookaheadw_ptr < __lookaheadw.length) {
			__lookaheadw[__lookaheadw_ptr++] = c;
		} else {
			a = new int[__lookaheadw.length * 2];
			System.arraycopy(__lookaheadw, 0, a, 0,
					__lookaheadw.length);
			__lookaheadw = a;
			__lookaheadw[__lookaheadw_ptr++] = c;
		}
	}

	private void __copy_lookahead(int p) {
		int[] a;
		int[] lookaheadOrg = __lookahead;
		int lookaheadLength = __lookahead != null ? __lookahead.length : __lookahead_ptr;

		if(__lookahead_ptr > 0) {
			a = new int[__lookahead.length - __lookahead_ptr];
			System.arraycopy(__lookahead, __lookahead_ptr, a, 0, a.length);
			__lookahead = a;
		}

		a = new int[__lookaheadw_ptr + (lookaheadLength - __lookahead_ptr)];
		System.arraycopy(__lookaheadw, 0, a, 0, __lookaheadw_ptr);
		if(lookaheadOrg != null) {
			System.arraycopy(lookaheadOrg,
					__lookahead_ptr,
					a,
					__lookaheadw_ptr,
					lookaheadLength - __lookahead_ptr);
		}
		__lookahead = a;
		__lookahead_ptr = p;
		__lookaheadw = null;
		__lookaheadw_ptr = -1;
	}

	void LOOKAHEAD_COMMIT() {
		if(__lookahead_mark < 0) {
			__lookaheadw = null;
			__lookaheadw_ptr = -1;
		} else {
			__copy_lookahead(__lookahead_mark);
		}
		__lookahead_mark = -1;
		__logprint("Commit Lookahead");
	}

	void LOOKAHEAD_RB() {
		__copy_lookahead(0);
		STATE = __lookahead_state;
		__lookahead_ok = false;
		__lookahead_mark = -1;
		__logprint("Rollback Lookahead");
	}

	void LOOKAHEAD_MARK() {
		__lookahead_mark = __lookaheadw_ptr;
	}

	void LOOKAHEAD_MARK_INIT() {
		__lookahead_mark = 0;
	}


	private int elem_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				$buffer = new StringBuffer();UNGET($c);
				STATE = 1;
				return 1;
			} else if(($c == '(')) {
				STATE = 2;
				return 1;
			}
			return 0;
		case 2:
			if($c >= 0) {
				__stkpush(3, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 3:
			if(($c == ')')) {
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			return 0;
		case 1:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 1;
				return 1;
			} else if($c < 0) {
				$int=Integer.parseInt($buffer.toString(), 10);
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				$int=Integer.parseInt($buffer.toString(), 10);UNGET($c);
				STATE = 5;
				return 1;
			}
		case 5:
			return 0;
		}
		return 0;
	}

	private boolean elem_accepted() {
		return (STATE == 4 ||
				STATE == 5);
	}

	int elem_execaction(int  $c) {
		switch(STATE) {
		case 1:
			break;
		case 4:
			break;
		case 2:
			break;
		case 5:
			konst($int);
			break;
		case 3:
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean elem_isend() {
		return (STATE == 1);
	}

	private final Engine ENGINE_elem = new Engine() {

		int step(int c) throws java.io.IOException {
			return elem_step(c);
		}

		boolean accepted() {
			return elem_accepted();
		}

		int execaction(int c) {
			return elem_execaction(c);
		}

		boolean isend() {
			return elem_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 6;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 4 ||
				STATE == 5);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "elem";
		}

	};

	private int unary_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 1;
				return 1;
			} else if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 2;
				return 1;
			} else if(($c == '~')) {
				LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(4, ENGINE_elem);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 4:
			return 0;
		case 3:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(5, ENGINE_elem);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 5:
			return 0;
		case 2:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 2;
				return 1;
			} else if($c >= 0) {
				__stkpush(6, ENGINE_elem);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 6:
			return 0;
		case 1:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			} else if($c >= 0) {
				__stkpush(7, ENGINE_elem);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 7:
			return 0;
		}
		return 0;
	}

	private boolean unary_accepted() {
		return (STATE == 4 ||
				STATE == 5 ||
				STATE == 6 ||
				STATE == 7);
	}

	int unary_execaction(int  $c) {
		switch(STATE) {
		case 6:
			uminus();
			break;
		case 5:
			bnot();
			break;
		case 0:
			break;
		case 4:
			break;
		case 3:
			break;
		case 1:
			break;
		case 2:
			break;
		case 7:
			break;
		}
		return 1;
	}

	boolean unary_isend() {
		return false;
	}

	private final Engine ENGINE_unary = new Engine() {

		int step(int c) throws java.io.IOException {
			return unary_step(c);
		}

		boolean accepted() {
			return unary_accepted();
		}

		int execaction(int c) {
			return unary_execaction(c);
		}

		boolean isend() {
			return unary_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 8;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 4 ||
				STATE == 5 ||
				STATE == 6 ||
				STATE == 7);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "unary";
		}

	};

	private int shft_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_poly);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 3:
			if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				__stkpush(6, ENGINE_poly);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 6:
				STATE = 1;
				return 1;
		case 2:
			if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 7;
				return 1;
			}
			return 0;
		case 7:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 8;
				return 1;
			}
			return 0;
		case 8:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 8;
				return 1;
			} else if($c >= 0) {
				__stkpush(9, ENGINE_poly);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean shft_accepted() {
		return (STATE == 1 ||
				STATE == 6 ||
				STATE == 9);
	}

	int shft_execaction(int  $c) {
		switch(STATE) {
		case 5:
			break;
		case 6:
			lshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 8:
			break;
		case 3:
			break;
		case 7:
			break;
		case 9:
			rshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 2:
			break;
		case 0:
			break;
		case 4:
			break;
		}
		return 1;
	}

	boolean shft_isend() {
		return (STATE == 6 ||
				STATE == 9);
	}

	private final Engine ENGINE_shft = new Engine() {

		int step(int c) throws java.io.IOException {
			return shft_step(c);
		}

		boolean accepted() {
			return shft_accepted();
		}

		int execaction(int c) {
			return shft_execaction(c);
		}

		boolean isend() {
			return shft_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 10;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 6 ||
				STATE == 9);
		}

		public String toString() {
			return "shft";
		}

	};

	private int band_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '&')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if((__l__ && $c >= 0 && $c <= '%') || (__l__ && $c >= '\'' && $c <= '<') || (__l__ && $c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if((__l__ && $c >= 0 && $c <= 2147483647)) {
				LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			} else if($c < 0) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c == '&')) {
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				__stkpush(6, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 6:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean band_accepted() {
		return (STATE == 1 ||
				STATE == 6);
	}

	int band_execaction(int  $c) {
		switch(STATE) {
		case 3:
			break;
		case 0:
			break;
		case 6:
			band(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 4:
			break;
		case 5:
			break;
		}
		return 1;
	}

	boolean band_isend() {
		return (STATE == 3 ||
				STATE == 6);
	}

	private final Engine ENGINE_band = new Engine() {

		int step(int c) throws java.io.IOException {
			return band_step(c);
		}

		boolean accepted() {
			return band_accepted();
		}

		int execaction(int c) {
			return band_execaction(c);
		}

		boolean isend() {
			return band_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 7;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 6);
		}

		public String toString() {
			return "band";
		}

	};

	private int term_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '*')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '%')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if((__l__ && $c == '/')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 4:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				__stkpush(6, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 6:
				STATE = 1;
				return 1;
		case 3:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 7;
				return 1;
			}
			return 0;
		case 7:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 7;
				return 1;
			} else if($c >= 0) {
				__stkpush(8, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 8:
				STATE = 1;
				return 1;
		case 2:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 9;
				return 1;
			}
			return 0;
		case 9:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 9;
				return 1;
			} else if($c >= 0) {
				__stkpush(10, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 10:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean term_accepted() {
		return (STATE == 1 ||
				STATE == 6 ||
				STATE == 8 ||
				STATE == 10);
	}

	int term_execaction(int  $c) {
		switch(STATE) {
		case 2:
			break;
		case 4:
			break;
		case 6:
			div(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			mod(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 7:
			break;
		case 5:
			break;
		case 3:
			break;
		case 9:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 10:
			mul(((AST)(__stv[__slen - 1][1])));
			break;
		}
		return 1;
	}

	boolean term_isend() {
		return (STATE == 6 ||
				STATE == 8 ||
				STATE == 10);
	}

	private final Engine ENGINE_term = new Engine() {

		int step(int c) throws java.io.IOException {
			return term_step(c);
		}

		boolean accepted() {
			return term_accepted();
		}

		int execaction(int c) {
			return term_execaction(c);
		}

		boolean isend() {
			return term_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 11;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 6 ||
				STATE == 8 ||
				STATE == 10);
		}

		public String toString() {
			return "term";
		}

	};

	private int poly_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_term);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '-')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '+')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 3:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 4;
				return 1;
			} else if($c >= 0) {
				__stkpush(5, ENGINE_term);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 5:
				STATE = 1;
				return 1;
		case 2:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 6;
				return 1;
			}
			return 0;
		case 6:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			} else if($c >= 0) {
				__stkpush(7, ENGINE_term);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 7:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean poly_accepted() {
		return (STATE == 1 ||
				STATE == 5 ||
				STATE == 7);
	}

	int poly_execaction(int  $c) {
		switch(STATE) {
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 3:
			break;
		case 7:
			sub(((AST)(__stv[__slen - 1][1])));
			break;
		case 4:
			break;
		case 2:
			break;
		case 5:
			add(((AST)(__stv[__slen - 1][1])));
			break;
		case 6:
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean poly_isend() {
		return (STATE == 5 ||
				STATE == 7);
	}

	private final Engine ENGINE_poly = new Engine() {

		int step(int c) throws java.io.IOException {
			return poly_step(c);
		}

		boolean accepted() {
			return poly_accepted();
		}

		int execaction(int c) {
			return poly_execaction(c);
		}

		boolean isend() {
			return poly_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 8;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 5 ||
				STATE == 7);
		}

		public String toString() {
			return "poly";
		}

	};

	private int bxor_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_band);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '^')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if(($c >= 0 && $c <= '<') || ($c >= '>' && $c <= 2147483647)) {
				LOOKAHEAD_MARK();LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(4, ENGINE_band);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 4:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean bxor_accepted() {
		return (STATE == 1 ||
				STATE == 4);
	}

	int bxor_execaction(int  $c) {
		switch(STATE) {
		case 4:
			bxor(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 0:
			break;
		case 2:
			break;
		case 3:
			break;
		}
		return 1;
	}

	boolean bxor_isend() {
		return (STATE == 4);
	}

	private final Engine ENGINE_bxor = new Engine() {

		int step(int c) throws java.io.IOException {
			return bxor_step(c);
		}

		boolean accepted() {
			return bxor_accepted();
		}

		int execaction(int c) {
			return bxor_execaction(c);
		}

		boolean isend() {
			return bxor_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 5;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 4);
		}

		public String toString() {
			return "bxor";
		}

	};

	private int expr_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_bior);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			return 0;
		}
		return 0;
	}

	private boolean expr_accepted() {
		return (STATE == 1);
	}

	int expr_execaction(int  $c) {
		switch(STATE) {
		case 1:
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean expr_isend() {
		return false;
	}

	private final Engine ENGINE_expr = new Engine() {

		int step(int c) throws java.io.IOException {
			return expr_step(c);
		}

		boolean accepted() {
			return expr_accepted();
		}

		int execaction(int c) {
			return expr_execaction(c);
		}

		boolean isend() {
			return expr_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 2;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 1);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "expr";
		}

	};

	private int miyuki2_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			return 0;
		}
		return 0;
	}

	private boolean miyuki2_accepted() {
		return (STATE == 1);
	}

	int miyuki2_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 1:
			break;
		}
		return 1;
	}

	boolean miyuki2_isend() {
		return false;
	}

	private final Engine ENGINE_miyuki2 = new Engine() {

		int step(int c) throws java.io.IOException {
			return miyuki2_step(c);
		}

		boolean accepted() {
			return miyuki2_accepted();
		}

		int execaction(int c) {
			return miyuki2_execaction(c);
		}

		boolean isend() {
			return miyuki2_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 2;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 1);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "miyuki2";
		}

	};

	private int bior_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_bxor);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '|')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if((__l__ && $c >= 0 && $c <= '<') || (__l__ && $c >= '>' && $c <= '{') || (__l__ && $c >= '}' && $c <= 2147483647)) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if((__l__ && $c >= 0 && $c <= 2147483647)) {
				LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			} else if($c < 0) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c == '|')) {
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				__stkpush(6, ENGINE_bxor);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 6:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean bior_accepted() {
		return (STATE == 1 ||
				STATE == 6);
	}

	int bior_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 6:
			bior(((AST)(__stv[__slen - 1][1])));
			break;
		case 4:
			break;
		case 3:
			break;
		case 5:
			break;
		case 2:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		}
		return 1;
	}

	boolean bior_isend() {
		return (STATE == 3 ||
				STATE == 6);
	}

	private final Engine ENGINE_bior = new Engine() {

		int step(int c) throws java.io.IOException {
			return bior_step(c);
		}

		boolean accepted() {
			return bior_accepted();
		}

		int execaction(int c) {
			return bior_execaction(c);
		}

		boolean isend() {
			return bior_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 7;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 6);
		}

		public String toString() {
			return "bior";
		}

	};

	void __stkpush(int st, Engine en) {
		Object[][] c;
		Engine[] b;
		int[] a;

		if(__slen >= __sts.length) {
			a = new int[__sts.length * 2];
			b = new Engine[__stk.length * 2];
			c = new Object[__stk.length * 2][];
			System.arraycopy(__sts, 0, a, 0, __sts.length);
			System.arraycopy(__stk, 0, b, 0, __stk.length);
			System.arraycopy(__stv, 0, c, 0, __stv.length);
			__sts = a;
			__stk = b;
			__stv = c;
		}
		__sts[__slen] = st;
		__stk[__slen] = en;
		__stv[__slen++] = new Object[en.stateSize()];
	}

	private int _parse(int x, Boolean rt, boolean skip,
			int[] st) throws java.io.IOException {
		boolean b = false, p = skip;
		int c = x;
		Engine en;
		int a;

		b = __stk[__slen - 1].accepted();
		if(rt.booleanValue()) {
			switch(__stk[__slen - 1].execaction(NINA_BEGIN)) {
			case NINA_ACCEPT:
				__logprint("accept " + __stk[__slen - 1]);
				st[0] = NINA_ACCEPT;  return -1;
			case NINA_FAIL:
				__logprint("match failed: begin");
				__puttrace();
				st[0] = NINA_FAIL;  return -1;
			case NINA_HALT_ACCEPT:
				__logprint("machine halted: begin");
				st[0] = NINA_HALT_ACCEPT;  return -1;
			case NINA_HALT_REJECT:
				__logprint("machine halted: begin");
				st[0] = NINA_HALT_REJECT;  return -1;
			case NINA_YIELD:
				__logprint("machine yielded: ", c);
				st[0] = NINA_YIELD;  return -1;
			}
		}

		try {
			do {
				en = __stk[__slen - 1];
				if(p) {
					p = false;
				} else if((a = en.step(c)) > 0) {
					__logprint("transit to state " + STATE + ": ", c);
					b = en.accepted();
					switch(en.execaction(c)) {
					case NINA_ACCEPT:
						__logprint("accept " + __stk[__slen - 1]);
						UNGET(c);
						st[0] = NINA_ACCEPT;  return -1;
					case NINA_FAIL:
						__logprint("match failed: ", c);
						__puttrace();
						UNGET(c);
						st[0] = NINA_FAIL;  return -1;
					case NINA_HALT_ACCEPT:
						__logprint("machine halted: ", c);
						st[0] = NINA_HALT_ACCEPT;  return -1;
					case NINA_HALT_REJECT:
						__logprint("machine halted: ", c);
						st[0] = NINA_HALT_REJECT;  return -1;
					case NINA_YIELD:
						__logprint("machine yielded: ", c);
						st[0] = NINA_YIELD;  return -1;
					}
				} else if(a < 0) {
					__logprint("entering " + __stk[__slen - 1]);
					return c;
				} else if(b) {
					__logprint("accept " + __stk[__slen - 1]);
					UNGET(c);
					st[0] = NINA_ACCEPT;  return -1;
				} else if(__lookaheadw_ptr >= 0) {
					__logprint("match failed: try lookahead: ", c);
					LOOKAHEAD(c);
					LOOKAHEAD_RB();
					b = en.accepted();
				} else if(c == -1) {
					if(!b)  throw new TokenException();
					st[0] = NINA_ACCEPT;
					return -1;
				} else {
					__logprint("match failed: ", c);
					__puttrace();
					UNGET(c);
					st[0] = NINA_FAIL;  return -1;
				}

				if(__stk[__slen - 1].isEmptyTransition()) {
					// do nothing
				} else if(!__stk[__slen - 1].isDead()) {
					c = _read();
				} else if(b) {
					__logprint("accept " + __stk[__slen - 1]);
					st[0] = NINA_ACCEPT;  return -1;
				} else if(__lookaheadw_ptr >= 0) {
					__logprint("match failed: try lookahead: ", c);
					LOOKAHEAD_RB();
					b = en.accepted();
				} else {
					__logprint("match failed: ", c);
					__puttrace();
					st[0] = NINA_FAIL;  return -1;
				}
			} while(true);
		} catch(RuntimeException e) {
			UNGET(c);
			throw e;
		}
	}

	private Boolean execfinally() {
		int a, b;

		if((a = __stk[__slen - 1].finallyState()) >= 0) {
			b = STATE;  STATE = a;
			switch(__stk[__slen - 1].execaction(NINA_BEGIN)) {
			case NINA_HALT_ACCEPT:
				__slen = 0;
				return Boolean.TRUE;
			case NINA_HALT_REJECT:
				__slen = 0;
				return Boolean.FALSE;
			}
			STATE = b;
		}
		return null;
	}

	private int getdeadstate() {
		return __stk[__slen - 1].deadState();
	}

	private int getrecover(Exception e) {
		return __stk[__slen - 1].recover(e);
	}

	boolean parse(Engine entry) throws java.io.IOException {
		Boolean b = Boolean.FALSE;
		int[] a = new int[1];
		boolean skip = true;
		int c = 0;

		__logopen();
		try {
			if(__slen == 0) {
				b = Boolean.TRUE;
				__stkpush(0, entry);
			}

			ot: while(true) {
				try {
					if((c = _parse(c, b, skip, a)) != -1) {
						skip = false;
					} else if(a[0] == NINA_FAIL) {
						while((STATE = getdeadstate()) < 0) {
							if((b = execfinally()) != null)  break ot;
							if(__slen-- <= 1) {
								throw new TokenException();
							}
						}
						skip = true;
					} else if(a[0] == NINA_HALT_ACCEPT) {
						if((b = execfinally()) != null)  break;
						__slen = 0;
						b = Boolean.TRUE;  break;
					} else if(a[0] == NINA_HALT_REJECT) {
						if((b = execfinally()) != null)  break;
						__slen = 0;
						b = Boolean.FALSE;  break;
					} else if(a[0] == NINA_YIELD) {
						return false;
					} else if(__slen > 1) {
						if((b = execfinally()) != null)  break;
						STATE = __sts[--__slen];
						skip = true;
					} else {
						if((b = execfinally()) != null)  break;
						b = new Boolean(__stk[--__slen].accepted());
						break;
					}
				} catch(RuntimeException e) {
					exception = e;
					if(__slen <= 0)  throw e;
					while((STATE = getrecover(e)) < 0) {
						if((b = execfinally()) != null)  return b;
						if(__slen-- <= 1)  throw e;
					}
				}
				b = Boolean.TRUE;
			}
			if(!b.booleanValue())  throw new TokenException();
			return b.booleanValue();
		} finally {
			__logclose();
		}
	}

	boolean parse(java.io.Reader rd) throws java.io.IOException {
		streamStack.push(rd);
		return parse(ENGINE_miyuki2);
	}

	static boolean parseAll(java.io.Reader rd) throws java.io.IOException {
		Miyuki2 o = new Miyuki2();

		return o.parse(rd);
	}

	void setStream(java.io.Reader rd) {
		if(streamStack.size() == 0) {
			throw new IllegalStateException();
		}
		yieldObject = rd;
		streamStack.push(rd);
	}

	Object parseNext() throws java.io.IOException {
		Object o;

		if(streamStack.size() == 0) {
			throw new IllegalStateException();
		} else if(yieldObject == null) {
			return null;
		} else if(parse(ENGINE_miyuki2)) {
			if(yieldObject == null)  throw new NullPointerException();
			o = yieldObject;  yieldObject = null;
			return o;
		} else {
			if(yieldObject == null)  throw new NullPointerException();
			return yieldObject;
		}
	}

	static void puts(String s) {
		System.out.println(s);
	}

	boolean parse(java.io.InputStream rd) throws java.io.IOException {
		return parse(new java.io.InputStreamReader(rd));
	}

	static boolean parseAll(
			java.io.InputStream rd) throws java.io.IOException {
		return parseAll(new java.io.InputStreamReader(rd));
	}

	/* @@@-PARSER-CODE-END-@@@ */
private static AST _e;

private void konst(Number num) {
	_e = new IntegerAST(num.intValue());
}

private void bior(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IBOR, a, _e);
}

private void bxor(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IBXOR, a, _e);
}

private void band(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IBAND, a, _e);
}

private void rshift(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ISHR, a, _e);
}

private void lshift(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ISHL, a, _e);
}

private void add(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IADD, a, _e);
}

private void sub(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ISUB, a, _e);
}

private void mul(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IMUL, a, _e);
}

private void div(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IDIV, a, _e);
}

private void mod(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IREM, a, _e);
}

private void uminus() {
	_e = new UnaryAST(UnaryAST.Type.INEG, _e);
}

private void bnot() {
	_e = new UnaryAST(UnaryAST.Type.IBNOT, _e);
}

public static void main(String[] args) throws Exception {
	Classfile cf = new Classfile();
	MethodInfo mi = new MethodInfo("main", "([Ljava/lang/String;)V");
	Code code = new Code();

	cf.setMajorVersion(45);
	cf.setMinorVersion(3);
	cf.setAccessFlag(Classfile.ACC_PUBLIC);
	cf.setThisClass(ConstantClass.getInstance(args[0]));
	cf.setSuperClass(ConstantClass.getInstance("Ljava/lang/Object;"));
	mi.setAccessFlags(MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC);
	code.setMaxStack(1024);
	code.setMaxLocals(1);
	code.addCode(new Getstatic(ConstantFieldref.getInstance(
			"java/lang/System", "out", "Ljava/io/PrintStream;")));
	parseAll(new StringReader(args[1]));
	_e.putCode(new FunctionSpace(args[0]), new LocalVariableSpace(), code);
	code.addCode(new Invokevirtual(ConstantMethodref.getInstance(
			"java/io/PrintStream", "println", "(I)V")));
	code.addCode(Mnemonic.RETURN);
	mi.addAttribute(code);
	cf.addMethod(mi);

	FileOutputStream fs = null;
	try {
		fs = new FileOutputStream(args[0] + ".class");
		cf.generateClassFile(fs);
	} finally {
		if(fs != null) {
			fs.close();
		}
	}
}
}
