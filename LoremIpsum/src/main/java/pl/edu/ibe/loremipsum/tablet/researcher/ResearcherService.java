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

package pl.edu.ibe.loremipsum.tablet.researcher;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherDao;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExaminee;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitution;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitutionDao;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.db.schema.TaskSuite;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Provides async operations on @link{Researcher} entity.
 * Created by adam on 17.03.14.
 */
public class ResearcherService extends BaseService {
    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public ResearcherService(ServiceProvider services) {
        super(services);
    }

    /**
     * Returns @link{Researcher} object for selected id;
     *
     * @param entityId Database identifier
     * @return Observable for researcher
     */
    public Observable<Researcher> getResearcher(Long entityId) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().load(entityId));
    }


    public Observable<Researcher> getResearcherByTextId(String id) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().queryBuilder().where(ResearcherDao.Properties.TextId.eq(id)).list().get(0));
    }

    /**
     * Checks if researcher exists
     *
     * @param id Database identifier
     * @return @link{Observable} for result
     */
    public Observable<Boolean> researcherExist(String id) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().queryBuilder().where(ResearcherDao.Properties.TextId.eq(id)).list().size() > 0);
    }

    /**
     * Updates @link{Researcher} in db
     *
     * @param researcher Researcher instance to update
     * @return @link{Observable} for result
     */
    public Observable<Void> updateResearcher(Researcher researcher) {
        return RxExecutor.run(() -> {
            dbAccess().getDaoSession().getResearcherDao().update(researcher);
            return null;
        });
    }

    /**
     * Inserts @link{Researcher} or updates if needed
     *
     * @param researcher Researcher instance to insert
     * @return @link{Observable} for inserted researcher id
     */
    public Observable<Long> insertResearcher(Researcher researcher) {
        if (researcher.getId() != null) {
            return RxExecutor.run(() -> {
                dbAccess().getDaoSession().getResearcherDao().update(researcher);
                return researcher.getId();
            });
        }
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().insert(researcher));
    }

    /**
     * Selects all @link{Researcher} from db
     *
     * @return @link{Observable} for result
     */
    public Observable<List<Researcher>> listResearchers() {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getResearcherDao().loadAll());
    }

    /**
     * Gets @link{Researcher}s @link{Institution}
     *
     * @param researcher Researcher instance
     * @return @link{Observable} for @link{Institution} list
     */
    public Observable<List<Institution>> getResearchersInstitutions(Researcher researcher) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().callInTx(() -> {
            List<Institution> institutions = new ArrayList<>();
            researcher.resetResearcherJoinInstitutionList();
            for (ResearcherJoinInstitution researcherJoinInstitution : researcher.getResearcherJoinInstitutionList()) {
                institutions.add(researcherJoinInstitution.getInstitution());
            }
            return institutions;
        }));
    }

    /**
     * Inserts or updates @link{Researcher} and @link{Institution} with proper dependencies.
     *
     * @param researcher  Researcher instance
     * @param institution Insitution instance
     * @return @link{Observable} for @link{Institution}
     */
    public Observable<Institution> insertInstitutionWithResearcher(Researcher researcher, Institution institution) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().callInTx(() -> {
                            if (researcher.getId() == null) {
                                dbAccess().getDaoSession().getResearcherDao().insert(researcher);
                            } else {
                                dbAccess().getDaoSession().getResearcherDao().update(researcher);
                            }
                            if (institution.getId() == null) {
                                dbAccess().getDaoSession().getInstitutionDao().insert(institution);
                            } else {
                                dbAccess().getDaoSession().getInstitutionDao().update(institution);
                            }

                            QueryBuilder qb = dbAccess().getDaoSession().getResearcherJoinInstitutionDao().queryBuilder();
                            qb.where(ResearcherJoinInstitutionDao.Properties.Institution_fk.eq(institution.getId()));
                            qb.where(ResearcherJoinInstitutionDao.Properties.Researcher_fk.eq(researcher.getId()));

                            if (qb.list().size() == 0) {
                                ResearcherJoinInstitution researcherJoinInstitution = new ResearcherJoinInstitution();
                                researcherJoinInstitution.setInstitution_fk(institution.getId());
                                researcherJoinInstitution.setResearcher_fk(researcher.getId());
                                dbAccess().getDaoSession().getResearcherJoinInstitutionDao().insert(researcherJoinInstitution);
                            }

                            researcher.resetResearcherJoinInstitutionList();
                            institution.resetResearcherJoinInstitutionList();
                            return institution;
                        }
                )
        );
    }

    public Observable<Object> removeResearcher(Researcher researcher) {
        return RxExecutor.run(() -> {
            getServiceProvider().taskSuites().listResearchersSuitesForResearcher(researcher)
                    .subscribe(researchersSuite -> dbAccess().getDaoSession().getResearchersSuiteDao().delete(researchersSuite));
            dbAccess().getDaoSession().getResearcherDao().delete(researcher);
            return RxExecutor.EMPTY_OBJECT;
        });
    }

    public Observable<List<Examinee>> getResearchersExaminee(Researcher researcher) {
        return RxExecutor.run(() -> {
            ArrayList<Examinee> examinees = new ArrayList<>();
            for (ResearcherJoinExaminee researcherJoinExaminee : researcher.getResearcherJoinExamineeList()) {
                examinees.add(researcherJoinExaminee.getExaminee());
            }
            return examinees;
        });
    }

    /**
     * Filters out examineds that do not belong to the researcher
     *
     * @param researcher   Researcher
     * @param examineeList List of many examinees
     * @return List of examineds that are assigned to the researcher
     */
    public Observable<List<Examinee>> filterExaminee(Researcher researcher, List<Examinee> examineeList) {
        return dbAccess().tx(researcher::getResearcherJoinExamineeList)
                .map(joins -> {
                    List<Examinee> result = new ArrayList<>();
                    for (Examinee examined : examineeList) {
                        for (ResearcherJoinExaminee join : joins)
                            if (examined.getId() == join.getExaminee_fk()) {
                                result.add(examined);
                                break;
                            }
                    }
                    return result;
                });
    }

    /**
     * Return current ResearchersSuite or null if no suite is active.
     *
     * @return
     */
    public Observable<ResearchersSuite> getCurrentResearcherSuite() {

        return RxExecutor.run(() -> {
            ResearchersSuite researchersSuite = null;
            Researcher researcher = getServiceProvider().login().currentLoggedInUser;
            TaskSuite taskSuite = getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite().getDbEntry();
            for (ResearchersSuite suite : taskSuite.getResearchersSuiteList()) {
                if (suite.getResearcher_fk() == researcher.getId()) {
                    researchersSuite = suite;
                    break;
                }
            }
            return researchersSuite;
        });
    }

    public Observable<Researcher> getCurrentResearcher() {
        return RxExecutor.run(() -> getServiceProvider().login().currentLoggedInUser);
    }
}
