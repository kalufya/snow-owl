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
package com.b2international.snowowl.snomed.core.rest;

import static com.b2international.snowowl.test.commons.rest.RestExtensions.COMMA_JOINER;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.JSON_UTF8;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.hamcrest.Matchers;

import com.b2international.snowowl.core.ResourceURI;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.snomed.core.domain.AssociationTarget;
import com.b2international.snowowl.snomed.core.domain.InactivationProperties;
import com.google.common.collect.ImmutableMap;

import io.restassured.response.ValidatableResponse;

/**
 * A set of assert methods related to manipulation of components through the REST API.
 *
 * @since 2.0
 */
public abstract class SnomedComponentRestRequests {

	public static ValidatableResponse assertInactivation(final IBranchPath branchPath, final String componentId, InactivationProperties inactivationProperties) {
		return assertInactivation(branchPath, componentId, inactivationProperties, null);
	}
	
	public static ValidatableResponse assertInactivation(final IBranchPath branchPath, final String componentId, InactivationProperties inactivationProperties, final String defaultModuleId) {
		ImmutableMap.Builder<String, Object> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationProperties", inactivationProperties)
				.put("commitComment", "Inactivated concept");
		
		if (defaultModuleId != null) {
			inactivationRequestBody.put("defaultModuleId", defaultModuleId);
		}

		SnomedComponentType type = SnomedComponentType.getByComponentId(componentId);
		updateComponent(branchPath, type, componentId, inactivationRequestBody.build())
			.statusCode(204);
		
		final String[] associationReferenceSetIds = inactivationProperties.getAssociationTargets().stream().map(AssociationTarget::getReferenceSetId).toArray(length -> new String[length]);
		final String[] associationTargets = inactivationProperties.getAssociationTargets().stream().map(AssociationTarget::getTargetComponentId).toArray(length -> new String[length]);
		
		return getComponent(branchPath, type, componentId, "inactivationProperties(),members()")
			.statusCode(200)
			.body("active", equalTo(false))
			.body("inactivationProperties.inactivationIndicatorId", equalTo(inactivationProperties.getInactivationIndicatorId()))
			.body("inactivationProperties.associationTargets.referenceSetId", Matchers.containsInAnyOrder(associationReferenceSetIds))
			.body("inactivationProperties.associationTargets.targetComponentId", Matchers.containsInAnyOrder(associationTargets));
	}
	
	public static ValidatableResponse createComponent(IBranchPath branchPath, SnomedComponentType type, Map<?, ?> requestBody) {
		return createComponent(branchPath.getPath(), type, requestBody);
	}
	
	public static ValidatableResponse createComponent(String path, SnomedComponentType type, Map<?, ?> requestBody) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.contentType(JSON_UTF8)
				.body(requestBody)
				.post("/{path}/{componentType}", path, type.toLowerCasePlural())
				.then();
	}
	
	public static ValidatableResponse searchComponent(IBranchPath branchPath, SnomedComponentType type, Map<String, Object> params) {
		return searchComponent(branchPath.getPath(), type, params);
	}
	
	public static ValidatableResponse searchComponent(String path, SnomedComponentType type, Map<String, Object> params) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.contentType(JSON_UTF8)
				.queryParams(params)
				.get("/{path}/{componentType}", path, type.toLowerCasePlural())
				.then();
	}

	public static ValidatableResponse getComponent(ResourceURI resourceURI, SnomedComponentType type, String id, String... expand) {
		return getComponent(resourceURI.withoutResourceType(), type, id, expand);
	}
	
	public static ValidatableResponse getComponent(IBranchPath branchPath, SnomedComponentType type, String id, String... expand) {
		return getComponent(branchPath.getPath(), type, id, expand);
	}
	
	public static ValidatableResponse getComponent(String path, SnomedComponentType type, String id, String... expand) {
		assertNotNull(id);

		final String url;
		if (expand.length > 0) {
			url = "/{path}/{componentType}/{id}?expand=" + COMMA_JOINER.join(expand);
		} else {
			url = "/{path}/{componentType}/{id}";
		}

		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.get(url, path, type.toLowerCasePlural(), id)
				.then();
	}

	public static ValidatableResponse updateComponent(IBranchPath branchPath, SnomedComponentType type, String id, Map<?, ?> requestBody) {
		return updateComponent(branchPath.getPath(), type, id, requestBody);
	}
	
	public static ValidatableResponse updateComponent(String path, SnomedComponentType type, String id, Map<?, ?> requestBody) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.contentType(JSON_UTF8)
				.body(requestBody)
				.put("/{path}/{componentType}/{id}", path, type.toLowerCasePlural(), id)
				.then();
	}

	public static ValidatableResponse deleteComponent(IBranchPath branchPath, SnomedComponentType componentType, String id, boolean force) {
		return deleteComponent(branchPath.getPath(), componentType, id, force);
	}
	
	public static ValidatableResponse deleteComponent(String path, SnomedComponentType componentType, String id, boolean force) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.queryParam("force", force)
				.delete("/{path}/{componentType}/{id}", path, componentType.toLowerCasePlural(), id)
				.then();
	}

	private SnomedComponentRestRequests() {
		throw new UnsupportedOperationException("This class is not supposed to be instantiated.");
	}
}
