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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 12.08.14.
 */
public class TutorialEndTask extends LookTask {

    private static final String TAG = TutorialEndTask.class.getSimpleName();

    private static final String TAG_BUTTON = "button";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_LOOP_BEGIN = "loopBegin";
    private static final String ATTR_BUTTON_HOVER = "hover";


    private static Rect touchCorrect = new Rect(100, 100, 200, 200);
    private static Rect touchWrong = new Rect(200, 200, 300, 300);

    private TaskGroupController taskGroupController;

    private int loopBegin;
    private Bitmap positiveButtonHover;
    private Bitmap negativeButtonHover;
    private boolean negativeButtonClicked;
    private boolean postiveButtonClicked;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public TutorialEndTask(Context context) {
        super(context);
    }

    public void setTaskGroupController(TaskGroupController taskGroupController) {
        this.taskGroupController = taskGroupController;
    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean valid = super.Create(a_info, a_handler, a_dir);

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
                                valid = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                            loopBegin = GetInteger(map, ATTR_LOOP_BEGIN, -1);

                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            }

            list = list.item(a_info.m_groupIndex).getChildNodes();
            VirtualFile bmpFile;
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_BUTTON)) {
                    try {
                        NamedNodeMap map = list.item(i).getAttributes();
                        if (GetString(map, ATTR_MODE).equals("positive")) {
                            String[] location = GetString(map, ATTR_LOCATION).split(",");
                            touchCorrect = new Rect(Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(location[3]));
                            String fileName = GetString(map, ATTR_BUTTON_HOVER);
                            bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // ale głownej planszy nie pomniejszamy
                                positiveButtonHover = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                positiveButtonHover = null;
                                valid = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                        } else {
                            String[] location = GetString(map, ATTR_LOCATION).split(",");
                            touchWrong = new Rect(Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(location[3]));
                            String fileName = GetString(map, ATTR_BUTTON_HOVER);
                            bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // ale głownej planszy nie pomniejszamy
                                negativeButtonHover = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                negativeButtonHover = null;
                                valid = false;

                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                        }
                    } catch (XMLFileException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                    }
                }
            }
        }
        return valid;
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint = new Paint();
            myPaint.setStrokeWidth(10);

            myPaint.setColor(Color.rgb(0, 255, 0));
            canvas.drawRect(touchCorrect, myPaint);

            myPaint.setColor(Color.rgb(255, 0, 0));
            canvas.drawRect(touchWrong, myPaint);
        }
        if (postiveButtonClicked) {
            canvas.drawBitmap(positiveButtonHover, null, touchCorrect, m_paint);
        }
        if (negativeButtonClicked) {
            canvas.drawBitmap(negativeButtonHover, null, touchWrong, m_paint);
        }

    }

    @Override
    public boolean TouchEvent(MotionEvent event) {
        ScreenTouched();

        boolean retValue = false;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int last = event.getPointerCount() - 1;
                if (last >= 0) {
                    Float xPos = new Float(event.getX(last));
                    Float yPos = new Float(event.getY(last));
                    int x = xPos.intValue();
                    int y = yPos.intValue();
                    if (touchCorrect.contains(x, y)) {
                        if (taskGroupController != null) {
                            taskGroupController.goToNextTask();
                            postiveButtonClicked = true;
                            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                        }
                    } else if (touchWrong.contains(x, y)) {
                        if (taskGroupController != null && loopBegin != -1) {
                            taskGroupController.rollbackToPoint(loopBegin - 1);
                            negativeButtonClicked = true;
                            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                        }
                    }

                }
            }
            break;

            case MotionEvent.ACTION_UP: {

            }
            break;
        }

        return retValue;
    }


}
