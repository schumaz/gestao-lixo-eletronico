package com.residuos.lixoeletronico.dto.empresa;

import com.residuos.lixoeletronico.model.Contato;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.Endereco;
import com.residuos.lixoeletronico.model.HorarioFuncionamento;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class EmpresaMapper {

    private EmpresaMapper() {
    }

    public static Empresa toEntity(EmpresaCreateRequest request) {
        if (request == null) {
            return null;
        }

        Empresa empresa = new Empresa();
        empresa.setNome(request.nome());
        empresa.setEmail(request.email());
        empresa.setSenha(request.senha());

        if (request.contato() != null) {
            empresa.setContato(toContatoEntity(request.contato()));
        }

        if (request.endereco() != null) {
            empresa.setEndereco(toEnderecoEntity(request.endereco()));
        }

        return empresa;
    }

    public static EmpresaResponse toResponse(Empresa empresa) {
        if (empresa == null) {
            return null;
        }

        List<HorarioFuncionamentoDTO> horariosDTO = null;
        if (empresa.getHorarios() != null) {
            horariosDTO = empresa.getHorarios().stream()
                    .map(EmpresaMapper::toHorarioDTO)
                    .sorted(Comparator.comparing(HorarioFuncionamentoDTO::diaSemana)
                            .thenComparing(HorarioFuncionamentoDTO::abertura))
                    .toList();
        }

        Set<CategoriaMaterial> materiais =
                empresa.getMateriaisAceitos() != null ? new HashSet<>(empresa.getMateriaisAceitos()) : Set.of();

        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNome(),
                empresa.getEmail(),
                toContatoDTO(empresa.getContato()),
                toEnderecoDTO(empresa.getEndereco()),
                empresa.getQuemSomos(),
                empresa.getPorQueReciclamos(),
                empresa.getInstrucoesDescarte(),
                horariosDTO,
                materiais,
                empresa.getTotalKgDescartado(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm()
        );
    }

    public static void updateEntityFromRequest(Empresa empresa, EmpresaUpdateRequest request) {
        if (empresa == null || request == null) {
            return;
        }

        empresa.setNome(request.nome());
        empresa.setQuemSomos(request.quemSomos());
        empresa.setPorQueReciclamos(request.porQueReciclamos());
        empresa.setInstrucoesDescarte(request.instrucoesDescarte());

        if (request.contato() != null) {
            empresa.setContato(toContatoEntity(request.contato()));
        }

        if (request.endereco() != null) {
            empresa.setEndereco(toEnderecoEntity(request.endereco()));
        }

        if (request.horarios() != null) {
            Set<HorarioFuncionamento> novosHorarios = request.horarios().stream()
                    .map(EmpresaMapper::toHorarioEntity)
                    .collect(Collectors.toSet());
            empresa.getHorarios().clear();
            empresa.getHorarios().addAll(novosHorarios);
        }

        if (request.materiaisAceitos() != null) {
            empresa.getMateriaisAceitos().clear();
            empresa.getMateriaisAceitos().addAll(request.materiaisAceitos());
        }
    }

    public static Endereco toEnderecoEntity(EnderecoDTO dto) {
        if (dto == null) {
            return new Endereco();
        }
        return new Endereco(
                dto.cep(),
                dto.logradouro(),
                dto.numero(),
                dto.complemento(),
                dto.bairro(),
                dto.cidade(),
                dto.uf(),
                dto.latitude(),
                dto.longitude()
        );
    }

    public static EnderecoDTO toEnderecoDTO(Endereco entity) {
        if (entity == null) {
            return null;
        }
        return new EnderecoDTO(
                entity.getCep(),
                entity.getLogradouro(),
                entity.getNumero(),
                entity.getComplemento(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }

    public static Contato toContatoEntity(ContatoDTO dto) {
        if (dto == null) {
            return new Contato();
        }
        return new Contato(
                dto.telefone(),
                dto.whatsapp(),
                dto.site(),
                dto.instagram()
        );
    }

    public static ContatoDTO toContatoDTO(Contato entity) {
        if (entity == null) {
            return null;
        }
        return new ContatoDTO(
                entity.getTelefone(),
                entity.getWhatsapp(),
                entity.getSite(),
                entity.getInstagram()
        );
    }

    public static HorarioFuncionamento toHorarioEntity(HorarioFuncionamentoDTO dto) {
        if (dto == null) {
            return null;
        }
        return new HorarioFuncionamento(
                dto.diaSemana(),
                dto.abertura(),
                dto.fechamento()
        );
    }

    public static HorarioFuncionamentoDTO toHorarioDTO(HorarioFuncionamento entity) {
        if (entity == null) {
            return null;
        }
        return new HorarioFuncionamentoDTO(
                entity.getDiaSemana(),
                entity.getAbertura(),
                entity.getFechamento()
        );
    }
}
