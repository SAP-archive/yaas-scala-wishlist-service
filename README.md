# Scala Wishlist
This is a wishlist service that implements the best practices for creating a Scala-based YaaS service. The service allows the user to create wishlists, which can be viewed and managed. The output can be paginated via query parameters.

The implementation is based on the Scala [Play Framework](https://github.com/playframework/playframework) and uses [WireMock](http://wiremock.org/) and [ScalaTest](https://github.com/scalatest/scalatest) to ensure functionality.

# How To Run
You need to install the Play Framework first using the Lightbend Activator as described here: [Installing Play](https://www.playframework.com/documentation/2.5.x/Installing).

To access Yaas services, you need to setup a client using the [Builder](https://builder.yaas.io) first. The client needs to be subscribed to the Persistence package with required scopes ```hybris.document_view``` and ```hybris.document_manage```.  With this information, you can then either configure the application in ```application.conf```, providing values for ```yaas.security.client_id```,  ```yaas.security.client_secret``` and ```yaas.client```. Or alternatively, you can pass the configuration as environment variables ```$CLIENT_ID```, ```$CLIENT_SECRET``` and ```$YAAS_CLIENT``` before running the application.

To start the service locally, call ```activator run```.

# API Console
The implementation provides an endpoint for the [API Console](https://github.com/mulesoft/api-console) by Mulesoft, to which you are redirected when you access the root path. To try it locally, use [http://localhost:9000](http://localhost:9000).

# RAML File
The API's RAML file is exposed under the context path ```meta-data/api.raml```. To access it locally, use [http://localhost:9000/meta-data/api.raml](http://localhost:9000/meta-data/api.raml). The default redirect loads the RAML file into the API Console.

# Incorporated Technology
- SBT (build system, see http://www.scala-sbt.org/)
- Play Framework (RESTful web framework, see https://www.playframework.com/)
- WireMock (Mock testing, see http://wiremock.org/)
- ScalaTest (Unit testing, see http://www.scalatest.org/)
- Akka Circuit Breaker (Circuit Breaker provided by Akka. See http://doc.akka.io/docs/akka/snapshot/common/circuitbreaker.html)
- API Console (See https://github.com/mulesoft/api-console)

# Topics Demonstrated
- Exposure of api.raml
- Exposure of api-console
- OAuth2 integration with token caching
- Circuit Breaker for client call resilience
- Basic Auth for server-side calls
- Error response mapping
- Logging
- Client calls to the Document and the OAuth2 service using Play's WSRequest
- Paging of results from the Document service
- Scope validation via Play's ActionFunctions
