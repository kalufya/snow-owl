/*
 * Copyright 2020 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.uri;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.b2international.snowowl.core.ComponentIdentifier;
import com.b2international.snowowl.core.terminology.TerminologyRegistry;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * @since 7.7
 */
@JsonDeserialize(using = ComponentURI.ComponentURIDeserializer.class)
public final class ComponentURI implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final class ComponentURIDeserializer extends JsonDeserializer<ComponentURI> {
		@Override
		public ComponentURI deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
			return of(parser.getValueAsString());
		}
	}
	
	public static final class ComponentURIKeyDeserializer extends KeyDeserializer {
		
		@Override
		public Object deserializeKey(String key, DeserializationContext context) throws IOException, JsonProcessingException {
			return of(key);
		}
		
	}
	
	/**
	 * Keeps weak references to every created {@link ComponentURI} in this JVM.
	 */
	private static final Interner<ComponentURI> COMPONENT_URI_INTERNER = Interners.newWeakInterner();
	
	protected static final Splitter SLASH_SPLITTER = Splitter.on('/');
	protected static final Joiner SLASH_JOINER = Joiner.on('/');
		
	@JsonIgnore
	public static final ComponentURI UNSPECIFIED = ComponentURI.of(TerminologyRegistry.UNSPECIFIED, TerminologyRegistry.UNSPECIFIED_NUMBER_SHORT, "");
	
	private final String codeSystem;
	private final short terminologyComponentId;
	private final String identifier;
	
	public String codeSystem() {
		return codeSystem;
	}
	
	public short terminologyComponentId() {
		return terminologyComponentId;
	}
	
	public String identifier() {
		return identifier;
	}

	@JsonIgnore
	public final boolean isUnspecified() {
		return TerminologyRegistry.UNSPECIFIED.equals(codeSystem());
	}
	
	public List<String> parts() {
		return ImmutableList.of(codeSystem(), Short.toString(terminologyComponentId()), identifier());
	}
		
	public final ComponentIdentifier toComponentIdentifier() {
		return ComponentIdentifier.of(terminologyComponentId(), identifier());
	}

	private ComponentURI(String codeSystem, short terminologyComponentId, String identifier) {
		checkArgument(!Strings.isNullOrEmpty(codeSystem), "Codesystem argument should not be null.");
		checkArgument(terminologyComponentId >= TerminologyRegistry.UNSPECIFIED_NUMBER_SHORT,
				"Terminology component id should be either unspecified (-1) or greater than zero. Got: '%s'.", terminologyComponentId);
		this.codeSystem = codeSystem;
		this.terminologyComponentId = terminologyComponentId;
		this.identifier = identifier;
	}
	
	@JsonCreator
	public static ComponentURI of(String codeSystem, short terminologyComponentId, String identifier) {
		return getOrCache(new ComponentURI(codeSystem, terminologyComponentId, Strings.nullToEmpty(identifier)));
	}
	
	private static ComponentURI getOrCache(final ComponentURI componentURI) {
		return COMPONENT_URI_INTERNER.intern(componentURI);
	}
	
	public static ComponentURI of(String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			return ComponentURI.UNSPECIFIED;
		}
		final List<String> parts = SLASH_SPLITTER.splitToList(uri);
		checkArgument(parts.size() == 3, "A component uri consists of three parts (codeSystem/componentType/componentId). Arg was: %s", uri);
		return of(parts.get(0), Short.valueOf(parts.get(1)), parts.get(2));
	}

	@JsonValue
	@Override
	public String toString() {
		return SLASH_JOINER.join(parts());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(codeSystem, terminologyComponentId, identifier);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ComponentURI other = (ComponentURI) obj;
		return Objects.equals(codeSystem, other.codeSystem)
				&& terminologyComponentId == other.terminologyComponentId
				&& Objects.equals(identifier, other.identifier);
	}
	
}
