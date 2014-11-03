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

package pl.edu.ibe.loremipsum.tablet.base;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import pl.edu.ibe.loremipsum.network.CompositeX509TrustManager;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.task.management.collector.NetworkChangeReceiver;
import pl.edu.ibe.loremipsum.task.management.storage.DefaultEncryptionLayer;
import pl.edu.ibe.loremipsum.task.management.storage.EncryptionLayer;
import pl.edu.ibe.loremipsum.task.management.storage.FilesystemTaskStorage;
import pl.edu.ibe.loremipsum.task.management.storage.TaskStorage;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.InstallationIdentifier;
import pl.edu.ibe.testplatform.R;

/**
 * @author Mariusz Pluciński
 *         Builds Service Provider
 */
public class ServiceProviderBuilder {
    private static final String taskSuitesDirectory = "suites";
    private static final String TAG = ServiceProviderBuilder.class.toString();

    private final Context context;
    private InstallationIdentifier installationIdentifier;
    private TaskStorage taskStorage;
    private File suitesStoragePath;
    private DbAccess dbAccess;

    private NetworkChangeReceiver.NetworkUtil networkUtil;
    private OkHttpClient httpClient;
    private SSLSocketFactory sslSocketFactory;

    private NetworkSupport networkSupport;
    private Gson gson;
    private Persister persister;
    private boolean disposeExistingProvider;
    private String supportUrl;

    private ServiceProviderBuilder(Context context, DbAccess dbAccess) {
        this.context = context;
        this.dbAccess = dbAccess;
    }

    static public ServiceProviderBuilder create(Context context, DbAccess dbAccess) {
        return new ServiceProviderBuilder(context, dbAccess);
    }

    public static SSLSocketFactory getSSLSocketFactory(InputStream keyStoreInputStream) {
        try {
            KeyStore localKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try {
                localKeyStore.load(keyStoreInputStream, "LoremIpsum_CA".toCharArray());
            } finally {
                keyStoreInputStream.close();
            }

            TrustManagerFactory systemTrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            systemTrustManagerFactory.init((KeyStore) null);

            TrustManagerFactory localTrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            localTrustManagerFactory.init(localKeyStore);

            CompositeX509TrustManager compositeTrustManager = new CompositeX509TrustManager();
            compositeTrustManager.addTrustManagers(systemTrustManagerFactory.getTrustManagers());
            compositeTrustManager.addTrustManagers(localTrustManagerFactory.getTrustManagers());

            TrustManager[] trustManagers = {compositeTrustManager};

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustManagers, null);
            return ctx.getSocketFactory();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ServiceProviderBuilder setInstallationIdentifier(InstallationIdentifier installationIdentifier) {
        this.installationIdentifier = installationIdentifier;
        return this;
    }

    public ServiceProviderBuilder setFilesystemTaskStoragePath(File path) {
        this.suitesStoragePath = path;
        return this;
    }

    public ServiceProviderBuilder setTaskStorage(TaskStorage taskStorage) {
        this.taskStorage = taskStorage;
        return this;
    }

    public ServiceProviderBuilder setNetworkUtil(NetworkChangeReceiver.NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
        return this;
    }

    public ServiceProviderBuilder setNetworkSupport(NetworkSupport networkSupport) {
        this.networkSupport = networkSupport;
        return this;
    }

    public ServiceProviderBuilder setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public ServiceProviderBuilder setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public ServiceProviderBuilder setGson(Gson gson) {
        this.gson = gson;
        return this;
    }

    public ServiceProviderBuilder setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
        return this;
    }

    public ServiceProviderBuilder setDisposeExistingProvider(boolean disposeExistingProvider) {
        this.disposeExistingProvider = disposeExistingProvider;
        return this;
    }

    public ServiceProvider build() throws IOException {
        if (installationIdentifier == null)
            installationIdentifier = new InstallationIdentifier(context);

        if (taskStorage == null) {
            if (suitesStoragePath == null)
                suitesStoragePath = new File(context.getFilesDir(), taskSuitesDirectory);
            if (!suitesStoragePath.exists() && !suitesStoragePath.mkdirs())
                throw new RuntimeException("Could not create suites storage directory: \"" + suitesStoragePath + "\"");
            taskStorage = new FilesystemTaskStorage(suitesStoragePath);
            EncryptionLayer taskEncryptionLayer = new DefaultEncryptionLayer(("36" + installationIdentifier.getDeviceId() + "58").getBytes());
            taskStorage.installCipher(taskEncryptionLayer);
        }

        if (networkUtil == null)
            networkUtil = new NetworkChangeReceiver.NetworkUtil(context);

        if (networkSupport == null) {
            if (httpClient == null) {
                httpClient = new OkHttpClient();
                if (sslSocketFactory == null)
                    sslSocketFactory = getSSLSocketFactory(context.getResources().openRawResource(R.raw.ca));
                httpClient.setSslSocketFactory(sslSocketFactory);
            }
            networkSupport = new NetworkSupport(httpClient);
        }

        if (gson == null)
            gson = new GsonBuilder().serializeNulls().create();

        if (persister == null)
            persister = new Persister(new Format("<?xml version=\"1.1\" encoding=\"UTF-8\" ?>"));

        if (supportUrl == null)
            supportUrl = context.getResources().getString(R.string.support_server_address);

        synchronized (ServiceProvider.class) {
            if (disposeExistingProvider) {
                try {
                    ServiceProvider.obtain().dispose();
                } catch (NullPointerException ignored) {
                }
            }

            return new ServiceProvider(context, installationIdentifier, taskStorage, dbAccess, networkUtil,
                    networkSupport, gson, persister, supportUrl);
        }
    }

}
