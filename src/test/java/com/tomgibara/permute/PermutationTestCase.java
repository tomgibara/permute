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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tomgibara.permute.permutable.PermutableList;

import junit.framework.TestCase;

abstract class PermutationTestCase extends TestCase {

	static List<Integer> list(Integer... ints) {
		return Arrays.asList(ints);
	}
	
	static List<Integer> copy(List<Integer> list) {
		return new ArrayList<Integer>(list);
	}
	
	static PermutableList<Integer> permutable(List<Integer> list) {
		return new PermutableList<Integer>(list);
	}
	
	static Set<Permutation> set(Permutation... perms) {
		HashSet<Permutation> set = new HashSet<Permutation>();
		set.addAll(Arrays.asList(perms));
		return set;
	}
	
}
