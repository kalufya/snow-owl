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
package com.b2international.index.revision;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.elasticsearch.common.UUIDs;
import org.junit.Test;

import com.b2international.index.Fixtures.NestedData;
import com.b2international.index.Fixtures.ParentData;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Query;
import com.b2international.index.revision.RevisionFixtures.DeeplyNestedData;
import com.google.common.collect.ImmutableList;

/**
 * @since 4.7
 */
public class ComplexDocumentRevisionIndexTest extends BaseRevisionIndexTest {

	@Override
	protected final Collection<Class<?>> getTypes() {
		return ImmutableList.<Class<?>>of(DeeplyNestedData.class);
	}
	
	@Test
	public void searchDeeplyNestedDocument() throws Exception {
		final DeeplyNestedData data = new DeeplyNestedData(STORAGE_KEY1, new ParentData(UUIDs.randomBase64UUID(), "field1", new NestedData("field2")));
		final DeeplyNestedData data2 = new DeeplyNestedData(STORAGE_KEY2, new ParentData(UUIDs.randomBase64UUID(), "field12", new NestedData("field22")));
		
		indexRevision(MAIN, data, data2);
		
		final Query<DeeplyNestedData> deeplyNestedQuery = Query.select(DeeplyNestedData.class).where(Expressions.nestedMatch("parentData.nestedData", Expressions.exactMatch("field2", "field2"))).build();
		final Iterable<DeeplyNestedData> matches = search(MAIN, deeplyNestedQuery);
		assertThat(matches).hasSize(1);
		assertThat(matches).containsOnly(data);
	}
	
}
