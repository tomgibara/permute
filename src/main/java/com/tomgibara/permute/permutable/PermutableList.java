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

import java.util.List;

import com.tomgibara.permute.Permutable;
import com.tomgibara.permute.Permutation;

public class PermutableList<E> implements Permutable<List<E>> {

	private final List<E> list;

	public PermutableList(List<E> list) {
		if (list == null) throw new IllegalArgumentException("null list");
		this.list = list;
	}

	@Override
	public List<E> permuted() {
		return list;
	}

	@Override
	public int getPermutableSize() {
		return list.size();
	}

	@Override
	public void transpose(int i, int j) {
		E e = list.get(i);
		list.set(i, list.get(j));
		list.set(j, e);
	}

	@Override
	public PermutableList<E> apply(Permutation permutation) {
		return (PermutableList<E>) Permutable.super.apply(permutation);
	}

	// object methods
	
	@Override
	public int hashCode() {
		return list.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableList)) return false;
		PermutableList<?> that = (PermutableList<?>) obj;
		return this.list.equals(that.list);
	}

	@Override
	public String toString() {
		return list.toString();
	}
}
