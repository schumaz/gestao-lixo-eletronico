package com.residuos.lixoeletronico.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    public void run(String... args) {
        // 1. Empresa Real: CODEL Reciclagem
        if (!empresaRepository.existsByEmail("contato@codelreciclagem.com.br")) {
            Empresa codel = new Empresa();
            codel.setNome("CODEL Reciclagem");
            codel.setEmail("contato@codelreciclagem.com.br");
            codel.setSenha("codel123");

            codel.setQuemSomos("Somos uma empresa especializada no descarte correto de eletrônicos. " +
                    "Atuamos com a coleta, a triagem e o desmonte seguro de equipamentos em desuso.");

            codel.setPorQueReciclamos("Trabalhamos para dar uma destinação ambientalmente correta aos resíduos, " +
                    "promovendo a reutilização de peças e componentes para reduzir o impacto ambiental.");

            codel.setInstrucoesDescarte("Recebemos os materiais nos pontos autorizados e realizamos a separação criteriosa por categoria de resíduo.");

            codel.adicionarKg(new BigDecimal("67000.000")); // 67 toneladas

            Contato contatoCodel = new Contato();
            contatoCodel.setWhatsapp("(34) 99672-3880");
            contatoCodel.setTelefone("(34) 3210-9900");
            contatoCodel.setSite("https://codelreciclagem.com.br");
            contatoCodel.setInstagram("@codelreciclagem");
            codel.setContato(contatoCodel);

            Endereco enderecoCodel = new Endereco();
            enderecoCodel.setCep("38408-100");
            enderecoCodel.setLogradouro("Av. João Naves de Ávila");
            enderecoCodel.setNumero("2200");
            enderecoCodel.setBairro("Santa Maria");
            enderecoCodel.setCidade("Uberlândia");
            enderecoCodel.setUf("MG");
            codel.setEndereco(enderecoCodel);

            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(17, 0)));
            codel.getHorarios().add(new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));

            codel.getMateriaisAceitos().addAll(Set.of(
                    CategoriaMaterial.INFORMATICA,
                    CategoriaMaterial.CELULARES_TABLETS,
                    CategoriaMaterial.PILHAS_BATERIAS,
                    CategoriaMaterial.CABOS_CARREGADORES
            ));

            empresaRepository.save(codel);
        }

        // 2. Empresa Fictícia de Teste: admin@admin / senha admin
        if (!empresaRepository.existsByEmail("admin@admin.com")) {
            Empresa admin = new Empresa();
            admin.setNome("EcoPonto Reciclagem Fictícia");
            admin.setEmail("admin@admin.com");
            admin.setSenha("admin");

            admin.setQuemSomos("Empresa modelo criada para demonstração e homologação do sistema de gestão de lixo eletrônico.");
            admin.setPorQueReciclamos("Promover práticas sustentáveis de descarte ecológico com transparência e rastreabilidade total.");
            admin.setInstrucoesDescarte("Separe os cabos e pilhas em recipientes isolados antes da entrega no ponto de coleta.");

            admin.adicionarKg(new BigDecimal("15500.000")); // 15.5 toneladas

            Contato contatoAdmin = new Contato();
            contatoAdmin.setWhatsapp("(34) 99999-0000");
            contatoAdmin.setTelefone("(34) 3232-0000");
            contatoAdmin.setSite("https://ecoponto-demo.eco.br");
            contatoAdmin.setInstagram("@ecoponto.demo");
            admin.setContato(contatoAdmin);

            Endereco enderecoAdmin = new Endereco();
            enderecoAdmin.setCep("38400-000");
            enderecoAdmin.setLogradouro("Rua da Sustentabilidade");
            enderecoAdmin.setNumero("500");
            enderecoAdmin.setBairro("Centro");
            enderecoAdmin.setCidade("Uberlândia");
            enderecoAdmin.setUf("MG");
            admin.setEndereco(enderecoAdmin);

            admin.getHorarios().add(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            admin.getHorarios().add(new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

            admin.getMateriaisAceitos().addAll(Set.of(
                    CategoriaMaterial.INFORMATICA,
                    CategoriaMaterial.CELULARES_TABLETS,
                    CategoriaMaterial.PILHAS_BATERIAS,
                    CategoriaMaterial.LAMPADAS,
                    CategoriaMaterial.CABOS_CARREGADORES
            ));

            empresaRepository.save(admin);
        }
    }
}
