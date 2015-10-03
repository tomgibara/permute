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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.tomgibara.permute.Permutation.Generator;
import com.tomgibara.permute.Permutation.Info;

public class PermutationSequenceTest extends PermutationTestCase {

	private static final BigInteger TWO = BigInteger.valueOf(2);

	private static boolean isFFI(Permutation p) {
		// check p is empty, or...
		if (p.size() == 0) return true;
		Info info = p.info();
		// ... it is an involution...
		// ...and has no fixed points
		return TWO.equals(info.getLengthOfOrbit()) && info.getFixedPoints().zeros().isAll();
	}

	public void testFirstAndLastOrderedSequence() {
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			int size = r.nextInt(100);
			Generator generator = Permutation.identity(size).generator();
			PermutationSequence sequence = generator.getOrderedSequence();
			assertFalse(sequence.first().hasPrevious());
			assertEquals(Permutation.identity(size), generator.permutation());
			assertFalse(sequence.last().hasNext());
			assertEquals(Permutation.reverse(size), generator.permutation());
		}
	}

	public void testFirstAndLastFFISequence() {
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			int size = 2 * r.nextInt(50);
			Generator generator = Permutation.identity(size).generator();
			PermutationSequence sequence = generator.getFixFreeInvolutionSequence();
			assertFalse(sequence.first().hasPrevious());
			assertTrue(isFFI(generator.permutation()));
			assertFalse(sequence.last().hasNext());
			assertTrue(isFFI(generator.permutation()));
		}
	}

	public void testNextAndPreviousByOrder() {
		int count = 1;
		for (int size = 0; size < 5; size++) {
			if (size > 0) count *= size;
			Permutation.Generator pg = Permutation.identity(size).generator();
			PermutationSequence ps = pg.getOrderedSequence();
			Permutation p;

			Set<Permutation> set1 = new HashSet<Permutation>();
			List<Permutation> list1 = new ArrayList<Permutation>();
			p = pg.permutation();
			set1.add(p);
			list1.add(p);
			while (ps.hasNext()) {
				p = ps.next().getGenerator().permutation();
				set1.add(p);
				list1.add(p);
			}
			assertEquals(count, set1.size());
			assertEquals(count, list1.size());

			Set<Permutation> set2 = new HashSet<Permutation>();
			List<Permutation> list2 = new ArrayList<Permutation>();
			p = pg.permutation();
			set2.add(p);
			list2.add(p);
			while (ps.hasPrevious()) {
				p = ps.previous().getGenerator().permutation();
				set2.add(p);
				list2.add(p);
			}
			assertEquals(count, set2.size());
			assertEquals(count, list2.size());

			assertEquals(set1, set2);
			Collections.reverse(list2);
			assertEquals(list1, list2);
		}
	}

	public void testFFISequence() {
		int expectedSize = 1;
		for (int i = 0; i < 6; i++) {
			int size = 2 * i;
			Permutation.Generator pg = Permutation.identity(size).generator();
			PermutationSequence ps = pg.getFixFreeInvolutionSequence().first();
			Permutation p;

			Set<Permutation> set = new HashSet<Permutation>();
			List<Permutation> list = new ArrayList<Permutation>();
			p = pg.permutation();
			set.add(p);
			list.add(p);
			while (ps.hasNext()) {
				p = ps.next().getGenerator().permutation();
				assertTrue(isFFI(p));
				set.add(p);
				list.add(p);
			}
			if (size > 0) expectedSize *= size - 1;
			assertEquals(expectedSize, list.size());
			assertEquals(expectedSize, set.size());
		}
	}

	public void testSequenceInterleaving() {
		Generator g = Permutation.identity(6).generator();
		List<Permutation> ffis = new ArrayList<Permutation>();
		for (PermutationSequence s = g.getFixFreeInvolutionSequence().first(); s.hasNext();) {
			ffis.add(s.next().getGenerator().permutation());
		}

		Random r = new Random(0L);
		int size = ffis.size();
		int tests = 20;
		PermutationSequence s = g.getFixFreeInvolutionSequence();
		for (int test = 0; test < tests;) {
			int start = r.nextInt(size);
			int finish = r.nextInt(size);
			if (start == finish) continue;
			Permutation p = ffis.get(start);
			Permutation q = ffis.get(finish);
			g.set(p);
			while (start < finish) {
				s.next();
				start++;
			}
			while (start > finish) {
				s.previous();
				start--;
			}
			assertEquals(q, g.permutation());
			test++;
		}
	}

}
