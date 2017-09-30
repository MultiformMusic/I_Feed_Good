package net.multiform_music.rss.ifeedgood.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import net.multiform_music.rss.ifeedgood.R;

import net.multiform_music.rss.ifeedgood.RssConfigActivity;
import net.multiform_music.rss.ifeedgood.adapter.CustomSpinnerCategoryAdapter;
import net.multiform_music.rss.ifeedgood.adapter.UrlAdapter;
import net.multiform_music.rss.ifeedgood.bean.ErrorBean;
import net.multiform_music.rss.ifeedgood.bean.UrlBean;
import net.multiform_music.rss.ifeedgood.helper.DatabaseHelper;
import net.multiform_music.rss.ifeedgood.helper.RssHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.multiform_music.rss.ifeedgood.R.id.rssConfigCategorie;
import static net.multiform_music.rss.ifeedgood.R.id.rssUrlDialogNom;
import static net.multiform_music.rss.ifeedgood.R.id.textViewRssUrl;
import static net.multiform_music.rss.ifeedgood.RssConfigActivity.db;

/**
 * Created by michel.dio on 18/04/2017.
 *
 */

public class UrlViewHolder extends RecyclerView.ViewHolder {

    private UrlAdapter urlAdapter;

    private final TextView nomTextview;
    private final TextView urlTextview;
    private final TextView urlHiddenTextview;
    private final TextView categoryTextview;
    private final TextView activeTextview;
    private final Switch switchActif;

    private final ImageButton buttonEdit;
    private final ImageButton buttonDelete;

    private AlertDialog dialogEdit;

    public UrlViewHolder(View itemView) {
        super(itemView);

        nomTextview = (TextView) itemView.findViewById(R.id.nomlList);
        urlTextview = (TextView) itemView.findViewById(R.id.urlList);
        urlHiddenTextview = (TextView) itemView.findViewById(R.id.urlListHidden);
        categoryTextview = (TextView) itemView.findViewById(R.id.categoryList);
        activeTextview = (TextView) itemView.findViewById(R.id.activeList);
        switchActif = (Switch) itemView.findViewById(R.id.switchActif);

        /*
        Listener sur le switch activation flux
        */
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked && activeTextview.getText().toString().equals(urlAdapter.getYes())) {
                    return;
                } else if (!isChecked && activeTextview.getText().toString().equals(urlAdapter.getNo())) {
                    return;
                }

                DatabaseHelper db = RssConfigActivity.db;

                int result = db.changeActiveUrlState(isChecked, urlTextview.getText().toString());
                Context context = urlAdapter.getContext();

