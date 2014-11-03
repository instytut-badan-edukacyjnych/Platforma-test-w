/*
 * This file is part of Test Platform.
 *
 * Test Platform is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Test Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Test Platform; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Ten plik jest częścią Platformy Testów.
 *
 * Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 *
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 *
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pl.edu.ibe.loremipsum.tablet.researcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherDao;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tablet.base.BaseDialogFragment;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.StringUtils;

/**
 * Dialog used to create/delete/update researcher.
 * <p>
 * Created by adam on 11.03.14.
 */
public abstract class ResearcherDialog extends ServiceDialogFragment implements View.OnClickListener {

    public static final long NO_RESERCHER_CHOOSEN = -1;
    public static final String RESEARCHER_ID = "id";
    private static final int HANDLE_CLICK = 1;
    private static final int HANDLE_DB_INSERT = 2;
    private static final int HANDLE_DB_UPDATE = 3;
    @InjectView(R.id.readable_id)
    EditText textId;
    @InjectView(R.id.firstname)
    EditText firstName;
    @InjectView(R.id.surname)
    EditText surName;
    @InjectView(R.id.password)
    EditText password;
    @InjectView(R.id.repeated_password)
    EditText repeatedPassword;
    private Long researcherId;
    private ResearcherDao daoSession;
    private Researcher researcher;
    private AlertDialog dialog;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_researcher, null, false);
        ButterKnife.inject(this, view);

        researcherId = getArguments().getLong(RESEARCHER_ID);
        daoSession = LoremIpsumApp.obtain().getDbHelper().getDaoSession().getResearcherDao();
        if (researcherId == NO_RESERCHER_CHOOSEN) {
            researcher = new Researcher();
        } else {
            getServiceProvider().researcher().getResearcher(researcherId).subscribe((researcher) -> {
                this.researcher = researcher;
                textId.setText(researcher.getTextId());
                textId.setEnabled(false);
                firstName.setText(researcher.getFirstName());
                surName.setText(researcher.getSurName());
                password.setText(researcher.getPassword());
                repeatedPassword.setText(researcher.getPassword());
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())//
                .setCancelable(false)//
                .setTitle(getActivity().getString(R.string.create_new_accoutn))
                .setPositiveButton(R.string.ok_text, null)//
                .setNegativeButton(R.string.cancel_text, null);//
        if (researcherId != NO_RESERCHER_CHOOSEN) {
            builder.setNeutralButton(R.string.del_menu, null);//
        }
        builder.setView(view);//


        AlertDialog dialog = builder.create();

        repeatedPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(dialog.getButton(Dialog.BUTTON_POSITIVE));
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(this);
            if (researcherId != NO_RESERCHER_CHOOSEN) {
                dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener(this);
            }
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == dialog.getButton(Dialog.BUTTON_POSITIVE).getId()) {
            handlePositiveButton();
        } else if (v.getId() == dialog.getButton(Dialog.BUTTON_NEUTRAL).getId()) {
            if (researcherId != NO_RESERCHER_CHOOSEN) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ConfirmDialog dialog = new ConfirmDialog() {

                    @Override
                    public void confirmed() {
                        daoSession.delete(researcher);
                        researchersUpdated(null);
                        ResearcherDialog.this.dismiss();
                    }
                };
                dialog.show(fragmentManager, "confirmDialog");
            }
        } else if (v.getId() == dialog.getButton(Dialog.BUTTON_NEGATIVE).getId()) {
            dismiss();
        } else {
            throw new RuntimeException("Unknow id: " + v.getId());
        }
    }

    /**
     * Handles positive button click. Updates or creates new Researcher.
     */
    private void handlePositiveButton() {

        RxExecutor.runWithUiCallback(getServiceProvider().researcher().researcherExist(textId.getText().toString()))
                .subscribe((exist) -> {
                    if (repeatedPassword.getText().toString().equals(password.getText().toString())//
                            && !StringUtils.isEmpty(repeatedPassword.getText().toString())//
                            && !StringUtils.isEmpty(password.getText().toString())//
                            && !StringUtils.isEmpty(textId.getText().toString())//
                            && !StringUtils.isEmpty(firstName.getText().toString())//
                            && !StringUtils.isEmpty(surName.getText().toString())
                            && repeatedPassword.getText().length() > 5
                            && password.getText().length() > 5
                            && textId.getText().length() > 5) {

                        if (researcherId == NO_RESERCHER_CHOOSEN) {
                            if (exist) {
                                textId.setError(getString(R.string.mes_exist));
                            } else {
                                updateResercherData();
                                getServiceProvider().researcher().insertResearcher(researcher).subscribe((id) -> {
                                    researchersUpdated(researcher);
                                    dismiss();
                                });
                            }
                        } else {
                            if (exist && researcherId == NO_RESERCHER_CHOOSEN) {
                                textId.setError(getString(R.string.mes_exist));
                            } else {
                                updateResercherData();
                                getServiceProvider().researcher().updateResearcher(researcher).subscribe((nullReturn) -> {
                                    researchersUpdated(researcher);
                                    dismiss();
                                });
                            }
                        }
                    } else {
                        if (StringUtils.isEmpty(repeatedPassword.getText().toString())//
                                || StringUtils.isEmpty(password.getText().toString())//
                                || StringUtils.isEmpty(textId.getText().toString())//
                                || StringUtils.isEmpty(firstName.getText().toString())//
                                || StringUtils.isEmpty(surName.getText().toString())) {
                            validateField(repeatedPassword);
                            validateField(password);
                            validateField(textId);
                            validateField(firstName);
                            validateField(surName);
                        }
                        if (!repeatedPassword.getText().toString().equals(password.getText().toString())) {
                            repeatedPassword.setError(getString(R.string.mes_pass_no_equ));
                            password.setError(getString(R.string.mes_pass_no_equ));
                        } else if (repeatedPassword.getText().length() < 6 && password.getText().length() < 6) {
                            repeatedPassword.setError(getString(R.string.password_too_short));
                            password.setError(getString(R.string.password_too_short));
                        } else {
                            repeatedPassword.setError(null);
                            password.setError(null);
                        }
                        if (textId.getText().length() < 6) {
                            textId.setError(getString(R.string.login_too_short));
                        } else {
                            textId.setError(null);
                        }

                    }
                });
    }

    private void validateField(EditText editText) {
        if (StringUtils.isEmpty(editText.getText().toString())) {
            editText.setError(getString(R.string.mes_empty));
        } else {
            editText.setError(null);
        }
    }


    private void updateResercherData() {
        researcher.setTextId(textId.getText().toString());
        researcher.setFirstName(firstName.getText().toString());
        researcher.setSurName(surName.getText().toString());
        researcher.setPassword(password.getText().toString());
        researcher.setHidden(false);
    }

    /**
     * Action triggered after update or creation new researcher
     */
    public abstract void researchersUpdated(Researcher researcher);

    /**
     * Confirm dialog showed before researcher deletion.
     */
    private abstract class ConfirmDialog extends BaseDialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())//
                    .setCancelable(false)//
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            confirmed();
                        }
                    })//
                    .setNegativeButton(R.string.cancel_text, null)//
                    .setTitle(R.string.mes_del_card);
            return builder.create();
        }

        /**
         * Method triggered after click on confirm button.
         */
        public abstract void confirmed();

    }

}
