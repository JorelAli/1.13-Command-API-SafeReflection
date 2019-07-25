# 1.13-Command-API-SafeReflections

An annotation processing system for the [1.13 CommandAPI](https://github.com/JorelAli/1.13-Command-API). Used to add extra compiler checks to ensure that reflection calls to methods and fields are safer.

## What does it do?

It makes sure that methods and field names of specific classes actually exist depending on what version your plugin is depending on. For example, say your plugin supports both 1.13 and 1.14. You're using reflection to retrieve the value of some String for example. The field name for this String is called `a` for the 1.13 library, but it's called `b` for the 1.14 library.

The current form of checking is to manually go through the libraries for 1.13 and 1.14 to check that the field actually exists, and ensure it will work properly. If for some reason, it doesn't exist, the error will be thrown only when you run the server.

SafeReflections checks it all for you at compile time and throws an error if it determines that a specific field or method name doesn't exist.

## Using SafeReflections

SafeReflections is designed only to work for maven projects that build on [Spigot](https://www.spigotmc.org/) _(i.e. Spigot plugins)_. 

### Directory setup

In your main project root folder _(where your `pom.xml` file is)_, you must have a directory called `spigotlibs`. Inside this folder should be your built `spigot-VERSION.jar`, which are generated using Spigot's BuildTools. _(For example, `spigot-1.14.jar` which is generated using the command `java -jar BuildTools.jar --rev 1.14`)_. **The naming of these libraries should not be changed, SafeReflections depends on it!**

You are allowed to have multiple versions of Spigot in your `spigotlibs` folder.

### Adding it to a maven project

Add the repository:

```xml
<repository>
    <id>mccommandapi</id>
    <url>https://raw.githubusercontent.com/JorelAli/1.13-Command-API/mvn-repo/1.13CommandAPI/</url>
</repository>
```

Add the dependency:

```xml
<dependency>
    <groupId>io.github.jorelali</groupId>
    <artifactId>commandapi-safereflection</artifactId>
    <version>1.0</version>
</dependency>
```

Add the annotation processor to your compiler setup:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId> 
            <configuration>
                <!-- Stuff goes here -->
                <annotationProcessors>
                    <annotationProcessor>io.github.jorelali.commandapi.safereflection.SafeReflectionProcessor</annotationProcessor>
                </annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Using it in your code

SafeReflections must be declared at the top of your class where you plan to use reflection. Then the general format is as follows:

```java
@SafeReflection(target = YourClass.class, method = "someMethod", versions = {"1.14", "1.14.1"})
@SafeReflection(target = AnotherClass.class, field = "someField", versions = "1.13.2")
public Class MyClass {
    //...
}
```

The annotation can take the following fields:

| SafeReflection field |                  values                 | Required? |
|:--------------------:|:---------------------------------------:|:---------:|
|       `target`       |  A Class where reflection is to be used |    yes    |
|       `method`       | A String of the method name to retrieve |     no    |
|        `field`       |  A String of the field name to retrieve |     no    |
|      `versions`      |     A String[] of Minecraft versions    |    yes    |

## Real life examples

This is primarily used in the CommandAPI. You can view an example use of it [here](https://github.com/JorelAli/1.13-Command-API/blob/master/1.13CommandAPI/src/io/github/jorelali/commandapi/api/nms/NMS_1_14_R1.java).
