package net.multiform_music.rss.ifeedgood.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.multiform_music.rss.ifeedgood.R;
import net.multiform_music.rss.ifeedgood.adapter.RssAdapter;
import net.multiform_music.rss.ifeedgood.bean.ErrorBean;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * Classe static contenant des méthodes utilitaires
 *
 * Created by michel.dio on 30/03/2017.
 *
 *
 */

public class RssHelper {

    private static Properties appProperties;


    // contient la template html pour une news de la liste
    public static String webNewsTemplate;
    public static String webNewsDecriptionTemplate;
    public static String webPopupInformationTemplate;

    // contient donnée nécessaire pour simulation d'un click sur Webview
    public static float moveThresholdDp;

    // temps de pause en ms lors du chargment des news par les AsyncTask
    public static int threadSleepValue = 0;

    // couleurs des news lues/non lues
    public static final String BODY_COLOR_SELECT = "body_select";
    public static final String BODY_COLOR_NORMAL = "body_normal";

    // non de la SharedPreference contenant la liste des titres des news déjà lues
    public static final String LIST_SET_NEWS_ALREADY_READED_PREF = "NewsAlreadyReaded";

    // non de la SharedPreference contenant la liste des titres des news favorites
    public static final String LIST_SET_NEWS_FAVOURITES = "NewsFavourites";

    private static String adresseMailReceiver = "";
    private static NewsBean newsToSend = null;
    private static String mailTemplateNewsHtml = "";


    // liste des titres déjà lus, imporant quand
    // - rafraissiment de la liste des news
    // - on ferme l'application  puis on revient
    public static ArrayList<String> listNewsTitleAlreadyReaded = new ArrayList<>();

    // liste des titres favoris, imporant quand
    // - rafraissiment de la liste des news
    // - on ferme l'application  puis on revient
    public static ArrayList<String> listNewsTitleFavourite = new ArrayList<>();

    // liste des titres sauvergardées, imporant quand
    // - rafraissiment de la liste des news
    // - on ferme l'application  puis on revient
    public static ArrayList<String> listNewsTitleSaved = new ArrayList<>();

    // gestion des label webview
    public static String name;
    public static String category;
    public static String publicationDate;

    // Pattern pour les dates
    private static final String PATTERN_DATE_SOURCE = "EEE, dd MMM yyyy HH:mm:ss Z";
    private static final String PATTERN_DATE_FINALE = "dd/MM/yyyy HH:mm";

    // provider de recherche de feed rss
    public static String providerSearchRssUrl;

    // locale
    public static Locale locale;

    /**
     * inititialisation du helper
     *
     * @param contextMain : contexte MainActivity
     */
    @SuppressWarnings("deprecation")
    public static void init(Context contextMain) {

        moveThresholdDp = 20 * contextMain.getResources().getDisplayMetrics().density;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = contextMain.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = contextMain.getResources().getConfiguration().locale;
        }

        PropertyReader propertyReader = new PropertyReader(contextMain);
        appProperties = propertyReader.getMyProperties();

        providerSearchRssUrl = appProperties.getProperty("providerSearchRssUrl");

        setWebContentTemplate(contextMain);
        setData();

