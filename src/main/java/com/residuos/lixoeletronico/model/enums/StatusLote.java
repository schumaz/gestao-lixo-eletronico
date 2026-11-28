package com.residuos.lixoeletronico.model.enums;

public enum StatusLote {
    EM_ESTOQUE,
    EM_PROCESSAMENTO,
    DESTINADO;

    /**
     * Valida se a transição do status atual para {@code novoStatus} respeita
     * o ciclo de vida permitido: EM_ESTOQUE -> EM_PROCESSAMENTO -> DESTINADO.
     */
    public boolean podeTransicionarPara(StatusLote novoStatus) {
        if (novoStatus == null) {
            return false;
        }
        return switch (this) {
            case EM_ESTOQUE -> novoStatus == EM_PROCESSAMENTO;
            case EM_PROCESSAMENTO -> novoStatus == DESTINADO;
            case DESTINADO -> false;
        };
    }
}
