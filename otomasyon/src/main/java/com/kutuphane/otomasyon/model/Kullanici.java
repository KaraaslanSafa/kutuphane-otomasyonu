package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kullanici")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kullanici_tipi", discriminatorType = DiscriminatorType.STRING)
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adSoyad;
    private String email;
    private String sifre;

    // Veritabanındaki 'kullanici_tipi' sütununu okumak için:
    @Column(name = "kullanici_tipi", insertable = false, updatable = false)
    private String kullaniciTipi;

    // --- CONSTRUCTORLAR ---
    public Kullanici() {}

    public Kullanici(String adSoyad, String email, String sifre, String kullaniciTipi) {
        this.adSoyad = adSoyad;
        this.email = email;
        this.sifre = sifre;
        this.kullaniciTipi = kullaniciTipi; // Bu constructor kullanımda tip manuel set edilebilir
    }

    // --- MİRAS ALINACAK METOD (Personel hatasını çözer) ---
    public int oduncAlmaLimitiHesapla() {
        return 3; 
    }

    // --- GETTER VE SETTERLAR (Girişin çalışması için şart) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAdSoyad() { return adSoyad; }
    public void setAdSoyad(String adSoyad) { this.adSoyad = adSoyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getKullaniciTipi() { return kullaniciTipi; }
    public void setKullaniciTipi(String kullaniciTipi) { this.kullaniciTipi = kullaniciTipi; }
}