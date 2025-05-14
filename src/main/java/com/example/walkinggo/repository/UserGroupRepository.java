package com.example.walkinggo.repository;

import com.example.walkinggo.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    Optional<UserGroup> findByParticipationCode(String participationCode);

    List<UserGroup> findByIsPublicTrueOrderByNameAsc();

    boolean existsByParticipationCode(String participationCode);

    @Query("SELECT CASE WHEN COUNT(ug) > 0 THEN true ELSE false END " +
            "FROM UserGroup ug JOIN ug.members m " +
            "WHERE ug.id = :groupId AND m.username = :username")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("username") String username);

    List<UserGroup> findByNameContainingIgnoreCaseAndIsPublicTrue(String name);
}