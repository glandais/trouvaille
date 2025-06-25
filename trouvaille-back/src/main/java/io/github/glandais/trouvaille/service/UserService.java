package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

    final UserRepository userRepository;

    final SecurityIdentity securityIdentity;

    final JsonWebToken jwt;

    private String getUserName() {
        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcPrincipal) {
            return oidcPrincipal.getClaim("preferred_username");
        }
        return jwt.getClaim("preferred_username");
    }

    public UserEntity getCurrentUser() {
        String username = getUserName();
        Optional<UserEntity> userOptional = userRepository.find("username", username).singleResultOptional();
        if (userOptional.isEmpty()) {
            UserEntity userEntity = new UserEntity(new ObjectId(), username);
            userRepository.persist(userEntity);
            return userEntity;
        } else {
            return userOptional.get();
        }
    }

    public UserEntity getUser(String username) {
        return userRepository.find("username", username).singleResultOptional().orElse(null);
    }
}
