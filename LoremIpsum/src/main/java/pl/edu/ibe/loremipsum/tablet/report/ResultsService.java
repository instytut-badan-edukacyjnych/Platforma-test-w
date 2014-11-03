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

package pl.edu.ibe.loremipsum.tablet.report;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.db.schema.DaoSession;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.ExamineeTprResults;
import pl.edu.ibe.loremipsum.db.schema.ExamineeTprResultsDao;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearchersSuite;
import pl.edu.ibe.loremipsum.db.schema.Result;
import pl.edu.ibe.loremipsum.db.schema.ResultArea;
import pl.edu.ibe.loremipsum.db.schema.ResultAreaDao;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.task.mark.TestResult;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import rx.Observable;

/**
 * Created by adam on 24.03.14.
 */
public class ResultsService extends BaseService {

    private Gson gson;

    public ResultsService(ServiceProvider services, Gson gson) {
        super(services);
        this.gson = gson;
    }

    /**
     * @return @link{Results} represented as JSON
     */
    public Observable<String> getResults() {
        return getServiceProvider().researcher().getCurrentResearcherSuite()
                .flatMap(suite -> {
                    if (suite == null) {
                        return Observable.error(new NullPointerException());
                    } else {
                        return Observable.from(suite);
                    }
                })
                .map(suite -> {
                    List<Result> results = suite.getResultList();
                    JSONArray array = new JSONArray();
                    try {
                        for (Result result : results) {
                            JSONObject obj = new JSONObject();
                            obj.put("id", result.getId());
                            obj.put("inst", result.getInstitution_text_id());
                            obj.put("dept", result.getDepartment_name());
                            obj.put("examinee", result.getExaminee_text_id());
                            obj.put("date", TimeUtils.dateToString(result.getDate(), TimeUtils.rfcFormat));
                            JSONObject areas = new JSONObject();
                            for (ResultArea area : result.getResultAreaList()) {
                                JSONObject o = new JSONObject();
                                o.put("score", area.getValue());
                                o.put("se", area.getStandard_error());
                                o.put("status", area.getStatus());
                                areas.put(area.getArea(), o);
                            }
                            obj.put("areas", areas);
                            array.put(obj);
                        }
                    } catch (JSONException | ParseException e) {
                        throw ExecutionException.wrap(e);
                    }
                    return array.toString();
                });
    }

    public Observable<Boolean> store(TestResult testResult) {
        return getServiceProvider().researcher().getCurrentResearcherSuite()
                .flatMap(suite -> {
                    if (suite == null) {
                        return Observable.error(new NullPointerException());
                    } else {
                        return Observable.from(suite);
                    }
                })
                .map(suite -> {
                    CurrentTaskSuiteService.TestRunData testRunData = getServiceProvider().currentTaskSuite().getCurrentTestRunData();

                    Result result = new Result();
                    result.setResearchersSuite(suite);
                    result.setDate(new Date());
                    if (testRunData.getDepartment() != null) {
                        result.setDepartment_name(testRunData.getDepartment().getName());
                    } else {
                        result.setDepartment_name("");
                    }
                    if (testRunData.getInstitution() != null) {
                        result.setInstitution_text_id(testRunData.getInstitution().getTextId());
                    } else {
                        result.setInstitution_text_id("");
                    }
                    result.setExaminee_text_id(testRunData.getExaminee().getTextId());
                    dbAccess().getDaoSession().getResultDao().insert(result);

                    ResultAreaDao areaDao = dbAccess().getDaoSession().getResultAreaDao();
                    for (TestResult.ResultItem item : testResult.m_result.values()) {
                        ResultArea area = new ResultArea();
                        area.setResult(result);
                        area.setArea(item.m_area);
                        area.setValue(item.m_theta);
                        area.setStandard_error(item.m_se);
                        area.setStatus(item.m_status);
                        areaDao.insert(area);
                    }
                    result.resetResultAreaList();
                    suite.resetResultList();
                    return true;
                })
                .doOnError(t -> Log.e(ResultsService.class.getName(), "Error during result save", t));
    }

