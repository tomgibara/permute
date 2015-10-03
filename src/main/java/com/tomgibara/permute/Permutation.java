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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Transposable;
import com.tomgibara.storage.Store;

/**
 * <p>
 * A permutation rearranges the elements of another object into a predefined
 * order. This is performed using transpositions which swap indexed values.
 * 
 * <p>
 * All permutations have a fixed size that determines the number of indices over
 * which they operate.
 * 
 * <p>
 * Permutations are immutable, final, Serializable and validate their input when
 * deserialized. So they can be reliably used in security sensitive contexts.
 * 
 * @author Tom Gibara
 *
 * @see #getInfo()
 * @see #permute(Transposable)
 */
public final class Permutation implements Comparable<Permutation>, Serializable {

	// statics

	static final long serialVersionUID = -9053863703146584610L;

	private static final int[] NO_CYCLES = {};

	private static void verifyRange(int[] correspondence) {
		for (int i = 0; i < correspondence.length; i++) {
			int c = correspondence[i];
			if (c < 0 || c >= correspondence.length) throw new IllegalArgumentException("invalid correspondence");
		}
	}
	
	private static void verifyUnique(int size, int[] indices) {
		SortedSet<Integer> check = Bits.newBitStore(size).ones().asSet();
		for (int i : indices) {
			boolean result;
			try {
				result = check.add(i);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("cycle contains invalid index: " + i);
			}
			if (!result) throw new IllegalArgumentException("cycle contains duplicate index: " + i);
		}
	}

	private static int[] computeIdentity(int[] correspondence) {
		for (int i = 0; i < correspondence.length; i++) {
			correspondence[i] = i;
		}
		return correspondence;
	}

	private static int[] computeInverse(int[] correspondence) {
		int[] array = new int[correspondence.length];
		for (int i = 0; i < array.length; i++) {
			array[correspondence[i]] = i;
		}
		return array;
	}
	
	private static int[] computeCycles(int[] correspondence) {
		int[] array = correspondence.clone();
		int[] cycles = new int[array.length + 1];
		int index = 0;
		outer: while (true) {
			for (int i = 0; i < array.length; i++) {
				int a = array[i];
				if (a == -1) {
					continue;
				}
				if (a == i) {
					array[i] = -1;
					continue;
				}
				for (int j = i;;) {
					int b = array[j];
					if (b == -1) throw new IllegalArgumentException("invalid correspondence");
					array[j] = -1;
					if (b == i) {
						cycles[index++] = -1 - b;
						break;
					}
					cycles[index++] = b;
					j = b;
				}
				continue outer;
			}
			break;
		}
		return cycles.length > index ? Arrays.copyOf(cycles, index) : cycles;
	}

	private static void computeShuffle(int[] array, Random random) {
		for (int i = array.length - 1; i > 0 ; i--) {
			int j = random.nextInt(i + 1);
			int t = array[i];
			array[i] = array[j];
			array[j] = t;
		}
	}
	private static void checkSize(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
	}

	/**
	 * Creates an identity permutation of a given size. The identity permutation
	 * does perform any reordering of indexed values.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @return an identity permutation
	 */
	public static Permutation identity(int size) {
		checkSize(size);
		int[] correspondence = new int[size];
		computeIdentity(correspondence);
		return new Permutation(correspondence, NO_CYCLES);
	}

	/**
	 * Creates a permutation that reverses the order of indexed values. Applying
	 * the permutation twice will leave all values at their original positions.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @return a reverse permutation
	 */
	public static Permutation reverse(int size) {
		checkSize(size);
		int[] correspondence = new int[size];
		for (int i = 0; i < size; i++) {
			correspondence[i] = size - i - 1;
		}
		int h = size / 2;
		int[] cycles = new int[h * 2];
		for (int i = 0, j = 0; i < h; i++) {
			cycles[j++] = i;
			cycles[j++] = i - size;
		}
		return new Permutation(correspondence, cycles);
	}

	/**
	 * Creates a rotation permutation which changes the index of each value by a
	 * fixed amount (the distance) which is added to the value's original index.
	 * The specified distance may be negative and may exceed the size, though
	 * only <i>size</i> distinct rotations are possible.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @param distance
	 *            the amount by which a value's index is increased.
	 * @return a rotation permutation
	 */
	public static Permutation rotate(int size, int distance) {
		checkSize(size);
		if (size < 2) return identity(size);
		distance = -distance % size;
		if (distance == 0) return identity(size);

		int[] correspondence = new int[size];
		if (distance < 0) distance += size;
		//TODO lazy, remove repeated %
		for (int i = 0; i < size; i++) {
			correspondence[i] = (i + distance) % size;
		}

		int[] cycles = new int[size];
		for (int i = 0, j = i, c = correspondence[j]; c != 0; i++, j = c, c = correspondence[j]) {
			cycles[i] = c;
		}
		cycles[size - 1] = -1 - cycles[size - 1];

		return new Permutation(correspondence, cycles);
	}

