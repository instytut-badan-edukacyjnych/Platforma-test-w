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

package pl.edu.ibe.loremipsum.task.tpr.actionscript.fourfields;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 24.07.14.
 */
public class FourFieldsBoardTask extends LookTask {
    private static final String TAG = FourFieldsBoardTask.class.getSimpleName();


    private static final String TAG_BUTTON = "button";
    private static final String ATTR_NORMAL = "normal";
    private static final String ATTR_PRESSED = "pressed";
    private static final String ATTR_GOOD_ANSWER = "goodAnswer";
    private static final String ATTR_WRONG_ANSWER = "wrongAnswer";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_COORDINATES = "coordinates";
    private static final String ATTR_FONT_SIZE = "fontSize";


    private static final String TAG_ENTRY = "entry";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_TEXT = "text";
    private static final String ATTR_IS_CORRECT = "isCorrect";
    private static final String ATTR_QUESTION = "question";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_TIMEOUT = "timeout";

    private static final String ATTR_COORDINATES_UL = "coordinatesUL";
    private static final String ATTR_COORDINATES_UR = "coordinatesUR";
    private static final String ATTR_COORDINATES_LR = "coordinatesLR";
    private static final String ATTR_COORDINATES_LL = "coordinatesLL";
    private static final String ATTR_COORDINATES_TEXT = "coordinatesText";
    private static final String ATTR_IS_TUTORIAL = "isTutorial";
    private static final String ATTR_MARK_START_ENTRY = "markStartEntry";

    private static final int MESSAGE_NEXT_ENTRY = 14547;
    private static final int MESSAGE_CLEAR_SCREEN = 12346;


    private Rect upperLeft;
    private Rect upperRight;
    private Rect lowerRight;
    private Rect lowerLeft;
    private Rect textRect;

    private float fontSize;

    private ArrayList<AnswerButton> answerButtons;
    private ArrayList<Entry> entries;


    private boolean isTutorial;
    private int markStartEntry;

    private AnimateHandler animateHandler;
    private int currentEntryIndex;
    private Entry currentEntry;


    private CurrentPressedButton currentPressedButton;
    private boolean allowPress;


    private HashMap<String, Bitmap> bitmapCache;
    private String area;

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
        area = a_info.m_area;

