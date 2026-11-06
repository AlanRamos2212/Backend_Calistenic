package com.backend.modulo.repository;

import com.backend.modulo.entity.UserRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoutineRepository extends JpaRepository<UserRoutine, Long> {

    @Query("""
            select distinct r from UserRoutine r
            left join fetch r.exercises re
            left join fetch re.exercise
            where r.user.id = :userId
            order by r.createdAt desc
            """)
    List<UserRoutine> findAllForUser(@Param("userId") Long userId);

    @Query("""
            select r from UserRoutine r
            left join fetch r.exercises re
            left join fetch re.exercise
            where r.id = :id
            """)
    Optional<UserRoutine> findDetailById(@Param("id") Long id);
}
