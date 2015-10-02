package com.tomgibara.permute;

import static com.tomgibara.bits.Bits.newBitStore;
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
		testBitStore(newBitStore("100"), reverse(3));
		testBitStore(newBitStore("100"), transpose(3, 1, 2));
		testBitStore(newBitStore("100"), rotate(3, 1));
		testBitStore(newBitStore("100"), rotate(3, -1));
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
