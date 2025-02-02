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
package com.b2international.snowowl.snomed.datastore.converter;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.b2international.commons.collections.Collections3;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.options.Options;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.request.BaseRevisionResourceConverter;
import com.b2international.snowowl.core.request.DescendantsExpander;
import com.b2international.snowowl.snomed.common.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.*;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSet;
import com.b2international.snowowl.snomed.datastore.SnomedDescriptionUtils;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Functions;
import com.google.common.collect.*;

/**
 * @since 4.5
 */
public final class SnomedConceptConverter extends BaseRevisionResourceConverter<SnomedConceptDocument, SnomedConcept, SnomedConcepts> {

	private SnomedReferenceSetConverter referenceSetConverter;
	
	public SnomedConceptConverter(final BranchContext context, Options expand, List<ExtendedLocale> locales) {
		super(context, expand, locales);
	}
	
	private SnomedReferenceSetConverter getReferenceSetConverter() {
		if (referenceSetConverter == null) {
			// Not null, even if the expand parameter is missing
			Options expandOptions = expand().getOptions(SnomedConcept.Expand.REFERENCE_SET);
			referenceSetConverter = new SnomedReferenceSetConverter(context(), expandOptions.getOptions("expand"), locales());
		}
		
		return referenceSetConverter;
	}
	
	@Override
	protected SnomedConcepts createCollectionResource(List<SnomedConcept> results, String searchAfter, int limit, int total) {
		return new SnomedConcepts(results, searchAfter, limit, total);
	}

	@Override
	protected SnomedConcept toResource(final SnomedConceptDocument input) {
		final SnomedConcept result = new SnomedConcept();
		result.setActive(input.isActive());
		result.setDefinitionStatusId(input.getDefinitionStatusId());
		result.setEffectiveTime(toEffectiveTime(input.getEffectiveTime()));
		result.setId(input.getId());
		result.setModuleId(input.getModuleId());
		result.setIconId(input.getIconId());
		result.setReleased(input.isReleased());
		result.setSubclassDefinitionStatus(toSubclassDefinitionStatus(input.isExhaustive()));
		result.setScore(input.getScore());
		
		// XXX: Core reference set information will not be included if the expand option is not set
		if (expand().containsKey(SnomedConcept.Expand.REFERENCE_SET) && input.isRefSet()) {
			result.setReferenceSet(getReferenceSetConverter().toResource(input));
		}
		
		if (input.getAncestors() != null) {
			result.setAncestorIds(input.getAncestors().toArray());
		}
		
		if (input.getParents() != null) { 
			result.setParentIds(input.getParents().toArray());
		}
		
		if (input.getStatedParents() != null) {
			result.setStatedParentIds(input.getStatedParents().toArray());
		}
		
		if (input.getStatedAncestors() != null) {
			result.setStatedAncestorIds(input.getStatedAncestors().toArray());
		}
		
		if (expand().containsKey(SnomedConcept.Expand.PREFERRED_DESCRIPTIONS) || expand().containsKey(SnomedConcept.Expand.PREFERRED_TERM) || expand().containsKey(SnomedConcept.Expand.FULLY_SPECIFIED_NAME)) {
			List<SnomedDescription> preferredDescriptions = input.getPreferredDescriptions().stream().map(description -> {
				SnomedDescription preferredDescription = new SnomedDescription(description.getId());
				preferredDescription.setConceptId(result.getId());
				preferredDescription.setTerm(description.getTerm());
				preferredDescription.setTypeId(description.getTypeId());
				preferredDescription.setAcceptabilityMap(Maps.toMap(description.getLanguageRefSetIds(), any -> Acceptability.PREFERRED));
				return preferredDescription;
			}).collect(Collectors.toList());
			result.setPreferredDescriptions(new SnomedDescriptions(preferredDescriptions, null, preferredDescriptions.size(), preferredDescriptions.size()));
		}
		
		if (expand().containsKey(SnomedConcept.Expand.SEMANTIC_TAGS)) {
			result.setSemanticTags(Collections3.toImmutableList(input.getSemanticTags()));
		}
			
		return result;
	}
	
