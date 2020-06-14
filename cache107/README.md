# Cache107

This project is a cache api 1.0 TCK compliant implementation for :

 - Specific Implementation
 - CDI Annotations
 
But the project not implements :

 - Spring Annotations
 - Guice Annotations

The goal of this implementation is to be used in lightweight environment.

This implementation use only on thread for asynchronous operation. The eviction is done very fast when calling a method of the cache.

## Why using this project

 - it uses few memory
 - it has few dependencies
 - it runs about the speed of Ehcache and it is faster than many implementations
 - it adds some extras features
    * the property maxSize for limited the cache size
    * the configuration can be read from an XML file
    * the cache can be written to a file on the fly
  
## How to install


```
    git clone https://github.com/gcolin/smallee.git
    cd smallee
    mvn install
```

## How to be sure that it is TCK compliant

Download the TCK at [github](https://github.com/jsr107/jsr107tck) 
    
     git clone https://github.com/jsr107/jsr107tck
     cd jsr107tck
     git reset --hard v1.0.1-release
     
Modify the pom.xml at the *RI Properties* part with this code

    <!--################################################################################################################-->
    <!--IPV4-->
    <java.net.preferIPv4Stack>true</java.net.preferIPv4Stack>
    <!--Change the following properties on the command line to override with the coordinates for your implementation-->
    <implementation-groupId>net.gcolin.smallee</implementation-groupId>
    <implementation-artifactId>cache107</implementation-artifactId>
    <implementation-version>1.2</implementation-version>
    <!-- Change the following properties to your CacheManager and Cache implementation. Used by the unwrap tests. -->
    <CacheManagerImpl>net.gcolin.cache.CacheManagerImpl</CacheManagerImpl>
    <CacheImpl>net.gcolin.cache.CacheImpl</CacheImpl>
    <CacheEntryImpl>net.gcolin.cache.EntryImpl</CacheEntryImpl>
    <!--Change the following to point to your MBeanServer, so that the TCK can resolve it. -->
    <javax.management.builder.initial>net.gcolin.cache.tck.TckMbeanServerBuilder
    </javax.management.builder.initial>
    <org.jsr107.tck.management.agentId>RIMBeanServer</org.jsr107.tck.management.agentId>
    <!--################################################################################################################-->
    
Modify the cdi-weld-annotations-test-harness/pom.xml

replace

        <dependency>
            <groupId>org.jsr107.ri</groupId>
            <artifactId>cache-annotations-ri-cdi</artifactId>
            <version>${implementation-version}</version>
        </dependency>

by

        <dependency>
            <groupId>net.gcolin.smallee</groupId>
            <artifactId>cache107-cdi-integration</artifactId>
            <version>1.2</version>
        </dependency>
    
Then run maven in the same folder

    mvn clean install
    
You will have

    [INFO] ------------------------------------------------------------------------
    [INFO] Reactor Summary:
    [INFO] 
    [INFO] TCK Parent ......................................... SUCCESS [  2.719 s]
    [INFO] App Domain ......................................... SUCCESS [  4.422 s]
    [INFO] Test Domain ........................................ SUCCESS [  3.529 s]
    [INFO] Cache Tests ........................................ SUCCESS [  5.077 s]
    [INFO] Implementation Tester .............................. SUCCESS [  0.074 s]
    [INFO] Implementation Tester - Specific Implementation .... SUCCESS [ 29.832 s]
    [INFO] CDI/Weld Annotations Test Harness .................. SUCCESS [  2.312 s]
    [INFO] Implementation Tester - CDI Annotations ............ SUCCESS [  2.730 s]
    [INFO] Spring Annotations Test Harness .................... FAILURE [  0.626 s]
    [INFO] Implementation Tester - Spring Annotations ......... SKIPPED
    [INFO] Guice Annotations Test Harness ..................... SKIPPED
    [INFO] Implementation Tester - Guice Annotations .......... SKIPPED
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD FAILURE
    [INFO] ------------------------------------------------------------------------
    
The two first test pass : 

 - Specific Implementation
 - CDI Annotations
 
## Extended features

### MaxSize

The maxSize property set the maximum size of the cache. This feature is not in the specification. So you need to add it to the properties at the cache creation. By default, the cache has no size limit.

### CacheFile 

A file to extend the cache. The cache is in memory. 

Sometimes it is better to store in a file when the memory cache is big. 

    fileCache.asIdle(config);

And sometimes it is good to not loose the cache data when the application crash. There are the two main goals of the *CacheFile*.

    fileCache.asMirror(config);
    
### XML Configuration file

The URI of the cache manager can be an XML configuration file. The XML configuration permits to configure caches with a lots of options.

For example, for a project with *config.xml* located in the root of the class path. The cache can be retrieved like this :

    Cache<String, String> c = Caching.getCachingProvider()
    .getCacheManager(new URI("config.xml"),this.getClass().getClassLoader()).getCache("hello", String.class, String.class);

The content of the file *config.xml*

    <caches>
        <cache>
            <name>hello</name>
            <maxSizeMemory>1000</maxSizeMemory>
            <keyType>java.lang.String</keyType>
            <valueType>java.lang.String</valueType>
            <expiryCreate>1800000</expiryCreate>
            <expiryAccess>1800000</expiryAccess>
            <expiryUpdate>1800000</expiryUpdate>
            <statistics>true</statistics>
            <management>true</management>
        </cache>
    </caches>
    
The available values :

| Name | Default value | Description |
| ---- | ------------- | ----------- |
| name |  | The name of the cache |
| maxSizeMemory | -1 | The maximum elements size of the cache in memory. -1 means no limit |
| maxSizeDisk   | -2 | The maximum elements size of the cache in disk (CacheFile). -1 means no limit. -2 means no disk store |
| statistics   | false | Enable statistics. Access via JMX |
| management   | false | Enable management. Access via JMX |
| expiryCreate   | Long.MAX_VALUE | The maximum time in ms after the creation |
| expiryAccess   | Long.MAX_VALUE | The maximum time in ms after an access |
| expiryUpdate   | Long.MAX_VALUE | The maximum time in ms after an update |
| expiryIdle   | Long.MAX_VALUE | The maximum time after an idle |
| byValue   | false | Serialize values to keep them unmodifiable out of the cache |
| persistent   | false | Copy the live cache to disk. If the application is restarted, the cache keeps data. |
| keyType   |  | The type of key. Example, *java.lang.String* |
| valueType   | | The type of key. Example, *java.lang.String* |
| dir   | null | The directory to write the cacheFile if needed |
| fileName   | null | The file name to write the cacheFile if needed |
