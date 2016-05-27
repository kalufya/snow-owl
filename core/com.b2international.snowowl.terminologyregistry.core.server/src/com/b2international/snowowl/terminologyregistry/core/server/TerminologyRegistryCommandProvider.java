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
package com.b2international.snowowl.terminologyregistry.core.server;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.datastore.ICodeSystem;
import com.b2international.snowowl.terminologyregistry.core.index.TerminologyRegistryClientService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

/**
 * OSGI command contribution with Snow Owl terminology registry commands.
 * 
 *
 */
public class TerminologyRegistryCommandProvider implements CommandProvider {

	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("--- Snow Owl terminology registry commands ---\n");
		buffer.append("\tterminologyregistry listall - List all registered terminologies\n");
		buffer.append("\tterminologyregistry details [codesystemid] - Prints the detailed information of a particular terminology\n");
		buffer.append("\tterminologyregistry versions [codesystemid] - List all the available versions for a particular terminology\n");
		return buffer.toString();
	}

	/**
	 * Reflective template method declaratively registered. Needs to start with "_".
	 * @param interpreter
	 */
	public void _terminologyregistry(CommandInterpreter interpreter) {
		try {
			String cmd = interpreter.nextArgument();
			if ("listall".equals(cmd)) {
				listall(interpreter);
				return;
			}
			
			if ("details".equals(cmd)) {
				details(interpreter);
				return;
			}
			
			if ("versions".equals(cmd)) {
				versions(interpreter);
				return;
			}
			
			interpreter.println(getHelp());
		} catch (Exception ex) {
			interpreter.println(ex.getMessage());
		}
	}
	/*
	 * Print the versions
	 */
	private synchronized void versions(CommandInterpreter interpreter) {
		String codeSystemId = interpreter.nextArgument();
		if (Strings.isNullOrEmpty(codeSystemId)) {
			interpreter.print("Command usage: terminologyregistry versions [codesystemid]\n");
			return;
		}
		throw new RuntimeException("Not implemented");
	}

	/*
	 * Print the details of the code system
	 */
	private synchronized void details(CommandInterpreter interpreter) {
		String codeSystemId = interpreter.nextArgument();
		if (Strings.isNullOrEmpty(codeSystemId)) {
			interpreter.print("Command usage: terminologyregistry details [codesystemid]\n");
			return;
		}
		throw new RuntimeException("Not implemented");
	}

	/*
	 * List all registered terminologies
	 */
	private synchronized void listall(CommandInterpreter interpreter) {
		final TerminologyRegistryClientService service = ApplicationContext.getInstance().getService(TerminologyRegistryClientService.class);
		interpreter.print(Joiner.on("\n").join(FluentIterable.from(service.getCodeSystems()).transform(new Function<ICodeSystem, String>() {
			@Override public String apply(ICodeSystem input) {
				return getCodeSystemInformation(input, service);
			}
		})));
	}
	
	private String getCodeSystemInformation(ICodeSystem codeSystem, TerminologyRegistryClientService service) {
		StringBuilder builder = new StringBuilder();
		builder
			.append("Name: ").append(codeSystem.getName()).append("\n")
			.append("Short name: ").append(codeSystem.getShortName()).append("\n")
			.append("Code System OID: ").append(codeSystem.getOid()).append("\n")
			.append("Maintaining organization link: ").append(codeSystem.getOrgLink()).append("\n")
			.append("Language: ").append(codeSystem.getLanguage()).append("\n")
			.append("Last version: ").append(null == service.getVersionId(codeSystem) ? "N/A" : service.getVersionId(codeSystem)).append("\n")
			.append("Current branch path: ").append(codeSystem.getBranchPath()).append("\n");
		return builder.toString();
	}
	
}