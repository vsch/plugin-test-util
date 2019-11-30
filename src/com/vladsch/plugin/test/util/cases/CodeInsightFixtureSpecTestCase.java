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

package com.vladsch.plugin.test.util.cases;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.exceptionCases.AbstractExceptionCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ThrowableRunnable;
import com.vladsch.flexmark.test.util.DumpSpecReader;
import com.vladsch.flexmark.test.util.ExceptionMatcher;
import com.vladsch.flexmark.test.util.SpecExampleParse;
import com.vladsch.flexmark.test.util.SpecExampleRenderer;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.test.util.spec.SpecReader;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.plugin.test.util.IntentionInfo;
import com.vladsch.plugin.test.util.renderers.LightFixtureSpecRenderer;
import com.vladsch.plugin.util.TestUtils;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.ComparisonFailure;
import org.junit.rules.ExpectedException;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Comparator.comparingInt;

public interface CodeInsightFixtureSpecTestCase extends SpecTest {
    String BANNER_PADDING = "------------------------------------------------------------------------";
    int BANNER_LENGTH = BANNER_PADDING.length();
    String BANNER_AST = bannerText("AST");
    String BANNER_QUICK_FIXES = bannerText("QUICK_FIXES");
    String BANNER_RANGES = bannerText("RANGES");
    String BANNER_AFTER_ACTION = bannerText("After Action");
    String BANNER_BEFORE_ACTION = bannerText("Before Action");
    ExceptionMatcher EXCEPTION_MATCHER = ExceptionMatcher.matchPrefix(RuntimeException.class, "junit.framework.ComparisonFailure: ");

