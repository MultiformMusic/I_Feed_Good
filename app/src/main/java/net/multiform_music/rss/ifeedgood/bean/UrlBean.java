package net.multiform_music.rss.ifeedgood.bean;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;

/**
 * Created by michel.dio on 18/04/2017.
 *
 */

public class UrlBean implements Comparable<UrlBean> {

    private String url;
    // url pour affichage : peut être tronquée si trop longue
    private String urlAffichageList;
    private String nom;
    private boolean active;
    private String categorie;
    private String dateAjoutModification;


    public int compareTo(@NonNull UrlBean urlBean) {
        DateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        int compare = 0;
        try {
            compare = sdf.parse(urlBean.dateAjoutModification).compareTo(sdf.parse(this.dateAjoutModification));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return compare;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlAffichageList() {
        return urlAffichageList;
    }

    public void setUrlAffichageList(String urlAffichageList) {
        this.urlAffichageList = urlAffichageList;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setDateAjoutModification(String dateAjoutModification) {
        this.dateAjoutModification = dateAjoutModification;
    }
}
