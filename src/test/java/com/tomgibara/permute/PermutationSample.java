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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.tomgibara.permute.permutable.PermutableChars;
import com.tomgibara.permute.permutable.PermutableInts;

public class PermutationSample {

	public static void main(String[] args) throws Exception {

		// SIMPLE PERMUTATIONS

		/**
		 * Let's start off by creating the simplest permutation: the identity
		 * permutation, in this case over 5 elements.
		 */

		Permutation identity = Permutation.identity(5);

		/**
		 * We can apply permutations to Strings with a small "Permutable"
		 * wrapper that allows the String elements to be manipulated by our
		 * Permutations.
		 */

		String r0 = identity.permute(new PermutableString("smite")).toString();
		assertEqual(r0, "smite"); // the identity permutation changes nothing

		/**
		 * As per its definition, the identity permutation does nothing. Note
		 * the fluent interface; the permute method returns the Permutable it
		 * was passed, and the toString() method recovers the String it wrapped.
		 */

		/**
		 * We can create Permutations directly with an array that indicates how
		 * the sequence [0..N-1] is permuted.
		 */

		Permutation p1 = new Permutation(1,2,3,4,0);
		String r1 = p1.permute(new PermutableString("smite")).toString();
		assertEqual(r1, "mites");

		/**
		 * The identity permutation is one of a number of permutations that can
		 * be constructed efficiently, and conveniently, via static methods on
		 * the Permutation class. Rotations are another, so we could have
		 * written:
		 */

		Permutation p2 = Permutation.rotate(5, -1);
		String r2 = p1.permute(new PermutableString("smite")).toString();
		assertEqual(r2, "mites"); // rotated every element one place left
		assertTrue(p1.equals(p2)); // this is the same permutation as p1

		/**
		 * All Permutations in this library operate by performing a sequence of
		 * swaps, or "transpositions". So the simplest/fastest type of
		 * permutation (excluding the identity permutation) is one that swaps
		 * just two elements, this is referred to as a transposition
		 */

		Permutation p3 = Permutation.transpose(5, 0, 4);
		String r3 = p3.permute(new PermutableString("smite")).toString();
		assertEqual(r3, "emits"); // swapped first and last letters

		/**
		 * It's also possible to coveniently construct a Permutation that
		 * reverses a whole sequence of elements.
		 */

		Permutation p4 = Permutation.reverse(5);
		String r4 = p4.permute(p4.permute(new PermutableString("smite"))).toString();
		assertEqual(r4, "smite"); // reversing twice restores the original string

		// PERMUTATION GENERATORS

		/**
		 * In the example above, we applied the same permutation twice. It's
		 * possible to combine multiple permutations into a single permutation
		 * that only has to be applied once using a Generator. Permutations are
		 * immutable, but Generators are not and you can switch from one to the
		 * other fluently as needed. The above could have been implemented
		 * using:
		 */

		Permutation p5 = p4.generator().apply(p4).permutation();
		assertEqual(identity, p5);

		/**
		 * The generator() method creates a new Generator and the permutation()
		 * method finally converts it back into a new Permutation. Here's
		 * another example that composes two permutations:
		 */

		Permutation p6 = p4.generator().apply(Permutation.transpose(5, 0, 2)).permutation();
		String r6 = p6.permute(new PermutableString("smite")).toString();
		assertEqual(r6, "items"); // reverses the string and then swaps the first and third characters

		/**
		 * Generators expose have many methods that allow Permutations to be
		 * constructed in convenient ways. For example, there's a transpose
		 * method that provides a faster and simpler way of doing the same
		 * thing:
		 */

		Permutation p7 = p4.generator().transpose(0,2).permutation();
		assertEqual(p7, p6);

		/**
		 * One of the most important methods on Generator is invert(). This
		 * inverts the generator so that will generate a permutation that
		 * 'undoes' another.
		 */

		Permutation p8 = p7.generator().invert().permutation();
		String r8 = p8.permute(new PermutableString("items")).toString();
		assertEqual(r8, "smite"); // swaps the first and third characters and then reverses the string

		/**
		 * A Permutation can be repeatedly applied to itself a specified number
		 * of times using the Generator's power() method. It's also possible to
		 * invert the permutation by specifying a negative power. Zero always
		 * results in the identity permutation.
		 */

		Permutation p9 = new Permutation(4, 2, 1, 0, 3);
		Permutation p9a = p9.generator().power(-1).permutation();
		Permutation p9b = p9.generator().power(0).permutation();
		Permutation p9c = p9.generator().power(1).permutation();
		assertEqual(p9a, p9.generator().invert().permutation()); // -1 always gives the inverse
		assertEqual(p9b, identity); // 0 always gives the identity
		assertEqual(p9c, p9); // 1 always returns the same permutation
		// the calculation remains efficient even for extremely large powers:
		Permutation p9d = p9.generator().power(100001).permutation();
		String r9 = p9d.permute(new PermutableString("smite")).toString();
		assertEqual(r9, "times");

		/**
		 * The generator can also be used to construct permutations that
		 * randomly shuffle elements.
		 */

		Permutation.identity(5).generator().shuffle(new Random()).permutation();

		// SEQUENCES OF PERMUTATIONS

		/**
		 * Permutations implement Comparable and are naturally ordered
		 * lexographically. It doesn't usually make sense to compare two
		 * permutations of different orders, but if you do, the smaller is
		 * always the lesser.
		 */

		assertLessThan(identity, p2);
		assertLessThan(identity, p3);
		assertLessThan(identity, p4); // the identity permutation is least

		assertLessThan(p2, p3); // (1,2,3,4,0) < (4,1,2,3,0)
		assertLessThan(p3, p9); // (4,1,2,3,0) < (4,2,1,0,3)

		/**
		 * Generators can also be used to create sequences over all Permutations
		 * via the getOrderedSequence() method. This returns a Sequence that can
		 * step systematically through Permutations in their natural order
		 */

		// we're going to build a set of all permutations
		Set<Permutation> set = new HashSet<Permutation>();
		// we add the first permutation: identity
		set.add(identity);
		// we obtain a fresh generator
		Permutation.Generator g = identity.generator();
		// and from that a sequence we can use to iterate
		PermutationSequence s = g.getOrderedSequence();
		// while there are more permutations
		while (s.hasNext()) {
			// move to the next one
			s.next();
			// and add it to our set
			set.add( g.permutation() );
		}
		assertEqual(set.size(), 120); // there are 120 permutations of order 5

		/**
		 * Note that the sequence manipulates the generator it belongs to, so
		 * it's possible interleave sequencing with other operations on the
		 * Generator.
		 */

		// GETTING INFORMATION ABOUT PERMUTATIONS

		/**
		 * Sometimes it's very useful to know more information about a
		 * permutation and this is available through an Info object associated
		 * with each Permutation. Info is created lazily so that the Permutation
		 * objects usually stay extremely compact. Here is some of the
		 * information available:
		 */

		Permutation.Info info = p9.getInfo(); // let's investigate permutation p9.
		assertFalse(info.isIdentity()); // no, it's not an identity permutation
		assertEqual(info.getNumberOfCycles(), 2); // into how many disjoint cycles does the permutation decompose?
		info.getDisjointCycles(); // ...and what are they as a set of separate permutations?
		assertTrue(info.getFixedPoints().zeros().isAll()); // the permutation leaves no element's position unchanged
		assertEqual(info.getNumberOfTranspositions(), 3); // three transpositions are required to effect this permutation...
		assertTrue(info.isOdd()); // ...so the permutation is odd

		// apply the permutation 6 times to leave every element's position unchanged
		assertEqual(info.getLengthOfOrbit().intValue(), 6);
		// to demonstrate this...
		Permutation p10 = p9.generator().power(6).permutation();
		String r10 = p10.permute(new PermutableString("smite")).toString();
		assertEqual(r10, "smite"); // the string we started with
		// ...or more simply...
		assertEqual(p10, identity);
		// ...or simpler still...
		assertTrue(p10.getInfo().isIdentity());

		/**
		 * Note that permutations are always applied with the minimum number of
		 * transpositions possible.
		 */

		// ADDITIONAL OBSERVATIONS

		/**
		 * It's valid, though not commonly useful, to have a zero length
		 * Permutation.
		 */

		Permutation px1 = new Permutation();
		assertTrue( Permutation.identity(0).equals(px1) );

		/**
		 * There are Permutables for lists and all primitive array types.
		 */

		Permutation.identity(4).permute(new PermutableInts(1,2,3,4));
		Permutation.rotate(4, -1).permute(new PermutableChars('s', 'c', 'a', 't'));

		/**
		 * Permutations are immutable, final, Serializable and validate their
		 * input when deserialized. So they can be reliably used in security
		 * sensitive contexts.
		 */

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeObject(p1);
		out.close();
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		assertEqual(in.readObject(), p1); // we recover an equal object


	}

	private static void assertEqual(Object x, Object y) {
		if (!x.equals(y)) throw new IllegalStateException();
	}

	private static void assertTrue(boolean b) {
		if (!b) throw new IllegalStateException();
	}

	private static void assertFalse(boolean b) {
		if (b) throw new IllegalStateException();
	}

	private static void assertLessThan(Permutation p1, Permutation p2) {
		if (p1.compareTo(p2) >= 0) throw new IllegalStateException();
	}

}
