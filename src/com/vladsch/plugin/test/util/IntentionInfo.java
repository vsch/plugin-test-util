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

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.TextRange;
import com.vladsch.flexmark.util.sequence.Range;
import org.jetbrains.annotations.NotNull;

public class IntentionInfo implements Comparable<IntentionInfo> {
    public static final IntentionInfo[] EMPTY_INTENTION_INFO = new IntentionInfo[0];
    final public boolean fileLevel;
    final public @NotNull IntentionAction action;
    final public @NotNull Range range;
    final public @NotNull IntentionInfo[] subActions;

    public IntentionInfo(boolean fileLevel, @NotNull IntentionAction action, @NotNull Range range, @NotNull IntentionInfo[] subActions) {
        this.fileLevel = fileLevel;
        this.action = action;
        this.range = range;
        this.subActions = subActions;
    }

    private int firstNonZero(int... values) {
        for (int value : values) {
            if (value != 0) return value;
        }
        return 0;
    }

    @Override
    public int compareTo(@NotNull IntentionInfo o) {
        int fileLevelCompare = fileLevel != o.fileLevel ? (fileLevel ? 1 : -1) : 0;
        int textCompare = action.getText().compareTo(o.action.getText());
        int rangeCompare = range.compare(o.range);
        return firstNonZero(fileLevelCompare, textCompare, rangeCompare);
    }

    public static IntentionInfo of(boolean fileLevel, @NotNull IntentionAction action) {
        return new IntentionInfo(fileLevel, action, Range.NULL, EMPTY_INTENTION_INFO);
    }

    public static IntentionInfo of(boolean fileLevel, @NotNull IntentionAction action, @NotNull Range range, @NotNull IntentionInfo[] subActions) {
        return new IntentionInfo(fileLevel, action, range, subActions);
    }

    public static IntentionInfo of(boolean fileLevel, @NotNull IntentionAction action, @NotNull TextRange range, @NotNull IntentionInfo[] subActions) {
        return new IntentionInfo(fileLevel, action, Range.of(range.getStartOffset(), range.getEndOffset()), subActions);
    }
}
