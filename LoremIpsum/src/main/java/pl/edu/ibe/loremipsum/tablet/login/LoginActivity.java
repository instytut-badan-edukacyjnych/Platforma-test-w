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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.demo.DemoSelectDialog;
import pl.edu.ibe.loremipsum.tablet.researcher.ResearcherDialog;
import pl.edu.ibe.loremipsum.tablet.support.SupportDialog;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.management.LoadingPerformer;
import pl.edu.ibe.loremipsum.task.management.SuiteSelectActivity;
import pl.edu.ibe.loremipsum.task.management.TaskSuite;
import pl.edu.ibe.loremipsum.tools.ExecutionException;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;
import rx.schedulers.Schedulers;

/**
 * First Activity of application allows user to login
 */
public class LoginActivity extends BaseServiceActivity {
    private static final String TAG = LoginActivity.class.toString();

    @InjectView(R.id.demo_button)
    Button demoButton;
    @InjectView(R.id.login)
    AutoCompleteTextView login;
    @InjectView(R.id.password)
    EditText passwordEditText;
    @InjectView(R.id.login_button)
    Button loginButton;
    @InjectView(R.id.create_account_button)
    Button createAccount;
    private int testRequestCode;


    @OnClick(R.id.demo_button)
    void demoButtonClicked() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DemoSelectDialog dialog = new DemoSelectDialog() {


            @Override
            public void dialogDismissed(TaskSuite startDemo) {
                if (startDemo != null) {
                    try {
                        runTest(startDemo);
                    } catch (IOException e) {
                        Toast.makeText(LoginActivity.this, R.string.demo_start_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        dialog.show(fragmentManager, "DemoDialog");
    }

    /**
     * Runs demo test
     *
     * @param demoSuite
     * @throws IOException
     */
    private void runTest(TaskSuite demoSuite) throws IOException {
        RxExecutor.runWithUiCallback(LoadingPerformer.performLoad(this, getServiceProvider(), demoSuite.getName(), demoSuite.getVersion())
                        .flatMap(ignore -> getServiceProvider().taskSuites().getTaskSuite(demoSuite.getName(), demoSuite.getVersion()))
                        .flatMap(taskSuite -> {
                                    try {
                                        return getServiceProvider().currentTaskSuite().loadTestConfig(taskSuite.getRootVirtualFile())
                                                .map(result -> {
                                                    getServiceProvider().currentTaskSuite().getCurrentTestRunData().setTaskSuite(taskSuite);
                                                    return result;
                                                });
                                    } catch (IOException e) {
                                        throw ExecutionException.wrap(e);
                                    }
                                }
                        ).subscribeOn(Schedulers.io())
        ).subscribe(ignore -> {
                    try {
                        CurrentTaskSuiteService.TestRunData testRunData = getServiceProvider().currentTaskSuite().prepareDemo(demoSuite).toBlockingObservable().single();
                        testRequestCode = testRunData.runTest(LoginActivity.this);
                    } catch (Exception e) {
                        throw ExecutionException.wrap(e);
                    }
                },
                throwable -> {
                    Toast.makeText(this, getString(R.string.error_loading_test) + " " + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    LogUtils.e(TAG, "Exception during test loading", throwable);
                }
        );
    }

    @OnClick(R.id.create_account_button)
    void createAccountClicked() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ResearcherDialog dialog = new ResearcherDialog() {

            @Override
            public void researchersUpdated(Researcher researcher) {
                performLogin(researcher.getTextId(), researcher.getPassword());
                RxExecutor.runWithUiCallback(() -> Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_LONG).show());
            }
        };

        Bundle bundle = new Bundle();
        bundle.putLong(ResearcherDialog.RESEARCHER_ID, -1l);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, "dialog");
    }

    @OnEditorAction(R.id.password)
    boolean editorAction(TextView v, int actionId, KeyEvent event) {
        performLogin(login.getText().toString(), passwordEditText.getText().toString());
        return true;
    }


    @OnClick(R.id.login_button)
    void loginButtonClicked() {
        performLogin(login.getText().toString(), passwordEditText.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        loginPasswordChanged();

        if (BuildConfig.AUTO_LOGIN) {
            getServiceProvider().login().performAutoLogin().subscribe(researcher -> {
                performLogin(researcher.getTextId(), researcher.getPassword());
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadLoginsSuggestions();
    }

    /**
     * Loads login suggestions for user
     */
    private void loadLoginsSuggestions() {
        RxExecutor.runWithUiCallback(getServiceProvider().login().getUserLogins()).subscribe((list) -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this,
                    android.R.layout.simple_dropdown_item_1line, list);
            login.setAdapter(adapter);
        });
    }

    /**
     * Performs login
     *
     * @param identifier
     * @param password
     */
    private void performLogin(String identifier, String password) {
        RxExecutor.runWithUiCallback(getServiceProvider().login().login(identifier, password))
                .subscribe((loggedIn) -> {
                    if (loggedIn.size() == 0) {
                        login.setError(getString(R.string.wrong_password_or_id));
                        passwordEditText.setError(getString(R.string.wrong_password_or_id));
                        LogUtils.d(TAG, "login failure: " + identifier + "   " + password);
                    } else if (loggedIn.size() == 1) {
                        Researcher researcher = loggedIn.get(0);
                        getServiceProvider().login().currentLoggedInUser = researcher;

                        LogUtils.d(TAG, "login success: " + identifier + "   " + password);
                        Intent intent = new Intent();
                        intent.putExtra(SuiteSelectActivity.SHOULD_CHECK_UPDATES, true);
                        intent.setClass(this, SuiteSelectActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (loggedIn.size() > 1) {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_user_duplicate), Toast.LENGTH_SHORT).show();
                        LogUtils.d(TAG, "two or more existing ids");
                    }
                }, (thr) -> {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_unknown) + thr.getMessage(), Toast.LENGTH_SHORT).show();
                    LogUtils.d(TAG, "unknown login error", thr);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == testRequestCode) {
            getServiceProvider().demo().showDemoReport(this);
        }
    }

    @OnTextChanged(R.id.login)
    protected void loginChanged() {
        loginPasswordChanged();
    }

    @OnTextChanged(R.id.password)
    protected void passwordChanged() {
        loginPasswordChanged();
    }

    /**
     * Enables login button after user types something if login and password field
     */
    private void loginPasswordChanged() {
        String loginText = login.getText().toString();
        String passwordText = passwordEditText.getText().toString();
        loginButton.setEnabled(!loginText.isEmpty() && !passwordText.isEmpty());
    }

    @OnClick(R.id.bug_report_button)
    protected void bugReportButtonClicked() {
        SupportDialog.show(this, getSupportFragmentManager());
    }
}
