# Restito

This project is a partial jax rs 2.0 implementation. 

This project do not pass the TCK.

The project does not scan annotations without CDI.

## Why using this project

 - it uses few memory
 - it has few dependencies
 - it is customizable
 
## What's new in 1.1

* upgrade rext-ext-json -> jsonp 1.1 and jsonb 1.0
* ExceptionMapper works with inheritance
  
## How to install

Download and install thes dependencies.
```
    git clone https://github.com/gcolin/common.git
    cd common
    gradle install
	cd ..
	git clone https://github.com/gcolin/jsonbyte.git
    cd jsonbyte
    gradle install
	cd ..
	git clone https://github.com/gcolin/jaccepte.git
    cd jaccepte
    gradle install
```


Download and install the project.

```
    git clone https://github.com/gcolin/restito.git
    cd restito
    gradle install
```

## Documentation

```
    gradle javadoc
```

see the report in **build/docs/javadocs**

## Usage

Add the dependency
```xml
	<dependency>
		<groupId>net.gcolin.rest</groupId>
		<artifactId>rest-all</artifactId>
		<version>1.0</version>
	</dependency>
```

To use json, you will need
```xml
	<dependency>
		<groupId>net.gcolin</groupId>
		<artifactId>jsonbyte</artifactId>
		<version>1.0</version>
	</dependency>
```

To use parameter validation, you will need a validation implementation JSR 349: Hibernate Validator or bval-jsr or
```xml
	<dependency>
		<groupId>net.gcolin</groupId>
		<artifactId>jaccepte</artifactId>
		<version>0.1</version>
	</dependency>
```

### Running with CDI

Add Juikito CDI and create a class that extends **javax.ws.rs.core.Application** with the annotation **javax.ws.rs.ApplicationPath**.

A sample project is available in the folder *sample*. This project works with Eclipse EE and Tomcat 8.5.

### Running without CDI

Add in the **web.xml** something like this:

```xml
    <servlet>
        <servlet-name>RestApplication</servlet-name>
        <servlet-class>net.gcolin.rest.servlet.RestServlet</servlet-class>
        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>org.foo.rest.MyApplication</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>RestApplication</servlet-name>
        <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>
```

A sample project is available in the folder *sample-cdi*. This project works with Eclipse EE and Tomcat 8.5.

## Advanced

### Open in Eclipse

```
    gradle eclipse
```

In Eclipse, import Existing Projects into Workspace.

### Display a test code coverage report

```
    gradle clean test jacocoTestReport
```

see the report in **build/reports/jacoco**


### Display findBugs report

```
    gradle findBugsMain
```

see the report in **build/reports/findbugs**

### Display pmd report

```
    gradle pmdMain
```

see the report in **build/reports/pmd**

### Display licenses of dependencies

```
    gradle downloadLicenses
```

see the report in **build/reports/license**


### Display the Apache RAT report

```
    gradle rat
```

see the report in **build/reports/rat**
