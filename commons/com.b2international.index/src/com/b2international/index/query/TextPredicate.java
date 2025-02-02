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
package com.b2international.index.query;

import com.b2international.commons.CompareUtils;
import com.b2international.index.Analyzers;

/**
 * @since 4.7
 */
public final class TextPredicate extends Predicate {

	public enum MatchType {
		ALL, ANY, PHRASE, FUZZY, PARSED, BOOLEAN_PREFIX
	}
	
	private final String term;
	private final MatchType type;
	private final int minShouldMatch;
	
	private Analyzers analyzer;
	
	TextPredicate(String field, String term, MatchType type) {
		this(field, term, type, 1);
	}
	
	TextPredicate(String field, String term, MatchType type, int minShouldMatch) {
		super(field);
		this.term = term;
		this.type = type;
		this.minShouldMatch = minShouldMatch;
	}
	
	public String term() {
		return term;
	}
	
	public MatchType type() {
		return type;
	}
	
	public int minShouldMatch() {
		return minShouldMatch;
	}
	
	public String analyzer() {
		return analyzer == null ? null : analyzer.getAnalyzer();
	}
	
	public TextPredicate withAnalyzer(Analyzers analyzer) {
		this.analyzer = analyzer;
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("TEXT(%s %s '%s'[])", getField(), type(), term(), CompareUtils.isEmpty(analyzer));
	}

	public TextPredicate withIgnoreStopwords(boolean ignoreStopwords) {
		if (ignoreStopwords) {
			return withAnalyzer((analyzer == Analyzers.TOKENIZED_SYNONYMS || analyzer == Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS) ? Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS : Analyzers.TOKENIZED_IGNORE_STOPWORDS);
		} else {
			return withAnalyzer(analyzer == Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS ? Analyzers.TOKENIZED_SYNONYMS : null);
		}
	}
	
	public TextPredicate withSynonyms(boolean enableSynonyms) {
		if (enableSynonyms) {
			return withAnalyzer((analyzer == Analyzers.TOKENIZED_IGNORE_STOPWORDS || analyzer == Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS) ? Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS : Analyzers.TOKENIZED_SYNONYMS);
		} else {
			return withAnalyzer(analyzer == Analyzers.TOKENIZED_SYNONYMS_IGNORE_STOPWORDS ? Analyzers.TOKENIZED_IGNORE_STOPWORDS : null);
		}
	}

}
