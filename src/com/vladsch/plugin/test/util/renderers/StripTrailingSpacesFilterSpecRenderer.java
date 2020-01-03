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

import com.intellij.openapi.editor.impl.DocumentImpl;
import com.vladsch.flexmark.test.util.spec.SpecExample;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.plugin.test.util.LightPlatformCodeInsightFixtureSpecTestCase;
import com.vladsch.plugin.test.util.cases.CodeInsightFixtureSpecTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StripTrailingSpacesFilterSpecRenderer extends LightFixtureSpecRenderer<CodeInsightFixtureSpecTestCase> {
    public StripTrailingSpacesFilterSpecRenderer(@NotNull LightPlatformCodeInsightFixtureSpecTestCase specTestBase, @NotNull SpecExample example, @Nullable DataHolder options) {
        super(specTestBase, example, options);
    }

    @NotNull
    @Override
    public String renderHtml() {
        // just need the PsiFile
        if (Objects.equals(myExample.getSection(), "Empty List Item") && myExample.getExampleNumber() == 3) {
            int tmp = 0;
        }

        // apply editor trailing spaces filtering
        DocumentImpl document = (DocumentImpl) getEditor().getDocument();
        document.setStripTrailingSpacesEnabled(true);
        document.stripTrailingSpaces(getProject());
        return getResultTextWithMarkup(false, false);
    }
}