    Map<String, DataHolder> optionsMap = new HashMap<>();

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            return optionsMap;
        }
    }

    @NotNull
    static String bannerText(@NotNull String message) {
        int leftPadding = (BANNER_LENGTH - message.length() - 2) >> 1;
        int rightPadding = BANNER_LENGTH - message.length() - 2 - leftPadding;
        return BANNER_PADDING.substring(0, leftPadding) + " " + message + " " + BANNER_PADDING.substring(0, rightPadding) + "\n";
    }

    static void appendBanner(@NotNull StringBuilder out, @NotNull String banner) {
        if (out.length() > 0) {
            out.append("\n");
        }

        out.append(banner);
    }

    static void appendBannerIfNeeded(@NotNull StringBuilder out, @NotNull String banner) {
        if (out.length() > 0) {
            out.append("\n");
            out.append(banner);
        }
    }

    @NotNull
    static List<Object[]> getTests(@NotNull ResourceLocation location) {
        return com.vladsch.flexmark.test.util.TestUtils.getTestData(location);
    }

    @Nullable
    static Map<String, ? extends DataHolder> optionsMaps(@Nullable Map<String, ? extends DataHolder> other, @Nullable Map<String, ? extends DataHolder> overrides) {
        return com.vladsch.flexmark.test.util.TestUtils.optionsMaps(other, overrides);
    }

    @Nullable
    static DataHolder[] dataHolders(@Nullable DataHolder other, @Nullable DataHolder[] overrides) {
        return com.vladsch.flexmark.test.util.TestUtils.dataHolders(other, overrides);
    }

    static void executeAction(@NonNls @NotNull String actionId, @NotNull Editor editor, Project project) {
        CommandProcessor.getInstance().executeCommand(project, () -> EditorTestUtil.executeAction(editor, actionId), "", null, editor.getDocument());
    }

    default String resolveIconName(@Nullable Icon icon) {
        return String.valueOf(icon);
    }

    @SuppressWarnings("rawtypes")
    @NotNull
    static String getActualLineMarkerFileText(@NotNull Document myDocument, @NotNull Collection<? extends LineMarkerInfo> markerInfos, @NotNull Function<Icon, String> iconResolver) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        List<LineMarkerInfo> lineMarkerInfos = new ArrayList<>(markerInfos);
        lineMarkerInfos.sort(comparingInt(o -> o.startOffset));
        String documentText = myDocument.getText();
        for (LineMarkerInfo expectedLineMarker : lineMarkerInfos) {
            result.append(documentText, index, expectedLineMarker.startOffset)
                    .append("<lineMarker ")
                    .append("icon=\"").append(iconResolver.apply(expectedLineMarker.getIcon())).append("\" ")
                    .append("descr=\"").append(expectedLineMarker.getLineMarkerTooltip()).append("\" ")
                    .append(">")
                    .append(documentText, expectedLineMarker.startOffset, expectedLineMarker.endOffset)
                    .append("</lineMarker>");
            index = expectedLineMarker.endOffset;
        }
        result.append(documentText, index, myDocument.getTextLength());
        return result.toString();
    }

    static void removeDuplicatedRangesForInjected(@NotNull List<? extends HighlightInfo> infos) {
        infos.sort((o1, o2) -> {
            final int i = o2.startOffset - o1.startOffset;
            return i != 0 ? i : o1.getSeverity().myVal - o2.getSeverity().myVal;
        });
        HighlightInfo prevInfo = null;
        for (Iterator<? extends HighlightInfo> it = infos.iterator(); it.hasNext(); ) {
            final HighlightInfo info = it.next();
            if (prevInfo != null &&
                    info.getSeverity() == HighlightInfoType.SYMBOL_TYPE_SEVERITY &&
                    info.getDescription() == null &&
                    info.startOffset == prevInfo.startOffset &&
                    info.endOffset == prevInfo.endOffset) {
                it.remove();
            }
            prevInfo = info.type == HighlightInfoType.INJECTED_LANGUAGE_FRAGMENT ? info : null;
        }
    }

    static void clearFields(@NotNull Object test) throws IllegalAccessException {
        Class<?> aClass = test.getClass();
        while (aClass != null) {
            UsefulTestCase.clearDeclaredFields(test, aClass);
            aClass = aClass.getSuperclass();
        }
    }

    @NotNull
    static String getExampleFileName(@NotNull Class<?> testCaseClass, @NotNull SpecExample example, @NotNull DataHolder options) {
        String name = SpecTest.EXAMPLE_SOURCE_NAME.get(options);
        if (!name.isEmpty()) return name;
        else return (testCaseClass.getSimpleName() + "_" + (example.getSection() != null ? example.getSection().replace(' ', '_') + "_" : "") +
                example.getExampleNumber()) +
                SpecTest.EXAMPLE_SOURCE_EXTENSION.get(options);
    }

    @NotNull
    static String getExampleName(@NotNull SpecExample example) {
        return String.format("[%s]", example.toString());
    }

    @NotNull
    default List<IntentionInfo> getAvailableIntentionsWithRanges(@NotNull LightFixtureSpecRenderer<?> specRenderer, @NotNull Editor editor, @NotNull PsiFile file, boolean atCaretOnly) {
        // NOTE: needed to simulate getting code analyzer topic
        beforeDoHighlighting(specRenderer, file);

        IdeaTestExecutionPolicy current = IdeaTestExecutionPolicy.current();
        if (current != null) {
            current.waitForHighlighting(file.getProject(), editor);
        }

        List<IntentionInfo> intentions = new ArrayList<>();

        Integer atOffset = atCaretOnly ? editor.getCaretModel().getOffset() : null;
        DaemonCodeAnalyzerEx.processHighlights(editor.getDocument(), file.getProject(), HighlightSeverity.INFORMATION, 0, editor.getDocument().getTextLength(), info -> {
            collectIntentionActions(atOffset, info, false, editor, file, intentions);
            return true;
        });

        List<HighlightInfo> infos = DaemonCodeAnalyzerEx.getInstanceEx(file.getProject()).getFileLevelHighlights(file.getProject(), file);
        for (HighlightInfo info : infos) {
            collectIntentionActions(null, info, true, editor, file, intentions);
        }

        intentions.sort(IntentionInfo::compareTo);

        return intentions;
    }

    static void collectIntentionActions(@Nullable Integer atOffset, HighlightInfo info, boolean fileLevel, @NotNull Editor editor, @NotNull PsiFile file, List<IntentionInfo> intentions) {
        List<Pair<HighlightInfo.IntentionActionDescriptor, TextRange>> fixRanges = info.quickFixActionRanges;
        if (fixRanges != null) {
            for (Pair<HighlightInfo.IntentionActionDescriptor, TextRange> pair : fixRanges) {
                if (atOffset == null || pair.second.contains(atOffset) || pair.second.getEndOffset() == atOffset) {
                    HighlightInfo.IntentionActionDescriptor actionInGroup = pair.first;
                    IntentionAction action = actionInGroup.getAction();
                    if (action.isAvailable(file.getProject(), editor, file)) {

                        List<IntentionAction> options = actionInGroup.getOptions(file, editor);
                        ArrayList<IntentionInfo> subActions = new ArrayList<>();
                        if (options != null) {
                            for (IntentionAction subAction : options) {
                                if (subAction.isAvailable(file.getProject(), editor, file)) {
                                    subActions.add(IntentionInfo.of(fileLevel, subAction));
                                }
                            }
                        }

                        intentions.add(IntentionInfo.of(fileLevel, action, pair.second, subActions.toArray(IntentionInfo.EMPTY_INTENTION_INFO)));
                    }
                }
            }
        }
    }

    LightFixtureSpecRenderer<?> createExampleSpecRenderer(@NotNull SpecExample example, @Nullable DataHolder options);

    /**
     * Load extra settings and initialize spec renderer for parse
     */
    <T extends CodeInsightFixtureSpecTestCase> void initializeRenderer(@NotNull LightFixtureSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     * Reset extra settings for next test and clean up any resources
     */
    <T extends CodeInsightFixtureSpecTestCase> void finalizeRenderer(@NotNull LightFixtureSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     * Add extra rendered info to spec test AST
     */
    <T extends CodeInsightFixtureSpecTestCase> void renderSpecTestAst(@NotNull StringBuilder ast, @NotNull LightFixtureSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    @NotNull
    @Override
    default LightFixtureSpecRenderer<?> getSpecExampleRenderer(@NotNull SpecExample example, @Nullable DataHolder exampleOptions) {
        if (exampleOptions == null) {
            return createExampleSpecRenderer(example, getDefaultOptions());
        } else {
            DataHolder options = DataSet.aggregate(getDefaultOptions(), exampleOptions);
            return createExampleSpecRenderer(example, options);
        }
    }

    default VirtualFile createImageFile(String fileName, InputStream content) {
        TempDirTestFixture fixture = getFixture().getTempDirFixture();
        VirtualFile virtualFile = fixture.createFile(fileName);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                byte[] bytes = IOUtils.toByteArray(content);
                virtualFile.setBinaryContent(bytes);
            } catch (IOException e) {
                LOG().error(e);
            }
        });

        return virtualFile;
    }

    @Override
    default void addFullSpecExample(@NotNull SpecExampleRenderer exampleRenderer, @NotNull SpecExampleParse exampleParse, @Nullable DataHolder exampleOptions, boolean ignoredTestCase, @NotNull String html, @Nullable String ast) {
        // called from dumpSpecReader as it is accumulating tests
    }

    default void fullTestSpecStarting() {

    }

    default void fullTestSpecComplete() {

    }

    default void beforeDoHighlighting(@NotNull LightFixtureSpecRenderer<?> specRenderer, @NotNull PsiFile file) {

    }

    @NotNull
    default DumpSpecReader create(@NotNull ResourceLocation location) {
        return SpecReader.create(location, (stream, fileUrl) -> new DumpSpecReader(stream, this, fileUrl, true));
    }

    @NotNull
    @Override
    default SpecExample checkExample(@NotNull SpecExample example) {
        return example;
    }

    @NotNull
    DataHolder getDefaultOptions();

    Logger LOG();

    @NotNull
    SpecExample getExample();

    @NotNull
    ExpectedException getThrown();

    PsiElementFactory getElementFactory();

    @NotNull
    TempDirTestFixture getTempDirFixture();

    Project getProject();

    // to make it same as LightPlatformCodeInsightTestCase
    default EditorEx getEditor() { return (EditorEx) getFixture().getEditor();}

    /**
     * Returns the offset of the caret in the in-memory editor instance.
     *
     * @return the offset of the caret in the in-memory editor instance.
     */
    default int getCaretOffset() { return getFixture().getCaretOffset();}

    /**
     * Returns the file currently loaded into the in-memory editor.
     *
     * @return the file currently loaded into the in-memory editor.
     */
    default PsiFile getFile() { return getFixture().getFile();}

    PsiManager getPsiManager();

    PsiFile createLightFile(FileType fileType, String text);

    PsiFile createLightFile(String fileName, Language language, String text);

    @NotNull
    Module getModule();

    void addSuppressedException(@NotNull Throwable e);

    boolean shouldContainTempFiles();

    boolean isIconRequired();

    void addTmpFileToKeep(@NotNull File file);

    @NotNull
    Disposable getTestRootDisposable();

    boolean shouldRunTest();

    void invokeTestRunnable(@NotNull Runnable runnable) throws Exception;

    void defaultRunBare() throws Throwable;

    void runBare() throws Throwable;

    boolean runInDispatchThread();

    void edt(@NotNull ThrowableRunnable<Throwable> runnable);

    @NotNull
    <T extends Disposable> T disposeOnTearDown(@NotNull T disposable);

    @NotNull
    String getTestName(boolean lowercaseFirstLetter);

    @NotNull
    String getTestDirectoryName();

    boolean isPerformanceTest();

    boolean isStressTest();

    void assertException(@NotNull AbstractExceptionCase<?> exceptionCase);

    void assertException(@NotNull AbstractExceptionCase<?> exceptionCase, @Nullable String expectedErrorMsg);

    <T extends Throwable> void assertNoException(@NotNull AbstractExceptionCase<T> exceptionCase) throws T;

    void assertNoThrowable(@NotNull Runnable closure);

    boolean annotatedWith(@NotNull Class<? extends Annotation> annotationClass);

    @NotNull
    String getHomePath();

    CodeInsightTestFixture getFixture();

    default void executeAction(@NonNls @NotNull String actionId) {
        executeAction(actionId, getEditor());
    }

    default void executeAction(@NonNls @NotNull String actionId, @NotNull Editor editor) {
        executeAction(actionId, editor, getProject());
    }

    @NotNull
    default String getResultTextWithMarkup(boolean withCarets, boolean withTestCaretMarkup) {
        Editor editor = getEditor();
        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

        if (!withCarets) return editor.getDocument().getText();
        else return TestUtils.getEditorTextWithCaretMarkup(editor, withTestCaretMarkup, LOG());
    }

    default com.vladsch.flexmark.util.Pair<String, String> collectAndCheckHighlighting(
            @NotNull LightFixtureSpecRenderer<?> specRenderer,
            boolean checkLineMarkers,
            boolean checkWarnings,
            boolean checkInfos,
            boolean checkWeakWarnings,
            boolean ignoreExtraHighlighting
    ) {
        ExpectedHighlightingData data = new ExpectedHighlightingData(
                getEditor().getDocument(), checkWarnings, checkWeakWarnings, checkInfos, ignoreExtraHighlighting, getHostFile());
        data.init();
        return collectAndCheckHighlighting(specRenderer, data, checkLineMarkers);
    }

    default PsiFile getHostFile() {
        VirtualFile myFile = getFile().getVirtualFile();
        VirtualFile hostVFile = myFile instanceof VirtualFileWindow ? ((VirtualFileWindow) myFile).getDelegate() : myFile;
        return ReadAction.compute(() -> PsiManager.getInstance(getProject()).findFile(hostVFile));
    }

    default com.vladsch.flexmark.util.Pair<String, String> collectAndCheckHighlighting(@NotNull LightFixtureSpecRenderer<?> specRenderer, @NotNull ExpectedHighlightingData data, boolean checkLineMarkers) {
        final Project project = getProject();
        EdtTestUtil.runInEdtAndWait(() -> PsiDocumentManager.getInstance(project).commitAllDocuments());

        PsiFileImpl file = (PsiFileImpl) getHostFile();
        FileElement hardRefToFileElement = file.calcTreeElement();//to load text

        // to load AST for changed files before it's prohibited by "fileTreeAccessFilter"
        CodeInsightTestFixtureImpl.ensureIndexesUpToDate(project);

        final long start = System.currentTimeMillis();
        final VirtualFile virtualFile = file.getVirtualFile();

        // NOTE: needed to simulate getting code analyzer topic
        beforeDoHighlighting(specRenderer, file);

        // NOTE: line markers may trigger access to project files which the fileTreeAccessFilter blocks, so we limit filter to only the file being checked
        final VirtualFileFilter fileTreeAccessFilter = new VirtualFileFilter() {
            @Override
            public boolean accept(VirtualFile file) {
                if (file instanceof VirtualFileWindow || !file.equals(virtualFile)) return false;

                FileType fileType = file.getFileType();
                return (fileType == StdFileTypes.JAVA || fileType == StdFileTypes.CLASS) && !file.getName().equals("package-info.java");
            }
        };

        Disposable disposable = Disposer.newDisposable();
        if (fileTreeAccessFilter != null) {
            PsiManagerEx.getInstanceEx(project).setAssertOnFileLoadingFilter(fileTreeAccessFilter, disposable);
        }

        //    ProfilingUtil.startCPUProfiling();
        List<HighlightInfo> infos;
        try {
            infos = getFixture().doHighlighting();
            removeDuplicatedRangesForInjected(infos);
        } finally {
            Disposer.dispose(disposable);
        }
        //    ProfilingUtil.captureCPUSnapshot("testing");
        final long elapsed = System.currentTimeMillis() - start;

        String actualInspection = "";
        String actualLineMarkers = "";

        try {
            data.checkResult(infos, file.getText());
        } catch (ComparisonFailure cf) {
            actualInspection = cf.getActual();
        }

        if (checkLineMarkers) {
            Document document = getFixture().getDocument(getFile());
            actualLineMarkers = CodeInsightFixtureSpecTestCase.getActualLineMarkerFileText(document, DaemonCodeAnalyzerImpl.getLineMarkers(document, getProject()), this::resolveIconName);
        }

        ObjectUtils.reachabilityFence(hardRefToFileElement);
        return com.vladsch.flexmark.util.Pair.of(actualInspection, actualLineMarkers);
    }

    default void defaultTestCase() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String expected;
            String actual;

            SpecExample example = getExample();
            boolean isFullSpec = example.isFullSpecExample();
            if (isFullSpec) {
                // full test case, figure out the best way to accumulate output from all the tests
                fullTestSpecStarting();

                ResourceLocation location = example.getResourceLocation();
                DumpSpecReader reader = create(location);

                reader.readExamples();

                fullTestSpecComplete();

                // NOTE: reading the full spec does not work when examples are modified by checkExample()
                actual = reader.getFullSpec();
                expected = reader.getExpectedFullSpec();
            } else {
                DataHolder options = com.vladsch.flexmark.test.util.TestUtils.getOptions(example, example.getOptionsSet(), this::options);
                LightFixtureSpecRenderer<?> exampleRenderer = getSpecExampleRenderer(example, options);

                if (options != null && com.vladsch.flexmark.test.util.TestUtils.FAIL.get(options)) {
                    getThrown().expect(CodeInsightFixtureSpecTestCase.EXCEPTION_MATCHER);
                }

                String source = example.getSource();
                if (com.vladsch.flexmark.test.util.TestUtils.NO_FILE_EOL.get(options)) {
                    source = com.vladsch.flexmark.test.util.TestUtils.trimTrailingEOL(source);
                }

                exampleRenderer.parse(source);
                exampleRenderer.finalizeDocument();

                String expectedHtml = example.getHtml();
                String actualHtml = exampleRenderer.getHtml();
                String expectedAst = example.getAst();

                // NOTE: null for section signals section does not exist. Adding "" AST will always add AST section if one did not exist in the spec example
                String actualAst = (expectedAst != null) ? exampleRenderer.getAst() : null;

                if (example.getSection() != null) {
                    StringBuilder outExpected = new StringBuilder();

                    com.vladsch.flexmark.test.util.TestUtils.addSpecExample(true, outExpected, source, expectedHtml, expectedAst, example.getOptionsSet(), true, example.getSection(), example.getExampleNumber());
                    expected = outExpected.toString();

                    StringBuilder outActual = new StringBuilder();
                    com.vladsch.flexmark.test.util.TestUtils.addSpecExample(true, outActual, source, actualHtml, actualAst, example.getOptionsSet(), true, example.getSection(), example.getExampleNumber());
                    actual = outActual.toString();
                } else {
                    expected = com.vladsch.flexmark.test.util.TestUtils.addSpecExample(true, source, expectedHtml, expectedAst, example.getOptionsSet());
                    actual = com.vladsch.flexmark.test.util.TestUtils.addSpecExample(true, source, actualHtml, actualAst, example.getOptionsSet());
                }
            }

            if (!expected.equals(actual)) {
                System.out.println(CodeInsightFixtureSpecTestCase.getExampleName(example) + " Test Failed, " + example.getFileUrlWithLineNumber());
            }

            TestCase.assertEquals("\n", expected, actual);
        });
    }

    default void closeOpenFile(@NotNull LightFixtureSpecRenderer<?> specRenderer) {
        Project project = getProject();
        if (project == null) {
            return;
        }

        LookupManager.hideActiveLookup(project);

        PsiDocumentManager.getInstance(project).commitAllDocuments();
        EditorEx editor = getEditor();
//        LOG.debug(String.format("Closing example file editor '%s' %d", Utils.escapeJavaString(editor.getDocument().getCharsSequence()), editor.getDocument().getModificationStamp()));

        FileEditorManagerEx.getInstanceEx(project).closeFile(editor.getVirtualFile());
        EditorHistoryManager.getInstance(project).removeFile(editor.getVirtualFile());
    }
}
