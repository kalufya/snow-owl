/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.index.entry;

import java.io.Serializable;

import org.apache.lucene.document.Document;

import com.b2international.commons.BooleanUtils;
import com.b2international.snowowl.core.api.IComponent;
import com.b2international.snowowl.core.api.index.IIndexEntry;
import com.b2international.snowowl.datastore.index.mapping.Mappings;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedMappings;

import bak.pcj.LongCollection;

/**
 * A transfer object representing a SNOMED CT concept.
 */
public class SnomedConceptIndexEntry extends SnomedIndexEntry implements IComponent<String>, IIndexEntry, Serializable {

	private static final long serialVersionUID = -824286402410205210L;

	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(final Document doc) {
		return builder()
				.id(SnomedMappings.id().getValueAsString(doc))
				.moduleId(SnomedMappings.module().getValueAsString(doc))
				.storageKey(Mappings.storageKey().getValue(doc))
				.active(BooleanUtils.valueOf(SnomedMappings.active().getValue(doc).intValue())) 
				.released(BooleanUtils.valueOf(SnomedMappings.released().getValue(doc).intValue()))
				.effectiveTimeLong(SnomedMappings.effectiveTime().getValue(doc))
				.iconId(Mappings.iconId().getValue(doc))
				.primitive(BooleanUtils.valueOf(SnomedMappings.primitive().getValue(doc).intValue()))
				.exhaustive(BooleanUtils.valueOf(SnomedMappings.exhaustive().getValue(doc).intValue()));
	}

	public static class Builder extends AbstractBuilder<Builder> {

		private String iconId;
		private boolean primitive;
		private boolean exhaustive;
		private LongCollection parents;
		private LongCollection ancestors;

		private Builder() {
			// Disallow instantiation outside static method
		}
		
		@Override
		protected Builder getSelf() {
			return this;
		}

		public Builder iconId(final String iconId) {
			this.iconId = iconId;
			return getSelf();
		}

		public Builder primitive(final boolean primitive) {
			this.primitive = primitive;
			return getSelf();
		}

		public Builder exhaustive(final boolean exhaustive) {
			this.exhaustive = exhaustive;
			return getSelf();
		}
		
		public Builder parents(final LongCollection parents) {
			this.parents = parents;
			return getSelf();
		}
		
		public Builder ancestors(final LongCollection ancestors) {
			this.ancestors = ancestors;
			return getSelf();
		}

		public SnomedConceptIndexEntry build() {
			final SnomedConceptIndexEntry entry = new SnomedConceptIndexEntry(id,
					label,
					iconId, 
					score, 
					storageKey,
					moduleId, 
					released, 
					active, 
					effectiveTimeLong, 
					primitive, 
					exhaustive);
			
			if (parents != null) {
				entry.setParents(parents);
			}
			
			if (ancestors != null) {
				entry.setAncestors(ancestors);
			}
			
			return entry;
		}
	}

	private final boolean primitive;
	private final boolean exhaustive;
	private LongCollection parents;
	private LongCollection ancestors;

	protected SnomedConceptIndexEntry(final String id,
			final String label,
			final String iconId, 
			final float score, 
			final long storageKey, 
			final String moduleId,
			final boolean released,
			final boolean active,
			final long effectiveTimeLong,
			final boolean primitive,
			final boolean exhaustive) {

		super(id, 
				label,
				iconId,
				score, 
				storageKey, 
				moduleId, 
				released, 
				active,
				effectiveTimeLong);

		this.primitive = primitive;
		this.exhaustive = exhaustive;
	}

	/**
	 * @return {@code true} if the concept definition status is 900000000000074008 (primitive), {@code false} otherwise
	 */
	public boolean isPrimitive() {
		return primitive;
	}

	/**
	 * @return {@code true} if the concept subclass definition status is exhaustive, {@code false} otherwise
	 */
	public boolean isExhaustive() {
		return exhaustive;
	}
	
	private void setParents(LongCollection parents) {
		this.parents = parents;
	}
	
	public LongCollection getParents() {
		return parents;
	}
	
	private void setAncestors(LongCollection ancestors) {
		this.ancestors = ancestors;
	}
	
	public LongCollection getAncestors() {
		return ancestors;
	}

	@Override
	public String toString() {
		return toStringHelper()
				.add("primitive", primitive)
				.add("exhaustive", exhaustive)
				.toString();
	}
}

