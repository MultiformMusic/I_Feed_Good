package net.multiform_music.rss.ifeedgood.task;

import android.os.AsyncTask;

import net.multiform_music.rss.ifeedgood.adapter.RssAdapter;
import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;
import net.multiform_music.rss.ifeedgood.interfaces.DocumentConsumer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by michel.dio on 27/03/2017.
 *
 */

public class XMLAsyncTask extends AsyncTask<UrlBean, Void, Document> {

    private final DocumentConsumer consumer;

    public XMLAsyncTask (DocumentConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Document doInBackground(UrlBean... urlBeen) {

        try {

            Thread.sleep(RssHelper.threadSleepValue);

            URL url = new URL(urlBeen[0].getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();

            try {

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

                int nbrItemtLength = document.getElementsByTagName("item").getLength();

                String categorie = urlBeen[0].getCategorie();
                String nom = urlBeen[0].getNom();

                // récupération des données et construction de la liste de news
                for (int i = 0; i < nbrItemtLength; i++) {

                    Element item = (Element) document.getElementsByTagName("item").item(i);
                    String currentTitle = item.getElementsByTagName("title").item(0).getTextContent();
                    String currentDescription = item.getElementsByTagName("description").item(0).getTextContent();
                    String currentLink = item.getElementsByTagName("link").item(0).getTextContent();
                    String currentDatePublication = RssHelper.dateConversion(item.getElementsByTagName("pubDate").item(0).getTextContent());

                    String currentImageUrl;

                    if (item.getElementsByTagName("enclosure") != null && item.getElementsByTagName("enclosure").getLength() >= 0
                            && item.getElementsByTagName("enclosure").item(0) != null) {
                        currentImageUrl = item.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem("url").getTextContent();

                    } /*else if (currentDescription != null && currentDescription.contains("<img")) {

                        StringBuilder stringBuilder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>");
                        stringBuilder.append("<root>");
                        String temp = currentDescription.replace("<![CDATA[", "");
                        temp = temp.replace("]]", "");
                        stringBuilder.append(temp);
                        stringBuilder.append("</root>");

                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(stringBuilder.toString()));

                        Document doc = db.parse(is);
                        if (doc.getElementsByTagName("img") != null) {
                            currentImageUrl = doc.getElementsByTagName("img").item(0).getAttributes().getNamedItem("src").getTextContent();
                        }
                    }*/

                    else {
                        currentImageUrl = RssHelper.getUrlImageFromString(item);
                    }

                    if (currentImageUrl != null && !currentImageUrl.contains("http")) {
                        currentImageUrl = "";
                    }

                    NewsBean newsBean = new NewsBean(currentTitle, currentDescription, currentLink, currentDatePublication, currentImageUrl, categorie, "", "", nom);

                    // vérification si le titre a déjà été lu grâce à la liste qui contient les news déjà cliquées
                    if (RssHelper.listNewsTitleAlreadyReaded.contains(currentTitle)) {
                        newsBean.setNewsBodyColor(RssHelper.BODY_COLOR_SELECT);
                    }

                    // vérification si le titre est un favoris

                    if (RssHelper.listNewsTitleFavourite.contains(currentTitle)) {
                        newsBean.setNewsFavourite("visible");
                    } else {
                        newsBean.setNewsFavourite("hidden");
                    }

                    // vérification si le titre est sauvegardé
                    if (RssHelper.listNewsTitleSaved.contains(currentTitle)) {
                        newsBean.setNewsSaved("block");
                    } else {
                        newsBean.setNewsSaved("none");
                    }

                    // ajout du bean à la liste des news
                    consumer.getNewsBeanList().add(newsBean);

                }

                // tri de la liste par date décroissante
                Collections.sort(consumer.getNewsBeanList());

                return document;

            } finally {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        } catch (InterruptedException iex) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Document result) {

        this.consumer.setNbrAsyncTaksFinished(this.consumer.getNbrAsyncTaksFinished() + 1);

        // tous les asynck ont terminé le traitement => on traite les données
        if (this.consumer.getNbrAsyncTaksFinished() == this.consumer.getNbrAsyncTaks()) {

            List<NewsBean> fromBase = consumer.getDb().getAllNews();
            List<NewsBean> fromSite = consumer.getNewsBeanList();
            List<NewsBean> allNews = new ArrayList<>(fromSite);

            for (NewsBean newsBase : fromBase) {

                boolean addNews = true;

                for (NewsBean newsSite : fromSite) {

                    if (newsSite.getNewsTitle().equals(newsBase.getNewsTitle())) {

                        addNews = false;
                    }
                }

                if (addNews) {
                    newsBase.setNewsSaved("block");
                    allNews.add(newsBase);
                }
            }

            // tri de la liste des news par date décroissante
            this.consumer.setCurrentNewsBeanList(allNews);
            Collections.sort(consumer.getCurrentNewsBeanList());
            this.consumer.setNewsBeanList(allNews);
            Collections.sort(consumer.getNewsBeanList());

            // construction de la liste des categories et map categorie/liste news
            RssHelper.constructMapCategory((RssAdapter) consumer);

            // réactivation de l'incone refresh
            this.consumer.reactivateIconRefreh();

            this.consumer.setNbrAsyncTaksFinished(0);
            this.consumer.setNbrAsyncTaks(0);
        }
    }
}
