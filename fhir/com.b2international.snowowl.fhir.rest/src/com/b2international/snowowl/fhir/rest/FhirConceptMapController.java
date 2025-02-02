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
package com.b2international.snowowl.fhir.rest;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2international.commons.collections.Collections3;
import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.fhir.core.model.Bundle;
import com.b2international.snowowl.fhir.core.model.conceptmap.ConceptMap;
import com.b2international.snowowl.fhir.core.request.FhirRequests;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * A concept map defines a mapping from a set of concepts defined in a code system to one or more concepts defined in other code systems. 
 * Mappings are one way - from the source to the destination.
 *  
 * @see <a href="https://www.hl7.org/fhir/conceptmap.html">ConceptMap</a>
 * @see <a href="https://www.hl7.org/fhir/conceptmap-operations.html">ConceptMap</a>
 * @since 7.0
 */
@Tag(description = "ConceptMap", name = "ConceptMap")
@RestController
@RequestMapping(value="/ConceptMap", produces = { AbstractFhirResourceController.APPLICATION_FHIR_JSON })
public class FhirConceptMapController extends AbstractFhirResourceController<ConceptMap> {
	
	@Override
	protected Class<ConceptMap> getModelClass() {
		return ConceptMap.class;
	}
	
	/**
	 * ConceptMaps
	 * @param parameters - request parameters
	 * @return bundle of concept maps
	 */
	@Operation(
		summary="Retrieve all concept maps",
		description="Returns a collection of the supported concept maps."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "OK")
	})
	@GetMapping
	public Promise<Bundle> getConceptMaps(@ParameterObject FhirConceptMapSearchParameters params) {
		return FhirRequests.conceptMaps().prepareSearch()
				.filterByIds(asList(params.get_id()))
				.filterByNames(asList(params.getName()))
				.filterByTitle(params.getTitle())
				.filterByLastUpdated(params.get_lastUpdated())
				.filterByUrls(Collections3.intersection(params.getUrl(), params.getSystem())) // values defined in both url and system match the same field, compute intersection to simulate ES behavior here
				.filterByVersions(params.getVersion())
				.setSearchAfter(params.get_after())
				.setCount(params.get_count())
				// XXX _summary=count may override the default _count=10 value, so order of method calls is important here
				.setSummary(params.get_summary())
				.setElements(params.get_elements())
				.sortByFields(params.get_sort())
				.buildAsync()
				.execute(getBus());
	}
	
	/**
	 * HTTP Get for retrieving a concept map by its concept map id
	 * @param conceptMapId
	 * @param parameters - request parameters
	 * @return @link {@link ConceptMap}
	 */
	@Operation(
		summary="Retrieve the concept map by id",
		description="Retrieves the concept map specified by its logical id."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "404", description = "Concept map not found")
	})
	@GetMapping(value="/{id:**}")
	public Promise<ConceptMap> getConceptMap(
			@Parameter(description = "The identifier of the ConceptMap resource")
			@PathVariable(value = "id") 
			final String id,
	
			@ParameterObject
			final FhirResourceSelectors selectors) {
		return FhirRequests.conceptMaps().prepareGet(id)
				.setElements(selectors.get_elements())
				.setSummary(selectors.get_summary())
				.buildAsync()
				.execute(getBus());
	}
	
//	/**
//	 * HTTP Get request to translate a code that belongs to a {@link ConceptMap} specified by its ID.
//	 * @param conceptMapId
//	 * @return translation of the code
//	 */
//	@Operation(
//		summary="Translate a code based on a specific Concept Map",
//		description="Translate a code from one value set to another, based on the existing value set and specific concept map."
//	)
//	@ApiResponses({
//		@ApiResponse(responseCode = "200", description = "OK"),
//		@ApiResponse(responseCode = "400", description = "Bad request"),
//		@ApiResponse(responseCode = "404", description = "Concept map not found")
//	})
//	@GetMapping(value="/{conceptMapId:**}/$translate")
//	public Parameters.Fhir translate(
//		@Parameter(description = "The id of the Concept Map to base the translation on") @PathVariable("conceptMapId") String conceptMapId,
//		@Parameter(description = "The code to translate") @RequestParam(value="code") final String code,
//		@Parameter(description = "The code system's uri") @RequestParam(value="system") final String system,
//		@Parameter(description = "The code system's version") @RequestParam(value="version") final Optional<String> version,
//		@Parameter(description = "The source value set") @RequestParam(value="source") final Optional<String> source,
//		@Parameter(description = "Value set in which a translation is sought") @RequestParam(value="target") final Optional<String> target,
//		@Parameter(description = "Target code system") @RequestParam(value="targetsystem") final Optional<String> targetSystem,
//		@Parameter(description = "If true, the mapping is reversed") @RequestParam(value="reverse") final Optional<Boolean> isReverse) {
//		
//		//validation is triggered by builder.build()
//		Builder builder = TranslateRequest.builder()
//			.code(code)
//			.system(system);
//		
//		if (version.isPresent()) {
//			builder.version(version.get());
//		}
//		
//		if(source.isPresent()) {
//			builder.source(source.get());
//		}
//		
//		if(target.isPresent()) {
//			builder.target(target.get());
//		}
//		
//		if(targetSystem.isPresent()) {
//			builder.targetSystem(targetSystem.get());
//		}
//		
//		if(isReverse.isPresent()) {
//			builder.isReverse(isReverse.get());
//		}
//		
//		return toResponse(doTranslate(conceptMapId, builder.build()));
//	}
	
