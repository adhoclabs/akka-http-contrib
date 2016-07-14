# akka-http-contrib

## Request throttling

`throttle` directive is a trait with throttle method that take implicit settings:

```
trait ThrottleDirective {
  def throttle(implicit settings: ThrottleSettings)
}
```

See [ThrottleDirective.scala](https://github.com/adhoclabs/akka-http-contrib/blob/master/src/main/scala/co/adhoclabs/akka/http/contrib/throttle/ThrottleDirective.scala), where settings is:

```
trait ThrottleSettings {
  def shouldThrottle(request: HttpRequest): Future[Boolean]
  def onExecute(request: HttpRequest): Future[Unit]
}
```

see [package.scala](https://github.com/adhoclabs/akka-http-contrib/blob/master/src/main/scala/co/adhoclabs/akka/http/contrib/throttle/package.scala)

This would be the low level api. Throttle directive delegates the decision making to settings, weather a route should be executed or not. Which in it's turn takes the http request and returns Future\[Boolean\]. And when route is executed it calls `onExecute` callback to let the implementation know that it was executed \(to increment a counter for example\). This model should be very extensible since decision maker is plugable and it will get the entire http request and return a boolean.

On top of this model we created the high level api that allows us to add throttling by adding it to the config file. Let's start with the configuration:

```
akka.http.contrib {
  throttle {
    enabled = true
    endpoints = [
      {
        method = "POST"
        pattern = "[REGEX MATCHING THE REQUEST URL]"
        window = 2 m
        allowed-calls = 2
        throttle-period = 10 m
      }
    ]
  }
}
```

All the configuration is under `akka.http.contrib.throttle` namespace. `enabled` is a convenience flag that allows us to disable throttling without removing all the endpoint configurations. To introduce throttling of an endpoint we add a new config in `endpoints`. It has the http `method`, `pattern` which is a regex matching the url \(open to a PR to have play style routes support instead of regex\), `allowed-calls` in a given `window` of time and optional `throttle-period` that prevents the endpoint form being called for specified period of time if `allowed-calls` was reached in a given `window`. Essentially it's a way to increase throttling time if needed. Also there is configuration for storage where the counters will be stored. We use redis but other storages can easily be implemented and wired in config file with `default-store`.

Once we have the configuration all we need to do is to bring implicit throttle settings into the scope and wrap our routes in `throttle` directive:

```
implicit val throttleSettings = MetricThrottleSettings.fromConfig

Http().bindAndHandle(
  throttle.apply(routes),
  httpInterface,
  httpPort
)
```
