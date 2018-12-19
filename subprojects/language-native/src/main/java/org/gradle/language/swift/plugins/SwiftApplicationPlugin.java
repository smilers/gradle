/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.language.swift.plugins;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.internal.NativeComponentFactory;
import org.gradle.language.nativeplatform.internal.Dimensions;
import org.gradle.language.nativeplatform.internal.toolchains.ToolChainSelector;
import org.gradle.language.swift.SwiftApplication;
import org.gradle.language.swift.SwiftExecutable;
import org.gradle.language.swift.SwiftPlatform;
import org.gradle.language.swift.internal.DefaultSwiftApplication;
import org.gradle.nativeplatform.TargetMachineFactory;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static org.gradle.language.nativeplatform.internal.Dimensions.isBuildable;

/**
 * <p>A plugin that produces an executable from Swift source.</p>
 *
 * <p>Adds compile, link and install tasks to build the executable. Defaults to looking for source files in `src/main/swift`.</p>
 *
 * <p>Adds a {@link SwiftApplication} extension to the project to allow configuration of the executable.</p>
 *
 * @since 4.5
 */
@Incubating
public class SwiftApplicationPlugin implements Plugin<ProjectInternal> {
    private final NativeComponentFactory componentFactory;
    private final ToolChainSelector toolChainSelector;
    private final ImmutableAttributesFactory attributesFactory;
    private final TargetMachineFactory targetMachineFactory;

    /**
     * Injects a {@link FileOperations} instance.
     *
     * @since 4.2
     */
    @Inject
    public SwiftApplicationPlugin(NativeComponentFactory componentFactory, ToolChainSelector toolChainSelector, ImmutableAttributesFactory attributesFactory, TargetMachineFactory targetMachineFactory) {
        this.componentFactory = componentFactory;
        this.toolChainSelector = toolChainSelector;
        this.attributesFactory = attributesFactory;
        this.targetMachineFactory = targetMachineFactory;
    }

    @Override
    public void apply(final ProjectInternal project) {
        project.getPluginManager().apply(SwiftBasePlugin.class);

        final ObjectFactory objectFactory = project.getObjects();
        final ProviderFactory providers = project.getProviders();

        // Add the application and extension
        final DefaultSwiftApplication application = componentFactory.newInstance(SwiftApplication.class, DefaultSwiftApplication.class, "main");
        project.getExtensions().add(SwiftApplication.class, "application", application);
        project.getComponents().add(application);

        // Setup component
        application.getModule().set(GUtil.toCamelCase(project.getName()));

        application.getTargetMachines().convention(Dimensions.getDefaultTargetMachines(targetMachineFactory));
        application.getDevelopmentBinary().convention(project.provider(() -> {
            return application.getBinaries().get().stream()
                    .filter(SwiftExecutable.class::isInstance)
                    .map(SwiftExecutable.class::cast)
                    .filter(binary -> !binary.isOptimized() && binary.getTargetPlatform().getArchitecture().equals(DefaultNativePlatform.host().getArchitecture()))
                    .findFirst()
                    .orElse(application.getBinaries().get().stream()
                            .filter(SwiftExecutable.class::isInstance)
                            .map(SwiftExecutable.class::cast)
                            .filter(binary -> !binary.isOptimized())
                            .findFirst()
                            .orElse(null));
        }));

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project project) {
                // TODO: make build type configurable for components
                Dimensions.applicationVariants(application.getModule(), application.getTargetMachines(), objectFactory, attributesFactory,
                        providers.provider(() -> project.getGroup().toString()), providers.provider(() -> project.getVersion().toString()),
                        variantIdentity -> {
                    if (isBuildable(variantIdentity)) {
                        ToolChainSelector.Result<SwiftPlatform> result = toolChainSelector.select(SwiftPlatform.class, variantIdentity.getTargetMachine());
                        application.addExecutable(variantIdentity, variantIdentity.isDebuggable() && !variantIdentity.isOptimized(), result.getTargetPlatform(), result.getToolChain(), result.getPlatformToolProvider());
                    }
                });

                // Configure the binaries
                application.getBinaries().realizeNow();
            }
        });
    }
}
