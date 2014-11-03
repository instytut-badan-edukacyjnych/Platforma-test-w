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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.network.HttpBasicAuth;
import pl.edu.ibe.loremipsum.tablet.MainActivity;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.task.management.collector.CollectorAgreementDialog;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.NetworkUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.Subject;

public class SuiteSelectActivity extends BaseServiceActivity {

    private static final String TAG = SuiteSelectActivity.class.toString();

    @InjectView(R.id.suite_list)
    ListView suiteList;

    @InjectView(R.id.add_new_test)
    View addNewTest;
    @InjectView(R.id.choose_bank)
    View chooseBank;


    private Adapter adapter;
    private Researcher researcher;
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
        suiteList.setAdapter(adapter);
        suiteList.setOnItemClickListener(adapter);
        suiteList.setOnItemLongClickListener(adapter);
        registerForContextMenu(suiteList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUpdates();
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
                checkUpdates();
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
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void showUninstallDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.uninstalling_task_suite)
                .setMessage(getString(R.string.do_you_really_want_to_uninstall_task_suite,
                        currentTaskSuite.getTaskSuite().getName(),
                        currentTaskSuite.getTaskSuite().getVersion()))
                .setPositiveButton(android.R.string.yes, (d, w) -> uninstallSuite())
                .setNegativeButton(android.R.string.no, (d, w) -> {
                })
                .create().show();
    }

    private void uninstallSuite() {
        installInProgress++;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.uninstalling_task_suite);
        progressDialog.setMessage(getString(R.string.uninstalling_task_suite_message));
        progressDialog.setCancelable(false);

        RxExecutor.runWithUiCallback(getServiceProvider().taskSuites().uninstallSuite(currentTaskSuite))
                .subscribe(ignore -> {
                        }, throwable -> {
                            progressDialog.dismiss();
                            Toast.makeText(SuiteSelectActivity.this, R.string.uninstall_failed, Toast.LENGTH_LONG).show();
                            installInProgress--;
                        }, () -> {
                            progressDialog.dismiss();
                            adapter.refresh();
                            installInProgress--;
                        }
                );
    }

    private void progressInstalling(Observable<TaskSuite> operationObservable,
                                    Observable<InstallationProgress> progressObservable) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.installing_title));
        progressDialog.setMessage(getResources().getString(R.string.preparing_download));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        StringBuffer suiteName = new StringBuffer(getString(R.string.unknown_task_suite));
        StringBuffer suiteVersion = new StringBuffer(getString(R.string.unknown_version));
        RxExecutor.runWithUiCallback(progressObservable.filter(progress -> progress != null))
                .subscribe(new Action1<InstallationProgress>() { // #4

                    int prevTotal;
                    int prevTime;

                    @Override
                    public void call(InstallationProgress progress) {
                        suiteName.setLength(0);
                        suiteName.append(progress.getSuiteName());
                        suiteVersion.setLength(0);
                        suiteVersion.append(progress.getSuiteVersion());

                        int count = progress.getInstalledBytes();
                        int time = progress.getTime();
                        int downloadSize = progress.getTotalBytes();

                        LogUtils.v(TAG, "download progress: " + count + " B; " + time + " ms");
                        if (downloadSize != 0) {
                            progressDialog.setMax(downloadSize);
                            progressDialog.setProgress(count);
                            String timeLeft = null;
                            String downloadSpeed = null;
                            if (count != 0) {
                                double totalTime = ((double) downloadSize) * ((double) time) / ((double) count);
                                int leftTime = ((int) totalTime - time) / 1000;
                                int leftHours = leftTime / 3600;
                                int leftMinutes = (leftTime / 60) % 60;
                                int leftSeconds = leftTime % 60;

                                double bytesDiff = Math.max(count - prevTotal, 0d);
                                prevTotal = count;
                                double timeDiff = Math.max(time - prevTime, 0d);
                                prevTime = time;
                                if (bytesDiff != 0 && timeDiff != 0) {
                                    double value = bytesDiff / timeDiff;
                                    String unit = "kB/s";
                                    if (value > 1024) {
                                        value /= 1024;
                                        unit = "MB/s";
                                    }
                                    downloadSpeed = String.valueOf((int) value) + " " + unit;
                                }
                                if (leftHours > 0) {
                                    timeLeft = leftHours + ":" + String.format("%02d", leftMinutes) + ":" + String.format("%02d", leftSeconds);
                                } else if (leftMinutes > 0) {
                                    timeLeft = leftMinutes + ":" + String.format("%02d", leftSeconds);
                                } else {
                                    timeLeft = String.valueOf(leftSeconds) + " s";
                                }
                            }
                            if (timeLeft != null) {
                                progressDialog.setMessage(getString(R.string.downloading_with_estimation_seconds, timeLeft, downloadSpeed));
                            } else {
                                progressDialog.setMessage(getString(R.string.downloading));
                            }
                        }
                    }
                });
        boolean[] done = {false};
        Subscription subscription = RxExecutor.runWithUiCallback(operationObservable).subscribe(
                ignore -> {
                },
                throwable -> {
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.task_suite_installation_failed);
                    message += "\n";
                    message += getResources().getString(R.string.error_details) + " ";
                    message += throwable.getMessage();
                    Toast.makeText(SuiteSelectActivity.this, message, Toast.LENGTH_LONG).show();
                    LogUtils.e(TAG, "installing failed", throwable);
                },
                () -> {
                    done[0] = true;
                    progressDialog.dismiss();
                    adapter.refresh();
                    LogUtils.i(TAG, "downloading finished");
                    getServiceProvider().taskSuites().clearDownloadData();
                }
        );

        progressDialog.setOnCancelListener(dialogInterface -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.interrupting_download)
                    .setMessage(getString(R.string.do_you_really_want_to_interrupt_download_of, suiteName, suiteVersion))
                    .setPositiveButton(android.R.string.yes, (d, w) -> subscription.unsubscribe())
                    .setNegativeButton(android.R.string.no, (d, w) -> {
                        if (!done[0]) {
                            progressDialog.show();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        });
    }

    private void installSuite(String manifestUrl, String username, String password) {
        installInProgress++;
        try {
            URL url = new URL(manifestUrl);
            Auth auth = new HttpBasicAuth(username, password);
            LogUtils.i(TAG, "Installing new task suite: " + manifestUrl + " (" + username + ":*****)");
            Pair<Subject<InstallationProgress, InstallationProgress>, Observable<TaskSuiteVersion>> installation =
                    getServiceProvider().taskSuites().installSuite(researcher, url, auth);
            Observable<InstallationProgress> p = installation.first;
            progressInstalling(installation.second.flatMap(v -> {
                String name = v.getTaskSuite().getName();
                String version = v.getIdentifier();
                return getServiceProvider().taskSuites().getTaskSuite(name, version);
            }), installation.first);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, R.string.malformed_url, Toast.LENGTH_LONG).show();
        }
    }

    private void checkUpdates() {
        if (installInProgress > 0 || updateCheckInProgress || updateInProgress > 0) {
            return;
        }

        updateCheckInProgress = true;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.checking_updates);
        progressDialog.show();

        Tuple.Two<Observable<TaskSuiteUpdate>, Observable<String>> check = getServiceProvider().taskSuites().checkUpdates(researcher);
        check.second.observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> progressDialog.setMessage(getResources().getString(R.string.checking) + " " + name));

        check.first.observeOn(AndroidSchedulers.mainThread()).subscribe(
                update -> {
                    if (!update.isUpdate()) {
                        Toast.makeText(this, String.format(
                                getResources().getString(R.string.no_update_for),
                                update.getName()
                        ), Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setPositiveButton(R.string.ok_text, (dialogInterface, which) -> {
                                    installUpdate(update);
                                }).setNegativeButton(R.string.cancel_text, (dialogInterface, which) -> {
                        }).setTitle(String.format(
                                getResources().getString(R.string.do_you_want_to_update),
                                update.getName(), update.getNewTaskSuite().getVersion(),
                                update.getOldTaskSuite().getTaskSuite().getVersion()
                        )).create().show();
                    }
                },
                throwable -> {
                    progressDialog.dismiss();
                    String message = getResources().getString(R.string.task_suite_update_check_failed);
                    message += "\n";
                    message += getResources().getString(R.string.error_details) + " ";
                    message += throwable.getMessage();
                    Toast.makeText(SuiteSelectActivity.this, message, Toast.LENGTH_LONG).show();
                    LogUtils.e(TAG, "checking update of suite failed", ExecutionException.wrap(throwable));
                },
                () -> {
                    progressDialog.dismiss();
                    adapter.refresh();
                    LogUtils.i(TAG, "update check finished");
                    updateCheckInProgress = false;
                }
        );
    }

    private void installUpdate(TaskSuiteUpdate update) {
        updateInProgress++;
        Pair<Observable<TaskSuite>, Observable<InstallationProgress>> process
                = getServiceProvider().taskSuites().installUpdate(update);
        progressInstalling(process.first, process.second);
    }

    private void repairSuite() {
        RepairDialog dialog = new RepairDialog();
        dialog.show(getSupportFragmentManager(), "dialogRepair");
    }

    private void reallyRepairSuite() {
        updateInProgress++;
        Pair<Observable<TaskSuite>, Observable<InstallationProgress>> process
                = getServiceProvider().taskSuites().repairSuite(currentTaskSuite);
        progressInstalling(process.first, process.second);
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
            ((SuiteSelectActivity) getActivity()).installSuite( manifestUrl,  username,  password);
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

    private class Adapter extends LoremIpsumSimpleAdapter<TaskSuite>
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
                Toast.makeText(SuiteSelectActivity.this, R.string.unable_to_run_broken_task_suite,
                        Toast.LENGTH_LONG).show();
            } else {
                returnSuite(getItem(position));
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            currentTaskSuite = getServiceProvider().taskSuites().getResearchersSuite(getItem(position), researcher).toBlockingObservable().single();
            openContextMenu(suiteList);
            return true;
        }
    }
}
