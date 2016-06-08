# Scala Wishlist
This is a wishlist service that implements the best practices for creating a Scala-based YaaS service. The service allows the user to create wishlists, which can be viewed and managed. The output can be paginated via query parameters.

The implementation is based on the Scala [Play Framework](https://github.com/playframework/playframework) and uses [WireMock](http://wiremock.org/) and [ScalaTest](https://github.com/scalatest/scalatest) to ensure functionality.

# API Console
The implementation provides an endpoint for the [API Console](https://github.com/mulesoft/api-console) by Mulesoft, to which you are redirected when you access the root path [protocol]://[endpoint]:[port]/

# RAML File
The API's RAML file is exposed under [protocol]://[endpoint]:[port]/meta-data/api.raml. The default redirect of the root node loads the RAML file into the API Console.

# How To Run
Call ```activator run``` to start the service locally. In the ```application.conf``` configure your ```yaas.security.client_id``` and ```yaas.security.client_secret``` or pass them as environment variables ```$CLIENT_ID``` and ```$CLIENT_SECRET```, for use in the application.

# Used Technology
- SBT (build system)
- Play Framework (RESTful web framework)
- WireMock (Mock testing)
- ScalaTest (Unit testing)
- Akka Circuit Breaker (Circuit Breaker provided by the Play Framework)

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
