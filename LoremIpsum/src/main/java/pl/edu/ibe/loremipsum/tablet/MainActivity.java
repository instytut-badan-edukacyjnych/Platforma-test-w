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

package pl.edu.ibe.loremipsum.tablet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.MainButtonLayout;
import pl.edu.ibe.loremipsum.localization.Exceptions;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.examinee.ExamineeManagerActivity;
import pl.edu.ibe.loremipsum.tablet.network.NetworkCheckDialog;
import pl.edu.ibe.loremipsum.tablet.report.ReportActivity;
import pl.edu.ibe.loremipsum.tablet.settings.SettingsDialog;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.tablet.test.TestConfigActivity;
import pl.edu.ibe.loremipsum.task.management.LoadingPerformer;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.NetworkUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;

public class MainActivity extends BaseServiceActivity {

    private static final String TAG = MainActivity.class.toString();

    @InjectView(R.id.test)
    View test;
    @InjectView(R.id.examinee)
    View examined;
    @InjectView(R.id.reports)
    View reports;
    @InjectView(R.id.demo_button)
    MainButtonLayout demoButton;
    @InjectView(R.id.context_help)
    TextView contextHelp;
    @InjectView(R.id.test_crash)
    Button testCrash;


    private NetworkCheckDialog networkCheckDialog;
    private int testRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setTitleBasedOnTestSuite();

        try {
            contextHelp.setText(getServiceProvider().currentTaskSuite().getLocalization().get("help_main"));
        } catch (Exceptions.LocalizationException e) {
            LogUtils.v(TAG, "Localization not found", e);
        }

        if (BuildConfig.DEBUG) {
            testCrash.setVisibility(View.VISIBLE);
            testCrash.setOnClickListener(v -> {
                throw new RuntimeException("Testowy crash aplikacji");
            });
        }

    }

    private void setTitleBasedOnTestSuite() {
        TaskSuite taskSuite = getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite();
        String text = taskSuite.getName() + " (" + getResources().getString(R.string.version) + " " + taskSuite.getVersion() + ")";
        if (taskSuite.getPilot()) {
            text += " - " + getResources().getString(R.string.pilot_task_suite);
        }
        setTitle(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        demoButton.setProgressBarVisibility(false);
        if (LoremIpsumApp.demoManager == null) {
            demoButton.setEnabled(false);
            demoButton.setAdditionalText(getString(R.string.task_suite_has_no_demo));
        } else {
            demoButton.setEnabled(true);
            demoButton.setAdditionalText("");
        }
        getServiceProvider().currentTaskSuite().setAllowScreenRotation(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getServiceProvider().testData().attachTestDataEverywhereIsPossible();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                displaySettingsDialog();
                return true;
            case R.id.action_report_bug:
                displayReportBugDialog();
                return true;
            case R.id.action_import_data:
                onImportClicked();
                return true;
            case R.id.action_export_data:
                onExportClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayReportBugDialog() {
        SupportDialog.show(this, getSupportFragmentManager());
    }

    private void displaySettingsDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsDialog dialog = new SettingsDialog();
        dialog.show(fragmentManager, "dialog");
    }

    public void onImportClicked() {
        RxExecutor.runWithUiCallback(
                getServiceProvider().importExport().importData()).subscribe((result) -> {
            if (result) {
                Toast.makeText(this, "import success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "import failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onExportClicked() {
        RxExecutor.runWithUiCallback(
                getServiceProvider().importExport().exportData()).subscribe((result) -> {
            if (result) {
                Toast.makeText(this, "export success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "export failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.test)
    public void onTestClicked() {
        handleTestStartRequest();
    }

    @OnClick(R.id.examinee)
    public void onExaminedClicked() {
        Intent intent = new Intent();
        intent.setClass(this, ExamineeManagerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.reports)
    public void onReportsClicked() {
        Intent intent = new Intent();
        intent.setClass(this, ReportActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.demo_button)
    void demoButtonClicked() {
        demoButton.setEnabled(false);
        demoButton.setProgressBarVisibility(true);
        runTest(getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite());

    }

    private void runTest(TaskSuite demoSuite) {
        RxExecutor.runWithUiCallback(RxExecutor.run(() -> {
            if (LoremIpsumApp.demoManager == null)
                throw new Exception(getString(R.string.task_suite_has_no_demo));
            return RxExecutor.EMPTY_OBJECT;
        }).flatMap(ignore -> getServiceProvider().currentTaskSuite().prepareDemo(demoSuite)
                .flatMap(testRunData -> LoadingPerformer.performLoad(this, getServiceProvider(),
                                testRunData.getTaskSuite().getName(), testRunData.getTaskSuite().getVersion())
                                .map(ignore2 -> testRunData)
                ))).subscribe(testRunData -> {
                    try {
                        testRequestCode = testRunData.runTest(this);
                    } catch (Exception e) {
                        throw ExecutionException.wrap(e);
                    }
                }, throwable -> Toast.makeText(MainActivity.this, getString(R.string.could_not_start_demo)
                        + ": " + throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void runTest() {
        Intent intent = new Intent(this, TestConfigActivity.class);
        startActivity(intent);
    }

    private void handleTestStartRequest() {
        if (BuildConfig.OMIT_NETWORK_CHECK) {
            runTest();
        } else {
            if (NetworkUtils.isOnline(this)) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                networkCheckDialog = new NetworkCheckDialog() {

                    @Override
                    public void startSettingsActivity() {
                        NetworkCheckDialog.openSettings(MainActivity.this);
                    }
                };
                networkCheckDialog.show(fragmentManager, "networkDialog");
            } else {
                runTest();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NetworkCheckDialog.DISABLE_INTERNET_REQUEST) {
            if (BuildConfig.OMIT_NETWORK_CHECK) {
                runTest();
            } else {
                if (NetworkUtils.isOnline(this)) {
                    Toast.makeText(this, getString(R.string.offline_mode_error), Toast.LENGTH_SHORT).show();
                } else {
                    runTest();
                }
            }
        } else if (requestCode == testRequestCode) {
            getServiceProvider().demo().showDemoReport(this);
        }
    }
}
