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

import com.intellij.openapi.actionSystem.IdeActions;

public interface TestIdeActions extends IdeActions {
    // editor actions
    String EditorBackspace = ACTION_EDITOR_BACKSPACE;
    String EditorSelectWordAtCaret = ACTION_EDITOR_SELECT_WORD_AT_CARET;
    String EditorDuplicate = ACTION_EDITOR_DUPLICATE;
    String EditorDelete = ACTION_EDITOR_DELETE;
    String EditorMoveLineStart = ACTION_EDITOR_MOVE_LINE_START;
    String EditorMoveLineEnd = ACTION_EDITOR_MOVE_LINE_END;
    String EditorMoveLineStartWithSelection = ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION;
    String EditorMoveLineEndWithSelection = ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION;
    String EditorCopy = ACTION_EDITOR_COPY;
    String EditorPaste = ACTION_EDITOR_PASTE;
    String EditorPreviousWordWithSelection = ACTION_EDITOR_PREVIOUS_WORD_WITH_SELECTION;
    String EditorNextWordWithSelection = ACTION_EDITOR_NEXT_WORD_WITH_SELECTION;
    String EditorPreviousWord = ACTION_EDITOR_PREVIOUS_WORD;
    String EditorNextWord = ACTION_EDITOR_NEXT_WORD;
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
    String CommentLine = ACTION_COMMENT_LINE;
    String EditorEnter = ACTION_EDITOR_ENTER;
    String EditorTab = ACTION_EDITOR_TAB;

    // alias for editor actions
    String backspace = EditorBackspace;
    String up = EditorUp;
    String down = EditorDown;
    String left = EditorLeft;
    String right = EditorRight;
    String paste = EditorPaste;
    String copy = EditorCopy; // NOTE: clipboard[] and clipboard-file-url[] variation which will put predetermined text on the clipboard
    String enter = EditorEnter;
    String tab = "tab";
    String backtab = "back-tab";
    String inject = "inject";
}
