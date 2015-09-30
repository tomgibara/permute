/*
 * Copyright 2011 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.permute;

import java.math.BigInteger;

//TODO copied from crinch - general methods that need a better home
class PermMath {

	public static int gcd(int a, int b) {
		while (a != b) {
			if (a > b) {
				int na = a % b;
				if (na == 0) return b;
				a = na;
			} else {
				int nb = b % a;
				if (nb == 0) return a;
				b = nb;
			}
		}
		return a;
	}

	public static int lcm(int a, int b) {
		return a * b / gcd(a, b);
	}

	public static boolean isCoprime(int a, int b) {
		return gcd(a, b) == 1;
	}

	public static BigInteger gcd(BigInteger a, BigInteger b) {
		while (!a.equals(b)) {
			if (a.compareTo(b) > 0) {
				BigInteger na = a.mod(b);
				if (na.signum() == 0) return b;
				a = na;
			} else {
				BigInteger nb = b.mod(a);
				if (nb.signum() == 0) return a;
				b = nb;
			}
		}
		return a;
	}

	public static BigInteger gcd(BigInteger... as) {
		if (as == null) throw new IllegalArgumentException("null as");
		switch (as.length) {
		case 0 : throw new IllegalArgumentException("empty as");
		case 1 : return as[0];
		case 2 : return gcd(as[0], as[1]);
		default:
			BigInteger d = as[0];
			for (int i = 1; i < as.length; i++) {
				d = gcd(d, as[i]);
			}
			return d;
		}
	}

	public static BigInteger lcm(BigInteger a, BigInteger b) {
		return a.multiply(b).divide(gcd(a, b));
	}

	public static BigInteger lcm(BigInteger... as) {
		if (as == null) throw new IllegalArgumentException("null as");
		switch (as.length) {
		case 0 : throw new IllegalArgumentException("empty as");
		case 1 : return as[0];
		case 2 : return lcm(as[0], as[1]);
		default:
			BigInteger m = as[0];
			for (int i = 1; i < as.length; i++) {
				m = lcm(m, as[i]);
			}
			return m;
		}
	}

	public static float pow(float f, int n) {
		if (n < 0) {
			f = 1/f;
			n = -n;
		}
		float p = 1f;
		while (n != 0) {
			if ((n & 1) == 1) p *= f;
			f *= f;
			n >>= 1;
		}
		return p;
	}

}
