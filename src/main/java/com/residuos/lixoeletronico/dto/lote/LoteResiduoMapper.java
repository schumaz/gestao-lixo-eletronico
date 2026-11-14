package com.residuos.lixoeletronico.dto.lote;

import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.LoteResiduo;

public final class LoteResiduoMapper {

    private LoteResiduoMapper() {
    }

    public static LoteResiduo toEntity(LoteResiduoCreateRequest request, Empresa empresa) {
        if (request == null) {
            return null;
        }

        return new LoteResiduo(
                empresa,
                request.nomeUsuario(),
                request.contatoUsuario(),
                request.dataRecepcao(),
                request.categoriaMaterial(),
                request.tipoResiduo(),
                request.pesoKg(),
                request.urgenciaDescarte()
        );
    }

    public static LoteResiduoResponse toResponse(LoteResiduo lote) {
        if (lote == null) {
            return null;
        }

        return new LoteResiduoResponse(
                lote.getId(),
                lote.getEmpresa() != null ? lote.getEmpresa().getId() : null,
                lote.getNomeUsuario(),
                lote.getContatoUsuario(),
                lote.getDataRecepcao(),
                lote.getCategoriaMaterial(),
                lote.getTipoResiduo(),
                lote.getPesoKg(),
                lote.getUrgenciaDescarte(),
                lote.getStatus(),
                lote.getCriadoEm(),
                lote.getAtualizadoEm()
        );
    }
}
