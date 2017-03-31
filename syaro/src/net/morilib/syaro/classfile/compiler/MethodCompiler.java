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
/*
 * morilib Nina Version: 0.4.16.612
 *
 * Nina homepage:      http://nina.morilib.net/
 * Plugin update site: http://nina.morilib.net/update-site/
 */
package net.morilib.syaro.classfile.compiler;

import java.io.StringReader;
import net.morilib.syaro.classfile.*;
import net.morilib.syaro.classfile.code.*;

/**
 * method compiler
 * 
 * @author Yuichiro MORIGUCHI
 */
public  class MethodCompiler   {

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
			} else if(($c == '}')) {
				UNGET($c);
				STATE = 1;
				return 1;
			} else if($c >= 0) {
				__stkpush(2, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 2:
				STATE = 0;
				return 1;
		case 1:
			return 0;
		}
		return 0;
	}

	private boolean stmtlist_accepted() {
		return (STATE == 0 ||
				STATE == 1 ||
				STATE == 2);
	}

	int stmtlist_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			putCodes();
			break;
		}
		return 1;
	}

	boolean stmtlist_isend() {
		return (STATE == 2);
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
			return 3;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 1);
		}

		boolean isEmptyTransition() {
		return (STATE == 2);
		}

		public String toString() {
			return "stmtlist";
		}

	};

	private int trys_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c < 0) {
				STATE = 1;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 1;
				return 1;
			}
		case 1:
				STATE = 2;
				return 1;
		case 2:
			if($c >= 0) {
				__stkpush(3, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 3:
				STATE = 4;
				return 1;
		case 4:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 4;
				return 1;
			} else if($c < 0) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 5;
				return 1;
			}
		case 5:
			if((__l__ && $c == 'c')) {
				LOOKAHEAD($c);
				STATE = 6;
				return 1;
			} else if($c < 0) {
				STATE = 7;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 7;
				return 1;
			}
		case 7:
				STATE = 8;
				return 1;
		case 8:
			if((__l__ && $c == 'f')) {
				LOOKAHEAD($c);
				STATE = 9;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 8;
				return 1;
			} else if($c < 0) {
				STATE = 10;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 10;
				return 1;
			}
		case 10:
			return 0;
		case 9:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 11;
				return 1;
			}
			return 0;
		case 11:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 12;
				return 1;
			}
			return 0;
		case 12:
			if((__l__ && $c == 'a')) {
				LOOKAHEAD($c);
				STATE = 13;
				return 1;
			}
			return 0;
		case 13:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 14;
				return 1;
			}
			return 0;
		case 14:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 15;
				return 1;
			}
			return 0;
		case 15:
			if((__l__ && $c == 'y')) {
				LOOKAHEAD($c);
				STATE = 16;
				return 1;
			}
			return 0;
		case 16:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 17;
				return 1;
			}
			return 0;
		case 17:
			if($c >= 0) {
				__stkpush(18, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 18:
				STATE = 10;
				return 1;
		case 6:
			if((__l__ && $c == 'a')) {
				LOOKAHEAD($c);
				STATE = 19;
				return 1;
			}
			return 0;
		case 19:
			if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 20;
				return 1;
			}
			return 0;
		case 20:
			if((__l__ && $c == 'c')) {
				LOOKAHEAD($c);
				STATE = 21;
				return 1;
			}
			return 0;
		case 21:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 22;
				return 1;
			}
			return 0;
		case 22:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 23;
				return 1;
			}
			return 0;
		case 23:
			if(($c == '(')) {
				STATE = 24;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 23;
				return 1;
			}
			return 0;
		case 24:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 24;
				return 1;
			} else if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 25;
				return 1;
			}
			return 0;
		case 25:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 26;
				return 1;
			}
			return 0;
		case 26:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 27;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 28;
				return 1;
			} else if($c < 0) {
				
				STATE = 28;
				return 1;
			}
			return 0;
		case 28:
				STATE = 29;
				return 1;
		case 29:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 29;
				return 1;
			} else if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 30;
				return 1;
			}
			return 0;
		case 30:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 31;
				return 1;
			}
			return 0;
		case 31:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 32;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 33;
				return 1;
			} else if($c < 0) {
				
				STATE = 33;
				return 1;
			}
			return 0;
		case 33:
				STATE = 34;
				return 1;
		case 34:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 34;
				return 1;
			} else if(($c == ')')) {
				STATE = 35;
				return 1;
			}
			return 0;
		case 35:
			if($c >= 0) {
				__stkpush(36, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 36:
				STATE = 37;
				return 1;
		case 37:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 37;
				return 1;
			} else if($c < 0) {
				STATE = 5;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 5;
				return 1;
			}
		case 32:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 32;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 33;
				return 1;
			} else if($c < 0) {
				
				STATE = 33;
				return 1;
			}
			return 0;
		case 27:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 27;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 28;
				return 1;
			} else if($c < 0) {
				
				STATE = 28;
				return 1;
			}
			return 0;
		}
		return 0;
	}

	private boolean trys_accepted() {
		return (STATE == 10);
	}

	int trys_execaction(int  $c) {
		switch(STATE) {
		case 33:
			(__stv[__slen - 1][33]) = $buffer.toString();
			break;
		case 26:
			break;
		case 31:
			break;
		case 17:
			break;
		case 13:
			break;
		case 22:
			break;
		case 2:
			break;
		case 8:
			break;
		case 6:
			break;
		case 25:
			break;
		case 36:
			addcatch(((java.util.List<CatchEntry>)(__stv[__slen - 1][1])), ((String)(__stv[__slen - 1][28])), ((String)(__stv[__slen - 1][33])), _s);
			break;
		case 3:
			(__stv[__slen - 1][3]) = _s;
			break;
		case 7:
			(__stv[__slen - 1][7]) = null;
			break;
		case 28:
			(__stv[__slen - 1][28]) = $buffer.toString();
			break;
		case 29:
			break;
		case 1:
			(__stv[__slen - 1][1]) = new java.util.ArrayList<CatchEntry>();
			break;
		case 9:
			break;
		case 21:
			break;
		case 12:
			break;
		case 16:
			break;
		case 11:
			break;
		case 19:
			break;
		case 24:
			break;
		case 30:
			break;
		case 0:
			break;
		case 5:
			break;
		case 27:
			break;
		case 10:
			_s = _catch(((SAST)(__stv[__slen - 1][3])), ((java.util.List<CatchEntry>)(__stv[__slen - 1][1])), ((SAST)(__stv[__slen - 1][7])));
			break;
		case 15:
			break;
		case 18:
			(__stv[__slen - 1][7]) = _s;
			break;
		case 14:
			break;
		case 20:
			break;
		case 32:
			break;
		case 34:
			break;
		case 35:
			break;
		case 23:
			break;
		case 4:
			break;
		case 37:
			break;
		}
		return 1;
	}

	boolean trys_isend() {
		return (STATE == 0 ||
				STATE == 1 ||
				STATE == 32 ||
				STATE == 33 ||
				STATE == 3 ||
				STATE == 4 ||
				STATE == 5 ||
				STATE == 36 ||
				STATE == 37 ||
				STATE == 7 ||
				STATE == 8 ||
				STATE == 18 ||
				STATE == 27 ||
				STATE == 26 ||
				STATE == 28 ||
				STATE == 31);
	}

	private final Engine ENGINE_trys = new Engine() {

		int step(int c) throws java.io.IOException {
			return trys_step(c);
		}

		boolean accepted() {
			return trys_accepted();
		}

		int execaction(int c) {
			return trys_execaction(c);
		}

		boolean isend() {
			return trys_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 38;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 10);
		}

		boolean isEmptyTransition() {
		return (STATE == 1 ||
				STATE == 33 ||
				STATE == 18 ||
				STATE == 3 ||
				STATE == 36 ||
				STATE == 7 ||
				STATE == 28);
		}

		public String toString() {
			return "trys";
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
			if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 3:
			if((__l__ && $c == '>')) {
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
			if((__l__ && $c == '<')) {
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
		case 4:
			break;
		case 6:
			rshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			break;
		case 0:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 5:
			break;
		case 9:
			lshift(((AST)(__stv[__slen - 1][1])));
			break;
		case 7:
			break;
		case 3:
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
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			} else if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 7;
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
		case 5:
			break;
		case 12:
			lge(((AST)(__stv[__slen - 1][1])));
			break;
		case 11:
			lgt(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			llt(((AST)(__stv[__slen - 1][1])));
			break;
		case 3:
			break;
		case 4:
			break;
		case 10:
			break;
		case 7:
			break;
		case 6:
			break;
		case 2:
			break;
		case 9:
			lle(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 0:
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
		case 2:
			break;
		case 7:
			sub(((AST)(__stv[__slen - 1][1])));
			break;
		case 3:
			break;
		case 4:
			break;
		case 0:
			break;
		case 5:
			add(((AST)(__stv[__slen - 1][1])));
			break;
		case 6:
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
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 0:
			break;
		case 3:
			break;
		case 4:
			bxor(((AST)(__stv[__slen - 1][1])));
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
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 1;
				return 1;
			} else if((__l__ && $c == 'b')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if((__l__ && $c == 'w')) {
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
			} else if((__l__ && $c == 'c')) {
				LOOKAHEAD($c);
				STATE = 7;
				return 1;
			} else if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 8;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c == '{')) {
				LOOKAHEAD_COMMIT();
				STATE = 9;
				return 1;
			} else if($c >= 0) {
				__stkpush(10, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 10:
			if(($c == ';')) {
				STATE = 11;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 10;
				return 1;
			}
			return 0;
		case 11:
			return 0;
		case 9:
				STATE = 12;
				return 1;
		case 12:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 12;
				return 1;
			} else if(($c == '}')) {
				STATE = 13;
				return 1;
			} else if($c >= 0) {
				__stkpush(14, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 14:
				STATE = 12;
				return 1;
		case 13:
			return 0;
		case 8:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 15;
				return 1;
			} else if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 16;
				return 1;
			}
			return 0;
		case 16:
			if((__l__ && $c == 'y')) {
				LOOKAHEAD($c);
				STATE = 17;
				return 1;
			}
			return 0;
		case 17:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 18;
				return 1;
			}
			return 0;
		case 18:
			if($c >= 0) {
				__stkpush(19, ENGINE_trys);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 19:
			return 0;
		case 15:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 20;
				return 1;
			}
			return 0;
		case 20:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 21;
				return 1;
			}
			return 0;
		case 21:
			if((__l__ && $c == 'w')) {
				LOOKAHEAD($c);
				STATE = 22;
				return 1;
			}
			return 0;
		case 22:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 23;
				return 1;
			}
			return 0;
		case 23:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 23;
				return 1;
			} else if($c >= 0) {
				__stkpush(24, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 24:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 24;
				return 1;
			} else if(($c == ';')) {
				STATE = 25;
				return 1;
			}
			return 0;
		case 25:
			return 0;
		case 7:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 26;
				return 1;
			}
			return 0;
		case 26:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 27;
				return 1;
			}
			return 0;
		case 27:
			if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 28;
				return 1;
			}
			return 0;
		case 28:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 29;
				return 1;
			}
			return 0;
		case 29:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 30;
				return 1;
			}
			return 0;
		case 30:
			if((__l__ && $c == 'u')) {
				LOOKAHEAD($c);
				STATE = 31;
				return 1;
			}
			return 0;
		case 31:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 32;
				return 1;
			}
			return 0;
		case 32:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 33;
				return 1;
			}
			return 0;
		case 33:
			if(($c == ';')) {
				STATE = 34;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 33;
				return 1;
			}
			return 0;
		case 34:
			return 0;
		case 6:
			if((__l__ && $c == 'o')) {
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
			if($c >= 0) {
				__stkpush(37, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 37:
			if((__l__ && $c == 'w')) {
				LOOKAHEAD($c);
				STATE = 38;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 37;
				return 1;
			}
			return 0;
		case 38:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 39;
				return 1;
			}
			return 0;
		case 39:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 40;
				return 1;
			}
			return 0;
		case 40:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 41;
				return 1;
			}
			return 0;
		case 41:
			if((__l__ && $c == 'e')) {
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
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 43;
				return 1;
			} else if(($c == '(')) {
				STATE = 44;
				return 1;
			}
			return 0;
		case 44:
			if($c >= 0) {
				__stkpush(45, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 45:
			if(($c == ')')) {
				STATE = 46;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 45;
				return 1;
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
			return 0;
		case 5:
			if((__l__ && $c == 'o')) {
				LOOKAHEAD($c);
				STATE = 48;
				return 1;
			}
			return 0;
		case 48:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 49;
				return 1;
			}
			return 0;
		case 49:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 50;
				return 1;
			}
			return 0;
		case 50:
			if(($c == '(')) {
				STATE = 51;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 50;
				return 1;
			}
			return 0;
		case 51:
			if(($c == ';')) {
				STATE = 52;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 51;
				return 1;
			} else if($c >= 0) {
				__stkpush(53, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 53:
			if(($c == ';')) {
				STATE = 54;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 53;
				return 1;
			}
			return 0;
		case 54:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 54;
				return 1;
			} else if($c < 0) {
				STATE = 55;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 55;
				return 1;
			}
		case 55:
				STATE = 56;
				return 1;
		case 56:
			if(($c == ';')) {
				STATE = 57;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 56;
				return 1;
			} else if($c >= 0) {
				__stkpush(58, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 58:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 58;
				return 1;
			} else if(($c == ';')) {
				STATE = 59;
				return 1;
			}
			return 0;
		case 59:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 59;
				return 1;
			} else if($c < 0) {
				STATE = 60;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 60;
				return 1;
			}
		case 60:
				STATE = 61;
				return 1;
		case 61:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 61;
				return 1;
			} else if(($c == ')')) {
				STATE = 62;
				return 1;
			} else if($c >= 0) {
				__stkpush(63, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 63:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 63;
				return 1;
			} else if(($c == ')')) {
				STATE = 64;
				return 1;
			}
			return 0;
		case 64:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 64;
				return 1;
			} else if($c < 0) {
				STATE = 65;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 65;
				return 1;
			}
		case 65:
			if($c >= 0) {
				__stkpush(66, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 66:
			return 0;
		case 62:
				STATE = 64;
				return 1;
		case 57:
				STATE = 59;
				return 1;
		case 52:
				STATE = 54;
				return 1;
		case 4:
			if((__l__ && $c == 'h')) {
				LOOKAHEAD($c);
				STATE = 67;
				return 1;
			}
			return 0;
		case 67:
			if((__l__ && $c == 'i')) {
				LOOKAHEAD($c);
				STATE = 68;
				return 1;
			}
			return 0;
		case 68:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 69;
				return 1;
			}
			return 0;
		case 69:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 70;
				return 1;
			}
			return 0;
		case 70:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 71;
				return 1;
			}
			return 0;
		case 71:
			if(($c == '(')) {
				STATE = 72;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 71;
				return 1;
			}
			return 0;
		case 72:
			if($c >= 0) {
				__stkpush(73, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 73:
			if(($c == ')')) {
				STATE = 74;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 73;
				return 1;
			}
			return 0;
		case 74:
			if($c >= 0) {
				__stkpush(75, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 75:
			return 0;
		case 3:
			if((__l__ && $c == 'f')) {
				LOOKAHEAD($c);
				STATE = 76;
				return 1;
			}
			return 0;
		case 76:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 77;
				return 1;
			}
			return 0;
		case 77:
			if(($c == '(')) {
				STATE = 78;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 77;
				return 1;
			}
			return 0;
		case 78:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 78;
				return 1;
			} else if($c >= 0) {
				__stkpush(79, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 79:
				STATE = 80;
				return 1;
		case 80:
			if(($c == ')')) {
				STATE = 81;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 80;
				return 1;
			}
			return 0;
		case 81:
			if($c >= 0) {
				__stkpush(82, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 82:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 83;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 82;
				return 1;
			} else if($c < 0) {
				STATE = 84;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 84;
				return 1;
			}
		case 84:
			return 0;
		case 83:
			if((__l__ && $c == 'l')) {
				LOOKAHEAD($c);
				STATE = 85;
				return 1;
			}
			return 0;
		case 85:
			if((__l__ && $c == 's')) {
				LOOKAHEAD($c);
				STATE = 86;
				return 1;
			}
			return 0;
		case 86:
			if((__l__ && $c == 'e')) {
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
			if($c >= 0) {
				__stkpush(89, ENGINE_stmt);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 89:
			return 0;
		case 2:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 90;
				return 1;
			}
			return 0;
		case 90:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 91;
				return 1;
			}
			return 0;
		case 91:
			if((__l__ && $c == 'a')) {
				LOOKAHEAD($c);
				STATE = 92;
				return 1;
			}
			return 0;
		case 92:
			if((__l__ && $c == 'k')) {
				LOOKAHEAD($c);
				STATE = 93;
				return 1;
			}
			return 0;
		case 93:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 94;
				return 1;
			}
			return 0;
		case 94:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 94;
				return 1;
			} else if(($c == ';')) {
				STATE = 95;
				return 1;
			}
			return 0;
		case 95:
			return 0;
		case 1:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 96;
				return 1;
			}
			return 0;
		case 96:
			if((__l__ && $c == 't')) {
				LOOKAHEAD($c);
				STATE = 97;
				return 1;
			}
			return 0;
		case 97:
			if((__l__ && $c == 'u')) {
				LOOKAHEAD($c);
				STATE = 98;
				return 1;
			}
			return 0;
		case 98:
			if((__l__ && $c == 'r')) {
				LOOKAHEAD($c);
				STATE = 99;
				return 1;
			}
			return 0;
		case 99:
			if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 100;
				return 1;
			}
			return 0;
		case 100:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 101;
				return 1;
			}
			return 0;
		case 101:
			if(($c == ';')) {
				STATE = 102;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 101;
				return 1;
			} else if($c >= 0) {
				__stkpush(103, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 103:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 103;
				return 1;
			} else if(($c == ';')) {
				STATE = 102;
				return 1;
			}
			return 0;
		case 102:
			return 0;
		}
		return 0;
	}

	private boolean stmt_accepted() {
		return (STATE == 102 ||
				STATE == 34 ||
				STATE == 84 ||
				STATE == 19 ||
				STATE == 66 ||
				STATE == 25 ||
				STATE == 95 ||
				STATE == 11 ||
				STATE == 89 ||
				STATE == 47 ||
				STATE == 13 ||
				STATE == 75);
	}

	int stmt_execaction(int  $c) {
		switch(STATE) {
		case 98:
			break;
		case 59:
			break;
		case 12:
			break;
		case 68:
			break;
		case 34:
			_continue();
			break;
		case 70:
			break;
		case 95:
			_break();
			break;
		case 85:
			break;
		case 29:
			break;
		case 6:
			break;
		case 3:
			break;
		case 22:
			break;
		case 26:
			break;
		case 96:
			break;
		case 82:
			break;
		case 60:
			(__stv[__slen - 1][60]) = _e;  
			_e = null;
			break;
		case 80:
			break;
		case 93:
			break;
		case 2:
			break;
		case 16:
			break;
		case 83:
			break;
		case 7:
			break;
		case 13:
			endb(((BlockAST)(__stv[__slen - 1][9])));
			break;
		case 0:
			break;
		case 99:
			break;
		case 103:
			break;
		case 58:
			break;
		case 73:
			break;
		case 30:
			break;
		case 61:
			break;
		case 20:
			break;
		case 52:
			break;
		case 11:
			simple();
			break;
		case 35:
			break;
		case 36:
			break;
		case 17:
			break;
		case 48:
			break;
		case 5:
			break;
		case 31:
			break;
		case 90:
			break;
		case 46:
			break;
		case 65:
			(__stv[__slen - 1][65]) = _e;
			break;
		case 101:
			_e = null;
			break;
		case 45:
			break;
		case 97:
			break;
		case 78:
			break;
		case 15:
			break;
		case 4:
			break;
		case 64:
			break;
		case 39:
			break;
		case 53:
			break;
		case 18:
			break;
		case 66:
			endfor(((AST)(__stv[__slen - 1][55])), ((AST)(__stv[__slen - 1][60])), ((AST)(__stv[__slen - 1][65])));
			break;
		case 40:
			break;
		case 75:
			endwh(((AST)(__stv[__slen - 1][74])));
			break;
		case 100:
			break;
		case 50:
			_e = null;
			break;
		case 32:
			break;
		case 88:
			(__stv[__slen - 1][88]) = _s;
			break;
		case 47:
			enddo();
			break;
		case 87:
			break;
		case 44:
			break;
		case 27:
			break;
		case 28:
			break;
		case 51:
			break;
		case 71:
			break;
		case 81:
			break;
		case 37:
			break;
		case 1:
			break;
		case 54:
			break;
		case 79:
			(__stv[__slen - 1][79]) = _e;
			break;
		case 91:
			break;
		case 84:
			endif(((AST)(__stv[__slen - 1][79])));
			break;
		case 42:
			break;
		case 67:
			break;
		case 43:
			break;
		case 69:
			break;
		case 56:
			break;
		case 41:
			break;
		case 76:
			break;
		case 25:
			_throw();
			break;
		case 19:
			break;
		case 77:
			break;
		case 62:
			break;
		case 38:
			break;
		case 94:
			break;
		case 49:
			break;
		case 21:
			break;
		case 63:
			break;
		case 74:
			(__stv[__slen - 1][74]) = _e;
			break;
		case 72:
			break;
		case 10:
			break;
		case 55:
			(__stv[__slen - 1][55]) = _e;  
			_e = null;
			break;
		case 23:
			break;
		case 24:
			break;
		case 92:
			break;
		case 8:
			break;
		case 14:
			addb(((BlockAST)(__stv[__slen - 1][9])));
			break;
		case 102:
			_return();
			break;
		case 33:
			break;
		case 86:
			break;
		case 9:
			(__stv[__slen - 1][9]) = beginb();
			break;
		case 89:
			endif(((AST)(__stv[__slen - 1][79])), ((SAST)(__stv[__slen - 1][88])));
			break;
		case 57:
			break;
		}
		return 1;
	}

	boolean stmt_isend() {
		return (STATE == 55 ||
				STATE == 64 ||
				STATE == 54 ||
				STATE == 52 ||
				STATE == 82 ||
				STATE == 59 ||
				STATE == 9 ||
				STATE == 57 ||
				STATE == 79 ||
				STATE == 62 ||
				STATE == 14 ||
				STATE == 60);
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
			return 104;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 102 ||
				STATE == 34 ||
				STATE == 84 ||
				STATE == 19 ||
				STATE == 66 ||
				STATE == 25 ||
				STATE == 95 ||
				STATE == 11 ||
				STATE == 89 ||
				STATE == 47 ||
				STATE == 13 ||
				STATE == 75);
		}

		boolean isEmptyTransition() {
		return (STATE == 55 ||
				STATE == 52 ||
				STATE == 9 ||
				STATE == 57 ||
				STATE == 79 ||
				STATE == 62 ||
				STATE == 14 ||
				STATE == 60);
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
		case 4:
			land(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 3:
			break;
		case 2:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
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
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 4:
			break;
		case 7:
			leq(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 2:
			break;
		case 3:
			break;
		case 5:
			lne(((AST)(__stv[__slen - 1][1])));
			break;
		case 6:
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
			} else if(($c == '[')) {
				STATE = 4;
				return 1;
			} else if(($c == '(')) {
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c == ')')) {
				STATE = 6;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 5;
				return 1;
			} else if($c < 0) {
				STATE = 7;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 7;
				return 1;
			}
		case 7:
			if($c >= 0) {
				__stkpush(8, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 8:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 8;
				return 1;
			} else if(($c == ',')) {
				STATE = 7;
				return 1;
			} else if(($c == ')')) {
				STATE = 6;
				return 1;
			}
			return 0;
		case 6:
			return 0;
		case 4:
			if($c >= 0) {
				__stkpush(9, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
			if(($c == ']')) {
				STATE = 10;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 9;
				return 1;
			}
			return 0;
		case 10:
				STATE = 11;
				return 1;
		case 11:
			if(($c == '[')) {
				STATE = 4;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 11;
				return 1;
			}
			return 0;
		case 3:
			if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 12;
				return 1;
			}
			return 0;
		case 12:
			return 0;
		case 2:
			if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 13;
				return 1;
			}
			return 0;
		case 13:
			return 0;
		}
		return 0;
	}

	private boolean prex_accepted() {
		return (STATE == 1 ||
				STATE == 6 ||
				STATE == 10 ||
				STATE == 11 ||
				STATE == 12 ||
				STATE == 13);
	}

	int prex_execaction(int  $c) {
		switch(STATE) {
		case 1:
			break;
		case 12:
			incpost();
			break;
		case 5:
			(__stv[__slen - 1][5]) = begincall();
			break;
		case 8:
			addcall(((CallAST)(__stv[__slen - 1][5])));
			break;
		case 3:
			break;
		case 0:
			break;
		case 2:
			break;
		case 4:
			(__stv[__slen - 1][4]) = _e;
			break;
		case 6:
			call(((CallAST)(__stv[__slen - 1][5])));
			break;
		case 10:
			_e = new ArrayIndexAST(((AST)(__stv[__slen - 1][4])), _e);
			break;
		case 13:
			decpost();
			break;
		case 7:
			break;
		case 9:
			break;
		case 11:
			break;
		}
		return 1;
	}

	boolean prex_isend() {
		return (STATE == 5 ||
				STATE == 10);
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
			return 14;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 6 ||
				STATE == 12 ||
				STATE == 13);
		}

		boolean isEmptyTransition() {
		return (STATE == 10);
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

	private int dot_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 1:
			if(($c == '.')) {
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			}
			return 0;
		case 2:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 2;
				return 1;
			} else if($c >= 0) {
				__stkpush(3, ENGINE_prex);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 3:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean dot_accepted() {
		return (STATE == 1 ||
				STATE == 3);
	}

	int dot_execaction(int  $c) {
		switch(STATE) {
		case 0:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 2:
			break;
		case 3:
			dot(((AST)(__stv[__slen - 1][1])));
			break;
		}
		return 1;
	}

	boolean dot_isend() {
		return (STATE == 3);
	}

	private final Engine ENGINE_dot = new Engine() {

		int step(int c) throws java.io.IOException {
			return dot_step(c);
		}

		boolean accepted() {
			return dot_accepted();
		}

		int execaction(int c) {
			return dot_execaction(c);
		}

		boolean isend() {
			return dot_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 4;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return false;
		}

		boolean isEmptyTransition() {
		return (STATE == 3);
		}

		public String toString() {
			return "dot";
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
			if((__l__ && $c == '|')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if((__l__ && $c == '-')) {
				LOOKAHEAD($c);
				STATE = 3;
				return 1;
			} else if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			} else if((__l__ && $c == '+')) {
				LOOKAHEAD($c);
				STATE = 5;
				return 1;
			} else if((__l__ && $c == '/')) {
				LOOKAHEAD($c);
				STATE = 6;
				return 1;
			} else if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 7;
				return 1;
			} else if((__l__ && $c == '&')) {
				LOOKAHEAD($c);
				STATE = 8;
				return 1;
			} else if((__l__ && $c == '^')) {
				LOOKAHEAD($c);
				STATE = 9;
				return 1;
			} else if((__l__ && $c == '*')) {
				LOOKAHEAD($c);
				STATE = 10;
				return 1;
			} else if((__l__ && $c == '%')) {
				LOOKAHEAD($c);
				STATE = 11;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 1;
				return 1;
			} else if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 12;
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
			if((__l__ && $c == '>')) {
				LOOKAHEAD($c);
				STATE = 27;
				return 1;
			}
			return 0;
		case 27:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 28;
				return 1;
			}
			return 0;
		case 28:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 28;
				return 1;
			} else if($c < 0) {
				STATE = 29;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 29;
				return 1;
			}
		case 29:
			if($c >= 0) {
				__stkpush(30, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 30:
			return 0;
		case 6:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 31;
				return 1;
			}
			return 0;
		case 31:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 31;
				return 1;
			} else if($c < 0) {
				STATE = 32;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 32;
				return 1;
			}
		case 32:
			if($c >= 0) {
				__stkpush(33, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 33:
			return 0;
		case 5:
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
			if((__l__ && $c == '<')) {
				LOOKAHEAD($c);
				STATE = 37;
				return 1;
			}
			return 0;
		case 37:
			if(($c == '=')) {
				LOOKAHEAD_COMMIT();
				STATE = 38;
				return 1;
			}
			return 0;
		case 38:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 38;
				return 1;
			} else if($c < 0) {
				STATE = 39;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 39;
				return 1;
			}
		case 39:
			if($c >= 0) {
				__stkpush(40, ENGINE_asgn);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 40:
			return 0;
		case 3:
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
				STATE == 33 ||
				STATE == 20 ||
				STATE == 36 ||
				STATE == 23 ||
				STATE == 43 ||
				STATE == 40 ||
				STATE == 26 ||
				STATE == 46 ||
				STATE == 14 ||
				STATE == 30);
	}

	int asgn_execaction(int  $c) {
		switch(STATE) {
		case 28:
			break;
		case 6:
			break;
		case 24:
			break;
		case 27:
			break;
		case 43:
			asgn(((AST)(__stv[__slen - 1][42])), AssignAST.Type.SUB);
			break;
		case 46:
			asgn(((AST)(__stv[__slen - 1][45])), AssignAST.Type.BOR);
			break;
		case 5:
			break;
		case 14:
			asgn(((AST)(__stv[__slen - 1][13])), null);
			break;
		case 0:
			break;
		case 22:
			(__stv[__slen - 1][22]) = _e;
			break;
		case 31:
			break;
		case 37:
			break;
		case 42:
			(__stv[__slen - 1][42]) = _e;
			break;
		case 9:
			break;
		case 40:
			asgn(((AST)(__stv[__slen - 1][39])), AssignAST.Type.SHL);
			break;
		case 33:
			asgn(((AST)(__stv[__slen - 1][32])), AssignAST.Type.DIV);
			break;
		case 13:
			(__stv[__slen - 1][13]) = _e;
			break;
		case 7:
			break;
		case 30:
			asgn(((AST)(__stv[__slen - 1][29])), AssignAST.Type.SHR);
			break;
		case 11:
			break;
		case 8:
			break;
		case 12:
			break;
		case 26:
			asgn(((AST)(__stv[__slen - 1][25])), AssignAST.Type.BAND);
			break;
		case 17:
			asgn(((AST)(__stv[__slen - 1][16])), AssignAST.Type.REM);
			break;
		case 23:
			asgn(((AST)(__stv[__slen - 1][22])), AssignAST.Type.BXOR);
			break;
		case 41:
			break;
		case 3:
			break;
		case 35:
			(__stv[__slen - 1][35]) = _e;
			break;
		case 38:
			break;
		case 15:
			break;
		case 34:
			break;
		case 18:
			break;
		case 21:
			break;
		case 1:
			break;
		case 10:
			break;
		case 45:
			(__stv[__slen - 1][45]) = _e;
			break;
		case 19:
			(__stv[__slen - 1][19]) = _e;
			break;
		case 44:
			break;
		case 39:
			(__stv[__slen - 1][39]) = _e;
			break;
		case 25:
			(__stv[__slen - 1][25]) = _e;
			break;
		case 32:
			(__stv[__slen - 1][32]) = _e;
			break;
		case 16:
			(__stv[__slen - 1][16]) = _e;
			break;
		case 20:
			asgn(((AST)(__stv[__slen - 1][19])), AssignAST.Type.MUL);
			break;
		case 36:
			asgn(((AST)(__stv[__slen - 1][35])), AssignAST.Type.ADD);
			break;
		case 29:
			(__stv[__slen - 1][29]) = _e;
			break;
		case 2:
			break;
		case 4:
			break;
		}
		return 1;
	}

	boolean asgn_isend() {
		return (STATE == 34 ||
				STATE == 18 ||
				STATE == 21 ||
				STATE == 38 ||
				STATE == 24 ||
				STATE == 41 ||
				STATE == 12 ||
				STATE == 28 ||
				STATE == 44 ||
				STATE == 31 ||
				STATE == 15);
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
				STATE == 33 ||
				STATE == 20 ||
				STATE == 36 ||
				STATE == 23 ||
				STATE == 43 ||
				STATE == 40 ||
				STATE == 26 ||
				STATE == 46 ||
				STATE == 14 ||
				STATE == 30);
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
		case 4:
			break;
		case 3:
			break;
		case 2:
			break;
		case 6:
			bior(((AST)(__stv[__slen - 1][1])));
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 5:
			break;
		case 0:
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
			if(($c >= '0' && $c <= '9')) {
				$buffer = new StringBuffer();$buffer.append((char)$c);
				STATE = 1;
				return 1;
			} else if(($c == '-')) {
				$buffer = new StringBuffer();$buffer.append((char)$c);
				STATE = 2;
				return 1;
			} else if(($c == '"')) {
				STATE = 3;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 4;
				return 1;
			} else if(($c == '.')) {
				$buffer = new StringBuffer();$buffer.append((char)$c);
				STATE = 5;
				return 1;
			} else if(($c == '+')) {
				$buffer = new StringBuffer();
				STATE = 2;
				return 1;
			} else if(($c == '\'')) {
				STATE = 6;
				return 1;
			} else if(($c == '(')) {
				STATE = 7;
				return 1;
			}
			return 0;
		case 7:
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
		case 6:
			if(($c >= 0 && $c <= 9) || ($c >= 11 && $c <= '&') || ($c >= '(' && $c <= '[') || ($c >= ']' && $c <= 2147483647)) {
				STATE = 10;
				return 1;
			} else if(($c == '\\')) {
				STATE = 11;
				return 1;
			}
			return 0;
		case 11:
			if(($c == 'u')) {
				STATE = 12;
				return 1;
			} else if(($c == '"') || ($c == '\'') || ($c == '\\') || ($c == 'b') || ($c == 'f') || ($c == 'n') || ($c == 'r') || ($c == 't')) {
				STATE = 13;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				UNGET($c);
				STATE = 14;
				return 1;
			}
			return 0;
		case 14:
			if(($c >= '0' && $c <= '7')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 15;
				return 1;
			}
			return 0;
		case 15:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 16;
				return 1;
			}
			return 0;
		case 16:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 17;
				return 1;
			}
			return 0;
		case 17:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 18;
				return 1;
			}
			return 0;
		case 18:
			if(($c >= 0 && $c <= 2147483647)) {
				UNGET($c);
				STATE = 19;
				return 1;
			} else if($c < 0) {
				
				STATE = 19;
				return 1;
			}
			return 0;
		case 19:
			if(($c == '\'')) {
				STATE = 20;
				return 1;
			}
			return 0;
		case 20:
			return 0;
		case 13:
			if(($c == '\'')) {
				STATE = 21;
				return 1;
			}
			return 0;
		case 21:
			return 0;
		case 12:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 22;
				return 1;
			}
			return 0;
		case 22:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 23;
				return 1;
			}
			return 0;
		case 23:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 24;
				return 1;
			}
			return 0;
		case 24:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 25;
				return 1;
			}
			return 0;
		case 25:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 26;
				return 1;
			}
			return 0;
		case 26:
			if(($c >= 0 && $c <= 2147483647)) {
				UNGET($c);
				STATE = 27;
				return 1;
			} else if($c < 0) {
				
				STATE = 27;
				return 1;
			}
			return 0;
		case 27:
			if(($c == '\'')) {
				STATE = 20;
				return 1;
			}
			return 0;
		case 10:
			if(($c == '\'')) {
				STATE = 28;
				return 1;
			}
			return 0;
		case 28:
			return 0;
		case 5:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 29;
				return 1;
			}
			return 0;
		case 29:
			if(($c == 'E') || ($c == 'e')) {
				$buffer.append((char)$c);
				STATE = 30;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 29;
				return 1;
			} else if($c < 0) {
				UNGET($c);$num=Double.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			} else if($c >= 0) {
				UNGET($c);$num=Double.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			}
		case 31:
			if(($c == 'F') || ($c == 'f')) {
				STATE = 32;
				return 1;
			} else if($c < 0) {
				STATE = 33;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 33;
				return 1;
			}
		case 33:
			return 0;
		case 32:
			return 0;
		case 30:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 34;
				return 1;
			} else if(($c == '+') || ($c == '-')) {
				$buffer.append((char)$c);
				STATE = 35;
				return 1;
			}
			return 0;
		case 35:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 34;
				return 1;
			}
			return 0;
		case 34:
			if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 34;
				return 1;
			} else if($c < 0) {
				UNGET($c);$num=Double.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			} else if($c >= 0) {
				UNGET($c);$num=Double.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			}
		case 4:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 36;
				return 1;
			}
			return 0;
		case 36:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 37;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 38;
				return 1;
			} else if($c < 0) {
				
				STATE = 38;
				return 1;
			}
			return 0;
		case 38:
			return 0;
		case 37:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 37;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 38;
				return 1;
			} else if($c < 0) {
				
				STATE = 38;
				return 1;
			}
			return 0;
		case 3:
			if($c >= 0) {
				__stkpush(39, ENGINE_str);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 39:
			return 0;
		case 2:
			if(($c == '.')) {
				$buffer.append((char)$c);
				STATE = 5;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 1;
				return 1;
			}
			return 0;
		case 1:
			if(($c == 'E') || ($c == 'e')) {
				$buffer.append((char)$c);
				STATE = 30;
				return 1;
			} else if(($c == '.')) {
				$buffer.append((char)$c);
				STATE = 5;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				$buffer.append((char)$c);
				STATE = 1;
				return 1;
			} else if($c < 0) {
				UNGET($c);$num=Integer.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			} else if($c >= 0) {
				UNGET($c);$num=Integer.valueOf($buffer.toString());
				STATE = 31;
				return 1;
			}
		}
		return 0;
	}

	private boolean elem_accepted() {
		return (STATE == 32 ||
				STATE == 33 ||
				STATE == 38 ||
				STATE == 21 ||
				STATE == 39 ||
				STATE == 20 ||
				STATE == 9 ||
				STATE == 28 ||
				STATE == 31);
	}

	int elem_execaction(int  $c) {
		switch(STATE) {
		case 2:
			break;
		case 21:
			break;
		case 15:
			break;
		case 19:
			char1(Integer.parseInt($buffer.toString(), 8));
			break;
		case 28:
			break;
		case 22:
			break;
		case 27:
			char1(Integer.parseInt($buffer.toString(), 16));
			break;
		case 39:
			str();
			break;
		case 10:
			char1($c);
			break;
		case 18:
			break;
		case 36:
			break;
		case 13:
			charesc1($c);
			break;
		case 29:
			break;
		case 0:
			break;
		case 4:
			break;
		case 26:
			break;
		case 25:
			break;
		case 9:
			break;
		case 7:
			break;
		case 31:
			break;
		case 34:
			break;
		case 5:
			break;
		case 6:
			break;
		case 11:
			break;
		case 33:
			konst($num);
			break;
		case 16:
			break;
		case 3:
			break;
		case 17:
			break;
		case 35:
			break;
		case 38:
			var($buffer.toString());
			break;
		case 1:
			break;
		case 14:
			break;
		case 20:
			break;
		case 23:
			break;
		case 30:
			break;
		case 8:
			break;
		case 32:
			konstfloat($num);
			break;
		case 24:
			break;
		case 37:
			break;
		case 12:
			break;
		}
		return 1;
	}

	boolean elem_isend() {
		return (STATE == 34 ||
				STATE == 1 ||
				STATE == 18 ||
				STATE == 36 ||
				STATE == 37 ||
				STATE == 26 ||
				STATE == 29 ||
				STATE == 31);
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
			return 40;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 32 ||
				STATE == 33 ||
				STATE == 38 ||
				STATE == 21 ||
				STATE == 39 ||
				STATE == 20 ||
				STATE == 9 ||
				STATE == 28);
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
			if(($c == '~')) {
				LOOKAHEAD_COMMIT();
				STATE = 1;
				return 1;
			} else if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 2;
				return 1;
			} else if(($c == '!')) {
				LOOKAHEAD_COMMIT();
				STATE = 3;
				return 1;
			} else if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 4;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c >= 0) {
				__stkpush(5, ENGINE_dot);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 5:
			return 0;
		case 4:
			if(($c == '+')) {
				LOOKAHEAD_COMMIT();
				STATE = 6;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 4;
				return 1;
			} else if($c >= 0) {
				__stkpush(7, ENGINE_dot);
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
				__stkpush(8, ENGINE_dot);
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
				__stkpush(9, ENGINE_dot);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 9:
			return 0;
		case 2:
			if(($c == '-')) {
				LOOKAHEAD_COMMIT();
				STATE = 10;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 2;
				return 1;
			} else if($c >= 0) {
				__stkpush(11, ENGINE_dot);
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
				__stkpush(12, ENGINE_dot);
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
				__stkpush(13, ENGINE_dot);
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
		case 12:
			decpre();
			break;
		case 2:
			break;
		case 6:
			break;
		case 11:
			uminus();
			break;
		case 0:
			break;
		case 9:
			lnot();
			break;
		case 1:
			break;
		case 13:
			bnot();
			break;
		case 8:
			incpre();
			break;
		case 10:
			break;
		case 7:
			break;
		case 4:
			break;
		case 3:
			break;
		case 5:
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

	private int decltype_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 1;
				return 1;
			}
			return 0;
		case 1:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 2;
				return 1;
			}
			return 0;
		case 2:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 3;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 4;
				return 1;
			} else if($c < 0) {
				
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
				STATE = 5;
				return 1;
		case 5:
				STATE = 6;
				return 1;
		case 6:
			if(($c == '[')) {
				STATE = 7;
				return 1;
			} else if($c < 0) {
				STATE = 8;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 8;
				return 1;
			}
		case 8:
			return 0;
		case 7:
			if(($c == ']')) {
				STATE = 9;
				return 1;
			}
			return 0;
		case 9:
				STATE = 6;
				return 1;
		case 3:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 3;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 4;
				return 1;
			} else if($c < 0) {
				
				STATE = 4;
				return 1;
			}
			return 0;
		}
		return 0;
	}

	private boolean decltype_accepted() {
		return (STATE == 8);
	}

	int decltype_execaction(int  $c) {
		switch(STATE) {
		case 5:
			(__stv[__slen - 1][5]) = 0;
			break;
		case 4:
			(__stv[__slen - 1][4]) = $buffer.toString();
			break;
		case 6:
			break;
		case 9:
			(__stv[__slen - 1][5]) = ((Integer)(__stv[__slen - 1][5])) + 1;
			break;
		case 0:
			break;
		case 7:
			break;
		case 1:
			break;
		case 2:
			break;
		case 8:
			decltype(((String)(__stv[__slen - 1][4])), ((Integer)(__stv[__slen - 1][5])));
			break;
		case 3:
			break;
		}
		return 1;
	}

	boolean decltype_isend() {
		return (STATE == 2 ||
				STATE == 3 ||
				STATE == 4 ||
				STATE == 5 ||
				STATE == 6 ||
				STATE == 9);
	}

	private final Engine ENGINE_decltype = new Engine() {

		int step(int c) throws java.io.IOException {
			return decltype_step(c);
		}

		boolean accepted() {
			return decltype_accepted();
		}

		int execaction(int c) {
			return decltype_execaction(c);
		}

		boolean isend() {
			return decltype_isend();
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
		return (STATE == 8);
		}

		boolean isEmptyTransition() {
		return (STATE == 4 ||
				STATE == 5 ||
				STATE == 9);
		}

		public String toString() {
			return "decltype";
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
		case 3:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 5:
			break;
		case 4:
			break;
		case 0:
			break;
		case 6:
			band(((AST)(__stv[__slen - 1][1])));
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

	private int str_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c < 0) {
				STATE = 1;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 1;
				return 1;
			}
		case 1:
			if(($c >= 0 && $c <= 9) || ($c >= 11 && $c <= '!') || ($c >= '#' && $c <= '[') || ($c >= ']' && $c <= 2147483647)) {
				STATE = 2;
				return 1;
			} else if(($c == '"')) {
				STATE = 3;
				return 1;
			} else if(($c == '\\')) {
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if(($c == 'u')) {
				STATE = 5;
				return 1;
			} else if(($c == '"') || ($c == '\\') || ($c == 'b') || ($c == 'f') || ($c == 'n') || ($c == 'r') || ($c == 't')) {
				STATE = 6;
				return 1;
			} else if(($c >= '0' && $c <= '9')) {
				UNGET($c);
				STATE = 7;
				return 1;
			}
			return 0;
		case 7:
			if(($c >= '0' && $c <= '7')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 8;
				return 1;
			}
			return 0;
		case 8:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 9;
				return 1;
			}
			return 0;
		case 9:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 10;
				return 1;
			}
			return 0;
		case 10:
			if(($c >= '0' && $c <= '7')) {
				$buffer.append((char)$c);
				STATE = 11;
				return 1;
			}
			return 0;
		case 11:
			if(($c >= 0 && $c <= 2147483647)) {
				UNGET($c);
				STATE = 12;
				return 1;
			} else if($c < 0) {
				
				STATE = 12;
				return 1;
			}
			return 0;
		case 12:
				STATE = 1;
				return 1;
		case 6:
				STATE = 1;
				return 1;
		case 5:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 13;
				return 1;
			}
			return 0;
		case 13:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 14;
				return 1;
			}
			return 0;
		case 14:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 15;
				return 1;
			}
			return 0;
		case 15:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 16;
				return 1;
			}
			return 0;
		case 16:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'F') || ($c >= 'a' && $c <= 'f')) {
				$buffer.append((char)$c);
				STATE = 17;
				return 1;
			}
			return 0;
		case 17:
			if(($c >= 0 && $c <= 2147483647)) {
				UNGET($c);
				STATE = 18;
				return 1;
			} else if($c < 0) {
				
				STATE = 18;
				return 1;
			}
			return 0;
		case 18:
				STATE = 1;
				return 1;
		case 3:
			return 0;
		case 2:
				STATE = 1;
				return 1;
		}
		return 0;
	}

	private boolean str_accepted() {
		return (STATE == 3);
	}

	int str_execaction(int  $c) {
		switch(STATE) {
		case 13:
			break;
		case 10:
			break;
		case 6:
			escStr($c);
			break;
		case 11:
			break;
		case 15:
			break;
		case 16:
			break;
		case 4:
			break;
		case 12:
			addStr(Integer.parseInt($buffer.toString(), 8));
			break;
		case 0:
			beginStr();
			break;
		case 3:
			break;
		case 5:
			break;
		case 18:
			addStr(Integer.parseInt($buffer.toString(), 16));
			break;
		case 17:
			break;
		case 1:
			break;
		case 2:
			addStr($c);
			break;
		case 8:
			break;
		case 9:
			break;
		case 14:
			break;
		case 7:
			break;
		}
		return 1;
	}

	boolean str_isend() {
		return (STATE == 17 ||
				STATE == 0 ||
				STATE == 2 ||
				STATE == 18 ||
				STATE == 6 ||
				STATE == 11 ||
				STATE == 12);
	}

	private final Engine ENGINE_str = new Engine() {

		int step(int c) throws java.io.IOException {
			return str_step(c);
		}

		boolean accepted() {
			return str_accepted();
		}

		int execaction(int c) {
			return str_execaction(c);
		}

		boolean isend() {
			return str_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 19;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 3);
		}

		boolean isEmptyTransition() {
		return (STATE == 2 ||
				STATE == 18 ||
				STATE == 6 ||
				STATE == 12);
		}

		public String toString() {
			return "str";
		}

	};

	private int cast_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if((__l__ && $c == '(')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 1;
				return 1;
			} else if((__l__ && $c == 'n')) {
				LOOKAHEAD($c);
				STATE = 2;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 0;
				return 1;
			} else if($c >= 0) {
				__stkpush(3, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 3:
			return 0;
		case 2:
			if((__l__ && $c == 'e')) {
				LOOKAHEAD($c);
				STATE = 4;
				return 1;
			}
			return 0;
		case 4:
			if((__l__ && $c == 'w')) {
				LOOKAHEAD($c);
				STATE = 5;
				return 1;
			}
			return 0;
		case 5:
			if(($c >= 0 && $c <= '@') || ($c >= '[' && $c <= '`') || ($c >= '{' && $c <= 169) || ($c >= 171 && $c <= 180) || ($c >= 182 && $c <= 185) || ($c >= 187 && $c <= 191) || ($c == 215) || ($c == 247) || ($c >= 706 && $c <= 709) || ($c >= 722 && $c <= 735) || ($c >= 741 && $c <= 747) || ($c == 749) || ($c >= 751 && $c <= 879) || ($c == 885) || ($c >= 888 && $c <= 889) || ($c >= 894 && $c <= 901) || ($c == 903) || ($c == 907) || ($c == 909) || ($c == 930) || ($c == 1014) || ($c >= 1154 && $c <= 1161) || ($c >= 1320 && $c <= 1328) || ($c >= 1367 && $c <= 1368) || ($c >= 1370 && $c <= 1376) || ($c >= 1416 && $c <= 1487) || ($c >= 1515 && $c <= 1519) || ($c >= 1523 && $c <= 1567) || ($c >= 1611 && $c <= 1645) || ($c == 1648) || ($c == 1748) || ($c >= 1750 && $c <= 1764) || ($c >= 1767 && $c <= 1773) || ($c >= 1776 && $c <= 1785) || ($c >= 1789 && $c <= 1790) || ($c >= 1792 && $c <= 1807) || ($c == 1809) || ($c >= 1840 && $c <= 1868) || ($c >= 1958 && $c <= 1968) || ($c >= 1970 && $c <= 1993) || ($c >= 2027 && $c <= 2035) || ($c >= 2038 && $c <= 2041) || ($c >= 2043 && $c <= 2047) || ($c >= 2070 && $c <= 2073) || ($c >= 2075 && $c <= 2083) || ($c >= 2085 && $c <= 2087) || ($c >= 2089 && $c <= 2111) || ($c >= 2137 && $c <= 2307) || ($c >= 2362 && $c <= 2364) || ($c >= 2366 && $c <= 2383) || ($c >= 2385 && $c <= 2391) || ($c >= 2402 && $c <= 2416) || ($c == 2424) || ($c >= 2432 && $c <= 2436) || ($c >= 2445 && $c <= 2446) || ($c >= 2449 && $c <= 2450) || ($c == 2473) || ($c == 2481) || ($c >= 2483 && $c <= 2485) || ($c >= 2490 && $c <= 2492) || ($c >= 2494 && $c <= 2509) || ($c >= 2511 && $c <= 2523) || ($c == 2526) || ($c >= 2530 && $c <= 2543) || ($c >= 2546 && $c <= 2564) || ($c >= 2571 && $c <= 2574) || ($c >= 2577 && $c <= 2578) || ($c == 2601) || ($c == 2609) || ($c == 2612) || ($c == 2615) || ($c >= 2618 && $c <= 2648) || ($c == 2653) || ($c >= 2655 && $c <= 2673) || ($c >= 2677 && $c <= 2692) || ($c == 2702) || ($c == 2706) || ($c == 2729) || ($c == 2737) || ($c == 2740) || ($c >= 2746 && $c <= 2748) || ($c >= 2750 && $c <= 2767) || ($c >= 2769 && $c <= 2783) || ($c >= 2786 && $c <= 2820) || ($c >= 2829 && $c <= 2830) || ($c >= 2833 && $c <= 2834) || ($c == 2857) || ($c == 2865) || ($c == 2868) || ($c >= 2874 && $c <= 2876) || ($c >= 2878 && $c <= 2907) || ($c == 2910) || ($c >= 2914 && $c <= 2928) || ($c >= 2930 && $c <= 2946) || ($c == 2948) || ($c >= 2955 && $c <= 2957) || ($c == 2961) || ($c >= 2966 && $c <= 2968) || ($c == 2971) || ($c == 2973) || ($c >= 2976 && $c <= 2978) || ($c >= 2981 && $c <= 2983) || ($c >= 2987 && $c <= 2989) || ($c >= 3002 && $c <= 3023) || ($c >= 3025 && $c <= 3076) || ($c == 3085) || ($c == 3089) || ($c == 3113) || ($c == 3124) || ($c >= 3130 && $c <= 3132) || ($c >= 3134 && $c <= 3159) || ($c >= 3162 && $c <= 3167) || ($c >= 3170 && $c <= 3204) || ($c == 3213) || ($c == 3217) || ($c == 3241) || ($c == 3252) || ($c >= 3258 && $c <= 3260) || ($c >= 3262 && $c <= 3293) || ($c == 3295) || ($c >= 3298 && $c <= 3312) || ($c >= 3315 && $c <= 3332) || ($c == 3341) || ($c == 3345) || ($c >= 3387 && $c <= 3388) || ($c >= 3390 && $c <= 3405) || ($c >= 3407 && $c <= 3423) || ($c >= 3426 && $c <= 3449) || ($c >= 3456 && $c <= 3460) || ($c >= 3479 && $c <= 3481) || ($c == 3506) || ($c == 3516) || ($c >= 3518 && $c <= 3519) || ($c >= 3527 && $c <= 3584) || ($c == 3633) || ($c >= 3636 && $c <= 3647) || ($c >= 3655 && $c <= 3712) || ($c == 3715) || ($c >= 3717 && $c <= 3718) || ($c == 3721) || ($c >= 3723 && $c <= 3724) || ($c >= 3726 && $c <= 3731) || ($c == 3736) || ($c == 3744) || ($c == 3748) || ($c == 3750) || ($c >= 3752 && $c <= 3753) || ($c == 3756) || ($c == 3761) || ($c >= 3764 && $c <= 3772) || ($c >= 3774 && $c <= 3775) || ($c == 3781) || ($c >= 3783 && $c <= 3803) || ($c >= 3806 && $c <= 3839) || ($c >= 3841 && $c <= 3903) || ($c == 3912) || ($c >= 3949 && $c <= 3975) || ($c >= 3981 && $c <= 4095) || ($c >= 4139 && $c <= 4158) || ($c >= 4160 && $c <= 4175) || ($c >= 4182 && $c <= 4185) || ($c >= 4190 && $c <= 4192) || ($c >= 4194 && $c <= 4196) || ($c >= 4199 && $c <= 4205) || ($c >= 4209 && $c <= 4212) || ($c >= 4226 && $c <= 4237) || ($c >= 4239 && $c <= 4255) || ($c >= 4294 && $c <= 4303) || ($c == 4347) || ($c >= 4349 && $c <= 4351) || ($c == 4681) || ($c >= 4686 && $c <= 4687) || ($c == 4695) || ($c == 4697) || ($c >= 4702 && $c <= 4703) || ($c == 4745) || ($c >= 4750 && $c <= 4751) || ($c == 4785) || ($c >= 4790 && $c <= 4791) || ($c == 4799) || ($c == 4801) || ($c >= 4806 && $c <= 4807) || ($c == 4823) || ($c == 4881) || ($c >= 4886 && $c <= 4887) || ($c >= 4955 && $c <= 4991) || ($c >= 5008 && $c <= 5023) || ($c >= 5109 && $c <= 5120) || ($c >= 5741 && $c <= 5742) || ($c == 5760) || ($c >= 5787 && $c <= 5791) || ($c >= 5867 && $c <= 5887) || ($c == 5901) || ($c >= 5906 && $c <= 5919) || ($c >= 5938 && $c <= 5951) || ($c >= 5970 && $c <= 5983) || ($c == 5997) || ($c >= 6001 && $c <= 6015) || ($c >= 6068 && $c <= 6102) || ($c >= 6104 && $c <= 6107) || ($c >= 6109 && $c <= 6175) || ($c >= 6264 && $c <= 6271) || ($c == 6313) || ($c >= 6315 && $c <= 6319) || ($c >= 6390 && $c <= 6399) || ($c >= 6429 && $c <= 6479) || ($c >= 6510 && $c <= 6511) || ($c >= 6517 && $c <= 6527) || ($c >= 6572 && $c <= 6592) || ($c >= 6600 && $c <= 6655) || ($c >= 6679 && $c <= 6687) || ($c >= 6741 && $c <= 6822) || ($c >= 6824 && $c <= 6916) || ($c >= 6964 && $c <= 6980) || ($c >= 6988 && $c <= 7042) || ($c >= 7073 && $c <= 7085) || ($c >= 7088 && $c <= 7103) || ($c >= 7142 && $c <= 7167) || ($c >= 7204 && $c <= 7244) || ($c >= 7248 && $c <= 7257) || ($c >= 7294 && $c <= 7400) || ($c == 7405) || ($c >= 7410 && $c <= 7423) || ($c >= 7616 && $c <= 7679) || ($c >= 7958 && $c <= 7959) || ($c >= 7966 && $c <= 7967) || ($c >= 8006 && $c <= 8007) || ($c >= 8014 && $c <= 8015) || ($c == 8024) || ($c == 8026) || ($c == 8028) || ($c == 8030) || ($c >= 8062 && $c <= 8063) || ($c == 8117) || ($c == 8125) || ($c >= 8127 && $c <= 8129) || ($c == 8133) || ($c >= 8141 && $c <= 8143) || ($c >= 8148 && $c <= 8149) || ($c >= 8156 && $c <= 8159) || ($c >= 8173 && $c <= 8177) || ($c == 8181) || ($c >= 8189 && $c <= 8304) || ($c >= 8306 && $c <= 8318) || ($c >= 8320 && $c <= 8335) || ($c >= 8349 && $c <= 8449) || ($c >= 8451 && $c <= 8454) || ($c >= 8456 && $c <= 8457) || ($c == 8468) || ($c >= 8470 && $c <= 8472) || ($c >= 8478 && $c <= 8483) || ($c == 8485) || ($c == 8487) || ($c == 8489) || ($c == 8494) || ($c >= 8506 && $c <= 8507) || ($c >= 8512 && $c <= 8516) || ($c >= 8522 && $c <= 8525) || ($c >= 8527 && $c <= 8578) || ($c >= 8581 && $c <= 11263) || ($c == 11311) || ($c == 11359) || ($c >= 11493 && $c <= 11498) || ($c >= 11503 && $c <= 11519) || ($c >= 11558 && $c <= 11567) || ($c >= 11622 && $c <= 11630) || ($c >= 11632 && $c <= 11647) || ($c >= 11671 && $c <= 11679) || ($c == 11687) || ($c == 11695) || ($c == 11703) || ($c == 11711) || ($c == 11719) || ($c == 11727) || ($c == 11735) || ($c >= 11743 && $c <= 11822) || ($c >= 11824 && $c <= 12292) || ($c >= 12295 && $c <= 12336) || ($c >= 12342 && $c <= 12346) || ($c >= 12349 && $c <= 12352) || ($c >= 12439 && $c <= 12444) || ($c == 12448) || ($c == 12539) || ($c >= 12544 && $c <= 12548) || ($c >= 12590 && $c <= 12592) || ($c >= 12687 && $c <= 12703) || ($c >= 12731 && $c <= 12783) || ($c >= 12800 && $c <= 13311) || ($c >= 19894 && $c <= 19967) || ($c >= 40908 && $c <= 40959) || ($c >= 42125 && $c <= 42191) || ($c >= 42238 && $c <= 42239) || ($c >= 42509 && $c <= 42511) || ($c >= 42528 && $c <= 42537) || ($c >= 42540 && $c <= 42559) || ($c >= 42607 && $c <= 42622) || ($c >= 42648 && $c <= 42655) || ($c >= 42726 && $c <= 42774) || ($c >= 42784 && $c <= 42785) || ($c >= 42889 && $c <= 42890) || ($c == 42895) || ($c >= 42898 && $c <= 42911) || ($c >= 42922 && $c <= 43001) || ($c == 43010) || ($c == 43014) || ($c == 43019) || ($c >= 43043 && $c <= 43071) || ($c >= 43124 && $c <= 43137) || ($c >= 43188 && $c <= 43249) || ($c >= 43256 && $c <= 43258) || ($c >= 43260 && $c <= 43273) || ($c >= 43302 && $c <= 43311) || ($c >= 43335 && $c <= 43359) || ($c >= 43389 && $c <= 43395) || ($c >= 43443 && $c <= 43470) || ($c >= 43472 && $c <= 43519) || ($c >= 43561 && $c <= 43583) || ($c == 43587) || ($c >= 43596 && $c <= 43615) || ($c >= 43639 && $c <= 43641) || ($c >= 43643 && $c <= 43647) || ($c == 43696) || ($c >= 43698 && $c <= 43700) || ($c >= 43703 && $c <= 43704) || ($c >= 43710 && $c <= 43711) || ($c == 43713) || ($c >= 43715 && $c <= 43738) || ($c >= 43742 && $c <= 43776) || ($c >= 43783 && $c <= 43784) || ($c >= 43791 && $c <= 43792) || ($c >= 43799 && $c <= 43807) || ($c == 43815) || ($c >= 43823 && $c <= 43967) || ($c >= 44003 && $c <= 44031) || ($c >= 55204 && $c <= 55215) || ($c >= 55239 && $c <= 55242) || ($c >= 55292 && $c <= 63743) || ($c >= 64046 && $c <= 64047) || ($c >= 64110 && $c <= 64111) || ($c >= 64218 && $c <= 64255) || ($c >= 64263 && $c <= 64274) || ($c >= 64280 && $c <= 64284) || ($c == 64286) || ($c == 64297) || ($c == 64311) || ($c == 64317) || ($c == 64319) || ($c == 64322) || ($c == 64325) || ($c >= 64434 && $c <= 64466) || ($c >= 64830 && $c <= 64847) || ($c >= 64912 && $c <= 64913) || ($c >= 64968 && $c <= 65007) || ($c >= 65020 && $c <= 65135) || ($c == 65141) || ($c >= 65277 && $c <= 65312) || ($c >= 65339 && $c <= 65344) || ($c >= 65371 && $c <= 65381) || ($c >= 65471 && $c <= 65473) || ($c >= 65480 && $c <= 65481) || ($c >= 65488 && $c <= 65489) || ($c >= 65496 && $c <= 65497) || ($c >= 65501 && $c <= 2147483647)) {
				LOOKAHEAD_COMMIT();UNGET($c);
				STATE = 6;
				return 1;
			}
			return 0;
		case 6:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				UNGET($c);$buffer = new StringBuffer();
				STATE = 7;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 6;
				return 1;
			}
			return 0;
		case 7:
			if(($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 8;
				return 1;
			}
			return 0;
		case 8:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 9;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 10;
				return 1;
			} else if($c < 0) {
				
				STATE = 10;
				return 1;
			}
			return 0;
		case 10:
				STATE = 11;
				return 1;
		case 11:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 11;
				return 1;
			} else if($c < 0) {
				STATE = 12;
				return 1;
			} else if($c >= 0) {
				UNGET($c);
				STATE = 12;
				return 1;
			}
		case 12:
			if(($c == '[')) {
				STATE = 13;
				return 1;
			} else if(($c == '(')) {
				STATE = 14;
				return 1;
			}
			return 0;
		case 14:
			if(($c == ')')) {
				STATE = 15;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 14;
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
				__stkpush(17, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 17:
			if(($c == ',')) {
				STATE = 16;
				return 1;
			} else if(($c == ')')) {
				STATE = 15;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 17;
				return 1;
			}
			return 0;
		case 15:
			return 0;
		case 13:
			if($c >= 0) {
				__stkpush(18, ENGINE_expr);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 18:
			if(($c == ']')) {
				STATE = 19;
				return 1;
			}
			return 0;
		case 19:
				STATE = 20;
				return 1;
		case 20:
			if(($c == '[')) {
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
			return 0;
		case 21:
			if(($c == ']')) {
				STATE = 23;
				return 1;
			}
			return 0;
		case 23:
				STATE = 20;
				return 1;
		case 9:
			if(($c >= '0' && $c <= '9') || ($c >= 'A' && $c <= 'Z') || ($c == '_') || ($c >= 'a' && $c <= 'z')) {
				$buffer.append((char)$c);
				STATE = 9;
				return 1;
			} else if(($c >= 0 && $c <= '/') || ($c >= ':' && $c <= '@') || ($c >= '[' && $c <= '^') || ($c == '`') || ($c >= '{' && $c <= 2147483647)) {
				UNGET($c);
				STATE = 10;
				return 1;
			} else if($c < 0) {
				
				STATE = 10;
				return 1;
			}
			return 0;
		case 1:
			if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 24;
				return 1;
			} else if((__l__ && $c >= 'A' && $c <= 'Z') || (__l__ && $c == '_') || (__l__ && $c >= 'a' && $c <= 'z')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 25;
				return 1;
			}
			return 0;
		case 25:
			if((__l__ && $c == ')')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 26;
				return 1;
			} else if((__l__ && $c == '[')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 27;
				return 1;
			} else if((__l__ && $c >= '0' && $c <= '9') || (__l__ && $c >= 'A' && $c <= 'Z') || (__l__ && $c == '_') || (__l__ && $c >= 'a' && $c <= 'z')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 28;
				return 1;
			} else if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 29;
				return 1;
			}
			return 0;
		case 29:
			if((__l__ && $c == ')')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 26;
				return 1;
			} else if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 29;
				return 1;
			}
			return 0;
		case 28:
			if((__l__ && $c == ')')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 26;
				return 1;
			} else if((__l__ && $c == '[')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 27;
				return 1;
			} else if((__l__ && $c >= '0' && $c <= '9') || (__l__ && $c >= 'A' && $c <= 'Z') || (__l__ && $c == '_') || (__l__ && $c >= 'a' && $c <= 'z')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 28;
				return 1;
			} else if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 29;
				return 1;
			}
			return 0;
		case 27:
			if((__l__ && $c == ']')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 30;
				return 1;
			}
			return 0;
		case 30:
			if((__l__ && $c == ')')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 26;
				return 1;
			} else if((__l__ && $c == '[')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 27;
				return 1;
			} else if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 29;
				return 1;
			}
			return 0;
		case 26:
			if((__l__ && $c == '(') || (__l__ && $c == '.') || (__l__ && $c >= '0' && $c <= '9') || (__l__ && $c >= 'A' && $c <= 'Z') || (__l__ && $c == '_') || (__l__ && $c >= 'a' && $c <= 'z')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 31;
				return 1;
			}
			return 0;
		case 31:
			if((__l__ && $c >= 0 && $c <= 2147483647)) {
				LOOKAHEAD($c);LOOKAHEAD_COMMIT();
				STATE = 32;
				return 1;
			} else if($c < 0) {
				LOOKAHEAD_COMMIT();
				STATE = 32;
				return 1;
			}
			return 0;
		case 32:
			if(($c == '(')) {
				STATE = 33;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 32;
				return 1;
			}
			return 0;
		case 33:
			if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 33;
				return 1;
			} else if($c >= 0) {
				__stkpush(34, ENGINE_decltype);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 34:
				STATE = 35;
				return 1;
		case 35:
			if(($c == ')')) {
				STATE = 36;
				return 1;
			} else if(($c >= 9 && $c <= '\n') || ($c == ' ')) {
				STATE = 35;
				return 1;
			}
			return 0;
		case 36:
			if($c >= 0) {
				__stkpush(37, ENGINE_unary);
				STATE = 0;
				return NINA_ACCEPT;
			}
			return 0;
		case 37:
			return 0;
		case 24:
			if((__l__ && $c >= 9 && $c <= '\n') || (__l__ && $c == ' ')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 24;
				return 1;
			} else if((__l__ && $c >= 'A' && $c <= 'Z') || (__l__ && $c == '_') || (__l__ && $c >= 'a' && $c <= 'z')) {
				LOOKAHEAD($c);LOOKAHEAD_MARK_INIT();
				STATE = 25;
				return 1;
			}
			return 0;
		}
		return 0;
	}

	private boolean cast_accepted() {
		return (STATE == 3 ||
				STATE == 37 ||
				STATE == 22 ||
				STATE == 15);
	}

	int cast_execaction(int  $c) {
		switch(STATE) {
		case 7:
			break;
		case 22:
			newarray(((String)(__stv[__slen - 1][10])), _e, ((Integer)(__stv[__slen - 1][19])));
			break;
		case 33:
			break;
		case 35:
			break;
		case 4:
			break;
		case 8:
			break;
		case 14:
			(__stv[__slen - 1][14]) = beginnew(((String)(__stv[__slen - 1][10])));
			break;
		case 27:
			break;
		case 0:
			break;
		case 29:
			break;
		case 19:
			(__stv[__slen - 1][19]) = 0;
			break;
		case 3:
			break;
		case 20:
			break;
		case 32:
			break;
		case 34:
			(__stv[__slen - 1][34]) = decltype;
			break;
		case 24:
			break;
		case 10:
			(__stv[__slen - 1][10]) = $buffer.toString();
			break;
		case 1:
			break;
		case 16:
			break;
		case 2:
			break;
		case 31:
			break;
		case 12:
			break;
		case 5:
			break;
		case 17:
			addnew(((NewAST)(__stv[__slen - 1][14])));
			break;
		case 25:
			break;
		case 28:
			break;
		case 6:
			break;
		case 30:
			break;
		case 9:
			break;
		case 36:
			break;
		case 37:
			cast(((VariableType)(__stv[__slen - 1][34])));
			break;
		case 11:
			break;
		case 18:
			break;
		case 23:
			(__stv[__slen - 1][19]) = ((Integer)(__stv[__slen - 1][19])) + 1;
			break;
		case 26:
			break;
		case 21:
			break;
		case 15:
			neu(((NewAST)(__stv[__slen - 1][14])));
			break;
		case 13:
			break;
		}
		return 1;
	}

	boolean cast_isend() {
		return (STATE == 34 ||
				STATE == 19 ||
				STATE == 20 ||
				STATE == 23 ||
				STATE == 8 ||
				STATE == 9 ||
				STATE == 10 ||
				STATE == 11 ||
				STATE == 31 ||
				STATE == 14);
	}

	private final Engine ENGINE_cast = new Engine() {

		int step(int c) throws java.io.IOException {
			return cast_step(c);
		}

		boolean accepted() {
			return cast_accepted();
		}

		int execaction(int c) {
			return cast_execaction(c);
		}

		boolean isend() {
			return cast_isend();
		}

		int recover(Exception e) {
			return -1;
		}

		int deadState() {
			return -1;
		}

		int stateSize() {
			return 38;
		}

		int finallyState() {
			return -1;
		}

		boolean isDead() {
		return (STATE == 3 ||
				STATE == 37 ||
				STATE == 22 ||
				STATE == 15);
		}

		boolean isEmptyTransition() {
		return (STATE == 34 ||
				STATE == 19 ||
				STATE == 23 ||
				STATE == 10);
		}

		public String toString() {
			return "cast";
		}

	};

	private int term_step(int  $c)  throws java.io.IOException {
		boolean __l__ = __lookahead_ok;

		__lookahead_ok = true;
		switch(STATE) {
		case 0:
			if($c >= 0) {
				__stkpush(1, ENGINE_cast);
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
				__stkpush(6, ENGINE_cast);
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
				__stkpush(8, ENGINE_cast);
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
				__stkpush(10, ENGINE_cast);
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
		case 7:
			break;
		case 3:
			break;
		case 2:
			break;
		case 10:
			mul(((AST)(__stv[__slen - 1][1])));
			break;
		case 4:
			break;
		case 9:
			break;
		case 1:
			(__stv[__slen - 1][1]) = _e;
			break;
		case 6:
			div(((AST)(__stv[__slen - 1][1])));
			break;
		case 8:
			mod(((AST)(__stv[__slen - 1][1])));
			break;
		case 0:
			break;
		case 5:
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

	private int methodCompiler_step(int  $c)  throws java.io.IOException {
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

	private boolean methodCompiler_accepted() {
		return (STATE == 0 ||
				STATE == 1);
	}

	int methodCompiler_execaction(int  $c) {
		switch(STATE) {
		case 1:
			break;
		case 0:
			break;
		}
		return 1;
	}

	boolean methodCompiler_isend() {
		return false;
	}

	private final Engine ENGINE_methodCompiler = new Engine() {

		int step(int c) throws java.io.IOException {
			return methodCompiler_step(c);
		}

		boolean accepted() {
			return methodCompiler_accepted();
		}

		int execaction(int c) {
			return methodCompiler_execaction(c);
		}

		boolean isend() {
			return methodCompiler_isend();
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
			return "methodCompiler";
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
		case 2:
			break;
		case 3:
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
		return parse(ENGINE_methodCompiler);
	}

	static boolean parseAll(java.io.Reader rd) throws java.io.IOException {
		MethodCompiler o = new MethodCompiler();

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
		} else if(parse(ENGINE_methodCompiler)) {
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
private Classfile classfile;
private MethodInfo method;
private FunctionSpace functions;
private LocalVariableSpace local;
private Code code;
private Goto gotoInst;
private int localStart;
private int localRet;
private NameAndType funcnt;
private java.util.List<NameAndType> funcargs;
private java.util.List<NameAndType> localvars;
private FunctionDefinition funcdefobj;
private VariableType decltype;
private SAST _s;
private AST _e;
private StringBuilder strbuilder;
private int methodFlags;

private void putCodes() {
	_s.putCode(functions, local, code, null, -1, null,
			new java.util.ArrayList<Integer>(),
			new java.util.ArrayList<Integer>());
}

private VariableType getTypeFromName(String tname) {
	if(tname.equals("byte")) {
		return Primitive.BYTE;
	} else if(tname.equals("char")) {
		return Primitive.CHAR;
	} else if(tname.equals("short")) {
		return Primitive.SHORT;
	} else if(tname.equals("int")) {
		return Primitive.INT;
	} else if(tname.equals("long")) {
		return Primitive.LONG;
	} else if(tname.equals("float")) {
		return Primitive.FLOAT;
	} else if(tname.equals("double")) {
		return Primitive.DOUBLE;
	} else {
		return new SymbolType(tname);
	}
}

private void decltype(String type, int dim) {
	VariableType t;

	t = getTypeFromName(type);
	for(int i = 0; i < dim; i++) {
		t = new ArrayType(t);
	}
	decltype = t;
}

private void getFuncdef() {
	java.util.List<VariableType> a = new java.util.ArrayList<VariableType>();

	for(NameAndType x : funcargs) {
		a.add(x.getType());
	}
	funcdefobj = new FunctionDefinition(functions.getClassname(),
			funcnt.getName(), funcnt.getType(), a);
}

private void funcbegin() {
	getFuncdef();
	local = new LocalVariableSpace(funcdefobj.getReturnType(),
			(methodFlags & MethodInfo.ACC_STATIC) != 0);
	for(NameAndType t : funcargs) {
		local.putVariable(t.getName(), t.getType());
	}
	local.putVariable(null, QuasiPrimitive.OBJECT);
	for(NameAndType t : localvars) {
		local.putVariable(t.getName(), t.getType());
	}
	method = new MethodInfo(funcnt.getName(), funcdefobj.getDescriptor(functions));
	method.setAccessFlags(methodFlags);
	code = new Code();
	gotoInst = new Goto();
	localStart = code.addCode(gotoInst);
	localRet = code.addCode(Mnemonic.NOP);
}

private void funcend() {
	Goto gb = new Goto();
	int fargs = 0;
	int off;

	off = (methodFlags & MethodInfo.ACC_STATIC) != 0 ? 0 : 1;
	gotoInst.setOffset(code.getCurrentOffset(localStart));
	for(NameAndType x : funcargs) {
		if(x.getType().equals(Primitive.DOUBLE) || x.getType().equals(Primitive.LONG)) {
			fargs += 2;
		} else {
			fargs++;
		}
	}
	for(int i = fargs + 1; i < local.getMax(); i++) {
		if(local.getType(i - fargs) == null) {
			// do nothing
		} else if(local.getType(i - fargs).isConversible(Primitive.INT)) {
			code.addCode(new IConst(0));
			code.addCode(new IStore(i));
		} else if(local.getType(i - fargs).equals(Primitive.LONG)) {
			code.addCode(new LConst(0));
			code.addCode(new LStore(i));
		} else if(local.getType(i - fargs).equals(Primitive.FLOAT)) {
			code.addCode(new FConst(0));
			code.addCode(new FStore(i));
		} else if(local.getType(i - fargs).equals(Primitive.DOUBLE)) {
			code.addCode(new DConst(0));
			code.addCode(new DStore(i));
		} else {
			code.addCode(Mnemonic.ACONST_NULL);
			code.addCode(new AStore(i));
		}
	}
	gb.setOffset(code.getAddress(localRet) - code.getCurrentAddress());
	code.addCode(gb);
	code.setMaxStack(1024);
	code.setMaxLocals(local.getMax() + 2);
	method.addAttribute(code);
	classfile.addMethod(method);
}

private void func() {
	funcbegin();
}

private void endfunc() {
	if(funcdefobj.getReturnType().equals(Primitive.VOID)) {
		code.addCode(Mnemonic.RETURN);
	} else if(funcdefobj.getReturnType().isConversible(Primitive.INT)) {
		code.addCode(new IConst(0));
		code.addCode(Mnemonic.IRETURN);
	} else if(funcdefobj.getReturnType().isConversible(Primitive.LONG)) {
		code.addCode(new LConst(0));
		code.addCode(Mnemonic.LRETURN);
	} else if(funcdefobj.getReturnType().isConversible(Primitive.FLOAT)) {
		code.addCode(new FConst(0));
		code.addCode(Mnemonic.FRETURN);
	} else if(funcdefobj.getReturnType().isConversible(Primitive.DOUBLE)) {
		code.addCode(new DConst(0));
		code.addCode(Mnemonic.DRETURN);
	} else {
		code.addCode(Mnemonic.ACONST_NULL);
		code.addCode(Mnemonic.ARETURN);
	}
	funcend();
}

private void asgn(AST a, AssignAST.Type op) {
	_e = new AssignAST(op, a, _e);
}

private void konst(Number num) {
	if(num instanceof Integer) {
		_e = new IntegerAST(num.intValue());
	} else {
		_e = new DoubleAST(num.doubleValue());
	}
}

private void konstfloat(Number num) {
	_e = new FloatAST(num.floatValue());
}

private void str()
{
	_e = new StringAST(strbuilder.toString());
}

private void char1(int c) {
	_e = new CharAST((char)c);
}

private void charesc1(int c) {
    switch(c)
    {
    case 'b':
        char1('\b');
        break;
    case 'f':
        char1('\f');
        break;
    case 'n':
        char1('\n');
        break;
    case 'r':
        char1('\r');
        break;
    case 't':
        char1('\t');
        break;
    default:
        char1((char)c);
        break;
    }
}

private void beginStr()
{
    strbuilder = new StringBuilder();
}

private void addStr(int c)
{
    strbuilder.append((char)c);
}

private void escStr(int c)
{
    switch(c)
    {
    case 'b':
        strbuilder.append('\b');
        break;
    case 'f':
        strbuilder.append('\f');
        break;
    case 'n':
        strbuilder.append('\n');
        break;
    case 'r':
        strbuilder.append('\r');
        break;
    case 't':
        strbuilder.append('\t');
        break;
    default:
        strbuilder.append((char)c);
        break;
    }
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

private void cast(VariableType type) {
	_e = new CastAST(type, _e);
}

private void newarray(String type, AST ind, int dim) {
	VariableType t;

	t = getTypeFromName(type);
	t = new ArrayType(t);
	for(int i = 0; i < dim; i++) {
		t = new ArrayType(t);
	}
	_e = new NewArrayAST((ArrayType)t, ind);
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

private void dot(AST a) {
	_e = new DotAST(a, _e);
}

private CallAST begincall() {
	return new CallAST(_e);
}

private void addcall(CallAST call) {
	call.addArgument(_e);
}

private void call(CallAST call) {
	_e = call;
}

private NewAST beginnew(String tname) {
	return new NewAST(getTypeFromName(tname));
}

private void addnew(NewAST neu) {
	neu.addArgument(_e);
}

private void neu(NewAST neu) {
	_e = neu;
}

private void var(String name) {
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

private void _return() {
	_s = new ReturnAST(_e);
}

private void simple() {
	_s = new SimpleAST(_e);
}

private void _throw() {
	_s = new ThrowAST(_e);
}

private void addcatch(java.util.List<CatchEntry> lst, String tname,
		String vname, SAST s) {
	lst.add(new CatchEntry(getTypeFromName(tname), vname, s));
}

private SAST _catch(SAST body, java.util.List<CatchEntry> lst, SAST fn) {
	return new TryAST(body, lst, fn);
}

public static void compile(Classfile classfile,
		int methodFlags,
		NameAndType returnType,
		java.util.List<NameAndType> args,
		java.util.List<NameAndType> localvars,
		FunctionSpace functionSpace,
		String code) {
	MethodCompiler mc = new MethodCompiler();

	try {
		mc.classfile = classfile;
		mc.methodFlags = methodFlags;
		mc.funcnt = returnType;
		mc.funcargs = new java.util.ArrayList<NameAndType>(args);
		mc.localvars = new java.util.ArrayList<NameAndType>(localvars);
		mc.functions = functionSpace;
		mc.func();
		mc.parse(new StringReader(code));
		mc.endfunc();
	} catch(java.io.IOException e) {
		throw new RuntimeException(e);
	}
}
}
