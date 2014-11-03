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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.localization.Exceptions;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.examinee.ExamineeManagerActivity;
import pl.edu.ibe.loremipsum.tablet.login.LoginService;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.tpr.FakeRandom;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;


public class TestConfigActivity extends BaseServiceActivity {
    private static final String TAG = TestConfigActivity.class.toString();

    @InjectView(R.id.institution_container)
    View institutionContainer;
    @InjectView(R.id.institution_spinner)
    Spinner institutionSpinner;
    @InjectView(R.id.department_container)
    View departmentContainer;
    @InjectView(R.id.department_spinner)
    Spinner departmentSpinner;
    @InjectView(R.id.examinee_spinner)
    Spinner examineeSpinner;
    @InjectView(R.id.test)
    View beginTestButton;
    @InjectView(R.id.tutorial_and_test)
    View beginTutorialAndTest;
    @InjectView(R.id.context_help)
    TextView contextHelp;

    private InstitutionAdapter institutionAdapter;
    private DepartmentAdapter departmentAdapter;
    private ExamineeAdapter examineeAdapter;

    private Researcher researcher;

    private CurrentTaskSuiteService.TestRunData runData;
    private int testRequestCode;
    private boolean disableInstitutions;
    private boolean disableDepartments;

    @OnClick(R.id.test)
    public void onTestClicked() {
        try {
            runTest(false);
        } catch (Exception e) {
            throw ExecutionException.wrap(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_report_bug) {
            SupportDialog.show(this, getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.tutorial_and_test)
    public void onTutorialAndTestClicked() {
        try {
            runTest(true);
        } catch (Exception e) {
            throw ExecutionException.wrap(e);
        }
    }

    private void runTest(boolean tutorial) throws Exception {
        try {
            testRequestCode = getServiceProvider().currentTaskSuite().prepare()
                    .setTestMode(tutorial ? CurrentTaskSuiteService.TestMode.TUTORIAL : CurrentTaskSuiteService.TestMode.NORMAL).
                            setTaskSuite(getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite()).
                            setInstitution((Institution) institutionSpinner.getSelectedItem()).
                            setDepartment((Department) departmentSpinner.getSelectedItem()).
                            setExaminee((Examinee) examineeSpinner.getSelectedItem()).runTest(this);
        } catch (AssertionError assertionError) {
            getServiceProvider().examinee().setExamineeToEdit((Examinee) examineeSpinner.getSelectedItem(), assertionError);
            Intent intent = new Intent();
            intent.setClass(this, ExamineeManagerActivity.class);
            startActivity(intent);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.ENABLE_FAKE_RANDOM) {
            FakeRandom.methodCallCounter = new HashMap<>();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == testRequestCode) {
            //TODO: more appropriate reaction for currentTaskSuite finish
            Toast.makeText(this, R.string.research_has_been_finished, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_config);
        ButterKnife.inject(this);
        setTitleBasedOnTestSuite();
        researcher = getServiceProvider().login().currentLoggedInUser;
        disableInstitutions = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableInstitutions;
        disableDepartments = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableDepartments;


        if (disableInstitutions) {
            institutionContainer.setVisibility(View.GONE);
        } else {
            institutionContainer.setVisibility(View.VISIBLE);
            institutionAdapter = new InstitutionAdapter(this, android.R.layout.simple_list_item_1);

            RxExecutor.runWithUiCallback(getServiceProvider().researcher().getResearchersInstitutions(researcher)).subscribe(institutionAdapter::populate);
            institutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (disableDepartments) {
                        getServiceProvider().institution().getInstitutionsExaminees(institutionAdapter.getItem(position)).subscribe((Examinees) -> {
                            RxExecutor.runWithUiCallback(getServiceProvider().researcher()
                                    .filterExaminee(researcher, Examinees)).subscribe(examineeAdapter::populate);
                        });
                    } else {
                        RxExecutor.runWithUiCallback(getServiceProvider().department().
                                getDepartmentsListBasedOnInstitution(institutionAdapter.getItem(position))).subscribe(departmentAdapter::populate);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        if (disableDepartments) {
            departmentContainer.setVisibility(View.GONE);
        } else {
            departmentContainer.setVisibility(View.VISIBLE);
            departmentAdapter = new DepartmentAdapter(this, android.R.layout.simple_list_item_1);
            RxExecutor.runWithUiCallback(getServiceProvider().department().getDepartmentsListBasedOnResearcher(researcher)).subscribe(departmentAdapter::populate);
            departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    RxExecutor.runWithUiCallback(getServiceProvider().researcher()
                            .filterExaminee(researcher, (departmentAdapter
                                    .getItem(position)).getExamineeList())).subscribe(examineeAdapter::populate);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        examineeAdapter = new ExamineeAdapter(this, android.R.layout.simple_list_item_1);

        if (disableDepartments && disableInstitutions) {
            RxExecutor.runWithUiCallback(getServiceProvider().researcher().getResearchersExaminee(researcher)).subscribe(examineeAdapter::populate);
        }

        institutionSpinner.setAdapter(institutionAdapter);
        departmentSpinner.setAdapter(departmentAdapter);
        examineeSpinner.setAdapter(examineeAdapter);

        examineeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean enable = (examineeSpinner.getSelectedItem() != null);
                beginTestButton.setEnabled(enable);
                beginTutorialAndTest.setEnabled(enable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        try {
            contextHelp.setText(getServiceProvider().currentTaskSuite().getLocalization().get("help_main"));
        } catch (Exceptions.LocalizationException e) {
            LogUtils.v(TAG, "Localization not found", e);
        }
        getServiceProvider().currentTaskSuite().setAllowScreenRotation(true);


        boolean tutorialExists = false;
        for (LoremIpsumApp.AreaWrapper areaWrapper : LoremIpsumApp.getTaskAreas()) {
            for (TaskInfo m_task : areaWrapper.m_tasks) {
                if (m_task.m_name.startsWith("P") || m_task.m_name.startsWith("p")) {
                    tutorialExists = true;
                    break;
                }
            }
        }
        for (LoremIpsumApp.AreaWrapper areaWrapper : LoremIpsumApp.m_manual) {
            for (TaskInfo m_task : areaWrapper.m_tasks) {
                if (m_task.m_name.startsWith("P") || m_task.m_name.startsWith("p")) {
                    tutorialExists = true;
                    break;
                }
            }
        }
        if (tutorialExists) {
            beginTutorialAndTest.setVisibility(View.VISIBLE);
        } else {
            beginTutorialAndTest.setVisibility(View.GONE);
        }
    }

    private void setTitleBasedOnTestSuite() {
        TaskSuite taskSuite = getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite();
        String text = taskSuite.getName() + " (" + getResources().getString(R.string.version) + " " + taskSuite.getVersion() + ")";
        if (taskSuite.getPilot()) {
            text += " - " + getResources().getString(R.string.pilot_task_suite);
        }
        setTitle(text);
    }


    public String getResultsDir() {
        CurrentTaskSuiteService currentTaskSuite = getServiceProvider().currentTaskSuite();
        LoginService loginService = getServiceProvider().login();
        try {
            return LoremIpsumApp.m_resultDir + File.separator + currentTaskSuite.getCurrentTestRunData().getTaskSuite().getName()
                    + File.separator + loginService.currentLoggedInUser.getTextId()
                    + File.separator + TimeUtils.dateToString(new Date(), TimeUtils.defaultPatern);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Date error");
    }

    private class InstitutionAdapter extends LoremIpsumSimpleAdapter<Institution> {
        public InstitutionAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        protected long extractId(Institution institution) {
            return institution.getId();
        }

        @Override
        protected String populateItem(Institution item) {
            return item.getTextId() + " - " + item.getName();
        }
    }

    private class DepartmentAdapter extends LoremIpsumSimpleAdapter<Department> {
        public DepartmentAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        protected long extractId(Department department) {
            return department.getId();
        }

        @Override
        protected String populateItem(Department item) {
            return item.getName();
        }
    }

    private class ExamineeAdapter extends LoremIpsumSimpleAdapter<Examinee> {
        public ExamineeAdapter(Context context, int resource) {
            super(context, resource);

        }

        @Override
        protected long extractId(Examinee examinee) {
            return examinee.getId();
        }

        @Override
        protected String populateItem(Examinee item) {
            return item.getTextId() + " - " + item.getFirstName() + " " + item.getLastName();
        }
    }

}
