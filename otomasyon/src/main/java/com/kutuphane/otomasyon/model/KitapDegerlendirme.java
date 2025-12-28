package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "kitap_degerlendirmeleri")
@Data
public class KitapDegerlendirme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long kullaniciId;
    private Long kitapId;
    private Integer yildiz; // 1-5 arası
    @Column(length = 1000)
    private String yorum;
    private LocalDateTime tarih;
}

