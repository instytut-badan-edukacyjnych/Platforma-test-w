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


package pl.edu.ibe.loremipsum.tablet.report;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Activity used to display reports
 */
public class ReportActivity extends BaseServiceActivity {
    public static final String REPORT_DIRECTORY = "report";
    public static final String EXTRA_DISABLE_PROVIDER = "disable_provider";
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    protected static final int REQUEST_CODE_CREATOR = 2;
    private static final String TAG = ReportActivity.class.toString();
    @InjectView(R.id.report_view)
    WebView reportView;
    Report report;
    private PrintingJSHandler printingHandler;
    private ProgressDialog printingProgress;
    private Subject<String, String> subjectPageFinished = BehaviorSubject.create((String) null);
    private GoogleDriveUploader googleDriveUploader;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleDriveUploader.activityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.inject(this);
        setTitle(getString(R.string.reports));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        reportView.setVisibility(View.GONE);

        googleDriveUploader = new GoogleDriveUploader(this, REQUEST_CODE_RESOLUTION, REQUEST_CODE_CREATOR);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.loading);
        dialog.setMessage(getString(R.string.preparing_report));
        dialog.setCancelable(false);
        dialog.show();

        RxExecutor.runWithUiCallback(
                RxExecutor.run(() -> getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite().getRootVirtualFile().getChildFile(REPORT_DIRECTORY))
                        .flatMap(reportSourceDir -> Report.load(this, reportSourceDir)).subscribeOn(Schedulers.io())
        ).subscribe(
                report -> {
                    try {
                        this.report = report;
                        dialog.dismiss();
                        reportView.setVisibility(View.VISIBLE);
                        String strUrl = report.getUrl().toString();
                        LogUtils.v(TAG, "Showing report from \"" + strUrl + "\"");
                        reportView.loadUrl(strUrl);
                    } catch (ReportException e) {
                        throw ExecutionException.wrap(e);
                    }
                },
                throwable -> {
                    LogUtils.e(TAG, "Report loading failed", throwable);
                    Toast.makeText(this, R.string.report_preparing_failed, Toast.LENGTH_SHORT).show();
                    finish();
                }
        );

        reportView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LogUtils.d("web", description);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                subjectPageFinished.onNext(url);
            }
        });
        reportView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LogUtils.d("web", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                LogUtils.d("web", message);

                return super.onJsAlert(view, url, message, result);
            }
        });


        ResultsJSHandler resultsHandler = new ResultsJSHandler(getServiceProvider(), this, reportView);
        resultsHandler.install(!getIntent().getBooleanExtra(EXTRA_DISABLE_PROVIDER, false));
        printingHandler = new PrintingJSHandler(this, reportView);
        printingHandler.install();

        //for live testing might be usefull, fell free to delete those
//        Log.i("JSON",  resultsHandler.getResults());
//        Log.i("JSON",  resultsHandler.getExaminee("d1"));
//        Log.i("JSON",  resultsHandler.getInstitution("s1"));
    }

    @Override
    public void onDestroy() {
        try {
            if (report != null)
                report.close();
        } catch (IOException e) {
            LogUtils.e(TAG, "Problem while activity closing", e);
        }
        try {
            if (printingHandler != null)
                printingHandler.dispose();
        } catch (IOException e) {
            LogUtils.e(TAG, "Problem while activity closing", e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Prepares pdf report file
     *
     * @return @link{Observable} on result
     */
    private Observable<Tuple.Two<File, String>> preparePdf() {
        return Observable.create((Subscriber<? super Tuple.Two<File, String>> subscriber) -> {
            if (printingProgress != null)
                throw new ExecutionException("Printing already in progress");

            String reportName = "";
            try {
                reportName = "Report " + TimeUtils.dateToString(new Date(), TimeUtils.dateTimeFileNamePattern);
            } catch (ParseException e) {
                throw ExecutionException.wrap(e);
            }

            File targetDir = Environment.getExternalStorageDirectory();

            printingProgress = new ProgressDialog(this);
            printingProgress.setTitle(R.string.printing);
            printingProgress.setMessage(getString(R.string.preparing_pdf));
            printingProgress.show();

            Subscription printing = printingHandler.print(reportName, targetDir)
                    .subscribe(new Subscriber<Tuple.Two<File, String>>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            printingProgress.dismiss();
                            printingProgress = null;
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(Tuple.Two<File, String> result) {
                            printingProgress.dismiss();
                            printingProgress = null;
                            subscriber.onNext(result);
                        }
                    });
            printingProgress.setOnCancelListener(dialog -> printing.unsubscribe());
        });
    }

    /**
     * Prints report
     */
    public void printReport() {
        RxExecutor.runWithUiCallback(preparePdf()).subscribe(
                result -> new AlertDialog.Builder(this)
                        .setTitle(R.string.printing_completed)
                        .setMessage(getString(R.string.printing_output, result.first.getName()))
                        .setPositiveButton(android.R.string.ok, (ignore1, ignore2) -> {
                        })
                        .create()
                        .show(),
                throwable -> Toast.makeText(this, R.string.printing_failed, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_sdcard:
                printReport();
                return false;
            case R.id.action_send_email:
                sendEmail();
                return false;
            case R.id.action_google_drive:
                googleDriveUpload();
                return false;
            case R.id.action_report_bug:
                SupportDialog.show(this, getSupportFragmentManager());
                return false;
            case R.id.action_google_cloud_print:
                googleCloudPrint();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sends email
     */
    private void sendEmail() {
        LogUtils.v(TAG, "send email");
        RxExecutor.runWithUiCallback(preparePdf()).subscribe(result -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_mail_subject, result.second));
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_mail_text));
            Uri uri = Uri.fromFile(result.first);
            LogUtils.v(TAG, "URI: " + uri.toString());
            i.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(i, getString(R.string.send_mail)));
        });
    }

    /**
     * Uploads file to Google Drive
     */
    private void googleDriveUpload() {
//        if (NetworkUtils.isOnline(this)) {
        LogUtils.v(TAG, "googleDriveUpload");
        RxExecutor.runWithUiCallback(preparePdf()
                        .flatMap(result -> googleDriveUploader.upload(result.first, result.second))
        ).subscribe(ignore -> {
                },
                throwable -> Toast.makeText(ReportActivity.this, getString(R.string.could_not_connect_with_google_drive, throwable.getLocalizedMessage()), Toast.LENGTH_LONG).show()
        );
//        } else {
//            OfflineModeEnabledDialog offlineModeEnabledDialog = new OfflineModeEnabledDialog(R.string.disable_offline_mode_to_use_google_drive) {
//                @Override
//                public void startSettingsActivity() {
//                    this.startActivityForResult(new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS), ENABLE_INTERNET_REQUEST);
//                }
//            };
//            offlineModeEnabledDialog.show(getSupportFragmentManager(), "networkDialog");
//        }
    }

    /**
     * Handles Google Cloud Print
     */
    private void googleCloudPrint() {
        LogUtils.v(TAG, "googleCloudPrint");
        RxExecutor.runWithUiCallback(preparePdf()).subscribe(results -> {
            Intent printIntent = new Intent(this, PrintDialogActivity.class);
            Uri uri = Uri.fromFile(results.first);
            printIntent.setDataAndType(uri, "application/pdf");
            printIntent.putExtra("title", results.second);
            startActivity(printIntent);
        });
    }
}
