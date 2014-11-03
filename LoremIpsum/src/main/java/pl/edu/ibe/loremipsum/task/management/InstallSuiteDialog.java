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

package pl.edu.ibe.loremipsum.task.management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnFocusChange;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.testplatform.R;

/**
 * @author Mariusz Pluciński
 */
public abstract class InstallSuiteDialog extends ServiceDialogFragment {
    private static final String MANIFEST_URL_COMMON_PREFIX = "https://";

    @InjectView(R.id.manifest_url)
    EditText manifestUrl;

    @InjectView(R.id.username)
    EditText username;

    @InjectView(R.id.password)
    EditText password;
    private View dialogView;

    @OnFocusChange(R.id.manifest_url)
    protected void manifestUrlFocus() {
        if (manifestUrl.getText().toString().isEmpty()) {
            manifestUrl.setText(MANIFEST_URL_COMMON_PREFIX);
            manifestUrl.setSelection(MANIFEST_URL_COMMON_PREFIX.length());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_task_suite, null, false);
        ButterKnife.inject(this, dialogView);
        username.setText(getServiceProvider().taskSuites().getDownloadData().first);
        manifestUrl.setText(getServiceProvider().taskSuites().getDownloadData().second);
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dismissDialog(true);
                dismiss();
            }
            return false;
        });

        return new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.add_task_suite)
                .setPositiveButton(R.string.install, (dialog1, which) -> dismissDialog(true))
                .setNegativeButton(R.string.cancel, (dialog1, which) -> dismissDialog(false))
                .create();
    }

    public void dismissDialog(boolean accepted) {
        if (accepted) {
            getServiceProvider().taskSuites().storeDownloadData(username.getText().toString(), manifestUrl.getText().toString());
        }


        String manifestUrl = ((EditText) dialogView.findViewById(R.id.manifest_url)).getText().toString();
        String username = ((EditText) dialogView.findViewById(R.id.username)).getText().toString();
        String password = ((EditText) dialogView.findViewById(R.id.password)).getText().toString();


        dialogDismissed(manifestUrl, username,password,accepted);
    }

    protected abstract void dialogDismissed(String manifestUrl, String username, String password, boolean accepted);

}
