/*
 * Copyright 2017-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.request.rf2.importer;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.b2international.index.revision.Revision;
import com.b2international.snowowl.core.CoreTerminologyBroker;
import com.b2international.snowowl.core.domain.DelegatingBranchContext;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedCoreComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.store.SnomedComponentBuilder;
import com.b2international.snowowl.snomed.core.store.SnomedComponents;
import com.b2international.snowowl.snomed.core.store.SnomedMemberBuilder;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedComponentDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @since 6.0.0
 */
final class Rf2TransactionContext extends DelegatingBranchContext implements TransactionContext {

//	private final Map<String, CDOObject> newComponents = newHashMap();
//	private final Map<String, SnomedRefSet> newRefSets = newHashMap();
	
	private final boolean loadOnDemand;
	
	Rf2TransactionContext(TransactionContext context, final boolean loadOnDemand) {
		super(context);
		this.loadOnDemand = loadOnDemand;
	}
	
	@Override
	public String userId() {
		return getDelegate().userId();
	}

	@Override
	protected TransactionContext getDelegate() {
		return (TransactionContext) super.getDelegate();
	}
	
	@Override
	public void add(Object o) {
		getDelegate().add(o);
	}
	
	@Override
	public void update(Revision oldVersion, Revision newVersion) {
		getDelegate().update(oldVersion, newVersion);
	}
	
	@Override
	public void clearContents() {
		getDelegate().clearContents();
	}
	
	@Override
	public long commit() {
		throw new UnsupportedOperationException("TODO implement me");
	}
	
	@Override
	public long commit(String userId, String commitComment, String parentContextDescription) {
		try {
			System.err.println("Pushing changes: " + commitComment);
			return getDelegate().commit(userId, commitComment, parentContextDescription);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
//			newComponents.values().forEach(component -> {
//				if (component instanceof Component) {
//					storageKeysByComponent.put(((Component) component).getId(), CDOIDUtil.getLong(component.cdoID()));
//				} else if (component instanceof SnomedRefSetMember) {
//					storageKeysByComponent.put(((SnomedRefSetMember) component).getUuid(), CDOIDUtil.getLong(component.cdoID()));
//				}
//			});
//			
//			newRefSets.values().forEach(refSet -> {
//				storageKeysByRefSet.put(refSet.getIdentifierId(), CDOIDUtil.getLong(refSet.cdoID()));
//			});
//			
//			this.newComponents.clear();
//			this.newRefSets.clear();
//			
//			this.editingContext.clearCache();
		} 
	}
	
	@Override
	public void delete(Object o) {
		getDelegate().delete(o);
	}
	
	@Override
	public void delete(Object o, boolean force) {
		getDelegate().delete(o, force);
	}
	
	@Override
	public void close() throws Exception {
		getDelegate().close();
	}

	@Override
	public <T> T lookupIfExists(String componentId, Class<T> type) {
		return getDelegate().lookupIfExists(componentId, type);
	}
	
	@Override
	public boolean isNotificationEnabled() {
		return getDelegate().isNotificationEnabled();
	}
	
	@Override
	public void setNotificationEnabled(boolean notificationEnabled) {
		getDelegate().setNotificationEnabled(notificationEnabled);
	}
	
	@Override
	public <T> Map<String, T> lookup(Collection<String> componentIds, Class<T> type) {
		return getDelegate().lookup(componentIds, type);
	}
	
	@Override
	public <T> T lookup(String componentId, Class<T> type) throws ComponentNotFoundException {
		return getDelegate().lookup(componentId, type);
	}
	
//	@Override
//	public void preCommit() {
//		// add concepts to tx context
//		for (CDOObject component : newComponents.values()) {
//			if (component instanceof Concept) {
//				add(component);
//			}
//		}
		
