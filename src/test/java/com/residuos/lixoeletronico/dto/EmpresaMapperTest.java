package com.residuos.lixoeletronico.dto;

import com.residuos.lixoeletronico.dto.empresa.*;
import com.residuos.lixoeletronico.model.*;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaMapperTest {

    @Test
    @DisplayName("Deve converter EmpresaCreateRequest para entidade Empresa com endereço e contato")
    void deveConverterCreateRequestParaEntidade() {
        ContatoDTO contatoDTO = new ContatoDTO("34999991111", "34999992222", "site.com", "@insta");
        EnderecoDTO enderecoDTO = new EnderecoDTO("38400000", "Rua A", "10", "Apto 1", "Bairro B", "Uberlandia", "MG", -18.9, -48.2);
        EmpresaCreateRequest request = new EmpresaCreateRequest("Tech Recicla", "tech@recicla.com", "senha123", contatoDTO, enderecoDTO);

        Empresa empresa = EmpresaMapper.toEntity(request);

        assertNotNull(empresa);
        assertEquals("Tech Recicla", empresa.getNome());
        assertEquals("tech@recicla.com", empresa.getEmail());
        assertEquals("senha123", empresa.getSenha());
        assertNotNull(empresa.getContato());
        assertEquals("site.com", empresa.getContato().getSite());
        assertNotNull(empresa.getEndereco());
        assertEquals("Uberlandia", empresa.getEndereco().getCidade());
    }

    @Test
    @DisplayName("Deve converter entidade para EmpresaResponse ordenando horários por dia da semana e abertura")
    void deveConverterEntidadeParaResponseComHorariosOrdenados() {
        Empresa empresa = new Empresa(1L, "Eco Ponto", "senha123", "eco@ponto.com");
        empresa.setTotalKgDescartado(new BigDecimal("1200.000"));

        HorarioFuncionamento sabado = new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        HorarioFuncionamento segundaTarde = new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(18, 0));
        HorarioFuncionamento segundaManha = new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        empresa.getHorarios().addAll(Set.of(sabado, segundaTarde, segundaManha));
        empresa.getMateriaisAceitos().add(CategoriaMaterial.INFORMATICA);

        EmpresaResponse response = EmpresaMapper.toResponse(empresa);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Eco Ponto", response.nome());
        assertEquals(new BigDecimal("1200.000"), response.totalKgDescartado());
        assertEquals(1, response.materiaisAceitos().size());
        assertTrue(response.materiaisAceitos().contains(CategoriaMaterial.INFORMATICA));

        assertEquals(3, response.horarios().size());
        assertEquals(DayOfWeek.MONDAY, response.horarios().get(0).diaSemana());
        assertEquals(LocalTime.of(8, 0), response.horarios().get(0).abertura());

        assertEquals(DayOfWeek.MONDAY, response.horarios().get(1).diaSemana());
        assertEquals(LocalTime.of(13, 0), response.horarios().get(1).abertura());

        assertEquals(DayOfWeek.SATURDAY, response.horarios().get(2).diaSemana());
    }

    @Test
    @DisplayName("Deve atualizar dados da entidade mantendo a mesma referência dos sets de horários e materiais")
    void deveAtualizarEntidadePreservandoColecoes() {
        Empresa empresa = new Empresa(1L, "Antigo Nome", "senha123", "email@antigo.com");
        empresa.getHorarios().add(new HorarioFuncionamento(DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        empresa.getMateriaisAceitos().add(CategoriaMaterial.LAMPADAS);

        Set<HorarioFuncionamento> refHorariosOriginal = empresa.getHorarios();
        Set<CategoriaMaterial> refMateriaisOriginal = empresa.getMateriaisAceitos();

        HorarioFuncionamentoDTO novoHorario = new HorarioFuncionamentoDTO(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        EmpresaUpdateRequest updateRequest = new EmpresaUpdateRequest(
                "Novo Nome",
                null,
                null,
                "Quem somos novo",
                "Por que reciclamos novo",
                "Instrucoes novas",
                Set.of(novoHorario),
                Set.of(CategoriaMaterial.INFORMATICA, CategoriaMaterial.PILHAS_BATERIAS)
        );

        EmpresaMapper.updateEntityFromRequest(empresa, updateRequest);

        assertEquals("Novo Nome", empresa.getNome());
        assertEquals("Quem somos novo", empresa.getQuemSomos());
        assertSame(refHorariosOriginal, empresa.getHorarios());
        assertSame(refMateriaisOriginal, empresa.getMateriaisAceitos());
        assertEquals(1, empresa.getHorarios().size());
        assertEquals(2, empresa.getMateriaisAceitos().size());
    }

    @Test
    @DisplayName("Deve retornar nulo sem lançar exceção ao passar nulos para o mapper")
    void deveTratarNulosComSeguranca() {
        assertNull(EmpresaMapper.toEntity(null));
        assertNull(EmpresaMapper.toResponse(null));
        assertNull(EmpresaMapper.toEnderecoDTO(null));
        assertNull(EmpresaMapper.toContatoDTO(null));
        assertNull(EmpresaMapper.toHorarioDTO(null));
    }
}
