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

package com.vladsch.plugin.test.util.renderers;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.misc.Pair;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.LightFixtureIntentionSpecTest;
import com.vladsch.plugin.test.util.cases.SpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntentionSpecRenderer<T extends LightFixtureIntentionSpecTest> extends ActionSpecRenderer<T> {
    public IntentionSpecRenderer(@NotNull T specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    @Override
    protected void renderAst(StringBuilder out) {
        renderIntentions(out, true);
        super.renderAst(out);
    }

//          // NOTE: called in ActionSpecTestCase from renderHtml which is overridden here, so this is useless
//        @Override
//        protected void doTestAction() {
//            String intentionAction = INTENTION_ACTION.get(myOptions);
//            if (intentionAction.isEmpty()) {
//                if (myExample.isSpecExample()) {
//                    checkHighlighting(true, true, true, false);
//                }
//            }
//        }

    private static final Function<IntentionAction, String> INTENTION_NAME_FUN = intentionAction -> '"' + intentionAction.getText() + '"';

    @NotNull
    @Override
    public String renderHtml() {
        getFixture().enableInspections(LightFixtureIntentionSpecTest.INSPECTION_CLASSES.get(myOptions));

        String intentionAction = LightFixtureIntentionSpecTest.INTENTION_ACTION.get(myOptions);
        if (!intentionAction.isEmpty()) {
            // do this
            if (SpecTest.WANT_AST.get(myOptions)) {
                CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(ast, CodeInsightFixtureSpecTestCase.BANNER_BEFORE_ACTION);
                super.renderAst(ast);
                CodeInsightFixtureSpecTestCase.appendBannerIfNeeded(ast, CodeInsightFixtureSpecTestCase.BANNER_AFTER_ACTION);
            }

            List<IntentionAction> list = filterAvailableIntentions(intentionAction);

            if (list.isEmpty()) {
                return ("\"" + intentionAction + "\" not in [" + StringUtil.join(getAvailableIntentions(), INTENTION_NAME_FUN, ", ") + "]");
            } else if (list.size() > 1) {
                // see if there is an exact match
                final List<IntentionAction> matchedList = ContainerUtil.filter(list, action -> action.getText().equals(intentionAction));
                if (matchedList.size() == 1) {
                    list = matchedList;
                } else {
                    return ("Too many intentions found without an exact match for \"" + intentionAction + "\": [" + StringUtil.join(list, INTENTION_NAME_FUN, ", ") + "]");
                }
            }

            mySpecTest.beforeDoTestAction(this, myOptions);
            launchAction(list.get(0));
            mySpecTest.afterDoTestAction(this, myOptions);

            html.append(getResultTextWithMarkup(true, CodeInsightFixtureSpecTestCase.TEST_CARET_MARKUP.get(myOptions)));
            return html.toString();
        } else {
            // do comparison of what is there
            Pair<String, String> highlighting = mySpecTest.collectAndCheckHighlighting(this, false, true, true, true, false);
            return highlighting.getFirst();
        }
    }
}
