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

import com.intellij.codeInsight.daemon.GutterIconDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LineMarkerSettings {
    final HashMap<GutterIconDescriptor, Boolean> myOptions = new HashMap<>();

    public LineMarkerSettings() {}

    public LineMarkerSettings disable(@NotNull GutterIconDescriptor... options) {
        for (GutterIconDescriptor option : options) {
            myOptions.put(option, false);
        }
        return this;
    }

    public LineMarkerSettings enable(@NotNull GutterIconDescriptor... options) {
        for (GutterIconDescriptor option : options) {
            myOptions.put(option, true);
        }
        return this;
    }

    public LineMarkerSettings remove(@NotNull GutterIconDescriptor... options) {
        for (GutterIconDescriptor option : options) {
            myOptions.remove(option);
        }
        return this;
    }

    public HashMap<GutterIconDescriptor, Boolean> getOptions() {
        return myOptions;
    }

    @NotNull
    public Map<String, Boolean> getOptionsById() {
        HashMap<String, Boolean> result = new HashMap<>();
        for (GutterIconDescriptor option : myOptions.keySet()) {
            result.put(option.getId(), myOptions.get(option));
        }
        return result;
    }

    @NotNull
    public Set<String> getDisabledIds() {
        HashSet<String> result = new HashSet<>();
        for (GutterIconDescriptor option : myOptions.keySet()) {
            if (!myOptions.get(option)) result.add(option.getId());
        }
        return result;
    }

    @NotNull
    public Set<String> getEnabledIds() {
        HashSet<String> result = new HashSet<>();
        for (GutterIconDescriptor option : myOptions.keySet()) {
            if (myOptions.get(option)) result.add(option.getId());
        }
        return result;
    }
}
