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

public class Permute {

	public static     Permutable <byte[]>        bytes(byte... values)       { return new PermutableBytes(values);    }
	public static     Permutable <short[]>       shorts(short... values)     { return new PermutableShorts(values);   }
	public static     Permutable <int[]>         ints(int... values)         { return new PermutableInts(values);     }
	public static     Permutable <long[]>        longs(long... values)       { return new PermutableLongs(values);    }

	public static     Permutable <boolean[]>     booleans(boolean... values) { return new PermutableBooleans(values); }
	public static     Permutable <char[]>        chars(char... values)       { return new PermutableChars(values);    }
	public static     Permutable <float[]>       floats(float... values)     { return new PermutableFloats(values);   }
	public static     Permutable <double[]>      doubles(double... values)   { return new PermutableDoubles(values);  }

	public static     Permutable <Object[]>      objects(Object... values)   { return new PermutableObjects(values);  }
	public static <E> Permutable <List<E>>       list(List<E>  values)       { return new PermutableList<>(values);   }

	public static     Permutable <StringBuilder> string(String value)        { return new PermutableString(value);    }
	public static     Permutable <StringBuilder> string(StringBuilder value) { return new PermutableString(value);    }
	public static     Permutable <BitStore>      bitStore(BitStore value)    { return new PermutableBitStore(value);  }

	public static Permutable<Transposable> transposable(int size, Transposable value) {
		return new PermutableTransposable(size, value);
	}

	private Permute() { }

}
