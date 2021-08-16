/*
 * Copyright 2019-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.authorization;

import java.util.List;

import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.context.TerminologyResourceRequest;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.identity.Permission;
import com.b2international.snowowl.core.request.BranchRequest;
import com.b2international.snowowl.core.request.RepositoryAwareRequest;
import com.google.common.collect.Lists;

/**
 * Represents an authorization context where a permission is required to get access.
 * 
 * @since 7.2
 */
public interface AccessControl {

	default Permission getPermission(ServiceProvider context) {
		return Permission.requireAny(getOperation(), getResources(context));
	}
	
	/**
	 * @return a {@link Permission}s required to access/execute/etc. this request.
	 */
	default List<String> getResources(ServiceProvider context) {
		final List<String> accessedResources = Lists.newArrayList();
		
		if (!(this instanceof Request<?, ?>)) {
			throw new UnsupportedOperationException("AccessControl interface needs to be declared on Request implementations");
		}
		
		Request<?, ?> req = (Request<?, ?>) this;

		// TODO support
		
		// extract repositoryId/branch resource if present (old 7.x format)
		RepositoryAwareRequest repositoryRequest = Request.getNestedRequest(req, RepositoryAwareRequest.class);
		if (repositoryRequest != null) {
			BranchRequest<?> branchRequest = Request.getNestedRequest(req, BranchRequest.class);
			if (branchRequest != null) {
				accessedResources.add(Permission.asResource(repositoryRequest.getRepositoryId(), branchRequest.getBranchPath()));
			} else {
				accessedResources.add(Permission.asResource(repositoryRequest.getRepositoryId()));
			}
		}
		
		// extract resourceUri format (new 8.x format)
		TerminologyResourceRequest<?> terminologyResourceRequest = Request.getNestedRequest(req, TerminologyResourceRequest.class);
		if (terminologyResourceRequest != null) {
			accessedResources.add(Permission.asResource(terminologyResourceRequest.getResourceURI(context).toString()));
		}
		
		return accessedResources;
	}

	/**
	 * @return the operation for this request access control configuration
	 */
	String getOperation();

}
