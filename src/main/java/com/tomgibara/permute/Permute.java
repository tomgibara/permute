/*
 * Copyright 2015 Tom Gibara
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

import java.util.List;

import com.tomgibara.bits.BitStore;
import com.tomgibara.fundament.Transposable;
import com.tomgibara.permute.permutable.PermutableBitStore;
import com.tomgibara.permute.permutable.PermutableBooleans;
import com.tomgibara.permute.permutable.PermutableBytes;
import com.tomgibara.permute.permutable.PermutableChars;
import com.tomgibara.permute.permutable.PermutableDoubles;
import com.tomgibara.permute.permutable.PermutableFloats;
import com.tomgibara.permute.permutable.PermutableInts;
import com.tomgibara.permute.permutable.PermutableList;
import com.tomgibara.permute.permutable.PermutableLongs;
import com.tomgibara.permute.permutable.PermutableObjects;
import com.tomgibara.permute.permutable.PermutableShorts;
import com.tomgibara.permute.permutable.PermutableString;
import com.tomgibara.permute.permutable.PermutableTransposable;

/**
 * Provides convenient methods for wrapping common types of objects as
 * permutables so that they can be acted upon by permutations.
 *
 * @author Tom Gibara
 *
 */
public class Permute {

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a byte array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <byte[]>        bytes(byte... values)       { return new PermutableBytes(values);    }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a short array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <short[]>       shorts(short... values)     { return new PermutableShorts(values);   }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values an int array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <int[]>         ints(int... values)         { return new PermutableInts(values);     }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a long array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <long[]>        longs(long... values)       { return new PermutableLongs(values);    }


	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a boolean array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <boolean[]>     booleans(boolean... values) { return new PermutableBooleans(values); }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a char array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <char[]>        chars(char... values)       { return new PermutableChars(values);    }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a float array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <float[]>       floats(float... values)     { return new PermutableFloats(values);   }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values a double array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <double[]>      doubles(double... values)   { return new PermutableDoubles(values);  }

	/**
	 * Creates a permutable wrapper around the supplied array.
	 *
	 * @param values an object array
	 *
	 * @return an object through which the array may be permuted
	 */
	public static     Permutable <Object[]>      objects(Object... values)   { return new PermutableObjects(values);  }

	/**
	 * Creates a permutable wrapper around the supplied list.
	 *
	 * @param values a list
	 *
	 * @return an object through which the list may be permuted
	 */
	public static <E> Permutable <List<E>>       list(List<E>  values)       { return new PermutableList<>(values);   }

	/**
	 * Creates a permutable wrapper around a string builder which initialized
	 * with the value of the supplied string. The permuted string may be
	 * recovered by calling <code>permuted().toString()</code> or more simply
	 * <code>toString</code> on the <code>Permutable</code>.
	 *
	 * @param value a string
	 *
	 * @return a permutable for the string.
	 */
	public static     Permutable <StringBuilder> string(String value)        { return new PermutableString(value);    }

	/**
	 * Creates a permutable wrapper around the supplied
	 * <code>StringBuilder<code>.
	 *
	 * @param value
	 *            a <code>StringBuilder<code>
	 *
	 * @return an object through which the <code>StringBuilder<code> may be
	 *         permuted
	 */
	public static     Permutable <StringBuilder> string(StringBuilder value) { return new PermutableString(value);    }

	/**
	 * Creates a permutable wrapper around the supplied <code>BitStore<code>.
	 *
	 * @param value
	 *            a <code>BitStore<code>
	 *
	 * @return an object through which the <code>BitStore<code> may be permuted
	 */
	public static     Permutable <BitStore>      bitStore(BitStore value)    { return new PermutableBitStore(value);  }

	/**
	 * Creates a permutable wrapper around the supplied
	 * <code>Transposable<code>. Since <code>Transposable</code> instances do
	 * not declare a size through that interface, it is necessary to explicitly
	 * supply one to this method.
	 *
	 * @param size
	 *            the putative size
	 * @param value
	 *            a <code>Transposable<code>
	 *
	 * @return an object through which the <code>Transposable<code> may be
	 *         permuted
	 */
	public static Permutable<Transposable> transposable(int size, Transposable value) {
		return new PermutableTransposable(size, value);
	}

	private Permute() { }

}
