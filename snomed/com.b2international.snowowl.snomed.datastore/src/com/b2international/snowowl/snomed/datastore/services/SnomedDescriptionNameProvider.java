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
package com.b2international.snowowl.snomed.datastore.services;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.b2international.commons.CompareUtils;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.ComponentIdAndLabel;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.api.IComponentNameProvider;
import com.b2international.snowowl.snomed.Description;
import com.b2international.snowowl.snomed.datastore.index.SnomedClientIndexService;
import com.b2international.snowowl.snomed.datastore.index.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.SnomedDescriptionIndexQueryAdapter;
import com.b2international.snowowl.snomed.datastore.index.SnomedDescriptionReducedQueryAdapter;
import com.google.common.base.Preconditions;

/**
 * Component name provider implementation for SNOMED CT descriptions.
 */
public enum SnomedDescriptionNameProvider implements IComponentNameProvider {

	INSTANCE;
	
	/**
	 * Accepts the followings as argument:
	 * <p>
	 * <ul>
	 * <li>{@link Description SNOMED CT description}</li>
	 * <li>{@link SnomedDescriptionIndexEntry SNOMED CT description (Lucene)}</li>
	 * <li>{@link String SNOMED CT description identifier as string}</li>
	 * <li>{@link IStatus Status}</li>
	 * </ul>
	 * </p>
	 */
	public String getText(final Object object) {
		if (object instanceof Description) {
			final Description description = (Description) object;
			final String label = description.getTerm();
			return label;
		} else if (object instanceof SnomedDescriptionIndexEntry) {
			return ((SnomedDescriptionIndexEntry) object).getLabel();
		} else if (object instanceof String) {
			final SnomedClientIndexService indexSearcher = getIndexService();
			final SnomedDescriptionIndexQueryAdapter queryBuilder = createQueryAdapter(object);
			final List<SnomedDescriptionIndexEntry> result = executeQuery(indexSearcher, queryBuilder);
			if (isValidResult(result)) {
				return result.get(0).getLabel();
			}
		}
		return null == object ? "" : String.valueOf(object);
	}

	@Override
	public String getComponentLabel(final IBranchPath branchPath, final String componentId) {
		Preconditions.checkNotNull(branchPath, "Branch path argument cannot be null.");
		Preconditions.checkNotNull(componentId, "Component ID argument cannot be null.");
		
		return ApplicationContext.getInstance().getService(ISnomedComponentService.class).getLabels(branchPath, componentId)[0];
	}
	
	// TODO
	@Override
	public ComponentIdAndLabel getComponentIdAndLabel(IBranchPath branchPath, long storageKey) {
		return null;
	}
	
	private List<SnomedDescriptionIndexEntry> executeQuery(final SnomedClientIndexService indexSearcher, final SnomedDescriptionIndexQueryAdapter queryBuilder) {
		return indexSearcher.search(queryBuilder, 1);
	}

	private boolean isValidResult(final Collection<SnomedDescriptionIndexEntry> result) {
		return !CompareUtils.isEmpty(result);
	}

	private SnomedDescriptionIndexQueryAdapter createQueryAdapter(final Object element) {
		return new SnomedDescriptionReducedQueryAdapter((String) element, SnomedDescriptionReducedQueryAdapter.SEARCH_DESCRIPTION_ID);
	}

	private SnomedClientIndexService getIndexService() {
		return ApplicationContext.getInstance().getService(SnomedClientIndexService.class);
	}
}
