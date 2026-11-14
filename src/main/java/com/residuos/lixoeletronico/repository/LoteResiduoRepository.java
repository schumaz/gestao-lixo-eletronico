package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteResiduoRepository extends JpaRepository<LoteResiduo, Long> {

    List<LoteResiduo> findByEmpresaIdOrderByDataRecepcaoDescIdDesc(Long empresaId);

    List<LoteResiduo> findByEmpresaIdAndStatusOrderByDataRecepcaoDescIdDesc(Long empresaId, StatusLote status);

    List<LoteResiduo> findByStatusOrderByDataRecepcaoDescIdDesc(StatusLote status);

    Optional<LoteResiduo> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT COALESCE(SUM(l.pesoKg), 0) FROM LoteResiduo l WHERE l.empresa.id = :empresaId AND l.status = :status")
    BigDecimal somarPesoKgPorEmpresaEStatus(@Param("empresaId") Long empresaId, @Param("status") StatusLote status);
}
