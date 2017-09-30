package net.multiform_music.rss.ifeedgood;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerCategoryAdapter;
import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerFeedAdapter;
import net.multiform_music.rss.ifeedgood.adapter.RssAdapter;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;
import net.multiform_music.rss.ifeedgood.helper.DatabaseHelper;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;
import net.multiform_music.rss.ifeedgood.task.XMLAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public final static String INTENT_NEWS_CONTENT = "com.oc.rss_act.intent.news.content";
    public final static String INTENT_RSS_CONFIG = "com.oc.rss_act.intent.rss.config";


    // Adapter gérant les news
    private RssAdapter rssAdapter;

    // tache de lecture flux RSS XML
    private XMLAsyncTask xmlAsyncTask;

    // barre de progression
    private ProgressBar progress;

    // layout pour la rafraichissement par slide vertical
    private SwipeRefreshLayout swipeLayout;

    // Spinner liste déroulante catégory
    private Spinner spinnerCustom;

    // le menu de la barre d'action
    private Menu menu;

    // le Help DB
    private DatabaseHelper db;

    //
    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        setTitle(getResources().getString(R.string.listNews));

        // initialisation du Helper (utilise un fichier properties dans assets
        // chargement template html pour une news de la liste
        // détermination des url rss à utiliser
        RssHelper.init(this);

        // initialisation de la base
        db = new DatabaseHelper(this);

        //*** définition du SwipeRefreshLayout ***
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeLayout.setDistanceToTriggerSync(400);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // Refresh items
                Log.i("onRefresh", "***** OnRefreshListener onRefresh");

                swipeLayout.setRefreshing(true);
                refreshListNews();
            }
        });

        //*** définition de la RecyclerView ***
        final RecyclerView rv = (RecyclerView) findViewById(R.id.listNews);
        rv.setLayoutManager(new LinearLayoutManager(this));

        //*** définition de l'Adapter ***
        // récupération des titres des news déjà lues dans les préférences
        rssAdapter =  new RssAdapter();
        rssAdapter.setContext(this);
        rssAdapter.setDb(db);

        // récupération des titres des news déjà lues dans les préférences
        Set<String> listSet = PreferenceManager.getDefaultSharedPreferences(this).getStringSet(RssHelper.LIST_SET_NEWS_ALREADY_READED_PREF, new HashSet<String>());
        RssHelper.listNewsTitleAlreadyReaded = new ArrayList<>(listSet);

        // récupération des titres favoris dans les préférences
        listSet = PreferenceManager.getDefaultSharedPreferences(this).getStringSet(RssHelper.LIST_SET_NEWS_FAVOURITES, new HashSet<String>());
        RssHelper.listNewsTitleFavourite = new ArrayList<>(listSet);

        // récupération des titres sauvegardés dans DB
        RssHelper.listNewsTitleSaved = (ArrayList<String>) db.getAllNewsTitle();

        rv.setAdapter(rssAdapter);

        // l'observer permet de savoir quand les datas ont été chargées et donc de supprimer les animations de chargement
        rssAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                // si assez de données chargées on supprime les animations si nécessaire
                if (rssAdapter.countItem > 1) {
                    progress.setVisibility(View.GONE);
                    swipeLayout.setRefreshing(false);
                }
            }
        });

        // barrre de progression
        progress = (ProgressBar) findViewById(R.id.loadRssProgress);
        progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_IN);

        //Android Custom Spinner Example Programmatically
        spinnerCustom = (Spinner) findViewById(R.id.spinnerCategories);
        rssAdapter.setSpinnerCustom(spinnerCustom);
        Spinner spinnerFeeds = (Spinner) findViewById(R.id.spinnerFeeds);
        rssAdapter.setSpinnerFeeds(spinnerFeeds);

        // pour chaque url rss on charge les news -- THREAD_POOL_EXECUTOR permet une exécution en parallèle des tasks
        List<UrlBean> urlBeanList = db.getAllUrlBeanRssActive();
        // si des url ont été paramètrées on les charges
        if (urlBeanList != null && urlBeanList.size() > 0) {

            if (RssHelper.isNetworkOk(this)) {

                for (UrlBean urlBean : urlBeanList) {

                    xmlAsyncTask = new XMLAsyncTask(rssAdapter);
                    xmlAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlBean);

                    rssAdapter.setNbrAsyncTaks(rssAdapter.getNbrAsyncTaks() + 1);

                }
            } else {
                progress.setVisibility(View.GONE);
                Toast toast = Toast.makeText(getApplicationContext(), rssAdapter.getContext().getString(R.string.NetworkError), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
            // cas intial : pas d'url actives
        } else {

            // recherche en base si des news existent.
            List<NewsBean> newsSaved = db.getAllNews();

            // si des news sauvegardées existent on met à jour l'adapteur et on affiche
            if (newsSaved != null && newsSaved.size() > 0) {
                // tri de la liste des news par date décroissante
                rssAdapter.setCurrentNewsBeanList(newsSaved);
                Collections.sort(rssAdapter.getCurrentNewsBeanList());
                rssAdapter.setNewsBeanList(newsSaved);
                Collections.sort(rssAdapter.getNewsBeanList());

                RssHelper.constructMapCategory(rssAdapter);

                // on supprime les animations si nécessaire
                progress.setVisibility(View.GONE);
                swipeLayout.setRefreshing(false);

                // si aucune news => on va vers la config des URL RSS
            } else {

                // on va lancer un intent vers l'activité de gestion config des url rss
                /*Intent intent = new Intent(getApplicationContext(), RssConfigActivity.class);
                intent.putExtra(INTENT_RSS_CONFIG, getResources().getString(R.string.configure_url));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_top, R.anim.slide_bottom);
                getWindow().setAllowEnterTransitionOverlap(true);*/
                progress.setVisibility(View.GONE);
                onButtonShowPopupWindowClick(true);
            }
        }

    }

    /**
     * Rafrichissant de la liste de news : relance des XMLAsyncTask de chargement
     *
     */
    private void refreshListNews () {

        // on remet les listes à vides
        rssAdapter.setNewsBeanList(Collections.synchronizedList(new ArrayList<NewsBean>()));
        rssAdapter.setCurrentNewsBeanList(Collections.synchronizedList(new ArrayList<NewsBean>()));

        // relance les taches
        rssAdapter.setNbrAsyncTaksFinished(0);
        List<UrlBean> urlBeanList = db.getAllUrlBeanRssActive();
        for (UrlBean urlBean : urlBeanList) {

            xmlAsyncTask = new XMLAsyncTask(rssAdapter);
            xmlAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlBean);

            rssAdapter.setNbrAsyncTaks(rssAdapter.getNbrAsyncTaks()+1);

        }

        // on réactive tous les item de la liste menu
        enabledAllMenuItem();

        // on remet la liste des catégorie à la première ligne
        razSpinner();

    }


    /**
     * Affichage menu actionbar : icone refresh/search/info
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        this.menu = menu;

        return true;
    }

    /**
     * Gestion du clic sur icones/menus de l'actionbar
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

                // rafraichissement de la liste
            case R.id.action_refresh:

                // on remet la progress bar
                progress.setVisibility(View.VISIBLE);

                // on désctive l'icone refresh
                MenuItem menuItem = menu.getItem(0);
                menuItem.setIcon(getDrawable(R.drawable.icon_refresh_clicked));
                menuItem.setEnabled(false);

                // rafraichissement de la liste de news
                refreshListNews();

                return true;

            // rafraichissement de la liste + toutes les news sont de nouveau marquées comme non-lues
            /*case R.id.action_reset:

                // on remet la progress bar
                progress.setVisibility(View.VISIBLE);

                // on recharge la liste des news
                refreshListNews();

                // on remet à vide la liste des news déjà lues
                //rssAdapter.setListNewsTitleAlreadyReaded( new ArrayList<String>());
                RssHelper.listNewsTitleAlreadyReaded = new ArrayList<String>();

                // on remet à vide la liste des news favoris
                RssHelper.listNewsTitleFavourite = new ArrayList<String>();

                return true;
                */

            // affichage de la popupdialog recherche
            case R.id.action_search:

                // on réactive toutes les actions du menu overflow (car masquer par boite de dialogue donc de toutes facçon pas clicable de suite)
                enabledAllMenuItem();
                searchDialog(this);
                return true;

            // affichage de la page infos
            case R.id.action_info:

                menuItem = menu.getItem(1);
                menuItem.setIcon(getDrawable(R.drawable.icon_info_clicked));

                onButtonShowPopupWindowClick(false);
                return true;

            // Affichage de la page de config
            case R.id.action_rss_config:

                menuItem = menu.getItem(2);
                menuItem.setIcon(getDrawable(R.drawable.icon_tools_clicked));

                // on va lancer un intent vers l'activité de gestion config des url rss
                Intent i = new Intent(getApplicationContext(), RssConfigActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_top, R.anim.slide_bottom);
                getWindow().setAllowEnterTransitionOverlap(true);

                return true;

            // Affichage de la liste de toutes les news
            case R.id.action_news_init:

                progress.setVisibility(View.VISIBLE);

                // on remet la currect liste dans l'adapter qui va notifier le changement
                rssAdapter.setItemMenuIdCliked(R.id.action_news_init);
                rssAdapter.changeCurrentNewsBeanList(getFilteredNewsBeanList());

                // on désactive l'item cliqué et on active les autres
                disableEnableMenuItem(R.id.action_news_init);

                // on remet la liste à la première ligne
                //razSpinner();

                progress.setVisibility(View.GONE);

                return true;

            // Affichage de la liste des news favorites
            case R.id.action_favourite:

                progress.setVisibility(View.VISIBLE);

                rssAdapter.setItemMenuIdCliked(R.id.action_favourite);

                // on remet la currect liste dans l'adapter qui va notifier le changement
                rssAdapter.changeCurrentNewsBeanList(getFilteredNewsBeanList());

                // on désactive l'item cliqué et on active les autres
                disableEnableMenuItem(R.id.action_favourite);

                progress.setVisibility(View.GONE);

                return true;

            // Affichage de la liste des news favorites
            case R.id.action_unread:

                progress.setVisibility(View.VISIBLE);

                rssAdapter.setItemMenuIdCliked(R.id.action_unread);

                // on remet la current liste dans l'adapter qui va notifier le changement
                rssAdapter.changeCurrentNewsBeanList(getFilteredNewsBeanList());

                // on désactive l'item cliqué et on active les autres
                disableEnableMenuItem(R.id.action_unread);

                progress.setVisibility(View.GONE);

                return true;


            // Affichage de la liste des news favorites
            case R.id.action_saved:

                progress.setVisibility(View.VISIBLE);

                rssAdapter.setItemMenuIdCliked(R.id.action_saved);

                // on remet la current liste dans l'adapter qui va notifier le changement
                rssAdapter.changeCurrentNewsBeanList(getFilteredNewsBeanList());

                // on désactive l'item cliqué et on active les autres
                disableEnableMenuItem(R.id.action_saved);

                progress.setVisibility(View.GONE);

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Permet de filtrer la liste des beans suivant :
     *
     * - item sélectionné dans menu action bar (favoris, non lus, etc....)
     * - catégorie sélectionnée dans spinner
     *
     */
    public List<NewsBean> getFilteredNewsBeanList() {

        List<NewsBean> newsBeanListInit = rssAdapter.getNewsBeanList();
        // liste qui va contenir les news favorites
        List<NewsBean> filteredNewsBeanList = Collections.synchronizedList(new ArrayList<NewsBean>());
        List<NewsBean> categorizedFeededNewsBeanList;
        List<NewsBean> categorizedNewsBeanList;
        List<String> listTitreNewscategorizedFeededFiltered = new ArrayList<>();

        // détermination de la catégorie sélectionnée et de la liste des beans associés
        if (!rssAdapter.getCategorySelected().equals("")&& !rssAdapter.getContext().getString(R.string.category_select).equalsIgnoreCase(rssAdapter.getCategorySelected())) {
            categorizedNewsBeanList = rssAdapter.getCategoryNewsMap().get(rssAdapter.getCategorySelected());
        } else {
            categorizedNewsBeanList = newsBeanListInit;
        }

        // filtrage suivant feed sélectionné
        String feedSelected = rssAdapter.getFeedSelected();
        // attention : si on a changé de catégorie ce n'est pas encore le bon nom feedSelected, faut vérif
        if (!(feedSelected.equals("") || rssAdapter.getContext().getString(R.string.feeds_select).equalsIgnoreCase(feedSelected))) {
            categorizedFeededNewsBeanList = new ArrayList<>();
            for (NewsBean news : categorizedNewsBeanList) {
                if (news.getNewsNom().equalsIgnoreCase(rssAdapter.getFeedSelected())) {
                    categorizedFeededNewsBeanList.add(news);
                }
            }
        } else {
            categorizedFeededNewsBeanList = categorizedNewsBeanList;
        }

        for (NewsBean newsBean : categorizedFeededNewsBeanList) {

            listTitreNewscategorizedFeededFiltered.add(newsBean.getNewsTitle());
        }

        // détermination de la liste des beans pour l'item menu sélectionné
        List<String> listTitreNewsMenuFiltered;

        if (rssAdapter.getItemMenuIdCliked() != null) {

            switch (rssAdapter.getItemMenuIdCliked()) {

                case R.id.action_favourite:

                    listTitreNewsMenuFiltered = RssHelper.listNewsTitleFavourite;

                    break;

                case R.id.action_unread:


                    listTitreNewsMenuFiltered = new ArrayList<>();

                    for (NewsBean newsBean : newsBeanListInit) {

                        if (!RssHelper.listNewsTitleAlreadyReaded.contains(newsBean.getNewsTitle())) {

                            listTitreNewsMenuFiltered.add(newsBean.getNewsTitle());
                        }
                    }

                    break;

                case R.id.action_saved:

                    listTitreNewsMenuFiltered = RssHelper.listNewsTitleSaved;

                    break;

                default:

                    listTitreNewsMenuFiltered = new ArrayList<>();

                    for (NewsBean newsBean : newsBeanListInit) {

                        listTitreNewsMenuFiltered.add(newsBean.getNewsTitle());
                    }


                    break;
            }

        } else {

            listTitreNewsMenuFiltered = new ArrayList<>();

            for (NewsBean newsBean : newsBeanListInit) {

                listTitreNewsMenuFiltered.add(newsBean.getNewsTitle());
            }
        }

        // parcours de la liste des news intiales
        for (NewsBean news : newsBeanListInit) {

            // si la news est favorites => on l'ajoute (on vérifie aussi catégorie)
            if (listTitreNewsMenuFiltered.contains(news.getNewsTitle()) && listTitreNewscategorizedFeededFiltered.contains(news.getNewsTitle())) {

                    filteredNewsBeanList.add(news);
            }
        }

        return filteredNewsBeanList;
    }

    /**
     * Après que les AsyncTask ont chargé les données il faut intialiser les Spinner de la MainActivity
     *
     */
    public void initCustomSpinner() {

        // Spinner CATEGORY Drop down elements
        List<String> category = rssAdapter.getCategoryList();
        CustomSpinnerCategoryAdapter customSpinnerCategoryAdapter = new CustomSpinnerCategoryAdapter(this, (ArrayList<String>) category);
        rssAdapter.getSpinnerCustom().setAdapter(customSpinnerCategoryAdapter);
        rssAdapter.getSpinnerCustom().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                rssAdapter.setCategorySelected(item);

                // si "toutes catégories" sélectionnées
                if (rssAdapter.getContext().getString(R.string.category_select).equals(item)) {

                    // on met tous les feeds
                    CustomSpinnerFeedAdapter customSpinnerFeedAdapter = new CustomSpinnerFeedAdapter(MainActivity.this, (ArrayList<String>) getAllFeeds());
                    rssAdapter.getSpinnerFeeds().setAdapter(customSpinnerFeedAdapter);

                    rssAdapter.changeCurrentNewsBeanList(rssAdapter.getFilteredNewsBeanList());

                } else {

                    majSpinnerFeeds();
                    rssAdapter.changeCurrentNewsBeanList(rssAdapter.getCategoryNewsMap().get(item));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Spinner FEEDS : le spinner feeds se rempli suivant celui catégory
        rssAdapter.getSpinnerFeeds().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                rssAdapter.setFeedSelected(item);

                rssAdapter.changeCurrentNewsBeanList(rssAdapter.getFilteredNewsBeanList());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Mise à jour du spinner feeds suivant la catégorie sélectionnée
     *
     *
     */
    private void majSpinnerFeeds() {

        HashMap<String, List<String>> categoryFeedMap = rssAdapter.getCategoryFeedMap();
        List<String> feeds = categoryFeedMap.get(rssAdapter.getCategorySelected());
        if (feeds == null) {
            feeds = getAllFeeds();
        }
        if (feeds.size() > 1 && !feeds.contains(rssAdapter.getContext().getString(R.string.feeds_select))) {
            feeds.add(0, rssAdapter.getContext().getString(R.string.feeds_select));
        }
        CustomSpinnerFeedAdapter customSpinnerFeedAdapter = new CustomSpinnerFeedAdapter(MainActivity.this, (ArrayList<String>) feeds);
        rssAdapter.getSpinnerFeeds().setAdapter(customSpinnerFeedAdapter);
        rssAdapter.setFeedSelected(feeds.get(0));
    }

    /**
     * Liste de tous les feeds de toutes les catégoriers
     *
     * @return List<String>
     *
     */
    private List<String> getAllFeeds() {

        HashMap<String, List<String>> categoryFeedMap = rssAdapter.getCategoryFeedMap();
        List<String> feeds = new ArrayList<>();
        for (Map.Entry mapentry : categoryFeedMap.entrySet()) {

            ArrayList namesFeed = (ArrayList)mapentry.getValue();
            feeds.addAll(namesFeed);
        }
        Collections.sort(feeds);
        if (feeds.size() > 1) {
            feeds.add(0, rssAdapter.getContext().getString(R.string.feeds_select));
        }

        return feeds;
    }

    /**
     * Désactivatiohn de l'item cliqué dans menu action bar
     *
     * @param idItemClick id de l'item clické
     *
     */
    private void disableEnableMenuItem(int idItemClick) {


        for (int i = 2; i<this.menu.size(); i++) {

            if (menu.getItem(i).getItemId() == idItemClick) {
                menu.getItem(i).setEnabled(false);
            } else {
                menu.getItem(i).setEnabled(true);
            }
        }

        rssAdapter.setItemMenuIdCliked(idItemClick);
    }

    /**
     * Réactivatiohn de tous les items dans menu action bar
     *
     *
     */
    private void enabledAllMenuItem() {

        for (int i = 2; i<this.menu.size(); i++) {
            menu.getItem(i).setEnabled(true);
        }

        rssAdapter.setItemMenuIdCliked(null);

    }

    public void reactivateIconRefreh() {

        // on remet l'image clicable de l'item 0 (incon refresh) et on le réactive
        MenuItem menuItem = menu.getItem(0);
        menuItem.setIcon(getDrawable(R.drawable.icon_refresh));
        menuItem.setEnabled(true);

    }

    /**
     * Remise à zéro de la liste catégorie sur première entrée
     *
     *
     */
    private void razSpinner() {

        rssAdapter.setCategorySelected("");
        spinnerCustom.setSelection(0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (xmlAsyncTask != null) {
            xmlAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        // on dégrise le bouton icon_tools
        if (menu != null) {
            MenuItem menuItem = menu.getItem(2);
            menuItem.setIcon(getDrawable(R.drawable.icon_tools));
        }

        if (db.getAllUrlBeanRssActive().size() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), rssAdapter.getContext().getString(R.string.NoRssConfig), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();


        // sauvegarde des préférences = liste des titres de news déjà cliquées
        // nettoyage liste si nécesaire (on enlève les news qui be sont plus référencées)
        ArrayList<String> listNewsTitleAlreadyReaded = RssHelper.listNewsTitleAlreadyReaded;
        List<String> listPurgeOk = purgeListOfNewsNonReferenced(listNewsTitleAlreadyReaded);
        Set<String> listSet = new HashSet<>(listPurgeOk);
        saveToSharedPreference(listSet, RssHelper.LIST_SET_NEWS_ALREADY_READED_PREF);

        // sauvegarde des préférences = liste des titres favoris
        // nettoyage liste si nécesaire (on enlève les news qui be sont plus référencées)
        ArrayList<String> listNewsTitleFavourite = RssHelper.listNewsTitleFavourite;
        listPurgeOk =  purgeListOfNewsNonReferenced(listNewsTitleFavourite);
        listSet = new HashSet<>(listPurgeOk);
        saveToSharedPreference(listSet, RssHelper.LIST_SET_NEWS_FAVOURITES);

    }

    /**
     * Sauvegarde d'une sharedPreference
     *
     * @param listSet liste
     * @param nameSharedPreference nom
     *
     */
    private void saveToSharedPreference(Set<String> listSet, String nameSharedPreference) {

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putStringSet(nameSharedPreference, listSet)
                .apply();

    }

    /**
     * Purege d'une liste de titres de news dont les titres ne sont plus dans la liste des news de références
     *
     * @param  listToPurge liste
     * @return  List<String> liste purgée liste
     *
     */
    private List<String> purgeListOfNewsNonReferenced( ArrayList<String> listToPurge) {

        List<NewsBean> newsRefrence = rssAdapter.getNewsBeanList();
        List<String> listPurgeOk = new ArrayList<>();
        boolean purge;

        for (String title : listToPurge) {

            purge = true;

            for (NewsBean refBean : newsRefrence) {

                if (refBean.getNewsTitle().equals(title)) {
                    purge = false;
                }
            }

            if (!purge) {
                listPurgeOk.add(title);
            }
        }

        return listPurgeOk;
    }


    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Ouverture de la popup information/notice utilisation
     *
     * @param delay : ajout d'un délai pour affichage, nécessaire quand on arrive sur l'appli et si pas d'url rss
     *
     */
    private void onButtonShowPopupWindowClick(boolean delay) {

        // get a reference to the already created main layout
        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.layout_root);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        View popupView = inflater.inflate(R.layout.popup_information, nullParent);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // chargement des données dans la Webview
        String webTemplate = RssHelper.webPopupInformationTemplate;
        WebView webViewPopupInformation = (WebView) popupView.findViewById(R.id.webviewPopupInformation);
        webViewPopupInformation.loadDataWithBaseURL("file:///android_asset/", webTemplate, "text/html", "utf-8",null);

        // initialisation du bouton fermeture popup
        ImageButton buttonClose = (ImageButton) popupView.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MenuItem menuItem = menu.getItem(1);
                menuItem.setIcon(getDrawable(R.drawable.icon_info));
                popupWindow.dismiss();
            }
        });

        // show the popup window
        if (delay) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
                }
            }, 1000); //Delay one second}

        } else {
            popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
        }

    }

    /**
     * Ouverture de la boite de dailogue de recherche
     *
     */
    private void searchDialog(Context context) {

        // get mail_dialog.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View mailDialogView = li.inflate(R.layout.search_dialog, new LinearLayout(context), false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.dialogTheme);
        alertDialogBuilder.setIcon(R.drawable.icon_search);
        alertDialogBuilder.setTitle(context.getString(R.string.search_dialog_title));

        // set mail_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(mailDialogView);

        editTextSearch = (EditText) mailDialogView.findViewById(R.id.searchDialogInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.search_dialog_search,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text

                                //RssHelper.adresseMailReceiver = mailInput.getText().toString();
                                //sendEmail(webview.getContext());
                            }
                        })
                .setNegativeButton(R.string.mail_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new MainActivity.CustomListener(alertDialog));
    }

    /**
     *
     * listener custom sur alert dialog d'ajout d'une url
     *
     */
    private class CustomListener implements View.OnClickListener {

        private final Dialog dialog;
        CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            // récupération des données
            String keyWords = editTextSearch.getText().toString().trim();
            if (keyWords.length() > 0) {

                List<NewsBean> currentNewsBeanList = rssAdapter.getCurrentNewsBeanList();
                List<NewsBean> searchNewsBeanList = new ArrayList<>();

                String[] listKeyWords = keyWords.split(",");

                for (String keyWord : listKeyWords) {

                    for (NewsBean news : currentNewsBeanList) {

                        if (news.getNewsDescription().toUpperCase().contains(keyWord.toUpperCase())) {
                            if (!searchNewsBeanList.contains(news)) {
                                searchNewsBeanList.add(news);
                            }
                        }
                    }
                }

                // si on trouve des résultats => on met à jour l'adapter
                if (searchNewsBeanList.size() > 0) {
                    rssAdapter.changeCurrentNewsBeanList(searchNewsBeanList);
                    rssAdapter.notifyDataSetChanged();
                } else {
                    RssHelper.afficheToastError(MainActivity.this, v.getContext().getString(R.string.search_no_result));
                }
            }

            // fermeture boite de dialogue
            dialog.dismiss();

        }
    }

}
