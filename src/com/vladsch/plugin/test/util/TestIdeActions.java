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

public interface TestIdeActions {
    // editor actions
    String EditorBackspace = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE;
    String EditorSelectWordAtCaret = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_SELECT_WORD_AT_CARET;
    String EditorDuplicate = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_DUPLICATE;
    String EditorDelete = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_DELETE;
    String EditorMoveLineStart = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_START;
    String EditorMoveLineEnd = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_END;
    String EditorMoveLineStartWithSelection = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION;
    String EditorMoveLineEndWithSelection = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION;
    String EditorCopy = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_COPY;
    String EditorPaste = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE;
    String EditorPreviousWordWithSelection = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PREVIOUS_WORD_WITH_SELECTION;
    String EditorNextWordWithSelection = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD_WITH_SELECTION;
    String EditorPreviousWord = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PREVIOUS_WORD;
    String EditorNextWord = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD;
    String EditorCutLineBackward = "EditorCutLineBackward";
    String EditorCutLineEnd = "EditorCutLineEnd";
    String EditorDeleteToLineStart = "EditorDeleteToLineStart";
    String EditorDeleteToLineEnd = "EditorDeleteToLineEnd";
    String EditorKillToWordStart = "EditorKillToWordStart";
    String EditorKillToWordEnd = "EditorKillToWordEnd";
    String EditorKillRegion = "EditorKillRegion";
    String EditorKillRingSave = "EditorKillRingSave";
    String EditorUnindentSelection = "EditorUnindentSelection";
    String EditorSelectLine = "EditorSelectLine";
    String EditorLeft = "EditorLeft";
    String EditorRight = "EditorRight";
    String EditorLeftWithSelection = "EditorLeftWithSelection";
    String EditorRightWithSelection = "EditorRightWithSelection";
    String EditorUp = "EditorUp";
    String EditorDown = "EditorDown";
    String CommentLine = com.intellij.openapi.actionSystem.IdeActions.ACTION_COMMENT_LINE;
    String EditorEnter = com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ENTER;

    // alias for editor actions
    String backspace = EditorBackspace;
    String up = EditorUp;
    String down = EditorDown;
    String left = EditorLeft;
    String right = EditorRight;
    String paste = EditorPaste;
    String copy = EditorCopy; // TEST: need to add copy[] variation which will put predetermined text on the clipboard
    String enter = EditorEnter;
}
