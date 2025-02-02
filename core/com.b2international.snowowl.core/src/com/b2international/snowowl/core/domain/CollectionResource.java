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
package com.b2international.snowowl.core.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterables;

/**
 * @since 8.0
 */
public interface CollectionResource<T> extends Serializable, Iterable<T> {

	/**
	 * @return the items associated in the collection, never <code>null</code>
	 */
	Collection<T> getItems();
	
	/**
	 * @return <tt>true</tt> if this {@link CollectionResource} contains no elements
	 */
	@JsonIgnore
	default boolean isEmpty() {
		return getItems().isEmpty(); 
	}

	/**
	 * @return an {@link Optional} item in this collection resource
	 */
	@JsonIgnore
	default Optional<T> first() {
		return Optional.ofNullable(Iterables.getFirst(getItems(), null));
	}
	
	/**
	 * Returns a sequential {@code Stream} with this collection resource as its source.
	 * 
	 * @return a {@link Stream} over the items of this collection resource
	 */
	@JsonIgnore
	default Stream<T> stream() {
		return getItems().stream();
	}
	
	@Override
	default Iterator<T> iterator() {
		return getItems().iterator();
	}
	
}
