import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

@Entity
@Table(name = "tb_empresa")
public class Empresa{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @JsonIgnore // Evita que a senha seja exposta nas respostas da API (JSON)
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(columnDefinition = "TEXT")
    private String quemSomos;

    @Column(columnDefinition = "TEXT")
    private String porQueReciclamos;

    @Column(length = 255)
    private String endereco; 

    @Column(length = 100)
    private String horarioFuncionamento;
    

   public Empresa(){

    }

    public Empresa(Long id, String nome, String senha, String email, String telefone, String quemSomos, String porQueReciclamos, String endereco, String horarioFuncionamento){
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.telefone = telefone;
        this.quemSomos = quemSomos;
        this.porQueReciclamos = porQueReciclamos;
        this.endereco = endereco;
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getQuemSomos() { return quemSomos; }
    public void setQuemSomos(String quemSomos) { this.quemSomos = quemSomos; }

    public String getPorQueReciclamos() { return porQueReciclamos; }
    public void setPorQueReciclamos(String porQueReciclamos) { this.porQueReciclamos = porQueReciclamos; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getHorarioFuncionamento() { return horarioFuncionamento; }
    public void setHorarioFuncionamento(String horarioFuncionamento) { this.horarioFuncionamento = horarioFuncionamento; }

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