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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.LineMarkerSettingsImpl;
import com.vladsch.flexmark.test.util.TestUtils;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.Pair;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.SequenceUtils;
import com.vladsch.plugin.test.util.LineMarkerSettings;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.LightFixtureLineMarkerSpecTest;
import com.vladsch.plugin.test.util.cases.SpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LineMarkerSpecRenderer<T extends LightFixtureLineMarkerSpecTest> extends LightFixtureSpecRenderer<T> {
    public LineMarkerSpecRenderer(@NotNull T specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    @Override
    protected void renderAst(StringBuilder out) {
        renderIntentions(out, true);
        renderIntentions(out, true);
        super.renderAst(out);
    }

    @NotNull
    @Override
    public String renderHtml() {
        String disableOne = LightFixtureLineMarkerSpecTest.DISABLE_ONE.get(myOptions).trim();

        if (!disableOne.isEmpty()) {
            List<String> options = SequenceUtils.splitList(disableOne, ";", 0, SequenceUtils.SPLIT_TRIM_SKIP_EMPTY);

            for (String option : options) {
                DataHolder modOptions = TestUtils.getOptions(myExample, "disable-" + option, this::options);
                updateDisabledLineMarkers(modOptions);

                DaemonCodeAnalyzer.getInstance(getProject()).restart(getFile());

                // do comparison of what is there for line markers
                Pair<String, String> highlighting = mySpecTest.collectAndCheckHighlighting(true, false, false, false, false);
                if (html.length() > 0) html.append("\n");
                CodeInsightFixtureSpecTestCase.appendBanner(html, CodeInsightFixtureSpecTestCase.bannerText("Disabled: " + option));
                html.append(highlighting.getSecond());
            }

            return html.toString();
        } else {
            updateDisabledLineMarkers(myOptions);

            // do comparison of what is there for line markers
            Pair<String, String> highlighting = mySpecTest.collectAndCheckHighlighting(true, false, false, false, false);
            return highlighting.getSecond();
        }
    }

    public static void updateDisabledLineMarkers(DataHolder options) {
        LineMarkerSettings markerSettings = SpecTest.LINE_MARKER_SETTINGS_OPTION.setInstanceData(new LineMarkerSettings(), options);
        Map<String, Boolean> lineMarkerOptions = markerSettings.getOptionsById();

        LineMarkerSettingsImpl lineMarkerSettings = (LineMarkerSettingsImpl) com.intellij.codeInsight.daemon.LineMarkerSettings.getSettings();
        lineMarkerSettings.providers.clear();

        // this adds any that were not in the settings to false
        lineMarkerSettings.providers.putAll(lineMarkerOptions);
    }
}
