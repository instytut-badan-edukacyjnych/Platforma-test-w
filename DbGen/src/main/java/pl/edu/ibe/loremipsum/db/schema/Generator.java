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

package pl.edu.ibe.loremipsum.db.schema;

import java.io.File;
import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Index;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class Generator {

    public static void main(String[] args) throws Exception {
        Schema schema = makeSchema();

        Entity examinee = addExaminee(schema);
        Entity department = addDepartment(schema);
        //department has many examinees
        Property departmentFk = examinee.addLongProperty("department_fk").notNull().getProperty();
        examinee.addToOne(department, departmentFk);
        department.addToMany(examinee, departmentFk);

        Entity institution = addInstitution(schema);
        //institution has many examinee
        Property institutionFk = examinee.addLongProperty("instituion_fk").notNull().getProperty();
        examinee.addToOne(institution, institutionFk);
        institution.addToMany(examinee, institutionFk);

        Entity researcher = addResearcher(schema);

        Entity researcherJoinExaminee = addResearcherJoinExaminee(schema, researcher, examinee);
        Entity researcherJoinInstitution = addResearcherJoinInstitution(schema, researcher, institution);

        Entity researchersSuite = addResearchersSuite(schema);
        //researcher's suite is assigned to one researcher
        Property researcherFk = researchersSuite.addLongProperty("researcher_fk").columnType("INTEGER REFERENCES 'RESEARCHER'('ID') ON DELETE RESTRICT").notNull().getProperty();
        researchersSuite.addToOne(researcher, researcherFk);
        researcher.addToMany(researchersSuite, researcherFk);

        Entity credential = addCredentials(schema);
        //researchers suite has one credential
        researchersSuite.addToOne(credential, researchersSuite.addLongProperty("credential_fk").columnType("INTEGER REFERENCES 'CREDENTIAL'('ID') ON DELETE RESTRICT").getProperty());

        Entity taskSuite = addTaskSuite(schema);
        //and one actual suite
        Property taskSuiteFk = researchersSuite.addLongProperty("task_suite_fk").columnType("INTEGER REFERENCES 'TASK_SUITE'('ID') ON DELETE RESTRICT").notNull().getProperty();
        researchersSuite.addToOne(taskSuite, taskSuiteFk);
        taskSuite.addToMany(researchersSuite, taskSuiteFk);

        Entity result = addDbResult(schema, researchersSuite);

        Index researchersSuiteFksUnique = new Index();
        researchersSuiteFksUnique.addProperty(researcherFk);
        researchersSuiteFksUnique.addProperty(taskSuiteFk);
        researchersSuiteFksUnique.makeUnique();
        researchersSuite.addIndex(researchersSuiteFksUnique);

        Entity resultsQueue = addResultsQueue(schema);


        Entity examineeTprResults = addExamineeTprResults(schema);
        Property examineeFk = examineeTprResults.addLongProperty("examinee_fk").notNull().getProperty();
        examineeTprResults.addToOne(examinee, examineeFk);
        examinee.addToMany(examineeTprResults, examineeFk);

        Property researcherSuiteFk = examineeTprResults.addLongProperty("researchers_suite_fk").notNull().getProperty();
        examineeTprResults.addToOne(researchersSuite, researcherSuiteFk);
        researchersSuite.addToMany(examineeTprResults, researcherSuiteFk);

        Property researcherTPRFk = examineeTprResults.addLongProperty("researcher_fk").notNull().getProperty();
        examineeTprResults.addToOne(researcher, researcherTPRFk);
        researcher.addToMany(examineeTprResults, researcherTPRFk);


        genereteDbSchema(schema, "../LoremIpsum/src/src_db_gen/java");
    }

    private static Entity addExamineeTprResults(Schema schema) {
        Entity entity = schema.addEntity("ExamineeTprResults");
        Property resultId = entity.addLongProperty("id").primaryKey().autoincrement().getProperty();
        entity.addDoubleProperty("result");
        entity.addStringProperty("test_summary");
        entity.addStringProperty("test_area");
        entity.addDateProperty("date");
        entity.addLongProperty("test_id");
        return entity;
    }

    private static Schema makeSchema() {
        return new Schema(7, "pl.edu.ibe.loremipsum.db.schema");
    }

    private static Entity addDbResult(Schema schema, Entity researchersSuite) {
        Entity result = schema.addEntity("Result");
        Property resultId = result.addLongProperty("id").primaryKey().autoincrement().getProperty();
        Property resultSuiteFk = result.addLongProperty("suite_fk").getProperty();
        result.addStringProperty("institution_text_id");
        result.addStringProperty("department_name");
        result.addStringProperty("examinee_text_id");
        result.addDateProperty("date");


        Entity area = schema.addEntity("ResultArea");
//        Property areaId = area.addLongProperty("id").primaryKey().autoincrement().getProperty();
        Property resultFk = area.addLongProperty("result_fk").getProperty();
        area.addStringProperty("area");
        area.addDoubleProperty("value");
        area.addDoubleProperty("standard_error");
        area.addIntProperty("status");

        researchersSuite.addToMany(result, resultSuiteFk);
        result.addToOne(researchersSuite, resultSuiteFk);
        result.addToMany(area, resultFk);
        area.addToOne(result, resultFk);
        return result;
    }

    private static void genereteDbSchema(Schema schema, String filePath) throws Exception, IOException {
        Util.deleteDirectory(new File(filePath), false);
        new File(filePath).mkdirs();
        new DaoGenerator().generateAll(schema, filePath);
    }

    private static Entity addResearcherJoinInstitution(Schema schema, Entity researcher, Entity institution) {
        Entity researcherJoinInstitution = schema.addEntity("ResearcherJoinInstitution");
        Property id = researcherJoinInstitution.addLongProperty("id").primaryKey().autoincrement().getProperty();
        Property researcher_fk = researcherJoinInstitution.addLongProperty("researcher_fk").notNull().getProperty();
        Property institution_fk = researcherJoinInstitution.addLongProperty("institution_fk").notNull().getProperty();

        //Researcher has many institution and Institution has many researchers
        researcher.addToMany(researcherJoinInstitution, researcher_fk);
        institution.addToMany(researcherJoinInstitution, institution_fk);
        researcherJoinInstitution.addToOne(researcher, researcher_fk);
        researcherJoinInstitution.addToOne(institution, institution_fk);

        return researcherJoinInstitution;
    }

    private static Entity addResearcherJoinExaminee(Schema schema, Entity researcher, Entity examinee) {
        Entity researcherJoinExaminee = schema.addEntity("ResearcherJoinExaminee");
        researcherJoinExaminee.addLongProperty("id").primaryKey().autoincrement();
        Property researcher_fk = researcherJoinExaminee.addLongProperty("researcher_fk").notNull().getProperty();
        Property examinee_fk = researcherJoinExaminee.addLongProperty("examinee_fk").notNull().getProperty();

        //Researcher has many examinee and Examinee has many researchers
        researcher.addToMany(researcherJoinExaminee, researcher_fk);
        examinee.addToMany(researcherJoinExaminee, examinee_fk);
        researcherJoinExaminee.addToOne(researcher, researcher_fk);
        researcherJoinExaminee.addToOne(examinee, examinee_fk);
        return researcherJoinExaminee;
    }

    private static Entity addResultsQueue(Schema schema) {
        Entity resultsQueue = schema.addEntity("ResultsQueue");
        resultsQueue.addLongProperty("id").primaryKey().autoincrement();
        resultsQueue.addStringProperty("fileName");
        resultsQueue.addStringProperty("submitUrl");
        resultsQueue.addBooleanProperty("markToDelete").notNull();
        resultsQueue.addIntProperty("attempts").notNull();
        resultsQueue.addDateProperty("lastAttemptDate");
        return resultsQueue;
    }

    private static Entity addTaskSuite(Schema schema) {
        Entity taskSuite = schema.addEntity("TaskSuite");
        taskSuite.addLongProperty("id").primaryKey().autoincrement();
        Property name = taskSuite.addStringProperty("name").getProperty();
        Property version = taskSuite.addStringProperty("version").getProperty();
        taskSuite.addBooleanProperty("pilot");
        taskSuite.addStringProperty("latestVersionSeen");
        taskSuite.addBooleanProperty("downloaded").notNull();
        taskSuite.addBooleanProperty("demo").notNull();

        Index indexUnique = new Index();
        indexUnique.addProperty(name);
        indexUnique.addProperty(version);
        indexUnique.makeUnique();
        taskSuite.addIndex(indexUnique);

        return taskSuite;
    }

    private static Entity addCredentials(Schema schema) {
        Entity credentials = schema.addEntity("Credential");
        credentials.addLongProperty("id").primaryKey().autoincrement();
        credentials.addStringProperty("user");
        credentials.addStringProperty("password");
        credentials.addStringProperty("manifestUrl");
        return credentials;
    }

    private static Entity addResearchersSuite(Schema schema) {
        Entity researchersSuite = schema.addEntity("ResearchersSuite");
        researchersSuite.addLongProperty("id").primaryKey().autoincrement();
        researchersSuite.addBooleanProperty("sawCollectorOpt").notNull();
        researchersSuite.addBooleanProperty("agreedForCollector").notNull();

        return researchersSuite;
    }

    private static Entity addDepartment(Schema schema) {
        Entity department = schema.addEntity("Department");
        department.addLongProperty("id").primaryKey().autoincrement();
        department.addStringProperty("name").unique();
        return department;
    }

    private static Entity addExaminee(Schema schema) {
        Entity examined = schema.addEntity("Examinee");
        examined.addLongProperty("id").primaryKey().autoincrement();
        examined.addStringProperty("textId").notNull().unique();
        examined.addStringProperty("firstName");
        examined.addStringProperty("lastName");
        examined.addStringProperty("gender");
        examined.addDateProperty("birthday");
        examined.addStringProperty("additionalData");

        return examined;
    }

    private static Entity addInstitution(Schema schema) {
        Entity institution = schema.addEntity("Institution");
        institution.addLongProperty("id").primaryKey().autoincrement();
        institution.addStringProperty("textId").notNull().unique();
        institution.addStringProperty("name");
        institution.addStringProperty("street");
        institution.addStringProperty("postalCode");
        institution.addStringProperty("city");
        institution.addStringProperty("province");
        institution.addStringProperty("legacyAddress");
        return institution;
    }

    private static Entity addResearcher(Schema schema) {
        Entity researcher = schema.addEntity("Researcher");
        researcher.addLongProperty("id").primaryKey().autoincrement();
        researcher.addStringProperty("textId").notNull().unique();
        researcher.addStringProperty("firstName");
        researcher.addStringProperty("surName");
        researcher.addStringProperty("password");

        //Determines if user is system default user, not visible for real user.
        researcher.addBooleanProperty("hidden");
        return researcher;
    }

    private static class Util {

        private static void deleteDirectory(File directory, boolean deleteRoot) {
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (null != files) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteDirectory(file, true);
                        } else {
                            file.delete();
                        }
                    }
                }
            }
            if (deleteRoot) {
                directory.delete();
            }
        }
    }
}
