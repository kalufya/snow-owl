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
package com.b2international.snowowl.core.internal.validation;

import java.io.IOException;

import com.b2international.index.*;
import com.b2international.index.admin.IndexAdmin;
import com.b2international.index.aggregations.Aggregation;
import com.b2international.index.aggregations.AggregationBuilder;
import com.b2international.index.query.Query;

/**
 * @since 6.0
 */
public final class ValidationRepository implements Index {

	private final Index index;

	public ValidationRepository(Index index) {
		this.index = index;
		this.index.admin().create();
	}
	
	@Override
	public IndexAdmin admin() {
		return index.admin();
	}

	@Override
	public String name() {
		return index.name();
	}

	@Override
	public <T> T read(IndexRead<T> read) {
		return index.read(read);
	}

	@Override
	public <T> T write(IndexWrite<T> write) {
		return index.write(write);
	}

	public Searcher searcher() {
		return new Searcher() {
			@Override
			public <T> Hits<T> search(Query<T> query) throws IOException {
				return read(searcher -> searcher.search(query));
			}
			
			@Override
			public <T> Hits<T> scroll(Scroll<T> scroll) throws IOException {
				return read(searcher -> searcher.scroll(scroll));
			}
			
			@Override
			public void cancelScroll(String scrollId) {
				read(searcher -> {
					searcher.cancelScroll(scrollId);
					return null;
				});
			}
			
			@Override
			public <T> Aggregation<T> aggregate(AggregationBuilder<T> aggregation) throws IOException {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T get(Class<T> type, String key) throws IOException {
				return read(searcher -> searcher.get(type, key));
			}

			@Override
			public <T> Iterable<T> get(Class<T> type, Iterable<String> keys) throws IOException {
				return read(searcher -> searcher.get(type, keys));
			}
		};
	}

	public void save(Object document) {
		write(writer -> {
			writer.put(document);
			writer.commit();
			return null;
		});
	}
	
	public <T> void remove(Class<T> clazz, String key) {
		index.write(writer -> {
			writer.remove(clazz, key);
			writer.commit();
			return null;
		});
	}
	
}
