package com.residuos.lixoeletronico.dto.empresa;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record EmpresaUpdateRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    String nome,

    @Valid
    ContatoDTO contato,

    @Valid
    EnderecoDTO endereco,

    String quemSomos,
    String porQueReciclamos,
    String instrucoesDescarte,

    @Valid
    Set<HorarioFuncionamentoDTO> horarios,

    Set<CategoriaMaterial> materiaisAceitos
) {}
