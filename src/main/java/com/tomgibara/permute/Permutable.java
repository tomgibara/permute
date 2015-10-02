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

public interface Permutable<T> {

	Permutable<T> apply(Permutation permutation);

	T permuted();

	int size();

	default Permutable<T> transpose(int i, int j) {
		return apply(Permutation.transpose(size(), i, j));
	}

	default Permutable<T> rotate(int distance) {
		return apply(Permutation.rotate(size(), distance));
	}

	default Permutable<T> reverse() {
		return apply(Permutation.reverse(size()));
	}
	
	default Permutable<T> cycle(int... cycle) {
		return apply(Permutation.cycle(size(), cycle));
	}
	
	default Permutable<T> correspond(int... correspondence) {
		return apply(Permutation.correspond(correspondence));
	}
	
	default Permutable<T> reorder(int... ordering) {
		return apply(Permutation.reorder(ordering));
	}

}