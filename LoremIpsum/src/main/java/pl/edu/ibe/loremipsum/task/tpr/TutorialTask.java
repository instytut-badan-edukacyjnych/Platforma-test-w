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
import android.util.Log;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 06.08.14.
 */
public class TutorialTask extends LookTask {
    private static final String TAG = TutorialTask.class.getSimpleName();

    private static final String TAG_ITEM = "item";
    private static final String ATTR_DISPLAY_START = "displayStart";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_DISPLAY_TIME = "displayTime";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_ZINDEX = "zindex";
    private static final String ATTR_REFRESH_TIME = "refreshTime";

    private ArrayList<ItemHolder> itemList;

    private boolean firstRun = true;

    private long taskStartTime;
    private int refreshTime;
    private Timer timer;


    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public TutorialTask(Context context) {
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
                            refreshTime = GetInteger(map, ATTR_REFRESH_TIME, 35);
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
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
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

                        String[] location = GetString(map, ATTR_LOCATION).split(",");
                        itemHolder.displayRect = new Rect(Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(location[3]));

                        itemHolder.displayStart = GetInteger(map, ATTR_DISPLAY_START);
                        itemHolder.displayTime = GetInteger(map, ATTR_DISPLAY_TIME);
                        itemHolder.zIndex = GetInteger(map, ATTR_ZINDEX);

                        bmpFile = a_info.getDirectory().getChildFile(GetString(map, ATTR_IMAGE));
                        if (bmpFile.exists()) {
                            itemHolder.bitmap = BitmapFactory.decodeStream(bmpFile.getInputStream());
                        }
                        itemList.add(itemHolder);
                        LogUtils.d(TAG, "item added  " + itemHolder);
                    } catch (XMLFileException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                    }
                }
            }
        }


        Collections.sort(itemList, (lhs, rhs) -> {
            if (lhs.zIndex == rhs.zIndex) {
                return 0;
            }
            return lhs.zIndex < rhs.zIndex ? -1 : 1;
        });

        for (ItemHolder itemHolder : itemList) {
            LogUtils.d(TAG, itemHolder.toString());
        }

//        m_animateHandler = new AnimateHandler();
//        m_animateHandler.sendEmptyMessageDelayed(12868, 100);

        timer = new Timer();
        timer.schedule(new Looper(), refreshTime);

        return valid;
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        LogUtils.d(TAG, "draw, refreshTime =" + refreshTime);
        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint;
            myPaint = new Paint();
            myPaint.setStrokeWidth(10);
            Random random = new Random();
            for (ItemHolder itemHolder : itemList) {
                myPaint.setColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                canvas.drawRect(itemHolder.displayRect, myPaint);
            }
        }

        for (ItemHolder itemHolder : itemList) {
            if (System.currentTimeMillis() > taskStartTime + itemHolder.displayStart && System.currentTimeMillis() < taskStartTime + itemHolder.displayStart + itemHolder.displayTime) {
                canvas.drawBitmap(itemHolder.bitmap, null, itemHolder.displayRect, m_paint);
                LogUtils.d(TAG, "item to draw= " + itemHolder);
            }
        }

    }

    @Override
    public void Destory() throws IOException {
        timer.cancel();
        super.Destory();
    }

    private static class ItemHolder {
        public Bitmap bitmap;
        public int displayStart;
        public int displayTime;
        public Rect displayRect;
        public int zIndex;

        @Override
        public String toString() {
            return "ItemHolder{" +
                    "bitmap=" + bitmap +
                    ", displayStart=" + displayStart +
                    ", displayTime=" + displayTime +
                    ", displayRect=" + displayRect +
                    ", zIndex=" + zIndex +
                    '}';
        }
    }


    private class Looper extends TimerTask {

        @Override
        public void run() {
            if (firstRun) {
                taskStartTime = System.currentTimeMillis();
                firstRun = false;
            }
            LogUtils.d(TAG, "sending refresh request -> " + m_actHandler + "success = " + m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW));
            timer.schedule(new Looper(), refreshTime);
        }
    }

}
