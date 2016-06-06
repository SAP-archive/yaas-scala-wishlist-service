# Scala Wishlist
This is a wishlist service, implementing best practices on how to create a scala based YaaS service. The service allows for the user to create wishlists. The wishlists can be viewed and managed. The output can be paginated view query parameters.

The implementation is based on the scala [Play Framework](https://github.com/playframework/playframework) and is using [WireMock](http://wiremock.org/) & [ScalaTest](https://github.com/scalatest/scalatest) to ensure functionality.

# API Console
The implementation is providing an endpoint for the [API Console](https://github.com/mulesoft/api-console) by Mulesoft to which you get redirected to by accessing the root path [protocol]://[endpoint]:[port]/

# RAML File
The api's RAML file is exposed under [protocol]://[endpoint]:[port]/meta-data/api.raml. The default redirect of the root node will load the RAML file into the API Console.

# How To Run
Just call ```activator run``` to start it locally. In the ```application.conf``` please configure your ```yaas.security.client_id``` and ```yaas.security.client_secret``` or pass them as environment variables ```$CLIENT_ID``` and ```$CLIENT_SECRET``` and they'll be used in the application.

# Used Technology
- SBT (build system)
- Play Framework (RESTful web framework)
- WireMock (Mock testing)
- ScalaTest (Unit testing)
- Akka Circuit Breaker (Play Frameworks provided circuit breaker)

# Topics Demonstrated
- Exposure of api.raml
- Exposure of api-console
- OAuth2 integration with Caching of tokens
- Circuit Breaker for resilience of client calls
- Basic Auth for server side calls
- error response mapping
- logging
- client calls to document service (and oauth2) using Play's WSRequest
- paging of results from document service
- scope validation via Play's ActionFunction's
