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
package pl.edu.ibe.loremipsum.tablet.login;

import java.util.ArrayList;
import java.util.List;

import pl.edu.ibe.loremipsum.db.schema.Credential;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherDao;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.db.schema.TaskSuite;
import pl.edu.ibe.loremipsum.db.schema.TaskSuiteDao;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Created by adam on 17.03.14.
 * Service providing operations connected with logging in
 */
public class LoginService extends BaseService {

    /**
     * Current logged in @link{Researcher}
     */
    public Researcher currentLoggedInUser;

    /**
     * Creates service with @link{ServiceProvider}
     *
     * @param services Services provider
     */
    public LoginService(ServiceProvider services) {
        super(services);
    }

    /**
     * Performs login
     *
     * @param user
     * @param password
     * @return @link{Observable} on result.
     */
    public Observable<List<Researcher>> login(String user, String password) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().queryBuilder()
                .where(ResearcherDao.Properties.TextId.eq(user), ResearcherDao.Properties.Password.eq(password)).list());
    }

    /**
     * Lists all available logins
     *
     * @return @link{Observable} on result
     */
    public Observable<List<String>> getUserLogins() {
        return RxExecutor.run(() -> {
            List<String> logins = new ArrayList<>();
            List<Researcher> researchers = dbAccess().getDaoSession().getResearcherDao().loadAll();
            for (Researcher researcher : researchers) {
                logins.add(researcher.getTextId());
            }
            return logins;
        });
    }

    public Observable<Researcher> performAutoLogin() {
        return new RxExecutor().run(() -> {
            List<Researcher> list = getServiceProvider().researcher().listResearchers().toBlockingObservable().first();
            if (list.size() > 0) {
                return list.get(0);
            } else {
                Researcher researcher = new Researcher();
                researcher.setFirstName("L");
                researcher.setSurName("L");
                researcher.setPassword("llllll");
                researcher.setTextId("llllll");
                researcher.setHidden(false);
                getServiceProvider().researcher().insertResearcher(researcher).toBlockingObservable().first();


                TaskSuite taskSuite = dbAccess().getDaoSession().getTaskSuiteDao().queryBuilder().where(TaskSuiteDao.Properties.Demo.eq(false)).list().get(0);

                Credential credential = new Credential();
                credential.setPassword("fakeData");
                credential.setManifestUrl("fakeData");
                credential.setUser("fakeData");
                dbAccess().getDaoSession().getCredentialDao().insert(credential);

                ResearchersSuite researchersSuite = new ResearchersSuite();
                researchersSuite.setAgreedForCollector(true);
                researchersSuite.setResearcher(researcher);
                researchersSuite.setSawCollectorOpt(true);
                researchersSuite.setTaskSuite(taskSuite);
                researchersSuite.setCredential(credential);
                dbAccess().getDaoSession().getResearchersSuiteDao().insert(researchersSuite);


                return researcher;
            }
        });
    }
}
