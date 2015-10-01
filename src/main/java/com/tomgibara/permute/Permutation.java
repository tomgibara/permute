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
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Transposable;

public final class Permutation implements Comparable<Permutation>, Serializable {

	// statics

	static final long serialVersionUID = -9053863703146584610L;

	private static final int[] NO_CYCLES = {};
	
	private static int[] inverted(int[] correspondence) {
		int[] array = new int[correspondence.length];
		for (int i = 0; i < array.length; i++) {
			array[correspondence[i]] = i;
		}
		return array;
	}

	public static Permutation identity(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		int[] correspondence = new int[size];
		for (int i = 0; i < size; i++) {
			correspondence[i] = i;
		}
		return new Permutation(correspondence, NO_CYCLES);
	}

	public static Permutation reverse(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
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

	public static Permutation rotate(int size, int distance) {
		if (size < 0) throw new IllegalArgumentException("negative size");
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

	public static Permutation transpose(int size, int i, int j) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (i < 0 || j < 0 || i >= size || j >= size) throw new IllegalArgumentException("invalid indices");
		if (i == j) return identity(size);
		int[] correspondence = new int[size];
		for (int k = 0; k < size; k++) {
			correspondence[k] = k;
		}
		correspondence[i] = j;
		correspondence[j] = i;
		int[] cycles = {i, -1 - j };
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

	public Permutation(int... correspondence) {
		if (correspondence == null) throw new IllegalArgumentException("null correspondence");
		this.correspondence = correspondence;
		cycles = computeCycles(true);
	}

	// accessors

	public int getSize() {
		return correspondence.length;
	}

	public int[] getCorrespondence() {
		return correspondence.clone();
	}

	public Info getInfo() {
		return info == null ? info = new Info() : info;
	}

	// public methods

	public Permutation inverse() {
		//TODO should derive cycles
		return new Permutation(inverted(correspondence), null);
	}

	public Generator generator() {
		return new Generator(correspondence.clone());
	}

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
			cycles = computeCycles(false);
		}
		return cycles;
	}

