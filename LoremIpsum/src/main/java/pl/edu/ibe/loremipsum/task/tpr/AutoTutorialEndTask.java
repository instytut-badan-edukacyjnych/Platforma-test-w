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

package pl.edu.ibe.loremipsum.task.tpr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 09.09.14.
 */
public class AutoTutorialEndTask extends LookTask {

    private static final String TAG = AutoTutorialEndTask.class.getSimpleName();

    private static final String ATTR_FAILURE_IMAGE = "failureImage";
    private static final String ATTR_LOOP_BEGIN = "loopBegin";


    private int loopBegin;
    private Bitmap failureImage;


    private TaskGroupController taskGroupController;
    private boolean tutorialWasSuccesfull;


    private View.OnClickListener onClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            Mode mode = taskGroupController.getMode();
            switch (mode) {
                case COUNTING:
                    if (tutorialWasSuccesfull) {
                        taskGroupController.goToNextTask();
                    } else {
                        taskGroupController.rollbackToPoint(loopBegin);
                        taskGroupController.clearTutorialData();
                    }
                    break;
                case FOUR_FIELDS:
                    if (tutorialWasSuccesfull) {
                        taskGroupController.rollbackToPoint(loopBegin);
                    } else {
                        taskGroupController.goToNextTask();
                    }
                    break;
                case GRID:
                    if (tutorialWasSuccesfull) {
                        taskGroupController.goToNextTask();
                    } else {
                        taskGroupController.rollbackToPoint(loopBegin);
                        taskGroupController.clearTutorialData();
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown mode: " + mode);
            }
            successButton.setOnClickListener(null);
            failureButton.setOnClickListener(null);
            successButton.setVisibility(View.GONE);
            failureButton.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
    };

    private View successButton;
    private View failureButton;
    private LinearLayout view;


    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public AutoTutorialEndTask(Context context) {
        super(context);
    }

    public void setTaskGroupController(TaskGroupController taskGroupController) {
        this.taskGroupController = taskGroupController;
        tutorialWasSuccesfull = taskGroupController.tutorialIsSuccesful();

    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean returnValue = super.Create(a_info, a_handler, a_dir);


        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            String fileName = GetString(map, XML_DETAILS_TASK_MAIN);
                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // ale głownej planszy nie pomniejszamy
                                m_main = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                m_main = null;
                                returnValue = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }

                            fileName = GetString(map, ATTR_FAILURE_IMAGE);
                            bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // ale głownej planszy nie pomniejszamy
                                failureImage = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                failureImage = null;
                                returnValue = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }

                            loopBegin = GetInteger(map, ATTR_LOOP_BEGIN, -1);

                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                            returnValue = false;
                        }
                    }
                }
            }
        }

        m_actHandler.sendEmptyMessageDelayed(TASK_MESS_REFRESH_VIEW, 400);
        return returnValue;
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);

        if (tutorialWasSuccesfull) {
            canvas.drawBitmap(m_main, null, new Rect(0, 0, 1130, 752), m_paint);
            successButton.setVisibility(View.VISIBLE);
            failureButton.setVisibility(View.GONE);
        } else {
            canvas.drawBitmap(failureImage, null, new Rect(0, 0, 1130, 752), m_paint);
            successButton.setVisibility(View.GONE);
            failureButton.setVisibility(View.VISIBLE);
        }
    }


    public void setView(LinearLayout view) {
        this.view = view;
        view.setVisibility(View.VISIBLE);
        successButton = view.findViewById(R.id.successButton);
        failureButton = view.findViewById(R.id.failureButton);

        successButton.setOnClickListener(onClickListener);
        failureButton.setOnClickListener(onClickListener);


    }

    public enum Mode {
        COUNTING, FOUR_FIELDS, GRID
    }
}
