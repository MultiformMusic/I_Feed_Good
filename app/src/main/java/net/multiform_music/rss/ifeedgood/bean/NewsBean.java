package net.multiform_music.rss.ifeedgood.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by michel.dio on 27/03/2017.
 *
 */

public class NewsBean implements Parcelable, Comparable<NewsBean> {

    private String newsTitle;
    private String newsDescription;
    private String newsDatePublication;
    private String newsLink;
    private String newsImageUrl;
    private String newsCategorie;
    private String newsNom;
    private String newsBodyColor;
    private String newsFavourite = "hidden";
    private String newsSaved = "none";

    public NewsBean() {

    }

    public NewsBean(String title, String description, String url, String datePublication, String imageUrl, String categorie, String newsFavourite, String newsSaved, String nom) {

        this.newsTitle = title;
        this.newsDescription = description;
        this.newsLink = url;
        this.newsDatePublication = datePublication;
        this.newsImageUrl = imageUrl;
        this.newsCategorie = categorie;
        this.newsFavourite = newsFavourite;
        this.newsSaved = newsSaved;
        this.newsNom = nom;
    }

    private NewsBean(Parcel in) {
        newsTitle = in.readString();
        newsDescription = in.readString();
        newsLink = in.readString();
        newsDatePublication = in.readString();
        newsImageUrl = in.readString();
        newsCategorie = in.readString();
        newsFavourite = in.readString();
        newsSaved = in.readString();
        newsNom = in.readString();
    }

    public static final Creator<NewsBean> CREATOR = new Creator<NewsBean>() {
        @Override
        public NewsBean createFromParcel(Parcel in) {
            return new NewsBean(in);
        }

        @Override
        public NewsBean[] newArray(int size) {
            return new NewsBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(newsTitle);
        parcel.writeString(newsDescription);
        parcel.writeString(newsLink);
        parcel.writeString(newsDatePublication);
        parcel.writeString(newsImageUrl);
        parcel.writeString(newsCategorie);
        parcel.writeString(newsFavourite);
        parcel.writeString(newsSaved);
        parcel.writeString(newsNom);
    }

    public int compareTo(@NonNull NewsBean newsBean) {
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        int compare = 0;
        try {
            compare = sdf.parse(newsBean.newsDatePublication).compareTo(sdf.parse(this.newsDatePublication));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return compare;
    }


    public String getNewsSaved() {
        return newsSaved;
    }

    public void setNewsSaved(String newsSaved) {
        this.newsSaved = newsSaved;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsDescription() {
        return newsDescription;
    }

    public void setNewsDescription(String newsDescription) {
        this.newsDescription = newsDescription;
    }

    public String getNewsDatePublication() {
        return newsDatePublication;
    }

    public void setNewsDatePublication(String newsDatePublication) {
        this.newsDatePublication = newsDatePublication;
    }

    public String getNewsLink() {
        return newsLink;
    }

    public void setNewsLink(String newsLink) {
        this.newsLink = newsLink;
    }

    public String getNewsImageUrl() {
        return newsImageUrl;
    }

    public void setNewsImageUrl(String newsImageUrl) {
        this.newsImageUrl = newsImageUrl;
    }

    public String getNewsCategorie() {
        return newsCategorie;
    }

    public void setNewsCategorie(String newsCategorie) {
        this.newsCategorie = newsCategorie;
    }

    public String getNewsBodyColor() {
        return newsBodyColor;
    }

    public void setNewsBodyColor(String newsBodyColor) {
        this.newsBodyColor = newsBodyColor;
    }

    public String getNewsFavourite() {
        return newsFavourite;
    }

    public void setNewsFavourite(String newsFavourite) {
        this.newsFavourite = newsFavourite;
    }

    public String getNewsNom() {
        return newsNom;
    }


}
