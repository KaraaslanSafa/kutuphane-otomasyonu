package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.KitapDegerlendirme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KitapDegerlendirmeRepository extends JpaRepository<KitapDegerlendirme, Long> {
    List<KitapDegerlendirme> findByKitapId(Long kitapId);
    List<KitapDegerlendirme> findByKullaniciId(Long kullaniciId);
    KitapDegerlendirme findByKullaniciIdAndKitapId(Long kullaniciId, Long kitapId);
}