		// XXX refsets were already added by the add method
		// XXX do NOT invoke preCommit on the delegate, RF2 import does not check ID uniqueness and module dependencies the standard way
//	}
	
//	@Override
//	public <T extends EObject> T lookup(String componentId, Class<T> type) throws ComponentNotFoundException {
//		if (SnomedRefSet.class.isAssignableFrom(type) && newRefSets.containsKey(componentId)) {
//			return type.cast(newRefSets.get(componentId));
//		} else if (!SnomedRefSet.class.isAssignableFrom(type) && newComponents.containsKey(componentId)) {
//			return type.cast(newComponents.get(componentId));
//		} else if (SnomedRefSet.class.isAssignableFrom(type) && storageKeysByRefSet.containsKey(componentId)) {
//			return type.cast(editingContext.lookup(storageKeysByRefSet.get(componentId)));
//		} else if (!SnomedRefSet.class.isAssignableFrom(type) && storageKeysByComponent.containsKey(componentId)) {
//			return type.cast(editingContext.lookup(storageKeysByComponent.get(componentId)));
//		} else {
//			// XXX allow only newly created refsets to lookup using the transaction, or allow all if loadOnDemand is enabled
//			if (SnomedRefSet.class.isAssignableFrom(type) || CodeSystem.class.isAssignableFrom(type) || loadOnDemand) {
//				return getDelegate().lookup(componentId, type);
//			} else {
//				throw new IllegalArgumentException("Missing component from maps: " + componentId);
//			}
//		}
//	}
	
//	@Override
//	public <T extends CDOObject> Map<String, T> lookup(Collection<String> componentIds, Class<T> type) {
//		final Map<String, T> resolvedComponentById = newHashMap();
//		Set<String> unresolvedComponentIds = newHashSet();
//
//		// resolve by new components first
//		for (String componentId : componentIds) {
//			if (SnomedRefSet.class.isAssignableFrom(type) && newRefSets.containsKey(componentId)) {
//				resolvedComponentById.put(componentId, type.cast(newRefSets.get(componentId)));
//			} else if (!SnomedRefSet.class.isAssignableFrom(type) && newComponents.containsKey(componentId)) {
//				resolvedComponentById.put(componentId, type.cast(newComponents.get(componentId)));
//			} else if (SnomedRefSet.class.isAssignableFrom(type) && storageKeysByRefSet.containsKey(componentId)) {
//				resolvedComponentById.put(componentId, type.cast(editingContext.lookup(storageKeysByRefSet.get(componentId))));
//			} else if (!SnomedRefSet.class.isAssignableFrom(type) && storageKeysByComponent.containsKey(componentId)) {
//				resolvedComponentById.put(componentId, type.cast(editingContext.lookup(storageKeysByComponent.get(componentId))));
//			} else {
//				unresolvedComponentIds.add(componentId);
//			}
//		}
//		
//		if (!unresolvedComponentIds.isEmpty() && loadOnDemand) {
//			// load any unresolved components via index lookup
//			final Map<String, T> resolvedByIndex = getDelegate().lookup(unresolvedComponentIds, type);
//			resolvedComponentById.putAll(resolvedByIndex);
//			resolvedByIndex.values()
//				.stream()
//				.filter(CDOObject.class::isInstance)
//				.map(CDOObject.class::cast)
//				.forEach(component -> {
//					if (component instanceof Component) {
//						storageKeysByComponent.put(((Component) component).getId(), CDOIDUtil.getLong(component.cdoID()));
//					} else if (component instanceof SnomedRefSetMember) {
//						storageKeysByComponent.put(((SnomedRefSetMember) component).getUuid(), CDOIDUtil.getLong(component.cdoID()));
//					} else if (component instanceof SnomedRefSet) {
//						storageKeysByRefSet.put(((SnomedRefSet) component).getIdentifierId(), CDOIDUtil.getLong(component.cdoID()));
//					}
//				});
//		}
//		
//		return resolvedComponentById;
//	}
	
//	Collection<SnomedRefSet> getNewRefSets() {
//		return ImmutableList.copyOf(newRefSets.values());
//	}
	
