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

import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.vladsch.flexmark.test.util.SettableInstance;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.DataKeyAggregator;
import com.vladsch.flexmark.util.data.DataSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public class SpecTestSetup {
    // @formatter:off
    final static public DataKey<Consumer<CodeStyleSettings>> CODE_STYLE_SETTINGS = new DataKey<>("CODE_STYLE_SETTINGS", (it) -> {});
    final public static SettableInstance<CodeStyleSettings> CODE_STYLE_SETTINGS_OPTION = new SettableInstance<>(CODE_STYLE_SETTINGS);

    final static public DataKey<Consumer<AdditionalProjectFiles>> ADDITIONAL_PROJECT_FILES = new DataKey<>("ADDITIONAL_PROJECT_FILES", (it) -> {});
    final public static SettableInstance<AdditionalProjectFiles> ADDITIONAL_PROJECT_FILES_OPTION = new SettableInstance<>(ADDITIONAL_PROJECT_FILES);

    final static public DataKey<Consumer<LineMarkerSettings>> LINE_MARKER_SETTINGS = new DataKey<>("LINE_MARKER_SETTINGS", (it) -> {});
    final public static SettableInstance<LineMarkerSettings> LINE_MARKER_SETTINGS_OPTION = new SettableInstance<>(LINE_MARKER_SETTINGS);

    final static public DataKey<Consumer<DebugLogSettings>> DEBUG_LOG_SETTINGS = new DataKey<>("DEBUG_LOG_SETTINGS", (it) -> {});
    final public static SettableInstance<DebugLogSettings> DEBUG_LOG_SETTINGS_OPTION = new SettableInstance<>(DEBUG_LOG_SETTINGS);
    
    final static public DataKey<Consumer<PsiFile>> CUSTOMIZE_FILE = new DataKey<>("CUSTOMIZE_FILE", (it) -> { });
    final public static SettableInstance<PsiFile> CUSTOMIZE_FILE_OPTION = new SettableInstance<>(CUSTOMIZE_FILE);

    // @formatter:on

    private final static SettingsKeyAggregator INSTANCE = new SettingsKeyAggregator();
    static {
        DataSet.registerDataKeyAggregator(INSTANCE);
    }

    static class SettingsKeyAggregator implements DataKeyAggregator {
        @NotNull
        @Override
        public DataHolder aggregate(@NotNull DataHolder combined) {
            return combined;
        }

        @NotNull
        @Override
        public DataHolder aggregateActions(@NotNull DataHolder combined, @NotNull DataHolder other, @NotNull DataHolder overrides) {
            combined = CODE_STYLE_SETTINGS_OPTION.aggregateActions(combined, other, overrides);
            combined = ADDITIONAL_PROJECT_FILES_OPTION.aggregateActions(combined, other, overrides);
            combined = LINE_MARKER_SETTINGS_OPTION.aggregateActions(combined, other, overrides);
            combined = DEBUG_LOG_SETTINGS_OPTION.aggregateActions(combined, other, overrides);
            combined = CUSTOMIZE_FILE_OPTION.aggregateActions(combined, other, overrides);
            return combined;
        }

        @NotNull
        @Override
        public DataHolder clean(DataHolder combined) {
            return combined;
        }

        @Nullable
        @Override
        public Set<Class<?>> invokeAfterSet() {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Consumer chainConsumerDataKeys(DataKey<Consumer> dataKey, @NotNull DataHolder other, @NotNull DataHolder overrides) {
        //noinspection unchecked
        return dataKey.get(other).andThen(dataKey.get(overrides));
    }
}