	@Override
	protected void expand(List<SnomedConcept> results) {
		if (expand().isEmpty()) {
			return;
		}
		
		final Set<String> conceptIds = FluentIterable.from(results).transform(SnomedConcept::getId).toSet();
		
		expandReferenceSet(results);
		new InactivationPropertiesExpander(context(), expand(), locales(), Concepts.REFSET_CONCEPT_INACTIVITY_INDICATOR).expand(results, conceptIds);
		new MembersExpander(context(), expand(), locales()).expand(results, conceptIds);
		new ModuleExpander(context(), expand(), locales()).expand(results);
		expandDefinitionStatus(results);
		
		expandPreferredTerm(results, conceptIds);
		expandFullySpecifiedName(results, conceptIds);
		expandDescriptions(results, conceptIds);
		
		expandRelationships(results, conceptIds);
		expandInboundRelationships(results, conceptIds);
		
		new SnomedDescendantsExpander(context(), expand(), locales(), SnomedConcept.Expand.DESCENDANTS).expand(results, conceptIds);
		new SnomedDescendantsExpander(context(), expand(), locales(), SnomedConcept.Expand.STATED_DESCENDANTS).expand(results, conceptIds);
		expandAncestors(results, conceptIds, SnomedConcept.Expand.ANCESTORS, false);
		expandAncestors(results, conceptIds, SnomedConcept.Expand.STATED_ANCESTORS, true);
		
		
		// XXX make sure we set the preferred descriptions field to null if the client did not explicitly request it
		// it is necessary to expand this field for pt and fsn expand, but do not increase the payload unnecessarily
		if (!expand().containsKey(SnomedConcept.Expand.PREFERRED_DESCRIPTIONS)) {
			results.forEach(concept -> {
				concept.setPreferredDescriptions(null);
			});
		}
	}

	private void expandDefinitionStatus(List<SnomedConcept> results) {
		if (!expand().containsKey(SnomedConcept.Expand.DEFINITION_STATUS)) {
			return;
		}
		
		final Options definitionStatusExpand = expand().getOptions(SnomedConcept.Expand.DEFINITION_STATUS).getOptions("expand");
		
		Set<String> definitionStatusIds = results.stream()
				.map(SnomedConcept::getDefinitionStatusId)
				.collect(Collectors.toSet());
		
		Map<String, SnomedConcept> definitionStatusesById = SnomedRequests.prepareSearchConcept()
			.filterByIds(definitionStatusIds)
			.setLimit(definitionStatusIds.size())
			.setExpand(definitionStatusExpand)
			.setLocales(locales())
			.build()
			.execute(context())
			.stream()
			.collect(Collectors.toMap(SnomedConcept::getId, c -> c));
		
		for (SnomedConcept result : results) {
			result.setDefinitionStatus(definitionStatusesById.get(result.getDefinitionStatusId()));
		}
	}

	private void expandReferenceSet(List<SnomedConcept> results) {
		if (!expand().containsKey(SnomedConcept.Expand.REFERENCE_SET)) {
			return;
		}
		
		List<SnomedReferenceSet> referenceSets = FluentIterable.from(results)
			.filter(concept -> concept.getReferenceSet() != null)
			.transform(concept -> concept.getReferenceSet())
			.toList();
		
		getReferenceSetConverter().expand(referenceSets);
	}

	private void expandPreferredTerm(List<SnomedConcept> results, final Set<String> conceptIds) {
		if (!expand().containsKey(SnomedConcept.Expand.PREFERRED_TERM)) {
			return;
		}
		
		final List<SnomedDescription> synonyms = newArrayList();
		for (SnomedConcept result : results) {
			for (SnomedDescription description : result.getPreferredDescriptions()) {
				if (!Concepts.FULLY_SPECIFIED_NAME.equals(description.getTypeId())) {
					synonyms.add(description);
				}
			}
		}
		final Map<String, SnomedDescription> terms = SnomedDescriptionUtils.indexBestPreferredByConceptId(context(), synonyms, locales());
		for (SnomedConcept concept : results) {
			concept.setPt(terms.get(concept.getId()));
		}
	}