                // vérification si update bien passé
                if (result == 1) {

                    if (isChecked) {
                        activeTextview.setText(urlAdapter.getYes());
                    } else {
                        activeTextview.setText(urlAdapter.getNo());
                    }

                    RssHelper.afficheToastOk(urlAdapter.getContext(), isChecked ? context.getResources().getString(R.string.rss_list_active_change_actif_ok) : context.getResources().getString(R.string.rss_list_active_change_inactif_ok));

                    // échec update
                } else {

                    RssHelper.afficheToastOk(urlAdapter.getContext(), isChecked ? context.getResources().getString(R.string.rss_list_active_change_actif_nok) : context.getResources().getString(R.string.rss_list_active_change_inactif_nok));
                }
            }
        };
        switchActif.setOnCheckedChangeListener(onCheckedChangeListener);
        buttonDelete = (ImageButton) itemView.findViewById(R.id.buttonDelete);

        /*
       Listener sur le bouton supprimer
     */
        View.OnClickListener clickListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonDelete.setImageResource(R.drawable.icon_delete_url_clicked);

                // construction de la dialog de confirmation de suppression
                LayoutInflater li = LayoutInflater.from(view.getContext());
                View urlDeleteDialogView = li.inflate(R.layout.rss_delete_dialog, new LinearLayout(view.getContext()), false);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext(), R.style.dialogTheme);
                alertDialogBuilder.setTitle(view.getContext().getString(R.string.rss_url_dialog_titre_delete));
                alertDialogBuilder.setIcon(R.drawable.icon_confirm_delete);
                alertDialogBuilder.setView(urlDeleteDialogView);

                TextView deleteNom = (TextView) urlDeleteDialogView.findViewById(R.id.nomlListDelete);
                deleteNom.setText(nomTextview.getText().toString());

                TextView deleteUrl = (TextView) urlDeleteDialogView.findViewById(R.id.urlTitreDelete);
                deleteUrl.setText(urlTextview.getText().toString());

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.common_delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        DatabaseHelper db = RssConfigActivity.db;
                                        boolean result = db.deleteFlux(urlHiddenTextview.getText().toString());

                                        Context context = urlAdapter.getContext();

                                        if (result) {

                                            RssHelper.afficheToastOk(urlAdapter.getContext(), context.getResources().getString(R.string.rss_url_dialog_delete_ok));
                                            urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
                                            urlAdapter.notifyDataSetChanged();

                                            dialog.dismiss();

                                        } else {
                                            RssHelper.afficheToastError(urlAdapter.getContext(), context.getResources().getString(R.string.rss_url_dialog_delete_nok));
                                        }

                                        buttonDelete.setImageResource(R.drawable.icon_delete_url);

                                    }
                                })
                        .setNegativeButton(R.string.rss_config_dialog_annuler,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        buttonDelete.setImageResource(R.drawable.icon_delete_url);
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                if(alertDialog.getWindow() != null) {
                    alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationFade;
                }

                // show it
                alertDialog.show();
            }
        };
        buttonDelete.setOnClickListener(clickListenerDelete);

        buttonEdit = (ImageButton) itemView.findViewById(R.id.buttonEdit);

        /*
      Listener sur le bouton Editer
     */
        View.OnClickListener clickListenerEdit = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEdit.setImageResource(R.drawable.icon_edit_url_clicked);

                // construction de la dialog de modification fil
                LayoutInflater li = LayoutInflater.from(view.getContext());
                View urlEditDialogView = li.inflate(R.layout.rss_edit_dialog, new LinearLayout(view.getContext()), false);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext(), R.style.dialogTheme);
                alertDialogBuilder.setTitle(view.getContext().getString(R.string.rss_url_dialog_titre_edit));
                alertDialogBuilder.setIcon(R.drawable.icon_edit_url);
                alertDialogBuilder.setView(urlEditDialogView);

                TextView url = (TextView) urlEditDialogView.findViewById(textViewRssUrl);
                // récupération véritable valeur url dans champ caché
                url.setText(urlHiddenTextview.getText().toString());

                EditText nom = (EditText) urlEditDialogView.findViewById(rssUrlDialogNom);
                nom.setText(nomTextview.getText().toString());

                CheckBox actif = (CheckBox) urlEditDialogView.findViewById(R.id.rssActif);
                actif.setChecked(switchActif.isChecked());

                // liste déroulante catégorie
                Spinner spinnerCategorie = (Spinner) urlEditDialogView.findViewById(rssConfigCategorie);
                Resources res = urlAdapter.getContext().getResources();
                String[] categories = res.getStringArray(R.array.categories_array);
                List<String> categorieList = new ArrayList<>();

                Collections.addAll(categorieList, categories);

                CustomSpinnerCategoryAdapter customSpinnerCategoryAdapter = new CustomSpinnerCategoryAdapter(view.getContext(), (ArrayList<String>) categorieList);
                spinnerCategorie.setAdapter(customSpinnerCategoryAdapter);
                spinnerCategorie.setSelection(getIndex(spinnerCategorie, categoryTextview.getText().toString()));

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.common_record,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                })
                        .setNegativeButton(R.string.rss_config_dialog_annuler,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        buttonEdit.setImageResource(R.drawable.icon_edit_url);
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                dialogEdit = alertDialog;

                // show it
                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationFade;
                    alertDialog.show();
                    Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    theButton.setOnClickListener(new CustomListenerEdit(alertDialog));
                }
            }
        };

        buttonEdit.setOnClickListener(clickListenerEdit);
    }

    /**
     *
     * listener custom sur alert dialog d'ajout d'une url
     *
     */
    private class CustomListenerEdit implements View.OnClickListener {
        private final Dialog dialog;
        CustomListenerEdit(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {

            // récupération des données
            String url = null;
            TextView tempTextview = (TextView) dialogEdit.findViewById(textViewRssUrl);
            if (tempTextview != null) {
                url = tempTextview.getText().toString();
            }
            String nom = null;
            EditText tempEditText = (EditText) dialogEdit.findViewById(rssUrlDialogNom);
            if (tempEditText != null) {
                nom = tempEditText.getText().toString();
            }
            Spinner spinnerCategorie = (Spinner) dialogEdit.findViewById(rssConfigCategorie);
            String categorie = null;
            if (spinnerCategorie != null) {
                categorie = spinnerCategorie.getSelectedItem().toString();
            }
            boolean actif = false;
            CheckBox tempCheckBox = (CheckBox) dialogEdit.findViewById(R.id.rssActif);
            if (tempCheckBox != null) {
                actif = tempCheckBox.isChecked();
            }

            // vérification catégorie sélectionnée
            ErrorBean errorBean  = new ErrorBean();

            Context context = urlAdapter.getContext();

            long intemSeleceted = 0;
            if (spinnerCategorie != null) {
                intemSeleceted = spinnerCategorie.getSelectedItemId();
            }
            if (intemSeleceted == 0) {
                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_url_error_category));
                RssHelper.afficheToastError((Activity) context, errorBean.getMessage());

                return;
            }

            if (nom != null && nom.trim().length() == 0) {
                errorBean.setMessage(context.getResources().getString(R.string.rss_config_dialog_nom_error_vide));
                RssHelper.afficheToastError((Activity) context, errorBean.getMessage());
                return;
            }

            // modification
            UrlBean urlBean = new UrlBean();
            urlBean.setUrl(url);
            urlBean.setNom(nom);
            urlBean.setCategorie(categorie);
            urlBean.setActive(actif);
            boolean ajoutOk = db.modifFluxRss(urlBean);

            if (!ajoutOk) {

                RssHelper.afficheToastError((Activity) context, context.getResources().getString(R.string.rss_url_dialog_edit_nok));

            } else {

                urlAdapter.setUrlBeanList(db.getAllUrlBeanRss());
                urlAdapter.notifyDataSetChanged();

                dialog.dismiss();

                RssHelper.afficheToastOk((Activity) context, context.getResources().getString(R.string.rss_url_dialog_edit_ok));
            }
            buttonEdit.setImageResource(R.drawable.icon_edit_url);
        }
    }

    /**
     *
     *  Permet de sélectionner dans spinner ma valeur passée en argument
     *
     *  @param value : valeur à positionner
     *  @param spinner : spinner
     *
     */
    private int getIndex(Spinner spinner, String value)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)){
                index = i;
                break;
            }
        }
        return index;
    }

    public void setUrlAdapter(UrlAdapter urlAdapter) {
        this.urlAdapter = urlAdapter;
    }

    public TextView getUrlTextview() {
        return urlTextview;
    }

    public TextView getNomTextview() {
        return nomTextview;
    }

    public TextView getCategoryTextview() {
        return categoryTextview;
    }

    public TextView getActiveTextview() {
        return activeTextview;
    }

    public Switch getSwitchActif() {
        return switchActif;
    }

    public TextView getUrlHiddenTextview() {
        return urlHiddenTextview;
    }
}