	private int[] computeCycles(boolean verify) {
		if (verify) {
			for (int i = 0; i < correspondence.length; i++) {
				int c = correspondence[i];
				if (c < 0 || c >= correspondence.length) throw new IllegalArgumentException("invalid correspondence");
			}
		}
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
				int[] correspondence = new int[array.length];
				for (int k = 0; k < array.length; k++) {
					correspondence[k] = k;
				}
				for (int j = i;;) {
					int b = array[j];
					if (verify && b == -1) throw new IllegalArgumentException("invalid correspondence");
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

	// innner classes

	private static class Serial implements Serializable {

		private static final long serialVersionUID = -8684974003295220000L;

		int[] correspondence;

		Serial(int[] correspondence) {
			this.correspondence = correspondence;
		}

		private Object readResolve() throws ObjectStreamException {
			return new Permutation(correspondence);
		}

	}

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

		public Info() {
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

		public Permutation getPermutation() {
			return Permutation.this;
		}

		public int getNumberOfCycles() {
			return numberOfCycles;
		}

		public int getNumberOfTranspositions() {
			return numberOfTranspositions;
		}

		public boolean isIdentity() {
			return numberOfTranspositions == 0;
		}

		public boolean isOdd() {
			return (numberOfTranspositions & 1) == 1;
		}

		public boolean isTransposition() {
			return numberOfTranspositions == 1;
		}

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

		public Optional<Integer> rotationDistance() {
			if (isIdentity()) return Optional.of(0);
			if (isRotation()) return Optional.of(correspondence.length - correspondence[0]);
			return Optional.empty();
		}

		public BitStore getFixedPoints() {
			if (fixedPoints == null) {
				int[] array = correspondence;
				fixedPoints = Bits.newBitStore(array.length);
				for (int i = 0; i < array.length; i++) {
					if (array[i] == i) fixedPoints.setBit(i, true);
				}
			}
			return fixedPoints;
		}

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
							for (int j = 0; j < array.length; j++) {
								array[j] = j;
							}
						}
						int a = cycles[i];
						if (a < 0) {
							a = -1 - a;
							array[a] = correspondence[a];
							set.add(new Permutation(array));
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

	private static abstract class Syncer {

		// called to indicate that state of generator has changed
		abstract void desync();

		// called to invite syncer to update the state of the generator
		abstract void resync();

	}

	public static final class Generator implements Permutable<Generator> {

		final int[] correspondence;
		private OrderedSequence orderedSequence = null;
		private Syncer syncer;

		Generator(int[] correspondence) {
			this.correspondence = correspondence;
		}

		// accessors

		public int getSize() {
			return correspondence.length;
		}

		public PermutationSequence getOrderedSequence() {
			return orderedSequence == null ? orderedSequence = new OrderedSequence() : orderedSequence;
		}

		public PermutationSequence getFixFreeInvolutionSequence() {
			if (syncer instanceof FixFreeInvolutionSequence) return (PermutationSequence) syncer;
			if ((correspondence.length & 1) != 0) throw new IllegalStateException("odd order");
			return new FixFreeInvolutionSequence();
		}

		// mutators

		public Generator set(Permutation permutation) {
			if (permutation == null) throw new IllegalArgumentException("null permutation");
			if (permutation.getSize() != correspondence.length) throw new IllegalArgumentException("incorrect size");
			permutation.generator(this);
			desync();
			return this;
		}

		public Generator identity() {
			for (int i = 0; i < correspondence.length; i++) {
				correspondence[i] = i;
			}
			desync();
			return this;
		}

		public Generator transpose(int i, int j) {
			if (i < 0) throw new IllegalArgumentException("negative i");
			if (j < 0) throw new IllegalArgumentException("negative j");
			if (i > correspondence.length) throw new IllegalArgumentException("i greater than or equal to size");
			if (j > correspondence.length) throw new IllegalArgumentException("j greater than or equal to size");

			if (i != j) {
				int t = correspondence[i];
				correspondence[i] = correspondence[j];
				correspondence[j] = t;
				desync();
			}
			return this;
		}

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

		public Generator invert() {
			System.arraycopy(inverted(correspondence), 0, correspondence, 0, correspondence.length);
			desync();
			return this;
		}

		public Generator reverse() {
			int h = correspondence.length / 2;
			for (int i = 0, j = correspondence.length - 1; i < h; i++, j--) {
				swap(i, j);
			}
			desync();
			return this;
		}

		public Generator shuffle(Random random) {
			if (random == null) throw new IllegalArgumentException("null random");
			for (int i = correspondence.length - 1; i > 0 ; i--) {
				swap(i, random.nextInt(i + 1));
			}
			desync();
			return this;
		}

		public Generator apply(Permutation permutation) {
			if (permutation == null) throw new IllegalArgumentException("null permutation");
			if (permutation.getSize() != correspondence.length) throw new IllegalArgumentException("size mismatched");
			permutation.permute((i,j) -> swap(i, j));
			desync();
			return this;
		}

		public Generator power(int power) {
			if (power == 0) return identity();

			if (power < 0) {
				invert();
				power = -power;
			}

			if (power > 1) {
				//TODO could be made more efficient
				Permutation p = permutation();
				identity();
				while (power > 0) {
					if ((power & 1) == 1) this.apply(p);
					p = p.generator().apply(p).permutation();
					power >>= 1;
				}
			}
			desync();
			return this;
		}

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
				SortedSet<Integer> check = Bits.newBitStore(correspondence.length).ones().asSet();
				for (int i : cycle) {
					boolean result;
					try {
						result = check.add(i);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("cycle contains invalid index: " + i);
					}
					if (!result) throw new IllegalArgumentException("cycle contains duplicate index: " + i);
				}
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

		// factory methods

		public Permutation permutation() {
			resync();
			return new Permutation(this);
		}

		// permutable interface

		@Override
		public Generator permuted() {
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
