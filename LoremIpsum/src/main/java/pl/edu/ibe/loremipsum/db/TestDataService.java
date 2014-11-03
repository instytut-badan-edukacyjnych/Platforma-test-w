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



package pl.edu.ibe.loremipsum.db;

import java.util.Date;
import java.util.List;

import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.DepartmentDao;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.ExamineeDao;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.InstitutionDao;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExaminee;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExamineeDao;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitution;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitutionDao;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;

/**
 * Created by adam on 17.10.14.
 */
public class TestDataService extends BaseService {

    public static final String TEST_STRING = "Test";

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public TestDataService(ServiceProvider services) {
        super(services);
    }

    public void attachTestDataEverywhereIsPossible() {
        Institution testInstitution = getTestInstitution();
        Department testDepartment = getTestDepartment();
        Examinee testExaminee = getTestExaminee(testInstitution, testDepartment);


        List<Researcher> researcherList = dbAccess().getDaoSession().getResearcherDao().loadAll();
        List<ResearcherJoinExaminee> researcherJoinExamineeList;
        boolean hasTestExaminee = false;
        ResearcherJoinExaminee testResearcherJoinExaminee;
        for (Researcher researcher : researcherList) {
            researcherJoinExamineeList = dbAccess().getDaoSession().getResearcherJoinExamineeDao().queryBuilder().where(ResearcherJoinExamineeDao.Properties.Researcher_fk.eq(researcher.getId())).list();
            for (ResearcherJoinExaminee researcherJoinExaminee : researcherJoinExamineeList) {
                if (researcherJoinExaminee.getExaminee_fk() == testExaminee.getId()) {
                    hasTestExaminee = true;
                }
            }
            if (!hasTestExaminee) {
                testResearcherJoinExaminee = new ResearcherJoinExaminee();
                testResearcherJoinExaminee.setExaminee(testExaminee);
                testResearcherJoinExaminee.setResearcher(researcher);
                dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(testResearcherJoinExaminee);
            }
            hasTestExaminee = false;
        }


        boolean hasTestInstitution = false;
        List<ResearcherJoinInstitution> researcherJoinInstitutionList;
        ResearcherJoinInstitution testResearcherJoinInstitution;
        for (Researcher researcher : researcherList) {
            researcherJoinInstitutionList = dbAccess().getDaoSession().getResearcherJoinInstitutionDao().queryBuilder().where(ResearcherJoinInstitutionDao.Properties.Researcher_fk.eq(researcher.getId())).list();
            for (ResearcherJoinInstitution researcherJoinInstitution : researcherJoinInstitutionList) {
                if (researcherJoinInstitution.getInstitution_fk() == testInstitution.getId()) {
                    hasTestInstitution = true;
                }
            }
            if (!hasTestInstitution) {
                testResearcherJoinInstitution = new ResearcherJoinInstitution();
                testResearcherJoinInstitution.setInstitution(testInstitution);
                testResearcherJoinInstitution.setResearcher(researcher);
                dbAccess().getDaoSession().getResearcherJoinInstitutionDao().insert(testResearcherJoinInstitution);
            }
            hasTestInstitution = false;
        }
        dbAccess().getDaoSession().clear();

    }

    private Department getTestDepartment() {
        List<Department> list = dbAccess().getDaoSession().getDepartmentDao().queryBuilder().where(DepartmentDao.Properties.Name.eq(TEST_STRING)).list();
        Department testDepartment = null;
        if (list.size() > 0) {
            testDepartment = list.get(0);
        } else {
            testDepartment = new Department();
            testDepartment.setName(TEST_STRING);
            dbAccess().getDaoSession().getDepartmentDao().insert(testDepartment);
        }
        return testDepartment;
    }

    private Examinee getTestExaminee(Institution testInstitution, Department testDepartment) {
        List<Examinee> list = dbAccess().getDaoSession().getExamineeDao().queryBuilder().where(ExamineeDao.Properties.TextId.eq(TEST_STRING)).list();
        Examinee testExaminee = null;
        if (list.size() > 0) {
            testExaminee = list.get(0);
        } else {
            testExaminee = new Examinee();
            testExaminee.setBirthday(new Date());
            testExaminee.setGender(Gender.MALE.toString());
            testExaminee.setFirstName(TEST_STRING);
            testExaminee.setLastName(TEST_STRING);
            testExaminee.setTextId(TEST_STRING);
            testExaminee.setDepartment(testDepartment);
            testExaminee.setInstitution(testInstitution);
            dbAccess().getDaoSession().getExamineeDao().insert(testExaminee);
        }
        return testExaminee;
    }

    private Institution getTestInstitution() {
        List<Institution> list = dbAccess().getDaoSession().getInstitutionDao().queryBuilder().where(InstitutionDao.Properties.TextId.eq(TEST_STRING)).list();
        Institution institution = null;
        if (list.size() > 0) {
            institution = list.get(0);
        } else {
            institution = new Institution();
            institution.setTextId(TEST_STRING);
            institution.setCity(TEST_STRING);
            institution.setName(TEST_STRING);
            institution.setStreet(TEST_STRING);
            institution.setPostalCode(TEST_STRING);
            institution.setProvince(TEST_STRING);
            dbAccess().getDaoSession().getInstitutionDao().insert(institution);
        }
        return institution;
    }
}
