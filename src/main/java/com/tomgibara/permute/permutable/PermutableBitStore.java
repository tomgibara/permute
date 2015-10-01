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

import com.tomgibara.bits.BitStore;
import com.tomgibara.permute.Permutable;
import com.tomgibara.permute.Permutation;

public class PermutableBitStore implements Permutable {

	private final BitStore store;
	private final BitStore.Permutes permutes;

	public PermutableBitStore(BitStore store) {
		if (store == null) throw new IllegalArgumentException("null store");
		this.store = store.mutable();
		this.permutes = store.permute();
	}

	public BitStore getBitStore() {
		return store;
	}
	
	@Override
	public int getPermutableSize() {
		return store.size();
	}

	@Override
	public void transpose(int i, int j) {
		permutes.transpose(i, j);
	}

	@Override
	public PermutableBitStore apply(Permutation permutation) {
		return (PermutableBitStore) Permutable.super.apply(permutation);
	}

}