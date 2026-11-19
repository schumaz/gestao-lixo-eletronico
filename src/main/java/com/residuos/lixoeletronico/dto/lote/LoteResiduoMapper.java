package com.residuos.lixoeletronico.dto.lote;

import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.LoteResiduo;

public final class LoteResiduoMapper {

    private LoteResiduoMapper() {}

    public static LoteResiduo toEntity(
        LoteResiduoCreateRequest request,
        Empresa empresa
    ) {
        if (request == null) {
            return null;
        }

        return new LoteResiduo(
            empresa,
            trimOrNull(request.nomeUsuario()),
            trimOrNull(request.contatoUsuario()),
            request.dataRecepcao(),
            request.categoriaMaterial(),
            trimOrNull(request.tipoResiduo()),
            request.pesoKg(),
            request.urgenciaDescarte()
        );
    }

    private static String trimOrNull(String value) {
        return value != null ? value.trim() : null;
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
