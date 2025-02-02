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
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.elasticsearch.common.UUIDs;
import org.junit.Test;

import com.b2international.index.Fixtures.Data;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Query;
import com.b2international.index.revision.RevisionFixtures.NestedRevisionData;
import com.google.common.collect.ImmutableList;

/**
 * @since 4.7
 */
public class NestedDocumentRevisionIndexTest extends BaseRevisionIndexTest {

	@Override
	protected Collection<Class<?>> getTypes() {
		return ImmutableList.<Class<?>>of(NestedRevisionData.class);
	}
	
	@Test
	public void indexNestedDocument() throws Exception {
		final Data child = new Data(UUIDs.randomBase64UUID());
		child.setField1("field1");
		child.setField2("field2");
		
		final NestedRevisionData parent = new NestedRevisionData(STORAGE_KEY1, "parent1", child);
		indexRevision(MAIN, parent);
		assertEquals(parent, getRevision(MAIN, NestedRevisionData.class, STORAGE_KEY1));
	}

	@Test
	public void searchParentDocumentWithNestedQuery() throws Exception {
		final Data child1 = new Data(UUIDs.randomBase64UUID());
		child1.setField1("field1_1");
		child1.setField2("field2_1");
		final NestedRevisionData parent1 = new NestedRevisionData(STORAGE_KEY1, "parent1", child1);
		
		final Data child2 = new Data(UUIDs.randomBase64UUID());
		child2.setField1("field1_2");
		child2.setField2("field2_2");
		final NestedRevisionData parent2 = new NestedRevisionData(STORAGE_KEY2, "parent2", child2);
		
		indexRevision(MAIN, parent1);
		indexRevision(MAIN, parent2);
		
		final Query<NestedRevisionData> query = Query.select(NestedRevisionData.class)
				.where(Expressions.nestedMatch("data", Expressions.exactMatch("field1", "field1_1")))
				.build();
		
		final Iterable<NestedRevisionData> matches = search(MAIN, query);
		assertThat(matches).hasSize(1);
		assertThat(matches).containsOnly(parent1);
	}
	
	@Test
	public void compareNestedRevisionData() throws Exception {
		final String id = UUIDs.randomBase64UUID();
		final Data nestedData = new Data(id);
		nestedData.setField1("field1_1");
		nestedData.setField2("field2_1");
		final NestedRevisionData doc = new NestedRevisionData(STORAGE_KEY1, "parent1", nestedData);
		
		indexRevision(MAIN, doc);
		
		String a = createBranch(MAIN, "a");
		
		final Data updatedNestedData = new Data(id);
		updatedNestedData.setField1("field1_2");
		updatedNestedData.setField2("field2_2");
		final NestedRevisionData updatedDoc = new NestedRevisionData(STORAGE_KEY1, "parent1", updatedNestedData);
		indexChange(a, doc, updatedDoc);
		
		RevisionCompare compare = index().compare(MAIN, a);
		assertThat(compare.getDetails()).containsOnly(
			RevisionCompareDetail.componentChange(Operation.CHANGE, updatedDoc.getContainerId(), updatedDoc.getObjectId()),
			RevisionCompareDetail.propertyChange(Operation.CHANGE, updatedDoc.getObjectId(), "data/field1", "field1_1", "field1_2"),
			RevisionCompareDetail.propertyChange(Operation.CHANGE, updatedDoc.getObjectId(), "data/field2", "field2_1", "field2_2")
		);
	}
	
}
