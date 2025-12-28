package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.IslemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IslemLogRepository extends JpaRepository<IslemLog, Long> {
    // Logları tarihe göre (En yeni en üstte) sırala
    List<IslemLog> findAllByOrderByTarihDesc();
}