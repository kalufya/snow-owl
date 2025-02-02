/*
 * Copyright 2020-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.request;

import static com.b2international.snowowl.core.request.ConceptSearchRequestEvaluator.OptionKey.DISPLAY;
import static com.b2international.snowowl.core.request.ConceptSearchRequestEvaluator.OptionKey.MUST_NOT_QUERY;
import static com.b2international.snowowl.core.request.ConceptSearchRequestEvaluator.OptionKey.QUERY;
import static com.b2international.snowowl.core.request.ConceptSearchRequestEvaluator.OptionKey.TERM;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Min;

import org.tartarus.snowball.ext.EnglishStemmer;

import com.b2international.index.compat.TextConstants;
import com.b2international.snowowl.core.ResourceURI;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.domain.Concept;
import com.b2international.snowowl.core.domain.Concepts;
import com.b2international.snowowl.core.domain.Suggestions;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * A generic concept suggestion request that uses the generic search functionality to return related concepts of
 * interest to the user.
 * 
 * @since 7.7
 * @see ConceptSearchRequest
 * @see ConceptSuggestionRequestBuilder
 */
public final class ConceptSuggestionRequest extends SearchResourceRequest<BranchContext, Suggestions> {

	private static final long serialVersionUID = 1L;
	
	// Split terms at delimiter or whitespace separators
	private static final Splitter TOKEN_SPLITTER = Splitter.on(TextConstants.WHITESPACE_OR_DELIMITER_MATCHER)
			.trimResults()
			.omitEmptyStrings();
	
	private static final int SCROLL_LIMIT = 1000;

	@Min(1)
	private int topTokenCount;
	
	@Min(1)
	private int minOccurrenceCount;
	
	private transient List<String> topTokens;
	
	void setTopTokenCount(int topTokenCount) {
		this.topTokenCount = topTokenCount;
	}
	
	void setMinOccurrenceCount(int minOccurrenceCount) {
		this.minOccurrenceCount = minOccurrenceCount;
	}

	@Override
	protected Suggestions createEmptyResult(int limit) {
		return new Suggestions(topTokens, limit, 0);
	}

	@Override
	protected Suggestions doExecute(BranchContext context) throws IOException {
		TermFilter termFilter;
		
		if (containsKey(TERM)) {
			termFilter = TermFilter.defaultTermMatch(getString(TERM)).withIgnoreStopwords();
		} else {
			// Gather tokens
			final Multiset<String> tokenOccurrences = HashMultiset.create(); 
			final EnglishStemmer stemmer = new EnglishStemmer();
			
			// Get the suggestion base set of concepts
			final ConceptSearchRequestBuilder baseRequestBuilder = new ConceptSearchRequestBuilder()
					.filterByCodeSystemUri(context.service(ResourceURI.class))
					.setLimit(SCROLL_LIMIT)
					.setLocales(locales());
			
			if (containsKey(QUERY)) {
				baseRequestBuilder.filterByInclusions(getCollection(QUERY, String.class));
			}
			
			if (containsKey(MUST_NOT_QUERY)) {
				baseRequestBuilder.filterByExclusions(getCollection(MUST_NOT_QUERY, String.class));
			}
			
			if (containsKey(TERM)) {
				baseRequestBuilder.filterByTerm(getString(TERM));
			}
			
			baseRequestBuilder.stream(context)
			.flatMap(Concepts::stream)
			.flatMap(concept -> getAllTerms(concept).stream())
			.map(term -> term.toLowerCase(Locale.US))
			.flatMap(lowerCaseTerm -> TOKEN_SPLITTER.splitToList(lowerCaseTerm).stream())
			.map(token -> stemToken(stemmer, token))
			.forEach(tokenOccurrences::add);
			
			topTokens = Multisets.copyHighestCountFirst(tokenOccurrences)
					.elementSet()
					.stream()
					.filter(token -> token.length() > 2) // skip short tokens
					.limit(topTokenCount)
					.collect(Collectors.toList());
			
			termFilter = TermFilter.minTermMatch(topTokens.stream().collect(Collectors.joining(" ")), minOccurrenceCount);
		}
		
		/* 
		 * Run a search with the top tokens and minimum number of matches, excluding everything
		 * that was included previously.
		 */
		final Set<String> exclusions = newHashSet();
		exclusions.addAll(getCollection(QUERY, String.class));
		exclusions.addAll(getCollection(MUST_NOT_QUERY, String.class));

		final ConceptSearchRequestBuilder resultRequestBuilder = new ConceptSearchRequestBuilder()
				.filterByCodeSystemUri(context.service(ResourceURI.class))
				.filterByActive(true)
				.filterByTerm(termFilter)
				.setPreferredDisplay(getString(DISPLAY))
				.setLimit(limit())
				.setLocales(locales())
				.setSearchAfter(searchAfter())
				.sortBy(sortBy());
		
		if (!exclusions.isEmpty()) {
			resultRequestBuilder.filterByExclusions(exclusions);
		}
		
		final Concepts conceptSuggestions = resultRequestBuilder.build().execute(context);
		
		return new Suggestions(topTokens, conceptSuggestions.getItems(), conceptSuggestions.getSearchAfter(), limit(), conceptSuggestions.getTotal());
	}

	private List<String> getAllTerms(Concept concept) {
		return ImmutableList.<String>builder()
			.add(concept.getTerm())
			.addAll(concept.getAlternativeTerms())
			.build();
	}

	private String stemToken(EnglishStemmer stemmer, String token) {
		stemmer.setCurrent(token);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}
