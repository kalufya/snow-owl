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
package com.b2international.snowowl.fhir.core.model.valueset.expansion;

import com.b2international.snowowl.fhir.core.model.dt.FhirDataType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Integer expansion parameter
 * @since 6.7
 */
@JsonDeserialize(using = JsonDeserializer.None.class, builder = IntegerParameter.Builder.class)
public class IntegerParameter extends Parameter<Integer> {

	IntegerParameter(String name, Integer value) {
		super(name, value);
	}
	
	@Override
	public FhirDataType getType() {
		return FhirDataType.INTEGER;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	@JsonPOJOBuilder(withPrefix = "")
	public static class Builder extends Parameter.Builder<Builder, IntegerParameter, Integer> {
		
		@Override
		protected Builder getSelf() {
			return this;
		}
		
		/*
		 * For deserialization support.
		 */
		protected Builder valueInteger(final Integer value) {
			this.value = value;
			return this;
		}

		@Override
		protected IntegerParameter doBuild() {
			return new IntegerParameter(name, value);
		}
	}

}
