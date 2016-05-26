/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.core.store;

import java.util.Map;

import com.b2international.snowowl.datastore.codesystem.BaseCodeSystemBuilder;
import com.b2international.snowowl.snomed.SnomedFactory;
import com.b2international.snowowl.snomed.SnomedRelease;
import com.b2international.snowowl.snomed.SnomedReleaseType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;

/**
 * @since 4.7
 */
public class SnomedReleaseBuilder extends BaseCodeSystemBuilder<SnomedReleaseBuilder, SnomedRelease> {

	private static final String KEY_BASE_CODE_SYSTEM_OID = "baseCodeSystemOID";
	private static final String KEY_RELEASE_TYPE = "releaseType";

	private String baseCodeSystemOid;
	private SnomedReleaseType type;

	@Override
	public SnomedReleaseBuilder init(Map<String, String> valueMap) {
		super.init(valueMap);
		return withExtension(valueMap);
	}

	public final SnomedReleaseBuilder withBaseCodeSystemOid(final String baseCodeSystemOid) {
		this.baseCodeSystemOid = baseCodeSystemOid;
		return getSelf();
	}

	public final SnomedReleaseBuilder withType(final SnomedReleaseType type) {
		this.type = type;
		return getSelf();
	}

	/**
	 * Returns a release builder with {@link SnomedReleaseType} set based on a
	 * case-sensitive name. If enum not found by name, returns null for the
	 * {@link SnomedReleaseType}
	 * 
	 * @param typeName
	 *            case-sensitive name of the enum
	 * @return
	 */
	public final SnomedReleaseBuilder withType(final String typeName) {
		this.type = SnomedReleaseType.getByName(typeName);
		return getSelf();
	}

	@Override
	public SnomedReleaseBuilder withExtension(final Map<String, String> valueMap) {
		this.baseCodeSystemOid = valueMap.get(KEY_BASE_CODE_SYSTEM_OID);

		if (valueMap.containsKey(KEY_RELEASE_TYPE)) {
			SnomedReleaseType typeFromMap = SnomedReleaseType.getByName(valueMap.get(KEY_RELEASE_TYPE));
			if (typeFromMap != null) {
				this.type = typeFromMap;
			}
		}

		return getSelf();
	}

	@Override
	public String getRepositoryUuid() {
		return SnomedDatastoreActivator.REPOSITORY_UUID;
	}

	@Override
	public SnomedRelease create() {
		return SnomedFactory.eINSTANCE.createSnomedRelease();
	}

	@Override
	public SnomedRelease build() {
		final SnomedRelease snomedRelease = super.build();
		snomedRelease.setBaseCodeSystemOID(baseCodeSystemOid);
		snomedRelease.setReleaseType(type);

		return snomedRelease;
	}

}
