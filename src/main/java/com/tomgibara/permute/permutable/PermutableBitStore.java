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

import java.util.Optional;

import com.tomgibara.bits.BitStore;
import com.tomgibara.permute.Permutable;
import com.tomgibara.permute.Permutation;
import com.tomgibara.permute.Permutation.Info;

public class PermutableBitStore implements Permutable<BitStore> {

	private final BitStore store;
	private final BitStore.Permutes permutes;

	public PermutableBitStore(BitStore store) {
		if (store == null) throw new IllegalArgumentException("null store");
		this.store = store.mutable();
		this.permutes = store.permute();
	}

	@Override
	public BitStore permuted() {
		return store;
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public PermutableBitStore apply(Permutation permutation) {
		PermutableUtil.check(permutation, store.size());
		Info info = permutation.info();
		if (info.isIdentity()) {
			/* do nothing */
		} else if (info.isReversal()) {
			permutes.reverse();
		} else {
			Optional<Integer> distance = info.rotationDistance();
			if (distance.isPresent()) {
				permutes.rotate(distance.get());
			} else {
				// fallback - just do it with transpositions
				permutation.permute(permutes);
			}
		}
		return this;
	}

	@Override
	public PermutableBitStore transpose(int i, int j) {
		permutes.transpose(i, j);
		return this;
	}

	@Override
	public PermutableBitStore reverse() {
		permutes.reverse();
		return this;
	}

	@Override
	public PermutableBitStore rotate(int distance) {
		permutes.rotate(distance);
		return this;
	}

	// object methods

	@Override
	public int hashCode() {
		return store.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableBitStore)) return false;
		PermutableBitStore that = (PermutableBitStore) obj;
		return this.store.equals(that.store);
	}

	@Override
	public String toString() {
		return store.toString();
	}
}
