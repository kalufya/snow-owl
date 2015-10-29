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
package com.b2international.snowowl.snomed.datastore.factory;

import com.b2international.commons.TypeSafeAdapterFactory;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IComponent;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.datastore.cdo.CDOUtils;
import com.b2international.snowowl.snomed.Concept;
import com.b2international.snowowl.snomed.datastore.SnomedClientTerminologyBrowser;
import com.b2international.snowowl.snomed.datastore.SnomedConceptIndexEntry;
import com.b2international.snowowl.snomed.datastore.SnomedIconProvider;
import com.b2international.snowowl.snomed.datastore.services.SnomedConceptNameProvider;

/**
 */
public class SnomedConceptAdapterFactory extends TypeSafeAdapterFactory {

	/*
	 * (non-Javadoc)
	 * @see com.b2international.commons.TypeSafeAdapterFactory#getAdapterSafe(java.lang.Object, java.lang.Class)
	 */
	@Override
	public <T> T getAdapterSafe(final Object adaptableObject, final Class<T> adapterType) {

		if (null == adaptableObject) {
			return null;
		}
		
		if (IComponent.class != adapterType) {
			return null;
		}
		
		if (adaptableObject instanceof SnomedConceptIndexEntry || adaptableObject instanceof SnomedConceptIndexEntry) {
			return adapterType.cast(adaptableObject);
		}
		
		if (adaptableObject instanceof Concept) {

			final Concept concept = (Concept) adaptableObject;
			final SnomedClientTerminologyBrowser terminologyBrowser = getTerminologyBrowser();
			SnomedConceptIndexEntry indexEntry = terminologyBrowser.getConcept(concept.getId());

			if (null != indexEntry) {
				return adapterType.cast(indexEntry);
			}
			
			indexEntry = SnomedConceptIndexEntry.builder()
					.id(concept.getId())
					.label(SnomedConceptNameProvider.INSTANCE.getText(concept.getId(), concept.cdoView())) 
					.iconId(SnomedIconProvider.getInstance().getIconComponentId(concept.getId())) 
					.moduleId(concept.getModule().getId()) 
					.storageKey(CDOUtils.getStorageKey(concept))
					.active(concept.isActive())
					.primitive(concept.isPrimitive())
					.exhaustive(concept.isExhaustive())
					.released(concept.isReleased()) 
					.effectiveTimeLong(concept.isSetEffectiveTime() ? concept.getEffectiveTime().getTime() : EffectiveTimes.UNSET_EFFECTIVE_TIME)
					.build();
			
			return adapterType.cast(indexEntry);
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.b2international.commons.TypeSafeAdapterFactory#getAdapterListSafe()
	 */
	@Override
	public Class<?>[] getAdapterListSafe() {
		return new Class<?>[] { IComponent.class };
	}

	protected SnomedClientTerminologyBrowser getTerminologyBrowser() {
		return ApplicationContext.getInstance().getService(SnomedClientTerminologyBrowser.class);
	}
}