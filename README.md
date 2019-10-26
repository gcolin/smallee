# Smallee

* A dependency injection (JSR-330) with optional modules
* A rest 2.0 implementation (JSR-339 not fully compliant) with modules
* A validation implementation 1.0 (not fully compliant)


Works with Java 11.

## Setup

Install locally

```
mvn clean install
```



Create the maven site in target/staging

```
mvn clean org.jacoco:jacoco-maven-plugin:0.8.4:prepare-agent install site:site site:stage
```

## Atinject modules

  * **atinject-cdi-like** CDI annotations support
  * **atinject-config** Inject configuration from config.properties
  * **atinject-el** Expression Language support
  * **atinject-event** CDI like Event
  * **atinject-injector** javax.interceptor annotations support (experimental)
  * **atinject-jmx** JMX annotation to expose fields/methods
  * **atinject-jndi** JNDI Resource annotation support
  * **atinject-jpa** JPA PersistenceContext/Transactional support (experimental)
  * **atinject-jsp** JSP bean injection
  * **atinject-loader** Load beans from META-INF/atinject
  * **atinject-producer** CDI like Producer 
  * **atinject-web** CDI like ApplicationScoped/SessionScoped/RequestScoped + Servlet beans injections
  
## Rest modules

* **rest-ext-atinject** Load @ApplicationPath with atinject
* **rest-ext-cdi** Load @ApplicationPath a CDI implementation
* **rest-ext-datasource** DataSource provider
* **rest-ext-freemarker** Freemarker view
* **rest-ext-gzip** Gzip input or outpout
* **rest-ext-jaxb** Jaxb provider
* **rest-ext-json** Json provider
* **rest-ext-jsp** Jsp view
* **rest-ext-mustache** Mustache view
* **rest-ext-validation** Parameter validation support
* **rest-ext-xml** Dom/Sax/Stax providers
