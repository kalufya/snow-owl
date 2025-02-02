/*
 * Copyright 2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.context;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.events.AsyncRequest;
import com.b2international.snowowl.core.request.BranchRequest;
import com.b2international.snowowl.core.request.CommitResult;
import com.b2international.snowowl.core.request.RepositoryCommitRequestBuilder;

/**
 * @since 8.0
 */
public final class ResourceRepositoryCommitRequestBuilder extends RepositoryCommitRequestBuilder<ResourceRepositoryCommitRequestBuilder> {

	public AsyncRequest<CommitResult> buildAsync() {
		return new AsyncRequest<>(
			new ResourceRepositoryRequest<>(
				new BranchRequest<>(Branch.MAIN_PATH, 
					build()
				)
			)
		); 
	}

}