    public Observable<Long> storeTprResults(MarkData markData) {
        return RxExecutor.run(() -> {
            ExamineeTprResults examineeTprResults = new ExamineeTprResults();
            examineeTprResults.setDate(new Date());
            if (getServiceProvider().currentTaskSuite().getCurrentTestRunData().getExaminee().getId() == null) {
                getServiceProvider().currentTaskSuite().getCurrentTestRunData().getExaminee().refresh();
            }
            examineeTprResults.setExaminee(getServiceProvider().currentTaskSuite().getCurrentTestRunData().getExaminee());
            Researcher researcher = getServiceProvider().login().currentLoggedInUser;
            pl.edu.ibe.loremipsum.task.management.TaskSuite taskSuite = getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite();

            for (ResearchersSuite researchersSuite : researcher.getResearchersSuiteList()) {
                if (researchersSuite.getTaskSuite().getId() == taskSuite.getDbEntry().getId()) {
                    examineeTprResults.setResearchersSuite(researchersSuite);
                    break;
                }
            }
            examineeTprResults.setResult(markData.m_mark);
            examineeTprResults.setTest_summary(markData.m_answer);
            examineeTprResults.setTest_area(markData.area);
            examineeTprResults.setResearcher(getServiceProvider().login().currentLoggedInUser);

            examineeTprResults.setTest_id(getServiceProvider().currentTaskSuite().getRandomTestIdentifier());

            return dbAccess().getDaoSession().getExamineeTprResultsDao().insert(examineeTprResults);
        });
    }


    public Observable<String> getTprResults() {
        return RxExecutor.run(() -> {

            DaoSession session = dbAccess().getDaoSession();

            Researcher researcher = getServiceProvider().login().currentLoggedInUser;

            List<ExamineeTprResults> results = session.getExamineeTprResultsDao().queryBuilder().where(ExamineeTprResultsDao.Properties.Researcher_fk.eq(researcher.getId())).list();
            TprResultInfo tprResultInfo;
            List<TprResultInfo> tprResults = new ArrayList<>();

            Examinee examinee;
            Institution institution;
            for (ExamineeTprResults result : results) {
                examinee = session.getExamineeDao().load(result.getExaminee_fk());
                tprResultInfo = new TprResultInfo();


                tprResultInfo.id = result.getId();
                tprResultInfo.result = result.getResult() == null || result.getResult() == -7 ? null : result.getResult();
                tprResultInfo.testSummary = result.getTest_summary();
                tprResultInfo.testArea = result.getTest_area() == null ? "" : result.getTest_area();

                tprResultInfo.date = result.getDate();
                tprResultInfo.testId = result.getTest_id();

                tprResultInfo.examineeTextId = examinee.getTextId();
                tprResultInfo.firstName = examinee.getFirstName();
                tprResultInfo.lastName = examinee.getLastName();
                tprResultInfo.gender = examinee.getGender();
                tprResultInfo.birthday = examinee.getBirthday();
                tprResultInfo.additionalData = examinee.getAdditionalData();

                tprResultInfo.researcherTextId = researcher.getTextId();
                tprResultInfo.institutionId = examinee.getInstitution().getTextId();
                tprResultInfo.departmentName = examinee.getDepartment().getName();
                tprResults.add(tprResultInfo);
            }


            return gson.toJson(tprResults);
        });
    }


    public static class TprResultInfo {
        public long id;
        public Double result;
        public String testSummary;
        public String testArea;
        public Date date;
        public long testId;


        public String examineeTextId;
        public String firstName;
        public String lastName;
        public String gender;
        public Date birthday;
        public String additionalData;
        public String departmentName;
        public String institutionId;
        public String researcherTextId;

    }
}
