package com.residuos.lixoeletronico.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.residuos.lixoeletronico.model.Contato;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.Endereco;
import com.residuos.lixoeletronico.model.HorarioFuncionamento;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.repository.EmpresaRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;

    public DataLoader(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Empresa Real: CODEL Reciclagem (cria ou atualiza a senha caso já exista)
        Empresa codel = empresaRepository.findByEmail("contato@codelreciclagem.com.br")
                .orElseGet(Empresa::new);

        codel.setNome("CODEL Reciclagem");
        codel.setEmail("contato@codelreciclagem.com.br");
        codel.setSenha("codel123"); // Garante a senha codel123 mesmo se já tiver sido criada antes

        codel.setQuemSomos("Somos uma empresa especializada no descarte correto de eletrônicos. " +
                "Atuamos com a coleta, a triagem e o desmonte seguro de equipamentos em desuso.");

        codel.setPorQueReciclamos("Trabalhamos para dar uma destinação ambientalmente correta aos resíduos, " +
                "promovendo a reutilização de peças e componentes para reduzir o impacto ambiental.");

        codel.setInstrucoesDescarte("Recebemos os materiais nos pontos autorizados e realizamos a separação criteriosa por categoria de resíduo.");

        if (codel.getTotalKgDescartado() == null || codel.getTotalKgDescartado().compareTo(BigDecimal.ZERO) == 0) {
            codel.adicionarKg(new BigDecimal("67000.000")); // 67 toneladas
        }

        if (codel.getContato() == null) {
            codel.setContato(new Contato());
        }
        codel.getContato().setWhatsapp("(34) 99672-3880");
        codel.getContato().setTelefone("(34) 3210-9900");
        codel.getContato().setSite("https://codelreciclagem.com.br");
        codel.getContato().setInstagram("@codelreciclagem");

        if (codel.getEndereco() == null) {
            codel.setEndereco(new Endereco());
        }
        codel.getEndereco().setCep("38408-100");
        codel.getEndereco().setLogradouro("Av. João Naves de Ávila");
        codel.getEndereco().setNumero("2200");
        codel.getEndereco().setBairro("Santa Maria");
        codel.getEndereco().setCidade("Uberlândia");
        codel.getEndereco().setUf("MG");

        if (codel.getHorarios().isEmpty()) {
            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        }

        if (codel.getMateriaisAceitos().isEmpty()) {
            codel.getMateriaisAceitos().addAll(Set.of(
                    CategoriaMaterial.INFORMATICA,
                    CategoriaMaterial.CELULARES_TABLETS,
                    CategoriaMaterial.PILHAS_BATERIAS,
                    CategoriaMaterial.CABOS_CARREGADORES
            ));
        }

        empresaRepository.save(codel);

        // 2. Empresa Fictícia de Teste: admin@admin / senha admin
        Empresa admin = empresaRepository.findByEmail("admin@admin.com")
                .orElseGet(Empresa::new);

        admin.setNome("EcoPonto Reciclagem Fictícia");
        admin.setEmail("admin@admin.com");
        admin.setSenha("admin");

        admin.setQuemSomos("Empresa modelo criada para demonstração e homologação do sistema de gestão de lixo eletrônico.");
        admin.setPorQueReciclamos("Promover práticas sustentáveis de descarte ecológico com transparência e rastreabilidade total.");
        admin.setInstrucoesDescarte("Separe os cabos e pilhas em recipientes isolados antes da entrega no ponto de coleta.");

        if (admin.getTotalKgDescartado() == null || admin.getTotalKgDescartado().compareTo(BigDecimal.ZERO) == 0) {
            admin.adicionarKg(new BigDecimal("15500.000")); // 15.5 toneladas
        }

        if (admin.getContato() == null) {
            admin.setContato(new Contato());
        }
        admin.getContato().setWhatsapp("(34) 99999-0000");
        admin.getContato().setTelefone("(34) 3232-0000");
        admin.getContato().setSite("https://ecoponto-demo.eco.br");
        admin.getContato().setInstagram("@ecoponto.demo");

        if (admin.getEndereco() == null) {
            admin.setEndereco(new Endereco());
        }
        admin.getEndereco().setCep("38400-000");
        admin.getEndereco().setLogradouro("Rua da Sustentabilidade");
        admin.getEndereco().setNumero("500");
        admin.getEndereco().setBairro("Centro");
        admin.getEndereco().setCidade("Uberlândia");
        admin.getEndereco().setUf("MG");

        if (admin.getHorarios().isEmpty()) {
            admin.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            admin.getHorarios().add(new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));
        }

        if (admin.getMateriaisAceitos().isEmpty()) {
            admin.getMateriaisAceitos().addAll(Set.of(
                    CategoriaMaterial.INFORMATICA,
                    CategoriaMaterial.CELULARES_TABLETS,
                    CategoriaMaterial.PILHAS_BATERIAS,
                    CategoriaMaterial.LAMPADAS,
                    CategoriaMaterial.CABOS_CARREGADORES
            ));
        }

        empresaRepository.save(admin);
    }
}
