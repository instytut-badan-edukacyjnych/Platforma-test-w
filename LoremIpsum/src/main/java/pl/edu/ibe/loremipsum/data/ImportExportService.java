
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

package pl.edu.ibe.loremipsum.data;

import org.simpleframework.xml.core.Persister;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.data.dataexport.DataExporter;
import pl.edu.ibe.loremipsum.data.dataexport.ExportException;
import pl.edu.ibe.loremipsum.data.dataimport.DataImporter;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.ExamineeDao;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.InstitutionDao;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherDao;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExaminee;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinInstitution;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import rx.Observable;

/**
 * Provides data import and export operations and helps convert between dataformat.
 * Note that data can be imported or exported only all at once because of database relations.
 * Xml file supports it only on base level id entry is represented in db as text_id
 * Created by adam on 25.03.14.
 */
public class ImportExportService extends BaseService {
    /**
     * Separator for address institution elements
     */
    private static final String INSTITUTION_ADDRESS_SEPARATOR = "::";
    protected String researchersPath;
    protected String examinedPath;
    protected String institutionPath;


    private DataImporter dataImporter;
    private DataExporter dataExporter;

    /**
     * Creates service with context and dbaccess.
     */
    public ImportExportService(ServiceProvider services, Persister persister) {
        super(services);
        dataImporter = new DataImporter(persister);
        dataExporter = new DataExporter(persister);
        String inputDir = LoremIpsumApp.APP_IMPORTEXPORT_PATH;
        researchersPath = inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + TaskSuiteConfig.m_cardsFileName;
        examinedPath = inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + TaskSuiteConfig.m_pupilsFileName;
        institutionPath = inputDir + LoremIpsumApp.SYS_FILE_SEPARATOR + TaskSuiteConfig.m_schoolsFileName;
    }

    /**
     * Imports data from xml files. Due database relations all data need to be imported at once
     *
     * @return true if import was successful
     */
    public Observable<Boolean> importData() {
        return RxExecutor.run(() -> {
            boolean success = true;
            try {
                IEInstitutionWrapper ieInstitutionWrapper = dataImporter.importFromFile(IEInstitutionWrapper.class, institutionPath);
                IEExamineeWrapper ieExamineeWrapper = dataImporter.importFromFile(IEExamineeWrapper.class, examinedPath);
                IEResearcherWrapper ieResearcherWrapper = dataImporter.importFromFile(IEResearcherWrapper.class, researchersPath);

                List<Researcher> researchers = convertIEResearchers(ieResearcherWrapper);
                List<Institution> institutions = convertIEInstitutions(ieInstitutionWrapper, researchers);
                List<Examinee> examineds = convertIEExaminee(ieExamineeWrapper, researchers, institutions);
                dbAccess().getDaoSession().clear();
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        });
    }

    /**
     * Converts xml data to database readable format
     *
     * @param ieExamineeWrapper
     * @param researchers
     * @param institutions
     * @return
     */
    private List<Examinee> convertIEExaminee(IEExamineeWrapper ieExamineeWrapper, List<Researcher> researchers, List<Institution> institutions) {
        ArrayList<Examinee> examineds = new ArrayList<>();
        Examinee examined;


        for (IEExamineeWrapper.IEExaminee ieExaminee : ieExamineeWrapper.examinded) {
            if (dbAccess().getDaoSession().getExamineeDao().queryBuilder().where(ExamineeDao.Properties.TextId.eq(ieExaminee.id)).list().size() > 0) {
                continue;
            }


            examined = new Examinee();

            examined.setTextId(ieExaminee.id);
            examined.setFirstName(ieExaminee.name);
            examined.setLastName(ieExaminee.surname);
            examined.setAdditionalData(ieExaminee.additionalData);
            try {
                examined.setBirthday(TimeUtils.stringToDate(ieExaminee.birth, TimeUtils.defaultPatern));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Gender gender = Gender.resolveGender(ieExaminee.gender);
            examined.setGender(gender.toString());


            resolveDepartment(examined, ieExaminee);


            for (Institution institution : institutions) {
                if (institution.getTextId().equals(ieExaminee.school)) {
                    examined.setInstitution(institution);
                    break;
                }
            }

            dbAccess().getDaoSession().getExamineeDao().insert(examined);


            ResearcherJoinExaminee researcherJoinExaminee;
            if (ieExaminee.assignedResearchers == null || ieExaminee.assignedResearchers.size() == 0) {
                Researcher researcher = dbAccess().getDaoSession().getResearcherDao().
                        queryBuilder().where(ResearcherDao.Properties.TextId.eq(ieExaminee.assign)).list().get(0);
                researcherJoinExaminee = new ResearcherJoinExaminee();
                researcherJoinExaminee.setResearcher(researcher);
                researcherJoinExaminee.setExaminee(examined);
                dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);
                researcher.resetResearcherJoinExamineeList();
                examined.resetResearcherJoinExamineeList();
            } else {
                for (String assignedResearcher : ieExaminee.assignedResearchers) {
                    for (Researcher researcher : researchers) {
                        if (researcher.getTextId().equals(assignedResearcher)) {
                            researcherJoinExaminee = new ResearcherJoinExaminee();
                            researcherJoinExaminee.setResearcher(researcher);
                            researcherJoinExaminee.setExaminee(examined);
                            dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);
                            researcher.resetResearcherJoinExamineeList();
                            examined.resetResearcherJoinExamineeList();
                        }
                    }
                }
            }
            examineds.add(examined);
        }


        return examineds;
    }