	void add(Collection<SnomedComponent> componentChanges, Multimap<Class<? extends SnomedDocument>, String> dependenciesByType) {
		final Multimap<Class<? extends SnomedDocument>, SnomedComponent> componentChangesByType = Multimaps.index(componentChanges, this::getCdoType);
		final List<Class<? extends SnomedDocument>> typeToImportInOrder = ImmutableList.of(SnomedConceptDocument.class, SnomedDescriptionIndexEntry.class, SnomedRelationshipIndexEntry.class, SnomedRefSetMemberIndexEntry.class);
		for (Class<? extends SnomedDocument> type : typeToImportInOrder) {
			final Collection<SnomedComponent> rf2Components = componentChangesByType.get(type);
			final Set<String> componentsToLookup = rf2Components.stream().map(IComponent::getId).collect(Collectors.toSet());
			// add all dependencies with the same type
			componentsToLookup.addAll(dependenciesByType.get(type));
			
			final Map<String, ? extends SnomedDocument> existingComponents = lookup(componentsToLookup, type);
			final Map<String, SnomedConceptDocument> existingRefSets;
			if (SnomedRefSetMemberIndexEntry.class == type) {
				existingRefSets = lookup(rf2Components.stream().map(member -> ((SnomedReferenceSetMember) member).getReferenceSetId()).collect(Collectors.toSet()), SnomedConceptDocument.class);
			} else {
				existingRefSets = Collections.emptyMap();
			}

			// seed missing component before applying row changes
			// and check for existing components with the same or greater effective time and skip them
			final Collection<SnomedComponent> componentsToImport = newArrayList();
			final Map<String, SnomedDocument.Builder<?, ?>> newComponents = newHashMap();
			for (SnomedComponent rf2Component : rf2Components) {
				SnomedDocument existingObject = existingComponents.get(rf2Component.getId());
				if (existingObject == null) {
					// new component, add to new components and apply row
					newComponents.put(rf2Component.getId(), createDocBuilder(rf2Component.getId(), type, null));
					if (rf2Component instanceof SnomedReferenceSetMember) {
						final SnomedReferenceSetMember member = (SnomedReferenceSetMember) rf2Component;
						// seed the refset if missing
						if (!existingRefSets.containsKey(member.getReferenceSetId()) && !newRefSets.containsKey(member.getReferenceSetId())) {
							final String referencedComponentType = SnomedTerminologyComponentConstants.getTerminologyComponentId(member.getReferencedComponent().getId());
							String mapTargetComponentType = CoreTerminologyBroker.UNSPECIFIED;
							try {
								mapTargetComponentType = SnomedTerminologyComponentConstants.getTerminologyComponentId((String) member.getProperties().get(SnomedRf2Headers.FIELD_MAP_TARGET));
							} catch (IllegalArgumentException e) {
								// ignored
							}
							SnomedRequests.prepareNewRefSet()
								.setIdentifierId(member.getReferenceSetId())
								.setType(member.type())
								.setReferencedComponentType(referencedComponentType)
								.setMapTargetComponentType(mapTargetComponentType)
								.build()
								.execute(this);
							
							newRefSets.put(member.getReferenceSetId(), lookup(member.getReferenceSetId(), SnomedRefSet.class));
						}
					}
					componentsToImport.add(rf2Component);
				} else if (existingObject instanceof SnomedComponentDocument && rf2Component instanceof SnomedCoreComponent) {
					final SnomedCoreComponent rf2Row = (SnomedCoreComponent) rf2Component;
					final SnomedComponentDocument existingRow = (SnomedComponentDocument) existingObject;
					if (rf2Row.getEffectiveTime().getTime() > existingRow.getEffectiveTime()) {
						componentsToImport.add(rf2Component);
					}
				} else if (existingObject instanceof SnomedRefSetMemberIndexEntry && rf2Component instanceof SnomedReferenceSetMember) {
					final SnomedReferenceSetMember rf2Row = (SnomedReferenceSetMember) rf2Component;
					final SnomedRefSetMemberIndexEntry existingRow = (SnomedRefSetMemberIndexEntry) existingObject;
					if (rf2Row.getEffectiveTime().getTime() > existingRow.getEffectiveTime()) {
						componentsToImport.add(rf2Component);
					}
				}
			}
			
			// apply row changes
			for (SnomedComponent rf2Component : componentsToImport) {
				final String id = rf2Component.getId();
				SnomedDocument.Builder<?, ?> existingObject;
				if (existingComponents.containsKey(id)) {
					existingObject = createDocBuilder(id, type, existingComponents.get(id));
				} else {
					existingObject = newComponents.get(id);
				}
				final SnomedComponentBuilder builder;
				if (rf2Component instanceof SnomedCoreComponent) {
					builder = prepareCoreComponent(rf2Component);
				} else if (rf2Component instanceof SnomedReferenceSetMember) {
					builder = prepareMember((SnomedReferenceSetMember) rf2Component);
				} else {
					throw new UnsupportedOperationException("Unsupported component: " + rf2Component);
				}
				builder.init(existingObject, this);
				if (builder instanceof SnomedMemberBuilder) {
					((SnomedMemberBuilder) builder).addTo(this);
				}
			}
		}
	}