	/**
	 * Creates a permutation that swaps two values.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @param i
	 *            an index in the range [0,size)
	 * @param j
	 *            an index in the range [0,size) which is not equal to i
	 * @return a transposition
	 */
	public static Permutation transpose(int size, int i, int j) {
		checkSize(size);
		if (i < 0 || j < 0 || i >= size || j >= size) throw new IllegalArgumentException("invalid indices");
		if (i == j) return identity(size);
		int[] correspondence = new int[size];
		computeIdentity(correspondence);
		correspondence[i] = j;
		correspondence[j] = i;
		int[] cycles = {i, -1 - j };
		return new Permutation(correspondence, cycles);
	}

	/**
	 * <p>
	 * Creates a permutation that shifts values through a specified cycle of
	 * indices such that the value at index <code>cycle[0]</code> will move to
	 * index <code>cycle[1]</code>; in general the value at index
	 * <code>cycle[i]</code> moving to index <code>cycle[i+1]</code> with the
	 * final value at <code>cycle[length-1]</code> moving to index
	 * <code>cycle[0]</code>.
	 * 
	 * <p>
	 * It is permitted for the supplied cycle array may be empty, or of length
	 * 1, in which case the generated permutation will be the identity
	 * permutation of the given size. The indices in the cycle array must be
	 * valid (ie. lie in the range [0..size) ). The cycle array must form a
	 * valid cycle and thus may not contain duplicates.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @param cycle
	 *            the indices through which permuted values should be cycled
	 * @return a cyclic permutation, or possibly the identity permutation
	 */
	public static Permutation cycle(int size, int... cycle) {
		checkSize(size);
		if (cycle == null) throw new IllegalArgumentException("null cycle");
		int length = cycle.length;
		if (length > size) throw new IllegalArgumentException("cycle larger than permutation");
		switch (length) {
		case 0:
			// nothing to do
			return identity(size);
		case 1: {
			// just check argument
			int i = cycle[0];
			if (i < 0) throw new IllegalArgumentException("negative index: " + i);
			if (i >= size) throw new IllegalArgumentException("index too large: " + i);
			return identity(size);
		}
		case 2: {
			int i = cycle[0];
			int j = cycle[1];
			// check for dupes...
			if (i == j) throw new IllegalArgumentException("cycle contains duplicate index: " + i);
			// ... otherwise treat as a transposition
			return transpose(size, i, j);
		}
		default:
			// check for dupes in cycle
			verifyUnique(size, cycle);
			// now start cycling
			int[] correspondence = new int[size];
			computeIdentity(correspondence);
			int target = cycle[0];
			int t = correspondence[target];
			for (int i = 1; i < length; i++) {
				int source = cycle[i];
				correspondence[target] = correspondence[source];
				target = source;
			}
			correspondence[target] = t;
			int[] cycles = cycle.clone();
			cycles[length - 1] = -1 - target;
			return new Permutation(correspondence, cycles);
		}
	}

	/**
	 * Creates a permutation that randomly shuffles values to new indices. The
	 * generated permutation is wholly determined by the state of the random
	 * generator supplied to the method.
	 * 
	 * @param size
	 *            the size of the permutation
	 * @param random
	 *            a source of random numbers
	 * @return a shuffling permutation
	 */
	public static Permutation shuffle(int size, Random random) {
		checkSize(size);
		if (random == null) throw new IllegalArgumentException("null random");
		int[] correspondence = new int[size];
		computeIdentity(correspondence);
		computeShuffle(correspondence, random);
		Arrays.toString(correspondence);
		return new Permutation(correspondence, null);
	}

	/**
	 * Specifies a permutation via a correspondence. This method is capable of
	 * creating any possible permutation. The size of the permutation will equal
	 * the length of the supplied array. The array must contain, in any order,
	 * each integer from zero to <code>length-1</code> exactly once. The
	 * resulting permutation will be such that the value at index <code>i</code>
	 * will have originated from index <code>correspondence[i]</code>
	 * 
	 * @param correspondence
	 *            the correspondence array
	 * @return a permutation with the specified correspondence.
	 * @see #reorder(int...)
	 */
	public static Permutation correspond(int... correspondence) {
		if (correspondence == null) throw new IllegalArgumentException("null correspondence");
		verifyRange(correspondence);
		int[] cycles = computeCycles(correspondence);
		return new Permutation(correspondence.clone(), cycles);
	}

