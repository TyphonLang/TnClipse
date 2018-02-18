# TnClipse: Typhon support for the Eclipse IDE

## Overview

TnClipse is an Eclipse plugin for the [Typhon](https://github.com/TyphonLang/Typhon) language.

## Features

* Syntax highlighting and outline view for Typhon files!
* Maintain Typhon projects in Eclipse!
* Built-in [TnBox](https://github.com/TyphonLang/TnBox) support- Run Typhon programs from inside Eclipse!

## Installing

To install TnClipse in your IDE, go to the `Help` menu, go to `Install new software...`, and then you will see the installation dialog. In the `Work with` field, there is a button labeled `Add...`; click on that and enter `http://typhonlang.github.io/tnclipse/` in the `Location` field. Name it whatever you want. Hit `OK`, and soon enough the Typhon plugin will appear in the dialog. Check the checkbox called `Typhon`, and hit `Next`. Follow any further instructions, and TnClipse should now be installed.

## Building

TnClipse must be built within Eclipse itself. To set up TnClipse for development, run:

```
./gradlew getLibs
```

And then import this project into Eclipse.

### From local JARs

To let TnClipse use a Typhon and TnBox JAR built locally, ensure this repository, Typhon's repository, and TnBox's repository are sitting inside your local machine, in the same directory. Then run the following in the two repositories:

```
./gradlew jar sourcesJar
```

And then run this in TnClipse's repository:

```
./gradlew getLibsDev
```

Run `gradle jar sourcesJar` again after you make any changes to Typhon/TnBox itself.