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

import static com.tomgibara.permute.Permutation.correspond;
import static com.tomgibara.permute.Permutation.identity;
import static com.tomgibara.permute.Permutation.reorder;
import static com.tomgibara.permute.Permutation.reverse;
import static com.tomgibara.permute.Permutation.rotate;
import static com.tomgibara.permute.Permute.string;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.storage.Store;
import com.tomgibara.storage.Stores;

public class PermutationTest extends PermutationTestCase {

	public void testSwapGeneration() {
		List<Integer> a = list(1,2,3,4,5);
		permutable(a).apply(Permutation.identity(5).generator().transpose(0, 1).permutation());
		assertEquals(list(2,1,3,4,5), a);
	}

	public void testPermute() {
		verifyPermute(list(), list());
		verifyPermute(list(5,4,3,1,2), list(1,2,3,4,5), 4,3,2,0,1);
		verifyPermute(list(3,4,5,2,1), list(1,2,3,4,5), 2,3,4,1,0);

		assertEquals("dog", string("god").apply(reverse(3)).toString());
		verifyPermute("dog", "god", Permutation.reverse(3));
		verifyPermute("time", "time", identity(4));
		verifyPermute("emit", "time", reverse(4));
		verifyPermute("item", "time", rotate(4,-2).generator().reverse().permutation());
		verifyPermute("mite", "time", rotate(4,1).generator().reverse().permutation());

	}

	private void verifyPermute(List<Integer> expected, List<Integer> input, int... corr) {
		assertEquals(expected, permutable(input).apply(correspond(corr)).permuted());
	}

	private void verifyPermute(String expected, String input, Permutation p) {
		assertEquals(expected, string(input).apply(p).toString());
	}

	public void testReverseConstructor() {
		for (int size = 0; size < 100; size++) {
			Permutation r = reverse(size);
			Permutation i = identity(size);
			if (size > 1) assertFalse(r.equals(i));
			assertEquals(i, r.generator().apply(r).permutation());
			if (size > 0) {
				assertEquals(0, r.correspondence()[size - 1]);
				assertEquals(size - 1, r.correspondence()[0]);
			}
		}
	}

	public void testRotateConstructor() {
		assertEquals("DABC", string("ABCD").apply(rotate(4, 1)).toString());
		assertEquals("BCDA", string("ABCD").apply(rotate(4, -1)).toString());
		for (int size = 0; size < 100; size++) {
			for (int dist = - 2 * size; dist < 2 * size; dist++) {
				Permutation r = Permutation.rotate(size, dist);
				if (size > 1) {
					if ((dist % size) == 0) {
						assertEquals(0, r.info().getDisjointCycles().size());
						assertTrue(r.info().getFixedPoints().ones().isAll());
					} else {
						assertEquals(1, r.info().getDisjointCycles().size());
						assertTrue(r.info().getFixedPoints().zeros().isAll());
					}
				} else {
					assertEquals(Permutation.identity(size), r);
				}
			}
		}
	}

	public void testBadConstructor() {
		verifyBadConstructor(0,1,2,3,3);
		verifyBadConstructor(1);
		verifyBadConstructor(-1, 0, -1);
		verifyBadConstructor(1, -1, 0);
		verifyBadConstructor(null);
	}

