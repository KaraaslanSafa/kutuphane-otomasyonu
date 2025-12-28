package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.IslemLog;
import com.kutuphane.otomasyon.service.KutuphaneService;
import com.kutuphane.otomasyon.repository.IslemLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KutuphaneController {

    private final KutuphaneService service;
    private final IslemLogRepository logRepository;
    private final com.kutuphane.otomasyon.service.EmailBildirimService emailBildirimService;

    public KutuphaneController(KutuphaneService service, IslemLogRepository logRepository, 
                                com.kutuphane.otomasyon.service.EmailBildirimService emailBildirimService) {
        this.service = service;
        this.logRepository = logRepository;
        this.emailBildirimService = emailBildirimService;
        // Email bildirim servisini KutuphaneService'e bağla
        service.setEmailBildirimService(emailBildirimService);
    }

    // --- EKSİK OLAN GİRİŞ VE KAYIT ENDPOINTLERİ (GERİ EKLENDİ) ---
    @PostMapping("/giris")
    public Kullanici giris(@RequestBody Kullanici kullanici) {
        return service.girisYap(kullanici.getEmail(), kullanici.getSifre());
    }

    @PostMapping("/kayit")
    public Kullanici kayit(@RequestBody Kullanici kullanici) {
        return service.kullaniciEkle(kullanici);
    }
    // -------------------------------------------------------------

    @GetMapping("/kitaplar")
    public List<Kitap> tumKitaplariGetir() {
        return service.tumKitaplariGetir();
    }

    @PostMapping("/kitaplar")
    public Kitap kitapEkle(@RequestBody Kitap kitap) {
        return service.kitapEkle(kitap);
    }

    @DeleteMapping("/kitaplar/{id}")
    public String kitapSil(@PathVariable Long id) {
        return service.kitapSil(id);
    }

    @PutMapping("/kitaplar/{id}")
    public Kitap kitapGuncelle(@PathVariable Long id, @RequestBody java.util.Map<String, Object> veri) {
        try {
            Kitap kitap = new Kitap();
            if (veri.containsKey("ad")) kitap.setAd((String) veri.get("ad"));
            if (veri.containsKey("yazar")) kitap.setYazar((String) veri.get("yazar"));
            if (veri.containsKey("isbn")) kitap.setIsbn((String) veri.get("isbn"));
            if (veri.containsKey("resimUrl")) kitap.setResimUrl((String) veri.get("resimUrl"));
            if (veri.containsKey("sonTeslimTarihi")) {
                Object tarihObj = veri.get("sonTeslimTarihi");
                if (tarihObj != null) {
                    String tarihStr = tarihObj.toString();
                    if (!tarihStr.isEmpty()) {
                        // Tarih formatını düzelt (eğer T ile bitiyorsa kaldır)
                        if (tarihStr.contains("T")) {
                            tarihStr = tarihStr.split("T")[0];
                        }
                        kitap.setSonTeslimTarihi(java.time.LocalDate.parse(tarihStr));
                    }
                }
            }
            Kitap guncellenmis = service.kitapGuncelle(id, kitap);
            if (guncellenmis == null) {
                throw new RuntimeException("Kitap bulunamadı");
            }
            return guncellenmis;
        } catch (Exception e) {
            throw new RuntimeException("Tarih güncelleme hatası: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/kitaplar/kullanici/{kullaniciId}")
    public List<Kitap> kullanicininKitaplari(@PathVariable Long kullaniciId) {
        return service.kullanicininKitaplariniGetir(kullaniciId);
    }

    @PostMapping("/odunc/ver")
    public String kitapOduncVer(@RequestParam Long kitapId, @RequestParam Long kullaniciId) {
        return service.kitapOduncVer(kitapId, kullaniciId);
    }

    @PostMapping("/odunc/iade")
    public String kitapIadeAl(@RequestParam Long kitapId) {
        return service.kitapIadeAl(kitapId);
    }

    @GetMapping("/istatistik")
    public java.util.Map<String, Object> istatistikGetir() {
        return service.istatistikleriGetir();
    }

    @GetMapping("/kullanicilar")
    public List<Kullanici> kullanicilariListele() {
        return service.tumKullanicilariGetir();
    }

    @PutMapping("/kullanicilar/{id}")
    public Kullanici kullaniciGuncelle(@PathVariable Long id, @RequestBody Kullanici kullanici) {
        return service.kullaniciGuncelle(id, kullanici);
    }

    @DeleteMapping("/kullanicilar/{id}")
    public java.util.Map<String, String> kullaniciSil(@PathVariable Long id) {
        String sonuc = service.kullaniciSil(id);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("mesaj", sonuc);
        response.put("basarili", sonuc.contains("silindi") ? "true" : "false");
        return response;
    }

    @GetMapping("/loglar")
    public List<IslemLog> loglariGetir() {
        return logRepository.findAllByOrderByTarihDesc();
    }

    // --- CEZA SİSTEMİ ENDPOINT'LERİ ---
    @GetMapping("/ceza/kullanici/{kullaniciId}")
    public java.util.Map<String, Object> kullaniciCezaBilgisi(@PathVariable Long kullaniciId) {
        java.util.Map<String, Object> sonuc = new java.util.HashMap<>();
        double toplamCeza = service.kullaniciToplamCeza(kullaniciId);
        java.util.List<java.util.Map<String, Object>> detaylar = service.kullaniciCezaDetaylari(kullaniciId);
        
        sonuc.put("toplamCeza", toplamCeza);
        sonuc.put("cezaDetaylari", detaylar);
        sonuc.put("gecikmisKitapSayisi", detaylar.size());
        
        return sonuc;
    }

    @GetMapping("/ceza/kitap/{kitapId}")
    public java.util.Map<String, Object> kitapCezaBilgisi(@PathVariable Long kitapId) {
        java.util.Map<String, Object> sonuc = new java.util.HashMap<>();
        double ceza = service.kitapCezaHesapla(kitapId);
        sonuc.put("ceza", ceza);
        sonuc.put("gecikmeVar", ceza > 0);
        return sonuc;
    }

    // --- PROFİL VE ŞİFRE DEĞİŞTİRME ---
    @PostMapping("/sifre-degistir")
    public java.util.Map<String, String> sifreDegistir(@RequestBody java.util.Map<String, Object> veri) {
        Long kullaniciId = Long.parseLong(veri.get("kullaniciId").toString());
        String eskiSifre = (String) veri.get("eskiSifre");
        String yeniSifre = (String) veri.get("yeniSifre");
        String sonuc = service.sifreDegistir(kullaniciId, eskiSifre, yeniSifre);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("mesaj", sonuc);
        response.put("basarili", sonuc.contains("başarıyla") ? "true" : "false");
        return response;
    }

    @GetMapping("/kullanici/{id}")
    public Kullanici kullaniciGetir(@PathVariable Long id) {
        return service.kullaniciGetir(id);
    }

    // --- İŞLEM GEÇMİŞİ ---
    @GetMapping("/loglar/kullanici/{kullaniciId}")
    public List<IslemLog> kullaniciIslemGecmisi(@PathVariable Long kullaniciId) {
        return service.kullaniciIslemGecmisi(kullaniciId);
    }

    // --- EMAIL BİLDİRİMLERİ ---
    @PostMapping("/bildirim/gonder")
    public java.util.Map<String, String> bildirimGonder(@RequestBody java.util.Map<String, Object> veri) {
        String tip = (String) veri.get("tip");
        if ("teslimYaklasan".equals(tip)) {
            emailBildirimService.teslimTarihiYaklasanKitaplariKontrolEt();
        } else if ("toplu".equals(tip)) {
            String konu = (String) veri.get("konu");
            String mesaj = (String) veri.get("mesaj");
            emailBildirimService.topluBildirimGonder(konu, mesaj);
        }
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("mesaj", "Bildirimler gönderildi");
        return response;
    }

    // --- KİTAP DETAY ---
    @GetMapping("/kitaplar/{id}")
    public Kitap kitapDetay(@PathVariable Long id) {
        return service.kitapGetir(id);
    }

    // --- FAVORİ KİTAPLAR ---
    @PostMapping("/favori/ekle")
    public java.util.Map<String, String> favoriEkle(@RequestParam Long kullaniciId, @RequestParam Long kitapId) {
        String sonuc = service.favoriEkle(kullaniciId, kitapId);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("mesaj", sonuc);
        return response;
    }

    @DeleteMapping("/favori/cikar")
    public java.util.Map<String, String> favoriCikar(@RequestParam Long kullaniciId, @RequestParam Long kitapId) {
        String sonuc = service.favoriCikar(kullaniciId, kitapId);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("mesaj", sonuc);
        return response;
    }

    @GetMapping("/favori/kullanici/{kullaniciId}")
    public List<Kitap> kullaniciFavorileri(@PathVariable Long kullaniciId) {
        return service.kullaniciFavorileri(kullaniciId);
    }

    // --- KİTAP DEĞERLENDİRME ---
    @PostMapping("/degerlendirme/ekle")
    public com.kutuphane.otomasyon.model.KitapDegerlendirme degerlendirmeEkle(@RequestBody java.util.Map<String, Object> veri) {
        Long kullaniciId = Long.parseLong(veri.get("kullaniciId").toString());
        Long kitapId = Long.parseLong(veri.get("kitapId").toString());
        Integer yildiz = Integer.parseInt(veri.get("yildiz").toString());
        String yorum = (String) veri.get("yorum");
        return service.degerlendirmeEkle(kullaniciId, kitapId, yildiz, yorum);
    }

    @GetMapping("/degerlendirme/kitap/{kitapId}")
    public List<com.kutuphane.otomasyon.model.KitapDegerlendirme> kitapDegerlendirmeleri(@PathVariable Long kitapId) {
        return service.kitapDegerlendirmeleri(kitapId);
    }

    // --- İSTATİSTİKLER ---
    @GetMapping("/istatistik/en-cok-okunan-kitaplar")
    public List<java.util.Map<String, Object>> enCokOkunanKitaplar() {
        return service.enCokOkunanKitaplar();
    }

    @GetMapping("/istatistik/en-cok-kitap-okuyan-kullanicilar")
    public List<java.util.Map<String, Object>> enCokKitapOkuyanKullanicilar() {
        return service.enCokKitapOkuyanKullanicilar();
    }
    
}