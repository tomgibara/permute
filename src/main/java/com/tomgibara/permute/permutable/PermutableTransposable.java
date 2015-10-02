package com.tomgibara.permute.permutable;

import com.tomgibara.fundament.Transposable;
import com.tomgibara.permute.Permutable;
import com.tomgibara.permute.Permutation;

public class PermutableTransposable implements Permutable<Transposable> {

	private final int size;
	private final Transposable transposable;
	
	public PermutableTransposable(int size, Transposable transposable) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (transposable == null) throw new IllegalArgumentException("null transposable");
		this.size = size;
		this.transposable = transposable;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public Permutable<Transposable> apply(Permutation permutation) {
		if (permutation == null) throw new IllegalArgumentException("null permutation");
		permutation.permute(transposable);
		return this;
	}

	@Override
	public Transposable permuted() {
		return transposable;
	}
	
	// object methods
	
	@Override
	public int hashCode() {
		return transposable.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof PermutableTransposable)) return false;
		PermutableTransposable that = (PermutableTransposable) obj;
		return this.transposable.equals(that.transposable);
	}
	
	@Override
	public String toString() {
		return transposable.toString();
	}

}
