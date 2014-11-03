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

package pl.edu.ibe.loremipsum.tablet.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.task.management.collector.CollectorAgreementDialog;
import pl.edu.ibe.loremipsum.tools.RxExecutor;

/**
 * Created by adam on 21.03.14.
 */
public class SettingsDialog extends ServiceDialogFragment {

    @InjectView(R.id.task_rotation)
    CheckBox taskViewRotation;
    @InjectView(R.id.reporting_container)
    View reportingContainer;
    @InjectView(R.id.change_raport_agreement)
    View changReportSendingAgreement;
    @InjectView(R.id.current_agreement_status)
    TextView currentAgreementStatus;

    @OnClick(R.id.change_raport_agreement)
    void onChangeReportSendingAgreement() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        CollectorAgreementDialog dialog = new CollectorAgreementDialog() {

            @Override
            public void dialogDismissed(boolean agreed) {
                getServiceProvider().collector().updateAgreement(agreed).subscribe((ignore2) -> {
                    updateReportSendingStatus();
                });
            }
        };
        dialog.show(fragmentManager, "dialog");
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_settings, null, false);
        ButterKnife.inject(this, view);
        updateReportSendingStatus();
        updateRotationStatus();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())//
                .setCancelable(false)//
                .setPositiveButton(R.string.ok_text, (dialog, which) -> {
                    getServiceProvider().sharedPreferences().setRotateTaskView(taskViewRotation.isChecked());
                })//
                .setNegativeButton(R.string.cancel_text, (dialog, which) -> {
                })//
                .setTitle(getActivity().getString(R.string.set_menu))//
                .setView(view);//


        AlertDialog dialog = builder.create();

        return dialog;
    }

    private void updateRotationStatus() {
        taskViewRotation.setChecked(getServiceProvider().sharedPreferences().isTaskViewRotated());
    }

    private void updateReportSendingStatus() {
        if (getServiceProvider().collector().isReportingRequired()) {
            RxExecutor.runWithUiCallback(
                    getServiceProvider().collector().isSendingDataAllowed()).subscribe((allowed) -> {
                currentAgreementStatus.setText(allowed ? getActivity().getString(R.string.reports_sending_allowed)
                        : getActivity().getString(R.string.report_sending_not_allowed));
            });
        } else {
            reportingContainer.setVisibility(View.GONE);
        }
    }

}
