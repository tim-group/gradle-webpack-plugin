Webpack plugin
==============

Publishing
----------

CI publishes to Nexus internally on each build as normal.

To publish to Gradle plugin portal, from a machine with internet access, do something like:

```bash
BUILD_NUMBER=11 ./gradlew --no-daemon publishPlugins
```

You must have already set up a plugin portal API key as described on the site: https://plugins.gradle.org/docs/submit