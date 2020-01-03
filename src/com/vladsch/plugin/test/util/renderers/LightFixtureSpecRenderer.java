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

package com.vladsch.plugin.test.util.renderers;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.diagnostic.DebugLogManager;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.lang.Language;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.testFramework.HighlightTestInfo;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.exceptionCases.AbstractExceptionCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.ui.components.breadcrumbs.Crumb;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.util.ThrowableRunnable;
import com.vladsch.flexmark.test.util.SpecExampleRendererBase;
import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.misc.Utils;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.Range;
import com.vladsch.flexmark.util.sequence.builder.BasedSegmentBuilder;
import com.vladsch.plugin.test.util.AdditionalProjectFiles;
import com.vladsch.plugin.test.util.DebugLogSettings;
import com.vladsch.plugin.test.util.IntentionInfo;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.SpecTest;
import com.vladsch.plugin.util.AppUtils;
import com.vladsch.plugin.util.PsiTreeAstRenderer;
import com.vladsch.plugin.util.TestUtils;
import gnu.trove.Equality;
import junit.framework.TestCase;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.KeyStroke;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class LightFixtureSpecRenderer<T extends CodeInsightFixtureSpecTestCase> extends SpecExampleRendererBase {
    protected final @NotNull T mySpecTest;
    protected final @NotNull HashMap<String, VirtualFile> myAdditionalVirtualFiles = new HashMap<>();
    protected final AdditionalProjectFiles myAdditionalProjectFiles = new AdditionalProjectFiles();

    protected final StringBuilder ast = new StringBuilder();
    protected final StringBuilder html = new StringBuilder();
    private List<DebugLogManager.Category> mySavedCategories;
    private DebugLogSettings myDebugLogSettings;

    public LightFixtureSpecRenderer(@NotNull T specTest, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(example, options, true);
        mySpecTest = specTest;
    }

    @NotNull
    public HashMap<String, VirtualFile> getAdditionalVirtualFiles() {
        return myAdditionalVirtualFiles;
    }

    public AdditionalProjectFiles getAdditionalProjectFiles() {
        return myAdditionalProjectFiles;
    }

    @NotNull
    public List<IntentionInfo> getAvailableIntentionsWithRanges(boolean atCaretOnly) {
        // NOTE: needed to simulate getting code analyzer topic
        PsiFile file = getHostFileAtCaret();

        mySpecTest.beforeDoHighlighting(this, file);
        doHighlighting();

        return ReadAction.compute(() -> mySpecTest.getAvailableIntentionsWithRanges(this, getHostEditor(), file, atCaretOnly));
    }

    @NotNull
    protected Editor getHostEditor() {
        return InjectedLanguageUtil.getTopLevelEditor(getEditor());
    }

    protected PsiFile getHostFileAtCaret() {
        return Objects.requireNonNull(PsiUtilBase.getPsiFileInEditor(getHostEditor(), getProject()));
    }

    @Override
    public boolean includeExampleInfo() {
        return true;
    }

    @NotNull
    @Override
    final public DataHolder getOptions() {
        return myOptions;
    }

    protected boolean wantAstByDefault() {
        return true;
    }

    protected void renderAst(StringBuilder out) {
        // NOTE: changed, now asking for AST must be specific in the options, no default or rendering case return true for wantAstByDefault()
        if (SpecTest.WANT_QUICK_FIXES.get(myOptions)) {
            CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(out, CodeInsightFixtureSpecTestCase.BANNER_QUICK_FIXES);
            renderQuickFixesText(out);
        }

        if (SpecTest.WANT_AST.get(myOptions) || (wantAstByDefault() && !myOptions.contains(SpecTest.WANT_AST) && myExample.getAst() != null)) {
            CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(out, getAstBanner());
            renderAstText(out, getResultFile(), "");
        }
    }

    protected String getAstBanner() {
        return CodeInsightFixtureSpecTestCase.BANNER_AST;
    }

    protected void renderQuickFixesText(StringBuilder out) {
        List<IntentionAction> intentions = getAllQuickFixes();
        for (IntentionAction intention : intentions) {
            out.append("Intention:[").append(intention.getText()).append("]\" ]\n");
        }
    }

    public void renderRanges(StringBuilder out, BasedSequence result) {
        // NOTE: changed, now asking for AST must be specific in the options, no default or rendering case return true for wantAstByDefault()
        if (SpecTest.WANT_RANGES.get(myOptions)) {
            CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(out, CodeInsightFixtureSpecTestCase.BANNER_RANGES);
            BasedSegmentBuilder builder = BasedSegmentBuilder.emptyBuilder(result.getBaseSequence());
            result.addSegments(builder);
            out.append(builder.toStringWithRanges());
        }
    }

    protected Editor getResultEditor() {
        return getEditor();
    }

    protected PsiFile getResultFile() {
        return getFile();
    }

    @NotNull
    @Override
    final protected String renderAst() {
        renderAst(ast);

        mySpecTest.renderSpecTestAst(ast, this, myOptions);

        return ast.toString();
    }

    @Override
    public void includeDocument(@NotNull String includedText) {

    }

    protected void renderAstText(StringBuilder out, PsiElement element, String indent) {
        PsiFile file = element.getContainingFile();
        BasedSequence fileChars = BasedSequence.of(file.getText());
        PsiTreeAstRenderer.generateAst(fileChars, out, indent, element);
    }

    @NotNull
    @Override
    // invoke test action and render
    protected abstract String renderHtml();

    protected void renderIntentions(StringBuilder out, boolean atCaretOnly) {
        List<IntentionInfo> intentions = getAvailableIntentionsWithRanges(atCaretOnly);
        boolean first = true;
        boolean firstFileLevel = true;

        for (IntentionInfo intention : intentions) {
            Range range = intention.range;

            if (intention.fileLevel) {
                if ((first || firstFileLevel) && out.length() > 0) out.append("\n----- File Level Intentions -----\n");
                first = false;
                firstFileLevel = false;
            } else {
                if (first && out.length() > 0) out.append("\n----- Intentions -----\n");
                first = false;
            }

            out.append("Intention:[").append(range.getStart()).append(", ").append(range.getEnd()).append(" \"").append("intention[").append(intention.action.getText()).append("]\" ]\n");

            for (IntentionInfo subAction : intention.subActions) {
                out.append("   ").append("intention[").append(subAction.action.getText()).append("]\n");
            }
        }
    }

    @Override
    public void parse(CharSequence input) {
        myDebugLogSettings = new DebugLogSettings();
        SpecTest.DEBUG_LOG_SETTINGS_OPTION.setInstanceData(myDebugLogSettings, myOptions);
        DebugLogManager logCustomizer = AppUtils.getApplicationComponentOrService(DebugLogManager.class);
        mySavedCategories = logCustomizer.getSavedCategories();

        logCustomizer.clearCategories(mySavedCategories);
        logCustomizer.applyCategories(myDebugLogSettings.getLogCategories());

        String testInput = TestUtils.replaceCaretMarkers(input, CodeInsightFixtureSpecTestCase.TEST_CARET_MARKUP.get(myOptions));

        CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager.getInstance(getProject()).getMainProjectCodeStyle();
        assert codeStyleSettings != null;

        CodeStyleSettings myCodeStyleSettings = codeStyleSettings.clone();
        SpecTest.CODE_STYLE_SETTINGS_OPTION.setInstanceData(myCodeStyleSettings, myOptions);
        CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(myCodeStyleSettings);

        // allow customization of initialization
        mySpecTest.initializeRenderer(this, myOptions);

        SpecTest.ADDITIONAL_PROJECT_FILES_OPTION.setInstanceData(myAdditionalProjectFiles, myOptions);

        if (!myAdditionalProjectFiles.getFiles().isEmpty()) {
            // create the files and keep track
            for (Map.Entry<String, Object> entry : myAdditionalProjectFiles.getFiles().entrySet()) {
                PsiFile psiFile;

                Object value = entry.getValue();
                if (value instanceof String) {
                    psiFile = addFileToProject(entry.getKey(), (String) value);
                    myAdditionalVirtualFiles.put(entry.getKey(), psiFile.getVirtualFile());
                    mySpecTest.LOG().debug(String.format("Created additional file %s '%s' %d", entry.getKey(), Utils.escapeJavaString((String) value), psiFile.getModificationStamp()));
                } else if (value instanceof ResourceLocation) {
                    // image file
                    ResourceLocation resourceLocation = (ResourceLocation) value;
                    VirtualFile imageFile = mySpecTest.createImageFile(resourceLocation.getResourcePath(), resourceLocation.getResourceInputStream());
                    myAdditionalVirtualFiles.put(entry.getKey(), imageFile);
                    mySpecTest.LOG().debug(String.format("Created additional image file %s %d", entry.getKey(), imageFile.getModificationStamp()));
                }
            }
        }

        String name = getExampleFileName(myExample, myOptions);
        if (name.contains("/")) {
            PsiFile psiFile = addFileToProject(name, testInput);
            myAdditionalVirtualFiles.put(name, psiFile.getVirtualFile());
            configureFromExistingVirtualFile(psiFile.getVirtualFile());
        } else {
            configureByText(name, testInput);
        }

        // CAUTION: getting document char sequence seems to be needed, without it some tests have document content reverted to original after action has modified it
        //   causing tests to fail and caret offsets to be out of sync with content and offset > textLength()
        getEditor().getDocument().getCharsSequence();
        //LOG.debug(String.format("Created example file %s '%s' %d", getExampleFileName(myExample), Utils.escapeJavaString(getEditor().getDocument().getCharsSequence()), getEditor().getDocument().getModificationStamp()));
    }

    @Override
    public void finalizeDocument() {
    }

    @Override
    public void finalizeRender() {
        // delete additional files
        // QUERY: seems to be not needed
        mySpecTest.closeOpenFile(this);

        EditorHistoryManager historyManager = EditorHistoryManager.getInstance(getProject());

        if (!myAdditionalVirtualFiles.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(getProject(), () -> {
                for (Map.Entry<String, VirtualFile> entry : myAdditionalVirtualFiles.entrySet()) {
                    VirtualFile virtualFile = entry.getValue();
                    if (virtualFile.isValid()) {
                        try {
                            historyManager.removeFile(virtualFile);
                            virtualFile.delete(this);
                        } catch (IOException e) {
                            mySpecTest.LOG().error("Deleting additional files", e);
                        }
                    }
                }
            });

            myAdditionalVirtualFiles.clear();
        }

        mySpecTest.finalizeRenderer(this, myOptions);

        CodeStyleSettingsManager.getInstance(getProject()).dropTemporarySettings();
        CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager.getInstance(getProject()).getTemporarySettings();
        assert codeStyleSettings == null;

        DebugLogManager logCustomizer = AppUtils.getApplicationComponentOrService(DebugLogManager.class);
        logCustomizer.clearCategories(myDebugLogSettings.getLogCategories());
        logCustomizer.applyCategories(mySavedCategories);
    }

    // @formatter:off
    // compatibility delegates
    public void executeAction(@NotNull String actionId) {mySpecTest.executeAction(actionId);}
    public void executeAction(@NotNull String actionId, @NotNull Editor editor) {mySpecTest.executeAction(actionId, editor);}
    public static void executeAction(@NotNull String actionId, @NotNull Editor editor, Project project) {CodeInsightFixtureSpecTestCase.executeAction(actionId, editor, project);}
    public static void executeAction(@NotNull Editor editor, boolean assertActionIsEnabled, @NotNull AnAction action) {CodeInsightFixtureSpecTestCase.executeAction(editor, assertActionIsEnabled, action);}
    public static void executeKeystroke(@NotNull Editor editor, @NotNull KeyStroke stroke) {CodeInsightFixtureSpecTestCase.executeKeystroke(editor,  stroke);}

    // delegates to mySpecTest.myFixture
    public Editor getEditor() {return getFixture().getEditor();}
    public int getCaretOffset() {return getFixture().getCaretOffset();}
    public PsiFile getFile() {return getFixture().getFile();}
    public void setTestDataPath(@NotNull String dataPath) {getFixture().setTestDataPath(dataPath);}
    @NotNull public String getTempDirPath() {return getFixture().getTempDirPath();}
    @NotNull public TempDirTestFixture getTempDirFixture() {return getFixture().getTempDirFixture();}
    @NotNull public VirtualFile copyFileToProject(@NotNull String sourceFilePath) {return getFixture().copyFileToProject(sourceFilePath);}
    @NotNull public VirtualFile copyFileToProject(@NotNull String sourceFilePath, @NotNull String targetPath) {return getFixture().copyFileToProject(sourceFilePath, targetPath);}
    @NotNull public VirtualFile copyDirectoryToProject(@NotNull String sourceFilePath, @NotNull String targetPath) {return getFixture().copyDirectoryToProject(sourceFilePath, targetPath);}
    public PsiFile configureByFile(@NotNull String filePath) {return getFixture().configureByFile(filePath);}
    @NotNull public PsiFile[] configureByFiles(@NotNull String... filePaths) {return getFixture().configureByFiles(filePaths);}
    public PsiFile configureByText(@NotNull FileType fileType, @NotNull String text) {return getFixture().configureByText(fileType, text);}
    public PsiFile configureByText(@NotNull String fileName, @NotNull String text) {return getFixture().configureByText(fileName, text);}
    public PsiFile configureFromTempProjectFile(@NotNull String filePath) {return getFixture().configureFromTempProjectFile(filePath);}
    public void configureFromExistingVirtualFile(@NotNull VirtualFile virtualFile) {getFixture().configureFromExistingVirtualFile(virtualFile);}
    public PsiFile addFileToProject(@NotNull String relativePath, @NotNull String fileText) {return getFixture().addFileToProject(relativePath, fileText);}
    public void checkResultByFile(@NotNull String expectedFile) {getFixture().checkResultByFile(expectedFile);}
    public void checkResultByFile(@NotNull String expectedFile, boolean ignoreTrailingWhitespaces) {getFixture().checkResultByFile(expectedFile, ignoreTrailingWhitespaces);}
    public void checkResultByFile(@NotNull String filePath, @NotNull String expectedFile, boolean ignoreTrailingWhitespaces) {getFixture().checkResultByFile(filePath, expectedFile, ignoreTrailingWhitespaces);}
    public void enableInspections(@NotNull InspectionProfileEntry... inspections) {getFixture().enableInspections(inspections);}
    public void enableInspections(@NotNull Class<? extends LocalInspectionTool>... inspections) {getFixture().enableInspections(inspections);}
    public void enableInspections(@NotNull Collection<Class<? extends LocalInspectionTool>> inspections) {getFixture().enableInspections(inspections);}
    public void disableInspections(@NotNull InspectionProfileEntry... inspections) {getFixture().disableInspections(inspections);}
    public void enableInspections(@NotNull InspectionToolProvider... providers) {getFixture().enableInspections(providers);}
    public long testHighlighting(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings, @NotNull String... filePaths) {return getFixture().testHighlighting(checkWarnings, checkInfos, checkWeakWarnings, filePaths);}
    public long testHighlightingAllFiles(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings, @NotNull String... filePaths) {return getFixture().testHighlightingAllFiles(checkWarnings, checkInfos, checkWeakWarnings, filePaths);}
    public long testHighlightingAllFiles(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings, @NotNull VirtualFile... files) {return getFixture().testHighlightingAllFiles(checkWarnings, checkInfos, checkWeakWarnings, files);}
    public long checkHighlighting(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings) {return getFixture().checkHighlighting(checkWarnings, checkInfos, checkWeakWarnings);}
    public long checkHighlighting(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings, boolean ignoreExtraHighlighting) {return getFixture().checkHighlighting(checkWarnings, checkInfos, checkWeakWarnings, ignoreExtraHighlighting);}
    public long checkHighlighting() {return getFixture().checkHighlighting();}
    public long testHighlighting(@NotNull String... filePaths) {return getFixture().testHighlighting(filePaths);}
    public long testHighlighting(boolean checkWarnings, boolean checkInfos, boolean checkWeakWarnings, @NotNull VirtualFile file) {return getFixture().testHighlighting(checkWarnings, checkInfos, checkWeakWarnings, file);}
    @NotNull public HighlightTestInfo testFile(@NotNull String... filePath) {return getFixture().testFile(filePath);}
    public void openFileInEditor(@NotNull VirtualFile file) {getFixture().openFileInEditor(file);}
    public void testInspection(@NotNull String testDir, @NotNull InspectionToolWrapper toolWrapper) {getFixture().testInspection(testDir, toolWrapper);}
    @NotNull public List<HighlightInfo> doHighlighting() {return getFixture().doHighlighting();}
    @NotNull public List<HighlightInfo> doHighlighting(@NotNull HighlightSeverity minimalSeverity) {return getFixture().doHighlighting(minimalSeverity);}
    @Nullable public PsiReference getReferenceAtCaretPosition(@NotNull String... filePaths) {return getFixture().getReferenceAtCaretPosition(filePaths);}
    @NotNull public PsiReference getReferenceAtCaretPositionWithAssertion(@NotNull String... filePaths) {return getFixture().getReferenceAtCaretPositionWithAssertion(filePaths);}
    @NotNull public List<IntentionAction> getAvailableIntentions(@NotNull String... filePaths) {return getFixture().getAvailableIntentions(filePaths);}
    @NotNull public List<IntentionAction> getAllQuickFixes(@NotNull String... filePaths) {return getFixture().getAllQuickFixes(filePaths);}
    @NotNull public List<IntentionAction> getAvailableIntentions() {return getFixture().getAvailableIntentions();}
    @NotNull public List<IntentionAction> filterAvailableIntentions(@NotNull String hint) {return getFixture().filterAvailableIntentions(hint);}
    @NotNull public IntentionAction findSingleIntention(@NotNull String hint) {return getFixture().findSingleIntention(hint);}
    @Nullable public IntentionAction getAvailableIntention(@NotNull String intentionName, @NotNull String... filePaths) {return getFixture().getAvailableIntention(intentionName, filePaths);}
    public void launchAction(@NotNull IntentionAction action) {getFixture().launchAction(action);}
    public void testCompletion(@NotNull String[] filesBefore, @NotNull String fileAfter) {getFixture().testCompletion(filesBefore, fileAfter);}
    public void testCompletionTyping(@NotNull String[] filesBefore, @NotNull String toType, @NotNull String fileAfter) {getFixture().testCompletionTyping(filesBefore, toType, fileAfter);}
    public void testCompletion(@NotNull String fileBefore, @NotNull String fileAfter, @NotNull String... additionalFiles) {getFixture().testCompletion(fileBefore, fileAfter, additionalFiles);}
    public void testCompletionTyping(@NotNull String fileBefore, @NotNull String toType, @NotNull String fileAfter, @NotNull String... additionalFiles) {getFixture().testCompletionTyping(fileBefore, toType, fileAfter, additionalFiles);}
    public void testCompletionVariants(@NotNull String fileBefore, @NotNull String... items) {getFixture().testCompletionVariants(fileBefore, items);}
    public void testRename(@NotNull String fileBefore, @NotNull String fileAfter, @NotNull String newName, @NotNull String... additionalFiles) {getFixture().testRename(fileBefore, fileAfter, newName, additionalFiles);}
    public void testRenameUsingHandler(@NotNull String fileBefore, @NotNull String fileAfter, @NotNull String newName, @NotNull String... additionalFiles) {getFixture().testRenameUsingHandler(fileBefore, fileAfter, newName, additionalFiles);}
    public void testRename(@NotNull String fileAfter, @NotNull String newName) {getFixture().testRename(fileAfter, newName);}
    public void testRenameUsingHandler(@NotNull String fileAfter, @NotNull String newName) {getFixture().testRenameUsingHandler(fileAfter, newName);}
    @NotNull public Collection<UsageInfo> testFindUsages(@NotNull String... fileNames) {return getFixture().testFindUsages(fileNames);}
    @NotNull public Collection<Usage> testFindUsagesUsingAction(@NotNull String... fileNames) {return getFixture().testFindUsagesUsingAction(fileNames);}
    @NotNull public Collection<UsageInfo> findUsages(@NotNull PsiElement to) {return getFixture().findUsages(to);}
    @NotNull public String getUsageViewTreeTextRepresentation(@NotNull Collection<? extends UsageInfo> usages) {return getFixture().getUsageViewTreeTextRepresentation(usages);}
    @NotNull public String getUsageViewTreeTextRepresentation(@NotNull PsiElement to) {return getFixture().getUsageViewTreeTextRepresentation(to);}
    @NotNull public RangeHighlighter[] testHighlightUsages(@NotNull String... files) {return getFixture().testHighlightUsages(files);}
    public void moveFile(@NotNull String filePath, @NotNull String to, @NotNull String... additionalFiles) {getFixture().moveFile(filePath, to, additionalFiles);}
    @Nullable public GutterMark findGutter(@NotNull String filePath) {return getFixture().findGutter(filePath);}
    @NotNull public List<GutterMark> findGuttersAtCaret() {return getFixture().findGuttersAtCaret();}
    public LookupElement[] completeBasic() {return getFixture().completeBasic();}
    public LookupElement[] complete(@NotNull CompletionType type) {return getFixture().complete(type);}
    public LookupElement[] complete(@NotNull CompletionType type, int invocationCount) {return getFixture().complete(type, invocationCount);}
    public void checkResult(@NotNull String text) {getFixture().checkResult(text);}
    public void checkResult(@NotNull String text, boolean stripTrailingSpaces) {getFixture().checkResult(text, stripTrailingSpaces);}
    public void checkResult(@NotNull String filePath, @NotNull String text, boolean stripTrailingSpaces) {getFixture().checkResult(filePath, text, stripTrailingSpaces);}
    public Document getDocument(@NotNull PsiFile file) {return getFixture().getDocument(file);}
    @NotNull public List<GutterMark> findAllGutters(@NotNull String filePath) {return getFixture().findAllGutters(filePath);}
    public List<GutterMark> findAllGutters() {return getFixture().findAllGutters();}
    public void type(char c) {getFixture().type(c);}
    public void type(@NotNull String s) {getFixture().type(s);}
    public void performEditorAction(@NotNull String actionId) {getFixture().performEditorAction(actionId);}
    @NotNull public Presentation testAction(@NotNull AnAction action) {return getFixture().testAction(action);}
    @Nullable public List<String> getCompletionVariants(@NotNull String... filesBefore) {return getFixture().getCompletionVariants(filesBefore);}
    @Nullable public LookupElement[] getLookupElements() {return getFixture().getLookupElements();}
    public VirtualFile findFileInTempDir(@NotNull String filePath) {return getFixture().findFileInTempDir(filePath);}
    @Nullable public List<String> getLookupElementStrings() {return getFixture().getLookupElementStrings();}
    public void finishLookup(char completionChar) {getFixture().finishLookup(completionChar);}
    public LookupEx getLookup() {return getFixture().getLookup();}
    @NotNull public PsiElement getElementAtCaret() {return getFixture().getElementAtCaret();}
    public void renameElementAtCaret(@NotNull String newName) {getFixture().renameElementAtCaret(newName);}
    public void renameElementAtCaretUsingHandler(@NotNull String newName) {getFixture().renameElementAtCaretUsingHandler(newName);}
    public void renameElement(@NotNull PsiElement element, @NotNull String newName) {getFixture().renameElement(element, newName);}
    public void allowTreeAccessForFile(@NotNull VirtualFile file) {getFixture().allowTreeAccessForFile(file);}
    public void allowTreeAccessForAllFiles() {getFixture().allowTreeAccessForAllFiles();}
    public void renameElement(@NotNull PsiElement element, @NotNull String newName, boolean searchInComments, boolean searchTextOccurrences) {getFixture().renameElement(element, newName, searchInComments, searchTextOccurrences);}
    public<T extends PsiElement> T findElementByText(@NotNull String text, @NotNull Class<T> elementClass) {return getFixture().findElementByText(text, elementClass);}
    public void testFolding(@NotNull String fileName) {getFixture().testFolding(fileName);}
    public void testFoldingWithCollapseStatus(@NotNull String verificationFileName, @Nullable String destinationFileName) {getFixture().testFoldingWithCollapseStatus(verificationFileName, destinationFileName);}
    public void testFoldingWithCollapseStatus(@NotNull String fileName) {getFixture().testFoldingWithCollapseStatus(fileName);}
    public void testRainbow(@NotNull String fileName, @NotNull String text, boolean isRainbowOn, boolean withColor) {getFixture().testRainbow(fileName, text, isRainbowOn, withColor);}
    public void testInlays() {getFixture().testInlays();}
    public void testInlays(Function<? super Inlay, String> inlayPresenter, Predicate<? super Inlay> inlayFilter) {getFixture().testInlays(inlayPresenter, inlayFilter);}
    public void checkResultWithInlays(String text) {getFixture().checkResultWithInlays(text);}
    public void assertPreferredCompletionItems(int selected, @NotNull String... expected) {getFixture().assertPreferredCompletionItems(selected, expected);}
    public void testStructureView(@NotNull com.intellij.util.Consumer<? super StructureViewComponent> consumer) {getFixture().testStructureView(consumer);}
    public void setCaresAboutInjection(boolean caresAboutInjection) {getFixture().setCaresAboutInjection(caresAboutInjection);}
    @NotNull public List<LookupElement> completeBasicAllCarets(@Nullable Character charToTypeAfterCompletion) {return getFixture().completeBasicAllCarets(charToTypeAfterCompletion);}
    @NotNull public List<Object> getGotoClassResults(@NotNull String pattern, boolean searchEverywhere, @Nullable PsiElement contextForSorting) {return getFixture().getGotoClassResults(pattern, searchEverywhere, contextForSorting);}
    @NotNull public List<Crumb> getBreadcrumbsAtCaret() {return getFixture().getBreadcrumbsAtCaret();}
    public void saveText(@NotNull VirtualFile file, @NotNull String text) {getFixture().saveText(file, text);}
    @NotNull public Disposable getProjectDisposable() {return getFixture().getProjectDisposable();}

    // delegates to mySpecTest
    public CodeInsightTestFixture getFixture() {return mySpecTest.getFixture();}
    protected DataHolder options(String optionSet) {return mySpecTest.options(optionSet);}
    @NotNull protected String getExampleFileName(@NotNull SpecExample example, @NotNull DataHolder options) {return CodeInsightFixtureSpecTestCase.getExampleFileName(getClass(),example, options);}
    @NotNull protected String getExampleName(@NotNull SpecExample example) {return CodeInsightFixtureSpecTestCase.getExampleName(example);}
    @NotNull protected String getResultTextWithMarkup(boolean withCarets, boolean withTestCaretMarkup) {return mySpecTest.getResultTextWithMarkup(withCarets, withTestCaretMarkup);}
    @NotNull protected String getResultTextWithMarkup(@NotNull Editor editor, boolean withCarets, boolean withTestCaretMarkup) {return mySpecTest.getResultTextWithMarkup(editor, withCarets, withTestCaretMarkup);}
    protected Project getProject() {return mySpecTest.getProject();}
    public PsiManager getPsiManager() {return mySpecTest.getPsiManager();}
    public PsiFile createLightFile(FileType fileType, String text) {return mySpecTest.createLightFile(fileType, text);}
    public PsiFile createLightFile(String fileName, Language language, String text) {return mySpecTest.createLightFile(fileName, language, text);}
    @NotNull public Module getModule() {return mySpecTest.getModule();}
    public void addSuppressedException(@NotNull Throwable e) {mySpecTest.addSuppressedException(e);}
    public boolean shouldContainTempFiles() {return mySpecTest.shouldContainTempFiles();}
    public boolean isIconRequired() {return mySpecTest.isIconRequired();}
    public void addTmpFileToKeep(@NotNull File file) {mySpecTest.addTmpFileToKeep(file);}
    @NotNull public Disposable getTestRootDisposable() {return mySpecTest.getTestRootDisposable();}
    public boolean shouldRunTest() {return mySpecTest.shouldRunTest();}
    public void invokeTestRunnable(@NotNull Runnable runnable) throws Exception {mySpecTest.invokeTestRunnable(runnable);}
    public void defaultRunBare() throws Throwable {mySpecTest.defaultRunBare();}
    public void runBare() throws Throwable {mySpecTest.runBare();}
    public boolean runInDispatchThread() {return mySpecTest.runInDispatchThread();}
    public void edt(@NotNull ThrowableRunnable<Throwable> runnable) {mySpecTest.edt(runnable);}
    @NotNull public <T extends Disposable> T disposeOnTearDown(@NotNull T disposable) {return mySpecTest.disposeOnTearDown(disposable);}
    @NotNull public String getTestName(boolean lowercaseFirstLetter) {return mySpecTest.getTestName(lowercaseFirstLetter);}
    @NotNull public String getTestDirectoryName() {return mySpecTest.getTestDirectoryName();}
    public boolean isPerformanceTest() {return mySpecTest.isPerformanceTest();}
    public boolean isStressTest() {return mySpecTest.isStressTest();}
    public void assertException(@NotNull AbstractExceptionCase<?> exceptionCase) {mySpecTest.assertException(exceptionCase);}
    public void assertException(@NotNull AbstractExceptionCase exceptionCase, @Nullable String expectedErrorMsg) {mySpecTest.assertException(exceptionCase, expectedErrorMsg);}
    public <T extends Throwable> void assertNoException(@NotNull AbstractExceptionCase<T> exceptionCase) throws T {mySpecTest.assertNoException(exceptionCase);}
    public void assertNoThrowable(@NotNull Runnable closure) {mySpecTest.assertNoThrowable(closure);}
    public boolean annotatedWith(@NotNull Class<? extends Annotation> annotationClass) {return mySpecTest.annotatedWith(annotationClass);}
    @NotNull public String getHomePath() {return mySpecTest.getHomePath();}
    @SafeVarargs public static <T> void assertOrderedEquals(@NotNull T[] actual, @NotNull T... expected) {UsefulTestCase.assertOrderedEquals(actual, expected);}
    @SafeVarargs public static <T> void assertOrderedEquals(@NotNull Iterable<? extends T> actual, @NotNull T... expected) {UsefulTestCase.assertOrderedEquals(actual, expected);}
    public static void assertOrderedEquals(@NotNull byte[] actual, @NotNull byte[] expected) {UsefulTestCase.assertOrderedEquals(actual, expected);}
    public static void assertOrderedEquals(@NotNull int[] actual, @NotNull int[] expected) {UsefulTestCase.assertOrderedEquals(actual, expected);}
    @SafeVarargs public static <T> void assertOrderedEquals(@NotNull String errorMsg, @NotNull Iterable<? extends T> actual, @NotNull T... expected) {UsefulTestCase.assertOrderedEquals(errorMsg, actual, expected);}
    public static <T> void assertOrderedEquals(@NotNull Iterable<? extends T> actual, @NotNull Iterable<? extends T> expected) {UsefulTestCase.assertOrderedEquals(actual, expected);}
    public static <T> void assertOrderedEquals(@NotNull String errorMsg, @NotNull Iterable<? extends T> actual, @NotNull Iterable<? extends T> expected) {UsefulTestCase.assertOrderedEquals(errorMsg, actual, expected);}
    public static <T> void assertOrderedEquals(@NotNull String errorMsg, @NotNull Iterable<? extends T> actual, @NotNull Iterable<? extends T> expected, @NotNull Equality<? super T> comparator) {UsefulTestCase.assertOrderedEquals(errorMsg, actual, expected, comparator);}
    @SafeVarargs public static <T> void assertOrderedCollection(@NotNull T[] collection, @NotNull com.intellij.util.Consumer<T>... checkers) {UsefulTestCase.assertOrderedCollection(collection, checkers);}
    @SafeVarargs public static <T> void assertSameElements(@NotNull T[] actual, @NotNull T... expected) {UsefulTestCase.assertSameElements(actual, expected);}
    @SafeVarargs public static <T> void assertSameElements(@NotNull Collection<? extends T> actual, @NotNull T... expected) {UsefulTestCase.assertSameElements(actual, expected);}
    public static <T> void assertSameElements(@NotNull Collection<? extends T> actual, @NotNull Collection<? extends T> expected) {UsefulTestCase.assertSameElements(actual, expected);}
    public static <T> void assertSameElements(@NotNull String message, @NotNull Collection<? extends T> actual, @NotNull Collection<? extends T> expected) {UsefulTestCase.assertSameElements(message, actual, expected);}
    @SafeVarargs public static <T> void assertContainsOrdered(@NotNull Collection<? extends T> collection, @NotNull T... expected) {UsefulTestCase.assertContainsOrdered(collection, expected);}
    public static <T> void assertContainsOrdered(@NotNull Collection<? extends T> collection, @NotNull Collection<? extends T> expected) {UsefulTestCase.assertContainsOrdered(collection, expected);}
    @SafeVarargs public static <T> void assertContainsElements(@NotNull Collection<? extends T> collection, @NotNull T... expected) {UsefulTestCase.assertContainsElements(collection, expected);}
    public static <T> void assertContainsElements(@NotNull Collection<? extends T> collection, @NotNull Collection<? extends T> expected) {UsefulTestCase.assertContainsElements(collection, expected);}
    @SafeVarargs public static <T> void assertDoesntContain(@NotNull Collection<? extends T> collection, @NotNull T... notExpected) {UsefulTestCase.assertDoesntContain(collection, notExpected);}
    public static <T> void assertDoesntContain(@NotNull Collection<? extends T> collection, @NotNull Collection<? extends T> notExpected) {UsefulTestCase.assertDoesntContain(collection, notExpected);}
    @SafeVarargs public static <T> void assertOrderedCollection(@NotNull Collection<? extends T> collection, @NotNull com.intellij.util.Consumer<T>... checkers) {UsefulTestCase.assertOrderedCollection(collection, checkers);}
    @SafeVarargs public static <T> void assertUnorderedCollection(@NotNull T[] collection, @NotNull com.intellij.util.Consumer<T>... checkers) {UsefulTestCase.assertUnorderedCollection(collection, checkers);}
    @SafeVarargs public static <T> void assertUnorderedCollection(@NotNull Collection<? extends T> collection, @NotNull com.intellij.util.Consumer<T>... checkers) {UsefulTestCase.assertUnorderedCollection(collection, checkers);}
    @NotNull @Contract("null, _ -> fail") public static <T> T assertInstanceOf(Object o, @NotNull Class<T> aClass) {return UsefulTestCase.assertInstanceOf(o, aClass);}
    public static <T> T assertOneElement(@NotNull Collection<? extends T> collection) {return UsefulTestCase.assertOneElement(collection);}
    public static <T> T assertOneElement(@NotNull T[] ts) {return UsefulTestCase.assertOneElement(ts);}
    @SafeVarargs public static <T> void assertOneOf(T value, @NotNull T... values) {UsefulTestCase.assertOneOf(value, values);}
    public static void assertEmpty(@NotNull Object[] array) {UsefulTestCase.assertEmpty(array);}
    public static void assertNotEmpty(Collection<?> collection) {UsefulTestCase.assertNotEmpty(collection);}
    public static void assertEmpty(@NotNull Collection<?> collection) {UsefulTestCase.assertEmpty(collection);}
    public static void assertNullOrEmpty(@Nullable Collection<?> collection) {UsefulTestCase.assertNullOrEmpty(collection);}
    public static void assertEmpty(String s) {UsefulTestCase.assertEmpty(s);}
    public static <T> void assertEmpty(@NotNull String errorMsg, @NotNull Collection<? extends T> collection) {UsefulTestCase.assertEmpty(errorMsg, collection);}
    public static void assertSize(int expectedSize, @NotNull Object[] array) {UsefulTestCase.assertSize(expectedSize, array);}
    public static void assertSize(int expectedSize, @NotNull Collection<?> c) {UsefulTestCase.assertSize(expectedSize, c);}
    public static void assertSameLines(@NotNull String expected, @NotNull String actual) {UsefulTestCase.assertSameLines(expected, actual);}
    public static void assertSameLines(@Nullable String message, @NotNull String expected, @NotNull String actual) {UsefulTestCase.assertSameLines(message, expected, actual);}
    public static void assertExists(@NotNull File file) {UsefulTestCase.assertExists(file);}
    public static void assertDoesntExist(@NotNull File file) {UsefulTestCase.assertDoesntExist(file);}
    @NotNull public static String getTestName(@Nullable String name, boolean lowercaseFirstLetter) {return UsefulTestCase.getTestName(name, lowercaseFirstLetter);}
    public static void assertSameLinesWithFile(@NotNull String filePath, @NotNull String actualText) {UsefulTestCase.assertSameLinesWithFile(filePath, actualText);}
    public static void assertSameLinesWithFile(@NotNull String filePath, @NotNull String actualText, @NotNull Supplier<String> messageProducer) {UsefulTestCase.assertSameLinesWithFile(filePath, actualText, messageProducer);}
    public static void assertSameLinesWithFile(@NotNull String filePath, @NotNull String actualText, boolean trimBeforeComparing) {UsefulTestCase.assertSameLinesWithFile(filePath, actualText, trimBeforeComparing);}
    public static void assertSameLinesWithFile(@NotNull String filePath, @NotNull String actualText, boolean trimBeforeComparing, @Nullable Supplier<String> messageProducer) {UsefulTestCase.assertSameLinesWithFile(filePath, actualText, trimBeforeComparing, messageProducer);}
    public static void clearFields(@NotNull Object test) throws IllegalAccessException {CodeInsightFixtureSpecTestCase.clearFields(test);}
    public static void clearDeclaredFields(@NotNull Object test, @NotNull Class<?> aClass) throws IllegalAccessException {UsefulTestCase.clearDeclaredFields(test, aClass);}
    public static <T extends Throwable> void assertThrows(@NotNull Class<? extends Throwable> exceptionClass, @NotNull ThrowableRunnable<T> runnable) {UsefulTestCase.assertThrows(exceptionClass, runnable);}
    public static <T extends Throwable> void assertThrows(@NotNull Class<? extends Throwable> exceptionClass, @Nullable String expectedErrorMsg, @NotNull ThrowableRunnable<T> runnable) {UsefulTestCase.assertThrows(exceptionClass, expectedErrorMsg, runnable);}
    public static void assertTrue(String message, boolean condition) {TestCase.assertTrue(message, condition);}
    public static void assertTrue(boolean condition) {TestCase.assertTrue(condition);}
    public static void assertFalse(String message, boolean condition) {TestCase.assertFalse(message, condition);}
    public static void assertFalse(boolean condition) {TestCase.assertFalse(condition);}
    public static void fail(String message) {TestCase.fail(message);}
    public static void fail() {TestCase.fail();}
    public static void assertEquals(String message, Object expected, Object actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(Object expected, Object actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, String expected, String actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(String expected, String actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, double expected, double actual, double delta) {TestCase.assertEquals(message, expected, actual, delta);}
    public static void assertEquals(double expected, double actual, double delta) {TestCase.assertEquals(expected, actual, delta);}
    public static void assertEquals(String message, float expected, float actual, float delta) {TestCase.assertEquals(message, expected, actual, delta);}
    public static void assertEquals(float expected, float actual, float delta) {TestCase.assertEquals(expected, actual, delta);}
    public static void assertEquals(String message, long expected, long actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(long expected, long actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, boolean expected, boolean actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(boolean expected, boolean actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, byte expected, byte actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(byte expected, byte actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, char expected, char actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(char expected, char actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, short expected, short actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(short expected, short actual) {TestCase.assertEquals(expected, actual);}
    public static void assertEquals(String message, int expected, int actual) {TestCase.assertEquals(message, expected, actual);}
    public static void assertEquals(int expected, int actual) {TestCase.assertEquals(expected, actual);}
    public static void assertNotNull(Object object) {TestCase.assertNotNull(object);}
    public static void assertNotNull(String message, Object object) {TestCase.assertNotNull(message, object);}
    public static void assertNull(Object object) {TestCase.assertNull(object);}
    public static void assertNull(String message, Object object) {TestCase.assertNull(message, object);}
    public static void assertSame(String message, Object expected, Object actual) {TestCase.assertSame(message, expected, actual);}
    public static void assertSame(Object expected, Object actual) {TestCase.assertSame(expected, actual);}
    public static void assertNotSame(String message, Object expected, Object actual) {TestCase.assertNotSame(message, expected, actual);}
    public static void assertNotSame(Object expected, Object actual) {TestCase.assertNotSame(expected, actual);}
    public static void failSame(String message) {TestCase.failSame(message);}
    public static void failNotSame(String message, Object expected, Object actual) {TestCase.failNotSame(message, expected, actual);}
    public static void failNotEquals(String message, Object expected, Object actual) {TestCase.failNotEquals(message, expected, actual);}
    public static String format(String message, Object expected, Object actual) {return TestCase.format(message, expected, actual);}
    @NotNull public static String toString(@NotNull Iterable<?> collection) {return UsefulTestCase.toString(collection);}
    @NotNull public static String toString(@NotNull Object[] collection, @NotNull String separator) {return UsefulTestCase.toString(collection, separator);}
    @NotNull public static String toString(@NotNull Collection<?> collection, @NotNull String separator) {return UsefulTestCase.toString(collection, separator);}
    public static void printThreadDump() {UsefulTestCase.printThreadDump();}
    public static void doPostponedFormatting(@NotNull Project project) {UsefulTestCase.doPostponedFormatting(project);}
    public static void refreshRecursively(@NotNull VirtualFile file) {UsefulTestCase.refreshRecursively(file);}
    public static VirtualFile refreshAndFindFile(@NotNull File file) {return UsefulTestCase.refreshAndFindFile(file);}
    public static void waitForAppLeakingThreads(long timeout, @NotNull TimeUnit timeUnit) {UsefulTestCase.waitForAppLeakingThreads(timeout, timeUnit);}
// @formatter:on
}
