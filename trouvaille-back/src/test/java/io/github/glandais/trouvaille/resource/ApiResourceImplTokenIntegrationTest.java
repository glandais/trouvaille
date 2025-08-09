package io.github.glandais.trouvaille.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.glandais.trouvaille.api.model.OAuthTokenRequest;
import io.github.glandais.trouvaille.api.model.OAuthTokenResponse;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ApiResourceImplTokenIntegrationTest {

  private WireMockServer wireMockServer;

  @Inject UserRepository userRepository;

  @ConfigProperty(name = "trouvaille.oauth.client-id")
  String clientId;

  @ConfigProperty(name = "trouvaille.oauth.client-secret")
  String clientSecret;

  private static final String REDIRECT_URI = "http://localhost:8080/callback";
  private static final String AUTH_CODE = "test-auth-code";
  private static final String ACCESS_TOKEN = "test-access-token";
  private static final String USER_ID = "mattermost-user-id";
  private static final String USERNAME = "testuser";
  private static final String NICKNAME = "Test User";

  private void setupSuccessfulOAuthFlow() {
    setupTokenExchangeSuccess();
    setupGetUserSuccess();
  }

  private void setupTokenExchangeSuccess() {
    wireMockServer.stubFor(
        post(urlEqualTo("/oauth/access_token"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "{"
                                + "\"access_token\":\"%s\","
                                + "\"token_type\":\"Bearer\","
                                + "\"expires_in\":3600,"
                                + "\"refresh_token\":\"refresh-token\","
                                + "\"scope\":\"read\""
                                + "}",
                            ACCESS_TOKEN))));
  }

  private void setupGetUserSuccess() {
    wireMockServer.stubFor(
        get(urlEqualTo("/api/v4/users/me"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "{"
                                + "\"id\":\"%s\","
                                + "\"username\":\"%s\","
                                + "\"nickname\":\"%s\","
                                + "\"email\":\"test@example.com\""
                                + "}",
                            USER_ID, USERNAME, NICKNAME))));
  }

  @BeforeEach
  void setUp() {
    // Start WireMock server on port 8089 to match test profile
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
    wireMockServer.start();

    userRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
    userRepository.deleteAll();
  }

  @Test
  void testExchangeOAuthToken_Success() {
    // Given
    setupSuccessfulOAuthFlow();

    OAuthTokenRequest request = new OAuthTokenRequest();
    request.setCode(AUTH_CODE);
    request.setRedirectUri(REDIRECT_URI);
    request.setState("state");

    // When

    OAuthTokenResponse response =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/auth/token")
            .then()
            .statusCode(200)
            .extract()
            .as(OAuthTokenResponse.class);

    // Then
    assertNotNull(response);
    assertNotNull(response.getAccessToken());

    // Verify user was created in database
    UserEntity user = userRepository.find("externalId", USER_ID).singleResult();
    assertNotNull(user);
    assertEquals(USER_ID, user.getExternalId());
    assertEquals(USERNAME, user.getUsername());
    assertEquals(NICKNAME, user.getNickname());
    assertEquals(false, user.getAdmin());

    // Verify WireMock calls
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/oauth/access_token"))
            .withFormParam("grant_type", equalTo("authorization_code"))
            .withFormParam("client_id", equalTo(clientId))
            .withFormParam("client_secret", equalTo(clientSecret))
            .withFormParam("redirect_uri", equalTo(REDIRECT_URI))
            .withFormParam("code", equalTo(AUTH_CODE)));

    wireMockServer.verify(
        getRequestedFor(urlEqualTo("/api/v4/users/me"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN)));
  }
}