	private void expandFullySpecifiedName(List<SnomedConcept> results, final Set<String> conceptIds) {
		if (!expand().containsKey(SnomedConcept.Expand.FULLY_SPECIFIED_NAME)) {
			return;
		}
		
		final Map<String, SnomedDescription> firstFsnByConceptId = newHashMap();
		final List<SnomedDescription> fsns = newArrayList();
		for (SnomedConcept concept : results) {
			for (SnomedDescription description : concept.getPreferredDescriptions()) {
				if (Concepts.FULLY_SPECIFIED_NAME.equals(description.getTypeId())) {
					fsns.add(description);
					if (!firstFsnByConceptId.containsKey(concept.getId())) {
						firstFsnByConceptId.put(concept.getId(), description);
					}
				}
			}
		}
		
		final Map<String, SnomedDescription> terms = SnomedDescriptionUtils.indexBestPreferredByConceptId(context(), fsns, locales());
		
		for (SnomedConcept concept : results) {
			SnomedDescription fsn = terms.get(concept.getId());
			if (fsn == null) {
				fsn = firstFsnByConceptId.get(concept.getId());
			}
			concept.setFsn(fsn);
		}
	}

	private void expandDescriptions(List<SnomedConcept> results, final Set<String> conceptIds) {
		if (!expand().containsKey(SnomedConcept.Expand.DESCRIPTIONS)) {
			return;
		}
		
		final Options expandOptions = expand().get(SnomedConcept.Expand.DESCRIPTIONS, Options.class);
		final SnomedDescriptions descriptions = SnomedRequests
			.prepareSearchDescription()
			.all()
			.setExpand(expandOptions.get("expand", Options.class))
			.filterByActive(expandOptions.get("active", Boolean.class))
			.filterByType(expandOptions.get("typeId", String.class))
			.filterByConcepts(conceptIds)
			.setLocales(locales())
			.sortBy(expandOptions.get("sort", String.class))
			.build()
			.execute(context());
		
		final ListMultimap<String, SnomedDescription> descriptionsByConceptId = Multimaps.index(descriptions, 
				description -> description.getConceptId());
		
		for (SnomedConcept concept : results) {
			final List<SnomedDescription> conceptDescriptions = descriptionsByConceptId.get(concept.getId());
			concept.setDescriptions(new SnomedDescriptions(conceptDescriptions, null, conceptDescriptions.size(), conceptDescriptions.size()));
		}
	}
	
	private void expandRelationships(List<SnomedConcept> results, final Set<String> conceptIds) {
		if (!expand().containsKey(SnomedConcept.Expand.RELATIONSHIPS)) {
			return;
		}
		
		final Options expandOptions = expand().get(SnomedConcept.Expand.RELATIONSHIPS, Options.class);
		final SnomedRelationships relationships = SnomedRequests
				.prepareSearchRelationship()
				.all()
				.filterByActive(expandOptions.get("active", Boolean.class))
				.filterByCharacteristicType(expandOptions.get("characteristicTypeId", String.class))
				.filterByTypes(expandOptions.containsKey("typeId") ? expandOptions.getCollection("typeId", String.class) : null)
				.filterByDestinations(expandOptions.containsKey("destinationId") ? expandOptions.getCollection("destinationId", String.class) : null)
				.filterBySources(conceptIds)
				.setExpand(expandOptions.get("expand", Options.class))
				.setLocales(locales())
				.sortBy(expandOptions.get("sort", String.class))
				.build()
				.execute(context());
		
		final ListMultimap<String, SnomedRelationship> relationshipsByConceptId = Multimaps.index(relationships, 
				relationship -> relationship.getSourceId());
		
		for (SnomedConcept concept : results) {
			final List<SnomedRelationship> conceptRelationships = relationshipsByConceptId.get(concept.getId());
			concept.setRelationships(new SnomedRelationships(conceptRelationships, null, conceptRelationships.size(), conceptRelationships.size()));
		}
	}
	
