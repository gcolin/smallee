# Common

Such as Google Guava or Apache Commons projects, it contains code that can increase the developement speed of a Java application.

## How to install

```
    gradle install
```

## Documentation

```
    gradle javadoc
```

see the report in **build/docs/javadocs**

## Advanced

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


