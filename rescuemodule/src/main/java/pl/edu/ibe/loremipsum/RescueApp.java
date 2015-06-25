package pl.edu.ibe.loremipsum;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by adam on 6/21/15.
 */
public class RescueApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
          Stetho.newInitializerBuilder(this)
            .enableDumpapp(
              Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector(
              Stetho.defaultInspectorModulesProvider(this))
            .build());
    }
}