	private void expandInboundRelationships(List<SnomedConcept> results, final Set<String> conceptIds) {
		if (!expand().containsKey(SnomedConcept.Expand.INBOUND_RELATIONSHIPS)) {
			return;
		}
		
		final Options expandOptions = expand().get(SnomedConcept.Expand.INBOUND_RELATIONSHIPS, Options.class);
		
		final int relationshipSearchLimit = getLimit(expandOptions);
		
		final SnomedRelationships inboundRelationships = SnomedRequests.prepareSearchRelationship()
			.setLimit(relationshipSearchLimit)
			.filterByTypes(expandOptions.containsKey("typeId") ? expandOptions.getCollection("typeId", String.class) : null)
			.filterBySources(expandOptions.containsKey("sourceId") ? expandOptions.getCollection("sourceId", String.class) : null)
			.filterByActive(expandOptions.get("active", Boolean.class))
			.filterByCharacteristicType(expandOptions.get("characteristicTypeId", String.class))
			.filterByDestinations(conceptIds)
			.setExpand(expandOptions.get("expand", Options.class))
			.setLocales(locales())
			.sortBy(expandOptions.get("sort", String.class))
			.build()
			.execute(context());
		
		// filterByDestination above ensures that all relationships have a destinationId
		final ListMultimap<String, SnomedRelationship> inboundRelationshipsByConceptId = Multimaps.index(inboundRelationships,
				inboundRelationship -> inboundRelationship.getDestinationId());
		
		for (SnomedConcept concept : results) {
			final List<SnomedRelationship> conceptInboundRelationships = inboundRelationshipsByConceptId.get(concept.getId());
			concept.setInboundRelationships(new SnomedRelationships(conceptInboundRelationships, null, conceptInboundRelationships.size(), conceptInboundRelationships.size()));
		}
	}

	private void expandAncestors(List<SnomedConcept> results, Set<String> conceptIds, String ancestorsExpandKey, boolean stated) {
		if (!expand().containsKey(ancestorsExpandKey)) {
			return;
		}

		final Options expandOptions = expand().get(ancestorsExpandKey, Options.class);
		final boolean direct = DescendantsExpander.checkDirect(expandOptions, ancestorsExpandKey);
		
		final Multimap<String, String> ancestorsByDescendant = TreeMultimap.create();
		
		for (SnomedConcept concept : results) {
			final long[] parentIds = stated ? concept.getStatedParentIds() : concept.getParentIds();
			if (parentIds != null) {
				for (long parent : parentIds) {
					if (IComponent.ROOT_IDL != parent) {
						ancestorsByDescendant.put(concept.getId(), Long.toString(parent));
					}
				}
			}
			if (!direct) {
				final long[] ancestorIds = stated ? concept.getStatedAncestorIds() : concept.getAncestorIds();
				if (ancestorIds != null) {
					for (long ancestor : ancestorIds) {
						if (IComponent.ROOT_IDL != ancestor) {
							ancestorsByDescendant.put(concept.getId(), Long.toString(ancestor));
						}
					}
				}
			}
		}
		
		final int limit = getLimit(expandOptions);

		final Collection<String> componentIds = newHashSet(ancestorsByDescendant.values());
		
		if (limit > 0 && !componentIds.isEmpty()) {
			final SnomedConcepts ancestors = SnomedRequests.prepareSearchConcept()
					.all()
					.filterByIds(componentIds)
					.setLocales(locales())
					.setExpand(expandOptions.get("expand", Options.class))
					.build().execute(context());
			
			final Map<String, SnomedConcept> ancestorsById = newHashMap();
			ancestorsById.putAll(Maps.uniqueIndex(ancestors, SnomedConcept::getId));
			for (SnomedConcept concept : results) {
				final Collection<String> ancestorIds = ancestorsByDescendant.get(concept.getId());
				final List<SnomedConcept> conceptAncestors = FluentIterable.from(ancestorIds).limit(limit).transform(Functions.forMap(ancestorsById)).toList();
				final SnomedConcepts ancestorConcepts = new SnomedConcepts(conceptAncestors, null, limit, ancestorIds.size());
				if (stated) {
					concept.setStatedAncestors(ancestorConcepts);
				} else {
					concept.setAncestors(ancestorConcepts);
				}
			}
		} else {
			for (SnomedConcept concept : results) {
				final Collection<String> ancestorIds = ancestorsByDescendant.get(concept.getId());
				final SnomedConcepts ancestors = new SnomedConcepts(limit, ancestorIds.size());
				if (stated) {
					concept.setStatedAncestors(ancestors);
				} else {
					concept.setAncestors(ancestors);
				}
			}
		}
	}

	private SubclassDefinitionStatus toSubclassDefinitionStatus(final Boolean exhaustive) {
		if (exhaustive == null) return null;
		return exhaustive ? SubclassDefinitionStatus.DISJOINT_SUBCLASSES : SubclassDefinitionStatus.NON_DISJOINT_SUBCLASSES;
	}
	
}