	private Class<? extends SnomedDocument> getCdoType(SnomedComponent component) {
		if (component instanceof SnomedConcept) {
			return SnomedConceptDocument.class;
		} else if (component instanceof SnomedDescription) {
			return SnomedDescriptionIndexEntry.class;
		} else if (component instanceof SnomedRelationship) {
			return SnomedRelationshipIndexEntry.class;
		} else if (component instanceof SnomedReferenceSetMember) {
			return SnomedRefSetMemberIndexEntry.class;
		}
		throw new UnsupportedOperationException("Unsupported component: " + component.getClass().getSimpleName());
	}
	
	private SnomedDocument.Builder<?, ?> createDocBuilder(String componentId, Class<? extends SnomedDocument> type, SnomedDocument initializeFrom) {
		if (type.isAssignableFrom(SnomedRefSetMemberIndexEntry.class)) {
			return initializeFrom == null ? SnomedRefSetMemberIndexEntry.builder() : SnomedRefSetMemberIndexEntry.builder((SnomedRefSetMemberIndexEntry) initializeFrom);
		} else if (type.isAssignableFrom(SnomedConceptDocument.class)) {
			return initializeFrom == null ? SnomedConceptDocument.builder() : SnomedConceptDocument.builder((SnomedConceptDocument) initializeFrom);
		} else if (type.isAssignableFrom(SnomedDescriptionIndexEntry.class)) {
			return initializeFrom == null ? SnomedDescriptionIndexEntry.builder() : SnomedDescriptionIndexEntry.builder((SnomedDescriptionIndexEntry) initializeFrom);
		} else if (type.isAssignableFrom(SnomedRelationshipIndexEntry.class)) {
			return initializeFrom == null ? SnomedRelationshipIndexEntry.builder() : SnomedRelationshipIndexEntry.builder((SnomedRelationshipIndexEntry) initializeFrom);
		} else {
			throw new UnsupportedOperationException("Unknown core component type: " + type);
		}
	}
	
//	private SnomedRefSetMemberIndexEntry.Builder createMember(String memberId, SnomedRefSetType type) {
//		switch (type) {
//		case ASSOCIATION: 
//			return SnomedRefSetFactory.eINSTANCE.createSnomedAssociationRefSetMember();
//		case ATTRIBUTE_VALUE: 
//			return SnomedRefSetFactory.eINSTANCE.createSnomedAttributeValueRefSetMember();
//		case DESCRIPTION_TYPE: 
//			return SnomedRefSetFactory.eINSTANCE.createSnomedDescriptionTypeRefSetMember();
//		case COMPLEX_MAP: //$FALL-THROUGH$
//		case EXTENDED_MAP: 
//			return SnomedRefSetFactory.eINSTANCE.createSnomedComplexMapRefSetMember();
//		case LANGUAGE: 
//			return SnomedRefSetFactory.eINSTANCE.createSnomedLanguageRefSetMember();
//		case SIMPLE_MAP: //$FALL-THROUGH$ 
//		case SIMPLE_MAP_WITH_DESCRIPTION:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedSimpleMapRefSetMember();
//		case MODULE_DEPENDENCY:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedModuleDependencyRefSetMember();
//		case SIMPLE:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedRefSetMember();
//		case OWL_AXIOM: //$FALL-THROUGH$
//		case OWL_ONTOLOGY:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedOWLExpressionRefSetMember();
//		case MRCM_DOMAIN:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedMRCMDomainRefSetMember();
//		case MRCM_ATTRIBUTE_DOMAIN:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedMRCMAttributeDomainRefSetMember();
//		case MRCM_ATTRIBUTE_RANGE:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedMRCMAttributeRangeRefSetMember();
//		case MRCM_MODULE_SCOPE:
//			return SnomedRefSetFactory.eINSTANCE.createSnomedMRCMModuleScopeRefSetMember();
//		default: throw new UnsupportedOperationException("Unknown refset member type: " + type);
//		}
//	}
	