	/**
	 * <p>
	 * Specifies a permutation via a reordering. This method is capable of
	 * creating any possible permutation. The size of the permutation will equal
	 * the length of the supplied array. The array must contain, in any order,
	 * each integer from zero to <code>length-1</code> exactly once. The
	 * resulting permutation will be such that the value at index
	 * <code>correspondence[i]</code> will have originated from index
	 * <code>i</code>.
	 * 
	 * <p>
	 * This method of constructing permutations is the inverse of the
	 * {@link #correspond(int...)} method in the sense that the permutations
	 * created by <code>Permutation.correspond(array)</code> and
	 * <code>Permutation.reorder(array)</code> are inverse.
	 * 
	 * @param ordering
	 *            a reordering of indices
	 * @return a permutation that will reorder the supplied array
	 * @see #correspond(int...)
	 */
	public static Permutation reorder(int... ordering) {
		if (ordering == null) throw new IllegalArgumentException("null ordering");
		int[] correspondence;
		try {
			correspondence = computeInverse(ordering);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid ordering");
		}
		int[] cycles = computeCycles(correspondence);
		return new Permutation(correspondence, cycles);
	}

	// fields

	private final int[] correspondence;
	//cycles is so important, we keep that on permutation
	private int[] cycles = null;
	//everything else is secondary, and we store it separately
	private Info info = null;

	// constructors

	private Permutation(int[] correspondence, int[] cycles) {
		this.correspondence = correspondence;
		this.cycles = cycles;
	}

	Permutation(Generator generator) {
		this.correspondence = generator.correspondence.clone();
	}

	// accessors

	/**
	 * The size of the permutation. This is the number of indices over which the
	 * permutation can operate.
	 * 
	 * @return the size of the permutation.
	 */
	public int getSize() {
		return correspondence.length;
	}

	/**
	 * The correspondence between pre and post permutation indices. This array
	 * completely determines the effect of the permutation which will operate
	 * such that the value at index <code>correspondence[i]</code> will be moved
	 * to <code>i</code>. The length of the array will always match the size of
	 * the permutation.
	 * 
	 * @return the correspondence for the permutation
	 * @see #correspond(int...)
	 * @see #getSize()
	 */
	public int[] getCorrespondence() {
		return correspondence.clone();
	}

	/**
	 * Information about the permutation beyond its size and correspondence.
	 * 
	 * @return information about the permutation
	 */
	public Info getInfo() {
		return info == null ? info = new Info() : info;
	}

	// public methods

	/**
	 * <p>
	 * A convenient method for generating a permutation that is the inverse of
	 * this permutation.
	 * 
	 * <p>
	 * This is logically equivalent to calling
	 * <code>p.generator().invert().permutation()</code>.
	 * 
	 * @return the inverse permutation.
	 */
	public Permutation inverse() {
		//TODO should derive cycles
		return new Permutation(computeInverse(correspondence), null);
	}

	/**
	 * Creates a new permutation generator initialized with this permutation.
	 * 
	 * @return a new permutation generator
	 */
	public Generator generator() {
		return new Generator(correspondence.clone());
	}

	/**
	 * Permutes an object by transposing its elements. Permutations are applied
	 * to objects by repeatedly swapping elements until the value order has been
	 * changed to match the permutation. The <code>Transposable</code> interface
	 * provides the means by which these swaps are executed on the object.
	 * 
	 * @param transposable
	 *            an object whose values may be transposed
	 */
	public void permute(Transposable transposable) {
		if (transposable == null) throw new IllegalArgumentException("null transposable");

		int[] cycles = getCycles();
		for (int i = 0, initial = -1, previous = -1; i < cycles.length; i++) {
			int next = cycles[i];
			if (initial < 0) {
				initial = next;
			} else {
				if (next < 0) {
					next = -1 - next;
					initial = -1;
				}
				transposable.transpose(previous, next);
			}
			previous = next;
		}
	}

	// comparable methods

	/**
	 * Permutations are ordered first by size (smaller sizes precede larger
	 * sizes) and then by their correspondence arrays (in the natural order
	 * induced by their elements).
	 */
	@Override
	public int compareTo(Permutation that) {
		if (this == that) return 0;
		int thisSize = this.correspondence.length;
		int thatSize = that.correspondence.length;
		if (thisSize < thatSize) return -1;
		if (thisSize > thatSize) return 1;
		int[] thisArray = this.correspondence;
		int[] thatArray = that.correspondence;
		for (int i = 0; i < thisSize; i++) {
			int c = thisArray[i] - thatArray[i];
			if (c == 0) continue;
			return c;
		}
		return 0;
	}

	// object methods

