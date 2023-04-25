# Plugin Test Util Library

[TOC]: #

### Table of Contents
- [Summary](#summary)
- [Test File Format](#test-file-format)

### Summary

Collection of utilities and classes used for testing JetBrains API plugins using flexmark-java
spec files to provide input and expected results.

The tests consist of a Java test case file providing the implementation for actions and a
special format markdown file providing the input and options for each test.

The Java test case implements the definition of options used in the markdown test spec and the
test actions to be performed on the input. Each test case class is parameterized and only has
one test method.

The parameter data comes from the markdown spec file, each input called a spec example, after
the CommonMark spec examples which were the inspiration for this test format.

The test example appears as an individual test in IntelliJ but there is also a `Full Spec` test
which contains the original spec file inputs for all examples with actual results of the test
run. This allows updating of all expected values from actual results using a single diff in the
IDE. It makes updating expected from actual values a quick and painless operation, performed on
all tests in the spec instead of a tedious cut/paste/edit process on each test separately.

### Test File Format

The markdown file format is based on the examples used in [CommonMark (spec 0.28)] spec.txt
file, augmented to allow arbitrary configuration options on a per-test basis and an extra
section per example to provide expected resulting AST or any other desired text output to be
embedded in the tests expected result.

The options are given as text strings in the Markdown file with mapping done in the Java test
case class file. Most of the `flexmark-java` library tests are in this format and I found it so
convenient that I started to use them for [Markdown Navigator] plugin for IntelliJ
`LightPlatformCodeInsightTestCase` derived tests and finally factored out the code as a library
so I could use it for testing all my plugins.

The caret positions and selection markers, for now, are hardcoded to be `⦙` for caret and `⟦ ⟧`
for selection markers. They do not stand out as much as the standard: `<caret>`, `<selection>`
and `</selection>` IDE markup but they also do not look like HTML to markdown source and tend to
disturb the visual placement of text less.

Here is a sample, input from expected results is separated by a line with a single `.` at the
start. Options `type[]`, `margin[]`, `wrap` are all Java test file specific options. In this
case used to set right margin, turn on wrap on typing and invoke `type()` for text given between
`[]`.

    ## Typing Handler

    ```````````````````````````````` example(Typing Handler: 1) options(type[ ])
    ⦙text
    .
     ⦙text
    ````````````````````````````````


    ```````````````````````````````` example(Typing Handler: 2) options(type[ ])
    ⦙ text
    .
     ⦙ text
    ````````````````````````````````


    ```````````````````````````````` example(Typing Handler: 3) options(wrap, margin[30], type[ ])
    ⦙text should wrap onto the next line at right margin of 30
    .
     ⦙text should wrap onto the
     next line at right margin of
     30
    ````````````````````````````````

---

Copyright (c) 2019-2023, Vladimir Schneider,

Apache License Version 2.0, see [LICENSE.txt] file.

[CommonMark (spec 0.28)]: https://spec.commonmark.org/0.28
[LICENSE.txt]: LICENSE.txt
[Markdown Navigator]: https://github.com/vsch/idea-multimarkdown

