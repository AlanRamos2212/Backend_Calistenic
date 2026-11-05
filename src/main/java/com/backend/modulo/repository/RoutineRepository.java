package com.backend.modulo.repository;

import com.backend.modulo.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    @Query("""
            select distinct r from Routine r
            left join fetch r.exercises re
            left join fetch re.exercise
            where r.category = :category
            order by r.createdAt desc
            """)
    List<Routine> findByCategory(@Param("category") String category);

    @Query("""
            select distinct r from Routine r
            left join fetch r.exercises re
            left join fetch re.exercise
            order by r.createdAt desc
            """)
    List<Routine> findAllWithExercises();

    @Query("""
            select r from Routine r
            left join fetch r.exercises re
            left join fetch re.exercise
            where r.id = :id
            """)
    Optional<Routine> findDetailById(@Param("id") Long id);
}
