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

public class PermutableString implements Permutable<StringBuilder> {

	private final StringBuilder sb;

	public PermutableString(String str) {
		if (str == null) throw new IllegalArgumentException("null str");
		sb = new StringBuilder(str);
	}

	public PermutableString(StringBuilder sb) {
		if (sb == null) throw new IllegalArgumentException("null sb");
		this.sb = sb;
	}
	
	@Override
	public StringBuilder permuted() {
		return sb;
	}

	public StringBuilder getStringBuilder() {
		return sb;
	}

	@Override
	public PermutableString apply(Permutation permutation) {
		PermutableUtil.check(permutation, sb.length());
		permutation.permute((i,j) -> {
			char c = sb.charAt(i);
			sb.setCharAt(i, sb.charAt(j));
			sb.setCharAt(j, c);
		});
		return this;
	}

	// object methods

	@Override
	public int hashCode() {
		return sb.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableString)) return false;
		PermutableString that = (PermutableString) obj;
		return this.sb.equals(that.sb);
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}

}
