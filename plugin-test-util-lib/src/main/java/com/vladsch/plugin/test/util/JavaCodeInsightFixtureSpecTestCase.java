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

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.exceptionCases.AbstractExceptionCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.util.ThrowableRunnable;
import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.SpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import static org.junit.rules.ExpectedException.none;

@RunWith(value = Parameterized.class)
public abstract class JavaCodeInsightFixtureSpecTestCase extends JavaCodeInsightFixtureTestCase implements CodeInsightFixtureSpecTestCase {
    @Before
    public void before() throws Throwable {
        // NOTE: for UsefulTestCase setUp() should not be invoked from any @Before methods
    }

    @After
    public void after() throws Throwable {
        // NOTE: for UsefulTestCase tearDown() should not be invoked from any @After methods
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
        super.tuneFixture(moduleBuilder);

        //moduleBuilder.addJdk("/Applications/IntelliJ-IDEA-2019.3-CE-EAP.app/Contents/jbr/Contents/Home");
        moduleBuilder.setLanguageLevel(LanguageLevel.JDK_11);
//        moduleBuilder.addLibrary("test_lib",
//                "/Users/vlad/src/projects/idea-multimarkdown3/WebViewDebugSample/lib/annotations-18.0.0.jar"
//                , "/Users/vlad/src/projects/idea-multimarkdown3/lib/flexmark-parent.jar"
//                , "/Users/vlad/src/projects/idea-multimarkdown3/lib/flexmark-util.jar"
//                , "/Users/vlad/src/projects/idea-multimarkdown3/lib/flexmark-test-util.jar"
//                , "/Users/vlad/src/projects/plugin-test-util/lib/junit-4.12.jar"
//                , "/Users/vlad/src/projects/plugin-test-util/lib/hamcrest-core-1.3.jar"
//        );
    }

    @Override
    public String getName() {
        return "JavaSpecTestCase";
    }

    @Rule final public ExpectedException myThrown = none();

    @Parameterized.Parameter(0)
    public @NotNull SpecExample myExample = SpecExample.NULL;

    // standard options
    final private static Map<String, DataHolder> optionsMap = new HashMap<>();
    static {
        optionsMap.putAll(SpecTest.getOptionsMap());
    }

    private final Map<String, ? extends DataHolder> myOptionsMap;
    private final @NotNull DataHolder myDefaultOptions;

    public JavaCodeInsightFixtureSpecTestCase(@Nullable Map<String, ? extends DataHolder> optionMap, @Nullable DataHolder... defaultOptions) {
        // add standard options
        DataHolder options = TestUtils.combineDefaultOptions(defaultOptions);
        myDefaultOptions = options == null ? new DataSet() : options;
        myOptionsMap = CodeInsightFixtureSpecTestCase.optionsMaps(optionsMap, optionMap);
    }

    @Override
    final public DataHolder options(@NotNull String option) {
        return TestUtils.processOption(myOptionsMap, option);
    }

//    @Nullable
//    protected SpecTestCaseJavaProjectDescriptor getProjectDescriptor() {
//        return null;
//    }

    @Test
    final public void test_case() {
        defaultTestCase();
    }

    // CodeInsightFixtureSpecTestCase implementation
    // @formatter:off
    @Override final public PsiFile getFile() { return getFixture().getFile();}
    @Override final public EditorEx getEditor() { return (EditorEx) getFixture().getEditor();}
    // @formatter:on

    // CodeInsightFixtureSpecTestCase implementation
    // @formatter:off
    @Override @NotNull final public DataHolder getDefaultOptions() { return myDefaultOptions; }
    @Override final public CodeInsightTestFixture getFixture() { return myFixture;}
    @Override final public Logger LOG() { return LOG; }
    @Override final public @NotNull SpecExample getExample() { return myExample; }
    @Override final public @NotNull ExpectedException getThrown() { return myThrown; }
    // @formatter:on

    // Light platform/java methods pulled up to CodeInsightFixtureSpecTestCase
    // @formatter:off
    @Override final public PsiElementFactory getElementFactory() { return super.getElementFactory();}
    @Override @NotNull final public TempDirTestFixture getTempDirFixture() {return getFixture().getTempDirFixture();}

    @Override final public Project getProject() { return super.getProject();}
    @Override final public PsiManagerEx getPsiManager() { return super.getPsiManager();}
    @Override final public PsiFile createLightFile(FileType fileType, String text) { return getFixture().configureByText(fileType, text);}
    @Override final public PsiFile createLightFile(String fileName, Language language, String text) { return getFixture().configureByText(fileName, text);}
    @NotNull @Override final public Module getModule() { return super.getModule();}
    @Override final public void addSuppressedException(@NotNull Throwable e) { super.addSuppressedException(e);}
    @Override final public boolean shouldContainTempFiles() { return super.shouldContainTempFiles();}
    @Override final public boolean isIconRequired() { return super.isIconRequired();}
    //@Override final public void addTmpFileToKeep(@NotNull File file) { super.addTmpFileToKeep(file);}
    @NotNull @Override final public Disposable getTestRootDisposable() { return super.getTestRootDisposable();}
    @Override final public boolean shouldRunTest() { return super.shouldRunTest();}
    //@Override final public void invokeTestRunnable(@NotNull Runnable runnable) throws Exception { super.invokeTestRunnable(runnable);}
    @Override final public void defaultRunBare(@NotNull ThrowableRunnable<Throwable> testRunnable) throws Throwable { super.defaultRunBare(testRunnable);}
    //@Override final public void runBare() throws Throwable { super.runBare();}
    @Override final public boolean runInDispatchThread() { return super.runInDispatchThread();}
    @NotNull @Override final public <T extends Disposable> T disposeOnTearDown(@NotNull T disposable) { return super.disposeOnTearDown(disposable);}@NotNull
    @Override final public String getTestName(boolean lowercaseFirstLetter) { return super.getTestName(lowercaseFirstLetter);}
    @NotNull @Override final public String getTestDirectoryName() { return super.getTestDirectoryName();}
    @Override final public boolean isPerformanceTest() { return super.isPerformanceTest();}
    @Override final public boolean isStressTest() { return super.isStressTest();}
    @Override final public void assertException(@NotNull AbstractExceptionCase<?> exceptionCase) { super.assertException(exceptionCase);}
    @Override final public void assertException(@NotNull AbstractExceptionCase exceptionCase, @Nullable String expectedErrorMsg) { super.assertException(exceptionCase, expectedErrorMsg);}
    @Override final public <T extends Throwable> void assertNoException(@NotNull AbstractExceptionCase<T> exceptionCase) throws T { super.assertNoException(exceptionCase);}
    @Override final public void assertNoThrowable(@NotNull Runnable closure) { super.assertNoThrowable(closure);}
    @Override final public boolean annotatedWith(@NotNull Class<? extends Annotation> annotationClass) { return super.annotatedWith(annotationClass);}
    @NotNull @Override final public String getHomePath() { return super.getHomePath();}
    // @formatter:on
}
