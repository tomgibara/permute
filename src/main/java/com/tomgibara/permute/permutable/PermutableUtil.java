package com.tomgibara.permute.permutable;

import com.tomgibara.permute.Permutation;

class PermutableUtil {

	static void check(Permutation permutation, int size) {
		if (permutation == null) throw new IllegalArgumentException("null permutation");
		if (permutation.getSize() != size) throw new IllegalArgumentException("mismatched size");
	}
}
