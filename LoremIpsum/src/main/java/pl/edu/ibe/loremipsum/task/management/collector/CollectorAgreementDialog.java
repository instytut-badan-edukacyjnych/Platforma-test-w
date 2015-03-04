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

package pl.edu.ibe.loremipsum.task.management.collector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;

import pl.edu.ibe.loremipsum.configuration.CollectorConfig;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 17.04.14.
 */
public abstract class CollectorAgreementDialog extends ServiceDialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        CollectorConfig collectorConfig = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().collectorConfig;
        PermissionAdapted permissionAdapted = new PermissionAdapted(getActivity(), android.R.layout.simple_list_item_1);
        ListView view = (ListView) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_collector_agreement, null, false);
        fillAdapter(collectorConfig, permissionAdapted);
        view.setAdapter(permissionAdapted);


        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setPositiveButton(getActivity().getString(R.string.agree), (dialog1, which) -> {
                    dialogDismissed(true);
                })
                .setNegativeButton(getActivity().getString(R.string.nagree), (dialog1, which) -> {
                    dialogDismissed(false);
                })
                .setTitle(getActivity().getString(R.string.report_request))
                .setView(view)
                .create();
        return dialog;
    }

    private void fillAdapter(CollectorConfig collectorConfig, PermissionAdapted permissionAdapted) {
        if (collectorConfig.sendExamineeFullname) {
            permissionAdapted.add(getActivity().getString(R.string.examinee_fullname));
        }
        if (collectorConfig.sendExamineeBirthday) {
            permissionAdapted.add(getActivity().getString(R.string.examinee_birthday));
        }
        if (collectorConfig.sendExamineeGender) {
            permissionAdapted.add(getActivity().getString(R.string.examinee_gender));
        }
        if (collectorConfig.sendExamineeId) {
            permissionAdapted.add(getActivity().getString(R.string.examinee_id));
        }
        if (collectorConfig.sendInstitutionId) {
            permissionAdapted.add(getActivity().getString(R.string.institution_id));
        }
        if (collectorConfig.sendResearcherId) {
            permissionAdapted.add(getActivity().getString(R.string.researcher_id));
        }
        if (collectorConfig.raportType == CollectorConfig.RaportType.TASKS_AND_SUMMARY) {
            permissionAdapted.add(getActivity().getString(R.string.task_and_summary_result));
        } else if (collectorConfig.raportType == CollectorConfig.RaportType.JUST_SUMMARY) {
            permissionAdapted.add(getActivity().getString(R.string.summary_result));
        }
    }


    public abstract void dialogDismissed(boolean agreed);

    private class PermissionAdapted extends LoremIpsumSimpleAdapter<String> {

        public PermissionAdapted(Context context, int resource) {
            super(context, resource);
        }

        @Override
        protected long extractId(String item) {
            return 0;
        }

        @Override
        protected String populateItem(String item) {
            return item;
        }
    }
}
