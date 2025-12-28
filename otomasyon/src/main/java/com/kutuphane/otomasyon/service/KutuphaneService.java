package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.IslemLog;
import com.kutuphane.otomasyon.model.FavoriKitap;
import com.kutuphane.otomasyon.model.KitapDegerlendirme;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import com.kutuphane.otomasyon.repository.IslemLogRepository;
import com.kutuphane.otomasyon.repository.FavoriKitapRepository;
import com.kutuphane.otomasyon.repository.KitapDegerlendirmeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KutuphaneService {

    private final KitapRepository kitapRepository;
    private final KullaniciRepository kullaniciRepository;
    private final IslemLogRepository logRepository;
    private final FavoriKitapRepository favoriKitapRepository;
    private final KitapDegerlendirmeRepository degerlendirmeRepository;
    private com.kutuphane.otomasyon.service.EmailBildirimService emailBildirimService;

    public KutuphaneService(KitapRepository kitapRepository, KullaniciRepository kullaniciRepository, 
                           IslemLogRepository logRepository, FavoriKitapRepository favoriKitapRepository,
                           KitapDegerlendirmeRepository degerlendirmeRepository) {
        this.kitapRepository = kitapRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.logRepository = logRepository;
        this.favoriKitapRepository = favoriKitapRepository;
        this.degerlendirmeRepository = degerlendirmeRepository;
    }

    // Email bildirim servisini set et (constructor injection yerine setter injection)
    public void setEmailBildirimService(com.kutuphane.otomasyon.service.EmailBildirimService emailBildirimService) {
        this.emailBildirimService = emailBildirimService;
    }

    // --- GÜNCELLENEN GİRİŞ METODU (GARANTİ ÇÖZÜM) ---
    public Kullanici girisYap(String email, String sifre) {
        Kullanici bulunan = kullaniciRepository.findByEmailAndSifre(email, sifre).orElse(null);

        if (bulunan != null) {
            // HATA ÇÖZÜCÜ: Eğer veritabanından 'kullaniciTipi' boş gelirse veya
            // email 'admin' ise, rolü biz zorla 'PERSONEL' yapıyoruz.
            if (email.equals("admin@kutuphane.com")) {
                bulunan.setKullaniciTipi("PERSONEL");
            } 
            // Eğer tipi hala boşsa (normal üyeler için), varsayılan olarak 'UYE' yap
            else if (bulunan.getKullaniciTipi() == null) {
                bulunan.setKullaniciTipi("UYE");
            }
        }
        
        return bulunan;
    }
    // ----------------------------------------------------

    public Kullanici kullaniciEkle(Kullanici kullanici) {
        // Yeni kayıt olanlara varsayılan rol atayalım
        if(kullanici.getKullaniciTipi() == null) {
            kullanici.setKullaniciTipi("UYE");
        }
        return kullaniciRepository.save(kullanici);
    }

    public String sifreDegistir(Long kullaniciId, String eskiSifre, String yeniSifre) {
        Kullanici kullanici = kullaniciRepository.findById(kullaniciId).orElse(null);
        if (kullanici == null) {
            return "Kullanıcı bulunamadı";
        }
        if (!kullanici.getSifre().equals(eskiSifre)) {
            return "Eski şifre yanlış";
        }
        kullanici.setSifre(yeniSifre);
        kullaniciRepository.save(kullanici);
        return "Şifre başarıyla değiştirildi";
    }

    public List<Kitap> tumKitaplariGetir() { return kitapRepository.findAll(); }
    public Kitap kitapEkle(Kitap kitap) { return kitapRepository.save(kitap); }
    public String kitapSil(Long id) { kitapRepository.deleteById(id); return "Silindi"; }
    public List<Kitap> kullanicininKitaplariniGetir(Long id) { return kitapRepository.findByOduncAlanId(id); }
    
    public Kitap kitapGuncelle(Long id, Kitap guncelKitap) {
        Kitap mevcut = kitapRepository.findById(id).orElse(null);
        if (mevcut != null) {
            if (guncelKitap.getAd() != null) mevcut.setAd(guncelKitap.getAd());
            if (guncelKitap.getYazar() != null) mevcut.setYazar(guncelKitap.getYazar());
            if (guncelKitap.getIsbn() != null) mevcut.setIsbn(guncelKitap.getIsbn());
            if (guncelKitap.getResimUrl() != null) mevcut.setResimUrl(guncelKitap.getResimUrl());
            if (guncelKitap.getSonTeslimTarihi() != null) mevcut.setSonTeslimTarihi(guncelKitap.getSonTeslimTarihi());
            return kitapRepository.save(mevcut);
        }
        return null;
    }
    
    public String kitapOduncVer(Long kId, Long uId) {
        Kitap k = kitapRepository.findById(kId).orElse(null);
        Kullanici u = kullaniciRepository.findById(uId).orElse(null);
        if(k!=null && k.isMusaitMi() && u!=null) {
            k.setMusaitMi(false); k.setOduncAlanId(u.getId()); k.setOduncAlanAd(u.getAdSoyad());
            k.setSonTeslimTarihi(LocalDate.now().plusDays(15));
            kitapRepository.save(k);
            
            // Log kaydı oluştur
            IslemLog log = new IslemLog();
            log.setIslem(u.getAdSoyad() + " kitabı ödünç aldı");
            log.setKitapAd(k.getAd());
            log.setTarih(LocalDateTime.now());
            logRepository.save(log);
            
            return "Verildi";
        }
        return "Hata";
    }

    public String kitapIadeAl(Long kId) {
        Kitap k = kitapRepository.findById(kId).orElse(null);
        if(k!=null) {
            String oduncAlanAd = k.getOduncAlanAd();
            k.setMusaitMi(true); k.setOduncAlanId(null); k.setOduncAlanAd(null); k.setSonTeslimTarihi(null);
            kitapRepository.save(k);
            
            // Log kaydı oluştur
            IslemLog log = new IslemLog();
            log.setIslem((oduncAlanAd != null ? oduncAlanAd : "Bilinmeyen") + " kitabı iade etti");
            log.setKitapAd(k.getAd());
            log.setTarih(LocalDateTime.now());
            logRepository.save(log);
            
            return "Alındı";
        }
        return "Hata";
    }

    public Map<String, Object> istatistikleriGetir() {
        Map<String, Object> m = new HashMap<>();
        List<Kitap> ks = kitapRepository.findAll();
        m.put("toplamKitap", ks.size());
        m.put("toplamUye", kullaniciRepository.count());
        m.put("odunctekiKitap", ks.stream().filter(k->!k.isMusaitMi()).count());
        return m;
    }
    public List<Kullanici> tumKullanicilariGetir() { return kullaniciRepository.findAll(); }
    public Kullanici kullaniciGetir(Long id) { return kullaniciRepository.findById(id).orElse(null); }
    
    public Kullanici kullaniciGuncelle(Long id, Kullanici guncelKullanici) {
        Kullanici mevcut = kullaniciRepository.findById(id).orElse(null);
        if (mevcut != null) {
            if (guncelKullanici.getAdSoyad() != null) mevcut.setAdSoyad(guncelKullanici.getAdSoyad());
            if (guncelKullanici.getEmail() != null) mevcut.setEmail(guncelKullanici.getEmail());
            if (guncelKullanici.getKullaniciTipi() != null) mevcut.setKullaniciTipi(guncelKullanici.getKullaniciTipi());
            return kullaniciRepository.save(mevcut);
        }
        return null;
    }
    
    public String kullaniciSil(Long id) {
        // Admin kendini silemez
        Kullanici kullanici = kullaniciRepository.findById(id).orElse(null);
        if (kullanici == null) {
            return "Kullanıcı bulunamadı";
        }
        if (kullanici.getEmail() != null && kullanici.getEmail().equals("admin@kutuphane.com")) {
            return "Admin kullanıcısı silinemez";
        }
        
        // Kullanıcının elindeki kitapları iade et
        List<Kitap> elindekiKitaplar = kitapRepository.findByOduncAlanId(id);
        for (Kitap kitap : elindekiKitaplar) {
            kitap.setMusaitMi(true);
            kitap.setOduncAlanId(null);
            kitap.setOduncAlanAd(null);
            kitap.setSonTeslimTarihi(null);
            kitapRepository.save(kitap);
        }
        
        kullaniciRepository.deleteById(id);
        return "Kullanıcı silindi";
    }
    
    public List<IslemLog> kullaniciIslemGecmisi(Long kullaniciId) {
        Kullanici kullanici = kullaniciRepository.findById(kullaniciId).orElse(null);
        if (kullanici == null) {
            return java.util.Collections.emptyList();
        }
        // Kullanıcının adını içeren logları getir
        return logRepository.findAll().stream()
            .filter(log -> log.getIslem() != null && 
                (log.getIslem().contains(kullanici.getAdSoyad()) || 
                 log.getIslem().contains(kullanici.getEmail())))
            .sorted((a, b) -> b.getTarih().compareTo(a.getTarih()))
            .collect(java.util.stream.Collectors.toList());
    }

    // --- CEZA SİSTEMİ ---
    // Bir kitap için geç teslim cezasını hesapla (gün başına 5 TL)
    public double kitapCezaHesapla(Long kitapId) {
        Kitap k = kitapRepository.findById(kitapId).orElse(null);
        if (k == null || k.isMusaitMi() || k.getSonTeslimTarihi() == null) {
            return 0.0;
        }
        
        LocalDate bugun = LocalDate.now();
        LocalDate teslimTarihi = k.getSonTeslimTarihi();
        
        if (bugun.isAfter(teslimTarihi)) {
            long gecenGun = java.time.temporal.ChronoUnit.DAYS.between(teslimTarihi, bugun);
            return gecenGun * 5.0; // Her gün için 5 TL ceza
        }
        return 0.0;
    }

    // Kullanıcının toplam cezasını hesapla
    public double kullaniciToplamCeza(Long kullaniciId) {
        List<Kitap> kitaplar = kitapRepository.findByOduncAlanId(kullaniciId);
        double toplamCeza = 0.0;
        
        for (Kitap k : kitaplar) {
            toplamCeza += kitapCezaHesapla(k.getId());
        }
        
        return toplamCeza;
    }

    // Kullanıcının ceza detaylarını getir (hangi kitap, kaç gün geç, ne kadar ceza)
    public List<Map<String, Object>> kullaniciCezaDetaylari(Long kullaniciId) {
        List<Kitap> kitaplar = kitapRepository.findByOduncAlanId(kullaniciId);
        List<Map<String, Object>> detaylar = new java.util.ArrayList<>();
        
        LocalDate bugun = LocalDate.now();
        
        for (Kitap k : kitaplar) {
            if (k.getSonTeslimTarihi() != null && bugun.isAfter(k.getSonTeslimTarihi())) {
                long gecenGun = java.time.temporal.ChronoUnit.DAYS.between(k.getSonTeslimTarihi(), bugun);
                double ceza = gecenGun * 5.0;
                
                Map<String, Object> detay = new HashMap<>();
                detay.put("kitapId", k.getId());
                detay.put("kitapAd", k.getAd());
                detay.put("yazar", k.getYazar());
                detay.put("teslimTarihi", k.getSonTeslimTarihi().toString());
                detay.put("gecenGun", gecenGun);
                detay.put("ceza", ceza);
                detaylar.add(detay);
            }
        }
        
        return detaylar;
    }

    // --- FAVORİ KİTAPLAR ---
    public String favoriEkle(Long kullaniciId, Long kitapId) {
        if (favoriKitapRepository.findByKullaniciIdAndKitapId(kullaniciId, kitapId).isPresent()) {
            return "Bu kitap zaten favorilerinizde";
        }
        FavoriKitap favori = new FavoriKitap();
        favori.setKullaniciId(kullaniciId);
        favori.setKitapId(kitapId);
        favoriKitapRepository.save(favori);
        return "Favorilere eklendi";
    }

    public String favoriCikar(Long kullaniciId, Long kitapId) {
        favoriKitapRepository.deleteByKullaniciIdAndKitapId(kullaniciId, kitapId);
        return "Favorilerden çıkarıldı";
    }

    public List<Kitap> kullaniciFavorileri(Long kullaniciId) {
        List<FavoriKitap> favoriler = favoriKitapRepository.findByKullaniciId(kullaniciId);
        return favoriler.stream()
            .map(f -> kitapRepository.findById(f.getKitapId()).orElse(null))
            .filter(k -> k != null)
            .collect(java.util.stream.Collectors.toList());
    }

    // --- KİTAP DEĞERLENDİRME ---
    public KitapDegerlendirme degerlendirmeEkle(Long kullaniciId, Long kitapId, Integer yildiz, String yorum) {
        KitapDegerlendirme mevcut = degerlendirmeRepository.findByKullaniciIdAndKitapId(kullaniciId, kitapId);
        
        if (mevcut != null) {
            mevcut.setYildiz(yildiz);
            mevcut.setYorum(yorum);
            mevcut.setTarih(java.time.LocalDateTime.now());
        } else {
            mevcut = new KitapDegerlendirme();
            mevcut.setKullaniciId(kullaniciId);
            mevcut.setKitapId(kitapId);
            mevcut.setYildiz(yildiz);
            mevcut.setYorum(yorum);
            mevcut.setTarih(java.time.LocalDateTime.now());
        }
        
        degerlendirmeRepository.save(mevcut);
        
        // Kitabın ortalama değerlendirmesini güncelle
        kitapDegerlendirmeOrtalamasiGuncelle(kitapId);
        
        return mevcut;
    }

    private void kitapDegerlendirmeOrtalamasiGuncelle(Long kitapId) {
        List<KitapDegerlendirme> degerlendirmeler = degerlendirmeRepository.findByKitapId(kitapId);
        if (!degerlendirmeler.isEmpty()) {
            double ortalama = degerlendirmeler.stream()
                .mapToInt(KitapDegerlendirme::getYildiz)
                .average()
                .orElse(0.0);
            
            Kitap kitap = kitapRepository.findById(kitapId).orElse(null);
            if (kitap != null) {
                kitap.setDegerlendirmeOrtalamasi(ortalama);
                kitap.setDegerlendirmeSayisi(degerlendirmeler.size());
                kitapRepository.save(kitap);
            }
        }
    }

    public List<KitapDegerlendirme> kitapDegerlendirmeleri(Long kitapId) {
        return degerlendirmeRepository.findByKitapId(kitapId);
    }

    // --- İSTATİSTİKLER ---
    public List<java.util.Map<String, Object>> enCokOkunanKitaplar() {
        List<Kitap> kitaplar = kitapRepository.findAll();
        return kitaplar.stream()
            .filter(k -> k.getDegerlendirmeSayisi() != null && k.getDegerlendirmeSayisi() > 0)
            .sorted((a, b) -> Integer.compare(
                b.getDegerlendirmeSayisi() != null ? b.getDegerlendirmeSayisi() : 0,
                a.getDegerlendirmeSayisi() != null ? a.getDegerlendirmeSayisi() : 0
            ))
            .limit(10)
            .map(k -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("kitap", k);
                map.put("okunmaSayisi", k.getDegerlendirmeSayisi());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    public List<java.util.Map<String, Object>> enCokKitapOkuyanKullanicilar() {
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();
        return kullanicilar.stream()
            .map(k -> {
                List<IslemLog> loglar = logRepository.findAll().stream()
                    .filter(log -> log.getIslem() != null && 
                        (log.getIslem().contains(k.getAdSoyad()) || log.getIslem().contains(k.getEmail())))
                    .filter(log -> log.getIslem() != null && log.getIslem().contains("ödünç aldı"))
                    .collect(java.util.stream.Collectors.toList());
                
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("kullanici", k);
                map.put("okunanKitapSayisi", loglar.size());
                return map;
            })
            .sorted((a, b) -> Integer.compare(
                (Integer) b.get("okunanKitapSayisi"),
                (Integer) a.get("okunanKitapSayisi")
            ))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());
    }

    public Kitap kitapGetir(Long id) {
        return kitapRepository.findById(id).orElse(null);
    }
}