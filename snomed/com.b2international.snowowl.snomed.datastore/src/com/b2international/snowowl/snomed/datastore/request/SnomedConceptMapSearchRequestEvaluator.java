/*
 * Copyright 2020-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.request;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.options.Options;
import com.b2international.commons.options.OptionsBuilder;
import com.b2international.snowowl.core.ResourceURI;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.codesystem.CodeSystem;
import com.b2international.snowowl.core.codesystem.CodeSystemRequests;
import com.b2international.snowowl.core.domain.Concept;
import com.b2international.snowowl.core.domain.ConceptMapMapping;
import com.b2international.snowowl.core.domain.ConceptMapMapping.Builder;
import com.b2international.snowowl.core.domain.ConceptMapMappings;
import com.b2international.snowowl.core.internal.ResourceDocument;
import com.b2international.snowowl.core.request.ConceptMapMappingSearchRequestEvaluator;
import com.b2international.snowowl.core.request.MappingCorrelation;
import com.b2international.snowowl.core.terminology.TerminologyRegistry;
import com.b2international.snowowl.core.uri.ComponentURI;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.common.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.SnomedDisplayTermType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedRefSetType;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSet;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedRefSetUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Multimaps;

/**
 * @since 7.8
 */
public final class SnomedConceptMapSearchRequestEvaluator implements ConceptMapMappingSearchRequestEvaluator {

	@Override
	public Set<ResourceURI> evaluateSearchTargetResources(ServiceProvider context, Options search) {
		if (search.containsKey(OptionKey.URI)) {
			// TODO support proper refset SNOMED URIs as well
			Set<ResourceURI> targetResources = search.getCollection(OptionKey.URI, String.class).stream()
				.filter(ComponentURI::isValid)
				.map(ComponentURI::of)
				.map(ComponentURI::resourceUri)
				.collect(Collectors.toSet());
			
			if (!targetResources.isEmpty()) {
				return targetResources;
			}
		}

		// any SNOMED CT CodeSystem can be target resource, so search all by default
		return CodeSystemRequests.prepareSearchCodeSystem()
				.all()
				.setFields(ResourceDocument.Fields.RESOURCE_TYPE, ResourceDocument.Fields.ID)
				.buildAsync()
				.execute(context)
				.stream()
				.map(CodeSystem::getResourceURI)
				.collect(Collectors.toSet());
	}

	@Override
	public ConceptMapMappings evaluate(ResourceURI uri, ServiceProvider context, Options search) {
		final String preferredDisplay = search.get(OptionKey.DISPLAY, String.class);
		final SnomedDisplayTermType snomedDisplayTermType = SnomedDisplayTermType.getEnum(preferredDisplay);

		SnomedReferenceSetMembers referenceSetMembers = fetchRefsetMembers(uri, context, search, snomedDisplayTermType);
		return toCollectionResource(referenceSetMembers, uri, context, search, snomedDisplayTermType);
	}

	private ConceptMapMappings toCollectionResource(SnomedReferenceSetMembers referenceSetMembers, ResourceURI uri, ServiceProvider context, Options search, SnomedDisplayTermType snomedDisplayTermType) {
		final Set<String> refSetsToFetch = referenceSetMembers.stream()
				.map(SnomedReferenceSetMember::getRefsetId)
				.collect(Collectors.toSet());
		
		final Map<String, SnomedConcept> refSetsById = SnomedRequests.prepareSearchConcept()
				.all()
				.filterByIds(refSetsToFetch)
				.setLocales(search.getList(OptionKey.LOCALES, ExtendedLocale.class))
				.setExpand("pt(),referenceSet()")
				.build(uri)
				.execute(context.service(IEventBus.class))
				.getSync(1, TimeUnit.MINUTES)
				.stream()
				.collect(Collectors.toMap(SnomedConcept::getId, concept -> concept));
		
		final Map<String, ComponentURI> targetComponentsByRefSetId = getTargetComponentsByRefSetId(context, refSetsById);
		
		List<ConceptMapMapping> mappings = referenceSetMembers.stream()
				.filter(m -> SnomedConcept.TYPE.equals(m.getReferencedComponent().getComponentType()))
				.map(m -> {
					return toMapping(m, uri, targetComponentsByRefSetId.get(m.getRefsetId()), snomedDisplayTermType, refSetsById);
				})
				.collect(Collectors.toList());
		
		if (!mappings.isEmpty()) {
			final Map<String, Concept> conceptsById = Multimaps.index(mappings, mapping -> mapping.getTargetComponentURI().resourceUri())
					.asMap()
					.entrySet()
					.stream()
					.filter(entry -> !TerminologyRegistry.UNSPECIFIED.equals(entry.getKey().getResourceId()))
					.map(entry -> {
								final Set<String> idsToFetch = entry.getValue().stream().map(map -> map.getTargetComponentURI().identifier()).collect(Collectors.toSet());
								return CodeSystemRequests.prepareSearchConcepts()
										.all()
										.filterByCodeSystemUri(entry.getKey())
										.filterByIds(idsToFetch)
										.buildAsync()
										.execute(context.service(IEventBus.class))
										.getSync(5, TimeUnit.MINUTES)
										.stream()
										.collect(Collectors.toMap(Concept::getId, c -> c));
							})
					.flatMap(map -> map.entrySet().stream())
					.collect(Collectors.toMap(
							entry -> entry.getKey(), 
							entry -> entry.getValue(),
							(concept1, concept2) -> concept1));
			
			mappings = mappings.stream().map(mapping -> {
				final String mapTargetId = mapping.getTargetComponentURI().identifier();
				if (conceptsById.containsKey(mapTargetId) && !mapping.getTargetComponentURI().isUnspecified()) {
					final Concept concept = conceptsById.get(mapTargetId);
					return mapping.toBuilder()
							.targetTerm(concept.getTerm())
							.targetIconId(concept.getIconId())
							.build();
				} else {
					return mapping;
				}
			}).collect(Collectors.toList());
		}
		
		return new ConceptMapMappings(
			mappings,
			referenceSetMembers.getSearchAfter(),
			referenceSetMembers.getLimit(),
			referenceSetMembers.getTotal()
		);
	}

