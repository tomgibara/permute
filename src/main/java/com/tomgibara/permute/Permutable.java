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

/**
 * <p>
 * Applies permutations to an object. Permutable instances are most often
 * created as temporary wrappers around other objects. Some implementations of
 * this interface may be able to provide accelerated implementations of common
 * permutations.
 * 
 * <p>
 * Permutations are typically applied via the {@link #apply(Permutation)} method
 * though convenience methods with default implementations are provided to
 * enable some common permutations to be applied more succinctly, and possibly
 * with improved performance.
 * 
 * <p>
 * Many of the interface methods on this object return {@link Permutable}. It is
 * expected that in almost all cases, the object returned will be the object
 * called. However, in the case where the interface is implemented by an
 * immutable class, it is permitted (and may be necessary) to return a new
 * implementation of the interface encapsulating the permuted state.
 * 
 * @author Tom Gibara
 *
 * @param <T>
 *            the type of object being permuted
 * @see Permute
 */

public interface Permutable<T> {

	/**
	 * Uses a permutation to permute the held object.
	 * 
	 * @param permutation
	 *            the permutation to apply
	 * 
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 */
	Permutable<T> apply(Permutation permutation);

	/**
	 * The object being permuted. This method is commonly chained after one or
	 * more calls to the {@link #apply(Permutation)} method, or its convenient
	 * equivalents.
	 * 
	 * @return the object being permuted
	 */
	T permuted();

	/**
	 * This is the number of indices that can be transposed in the object being
	 * permuted.
	 * 
	 * @return the size of the permuted object.
	 */
	int size();

	/**
	 * Transposes the values at two indices. Equivalent to
	 * <code>Permutatable.apply(Permutation.transpose(i,j)</code>
	 * 
	 * @param i
	 *            a valid index, not equal to j
	 * @param j
	 *            a valid index, not equal to i
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#transpose(int, int, int)
	 */
	default Permutable<T> transpose(int i, int j) {
		return apply(Permutation.transpose(size(), i, j));
	}

	/**
	 * Rotates the values of the permuted object. Conceptually, positive
	 * distances will move values to higher indices.
	 * 
	 * @param distance
	 *            the number of indices through which values are shifted, may
	 *            exceed {@link #size()}; may be negative.
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#rotate(int, int)
	 */
	default Permutable<T> rotate(int distance) {
		return apply(Permutation.rotate(size(), distance));
	}

	/**
	 * Reverses the values of the permuted object.
	 * 
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#reverse(int)
	 */
	default Permutable<T> reverse() {
		return apply(Permutation.reverse(size()));
	}

	/**
	 * Moves values of the permuted object through the specified cycle of
	 * indices; the value at index <code>cycle[i]</code> will move to index
	 * <code>cycle[i+1]</code> and so on.
	 * 
	 * @param cycle
	 *            an array of valid indices with no duplicates
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#cycle(int, int...)
	 */
	default Permutable<T> cycle(int... cycle) {
		return apply(Permutation.cycle(size(), cycle));
	}

	/**
	 * Randomly shuffles values in the permuted object to new indices.
	 * @param random a source of random numbers
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#shuffle(int, Random)
	 */
	default Permutable<T> shuffle(Random random) {
		return apply(Permutation.shuffle(size(), random));
	}

	/**
	 * Moves the values of the permuted object according to corresponding
	 * indices.
	 * 
	 * @param correspondence
	 *            an ordering of the {@link #size()} indices.
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#correspond(int...)
	 */
	default Permutable<T> correspond(int... correspondence) {
		return apply(Permutation.correspond(correspondence));
	}
	
	/**
	 * Reorders the values of the permuted object according to ordering of the
	 * indices.
	 * 
	 * @param ordering
	 *            an ordering of the {@link #size()} indices.
	 * @return a {@link Permutable} through which a new permutation can be
	 *         applied to the same object.
	 * @see Permutation#reorder(int...)
	 */
	default Permutable<T> reorder(int... ordering) {
		return apply(Permutation.reorder(ordering));
	}

}