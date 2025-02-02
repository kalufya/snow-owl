/*
 * Copyright 2011-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.request.version;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.b2international.index.Hits;
import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.snowowl.core.authorization.AccessControl;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.identity.Permission;
import com.b2international.snowowl.core.request.SearchIndexResourceRequest;
import com.b2international.snowowl.core.version.Version;
import com.b2international.snowowl.core.version.VersionDocument;
import com.b2international.snowowl.core.version.Versions;

/**
 * @since 4.7
 */
public final class VersionSearchRequest
		extends SearchIndexResourceRequest<RepositoryContext, Versions, VersionDocument>
		implements AccessControl {

	private static final long serialVersionUID = 3L;

	/**
	 * @since 6.15
	 */
	public static enum OptionKey {
		/**
		 * Filter versions by their tag.
		 */
		VERSION,

		/**
		 * Filter versions by associated resource.
		 */
		RESOURCE,

		/**
		 * Filter versions by effective date starting with this value, inclusive.
		 */
		EFFECTIVE_TIME_START,

		/**
		 * Filter versions by effective date ending with this value, inclusive.
		 */
		EFFECTIVE_TIME_END,

		/**
		 * Filter versions by associated resource's type.
		 */
		RESOURCE_TYPE,

		/**
		 * Filter matches by corresponding resource branch path (formerly parent branch path).
		 */
		RESOURCE_BRANCHPATH,

		/**
		 * Filter by the author's username who have created the version.
		 */
		AUTHOR,
		/**
		 * "Greater than equal to filter
		 */
		CREATED_AT_FROM,
		/**
		 * "Less than equal to filter
		 */
		CREATED_AT_TO,
	}

	VersionSearchRequest() {
	}

	@Override
	protected Expression prepareQuery(RepositoryContext context) {
		final ExpressionBuilder query = Expressions.builder();

		addIdFilter(query, VersionDocument.Expressions::ids);
		addFilter(query, OptionKey.RESOURCE_TYPE, String.class, VersionDocument.Expressions::resourceTypes);
		addFilter(query, OptionKey.RESOURCE, String.class, resources -> Expressions.builder()
				.should(VersionDocument.Expressions.resources(resources))
				.should(VersionDocument.Expressions.resourceIds(resources))
				.build());
		addFilter(query, OptionKey.VERSION, String.class, VersionDocument.Expressions::versions);
		addFilter(query, OptionKey.RESOURCE_BRANCHPATH, String.class, VersionDocument.Expressions::resourceBranchPaths);
		addFilter(query, OptionKey.AUTHOR, String.class, VersionDocument.Expressions::authors);

		if (containsKey(OptionKey.CREATED_AT_FROM) || containsKey(OptionKey.CREATED_AT_TO)) {
			final Long createdAtFrom = containsKey(OptionKey.CREATED_AT_FROM) ? get(OptionKey.CREATED_AT_FROM, Long.class) : 0L;
			final Long createdAtTo = containsKey(OptionKey.CREATED_AT_TO) ? get(OptionKey.CREATED_AT_TO, Long.class) : Long.MAX_VALUE;
			query.filter(VersionDocument.Expressions.createdAt(createdAtFrom, createdAtTo));
		}

		if (containsKey(OptionKey.EFFECTIVE_TIME_START) || containsKey(OptionKey.EFFECTIVE_TIME_END)) {
			final long from = containsKey(OptionKey.EFFECTIVE_TIME_START) ? get(OptionKey.EFFECTIVE_TIME_START, Long.class) : 0;
			final long to = containsKey(OptionKey.EFFECTIVE_TIME_END) ? get(OptionKey.EFFECTIVE_TIME_END, Long.class) : Long.MAX_VALUE;
			query.filter(VersionDocument.Expressions.effectiveTime(from, to));
		}

		return query.build();
	}

	@Override
	protected Class<VersionDocument> getDocumentType() {
		return VersionDocument.class;
	}

	@Override
	protected Versions toCollectionResource(RepositoryContext context, Hits<VersionDocument> hits) {
		return new Versions(toResource(hits), hits.getSearchAfter(), limit(), hits.getTotal());
	}

	private List<Version> toResource(Hits<VersionDocument> hits) {
		return hits.stream()
				.map(this::toResource)
				.collect(Collectors.toList());
	}

	private Version toResource(VersionDocument doc) {
		Version version = new Version();
		version.setId(doc.getId());
		version.setVersion(doc.getVersion());
		version.setDescription(doc.getDescription());
		version.setEffectiveTime(doc.getEffectiveTimeAsLocalDate());
		version.setResource(doc.getResource());
		version.setBranchPath(doc.getBranchPath());
		version.setCreatedAt(doc.getCreatedAt());
		version.setAuthor(doc.getAuthor());
		return version;
	}

	@Override
	protected Versions createEmptyResult(int limit) {
		return new Versions(Collections.emptyList(), null, limit, 0);
	}

	@Override
	public String getOperation() {
		return Permission.OPERATION_BROWSE;
	}

}
