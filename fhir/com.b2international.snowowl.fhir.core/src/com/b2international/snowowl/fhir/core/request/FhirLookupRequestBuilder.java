/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.fhir.core.request;

import com.b2international.snowowl.core.events.BaseRequestBuilder;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.fhir.core.model.codesystem.LookupRequest;
import com.b2international.snowowl.fhir.core.model.codesystem.LookupResult;

/**
 * @since 7.2
 */
public final class FhirLookupRequestBuilder 
		extends BaseRequestBuilder<FhirLookupRequestBuilder, FhirCodeSystemContext, LookupResult>
		implements FhirCodeSystemRequestBuilder<LookupResult> {

	private LookupRequest lookup;
	
	public FhirLookupRequestBuilder setLookup(LookupRequest lookup) {
		this.lookup = lookup;
		return getSelf();
	}
	
	@Override
	protected Request<FhirCodeSystemContext, LookupResult> doBuild() {
		return new FhirLookupRequest(lookup);
	}

}
