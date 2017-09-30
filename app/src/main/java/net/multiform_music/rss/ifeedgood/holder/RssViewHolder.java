package net.multiform_music.rss.ifeedgood.holder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import net.multiform_music.rss.ifeedgood.R;

import net.multiform_music.rss.ifeedgood.DescriptionNewsActivity;
import net.multiform_music.rss.ifeedgood.MainActivity;
import net.multiform_music.rss.ifeedgood.adapter.RssAdapter;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.helper.MyPopupMenu;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michel on 26/03/2017.
 *
 */

public class RssViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnLongClickListener {

    private String currentTitle;
    private String currentDescription;
    private String currentLink;
    private String currentDatePublication;
    private String currentImageUrl = "";
    private String currentCategorie;
    private String currentNom = "";
    private String currentBodyColor = "";
    private String currentFavourite = "";
    private String currentSaved = "";

    public WebView webViewNews = null;

    // gestion du clicklistener pour une webview
    private boolean moveOccured;
    private float downPosX;
    private float downPosY;

    public void setRssAdapter(RssAdapter rssAdapter) {
        this.rssAdapter = rssAdapter;
    }

    private RssAdapter rssAdapter;


    public RssViewHolder(View itemView) {

        super(itemView);

        // récupération Webview
        webViewNews = (WebView) itemView.findViewById(R.id.webviewNews);
        webViewNews.getSettings().setJavaScriptEnabled(true);

        /*
        /*listener click sur Webview news => lancer Intent vers activit&eacute; d'affichage du contenu de la news (WebView)
        */
        View.OnTouchListener onTouchListenerWebNews = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float MOVE_THRESHOLD_DP = RssHelper.moveThresholdDp;

                final int action = event.getAction();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:

                        moveOccured = false;
                        downPosX = event.getX();
                        downPosY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:

                        if (!moveOccured) {

                            // création de l'intent vers la page description de la news
                            Intent intent = new Intent(v.getContext(), DescriptionNewsActivity.class);
                            NewsBean newsBean = new NewsBean(currentTitle, currentDescription, currentLink, currentDatePublication, currentImageUrl, currentCategorie, currentFavourite, currentSaved, currentNom);
                            newsBean.setNewsBodyColor(RssHelper.BODY_COLOR_SELECT);
                            intent.putExtra(MainActivity.INTENT_NEWS_CONTENT, newsBean);

                            // changement de couleur de la webview de la news cliquée
                            WebView webView = (WebView) v;
                            currentBodyColor = RssHelper.BODY_COLOR_SELECT;
                            webView.loadUrl("javascript:changeNewsBodyColor('" + currentBodyColor + "')");

                            // parcours de la liste des news pour positionner la couleeur cliqué : important pour
                            // retrouver la couleur des news cliquées quand on revient en arrière
                            for (int i = 0; i < rssAdapter.getNewsBeanList().size(); i++) {
                                if (rssAdapter.getNewsBeanList().get(i).getNewsTitle().equalsIgnoreCase(currentTitle)) {
                                    rssAdapter.getNewsBeanList().get(i).setNewsBodyColor(currentBodyColor);
                                }
                            }

                            // avant de passer vers l'activité suivante on flag que la news a été cliqué
                            // cette liste est sauvé dans SharedPreference pour retrouver l'état quand on quitte/revient sur l'appli
                            if (!RssHelper.listNewsTitleAlreadyReaded.contains(currentTitle)) {
                                RssHelper.listNewsTitleAlreadyReaded.add(currentTitle);
                            }

                            v.getContext().startActivity(intent);
                            ((Activity) v.getContext()).overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                            ((Activity) v.getContext()).getWindow().setAllowEnterTransitionOverlap(true);

                        }
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (Math.abs(event.getX() - downPosX) > MOVE_THRESHOLD_DP || Math.abs(event.getY() - downPosY) > MOVE_THRESHOLD_DP) {
                            moveOccured = true;
                        }
                        break;
                }
                return false;
            }
        };
        webViewNews.setOnTouchListener(onTouchListenerWebNews);
        webViewNews.setOnLongClickListener(this);

    }


    public void setCurrentElement(NewsBean newsBean) {

        this.currentTitle = newsBean.getNewsTitle();
        this.currentDescription = newsBean.getNewsDescription();
        this.currentLink = newsBean.getNewsLink();
        this.currentDatePublication = newsBean.getNewsDatePublication();
        this.currentImageUrl = newsBean.getNewsImageUrl();
        this.currentCategorie = newsBean.getNewsCategorie();
        this.currentBodyColor = (newsBean.getNewsBodyColor() == null ? "body_normal" : newsBean.getNewsBodyColor());
        this.currentFavourite = newsBean.getNewsFavourite();
        this.currentSaved = newsBean.getNewsSaved();
        this.currentNom = newsBean.getNewsNom();

        // positionnement des données dans template web
        String webTemplate = RssHelper.webNewsTemplate;
        webTemplate = webTemplate.replace("{0}", currentTitle);
        webTemplate = webTemplate.replace("{1}", currentImageUrl);
        webTemplate = webTemplate.replace("{2}", currentDatePublication);
        webTemplate = webTemplate.replace("{3}", currentCategorie);
        webTemplate = webTemplate.replace("class='" + RssHelper.BODY_COLOR_NORMAL + "'", "class='" + currentBodyColor + "'");
        webTemplate = webTemplate.replace("{4}", (currentFavourite.equalsIgnoreCase("visible") ? "visible" : "hidden"));
        webTemplate = webTemplate.replace("{5}", currentSaved);
        if (currentNom != null) {
            webTemplate = webTemplate.replace("{6}", currentNom);
        }
        webTemplate = webTemplate.replace("{7}", RssHelper.name);
        webTemplate = webTemplate.replace("{8}", RssHelper.category);
        webTemplate = webTemplate.replace("{9}", RssHelper.publicationDate);

        // chargement des données dans la Webview
        webViewNews.loadDataWithBaseURL("file:///android_asset/", webTemplate, "text/html; charset=UTF-8", "UTF-8", null);

    }


    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        contextMenu.setHeaderTitle("Select The Action");
        contextMenu.add(0, view.getId(), 0, "Call");//groupId, itemId, order, title
        contextMenu.add(0, view.getId(), 0, "SMS");
    }

    @Override
    public boolean onLongClick(View view) {

        //creating a popup menu
        final MyPopupMenu popup = new MyPopupMenu(view.getContext(), this.webViewNews);

        //inflating menu from xml resource
        popup.inflate(R.menu.context_menu_news);

        // noinspection RestrictedApi
        Context wrapper = new ContextThemeWrapper(view.getContext(), R.style.popupMenuStyle);
        // noinspection RestrictedApi
        MenuPopupHelper menuHelper = new MenuPopupHelper(wrapper, (MenuBuilder) popup.getMenu(), webViewNews);
        // noinspection RestrictedApi
        menuHelper.setForceShowIcon(true);
        // noinspection RestrictedApi
        menuHelper.show();

        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //List<NewsBean> newsBeanList = rssAdapter.getNewsBeanList();
                List<NewsBean> newsBeanList = rssAdapter.getCurrentNewsBeanList();
                ArrayList<String> listNewsTitleAlreadyReaded = RssHelper.listNewsTitleAlreadyReaded;
                ArrayList<String> listNewsFavourites = RssHelper.listNewsTitleFavourite;
                ArrayList<String> listNewsSaved = RssHelper.listNewsTitleSaved;
                WebView webview = (WebView) popup.getView();

                MainActivity mainActivity = (MainActivity) rssAdapter.getContext();

                switch (item.getItemId()) {

                    case R.id.mark_readed:

                        Log.i("onLongClick", "***** onLongClick mark_readed");

                        // changement de la couleur de la vue
                        webview.loadUrl("javascript:changeNewsBodyColor('" + RssHelper.BODY_COLOR_SELECT +"')");

                        // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                        for (NewsBean news : newsBeanList) {
                            if (news.getNewsTitle().equals(currentTitle)) {
                                news.setNewsBodyColor(RssHelper.BODY_COLOR_SELECT);
                            }
                        }

                        // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                        if (!listNewsTitleAlreadyReaded.contains(currentTitle)) {
                            listNewsTitleAlreadyReaded.add(currentTitle);
                        }

                        // on remet la currect liste dans l'adapter qui va notifier le changement
                        rssAdapter.changeCurrentNewsBeanList(mainActivity.getFilteredNewsBeanList());

                        break;

                    case R.id.mark_nonreaded:

                        Log.i("onLongClick", "***** onLongClick mark_nonreaded");

                        // changement de la couleur de la vue
                        webview.loadUrl("javascript:changeNewsBodyColor('" + RssHelper.BODY_COLOR_NORMAL +"')");

                        // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                        for (NewsBean news : newsBeanList) {
                            if (news.getNewsTitle().equals(currentTitle)) {
                                news.setNewsBodyColor(RssHelper.BODY_COLOR_NORMAL);
                            }
                        }

                        // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                        if (listNewsTitleAlreadyReaded.contains(currentTitle)) {
                            listNewsTitleAlreadyReaded.remove(currentTitle);
                        }

                        break;

                    case R.id.mark_mail:

                        NewsBean newsToShare = new NewsBean(currentTitle,currentDescription, currentLink,currentDatePublication,currentImageUrl,currentCategorie, currentFavourite, currentSaved, currentNom);
                        RssHelper.getMailReceiver(webview, newsToShare);

                        break;

                    case R.id.mark_favourite:

                        // changement = on passe à favori
                        webview.loadUrl("javascript:changeFavouriteState('visible')");

                        // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                        if (!listNewsFavourites.contains(currentTitle)) {
                            listNewsFavourites.add(currentTitle);
                        }

                        // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                        for (NewsBean news : newsBeanList) {
                            if (news.getNewsTitle().equals(currentTitle)) {
                                news.setNewsFavourite("visible");
                            }
                        }

                        // les news est en base => on change l'état favori en base
                        if (currentSaved.equalsIgnoreCase("block")) {

                            rssAdapter.getDb().changeFavouriteNewsState(true, currentTitle);
                        }

                        break;

                    case R.id.mark_unfavourite:

                        // changement = on passe à non favori
                        webview.loadUrl("javascript:changeFavouriteState('hidden')");

                        // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                        if (listNewsFavourites.contains(currentTitle)) {
                            listNewsFavourites.remove(currentTitle);
                        }

                        // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                        for (NewsBean news : newsBeanList) {
                            if (news.getNewsTitle().equals(currentTitle)) {
                                news.setNewsFavourite("hidden");
                            }
                        }

                        // les news est en base => on change l'état favori en base
                        if (currentSaved.equalsIgnoreCase("block")) {

                            rssAdapter.getDb().changeFavouriteNewsState(false, currentTitle);
                        }

                        rssAdapter.setItemMenuIdCliked(R.id.action_favourite);

                        // on remet la currect liste dans l'adapter qui va notifier le changement
                        rssAdapter.changeCurrentNewsBeanList(mainActivity.getFilteredNewsBeanList());

                        break;

                    case R.id.mark_save:

                        // vérification si article déjà en base
                        boolean exitsSavedNews = rssAdapter.getDb().newsExits(currentTitle);
                        if (exitsSavedNews) {


                            RssHelper.afficheToastError((Activity)rssAdapter.getContext(), rssAdapter.getContext().getString(R.string.context_already_save_toast));

                        } else {

                            rssAdapter.getDb().addNews(currentTitle, currentDescription, currentDatePublication, currentLink, currentImageUrl, currentCategorie, currentBodyColor, currentFavourite);

                            // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                            if (!listNewsSaved.contains(currentTitle)) {
                                listNewsSaved.add(currentTitle);
                            }

                            // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                            for (NewsBean news : newsBeanList) {
                                if (news.getNewsTitle().equals(currentTitle)) {
                                    news.setNewsSaved("block");
                                }
                            }

                            // changement = on passe à favori
                            webview.loadUrl("javascript:changeSavedState('block')");
                            currentSaved = "O";

                            RssHelper.afficheToastOk((Activity)rssAdapter.getContext(), rssAdapter.getContext().getString(R.string.context_save_toast));

                        }

                        break;

                    case R.id.mark_delete:

                        // vérification si article déjà en base
                        boolean exitsDeletedNews = rssAdapter.getDb().newsExits(currentTitle);

                        // vérifie si news existe avant suppression
                        if (!exitsDeletedNews) {

                            RssHelper.afficheToastError((Activity)rssAdapter.getContext(), rssAdapter.getContext().getString(R.string.context_already_delete_toast));

                        } else {

                            boolean deleteOK = rssAdapter.getDb().deleteNews(currentTitle);

                            if (!deleteOK) {

                                RssHelper.afficheToastError((Activity)rssAdapter.getContext(), rssAdapter.getContext().getString(R.string.context_nok_delete_toast));

                            } else {

                                currentSaved = "N";

                                // changement = on n'affiche plus
                                webview.loadUrl("javascript:changeSavedState('none')");

                                // parcours de la liste des news déjà lues de l'adapter pour mettre à jour
                                if (listNewsSaved.contains(currentTitle)) {
                                    listNewsSaved.remove(currentTitle);
                                }

                                // parcours de la liste des news de l'adapter pour positionner la bonne couleur au bean modifié
                                for (NewsBean news : newsBeanList) {
                                    if (news.getNewsTitle().equals(currentTitle)) {
                                        news.setNewsSaved("none");
                                    }
                                }

                                RssHelper.afficheToastOk((Activity)rssAdapter.getContext(), rssAdapter.getContext().getString(R.string.context_delete_toast));

                                // on remet la currect liste dans l'adapter qui va notifier le changement
                                rssAdapter.changeCurrentNewsBeanList(mainActivity.getFilteredNewsBeanList());

                            }
                        }

                        break;
                }
                return false;
            }
        });

        moveOccured = true;

        return false;

    }

}
