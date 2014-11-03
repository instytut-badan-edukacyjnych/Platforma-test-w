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


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import pl.edu.ibe.loremipsum.arbiter.BaseArbiter;
import pl.edu.ibe.loremipsum.arbiter.MarkData;
import pl.edu.ibe.loremipsum.configuration.TaskSuiteConfig;
import pl.edu.ibe.loremipsum.manager.BaseManager.ManagerTestInfo;
import pl.edu.ibe.loremipsum.manager.TestManager;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.task.TaskGroup;
import pl.edu.ibe.loremipsum.task.tpr.cordova.counting.CountingSummaryTask;
import pl.edu.ibe.testplatform.R;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.handler.AppHandlerService;
import pl.edu.ibe.loremipsum.tablet.task.mark.TaskResultStorage;
import pl.edu.ibe.loremipsum.tablet.task.note.NoteDialog;
import pl.edu.ibe.loremipsum.tablet.test.CurrentTaskSuiteService;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.TaskView;
import pl.edu.ibe.loremipsum.tools.LogUtils;

/**
 * Klasa (Activity) wyświetlania zadań
 *
 *
 */
public class TaskAct extends TaskWrapperActivity {

    public static final int RUN_TASK_SUITE_REQUEST = 6546;
    private static final String TAG = TaskAct.class.toString();
    private View quit;
    private ImageView photo;
    private CheckBox microphone;
    private View expandPositionHolder;
    private View expandMarks;
    private View markZero;
    private View markOne;
    private View markTwo;
    private View markCustom;
    private View previous;
    private ImageView next;
    private View reload;
    private View step;
    private View tcomBtn;
    /**
     * wyswietlane zadanie
     */
    private BaseTask m_exeTask = null;
    /**
     * Uchwyt do komunikacji z głwna aktywnoscia
     */
    private Handler m_taskHandler = null;
    private CurrentTaskSuiteService.TestMode currentTestMode;
    /**
     * Obsługa przycisku przywołania przyciskow oceny
     */
    View.OnClickListener m_expBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick expose");

