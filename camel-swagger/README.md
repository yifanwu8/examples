# Camel Swagger REST on embedded Jetty with SwaggerUI

Some simple REST endpoints to manage Camel routes are hosted and defined by Camel-Swagger on a embedded Jetty.
SwaggerUI is also included provided by webjars.org and served as Resource Handler in Jetty.

#### How to run
using gradle wrapper:
./gradlew :camel-swagger:run
<br>
Point your browser to http://localhost:35555/swagger-ui/${version.number}/. version=3.10.0 at the writing.
Swagger document is generated at http://localhost:35555/api-docs/rest/swagger.yaml