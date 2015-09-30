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
package com.tomgibara.permute.permutable;

import java.util.BitSet;

import com.tomgibara.permute.Permutable;

public class PermutableBitSet implements Permutable {

	private final BitSet bitSet;

	public PermutableBitSet(BitSet bitSet) {
		if (bitSet == null) throw new IllegalArgumentException("null bitSet");
		this.bitSet = bitSet;
	}

	public BitSet getBitSet() {
		return bitSet;
	}

	@Override
	public int getPermutableSize() {
		return bitSet.size();
	}

	@Override
	public PermutableBitSet transpose(int i, int j) {
		boolean b = bitSet.get(i);
		bitSet.set(i, bitSet.get(j));
		bitSet.set(j, b);
		return this;
	}

}
