# Continuity

The goal of this library is to add an ability to pass some context throughout an application even between different threads (in asynchronous application).
It is implemented using a `ThreadLocal` variable and decoration of `Executor`/`ExecutionContext`. The implementation also copies the context values
into [MDC](logback.qos.ch/manual/mdc.html) so that you can see those values in your logging statements (e.g. traceId).

### Gradle
```groovy
dependencies {
    compile 'com.avast:continuity_?:1.0-SNAPSHOT'
}
```

### Maven
```xml
<dependency>
    <groupId>com.avast</groupId>
    <artifactId>continuity_${build.scala.version}</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Example

```scala
val executor = Continuity.wrapExecutor(ExecutionContext.global)

Continuity.putToContext("myContext", "value")

executor.execute(new Runnable {
  override def run(): Unit = {
    println(Continuity.getFromContext("myContext"))
    logger.info("logging message")
  }
})
```
