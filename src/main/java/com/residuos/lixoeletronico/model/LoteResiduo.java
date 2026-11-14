package com.residuos.lixoeletronico.model;

import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import com.residuos.lixoeletronico.model.enums.StatusLote;
import com.residuos.lixoeletronico.model.enums.UrgenciaDescarte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "tb_lote_residuo")
@Getter
@Setter
@NoArgsConstructor
public class LoteResiduo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nome_usuario", nullable = false, length = 150)
    private String nomeUsuario;

    @Column(name = "contato_usuario", nullable = false, length = 100)
    private String contatoUsuario;

    @Column(name = "data_recepcao", nullable = false)
    private LocalDate dataRecepcao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_material", nullable = false, length = 40)
    private CategoriaMaterial categoriaMaterial;

    @Column(name = "tipo_residuo", nullable = false, length = 150)
    private String tipoResiduo;

    @Column(name = "peso_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia_descarte", nullable = false, length = 20)
    private UrgenciaDescarte urgenciaDescarte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusLote status = StatusLote.EM_ESTOQUE;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public LoteResiduo(
            Empresa empresa,
            String nomeUsuario,
            String contatoUsuario,
            LocalDate dataRecepcao,
            CategoriaMaterial categoriaMaterial,
            String tipoResiduo,
            BigDecimal pesoKg,
            UrgenciaDescarte urgenciaDescarte
    ) {
        this.empresa = empresa;
        this.nomeUsuario = nomeUsuario;
        this.contatoUsuario = contatoUsuario;
        this.dataRecepcao = dataRecepcao;
        this.categoriaMaterial = categoriaMaterial;
        this.tipoResiduo = tipoResiduo;
        this.pesoKg = pesoKg;
        this.urgenciaDescarte = urgenciaDescarte;
        this.status = StatusLote.EM_ESTOQUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoteResiduo that = (LoteResiduo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
