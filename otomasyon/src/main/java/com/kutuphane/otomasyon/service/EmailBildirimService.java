package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmailBildirimService {

    private final KitapRepository kitapRepository;
    private final KullaniciRepository kullaniciRepository;

    public EmailBildirimService(KitapRepository kitapRepository, KullaniciRepository kullaniciRepository) {
        this.kitapRepository = kitapRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    // Her gün saat 09:00'da çalışır ve teslim tarihi yaklaşan kitaplar için bildirim gönderir
    @Scheduled(cron = "0 0 9 * * ?") // Her gün saat 09:00
    public void teslimTarihiYaklasanKitaplariKontrolEt() {
        List<Kitap> odunctekiKitaplar = kitapRepository.findAll().stream()
            .filter(k -> !k.isMusaitMi() && k.getSonTeslimTarihi() != null)
            .toList();

        LocalDate bugun = LocalDate.now();
        LocalDate ucGunSonra = bugun.plusDays(3);

        for (Kitap kitap : odunctekiKitaplar) {
            LocalDate teslimTarihi = kitap.getSonTeslimTarihi();
            
            // Teslim tarihi 3 gün içindeyse bildirim gönder
            if (teslimTarihi.isAfter(bugun) && teslimTarihi.isBefore(ucGunSonra) || teslimTarihi.equals(ucGunSonra)) {
                bildirimGonder(kitap, "Yaklaşan Teslim Tarihi", 
                    "Kitabınızın teslim tarihi yaklaşıyor: " + teslimTarihi);
            }
            
            // Teslim tarihi geçmişse uyarı gönder
            if (teslimTarihi.isBefore(bugun)) {
                long gecenGun = java.time.temporal.ChronoUnit.DAYS.between(teslimTarihi, bugun);
                bildirimGonder(kitap, "Geç Teslim Uyarısı", 
                    "Kitabınızın teslim tarihi " + gecenGun + " gün önce geçti! Lütfen iade edin.");
            }
        }
    }

    // Manuel bildirim gönderme
    public void bildirimGonder(Kitap kitap, String konu, String mesaj) {
        if (kitap.getOduncAlanId() == null) {
            return;
        }

        Kullanici kullanici = kullaniciRepository.findById(kitap.getOduncAlanId()).orElse(null);
        if (kullanici == null) {
            return;
        }

        // Gerçek email gönderimi için JavaMailSender kullanılabilir
        // Şimdilik console'a log yazıyoruz
        System.out.println("========================================");
        System.out.println("EMAIL BİLDİRİMİ");
        System.out.println("Alıcı: " + kullanici.getEmail());
        System.out.println("Konu: " + konu);
        System.out.println("Mesaj: " + mesaj);
        System.out.println("Kitap: " + kitap.getAd());
        System.out.println("========================================");

        // Gerçek email göndermek için:
        // 1. pom.xml'e spring-boot-starter-mail dependency ekle
        // 2. application.properties'e SMTP ayarları ekle
        // 3. JavaMailSender kullanarak email gönder
    }

    // Tüm kullanıcılara toplu bildirim gönder
    public void topluBildirimGonder(String konu, String mesaj) {
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();
        for (Kullanici kullanici : kullanicilar) {
            System.out.println("Bildirim gönderiliyor: " + kullanici.getEmail() + " - " + konu);
        }
    }
}

