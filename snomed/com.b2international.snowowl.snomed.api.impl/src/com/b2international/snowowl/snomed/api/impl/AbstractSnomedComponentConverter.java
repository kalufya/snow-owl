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
package com.b2international.snowowl.snomed.api.impl;

import java.util.Date;

import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.snomed.api.domain.ISnomedComponent;
import com.b2international.snowowl.snomed.datastore.index.SnomedIndexEntry;
import com.b2international.snowowl.snomed.datastore.services.AbstractSnomedRefSetMembershipLookupService;
import com.google.common.base.Function;

public abstract class AbstractSnomedComponentConverter<F extends SnomedIndexEntry, T extends ISnomedComponent> implements Function<F, T> {

	private final AbstractSnomedRefSetMembershipLookupService refSetMembershipLookupService;

	public AbstractSnomedComponentConverter(AbstractSnomedRefSetMembershipLookupService refSetMembershipLookupService) {
		this.refSetMembershipLookupService = refSetMembershipLookupService;
	}

	protected final Date toEffectiveTime(final long effectiveTimeAsLong) {
		return EffectiveTimes.toDate(effectiveTimeAsLong);
	}

	protected final AbstractSnomedRefSetMembershipLookupService getRefSetMembershipLookupService() {
		return refSetMembershipLookupService;
	}

}