	private void verifyBadConstructor(int... array) {
		try {
			correspond(array);
			fail("allowed invalid correspondence array " + Arrays.toString(array));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		try {
			reorder(array);
			fail("allowed invalid ordering array " + Arrays.toString(array));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	public void testTransposeConstructor() {
		Random r = new Random(0L);
		for (int n = 0; n < 10000; n++) {
			int size = r.nextInt(10) + 2;
			int i = r.nextInt(size);
			int j = r.nextInt(size);
			Permutation p = Permutation.transpose(size, i, j);
			assertEquals(i == j, p.info().isIdentity());
			assertEquals(p, p.generator().invert().permutation());
			assertEquals(i == j ? 0 : 1, p.info().getNumberOfTranspositions());
			assertEquals(i == j ? 0 : 1, p.info().getNumberOfCycles());
		}
	}

	public void testCorrespondConstructor() {
		permutable(list()).apply(correspond());

		assertEquals(list(2,3,1), permutable(list(1,2,3)).apply(correspond(1,2,0)).permuted());
		assertEquals(list(5,4,3,2,1), permutable(list(1,2,3,4,5)).apply(correspond(4,3,2,1,0)).permuted());

	}

	public void testReorderConstructor() {
		assertEquals(list(2,0,1,3), permutable(list(0,1,2,3)).apply(Permutation.reorder(1,2,0,3)).permuted());
		assertEquals(list(0,1,2,3), permutable(list(1,2,3,0)).apply(Permutation.reorder(1,2,3,0)).permuted());
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			Permutation p = Permutation.identity(r.nextInt(100)).generator().shuffle(r).permutation();
			assertEquals(p.inverse(), Permutation.reorder(p.correspondence()));
		}
	}

	public void testSortConstructor() {
		testSortConstructor(list(4,2,0,3,1), null);

		Random r = new Random();
		for (int i = 0; i < 1000; i++) {
			int size = r.nextInt(100);
			List<Integer> list = new ArrayList<>();
			for (int j = 0; j < size; j++) {
				list.add(r.nextInt(10 + size));
			}
			testSortConstructor(list, null);
			testSortConstructor(list, Comparator.naturalOrder());
			testSortConstructor(list, Comparator.reverseOrder());
		}
	}

	private void testSortConstructor(List<Integer> list, Comparator<Integer> c) {
		Integer[] array = (Integer[]) list.toArray(new Integer[list.size()]);
		int[] ints = new int[array.length];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = array[i];
		}
		list = copy(list);
		List<Integer> copy = copy(list);
		Permutation p = Permutation.Sorting.list(list, c);
		assertEquals(copy, list); // list not changed
		copy.sort(c);
		assertEquals(copy, permutable(list).apply(p).permuted());
		Permutation q = Permutation.Sorting.objects(array, c);
		assertEquals(p, q);
		if (c == null) {
			Permutation r = Permutation.Sorting.ints(ints);
			assertEquals(p, r);
		}
	}

	public void testCycleConstructor() {
		int size = 20;
		int over = size / 10;
		Random r = new Random(0L);
		for (int i = 0; i < 200; i++) {
			int length = r.nextInt(7);
			int[] cycle = new int[length];
			for (int j = 0; j < cycle.length; j++) {
				cycle[j] = r.nextInt(size + 2 * over) - over;
			}

			Permutation p;
			try {
				p = identity(size).generator().cycle(cycle).permutation();
			} catch (IllegalArgumentException e) {
				p = null;
			}

			Permutation q;
			try {
				q = Permutation.cycle(size, cycle);
			} catch (IllegalArgumentException e) {
				q = null;
			}

			assertEquals(p, q);
		}
	}

	public void testShuffleConstructor() {
		Random r = new Random(0L);
		int reps = 10000;
		int size = 20;
		int[] counts = new int[size];
		for (int i = 0; i < reps; i++) {
			BitStore store = Bits.store(size);
			store.rangeTo(size/2).fill();
			Permutation.shuffle(size, r).permute(store.permute());
			store.ones().asSet().forEach(n -> counts[n]++);
		}
		int exp = reps / 2;
		int err = exp / 40;
		for (int count : counts) {
			assertTrue(count + " vs " + exp, count <= exp + err);
			assertTrue(count + " vs " + exp, count >= exp - err);
		}
	}

	public void testComparable() {

		assertTrue(Permutation.identity(6).compareTo(Permutation.identity(5)) > 0);
		assertTrue(correspond().compareTo(Permutation.identity(1)) < 0);
		assertTrue(correspond().compareTo(Permutation.identity(0)) == 0);

		Permutation.Generator pg = Permutation.identity(6).generator();
		PermutationSequence ps = pg.getOrderedSequence();
		Permutation prev = pg.permutation();
		while (ps.hasNext()) {
			Permutation next = ps.next().getGenerator().permutation();
			Permutation next2 = pg.permutation();
			assertEquals(0, next.compareTo(next2));
			assertTrue(next.compareTo(prev) > 0);
			assertTrue(prev.compareTo(next) < 0);
		}

	}

	public void testSerialization() throws Exception {

		Random random = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			Permutation p = Permutation.identity(random.nextInt(100)).generator().shuffle(random).permutation();
			Permutation.Info pi = p.info();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(p);
			out.close();
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			Permutation q = (Permutation) in.readObject();
			assertEquals(p, q);
			assertEquals(pi, q.info());
		}

	}

	public void testInverse() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = r.nextInt(50);
			Permutation identity = identity(size);
			Permutation p = identity.generator().shuffle(r).permutation();
			Permutation inverse1 = p.inverse();
			Permutation inverse2 = p.generator().invert().permutation();
			// ensure inverses match
			assertEquals(inverse1, inverse2);
			// ensure inverses are inverses
			assertEquals(identity, p.generator().apply(inverse1).permutation());
			assertEquals(identity, p.generator().apply(inverse2).permutation());
		}
	}

	public void testUnpermute() {
		int[] array = new int[50];
		for (int i = 0; i < 50; i++) {
			array[i] = i;
		}
		Store<Integer> store = Stores.ints(array);

		{
			Permutation p = Permutation.rotate(10, 1);
			Permutation q = Permutation.rotate(10, -1);
			Store<Integer> ps = store.resizedCopy(10);
			Store<Integer> qs = store.resizedCopy(10);
			p.unpermute(ps);
			q.permute(qs);
			assertEquals(qs, ps);
		}

		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = r.nextInt(50);
			Permutation identity = identity(size);
			Permutation p = identity.generator().shuffle(r).permutation();
			Permutation inv = p.inverse();

			Store<Integer> copy1 = store.resizedCopy(size);
			Store<Integer> copy2 = copy1.mutableCopy();
			assertEquals(copy1, copy2);

			p.unpermute(copy1);
			inv.permute(copy2);
			assertEquals(copy2, copy1);
		}
	}
}
