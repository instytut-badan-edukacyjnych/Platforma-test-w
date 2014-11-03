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

package pl.edu.ibe.loremipsum.task.tpr.cordova.fourfields;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 24.07.14.
 */
public class FourFieldsBoardTask extends LookTask implements MediaPlayer.OnCompletionListener {
    private static final String TAG = FourFieldsBoardTask.class.getSimpleName();


    private static final String TAG_BUTTON = "button";
    private static final String ATTR_CONFIG = "config";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_ULSOUND = "ULSound";
    private static final String ATTR_URSOUND = "URSound";
    private static final String ATTR_LLSOUND = "LLSound";
    private static final String ATTR_LRSOUND = "LRSound";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_AUTO_CONTINUE = "autoContinue";
    private static final String ATTR_HOVER = "hover";

    private static final String TAG_FACES = "faces";
    private static final String ATTR_ID = "id";

    private static final Rect upperLeft = new Rect(103, 79, 410, 361);
    private static final Rect upperRight = new Rect(411, 79, 713, 361);
    private static final Rect lowerRight = new Rect(411, 395, 713, 679);
    private static final Rect lowerLeft = new Rect(103, 395, 410, 679);
    private static final int MESSAGE_NEXT_ENTRY = 1246;


    private static Rect touchCorrect = new Rect(770, 560, 940, 720);
    private static Rect touchWrong = new Rect(950, 560, 1120, 720);


    private Handler m_animateHandler = null;
    private int currentItemPosition = 0;
    private MediaPlayer player;
    private Location currentLocation;

    private boolean firstRun = true;
    private boolean firstLocation = true;

    private ArrayList<Integer> config;
    private String ULSound;
    private String URSound;
    private String LLSound;
    private String LRSound;
    private ArrayList<Face> facesList;
    private boolean last_answer;
    private Random random;
    private boolean startWasSend;
    private CurrentItemHandler currentItemHandler;

    private ArrayList<CurrentItemHandler> history;
    private String area;
    private boolean activateButtons;
    private boolean hide;
    private boolean autoContinue = false;
    private Bitmap positiveButtonHover;
    private Bitmap negativeButtonHover;
    private boolean positiveButtonPressed;
    private boolean negativeButtonPressed;
    private boolean dontDrawFace;
    private int lastItem = -5;
    private boolean drawButtons;
    private boolean clearButtons;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public FourFieldsBoardTask(Context context) {
        super(context);
    }


    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean valid = super.Create(a_info, a_handler, a_dir);
        history = new ArrayList<>();
        config = new ArrayList<>();
        area = a_info.m_area;
        random = new Random();
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
                            currentLocation = Location.valueOf(getString(map, ATTR_LOCATION, "UL"));
                            String[] configuration = GetString(map, ATTR_CONFIG).split(",");
                            for (String s : configuration) {
                                config.add(Integer.valueOf(s));
                            }
                            ULSound = GetString(map, ATTR_ULSOUND);
                            URSound = GetString(map, ATTR_URSOUND);
                            LLSound = GetString(map, ATTR_LLSOUND);
                            LRSound = GetString(map, ATTR_LRSOUND);
                            autoContinue = getString(map, ATTR_AUTO_CONTINUE, "false").equals("true");
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

