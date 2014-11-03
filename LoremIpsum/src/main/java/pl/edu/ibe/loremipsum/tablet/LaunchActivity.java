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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.edu.ibe.loremipsum.tablet.login.LoginActivity;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.testplatform.R;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Mariusz Pluciński
 */
public class LaunchActivity extends Activity {
    private static final String TAG = LaunchActivity.class.toString();

    @InjectView(R.id.layout_progress)
    ViewGroup layoutProgress;

    @InjectView(R.id.layout_failed)
    ViewGroup layoutFailed;

    @InjectView(R.id.text_preparing_application_start)
    TextView preparingApplicationStart;

    @InjectView(R.id.text_detail_information)
    TextView failureDetailsInformation;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_launch);
        ButterKnife.inject(this);

        switchMode(false);

        LogUtils.v(TAG, "running initializations...");
        RxExecutor.runWithUiCallback(
                Observable.timer(2, TimeUnit.SECONDS) //this prevents quick closing this activity, which may look "wrong" for the user
                        .flatMap(ignore -> LoremIpsumApp.obtain().initialize())
                        .flatMap(ignore -> LoremIpsumApp.obtain().getServiceProvider().prepareApplication())
                        .subscribeOn(Schedulers.io())
        ).subscribe(firstRun -> {
            if (firstRun)
                preparingApplicationStart.setText(R.string.preparing_first_application_start);
        }, throwable -> {
            LogUtils.e(TAG, "Could not run application", throwable);
            failureDetailsInformation.setText(throwable.getLocalizedMessage());
            switchMode(true);
        }, () -> {
            LogUtils.v(TAG, "Closing launch activity");
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
        LogUtils.v(TAG, "running initializations done");
    }

    private void switchMode(boolean failure) {
        layoutProgress.setVisibility(failure ? View.GONE : View.VISIBLE);
        layoutFailed.setVisibility(failure ? View.VISIBLE : View.GONE);
    }
}
