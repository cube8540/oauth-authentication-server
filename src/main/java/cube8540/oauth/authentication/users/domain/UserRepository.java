package cube8540.oauth.authentication.users.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UserEmail> {

    @EntityGraph(attributePaths = "authorities", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findByEmail(UserEmail userEmail);

    Long countByEmail(UserEmail userEmail);
}
