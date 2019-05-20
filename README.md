# Alloy Generator

See also [*Component Development Checklist*](http://stiemannkj1.github.io/alloy-generator/component-development-checklist.html)

A code generator developed to help generating taglibs and facelet custom components based on AlloyUI inline code documentaiton.

![Preview](http://s17.postimg.org/4a9rf4nq7/Screen_Shot_2013_12_23_at_8_32_57_PM.png)

## Generating the source: 

To run the generator for the first time, no configuration is needed.

To generate only the `.xml` template, run:

```sh
$ mvn generate-sources -P generate-xml 
```

To generate the complete source code for `.jsp` taglibs, run:

```sh
$ mvn generate-sources -P generate-taglibs
```

To generate the complete source code for JSF Custom Components, run:

```sh
$ mvn generate-sources -P generate-faces
```

And it's gonna generate code for you under the [build](build) folder.

## Installing the generator:

To install the generator to your local `.m2/repository`, you must first run the `install.sh` script to install some dependencies from the `lib` folder which are not in Maven Central:

```sh
$ ./install.sh
```

After that, you can run this command to install the generator jar to your local repository (if you need to rebuild the generator, you can also run this command):

```sh
$ mvn clean install
```

## Configuring the generator: 

To configure the generator, simply create a `.properties` file and override the properties you want from the [build.properties](build.properties) file. After you have created your properties file, you can specify it for use in your `pom.xml` under the `<configuration>` section which is utilizing the `exec-maven-plugin` to run the `.jar`'s classes, as shown below:

```xml
<configuration>

	...

	<systemProperties>
		<systemProperty>
			<key>build-ext</key>
			<value>path/to/your/file.properties</value>
		</systemProperty>
	</systemProperties>

	...

</configuration>
```

You can also specify the `.properties` file using a system property like this:

```sh
-Dbuild-ext=path/to/your/file.properties
```

## Using the generator in your `pom.xml`:

To specify the generator to be used within your maven project, you will need to use the `exec-maven-plugin` to execute the Java class that you wish to use. You can use the code example below to specify the generator in the `<plugins>` section of your `pom.xml`:

```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>exec-maven-plugin</artifactId>
	<version>1.2.1</version>
	<executions>
		<execution>
			<goals>
				<goal>java</goal>
			</goals>
			<id>generate-xml</id>
			<phase>initialize</phase> <!-- can be any phase, but most likely you will want to use "initialize" or "generate-sources" -->
			<configuration>
				<includeProjectDependencies>false</includeProjectDependencies>
				<includePluginDependencies>true</includePluginDependencies>
				<mainClass>com.liferay.alloy.tools.transformer.AlloyDocsTransformer</mainClass>
				<!-- OR <mainClass>com.liferay.alloy.tools.builder.FacesBuilder.java</mainClass> <!-- If you want to use the Faces builder -->
				<!-- OR <mainClass>com.liferay.alloy.tools.builder.FacesBuilder.java</mainClass> <!-- If you want to use the Taglib builder --> 
				<executableDependency>
					<groupId>com.liferay.alloy</groupId>
					<artifactId>alloy-generator</artifactId>
				</executableDependency>
			</configuration>
		</execution>
	</executions>
	<dependencies>
		<dependency>
			<groupId>com.liferay.alloy</groupId>
			<artifactId>alloy-generator</artifactId>
			<version>0.1.0</version>
		</dependency>
	</dependencies>
</plugin>
```
