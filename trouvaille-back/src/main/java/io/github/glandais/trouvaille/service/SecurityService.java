package io.github.glandais.trouvaille.service;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class SecurityService {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    public String getCurrentUserId() {
        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcPrincipal) {
            return oidcPrincipal.getClaim("sub");
        }
        return jwt.getSubject();
    }

    public String getCurrentUserName() {
        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcPrincipal) {
            return oidcPrincipal.getClaim("preferred_username");
        }
        return jwt.getClaim("preferred_username");
    }

    public String getCurrentUserEmail() {
        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcPrincipal) {
            return oidcPrincipal.getClaim("email");
        }
        return jwt.getClaim("email");
    }
}