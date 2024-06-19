/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.internal.component.resolution.failure.type;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.internal.component.model.ComponentGraphResolveState;
import org.gradle.internal.component.resolution.failure.interfaces.VariantSelectionByAttributesFailure;

import java.util.Set;

/**
 * An abstract {@link VariantSelectionByAttributesFailure} that represents the situation when a variant
 * was requested via variant-aware matching and that matching failed.
 */
public abstract class AbstractVariantSelectionByAttributesFailure implements VariantSelectionByAttributesFailure {
    private final ComponentGraphResolveState targetComponent;
    private final ImmutableAttributes requestedAttributes;
    private final ImmutableSet<Capability> requestedCapabilities;

    public AbstractVariantSelectionByAttributesFailure(ComponentGraphResolveState targetComponent, AttributeContainerInternal requestedAttributes, Set<Capability> requestedCapabilities) {
        this.targetComponent = targetComponent;
        this.requestedAttributes = requestedAttributes.asImmutable();
        this.requestedCapabilities = ImmutableSet.copyOf(requestedCapabilities);
    }

    @Override
    public String describeRequestTarget() {
        return targetComponent.getId().getDisplayName();
    }

    @Override
    public ComponentGraphResolveState getTargetComponentState() {
        return targetComponent;
    }

    @Override
    public ImmutableAttributes getRequestedAttributes() {
        return requestedAttributes;
    }

    @Override
    public ImmutableSet<Capability> getRequestedCapabilities() {
        return requestedCapabilities;
    }
}
