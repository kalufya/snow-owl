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
package com.b2international.snowowl.fhir.core.model.valueset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.validation.Valid;

import com.b2international.snowowl.fhir.core.model.ContactDetail;
import com.b2international.snowowl.fhir.core.model.Meta;
import com.b2international.snowowl.fhir.core.model.MetadataResource;
import com.b2international.snowowl.fhir.core.model.dt.*;
import com.b2international.snowowl.fhir.core.model.usagecontext.UsageContext;
import com.b2international.snowowl.fhir.core.model.valueset.expansion.Expansion;
import com.b2international.snowowl.fhir.core.search.Mandatory;
import com.b2international.snowowl.fhir.core.search.Summary;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * A value set contains a set of codes from those defined by one or more code systems to specify which codes can be used in a particular context.
 * 
 * Value sets aspects:
 * <ul>
 * <li>.compose: A definition of which codes are intended to be in the value set ("intension")
 * <li>.expansion: The list of codes that are actually in the value set under a given set of conditions ("extension")
 * </ul>
 * 
 * @see <a href="https://www.hl7.org/fhir/valueset.html">FHIR:ValueSet</a>
 * @since 6.3
 */
@JsonDeserialize(builder = ValueSet.Builder.class, using = JsonDeserializer.None.class)
public class ValueSet extends MetadataResource {
	
	private static final long serialVersionUID = 1L;
	
	public static final String RESOURCE_TYPE_VALUE_SET = "ValueSet";

	//FHIR header "resourceType" : "ValueSet",
	@Mandatory
	@JsonProperty
	private final String resourceType;
	
	@Summary
	@JsonProperty("identifier")
	@JsonInclude(value = Include.NON_EMPTY)
	private Collection<Identifier> identifiers;
	
	@Summary
	@JsonProperty
	private final Boolean immutable;
	
	//at least one compose or expansion should exist
	@Valid
	private final Compose compose;
	
	@Valid
	@JsonProperty
	private final Expansion expansion;
	
	@SuppressWarnings("rawtypes")
	public ValueSet(Id id, final Meta meta, final Uri impliciteRules, Code language, Narrative text,
			
			final Uri url, final String version, final String name, final String title, Code status, 
			final Boolean experimental, final Date date, String publisher, 
			final Collection<ContactDetail> contacts, String description, final Collection<UsageContext> usageContexts,
			final Collection<CodeableConcept> jurisdictions, 
			final String resourceType, final Collection<Identifier> identifiers, 
			final Boolean immutable, final String purpose, final String copyright, final String toolingId,
			final Compose compose, final Expansion expansion) {
		
		super(id, meta, impliciteRules, language, text, url, version, name, title, status, experimental, 
				date, publisher, contacts, description, usageContexts, jurisdictions, purpose, copyright, toolingId);
		
		this.resourceType = resourceType;
		this.identifiers = identifiers;
		this.immutable = immutable;
		this.compose = compose;
		this.expansion = expansion;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public Boolean getImmutable() {
		return immutable;
	}
	
	public Collection<Identifier> getIdentifiers() {
		return identifiers;
	}
	
	public Compose getCompose() {
		return compose;
	}
	
	public Expansion getExpansion() {
		return expansion;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(String valueSetId) {
		return new Builder(valueSetId);
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class Builder extends MetadataResource.Builder<Builder, ValueSet> {

		private String resourceType = RESOURCE_TYPE_VALUE_SET;
		
		private Collection<Identifier> identifiers;
		private Boolean immutable;
		private Compose compose;
		private Expansion expansion;

		@JsonCreator
		private Builder() {
		}
		
		private Builder(String valueSetId) {
			super(valueSetId);
		}
		
		public Builder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return getSelf();
		}
		
		@JsonProperty("identifier")
		@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		public Builder identifiers(final Collection<Identifier> identifers) {
			this.identifiers = identifers;
			return getSelf();
		}
		
		public Builder addIdentifier(Identifier identifier) {
			if (identifiers == null) {
				identifiers = new ArrayList<>();
			}
			identifiers.add(identifier);
			return getSelf();
		}
		
		public Builder immutable(Boolean immutable) {
			this.immutable = immutable;
			return getSelf();
		}
		
		public Builder compose(final Compose compose) {
			this.compose = compose;
			return getSelf();
		}
		
		public Builder expansion(Expansion expansion) {
			this.expansion = expansion;
			return getSelf();
		}
		
		@Override
		protected Builder getSelf() {
			return this;
		}
		
		@Override
		protected ValueSet doBuild() {
			
			//cross field validation
			//if (composeParts.isEmpty() && expansion == null) {
			//	throw new FhirException("No 'compose' or 'expansion' fields are defined for the value set.", "ValueSet");
			//}
			
			return new ValueSet(id, meta, implicitRules, language, text, url, version, name,  title, status, experimental, 
					date, publisher, contacts, description, usageContexts,	jurisdictions, 
					
					resourceType, identifiers, immutable, purpose, copyright, toolingId, compose, expansion);
		}
	}
		
}
