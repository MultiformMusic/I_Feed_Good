package net.multiform_music.rss.ifeedgood;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.webkit.WebView;

import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;

public class DescriptionNewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_news);
        setupWindowAnimations();

        // récupération des vues à afficher
        WebView newsDescription = (WebView) findViewById(R.id.webviewNewsDescription);

        // récupération des données à afficher
        Intent intent = getIntent();
        NewsBean newsBean = intent.getParcelableExtra(MainActivity.INTENT_NEWS_CONTENT);

        // affichage des données
        // positionnement des données dans template web
        String webTemplate = RssHelper.webNewsDecriptionTemplate;

        // test si image présente dans description de la news => si c'est le cas on ne met pas d'image en en-tête
        if (newsBean.getNewsDescription().contains("jpg")
                || newsBean.getNewsDescription().contains("jpeg")
                || newsBean.getNewsDescription().contains("JPEG")
                || newsBean.getNewsDescription().contains("JPG")
                || newsBean.getNewsDescription().contains("png")
                || newsBean.getNewsDescription().contains("PNG")) {
            webTemplate = webTemplate.replace("{0}", "");
        } else {
            webTemplate = webTemplate.replace("{0}", newsBean.getNewsImageUrl());
        }
        webTemplate = webTemplate.replace("{1}", newsBean.getNewsDescription());
        webTemplate = webTemplate.replace("{2}", newsBean.getNewsLink());
        webTemplate = webTemplate.replace("{4}", newsBean.getNewsDatePublication());
        webTemplate = webTemplate.replace("{5}", newsBean.getNewsNom());
        webTemplate = webTemplate.replace("{3}", getResources().getString(R.string.linkText));

        // chargement des données dans la Webview
        newsDescription.loadData(webTemplate, "text/html; charset=UTF-8", null);

        // titre de l'activité
        setTitle(newsBean.getNewsTitle());

    }

    private void setupWindowAnimations() {

        Fade fade = new Fade();
        fade.setDuration(2000);
        getWindow().setEnterTransition(fade);

    }

    /**
     * Gestion du clic sur icones/menus de l'actionbar
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case android.R.id.home:

                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.fade_in, R.anim.slide_bottom);

                return true;

            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();

        overridePendingTransition(R.anim.fade_in, R.anim.slide_bottom);
    }

}
