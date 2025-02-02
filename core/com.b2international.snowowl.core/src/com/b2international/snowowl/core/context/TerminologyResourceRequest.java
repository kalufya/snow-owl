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

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.commons.exceptions.BadRequestException;
import com.b2international.commons.exceptions.NotFoundException;
import com.b2international.snowowl.core.Resource;
import com.b2international.snowowl.core.ResourceURI;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.TerminologyResource;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.events.DelegatingRequest;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.repository.PathTerminologyResourceResolver;
import com.b2international.snowowl.core.request.ResourceRequests;
import com.b2international.snowowl.core.uri.ResourceURIPathResolver;
import com.google.common.base.Strings;

/**
 * @since 8.0
 * @param <R>
 */
public final class TerminologyResourceRequest<R> extends DelegatingRequest<ServiceProvider, TerminologyResourceContext, R> {

	private static final long serialVersionUID = 1L;
	
	private final String toolingId;
	
	@NotEmpty
	private final String resourcePath;

	private transient ResourceURI resourceUri;
	private transient TerminologyResource resource;
	private transient String branchPath;
	
	public TerminologyResourceRequest(final String toolingId, final String resourcePath, final Request<TerminologyResourceContext, R> next) {
		super(next);
		this.toolingId = toolingId;
		this.resourcePath = resourcePath;
		if (resourcePath.startsWith(Branch.MAIN_PATH) && Strings.isNullOrEmpty(toolingId)) {
			throw new BadRequestException("Reflective access ('repositoryId/path') to terminology resource content is not supported in this request builder.");
		}
	}
	
	public ResourceURI getResourceURI(ServiceProvider context) {
		if (resourceUri == null) {
			initialize(context);
		}
		return resourceUri;
	}
	
	@Override
	public R execute(ServiceProvider context) {
		final TerminologyResource resource = getResource(context);
		return next(new DefaultTerminologyResourceContext(context, resourceUri, resource));
	}
	
	public TerminologyResource getResource(ServiceProvider context) {
		if (resource == null) {
			initialize(context);
		}
		return resource;
	}

	public String getBranchPath(ServiceProvider context) {
		if (branchPath == null) {
			initialize(context);
		}
		return branchPath;
	}
	
	private void initialize(ServiceProvider context) {
		if (resourcePath.startsWith(Branch.MAIN_PATH)) {
			context.log().warn("Reflective access of terminology resources ('{}/{}') is not the recommended way of accessing resources. Consider using Resource IDs and relative branch path expressions.", toolingId, resourcePath);
			this.resource = context.service(PathTerminologyResourceResolver.class).resolve(context, toolingId, resourcePath);
			this.resourceUri = resource.getResourceURI(resourcePath);
			this.branchPath = resourcePath;
		} else {
			// resourcePaths are just ID/PATH style expressions to reference content in a terminology repository
			final ResourceURI referenceResourceUri = ResourceURI.of("any", resourcePath);
			Resource resource = ResourceRequests.prepareGet(referenceResourceUri).buildAsync().getRequest().execute(context);
			if (!(resource instanceof TerminologyResource)) {
				throw new NotFoundException("Terminology Resource", referenceResourceUri.getResourceId());
			}
			this.resource = (TerminologyResource) resource;
			this.resourceUri = this.resource.getResourceURI()
				.withPath(referenceResourceUri.getPath())
				.withTimestampPart(referenceResourceUri.getTimestampPart());
			this.branchPath = context.service(ResourceURIPathResolver.class)
				.resolve(context, referenceResourceUri, resource);
		}		
	}

}
