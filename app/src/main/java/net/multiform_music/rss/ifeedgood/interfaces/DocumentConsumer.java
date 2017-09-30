package net.multiform_music.rss.ifeedgood.interfaces;

import net.multiform_music.rss.ifeedgood.bean.NewsBean;
import net.multiform_music.rss.ifeedgood.helper.DatabaseHelper;

import java.util.List;

/**
 *
 * Interface de données de l'adapter
 *
 * Created by michel.dio on 27/03/2017.
 */

public interface DocumentConsumer {

    //void setXMLDocument(Document document);

    List<NewsBean> getNewsBeanList();

    void setNewsBeanList(List<NewsBean> newsBeanList);

    List<NewsBean> getCurrentNewsBeanList();

    void setCurrentNewsBeanList(List<NewsBean> currentNewsBeanList);

    int getNbrAsyncTaks();

    void setNbrAsyncTaks(int nbrAsyncTaks);

    int getNbrAsyncTaksFinished();

    void setNbrAsyncTaksFinished(int nbrAsyncTaksFinished);

    DatabaseHelper getDb();

    void reactivateIconRefreh();
}
