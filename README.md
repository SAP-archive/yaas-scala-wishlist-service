# Scala Wishlist
This is a wishlist service that implements the best practices for creating a Scala-based YaaS service. The service allows the user to create wishlists, which can be viewed and managed. The output can be paginated via query parameters.

The implementation is based on the Scala [Play Framework](https://github.com/playframework/playframework) and uses [WireMock](http://wiremock.org/) and [ScalaTest](https://github.com/scalatest/scalatest) to ensure functionality.

# How To Run
You need to install the Play first using the Lightbend Activator as described here: [Installing Play](https://www.playframework.com/documentation/2.5.x/Installing). To access Yaas services, you need to setup a client using the builder first.  With this information, you can either configure the application either in ```application.conf``` providing ```yaas.security.client_id```,  ```yaas.security.client_secret``` and ```yaas.client```. Alternatively, you can pass them as environment variables ```$CLIENT_ID``` and ```$CLIENT_SECRET``` and ```$YAAS_CLIENT```.

To start the service locally, you call ```activator run```.

# API Console
The implementation provides an endpoint for the [API Console](https://github.com/mulesoft/api-console) by Mulesoft, to which you are redirected when you access the root path, e.g. to try it locally you can use: http://localhost:9000

# RAML File
The API's RAML file is exposed under the context path ```meta-data/api.raml```. To access it locally, you use http://localhost:9000/meta-data/api.raml. The default redirect of the root node loads the RAML file into the API Console.

# Incorporated Technology
- SBT (build system, see http://www.scala-sbt.org/)
- Play Framework (RESTful web framework, see https://www.playframework.com/)
- WireMock (Mock testing, see http://wiremock.org/)
- ScalaTest (Unit testing, see http://www.scalatest.org/)
- Akka Circuit Breaker (Circuit Breaker provided by the Play Framework, see http://doc.akka.io/docs/akka/snapshot/common/circuitbreaker.html)

# Topics Demonstrated
- Exposure of api.raml
- Exposure of api-console
- OAuth2 integration with token caching
- Circuit Breaker for client call resilience
- Basic Auth for server-side calls
- Error response mapping
- Logging
- Client calls to the Document service (and OAuth2) using Play's WSRequest
- Paging of results from the Document service
- Scope validation via Play's ActionFunctions
