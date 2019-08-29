# Juikito

A partial CDI 1.2 implementation.

## Why using this project

* implements a lot of the CDI specification (missing EJB and Webservices integration)
* consumes less memory than the reference implementation
* creates few proxies (No proxy for ApplicationScoped and No proxy for a SessionScoped in a RequestScoped)
* runs fast
* can run months without server restart
* passes the atinject TCK
  
## How to install

Download and install the dependency.
```
    git clone https://github.com/gcolin/common.git
    cd common
    gradle install
```


Download and install the project.

```
    git clone https://github.com/gcolin/juikito.git
    cd juikito
    gradle install
```

## How to use

The cdi project contains a web-fragment.xml for enabling a **Listener**. If the web-fragment.xml is not activated, the web.xml must contains

```
    <listener>
		<listener-class>net.gcolin.di.cdi.Listener</listener-class>
	</listener>
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