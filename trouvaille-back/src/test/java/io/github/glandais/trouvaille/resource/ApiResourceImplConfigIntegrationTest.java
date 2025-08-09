package io.github.glandais.trouvaille.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ApiResourceImplConfigIntegrationTest {

  @Test
  void testConfig_Success() {
    given()
        .when()
        .get("/api/v1/config")
        .then()
        .statusCode(200)
        .body("authorizeUri", equalTo("https://auth"))
        .body("clientId", equalTo("client-id"));
  }
}
