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

package com.vladsch.plugin.test.util;

import com.vladsch.flexmark.test.util.spec.ResourceLocation;
import com.vladsch.flexmark.util.misc.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class AdditionalProjectFiles {
    final private @NotNull HashMap<String, Object> myFiles = new HashMap<>();

    public AdditionalProjectFiles add(@NotNull String relativePath, @NotNull String fileText) {
        myFiles.put(relativePath, fileText);
        return this;
    }

    public AdditionalProjectFiles add(@NotNull ResourceLocation resourceLocation) {
        return add(resourceLocation, "");
    }

    public boolean isImageExt(@NotNull String filePath) {
        return Utils.endsWith(filePath, true, ".png", ".jpg", ".jpeg", ".gif", ".svg");
    }

    public AdditionalProjectFiles add(@NotNull ResourceLocation resourceLocation, @NotNull String relativePath) {
        Object fileText;
        if (isImageExt(resourceLocation.getResourcePath())) {
            // this is an image, text will be null
            fileText = resourceLocation;
        } else {
            fileText = resourceLocation.getResourceText();
        }

        String path = relativePath.isEmpty() ? resourceLocation.getResourcePath() : relativePath;
        myFiles.put(path, fileText);
        return this;
    }

    @NotNull
    public HashMap<String, Object> getFiles() {
        return myFiles;
    }
}