//	/**
//	 * HTTP POST request to translate a code that belongs to a {@link ConceptMap} specified by its ID.
//	 * @param conceptMapId
//	 * @return translation of the code
//	 */
//	@Operation(
//		summary="Translate a code based on a specific Concept Map",
//		description="Translate a code from one value set to another, based on the existing value set and specific concept map."
//	)
//	@ApiResponses({
//		@ApiResponse(responseCode = "200", description = "OK"),
//		@ApiResponse(responseCode = "404", description = "Not found"),
//		@ApiResponse(responseCode = "400", description = "Bad request")
//	})
//	@PostMapping(value="/{conceptMapId:**}/$translate", consumes = AbstractFhirResourceController.APPLICATION_FHIR_JSON)
//	public Parameters.Fhir translate(
//		@Parameter(description = "The id of the conceptMap to base the translation on") 
//		@PathVariable("conceptMapId") 
//		String conceptMapId,
//		
//		@Parameter(description = "The translate request parameters")
//		@RequestBody 
//		Parameters.Fhir body) {
//		
//		//validation is triggered by builder.build()
//		final TranslateRequest request = toRequest(body, TranslateRequest.class);
//		TranslateResult result = doTranslate(conceptMapId, request);
//		return toResponse(result);
//	}

//	/**
//	 * HTTP Get request to translate a code that could belongs to any {@link ConceptMap} in the system.
//	 * @return translation of the code
//	 */
//	@Operation(
//		summary="Translate a code",
//		description="Translate a code from one value set to another, based on the existing value set and concept maps resources, and/or other additional knowledge available to the server."
//	)
//	@ApiResponses({
//		@ApiResponse(responseCode = "200", description = "OK"),
//		@ApiResponse(responseCode = "400", description = "Bad request"),
//		@ApiResponse(responseCode = "404", description = "Concept map not found")
//	})
//	@RequestMapping(value="/$translate", method=RequestMethod.GET)
//	public Parameters.Fhir translate(
//		@Parameter(description = "The code to translate") @RequestParam(value="code") final String code,
//		@Parameter(description = "The code system's uri") @RequestParam(value="system") final String system,
//		@Parameter(description = "The code system's version, if null latest is used") @RequestParam(value="version") final Optional<String> version,
//		@Parameter(description = "The source value set") @RequestParam(value="source") final Optional<String> source,
//		@Parameter(description = "Value set in which a translation is sought") @RequestParam(value="target") final Optional<String> target,
//		@Parameter(description = "Target code system") @RequestParam(value="targetsystem") final Optional<String> targetSystem,
//		@Parameter(description = "If true, the mapping is reversed") @RequestParam(value="reverse") final Optional<Boolean> isReverse) {
//		
//		//validation is triggered by builder.build()
//		Builder builder = TranslateRequest.builder()
//			.code(code)
//			.system(system);
//		
//		if (version.isPresent()) {
//			builder.version(version.get());
//		}
//			
//		if(source.isPresent()) {
//			builder.source(source.get());
//		}
//		
//		if(target.isPresent()) {
//			builder.target(target.get());
//		}
//		
//		if(targetSystem.isPresent()) {
//			builder.targetSystem(targetSystem.get());
//		}
//		
//		if(isReverse.isPresent()) {
//			builder.isReverse(isReverse.get());
//		}
//		
//		return toResponse(doTranslate(builder.build()));
//	}
	
//	/**
//	 * HTTP POST request to translate a code that belongs to any {@link ConceptMap} in the system.
//	 * @param body - {@link TranslateRequest}}
//	 * @return translation of the code
//	 */
//	@Operation(
//		summary="Translate a code",
//		description="Translate a code from one value set to another, based on the existing value set and concept map resources."
//	)
//	@ApiResponses({
//		@ApiResponse(responseCode = "200", description = "OK"),
//		@ApiResponse(responseCode = "404", description = "Not found"),
//		@ApiResponse(responseCode = "400", description = "Bad request")
//	})
//	@RequestMapping(value="/$translate", method=RequestMethod.POST, consumes = AbstractFhirResourceController.APPLICATION_FHIR_JSON)
//	public Parameters.Fhir translate(
//			@Parameter(description = "The translate request parameters")
//			@RequestBody 
//			Parameters.Fhir body) {
//		
//		//validation is triggered by builder.build()
//		final TranslateRequest request = toRequest(body, TranslateRequest.class);
//		TranslateResult result = doTranslate(request);
//		return toResponse(result);
//	}
//	
//	/*
//	 * Specific Concept Map based translation
//	 */
//	private TranslateResult doTranslate(String conceptMapId, TranslateRequest translateRequest) {
//		
//		ComponentURI componentURI = ComponentURI.of(conceptMapId);
//		IConceptMapApiProvider conceptMapApiProvider = conceptMapProviderRegistry.getConceptMapProvider(getBus(), locales, componentURI);
//		TranslateResult translateResult = conceptMapApiProvider.translate(componentURI, translateRequest);
//		return translateResult;
//	}
	
//	/*
//	 * Translation from ANY Concept Maps
//	 */
//	private TranslateResult doTranslate(TranslateRequest translateRequest) {
//		
//		TranslateResult.Builder resultBuilder = TranslateResult.builder();
//		
//		int totalMatch = 0;
//		
//		Collection<IConceptMapApiProvider> providers = conceptMapProviderRegistry.getProviders(getBus(), locales);
//		
//		for (IConceptMapApiProvider provider : providers) {
//			Collection<Match> matches = provider.translate(translateRequest);
//			totalMatch = totalMatch + matches.size();
//			resultBuilder.addMatches(matches);
//		}
//		
//		return resultBuilder
//			.message(totalMatch + " match(es).")
//			.build();
//	}
	
}