    /**
     * Creates departments based on examinee
     *
     * @param examined
     * @param ieExaminee
     */
    private void resolveDepartment(Examinee examined, IEExamineeWrapper.IEExaminee ieExaminee) {
        List<Department> departments = dbAccess().getDaoSession().getDepartmentDao().loadAll();
        boolean departmentExists = false;
        for (Department department : departments) {
            if (department.getName().equals(ieExaminee.group)) {
                departmentExists = true;
                examined.setDepartment(department);
                break;
            }
        }

        if (!departmentExists) {
            Department department = new Department();
            department.setName(ieExaminee.group);
            dbAccess().getDaoSession().getDepartmentDao().insert(department);
            examined.setDepartment(department);
        }
    }

    /**
     * Converts xml institutions to db readable format
     *
     * @param ieInstitutionWrapper
     * @param researchers
     * @return
     */
    private List<Institution> convertIEInstitutions(IEInstitutionWrapper ieInstitutionWrapper, List<Researcher> researchers) {
        ArrayList<Institution> institutions = new ArrayList<Institution>();
        Institution institution;
        for (IEInstitutionWrapper.IEInstitution ieInstitution : ieInstitutionWrapper.institutions) {
            if (dbAccess().getDaoSession().getInstitutionDao().queryBuilder().where(InstitutionDao.Properties.TextId.eq(ieInstitution.id)).list().size() > 0) {
                continue;
            }


            institution = new Institution();

            institution.setTextId(ieInstitution.id);
            institution.setName(ieInstitution.name);

            String[] addressArray = ieInstitution.address.split(INSTITUTION_ADDRESS_SEPARATOR);

            //Imported xml is in new data format so we can put it into db properly.
            if (addressArray.length == 4) {
                institution.setStreet(addressArray[0]);
                institution.setPostalCode(addressArray[1]);
                institution.setCity(addressArray[2]);
                institution.setProvince(addressArray[3]);
            } else {
                institution.setLegacyAddress(ieInstitution.address);
            }
            dbAccess().getDaoSession().getInstitutionDao().insert(institution);

            ResearcherJoinInstitution researcherJoinInstitution;
            if (ieInstitution.assignedResearchers == null || ieInstitution.assignedResearchers.size() == 0) {
                Researcher researcher = dbAccess().getDaoSession().getResearcherDao().
                        queryBuilder().where(ResearcherDao.Properties.TextId.eq(ieInstitution.assign)).list().get(0);
                researcherJoinInstitution = new ResearcherJoinInstitution();
                researcherJoinInstitution.setResearcher(researcher);
                researcherJoinInstitution.setInstitution(institution);

                dbAccess().getDaoSession().getResearcherJoinInstitutionDao().insert(researcherJoinInstitution);
                institution.resetResearcherJoinInstitutionList();
                researcher.resetResearcherJoinInstitutionList();
            } else {
                for (String researcherTextId : ieInstitution.assignedResearchers) {
                    for (Researcher researcher : researchers) {
                        if (researcherTextId.equals(researcher.getTextId())) {
                            researcherJoinInstitution = new ResearcherJoinInstitution();
                            researcherJoinInstitution.setResearcher(researcher);
                            researcherJoinInstitution.setInstitution(institution);

                            dbAccess().getDaoSession().getResearcherJoinInstitutionDao().insert(researcherJoinInstitution);
                            institution.resetResearcherJoinInstitutionList();
                            researcher.resetResearcherJoinInstitutionList();
                        }
                    }
                }
            }
            institutions.add(institution);
        }


        return institutions;
    }

    /**
     * Converts xml researchers to db readable format
     *
     * @param ieResearcherWrapper
     * @return
     */
    private List<Researcher> convertIEResearchers(IEResearcherWrapper ieResearcherWrapper) {
        ArrayList<Researcher> researchers = new ArrayList<Researcher>();
        Researcher researcher;
        for (IEResearcherWrapper.IEResearcher ieResearcher : ieResearcherWrapper.researchers) {
            if (dbAccess().getDaoSession().getResearcherDao().queryBuilder().where(ResearcherDao.Properties.TextId.eq(ieResearcher.id)).list().size() > 0) {
                continue;
            }
            researcher = new Researcher();

            researcher.setTextId(ieResearcher.id);
            researcher.setFirstName(ieResearcher.name);
            researcher.setSurName(ieResearcher.surname);
            researcher.setPassword(ieResearcher.password);
            researcher.setHidden(false);
            dbAccess().getDaoSession().getResearcherDao().insert(researcher);

            researchers.add(researcher);
        }

        return researchers;
    }

