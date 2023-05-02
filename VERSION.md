# Plugin Util Version History

[TOC]: #

### Table of Contents
- [Version 1.213.6](#version-12136)
- [Version 1.212.4](#version-12124)
- [Version 1.0.2](#version-102)
- [Version 1.0.0](#version-100)

### Version 1.213.6

* Change: compatibility with IDE 2021.3 and up
* Fix: remove invocation of `tearDown()`, 
* Fix: remove dependency on deprecated IntelliJ test case classes
* Fix: `ParamRowGenerator` nullability of row params to be non-null array of nullable objectes.

### Version 1.212.4

* Change: disable intellij plugin related tasks
* Change: switch to sev ver, minor ver reflects IDE min version compatibility, patch ver
  reflects changes to the library itself
* Change: migrate to gradle build.

### Version 1.0.2

* Fix: default test case when running individual spec examples always adding AST section
  regardless of spec AST presence.
* Add: `log[]` option to enable IDE debug logs in spec example options

### Version 1.0.0

* Initial version

