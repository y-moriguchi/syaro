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
public  class Miyuki5   {

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


	private int stmtlist_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c >= 0) {
				__stkpush(1, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
				STATE = 0;
				return 1;
		}
		return 0;
	}

	private boolean stmtlist_accepted() {
		return (STATE == 0 ||
				STATE == 1);
	}

	int stmtlist_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 1:
			_s.putCode(functions, local, code, null, -1, null);
			break;
		}
		return 1;
	}

	boolean stmtlist_isend() {
		return (STATE == 1);
	}

	private final Engine ENGINE_stmtlist = new Engine() {

		int step(int c) throws java.io.IOException {
			return stmtlist_step(c);
		}

		boolean accepted() {
			return stmtlist_accepted();
		}

		int execaction(int c) {
			return stmtlist_execaction(c);
		}

		boolean isend() {
			return stmtlist_isend();
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
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 1);
		}

		public String toString() {
			return "stmtlist";
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
		case 7:
			break;
		case 9:
			rshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			break;
		case 2:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 3:
			break;
		case 6:
			lshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 4:
			break;
		case 0:
			break;
		case 5:
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

	private int lcmp_step(int  $c)  throws java.io.IOException {
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
			if((__l__ && $c == '<') || (__l__ && $c == '>')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if((__l__ && $c >= 0 && $c <= ';') || (__l__ && $c == '=') || (__l__ && $c >= '?' && $c <= 2147483647)) {
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
			if(($c == '>')) {
				LOOKAHEAD_COMMIT();
				STATE = 5;
				return 1;
			} else if(($c == '<')) {
				LOOKAHEAD_COMMIT();
				STATE = 6;
				return 1;
			}
			return 0;
		case 6:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 7;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			} else if($c >= 0) {
				__stkpush(8, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 8:
				STATE = 1;
				return 1;
		case 7:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 7;
				return 1;
			} else if($c >= 0) {
				__stkpush(9, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
				STATE = 1;
				return 1;
		case 5:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 10;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				__stkpush(11, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 11:
				STATE = 1;
				return 1;
		case 10:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 10;
				return 1;
			} else if($c >= 0) {
				__stkpush(12, ENGINE_shft);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 12:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean lcmp_accepted() {
		return (STATE == 1 ||
				STATE == 8 ||
				STATE == 9 ||
				STATE == 11 ||
				STATE == 12);
	}

	int lcmp_execaction(int  $c) {
		switch(STATE) {
		case 2:
			break;
		case 5:
			break;
		case 4:
			break;
		case 6:
			break;
		case 7:
			break;
		case 9:
			lle(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			llt(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 3:
			break;
		case 10:
			break;
		case 12:
			lge(((AST)(__stv[__slen - 1][1])));
			break;
		case 11:
			lgt(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		}
		return 1;
	}

	boolean lcmp_isend() {
		return (STATE == 3 ||
				STATE == 8 ||
				STATE == 9 ||
				STATE == 11 ||
				STATE == 12);
	}

	private final Engine ENGINE_lcmp = new Engine() {

		int step(int c) throws java.io.IOException {
			return lcmp_step(c);
		}

		boolean accepted() {
			return lcmp_accepted();
		}

		int execaction(int c) {
			return lcmp_execaction(c);
		}

		boolean isend() {
			return lcmp_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 13;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 8 ||
				STATE == 9 ||
				STATE == 11 ||
				STATE == 12);
		}

		public String toString() {
			return "lcmp";
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
			if((__l__ && $c == '+')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '-')) {
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
		case 0:
			break;
		case 6:
			break;
		case 4:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 5:
			sub(((AST)(__stv[__slen - 1][1])));
			break;
		case 7:
			add(((AST)(__stv[__slen - 1][1])));
			break;
		case 2:
			break;
		case 3:
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
		case 3:
			break;
		case 2:
			break;
		case 0:
			break;
		case 4:
			bxor(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
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

	private int stmt_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if((__l__ && $c == 'p')) {
				LOOKAHEAD($c);
				STATE = 1;
				return 1;
			} else if((__l__ && $c == 'b')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == 'w')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if((__l__ && $c == 'c')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			} else if((__l__ && $c == 'f')) {
				LOOKAHEAD($c);
				STATE = 5;
				return 1;
			} else if((__l__ && $c == 'd')) {
				LOOKAHEAD($c);
				STATE = 6;
				return 1;
			} else if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 7;
				return 1;
			} else if(($c == '{')) {
				LOOKAHEAD_COMMIT();
				STATE = 8;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c >= 0) {
				__stkpush(9, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 9;
				return 1;
			} else if(($c == ';')) {
				STATE = 10;
				return 1;
			}
			return 0;
		case 10:
			return 0;
		case 8:
				STATE = 11;
				return 1;
		case 11:
			if(($c == '}')) {
				STATE = 12;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 11;
				return 1;
			} else if($c >= 0) {
				__stkpush(13, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 13:
				STATE = 11;
				return 1;
		case 12:
			return 0;
		case 7:
			if((__l__ && $c == 'f')) {
				LOOKAHEAD($c);
				STATE = 14;
				return 1;
			}
			return 0;
		case 14:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 15;
				return 1;
			}
			return 0;
		case 15:
			if(($c == '(')) {
				STATE = 16;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 15;
				return 1;
			}
			return 0;
		case 16:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 16;
				return 1;
			} else if($c >= 0) {
				__stkpush(17, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 17:
				STATE = 18;
				return 1;
		case 18:
			if(($c == ')')) {
				STATE = 19;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 18;
				return 1;
			}
			return 0;
		case 19:
			if($c >= 0) {
				__stkpush(20, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 20:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 21;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 20;
				return 1;
			} else if($c < 0) {
				STATE = 22;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 22;
				return 1;
			}
		case 22:
			return 0;
		case 21:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 23;
				return 1;
			}
			return 0;
		case 23:
			if((__l__ && $c == 's')) {
				LOOKAHEAD($c);
				STATE = 24;
				return 1;
			}
			return 0;
		case 24:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 25;
				return 1;
			}
			return 0;
		case 25:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 26;
				return 1;
			}
			return 0;
		case 26:
			if($c >= 0) {
				__stkpush(27, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 27:
			return 0;
		case 6:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 28;
				return 1;
			}
			return 0;
		case 28:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 29;
				return 1;
			}
			return 0;
		case 29:
			if($c >= 0) {
				__stkpush(30, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 30:
			if((__l__ && $c == 'w')) {
				LOOKAHEAD($c);
				STATE = 31;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 30;
				return 1;
			}
			return 0;
		case 31:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 32;
				return 1;
			}
			return 0;
		case 32:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 33;
				return 1;
			}
			return 0;
		case 33:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 34;
				return 1;
			}
			return 0;
		case 34:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 35;
				return 1;
			}
			return 0;
		case 35:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 36;
				return 1;
			}
			return 0;
		case 36:
			if(($c == '(')) {
				STATE = 37;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 36;
				return 1;
			}
			return 0;
		case 37:
			if($c >= 0) {
				__stkpush(38, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 38:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 38;
				return 1;
			} else if(($c == ')')) {
				STATE = 39;
				return 1;
			}
			return 0;
		case 39:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 39;
				return 1;
			} else if(($c == ';')) {
				STATE = 40;
				return 1;
			}
			return 0;
		case 40:
			return 0;
		case 5:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 41;
				return 1;
			}
			return 0;
		case 41:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 42;
				return 1;
			}
			return 0;
		case 42:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 43;
				return 1;
			}
			return 0;
		case 43:
			if(($c == '(')) {
				STATE = 44;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 43;
				return 1;
			}
			return 0;
		case 44:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 44;
				return 1;
			} else if(($c == ';')) {
				STATE = 45;
				return 1;
			} else if($c >= 0) {
				__stkpush(46, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 46:
			if(($c == ';')) {
				STATE = 47;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 46;
				return 1;
			}
			return 0;
		case 47:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 47;
				return 1;
			} else if($c < 0) {
				STATE = 48;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 48;
				return 1;
			}
		case 48:
				STATE = 49;
				return 1;
		case 49:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 49;
				return 1;
			} else if(($c == ';')) {
				STATE = 50;
				return 1;
			} else if($c >= 0) {
				__stkpush(51, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 51:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 51;
				return 1;
			} else if(($c == ';')) {
				STATE = 52;
				return 1;
			}
			return 0;
		case 52:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 52;
				return 1;
			} else if($c < 0) {
				STATE = 53;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 53;
				return 1;
			}
		case 53:
				STATE = 54;
				return 1;
		case 54:
			if(($c == ')')) {
				STATE = 55;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 54;
				return 1;
			} else if($c >= 0) {
				__stkpush(56, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 56:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 56;
				return 1;
			} else if(($c == ')')) {
				STATE = 57;
				return 1;
			}
			return 0;
		case 57:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 57;
				return 1;
			} else if($c < 0) {
				STATE = 58;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 58;
				return 1;
			}
		case 58:
			if($c >= 0) {
				__stkpush(59, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 59:
			return 0;
		case 55:
				STATE = 57;
				return 1;
		case 50:
				STATE = 52;
				return 1;
		case 45:
				STATE = 47;
				return 1;
		case 4:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 60;
				return 1;
			}
			return 0;
		case 60:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 61;
				return 1;
			}
			return 0;
		case 61:
			if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 62;
				return 1;
			}
			return 0;
		case 62:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 63;
				return 1;
			}
			return 0;
		case 63:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 64;
				return 1;
			}
			return 0;
		case 64:
			if((__l__ && $c == 'u')) {
				LOOKAHEAD($c);
				STATE = 65;
				return 1;
			}
			return 0;
		case 65:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 66;
				return 1;
			}
			return 0;
		case 66:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 67;
				return 1;
			}
			return 0;
		case 67:
			if(($c == ';')) {
				STATE = 68;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 67;
				return 1;
			}
			return 0;
		case 68:
			return 0;
		case 3:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 69;
				return 1;
			}
			return 0;
		case 69:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 70;
				return 1;
			}
			return 0;
		case 70:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 71;
				return 1;
			}
			return 0;
		case 71:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 72;
				return 1;
			}
			return 0;
		case 72:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 73;
				return 1;
			}
			return 0;
		case 73:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 73;
				return 1;
			} else if(($c == '(')) {
				STATE = 74;
				return 1;
			}
			return 0;
		case 74:
			if($c >= 0) {
				__stkpush(75, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 75:
			if(($c == ')')) {
				STATE = 76;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 75;
				return 1;
			}
			return 0;
		case 76:
			if($c >= 0) {
				__stkpush(77, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 77:
			return 0;
		case 2:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 78;
				return 1;
			}
			return 0;
		case 78:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 79;
				return 1;
			}
			return 0;
		case 79:
			if((__l__ && $c == 'a')) {
				LOOKAHEAD($c);
				STATE = 80;
				return 1;
			}
			return 0;
		case 80:
			if((__l__ && $c == 'k')) {
				LOOKAHEAD($c);
				STATE = 81;
				return 1;
			}
			return 0;
		case 81:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 82;
				return 1;
			}
			return 0;
		case 82:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 82;
				return 1;
			} else if(($c == ';')) {
				STATE = 83;
				return 1;
			}
			return 0;
		case 83:
			return 0;
		case 1:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 84;
				return 1;
			}
			return 0;
		case 84:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 85;
				return 1;
			}
			return 0;
		case 85:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 86;
				return 1;
			}
			return 0;
		case 86:
			if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 87;
				return 1;
			}
			return 0;
		case 87:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 88;
				return 1;
			}
			return 0;
		case 88:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 88;
				return 1;
			} else if($c >= 0) {
				__stkpush(89, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 89:
			if(($c == ';')) {
				STATE = 90;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 89;
				return 1;
			}
			return 0;
		case 90:
			return 0;
		}
		return 0;
	}

	private boolean stmt_accepted() {
		return (STATE == 68 ||
				STATE == 83 ||
				STATE == 22 ||
				STATE == 59 ||
				STATE == 77 ||
				STATE == 40 ||
				STATE == 27 ||
				STATE == 10 ||
				STATE == 12 ||
				STATE == 90);
	}

	int stmt_execaction(int  $c) {
		switch(STATE) {
		case 58:
			(__stv[__slen - 1][58]) = _e;
			break;
		case 43:
			_e = null;
			break;
		case 87:
			break;
		case 88:
			break;
		case 65:
			break;
		case 53:
			(__stv[__slen - 1][53]) = _e;  
			_e = null;
			break;
		case 0:
			break;
		case 48:
			(__stv[__slen - 1][48]) = _e;  
			_e = null;
			break;
		case 25:
			break;
		case 28:
			break;
		case 68:
			_continue();
			break;
		case 81:
			break;
		case 71:
			break;
		case 64:
			break;
		case 11:
			break;
		case 30:
			break;
		case 5:
			break;
		case 40:
			enddo();
			break;
		case 31:
			break;
		case 37:
			break;
		case 47:
			break;
		case 69:
			break;
		case 32:
			break;
		case 83:
			_break();
			break;
		case 85:
			break;
		case 15:
			break;
		case 75:
			break;
		case 74:
			break;
		case 79:
			break;
		case 8:
			(__stv[__slen - 1][8]) = beginb();
			break;
		case 17:
			(__stv[__slen - 1][17]) = _e;
			break;
		case 45:
			break;
		case 90:
			print();
			break;
		case 42:
			break;
		case 89:
			break;
		case 78:
			break;
		case 80:
			break;
		case 67:
			break;
		case 14:
			break;
		case 21:
			break;
		case 12:
			endb(((BlockAST)(__stv[__slen - 1][8])));
			break;
		case 23:
			break;
		case 86:
			break;
		case 73:
			break;
		case 29:
			break;
		case 10:
			simple();
			break;
		case 7:
			break;
		case 19:
			break;
		case 59:
			endfor(((AST)(__stv[__slen - 1][48])), ((AST)(__stv[__slen - 1][53])), ((AST)(__stv[__slen - 1][58])));
			break;
		case 26:
			(__stv[__slen - 1][26]) = _s;
			break;
		case 9:
			break;
		case 13:
			addb(((BlockAST)(__stv[__slen - 1][8])));
			break;
		case 38:
			break;
		case 44:
			break;
		case 57:
			break;
		case 1:
			break;
		case 61:
			break;
		case 16:
			break;
		case 77:
			endwh(((AST)(__stv[__slen - 1][76])));
			break;
		case 49:
			break;
		case 84:
			break;
		case 46:
			break;
		case 62:
			break;
		case 63:
			break;
		case 4:
			break;
		case 3:
			break;
		case 55:
			break;
		case 56:
			break;
		case 35:
			break;
		case 41:
			break;
		case 54:
			break;
		case 51:
			break;
		case 33:
			break;
		case 39:
			break;
		case 2:
			break;
		case 6:
			break;
		case 24:
			break;
		case 18:
			break;
		case 76:
			(__stv[__slen - 1][76]) = _e;
			break;
		case 70:
			break;
		case 50:
			break;
		case 60:
			break;
		case 72:
			break;
		case 20:
			break;
		case 52:
			break;
		case 22:
			endif(((AST)(__stv[__slen - 1][17])));
			break;
		case 82:
			break;
		case 34:
			break;
		case 66:
			break;
		case 27:
			endif(((AST)(__stv[__slen - 1][17])), ((SAST)(__stv[__slen - 1][26])));
			break;
		case 36:
			break;
		}
		return 1;
	}

	boolean stmt_isend() {
		return (STATE == 17 ||
				STATE == 50 ||
				STATE == 48 ||
				STATE == 55 ||
				STATE == 20 ||
				STATE == 53 ||
				STATE == 52 ||
				STATE == 8 ||
				STATE == 57 ||
				STATE == 13 ||
				STATE == 47 ||
				STATE == 45);
	}

	private final Engine ENGINE_stmt = new Engine() {

		int step(int c) throws java.io.IOException {
			return stmt_step(c);
		}

		boolean accepted() {
			return stmt_accepted();
		}

		int execaction(int c) {
			return stmt_execaction(c);
		}

		boolean isend() {
			return stmt_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 91;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 68 ||
				STATE == 83 ||
				STATE == 22 ||
				STATE == 59 ||
				STATE == 77 ||
				STATE == 40 ||
				STATE == 27 ||
				STATE == 10 ||
				STATE == 12 ||
				STATE == 90);
		}

		boolean isEmptyTransition() {
		return (STATE == 17 ||
				STATE == 50 ||
				STATE == 48 ||
				STATE == 55 ||
				STATE == 53 ||
				STATE == 8 ||
				STATE == 13 ||
				STATE == 45);
		}

		public String toString() {
			return "stmt";
		}

	};

	private int land_step(int  $c)  throws java.io.IOException {
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
			if((__l__ && $c == '&')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if(($c == '&')) {
				LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(4, ENGINE_bior);
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

	private boolean land_accepted() {
		return (STATE == 1 ||
				STATE == 4);
	}

	int land_execaction(int  $c) {
		switch(STATE) {
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			land(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean land_isend() {
		return (STATE == 4);
	}

	private final Engine ENGINE_land = new Engine() {

		int step(int c) throws java.io.IOException {
			return land_step(c);
		}

		boolean accepted() {
			return land_accepted();
		}

		int execaction(int c) {
			return land_execaction(c);
		}

		boolean isend() {
			return land_isend();
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
			return "land";
		}

	};

	private int loeq_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_lcmp);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '=')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '!')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 3:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 4;
				return 1;
			} else if($c >= 0) {
				__stkpush(5, ENGINE_lcmp);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 5:
				STATE = 1;
				return 1;
		case 2:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 6;
				return 1;
			}
			return 0;
		case 6:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			} else if($c >= 0) {
				__stkpush(7, ENGINE_lcmp);
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

	private boolean loeq_accepted() {
		return (STATE == 1 ||
				STATE == 5 ||
				STATE == 7);
	}

	int loeq_execaction(int  $c) {
		switch(STATE) {
		case 6:
			break;
		case 0:
			break;
		case 4:
			break;
		case 7:
			leq(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 3:
			break;
		case 5:
			lne(((AST)(__stv[__slen - 1][1])));
			break;
		}
		return 1;
	}

	boolean loeq_isend() {
		return (STATE == 5 ||
				STATE == 7);
	}

	private final Engine ENGINE_loeq = new Engine() {

		int step(int c) throws java.io.IOException {
			return loeq_step(c);
		}

		boolean accepted() {
			return loeq_accepted();
		}

		int execaction(int c) {
			return loeq_execaction(c);
		}

		boolean isend() {
			return loeq_isend();
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
			return "loeq";
		}

	};

	private int prex_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_elem);
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
			if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			return 0;
		case 2:
			if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			return 0;
		}
		return 0;
	}

	private boolean prex_accepted() {
		return (STATE == 1 ||
				STATE == 4 ||
				STATE == 5);
	}

	int prex_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 3:
			break;
		case 4:
			incpost();
			break;
		case 5:
			decpost();
			break;
		case 1:
			break;
		case 2:
			break;
		}
		return 1;
	}

	boolean prex_isend() {
		return false;
	}

	private final Engine ENGINE_prex = new Engine() {

		int step(int c) throws java.io.IOException {
			return prex_step(c);
		}

		boolean accepted() {
			return prex_accepted();
		}

		int execaction(int c) {
			return prex_execaction(c);
		}

		boolean isend() {
			return prex_isend();
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
			return "prex";
		}

	};

	private int expr_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_asgn);
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

	private int asgn_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_lior);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '+')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if((__l__ && $c == '*')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			} else if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 5;
				return 1;
			} else if((__l__ && $c == '&')) {
				LOOKAHEAD($c);
				STATE = 6;
				return 1;
			} else if((__l__ && $c == '/')) {
				LOOKAHEAD($c);
				STATE = 7;
				return 1;
			} else if((__l__ && $c == '%')) {
				LOOKAHEAD($c);
				STATE = 8;
				return 1;
			} else if((__l__ && $c == '|')) {
				LOOKAHEAD($c);
				STATE = 9;
				return 1;
			} else if((__l__ && $c == '^')) {
				LOOKAHEAD($c);
				STATE = 10;
				return 1;
			} else if((__l__ && $c == '-')) {
				LOOKAHEAD($c);
				STATE = 11;
				return 1;
			} else if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 12;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 12:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 12;
				return 1;
			} else if($c < 0) {
				STATE = 13;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 13;
				return 1;
			}
		case 13:
			if($c >= 0) {
				__stkpush(14, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 14:
			return 0;
		case 11:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 15;
				return 1;
			}
			return 0;
		case 15:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 15;
				return 1;
			} else if($c < 0) {
				STATE = 16;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 16;
				return 1;
			}
		case 16:
			if($c >= 0) {
				__stkpush(17, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 17:
			return 0;
		case 10:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 18;
				return 1;
			}
			return 0;
		case 18:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 18;
				return 1;
			} else if($c < 0) {
				STATE = 19;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 19;
				return 1;
			}
		case 19:
			if($c >= 0) {
				__stkpush(20, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 20:
			return 0;
		case 9:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 21;
				return 1;
			}
			return 0;
		case 21:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 21;
				return 1;
			} else if($c < 0) {
				STATE = 22;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 22;
				return 1;
			}
		case 22:
			if($c >= 0) {
				__stkpush(23, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 23:
			return 0;
		case 8:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 24;
				return 1;
			}
			return 0;
		case 24:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 24;
				return 1;
			} else if($c < 0) {
				STATE = 25;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 25;
				return 1;
			}
		case 25:
			if($c >= 0) {
				__stkpush(26, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 26:
			return 0;
		case 7:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 27;
				return 1;
			}
			return 0;
		case 27:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 27;
				return 1;
			} else if($c < 0) {
				STATE = 28;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 28;
				return 1;
			}
		case 28:
			if($c >= 0) {
				__stkpush(29, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 29:
			return 0;
		case 6:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 30;
				return 1;
			}
			return 0;
		case 30:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 30;
				return 1;
			} else if($c < 0) {
				STATE = 31;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 31;
				return 1;
			}
		case 31:
			if($c >= 0) {
				__stkpush(32, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 32:
			return 0;
		case 5:
			if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 33;
				return 1;
			}
			return 0;
		case 33:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 34;
				return 1;
			}
			return 0;
		case 34:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 34;
				return 1;
			} else if($c < 0) {
				STATE = 35;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 35;
				return 1;
			}
		case 35:
			if($c >= 0) {
				__stkpush(36, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 36:
			return 0;
		case 4:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 37;
				return 1;
			}
			return 0;
		case 37:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 37;
				return 1;
			} else if($c < 0) {
				STATE = 38;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 38;
				return 1;
			}
		case 38:
			if($c >= 0) {
				__stkpush(39, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 39:
			return 0;
		case 3:
			if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 40;
				return 1;
			}
			return 0;
		case 40:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 41;
				return 1;
			}
			return 0;
		case 41:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 41;
				return 1;
			} else if($c < 0) {
				STATE = 42;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 42;
				return 1;
			}
		case 42:
			if($c >= 0) {
				__stkpush(43, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 43:
			return 0;
		case 2:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 44;
				return 1;
			}
			return 0;
		case 44:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 44;
				return 1;
			} else if($c < 0) {
				STATE = 45;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 45;
				return 1;
			}
		case 45:
			if($c >= 0) {
				__stkpush(46, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 46:
			return 0;
		}
		return 0;
	}

	private boolean asgn_accepted() {
		return (STATE == 17 ||
				STATE == 1 ||
				STATE == 32 ||
				STATE == 39 ||
				STATE == 20 ||
				STATE == 36 ||
				STATE == 23 ||
				STATE == 43 ||
				STATE == 26 ||
				STATE == 46 ||
				STATE == 29 ||
				STATE == 14);
	}

	int asgn_execaction(int  $c) {
		switch(STATE) {
		case 44:
			break;
		case 4:
			break;
		case 21:
			break;
		case 32:
			asgn(((AST)(__stv[__slen - 1][31])), Mnemonic.IAND);
			break;
		case 27:
			break;
		case 41:
			break;
		case 33:
			break;
		case 42:
			(__stv[__slen - 1][42]) = _e;
			break;
		case 43:
			asgn(((AST)(__stv[__slen - 1][42])), Mnemonic.ISHR);
			break;
		case 46:
			asgn(((AST)(__stv[__slen - 1][45])), Mnemonic.IADD);
			break;
		case 20:
			asgn(((AST)(__stv[__slen - 1][19])), Mnemonic.IXOR);
			break;
		case 28:
			(__stv[__slen - 1][28]) = _e;
			break;
		case 30:
			break;
		case 22:
			(__stv[__slen - 1][22]) = _e;
			break;
		case 19:
			(__stv[__slen - 1][19]) = _e;
			break;
		case 29:
			asgn(((AST)(__stv[__slen - 1][28])), Mnemonic.IDIV);
			break;
		case 5:
			break;
		case 1:
			break;
		case 34:
			break;
		case 40:
			break;
		case 10:
			break;
		case 9:
			break;
		case 7:
			break;
		case 25:
			(__stv[__slen - 1][25]) = _e;
			break;
		case 24:
			break;
		case 26:
			asgn(((AST)(__stv[__slen - 1][25])), Mnemonic.IREM);
			break;
		case 17:
			asgn(((AST)(__stv[__slen - 1][16])), Mnemonic.ISUB);
			break;
		case 12:
			break;
		case 35:
			(__stv[__slen - 1][35]) = _e;
			break;
		case 11:
			break;
		case 3:
			break;
		case 23:
			asgn(((AST)(__stv[__slen - 1][22])), Mnemonic.IOR);
			break;
		case 31:
			(__stv[__slen - 1][31]) = _e;
			break;
		case 37:
			break;
		case 45:
			(__stv[__slen - 1][45]) = _e;
			break;
		case 18:
			break;
		case 15:
			break;
		case 36:
			asgn(((AST)(__stv[__slen - 1][35])), Mnemonic.ISHL);
			break;
		case 0:
			break;
		case 2:
			break;
		case 38:
			(__stv[__slen - 1][38]) = _e;
			break;
		case 13:
			(__stv[__slen - 1][13]) = _e;
			break;
		case 39:
			asgn(((AST)(__stv[__slen - 1][38])), Mnemonic.IMUL);
			break;
		case 14:
			asgn(((AST)(__stv[__slen - 1][13])), null);
			break;
		case 8:
			break;
		case 16:
			(__stv[__slen - 1][16]) = _e;
			break;
		case 6:
			break;
		}
		return 1;
	}

	boolean asgn_isend() {
		return (STATE == 34 ||
				STATE == 18 ||
				STATE == 21 ||
				STATE == 37 ||
				STATE == 24 ||
				STATE == 27 ||
				STATE == 41 ||
				STATE == 12 ||
				STATE == 44 ||
				STATE == 15 ||
				STATE == 30);
	}

	private final Engine ENGINE_asgn = new Engine() {

		int step(int c) throws java.io.IOException {
			return asgn_step(c);
		}

		boolean accepted() {
			return asgn_accepted();
		}

		int execaction(int c) {
			return asgn_execaction(c);
		}

		boolean isend() {
			return asgn_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 47;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 17 ||
				STATE == 32 ||
				STATE == 39 ||
				STATE == 20 ||
				STATE == 36 ||
				STATE == 23 ||
				STATE == 43 ||
				STATE == 26 ||
				STATE == 46 ||
				STATE == 29 ||
				STATE == 14);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "asgn";
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
		case 5:
			break;
		case 3:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
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

	private int elem_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c == '(')) {
				STATE = 1;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				$buffer = new StringBuffer();UNGET($c);
				STATE = 2;
				return 1;
			} else if(($c >= 'A' && $c <= 'Z') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if(($c >= 'A' && $c <= 'Z') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 5;
				return 1;
			} else if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 6;
				return 1;
			} else if($c < 0) {
				
				STATE = 5;
				return 1;
			}
			return 0;
		case 6:
			if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 5;
				return 1;
			} else if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 6;
				return 1;
			} else if($c < 0) {
				
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			return 0;
		case 2:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 2;
				return 1;
			} else if($c < 0) {
				$int=Integer.parseInt($buffer.toString(), 10);
				STATE = 7;
				return 1;
			} else if($c >= 0) {
				$int=Integer.parseInt($buffer.toString(), 10);UNGET($c);
				STATE = 7;
				return 1;
			}
		case 7:
			return 0;
		case 1:
			if($c >= 0) {
				__stkpush(8, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 8:
			if(($c == ')')) {
				STATE = 9;
				return 1;
			}
			return 0;
		case 9:
			return 0;
		}
		return 0;
	}

	private boolean elem_accepted() {
		return (STATE == 5 ||
				STATE == 7 ||
				STATE == 9);
	}

	int elem_execaction(int  $c) {
		switch(STATE) {
		case 5:
			var($buffer.toString());
			break;
		case 0:
			break;
		case 3:
			break;
		case 6:
			break;
		case 7:
			konst($int);
			break;
		case 4:
			break;
		case 2:
			break;
		case 8:
			break;
		case 9:
			break;
		case 1:
			break;
		}
		return 1;
	}

	boolean elem_isend() {
		return (STATE == 2 ||
				STATE == 4 ||
				STATE == 6);
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
			return 10;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 5 ||
				STATE == 7 ||
				STATE == 9);
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
			if(($c == '!')) {
				LOOKAHEAD_COMMIT();
				STATE = 1;
				return 1;
			} else if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 2;
				return 1;
			} else if(($c == '~')) {
				LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			} else if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c >= 0) {
				__stkpush(5, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 5:
			return 0;
		case 4:
			if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 6;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 4;
				return 1;
			} else if($c >= 0) {
				__stkpush(7, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 7:
			return 0;
		case 6:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			} else if($c >= 0) {
				__stkpush(8, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 8:
			return 0;
		case 3:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(9, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
			return 0;
		case 2:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 2;
				return 1;
			} else if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 10;
				return 1;
			} else if($c >= 0) {
				__stkpush(11, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 11:
			return 0;
		case 10:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 10;
				return 1;
			} else if($c >= 0) {
				__stkpush(12, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 12:
			return 0;
		case 1:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			} else if($c >= 0) {
				__stkpush(13, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 13:
			return 0;
		}
		return 0;
	}

	private boolean unary_accepted() {
		return (STATE == 5 ||
				STATE == 7 ||
				STATE == 8 ||
				STATE == 9 ||
				STATE == 11 ||
				STATE == 12 ||
				STATE == 13);
	}

	int unary_execaction(int  $c) {
		switch(STATE) {
		case 8:
			decpre();
			break;
		case 13:
			lnot();
			break;
		case 12:
			incpre();
			break;
		case 5:
			break;
		case 6:
			break;
		case 2:
			break;
		case 3:
			break;
		case 10:
			break;
		case 4:
			break;
		case 11:
			break;
		case 7:
			uminus();
			break;
		case 1:
			break;
		case 0:
			break;
		case 9:
			bnot();
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
			return 14;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 5 ||
				STATE == 7 ||
				STATE == 8 ||
				STATE == 9 ||
				STATE == 11 ||
				STATE == 12 ||
				STATE == 13);
		}

		boolean isEmptyTransition() {
		return false;
		}

		public String toString() {
			return "unary";
		}

	};

	private int band_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_loeq);
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
				__stkpush(6, ENGINE_loeq);
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
		case 5:
			break;
		case 4:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 3:
			break;
		case 6:
			band(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 2:
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
			if((__l__ && $c == '%')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '*')) {
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
		case 9:
			break;
		case 8:
			mul(((AST)(__stv[__slen - 1][1])));
			break;
		case 5:
			break;
		case 6:
			div(((AST)(__stv[__slen - 1][1])));
			break;
		case 4:
			break;
		case 7:
			break;
		case 0:
			break;
		case 10:
			mod(((AST)(__stv[__slen - 1][1])));
			break;
		case 2:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 3:
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

	private int miyuki5_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_stmtlist);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			return 0;
		}
		return 0;
	}

	private boolean miyuki5_accepted() {
		return (STATE == 0 ||
				STATE == 1);
	}

	int miyuki5_execaction(int  $c) {
		switch(STATE) {
		case 1:
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean miyuki5_isend() {
		return false;
	}

	private final Engine ENGINE_miyuki5 = new Engine() {

		int step(int c) throws java.io.IOException {
			return miyuki5_step(c);
		}

		boolean accepted() {
			return miyuki5_accepted();
		}

		int execaction(int c) {
			return miyuki5_execaction(c);
		}

		boolean isend() {
			return miyuki5_isend();
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
			return "miyuki5";
		}

	};

	private int lior_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_land);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if((__l__ && $c == '|')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if(($c == '|')) {
				LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			}
			return 0;
		case 3:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 3;
				return 1;
			} else if($c >= 0) {
				__stkpush(4, ENGINE_land);
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

	private boolean lior_accepted() {
		return (STATE == 1 ||
				STATE == 4);
	}

	int lior_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 4:
			lior(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 3:
			break;
		case 2:
			break;
		}
		return 1;
	}

	boolean lior_isend() {
		return (STATE == 4);
	}

	private final Engine ENGINE_lior = new Engine() {

		int step(int c) throws java.io.IOException {
			return lior_step(c);
		}

		boolean accepted() {
			return lior_accepted();
		}

		int execaction(int c) {
			return lior_execaction(c);
		}

		boolean isend() {
			return lior_isend();
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
			return "lior";
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
		return parse(ENGINE_miyuki5);
	}

	static boolean parseAll(java.io.Reader rd) throws java.io.IOException {
		Miyuki5 o = new Miyuki5();

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
		} else if(parse(ENGINE_miyuki5)) {
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
private FunctionSpace functions;
private LocalVariableSpace local = new LocalVariableSpace();
private Code code;
private SAST _s;
private AST _e;

private void asgn(AST a, Mnemonic op) {
	_e = new AssignAST(op, a, _e);
}

private void konst(Number num) {
	_e = new IntegerAST(num.intValue());
}

private void lior(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ILOR, a, _e);
}

private void land(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ILAND, a, _e);
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

private void leq(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IEQ, a, _e);
}

private void lne(AST a) {
	_e = new BinaryAST(BinaryAST.Type.INE, a, _e);
}

private void llt(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ILT, a, _e);
}

private void lle(AST a) {
	_e = new BinaryAST(BinaryAST.Type.ILE, a, _e);
}

private void lgt(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IGT, a, _e);
}