	private Map<String, ComponentURI> getTargetComponentsByRefSetId(ServiceProvider context, Map<String, SnomedConcept> refSetsById) {
		// TODO figure out a better way to access codesystems from the perspective of a refset/mapset
		
		// extract toolingIds from mapTargetComponentTypes
		Set<String> mapTargetCodeSystemToolings = refSetsById.values()
			.stream()
			.map(SnomedConcept::getReferenceSet)
			.map(SnomedReferenceSet::getMapTargetComponentType)
			.map(type -> type.split("\\.")[0])
			.collect(Collectors.toSet());
		
		
		final Map<String, CodeSystem> codeSystemByToolingId = CodeSystemRequests.prepareSearchCodeSystem()
				.all()
				.filterByToolingIds(mapTargetCodeSystemToolings)
				.buildAsync()
				.execute(context)
				.stream()
				// XXX if multiple CodeSystems use the same toolingId (like in case of SNOMED), then fall back to the first one
				.collect(Collectors.toMap(CodeSystem::getToolingId, c -> c, (c1, c2) -> c1)); 
		
		return refSetsById.entrySet()
				.stream()
				.collect(Collectors.toMap(
					entry -> entry.getKey(), 
					entry -> {
						final String mapTargetComponentType = entry.getValue().getReferenceSet().getMapTargetComponentType();
						String[] typeParts = mapTargetComponentType.split("\\.");
						if (Strings.isNullOrEmpty(mapTargetComponentType) || !codeSystemByToolingId.containsKey(typeParts[0])) {
							return ComponentURI.UNSPECIFIED;
						}
						CodeSystem codeSystem = codeSystemByToolingId.get(typeParts[0]);
						return ComponentURI.of(codeSystem.getResourceURI(), typeParts[1], entry.getValue().getId());
					}
				));
	}

	private ConceptMapMapping toMapping(SnomedReferenceSetMember member, ResourceURI codeSystemURI, ComponentURI targetURI, final SnomedDisplayTermType snomedDisplayTermType, final Map<String, SnomedConcept> refSetsByIds) {	 		
		final String iconId = member.getReferencedComponent().getIconId();
		final String componentType = member.getReferencedComponent().getComponentType();

		final SnomedConcept concept = (SnomedConcept) member.getReferencedComponent();
		final String term  = snomedDisplayTermType.getLabel(concept);

		Builder mappingBuilder = ConceptMapMapping.builder();

		Map<String, Object> properties = member.getProperties();
		String mapTarget = (String) properties.get(SnomedRf2Headers.FIELD_MAP_TARGET);
		if (ComponentURI.isValid(mapTarget)) {
			mappingBuilder.targetComponentURI(ComponentURI.of(mapTarget));
		} else {
			mappingBuilder.targetComponentURI(ComponentURI.of(targetURI.resourceUri(), targetURI.componentType(), mapTarget));
		}

		if (properties.containsKey(SnomedRf2Headers.FIELD_MAP_PRIORITY)) {
			int mapPriority = (int) properties.get(SnomedRf2Headers.FIELD_MAP_PRIORITY);
			mappingBuilder.mapPriority(mapPriority);
		}

		if (properties.containsKey(SnomedRf2Headers.FIELD_MAP_GROUP)) {
			int mapGroup = (int) properties.get(SnomedRf2Headers.FIELD_MAP_GROUP);
			mappingBuilder.mapGroup(mapGroup);
		}

		mappingBuilder.mapAdvice((String) properties.get(SnomedRf2Headers.FIELD_MAP_ADVICE));
		mappingBuilder.mapRule((String) properties.get(SnomedRf2Headers.FIELD_MAP_RULE));
		
		final SnomedConcept referenceSet = refSetsByIds.get(member.getRefsetId());

		return mappingBuilder
				.uri(ComponentURI.of(codeSystemURI, SnomedReferenceSetMember.TYPE, member.getId()).toString())
				.conceptMapUri(ComponentURI.of(codeSystemURI, SnomedConcept.REFSET_TYPE, member.getRefsetId()).toString())
				.conceptMapTerm(referenceSet.getPt().getTerm())
				.conceptMapIconId(referenceSet.getIconId())
				.sourceComponentURI(ComponentURI.of(codeSystemURI, componentType, member.getReferencedComponentId()))
				.sourceIconId(iconId)
				.sourceTerm(term)
				.targetTerm(mapTarget)
				.active(member.isActive())
				.mappingCorrelation(getEquivalence(member))
				.build();
	}

