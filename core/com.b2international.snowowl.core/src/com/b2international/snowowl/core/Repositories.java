/*
 * Copyright 2017-2021 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.core;

import java.util.Collections;
import java.util.List;

import com.b2international.snowowl.core.domain.ListCollectionResource;

/**
 * @since 5.8
 */
public final class Repositories extends ListCollectionResource<RepositoryInfo> {

	private static final long serialVersionUID = 1L;
	
	public Repositories() {
		this(Collections.emptyList());
	}
	
	public Repositories(List<RepositoryInfo> items) {
		super(items);
	}

}
