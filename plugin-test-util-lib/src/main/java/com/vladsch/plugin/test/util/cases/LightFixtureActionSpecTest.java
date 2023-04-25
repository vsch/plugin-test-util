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
    DataKey<String> CLIPBOARD_TEXT = new DataKey<>("CLIPBOARD_TEXT", "");
    DataKey<String> CLIPBOARD_FILE_URL = new DataKey<>("CLIPBOARD_FILE_URL", "");
    DataKey<String> INJECTED_TEXT = new DataKey<>("INJECTED_TEXT", "");
    DataKey<Integer> ACTION_REPEAT = new DataKey<>("ACTION_REPEAT", 1);
    DataKey<Boolean> CLIPBOARD_CONTENT = new DataKey<>("CLIPBOARD_CONTENT", false);
    String TYPE_ACTION = "type";
    String SKIP_ACTION = "no-op";

    Map<String, DataHolder> optionsMap = new HashMap<>();

    static Map<String, DataHolder> getOptionsMap() {
        synchronized (optionsMap) {
            if (optionsMap.isEmpty()) {
                optionsMap.put("type-comma", new MutableDataSet().set(ACTION_NAME, ","));
                optionsMap.put("backspace", new MutableDataSet().set(ACTION_NAME, backspace));
                optionsMap.put("show-clipboard", new MutableDataSet().set(CLIPBOARD_CONTENT, true));
                optionsMap.put("enter", new MutableDataSet().set(ACTION_NAME, enter));
                optionsMap.put("copy", new MutableDataSet().set(ACTION_NAME, copy));
                optionsMap.put("paste", new MutableDataSet().set(ACTION_NAME, paste));
                optionsMap.put("tab", new MutableDataSet().set(ACTION_NAME, tab));
                optionsMap.put("back-tab", new MutableDataSet().set(ACTION_NAME, backtab));
                optionsMap.put("type", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureActionSpecTest::typeOption)));
                optionsMap.put("clipboard", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureActionSpecTest::clipboardOption)));
                optionsMap.put("clipboard-file-url", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureActionSpecTest::clipboardFileUrl)));
                optionsMap.put("inject", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customStringOption(option, params, LightFixtureActionSpecTest::injectOption)));
                optionsMap.put("repeat", new MutableDataSet().set(TestUtils.CUSTOM_OPTION, (option, params) -> TestUtils.customIntOption(option, params, LightFixtureActionSpecTest::repeatOption)));
            }
            return optionsMap;
        }
    }

    static DataHolder repeatOption(@Nullable Integer params) {
        int value = params != null ? params : 1;
        return new MutableDataSet().set(ACTION_REPEAT, value);
    }

    static DataHolder injectOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(ACTION_NAME, inject).set(INJECTED_TEXT, params);
        }

        throw new IllegalStateException("'inject' option requires non-empty text argument");
    }

    static DataHolder clipboardOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(CLIPBOARD_TEXT, params);
        }

        throw new IllegalStateException("'clipboard' option requires non-empty text argument");
    }

    /**
     * Copy the additional file virtual file URL given by file name in the option's text, with additional text appended from
     * clipboard[] option text. The latter can be used to add ref-anchor to the URL
     *
     * @param params text
     *
     * @return data
     */
    static DataHolder clipboardFileUrl(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(CLIPBOARD_FILE_URL, params);
        }

        throw new IllegalStateException("'clipboard-file-url' option requires non-empty text argument");
    }

    static DataHolder typeOption(@Nullable String params) {
        if (params != null) {
            return new MutableDataSet().set(ACTION_NAME, TYPE_ACTION).set(TYPE_ACTION_TEXT, params);
        }

        throw new IllegalStateException("'type' option requires non-empty text argument");
    }

    /**
     * Create spec renderer for example
     *
     * @param example spec example
     * @param options options
     *
     * @return action spec renderer
     */
    @Override
    ActionSpecRenderer<?> createExampleSpecRenderer(@NotNull SpecExample example, @Nullable DataHolder options);

    /**
     * Load extra settings and initialize spec renderer for parse
     *
     * @param <T>                 spec renderer type
     * @param specRenderer        spec renderer
     * @param specRendererOptions spec renderer options
     */
    <T extends LightFixtureActionSpecTest> void beforeDoTestAction(@NotNull ActionSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     * Reset extra settings for next test and clean up any resources
     *
     * @param <T>                 spec renderer type
     * @param specRenderer        spec renderer
     * @param specRendererOptions spec renderer options
     */
    <T extends LightFixtureActionSpecTest> void afterDoTestAction(@NotNull ActionSpecRenderer<T> specRenderer, @NotNull DataHolder specRendererOptions);

    /**
     * Render the test action html for the spec renderer
     *
     * @param <T>                 spec renderer type
     * @param html                html builder
     * @param specRenderer        spec renderer
     * @param specRendererOptions spec renderer options
     */
    <T extends LightFixtureActionSpecTest> void renderTesActionHtml(@NotNull StringBuilder html, @NotNull ActionSpecRenderer<T> specRenderer, DataHolder specRendererOptions);
}
