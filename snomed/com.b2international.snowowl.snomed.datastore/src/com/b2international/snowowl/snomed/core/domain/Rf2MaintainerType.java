/*
 * Copyright 2018-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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

import static com.google.common.base.Strings.nullToEmpty;

import com.b2international.commons.exceptions.BadRequestException;

/**
 * Enumerates organization types responsible for developing and maintaining RF2 release file content.
 * 
 * @see <a href="https://confluence.ihtsdotools.org/display/DOCRELFMT/2.1.2.+Release+File+Naming+Convention#id-2.1.2.ReleaseFileNamingConvention-CountryNamespaceElement">
 * 2.1.2. Release File Naming Convention</a>
 */
public enum Rf2MaintainerType {

	/** Files are maintained and distributed by SNOMED International. */
	SNOMED_INTERNATIONAL("SNOMED International"), 
	
	/** Files are maintained and distributed by a National Release Center (NRC). */
	NRC("National release center"), 
	
	/** Files are maintained by a non-NRC extension provider. */
	OTHER_EXTENSION_PROVIDER("Other extension provider");

	private final String displayLabel;

	private Rf2MaintainerType(final String displayLabel) {
		this.displayLabel = displayLabel;
	}

	@Override
	public String toString() {
		return displayLabel;
	}
	
	public static Rf2MaintainerType getByNameIgnoreCase(String name) {
		for (final Rf2MaintainerType type : values()) {
			if (nullToEmpty(name).equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		throw new BadRequestException("Unknown RF2 refset maintainer type '%s'.", name);
	}
}
