# Restito

This project is a partial jax rs 2.0 implementation. 

This project do not pass the TCK.

The project does not scan annotations without CDI.

## Why using this project

 - it uses few memory
 - it has few dependencies
 - it is customizable
 
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
