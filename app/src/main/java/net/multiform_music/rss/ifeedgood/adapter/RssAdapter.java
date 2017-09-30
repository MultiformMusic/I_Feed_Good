package net.multiform_music.rss.ifeedgood.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Spinner;

import net.multiform_music.rss.ifeedgood.R;

import net.multiform_music.rss.ifeedgood.MainActivity;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.helper.DatabaseHelper;
import net.multiform_music.rss.ifeedgood.holder.RssViewHolder;
import net.multiform_music.rss.ifeedgood.interfaces.DocumentConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michel on 26/03/2017.
 *
 */

public class RssAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DocumentConsumer {

    private MainActivity context;

    public int countItem = 0;

    // liste qui va contenir les newsBean des Document chargés
    private List<NewsBean> newsBeanList = Collections.synchronizedList(new ArrayList<NewsBean>());

    // liste qui va contenir la liste réelle des news à afficher
    private List<NewsBean> currentNewsBeanList = Collections.synchronizedList(new ArrayList<NewsBean>());

    private HashMap<String, List<NewsBean>> categoryNewsMap;
    private HashMap<String, List<String>> categoryFeedMap;
    private List<String> categoryList;

    // nombre de AsyncTask lancées
    private int nbrAsyncTaks = 0;
    private int nbrAsyncTaksFinished = 0;

    private Spinner spinnerCustom;
    private Spinner spinnerFeeds;
    private String categorySelected = "";
    private String feedSelected = "";
    private Integer itemMenuIdCliked = null;

    private DatabaseHelper db;
    private RecyclerView mRecyclerView;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_news, parent, false);
        RssViewHolder rssViewHolder = new RssViewHolder(view);
        rssViewHolder.setRssAdapter(this);
        return rssViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        RssViewHolder rssViewHolder = (RssViewHolder) holder;

        // n'affiche pas la liste favoris
        rssViewHolder.setCurrentElement(currentNewsBeanList.get(position));
        Animation animation;
        if (position % 2 == 0 ) {
            animation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in_card);
            //animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        } else {
            animation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in_card);
            //animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        }
        rssViewHolder.webViewNews.startAnimation(animation);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {

        int count;
        countItem++;

        count = currentNewsBeanList.size();

        return count;
    }

    /**
     * Permet de changer la liste des bean de l'adapter, de notifier le changement pour rafraichissement
     * et revenir à la première position pour l'affichage
     *
     * @param currentNewsBeanList (nouvelle liste à afficher)
     *
     */
    public void changeCurrentNewsBeanList(List<NewsBean> currentNewsBeanList) {

        this.currentNewsBeanList = currentNewsBeanList;
        notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);

    }

    public List<NewsBean> getFilteredNewsBeanList() {

        return context.getFilteredNewsBeanList();
    }

    public void initCustomSpinner() {

        context.initCustomSpinner();
    }

    public void reactivateIconRefreh() {

        context.reactivateIconRefreh();
    }

    public List<NewsBean> getNewsBeanList() {
        return newsBeanList;
    }

    public void setNewsBeanList(List<NewsBean> newsBeanList) {
        this.newsBeanList = newsBeanList;
    }

    public Context getContext() {
        return context;
    }

    public List<NewsBean> getCurrentNewsBeanList() {
        return currentNewsBeanList;
    }

    public void setCurrentNewsBeanList(List<NewsBean> currentNewsBeanList) {
        this.currentNewsBeanList = currentNewsBeanList;
    }

    public int getNbrAsyncTaks() {
        return nbrAsyncTaks;
    }

    public void setNbrAsyncTaks(int nbrAsyncTaks) {
        this.nbrAsyncTaks = nbrAsyncTaks;
    }

    public int getNbrAsyncTaksFinished() {
        return nbrAsyncTaksFinished;
    }

    public void setNbrAsyncTaksFinished(int nbrAsyncTaksFinished) {
        this.nbrAsyncTaksFinished = nbrAsyncTaksFinished;
    }

    public HashMap<String, List<NewsBean>> getCategoryNewsMap() {
        return categoryNewsMap;
    }

    public void setCategoryNewsMap(HashMap<String, List<NewsBean>> categoryNewsMap) {
        this.categoryNewsMap = categoryNewsMap;
    }

    public List<String> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<String> categoryList) {
        this.categoryList = categoryList;
    }

    public Spinner getSpinnerCustom() {
        return spinnerCustom;
    }

    public void setSpinnerCustom(Spinner spinnerCustom) {
        this.spinnerCustom = spinnerCustom;
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }

    public Integer getItemMenuIdCliked() {
        return itemMenuIdCliked;
    }

    public void setItemMenuIdCliked(Integer itemMenuIdCliked) {
        this.itemMenuIdCliked = itemMenuIdCliked;
    }

    public String getCategorySelected() {
        return categorySelected;
    }

    public void setCategorySelected(String categorySelected) {
        this.categorySelected = categorySelected;
    }

    public DatabaseHelper getDb() {
        return db;
    }

    public void setDb(DatabaseHelper db) {
        this.db = db;
    }


    public HashMap<String, List<String>> getCategoryFeedMap() {
        return categoryFeedMap;
    }

    public void setCategoryFeedMap(HashMap<String, List<String>> categoryFeedMap) {
        this.categoryFeedMap = categoryFeedMap;
    }


    public Spinner getSpinnerFeeds() {
        return spinnerFeeds;
    }

    public void setSpinnerFeeds(Spinner spinnerFeeds) {
        this.spinnerFeeds = spinnerFeeds;
    }


    public String getFeedSelected() {
        return feedSelected;
    }

    public void setFeedSelected(String feedSelected) {
        this.feedSelected = feedSelected;
    }

}
