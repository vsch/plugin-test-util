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

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.util.ui.TextTransferable;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.plugin.test.util.TestIdeActions;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest;
import com.vladsch.plugin.test.util.cases.SpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Toolkit;
import java.util.List;

import static com.vladsch.plugin.test.util.TestIdeActions.inject;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.ACTION_NAME;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.CLIPBOARD_FILE_URL;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.CLIPBOARD_TEXT;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.SKIP_ACTION;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.TYPE_ACTION;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.TYPE_ACTION_TEXT;

public class ActionSpecRenderer<T extends LightFixtureActionSpecTest> extends LightFixtureSpecRenderer<T> {
    Editor myResultEditor;
    PsiFile myResultFile;

    public ActionSpecRenderer(@NotNull T specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    void updateResults() {
        if (myResultEditor == null) {
            String action = ACTION_NAME.get(myOptions);

            if (action.equals(inject)) {
                myResultEditor = getHostEditor();
            } else {
                myResultEditor = getEditor();
            }
        }

        if (myResultFile == null) {
            PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

            Document document = myResultEditor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null) {
                myResultFile = ReadAction.compute(() -> PsiManager.getInstance(getProject()).findFile(virtualFile));
            }
        }
    }

    @Override
    public Editor getResultEditor() {
        updateResults();
        return myResultEditor == null ? getEditor() : myResultEditor;
    }

    @Override
    public PsiFile getResultFile() {
        updateResults();
        return myResultFile == null ? getFile() : myResultFile;
    }

