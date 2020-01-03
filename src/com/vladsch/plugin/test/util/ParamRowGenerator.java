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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ParamRowGenerator {
    private final @Nullable String locationPrefix;
    private int index;

    public interface Decorator {
        String decorate(int index, @NotNull String prefix, @NotNull String suffix);
    }

    public interface ColumnProvider {
        int getColumn(@NotNull Object[] row);
    }

    public interface LineProvider {
        int getLineOffset(@NotNull Object[] row);
    }

    final public static Decorator NULL_DECORATOR = (index, prefix, suffix) -> prefix + suffix;
    final public static ColumnProvider NULL_COLUMN_PROVIDER = (row) -> 0;
    final public static LineProvider NULL_LINE_PROVIDER = (row) -> 0;

    final public ArrayList<Object[]> rows = new ArrayList<>();

    final public ArrayList<Object[]> getRows() {
        return rows;
    }

    public ParamRowGenerator(@NotNull String locationPrefix) {
        this(locationPrefix, null);
    }

    public ParamRowGenerator(@NotNull String testRoot, @Nullable Class<?> testClass) {
        String rootPrefix = testRoot.endsWith("/") ? testRoot : testRoot + "/";
        this.locationPrefix = testClass == null ? rootPrefix : rootPrefix + testClass.getPackage().getName().replace('.', '/') + "/";
        this.index = 0;
    }

    public ParamRowGenerator() {
        this.locationPrefix = null;
        this.index = 0;
    }

    /**
     * Add parametrized test row and prefix with file location information at [0]
     *
     * @param row an array of objects parameters for the test
     *
     * @return this
     */
    public ParamRowGenerator row(Object[] row) {
        return row(1, row, null, null, null);
    }

    /**
     * Add parametrized test row and prefix with file location information at [0]
     *
     * @param callerOffset offset to the stack frame 0 if called directly from test, add 1 for every super call in between
     * @param row          an array of objects parameters for the test
     * @param decorator    decorator to customize the test message
     *
     * @return this
     */
    protected ParamRowGenerator row(int callerOffset, @NotNull Object[] row, @Nullable Decorator decorator) {
        return row(callerOffset + 1, row, decorator, null, null);
    }

    /**
     * Add parametrized test row and prefix with file location information at [0]
     *
     * @param callerOffset offset to the stack frame 0 if called directly from test, add 1 for every super call in between
     * @param row          an array of objects parameters for the test
     * @param decorator    decorator to customize the test message
     *
     * @return this
     */
    public ParamRowGenerator row(int callerOffset, @NotNull Object[] row, @Nullable Decorator decorator, @Nullable LineProvider lineProvider, @Nullable ColumnProvider columnProvider) {
        return rowOffset(callerOffset + 1, 0, row, decorator, lineProvider, columnProvider);
    }

    /**
     * Add parametrized test row and prefix with file location information at [0]
     *
     * @param callerOffset offset to the stack frame 0 if called directly from test, add 1 for every super call in between
     * @param insertAt     where to insert location string
     * @param row          an array of objects parameters for the test
     * @param decorator    decorator to customize the test message
     *
     * @return this
     */
    public ParamRowGenerator rowOffset(int callerOffset, int insertAt, @NotNull Object[] row, @Nullable Decorator decorator, @Nullable LineProvider lineProvider, @Nullable ColumnProvider columnProvider) {
        assert insertAt >= 0 && insertAt <= row.length;

        if (decorator == null) decorator = NULL_DECORATOR;
        if (columnProvider == null) columnProvider = NULL_COLUMN_PROVIDER;
        if (lineProvider == null) lineProvider = NULL_LINE_PROVIDER;

        Object[] newRow = new Object[row.length + 1];
        if (insertAt == 0) {
            System.arraycopy(row, 0, newRow, insertAt + 1, row.length);
        } else {
            System.arraycopy(row, 0, newRow, 0, insertAt);
            System.arraycopy(row, insertAt, newRow, insertAt + 1, row.length - insertAt);
        }

        // stack trace elements [1] is this function, [2] is direct caller of this function,
        // add 1 for every level of function call nesting before reaching here
        StackTraceElement callerInfo = Thread.currentThread().getStackTrace()[2 + callerOffset];

        int index = this.index++;
        String file = locationPrefix == null ? "fqn://" + removeSuffix(callerInfo.getClassName()) : locationPrefix + callerInfo.getFileName();
        int line = callerInfo.getLineNumber() + lineProvider.getLineOffset(row);
        int column = columnProvider.getColumn(row);

        String prefix = String.format("%d: ", index);
        String suffix = line <= 0 ? file : column <= 1 ? String.format("%s:%d", file, line) : String.format("%s:%d:%d", file, line, column);
        newRow[insertAt] = decorator.decorate(index, prefix, suffix);
        rows.add(newRow);

        return this;
    }

    @NotNull
    public static String removeSuffix(@NotNull String className) {
        int pos = className.indexOf('$');
        if (pos > 0) return className.substring(0, pos);
        else return className;
    }
}
