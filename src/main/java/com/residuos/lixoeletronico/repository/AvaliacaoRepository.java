package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByEmpresaIdOrderByCriadoEmDescIdDesc(Long empresaId);

    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM Avaliacao a WHERE a.empresa.id = :empresaId")
    Double calcularMediaNotasPorEmpresa(@Param("empresaId") Long empresaId);

    long countByEmpresaId(Long empresaId);
}