private void lge(AST a) {
	_e = new BinaryAST(BinaryAST.Type.IGE, a, _e);
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

private void lnot() {
	_e = new UnaryAST(UnaryAST.Type.ILNOT, _e);
}

private void incpre() {
	_e = new IncDecAST(true, true, _e);
}

private void decpre() {
	_e = new IncDecAST(false, true, _e);
}

private void incpost() {
	_e = new IncDecAST(true, false, _e);
}

private void decpost() {
	_e = new IncDecAST(false, false, _e);
}

private void var(String name) {
	local.putVariable(name);
	_e = new SymbolAST(name);
}

private void endif(AST expr) {
	_s = new IfAST(expr, _s, null);
}

private void endif(AST expr, SAST _if) {
	_s = new IfAST(expr, _if, _s);
}

private void _break() {
	_s = new BreakAST();
}

private void _continue() {
	_s = new ContinueAST();
}

private void endwh(AST expr) {
	_s = new WhileAST(expr, _s);
}

private void enddo() {
	_s = new DoAST(_e, _s);
}

private void endfor(AST fe, AST fi, AST fs) {
	_s = new ForAST(fe, fi, fs, _s);
}

private BlockAST beginb() {
	return new BlockAST();
}

private void addb(BlockAST s) {
	s.addStatement(_s);
}

private void endb(BlockAST s) {
	_s = s;
}

private void print() {
	_s = new PrintAST(_e);
}

private void simple() {
	_s = new SimpleAST(_e);
}

public static void main(String[] args) throws Exception {
	Classfile cf = new Classfile();
	MethodInfo mi = new MethodInfo("main", "([Ljava/lang/String;)V");
	Miyuki5 parser = new Miyuki5();
	Goto gt = new Goto(), gb = new Goto();
	int jp, np;

	parser.code = new Code();
	parser.functions = new FunctionSpace(args[0]);
	cf.setMajorVersion(45);
	cf.setMinorVersion(3);
	cf.setAccessFlag(Classfile.ACC_PUBLIC);
	cf.setThisClass(new ConstantClass(args[0]));
	cf.setSuperClass(new ConstantClass("Ljava/lang/Object;"));
	mi.setAccessFlags(MethodInfo.ACC_PUBLIC | MethodInfo.ACC_STATIC);
	jp = parser.code.addCode(gt);
	np = parser.code.addCode(Mnemonic.NOP);
	parser.parse(System.in);
	parser.code.addCode(Mnemonic.RETURN);
	gt.setOffset(parser.code.getCurrentOffset(jp));
	for(int i = 1; i < parser.local.getMax(); i++) {
		parser.code.addCode(new IConst(0));
		parser.code.addCode(new IStore(i));
	}
	gb.setOffset(parser.code.getAddress(np) - parser.code.getCurrentAddress());
	parser.code.addCode(gb);
	parser.code.setMaxStack(1024);
	parser.code.setMaxLocals(parser.local.getMax());
	mi.addAttribute(parser.code);
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