        answerButtons = new ArrayList<>();
        entries = new ArrayList<>();
        animateHandler = new AnimateHandler();
        bitmapCache = new HashMap<>();
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
                            String[] coordinates = getString(map, ATTR_COORDINATES_LL, "70,400,390,710").split(",");
                            lowerLeft = new Rect(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]), Integer.valueOf(coordinates[2]), Integer.valueOf(coordinates[3]));

                            coordinates = getString(map, ATTR_COORDINATES_UL, "70,50,390,350").split(",");
                            upperLeft = new Rect(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]), Integer.valueOf(coordinates[2]), Integer.valueOf(coordinates[3]));

                            coordinates = getString(map, ATTR_COORDINATES_UR, "400,50,720,350").split(",");
                            upperRight = new Rect(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]), Integer.valueOf(coordinates[2]), Integer.valueOf(coordinates[3]));

                            coordinates = getString(map, ATTR_COORDINATES_LR, "400,400,720,710").split(",");
                            lowerRight = new Rect(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]), Integer.valueOf(coordinates[2]), Integer.valueOf(coordinates[3]));

                            coordinates = getString(map, ATTR_COORDINATES_TEXT, "725,50,1130,250").split(",");
                            textRect = new Rect(Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]), Integer.valueOf(coordinates[2]), Integer.valueOf(coordinates[3]));

                            isTutorial = getString(map, ATTR_IS_TUTORIAL, "false").equals("true");

                            markStartEntry = GetInteger(map, ATTR_MARK_START_ENTRY, 0);

                            fontSize = Float.valueOf(getString(map, ATTR_FONT_SIZE, "48.0"));

                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            }

            list = list.item(a_info.m_groupIndex).getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_BUTTON)) {
                    AnswerButton answerButton = new AnswerButton();
                    try {
                        NamedNodeMap map = list.item(i).getAttributes();
                        if (GetString(map, ATTR_MODE).equals("positive")) {
                            String[] location = getString(map, ATTR_COORDINATES, "770,560,940,720").split(",");
                            answerButton.rect = new Rect(Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(location[3]));
                            answerButton.isCorrect = true;
                        } else {
                            String[] location = getString(map, ATTR_COORDINATES, "950,560,1120,720").split(",");
                            answerButton.rect = new Rect(Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(location[3]));
                            answerButton.isCorrect = false;
                        }
                        answerButton.normal = getBitmap(map, ATTR_NORMAL, a_info);
                        answerButton.goodAnswer = getBitmap(map, ATTR_GOOD_ANSWER, a_info);
                        answerButton.wrongAnswer = getBitmap(map, ATTR_WRONG_ANSWER, a_info);
                        answerButton.pressed = getBitmap(map, ATTR_PRESSED, a_info);
                        answerButtons.add(answerButton);
                    } catch (XMLFileException e) {
                        valid = false;
                        e.printStackTrace();
                        LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                    }
                } else if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_ENTRY)) {
                    NamedNodeMap attributes = null;
                    try {
                        attributes = list.item(i).getAttributes();
                        Entry entry = new Entry();
                        entry.isCorrect = getString(attributes, ATTR_IS_CORRECT, "false").equals("true");
                        entry.location = Location.valueOf(GetString(attributes, ATTR_LOCATION));
                        entry.question = GetString(attributes, ATTR_QUESTION);
                        entry.isTutorial = isTutorial;
                        try {
                            entry.text = GetString(attributes, ATTR_TEXT);
                        } catch (Exception e) {
                            LogUtils.d(TAG, "no text, searching for image");
                            entry.image = getCachedBitmap(attributes, ATTR_IMAGE, a_info);
                        }
                        entry.timeout = Long.valueOf(GetString(attributes, ATTR_TIMEOUT));
                        entry.area = area;
                        entries.add(entry);
                    } catch (Exception e) {
                        valid = false;
                        e.printStackTrace();
                        LogUtils.e(TAG, "incorrect entry: " + attributes, e);
                    }


                }
            }
        }

        animateHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_ENTRY, 20);

        return valid;
    }

    private String getCachedBitmap(NamedNodeMap map, String attrNormal, TaskInfo a_info) throws XMLFileException, IOException {
        String fileName = GetString(map, attrNormal);
        if (bitmapCache.get(fileName) == null) {
            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

            if (bmpFile.exists()) {
                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                bitmapCache.put(fileName, BitmapFactory.decodeStream(bmpFile.getInputStream()));
            }
        }
        return fileName;
    }

    private Bitmap getBitmap(NamedNodeMap map, String attrNormal, TaskInfo a_info) throws XMLFileException, IOException {
        String fileName = GetString(map, attrNormal);
        VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

        if (bmpFile.exists()) {
            // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
            // ale głownej planszy nie pomniejszamy
            LogUtils.d(TAG, "BitmapFactory: " + fileName);
            return BitmapFactory.decodeStream(bmpFile.getInputStream());
        }


        LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
        throw new IOException("no image: " + attrNormal + " -> " + fileName);
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);
        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint = new Paint();
            myPaint.setStrokeWidth(10);
            myPaint.setColor(Color.rgb(0, 124, 0));
            canvas.drawRect(upperLeft, myPaint);

            myPaint.setColor(Color.rgb(0, 0, 124));
            canvas.drawRect(upperRight, myPaint);

            myPaint.setColor(Color.rgb(124, 0, 0));
            canvas.drawRect(lowerRight, myPaint);

            myPaint.setColor(Color.rgb(124, 124, 124));
            canvas.drawRect(lowerLeft, myPaint);


            for (AnswerButton answerButton : answerButtons) {
                myPaint.setColor(Color.rgb(answerButton.isCorrect ? 0 : 255, answerButton.isCorrect ? 255 : 0, 0));
                canvas.drawRect(answerButton.rect, myPaint);
            }
            myPaint.setColor(Color.rgb(168, 124, 255));
            canvas.drawRect(textRect, myPaint);
        }
        if (currentEntry != null) {
            if (currentEntry.image != null) {
                canvas.drawBitmap(bitmapCache.get(currentEntry.image), null, shrinkImages(getLocationRect(currentEntry.location)), m_paint);
            } else if (currentEntry.text != null && !currentEntry.text.equals("")) {
                Rect rect = getLocationRect(currentEntry.location);

                setTextSizeForWidth(m_paint, rect.width(), currentEntry.text);

                canvas.drawText(currentEntry.text, rect.exactCenterX() - rect.width() / 4, rect.exactCenterY() + rect.width() / 8, m_paint);
            }
            m_paint.setTextSize(fontSize);
            canvas.drawText(currentEntry.question, textRect.left, textRect.exactCenterY(), m_paint);
        }
        for (AnswerButton answerButton : answerButtons) {
            canvas.drawBitmap(answerButton.normal, null, answerButton.rect, m_paint);
        }

        if (currentPressedButton != null && currentPressedButton.bitmap != null) {
            canvas.drawBitmap(currentPressedButton.bitmap, null, currentPressedButton.rect, m_paint);
        }
    }

    private Rect shrinkImages(Rect locationRect) {
        return new Rect(locationRect.left + 20, locationRect.top + 20, locationRect.right - 20, locationRect.bottom - 20);
    }

    private void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        paint.setTextSize(desiredTextSize / 2);
    }


    private Rect getLocationRect(Location location) {
        switch (location) {
            case LL:
                return lowerLeft;
            case LR:
                return lowerRight;
            case UL:
                return upperLeft;
            case UR:
                return upperRight;
            default:
                throw new RuntimeException("Unknown location: " + location.toString());
        }
    }

    @Override
    public boolean TouchEvent(MotionEvent event) {
        ScreenTouched();
        boolean returnValue = false;
        if (allowPress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();
                        for (AnswerButton answerButton : answerButtons) {
                            if (answerButton.rect.contains(x, y)) {
                                animateHandler.removeMessages(MESSAGE_NEXT_ENTRY);
                                currentPressedButton = new CurrentPressedButton();
                                currentPressedButton.rect = answerButton.rect;

                                if (currentEntry != null) {
                                    if (currentEntry.isCorrect) {
                                        if (answerButton.isCorrect) {
                                            currentEntry.wasAnswerCorrect = true;
                                            if (isTutorial) {
                                                currentPressedButton.bitmap = answerButton.goodAnswer;
                                            } else {
                                                currentPressedButton.bitmap = answerButton.pressed;
                                            }
                                        } else {
                                            currentEntry.wasAnswerCorrect = false;
                                            if (isTutorial) {
                                                currentPressedButton.bitmap = answerButton.wrongAnswer;
                                            } else {
                                                currentPressedButton.bitmap = answerButton.pressed;
                                            }
                                        }
                                    } else {
                                        if (answerButton.isCorrect) {
                                            currentEntry.wasAnswerCorrect = false;
                                            if (isTutorial) {
                                                currentPressedButton.bitmap = answerButton.wrongAnswer;
                                            } else {
                                                currentPressedButton.bitmap = answerButton.pressed;
                                            }
                                        } else {
                                            currentEntry.wasAnswerCorrect = true;
                                            if (isTutorial) {
                                                currentPressedButton.bitmap = answerButton.goodAnswer;
                                            } else {
                                                currentPressedButton.bitmap = answerButton.pressed;
                                            }
                                        }


                                    }
                                    currentEntry.touchTime = System.currentTimeMillis() - currentEntry.displayStartTime;
                                }
                                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                                returnValue = true;
                            }
                        }
                    }
                }
                break;

                case MotionEvent.ACTION_UP: {
                    int last = event.getPointerCount() - 1;
                    Float xPos = new Float(event.getX(last));
                    Float yPos = new Float(event.getY(last));
                    int x = xPos.intValue();
                    int y = yPos.intValue();
                    for (AnswerButton answerButton : answerButtons) {
                        if (answerButton.rect.contains(x, y)) {
                            currentPressedButton = null;
                            m_actHandler.sendEmptyMessageDelayed(TASK_MESS_REFRESH_VIEW, 20);
                            animateHandler.sendEmptyMessage(MESSAGE_CLEAR_SCREEN);
                            allowPress = false;
                        }
                    }
                }
                break;
            }
        }
        return returnValue;
    }

    ArrayList<FourFieldsBoardTask.Entry> getData() {
        return entries;
    }

    public enum Location {
        UL, UR, LR, LL
    }

    private class CurrentPressedButton {
        public Rect rect;
        public Bitmap bitmap;
    }


    private class AnswerButton {
        public Rect rect;
        public Bitmap normal;
        public Bitmap pressed;
        public Bitmap goodAnswer;
        public Bitmap wrongAnswer;
        public boolean isCorrect;
    }

    public class Entry {
        public String image;
        public String text;
        public Location location;
        public String question;
        public long timeout;
        public boolean isCorrect;

        public String area;

        public long displayStartTime;
        public long displayEndTime;
        public long touchTime = -7;
        public long displayTime;
        public boolean wasAnswerCorrect;
        public boolean isTutorial;
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
            if (msg.what == MESSAGE_CLEAR_SCREEN) {
                if (currentEntry != null) {
                    currentEntry.displayEndTime = System.currentTimeMillis();
                    currentEntry.displayTime = currentEntry.displayEndTime - currentEntry.displayStartTime;
                }
                currentEntry = null;
                m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);


                animateHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_ENTRY, 250);

            } else if (msg.what == MESSAGE_NEXT_ENTRY) {

                if (currentEntry != null) {
                    currentEntry.displayEndTime = System.currentTimeMillis();
                    currentEntry.displayTime = currentEntry.displayEndTime - currentEntry.displayStartTime;
                }
                currentPressedButton = null;
                allowPress = true;
                if (currentEntryIndex < entries.size()) {
                    currentEntry = entries.get(currentEntryIndex);
                    currentEntry.displayStartTime = System.currentTimeMillis();

                    m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                    currentEntryIndex++;


                    animateHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_ENTRY, currentEntry.timeout);
                } else {
                    if (currentEntry != null) {
                        m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
                    }
                    currentEntry = null;
                }
            }
        }
    }
}
