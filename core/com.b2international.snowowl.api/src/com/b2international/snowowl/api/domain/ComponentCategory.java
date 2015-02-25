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
package com.b2international.snowowl.api.domain;

import com.b2international.commons.StringUtils;

/**
 * Enumerates high-level component categories which can exist in any code system.
 */
public enum ComponentCategory {

	/** 
	 * A category for ideas, physical objects or events. 
	 */
	CONCEPT,

	/** 
	 * A label or other textual representation for a concept. 
	 */
	DESCRIPTION,

	/** 
	 * A typed connection between two concepts. 
	 */
	RELATIONSHIP,

	/** 
	 * A scalar value or measurement associated with another component. 
	 */
	CONCRETE_DOMAIN,

	/** 
	 * A set of unique set members. 
	 */
	SET,

	/** 
	 * Points to another component, indicating that it is part of the member's parent set. 
	 */ 
	SET_MEMBER,

	/** 
	 * A set of unique map members. 
	 */
	MAP,

	/**
	 * Points to a source and a target component, indicating that a mapping exists between the two in the context of the
	 * member's parent map.
	 */
	MAP_MEMBER;

	/**
	 * Returns the human-readable name of this category, obtained by converting the original name of the enum value to lower case,
	 * changing underscore characters to whitespace separators, and changing the first letter to upper case.
	 * 
	 * @return the display name of this category
	 */
	public String getDisplayName() {
		return StringUtils.capitalizeFirstLetter(name().replace('_', ' ').toLowerCase());
	}
}
