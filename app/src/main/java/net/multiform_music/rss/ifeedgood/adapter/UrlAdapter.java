package net.multiform_music.rss.ifeedgood.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.multiform_music.rss.ifeedgood.R;
import net.multiform_music.rss.ifeedgood.RssConfigActivity;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;
import net.multiform_music.rss.ifeedgood.holder.UrlViewHolder;

import java.util.List;

/**
 * Created by michel.dio on 18/04/2017.
 *
 */

public class UrlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RssConfigActivity context;

    private List<UrlBean> urlBeanList;

    private String yes;
    private String no;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_url, parent, false);
        UrlViewHolder urlViewHolder = new UrlViewHolder(view);
        urlViewHolder.setUrlAdapter(this);
        return urlViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UrlViewHolder urlViewHolder = (UrlViewHolder) holder;
        urlViewHolder.getNomTextview().setText(urlBeanList.get(position).getNom());
        urlViewHolder.getUrlTextview().setText(urlBeanList.get(position).getUrlAffichageList());
        // positionnement valeur url originale dans champ caché
        urlViewHolder.getUrlHiddenTextview().setText(urlBeanList.get(position).getUrl());
        urlViewHolder.getCategoryTextview().setText(urlBeanList.get(position).getCategorie());

        String actif = (urlBeanList.get(position).isActive() ? yes : no);
        urlViewHolder.getActiveTextview().setText(actif);
        urlViewHolder.getSwitchActif().setChecked(urlBeanList.get(position).isActive());
    }


    @Override
    public int getItemCount() {
        return urlBeanList.size();
    }

    public void setUrlBeanList(List<UrlBean> urlBeanList) {
        this.urlBeanList = urlBeanList;
    }

    public String getYes() {
        return yes;
    }

    public void setYes(String yes) {
        this.yes = yes;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public RssConfigActivity getContext() {
        return context;
    }

    public void setContext(RssConfigActivity context) {
        this.context = context;
    }
}
