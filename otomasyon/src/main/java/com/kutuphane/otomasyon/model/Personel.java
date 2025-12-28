package com.kutuphane.otomasyon.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PERSONEL")
public class Personel extends Kullanici {
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 5;
    }
}