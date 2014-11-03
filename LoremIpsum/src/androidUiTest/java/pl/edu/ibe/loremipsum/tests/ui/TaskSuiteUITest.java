package pl.edu.ibe.loremipsum.tests.ui;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import android.util.Log;

import java.io.IOException;
import java.lang.ProcessBuilder;
import java.lang.RuntimeException;

public class TaskSuiteUITest extends UiAutomatorTestCase {
    private static final String serverAddress = "46.29.20.95:55062/";
    private static final String testmodePath = "testmode/";
    private static int shortWait = 1000;
    private static int longWait = 1000*60*5;

    private static final String packageName = "pl.edu.ibe.loremipsum.tablet";
    private static final String initialActivity = "pl.edu.ibe.loremipsum.tablet.LaunchActivity";
    private static final String nsId = packageName+":id/";

    private void run(String... cmd) {
        String scmd = new String();
        for (String pcmd : cmd)
            scmd += pcmd + " ";
        Log.v("UI testing", "Running command: " + scmd);

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            p.waitFor();
        } catch(Throwable t) {
            throw new RuntimeException("Problem with command: \""+scmd+"\"", t);
        }
    }

    private void uninstallApplication() {
        run("pm", "uninstall", packageName);
        if(getUiDevice().getCurrentPackageName() != null &&
                getUiDevice().getCurrentPackageName().contains(packageName))
            throw new RuntimeException("Could not stop application");
    }

    private void installApplication() {
        run("pm", "install", "/data/local/tmp/LoremIpsum-debug-unaligned.apk");
    }

    private void startApplication() {
        run("am", "start", packageName + "/" + initialActivity);
        if(!getUiDevice().getCurrentPackageName().contains(packageName))
            throw new RuntimeException("Could not start application");
    }

    /* acquire UI objects */
    private UiObject fromId(String id) {
        return new UiObject(new UiSelector().resourceId(nsId+id));
    }

    private UiObject activityLogin_usernameEditText = fromId("login");
    private UiObject activityLogin_passwordEditText = fromId("password");
    private UiObject activityLogin_loginButton = fromId("login");

    private UiObject activityLogin_newAccount_createAccountButton = new UiObject(new UiSelector().resourceId(nsId+"create_account_button"));;
    private UiObject activityLogin_newAccount_readableIdEditText = new UiObject(new UiSelector().resourceId(nsId+"readable_id"));
    private UiObject activityLogin_newAccount_firstnameEditText = new UiObject(new UiSelector().resourceId(nsId+"firstname"));
    private UiObject activityLogin_newAccount_surnameEditText = new UiObject(new UiSelector().resourceId(nsId+"surname"));
    private UiObject activityLogin_newAccount_passwordEditText = new UiObject(new UiSelector().resourceId(nsId+"password"));
    private UiObject activityLogin_newAccount_repeatedpasswordEditText = new UiObject(new UiSelector().resourceId(nsId+"repeated_password"));
    private UiObject activityLogin_newAccount_newaccount_ok = new UiObject(new UiSelector().textMatches("Ok"));

    private UiObject suiteSelectActivity_installNew = new UiObject(new UiSelector().resourceId(nsId+"action_add_new_bank"));
     //new UiSelector().textMatches("Zainstaluj nowy bank zadań"));

    private UiObject suiteSelectActivity_installNew_manifestUrl = new UiObject(new UiSelector().resourceId(nsId+"manifest_url"));
    private UiObject suiteSelectActivity_installNew_username = new UiObject(new UiSelector().resourceId(nsId+"username"));
    private UiObject suiteSelectActivity_installNew_password = new UiObject(new UiSelector().resourceId(nsId+"password"));
    private UiObject suiteSelectActivity_installNew_install = new UiObject(new UiSelector().resourceId(nsId+"install"));
    private UiObject suiteSelectActivity_installNew_cancel = new UiObject(new UiSelector().resourceId(nsId+"cancel"));
    private UiObject suiteSelectActivity_installNew_wait = new UiObject(new UiSelector().textContains("Instalacja"));
    private UiObject suiteSelectActivity_test_suite_1_0 = new UiObject(new UiSelector().textMatches("test-suite \\(.* 1.0\\)"));

    private UiObject main_test = new UiObject(new UiSelector().resourceId(nsId+"test"));
    private UiObject main_settings = new UiObject(new UiSelector().resourceId(nsId+"settings"));
    private UiObject main_examinee = new UiObject(new UiSelector().resourceId(nsId+"examinee"));
    private UiObject main_reports = new UiObject(new UiSelector().resourceId(nsId+"reports"));


    private UiObject airplaneOk = new UiObject(new UiSelector().textMatches("O[Kk]"));
    private UiObject airplaneMode = new UiObject(new UiSelector().textMatches("Airplane mode"));

    private UiObject config_test = fromId("test");
    private UiObject config_tutorialAndTest = fromId("tutorial_and_test");

    private UiObject examineeMgr_institutionId = fromId("institution_id");
    private UiObject examineeMgr_institutionName = fromId("institution_name");
    private UiObject examineeMgr_institutionStreet = fromId("institution_street");
    private UiObject examineeMgr_institutionPostalCode = fromId("institution_postal_code");
    private UiObject examineeMgr_institutionCity = fromId("institution_city");
    private UiObject examineeMgr_institutionProvince = fromId("institution_province");

    private UiObject examineeMgr_examineeId = fromId("examinee_text_id");
    private UiObject examineeMgr_examineeFirstName = fromId("examinee_first_name");
    private UiObject examineeMgr_examineeLastName = fromId("examinee_last_name");
    private UiObject examineeMgr_examineeBirthday = fromId("examinee_birthday");
    private UiObject examineeMgr_examineeGender = fromId("examinee_gender");
    private UiObject examineeMgr_examineeGender_male;
    private UiObject examineeMgr_examineeGender_female;

    private UiObject examineeMgr_departmentList = fromId("department_list");
    private UiObject examineeMgr_addNewDepartment = new UiObject(new UiSelector().textMatches("Dodaj nowy"));
    private UiObject examineeMgr_addDepartment = fromId("add_department");
    private UiObject examineeMgr_departmentName = fromId("department_name");

    private UiObject examineeMgr_save = fromId("save_and_exit");

    @Override
    public void setUp() throws Exception {
        super.setUp();

        examineeMgr_examineeGender_male = examineeMgr_examineeGender.getChild(new UiSelector().resourceId(nsId+"examinee_male"));
        examineeMgr_examineeGender_female = examineeMgr_examineeGender.getChild(new UiSelector().resourceId(nsId+"examinee_female"));

        uninstallApplication();
        installApplication();
        startApplication();
    }

    @Override
    public void tearDown() throws Exception {
        uninstallApplication();

        super.tearDown();
    }

    public void testInstallNewTaskSuite() throws UiObjectNotFoundException {
        activityLogin_newAccount_createAccountButton.waitForExists(longWait);
//        getUiDevice().pressBack(); //close onscreen keyboard
        activityLogin_newAccount_createAccountButton.clickAndWaitForNewWindow(shortWait);

        activityLogin_newAccount_readableIdEditText.setText("alice1");
        activityLogin_newAccount_firstnameEditText.setText("Alice");
        activityLogin_newAccount_surnameEditText.setText("Anonymous");
        activityLogin_newAccount_passwordEditText.setText("password");
        activityLogin_newAccount_repeatedpasswordEditText.setText("password");
        activityLogin_newAccount_newaccount_ok.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew.waitForExists(shortWait);
        suiteSelectActivity_installNew.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew_manifestUrl.setText(serverAddress);
        suiteSelectActivity_installNew_username.setText("test-Alice");
        suiteSelectActivity_installNew_password.setText("test-password");
        suiteSelectActivity_installNew_install.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew_wait.waitForExists(shortWait);
        suiteSelectActivity_installNew_wait.waitUntilGone(longWait);

        suiteSelectActivity_test_suite_1_0.clickAndWaitForNewWindow(shortWait);

        main_examinee.clickAndWaitForNewWindow(shortWait);

        examineeMgr_institutionId.waitForExists(shortWait);
        examineeMgr_institutionId.setText("school no 1");
        examineeMgr_institutionName.setText("Primary School no 1");
        examineeMgr_institutionStreet.setText("Education St.");
        examineeMgr_institutionPostalCode.setText("66679");
        examineeMgr_institutionCity.setText("Honolulu");
        examineeMgr_institutionProvince.setText("Hawaii");

        examineeMgr_examineeId.setText("charlie");
        examineeMgr_examineeFirstName.setText("Charlie");
        examineeMgr_examineeLastName.setText("Camouflaged");
        examineeMgr_examineeGender_male.click();

        examineeMgr_departmentName.setText("Class 1A");

        examineeMgr_save.clickAndWaitForNewWindow(shortWait);

        main_test.clickAndWaitForNewWindow(shortWait);
        sleep(shortWait);
        if(airplaneOk.exists()) {
            airplaneOk.clickAndWaitForNewWindow(shortWait);
            airplaneMode.click();
            getUiDevice().pressBack();
        }

        config_test.clickAndWaitForNewWindow(shortWait);
    }

    public void testCancelInstallation() throws UiObjectNotFoundException {
        activityLogin_newAccount_createAccountButton.waitForExists(longWait);
//        getUiDevice().pressBack(); //close onscreen keyboard
        activityLogin_newAccount_createAccountButton.clickAndWaitForNewWindow(shortWait);

        activityLogin_newAccount_readableIdEditText.setText("alice1");
        activityLogin_newAccount_firstnameEditText.setText("Alice");
        activityLogin_newAccount_surnameEditText.setText("Anonymous");
        activityLogin_newAccount_passwordEditText.setText("password");
        activityLogin_newAccount_repeatedpasswordEditText.setText("password");
        activityLogin_newAccount_newaccount_ok.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew.waitForExists(shortWait);
        suiteSelectActivity_installNew.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew_manifestUrl.setText(serverAddress);
        suiteSelectActivity_installNew_username.setText("test-Alice");
        suiteSelectActivity_installNew_password.setText("test-password");
        suiteSelectActivity_installNew_install.clickAndWaitForNewWindow(shortWait);

        suiteSelectActivity_installNew_wait.waitForExists(shortWait);
        sleep(2000);
        getUiDevice().pressBack(); //quit lorem ipsum
        getUiDevice().pressBack(); //quit lorem ipsum

        startApplication();
        activityLogin_usernameEditText.setText("alice");
        activityLogin_passwordEditText.setText("password");
        activityLogin_loginButton.clickAndWaitForNewWindow(shortWait);
    }
}
