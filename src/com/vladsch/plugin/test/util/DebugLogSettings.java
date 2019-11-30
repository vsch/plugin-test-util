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

package com.vladsch.plugin.test.util;

import com.intellij.diagnostic.DebugLogManager;
import com.vladsch.flexmark.util.collection.iteration.ArrayIterable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DebugLogSettings {
    final HashMap<String, DebugLogManager.DebugLogLevel> myOptions = new HashMap<>();

    public DebugLogSettings() {}

    public DebugLogSettings debug(@NotNull CharSequence... options) {
        return debug(ArrayIterable.of(options));
    }

    public DebugLogSettings debug(@NotNull Iterable<? extends CharSequence> options) {
        for (CharSequence option : options) {
            myOptions.put(option.toString(), DebugLogManager.DebugLogLevel.DEBUG);
        }
        return this;
    }

    public DebugLogSettings trace(@NotNull CharSequence... options) {
        return trace(ArrayIterable.of(options));
    }

    public DebugLogSettings trace(@NotNull Iterable<? extends CharSequence> options) {
        for (CharSequence option : options) {
            myOptions.put(option.toString(), DebugLogManager.DebugLogLevel.TRACE);
        }
        return this;
    }

    public DebugLogSettings remove(@NotNull CharSequence... options) {
        return remove(ArrayIterable.of(options));
    }

    public DebugLogSettings remove(@NotNull Iterable<? extends CharSequence> options) {
        for (CharSequence option : options) {
            myOptions.remove(option.toString());
        }
        return this;
    }

    public List<DebugLogManager.Category> getLogCategories() {
        List<DebugLogManager.Category> categories = new ArrayList<>();
        for (Map.Entry<String, DebugLogManager.DebugLogLevel> entry : myOptions.entrySet()) {
            categories.add(new DebugLogManager.Category(entry.getKey(), entry.getValue()));
        }
        return categories;
    }

    public HashMap<String, DebugLogManager.DebugLogLevel> getOptions() {
        return myOptions;
    }
}
