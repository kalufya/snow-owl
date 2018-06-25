/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.index.constraint;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 6.5
 */
public final class RelationshipDefinitionFragment extends ConceptSetDefinitionFragment {

	private final String typeId;
	private final String destinationId;

	@JsonCreator
	public RelationshipDefinitionFragment(
			@JsonProperty("uuid") final String uuid, 
			@JsonProperty("active") final boolean active, 
			@JsonProperty("effectiveTime") final long effectiveTime, 
			@JsonProperty("author") final String author,
			@JsonProperty("typeId") final String typeId,
			@JsonProperty("destinationId") final String destinationId) {

		super(uuid, active, effectiveTime, author);

		this.typeId = typeId;
		this.destinationId = destinationId;
	}

	public String getTypeId() {
		return typeId;
	}

	public String getDestinationId() {
		return destinationId;
	}
	
	@Override
	public int hashCode() {
		return 31 * super.hashCode() + Objects.hash(typeId, destinationId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		
		final RelationshipDefinitionFragment other = (RelationshipDefinitionFragment) obj;
		
		return Objects.equals(typeId, other.typeId)
				&& Objects.equals(destinationId, other.destinationId);
	}
}
