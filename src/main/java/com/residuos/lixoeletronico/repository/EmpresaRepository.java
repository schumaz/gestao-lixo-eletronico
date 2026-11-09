package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.model.Empresa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"horarios", "materiaisAceitos"})
    Optional<Empresa> findPerfilById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Empresa e SET e.totalKgDescartado = e.totalKgDescartado + :kg WHERE e.id = :id")
    int adicionarKgDescartado(@Param("id") Long id, @Param("kg") BigDecimal kg);
}
