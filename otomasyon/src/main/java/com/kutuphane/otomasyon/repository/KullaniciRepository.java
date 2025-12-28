package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    // Email ve Şifre kontrolü için hazır metot
    Optional<Kullanici> findByEmailAndSifre(String email, String sifre);
}