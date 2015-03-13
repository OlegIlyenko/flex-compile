## News ##

**21.05.09: Version 1.0-beta released!**

New features (for more details see ChangeLog):
  * Support of themes
  * Support of Flex 4 (Gumbo)
  * additional flex config xml file in app and comp settings
  * on server exception lost connention and no reconnection
  * Improved component modules
  * Localization support
  * Updated documentation
  * Example project and server configuration added to the documentation
  * Maven repository created

## Project description ##

**Project:**
flex-compile

**Version:**
1.0-beta

**Author:**
Oleg Ilyenko

**Modules:**
flex-compile-server,
flex-compile-common,
flex-compile-client,
flex-compile-distribution,
flex-compile-maven2-plugin

**Description:**


This project is wrapper for the Flex Compiler API. It's makes compilation faster by using applications and libraries cache and by performing compilation in the server. So Flex Compiler is held in the memory all the time.

In order to use flex-compile you should create simple flex project. See Flex project structure for details. So flex-compile can be used as simple flex project management tool.

Project also provides Maven 2 integration. It can be also included to any web application as filter, so that flex project compilation is made on fly. It dramatically simplifies and speeds up development productivity.

If you need additional help or information about the project, you can find it in the  [Reference manual](http://flex-compile.googlecode.com/files/flex-compile-reference-manual-1.0-beta.html). It also can be found in [project distribution](http://flex-compile.googlecode.com/files/flex-compile-distribution-1.0-beta.zip) (documentation/reference\_manual.html).

In order to see project in action you can start with provided [example project](http://flex-compile.googlecode.com/files/flex-compile-example-1.0-beta.zip).

Please, go to the [download section](http://code.google.com/p/flex-compile/downloads/list) in order to find project distribution files.

If you found a bug you can add it at [this page](http://code.google.com/p/flex-compile/issues/list).

Please post your comments and questions [here](http://code.google.com/p/flex-compile/wiki/UserFeedback)

Looking forward to your feedback! I hope this project would be useful for you!