	private SnomedComponentBuilder<?, ?, ?> prepareCoreComponent(SnomedComponent component) {
		if (component instanceof SnomedConcept) {
			SnomedConcept concept = (SnomedConcept) component;
			return SnomedComponents.newConcept()
					.withId(component.getId())
					.withActive(concept.isActive())
					.withEffectiveTime(concept.getEffectiveTime())
					.withModule(concept.getModuleId())
					.withDefinitionStatus(concept.getDefinitionStatus())
					.withExhaustive(concept.getSubclassDefinitionStatus().isExhaustive());
		} else if (component instanceof SnomedDescription) { 
			SnomedDescription description = (SnomedDescription) component;
			return SnomedComponents.newDescription()
					.withId(component.getId())
					.withActive(description.isActive())
					.withEffectiveTime(description.getEffectiveTime())
					.withModule(description.getModuleId())
					.withCaseSignificance(description.getCaseSignificance())
					.withLanguageCode(description.getLanguageCode())
					.withType(description.getTypeId())
					.withTerm(description.getTerm())
					.withConcept(description.getConceptId());
		} else if (component instanceof SnomedRelationship) {
			SnomedRelationship relationship = (SnomedRelationship) component;
			return SnomedComponents.newRelationship()
					.withId(component.getId())
					.withActive(relationship.isActive())
					.withEffectiveTime(relationship.getEffectiveTime())
					.withModule(relationship.getModuleId())
					.withSource(relationship.getSourceId())
					.withType(relationship.getTypeId())
					.withDestination(relationship.getDestinationId())
					.withCharacteristicType(relationship.getCharacteristicType())
					.withGroup(relationship.getGroup())
					.withUnionGroup(relationship.getUnionGroup())
					.withDestinationNegated(false)
					.withModifier(relationship.getModifier());
		} else {
			throw new UnsupportedOperationException("Cannot prepare unknown core component: " + component);
		}
	}
	
