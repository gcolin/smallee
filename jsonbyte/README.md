# JsonByte

This project is a javax.json 1.1 (JSR 374) and javax.jsonb (JSR 367) implementation. It is a fast working implementation. It is not tested against the TCK, so it may be not fully compatible.

The JSR 367 is not released yet and the api utilized is the Apache Johnzon api.

## Why using this project

 - it uses few memory
 - it has few dependencies
 - in serialization, it runs about 2 times faster than JAXB Sun implementation and GSon and only 10% slower than Jackson
 - in deserialization, it runs about the speed of JAXB Sun implementation.
 - it is tested
  
## How to install

Download and install the dependency.
```
    git clone https://github.com/gcolin/common.git
    cd common
    gradle install
```


Download and install the project.

```
    git clone https://github.com/gcolin/jsonbyte.git
    cd json
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