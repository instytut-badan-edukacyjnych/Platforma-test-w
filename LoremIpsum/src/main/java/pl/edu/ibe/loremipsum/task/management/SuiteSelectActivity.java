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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.tablet.MainActivity;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.task.management.collector.CollectorAgreementDialog;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.InstallationIdentifier;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.NetworkUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;
import rx.Observable;
import rx.schedulers.Schedulers;

public class SuiteSelectActivity extends BaseServiceActivity {

    private static final String TAG = SuiteSelectActivity.class.toString();
    public static final String SHOULD_CHECK_UPDATES = "should_check_updates";
    @InjectView(R.id.suite_list)
    ListView suiteList;

    @InjectView(R.id.add_new_test)
    View addNewTest;
    @InjectView(R.id.choose_bank)
    View chooseBank;


    private Adapter adapter;
    private static Researcher researcher;
    private boolean updateCheckInProgress = false;
    private int updateInProgress = 0;
    private int installInProgress = 0;
    private ResearchersSuite currentTaskSuite = null;
    private ProgressDialog progressDialog;

    @OnClick(R.id.add_new_test)
    void onAddNewTestClick() {
        showInstallDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suite_select);
        ButterKnife.inject(this);
        setTitle(getString(R.string.select_task_suite));
        researcher = getServiceProvider().login().currentLoggedInUser;

        adapter = new Adapter(this, R.layout.row_simple_list_item_1);
        getServiceProvider().downloadTaskSuiteService().registerAdapter(adapter);

        suiteList.setAdapter(adapter);
        suiteList.setOnItemClickListener(adapter);
        suiteList.setOnItemLongClickListener(adapter);
        registerForContextMenu(suiteList);


