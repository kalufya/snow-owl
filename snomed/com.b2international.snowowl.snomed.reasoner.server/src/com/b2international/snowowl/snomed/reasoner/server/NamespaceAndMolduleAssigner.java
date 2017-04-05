/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.reasoner.server;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.snomed.Concept;
import com.b2international.snowowl.snomed.datastore.SnomedEditingContext;
import com.b2international.snowowl.snomed.datastore.StatementFragment;
import com.google.common.collect.Multimap;

/**
 * Allocator to create Id namespaces and module concepts to inferred SNOMED CT properties
 * generated by the classifier. To improve performance this allocator provides the means of 
 * pre-fetching namespaces and modules in bulk.
 *
 */
public interface NamespaceAndMolduleAssigner {

	/**
	 * Pre-allocate namespaces and modules for the new relationships of each concept represented by the String key
	 * of the passed in map.  The allocated namespaces and modules can be later distributed by the 
	 * {@link #allocateRelationshipNamespace(String)} and {@link #allocateRelationshipModule(String)} methods respectively.
	 * 
	 * @param Map with concept id as a key and multiple relationships as values
	 * @param the active snomed editing context
	 */
	public void allocateRelationshipNamespacesAndModules(final Multimap<String, StatementFragment> conceptIdToRelationshipsMap, final SnomedEditingContext context);

	/**
	 * Returns a namespace to be assigned to a relationship based on its source concept id and branch path where it is located.
	 * @param source concept id of the relationship to allocate the namespace to
	 * @param branch path of the relationship the namespace is allocated to
	 * 
	 * @return namespace for the relationship
	 */
	public String getRelationshipNamespace(final String sourceConceptId, final IBranchPath branchPath);

	/**
	 * Returns a module concept to be assigned to a relationship based on its source concept id and branch path where it is located.
	 * @param source concept id of the relationship to allocate the namespace to
	 * @param branch path of the relationship the namespace is allocated to
	 * 
	 * @return module concept id for the relationship
	 */
	public Concept getRelationshipModule(final String sourceConcept, final IBranchPath branchPath);
	
	/**
	 * Pre-allocate  modules for the new concrete domains of each concept represented by the String key
	 * of the passed in map.  
	 * The allocated modules can be later distributed by the {@link #allocateConcreateDomainModule(String)} method.
	 * 
	 * @param Map with concept id as a key and multiple relationships as values
	 * @param the active snomed editing context
	 */
	public void allocateConcreateDomainModules(final Multimap<String, StatementFragment> conceptIdToRelationshipsMap, final SnomedEditingContext context);

	/**
	 * Returns a module concept to be assigned to a concrete domain based on its source concept id and branch path where it is located.
	 * @param source concept id of the concrete domain to allocate the namespace to
	 * @param branch path of the concrete domain the namespace is allocated to
	 * 
	 * @return module concept id for the concrete domain
	 */
	public Concept getConcreateDomainModule(final String sourceConceptId, final IBranchPath branchPath);


}
