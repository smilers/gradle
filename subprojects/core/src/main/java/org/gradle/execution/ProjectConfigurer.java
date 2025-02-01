/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.execution;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectState;
import org.gradle.internal.service.scopes.Scope;
import org.gradle.internal.service.scopes.ServiceScope;

@ServiceScope(Scope.BuildTree.class)
public interface ProjectConfigurer {
    /**
     * Configures the given project.
     */
    void configure(ProjectInternal project);

    /**
     * Configures the owned project, discovers tasks and binds model rules.
     */
    void configureFully(ProjectState projectState);

    /**
     * Configures the given project and all its subprojects.
     */
    void configureHierarchy(ProjectInternal project);

    /**
     * Configures the given project and all its subprojects in parallel.
     */
    void configureHierarchyInParallel(ProjectInternal project);
}
