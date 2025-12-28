package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Kitap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ad;
    private String yazar;
    private String isbn;
    
    // Kitap kütüphanede mi yoksa birinde mi?
    private boolean musaitMi = true; 

    // Kitabı kim aldı? (ID ve İsim olarak tutuyoruz)
    private Long oduncAlanId; 
    private String oduncAlanAd;

    // Ne zaman getirmesi gerekiyor? (Ceza hesaplamak için)
    private LocalDate sonTeslimTarihi;

    // Kitabın Kapak Resmi (Link uzun olabilir diye limiti artırdık)
    @Column(length = 500) 
    private String resimUrl;
    
    // Yeni alanlar
    private Integer sayfaSayisi;
    private Integer yayinYili;
    @Column(length = 2000)
    private String aciklama;
    @Column(length = 1000)
    private String ozet;
    
    // Değerlendirme ortalaması
    private Double degerlendirmeOrtalamasi = 0.0;
    private Integer degerlendirmeSayisi = 0;
}