# Simple Mustache

An alternative implementation of the java mustache implementation [mustache.java](https://github.com/spullara/mustache.java).

This project have no compile dependency.

## Usage

Look at the Sample file in *src/test/java/net/gcolin/mustache/test/Samples.java*

## How to install

```
    gradle install
```

The maven dependency
```
<dependency>
  <groupId>net.gcolin</groupId>
  <artifactId>simplemustache</artifactId>
  <version>1.0</version>
</dependency>
```

## Documentation

```
    gradle javadoc
```

see the report in **build/docs/javadocs**

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

### Display PMD report

```
    gradle pmdMain
```

see the report in **build/reports/pmd**

### Display findBugs report

```
    gradle findBugsMain
```

see the report in **build/reports/findbugs**

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