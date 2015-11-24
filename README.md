# Continuity

The goal of this library is to add an ability to pass some context throughout an application even between different threads (in asynchronous application).
It is implemented using a `ThreadLocal` variable and decoration of `Executor`/`ExecutionContext`. The implementation also copies the context values
into [MDC](logback.qos.ch/manual/mdc.html) so that you can see those values in your logging statements and can name threads according to some format.

The library consists of two modules. The `core` is the library itself, `netty` is an extension for the [Netty](http://netty.io) library.

It has both [Scala](core/src/main/scala/com/avast/continuity) and [Java](core/src/java/com/avast/continuity/javaapi) APIs.

Most of you won't need to work with this library as it should be integrated into other libraries ([Yap](https://git.int.avast.com/ff/yap), 
[HTTP Clients](https://git.int.avast.com/ff/clients)) for seamless cooperation. The only thing that is required and you might need to do
yourself is to wrap all your executors (`Executor`, `ExecutorService`, `ExecutionContext`, `EventLoopGroup`) in Continuity wrappers.

### Gradle
```groovy
dependencies {
    compile 'com.avast.continuity:continuity-core_?:1.0-SNAPSHOT'
}
```

```groovy
dependencies {
    compile 'com.avast.continuity:continuity-netty_?:1.0-SNAPSHOT'
}
```

### Maven
```xml
<dependency>
    <groupId>com.avast.continuity</groupId>
    <artifactId>continuity-core_${build.scala.version}</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

```xml
<dependency>
    <groupId>com.avast.continuity</groupId>
    <artifactId>continuity-netty_${build.scala.version}</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Logback
If you want to see a value from [MDC](logback.qos.ch/manual/mdc.html) in your logs you can the `%mdc` specifier.

This is a recommended format for the [Kluzo](https://git.int.avast.com/ff/kluzo) trace ID: 
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%-10mdc{traceId}] [%thread] %-35logger{35}: %msg \(%file:%line\)%n%xThrowable{full}</pattern>
```

## Thread Naming
One thing is to see the context values in your logs but there are other tools where this information might be beneficial but wouldn't be visible (VisualVM, ...).
Continuity allows you to set a [ThreadNamer](core/src/scala/com/avast/continuity/ThreadNamer.scala) which can change the name of the thread
according to your needs. It is [IdentityThreadNamer](core/src/scala/com/avast/continuity/IdentityThreadNamer.scala) by default but there is
[ContinuityContextThreadNamer](core/src/scala/com/avast/continuity/ContinuityContextThreadNamer.scala) which can be used to alter the thread name according 
to the provided format with keys from the context.

## Example
```scala
val executor = Continuity.wrapExecutionContext(ExecutionContext.global)

Continuity.withContext("myContext" -> "value") {
    executor.execute(new Runnable {
      override def run(): Unit = {
        println(Continuity.getFromContext("myContext"))
        logger.info("logging message")
      }
    })
}
```
