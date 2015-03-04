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

package pl.edu.ibe.loremipsum.tablet.test;

import android.app.Activity;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.data.PupilData;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.ExamineeTprResultsDao;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.localization.Exceptions;
import pl.edu.ibe.loremipsum.localization.Localization;
import pl.edu.ibe.loremipsum.manager.MappingDependency;
import pl.edu.ibe.loremipsum.manager.TestManager;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.login.LoginService;
import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import rx.Observable;

/**
 * Created by adam on 20.03.14.
 */
public class CurrentTaskSuiteService extends BaseService {

    private static final String TAG = CurrentTaskSuiteService.class.toString();
    private TestRunData currentTestRunData;
    private TaskSuiteConfig taskSuiteConfig;
    private Long randomTestIdentifier;

    private boolean allowScreenRotation;
    private boolean menuOnRight;

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public CurrentTaskSuiteService(ServiceProvider services) {
        super(services);
    }


    public boolean isAllowedScreenRotation() {
        return allowScreenRotation;
    }

    public void setAllowScreenRotation(boolean allowScreenRotation) {
        this.allowScreenRotation = allowScreenRotation;
    }



    public TestRunData getCurrentTestRunData() {
        if (currentTestRunData == null) {
            currentTestRunData = new TestRunData();
        }
        return currentTestRunData;
    }

    public TaskSuiteConfig getCurrentTaskSuiteConfig() {
        return taskSuiteConfig;
    }

    public void handleTestEnd() {
//        currentTestRunData = null;
    }

    public TestRunData prepare() {
        return new TestRunData();
    }

    public Observable<TestRunData> prepareDemo(TaskSuite demoSuite) {
        return RxExecutor.run(() -> new TestRunData()
                .setTaskSuite(demoSuite)
                .setTestMode(TestMode.DEMO)
                .setInstitution(new Institution())
                .setDepartment(new Department())
                .setExaminee(new Examinee()));
    }

    public Observable<Boolean> loadTestConfig(VirtualFile rootVirtualFile) throws IOException {
        return RxExecutor.run(() -> {
            LogUtils.v(TAG, "loadTestConfig(" + rootVirtualFile + ")");
            taskSuiteConfig = new TaskSuiteConfig();
            boolean result = taskSuiteConfig.loadConfigXML(rootVirtualFile);
            LoremIpsumApp.PrepareData(rootVirtualFile);
            return result;
        });
    }


    private int runTest(Activity activity) throws AssertionError, Exception {
        PupilData pupilData = new PupilData();

        LogUtils.v(TAG, "Running test from " + currentTestRunData);
        LogUtils.v(TAG, "Task suite: " + currentTestRunData.getTaskSuite());
        LogUtils.v(TAG, "Storage: " + currentTestRunData.getTaskSuite().getRootVirtualFile());

        try {
            pupilData.m_birthday = TimeUtils.dateToString(currentTestRunData.getExaminee().getBirthday(), TimeUtils.defaultPatern);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO Cat algorithm needs birth date.
            pupilData.m_birthday = "01-01-1970";
        }

        pupilData.m_gender = currentTestRunData.getExaminee().getGender();
        pupilData.m_id = currentTestRunData.getExaminee().getTextId();
        LoremIpsumApp.m_pupilData = pupilData;
        updateTestDependency(currentTestRunData.getExaminee(), currentTestRunData.getInstitution());
        TestManager.prepareTest(currentTestRunData.getTestMode(), LoremIpsumApp.m_pupilData);

        int requestCode = TaskAct.RUN_TASK_SUITE_REQUEST;
        Intent intent = new Intent(context(), TaskAct.class);

        activity.startActivityForResult(intent, requestCode);
        return requestCode;
    }

    private void updateTestDependency(Examinee examinee, Institution institution) throws AssertionError {
        if (TestManager.m_runTestFlag != CurrentTaskSuiteService.TestMode.DEMO) {
            MappingDependency mappingDependency = LoremIpsumApp.m_testManager.getMappingDependency();
            if (examinee.getBirthday() != null) {
                mappingDependency.set(MappingDependency.EXAMINEE_AGE, examinee.getBirthday().getTime());
            }
            if (examinee.getGender() != null) {
                mappingDependency.set(MappingDependency.EXAMINEE_GENDER, examinee.getGender().toString());
            }
            if (institution != null) {
                mappingDependency.set(MappingDependency.INSTITUTION_CITY, institution.getCity());
                mappingDependency.set(MappingDependency.INSTITUTION_POSTAL, institution.getPostalCode());
            }
            mappingDependency.checkRequiredFieldsFilled();
            getServiceProvider().examinee().setExamineeToEdit(examinee, null);
            getServiceProvider().examinee().checkIfRequiredFieldsAreFilled();


        }
    }

    public String getResultsDir() {
        CurrentTaskSuiteService currentTaskSuite = getServiceProvider().currentTaskSuite();
        LoginService loginService = getServiceProvider().login();
        try {
            String login;
            try {
                login = loginService.currentLoggedInUser.getTextId();
            } catch (Exception e) {
                login = "demo";
            }
            String dir = LoremIpsumApp.m_resultDir + File.separator + currentTaskSuite.getCurrentTestRunData().getTaskSuite().getName()
                    + File.separator + login
                    + File.separator + TimeUtils.dateToString(new Date(), TimeUtils.defaultPatern);
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            return dir;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Date error");
    }

    public Localization getLocalization() throws Exceptions.LoadingException {
        return getCurrentTestRunData().getTaskSuite().getLocalization();
    }


    public static enum TestMode {
        DEMO, TUTORIAL, NORMAL
    }

    public Long getRandomTestIdentifier() {
        return randomTestIdentifier;
    }

    public class TestRunData {

        private TestMode testMode;
        private Institution institution;
        private Department department;
        private Examinee examined;
        private TaskSuite taskSuite;


        private TestRunData() {
        }

        public TaskSuite getTaskSuite() {
            return taskSuite;
        }

        public TestRunData setTaskSuite(TaskSuite taskSuite) {
            this.taskSuite = taskSuite;
            return this;
        }

        public TestMode getTestMode() {
            return testMode;
        }

        public TestRunData setTestMode(TestMode testMode) {
            TestManager.m_runTestFlag = testMode;
            this.testMode = testMode;
            return this;
        }

        public Institution getInstitution() {
            return institution;
        }

        public TestRunData setInstitution(Institution institution) {
            this.institution = institution;
            return this;
        }

        public Department getDepartment() {
            return department;
        }

        public TestRunData setDepartment(Department department) {
            this.department = department;
            return this;
        }

        public Examinee getExaminee() {
            return examined;
        }

        public TestRunData setExaminee(Examinee examined) {
            this.examined = examined;
            return this;
        }


        public int runTest(Activity parentActivity) throws AssertionError, Exception {
            Random random = new Random();
            long randomTestId = random.nextLong();
            while (getServiceProvider().getDbAccess().getDaoSession().getExamineeTprResultsDao().queryBuilder().where(ExamineeTprResultsDao.Properties.Test_id.eq(randomTestId)).list().size() != 0) {
                randomTestId = random.nextLong();
            }
            randomTestIdentifier = randomTestId;

            CurrentTaskSuiteService.this.currentTestRunData = this;
            return CurrentTaskSuiteService.this.runTest(parentActivity);
        }
    }
}
