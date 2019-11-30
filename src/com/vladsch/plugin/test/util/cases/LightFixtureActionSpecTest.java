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

import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.plugin.test.util.TestIdeActions;
import com.vladsch.plugin.test.util.renderers.ActionSpecRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface LightFixtureActionSpecTest extends CodeInsightFixtureSpecTestCase, TestIdeActions {
    DataKey<String> ACTION_NAME = new DataKey<>("ACTION_NAME", "");
    DataKey<String> TYPE_ACTION_TEXT = new DataKey<>("TYPE_ACTION_TEXT", "");
    String TYPE_ACTION = "type";
    String SKIP_ACTION = "no-op";

    Map<String, DataHolder> optionsMap = new HashMap<>();

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            if (optionsMap.isEmpty()) {
                optionsMap.put("type-comma", new MutableDataSet().set(ACTION_NAME, ","));
                optionsMap.put("backspace", new MutableDataSet().set(ACTION_NAME, backspace));
                optionsMap.put("enter", new MutableDataSet().set(ACTION_NAME, enter));
                optionsMap.put("type", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureActionSpecTest::typeOption)));
            }
            return optionsMap;
        }
    }

    static DataHolder typeOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(ACTION_NAME, TYPE_ACTION).set(TYPE_ACTION_TEXT, params);
        }

        throw new IllegalStateException("'type' option requires non-empty text argument");
    }

    @Override
    ActionSpecRenderer<?> createExampleSpecRenderer(@NotNull SpecExample example, @Nullable DataHolder options);

    /**
     * Load extra settings and initialize spec renderer for parse
     */
    <T extends LightFixtureActionSpecTest> void beforeDoTestAction(@NotNull ActionSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     * Reset extra settings for next test and clean up any resources
     */
    <T extends LightFixtureActionSpecTest> void afterDoTestAction(@NotNull ActionSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     *
     */
    <T extends LightFixtureActionSpecTest> void renderTesActionHtml(@NotNull StringBuilder html, @NotNull ActionSpecRenderer<T> specRenderer, DataHolder specRendererOptions);
}
