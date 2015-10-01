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
package com.tomgibara.permute.permutable;

import com.tomgibara.permute.Permutable;
import com.tomgibara.permute.Permutation;

public class PermutableInterval<T> implements Permutable<Permutable<T>> {

	private final Permutable<T> permutable;
	private final int offset;
	private final int length;

	public PermutableInterval(Permutable<T> permutable, int offset, int length) {
		if (permutable == null) throw new IllegalArgumentException("null permutable");
		if (length < 0) throw new IllegalArgumentException("negative length");
		if (offset < 0) throw new IllegalArgumentException("negative offset");
		if (offset + length > permutable.getPermutableSize()) throw new IllegalArgumentException("size exceeded");

		this.permutable = permutable;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public Permutable<T> permuted() {
		return permutable;
	}

	@Override
	public int getPermutableSize() {
		return length;
	}

	@Override
	public void transpose(int i, int j) {
		permutable.transpose(i + offset, j + offset);
	}

	@Override
	public PermutableInterval<T> apply(Permutation permutation) {
		return (PermutableInterval<T>) Permutable.super.apply(permutation);
	}

	// object methods
	
	@Override
	public int hashCode() {
		return permutable.hashCode() + offset + 31 * length;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableInterval)) return false;
		PermutableInterval<?> that = (PermutableInterval<?>) obj;
		if (this.offset != that.offset) return false;
		if (this.length != that.length) return false;
		if (!this.permutable.equals(that.permutable)) return false;
		return true;
	}

	@Override
	public String toString() {
		return permutable.toString() + " offset " + offset + " length " + length;
	}
}
