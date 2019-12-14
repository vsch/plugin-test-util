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

package com.vladsch.plugin.test.util.renderers;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.util.ui.TextTransferable;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.plugin.test.util.TestIdeActions;
import com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Toolkit;

import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.ACTION_NAME;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.CLIPBOARD_FILE_URL;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.CLIPBOARD_TEXT;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.SKIP_ACTION;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.TYPE_ACTION;
import static com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest.TYPE_ACTION_TEXT;

public class ActionSpecRenderer<T extends LightFixtureActionSpecTest> extends LightFixtureSpecRenderer<T> {
    public ActionSpecRenderer(@NotNull T specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    protected void executeRendererAction(@NotNull String action) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (action) {
            case TestIdeActions.inject:
                String injectedText = LightFixtureActionSpecTest.INJECTED_TEXT.get(getOptions());

                int offset = getEditor().getCaretModel().getOffset();
                PsiElement elementAt = getFile().findElementAt(offset);
                PsiElement hostElement = elementAt;
                while (!(hostElement == null || hostElement instanceof PsiLanguageInjectionHost || hostElement instanceof PsiFile)) hostElement = hostElement.getParent();

                assert hostElement instanceof PsiLanguageInjectionHost : String.format("Element at caret offset: %d is not PsiLanguageInjectionHost, got: %s", offset, elementAt == null ? "null" : elementAt.getClass().getSimpleName());
                PsiLanguageInjectionHost injectionHost = (PsiLanguageInjectionHost) hostElement;

                ElementManipulator<PsiLanguageInjectionHost> manipulator = ElementManipulators.getManipulator(injectionHost);
                assert manipulator != null : "No Element manipulator for " + injectionHost.getClass().getSimpleName();

                assert !injectedText.isEmpty();
                TextRange rangeInElement = manipulator.getRangeInElement(injectionHost);
                LiteralTextEscaper<? extends PsiLanguageInjectionHost> escaper = injectionHost.createLiteralTextEscaper();
                StringBuilder out = new StringBuilder();
                escaper.decode(rangeInElement, out);
                String content = out.toString();

                // find caret position in the text
                int iMax = content.length();
                int insertPos = -1;
                int offsetDelta = injectionHost.getTextOffset();
                for (int i = 0; i <= iMax; i++) {
                    int offsetInHost = escaper.getOffsetInHost(i, rangeInElement);
                    if (offsetInHost >= offset - offsetDelta) {
                        insertPos = i;
                        break;
                    }
                }

                assert insertPos != -1 : "Caret position not found in decoded element content";

                String finalInjectedText = content.substring(0, insertPos) + injectedText + content.substring(insertPos);
                PsiLanguageInjectionHost finalHostElement = (PsiLanguageInjectionHost) hostElement;
                WriteCommandAction.runWriteCommandAction(getProject(), () -> {
                    manipulator.handleContentChange(finalHostElement, finalInjectedText);
                });
                break;

            default:
                executeAction(action);
                break;
        }
    }

    protected void doTestAction() {
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

                mySpecTest.afterDoTestAction(this, myOptions);
            } catch (Throwable t) {
                html.append(t.getMessage()).append("\n");
                t.printStackTrace(System.out);
                System.out.println();
            }
        }
    }

    @NotNull
    @Override
    public String renderHtml() {
        doTestAction();
        html.append(getResultTextWithMarkup(true, false));

        mySpecTest.renderTesActionHtml(html, this, myOptions);

        return html.toString();
    }
}
