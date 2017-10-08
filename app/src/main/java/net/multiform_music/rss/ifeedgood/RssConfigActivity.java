package net.multiform_music.rss.ifeedgood;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.multiform_music.rss.ifeedgood.R;

import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerCategoryAdapter;
import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerConfigFiltreCategoryAdapter;
import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerSearchRssAdapter;
import net.multiform_music.rss.ifeedgood.adapter.UrlAdapter;
import net.multiform_music.rss.ifeedgood.bean.CategoryBean;
import net.multiform_music.rss.ifeedgood.bean.ErrorBean;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;
import net.multiform_music.rss.ifeedgood.helper.DatabaseHelper;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michel on 15/04/2017.
 *
 */

public class RssConfigActivity extends AppCompatActivity {

    // les champs à renseigner
    private EditText editTextUrl;
    private EditText editTextNom;
    private EditText editTextKeywords;
    private Spinner spinnerCategorie;
    private ScrollView scrollView;

    // adapter pour spinner résultat liste rss recherche
    private CustomSpinnerSearchRssAdapter customSpinnerAdapterResultatRecherche;

    // le Help DB
    public static DatabaseHelper db;

    // adapter pour recyclerview
    private UrlAdapter urlAdapter;

    private CustomSpinnerConfigFiltreCategoryAdapter customSpinnerConfigFiltreCategoryAdapter;

    // le menu de la barre d'action
    private Menu menu;

    /** POUR BOITES DE DIALOGUES */
    private Spinner spinnerSearchResult;
    private ProgressDialog progressDialog;

