package com.residuos.lixoeletronico.repository;

import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.HorarioFuncionamento;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se email existe corretamente no banco")
    void deveVerificarExistenciaDeEmail() {
        Empresa empresa = new Empresa(null, "Empresa Alpha", "senha123", "alpha@recicla.com");
        entityManager.persistAndFlush(empresa);

        assertTrue(repository.existsByEmail("alpha@recicla.com"));
        assertFalse(repository.existsByEmail("beta@recicla.com"));
    }

    @Test
    @DisplayName("Deve buscar perfil por ID com coleções carregadas via EntityGraph")
    void deveBuscarPerfilComColecoes() {
        Empresa empresa = new Empresa(null, "Empresa Beta", "senha123", "beta@recicla.com");
        empresa.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
        empresa.getMateriaisAceitos().add(CategoriaMaterial.INFORMATICA);

        Empresa salva = entityManager.persistAndFlush(empresa);
        entityManager.clear(); // Limpa o contexto de persistência para forçar consulta ao banco

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

        Empresa salva = entityManager.persistAndFlush(empresa);
        Long id = salva.getId();

        int linhasAfetadas = repository.adicionarKgDescartado(id, new BigDecimal("50.500"));
        assertEquals(1, linhasAfetadas);

        entityManager.clear(); // Recarrega do banco para validar o valor atualizado
        Empresa atualizada = entityManager.find(Empresa.class, id);

        assertEquals(new BigDecimal("150.500"), atualizada.getTotalKgDescartado());
    }
}
