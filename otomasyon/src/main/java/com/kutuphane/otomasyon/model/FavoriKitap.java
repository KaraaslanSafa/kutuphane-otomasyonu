package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "favori_kitaplar")
@Data
public class FavoriKitap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long kullaniciId;
    private Long kitapId;
}

