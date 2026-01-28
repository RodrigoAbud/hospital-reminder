package com.hospital.reminder.repository;

import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.enums.FaseRetorno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescricaoEscalonadaRepository extends JpaRepository<PrescricaoEscalonada, UUID> {

    List<PrescricaoEscalonada> findByPacienteIdExternoOrderByCreatedAtDesc(String pacienteIdExterno);

    @Query("SELECT p FROM PrescricaoEscalonada p WHERE p.finalizada = false AND p.faseAtual IN :fases")
    List<PrescricaoEscalonada> findAbertasPorFases(@Param("fases") List<FaseRetorno> fases);

    @Query("SELECT p FROM PrescricaoEscalonada p WHERE p.finalizada = false AND p.faseAtual = 'FASE1' AND p.dataLimiteFase1 IS NOT NULL AND p.dataLimiteFase1 <= :hoje")
    List<PrescricaoEscalonada> findVencidasFase1(@Param("hoje") LocalDate hoje);

    @Query("SELECT p FROM PrescricaoEscalonada p WHERE p.finalizada = false AND p.faseAtual = 'FASE2' AND p.dataLimiteFase2 IS NOT NULL AND p.dataLimiteFase2 <= :hoje")
    List<PrescricaoEscalonada> findVencidasFase2(@Param("hoje") LocalDate hoje);

    @Query("SELECT p FROM PrescricaoEscalonada p WHERE p.finalizada = false AND p.faseAtual = 'FASE3' AND p.dataInicioFase3 IS NOT NULL")
    List<PrescricaoEscalonada> findEmFase3();

    Optional<PrescricaoEscalonada> findById(UUID id);
}
