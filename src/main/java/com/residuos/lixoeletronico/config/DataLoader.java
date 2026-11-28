package com.residuos.lixoeletronico.config;

import com.residuos.lixoeletronico.model.Avaliacao;
import com.residuos.lixoeletronico.model.Contato;
import com.residuos.lixoeletronico.model.Empresa;
import com.residuos.lixoeletronico.model.Endereco;
import com.residuos.lixoeletronico.model.HorarioFuncionamento;
import com.residuos.lixoeletronico.model.LoteResiduo;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import com.residuos.lixoeletronico.repository.AvaliacaoRepository;
import com.residuos.lixoeletronico.repository.EmpresaRepository;
import com.residuos.lixoeletronico.repository.LoteResiduoRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final LoteResiduoRepository loteResiduoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public DataLoader(
        EmpresaRepository empresaRepository,
        LoteResiduoRepository loteResiduoRepository,
        AvaliacaoRepository avaliacaoRepository
    ) {
        this.empresaRepository = empresaRepository;
        this.loteResiduoRepository = loteResiduoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Empresa Real: CODEL Reciclagem (cria ou atualiza a senha caso já exista)
        Empresa codel = empresaRepository
            .findByEmail("contato@codelreciclagem.com.br")
            .orElseGet(Empresa::new);

        codel.setNome("CODEL Reciclagem");
        codel.setEmail("contato@codelreciclagem.com.br");
        codel.setSenha("codel123"); // Garante a senha codel123 mesmo se já tiver sido criada antes

        codel.setQuemSomos(
            "Somos uma empresa especializada no descarte correto de eletrônicos. " +
                "Atuamos com a coleta, a triagem e o desmonte seguro de equipamentos em desuso."
        );

        codel.setPorQueReciclamos(
            "Trabalhamos para dar uma destinação ambientalmente correta aos resíduos, " +
                "promovendo a reutilização de peças e componentes para reduzir o impacto ambiental."
        );

        codel.setInstrucoesDescarte(
            "Recebemos os materiais nos pontos autorizados e realizamos a separação criteriosa por categoria de resíduo."
        );

        if (
            codel.getTotalKgDescartado() == null ||
            codel.getTotalKgDescartado().compareTo(BigDecimal.ZERO) == 0
        ) {
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
            codel
                .getHorarios()
                .add(
                    new HorarioFuncionamento(
                        DayOfWeek.MONDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(17, 0)
                    )
                );
            codel
                .getHorarios()
                .add(
                    new HorarioFuncionamento(
                        DayOfWeek.FRIDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(17, 0)
                    )
                );
            codel
                .getHorarios()
                .add(
                    new HorarioFuncionamento(
                        DayOfWeek.SATURDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(12, 0)
                    )
                );
        }

        if (codel.getMateriaisAceitos().isEmpty()) {
            codel
                .getMateriaisAceitos()
                .addAll(
                    Set.of(
                        CategoriaMaterial.INFORMATICA,
                        CategoriaMaterial.CELULARES_TABLETS,
                        CategoriaMaterial.PILHAS_BATERIAS,
                        CategoriaMaterial.CABOS_CARREGADORES
                    )
                );
        }

        Empresa codelSalva = empresaRepository.save(codel);

        // 2. Empresa Fictícia de Teste: admin@admin / senha admin
        Empresa admin = empresaRepository
            .findByEmail("admin@admin.com")
            .orElseGet(Empresa::new);

        admin.setNome("EcoPonto Reciclagem Fictícia");
        admin.setEmail("admin@admin.com");
        admin.setSenha("admin");

        admin.setQuemSomos(
            "Empresa modelo criada para demonstração e homologação do sistema de gestão de lixo eletrônico."
        );
        admin.setPorQueReciclamos(
            "Promover práticas sustentáveis de descarte ecológico com transparência e rastreabilidade total."
        );
        admin.setInstrucoesDescarte(
            "Separe os cabos e pilhas em recipientes isolados antes da entrega no ponto de coleta."
        );

        if (
            admin.getTotalKgDescartado() == null ||
            admin.getTotalKgDescartado().compareTo(BigDecimal.ZERO) == 0
        ) {
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
            admin
                .getHorarios()
                .add(
                    new HorarioFuncionamento(
                        DayOfWeek.MONDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(18, 0)
                    )
                );
            admin
                .getHorarios()
                .add(
                    new HorarioFuncionamento(
                        DayOfWeek.SATURDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(13, 0)
                    )
                );
        }

        if (admin.getMateriaisAceitos().isEmpty()) {
            admin
                .getMateriaisAceitos()
                .addAll(
                    Set.of(
                        CategoriaMaterial.INFORMATICA,
                        CategoriaMaterial.CELULARES_TABLETS,
                        CategoriaMaterial.PILHAS_BATERIAS,
                        CategoriaMaterial.LAMPADAS,
                        CategoriaMaterial.CABOS_CARREGADORES
                    )
                );
        }

        Empresa adminSalva = empresaRepository.save(admin);

        // 3. Seed inicial de Lotes de Resíduos para consulta em estoque
        if (loteResiduoRepository.count() == 0) {
            LoteResiduo lote1 = new LoteResiduo(
                codelSalva,
                "Mariana Oliveira",
                "(34) 99123-4567",
                LocalDate.now().minusDays(2),
                CategoriaMaterial.INFORMATICA,
                "Notebooks corporativos e placas-mãe",
                new BigDecimal("42.500"),
                UrgenciaDescarte.MEDIA
            );

            LoteResiduo lote2 = new LoteResiduo(
                codelSalva,
                "Carlos Eduardo Lima",
                "carlos.lima@email.com",
                LocalDate.now().minusDays(1),
                CategoriaMaterial.PILHAS_BATERIAS,
                "Baterias de íon-lítio inchadas",
                new BigDecimal("18.250"),
                UrgenciaDescarte.CRITICA
            );

            LoteResiduo lote3 = new LoteResiduo(
                codelSalva,
                "Fernanda Souza",
                "(34) 99876-5432",
                LocalDate.now().minusDays(10),
                CategoriaMaterial.CABOS_CARREGADORES,
                "Fontes ATX e cabos de rede",
                new BigDecimal("12.000"),
                UrgenciaDescarte.BAIXA
            );
            lote3.setStatus(StatusLote.DESTINADO);

            LoteResiduo lote4 = new LoteResiduo(
                adminSalva,
                "João Pedro Mendes",
                "(34) 99777-8899",
                LocalDate.now(),
                CategoriaMaterial.CELULARES_TABLETS,
                "Smartphones e tablets com tela trincada",
                new BigDecimal("9.800"),
                UrgenciaDescarte.ALTA
            );

            loteResiduoRepository.saveAll(List.of(lote1, lote2, lote3, lote4));
        }

        // 4. Seed inicial de Avaliações dos centros de coleta
        if (avaliacaoRepository.count() == 0) {
            Avaliacao av1 = new Avaliacao(
                codelSalva,
                "Mariana Oliveira",
                5,
                "Atendimento rápido, local muito organizado e triagem transparente."
            );
            Avaliacao av2 = new Avaliacao(
                codelSalva,
                "Carlos Eduardo Lima",
                4,
                "Ótima sinalização para entrega de pilhas e baterias."
            );
            Avaliacao av3 = new Avaliacao(
                adminSalva,
                "João Pedro Mendes",
                5,
                "Ponto de coleta limpo e equipe muito atenciosa."
            );
            Avaliacao av4 = new Avaliacao(
                adminSalva,
                "Fernanda Souza",
                4,
                "Fácil acesso e recebimento ágil dos eletrônicos."
            );

            avaliacaoRepository.saveAll(List.of(av1, av2, av3, av4));
        }
    }
}
