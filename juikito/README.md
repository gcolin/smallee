# Juikito

Juikito is a light dependency injection library. It is very modular. Currently Juikito is an atinject implementation (without static) with some CDI features.

The main caracteristics of Juikito are
* No proxy (unless for some interceptors)
* Only the first bean found is selected
* Modular and customizable
* Nothing is static such as *CDI.instance();*

If you do a module, make pull request or an issue for adding to the project.

The CDI module is abandoned because it does not support Passivation. Instead the atinject implementation has been extended.

## Core module

This is a very basic dependency injection library.

## Atinject module

This is the Atinject implementation based on the core library.

### How dependency injection works

A lot of documentation about injection with Google Guice or Weld are present on Internet. This project use mostly the same syntax.

```java
@Qualifier
@Retention(RUNTIME)
public @interface Driver {}

public class Car {

  @Inject
  Seat seat;
  
  @Inject @Driver
  Seat seat;

}

public class Seat { }

@Driver
public class DriverSeat extends Seat { }

public void main(String[] args) {
  Environment env = new Environment();
  env.add(Car.class, Seat.class, DriverSeat.class);
  
  Car carWithInjedFields = env.get(Car.class);
}
```


### Mixing with other libraries

When Juikito is used along with another library, some conflicts may occurs. For this case, Juikito has a copy of the annotation *@Inject* in it owns package. Both *@Inject* annotations work. You can also add another inject annotation.

```java
env.getInjectAnnotations().add(MyInject.class);
```

The module *atinject-cdi-like* give a complete example how using other annotations with extensions. 

### The lack of proxy

The proxies are not important in the **atinject** specification but they are in the **CDI** specification. So when you want to use an object that needs a proxy, use a *javax.inject.Provider* instead.

```java
public class MyServlet extends BindHttpServlet {
  @Inject
  Provider<MyScopedSessionBean> mybeanProvider;
   
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    MyScopedSessionBean mybean = mybeanProvider.get();
    ...
  }
}
```

When you use an interceptor @AroundInvoke, a proxy must be created. So you must get your bean by one of its interface.

```java
public class MyBean implements MyBeanIntf { ... }
```

```java
@Inject
MyBeanIntf mybean;
```

### The alternative

There are no alternative in Juikito but you can set a *@Priority* annotation in your bean class declaration. The lower priority is chosen.

### The Extensions

An extension system is available with *Extensions*. Each extension has 4 life cycle phases:
  - *doStart*: add bean classes, add scopes, add builders, add custom annotations
  - *doStarted*: create some beans, enable bean features (observers, message driven, producers)
  - *doStop*: called before the beans destruction
  - *doStopped*: called just before the environment shutdown
  
The extensions also support the *@Priority* annotation and the method *priority*. The lower priority is executed first.

### Java 8 Optional

The *java.util.Optional<T>* of Java 8 is supported. The use case of Optional is similar to CDI Instance with more limitations.

### Java 8 Supplier

The *java.util.Supplier<T>* of Java 8 is supported. It is interpreted as an *javax.inject.Provider<T>*.

### Auto discover beans

By default, beans are not discovered. By setting *setSealed(false)*, the environment will create beans that are not registered if the bean class is not abstract and is not an interface.

## Atinject Producer

Enable field and method producer like CDI implementation. The annotations @Produces and @Disposes are duplicated in the Juikito package.

For some example, you can look at CDI example with producers.

## Atinject Event

Enable event system like CDI Event/Observes. The Event class of Juikito is limited and cannot select subevents.

The usage is similar to CDI.

### Send an event

```java
@Inject
Event<Message> messageEvent;

...

messageEvent.fire(new Message("Hello"));

```

### Receive an event

```java
public void listenMessage(@Observes Message message) { ... }
```

### Receive an event asynchronously (on the default queue)

```java
public void listenMessage(@Observes @Async Message message) { ... }
```

### Receive an event asynchronously on a specific queue

```java
public void listenMessage(@Observes @Async("myQueue") Message message) { ... }
```

### Receive an event asynchronously on a specific queue with a simultaneous size of 3

In this case 3 events can be listened a once.

```java
public void listenMessage(@Observes @Async(value = "myQueue", size = 3) Message message) { ... }
```

## Atinject Interceptor

Enable the *javax.interceptor* API with Juikito. The annotations *@AroundConstruct* and @AroundInvoke are supported. The life cycle of the interceptor instances is the same of the intercepted bean.