	private MappingCorrelation getEquivalence(SnomedReferenceSetMember conceptMapMember) {

		SnomedRefSetType snomedRefSetType = conceptMapMember.type();

		if (snomedRefSetType == SnomedRefSetType.SIMPLE_MAP || snomedRefSetType == SnomedRefSetType.SIMPLE_MAP_WITH_DESCRIPTION) {
			return MappingCorrelation.EXACT_MATCH; //probably true
		}

		Map<String, Object> properties = conceptMapMember.getProperties();

		if (properties == null || properties.isEmpty()) {
			return MappingCorrelation.NOT_SPECIFIED; 
		}

		String correlationId = (String) properties.getOrDefault(SnomedRf2Headers.FIELD_CORRELATION_ID, "");

		switch (correlationId) {
		case Concepts.MAP_CORRELATION_EXACT_MATCH:
			return MappingCorrelation.EXACT_MATCH;
		case Concepts.MAP_CORRELATION_BROAD_TO_NARROW:
			return MappingCorrelation.BROAD_TO_NARROW;
		case Concepts.MAP_CORRELATION_NARROW_TO_BROAD:
			return MappingCorrelation.NARROW_TO_BROAD;
		case Concepts.MAP_CORRELATION_PARTIAL_OVERLAP:
			return MappingCorrelation.PARTIAL_OVERLAP;
		case Concepts.MAP_CORRELATION_NOT_MAPPABLE:
			return MappingCorrelation.NOT_MAPPABLE;
		case Concepts.MAP_CORRELATION_NOT_SPECIFIED:
			return MappingCorrelation.NOT_SPECIFIED;
		default:
			return MappingCorrelation.NOT_SPECIFIED;
		}
	}

	private SnomedReferenceSetMembers fetchRefsetMembers(ResourceURI uri, ServiceProvider context, Options search, SnomedDisplayTermType snomedDisplayTermType) {

		final Integer limit = search.get(OptionKey.LIMIT, Integer.class);
		final String searchAfter = search.get(OptionKey.AFTER, String.class);
		final List<ExtendedLocale> locales = search.getList(OptionKey.LOCALES, ExtendedLocale.class);
		final Collection<String> referencedComponentIds = search.containsKey(OptionKey.REFERENCED_COMPONENT) ? search.getCollection(OptionKey.REFERENCED_COMPONENT, String.class) : null;
		final Collection<String> componentIds = search.containsKey(OptionKey.COMPONENT) ? search.getCollection(OptionKey.COMPONENT, String.class) : null;
		final Collection<String> mapTargetIds = search.containsKey(OptionKey.MAP_TARGET) ? search.getCollection(OptionKey.MAP_TARGET, String.class) : null;

		SnomedRefSetMemberSearchRequestBuilder requestBuilder = SnomedRequests.prepareSearchMember();

		if (search.containsKey(OptionKey.URI)) {
			// extract refset IDs from pontential URI-like filter values
			final Set<String> refsetId = search.getCollection(OptionKey.URI, String.class).stream()
					.map(uriValue -> {
						// TODO support SNOMED URIs
						try {
							return ComponentURI.of(uriValue).identifier();
						} catch (Exception e) {
							return uriValue;
						}
					})
					.collect(Collectors.toSet());;
			
			
			requestBuilder.filterByRefSet(refsetId);
		}
		
		if (search.containsKey(OptionKey.ACTIVE)) {
			requestBuilder.filterByActive(search.getBoolean(OptionKey.ACTIVE));
		}

		return requestBuilder
				.filterByRefSetType(SnomedRefSetUtil.getMapTypeRefSets())
				.filterByReferencedComponent(referencedComponentIds)
				.filterByComponentIds(componentIds)
				.filterByProps(OptionsBuilder.newBuilder().put(SnomedRf2Headers.FIELD_MAP_TARGET, mapTargetIds).build())
				.setLocales(locales)
				.setExpand(String.format("referencedComponent(%s)", !Strings.isNullOrEmpty(snomedDisplayTermType.getExpand()) ? "expand(" + snomedDisplayTermType.getExpand() + ")" : ""))
				.setLimit(limit)
				.setSearchAfter(searchAfter)
				.build(uri)
				.execute(context);
	}

}
