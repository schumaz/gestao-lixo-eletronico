package com.residuos.lixoeletronico.dto.avaliacao;

import com.residuos.lixoeletronico.model.Avaliacao;
import com.residuos.lixoeletronico.model.Empresa;

public final class AvaliacaoMapper {

    private AvaliacaoMapper() {
    }

    public static Avaliacao toEntity(AvaliacaoCreateRequest request, Empresa empresa) {
        if (request == null) {
            return null;
        }

        String comentarioLimpo = request.comentario() != null && !request.comentario().isBlank()
                ? request.comentario().trim()
                : null;

        return new Avaliacao(
                empresa,
                trimOrNull(request.nomeUsuario()),
                request.nota(),
                comentarioLimpo
        );
    }

    public static AvaliacaoResponse toResponse(Avaliacao avaliacao) {
        if (avaliacao == null) {
            return null;
        }

        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getEmpresa() != null ? avaliacao.getEmpresa().getId() : null,
                avaliacao.getNomeUsuario(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getCriadoEm()
        );
    }

    private static String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }
}