    protected void executeRendererAction(@NotNull String action) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (action) {
            case TestIdeActions.inject:
                String injectedText = LightFixtureActionSpecTest.INJECTED_TEXT.get(getOptions());

                InjectedLanguageManager languageManager = InjectedLanguageManager.getInstance(getProject());

                Editor editor = getEditor();
                PsiFile psiFile = getFile();
                boolean isInjectedEditor = false;

                int offset = editor.getCaretModel().getOffset();
                PsiElement elementAt = psiFile.findElementAt(offset == editor.getDocument().getTextLength() ? offset - 1 : offset);

                PsiElement hostElement = elementAt;
                while (!(hostElement == null || hostElement instanceof PsiLanguageInjectionHost || hostElement instanceof PsiFile)) hostElement = hostElement.getParent();

                if (psiFile.getContext() == null && hostElement != null) {
                    List<Pair<PsiElement, TextRange>> files = languageManager.getInjectedPsiFiles(hostElement);
                    if (files != null && !files.isEmpty()) {
                        elementAt = files.get(0).first;
                        psiFile = elementAt.getContainingFile();
                        editor = InjectedLanguageUtil.getInjectedEditorForInjectedFile(editor, psiFile);
                        offset = editor.getCaretModel().getOffset();
                        hostElement = psiFile.getContext();
                        isInjectedEditor = true;
                    }
                }

                if (!isInjectedEditor && hostElement != null && hostElement.getContext() instanceof PsiLanguageInjectionHost) {
                    hostElement = hostElement.getContext();
                    isInjectedEditor = true;
                }

                assert hostElement instanceof PsiLanguageInjectionHost : String.format("Element at caret offset: %d is not PsiLanguageInjectionHost, got: %s", offset, elementAt == null ? "null" : elementAt.getClass().getSimpleName());
                PsiLanguageInjectionHost injectionHost = (PsiLanguageInjectionHost) hostElement;

                ElementManipulator<PsiLanguageInjectionHost> manipulator = ElementManipulators.getManipulator(injectionHost);
                assert manipulator != null : "No Element manipulator for " + injectionHost.getClass().getSimpleName();

                assert !injectedText.isEmpty();
                if (isInjectedEditor) {
                    PsiLanguageInjectionHost finalHostElement = (PsiLanguageInjectionHost) hostElement;
                    int finalOffset = offset;
                    Editor finalEditor = editor;
                    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
                        Document document = finalEditor.getDocument();
                        PsiDocumentManager.getInstance(getProject()).commitDocument(document);

                        String content = document.getText();
                        int insertPos = finalOffset;

                        if (document instanceof DocumentWindow) {
                            List<TextRange> fragments = languageManager.getNonEditableFragments((DocumentWindow) finalEditor.getDocument());
                            if (!fragments.isEmpty()) {
                                TextRange firstFragment = fragments.get(0);
                                TextRange lastFragment = fragments.get(fragments.size() - 1);

                                if (firstFragment.getStartOffset() == 0 && lastFragment.getEndOffset() == content.length()) {
                                    content = content.substring(firstFragment.getEndOffset(), lastFragment.getStartOffset());
                                    insertPos -= firstFragment.getEndOffset();
                                }
                                if (firstFragment.getStartOffset() == 0) {
                                    content = content.substring(firstFragment.getEndOffset());
                                    insertPos -= firstFragment.getEndOffset();
                                }
                                if (lastFragment.getEndOffset() == content.length()) {
                                    content = content.substring(0, lastFragment.getStartOffset());
                                }
                            }
                        }

                        // find caret position in the text
                        TextRange rangeInElement = manipulator.getRangeInElement(injectionHost);
                        LiteralTextEscaper<? extends PsiLanguageInjectionHost> escaper = injectionHost.createLiteralTextEscaper();
                        String useContent = content.substring(0, insertPos) + injectedText + content.substring(insertPos);
                        manipulator.handleContentChange(finalHostElement, useContent);
                    });
                } else {
                    // find caret position in the text
                    TextRange rangeInElement = manipulator.getRangeInElement(injectionHost);
                    LiteralTextEscaper<? extends PsiLanguageInjectionHost> escaper = injectionHost.createLiteralTextEscaper();
                    StringBuilder out = new StringBuilder();
                    escaper.decode(rangeInElement, out);
                    String content = out.toString();

                    int iMax = content.length();
                    int insertPos = -1;
                    int offsetDelta = injectionHost.getTextOffset();
                    for (int i = 0; i <= iMax; i++) {
                        int offsetInHost = escaper.getOffsetInHost(i, rangeInElement);
                        if (offsetInHost + offsetDelta >= offset) {
                            insertPos = i;
                            offsetInHost = escaper.getOffsetInHost(i, rangeInElement);
                            break;
                        }
                    }

                    assert insertPos != -1 : "Caret position not found in decoded element content";

                    String finalInjectedText = content.substring(0, insertPos) + injectedText + content.substring(insertPos);
                    PsiLanguageInjectionHost finalHostElement = (PsiLanguageInjectionHost) hostElement;
                    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
                        manipulator.handleContentChange(finalHostElement, finalInjectedText);
                    });
                }

                break;

            default:
                executeAction(action);
                break;
        }
    }

    @Override
    protected String getAstBanner() {
        return CodeInsightFixtureSpecTestCase.BANNER_AST_AFTER_ACTION;
    }

    protected void doTestAction() {
        if (SpecTest.WANT_AST.get(myOptions) || (wantAstByDefault() && !myOptions.contains(SpecTest.WANT_AST) && myExample.getAst() != null)) {
            CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(ast, CodeInsightFixtureSpecTestCase.BANNER_AST_BEFORE_ACTION);
            renderAstText(ast, getResultFile(), "");
        }

        String action = ACTION_NAME.get(myOptions);
        if (action.isEmpty()) {
            assertEquals(getExample().getFileUrlWithLineNumber() + "\nACTION_NAME cannot be empty", "action", "");
        } else if (!action.equals(SKIP_ACTION)) {
            try {
                mySpecTest.beforeDoTestAction(this, myOptions);

                String clipboardFileUrl = CLIPBOARD_FILE_URL.get(myOptions);
                String clipboardText = CLIPBOARD_TEXT.get(myOptions);
                if (!clipboardFileUrl.isEmpty()) {
                    VirtualFile virtualFile = myAdditionalVirtualFiles.get(clipboardFileUrl);
                    assert virtualFile != null : "File: " + clipboardFileUrl + " not found in additional virtual files: " + myAdditionalVirtualFiles;

                    TextTransferable transferable = new TextTransferable(virtualFile.getUrl() + clipboardText);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                    CopyPasteManager.getInstance().setContents(transferable);
                } else if (!clipboardText.isEmpty()) {
                    // need to place it on the clipboard
                    TextTransferable transferable = new TextTransferable(clipboardText);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
                    CopyPasteManager.getInstance().setContents(transferable);
                }

                if (action.equals(TYPE_ACTION)) {
                    String text = TYPE_ACTION_TEXT.get(myOptions);
                    if (!text.isEmpty()) {
                        type(text);
                    } else {
                        assertEquals(getExample().getFileUrlWithLineNumber() + "\nTYPE_ACTION_TEXT cannot be empty for TYPE_ACTION", "text to type", "");
                    }
                } else {
                    executeRendererAction(action);
                }
            } catch (Throwable t) {
                html.append(t.getMessage()).append("\n");
                t.printStackTrace(System.out);
                System.out.println();
            } finally {
                mySpecTest.afterDoTestAction(this, myOptions);
            }
        }
    }

    @NotNull
    @Override
    public String renderHtml() {
        doTestAction();

        Editor editor = getResultEditor();
        html.append(getResultTextWithMarkup(editor, true, false));

        mySpecTest.renderTesActionHtml(html, this, myOptions);

        return html.toString();
    }
}
