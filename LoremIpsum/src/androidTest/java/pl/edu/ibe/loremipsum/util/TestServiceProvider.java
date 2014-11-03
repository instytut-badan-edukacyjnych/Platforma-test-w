package pl.edu.ibe.loremipsum.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import pl.edu.ibe.loremipsum.db.DbHelper;
import pl.edu.ibe.loremipsum.db.schema.DaoMaster;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProviderBuilder;
import pl.edu.ibe.loremipsum.tools.DbAccess;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.InstallationIdentifier;
import rx.functions.Func1;

/**
 * @author Mariusz Pluciński
 */
public class TestServiceProvider {

    private ServiceProvider serviceProvider;
    private File storageDir;

    private class TestInstallationIdentifier extends InstallationIdentifier {
        public TestInstallationIdentifier(String deviceId, boolean firstRun) {
            super(deviceId, firstRun);
        }
    }

    public ServiceProvider getServiceProvider(Context testContext, Context context, File storageDir, Func1<ServiceProviderBuilder, ServiceProviderBuilder> builderOpts) {
        if (serviceProvider == null) {
            try {
                this.storageDir = storageDir;
                SQLiteDatabase sqLiteDatabase = SQLiteDatabase.create(null);
                DaoMaster.createAllTables(sqLiteDatabase, false);
                DbHelper dbHelper = new DbHelper(context, sqLiteDatabase);
                int r = pl.edu.ibe.testplatform.test.R.raw.ca;
                InputStream sslKeyStore = testContext.getResources().openRawResource(r);
                ServiceProviderBuilder builder = ServiceProviderBuilder.create(context,
                        new DbAccess(dbHelper.getDaoSession()))
                        .setInstallationIdentifier(new TestInstallationIdentifier("test-" + UUID.randomUUID(), false))
                        .setFilesystemTaskStoragePath(storageDir)
                        .setSSLSocketFactory(ServiceProviderBuilder.getSSLSocketFactory(sslKeyStore))
                        .setDisposeExistingProvider(true);
                serviceProvider = builderOpts.call(builder).build();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        return serviceProvider;
    }

    public ServiceProvider getServiceProvider(Context testContext, Context context, File storageDir) {
        return getServiceProvider(testContext, context, storageDir, builder -> builder);
    }

    public void dispose() {
        if (serviceProvider != null) {
            serviceProvider.dispose();
            try {
                if (storageDir.exists())
                    FileUtils.deleteRecursive(storageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serviceProvider = null;
        }
    }
}
