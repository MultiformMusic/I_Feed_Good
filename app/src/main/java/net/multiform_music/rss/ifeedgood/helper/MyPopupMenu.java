package net.multiform_music.rss.ifeedgood.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.View;

/**
 * Created by michel.dio on 06/04/2017.
 *
 */

public class MyPopupMenu extends PopupMenu {

    private final View view;

    public MyPopupMenu(@NonNull Context context, @NonNull View anchor) {
        super(context, anchor);
        this.view = anchor;
    }

    public View getView() {
        return view;
    }

}
