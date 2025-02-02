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
package com.b2international.snowowl.snomed.core.domain.refset;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.core.terminology.TerminologyComponent;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.*;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedOWLRelationshipDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.base.Strings;

/**
 * Represents a SNOMED&nbsp;CT Reference Set Member.
 * <br>
 * Reference sets returned by search requests are populated based on the expand parameters passed into the {@link BaseResourceRequestBuilder#setExpand(String)}
 * methods. The expand parameters can be nested allowing a fine control for the details returned in the resultset.  
 * 
 * The supported expand parameters are:
 * <p>
 * <ul>
 * <li>{@code targetComponent()} - returns the target component of the member</li>
 * <li>{@code referencedComponent()} - returns the referenced component of the member</li>
 * </ul>
 * 
 * Expand parameters can be nested to further expand or filter the details returned. For example:
 * <p>
 * {@code referencedComponent(expand(pt()))}, would return the preferred term of a <i>Concept</i> type referenced component.
 * 
 * @see SnomedConcept
 * @see SnomedDescription
 * @see SnomedRelationship
 * @see SnomedReferenceSet
 * 
 * @since 4.5
 */
@TerminologyComponent(
	name = "SNOMED CT Reference Set Member",
	componentCategory = ComponentCategory.SET_MEMBER,
	docType = SnomedRefSetMemberIndexEntry.class,
	allowedAsMapTarget = false
)
public final class SnomedReferenceSetMember extends SnomedComponent {

	private static final long serialVersionUID = -7471488952871955209L;

	public static final String TYPE = "member";
	
	/**
	 * Enumerates expandable property keys.
	 * 
	 * @since 7.0
	 */
	public static final class Expand {
		public static final String REFERENCED_COMPONENT = "referencedComponent";
		public static final String TARGET_COMPONENT = "targetComponent";
	}
	
	public static final Function<SnomedReferenceSetMember, String> GET_REFERENCED_COMPONENT_ID = (member) -> member.getReferencedComponent().getId();
	
	/**
	 * @since 6.16 
	 */
	public static final class Fields extends SnomedComponent.Fields {
		
		public static final String TYPE = "type";
		public static final String REFERENCED_COMPONENT_ID = SnomedRf2Headers.FIELD_REFERENCED_COMPONENT_ID;
		public static final String REFSET_ID = SnomedRf2Headers.FIELD_REFSET_ID;
		
		public static final Set<String> ALL = Set.of(
				// RF2 fields
				ID,
				ACTIVE,
				EFFECTIVE_TIME,
				MODULE_ID,
				REFSET_ID,
				REFERENCED_COMPONENT_ID,
				// special fieldss
				TYPE,
				RELEASED);
		
	}
	
	private SnomedRefSetType type;
	private SnomedCoreComponent referencedComponent;
	private String refsetId;
	private Map<String, Object> properties = newHashMap();
	private List<SnomedOWLRelationshipDocument> equivalentOWLRelationships;
	private List<SnomedOWLRelationshipDocument> classOWLRelationships;
	private List<SnomedOWLRelationshipDocument> gciOWLRelationships;

	@Override
	public String getComponentType() {
		return SnomedReferenceSetMember.TYPE;
	}
	
	/**
	 * @return the containing reference set's type
	 */
	public SnomedRefSetType type() {
		return type;
	}

	/**
	 * Returns the component referenced by this SNOMED CT Reference Set Member. It includes only the SNOMED CT ID property by default, see
	 * {@link SnomedCoreComponent#getId()}.
	 * 
	 * @return
	 */
	public SnomedCoreComponent getReferencedComponent() {
		return referencedComponent;
	}
	
	/**
	 * @return the referenced component's identifier or <code>null</code> if this is a partially loaded member.
	 */
	public String getReferencedComponentId() {
		return referencedComponent == null ? null : referencedComponent.getId();
	}

	/**
	 * Returns the identifier of the SNOMED CT Reference Set this SNOMED CT Reference Set Member belongs to.
	 * 
	 * @return
	 */
	public String getRefsetId() {
		return refsetId;
	}

	/**
	 * Returns special properties of the SNOMED CT Reference Set or an empty {@link Map} if none found.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@JsonAnyGetter
	private Map<String, Object> getPropertiesJson() {
		HashMap<String, Object> jsonMap = newHashMap(properties);
		jsonMap.computeIfPresent(SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME, (k,v) -> EffectiveTimes.format(v, DateFormats.SHORT, ""));
		jsonMap.computeIfPresent(SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME, (k,v) -> EffectiveTimes.format(v, DateFormats.SHORT, ""));
		return jsonMap;
	}
	
	public void setType(SnomedRefSetType type) {
		this.type = type;
	}
	
	public void setReferencedComponent(SnomedCoreComponent referencedComponent) {
		this.referencedComponent = referencedComponent;
	}
	
	public void setRefsetId(String refsetId) {
		this.refsetId = refsetId;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public List<SnomedOWLRelationshipDocument> getEquivalentOWLRelationships() {
		return equivalentOWLRelationships;
	}
	
	public void setEquivalentOWLRelationships(List<SnomedOWLRelationshipDocument> equivalentOWLRelationships) {
		this.equivalentOWLRelationships = equivalentOWLRelationships;
	}
	
	public List<SnomedOWLRelationshipDocument> getClassOWLRelationships() {
		return classOWLRelationships;
	}
	
	public void setClassOWLRelationships(List<SnomedOWLRelationshipDocument> classOWLRelationships) {
		this.classOWLRelationships = classOWLRelationships;
	}
	
	public List<SnomedOWLRelationshipDocument> getGciOWLRelationships() {
		return gciOWLRelationships;
	}
	
	public void setGciOWLRelationships(List<SnomedOWLRelationshipDocument> gciOWLRelationships) {
		this.gciOWLRelationships = gciOWLRelationships;
	}
	
	@JsonAnySetter
	private void setPropertiesJson(String key, Object value) {
		switch (key) {
			case SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME: //$FALL-THROUGH$
			case SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME:
				properties.put(key, Strings.isNullOrEmpty((String) value) ? null : EffectiveTimes.parse((String) value, DateFormats.SHORT));
				break;
			default:
				properties.put(key, value);
		}
	}
	
	@Override
	public Request<TransactionContext, String> toCreateRequest(String containerId) {
		return SnomedRequests.prepareNewMember()
				.setId(getId())
				.setActive(isActive())
				.setReferencedComponentId(containerId)
				.setRefsetId(getRefsetId())
				.setModuleId(getModuleId())
				.setProperties(getProperties())
				.build();
	}
	
	@Override
	public Request<TransactionContext, Boolean> toUpdateRequest() {
		final Map<String, Object> changes = newHashMap(getProperties());
		changes.put(SnomedRf2Headers.FIELD_ACTIVE, isActive());
		changes.put(SnomedRf2Headers.FIELD_MODULE_ID, getModuleId());
		return SnomedRequests.prepareUpdateMember(getId())
				.setSource(changes)
				.build();
	}

	@Override
	public String toString() {
		return String.format(
				"SnomedReferenceSetMember [type=%s, referencedComponent=%s, refsetId=%s, properties=%s, equivalentOWLRelationships=%s, classOWLRelationships=%s, gciOWLRelationships=%s]",
				type, referencedComponent, refsetId, properties, equivalentOWLRelationships, classOWLRelationships, gciOWLRelationships);
	}
	
	
	
}