	@Override
	public int hashCode() {
		return Arrays.hashCode(correspondence);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Permutation)) return false;
		Permutation that = (Permutation) obj;
		return Arrays.equals(this.correspondence, that.correspondence);
	}

	@Override
	public String toString() {
		return Arrays.toString(correspondence);
	}

	// package scoped methods

	void generator(Generator generator) {
		System.arraycopy(this.correspondence, 0, generator.correspondence, 0, this.correspondence.length);
	}

	// serialization methods

	private Object writeReplace() throws ObjectStreamException {
		return new Serial(correspondence);
	}

	// private utility methods

	private int[] getCycles() {
		if (cycles == null) {
			cycles = computeCycles(correspondence);
		}
		return cycles;
	}

	// innner classes

	private static class Serial implements Serializable {

		private static final long serialVersionUID = -8684974003295220000L;

		int[] correspondence;

		Serial(int[] correspondence) {
			this.correspondence = correspondence;
		}

		private Object readResolve() throws ObjectStreamException {
			return Permutation.correspond(correspondence);
		}

	}

	/**
	 * Provides information about a permutation
	 * 
	 * @author Tom Gibara
	 */
	public final class Info {

		private final static int BIT_GET_REVERSAL = 0;
		private final static int BIT_SET_REVERSAL = 1;
		private final static int BIT_GET_ROTATION = 2;
		private final static int BIT_SET_ROTATION = 3;
		
		private final static int MSK_GET_REVERSAL = 1 << BIT_GET_REVERSAL;
		private final static int MSK_SET_REVERSAL = 1 << BIT_SET_REVERSAL;
		private final static int MSK_GET_ROTATION = 1 << BIT_GET_ROTATION;
		private final static int MSK_SET_ROTATION = 1 << BIT_SET_ROTATION;
		
		// computed eagerly
		private final int numberOfCycles;
		private final int numberOfTranspositions;

		// computed lazily
		private short flags = 0;
		private BitStore fixedPoints;
		private Set<Permutation> disjointCycles;
		private BigInteger lengthOfOrbit;

		Info() {
			// ensure number of cycles has been computed
			// set properties that are cheap, eagerly
			int numberOfCycles = 0;
			int[] cycles = getCycles();
			for (int i = 0; i < cycles.length; i++) {
				if (cycles[i] < 0) numberOfCycles++;
			}
			this.numberOfCycles = numberOfCycles;
			numberOfTranspositions = cycles.length - numberOfCycles;
		}

		/**
		 * The permutation about which information is being provided.
		 * 
		 * @return the permutation
		 */
		public Permutation getPermutation() {
			return Permutation.this;
		}

		/**
		 * The number of disjoint cycles. All permutations can be decomposed
		 * into a set of disjoint cycles.
		 * 
		 * @return the number of cycles
		 * @see #getDisjointCycles()
		 */
		public int getNumberOfCycles() {
			return numberOfCycles;
		}

		/**
		 * The number of transpositions made by the permutation. All
		 * permutations are applied as a sequence of transpositions.
		 * 
		 * @return the number of transpositions
		 */
		public int getNumberOfTranspositions() {
			return numberOfTranspositions;
		}

		/**
		 * Whether the permutation makes no changes to the indexing of values.
		 * 
		 * @return whether the permutation is the identity permutation
		 */
		public boolean isIdentity() {
			return numberOfTranspositions == 0;
		}

		/**
		 * Whether the permutation consists of an odd number of transpositions.
		 * 
		 * @return whether the permutation is odd
		 */
		public boolean isOdd() {
			return (numberOfTranspositions & 1) == 1;
		}

		/**
		 * Whether the permutation consists of a single transposition.
		 * 
		 * @return whether the permutation is a transposition
		 * @see Permutation#transpose(int, int, int)
		 */
		public boolean isTransposition() {
			return numberOfTranspositions == 1;
		}

		/**
		 * Whether the permutation reverses the indexing of values.
		 * 
		 * @return whether the permutation is a reversal
		 * @see Permutation#reverse(int)
		 */
		public boolean isReversal() {
			int m = MSK_SET_REVERSAL | MSK_GET_REVERSAL;
			int bits = flags & m;
			if (bits != 0) return bits == m;
			boolean f = isReversalImpl();
			flags |= f ? m : MSK_SET_REVERSAL;
			return f;
		}
		
		private boolean isReversalImpl() {
			int len = correspondence.length - 1;
			for (int i = 0; i <= len; i++) {
				if (correspondence[i] != len - i) return false;
			}
			return true;
		}

		/**
		 * Whether the permutation is a rotation.
		 * 
		 * @return whether the permutation is a rotation
		 * @see Permutation#rotate(int, int)
		 */
		public boolean isRotation() {
			int m = MSK_SET_ROTATION | MSK_GET_ROTATION;
			int bits = flags & m;
			if (bits != 0) return bits == m;
			boolean f = isRotationImpl();
			flags |= f ? m : MSK_SET_ROTATION;
			return f;
		}

		private boolean isRotationImpl() {
			int length = correspondence.length;
			if (length < 3) return true;
			if (isIdentity()) return true;
			int e = correspondence[0];
			for (int i = 0; i < length; i++) {
				if (correspondence[i] != e) return false;
				e++;
				if (e == length) e -= length;
			}
			return true;
		}

		/**
		 * Optionally, the distance through which the permutation rotates
		 * values. The rotation distance of any identity permutation is zero.
		 * 
		 * @return the distance of rotation, or empty if the permutation is not
		 *         a rotation
		 * @see Permutation#rotate(int, int)
		 */
		public Optional<Integer> rotationDistance() {
			if (isIdentity()) return Optional.of(0);
			if (isRotation()) return Optional.of(correspondence.length - correspondence[0]);
			return Optional.empty();
		}

		/**
		 * <p>
		 * An immutable bit store indicating the positions at which values
		 * remain untouched by the permutation. That is:
		 * <code>p.info().getFixedPoints().getBit(i)<code> is true iff <code>p.correspondence()[i] == i</code>
		 * 
		 * <p>
		 * Note that the supplied bits can be viewed as a set of integer
		 * positions using: <code>p.info().getFixedPoints().asSet()</code>
		 * 
		 * @return bits the positions that are fixed by the permutation.
		 */
		public BitStore getFixedPoints() {
			if (fixedPoints == null) {
				int[] array = correspondence;
				fixedPoints = Bits.newBitStore(array.length);
				for (int i = 0; i < array.length; i++) {
					if (array[i] == i) fixedPoints.setBit(i, true);
				}
				fixedPoints = fixedPoints.immutableCopy();
			}
			return fixedPoints;
		}

		/**
		 * The disjoint cycles of the permutation. Each permutation in the
		 * returned set is cyclic and no two permutations will transpose values
		 * at the same index.
		 * 
		 * @return the disjoint cycles
		 */
		public Set<Permutation> getDisjointCycles() {
			if (disjointCycles == null) {
				switch (numberOfCycles) {
				case 0 :
					disjointCycles = Collections.emptySet();
					break;
				case 1 :
					disjointCycles = Collections.singleton(Permutation.this);
					break;
				default :
					Set<Permutation> set = new HashSet<Permutation>();
					int[] array = null;
					for (int i = 0; i < cycles.length; i++) {
						if (array == null) {
							array = new int[correspondence.length];
							computeIdentity(array);
						}
						int a = cycles[i];
						if (a < 0) {
							a = -1 - a;
							array[a] = correspondence[a];
							set.add(new Permutation(array, null));
							array = null;
						} else {
							array[a] = correspondence[a];
						}
					}
					disjointCycles = Collections.unmodifiableSet(set);
				}
			}
			return disjointCycles;
		}

		/**
		 * The number of times that the permutation would need to be applied
		 * until it yielded the identity permutation. The orbit of any identity
		 * permutation is naturally zero.
		 * 
		 * @return the length of the permutation's orbit
		 */
		public BigInteger getLengthOfOrbit() {
			if (lengthOfOrbit == null) {
				if (numberOfCycles == 0) {
					lengthOfOrbit = BigInteger.ONE;
				} else {
					BigInteger[] lengths = new BigInteger[numberOfCycles];
					int[] cycles = getCycles();
					int count = 0;
					int length = 0;
					for (int i = 0; i < cycles.length; i++) {
						if (cycles[i] < 0) {
							lengths[count++] = BigInteger.valueOf(length + 1);
							length = 0;
						} else {
							length++;
						}
					}
					lengthOfOrbit = PermMath.lcm(lengths);
				}
			}
			return lengthOfOrbit;
		}

		@Override
		public int hashCode() {
			return Permutation.this.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Info)) return false;
			Info that = (Info) obj;
			return this.getPermutation().equals(that.getPermutation());
		}

		@Override
		public String toString() {
			return "Permutation.Info for " + getPermutation().toString();
		}

	}

	// inner classes

	/**
	 * Provides a set of method for generating permutations that will sort a
	 * given array or list.
	 * 
	 * @author Tom Gibara
	 */
	public static class Sorting {

		private static void checkValuesNotNull(Object values) {
			if (values == null) throw new IllegalArgumentException("null values");
		}
		
		private static Permutation sort(int size, Comparator<Integer> c) {
			int[] correspondence = new int[size];
			computeIdentity(correspondence);
			Store<Integer> store = Store.newStore(correspondence);
			store.asList().sort(c);
			return new Permutation(correspondence, null);
		}
		
		/**
		 * Creates a permutation that will sort the supplied list. The
		 * comparator is optional; if it is omitted then the elements of the
		 * list must be mutually comparable.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @param c
		 *            an optional comparator which defines the sort order
		 * @return a permutation which, if applied to the list, would sort it
		 */
		public static <E> Permutation list(List<E> values, Comparator<? super E> c) {
			checkValuesNotNull(values);
			return c == null ?
				sort(values.size(), (i,j) -> ((Comparable) values.get(i)).compareTo(values.get(j))) :
				sort(values.size(), (i,j) -> c.compare(values.get(i), values.get(j)));
		}

		/**
		 * Creates a permutation that will sort the supplied array. The
		 * comparator is optional; if it is omitted then the elements of the
		 * array must be mutually comparable.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @param c
		 *            an optional comparator which defines the sort order
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static <E> Permutation objects(E[] values, Comparator<? super E> c) {
			checkValuesNotNull(values);
			return c == null ?
					sort(values.length, (i,j) -> ((Comparable) values[i]).compareTo(values[j])) :
					sort(values.length, (i,j) -> c.compare(values[i], values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation bytes(byte... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Byte.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation shorts(short... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Short.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation ints(int... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Integer.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation unsignedInts(int... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Integer.compareUnsigned(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation longs(long... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Long.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation unsignedLongs(long... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Long.compareUnsigned(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation booleans(boolean... values) {
			//TODO could optimize this, but is it worth it?
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Boolean.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation chars(char... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Character.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation floats(float... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Float.compare(values[i],values[j]));
		}
		
		/**
		 * Creates a permutation that will sort the supplied array.
		 * 
		 * @param values
		 *            the elements to be sorted
		 * @return a permutation which, if applied to the array, would sort it
		 */
		public static Permutation doubles(double... values) {
			checkValuesNotNull(values);
			return sort(values.length, (i,j) -> Double.compare(values[i],values[j]));
		}
		
	}

	private static abstract class Syncer {

		// called to indicate that state of generator has changed
		abstract void desync();

		// called to invite syncer to update the state of the generator
		abstract void resync();

	}

	/**
	 * <p>
	 * Generators provide an efficient way to combine permutations and other
	 * operations to create new permutations. Most methods on this class return
	 * the generator itself so that multiple calls can be chained.
	 * 
	 * <p>
	 * One way to think of a generator is as a mutable permutation. Another way
	 * is as a permutation which is itself permutable.
	 * 
	 * @author Tom Gibara
	 *
	 */
	public static final class Generator implements Permutable<Generator> {

		final int[] correspondence;
		private OrderedSequence orderedSequence = null;
		private Syncer syncer;

		Generator(int[] correspondence) {
			this.correspondence = correspondence;
		}

		// accessors

		/**
		 * <p>
		 * Obtains an ordered sequence of all permutations. The permutations are
		 * ordered consistently with {@link Permutation#compareTo(Permutation)}.
		 * Thus the first permutation in the sequence is identity and the final
		 * permutation is reverse.
		 * 
		 * <p>
		 * Note that moving through the sequence will change the permutation
		 * stored by the generator. Correspondingly changing the state of the
		 * generator will cause the sequence position to alter.
		 * 
		 * @return an ordered sequence of all permutations.
		 */
		public PermutationSequence getOrderedSequence() {
			return orderedSequence == null ? orderedSequence = new OrderedSequence() : orderedSequence;
		}

		/**
		 * <p>
		 * Obtains a sequence over all involutions with no fixed points. An
		 * involution in this context is a permutation which is self-inverting.
		 *
		 * <p>
		 * Note that moving through the sequence will change the permutation
		 * stored by the generator. Correspondingly changing the state of the
		 * generator will cause the sequence position to alter.
		 * 
		 * @return a sequence over all permutations that are involutions with
		 * no fixed points.
		 */
		public PermutationSequence getFixFreeInvolutionSequence() {
			if (syncer instanceof FixFreeInvolutionSequence) return (PermutationSequence) syncer;
			if ((correspondence.length & 1) != 0) throw new IllegalStateException("odd order");
			return new FixFreeInvolutionSequence();
		}

		// mutators

		/**
		 * Sets the generated permutation to that specified.
		 * 
		 * @param permutation
		 *            any permutation
		 * @return the generator
		 */
		public Generator set(Permutation permutation) {
			if (permutation == null) throw new IllegalArgumentException("null permutation");
			if (permutation.getSize() != correspondence.length) throw new IllegalArgumentException("incorrect size");
			permutation.generator(this);
			desync();
			return this;
		}

		/**
		 * Sets the generated permutation to the identity permutation.
		 * 
		 * @return the generator
		 */
		public Generator setIdentity() {
			computeIdentity(correspondence);
			desync();
			return this;
		}

		/**
		 * Changes generated permutation to the inverse of its current value.
		 * 
		 * @return the generator
		 */
		public Generator invert() {
			System.arraycopy(computeInverse(correspondence), 0, correspondence, 0, correspondence.length);
			desync();
			return this;
		}

		/**
		 * Raises the generated permutation to the specified power. A zero
		 * parameter will always yield the identity permutation. Supplying a
		 * negative power will invert the permutation.
		 * 
		 * @param power
		 *            a power to which the generaed permutation should be raised
		 * @return the generator
		 */
		public Generator power(int power) {
			if (power == 0) return setIdentity();

			if (power < 0) {
				invert();
				power = -power;
			}

			if (power > 1) {
				//TODO could be made more efficient
				Permutation p = permutation();
				setIdentity();
				while (power > 0) {
					if ((power & 1) == 1) this.apply(p);
					p = p.generator().apply(p).permutation();
					power >>= 1;
				}
			}
			desync();
			return this;
		}

		// factory methods

		/**
		 * The generated permutation.
		 * 
		 * @return the generated permutation.
		 */
		public Permutation permutation() {
			resync();
			return new Permutation(this);
		}

		// permutable interface

		@Override
		public Generator apply(Permutation permutation) {
			if (permutation == null) throw new IllegalArgumentException("null permutation");
			if (permutation.getSize() != correspondence.length) throw new IllegalArgumentException("size mismatched");
			permutation.permute((i,j) -> swap(i, j));
			desync();
			return this;
		}

		@Override
		public Generator permuted() {
			return this;
		}

		public int size() {
			return correspondence.length;
		}

		@Override
		public Generator reverse() {
			int h = correspondence.length / 2;
			for (int i = 0, j = correspondence.length - 1; i < h; i++, j--) {
				swap(i, j);
			}
			desync();
			return this;
		}

		@Override
		public Generator transpose(int i, int j) {
			if (i < 0) throw new IllegalArgumentException("negative i");
			if (j < 0) throw new IllegalArgumentException("negative j");
			if (i >= correspondence.length) throw new IllegalArgumentException("i greater than or equal to size");
			if (j >= correspondence.length) throw new IllegalArgumentException("j greater than or equal to size");

			if (i != j) {
				int t = correspondence[i];
				correspondence[i] = correspondence[j];
				correspondence[j] = t;
				desync();
			}
			return this;
		}

		@Override
		public Generator rotate(int distance) {
			int size = correspondence.length;
			if (size == 0) return this;
			distance = distance % size;
			if (distance == 0) return this;
			if (distance < 0) distance += size;

			for (int start = 0, count = 0; count != size; start++) {
				int prev = correspondence[start];
				int i = start;
				do {
					i += distance;
					if (i >= size) i -= size;
					int next = correspondence[i];
					correspondence[i] = prev;
					prev = next;
					count ++;
				} while(i != start);
			}
			desync();
			return this;
		}

		@Override
		public Generator cycle(int... cycle) {
			if (cycle == null) throw new IllegalArgumentException("null cycle");
			int length = cycle.length;
			if (length > correspondence.length) throw new IllegalArgumentException("cycle larger than permutation");
			switch (length) {
			case 0:
				// nothing to do
				return this;
			case 1: {
				// just check argument
				int i = cycle[0];
				if (i < 0) throw new IllegalArgumentException("negative index: " + i);
				if (i >= correspondence.length) throw new IllegalArgumentException("index too large: " + i);
				return this;
			}
			case 2: {
				int i = cycle[0];
				int j = cycle[1];
				// check for dupes...
				if (i == j) throw new IllegalArgumentException("cycle contains duplicate index: " + i);
				// ... otherwise treat as a transposition
				return transpose(i, j);
			}
			default:
				// check for dupes in cycle
				verifyUnique(correspondence.length, cycle);
				// now start cycling
				int target = cycle[0];
				int t = correspondence[target];
				for (int i = 1; i < length; i++) {
					int source = cycle[i];
					correspondence[target] = correspondence[source];
					target = source;
				}
				correspondence[target] = t;
				return this;
			}
		}

		@Override
		public Generator shuffle(Random random) {
			if (random == null) throw new IllegalArgumentException("null random");
			computeShuffle(correspondence, random);
			desync();
			return this;
		}

		// object methods

		// equality predicated on strict object equality

		@Override
		public String toString() {
			return Arrays.toString(correspondence);
		}

		// private utility methods

		private void desync() {
			if (syncer != null) syncer.desync();
		}

		private void resync() {
			if (syncer != null) syncer.resync();
		}

		private void setSyncer(Syncer syncer) {
			if (syncer == this.syncer) return;
			if (this.syncer != null) syncer.desync();
			this.syncer = syncer;
		}

		private void swap(int i, int j) {
			if (i == j) return;
			int t = correspondence[i];
			correspondence[i] = correspondence[j];
			correspondence[j] = t;
		}

		private void nextByNumber(boolean ascending) {
			int len = correspondence.length;

			int j = -1;
			for (int i = len - 2; i >= 0; i--) {
				if (ascending ? correspondence[i] < correspondence[i + 1] : correspondence[i] > correspondence[i + 1]) {
					j = i;
					break;
				}
			}
			if (j == -1) throw new IllegalStateException("no such permutation");
			int c = correspondence[j];

			int k = 0;
			for (int i = len - 1; i > j; i--) {
				if (ascending ? c < correspondence[i] : c > correspondence[i]) {
					k = i;
					break;
				}
			}

			swap(j, k);

			int h = (j + 1 + len) / 2;
			for (int i = j + 1, m = len - 1; i < h; i++, m--) {
				swap(i, m);
			}
		}

		private void makeIdentity() {
			for (int i = 0; i < correspondence.length; i++) {
				correspondence[i] = i;
			}
		}

		private void makeReverse() {
			int max = correspondence.length - 1;
			for (int i = 0; i <= max; i++) {
				correspondence[i] = max - i;
			}
		}

		private void makeSwaps() {
			for (int i = 0; i < correspondence.length; i++) {
				correspondence[i] = i++ + 1;
				correspondence[i] = i - 1;
			}
		}

		private final class OrderedSequence implements PermutationSequence {

			public boolean hasNext() {
				int[] array = correspondence;
				for (int i = 1; i < array.length; i++) {
					if (array[i] > array[i - 1]) return true;
				}
				return false;
			}

			public boolean hasPrevious() {
				int[] array = correspondence;
				for (int i = 1; i < array.length; i++) {
					if (array[i] < array[i - 1]) return true;
				}
				return false;
			}

			@Override
			public PermutationSequence first() {
				makeIdentity();
				return this;
			}

			@Override
			public PermutationSequence last() {
				makeReverse();
				return this;
			}

			@Override
			public PermutationSequence next() {
				nextByNumber(true);
				return this;
			}

			@Override
			public PermutationSequence previous() {
				nextByNumber(false);
				return this;
			}

			@Override
			public Generator getGenerator() {
				return Generator.this;
			}

			@Override
			public String toString() {
				return "OrderedSequence at " + Generator.this.toString();
			}

		}

		private final class FixFreeInvolutionSequence extends Syncer implements PermutationSequence {

			private boolean theyChanged;
			private boolean weChanged;

			private final int[] values = new int[correspondence.length / 2];

			FixFreeInvolutionSequence() {
				setSyncer(this);
				theyChanged = true;
				weChanged = false;
			}

			@Override
			public boolean hasNext() {
				if (theyChanged) index();
				int limit = values.length * 2 - 2;
				for (int i = 0; i < values.length; i++) {
					if (values[i] < limit) return true;
					limit -= 2;
				}
				return false;
			}

			@Override
			public boolean hasPrevious() {
				if (theyChanged) index();
				for (int i = 0; i < values.length; i++) {
					if (values[i] > 0) return true;
				}
				return false;
			}

			@Override
			public PermutationSequence first() {
				makeSwaps();
				Arrays.fill(values, 0);
				sync();
				return this;
			}

			@Override
			public PermutationSequence last() {
				makeReverse();
				int steps = values.length - 1;
				for (int i = 0; i <= steps; i++) {
					values[i] = 2 * (steps - i);
				}
				sync();
				return this;
			}

			@Override
			public PermutationSequence next() {
				if (theyChanged) index();
				boolean overflow = true;
				int limit = 2;
				for (int i = values.length - 2; i >= 0; i--) {
					if (values[i] < limit) {
						values[i]++;
						overflow = false;
						break;
					}
					values[i] = 0;
					limit += 2;
				}
				if (overflow) throw new IllegalStateException("no such permutation");
				weChanged = true;
				return this;
			}

			@Override
			public PermutationSequence previous() {
				if (theyChanged) index();
				boolean overflow = true;
				int limit = 2;
				for (int i = values.length - 2; i >= 0; i--) {
					if (values[i] > 0) {
						values[i]--;
						overflow = false;
						break;
					}
					values[i] = limit;
					limit += 2;
				}
				if (overflow) throw new IllegalStateException("no such permutation");
				weChanged = true;
				return this;
			}

			@Override
			public Generator getGenerator() {
				return Generator.this;
			}

			@Override
			void desync() {
				theyChanged = true;
				weChanged = false;
			}

			@Override
			void resync() {
				if (weChanged) correspond();
			}

			// clears supplied array, populates values
			private void index() {
				int[] correspondence = Generator.this.correspondence.clone();
				for (int i = 0; i < values.length; i++) {
					int j = correspondence[0];
					if (j == 0 || correspondence[j] != 0) throw new IllegalStateException("not fix free involution");
					values[i] = j - 1;
					int length = correspondence.length - 2 * i;
					int m = 0;
					for (int n = 1; n < correspondence.length; n++) {
						if (n != j) {
							int c = correspondence[n];
							correspondence[m++] = c >= j ? c - 2 : c - 1;
						}
					}
					correspondence[length - 1] = -1;
					correspondence[length - 2] = -1;
				}
				if (syncer == this) {
					theyChanged = false;
					weChanged = false;
				}
			}

			private void correspond() {
				Arrays.fill(correspondence, -1);
				int steps = values.length - 1;
				for (int a = 0; a <= steps; a++) {
					int b = values[a];
					int ai = -1;
					int bi = -1;
					for (int i = 0; i < correspondence.length; i++) {
						if (correspondence[i] == -1) {
							if (ai >= 0) { // case where a is already placed
								if (b == 0) { // case where we need to place "b"
									bi = i;
									break;
								} else { // case where we need to look further to place "b"
									b--;
								}
							} else { // case where we need to place "a"
								ai = i;
							}
						} else { // case where correspondence is already set
							/* do nothing */
						}
					}
					correspondence[ai] = bi;
					correspondence[bi] = ai;
				}
				sync();
			}

			private void sync() {
				setSyncer(this);
				weChanged = false;
				theyChanged = false;
			}

			@Override
			public String toString() {
				return "FixFreeInvolutionSequence at " + Generator.this.toString();
			}

		}

	}

}