## Atinject Loader

The module load classes with a file named *META-INF/atinject* which contains class names to add to the environment. One class name per line without space.

If you want annotation scanning like CDI, you need to do your own loader. 

## Atinject Web

Enable request, session and application scopes with the annotations *@RequestScoped*, *@SessionScoped* and *@ApplicationScoped*. This module must be used in a Servlet application.

### Configure web.xml

In your web.xml, add

```xml
<listener>
    <listener-class>net.gcolin.di.atinject.web.DiListener</listener-class>
</listener>

<filter>
    <filter-name>di</filter-name>
    <filter-class>net.gcolin.di.atinject.web.DiFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>di</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

For Tomcat and maybe some other Java Servers,

```xml
<jsp-config>
    <jsp-property-group>
        <url-pattern>*.jsp</url-pattern>
        <el-ignored>false</el-ignored>
    </jsp-property-group>
</jsp-config>
```

It is advisable that these scopes contains only *Serializable* classes. And the session bean should contain as few as possible information. The serialization is done by the Java Server so it is possible that non *Serializable* session bean works.

### Inject into a Servlet

A Servlet with injection fields must extends:
* *net.gcolin.di.atinject.web.BindServlet* for a *javax.servlet.Servlet*
* *net.gcolin.di.atinject.web.BindGenericServlet* for a *javax.servlet.GenericServlet*
* *net.gcolin.di.atinject.web.BindHttpServlet* for a *javax.servlet.http.HttpServlet*

### Inject into a Filter

A Filter with injection fields must extends *net.gcolin.di.atinject.web.BindFilter*.

### get the Environment anywhere

The Environment can be retrieved from the *ServletContext*.

```java
Environment env = (Environment) servletContext.getAttribute("dienv");
```
For accessing in an application, do a Listener declared after the *DiListener* in the *web.xml*.

```java
public class MyListener implements ServletContextListener {

  public static Environment env;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
     env = (Environment) sce.getServletContext().getAttribute("dienv");
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
     env = null;
  }

}
```

## Atinject El

This module can create an ElResolver that get beans by name (*@Named*).

## Atinject JSP

This module bind the Juikito ELResolver to the JspApplicationContext. There is nothing to configure.

## Atinject MessageDriven

The *@MessageDriven* annotated classes will be created like an EJB messagedriven.

## Atinject Jmx

This module make some bean properties and methods accessible through Jmx with the annotation *@Jmx*.

For example, the bean A make the attribute *val* and the operation *square* accessible with the name **my.example:type=A**.

```java
package my.example;

@Singleton
public static class A {

  @Jmx
  int val;
    
  @Jmx
  int square(int val) {
    return val * val;
  }

}
```

It is possible to override the name of an attribute or an operation.

```java
package my.example;

@Singleton
public static class A {

  @Jmx("value")
  int val;
    
  @Jmx("calculate")
  int square(int val) {
    return val * val;
  }

}
```

It is also possible to add a description.

```java
package my.example;

@Singleton
public static class A {

  @Jmx(value = "value", description = "an attribute")
  int val;
    
  @Jmx(value = "calculate", description = "an operation")
  int square(int val) {
    return val * val;
  }

}
```

It is possible to register/unregister an instance

```java
JmxExtension jmx = environment.get(JmxExtension.class);
jmx.add(myinstance);
jmx.remove(myinstance);
```

The annotation *@JmxAttribute* can add some extra JMX path attribute but it is usable only with an instance registration.

```java
package my.example;

@Singleton
public static class A {

  @Jmx
  int val;
    
  @JmxAttribute
  String name;

}

A a = new A();
a.name = "a1";
jmx.add(a);  //my.example:type=A,name=a1
```


## Atinject Rest

This module allows the use of Restito (one of my Github project that implements Javax RS 2.0) by detecting *@ApplicationPath*. For more information about *@ApplicationPath*, look at the general documentation about **javax.ws.rs**.

For other rest implementation, copy this module and modify it to make another implementation works.

## Atinject CDI like

This module allows the use of some CDI annotations. Nevertheless with this module the bean produced are not compatible with the CDI specification.

The supported features:
  - @RequestScoped, @SessionScoped, @ApplicationScoped
  - @Produces, @Disposes
  - @AroundInvoke, @AroundConstruct
  - Event, @Observes