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

import static com.tomgibara.bits.Bits.toStore;
import static com.tomgibara.permute.Permutation.reverse;
import static com.tomgibara.permute.Permutation.rotate;
import static com.tomgibara.permute.Permutation.transpose;
import static com.tomgibara.permute.Permute.bitStore;

import java.util.List;

import com.tomgibara.bits.BitStore;

public class PermutableTest extends PermutationTestCase {

	static DumbPermutableBitStore dumb(BitStore store) {
		return new DumbPermutableBitStore(store);
	}

	public void testEquivalence() {
		List<Integer> a = permutable(list(0,1,2,3,4,5)).apply(Permutation.rotate(6, 1)).permuted();
		List<Integer> b = permutable(list(0,1,2,3,4,5)).rotate(1).permuted();
		assertEquals(a, b);
	}
	public void testBitStore() {
		testBitStore(toStore("100"), reverse(3));
		testBitStore(toStore("100"), transpose(3, 1, 2));
		testBitStore(toStore("100"), rotate(3, 1));
		testBitStore(toStore("100"), rotate(3, -1));
	}

	private void testBitStore(BitStore store, Permutation p) {
		assertEquals(dumb(store.mutableCopy()).apply(p).permuted(), bitStore(store.mutableCopy()).apply(p).permuted());
	}

	private static class DumbPermutableBitStore implements Permutable<BitStore> {

		private final BitStore store;

		DumbPermutableBitStore(BitStore store) {
			this.store = store;
		}

		@Override
		public Permutable<BitStore> apply(Permutation permutation) {
			permutation.permute(store.permute());
			return this;
		}

		@Override
		public BitStore permuted() {
			return store;
		}

		@Override
		public int size() {
			return store.size();
		}
	}
}
