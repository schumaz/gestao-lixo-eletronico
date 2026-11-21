package com.residuos.lixoeletronico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.residuos.lixoeletronico.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCpf(String cpf);
}