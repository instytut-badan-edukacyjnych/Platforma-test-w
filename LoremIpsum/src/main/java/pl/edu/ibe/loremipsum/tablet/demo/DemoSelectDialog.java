/************************************
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
 ************************************/
package pl.edu.ibe.loremipsum.tablet.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;

/**
 * Created by adam on 22.04.14.
 */
public abstract class DemoSelectDialog extends ServiceDialogFragment implements AdapterView.OnItemClickListener {

    @InjectView(R.id.demo_list)
    ListView demoList;
    DemoAdapter demoAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_demo_select, null, false);
        ButterKnife.inject(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.select_demo_test))
                .setNegativeButton(R.string.cancel_text, (dialog1, which) -> {
                    dialogDismissed(null);
                })
                .setView(view)
                .create();

        demoAdapter = new DemoAdapter(getActivity(), android.R.layout.simple_list_item_1);
        demoList.setOnItemClickListener(this);
        demoList.setAdapter(demoAdapter);

        getServiceProvider().demo().listDemoTaskSuite().subscribe(
                demoAdapter::addItem,
                throwable -> {
                    Toast.makeText(this.getActivity(),
                            getResources().getString(R.string.checking_demo_failed, throwable.getLocalizedMessage()),
                            Toast.LENGTH_SHORT).show();
                    dialogDismissed(null);
                    dismiss();
                },
                () -> {
                    if (demoAdapter.getCount() == 0) {
                        Toast.makeText(this.getActivity(),
                                R.string.no_demo_suites,
                                Toast.LENGTH_SHORT).show();
                        dialogDismissed(null);
                        dismiss();
                    }
                }
        );
        return dialog;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dialogDismissed(demoAdapter.getItem(position));
        dismiss();
    }

    public abstract void dialogDismissed(TaskSuite startDemo);

    private class DemoAdapter extends LoremIpsumSimpleAdapter<TaskSuite> {

        public DemoAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        protected long extractId(TaskSuite item) {
            return item.getDbEntry().getId();
        }

        @Override
        protected String populateItem(TaskSuite item) {
            return item.getName() + " (" + getContext().getResources().getString(R.string.version) + " " + item.getVersion() + ")";
        }
    }

}
