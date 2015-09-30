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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.tomgibara.permute.permutable.PermutableString;

public class PermutationTest extends PermutationTestCase {

	public void testSwapGeneration() {
		List<Integer> a = list(1,2,3,4,5);
		Permutation.identity(5).generator().transpose(0, 1).permutation().permute(permutable(a));
		assertEquals(list(2,1,3,4,5), a);
	}

	public void testPermute() {
		verifyPermute(list(), list());
		verifyPermute(list(5,4,3,1,2), list(1,2,3,4,5), 4,3,2,0,1);
		verifyPermute(list(3,4,5,2,1), list(1,2,3,4,5), 2,3,4,1,0);

		assertEquals("dog", Permutation.reverse(3).permute(new PermutableString("god")).toString());
		verifyPermute("dog", "god", Permutation.reverse(3));
		verifyPermute("time", "time", Permutation.identity(4));
		verifyPermute("emit", "time", Permutation.reverse(4));
		verifyPermute("item", "time", Permutation.rotate(4,-2).generator().reverse().permutation());
		verifyPermute("mite", "time", Permutation.rotate(4,1).generator().reverse().permutation());

	}

	private void verifyPermute(List<Integer> expected, List<Integer> input, int... corr) {
		assertEquals(expected, new Permutation(corr).permute(permutable(input)).getList());
	}

	private void verifyPermute(String expected, String input, Permutation p) {
		assertEquals(expected, p.permute(new PermutableString(input)).toString());
	}

	public void testReverseConstructor() {
		for (int size = 0; size < 100; size++) {
			Permutation r = Permutation.reverse(size);
			Permutation i = Permutation.identity(size);
			if (size > 1) assertFalse(r.equals(i));
			assertEquals(i, r.generator().apply(r).permutation());
			if (size > 0) {
				assertEquals(0, r.getCorrespondence()[size - 1]);
				assertEquals(size - 1, r.getCorrespondence()[0]);
			}
		}
	}

	public void testRotateConstructor() {
		assertEquals("DABC", Permutation.rotate(4, 1).permute(new PermutableString("ABCD")).toString());
		assertEquals("BCDA", Permutation.rotate(4, -1).permute(new PermutableString("ABCD")).toString());
		for (int size = 0; size < 100; size++) {
			for (int dist = - 2 * size; dist < 2 * size; dist++) {
				Permutation r = Permutation.rotate(size, dist);
				if (size > 1) {
					if ((dist % size) == 0) {
						assertEquals(0, r.getInfo().getDisjointCycles().size());
						assertTrue(r.getInfo().getFixedPoints().ones().isAll());
					} else {
						assertEquals(1, r.getInfo().getDisjointCycles().size());
						assertTrue(r.getInfo().getFixedPoints().zeros().isAll());
					}
				} else {
					assertEquals(Permutation.identity(size), r);
				}
			}
		}
	}

	public void testTransposeConstructor() {
		Random r = new Random(0L);
		for (int n = 0; n < 10000; n++) {
			int size = r.nextInt(10) + 2;
			int i = r.nextInt(size);
			int j = r.nextInt(size);
			Permutation p = Permutation.transpose(size, i, j);
			assertEquals(i == j, p.getInfo().isIdentity());
			assertEquals(p, p.generator().invert().permutation());
			assertEquals(i == j ? 0 : 1, p.getInfo().getNumberOfTranspositions());
			assertEquals(i == j ? 0 : 1, p.getInfo().getNumberOfCycles());
		}
	}

	public void testCorrespondenceConstructor() {
		verifyBadConstructor(0,1,2,3,3);
		verifyBadConstructor(1);
		verifyBadConstructor(-1, 0, -1);
		verifyBadConstructor(1, -1, 0);
		verifyBadConstructor(null);

		new Permutation().permute(permutable(list()));

		assertEquals(list(5,4,3,2,1), new Permutation(4,3,2,1,0).permute(permutable(list(1,2,3,4,5))).getList());

	}

	private void verifyBadConstructor(int... correspondence) {
		try {
			new Permutation(correspondence);
			fail("allowed invalid construction array " + Arrays.toString(correspondence));
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	public void testComparable() {

		assertTrue(Permutation.identity(6).compareTo(Permutation.identity(5)) > 0);
		assertTrue(new Permutation().compareTo(Permutation.identity(1)) < 0);
		assertTrue(new Permutation().compareTo(Permutation.identity(0)) == 0);

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
			Permutation.Info pi = p.getInfo();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(p);
			out.close();
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			Permutation q = (Permutation) in.readObject();
			assertEquals(p, q);
			assertEquals(pi, q.getInfo());
		}


	}

}
