package com.residuos.lixoeletronico.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.residuos.lixoeletronico.model.enums.CategoriaMaterial;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_empresa")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Contato contato = new Contato();

    @Embedded
    private Endereco endereco = new Endereco();

    @Column(columnDefinition = "TEXT")
    private String quemSomos;

    @Column(columnDefinition = "TEXT")
    private String porQueReciclamos;

    @Column(columnDefinition = "TEXT")
    private String instrucoesDescarte;

    @ElementCollection
    @CollectionTable(name = "tb_empresa_horario", joinColumns = @JoinColumn(name = "empresa_id"))
    private Set<HorarioFuncionamento> horarios = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "tb_empresa_material", joinColumns = @JoinColumn(name = "empresa_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "material", length = 40, nullable = false)
    private Set<CategoriaMaterial> materiaisAceitos = new HashSet<>();

    @Column(name = "total_kg_descartado", nullable = false, precision = 12, scale = 3)
    private BigDecimal totalKgDescartado = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public Empresa(Long id, String nome, String senha, String email) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.email = email;
    }

    public void adicionarKg(BigDecimal kg) {
        if (kg != null && kg.compareTo(BigDecimal.ZERO) > 0) {
            this.totalKgDescartado = this.totalKgDescartado.add(kg);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return Objects.equals(id, empresa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
