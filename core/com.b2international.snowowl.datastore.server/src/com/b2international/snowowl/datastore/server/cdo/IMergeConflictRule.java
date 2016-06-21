/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.server.cdo;

import java.util.Collection;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import com.b2international.snowowl.core.merge.MergeConflict;

/**
 * Generic interface for merge conflict rules
 * 
 * @since 4.7
 */
public interface IMergeConflictRule {

	/**
	 * Executes the given conflict rule and returns a collection of {@link MergeConflict} if there was any.
	 * 
	 * @param transaction
	 * @return
	 */
	Collection<MergeConflict> validate(CDOBranch sourceBranch, CDOTransaction targetTransaction);
	
}
