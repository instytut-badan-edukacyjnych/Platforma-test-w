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

package pl.edu.ibe.loremipsum.tablet.task;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import pl.edu.ibe.loremipsum.TestParameterView;
import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.manager.BaseManager;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceActivity;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 28.03.14.
 */
public abstract class TaskWrapperActivity extends BaseServiceActivity {

    public static float ratio;
    @InjectView(R.id.content_frame)
    ViewGroup frameLayout;
    @InjectView(R.id.test_options)
    View testOptions;
    @InjectView(R.id.additional_info)
    CheckBox additionalInfo;
    @InjectView(R.id.parameters_box)
    View parameterBox;
    @InjectView(R.id.test_name)
    TestParameterView name;
    @InjectView(R.id.test_area)
    TestParameterView area;
    @InjectView(R.id.test_mark)
    TestParameterView mark;
    @InjectView(R.id.test_answer)
    TestParameterView testAnswer;
    @InjectView(R.id.test_a)
    TestParameterView aParameter;
    @InjectView(R.id.test_b)
    TestParameterView bParameter;
    @InjectView(R.id.test_c)
    TestParameterView cParameter;
    @InjectView(R.id.test_number)
    TestParameterView number;
    @InjectView(R.id.test_theta)
    TestParameterView theta;
    @InjectView(R.id.test_se_theta)
    TestParameterView seTheta;
    @InjectView(R.id.test_manager)
    TestParameterView testManager;
    @InjectView(R.id.test_type)
    TestParameterView testType;
    @InjectView(R.id.test_properties)
    TestParameterView testProperties;
    @InjectView(R.id.test_info)
    View testInfo;
    @InjectView(R.id.test_name_textview)
    TextView testName;
    @InjectView(R.id.test_version)
    TextView testVersion;
    @InjectView(R.id.test_mode)
    TextView testMode;
    //Always visible
    @InjectView(R.id.task_name)
    TextView taskName;
    @InjectView(R.id.tpr_2_container)
    LinearLayout countingContainer;

    private LoremIpsumSimpleAdapter<String> stringLoremIpsumSimpleAdapter;
    private CurrentTaskSuiteService.TestRunData testRunData;
    private TaskSuiteConfig taskSuiteConfig;
    private boolean shouldRotateTaskView;
    private boolean isMenuOnRight;

    @OnCheckedChanged(R.id.additional_info)
    public void onCheckedChangeListener(boolean checked) {
        if (checked) {
            parameterBox.setVisibility(View.VISIBLE);
        } else {
            parameterBox.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_task_wrapper);
        ButterKnife.inject(this);

        testRunData = getServiceProvider().currentTaskSuite().getCurrentTestRunData();
        taskSuiteConfig = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig();

        stringLoremIpsumSimpleAdapter = new LoremIpsumSimpleAdapter<String>(this, android.R.layout.simple_spinner_item) {
            @Override
            protected long extractId(String item) {
                return 0;
            }

            @Override
            protected String populateItem(String item) {
                return item;
            }
        };

        if (testRunData.getTestMode() == CurrentTaskSuiteService.TestMode.NORMAL)
            testInfo.setVisibility(View.GONE);
        else {
            testInfo.setVisibility(View.VISIBLE);
            testName.setText(testRunData.getTaskSuite().getName());
            testVersion.setText(testRunData.getTaskSuite().getVersion());
            testMode.setText(testRunData.getTestMode().toString());
        }

        if ((testRunData.getTaskSuite().getPilot() && !taskSuiteConfig.disableCatAlgoritm)
                || testRunData.getTestMode() == CurrentTaskSuiteService.TestMode.DEMO) {
            testOptions.setVisibility(View.VISIBLE);
        } else {
            testOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        }
    }

    @Override
    public void setContentView(int layoutResId) {
        setContentView(getLayoutInflater().inflate(layoutResId, null));
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        frameLayout.removeAllViews();
        frameLayout.addView(view);

        View taskView = view.findViewById(R.id.task_wrapper);
        shouldRotateTaskView = getServiceProvider().sharedPreferences().isTaskViewRotated();
        isMenuOnRight = getServiceProvider().sharedPreferences().isMenuOnRight();
        ViewTreeObserver viewTreeObserver = frameLayout.getViewTreeObserver();
        assert viewTreeObserver != null;
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                View butonPosition = view.findViewById(R.id.buttons_position);
                if (shouldRotateTaskView && getServiceProvider().currentTaskSuite().isAllowedScreenRotation()) {
                    taskView.setRotation(180);
                    butonPosition.setVisibility(View.VISIBLE);
                } else {
                    butonPosition.setVisibility(View.GONE);
                }
                if (isMenuOnRight) {
                    View buttonsContainer = view.findViewById(R.id.buttons_container);
                    buttonsContainer.setX(getResources().getDimension(R.dimen.menu_on_right_offset));
                    buttonsContainer.bringToFront();
                    view.refreshDrawableState();
                }


                float frameWidth = frameLayout.getWidth();
                float viewWidth = 1280;
                float ratioWidth = frameWidth / viewWidth;
                float frameHeight = frameLayout.getHeight();
                float viewHeight = 800;
                float ratioHeight = frameHeight / viewHeight;
                ratio = ratioHeight > ratioWidth ? ratioWidth : ratioHeight;
                View testView = ((ViewGroup) view).getChildAt(0);
                assert testView != null;
                testView.setPivotX(0f);
                testView.setPivotY(0f);
                testView.setScaleX(ratio);
                testView.setScaleY(ratio);

                frameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    protected void showTestInformation(BaseTask m_exeTask, BaseManager.ManagerTestInfo info) {
        if (LoremIpsumApp.m_testManager != null) {
            testManager.setText(LoremIpsumApp.m_testManager.getClass().getSimpleName());
        }
        testProperties.setText(m_exeTask.getTaskFlags().getFlagString());
        testType.setText(m_exeTask.m_taskInfo.m_class);
        theta.setText(Double.toString(info.m_vector.m_theta));
        seTheta.setText(Double.toString(info.m_vector.m_se));
        number.setText(Integer.toString(info.m_vector.m_piece.size()));

        aParameter.setText(Double.toString(m_exeTask.m_taskInfo.m_irt_a));
        StringBuilder bBuilder = new StringBuilder();
        for (Double bVal : m_exeTask.m_taskInfo.m_irt_bs) {
            bBuilder.append(bVal);
            bBuilder.append("\n");
        }
        bParameter.setText(bBuilder.toString());
        cParameter.setText(Double.toString(m_exeTask.m_taskInfo.m_irt_c));
    }

    protected void showTestName(TaskInfo a_info) {
        taskName.setText(a_info.m_name);
        name.setText(a_info.m_name);
        area.setText(a_info.m_area);
    }

    protected void showMark(MarkData mark) {
        this.mark.setText(mark.m_mark + "");
        testAnswer.setText(mark.m_answer);
    }

    public void addTask(String m_name) {
        stringLoremIpsumSimpleAdapter.add(m_name);
        stringLoremIpsumSimpleAdapter.notifyDataSetChanged();
    }

    public void handleModeChange(CurrentTaskSuiteService.TestMode mode) {
        if (mode == CurrentTaskSuiteService.TestMode.NORMAL) {
            testInfo.setVisibility(View.GONE);
        } else if (mode == CurrentTaskSuiteService.TestMode.TUTORIAL) {
            testInfo.setVisibility(View.VISIBLE);
        }

    }
}
