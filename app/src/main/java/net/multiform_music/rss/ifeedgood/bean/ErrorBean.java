package net.multiform_music.rss.ifeedgood.bean;

/**
 * Created by Michel on 17/04/2017.
 *
 */

public class ErrorBean {

    private boolean error;
    private String message;
    private String newUrl;

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.error = true;
    }

    public String getNewUrl() {
        return newUrl;
    }

    public void setNewUrl(String newUrl) {
        this.newUrl = newUrl;
    }


    public void setError(boolean error) {
        this.error = error;
    }
}
