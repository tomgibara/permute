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

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.tomgibara.permute.Permutation.Info;

public class PermutationInfoTest extends PermutationTestCase {

	public void testOdd() {
		assertFalse( Permutation.identity(5).info().isOdd() );
		assertTrue( Permutation.identity(5).generator().transpose(0, 1).permutation().info().isOdd() );
		assertFalse( Permutation.identity(5).generator().transpose(0, 1).transpose(1, 2).permutation().info().isOdd() );
	}

	public void testIdentity() {
		List<Integer> a = list(1,2,3,4,5);
		List<Integer> b = copy(a);
		Permutation p = Permutation.identity(5);
		assertTrue(p.info().isIdentity());
		permutable(b).apply(p);
		assertEquals(b, a);
	}
	
	public void testTransposition() {
		assertFalse(Permutation.identity(0).info().isTransposition());
		assertFalse(Permutation.identity(1).info().isTransposition());
		assertTrue(Permutation.rotate(2, 1).info().isTransposition());
		assertTrue(Permutation.transpose(5, 0, 1).info().isTransposition());
		assertFalse(Permutation.rotate(5, 1).info().isTransposition());
	}

	public void testReversal() {
		for (int i = 0; i < 10; i++) {
			Info info = Permutation.reverse(i).info();
			// test twice to check any cached values are consistent
			assertTrue(info.isReversal());
			assertTrue(info.isReversal());
		}
		Random r = new Random(0L);
		for (int i = 0; i < 100; i++) {
			int size = r.nextInt(30);
			Permutation p = Permutation.identity(size).generator().shuffle(r).permutation();
			assertTrue( p.equals(Permutation.reverse(size)) || !p.info().isReversal() );
		}
	}
	
	public void testRotation() {
		assertTrue(Permutation.identity(0).info().isRotation());
		assertEquals(0, Permutation.identity(0).info().rotationDistance().get().intValue());
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int size = 1 + r.nextInt(50);
			int distance = r.nextInt(size);
			Permutation p = Permutation.rotate(size, distance);
			assertTrue(p.info().isRotation());
			assertEquals(distance, p.info().rotationDistance().get().intValue());
		}
	}

	public void testCyclic() {
		assertEquals(1, correspond(1,2,3,4,0).info().getNumberOfCycles());
		assertEquals(0, correspond(0,1,2,3,4).info().getNumberOfCycles());
		assertEquals(1, correspond(1,0,2,3,4).info().getNumberOfCycles());
		assertEquals(2, correspond(1,0,2,4,3).info().getNumberOfCycles());
		assertEquals(0, correspond(0).info().getNumberOfCycles());
		assertEquals(0, correspond().info().getNumberOfCycles());
	}

	public void testDisjointCycles() {
		{
			Permutation p = Permutation.identity(5);
			assertTrue(p.info().getDisjointCycles().isEmpty());
		}
		{
			Permutation p = correspond(1,2,3,4,0);
			assertEquals(set(p), p.info().getDisjointCycles());
		}
		{
			Permutation p = correspond(1,0,2,4,3);
			assertEquals(set(correspond(1,0,2,3,4), correspond(0,1,2,4,3)), p.info().getDisjointCycles());
		}
		{
			Permutation p = correspond(1,2,0,4,3);
			assertEquals(set(correspond(1,2,0,3,4), correspond(0,1,2,4,3)), p.info().getDisjointCycles());
		}

		Random random = new Random(0);
		for (int i = 0; i < 1000; i++) {
			int size = 1 + random.nextInt(20);
			Permutation permutation = Permutation.identity(size).generator().shuffle(random).permutation();
			Permutation.Info info = permutation.info();
			Set<Permutation> cycles = info.getDisjointCycles();
			assertEquals(info.getNumberOfCycles(), cycles.size());
			Permutation.Generator generator = Permutation.identity(size).generator();
			for (Permutation p : cycles) {
				generator.apply(p);
			}
			assertEquals(permutation, generator.permutation());
		}
	}

	public void testLengthOfOrbit() {
		Random random = new Random(0);
		for (int n = 0; n < 1000; n++) {
			int size = random.nextInt(11);
			Permutation identity = Permutation.identity(size);
			Permutation permutation = identity.generator().shuffle(random).permutation();
			Permutation p = permutation;
			int orbit = permutation.info().getLengthOfOrbit().intValue();
			for(int i = orbit - 1; i > 0; i--) {
				p = p.generator().apply(permutation).permutation();
				assertFalse(p.equals(permutation));
			}
			assertEquals(identity, p);
		}
	}

}
