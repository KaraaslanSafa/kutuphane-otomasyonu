package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.FavoriKitap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriKitapRepository extends JpaRepository<FavoriKitap, Long> {
    List<FavoriKitap> findByKullaniciId(Long kullaniciId);
    Optional<FavoriKitap> findByKullaniciIdAndKitapId(Long kullaniciId, Long kitapId);
    void deleteByKullaniciIdAndKitapId(Long kullaniciId, Long kitapId);
}

