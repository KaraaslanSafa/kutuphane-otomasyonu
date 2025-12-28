package com.kutuphane.otomasyon.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("UYE")
public class Uye extends Kullanici {
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 3;
    }
}