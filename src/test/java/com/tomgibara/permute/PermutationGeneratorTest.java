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

import java.util.Random;

public class PermutationGeneratorTest extends PermutationTestCase {

	public void testInvert() {
		{
			Permutation p = new Permutation(4,3,2,1,0);
			Permutation i = p.generator().invert().permutation();
			assertEquals(p, i);
		}

		{
			Permutation p = new Permutation(1,2,3,4,0);
			Permutation i = p.generator().invert().permutation();
			assertTrue(p.generator().apply(i).permutation().getInfo().isIdentity());
		}

		Random random = new Random(0L);
		for (int n = 0; n < 10000; n++) {
			int size = random.nextInt(20);
			Permutation p = Permutation.identity(size).generator().shuffle(random).permutation();
			Permutation q = p.generator().invert().permutation();
			assertTrue(p.generator().apply(q).permutation().getInfo().isIdentity());
			assertTrue(q.generator().apply(p).permutation().getInfo().isIdentity());
		}
	}

	public void testApply() {
		Permutation p = Permutation.identity(5);
		Permutation p1 = Permutation.identity(5).generator().swap(0, 1).permutation();
		Permutation p2 = Permutation.identity(5).generator().swap(1, 2).permutation();
		assertEquals(new Permutation(1,2,0,3,4), p.generator().apply(p1).apply(p2).permutation());
	}

	public void testIdentity() {
		assertTrue(Permutation.reverse(10).generator().identity().permutation().getInfo().isIdentity());
	}

	public void testRotate() {
		assertTrue(Permutation.rotate(10, 2).generator().rotate(-2).permutation().getInfo().isIdentity());
	}

	public void testPower() {
		for (int i = 0; i < 100; i++) {
			Permutation p = Permutation.rotate(10, 1);
			assertEquals(Permutation.rotate(10, i), p.generator().power(i).permutation());
		}
	}

	public void testCycle() {
		Permutation id = Permutation.identity(5);
		assertEquals(id, id.generator().cycle(4).permutation());
		assertEquals(id, id.generator().cycle().permutation());
		assertEquals(Permutation.transpose(5, 0, 1), id.generator().cycle(0,1).permutation());
		assertEquals(Permutation.transpose(5, 1, 4), id.generator().cycle(4,1).permutation());
		assertEquals(new Permutation(0, 3, 2, 4, 1), id.generator().cycle(1, 3, 4).permutation());
		//test failures
		try {
			id.generator().cycle(0, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			id.generator().cycle(1, 2, 1);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			id.generator().cycle(-1);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			id.generator().cycle(1, 9);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			id.generator().cycle(0, 1, 5);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

}
