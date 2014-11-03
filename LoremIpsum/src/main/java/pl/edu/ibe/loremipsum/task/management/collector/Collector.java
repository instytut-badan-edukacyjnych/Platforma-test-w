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

package pl.edu.ibe.loremipsum.task.management.collector;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.db.schema.ResultsQueue;
import pl.edu.ibe.loremipsum.network.NetworkSupport;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import rx.Observable;

/**
 * Created by mikolaj on 10.04.14.
 */
public class Collector extends BaseService {

    private static final String TAG = Collector.class.getSimpleName();
    private final String collector = "Collector";

    public Collector(ServiceProvider services) {
        super(services);
    }

    private NetworkChangeReceiver.NetworkUtil networkUtil() {
        return getServiceProvider().getNetworkUtil();
    }

    private NetworkSupport networkSupport() {
        return getServiceProvider().getNetworkSupport();
    }

    public void addResult(ResultsQueue result) {
        dbAccess().getDaoSession().getResultsQueueDao().insert(result);
    }

    public Observable<Boolean> send() {
        if (networkUtil().getConnectivityStatus() != NetworkChangeReceiver.NetworkUtil.TYPE_WIFI) {
            return Observable.error(new IOException("Wifi not available"));
        }
        List<ResultsQueue> resultsToSend = dbAccess().getDaoSession().getResultsQueueDao().loadAll();
        List<Observable<ResultsQueue>> observables = new ArrayList<>(resultsToSend.size());
        for (ResultsQueue result : resultsToSend) {
            LogUtils.d(TAG, "ResultsQueue{" +
                    "id=" + result.getId() +
                    ", fileName='" + result.getFileName() + '\'' +
                    ", submitUrl='" + result.getSubmitUrl() + '\'' +
                    ", markToDelete=" + result.getMarkToDelete() +
                    ", attempts=" + result.getAttempts() +
                    ", lastAttemptDate=" + result.getLastAttemptDate() +
                    '}');
            try {
                Observable<ResultsQueue> observable = send(result)
                        .map(_ign -> {
                            result.setMarkToDelete(true);
                            return result;
                        })
                        .doOnError(e -> {
                            result.setAttempts(result.getAttempts() + 1);
                            LogUtils.e(collector, "Failed to send", e);
                        })
                        .onErrorResumeNext(Observable.from(result));
                observables.add(observable);
            } catch (MalformedURLException | FileNotFoundException e) {
                LogUtils.e(collector, "Couldn't send results", e);
                //if we cannot create url, we will never ever be able to send it, we should consider this as done, and remove from queue.
                result.setMarkToDelete(true);
                observables.add(Observable.from(result));
            }
        }

        return Observable.from(observables)
                .flatMap(t -> t)
                .map(result -> {
                    if (result.getMarkToDelete()) {
                        dbAccess().getDaoSession().getResultsQueueDao().delete(result);
                    } else {
                        result.setLastAttemptDate(new Date(System.currentTimeMillis()));
                        dbAccess().getDaoSession().getResultsQueueDao().update(result);
                    }
                    return true;
                })
                .all(t -> t)
                .doOnError(e -> LogUtils.e(collector, "Failed to send", e));
    }

    private Observable<CollectorSenderRequest.CollectorSenderResponse> send(ResultsQueue result) throws MalformedURLException, FileNotFoundException {
        FileInputStream fis = new FileInputStream(new File(result.getFileName()));
        return new CollectorSenderRequest(new URL(result.getSubmitUrl()), result.getFileName(), fis, networkSupport()).prepare();
    }


    private Observable<ResearchersSuite> getResearcherSuiteToUpdate() {
        return getServiceProvider().researcher().getCurrentResearcherSuite();
    }

    public Observable<Boolean> shouldDisplayAgreedForCollectionDialog() {
        return getResearcherSuiteToUpdate().map(ResearchersSuite::getSawCollectorOpt);
    }

    public Observable<Boolean> isSendingDataAllowed() {
        return getResearcherSuiteToUpdate().map(ResearchersSuite::getAgreedForCollector);
    }

    public Observable<Boolean> updateAgreement(boolean agreed) {
        return getResearcherSuiteToUpdate().map((researchersSuite) -> {
            researchersSuite.setAgreedForCollector(agreed);
            dbAccess().getDaoSession().getResearchersSuiteDao().update(researchersSuite);
            return true;
        });
    }

    public Observable<Boolean> updateSawCollectorOpt(boolean saw) {
        return getResearcherSuiteToUpdate().map((researchersSuite) -> {
            researchersSuite.setSawCollectorOpt(saw);
            dbAccess().getDaoSession().getResearchersSuiteDao().update(researchersSuite);
            return true;
        });
    }

    public boolean isReportingRequired() {
        return services.currentTaskSuite().getCurrentTaskSuiteConfig().collectorConfig.isRaportingRequired;
    }
}
