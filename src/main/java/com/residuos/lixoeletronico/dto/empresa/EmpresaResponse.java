package com.residuos.lixoeletronico.dto.empresa;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record EmpresaResponse(
    Long id,
    String nome,
    String email,
    ContatoDTO contato,
    EnderecoDTO endereco,
    String quemSomos,
    String porQueReciclamos,
    String instrucoesDescarte,
    List<HorarioFuncionamentoDTO> horarios,
    Set<CategoriaMaterial> materiaisAceitos,
    BigDecimal totalKgDescartado,
    Instant criadoEm,
    Instant atualizadoEm
) {}