        // label pour webview liste de news
        if (locale.getLanguage().contains("fr") || locale.getLanguage().contains("FR")) {

            name = "Nom";
            category = "Catégorie";
            publicationDate = "Date de publication";

        } else {

            name = "Name";
            category = "Category";
            publicationDate = "Publication date";
        }
    }

    private static void setData() {

        threadSleepValue = Integer.valueOf(appProperties.getProperty("threadSleepValue"));

    }

    /**
     * Conversion d'une date suivant pattern
     *  @param dateSource : datasource
     * */
    public static String dateConversion(String dateSource) {

        String dateFinale;
        DateFormat sourceFormat = new SimpleDateFormat(RssHelper.PATTERN_DATE_SOURCE, Locale.US);
        DateFormat targetFormat = new SimpleDateFormat(RssHelper.PATTERN_DATE_FINALE);

        Date date = null;
        try {

            date = sourceFormat.parse(dateSource);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        dateFinale = targetFormat.format(date);

        return dateFinale;
    }

    /**
     * Chargement des web template :
     * <p>
     * - liste de news
     * - description news
     */
    private static void setWebContentTemplate(Context context) {

        String nameWebTemplate = appProperties.getProperty("webTemplateListNews");

        AssetManager assetManager = context.getAssets();
        InputStream input = null;
        String text = "";

        try {
            input = assetManager.open("webTemplates/" + nameWebTemplate);

            int size = input.available();
            byte[] buffer = new byte[size];
            final int read = input.read(buffer);
            if (read !=-1) {
                text = new String(buffer);
            } else {
                text = new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        webNewsTemplate = text;
        nameWebTemplate = appProperties.getProperty("webTemplateDescriptionNews");
        text = "";

        try {
            input = assetManager.open("webTemplates/" + nameWebTemplate);

            int size = input.available();
            byte[] buffer = new byte[size];
            final int read = input.read(buffer);
            if (read !=-1) {
                text = new String(buffer);
            } else {
                text = new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        webNewsDecriptionTemplate = text;

        nameWebTemplate = appProperties.getProperty("webTemplateDescriptionNews");

        try {
            input = assetManager.open("webTemplates/" + nameWebTemplate);

            int size = input.available();
            byte[] buffer = new byte[size];
            final int read = input.read(buffer);
            if (read !=-1) {
                text = new String(buffer);
            } else {
                text = new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        nameWebTemplate = appProperties.getProperty("mailTemplateNewsHtml");

        try {
            input = assetManager.open("mailTemplates/" + nameWebTemplate);

            int size = input.available();
            byte[] buffer = new byte[size];
            final int read = input.read(buffer);
            if (read !=-1) {
                text = new String(buffer);
            } else {
                text = new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mailTemplateNewsHtml = text;

        // template pour la popup information
        nameWebTemplate = appProperties.getProperty("webPopupInformationTemplate");

        try {
            input = assetManager.open("webTemplates/" + nameWebTemplate);

            int size = input.available();
            byte[] buffer = new byte[size];
            final int read = input.read(buffer);
            if (read !=-1) {
                text = new String(buffer);
            } else {
                text = new String(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        webPopupInformationTemplate = text;
    }


    public static void getMailReceiver(final WebView webview, NewsBean newsToSend) {

        RssHelper.newsToSend = newsToSend;

        // get mail_dialog.xml view
        LayoutInflater li = LayoutInflater.from(webview.getContext());
        View mailDialogView = li.inflate(R.layout.mail_dialog, new LinearLayout(webview.getContext()), false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(webview.getContext(), R.style.dialogTheme);
        alertDialogBuilder.setIcon(R.drawable.icon_mail);
        alertDialogBuilder.setTitle(webview.getContext().getString(R.string.mail_dialog_title));

        // set mail_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(mailDialogView);
        final EditText mailInput = (EditText) mailDialogView.findViewById(R.id.mailDialogInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.mail_dialog_send,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text

                                RssHelper.adresseMailReceiver = mailInput.getText().toString();
                                sendEmail(webview.getContext());
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
    }

    @SuppressWarnings("deprecation")
    private static void sendEmail(Context context) {

        // construction de la liste mail
        /*String[] mails = RssHelper.adresseMailReceiver.split(";");
        StringBuffer adresseMailConcat = new StringBuffer();
        for (int i = 1; i<=mails.length; i++) {
            adresseMailConcat.append(mails[i-1]);

            if (i < mails.length) {
                adresseMailConcat.append(",");
            }
        }*/

        mailTemplateNewsHtml = mailTemplateNewsHtml.replace("{0}", newsToSend.getNewsTitle());
        mailTemplateNewsHtml = mailTemplateNewsHtml.replace("{1}", newsToSend.getNewsDescription());
        mailTemplateNewsHtml = mailTemplateNewsHtml.replace("{2}", newsToSend.getNewsLink());

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + RssHelper.adresseMailReceiver +
                         "?subject=" + context.getString(R.string.mail_intent_subject);

        Uri uri = Uri.parse(uriText);
        emailIntent.setData(uri);
        if (Build.VERSION.SDK_INT >= 24) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mailTemplateNewsHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mailTemplateNewsHtml));
        }
        emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, mailTemplateNewsHtml);

        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.mail_intent_choice)));


        /*File file = new File(context.getCacheDir(), "mailTemplate.html");



        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(mailTemplate);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri myUri = Uri.parse("content://" + file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, myUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
        */
    }


    static String getDateDuJour() {

        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        return formatter.format(date);
    }



    public static ErrorBean verifRssUrl(String url, Context context) {

        URL urlDocument;
        InputStream stream;
        Document document;

        ErrorBean errorBean = new ErrorBean();

        /*

        /*

        boolean verifOk = Patterns.WEB_URL.matcher(url).matches();


        if (!verifOk) {

            errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_pattern));

            return errorBean;

        } else {*/

            try {

                urlDocument = new URL(url.trim());

            } catch (MalformedURLException e) {

                e.printStackTrace();

                Log.i("MalformedURLException", e.getMessage());

                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_connexion));

                return errorBean;
            }

            HttpURLConnection connection;

            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                connection = (HttpURLConnection) urlDocument.openConnection();
                int code = connection.getResponseCode();

                // si code != 200 => peut-être redirection => on récup Header Location
                if (code != 200) {

                    String redirectLocation = connection.getHeaderField("Location");

                    if (redirectLocation.contains("[")) {

                        redirectLocation = redirectLocation.replace("[", "");
                        redirectLocation = redirectLocation.replace("]", "");

                    }

                    urlDocument = new URL(redirectLocation.trim());
                    connection = (HttpURLConnection) urlDocument.openConnection();

                    errorBean.setMessage("Location");
                    errorBean.setNewUrl(redirectLocation);
                    errorBean.setError(false);
                }

                stream = connection.getInputStream();

            } catch (Exception e) {

                e.printStackTrace();
                Log.i("Exception", "Erreur sur lecteur dans connection ou strem");

                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_data));

                return errorBean;

            }


            try {

                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);


            } catch (Exception e) {

                e.printStackTrace();

                Log.i("IOException", e.getMessage());

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_no_xml));

                return errorBean;
            }

            NodeList nodeList = document.getElementsByTagName("item");

            if (nodeList == null || nodeList.getLength() == 0) {

                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_rss));

                return errorBean;
            }

        //}

        return errorBean;

    }


    public static void afficheToastError(Activity activity, String texte) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View layoutErrorToast = inflater.inflate(R.layout.custom_toast_error, (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        Toast custToast = new Toast(activity);
        TextView textSaveToast = (TextView) layoutErrorToast.findViewById(R.id.toast_error_text);
        textSaveToast.setText(texte);
        custToast.setView(layoutErrorToast);
        custToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        custToast.setDuration(Toast.LENGTH_LONG);
        custToast.show();
    }

    public static void afficheToastOk(Activity activity, String texte) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View layoutErrorToast = inflater.inflate(R.layout.custom_toast_ok, (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        Toast custToast = new Toast(activity);
        TextView textSaveToast = (TextView) layoutErrorToast.findViewById(R.id.toast_ok_text);
        textSaveToast.setText(texte);
        custToast.setView(layoutErrorToast);
        custToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        custToast.setDuration(Toast.LENGTH_SHORT);
        custToast.show();
    }

    /**
     *
     *  Construction de la liste des catégorie et maps categorie/liste news
     *
     *  @param rssAdapter : adapter
     *
     */

    public static void constructMapCategory(RssAdapter rssAdapter) {

        // détermination des catégories / nom feed par category (hashmap)
        List<NewsBean> listNews = rssAdapter.getCurrentNewsBeanList();
        List<String> categoryList = new ArrayList<>();
        HashMap<String, List<String>> categoryFeedMap = new HashMap<>();
        for (NewsBean news : listNews) {
            if (!categoryList.contains(news.getNewsCategorie())) {
                categoryList.add(news.getNewsCategorie());
            }

            // si la hash contient déjà la catégorie
            if (categoryFeedMap.containsKey(news.getNewsCategorie())) {
                List<String> namesFeed = categoryFeedMap.get(news.getNewsCategorie());
                // on ajoute que si la liste des feeds ne contient pas déjà ce nom
                if (!namesFeed.contains(news.getNewsNom())) {
                    namesFeed.add(news.getNewsNom());
                    categoryFeedMap.remove(news.getNewsCategorie());
                    categoryFeedMap.put(news.getNewsCategorie(), namesFeed);
                }
            } else {
                List<String> namesFeed = new ArrayList<>();
                namesFeed.add(news.getNewsNom());
                categoryFeedMap.put(news.getNewsCategorie(), namesFeed);
            }
        }

        // tri des listes CATEGORY
        Collections.sort(categoryList);
        // ajout de la catégorie générique pour spinner si plus d'une catégorie
        if (categoryList.size() > 1) {
            categoryList.add(0, rssAdapter.getContext().getString(R.string.category_select));
        }

        rssAdapter.setCategoryList(categoryList);

        // tri des listes NAMEFEED
        HashMap<String, List<String>> categoryFeedMapFinal = new HashMap<>();
        for (Map.Entry mapentry : categoryFeedMap.entrySet()) {

            String category = (String)mapentry.getKey();
            ArrayList namesFeed = (ArrayList) mapentry.getValue();
            Collections.sort(namesFeed);
            categoryFeedMapFinal.put(category, namesFeed);
        }

        rssAdapter.setCategoryFeedMap(categoryFeedMapFinal);


        // construction de la hashmap : caterogy/news
        HashMap<String, List<NewsBean>> categoryNewsMap = new HashMap<>();
        for (String category : categoryList) {
            List<NewsBean> listNewsCategory = new ArrayList<>();

            for (NewsBean news : listNews) {
                if (news.getNewsCategorie().equals(category)) {
                    listNewsCategory.add(news);
                }
            }
            Collections.sort(listNewsCategory);
            categoryNewsMap.put(category, listNewsCategory);
        }

        rssAdapter.setCategoryNewsMap(categoryNewsMap);

        // intialisation du Spinner de la MainActivity
        rssAdapter.initCustomSpinner();
    }

    /**
     *
     *  Essais de retrouver l'url de li'mage dans le tag "enclosure" n'est pas présent
     *
     *  @param  item : Element source
     *
     */
    public static String getUrlImageFromString(Element item) {

        String input = extractTextChildren(item);
        String inputInit = input;

        int indexImg = input.indexOf("jpg");

        if (indexImg == -1) {
            indexImg = input.indexOf("jpeg");
        }
        if (indexImg == -1) {
            indexImg = input.indexOf("JPG");
        }
        if (indexImg == -1) {
            indexImg = input.indexOf("JPEG");
        }
        if (indexImg == -1) {
            indexImg = input.indexOf("png");
        }
        if (indexImg == -1) {
            indexImg = input.indexOf("PNG");
        }

        String urlImage = "";

        if (indexImg != -1) {

            int index = input.indexOf("http");
            int indexPrevious = 0;
            while (index != -1) {

                indexPrevious = indexPrevious + index;

                input = input.substring(index + 1);
                index = input.indexOf("http");

                // on a dépassé li'mage
                if ((index + indexPrevious) > indexImg || index == -1) {

                    urlImage = inputInit.substring(indexPrevious, indexImg + 3);
                    urlImage = urlImage.replaceAll("\"", "");
                    urlImage = urlImage.substring(urlImage.indexOf("http"));
                    break;
                }
            }
        }

        return urlImage;
    }

    /**
     *
     *  TRansforme le DOM Element en String
     *
     *  @param  parentNode : element source
     *
     */
    private static String extractTextChildren(Element parentNode) {

        String strObject = "";

        try {

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(parentNode);
            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(source, result);
            strObject = result.getWriter().toString();

        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return strObject;
    }

    /**
     * Détermine si une connexion réseau est active
     *
     * @param context context
     * @return boolean etat
     */
    public static boolean isNetworkOk(Context context) {

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&  activeNetwork.isConnectedOrConnecting();
    }

}


