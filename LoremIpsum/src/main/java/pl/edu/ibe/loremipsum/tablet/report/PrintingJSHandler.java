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
package pl.edu.ibe.loremipsum.tablet.report;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import pl.edu.ibe.loremipsum.tools.CancelException;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by adam on 08.04.14.
 * Connector with js interface
 */
public class PrintingJSHandler {
    private static final String TAG = PrintingJSHandler.class.toString();

    private final String pagePrefix = "page_";
    private boolean inPrint;
    private boolean pageCountSet;
    /**
     * Total page count
     */
    private int pageCount;
    /**
     * One based index
     */
    private int currentPage;
    private Activity activity;
    private WebView webView;
    private File cacheDir;
    private StoreImageTask storeImageTask;
    private Subject<File, File> subjectPrint;
    private boolean done = false;
    private String reportName;
    private File targetDir;

    public PrintingJSHandler(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    /**
     * install interface to webView
     */
    public void install() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "printer");
    }

    /**
     * prints report to file
     *
     * @param reportName
     * @param targetDir
     * @return
     */
    public Observable<Tuple.Two<File, String>> print(String reportName, File targetDir) {
        this.reportName = reportName;
        this.targetDir = targetDir;
        subjectPrint = BehaviorSubject.create((File) null);
        Observable<File> controller = Observable.create((Subscriber<? super File> subscriber) -> {
            try {
                beginPrinting();
                while (!done) {
                    if (subscriber.isUnsubscribed()) {
                        LogUtils.w(TAG, "Printing cancelled");
                        inPrint = false;
                        throw new CancelException();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LogUtils.e(TAG, "Problem while sleeping", e);
                    }
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread());
        return Observable.merge(subjectPrint, controller)
                .filter(file -> file != null)
                .doOnNext(file -> done = true)
                .map(file -> Tuple.Two.create(file, reportName));
    }

    /**
     * Begins printing to file
     *
     * @throws IOException
     */
    private void beginPrinting() throws IOException {
        inPrint = true;

        cacheCleanup();

        File dir;
        while (true) {
            dir = new File(activity.getCacheDir(), UUID.randomUUID().toString());
            if (!dir.exists())
                break;
        }
        if (!dir.mkdirs())
            throw new IOException("Could not create directory \"" + dir.getAbsolutePath() + "\"");
        cacheDir = dir;

        pageCount = 1;
        currentPage = 1;
        pageCountSet = false;
        LogUtils.v(TAG, "beginPrinting - PrintHelper.requestPageCount()");
        RxExecutor.runWithUiCallback(() -> webView.loadUrl("javascript:PrintHelper.requestPageCount()"));
    }

    /**
     * Action called after printing finish
     */
    private void finishPrinting() {
        inPrint = false;
        new MakePdf((result, problem, output) -> {
            try {
                File target = new File(
                        targetDir,
                        reportName + ".pdf"
                );
                CopyStream.copyStream(new FileInputStream(output), new FileOutputStream(target))
                        .execute().subscribe();

                currentPage = 1;
                displayPage();

                cacheCleanup();

                subjectPrint.onNext(target);
            } catch (Throwable t) {
                subjectPrint.onError(t);
            }
        }).execute((Void) null);
    }

    /**
     * cleans up cache
     *
     * @throws IOException
     */
    private void cacheCleanup() throws IOException {
        if (cacheDir != null && cacheDir.exists())
            FileUtils.deleteRecursive(cacheDir);
        cacheDir = null;
    }

    public void dispose() throws IOException {
        cacheCleanup();
    }

    /**
     * Displays page
     */
    private void displayPage() {
        activity.runOnUiThread(() -> {
            LogUtils.v(TAG, "displayPage - PrintHelper.displayPage(" + currentPage + ")");
            webView.loadUrl("javascript:PrintHelper.displayPage(" + currentPage + ");");
        });
    }

    /**
     * Prints page
     */
    private void printPage() {
        LogUtils.d(TAG, " printing");
        new android.os.Handler().postDelayed(() -> {
            LogUtils.d(TAG, "delayed printing");
            activity.runOnUiThread(() -> {
                storeImageTask = new StoreImageTask();
                Bitmap b = getBitmapFromView(webView);
                storeImageTask.execute(b);
            });
        }, 250);
    }

    @JavascriptInterface
    public void setNumberOfPages(int pageCount) {
        LogUtils.v(TAG, "setNumberOfPages(" + pageCount + ")");
        this.pageCount = pageCount;
        pageCountSet = true;
        if (inPrint) {
            displayPage();
        }
    }

    @JavascriptInterface
    public void renderFinished() {
        LogUtils.v(TAG, "renderFinished()");
        if (inPrint) {
            printPage();
        }
    }

    /**
     * Callback on page printing finish
     */
    private void onPrintPageFinished() {
        if (shouldDisplayNextPage()) {
            currentPage++;
            displayPage();
        } else if (shouldFinishPrinting()) {
            finishPrinting();
        }
    }

    /**
     * Determines if should finins printing
     *
     * @return true if should finish printing
     */
    private boolean shouldFinishPrinting() {
        return currentPage == pageCount;
    }

    /**
     * Determines if should display next page
     *
     * @return true if should display next page
     */
    private boolean shouldDisplayNextPage() {
        return currentPage < pageCount;
    }

    /**
     * Returns @link{Bitmap} created from given @link{View}
     *
     * @param view
     * @return @link{Bitmap}
     */
    public Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private interface MakePdfDone {
        void done(Boolean result, Throwable problem, File outputFile);
    }

    /**
     * Stores image
     */
    private class StoreImageTask extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            Bitmap b = params[0];
            File store = new File(cacheDir, pagePrefix + String.valueOf(currentPage) + ".png");
            FileOutputStream fos = null;
            try {
                LogUtils.v(TAG, "Will store next image as \"" + store.getAbsolutePath() + "\"");
                store.createNewFile();
                fos = new FileOutputStream(store);
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception ignore) {
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                onPrintPageFinished();
            }
        }
    }

    private class MakePdf extends AsyncTask<Void, Void, Boolean> {
        private final MakePdfDone done;
        Throwable problem = null;
        File outputFile = null;


        public MakePdf(MakePdfDone done) {
            this.done = done;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<String> pages = new ArrayList<>();
            for (String fileName : cacheDir.list()) {
                if (fileName.startsWith(pagePrefix)) {
                    pages.add(fileName);
                }
            }

            File pdfFile = new File(cacheDir, "output.pdf");
            try {
                Document document = new Document(new Rectangle(webView.getWidth() + 100, webView.getHeight() + 100), 50, 50, 50, 50);
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                for (String pageName : pages) {
                    document.add(PngImage.getImage(cacheDir.getAbsolutePath() + File.separator + pageName));
                }
                document.close();
                //remove png's
                for (String pageName : pages) {
                    try {
                        new File(cacheDir.getAbsolutePath() + File.separator + pageName).delete();
                    } catch (Exception ignore) {
                    }
                }
            } catch (DocumentException e) {
                problem = e;
                return false;
            } catch (IOException e) {
                problem = e;
                return false;
            }

            outputFile = pdfFile;
            return true;
        }

        @Override
        protected void onCancelled(Boolean result) {
            done.done(result, problem, outputFile);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            done.done(result, problem, outputFile);
        }
    }
}