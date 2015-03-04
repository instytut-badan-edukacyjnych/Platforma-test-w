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

package pl.edu.ibe.loremipsum.task.tpr.actionscript.counting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.loremipsum.tools.support.task.RectPlacer;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 24.07.14.
 */
public class CountingTask extends LookTask {
    private static final String TAG = CountingTask.class.getSimpleName();


    private static final String ATTR_DRAWING_AREA_LEFT = "drawing_area_left";
    private static final String ATTR_DRAWING_AREA_TOP = "drawing_area_top";
    private static final String ATTR_DRAWING_AREA_RIGHT = "drawing_area_right";
    private static final String ATTR_DRAWING_AREA_BOTTOM = "drawing_area_bottom";
    private static final String ATTR_TIMEOUT = "timeout";
    private static final String TAG_ITEM = "item";
    private static final String ATTR_CORRECT = "correct";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_QUANTITY = "quantity";
    private static final String ATTR_RANGE = "range";
    private static final String ATTR_WIDTH = "width";
    private static final String ATTR_HEIGHT = "height";

    private int drawingLeft;
    private int drawingTop;
    private int drawingRight;
    private int drawingBottom;
    private int timeout;


    private ArrayList<ItemHolder> itemList;
    private Rect container;
    private RectPlacer rectPlacer;


    private int correctBallsCount;
    private int wrongBallsCount;
    private long startTime;
    private long endTime;
    private CountingTaskInterface countingTaskInterface;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public CountingTask(Context context) {
        super(context);
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


                            drawingLeft = GetInteger(map, ATTR_DRAWING_AREA_LEFT, 0);
                            drawingTop = GetInteger(map, ATTR_DRAWING_AREA_TOP, 0);
                            drawingRight = GetInteger(map, ATTR_DRAWING_AREA_RIGHT, 40);
                            drawingBottom = GetInteger(map, ATTR_DRAWING_AREA_BOTTOM, 40);
                            timeout = GetInteger(map, ATTR_TIMEOUT, -1);

                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }

                list = list.item(a_info.m_groupIndex).getChildNodes();
                itemList = new ArrayList<ItemHolder>();
                ItemHolder itemHolder;
                VirtualFile bmpFile;
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_ITEM)) {
                        try {
                            itemHolder = new ItemHolder();
                            NamedNodeMap map = list.item(i).getAttributes();
                            itemHolder.isCorrect = GetString(map, ATTR_CORRECT).equals("true");


                            itemHolder.width = GetInteger(map, ATTR_WIDTH, 55);
                            itemHolder.height = GetInteger(map, ATTR_HEIGHT, 55);

                            bmpFile = a_info.getDirectory().getChildFile(GetString(map, ATTR_IMAGE));
                            if (bmpFile.exists()) {
                                itemHolder.bitmap = BitmapFactory.decodeStream(bmpFile.getInputStream());
                            }
                            itemHolder.setQuantity(extractQuantity(map));
                            itemList.add(itemHolder);
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            }
        }
        container = new Rect(drawingLeft, drawingTop, drawingRight, drawingBottom);

        int itemsCount = 0;
        for (ItemHolder itemHolder : itemList) {
            itemsCount += itemHolder.quantity;
        }


        rectPlacer = new RectPlacer(container, itemsCount);
        for (ItemHolder itemHolder : itemList) {
            for (int i = 0; i < itemHolder.quantity; i++) {
                itemHolder.positions.add(rectPlacer.getItemPosition(itemHolder.width, itemHolder.height));
            }
        }
        for (ItemHolder itemHolder : itemList) {
            if (itemHolder.isCorrect) {
                correctBallsCount += itemHolder.quantity;
            } else {
                wrongBallsCount += itemHolder.quantity;
            }
        }

        LogUtils.d("Container", container.flattenToString());


        if (timeout != -1) {
            ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, timeout);
        }

        startTime = System.currentTimeMillis();
        return valid;
    }

    private String extractQuantity(NamedNodeMap map) {
        try {
            return GetString(map, ATTR_QUANTITY);
        } catch (XMLFileException e) {
            e.printStackTrace();
        }
        try {
            return GetString(map, ATTR_RANGE);
        } catch (XMLFileException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("range or quantity expected");
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint = new Paint();
            myPaint.setColor(Color.rgb(0, 124, 0));
            myPaint.setStrokeWidth(10);
            canvas.drawRect(container, myPaint);
        }

        for (ItemHolder itemHolder : itemList) {
            for (Rect position : itemHolder.positions) {
                canvas.drawBitmap(itemHolder.bitmap, null, position, m_paint);
            }
        }
    }

    /*
        * (non-Javadoc)
        * @see pl.edu.ibe.loremipsum.task.BaseTask#Destory()
        */
    @Override
    public void Destory() throws IOException {
        LogUtils.d(TAG, "Destory");

        if (m_main != null) {
            m_main.recycle();
            m_main = null;
        }
        endTime = System.currentTimeMillis();
        if (countingTaskInterface != null) {
            countingTaskInterface.setTaskStatistics(correctBallsCount, wrongBallsCount, startTime, endTime);
        }
        super.Destory();
    }

    public void setCountingTaskInterface(CountingTaskInterface countingTaskInterface) {
        this.countingTaskInterface = countingTaskInterface;
    }


    interface CountingTaskInterface {
        public void setTaskStatistics(int correctBallsCount, int wrongBallsCount, long startTime, long endTime);
    }

    private static class ItemHolder {
        public ArrayList<Rect> positions;
        public Bitmap bitmap;
        public boolean isCorrect;
        public int quantity;
        public int width;
        public int height;

        private ItemHolder() {
            positions = new ArrayList<>();
        }

        public void setQuantity(String quantity) {
            String[] digits = quantity.split(",");
            if (digits.length == 1) {
                this.quantity = Integer.valueOf(digits[0]);
            } else {
                int startRange = Integer.valueOf(digits[0]);
                int endRange = Integer.valueOf(digits[1]);
                this.quantity = new Random().nextInt(endRange - startRange) + startRange;
            }
        }
    }


}
