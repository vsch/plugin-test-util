/*
 * Copyright (c) 2015-2019 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the copyright holder.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.vladsch.plugin.test.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.pom.java.AcceptedLanguageLevelsSettings;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.MavenDependencyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpecTestCaseJavaProjectDescriptor extends LightProjectDescriptor {
    public static final String MAVEN_PREFIX = "maven:";
    protected final LanguageLevel myLanguageLevel;
    protected final String[] myModuleLibraries;

    public SpecTestCaseJavaProjectDescriptor(@NotNull LanguageLevel languageLevel, String... moduleLibraries) {
        myLanguageLevel = languageLevel;
        myModuleLibraries = moduleLibraries;
    }

    public LanguageLevel getLanguageLevel() {
        return myLanguageLevel;
    }

    @Override
    public void setUpProject(@NotNull Project project, @NotNull SetupHandler handler) throws Exception {
        if (myLanguageLevel.isPreview() || myLanguageLevel == LanguageLevel.JDK_X) {
            AcceptedLanguageLevelsSettings.allowLevel(project, myLanguageLevel);
        }
        super.setUpProject(project, handler);
    }

    @Override
    public Sdk getSdk() {
        return IdeaTestUtil.getMockJdk(myLanguageLevel.toJavaVersion());
    }

    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(myLanguageLevel);
        List<String> jarLibs = new ArrayList<>();

        for (String library : myModuleLibraries) {
            if (library.startsWith(MAVEN_PREFIX)) {
                // use maven dependency
                MavenDependencyUtil.addFromMaven(model, library.substring(MAVEN_PREFIX.length()));
            } else {
                jarLibs.add(library);
            }
        }

        if (jarLibs.size() > 0) {
            PsiTestUtil.addProjectLibrary(module, "test-lib", jarLibs);
        }
    }

    @NotNull
    @Override
    public String getModuleTypeId() {
        return ModuleTypeId.JAVA_MODULE;
    }
}
