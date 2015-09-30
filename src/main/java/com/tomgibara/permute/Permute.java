package com.tomgibara.permute;

import java.util.List;

import com.tomgibara.bits.BitStore;
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

public class Permute {

	public static     PermutableBytes    bytes(byte... values)       { return new PermutableBytes(values);    }
	public static     PermutableShorts   shorts(short... values)     { return new PermutableShorts(values);   }
	public static     PermutableInts     ints(int... values)         { return new PermutableInts(values);     }
	public static     PermutableLongs    longs(long... values)       { return new PermutableLongs(values);    }

	public static     PermutableBooleans booleans(boolean... values) { return new PermutableBooleans(values); }
	public static     PermutableChars    chars(char... values)       { return new PermutableChars(values);    }
	public static     PermutableFloats   floats(float... values)     { return new PermutableFloats(values);   }
	public static     PermutableDoubles  doubles(double... values)   { return new PermutableDoubles(values);  }

	public static     PermutableObjects  objects(Object... values)   { return new PermutableObjects(values);  }
	public static <E> PermutableList<E>  list(List<E>  values)       { return new PermutableList<>(values);   }

	public static     PermutableString   string(String value)        { return new PermutableString(value);    }
	public static     PermutableString   string(StringBuilder value) { return new PermutableString(value);    }
	public static     PermutableBitStore bitStore(BitStore value)    { return new PermutableBitStore(value);  }

	private Permute() { }

}
