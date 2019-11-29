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

import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.plugin.test.util.cases.LightFixtureActionSpecTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionSpecRenderer<T extends LightFixtureActionSpecTest> extends LightFixtureSpecRenderer<T> {
    public ActionSpecRenderer(@NotNull T specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    protected void doTestAction() {
        String action = LightFixtureActionSpecTest.ACTION_NAME.get(myOptions);
        if (action.isEmpty()) {
            assertEquals(getExample().getFileUrlWithLineNumber() + "\nACTION_NAME cannot be empty", "action", "");
        } else if (!action.equals(LightFixtureActionSpecTest.SKIP_ACTION)) {
            try {
                mySpecTest.beforeDoTestAction(this, myOptions);

                if (action.equals(LightFixtureActionSpecTest.TYPE_ACTION)) {
                    String text = LightFixtureActionSpecTest.TYPE_ACTION_TEXT.get(myOptions);
                    if (!text.isEmpty()) {
                        type(text);
                    } else {
                        assertEquals(getExample().getFileUrlWithLineNumber() + "\nTYPE_ACTION_TEXT cannot be empty for TYPE_ACTION", "text to type", "");
                    }
                } else {
                    executeAction(action);
                }

                mySpecTest.afterDoTestAction(this, myOptions);
            } catch (Throwable t) {
                html.append(t.getMessage()).append("\n");
                t.printStackTrace(System.out);
                System.out.println();
            }
        }
    }

    @NotNull
    @Override
    public String renderHtml() {
        doTestAction();
        html.append(getResultTextWithMarkup(true, false));

        mySpecTest.renderTesActionHtml(html, this, myOptions);

        return html.toString();
    }
}
