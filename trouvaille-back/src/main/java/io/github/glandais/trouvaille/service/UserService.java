package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  final UserRepository userRepository;

  final SecurityIdentity securityIdentity;

  final JsonWebToken jsonWebToken;

  public UserEntity getCurrentUser() {
    if (securityIdentity.isAnonymous()) {
      throw new IllegalStateException("User is not authenticated");
    }

    String externalId = getAttribute("externalId");
    String username = getAttribute("username");
    String nickname = getAttribute("nickname");

    return getUserEntity(externalId, username, nickname);
  }

  public UserEntity getUserEntity(String externalId, String username, String nickname) {
    Optional<UserEntity> userOptional =
        userRepository.find("externalId", externalId).singleResultOptional();
    if (userOptional.isEmpty()) {
      UserEntity userEntity = new UserEntity(new ObjectId(), externalId, username, nickname);
      userRepository.persist(userEntity);
      return userEntity;
    } else {

      return userOptional.get();
    }
  }

  private String getAttribute(String attribute) {
    String username = jsonWebToken.getClaim(attribute);
    if (username == null) {
      throw new IllegalStateException(attribute + " not found in token");
    }
    return username;
  }

  public UserEntity getUser(String userId) {
    return userRepository.findById(new ObjectId(userId));
  }
}