    /**
     * Exports data to xml files. Due database relations all data need to be exported at once
     *
     * @return true if export was successful
     */
    public Observable<Boolean> exportData() {
        return new RxExecutor().run(() -> {
            boolean success = true;
            List<Institution> institutions = dbAccess().getDaoSession().getInstitutionDao().loadAll();
            List<Researcher> researchers = dbAccess().getDaoSession().getResearcherDao().loadAll();
            List<Examinee> examineds = dbAccess().getDaoSession().getExamineeDao().loadAll();

            IEInstitutionWrapper ieInstitutionWrapper = convertInstitutions(institutions);
            IEResearcherWrapper ieResearcherWrapper = convertResearchers(researchers);
            IEExamineeWrapper ieExamineeWrapper = convertExaminee(examineds);

            try {
                dataExporter.exportToXml(ieInstitutionWrapper, institutionPath);
                dataExporter.exportToXml(ieExamineeWrapper, examinedPath);
                dataExporter.exportToXml(ieResearcherWrapper, researchersPath);
            } catch (ExportException e) {
                e.printStackTrace();
                success = false;
            }

            return success;
        });
    }

    /**
     * Converts examinee in db format to xml format
     *
     * @param examineds
     * @return
     */
    private IEExamineeWrapper convertExaminee(List<Examinee> examineds) {
        IEExamineeWrapper ieExamineeWrapper = new IEExamineeWrapper();
        IEExamineeWrapper.IEExaminee ieExaminee;

        for (Examinee examined : examineds) {
            ieExaminee = new IEExamineeWrapper.IEExaminee();

            ieExaminee.id = examined.getTextId();
            ieExaminee.name = examined.getFirstName();
            ieExaminee.surname = examined.getLastName();
            ieExaminee.assign = examined.getResearcherJoinExamineeList().get(0).getResearcher().getTextId();
            try {
                ieExaminee.birth = TimeUtils.dateToString(examined.getBirthday(), TimeUtils.defaultPatern);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ieExaminee.gender = examined.getGender();
            ieExaminee.group = examined.getDepartment().getName();
            ieExaminee.school = examined.getInstitution().getTextId();
            ieExaminee.additionalData = examined.getAdditionalData();
            for (ResearcherJoinExaminee researcherJoinExaminee : examined.getResearcherJoinExamineeList()) {
                ieExaminee.assignedResearchers.add(researcherJoinExaminee.getResearcher().getTextId());
            }


            ieExamineeWrapper.examinded.add(ieExaminee);
        }


        return ieExamineeWrapper;
    }

    /**
     * Converts researcher in db format to xml format
     *
     * @param researchers
     * @return
     */
    private IEResearcherWrapper convertResearchers(List<Researcher> researchers) {
        IEResearcherWrapper ieResearcherWrapper = new IEResearcherWrapper();
        IEResearcherWrapper.IEResearcher ieResearcher;
        for (Researcher researcher : researchers) {
            if (researcher.getHidden() == null || researcher.getHidden()) {
                continue;
            }
            ieResearcher = new IEResearcherWrapper.IEResearcher();

            ieResearcher.id = researcher.getTextId();
            ieResearcher.name = researcher.getFirstName();
            ieResearcher.surname = researcher.getSurName();
            ieResearcher.password = researcher.getPassword();

            ieResearcherWrapper.researchers.add(ieResearcher);
        }

        return ieResearcherWrapper;
    }

    /**
     * Converts institution in db format to xml format
     *
     * @param institutions
     * @return
     */
    private IEInstitutionWrapper convertInstitutions(List<Institution> institutions) {
        IEInstitutionWrapper wrapper = new IEInstitutionWrapper();
        IEInstitutionWrapper.IEInstitution ieInstitution;

        for (Institution institution : institutions) {
            ieInstitution = new IEInstitutionWrapper.IEInstitution();

            if (institution.getLegacyAddress() != null) {
                ieInstitution.address = institution.getLegacyAddress();
            } else {
                ieInstitution.address = institution.getStreet() + INSTITUTION_ADDRESS_SEPARATOR + institution.getPostalCode() + INSTITUTION_ADDRESS_SEPARATOR
                        + institution.getCity() + INSTITUTION_ADDRESS_SEPARATOR + institution.getProvince();
            }
            ieInstitution.id = institution.getTextId();
            ieInstitution.assign = institution.getResearcherJoinInstitutionList().get(0).getResearcher().getTextId();
            ieInstitution.name = institution.getName();
            for (ResearcherJoinInstitution researcherJoinInstitution : institution.getResearcherJoinInstitutionList()) {
                ieInstitution.assignedResearchers.add(researcherJoinInstitution.getResearcher().getTextId());
            }

            wrapper.institutions.add(ieInstitution);

        }
        return wrapper;
    }
}
