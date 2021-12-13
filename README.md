# GSit

## Building

GSit depends on remapped versions of Spigot. You can build these using [BuildTools](https://www.spigotmc.org/wiki/buildtools/#running-buildtools). BuildTools will automatically install the builds to your local maven repo.

You must build for 1.17, 1.17.1, and 1.18, all with their respective Java versions.

With Java 16 on your `$JAVA_HOME`:

```bash
java -jar .\BuildTools.jar --rev 1.17 --remapped
```

```bash
java -jar .\BuildTools.jar --rev 1.17.1 --remapped
```

And with Java 17 on your `$JAVA_HOME`:

```bash
java -jar .\BuildTools.jar --rev 1.18.1 --remapped
```

After all remapped server jars have finished building and installing, you can build GSit like you would any other maven project with `mvn`.

With GSit installed to your local maven repository, you can depend on it in your plugin like any other:

```xml
<dependency>
    <groupId>dev.geco</groupId>
    <artifactId>GSit</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```
