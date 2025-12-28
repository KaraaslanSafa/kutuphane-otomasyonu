package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "islem_loglari")
@Data
public class IslemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String islem;
    private String kitapAd;
    private LocalDateTime tarih;
}