	private SnomedComponentBuilder<?, ?, ?> prepareMember(SnomedReferenceSetMember rf2Component) {
		final Map<String, Object> properties = rf2Component.getProperties();
		SnomedMemberBuilder<?> builder;
		switch (rf2Component.type()) {
			case ASSOCIATION: 
				builder = SnomedComponents.newAssociationMember()
						.withTargetComponentId((String) properties.get(SnomedRf2Headers.FIELD_TARGET_COMPONENT_ID));
				break;
			case ATTRIBUTE_VALUE:
				builder = SnomedComponents.newAttributeValueMember()
						.withValueId((String) properties.get(SnomedRf2Headers.FIELD_VALUE_ID));
				break;
			case DESCRIPTION_TYPE: 
				builder = SnomedComponents.newDescriptionTypeMember()
						.withDescriptionFormatId((String) properties.get(SnomedRf2Headers.FIELD_DESCRIPTION_FORMAT))
						.withDescriptionLength((Integer) properties.get(SnomedRf2Headers.FIELD_DESCRIPTION_LENGTH));
				break;
			case COMPLEX_MAP:
			case EXTENDED_MAP:
				builder = SnomedComponents.newComplexMapMember()
						.withGroup((Integer) properties.get(SnomedRf2Headers.FIELD_MAP_GROUP))
						.withPriority((Integer) properties.get(SnomedRf2Headers.FIELD_MAP_PRIORITY))
						.withMapAdvice((String) properties.get(SnomedRf2Headers.FIELD_MAP_ADVICE))
						.withCorrelationId((String) properties.get(SnomedRf2Headers.FIELD_CORRELATION_ID))
						.withMapCategoryId((String) properties.get(SnomedRf2Headers.FIELD_MAP_CATEGORY_ID))
						.withMapRule((String) properties.get(SnomedRf2Headers.FIELD_MAP_RULE))
						.withMapTargetId((String) properties.get(SnomedRf2Headers.FIELD_MAP_TARGET));
				break;
			case LANGUAGE: 
				builder = SnomedComponents.newLanguageMember()
						.withAcceptability(Acceptability.getByConceptId((String) properties.get(SnomedRf2Headers.FIELD_ACCEPTABILITY_ID)));
				break;
			case SIMPLE_MAP_WITH_DESCRIPTION: 
				builder = SnomedComponents.newSimpleMapMember()
						.withMapTargetId((String) properties.get(SnomedRf2Headers.FIELD_MAP_TARGET))
						.withMapTargetDescription((String) properties.get(SnomedRf2Headers.FIELD_MAP_TARGET_DESCRIPTION));
				break;
			case SIMPLE_MAP: 
				builder = SnomedComponents.newSimpleMapMember()
						.withMapTargetId((String) properties.get(SnomedRf2Headers.FIELD_MAP_TARGET));
				break;
			case MODULE_DEPENDENCY:
				builder = SnomedComponents.newModuleDependencyMember()
						.withSourceEffectiveTime((Date) properties.get(SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME))
						.withTargetEffectiveTime((Date) properties.get(SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME));
				break;
			case SIMPLE:
				builder = SnomedComponents.newSimpleMember();
				break;
			case OWL_AXIOM: //$FALL-THROUGH$
			case OWL_ONTOLOGY:
				builder = SnomedComponents.newOWLExpressionReferenceSetMember()
						.withOWLExpression((String) properties.get(SnomedRf2Headers.FIELD_OWL_EXPRESSION));
				break;
			case MRCM_DOMAIN:
				builder = SnomedComponents.newMRCMDomainReferenceSetMember()
						.withDomainConstraint((String) properties.get(SnomedRf2Headers.FIELD_MRCM_DOMAIN_CONSTRAINT))
						.withParentDomain((String) properties.get(SnomedRf2Headers.FIELD_MRCM_PARENT_DOMAIN))
						.withProximalPrimitiveConstraint((String) properties.get(SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_CONSTRAINT))
						.withProximalPrimitiveRefinement((String) properties.get(SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_REFINEMENT))
						.withDomainTemplateForPrecoordination((String) properties.get(SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_PRECOORDINATION))
						.withDomainTemplateForPostcoordination((String) properties.get(SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_POSTCOORDINATION))
						.withEditorialGuideReference((String) properties.get(SnomedRf2Headers.FIELD_MRCM_EDITORIAL_GUIDE_REFERENCE));
				break;
			case MRCM_ATTRIBUTE_DOMAIN:
				builder = SnomedComponents.newMRCMAttributeDomainReferenceSetMember()
						.withDomainId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_DOMAIN_ID))
						.withGrouped((Boolean) properties.get(SnomedRf2Headers.FIELD_MRCM_GROUPED))
						.withAttributeCardinality((String) properties.get(SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_CARDINALITY))
						.withAttributeInGroupCardinality((String) properties.get(SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_IN_GROUP_CARDINALITY))
						.withRuleStrengthId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_RULE_STRENGTH_ID))
						.withContentTypeId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_CONTENT_TYPE_ID));
				break;
			case MRCM_ATTRIBUTE_RANGE:
				builder = SnomedComponents.newMRCMAttributeRangeReferenceSetMember()
						.withRangeConstraint((String) properties.get(SnomedRf2Headers.FIELD_MRCM_RANGE_CONSTRAINT))
						.withAttributeRule((String) properties.get(SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_RULE))
						.withRuleStrengthId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_RULE_STRENGTH_ID))
						.withContentTypeId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_CONTENT_TYPE_ID));
				break;
			case MRCM_MODULE_SCOPE:
				builder = SnomedComponents.newMRCMModuleScopeReferenceSetMember()
						.withMRCMRuleRefsetId((String) properties.get(SnomedRf2Headers.FIELD_MRCM_RULE_REFSET_ID));
				break;
			default: 
				throw new UnsupportedOperationException("Unknown refset member type: " + rf2Component.type());
		}
		return builder
				.withId(rf2Component.getId())
				.withActive(rf2Component.isActive())
				.withEffectiveTime(rf2Component.getEffectiveTime())
				.withModule(rf2Component.getModuleId())
				.withReferencedComponent(rf2Component.getReferencedComponent().getId())
				.withRefSet(rf2Component.getReferenceSetId());
	}
}
