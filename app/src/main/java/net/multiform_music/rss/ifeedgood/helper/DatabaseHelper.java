package net.multiform_music.rss.ifeedgood.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.multiform_music.rss.ifeedgood.bean.CategoryBean;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "NewsReaderDB";


    private static final String TABLE_NAME_NEWS = "News";
    private static final String COLUMN_ID = "id";                                // intgeger
    private static final String COLUMN_TITLE ="titre";                           // TEXT
    private static final String COLUMN_DESCRIPTION = "description";              // TEXT
    private static final String COLUMN_DATE_PUBLICATION = "date_publication";   // TEXT
    private static final String COLUMN_LINK = "link";                            // TEXT
    private static final String COLUMN_IMAGE_URL = "image_url";                  // TEXT
    private static final String COLUMN_CATEGORIE = "categorie";                  // TEXT
    private static final String COLUMN_BODY_COLOR = "body_color";                // TEXT
    private static final String COLUMN_FAVORI = "favori";                        // TEXT
    private static final String COLUMN_HASH_TITLE = "hash_titre";                // TEXT
    private static final String COLUMN_HASH_CATEGORIE = "hash_categorie";        // TEXT

    private static final String TABLE_NAME_RSS_SOURCE = "RssSource";
    private static final String COLUMN_RSS_URL = "rss_url";                       // TEXT
    private static final String COLUMN_RSS_NOM = "nom";                           // TEXT
    private static final String COLUMN_RSS_ACTIVE = "active";                     // TEXT
    private static final String COLUMN_DATE_AJOUT = "date_ajout";                 // TEXT
    private static final String COLUMN_HASH_NOM = "hash_nom";                     // TEXT
    private static final String COLUMN_HASH_URL = "hash_url";                     // TEXT

    private static final String TABLE_RSS_CATEGORY = "Category";
    private static final String COLUMN_RSS_CATEGORY = "category";            // TEXT
    private static final String COLUMN_RSS_CATEGORY_HASH = "category_hash";  // TEXT

    private static final String[] CATEGORY_FR = {
            "Actualités",
            "Apple/IOS",
            "Bourse actualités",
            "Bourse cotation",
            "Cuisine",
            "Culture",
            "Divers",
            "Economie",
            "Google/Android",
            "Hi-Tech",
            "Informatique",
            "Musique",
            "Politique",
            "Sciences",
            "Santé",
            "Sport"
    };

    private static final String[] CATEGORY_EN = {
            "Apple/IOS",
            "Cooking",
            "Culture",
            "Economy",
            "Google/Android",
            "Hi-Tech",
            "Health",
            "Informatic",
            "Music",
            "News",
            "Politics",
            "Science",
            "Stock exchange quotation",
            "Stock exchange News",
            "Sport",
            "Various"
    };

    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlNews = "CREATE TABLE " + TABLE_NAME_NEWS
                +"(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATE_PUBLICATION + " TEXT, "
                + COLUMN_LINK + " TEXT, "
                + COLUMN_IMAGE_URL + " TEXT, "
                + COLUMN_CATEGORIE + " TEXT, "
                + COLUMN_BODY_COLOR + " TEXT, "
                + COLUMN_FAVORI + " TEXT, "
                + COLUMN_HASH_TITLE + " TEXT, "
                + COLUMN_HASH_CATEGORIE + " TEXT "
                + ");";

        db.execSQL(sqlNews);

        String sqlRssSource = "CREATE TABLE " + TABLE_NAME_RSS_SOURCE
                +"(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RSS_URL + " TEXT, "
                + COLUMN_RSS_NOM + " TEXT, "
                + COLUMN_CATEGORIE + " TEXT, "
                + COLUMN_RSS_ACTIVE + " TEXT, "
                + COLUMN_DATE_AJOUT + " TEXT, "
                + COLUMN_HASH_URL+ " TEXT, "
                + COLUMN_HASH_NOM + " TEXT, "
                + COLUMN_HASH_CATEGORIE + " TEXT "
                + ");";

        db.execSQL(sqlRssSource);

        String sqlRssCategory = "CREATE TABLE " + TABLE_RSS_CATEGORY
                +"("
                + COLUMN_RSS_CATEGORY + " TEXT, "
                + COLUMN_RSS_CATEGORY_HASH + " TEXT "
                + ");";

        db.execSQL(sqlRssCategory);

        initCategory(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_NEWS;
        db.execSQL(sql);

        sql = "DROP TABLE IF EXISTS " + TABLE_NAME_RSS_SOURCE;
        db.execSQL(sql);

        sql = "DROP TABLE IF EXISTS " + TABLE_RSS_CATEGORY;
        db.execSQL(sql);

        onCreate(db);
    }


    /**
     *
     * Initialisation de la table category suivant la locale
     *
     * @param db db
     */
    private void initCategory(SQLiteDatabase db) {

        int nbrCategory = CATEGORY_EN.length;

        for (int i = 0; i<nbrCategory; i++) {

            if (RssHelper.locale.getLanguage().contains("fr") || RssHelper.locale.getLanguage().contains("FR")) {

                db.execSQL("INSERT INTO "+ TABLE_RSS_CATEGORY  + " VALUES('" + CATEGORY_FR[i] + "', '" + md5(CATEGORY_FR[i]) + "');");
            } else {
                db.execSQL("INSERT INTO "+ TABLE_RSS_CATEGORY  + " VALUES('" + CATEGORY_EN[i] + "', '" + md5(CATEGORY_EN[i]) + "');");
            }
        }
    }

    /**
     *
     *  Récupération de toutes les catégories rss en base
     *
     */
    public List<CategoryBean> getAllCategory() {

        List<CategoryBean> categoryBeanList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] allColumns = new String[] { COLUMN_RSS_CATEGORY  };
        Cursor cursor = db.query(TABLE_RSS_CATEGORY, allColumns, null, null, null, null, COLUMN_RSS_CATEGORY + " ASC");

        if(cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                CategoryBean categoryBean = new CategoryBean();

                categoryBean.setCategoryName(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_CATEGORY)));

                categoryBeanList.add(categoryBean);
            }

        }

        cursor.close();

        return categoryBeanList;

    }

    /**
     *
     *  Ajout d'une news en base
     *
     */
    public void addNews(String titre, String description, String date_publication, String link, String image_url, String categorie, String body_color, String favori){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_TITLE, titre);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_DATE_PUBLICATION, date_publication);
        contentValues.put(COLUMN_LINK, link);
        contentValues.put(COLUMN_IMAGE_URL, image_url);
        contentValues.put(COLUMN_CATEGORIE, categorie);
        contentValues.put(COLUMN_BODY_COLOR, body_color);
        contentValues.put(COLUMN_FAVORI, favori);

        String md5Titre = md5(titre);
        contentValues.put(COLUMN_HASH_TITLE, md5Titre);

        String md5Categorie = md5(categorie);
        contentValues.put(COLUMN_HASH_CATEGORIE, md5Categorie);

        db.insert(TABLE_NAME_NEWS, null, contentValues);
        db.close();
    }

    /**
     *
     *  Mise à jour du statut actif pour l'url donnée
     *
     */
    public void changeFavouriteNewsState(boolean favourite, String title) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORI, (favourite ? "visible" : "hidden"));

        db.update(TABLE_NAME_NEWS, values, COLUMN_HASH_TITLE + " LIKE ?", new String[] {md5(title)});
    }

    /**
     *
     *  Ajout d'une news en base
     *
     */
    public void addRssUrl(String url, String nom, String categorie, boolean actif) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_RSS_URL, url);
        contentValues.put(COLUMN_RSS_NOM, nom);
        contentValues.put(COLUMN_CATEGORIE, categorie);
        contentValues.put(COLUMN_RSS_ACTIVE, (actif ? "O" : "N"));
        contentValues.put(COLUMN_DATE_AJOUT, RssHelper.getDateDuJour());

        String md5String = md5(url);
        contentValues.put(COLUMN_HASH_URL, md5String);
        md5String = md5(nom);
        contentValues.put(COLUMN_HASH_NOM, md5String);
        md5String = md5(categorie);
        contentValues.put(COLUMN_HASH_CATEGORIE, md5String);

        db.insert(TABLE_NAME_RSS_SOURCE, null, contentValues);
        db.close();
    }

    /**
     *
     * Vérification si une news existe
     *
     * @param titre : titre
     *
     */
    public boolean newsExits(String titre) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { COLUMN_HASH_TITLE };
        String selection = COLUMN_HASH_TITLE + " = ?";
        String[] selectionArgs = { md5(titre) };
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME_NEWS, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    /**
     *
     * Suppersion d'une news en base
     *
     * @param titre : titre
     *
     */
    public boolean deleteNews(String titre) {

        SQLiteDatabase db = this.getReadableDatabase();

        String whereClause = COLUMN_HASH_TITLE + " = ?";
        String[] whereArgs = new String[] { md5(titre) };

        int delete = db.delete(TABLE_NAME_NEWS, whereClause, whereArgs);

        return (delete > 0);
    }


    /**
     *
     *  Récupération de toutes les news en base
     *
     */
    public List<NewsBean> getAllNews() {

        List<NewsBean> newsBeanList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] allColumns = new String[] { COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE_PUBLICATION, COLUMN_LINK, COLUMN_IMAGE_URL, COLUMN_CATEGORIE, COLUMN_BODY_COLOR, COLUMN_FAVORI, COLUMN_HASH_TITLE, COLUMN_HASH_CATEGORIE };
        Cursor cursor = db.query(TABLE_NAME_NEWS, allColumns, null, null, null, null, null);


        if(cursor.getCount() > 0) {

            NewsBean newsBean;

            while (cursor.moveToNext()) {

                newsBean = new NewsBean();

                newsBean.setNewsTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                newsBean.setNewsDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                newsBean.setNewsDatePublication(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_PUBLICATION)));
                newsBean.setNewsLink(cursor.getString(cursor.getColumnIndex(COLUMN_LINK)));
                newsBean.setNewsImageUrl(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)));
                newsBean.setNewsCategorie(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORIE)));
                newsBean.setNewsBodyColor(cursor.getString(cursor.getColumnIndex(COLUMN_BODY_COLOR)));
                newsBean.setNewsFavourite(cursor.getString(cursor.getColumnIndex(COLUMN_FAVORI)));
                newsBean.setNewsSaved("block");

                // vérification si le titre a déjà été lu grâce à la liste qui contient les news déjà cliquées
                if (RssHelper.listNewsTitleAlreadyReaded.contains(newsBean.getNewsTitle())) {
                    newsBean.setNewsBodyColor(RssHelper.BODY_COLOR_SELECT);
                }

                newsBeanList.add(newsBean);
            }
        }
        cursor.close();

        // on tri par date
        Collections.sort(newsBeanList);

        // on retourne la liste
        return newsBeanList;

    }

    /**
     *
     *  Récupération de toutes les url rss en base
     *
     */
    public List<UrlBean> getAllUrlBeanRss() {

        List<UrlBean> urlBeanList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] allColumns = new String[] { COLUMN_RSS_URL, COLUMN_RSS_NOM, COLUMN_CATEGORIE, COLUMN_RSS_ACTIVE, COLUMN_DATE_AJOUT };
        Cursor cursor = db.query(TABLE_NAME_RSS_SOURCE, allColumns, null, null, null, null, "ID DESC");

        if(cursor.getCount() > 0) {

            UrlBean urlBean;

            while (cursor.moveToNext()) {

                urlBean = new UrlBean();

                String url = cursor.getString(cursor.getColumnIndex(COLUMN_RSS_URL));
                urlBean.setUrl(url);
                // pour affichage dans liste des url rss de la config
                if (url.length() > 50) {
                    url = url.substring(0, 45);
                    url = url + "...";
                    urlBean.setUrlAffichageList(url);
                } else {
                    urlBean.setUrlAffichageList(url);
                }

                urlBean.setNom(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_NOM)));
                urlBean.setCategorie(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORIE)));
                urlBean.setActive(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_ACTIVE)).equals("O"));
                urlBean.setDateAjoutModification(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_AJOUT)));

                urlBeanList.add(urlBean);
            }
        }

        cursor.close();

        return urlBeanList;

    }

    /**
     *
     *  Récupération de toutes les url rss actives en base
     *
     */
    public List<UrlBean> getAllUrlBeanRssActive() {

        List<UrlBean> urlBeanList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] allColumns = new String[] { COLUMN_RSS_URL, COLUMN_RSS_NOM, COLUMN_CATEGORIE, COLUMN_RSS_ACTIVE, COLUMN_DATE_AJOUT };
        String whereClause = COLUMN_RSS_ACTIVE + " LIKE ?";
        String[] whereArgs = new String[] { "O" };
        Cursor cursor = db.query(TABLE_NAME_RSS_SOURCE, allColumns, whereClause, whereArgs, null, null, null);

        if(cursor.getCount() > 0) {

            UrlBean urlBean;

            while (cursor.moveToNext()) {

                urlBean = new UrlBean();

                urlBean.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_URL)));
                urlBean.setNom(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_NOM)));
                urlBean.setCategorie(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORIE)));
                urlBean.setActive(cursor.getString(cursor.getColumnIndex(COLUMN_RSS_ACTIVE)).equals("O"));
                urlBean.setDateAjoutModification(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_AJOUT)));

                urlBeanList.add(urlBean);
            }
        }

        cursor.close();

        Collections.sort(urlBeanList);

        return urlBeanList;

    }

    /**
     *
     *  Mise à jour du statut actif pour l'url donnée
     *
     */
    public int changeActiveUrlState(boolean actif, String url) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_RSS_ACTIVE, (actif ? "O" : "N"));

        return db.update(TABLE_NAME_RSS_SOURCE, values, COLUMN_HASH_URL + " LIKE ?", new String[] {md5(url)});
    }

    /**
     *
     *  Mise à jour du statut actif pour l'url donnée
     *
     */
    public boolean modifFluxRss(UrlBean urlBean) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RSS_NOM, urlBean.getNom());
        contentValues.put(COLUMN_CATEGORIE, urlBean.getCategorie());
        contentValues.put(COLUMN_RSS_ACTIVE, (urlBean.isActive() ? "O" : "N"));
        contentValues.put(COLUMN_DATE_AJOUT, RssHelper.getDateDuJour());

        String md5String = md5(urlBean.getUrl());
        contentValues.put(COLUMN_HASH_URL, md5String);
        md5String = md5(urlBean.getNom());
        contentValues.put(COLUMN_HASH_NOM, md5String);
        md5String = md5(urlBean.getCategorie());
        contentValues.put(COLUMN_HASH_CATEGORIE, md5String);

        int result = db.update(TABLE_NAME_RSS_SOURCE, contentValues, COLUMN_HASH_URL + " LIKE ?", new String[] {md5(urlBean.getUrl())});

        return (result == 1);
    }

    /**
     *
     *  Le fil existe déjà ?
     *
     *  @param url du fil
     *
     */
    public boolean fluxExists(String url) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { COLUMN_HASH_URL };
        String selection = COLUMN_HASH_URL + " = ?";
        String[] selectionArgs = { md5(url) };
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME_RSS_SOURCE, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    /**
     *
     *  Suppresion d'un fil
     *
     *  @param url du fil
     *
     */
    public boolean deleteFlux(String url) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_HASH_URL +" LIKE ?";
        String[] seletion_args = {md5(url)};

        int result = db.delete(TABLE_NAME_RSS_SOURCE, selection, seletion_args);

        return (result == 1);
    }

    /**
     *
     *  Récupération de toutes les news en base
     *
     */
    public List<String> getAllNewsTitle() {

        List<String> newsBeanListSavedTitle = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] allColumns = new String[] { COLUMN_TITLE };
        Cursor cursor = db.query(TABLE_NAME_NEWS, allColumns, null, null, null, null, null);

        if(cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                newsBeanListSavedTitle.add(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            }

        }

        cursor.close();

        return newsBeanListSavedTitle;

    }


    /**
     *
     * Modification du nom de la catégorie :
     *
     * - on modifie dans la table category
     * - on modifie aussi dans la table des ressources url contennant les flux rss
     *
     * @param oldCategoryName : ancien nom de la catégorie
     * @param newCategoryName : nouveau nom de la catégorie
     *
     */
    public boolean updateCategory(String oldCategoryName, String newCategoryName) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValuesCategory = new ContentValues();
        contentValuesCategory.put(COLUMN_RSS_CATEGORY, newCategoryName);
        contentValuesCategory.put(COLUMN_RSS_CATEGORY_HASH, md5(newCategoryName));

        // update du nom de la catégorie
        int resultCat = db.update(TABLE_RSS_CATEGORY, contentValuesCategory, COLUMN_RSS_CATEGORY_HASH + " LIKE ?", new String[] {md5(oldCategoryName)});

        ContentValues contentValuesRss = new ContentValues();
        contentValuesRss.put(COLUMN_CATEGORIE, newCategoryName);
        contentValuesRss.put(COLUMN_HASH_CATEGORIE, md5(newCategoryName));

        // update des noms catégories des RSS
        db.update(TABLE_NAME_RSS_SOURCE, contentValuesRss, COLUMN_HASH_CATEGORIE + " LIKE ?", new String[] {md5(oldCategoryName)});

        return (resultCat == 1);
    }

    /**
     *
     * Permet de déterminer si la catégorie à supprimer est liée à au moins un fils RSS
     *
     * @param categoryName : nom de la catégorie
     * @return boolean
     */
    public boolean categoryRssExist(String categoryName) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { COLUMN_HASH_CATEGORIE };
        String selection = COLUMN_HASH_CATEGORIE + " = ?";
        String[] selectionArgs = { md5(categoryName) };
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME_RSS_SOURCE, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);

        cursor.close();

        return exists;

    }

    /**
     *
     *  Suppresion d'une catégorie
     *
     *  @param categoryName nom
     *
     */
    public boolean deleteCategory(String categoryName) {

        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_RSS_CATEGORY_HASH +" LIKE ?";
        String[] seletion_args = {md5(categoryName)};

        int result = db.delete(TABLE_RSS_CATEGORY, selection, seletion_args);

        return (result == 1);
    }

    /**
     *
     * Permet de déterminer si la catégorie existe déjà
     *
     * @param categoryName : nom de la catégorie
     * @return boolean
     */
    public boolean categoryExist(String categoryName) {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { COLUMN_RSS_CATEGORY_HASH };
        String selection = COLUMN_RSS_CATEGORY_HASH + " = ?";
        String[] selectionArgs = { md5(categoryName) };
        String limit = "1";

        Cursor cursor = db.query(TABLE_RSS_CATEGORY, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);

        cursor.close();

        return exists;

    }

    /**
     *
     *  Ajout d'une categorie en base
     *
     */
    public boolean addCategory(String categoryName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_RSS_CATEGORY, categoryName);
        contentValues.put(COLUMN_RSS_CATEGORY_HASH, md5(categoryName));

        long result = db.insert(TABLE_RSS_CATEGORY, null, contentValues);
        db.close();

        return result != -1;
    }


    /**
     *
     *
     * Hashage string en md5
     *
     * @param s : md5
     *
     */
    private String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}