/*
 * Copyright (c) 2015-2019 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the of the copyright holder.
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
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.pom.java.AcceptedLanguageLevelsSettings;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SpecTestCaseJavaProjectDescriptor extends LightProjectDescriptor {
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

    public void addModuleLibrary(@NotNull Module module, String... libraryJars) {
        for (String library : libraryJars) {
            ModuleRootModificationUtil.addModuleLibrary(module, library);
        }
    }

    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(myLanguageLevel);
        PsiTestUtil.addProjectLibrary(module, "test-lib", Arrays.asList(myModuleLibraries));
    }

    @NotNull
    @Override
    public String getModuleTypeId() {
        return ModuleTypeId.JAVA_MODULE;
    }
}
