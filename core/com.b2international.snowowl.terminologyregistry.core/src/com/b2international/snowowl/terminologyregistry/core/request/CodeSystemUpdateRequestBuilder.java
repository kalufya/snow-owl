/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.terminologyregistry.core.request;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.datastore.request.BaseTransactionalRequestBuilder;

/**
 * @since 4.7
 */
public final class CodeSystemUpdateRequestBuilder extends BaseTransactionalRequestBuilder<CodeSystemUpdateRequestBuilder, Void> {

	private final String repositoryId;
	private final String uniqueId;

	private String oid;
	private String name;
	private String shortName;
	private String link;
	private String language;
	private String citation;
	private String branchPath;
	private String iconPath;
	private String terminologyId;

	CodeSystemUpdateRequestBuilder(final String repositoryId, final String uniqueId) {
		super(repositoryId);
		this.repositoryId = repositoryId;
		this.uniqueId = uniqueId;
	}

	public CodeSystemUpdateRequestBuilder setOid(String oid) {
		this.oid = oid;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setName(String name) {
		this.name = name;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setShortName(String shortName) {
		this.shortName = shortName;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setLink(String link) {
		this.link = link;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setLanguage(String language) {
		this.language = language;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setCitation(String citation) {
		this.citation = citation;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setBranchPath(String branchPath) {
		this.branchPath = branchPath;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setIconPath(String iconPath) {
		this.iconPath = iconPath;
		return getSelf();
	}

	public CodeSystemUpdateRequestBuilder setTerminologyId(String terminologyId) {
		this.terminologyId = terminologyId;
		return getSelf();
	}

	@Override
	protected Request<TransactionContext, Void> doBuild() {
		final CodeSystemUpdateRequest req = new CodeSystemUpdateRequest(repositoryId, uniqueId);

		req.setOid(oid);
		req.setName(name);
		req.setShortName(shortName);
		req.setLink(link);
		req.setLanguage(language);
		req.setCitation(citation);
		req.setBranchPath(branchPath);
		req.setIconPath(iconPath);
		req.setTerminologyId(terminologyId);

		return req;
	}

}
