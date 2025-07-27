package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.Pagination;
import io.github.glandais.trouvaille.api.model.Users;
import io.github.glandais.trouvaille.api.model.Utilisateur;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
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
      UserEntity userEntity = new UserEntity(new ObjectId(), externalId, username, nickname, false);
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

  public Users listUsers(Integer page, Integer limit, String search) {
    Page pageRequest = Page.of(page - 1, limit);

    List<UserEntity> userEntities;
    long totalCount;

    if (search != null && !search.trim().isEmpty()) {
      String searchTerm = search.trim();
      String query =
          "{$or: [{'username': {$regex: ?1, $options: 'i'}}, {'nickname': {$regex: ?1, $options:"
              + " 'i'}}]}";
      userEntities = userRepository.find(query, searchTerm).page(pageRequest).list();
      totalCount = userRepository.count(query, searchTerm);
    } else {
      userEntities = userRepository.findAll().page(pageRequest).list();
      totalCount = userRepository.count();
    }

    List<Utilisateur> users = userEntities.stream().map(this::mapToUtilisateur).toList();

    Pagination pagination = new Pagination();
    pagination.setPageCourante(page);
    pagination.setElementsParPage(limit);
    pagination.setTotalElements((int) totalCount);
    pagination.setTotalPages((int) Math.ceil((double) totalCount / limit));

    Users result = new Users();
    result.setData(users);
    result.setPagination(pagination);

    return result;
  }

  public Utilisateur updateUserAdmin(String userId, boolean admin) {
    UserEntity userEntity = userRepository.findById(new ObjectId(userId));
    if (userEntity == null) {
      throw new IllegalArgumentException("User not found: " + userId);
    }

    userEntity.admin = admin;
    userRepository.update(userEntity);

    return mapToUtilisateur(userEntity);
  }

  private Utilisateur mapToUtilisateur(UserEntity userEntity) {
    Utilisateur user = new Utilisateur();
    user.setId(userEntity.id.toString());
    user.setUsername(userEntity.username);
    user.setNickname(userEntity.nickname);
    user.setAdmin(Boolean.TRUE.equals(userEntity.admin));
    return user;
  }
}
