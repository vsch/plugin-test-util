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

package com.vladsch.plugin.test.util.cases;

import com.intellij.openapi.util.Key;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.vladsch.flexmark.test.util.SettableInstance;
import com.vladsch.flexmark.test.util.SpecExampleProcessor;
import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.CharPredicate;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SequenceUtils;
import com.vladsch.plugin.test.util.AdditionalProjectFiles;
import com.vladsch.plugin.test.util.DebugLogSettings;
import com.vladsch.plugin.test.util.LineMarkerSettings;
import com.vladsch.plugin.test.util.SpecTestSetup;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface SpecTest extends SpecExampleProcessor {
    DataKey<Boolean> IGNORE = TestUtils.IGNORE;
    DataKey<Boolean> FAIL = TestUtils.FAIL;
    DataKey<Boolean> NO_FILE_EOL = TestUtils.NO_FILE_EOL;
    DataKey<Integer> TIMED_ITERATIONS = TestUtils.TIMED_ITERATIONS;
    DataKey<Boolean> EMBED_TIMED = TestUtils.EMBED_TIMED;
    DataKey<Boolean> TIMED = TestUtils.TIMED;
    DataKey<Boolean> WANT_AST = new DataKey<>("WANT_AST", false);
    DataKey<Boolean> WANT_QUICK_FIXES = new DataKey<>("WANT_QUICK_FIXES", false);
    DataKey<Boolean> WANT_RANGES = new DataKey<>("WANT_RANGES", false);
    DataKey<Consumer<CodeStyleSettings>> CODE_STYLE_SETTINGS = SpecTestSetup.CODE_STYLE_SETTINGS;
    SettableInstance<CodeStyleSettings> CODE_STYLE_SETTINGS_OPTION = SpecTestSetup.CODE_STYLE_SETTINGS_OPTION;
    DataKey<Consumer<AdditionalProjectFiles>> ADDITIONAL_PROJECT_FILES = SpecTestSetup.ADDITIONAL_PROJECT_FILES;
    SettableInstance<AdditionalProjectFiles> ADDITIONAL_PROJECT_FILES_OPTION = SpecTestSetup.ADDITIONAL_PROJECT_FILES_OPTION;
    DataKey<Consumer<LineMarkerSettings>> LINE_MARKER_SETTINGS = SpecTestSetup.LINE_MARKER_SETTINGS;
    SettableInstance<LineMarkerSettings> LINE_MARKER_SETTINGS_OPTION = SpecTestSetup.LINE_MARKER_SETTINGS_OPTION;
    DataKey<Consumer<DebugLogSettings>> DEBUG_LOG_SETTINGS = SpecTestSetup.DEBUG_LOG_SETTINGS;
    SettableInstance<DebugLogSettings> DEBUG_LOG_SETTINGS_OPTION = SpecTestSetup.DEBUG_LOG_SETTINGS_OPTION;

    DataKey<BiFunction<String, String, DataHolder>> CUSTOM_OPTION = TestUtils.CUSTOM_OPTION;

    /**
     * Example source name overrides example generated name and extension
     */
    DataKey<String> EXAMPLE_SOURCE_NAME = new DataKey<>("EXAMPLE_SOURCE_NAME", "");
    /**
     * Example source name overrides example generated name and extension
     */
    DataKey<String> EXAMPLE_SOURCE_EXTENSION = new DataKey<>("EXAMPLE_SOURCE_EXTENSION", ".md");

    Map<String, DataHolder> optionsMap = new HashMap<>();

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            if (optionsMap.isEmpty()) {
                optionsMap.put("with-ast", new MutableDataSet().set(SpecTest.WANT_AST, true));
                optionsMap.put("with-ranges", new MutableDataSet().set(SpecTest.WANT_RANGES, true));
                optionsMap.put("with-quick-fixes", new MutableDataSet().set(SpecTest.WANT_QUICK_FIXES, true));
                optionsMap.put("no-ast", new MutableDataSet().set(SpecTest.WANT_AST, false));
                optionsMap.put("no-ranges", new MutableDataSet().set(SpecTest.WANT_RANGES, false));
                optionsMap.put("no-quick-fixes", new MutableDataSet().set(SpecTest.WANT_QUICK_FIXES, false));
                optionsMap.put("source-extension", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, SpecTest::sourceExtensionOption)));
                optionsMap.put("source-name", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, SpecTest::sourceNameOption)));
                optionsMap.put("log", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, SpecTest::debugLogOption)));
            }
            return optionsMap;
        }
    }

    static DataHolder debugLogOption(@Nullable String params) {
        if (params != null) {
            BasedSequence basedParams = BasedSequence.of(params);
            List<BasedSequence> list = basedParams.splitList(",", 0, SequenceUtils.SPLIT_TRIM_SKIP_EMPTY, CharPredicate.WHITESPACE_NBSP);
            return new MutableDataSet().set(DEBUG_LOG_SETTINGS, it -> it.trace(list));
        }

        throw new IllegalStateException("'log' option requires semicolon separated log ids");
    }

    static DataHolder sourceNameOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(EXAMPLE_SOURCE_NAME, params);
        }

        throw new IllegalStateException("'source-extension' option requires text for the extension");
    }

    static DataHolder sourceExtensionOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(EXAMPLE_SOURCE_EXTENSION, params);
        }

        throw new IllegalStateException("'source-extension' option requires text for the extension");
    }
}
