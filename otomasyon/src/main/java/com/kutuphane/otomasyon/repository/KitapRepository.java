package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KitapRepository extends JpaRepository<Kitap, Long> {
    // Service dosyasının ihtiyaç duyduğu özel arama kodu:
    List<Kitap> findByOduncAlanId(Long oduncAlanId);
}