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
package com.b2international.snowowl.snomed.core.domain;

import java.time.LocalDate;
import java.util.regex.Pattern;

import com.b2international.commons.VerhoeffCheck;
import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.domain.BaseComponent;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.terminology.TerminologyRegistry;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

/**
 * Holds common properties of SNOMED CT components.
 * @since 4.0
 */
public abstract class SnomedComponent extends BaseComponent {

	private static final Pattern PATTERN = Pattern.compile("^\\d*$");
	
	private static final long serialVersionUID = 1L;

	/**
	 * @since 7.4
	 */
	public static abstract class Expand {
		public static final String MODULE = "module";
	}

	/**
	 * @since 6.16
	 */
	public static abstract class Fields extends BaseComponent.Fields {
		
		public static final String ACTIVE = SnomedRf2Headers.FIELD_ACTIVE;
		public static final String EFFECTIVE_TIME = SnomedRf2Headers.FIELD_EFFECTIVE_TIME;
		public static final String MODULE_ID = SnomedRf2Headers.FIELD_MODULE_ID;
		public static final String ICON_ID = "iconId";
		
	} 
	
	private Boolean active;
	private LocalDate effectiveTime;
	private String moduleId;
	private String iconId;
	private Float score;
	
	private SnomedConcept module;

	/**
	 * Returns the component's current status as a boolean value.
	 *  
	 * @return {@code true} if the component is active, {@code false} if it is inactive
	 */
	public Boolean isActive() {
		return active;
	}

	/**
	 * Returns the date at which the current state of the component becomes effective.
	 * 
	 * @return the component's effective time
	 */
	@JsonFormat(shape=Shape.STRING, pattern=DateFormats.SHORT, timezone="UTC")
	public LocalDate getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * Returns the containing module's concept identifier.
	 * 
	 * @return the module identifier for the component
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * @beta - this method is subject to changes or even removal in future releases.  
	 * @return - the icon ID associated with this component
	 */
	public String getIconId() {
		return iconId;
	}

	/**
	 * @beta - this method is subject to changes or even removal in future releases.
	 * @return - the score associated with this component if it's a match in a query, can be <code>null</code>
	 */
	@JsonIgnore
	public Float getScore() {
		return score;
	}
	
	/**
	 * @return the expanded module of a SNOMED CT Concept
	 */
	public SnomedConcept getModule() {
		return module;
	}

	public void setActive(final Boolean active) {
		this.active = active;
	}

	@JsonFormat(shape=Shape.STRING, pattern = DateFormats.SHORT, timezone="UTC")
	public void setEffectiveTime(final LocalDate effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public void setModuleId(final String moduleId) {
		this.moduleId = moduleId;
	}
	
	public void setIconId(String iconId) {
		this.iconId = iconId;
	}
	
	public void setScore(Float score) {
		this.score = score;
	}
	
	public void setModule(SnomedConcept module) {
		this.module = module;
	}
	
	/**
	 * Creates an update {@link Request} to update the component to the state represented by this instance.
	 * @return
	 */
	public abstract Request<TransactionContext, Boolean> toUpdateRequest();
	
	/**
	 * Creates a create {@link Request} to create the component represented by this instance.
	 * @return
	 */
	public final Request<TransactionContext, String> toCreateRequest() {
		return toCreateRequest(null);
	}
	
	/**
	 * Creates a create {@link Request} to create the component represented by this instance.
	 * @param containerId the container component identifier to enforce attachment to it, may be <code>null</code> 
	 * @return
	 */
	public abstract Request<TransactionContext, String> toCreateRequest(String containerId);

	public static String getType(String componentId) {
		String componentType = getTypeSafe(componentId);
		if (TerminologyRegistry.UNKNOWN_COMPONENT_TYPE.equals(componentType)) {
			throw new IllegalArgumentException("'" + componentId + "' component type is unknown.");
		} else {
			return componentType;
		}
	}
	
	public static String getTypeSafe(String componentId) {
		if (Strings.isNullOrEmpty(componentId)) {
			return TerminologyRegistry.UNKNOWN_COMPONENT_TYPE;
		}
		
		if (!PATTERN.matcher(componentId).matches() || componentId.length() < 6 || componentId.length() > 18) {
			return TerminologyRegistry.UNKNOWN_COMPONENT_TYPE;
		}
	
		if (!VerhoeffCheck.validateLastChecksumDigit(componentId)) {
			return TerminologyRegistry.UNKNOWN_COMPONENT_TYPE;
		}
	
		switch (componentId.charAt(componentId.length() - 2)) {
			case '0': return SnomedConcept.TYPE;
			case '1': return SnomedDescription.TYPE;
			case '2': return SnomedRelationship.TYPE;
			default: return TerminologyRegistry.UNKNOWN_COMPONENT_TYPE;
		}
	}
	
}