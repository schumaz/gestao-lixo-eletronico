package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.HorarioFuncionamento;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository repository;

    @Test
    @DisplayName("Deve verificar se email existe corretamente no banco")
    void deveVerificarExistenciaDeEmail() {
        Empresa empresa = new Empresa(null, "Empresa Alpha", "senha123", "alpha@recicla.com");
        repository.save(empresa);

        assertTrue(repository.existsByEmail("alpha@recicla.com"));
        assertFalse(repository.existsByEmail("beta@recicla.com"));
    }

    @Test
    @DisplayName("Deve buscar perfil por ID com coleções carregadas via EntityGraph")
    void deveBuscarPerfilComColecoes() {
        Empresa empresa = new Empresa(null, "Empresa Beta", "senha123", "beta@recicla.com");
        empresa.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
        empresa.getMateriaisAceitos().add(CategoriaMaterial.INFORMATICA);

        Empresa salva = repository.save(empresa);

        Optional<Empresa> perfilOpt = repository.findPerfilById(salva.getId());

        assertTrue(perfilOpt.isPresent());
        Empresa perfil = perfilOpt.get();
        assertEquals("Empresa Beta", perfil.getNome());
        assertEquals(1, perfil.getHorarios().size());
        assertEquals(1, perfil.getMateriaisAceitos().size());
    }

    @Test
    @DisplayName("Deve executar incremento atômico de kg descartados via Query Modifying")
    void deveIncrementarKgDescartados() {
        Empresa empresa = new Empresa(null, "Empresa Gamma", "senha123", "gamma@recicla.com");
        empresa.setTotalKgDescartado(new BigDecimal("100.000"));

        Empresa salva = repository.save(empresa);
        Long id = salva.getId();

        int linhasAfetadas = repository.adicionarKgDescartado(id, new BigDecimal("50.500"));
        assertEquals(1, linhasAfetadas);

        Optional<Empresa> atualizadaOpt = repository.findById(id);
        assertTrue(atualizadaOpt.isPresent());
        assertEquals(new BigDecimal("150.500"), atualizadaOpt.get().getTotalKgDescartado());
    }
}