        markZero.setVisibility(View.INVISIBLE);
        markOne.setVisibility(View.INVISIBLE);
        markTwo.setVisibility(View.INVISIBLE);
        switch (m_exeTask.GetMarkRange()) {
            case BaseTask.TASK_MARK_RANGE_0_1_2:
                markTwo.setVisibility(View.VISIBLE);
            case BaseTask.TASK_MARK_RANGE_0_1:
                markOne.setVisibility(View.VISIBLE);
                markZero.setVisibility(View.VISIBLE);
                break;
            case BaseTask.TASK_MARK_RANGE_UNLIMITED:
                displayMarkUnlimitedPopup();
                break;
        }
    };
    /**
     * Obsługa przycisku oceny 0
     */
    View.OnClickListener m_zerBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick mark 0");
        m_exeTask.SetMark(BaseArbiter.APP_MARK_0);
        m_taskHandler.sendEmptyMessage(BaseTask.TASK_MESS_MARK);
    };
    /**
     * Obsługa przycisku oceny 1
     */
    View.OnClickListener m_oneBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick mark 1");
        m_exeTask.SetMark(BaseArbiter.APP_MARK_1);
        m_taskHandler.sendEmptyMessage(BaseTask.TASK_MESS_MARK);
    };
    /**
     * Obsługa przycisku oceny 2
     */
    View.OnClickListener m_twoBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick mark 2");
        m_exeTask.SetMark(BaseArbiter.APP_MARK_2);
        m_taskHandler.sendEmptyMessage(BaseTask.TASK_MESS_MARK);
    };
    /**
     * Obsluga przycisku poprzednie zadanie
     */
    View.OnLongClickListener m_preBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick previous");

        try {
            changeTask(TestManager.GetPrevTask());
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception!", e);
        }

        return true;
    };
    /**
     * Obsługa przyciksu wykonania zdjecia
     */
    View.OnClickListener m_trigBtnListener = a_view -> LogUtils.d(TAG, "onClick cam trigger");
    private static final int TAKE_PHOTO_CODE = 3;
    // INFO_CHANGE <end>
    /**
     * Obsługa przycisku zakończenia badania
     */
    View.OnLongClickListener m_menBtnListener = a_view -> {
        new AlertDialog.Builder(this).setTitle(R.string.close_task_view)
                .setMessage(R.string.do_you_really_want_to_close_research)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (currentTestMode == CurrentTaskSuiteService.TestMode.NORMAL) {
                        NoteDialog noteDialog = new NoteDialog() {
                            @Override
                            public void dialogDissmised(boolean accepted) {
                                if (accepted)
                                    finishActivity();
                            }
                        };
                        noteDialog.show(fragmentManager, "dialog");
                    } else {
                        finishActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create().show();
        return true;
    };
    /**
     * uchwyt do pierwszego planu wyświetlania
     */
    private TaskView m_foreView = null;
    /**
     * uchwyt do drugiego planu wyświetlania
     */
    private TaskView m_backView = null;
    /**
     * kontrolka wyświetlająca listę zadań
     */
    private Spinner m_taskList = null;
    /**
     * adapter dostepu do danych na liście
     */
    private ArrayAdapter<String> m_taskListAdapter = null;
    /**
     * Nazwy zadan skojarzone z opisem
     */
    private HashMap<String, TaskInfo> m_tasks = null;
    // INFO_CHANGE miejsca gdzie zrobiono lokalną implementacje zmiany sposobu wyświetlania zadania
    private ViewChangeBase m_changeExecutor = null;

    /**
     * Obsluga przycisku ponownego uruchamienia zadania
     */
    View.OnClickListener m_relBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick reload");

        if (m_exeTask != null) {
            m_exeTask.ReloadTask();

            if (m_foreView != null) {
                m_foreView.invalidate();
            }

            next.setImageResource(R.drawable.wai);
        }
    };
    /**
     * Obsluga zakończenia odtwarzania dźwieku
     */
    MediaPlayer.OnCompletionListener m_mediaListener = mp -> {
        if (m_exeTask != null) {
            m_exeTask.SoundFinish();
            if (m_exeTask.IsSoundNextTask()) {
                m_taskHandler.sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 50);
            }
        }
    };
    /**
     * poprzednie zadanie czekające na usunecie
     */
    private BaseTask m_oldTask = null;
    /**
     * odtwarzacz dźwieków
     */
    private MediaPlayer m_player = null;
    /**
     * Obsluga przycisku krokow w zadaniu
     */
    View.OnClickListener m_steBtnListener = a_view -> {
        try {
            LogUtils.d(TAG, "onClick step");

            if (m_exeTask != null) {
                if (m_exeTask.getTaskFlags().stepCommand) {
                    m_exeTask.PlayExtraCommand(m_player);
                } else {
                    m_taskHandler.sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 50);
                }
            }
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception", e);
        }
    };
    /**
     * Obsluga przycisku odtworzenia polecenia
     */
    View.OnClickListener m_comBtnListener = a_view -> {
        try {
            LogUtils.d(TAG, "onClick command");

            if (m_player == null) {
                m_player = new MediaPlayer();
                m_player.setOnCompletionListener(m_mediaListener);
            }

            m_exeTask.RepeatCommand(m_player);
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception", e);
        }
    };

    private TaskInfo currentTask = null;
    /**
     * flaga mozliwosci robienia zdjęc
     */
    private boolean m_shootEnabled = true;
    private String fileName;
    /**
     * Obsługa przycisku wykonania zdjęcia
     */
    View.OnClickListener m_phoBtnListener = a_view -> {

        fileName = m_exeTask.GetName();
        fileName += " ";
        fileName += LoremIpsumApp.GetDateTimeString();
        fileName += LoremIpsumApp.SYS_PICTURE_FILE_EXT;
        File outFile = new File(ServiceProvider.obtain().currentTaskSuite().getResultsDir(), fileName);
        Uri outputFileUri = Uri.fromFile(outFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        TaskService.photoTaken =true;
        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
    };

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        switch (buttonView.getId()) {
            case R.id.microphone:
                if (isChecked) {
                    m_exeTask.startRecord();
                } else {
                    m_exeTask.stopRecord();
                }
                break;
            default:
                throw new RuntimeException("Sorry unimplemented :(");
        }
    };

    private FrameLayout taskWrapperView;
    /**
     * Obsługa przycisku nastepne zadanie
     */
    View.OnLongClickListener m_nexBtnListener = a_view -> {
        LogUtils.d(TAG, "onClick next");
        if(countingContainer !=null &&countingContainer.getVisibility() == View.VISIBLE){
            countingContainer.setVisibility(View.GONE);
        }
        if (currentTask == LoremIpsumApp.m_finishTask)
            m_menBtnListener.onLongClick(quit);
        else {
            try {
                storeMark();
                changeTask(TestManager.GetNextTask(testMode ->{
                    currentTestMode = testMode;
                    handleModeChange(testMode);
                }));
            } catch (IOException e) {
                LogUtils.e(TAG, "Exception!", e);
            }
        }
        return true;
    };
    /**
     * interpolator zwiekszający
     */
    private Interpolator m_accelerator = new AccelerateInterpolator();
    /**
     * interpolator zmniejszający
     */
    private Interpolator m_decelerator = new DecelerateInterpolator();

    /**
     * Konstruktor
     */
    public TaskAct() {
        super();
        LogUtils.d(TAG, "TaskAct");
        m_tasks = new HashMap<>(LoremIpsumApp.APP_PREDICT_AREA_NUMBER * LoremIpsumApp.APP_PREDICT_TASK_NUMBER);
    }

    /**
     * Uruchamienie zadania
     */
    public void StartTask() throws IOException {
        if (m_exeTask != null) {
            LogUtils.d(TAG, "StartTask");

            hideButtons();
            showButtons(m_exeTask.GetButtonVisibility());

            if (m_player == null) {
                m_player = new MediaPlayer();
                m_player.setOnCompletionListener(m_mediaListener);
            }

            m_exeTask.StartTask();
            m_exeTask.PlayCommand(m_player);
        }
    }
    public MediaPlayer getPlayer() {
        return m_player;
    }
    /**
     * Usnięcie zakończonego zadania
     */
    public void DeleteTask() throws IOException {
        if (m_oldTask != null) {
            m_oldTask.Destory();

            LogUtils.d(TAG, "DeleteTask: " + m_oldTask.m_taskInfo.m_class + " " + m_oldTask.m_taskInfo.m_name);
            LoremIpsumApp.LoggerSaveMemoryInfo(m_oldTask.m_taskInfo.m_name);

            m_oldTask = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate");

        setContentView(R.layout.task);

        ActionBar bar = getActionBar();
        bar.hide();
        // wymuszenie odwróconej pozycji ekranu
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        currentTestMode = getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTestMode();
        quit = findViewById(R.id.quit);
        quit.setOnLongClickListener(m_menBtnListener);

        photo = (ImageButton) findViewById(R.id.photo);
        photo.setOnClickListener(m_phoBtnListener);
        photo.setVisibility(View.INVISIBLE);

        microphone = (CheckBox) findViewById(R.id.microphone);
        microphone.setOnCheckedChangeListener(checkedChangeListener);
        microphone.setVisibility(View.INVISIBLE);

        expandPositionHolder = findViewById(R.id.mark_dropdown_position_holder);

        expandMarks = findViewById(R.id.texp_btn);
        expandMarks.setOnClickListener(m_expBtnListener);
        expandMarks.setVisibility(View.INVISIBLE);

        markZero = findViewById(R.id.zero);
        markZero.setOnClickListener(m_zerBtnListener);
        markZero.setVisibility(View.INVISIBLE);

        markOne = findViewById(R.id.one);
        markOne.setOnClickListener(m_oneBtnListener);
        markOne.setVisibility(View.INVISIBLE);

        markTwo = findViewById(R.id.two);
        markTwo.setOnClickListener(m_twoBtnListener);
        markTwo.setVisibility(View.INVISIBLE);

        markCustom = findViewById(R.id.custom_mark);
        markCustom.setOnClickListener(v -> displayMarkUnlimitedPopup());
        markCustom.setVisibility(View.INVISIBLE);

        previous = findViewById(R.id.previous);
        previous.setOnLongClickListener(m_preBtnListener);
        previous.setVisibility(View.INVISIBLE);

        next = (ImageButton) findViewById(R.id.next);
        next.setOnLongClickListener(m_nexBtnListener);
        next.setVisibility(View.INVISIBLE);

        reload = findViewById(R.id.reload);
        reload.setOnClickListener(m_relBtnListener);
        reload.setVisibility(View.INVISIBLE);

        step = findViewById(R.id.step);
        step.setOnClickListener(m_steBtnListener);
        step.setVisibility(View.INVISIBLE);

        tcomBtn = findViewById(R.id.tcom_btn);
        tcomBtn.setOnClickListener(m_comBtnListener);
        tcomBtn.setVisibility(View.INVISIBLE);

        ImageButton m_cameraTrigger = (ImageButton) findViewById(R.id.cam_trig);
        m_cameraTrigger.setOnClickListener(m_trigBtnListener);
        m_cameraTrigger.setVisibility(View.INVISIBLE);

        // INFO_CHANGE miejsca gdzie zrobiono lokalną implementacje zmiany sposobu wyświetlania zadania
        m_changeExecutor = new ViewChangeOpaque(() -> {
            TaskView fake = m_foreView;
            m_foreView = m_backView;
            m_backView = fake;

            m_backView.Release();

            DeleteTask();
            StartTask();
        });
        // INFO_CHANGE <end>

        m_foreView = new TaskView(this);
//        m_foreView.setRotation(180.0f);
        m_backView = new TaskView(this);
//        m_backView.setRotation(180.0f);

         taskWrapperView = (FrameLayout) findViewById(R.id.task_wrapper);

        ImageView iv = (ImageView) findViewById(R.id.fake_img);
        taskWrapperView.addView(m_foreView, iv.getLayoutParams());
        taskWrapperView.addView(m_backView, iv.getLayoutParams());

        m_backView.setVisibility(View.GONE);
        iv.setVisibility(View.GONE);

        m_taskHandler = new TaskHandler();

        if (savedInstanceState != null) {
            LogUtils.v(TAG, "SIS is not null");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(TAG, "onStart");
        if(!TaskService.photoTaken){
            View view = findViewById(R.id.task_layout);
            view.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);

            m_tasks.clear();

            try {
                changeTask(TestManager.GetNextTask(mode ->{
                    currentTestMode = mode;
                    handleModeChange(mode);
                }));
            } catch (IOException e) {
                LogUtils.e(TAG, "Exception!", e);
            }
        }
        TaskService.photoTaken = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");

        if (m_player == null) {
            m_player = new MediaPlayer();
            m_player.setOnCompletionListener(m_mediaListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause");

        storeMark();

        if (m_player != null) {
            m_player.release();
            m_player = null;
        }
    }

    @Override
    public void onBackPressed() {
        // blokuje wyjscie przyciskiem back
    }

    /**
     * Chowa przyciski w polu badacza
     */
    private void hideButtons() {
        photo.setVisibility(View.GONE);
        microphone.setVisibility(View.GONE);
        expandMarks.setVisibility(View.GONE);
        markZero.setVisibility(View.GONE);
        markOne.setVisibility(View.GONE);
        markTwo.setVisibility(View.GONE);
        markCustom.setVisibility(View.GONE);
        previous.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        reload.setVisibility(View.GONE);
        step.setVisibility(View.GONE);
        tcomBtn.setVisibility(View.GONE);
    }

    /**
     * POkazuje przyciski zfgodnei z ustawionymi flagami
     *
     * @param a_visibility - flagi widoczności przycisków
     */
    private void showButtons(int a_visibility) {
        if ((a_visibility & BaseTask.TASK_BTN_COMMAND) != 0) {
            tcomBtn.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_MENU) != 0) {
            quit.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_NEXT) != 0) {
            next.setImageResource(R.drawable.wai);
            next.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_PHOTO) != 0) {
            if (m_shootEnabled) {
                photo.setImageResource(R.drawable.pho);
            } else {
                photo.setImageResource(R.drawable.nph);
            }
            photo.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_PREV) != 0) {
            previous.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_RELOAD) != 0) {
            reload.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_STEP) != 0) {
            step.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_MARK_ONE) != 0) {
            markOne.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_MARK_TWO) != 0) {
            markTwo.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_MARK_ZERO) != 0) {
            markZero.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_MARK_UNLIMITED) != 0) {
            markCustom.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_EXPOSE) != 0) {
            expandMarks.setVisibility(View.VISIBLE);
        }
        if ((a_visibility & BaseTask.TASK_BTN_SOUND) != 0) {
            microphone.setVisibility(View.VISIBLE);
            if (getServiceProvider().currentTaskSuite().getCurrentTestRunData().getTaskSuite().getPilot()) {
                microphone.setChecked(true);
            } else {
                microphone.setChecked(false);
            }
        }

        if (LoremIpsumApp.SHOW_IRT_FLAG) {
            if (m_exeTask != null) {
                ManagerTestInfo info = LoremIpsumApp.m_testManager.GetTestInfo(m_exeTask.m_taskInfo.m_area);
                if (info != null) {
                    showTestInformation(m_exeTask, info);
                }
            }
        }
    }

    /**
     * Przygotowuje zadanie o podanym opisie
     *
     * @param a_info - opis zadania
     * @return true jezeli uruchamienie zakończone powodzeniem
     */
    private boolean loadTask(TaskInfo a_info) throws IOException {
        LogUtils.d(TAG, "loadTask: " + a_info.m_class + " " + a_info.m_name);
        LoremIpsumApp.LoggerSaveMemoryInfo(a_info.m_name);

        m_exeTask = BaseTask.CreateInstance(this, a_info, a_info.m_class, m_taskHandler, ServiceProvider.obtain().currentTaskSuite().getResultsDir());
        if(m_exeTask instanceof CountingSummaryTask ){
             ((CountingSummaryTask)m_exeTask).setView(countingContainer);
        }
        if(m_exeTask instanceof TaskGroup ){
            ((TaskGroup)m_exeTask).setView(countingContainer);
        }
        if (m_exeTask != null) {
            if (m_player == null) {
                m_player = new MediaPlayer();
                m_player.setOnCompletionListener(m_mediaListener);
            }

            m_player.reset();
            hideButtons();

            m_oldTask = m_backView.GetTask();
            m_backView.Assign(m_exeTask);

            return true;
        }

        return false;
    }

    /**
     * Zmienia zadania na zadanie o podanym opisie
     *
     * @param a_info - opis zadania
     */
    private void changeTask(TaskInfo a_info) throws IOException {
        currentTask = a_info;
        if (a_info != null) {
            LogUtils.v(TAG, "changeTask(" + a_info.m_name + ")");

            if (loadTask(a_info)) {
                if (m_taskList != null) {
                    String name = a_info.m_name;
                    int i;
                    for (i = 0; i != m_taskListAdapter.getCount(); ++i)
                        if (m_taskListAdapter.getItem(i).equals(name))
                            break;
                    if (currentTestMode != CurrentTaskSuiteService.TestMode.DEMO && currentTestMode != CurrentTaskSuiteService.TestMode.TUTORIAL) {
                        if (i == m_taskListAdapter.getCount())
                            throw new IllegalStateException("Could not find item \"" + name + "\"");
                        m_taskList.setSelection(i);
                    }
                }
                showTestName(a_info);
                // INFO_CHANGE miejsca gdzie zrobiono lokalną implementacje zmiany sposobu wyświetlania zadania

                m_changeExecutor.Change(m_foreView, m_backView, m_accelerator, m_decelerator);
                // INFO_CHANGE <else>
//              oremIpsumTabletApp.m_taskChange.Change( this, m_foreView, m_backView );
                // INFO_CHANGE <end>
            } else {
                String mess = getResources().getString(R.string.e_task);
                mess += a_info.m_name + a_info.m_area;
                Toast.makeText(this,
                        mess + a_info.m_name + a_info.m_area,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Rejestruje ocenę zadania
     */
    private void storeMark() {

        if (m_exeTask != null) {
            m_exeTask.EndTask();
            MarkData mark = m_exeTask.GetMark();
            if (mark != null) {
                TaskResultStorage.StoreTaskResult(mark);
            }
        }
    }

    private void finishActivity() {
        try {
            getServiceProvider().currentTaskSuite().handleTestEnd();
            LogUtils.d(TAG, "onClick menu");

            storeMark();

            m_exeTask = null;

            m_foreView.playSoundEffect(SoundEffectConstants.CLICK);
            AppHandlerService.AppHandler hand = getServiceProvider().appHandler().getAppHandler();
            hand.setAppHandlerInteface(new AppHandlerService.AppHandlerInterface() {
                @Override
                public void showLoginRequest(Message msg) {

                }

                @Override
                public void loadData(Message msg) {

                }

                @Override
                public void dataLoaded(Message msg) {

                }

                @Override
                public void login(Message msg) {

                }

                @Override
                public void testEnding(Message msg) {
                    if (LoremIpsumApp.FinishTest()) {
                        Thread rl = new LoremIpsumApp.LoadDataThread(hand, LoremIpsumApp.APP_RESULT_LOAD_FLAG, true);
                        rl.start();
                    }
                }

                @Override
                public void resultsLoaded(Message msg) {
                }
            });

            hand.sendEmptyMessage(LoremIpsumApp.APP_MESS_TEST_ENDING);

            BaseTask oldTask = m_backView.GetTask();
            m_backView.Assign(null);
            if (oldTask != null) {
                oldTask.Destory();
            }

            oldTask = m_foreView.GetTask();
            m_foreView.Assign(null);
            if (oldTask != null) {
                oldTask.Destory();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception", e);
        }

        finish();
    }

    /**
     * Displays popup responsible for handling custom marks
     */
    private void displayMarkUnlimitedPopup() {

        ListPopupWindow lpw = new ListPopupWindow(this);
        lpw.setAnchorView(expandPositionHolder);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.row_test_mark);
        for (int i = 0; i <= m_exeTask.m_taskInfo.getM_range(); i++) {
            adapter.add(i);
        }
        lpw.setAdapter(adapter);
        lpw.setOnItemClickListener((parent, view, position, id) -> {
            LogUtils.d(TAG, "onClick mark " + position);
            m_exeTask.SetMark(position);
            m_taskHandler.sendEmptyMessage(BaseTask.TASK_MESS_MARK);
        });
        lpw.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            LoremIpsumApp.StoreTaskFile(fileName);
        }
    }

    public Handler getTaskHandler() {
        return m_taskHandler;
    }

    /**
     * Klasa obsług zdarzeń związanych z obsługą zadań
     *
     *
     */
    private class TaskHandler extends Handler {

        /**
         * Konstruktor
         */
        public TaskHandler() {

            super();
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message a_msg) {

            super.handleMessage(a_msg);

            switch (a_msg.what) {
                case BaseTask.TASK_MESS_REFRESH_VIEW:
                    Log.d(TAG,"refresh");
                    m_foreView.invalidate();
                    break;

                case BaseTask.TASK_MESS_HAPTIC_FEEDBACK:

                    if (TaskSuiteConfig.m_hapticFlag) {
                        m_foreView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    if (TaskSuiteConfig.m_clickFlag) {
                        m_foreView.playSoundEffect(SoundEffectConstants.CLICK);
                    }
                    break;

                case BaseTask.TASK_MESS_MARK:

                    next.setImageResource(R.drawable.nex);

                    if (LoremIpsumApp.SHOW_MARK_FLAG) {
                        MarkData mark = m_exeTask.GetMark();
                        showMark(mark);
                    }

                    break;

                case BaseTask.TASK_MESS_NO_MARK:
                    next.setImageResource(R.drawable.wai);
                    break;

                case BaseTask.TASK_MESS_NEXT_TASK:
                    if (m_exeTask != null) {

                        try {
                            if (m_exeTask.NextTask()) {
                                StartTask();

                                if (m_foreView != null) {
                                    m_foreView.invalidate();
                                }
                            }
                        } catch (IOException e) {
                            LogUtils.e(TAG, "Exception!", e);
                        }
                    }

                    break;

                case BaseTask.TASK_MESS_SELECT_TASK:

                    if (m_exeTask != null) {

                        try {
                            if (m_exeTask.SelectTask(a_msg.arg1)) {

                                StartTask();

                                if (m_foreView != null) {
                                    m_foreView.invalidate();
                                }
                            }
                        } catch (IOException e) {
                            LogUtils.e(TAG, "Exception!", e);
                        }
                    }
                    break;
            }
        }
    }
}