    private EditText categoryNameConfigSelected;
    private EditText categoryNameConfigAdd;
    private String categoryNameInitiale;
    private int positionCategoryConfigSelected;
    private Spinner spinnerConfigCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_config);
        setTitle(getResources().getString(R.string.rss_config_title));
        //((AppCompatActivity) RssConfigActivity.this).getSupportActionBar().setSubtitle(R.string.rss_config_subtitle);

        Button boutonRetournerMain = (Button) findViewById(R.id.btnReturnMain);
        boutonRetournerMain.setOnClickListener(clickListenerButtonMain);

        // initialisation de la base
        db = new DatabaseHelper(this);

        // recycler view + adapter
        RecyclerView urlRecyclerView = (RecyclerView) findViewById(R.id.recyclerListUrl);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        urlRecyclerView.setLayoutManager(layoutManager);
        urlAdapter = new UrlAdapter();
        urlAdapter.setContext(this);

        urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
        urlAdapter.setYes(getResources().getString(R.string.rss_list_active_yes));
        urlAdapter.setNo(getResources().getString(R.string.rss_list_active_no));
        urlRecyclerView.setAdapter(urlAdapter);
        urlAdapter.notifyDataSetChanged();

        // spinner filtrage catégorie
        Spinner spinnerConfigCategoryFiltre = (Spinner) findViewById(R.id.spinnerConfigFiltreCategory);
        customSpinnerConfigFiltreCategoryAdapter = new CustomSpinnerConfigFiltreCategoryAdapter(RssConfigActivity.this, new ArrayList<String>());
        spinnerConfigCategoryFiltre.setAdapter(customSpinnerConfigFiltreCategoryAdapter);
        majSpinnerConfigCategoryFiltre();
        spinnerConfigCategoryFiltre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                if (!item.equalsIgnoreCase(getResources().getString(R.string.rss_config_list_all_categories))) {

                    ArrayList<UrlBean> listUrlBeanFiltre = new ArrayList<>();
                    List<UrlBean> listRss = db.getAllUrlBeanRss();

                    for (UrlBean rss : listRss) {
                        if (rss.getCategorie().equals(item)) {
                            listUrlBeanFiltre.add(rss);
                        }
                    }

                    urlAdapter.setUrlBeanList(listUrlBeanFiltre);
                    urlAdapter.notifyDataSetChanged();

                } else {
                    urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
                    urlAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.INTENT_RSS_CONFIG);
        if (message != null) {
            RssHelper.afficheToastError(RssConfigActivity.this, message);
            showAddRssDialog();
        }
    }

    /**
     *
     * Met à jour le spinner filtre catégorie de l'écran liste RSS
     *
     */
    private void majSpinnerConfigCategoryFiltre() {
        List<UrlBean> listRss = db.getAllUrlBeanRss();
        List<String> listSpinnerFiltreCategory = new ArrayList<>();
        for (UrlBean rss : listRss) {
            if (!listSpinnerFiltreCategory.contains(rss.getCategorie())) {
                listSpinnerFiltreCategory.add(rss.getCategorie());
            }
        }

        Collections.sort(listSpinnerFiltreCategory);
        listSpinnerFiltreCategory.add(0, getResources().getString(R.string.rss_config_list_all_categories));

        customSpinnerConfigFiltreCategoryAdapter.setAsr((ArrayList<String>) listSpinnerFiltreCategory);
        customSpinnerConfigFiltreCategoryAdapter.notifyDataSetChanged();

    }

    /**
     * Affichage menu actionbar : icone refresh/search/info
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        this.menu = menu;

        return true;
    }

    /**
     *
     *  Gestion du clic sur le bouton de retour à l'activité pricipale (liste news)
     *
     */
    private final Button.OnClickListener clickListenerButtonMain = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            getWindow().setAllowEnterTransitionOverlap(true);
            overridePendingTransition(R.anim.slide_top, R.anim.fade_out);
        }
    };

    /**
     *
     *  Gestion du clic sur le bouton de configuration des catégories
     *
     */
    private void showConfigCategoryDialog() {

        LayoutInflater li = LayoutInflater.from(RssConfigActivity.this);
        View categoryConfigDialogView = li.inflate(R.layout.category_config_dialog, new LinearLayout(RssConfigActivity.this), false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RssConfigActivity.this, R.style.dialogTheme);
        alertDialogBuilder.setTitle(RssConfigActivity.this.getString(R.string.category_config_dialog_title));
        alertDialogBuilder.setIcon(R.drawable.icon_add_category);
        alertDialogBuilder.setView(categoryConfigDialogView);

        // spinner category
        spinnerConfigCategories = (Spinner) categoryConfigDialogView.findViewById(R.id.spinnerConfigCategories);
        majSpinnerConfigCategories();

        // champ saisi pour ajout catégorie
        categoryNameConfigAdd = (EditText) categoryConfigDialogView.findViewById(R.id.categorygAdddName);

        spinnerConfigCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                if (!item.equalsIgnoreCase(getResources().getString(R.string.rss_config_dialog_category))) {
                    categoryNameConfigSelected.setText(item);
                    categoryNameInitiale = item;
                } else {
                    categoryNameConfigSelected.setText("");
                    categoryNameInitiale = "";

                }

                // on sauvedarge la position sélectionnée
                positionCategoryConfigSelected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // editText de la catégorie sélectionnée
        categoryNameConfigSelected = (EditText) categoryConfigDialogView.findViewById(R.id.categorygSelectedName);

        // button modifier/supprimer/ajouter
        Button buttonUpdateCategory = (Button) categoryConfigDialogView.findViewById(R.id.btnUpdateCategoryName);
        buttonUpdateCategory.setOnClickListener(clickListenerButtonCategoryUpdate);
        Button buttonDeleteCategory = (Button) categoryConfigDialogView.findViewById(R.id.btnDeleteCategoryName);
        buttonDeleteCategory.setOnClickListener(clickListenerButtonCategoryDelete);
        Button buttonAddCategory = (Button) categoryConfigDialogView.findViewById(R.id.btnAddCategoryName);
        buttonAddCategory.setOnClickListener(clickListenerButtonCategoryAdd);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(R.string.rss_config_dialog_annuler,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        if(alertDialog.getWindow() != null) {
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationSlideConfigCategory;
        }

        alertDialog.show();
    }

    /**
     *
     *  Gestion du clic sur le bouton ajout d'un feed RSS de la toolbar
     *
     * */
    private void showAddRssDialog() {

            LayoutInflater li = LayoutInflater.from(RssConfigActivity.this);
            View rssAddDialogView = li.inflate(R.layout.rss_add_dialog, new LinearLayout(RssConfigActivity.this), false);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RssConfigActivity.this, R.style.dialogTheme);
            alertDialogBuilder.setTitle(RssConfigActivity.this.getString(R.string.rss_config_dialog_title));
            alertDialogBuilder.setIcon(R.drawable.icon_add_rss);
            alertDialogBuilder.setView(rssAddDialogView);

            // edit text url
            editTextUrl = (EditText) rssAddDialogView.findViewById(R.id.rssUrlDialogInput);
            editTextUrl.setText(RssConfigActivity.this.getString(R.string.rss_config_dialog_pattern_http));
            editTextUrl.setSelection(editTextUrl.getText().length());

            // edit text nom
            editTextNom = (EditText) rssAddDialogView.findViewById(R.id.rssUrlDialogNom);

            // edit text mot-clés
            editTextKeywords = (EditText) rssAddDialogView.findViewById(R.id.rssUrlSearchDialogInput);

            // liste déroulante catégorie
            spinnerCategorie = (Spinner) rssAddDialogView.findViewById(R.id.rssConfigCategorie);
            List<CategoryBean> categoryBeanList = db.getAllCategory();
            List<String> categorieList = new ArrayList<>();
            for (CategoryBean categoryBean : categoryBeanList) {
                categorieList.add(categoryBean.getCategoryName());
            }
            categorieList.add(0, getResources().getString(R.string.rss_config_dialog_category));
            CustomSpinnerCategoryAdapter customSpinnerCategoryAdapter = new CustomSpinnerCategoryAdapter(RssConfigActivity.this, (ArrayList<String>) categorieList);
            spinnerCategorie.setAdapter(customSpinnerCategoryAdapter);

            // liste déroulante résultat recherche
            spinnerSearchResult = (Spinner) rssAddDialogView.findViewById(R.id.rssSearchUrlResult);
            String[] resusltRecherche = {getResources().getString(R.string.rss_config_dialog_url_search_waiting)};
            List<String> resultatRechercheList = new ArrayList<>();
            Collections.addAll(resultatRechercheList, resusltRecherche);
            customSpinnerAdapterResultatRecherche = new CustomSpinnerSearchRssAdapter(RssConfigActivity.this, (ArrayList<String>) resultatRechercheList);
            spinnerSearchResult.setOnItemSelectedListener(spinnerRssItemListener);
            spinnerSearchResult.setAdapter(customSpinnerAdapterResultatRecherche);

            // bouton de recherche
            Button boutonRssRecherche = (Button) rssAddDialogView.findViewById(R.id.btnSearchUrl);
            boutonRssRecherche.setOnClickListener(clickListenerButtonRssSearch);

            // scrollview
            scrollView = (ScrollView) rssAddDialogView.findViewById(R.id.scrollViewDialogRssAdd);

            // checkbox config manuelle
            CheckBox configManually = (CheckBox) rssAddDialogView.findViewById(R.id.rssConfigManually);
            configManually.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        editTextNom.setVisibility(View.VISIBLE);
                        editTextUrl.setVisibility(View.VISIBLE);

                        scrollView.smoothScrollTo(0, scrollView.getBottom());

                    } else {
                        editTextNom.setVisibility(View.INVISIBLE);
                        editTextUrl.setVisibility(View.INVISIBLE);

                        scrollView.smoothScrollTo(scrollView.getBottom(), 0);
                    }

                }
            });

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton(R.string.rss_config_dialog_ajouter,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            })
                    .setNegativeButton(R.string.rss_config_dialog_annuler,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            if(alertDialog.getWindow() != null) {
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationSlide;
            }
            alertDialog.show();
            Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            theButton.setOnClickListener(new CustomListener());

            MenuItem menuItem = menu.getItem(0);
            menuItem.setIcon(getDrawable(R.drawable.icon_add_rss));
            menuItem.setEnabled(true);
        }


    /**
     *
     * listener custom sur dialog d'ajout d'un feed
     *
     */
    private class CustomListener implements View.OnClickListener {

        CustomListener() {
        }

        @Override
        public void onClick(View v) {

            progressDialog = ProgressDialog.show(RssConfigActivity.this, RssConfigActivity.this.getResources().getString(R.string.rss_config_dialog_url_add_waiting), RssConfigActivity.this.getResources().getString(R.string.rss_config_dialog_url_add_waiting_subtitle), true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {
                        // récupération des données
                        String url = editTextUrl.getText().toString().trim();
                        url = url.replace(" ", "");
                        String nom = editTextNom.getText().toString();
                        String categorie = spinnerCategorie.getSelectedItem().toString();

                        // vérification catégorie sélectionnée
                        ErrorBean errorBean = new ErrorBean();

                        long intemSeleceted = spinnerCategorie.getSelectedItemId();
                        if (intemSeleceted == 0) {
                            errorBean.setMessage(getResources().getString(R.string.rss_config_dialog_url_error_category));
                            progressDialog.dismiss();
                            RssHelper.afficheToastError(RssConfigActivity.this, errorBean.getMessage());

                            return;
                        }


                        // vérification url : valide, bien rss, etc....
                        if (url.trim().length() == 0) {
                            errorBean.setMessage(getResources().getString(R.string.rss_config_dialog_url_error_vide));

                        } else if (nom.trim().length() == 0) {
                            errorBean.setMessage(getResources().getString(R.string.rss_config_dialog_nom_error_vide));
                        }

                        // si pas d'erreur d'url on vérifie les champs bien renseignés
                        if (!errorBean.isError()) {

                            errorBean = RssHelper.verifRssUrl(url, RssConfigActivity.this);

                            // si redirection
                            if (errorBean.getMessage() != null && errorBean.getMessage().equalsIgnoreCase("Location")) {
                                editTextUrl.setText(errorBean.getNewUrl());
                                url = errorBean.getNewUrl();
                            }
                        }

                        if (db.fluxExists(url)) {
                            errorBean.setMessage(getResources().getString(R.string.rss_config_dialog_url_error_url_exists));
                        }

                        if (errorBean.isError()) {

                            progressDialog.dismiss();
                            RssHelper.afficheToastError(RssConfigActivity.this, errorBean.getMessage());

                        } else {

                            // ajout en base
                            db.addRssUrl(url, nom, categorie, true);

                            urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
                            urlAdapter.notifyDataSetChanged();

                            progressDialog.dismiss();

                            String textAjoutOk = getResources().getString(R.string.rss_config_dialog_url_add_ok);
                            textAjoutOk = textAjoutOk.replace("{NAME}", nom);

                            RssHelper.afficheToastOk(RssConfigActivity.this, textAjoutOk);

                            filtreSpinnerResultSearch();
                            spinnerSearchResult.setSelection(0);

                            majSpinnerConfigCategoryFiltre();

                        }

                    } catch (Exception e) {
                        progressDialog.dismiss();
                    }
                }
        }, 1000);
        }
    }

    /**
     * mise à jour spinner catégories dans dialog config
     *
     */
    private void majSpinnerConfigCategories() {

        List<CategoryBean> categoryBeanList = db.getAllCategory();
        List<String> categorieList = new ArrayList<>();
        for (CategoryBean categoryBean : categoryBeanList) {
            categorieList.add(categoryBean.getCategoryName());
        }
        categorieList.add(0, getResources().getString(R.string.rss_config_dialog_category));
        CustomSpinnerCategoryAdapter customSpinnerCategoryAdapter = new CustomSpinnerCategoryAdapter(RssConfigActivity.this, (ArrayList<String>) categorieList);
        spinnerConfigCategories.setAdapter(customSpinnerCategoryAdapter);
        spinnerConfigCategories.setSelection(positionCategoryConfigSelected);
    }

    /**
     * Filtre la liste des résultats de recherhce avant affichage suivant le liste des rss déjà en bases
     *
     */
    private void filtreSpinnerResultSearch() {

        List<UrlBean> urlList = db.getAllUrlBeanRss();
        List<String> urlNomBase = new ArrayList<>(urlList.size());
        List<String> rssNameList = customSpinnerAdapterResultatRecherche.getAsr();
        ArrayList<String> listNomFinale = new ArrayList<>();

        for (UrlBean urlBean : urlList) {
            urlNomBase.add(urlBean.getNom());
        }

        for (String rssName : rssNameList) {

            if (!urlNomBase.contains(rssName)) {
                listNomFinale.add(rssName);
            }
        }

        customSpinnerAdapterResultatRecherche.setAsr(listNomFinale);
        customSpinnerAdapterResultatRecherche.notifyDataSetChanged();
    }

    /**
     * Gestion du clic sur icones/menus de l'actionbar
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {

                case android.R.id.home:

                    // vérification si un fil actif, si non => message erreur
                    if (okRssActive()) {

                        RssHelper.afficheToastError(RssConfigActivity.this, this.getResources().getString(R.string.configure_url));
                        return false;

                    } else {

                        NavUtils.navigateUpFromSameTask(this);
                        overridePendingTransition(R.anim.slide_top, R.anim.fade_out);

                        return true;
                    }

                case R.id.action_add:

                    MenuItem menuItem = menu.getItem(0);
                    menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.icon_add_rss_clicked));
                    menuItem.setEnabled(false);

                    showAddRssDialog();

                    return true;

                case R.id.action_add_category:

                    showConfigCategoryDialog();

                    return true;

                default:
            }

            return false;
    }

    /**
     * Gère le clic sur bouton retour
     *
     */
    @Override
    public void onBackPressed() {

        // vérification si un fil actif, si non => message erreur
        if (okRssActive()) {
            RssHelper.afficheToastError(RssConfigActivity.this, this.getResources().getString(R.string.configure_url));
            showAddRssDialog();
            return;
        }

        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();
        getWindow().setAllowEnterTransitionOverlap(true);
        overridePendingTransition(R.anim.slide_top, R.anim.fade_out);
    }

    /**
     * Détermine si au moins une url est active
     *
     * @return boolean
     *
     */
    private boolean okRssActive() {

        List<UrlBean> urlBeanList = db.getAllUrlBeanRssActive();
        return (urlBeanList == null || urlBeanList.size() <= 0);
    }


    /**
     * Gestion du clic le bouton "Chercher" da la boite de dialogue
     *
     */
    private final Button.OnClickListener clickListenerButtonRssSearch = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(RssConfigActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextKeywords.getWindowToken(), 0);

            if (RssHelper.isNetworkOk(getApplicationContext())) {
                // controle si présence mot-clés
                if (editTextKeywords.getText().toString().trim().length() < 1) {

                    RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.rss_config_dialog_url_search_error_keywords));

                } else {

                    SearchRssUrlTask searchRssUrlTask = new SearchRssUrlTask(customSpinnerAdapterResultatRecherche);
                    searchRssUrlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editTextKeywords.getText().toString());
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.NetworkError), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }

        }
    };

    /**
     * Gestion du clic le bouton "Modifier" da la boite de dialogue config category
     *
     */
    private final Button.OnClickListener clickListenerButtonCategoryUpdate = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            String categoryNameModif = categoryNameConfigSelected.getText().toString();

            if (controleOkNameEmptyExist(categoryNameModif)) {

                boolean ok = db.updateCategory(categoryNameInitiale, categoryNameConfigSelected.getText().toString());

                if (ok) {
                    RssHelper.afficheToastOk(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_liste_modify_ok));
                    majSpinnerConfigCategories();
                    urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
                    urlAdapter.notifyDataSetChanged();
                    majSpinnerConfigCategoryFiltre();

                } else {
                    RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_liste_modify_nok));
                }

            }
        }
    };

    /**
     * Gestion du clic le bouton "Ajouter" da la boite de dialogue config category
     *
     */
    private final Button.OnClickListener clickListenerButtonCategoryAdd = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            String categoryNameAdd = categoryNameConfigAdd.getText().toString();

            if (controleOkNameEmptyExist(categoryNameAdd)) {

                boolean addOk = db.addCategory(categoryNameAdd);

                if (!addOk) {
                    RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_liste_add_nok));
                } else {
                    // message info ok
                    RssHelper.afficheToastOk(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_liste_add_ok));

                    // rechargement spinner category
                    majSpinnerConfigCategories();
                    CustomSpinnerCategoryAdapter customSpinnerCategoryAdapter = (CustomSpinnerCategoryAdapter) spinnerConfigCategories.getAdapter();
                    ArrayList<String> listCategory = customSpinnerCategoryAdapter.getAsr();
                    int indexNewCategory = listCategory.indexOf(categoryNameAdd);
                    spinnerConfigCategories.setSelection(indexNewCategory);

                    majSpinnerConfigCategoryFiltre();
                }

            }
        }
    };

    /**
     * Détermine si le nom de la catégorie est vide et s'il existe déjà
     *
     * @param categoryName : nom
     * @return boolean
     */
    private boolean controleOkNameEmptyExist(String categoryName) {

        if (categoryName.trim().equalsIgnoreCase("")) {
            RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_name_error_empty));
            return false;
        }

        boolean categoryAlreadyExist = db.categoryExist(categoryName);

        if (categoryAlreadyExist) {
            RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_name_error_exist));
            return false;
        }

        return true;
    }
    /**
     * Gestion du clic le bouton "Supprimer" da la boite de dialogue config category
     *
     */
    private final Button.OnClickListener clickListenerButtonCategoryDelete = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {


            // avant suppression : test pour savoir si des fils rss sont rattachés à cette catégorie
            boolean existRssToCategory = db.categoryRssExist(categoryNameInitiale);
            if (existRssToCategory) {
                RssHelper.afficheToastError(RssConfigActivity.this, getApplicationContext().getString(R.string.category_config_liste_delete_error));

            // si pas de fil rattachés => boite de dialogue de confirmation de suppresion
            } else {

                LayoutInflater li = LayoutInflater.from(RssConfigActivity.this);
                View categoryDeleteAddDialogView = li.inflate(R.layout.category_delete_dialog, new LinearLayout(RssConfigActivity.this), false);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RssConfigActivity.this, R.style.dialogTheme);
                alertDialogBuilder.setTitle(RssConfigActivity.this.getString(R.string.category_config_dialog_title_delete));
                alertDialogBuilder.setIcon(R.drawable.icon_confirm_delete);
                alertDialogBuilder.setView(categoryDeleteAddDialogView);

                TextView categoryNameToDelete = (TextView)  categoryDeleteAddDialogView.findViewById(R.id.nomCategoryDeelete);
                categoryNameToDelete.setText(categoryNameInitiale);

                // dialogue pour confirmation suppression
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.common_delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        // suppression effective
                                        boolean deleteOk = db.deleteCategory(categoryNameInitiale);

                                        // suppression OK
                                        if (deleteOk) {

                                            // toast d'info OK
                                            String message = getApplicationContext().getString(R.string.category_config_liste_delete_ok);
                                            message = message.replace("{NAME}", categoryNameInitiale);
                                            RssHelper.afficheToastOk(RssConfigActivity.this, message);

                                            // rechargement spinner category
                                            majSpinnerConfigCategories();
                                            spinnerConfigCategories.setSelection(0);

                                            majSpinnerConfigCategoryFiltre();

                                        // suppression NOK
                                        } else {

                                            // test d'infos NOK
                                            String message = getApplicationContext().getString(R.string.category_config_liste_delete_nok);
                                            message = message.replace("{NAME}", categoryNameInitiale);
                                            RssHelper.afficheToastError(RssConfigActivity.this, message);
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.rss_config_dialog_annuler,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                if(alertDialog.getWindow() != null) {
                    alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationDeleteCategoryFade;
                }

                alertDialog.show();

            }

        }
    };


    /**
     * Gestion de sélection d'un item dans le spinner résultat recherche RSS
     *
     */
    private final AdapterView.OnItemSelectedListener spinnerRssItemListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String selectedItem = parent.getItemAtPosition(position).toString();

            if (!selectedItem.equalsIgnoreCase(getApplicationContext().getString(R.string.rss_config_dialog_url_search_waiting))) {

                editTextNom.setText(selectedItem);
                String url = customSpinnerAdapterResultatRecherche.getHashTitleUrl().get(selectedItem);
                editTextUrl.setText(url);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /***********  ASYNCTASK SEARCH ENGINE  **************************/

    private class SearchRssUrlTask extends AsyncTask<String, Void, String> {

        final CustomSpinnerSearchRssAdapter customSpinnerAdapterResultatRecherche;
        ProgressDialog progressDialog;

        SearchRssUrlTask(CustomSpinnerSearchRssAdapter customSpinnerAdapterResultatRecherche) {
            this.customSpinnerAdapterResultatRecherche = customSpinnerAdapterResultatRecherche;
        }

        @Override
        protected String doInBackground(String... params) {

            String result;

            try {

                String requeteUrl = RssHelper.providerSearchRssUrl + params[0];
                URL url = new URL(requeteUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();

                // convert inputstream to string
                if(inputStream != null){
                    result = convertInputStreamToString(inputStream);

                } else {
                    result = "Failed to search";
                }

                return result;

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(RssConfigActivity.this, RssConfigActivity.this.getResources().getString(R.string.rss_config_dialog_url_search_waiting_result), RssConfigActivity.this.getResources().getString(R.string.rss_config_dialog_url_search_waiting_result_message), true);
        }

        @Override
        protected void onPostExecute(String dataFetched) {

            //parse the JSON data and then display
            try{

                JSONObject object = new JSONObject(dataFetched);
                JSONArray jsonMainNode = object.getJSONArray("results");
                int jsonArrLength = jsonMainNode.length();

                HashMap<String, String> hashTitleUrl = new HashMap<>(jsonArrLength);
                ArrayList<String> titleList = new ArrayList<>(jsonArrLength);
                int count = 0;

                // parcours des objets json Resulsts
                for(int i=0; i < jsonArrLength; i++) {

                    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                    String title = jsonChildNode.getString("title");
                    String feedId = jsonChildNode.getString("feedId");
                    if (feedId != null && feedId.contains("http")) {
                        feedId = feedId.substring(feedId.indexOf("http"));
                    }

                    // si l'url n'est pas déjà en base => on l'ajoute à la liste
                    if (!db.fluxExists(feedId)) {

                        titleList.add(count, title);
                        hashTitleUrl.put(title, feedId);
                        count++;
                    }
                }

                titleList.add(0, customSpinnerAdapterResultatRecherche.getActivity().getResources().getString(R.string.rss_config_dialog_url_search_result));

                // mise à jour de l'adapter spinner résultat recherche
                customSpinnerAdapterResultatRecherche.setAsr(titleList);
                customSpinnerAdapterResultatRecherche.setHashTitleUrl(hashTitleUrl);

                // filtrage des résultats et on se remet à la première ligne
                filtreSpinnerResultSearch();
                spinnerSearchResult.setSelection(0);

                progressDialog.dismiss();

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {

            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line;
            String result = "";

            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            inputStream.close();
            return result;

        }
    }
}
