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
package com.b2international.snowowl.snomed.datastore.request;

import static com.b2international.snowowl.core.repository.RevisionDocument.Expressions.id;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.*;

import java.util.List;

import com.b2international.commons.exceptions.BadRequestException;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.index.Hits;
import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.repository.RevisionDocument;
import com.b2international.snowowl.core.request.TermFilter;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.snomed.cis.SnomedIdentifiers;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.datastore.SnomedDescriptionUtils;
import com.b2international.snowowl.snomed.datastore.converter.SnomedDescriptionConverter;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDocument;

/**
 * @since 4.5
 */
final class SnomedDescriptionSearchRequest extends SnomedComponentSearchRequest<SnomedDescriptions, SnomedDescriptionIndexEntry> {

	private static final long serialVersionUID = 1L;

	enum OptionKey {
		TERM,
		CONCEPT,
		TYPE,
		LANGUAGE,
		CASE_SIGNIFICANCE, 
		TERM_REGEX, 
		SEMANTIC_TAG,
		SEMANTIC_TAG_REGEX,
		LANGUAGE_REFSET,
		LANGUAGE_REFSET_LOCALES, 
		ACCEPTABLE_IN,
		ACCEPTABLE_IN_LOCALES,
		PREFERRED_IN,
		PREFERRED_IN_LOCALES, 
	}
	
	SnomedDescriptionSearchRequest() {}

	@Override
	protected Class<SnomedDescriptionIndexEntry> getDocumentType() {
		return SnomedDescriptionIndexEntry.class;
	}
	
	@Override
	protected Expression prepareQuery(BranchContext context) {
		if (containsKey(OptionKey.TERM) && get(OptionKey.TERM, TermFilter.class).getTerm().length() < 2) {
			throw new BadRequestException("Description term must be at least 2 characters long.");
		}

		final ExpressionBuilder queryBuilder = Expressions.builder();
		// Add (presumably) most selective filters first
		addActiveClause(queryBuilder);
		addReleasedClause(queryBuilder);
		addLanguageFilter(queryBuilder);
		addNamespaceFilter(queryBuilder);
		addNamespaceConceptIdFilter(context, queryBuilder);
		addActiveMemberOfClause(context, queryBuilder);
		addMemberOfClause(context, queryBuilder);
		
		addEclFilter(context, queryBuilder, OptionKey.LANGUAGE_REFSET, ids -> {
			return Expressions.builder()
					.should(preferredIn(ids))
					.should(acceptableIn(ids))
					.build();
		});
		addEclFilter(context, queryBuilder, OptionKey.ACCEPTABLE_IN, ids -> acceptableIn(ids));
		addEclFilter(context, queryBuilder, OptionKey.PREFERRED_IN, ids -> preferredIn(ids));
		
		// apply locale based filters
		addFilter(queryBuilder, OptionKey.LANGUAGE_REFSET_LOCALES, ExtendedLocale.class, locales -> {
			final List<String> languageRefSetIds = SnomedDescriptionUtils.getLanguageRefSetIds(context, (List<ExtendedLocale>) locales);
			return Expressions.builder()
					.should(preferredIn(languageRefSetIds))
					.should(acceptableIn(languageRefSetIds))
					.build();
		});
		addFilter(queryBuilder, OptionKey.ACCEPTABLE_IN_LOCALES, ExtendedLocale.class, locales -> {
			final List<String> languageRefSetIds = SnomedDescriptionUtils.getLanguageRefSetIds(context, (List<ExtendedLocale>) locales);
			return acceptableIn(languageRefSetIds);
		});
		addFilter(queryBuilder, OptionKey.PREFERRED_IN_LOCALES, ExtendedLocale.class, locales -> {
			final List<String> languageRefSetIds = SnomedDescriptionUtils.getLanguageRefSetIds(context, (List<ExtendedLocale>) locales);
			return preferredIn(languageRefSetIds);
		});
		
		addEffectiveTimeClause(queryBuilder);
		addIdFilter(queryBuilder, RevisionDocument.Expressions::ids);
		addEclFilter(context, queryBuilder, SnomedSearchRequest.OptionKey.MODULE, SnomedDocument.Expressions::modules);
		addEclFilter(context, queryBuilder, OptionKey.CONCEPT, SnomedDescriptionIndexEntry.Expressions::concepts);
		addEclFilter(context, queryBuilder, OptionKey.TYPE, SnomedDescriptionIndexEntry.Expressions::types);
		addEclFilter(context, queryBuilder, OptionKey.CASE_SIGNIFICANCE, SnomedDescriptionIndexEntry.Expressions::caseSignificances);
		
		if (containsKey(OptionKey.SEMANTIC_TAG)) {
			queryBuilder.filter(SnomedDescriptionIndexEntry.Expressions.semanticTags(getCollection(OptionKey.SEMANTIC_TAG, String.class)));
		}
		
		if (containsKey(OptionKey.SEMANTIC_TAG_REGEX)) {
			queryBuilder.filter(SnomedDescriptionIndexEntry.Expressions.semanticTagRegex(getString(OptionKey.SEMANTIC_TAG_REGEX)));
		}
			
		if (containsKey(OptionKey.TERM_REGEX)) {
			final String regex = getString(OptionKey.TERM_REGEX);
			queryBuilder.filter(SnomedDescriptionIndexEntry.Expressions.matchTermRegex(regex));
		}
		
		if (containsKey(OptionKey.TERM)) {
			final TermFilter termFilter = get(OptionKey.TERM, TermFilter.class);
			queryBuilder.must(toDescriptionTermQuery(termFilter));
		}
		
		return queryBuilder.build();
	}

	@Override
	protected boolean trackScores() {
		return containsKey(OptionKey.TERM);
	}

	@Override
	protected SnomedDescriptions toCollectionResource(BranchContext context, Hits<SnomedDescriptionIndexEntry> hits) {
		if (limit() < 1 || hits.getTotal() < 1) {
			return new SnomedDescriptions(limit(), hits.getTotal());
		} else {
			return new SnomedDescriptionConverter(context, expand(), locales()).convert(hits);
		}
	}
	
	@Override
	protected SnomedDescriptions createEmptyResult(int limit) {
		return new SnomedDescriptions(limit, 0);
	}
	
	private Expression toDescriptionTermQuery(final TermFilter termFilter) {
		final ExpressionBuilder qb = Expressions.builder();
	
		if (termFilter.isFuzzy()) {
			qb.should(fuzzy(termFilter.getTerm()));
		} else if (termFilter.isExact()) {
			qb.should(matchTerm(termFilter.getTerm(), termFilter.isCaseSensitive()));
		} else if (termFilter.isParsed()) {
			qb.should(parsedTerm(termFilter.getTerm()));
		} else if (termFilter.isAnyMatch()) {
			qb.should(minShouldMatchTermDisjunctionQuery(termFilter));
		} else {
			qb.should(termDisjunctionQuery(termFilter));
		}
		
		if (isComponentId(termFilter.getTerm(), ComponentCategory.DESCRIPTION)) {
			qb.should(Expressions.boost(id(termFilter.getTerm()), 1000f));
		}
		
		return qb.build();
	}
	
	private boolean isComponentId(String value, ComponentCategory type) {
		try {
			return SnomedIdentifiers.getComponentCategory(value) == type;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private void addLanguageFilter(ExpressionBuilder queryBuilder) {
		if (containsKey(OptionKey.LANGUAGE)) {
			queryBuilder.filter(languageCodes(getCollection(OptionKey.LANGUAGE, String.class)));
		}
	}

}