        try {
            ((TextView) findViewById(R.id.device_id)).setText(getString(R.string.device_id, new InstallationIdentifier(this).getDeviceId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        getServiceProvider().downloadTaskSuiteService().setActivityContext(this);
        if (getIntent().getBooleanExtra(SHOULD_CHECK_UPDATES, false)) {
            getServiceProvider().downloadTaskSuiteService().checkUpdates(researcher);
            getIntent().removeExtra(SHOULD_CHECK_UPDATES);
        }
    }

    @Override
    protected void onStop() {
        getServiceProvider().downloadTaskSuiteService().unregister();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.suite_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.v(TAG, "Item selected: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_check_update:
                getServiceProvider().downloadTaskSuiteService().checkUpdates(researcher);
                return true;
            case R.id.action_add_new_bank:
                showInstallDialog();
                return true;
            case R.id.action_report_bug:
                SupportDialog.show(this, getSupportFragmentManager());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean hasUpButton() {
        return false;
    }

    private void returnSuite(TaskSuite suite) {
        String suiteName = suite.getName();
        String suiteVersion = suite.getVersion();

        ProgressDialog d = new ProgressDialog(this);
        d.setTitle(R.string.loading);
        d.setMessage(getResources().getString(R.string.preparing_task_suite));
        d.show();

        //TODO MP: czemu są ładowane najpierw wszystkie zadania a potem config? To powoduje, że szukanie wszystkich zadań nie ma jak wykryć zadania kończącego, gdyż jego nazwa jest ładowana z config.xml.
        RxExecutor.runWithUiCallback(LoadingPerformer.performLoad(this, getServiceProvider(), suiteName, suiteVersion)
                        .flatMap(ignore -> getServiceProvider().taskSuites().getTaskSuite(suiteName, suiteVersion))
                        .flatMap(taskSuite -> {
                                    try {
                                        getServiceProvider().examinee().fillExamineeAdditionalFieldsManager(taskSuite.getRootVirtualFile());
                                        return getServiceProvider().currentTaskSuite().loadTestConfig(taskSuite.getRootVirtualFile())
                                                .map(result -> {
                                                    getServiceProvider().currentTaskSuite().getCurrentTestRunData().setTaskSuite(taskSuite);
                                                    return result;
                                                });
                                    } catch (IOException e) {
                                        throw ExecutionException.wrap(e);
                                    }
                                }
                        ).subscribeOn(Schedulers.io())
        ).subscribe(result -> {
            d.dismiss();
            displayDialog();
        }, throwable -> {
            Toast.makeText(this, getString(R.string.error_loading_test) + " " + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "Exception during test loading", throwable);
            adapter.refresh();
        });
    }

    private void displayDialog() {
        Intent intent = new Intent();
        getServiceProvider().collector().shouldDisplayAgreedForCollectionDialog().subscribe((dialogWasDisplayed) -> {
            if (!dialogWasDisplayed && getServiceProvider().collector().isReportingRequired()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                CollectorAgreementDialog dialog = new CollectorAgreementDialog() { // $1

                    @Override
                    public void dialogDismissed(boolean agreed) {
                        getServiceProvider().collector().updateSawCollectorOpt(true).subscribe((ignore) -> {
                            getServiceProvider().collector().updateAgreement(agreed).subscribe((ignore2) -> {
                                performStartActivity(intent);
                            });
                        });
                    }
                };
                dialog.show(fragmentManager, "dialog");
            } else {
                performStartActivity(intent);
            }
        });
    }

    private void performStartActivity(Intent intent) {
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
    }

    private void showInstallDialog() {
        if (BuildConfig.OMIT_NETWORK_CHECK) {
            displayInstallSuiteDialog();
        } else {
            if (NetworkUtils.isOnline(this)) {
                displayInstallSuiteDialog();
            } else {
                OfflineModeEnabledDialog offlineModeEnabledDialog = new OfflineModeEnabledDialog() { // $2
                    @Override
                    public void startSettingsActivity() {
                        OfflineModeEnabledDialog.openSettings(SuiteSelectActivity.this);
                    }
                };
                offlineModeEnabledDialog.show(getSupportFragmentManager(), "networkDialog");
            }
        }
    }


    private void displayInstallSuiteDialog() {
        InstallSuiteDialog dialog = new InstallDialog();
        if (!isFinishing()) {

            dialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    private void showUninstallDialog() {
        if (currentTaskSuite != null) {
            pl.edu.ibe.loremipsum.db.schema.TaskSuite suite = currentTaskSuite.getTaskSuite();
            if (suite != null) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.uninstalling_task_suite)
                        .setMessage(getString(R.string.do_you_really_want_to_uninstall_task_suite,
                                suite.getName(),
                                suite.getVersion()))
                        .setPositiveButton(android.R.string.yes, (d, w) -> getServiceProvider().downloadTaskSuiteService().uninstallSuite(currentTaskSuite))
                        .setNegativeButton(android.R.string.no, (d, w) -> {
                        })
                        .create().show();
            }
        }
    }


    private void repairSuite() {
        RepairDialog dialog = new RepairDialog();
        dialog.show(getSupportFragmentManager(), "dialogRepair");
    }

    private void reallyRepairSuite() {
        updateInProgress++;
        Pair<Observable<TaskSuite>, Observable<InstallationProgress>> process
                = getServiceProvider().taskSuites().repairSuite(currentTaskSuite);
        getServiceProvider().downloadTaskSuiteService().progressInstalling(process.first, process.second);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        LogUtils.v(TAG, "onContextItemSelected");


        switch (item.getItemId()) {
            case R.id.repair_task_suite:
                repairSuite();
                return true;
            case R.id.uninstall_task_suite:
                showUninstallDialog();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        LogUtils.v(TAG, "onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
        if (currentTaskSuite != null) {
            menu.setHeaderTitle(getString(R.string.suite_context_menu, currentTaskSuite.getCredential().getUser(), currentTaskSuite.getCredential().getManifestUrl()));
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.suite_menu, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OfflineModeEnabledDialog.ENABLE_INTERNET_REQUEST) {
            if (BuildConfig.OMIT_NETWORK_CHECK) {
                displayInstallSuiteDialog();
            } else {
                if (NetworkUtils.isOnline(this)) {
                    displayInstallSuiteDialog();
                }
            }
        }
    }

    public static class InstallDialog extends InstallSuiteDialog {


        @Override
        protected void dialogDismissed(String manifestUrl, String username, String password, boolean accepted) {
            if (accepted) {
                getServiceProvider().downloadTaskSuiteService().installSuite(manifestUrl, username, password, researcher);
            }
        }
    }

    public static class RepairDialog extends ServiceDialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.repair_task_suite_title)
                    .setMessage(R.string.repair_task_suite_message)
                    .setPositiveButton(android.R.string.yes, (d, w) -> {
                        SuiteSelectActivity owner = (SuiteSelectActivity) getActivity();
                        owner.reallyRepairSuite();
                    })
                    .setNegativeButton(android.R.string.no, (d, w) -> {
                    })
                    .setCancelable(false)
                    .create();
        }
    }

    class Adapter extends LoremIpsumSimpleAdapter<TaskSuite>
            implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        Adapter(Context context, int resource) {
            super(context, resource);
            refresh();
        }

        @Override
        protected long extractId(TaskSuite item) {
            return item.getDbEntry().getId();
        }

        @Override
        protected String populateItem(TaskSuite item) {
            String text = item.getName() + " (" + SuiteSelectActivity.this.getResources().getString(R.string.version) + " " + item.getVersion() + ")";
            if (item.getPilot()) {
                text += " - " + getResources().getString(R.string.pilot_task_suite);
            }
            if (!item.isDownloaded()) {
                text += " (" + getResources().getString(R.string.broken) + ")";
            }
            return text;
        }


        public void refresh() {
            this.clear();
            RxExecutor.runWithUiCallback(
                    getServiceProvider().taskSuites().listSuitesForResearcher(researcher)
            ).subscribe(suite -> {
                this.addAll(suite);
                super.notifyDataSetChanged();
                if (getCount() == 0) {
                    addNewTest.setVisibility(View.VISIBLE);
                    chooseBank.setVisibility(View.INVISIBLE);
                } else {
                    addNewTest.setVisibility(View.INVISIBLE);
                    chooseBank.setVisibility(View.VISIBLE);
                }
            });

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TaskSuite suite = getItem(position);
            if (!suite.isDownloaded()) {
                Toast.makeText(SuiteSelectActivity.this, R.string.unable_to_run_broken_task_suite, Toast.LENGTH_LONG).show();
            } else {
                returnSuite(getItem(position));
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            currentTaskSuite = getServiceProvider().taskSuites().getResearchersSuite(getItem(position), researcher).toBlockingObservable().singleOrDefault(null);
            if (currentTaskSuite == null) {
                adapter.refresh();
                Toast.makeText(SuiteSelectActivity.this, "Błąd operacji na banku zadań", Toast.LENGTH_SHORT).show();
            }
            openContextMenu(suiteList);
            return true;
        }
    }
}
