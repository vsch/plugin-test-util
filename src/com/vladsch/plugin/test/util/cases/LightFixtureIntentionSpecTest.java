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

import com.intellij.codeInspection.LocalInspectionTool;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.data.NullableDataKey;
import com.vladsch.plugin.test.util.renderers.IntentionSpecRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface LightFixtureIntentionSpecTest extends LightFixtureActionSpecTest {
    Class<? extends LocalInspectionTool>[] EMPTY_CLASSES = new Class[0];
    DataKey<Class<? extends LocalInspectionTool>[]> INSPECTION_CLASSES = new DataKey<>("INSPECTION_CLASSES", EMPTY_CLASSES);
    DataKey<String> INTENTION_ACTION = new DataKey<>("INTENTION_ACTION", "");
    NullableDataKey<String> FILE_PARAM = new NullableDataKey<>("FILE_PARAM", (String) null);

    Map<String, DataHolder> optionsMap = new HashMap<>();

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            if (optionsMap.isEmpty()) {
                optionsMap.put("intention", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, LightFixtureIntentionSpecTest::intentionOption));
                optionsMap.put("file-param", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, LightFixtureIntentionSpecTest::fileParamOption));
                optionsMap.put("caret-markup", new MutableDataSet().set(CodeInsightFixtureSpecTestCase.TEST_CARET_MARKUP, true));
            }
            return optionsMap;
        }
    }

    static DataHolder intentionOption(@NotNull String option, @Nullable String params) {
        if (params != null) {
            // allow escape
            return new MutableDataSet().set(INTENTION_ACTION, params);
        }

        throw new IllegalStateException("'intention' option requires non-empty text argument");
    }

    static DataHolder fileParamOption(@NotNull String option, @Nullable String params) {
        if (params != null) {
            // allow escape
            return new MutableDataSet().set(FILE_PARAM, params);
        }

        throw new IllegalStateException("'file-param' option requires non-empty text argument");
    }

    @Override
    IntentionSpecRenderer<?> createExampleSpecRenderer(@NotNull SpecExample example, @Nullable DataHolder options);
}
