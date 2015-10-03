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

/**
 * A sequence of permutations that can be iterated over forwards and backwards.
 * Most methods on this interface return the sequence. This allows calls that
 * modify the sequence position to be chained. All permutation sequences are
 * associated with a permutation generator. The current permutation of the
 * sequence can be obtained from this generator. Otherwise modifying the
 * generator can also change the sequence position.
 * 
 * @author Tom Gibara
 *
 * @see Permutation.Generator
 */
public interface PermutationSequence {

	/**
	 * Whether there is a subsequent permutation in the sequence.
	 * 
	 * @return whether there is a next permutation
	 */
	boolean hasNext();

	/**
	 * Whether there is a prior permutation in the sequence.
	 * 
	 * @return whether there is a previous permutation
	 */
	boolean hasPrevious();

	/**
	 * Moves to the first in the sequence.
	 * 
	 * @return the sequence
	 */
	PermutationSequence first();

	/**
	 * Moves to the last in the sequence.
	 * 
	 * @return the sequence
	 */
	PermutationSequence last();

	/**
	 * Moves to the next in the sequence.
	 * 
	 * @return the sequence
	 */
	PermutationSequence next();

	/**
	 * Moves to the previous in the sequence.
	 * 
	 * @return the sequence
	 */
	PermutationSequence previous();

	/**
	 * The generator associated with this sequence. The current permutation of
	 * the sequence can be obtained from this generator.
	 * 
	 * @return the sequence generator
	 */
	Permutation.Generator getGenerator();
}
