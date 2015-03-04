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
import android.app.ProgressDialog;
import android.util.Pair;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.network.Auth;
import pl.edu.ibe.loremipsum.network.HttpBasicAuth;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.task.ProgressDialogFragment;
import pl.edu.ibe.loremipsum.task.management.installation.InstallationProgress;
import pl.edu.ibe.loremipsum.task.management.storage.TaskSuiteVersion;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.testplatform.R;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.Subject;

/**
 * Created by adam on 23.01.15.
 */
public class DownloadTaskSuiteService extends BaseService {
    private static final String TAG = DownloadTaskSuiteService.class.getCanonicalName();
    private static final String PROGRESS_INSTALLING_TAG = "progressInstalling";
    private int installInProgress;
    private boolean updateCheckInProgress;
    private int updateInProgress;
    private ProgressDialog progressDialog;
    private SuiteSelectActivity.Adapter adapter;
    private SuiteSelectActivity activityContext;
    private ProgressDialogFragment progressInstallingDialogFragment;
    private ProgressDialogFragment progressDialogFragment;

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public DownloadTaskSuiteService(ServiceProvider services) {
        super(services);
    }

    public void installSuite(String manifestUrl, String username, String password, Researcher researcher) {

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
                installInProgress--;
                return getServiceProvider().taskSuites().getTaskSuite(name, version);
            }), installation.first);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (progressInstallingDialogFragment != null) {
                progressInstallingDialogFragment.dismiss();
            }
            installInProgress--;
            Toast.makeText(services.context(), R.string.malformed_url, Toast.LENGTH_LONG).show();
        }
    }

    public void progressInstalling(Observable<TaskSuite> operationObservable, Observable<InstallationProgress> progressObservable) {


        progressInstallingDialogFragment = (ProgressDialogFragment) activityContext.getSupportFragmentManager().findFragmentByTag(PROGRESS_INSTALLING_TAG);
        if (progressInstallingDialogFragment == null) {
            progressInstallingDialogFragment = ProgressDialogFragment.getInstance(services.context().getResources().getString(R.string.installing_title),
                    services.context().getResources().getString(R.string.preparing_download),
                    ProgressDialog.STYLE_HORIZONTAL);
        }
        progressInstallingDialogFragment.show(activityContext.getSupportFragmentManager(), PROGRESS_INSTALLING_TAG);

//        ProgressDialog progressDialog = new ProgressDialog(activityContext);
//
//        progressDialog.setTitle(services.context().getResources().getString(R.string.installing_title));
//        progressDialog.setMessage(services.context().getResources().getString(R.string.preparing_download));
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.show();


        StringBuffer suiteName = new StringBuffer(services.context().getString(R.string.unknown_task_suite));
        StringBuffer suiteVersion = new StringBuffer(services.context().getString(R.string.unknown_version));

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
                            if (progressDialogFragment != null) {
                                progressInstallingDialogFragment.setMax(downloadSize);
                                progressInstallingDialogFragment.setProgress(count);
                            }
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
                                progressInstallingDialogFragment.setMessage(services.context().getString(R.string.downloading_with_estimation_seconds, timeLeft, downloadSpeed));
                            } else {
                                progressInstallingDialogFragment.setMessage(services.context().getString(R.string.downloading));
                            }
                        }
                    }
                });
        boolean[] done = {false};
        Subscription subscription = RxExecutor.runWithUiCallback(operationObservable).subscribe(
                ignore -> {
                },
                throwable -> {
                    progressInstallingDialogFragment.dismiss();
                    String message = services.context().getResources().getString(R.string.task_suite_installation_failed);
                    message += "\n";
                    message += services.context().getResources().getString(R.string.error_details) + " ";
                    message += throwable.getMessage();
                    Toast.makeText(services.context(), message, Toast.LENGTH_LONG).show();
                    installInProgress = 0;
                    updateInProgress = 0;
                    LogUtils.e(TAG, "installing failed", throwable);
                    adapter.refresh();
                },
                () -> {
                    done[0] = true;
                    progressInstallingDialogFragment.dismiss();
                    adapter.refresh();
                    installInProgress = 0;
                    updateInProgress = 0;
                    LogUtils.i(TAG, "downloading finished");
                    getServiceProvider().taskSuites().clearDownloadData();
                }
        );

        progressInstallingDialogFragment.setOnCancelListener(dialogInterface -> {
            new AlertDialog.Builder(activityContext)
                    .setTitle(R.string.interrupting_download)
                    .setMessage(services.context().getString(R.string.do_you_really_want_to_interrupt_download_of, suiteName, suiteVersion))
                    .setPositiveButton(android.R.string.yes, (d, w) -> subscription.unsubscribe())
                    .setNegativeButton(android.R.string.no, (d, w) -> {
                        if (!done[0]) {
                            progressInstallingDialogFragment.show(activityContext.getSupportFragmentManager(), "progressInstalling");
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        });
    }

    public void uninstallSuite(ResearchersSuite currentTaskSuite) {
        installInProgress++;
        RxExecutor.runWithUiCallback(getServiceProvider().taskSuites().uninstallSuite(currentTaskSuite))
                .subscribe(ignore -> {
                        }, throwable -> {
                            Toast.makeText(services.context(), R.string.uninstall_failed, Toast.LENGTH_LONG).show();
                            adapter.refresh();
                            installInProgress--;
                        }, () -> {
                            adapter.refresh();
                            installInProgress--;
                        }
                );
    }

    public void checkUpdates(Researcher researcher) {
        if (installInProgress > 0 || updateCheckInProgress || updateInProgress > 0) {
            LogUtils.d(TAG, "installInProgress: " + installInProgress + " updateCheckInProgress: " + updateCheckInProgress + " updateInProgress: " + updateInProgress);
            return;
        }


        updateCheckInProgress = true;

        progressDialogFragment = (ProgressDialogFragment) activityContext.getSupportFragmentManager().findFragmentByTag("updateDialog");
        if (progressDialogFragment == null) {
            progressDialogFragment = ProgressDialogFragment.getInstance(activityContext.getString(R.string.checking_updates), null, ProgressDialog.STYLE_HORIZONTAL);
        }
        progressDialogFragment.show(activityContext.getSupportFragmentManager(), "updateDialog");

        Tuple.Two<Observable<TaskSuiteUpdate>, Observable<String>> check = getServiceProvider().taskSuites().checkUpdates(researcher);
        final ProgressDialogFragment finalProgressDialogFragment = progressDialogFragment;
        check.second.observeOn(AndroidSchedulers.mainThread())
                .subscribe(name -> finalProgressDialogFragment.setMessage(services.context().getResources().getString(R.string.checking) + " " + name));

        check.first.observeOn(AndroidSchedulers.mainThread()).subscribe(
                update -> {
                    if (!update.isUpdate()) {
                        Toast.makeText(services.context(), String.format(
                                services.context().getResources().getString(R.string.no_update_for),
                                update.getName()
                        ), Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(activityContext)
                                .setCancelable(false)
                                .setPositiveButton(R.string.ok_text, (dialogInterface, which) -> {
                                    installUpdate(update);
                                }).setNegativeButton(R.string.cancel_text, (dialogInterface, which) -> {
                        }).setTitle(String.format(
                                services.context().getResources().getString(R.string.do_you_want_to_update),
                                update.getName(), update.getNewTaskSuite().getVersion(),
                                update.getOldTaskSuite().getTaskSuite().getVersion()
                        )).create().show();
                    }
                },
                throwable -> {
                    finalProgressDialogFragment.dismiss();
                    String message = services.context().getResources().getString(R.string.task_suite_update_check_failed);
                    message += "\n";
                    message += services.context().getResources().getString(R.string.error_details) + " ";
                    message += throwable.getMessage();
                    Toast.makeText(services.context(), message, Toast.LENGTH_LONG).show();
                    updateCheckInProgress = false;
                    LogUtils.e(TAG, "checking update of suite failed", ExecutionException.wrap(throwable));

                },
                () -> {
                    finalProgressDialogFragment.dismiss();
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


    public void registerAdapter(SuiteSelectActivity.Adapter adapter) {
        this.adapter = adapter;
    }

    public void setActivityContext(SuiteSelectActivity activityContext) {
        this.activityContext = activityContext;
        progressInstallingDialogFragment = (ProgressDialogFragment) activityContext.getSupportFragmentManager().findFragmentByTag(PROGRESS_INSTALLING_TAG);
    }

    public void unregister() {
        progressInstallingDialogFragment = null;
        activityContext = null;
    }
}
