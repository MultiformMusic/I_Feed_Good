package net.multiform_music.rss.ifeedgood.helper;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by michel.dio on 29/03/2017.
 *
 */

class PropertyReader {

    private final Context context;
    private final Properties properties;

    PropertyReader(Context context) {
        this.context = context;
        properties = new Properties();
    }

    Properties getMyProperties() {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("conf/application.properties");
            properties.load(inputStream);

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        return properties;
    }
}