                            String fileName = GetString(map, ATTR_HOVER);
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
                            String fileName = GetString(map, ATTR_HOVER);
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
                } else if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_FACES)) {
                    NodeList faces = list.item(i).getChildNodes();
                    facesList = new ArrayList<Face>();
                    Face face;
                    for (int j = 0; j < faces.getLength(); j++) {
                        NamedNodeMap map = faces.item(j).getAttributes();

                        face = new Face();
                        if (map != null) {
                            try {
                                face.id = GetInteger(map, ATTR_ID);
                                String fileName = GetString(map, ATTR_IMAGE);
                                VirtualFile faceFile = a_info.getDirectory().getChildFile(fileName);
                                if (faceFile.exists()) {
                                    // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                    // ale głownej planszy nie pomniejszamy
                                    face.bitmap = BitmapFactory.decodeStream(faceFile.getInputStream());
                                    LogUtils.d(TAG, "BitmapFactory: " + faceFile);
                                    facesList.add(face);
                                }
                            } catch (XMLFileException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(facesList, (lhs, rhs) -> {
            if (lhs.id == rhs.id) {
                return 0;
            }
            return lhs.id < rhs.id ? -1 : 1;
        });

        if (!startWasSend) {
            m_animateHandler = new AnimateHandler();
            m_animateHandler.sendEmptyMessageDelayed(currentItemPosition, 20);
            startWasSend = true;
        }
        if (getContext() instanceof TaskAct) {
            player = new MediaPlayer();
            player.setOnCompletionListener(this);
        }
        return valid;
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint = new Paint();
            myPaint.setStrokeWidth(10);
            myPaint.setColor(Color.rgb(0, 124, 0));
            canvas.drawRect(upperLeft, myPaint);

//            myPaint.setColor(Color.rgb(0, 0, 124));
//            canvas.drawRect(upperRight, myPaint);

            myPaint.setColor(Color.rgb(124, 0, 0));
            canvas.drawRect(lowerRight, myPaint);

//            myPaint.setColor(Color.rgb(124, 124, 124));
//            canvas.drawRect(lowerLeft, myPaint);

            myPaint.setColor(Color.rgb(0, 255, 0));
            canvas.drawRect(touchCorrect, myPaint);

            myPaint.setColor(Color.rgb(255, 0, 0));
            canvas.drawRect(touchWrong, myPaint);
        }


        if (positiveButtonPressed && drawButtons) {
            canvas.drawBitmap(positiveButtonHover, null, touchCorrect, m_paint);
            positiveButtonPressed = false;
        }
        if (negativeButtonPressed && drawButtons) {
            canvas.drawBitmap(negativeButtonHover, null, touchWrong, m_paint);
            negativeButtonPressed = false;
        }

        if (!clearButtons) {
            if (!hide) {
                if (!firstRun) {
                    if (currentItemPosition >= 0 && currentItemPosition < config.size()) {
                        canvas.drawBitmap(getFaceById(currentItemHandler.faceId).bitmap, null, getLocationRect(currentItemHandler.location), m_paint);
                        currentItemHandler.isCorrect = last_answer;
                        if (lastItem != currentItemPosition) {
                            lastItem = currentItemPosition;
                            playSound(getSoundByPosition(currentItemHandler.location));
                            currentItemHandler.displayStartTime = System.currentTimeMillis();
                        }
                        activateButtons = true;
                    }
                }
            }
        }
        clearButtons = false;
    }


    private Face getFaceById(int id) {
        for (Face face : facesList) {
            if (face.id == id) {
                return face;
            }
        }
        throw new RuntimeException("unknown id: " + id);
    }

    private Rect getLocationRect(Location currentLocation) {
        Rect returnRect;
        switch (currentLocation) {
            case UL:
                returnRect = new Rect(upperLeft.left + 30, upperLeft.top + 30, upperLeft.right - 30, upperLeft.bottom - 30);
                break;
            case UR:
                returnRect = new Rect(upperRight.left + 30, upperRight.top + 30, upperRight.right - 30, upperRight.bottom - 30);
                break;
            case LR:
                returnRect = new Rect(lowerRight.left + 30, lowerRight.top + 30, lowerRight.right - 30, lowerRight.bottom - 30);
                break;
            case LL:
                returnRect = new Rect(lowerLeft.left + 30, lowerLeft.top + 30, lowerLeft.right - 30, lowerLeft.bottom - 30);
                break;
            default:
                throw new RuntimeException("Unknown location: " + currentLocation);
        }
        return returnRect;
    }

    private Location getCurrentLocation() {
        if (firstLocation) {
            firstLocation = false;
            return currentLocation;
        }
        switch (currentLocation) {
            case UL:
                currentLocation = Location.UR;
                return Location.UR;
            case UR:
                currentLocation = Location.LR;
                return Location.LR;
            case LR:
                currentLocation = Location.LL;
                return Location.LL;
            case LL:
                currentLocation = Location.UL;
                return Location.UL;
        }
        currentLocation = Location.UL;
        return Location.UL;
    }

    private String getSoundByPosition(Location location) {
        switch (location) {
            case UL:
                return ULSound;
            case UR:
                return URSound;
            case LR:
                return LRSound;
            case LL:
                return LLSound;
            default:
                throw new RuntimeException("Unknown location: " + location);
        }

    }

    private void playSound(String name) {
        try {
            VirtualFile file = m_taskInfo.getDirectory().getChildFile(name);
            File cacheFile = new File(getTaskTempDir() + "/sound_" + name);
            CopyStream.copyStream(file.getInputStream(), new FileOutputStream(cacheFile)).execute().subscribe();
            LogUtils.v(TAG, "Writing to temp file: " + cacheFile.getAbsolutePath());
            LogUtils.v(TAG, "From " + file);
            try {
                FileInputStream fis = new FileInputStream(cacheFile);
                player.reset();
                player.setDataSource(fis.getFD());
                player.prepare();
                player.start();
            } catch (IOException e) {
                LogUtils.e(TAG, e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.stop();
        }
    }

    @Override
    public boolean TouchEvent(MotionEvent event) {
        ScreenTouched();

        boolean retValue = false;

        if (activateButtons) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();

                        if (touchCorrect.contains(x, y)) {
                            Log.d(TAG, "positive button clicked");
                            currentItemHandler.clickTime = System.currentTimeMillis();
                            currentItemHandler.userAnswer = true;
                            hide = true;
                            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                            dontDrawFace = true;
                            positiveButtonPressed = true;
                            m_animateHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_ENTRY, 100);
                            activateButtons = false;
                            drawButtons = true;
                        } else if (touchWrong.contains(x, y)) {
                            Log.d(TAG, "negative button clicked");
                            currentItemHandler.clickTime = System.currentTimeMillis();
                            currentItemHandler.userAnswer = false;
                            hide = true;
                            negativeButtonPressed = true;
                            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                            m_animateHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_ENTRY, 100);
                            activateButtons = false;
                            drawButtons = true;
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    positiveButtonPressed = false;
                    negativeButtonPressed = false;
                    m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                }
                break;
            }
        }
        return retValue;
    }

    // TPR1 supervision.js task_config function

    public int getFace(int type) {

        if (type == 5 || type == 1 || type == 3) {
            return answerChange(last_answer);
        } else if (type == 2 || type == 4) {
            Log.d(TAG, "zmiana odpowiedzi");
            if (last_answer) {
                last_answer = false;
            } else {
                last_answer = true;
            }
            return answerChange(last_answer);

        }
        throw new RuntimeException("accepted range: 1-5: " + type);
    }

    // TPR1 supervision.js answerChange function
    public int answerChange(boolean answer) {
        if (answer) {
            if (((currentItemPosition + 3) % 4) < 2) {
                return random.nextBoolean() ? 3 : 4;
            } else {
                return random.nextBoolean() ? 2 : 3;
            }
        } else {
            if (((currentItemPosition + 3) % 4) < 2) {
                return random.nextBoolean() ? 1 : 2;
            } else {
                return random.nextBoolean() ? 1 : 4;
            }
        }
    }

    @Override
    public void Destory() throws IOException {
        super.Destory();
        m_animateHandler = null;
    }

    public ArrayList<CurrentItemHandler> getData() {
        currentItemHandler.endDisplayTime = System.currentTimeMillis();
        return history;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }


    private enum Location {
        UL, UR, LR, LL
    }

    public static class CurrentItemHandler {

        public Location location;
        public Integer configItem;
        public int faceId;
        public boolean isCorrect;
        public boolean userAnswer;
        public long displayStartTime;
        public long clickTime;
        public long endDisplayTime;
        public String area;

        @Override
        public String toString() {
            return "CurrentItemHandler{" +
                    "location=" + location +
                    ", configItem=" + configItem +
                    ", faceId=" + faceId +
                    ", isCorrect=" + isCorrect +
                    ", userAnswer=" + userAnswer +
                    ", displayStartTime=" + displayStartTime +
                    ", clickTime=" + clickTime +
                    ", endDisplayTime=" + endDisplayTime +
                    ", area='" + area + '\'' +
                    '}';
        }
    }

    private class Face {
        public int id;
        public Bitmap bitmap;
    }

    /**
     * Klasa obsługi zdarzeń związanych z animacja
     */
    private class AnimateHandler extends Handler {

        /**
         * Constructor
         */
        public AnimateHandler() {

            super();
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            clearButtons = true;
            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);


            hide = false;


            if (currentItemPosition != -1) {
                if (firstRun) {
                    currentItemPosition = 0;
                    firstRun = false;
                } else {
                    currentItemPosition++;

                    if (currentItemPosition >= config.size()) {
                        currentItemPosition = -1;
                        if (autoContinue) {
                            ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 150);
                        }
                    }
                }
            }

            /* Calculate all needed things to render item */
            if (currentItemPosition != -1) {
                if (currentItemHandler != null) {
                    currentItemHandler.endDisplayTime = System.currentTimeMillis();
                }
                currentItemHandler = new CurrentItemHandler();
                currentItemHandler.location = getCurrentLocation();
                currentItemHandler.configItem = config.get(currentItemPosition);

                currentItemHandler.faceId = getFace(currentItemHandler.configItem);
                LogUtils.d(TAG, "currentItemPosition:" + currentItemPosition + " configItem: " + currentItemHandler.configItem + " faceId: "
                        + currentItemHandler.faceId + " lastAnswer: " + last_answer + " location: " + currentItemHandler.location + " timestamp: " + System.currentTimeMillis());
                currentItemHandler.area = area;
                history.add(currentItemHandler);

            }

            m_actHandler.sendEmptyMessageDelayed(TASK_MESS_REFRESH_VIEW, 500);
            drawButtons = false;
            LogUtils.d(TAG, "current index: " + currentItemPosition);
        }
    }
}
