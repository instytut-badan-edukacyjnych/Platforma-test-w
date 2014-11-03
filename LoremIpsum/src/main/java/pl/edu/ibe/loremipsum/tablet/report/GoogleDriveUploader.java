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
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * @author Mariusz Pluciński
 *         Allows upload files to Google Drive
 */
public class GoogleDriveUploader {
    private static final String TAG = GoogleDriveUploader.class.toString();

    private final GoogleApiClient googleApiClient;
    private final int requestCodeResolution;
    private final int requestCodeCreator;
    private Activity activity;
    private Subject<Object, Object> uploadSubject = BehaviorSubject.create((Object) null);
    private File inputFile;
    private String inputName;

    public GoogleDriveUploader(Activity activity, int requestCodeResolution, int requestCodeCreator) {
        this.activity = activity;
        this.requestCodeResolution = requestCodeResolution;
        this.requestCodeCreator = requestCodeCreator;
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(new ConnectionCallbacks())
                .addOnConnectionFailedListener(new OnConnectionFailedListener()).build();
    }

    /**
     * Uploads file to Google Drive
     *
     * @param file
     * @param name
     * @return
     */
    public Observable<Object> upload(File file, String name) {
        return RxExecutor.run(() -> {
            LogUtils.v(TAG, "Connecting with Google Drive");
            this.inputFile = file;
            this.inputName = name;
            connect();
            return RxExecutor.EMPTY_OBJECT;
        }).flatMap(ignore -> uploadSubject.asObservable())
                .doOnCompleted(this::disconnect)
                .doOnError(throwable -> disconnect());
    }

    /**
     * Connects to GoogleDrive
     */
    public void connect() {
        if (googleApiClient.isConnected())
            continueUpload();
        else
            googleApiClient.connect();
    }

    /**
     * Disconnects from GoogleDrive
     */
    public void disconnect() {
        googleApiClient.disconnect();
    }

    /**
     * Handles activity result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void activityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestCodeResolution) {
            if (resultCode == Activity.RESULT_OK) {
                LogUtils.v(TAG, "resolution ok, connecting");
                connect();
            } else {
                LogUtils.e(TAG, "resolution failed");
            }
        }
        if (requestCode == requestCodeCreator) {
            if (resultCode == Activity.RESULT_OK) {
                DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                LogUtils.i(TAG, "File created with id: " + driveId);
                uploadSubject.onNext(driveId);
                uploadSubject.onCompleted();
            } else {
                LogUtils.e(TAG, "creator failed");
                uploadSubject.onError(new ExecutionException("Creator failed"));
            }
        }
    }

    /**
     * Continues upload
     */
    private void continueUpload() {
        Drive.DriveApi.newContents(googleApiClient).setResultCallback(new ContentsCallback());
    }

    /**
     * Handles connection callbacks
     */
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            LogUtils.v(TAG, "onConnected");
            continueUpload();
        }

        @Override
        public void onConnectionSuspended(int i) {
            LogUtils.v(TAG, "onConnectionSuspended");
            uploadSubject.onError(new ExecutionException("Connection suspended"));
        }
    }

    /**
     * Handles connection failure
     */
    private class OnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            LogUtils.e(TAG, "Google Drive connection failed: " + connectionResult.toString());
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(activity, requestCodeResolution);
                } catch (IntentSender.SendIntentException e) {
                    LogUtils.e(TAG, "SendIntentException", e);
                    uploadSubject.onError(e);
//                    GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
                }
            } else {
                LogUtils.v(TAG, "hasResolution = false");
                uploadSubject.onError(new ExecutionException("Connection with Google Drive has failed"));
//                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
            }
        }
    }

    /**
     * Handles content callbacks
     */
    private class ContentsCallback implements ResultCallback<DriveApi.ContentsResult> {
        @Override
        public void onResult(DriveApi.ContentsResult contentsResult) {
            try {
                LogUtils.v(TAG, "ContentsCallback.onResult(" + contentsResult.toString() + ")");

                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("application/pdf")
                        .setTitle(inputName)
                        .build();

                Contents contents = contentsResult.getContents();
                InputStream inputStream = new FileInputStream(inputFile);

                CopyStream.copyStream(inputStream, contents.getOutputStream())
                        .setName("Uploading to Google Drive \"" + inputFile.getAbsolutePath() + "\"")
                        .execute().subscribe();

                IntentSender intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialContents(contents)
                        .build(googleApiClient);
                activity.startIntentSenderForResult(intentSender, requestCodeCreator, null, 0, 0, 0);
            } catch (Exception e) {
                LogUtils.e(TAG, "Could not start intent sender", e);
                uploadSubject.onError(new ExecutionException("Could not start intent sender for contents callback"));
            }
        }
    }
}
