/*
 * Copyright 2017-2021 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.b2international.index.Index;
import com.b2international.index.Indexes;
import com.b2international.index.mapping.Mappings;
import com.b2international.snowowl.core.ComponentIdentifier;
import com.b2international.snowowl.core.IDisposableService;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.internal.validation.ValidationRepository;
import com.b2international.snowowl.core.repository.JsonSupport;
import com.b2international.snowowl.core.validation.issue.ValidationIssue;
import com.b2international.snowowl.core.validation.whitelist.ValidationWhiteList;
import com.b2international.snowowl.core.validation.whitelist.ValidationWhiteLists;
import com.b2international.snowowl.eventbus.EventBusUtil;
import com.b2international.snowowl.eventbus.IEventBus;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 6.1
 */
public class ValidationWhiteListApiTest {

	private static final ComponentIdentifier COMPONENT_ID = ComponentIdentifier.of("concept", "123");
	
	private ServiceProvider context;
	
	@Before
	public void setup() {
		final ObjectMapper mapper = JsonSupport.getDefaultObjectMapper();
		final Index index = Indexes.createIndex(UUID.randomUUID().toString(), mapper, new Mappings(ValidationWhiteList.class, ValidationIssue.class));
		index.admin().create();
		final ValidationRepository repository = new ValidationRepository(index);
		context = ServiceProvider.EMPTY.inject()
				.bind(IEventBus.class, EventBusUtil.getBus())
				.bind(ValidationRepository.class, repository)
				.build();
	}
	
	@After
	public void after() {
		context.service(ValidationRepository.class).admin().delete();
		if(context instanceof IDisposableService) {
			((IDisposableService) context).dispose();
		}
	}
	
	@Test
	public void createWhiteList() throws Exception {
		final String whiteListId = createWhiteLists("58", COMPONENT_ID);
		
		assertThat(whiteListId).isNotEmpty();
		
		final ValidationWhiteList whiteList = ValidationRequests.whiteList().prepareGet(whiteListId).buildAsync().getRequest().execute(context);
		assertThat(whiteList.getRuleId()).isEqualTo("58");
		assertThat(whiteList.getComponentIdentifier()).isEqualTo(COMPONENT_ID);
	}

	@Test
	public void deleteWhiteList() throws Exception {
		final String whiteList1 = createWhiteLists("1", COMPONENT_ID);
		final String whiteList2 = createWhiteLists("2", COMPONENT_ID);
		
		ValidationRequests.whiteList().prepareDelete(whiteList2).buildAsync().getRequest().execute(context);
		
		final ValidationWhiteLists WhiteLists = getWhiteLists();
		
		assertThat(WhiteLists.stream().map(ValidationWhiteList::getId).collect(Collectors.toList())).doesNotContain(whiteList2);
		assertThat(WhiteLists.getItems().size()).isEqualTo(1);
	}

	@Test
	public void filterByRuleId() throws Exception{
		createWhiteLists("3", COMPONENT_ID);
		createWhiteLists("4", COMPONENT_ID);
		
		final ValidationWhiteLists whiteLists = ValidationRequests.whiteList().prepareSearch()
			.filterByRuleId("3")
			.buildAsync().getRequest()
			.execute(context);
	
		assertThat(whiteLists).hasSize(1);
		assertThat(whiteLists.first().get().getRuleId()).isEqualTo("3");
	}
	
	@Test
	public void filterByComponentIdentifier() throws Exception{
		final ComponentIdentifier componentIdentifier = ComponentIdentifier.of("concept", "12345678");
		
		createWhiteLists("5", COMPONENT_ID);
		createWhiteLists("6", componentIdentifier);
		
		final ValidationWhiteLists whiteLists = ValidationRequests.whiteList().prepareSearch()
			.filterByComponentIdentifier(componentIdentifier.getComponentId())
			.filterByComponentType(componentIdentifier.getComponentType())
			.buildAsync()
			.getRequest()
			.execute(context);
		
		assertThat(whiteLists).hasSize(1);
		assertThat(whiteLists.first().get().getComponentIdentifier().getComponentId()).isEqualTo(componentIdentifier.getComponentId());
	}
	
	@Test
	public void getAllValidationWhiteLists() throws Exception {
		final ValidationWhiteLists validationWhiteLists = getWhiteLists();
		assertThat(validationWhiteLists).isEmpty();
	}
	
	
	private String createWhiteLists(final String ruleId, final ComponentIdentifier componentIdentifier) {
		return ValidationRequests.whiteList().prepareCreate()
				.setReporter("test")
				.setCreatedAt(new Date().getTime())
				.setAffectedComponentLabels(Collections.emptyList())
				.setRuleId(ruleId)
				.setComponentIdentifier(componentIdentifier)
				.buildAsync().getRequest()
				.execute(context);
	}
	
	private ValidationWhiteLists getWhiteLists() throws Exception {
		return ValidationRequests.whiteList().prepareSearch()
			.all()
			.buildAsync().getRequest()
			.execute(context);
	}
	
}