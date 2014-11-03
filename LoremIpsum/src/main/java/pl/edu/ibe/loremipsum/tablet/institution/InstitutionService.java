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

package pl.edu.ibe.loremipsum.tablet.institution;

import java.util.ArrayList;
import java.util.List;

import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.InstitutionDao;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitution;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Created by adam on 17.03.14.
 * Service providing operations on @link{Institution}
 */
public class InstitutionService extends BaseService {
    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public InstitutionService(ServiceProvider services) {
        super(services);
    }

    public Observable<Institution> getInstitutionByTextId(String textId) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getInstitutionDao().queryBuilder().where(InstitutionDao.Properties.TextId.eq(textId)).unique());
    }

    /**
     * Returns list of all @link{Institution} in database
     *
     * @return @link{Observable} on result
     */
    public Observable<List<Institution>> listInstitutions() {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getInstitutionDao().loadAll());
    }

    /**
     * Returns list of @link{Institution} that belongs to @link{Researcher}
     *
     * @param researcherId
     * @return @link{Observable} on result
     */
    public Observable<List<Institution>> getResearchersInstitutions(long researcherId) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().callInTx(() -> {
            List<ResearcherJoinInstitution> researcherJoinInstitutions =
                    dbAccess().getDaoSession().getResearcherDao().load(researcherId).getResearcherJoinInstitutionList();
            List<Institution> institutions = new ArrayList<>();
            for (ResearcherJoinInstitution joinInstitution : researcherJoinInstitutions) {
                institutions.add(dbAccess().getDaoSession().getInstitutionDao().load(joinInstitution.getInstitution_fk()));
            }
            return institutions;
        }));
    }

    /**
     * Checks if @link{Institution} with given textId exists in database
     *
     * @param textId
     * @return @link{Observable} on result. True if exists
     */
    public Observable<Boolean> checkIfInstitutionExist(String textId) {
        return RxExecutor.run(() -> {
            for (Institution institution : dbAccess().getDaoSession().getInstitutionDao().loadAll()) {
                if (institution.getTextId().equals(textId)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * List of @link{Examinee} that belongs to @link{Institution}
     *
     * @param institution
     * @return @link{Observable} on result
     */
    public Observable<List<Examinee>> getInstitutionsExaminees(Institution institution) {
        return RxExecutor.run(() -> {
            institution.resetExamineeList();
            return institution.getExamineeList();
        });
    }
}
