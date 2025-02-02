/*
 * Copyright 2019-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.cis.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.b2international.snowowl.core.events.AsyncRequest;
import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.core.identity.User;
import com.b2international.snowowl.core.jobs.JobRequests;
import com.b2international.snowowl.core.rest.AbstractRestService;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.cis.Identifiers;
import com.b2international.snowowl.snomed.cis.domain.SctId;
import com.b2international.snowowl.snomed.cis.model.*;
import com.b2international.snowowl.snomed.cis.rest.model.BulkJob;
import com.b2international.snowowl.snomed.cis.rest.model.SctIdsList;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @since 6.18
 */
@Tag(name = "SCTIDS - Bulk Operations")
@RestController
@RequestMapping(value = "/sct/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
public class CisBulkSctIdService extends AbstractRestService {

	private Identifiers identifiers = new Identifiers();
	
	@Operation(
		summary = "Returns the SCTIDs Record.",
		description = "Returns the required SCTID Records. Bulk SCTID operations do not currently include AdditionalIds"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	@GetMapping(value = "/ids")
	public Promise<List<SctId>> getSctIdsViaGet(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The required sctids list, separated with commas (,).", required = true)
			@RequestParam(value = "sctids")
			String sctIds) {
		return getSctIds(sctIds);
	}
	
	@Operation(
		summary = "Returns the SCTIDs Record.",
		description = "Returns the required SCTID Records. Bulk SCTID operations do not currently include AdditionalIds"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	@PostMapping(value = "/ids")
	public Promise<List<SctId>> getSctIdsViaPost(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The required sctids list, separated with commas (,).", required = true)
			@RequestBody
			SctIdsList sctIds) {
		return getSctIds(sctIds.getSctids());
	}
	
	private Promise<List<SctId>> getSctIds(String sctIds) {
		String[] sctIdValues = Strings.isNullOrEmpty(sctIds) ? new String[0] : sctIds.split(",");
		return identifiers
				.prepareGet()
				.setComponentIds(ImmutableSet.copyOf(sctIdValues))
				.buildAsync()
				.execute(getBus())
				.then(ids -> ids.getItems());
	}
	
	@Operation(
		summary = "Generates new SCTIDs",
		description = "Generates new SCTIDs, based on the metadata passed in the GenerationData parameter. The first available SCTIDs will be assigned. Returns an array of SCTIDs Record with status 'Assigned'"
	)
	@PostMapping(value = "/generate")
	public Promise<BulkJob> generateBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkGenerationData generationData) {
		return runInJob("Generate new SCTIDs", identifiers
						.prepareGenerate()
						.setCategory(generationData.getComponentCategory())
						.setNamespace(generationData.getNamespaceAsString())
						.setQuantity(generationData.getQuantity())
						.buildAsync());
	}
	
	@Operation(
		summary = "Registers SCTIDs",
		description = "Registers SCTIDs already in use in an external system, based on the metadata passed in the RegistrationData parameter. Returns an array of SCTID Records with status 'Assigned'."
	)
	@PostMapping(value = "/register")
	public Promise<BulkJob> registerBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkRegistrationData registrationData) {
		return runInJob("Register SCTIDs", identifiers
						.prepareRegister()
						.setComponentIds(registrationData.getRecords().stream().map(Record::getSctid).collect(Collectors.toSet()))
						.buildAsync());
	}
	
	@Operation(
		summary = "Reserves SCTIDs",
		description = "Reserves SCTIDs for use in an external system, based on the metadata passed in the ReservationData parameter. The first available SCTIDs will be reserved. Returns an array of SCTID Records with status 'Reserved'."
	)
	@PostMapping(value = "/reserve")
	public Promise<BulkJob> reserveBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkReservationData reservationData) {
		return runInJob("Reserve SCTIDs", identifiers
						.prepareReserve()
						.setCategory(reservationData.getComponentCategory())
						.setNamespace(reservationData.getNamespaceAsString())
						.setQuantity(reservationData.getQuantity())
						.buildAsync());
	}
	
	@Operation(
		summary = "Deprecates SCTIDs",
		description = "Deprecates SCTIDs, so they will not be assigned to any component, based on the metadata passed in the DeprecationData parameter. Returns an array of SCTID Records with status 'Deprecated'."
	)
	@PutMapping(value = "/deprecate")
	public Promise<BulkJob> deprecateBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkDeprecationData deprecationData) {
		return runInJob("Deprecate SCTIDs", identifiers
						.prepareDeprecate()
						.setComponentIds(deprecationData.getComponentIds())
						.buildAsync());
	}

	@Operation(
		summary = "Release SCTIDs",
		description = "Releases SCTIDs, so they will be available to be assigned again, based on the metadata passed in the DeprecationData parameter. Returns an array SCTID Records with status 'Available'."
	)
	@PutMapping(value = "/release")
	public Promise<BulkJob> releaseBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkReleaseData releaseData) {
		return runInJob("Release SCTIDs", identifiers
						.prepareRelease()
						.setComponentIds(releaseData.getComponentIds())
						.buildAsync());
	}

	@Operation(
		summary = "Publish SCTIDs",
		description = "Sets the SCTIDs as published, based on the metadata passed in the DeprecationData parameter. Returns an array SCTID Records with status 'Published'."
	)
	@PutMapping(value = "/publish")
	public Promise<BulkJob> publishBulk(
			@Parameter(description = "The security access token.", required = true)
			@RequestParam(value = "token")
			String token,
			@Parameter(description = "The requested operation.", required = true)
			@RequestBody 
			BulkPublicationData publicationData) {
		return runInJob("Publish SCTIDs", identifiers
				.preparePublish()
				.setComponentIds(publicationData.getComponentIds())
				.buildAsync());
	}
	
	private Promise<BulkJob> runInJob(String jobDescription, AsyncRequest<?> request) {
		final IEventBus bus = getBus();
		return JobRequests.prepareSchedule()
				.setDescription(jobDescription)
				.setRequest(request)
				.setUser(User.SYSTEM.getUsername())
				.buildAsync()
				.execute(bus)
				.thenWith(id -> JobRequests.prepareGet(id).buildAsync().execute(bus))
				.then(BulkJob::fromRemoteJob);
	}
	
}
