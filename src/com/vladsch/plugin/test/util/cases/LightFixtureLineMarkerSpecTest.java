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

import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.plugin.test.util.renderers.LineMarkerSpecRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface LightFixtureLineMarkerSpecTest extends CodeInsightFixtureSpecTestCase {
    Map<String, DataHolder> optionsMap = new HashMap<>();
    DataKey<String> DISABLE_ONE = new DataKey<>("DISABLE_ONE_TEXT", "");

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            if (optionsMap.isEmpty()) {
                optionsMap.putAll(CodeInsightFixtureSpecTestCase.getOptionsMap());

                optionsMap.put("disable-one", new MutableDataSet().set(SpecTest.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureLineMarkerSpecTest::disableOneOption)));
            }
            return optionsMap;
        }
    }

    static DataHolder disableOneOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(DISABLE_ONE, params);
        }

        throw new IllegalStateException("'disable-one' option requires non-empty options, separated by ; to disable one at a time");
    }

    @Override
    LineMarkerSpecRenderer<?> createExampleSpecRenderer(@NotNull SpecExample example, @Nullable DataHolder options);
}
