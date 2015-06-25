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

package pl.edu.ibe.loremipsum.task.tpr.actionscript.grid;

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
import java.util.Random;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.BaseTask;
import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.loremipsum.tools.tpr.FakeRandom;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 07.08.14.
 */
public class GridTask extends LookTask {
    private static final String TAG = GridTask.class.getSimpleName();

    private static final String ATTR_GRID_POSITION = "gridPosition";
    private static final String ATTR_GRID_COLUMNS = "gridColumns";
    private static final String ATTR_GRID_ROWS = "girdRows";


    private static final String ATTR_TIMEOUT = "timeout";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_SHOW_CORRECT = "showCorrectAnswer";

    private static final String TAG_ENTRY = "entry";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_IMAGE_SHADOW = "imageShadow";
    public TaskMode taskMode;
    private Rect gridContainer;
    private int gridColumns;
    private int gridRows;
    private Integer containerLeft;
    private Integer containerTop;
    private Integer containerRight;
    private Integer containerBottom;
    private ArrayList<Cell> gridCells;
    private ArrayList<Rect> hintList;
    private boolean showCorrectAnswer;
    private AnimateHandler m_animateHandler;
    private int currentItemIndex;
    private HashMap<Integer, Integer> usedFields;
    private Random random;
    private boolean allowRedrawRequest;
    private boolean firstRun = true;
    private int touchCounter;
    private HashMap<Integer, Cell> touches;
    private long startTime;
    private ArrayList<Double> calculatedDistancesFromClosestProperPosition;
    private String area;
    private HashMap<String, Bitmap> bitmapCache;
    private ArrayList<ItemHolder> itemList;
    private Paint transparentPaint;
    private boolean nextTaskRequested;
    private long answerStartTime;

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public GridTask(Context context) {
        super(context);
    }


    private Tuple.Two<Integer, Integer> calculatePosition(int position) {
        int decimal = position / 10;
        int ones = position % 10;

        int x = ones + 1;
        int y = 10 - decimal;


        return Tuple.create(x, y);
    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean valid = super.Create(a_info, a_handler, a_dir);
        bitmapCache = new HashMap<>();
        area = a_info.m_area;
        if (m_document != null) {
            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        try {
                            String fileName = GetString(map, XML_DETAILS_TASK_MAIN);
                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);
                            String[] location = GetString(map, ATTR_GRID_POSITION).split(",");
                            containerLeft = Integer.valueOf(location[0]);
                            containerTop = Integer.valueOf(location[1]);
                            containerRight = Integer.valueOf(location[2]);
                            containerBottom = Integer.valueOf(location[3]);
                            gridContainer = new Rect(containerLeft, containerTop, containerRight, containerBottom);

                            gridColumns = GetInteger(map, ATTR_GRID_COLUMNS);
                            gridRows = GetInteger(map, ATTR_GRID_ROWS);

                            String mode = getString(map, ATTR_MODE, "randomString");

                            if (mode.equals("tutorial")) {
                                taskMode = TaskMode.tutorial;
                            } else if (mode.equals("demo")) {
                                taskMode = TaskMode.demo;
                            } else {
                                taskMode = TaskMode.normal;
                            }


                            showCorrectAnswer = getString(map, ATTR_SHOW_CORRECT, "false").equals("true");
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
                list = list.item(a_info.m_groupIndex).getChildNodes();
                itemList = new ArrayList<>();
                ItemHolder itemHolder;
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getLocalName() != null && list.item(i).getLocalName().equals(TAG_ENTRY)) {
                        try {
                            itemHolder = new ItemHolder();
                            NamedNodeMap map = list.item(i).getAttributes();
                            itemHolder.timeout = Long.valueOf(GetString(map, ATTR_TIMEOUT));
                            itemHolder.image = getCachedBitmap(map, ATTR_IMAGE, a_info);
                            itemHolder.imageShadow = getCachedBitmap(map, ATTR_IMAGE_SHADOW, a_info);

                            itemList.add(itemHolder);
                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            }
        }
        touches = new HashMap<>();
        calculatedDistancesFromClosestProperPosition = new ArrayList<>();
        hintList = new ArrayList<>();
        gridCells = new ArrayList<>();
        int itemWidth = gridContainer.width() / gridColumns;
        int itemHeight = gridContainer.height() / gridRows;
        int currentRow = 0;
        int currentColumn = 0;
        Cell cell;

        Rect rect;

        for (int i = 0; i < gridColumns * gridRows; i++) {

            if (currentColumn == 0) {
                rect = new Rect(containerLeft - itemWidth * 2,
                        containerTop + itemHeight * currentRow,
                        containerLeft - itemWidth,
                        containerTop + itemHeight * currentRow + itemHeight);
                hintList.add(rect);
            }

            cell = new Cell();
            rect = new Rect(containerLeft + itemWidth * currentColumn,
                    containerTop + itemHeight * currentRow,
                    containerLeft + itemWidth * currentColumn + itemWidth,
                    containerTop + itemHeight * currentRow + itemHeight);
            cell.rect = rect;
            cell.x = currentColumn;
            cell.y = currentRow;
            gridCells.add(cell);
            currentColumn++;
            if (currentColumn >= gridColumns) {
                currentColumn = 0;
                currentRow++;
            }
        }


        //Dla przypomnienia.
        if (!BuildConfig.ENABLE_FAKE_RANDOM) {
            random = new Random();
        }

        m_animateHandler = new AnimateHandler();
        m_animateHandler.sendEmptyMessageDelayed(0, 1000);

        usedFields = new HashMap<>();

        transparentPaint = new Paint();
        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(Color.argb(100, 0, 255, 0));


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

    @Override
    public void StartTask() {
        super.StartTask();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void Draw(Canvas canvas) {
        super.Draw(canvas);

        if (BuildConfig.ENABLE_TEST_DRAWINGS) {
            Paint myPaint = new Paint();
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(5);
            Random random = new Random();

            myPaint.setColor(Color.rgb(125, 125, 125));
            canvas.drawRect(gridContainer, myPaint);

            for (Cell gridCell : gridCells) {
                myPaint.setColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                canvas.drawRect(gridCell.rect, myPaint);
            }

            for (Rect gridCell : hintList) {
                myPaint.setColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                canvas.drawRect(gridCell, myPaint);
            }

        }

        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setStrokeWidth(5);

        for (int i1 = 0; i1 < gridCells.size(); i1++) {
            Cell gridCell = gridCells.get(i1);
            m_paint.setColor(Color.rgb(0, 0, 0));
            canvas.drawRect(gridCell.rect, m_paint);
            if (BuildConfig.ENABLE_GRID_POSITIONS) {
                m_paint.setStrokeWidth(1);
                transparentPaint.setColor(Color.argb(100, 0, 255, 255));
                for (Integer integer : usedFields.values()) {
                    canvas.drawRect(gridCells.get(integer).rect, transparentPaint);
                }
                transparentPaint.setColor(Color.argb(100, 0, 255, 0));
                String s = "(" + calculatePosition(i1).first + "," + calculatePosition(i1).second + ")";
                setTextSizeForWidth(m_paint, gridCell.rect.width() * 2, s);
                canvas.drawText(s, gridCell.rect.left, gridCell.rect.exactCenterY(), m_paint);
                m_paint.setStrokeWidth(5);
            }

        }

        if (!firstRun) {
            int randomPosition;
            if (currentItemIndex == -1 || currentItemIndex == -2) {
                if (answerStartTime != 0) {
                    answerStartTime = System.currentTimeMillis();
                }
                if (touches.values().size() > 0) {
                    for (int i = 0; i < touches.values().size(); i++) {
                        canvas.drawBitmap(bitmapCache.get(itemList.get(i).image), null, shrinkImageToFitCell(touches.get(i).rect), m_paint);
                    }
                }

                for (int i = 0; i < touches.size(); i++) {
                    canvas.drawBitmap(bitmapCache.get(itemList.get(i).imageShadow), null, shrinkImageToFitCell(hintList.get(i)), m_paint);
                }

                for (int i = touches.size(); i < itemList.size(); i++) {
                    canvas.drawBitmap(bitmapCache.get(itemList.get(i).image), null, shrinkImageToFitCell(hintList.get(i)), m_paint);
                }
                if (currentItemIndex == -2) {
                    int i = 0;
                    for (Integer cell : usedFields.values()) {
                        canvas.drawBitmap(bitmapCache.get(itemList.get(i).image), null, shrinkImageToFitCell(gridCells.get(cell).rect), m_paint);
                        canvas.drawRect(gridCells.get(cell).rect, transparentPaint);
                        i++;
                    }
                }


            } else {
                if (usedFields.containsKey(currentItemIndex)) {
                    randomPosition = usedFields.get(currentItemIndex);
                } else {
                    do {
                        if (taskMode == TaskMode.demo && random == null) {
                            random = new Random();
                        }

                        randomPosition = random.nextInt(gridCells.size());
                    } while (usedFields.values().contains(randomPosition));
                    usedFields.put(currentItemIndex, randomPosition);
                }


                canvas.drawBitmap(bitmapCache.get(itemList.get(currentItemIndex).image), null, shrinkImageToFitCell(gridCells.get(randomPosition).rect), m_paint);

                if (currentItemIndex >= 0 && currentItemIndex < itemList.size()) {
                    if (allowRedrawRequest) {
                        m_animateHandler.sendEmptyMessageDelayed(currentItemIndex, itemList.get(currentItemIndex).timeout);
                        allowRedrawRequest = false;
                    }
                } else {
                    currentItemIndex = -1;
                    if (allowRedrawRequest) {
                        m_animateHandler.sendEmptyMessageDelayed(-1, itemList.get(currentItemIndex).timeout);
                        allowRedrawRequest = false;
                    }
                }
            }
        }
    }

    private void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        paint.setTextSize(desiredTextSize / 2);
    }


    private Rect shrinkImageToFitCell(Rect cell) {
        Rect rect = new Rect(cell.left + 5, cell.top + 5, cell.right - 5, cell.bottom - 5);
        return rect;
    }

    @Override
    public boolean TouchEvent(MotionEvent event) {
        boolean returnValue = false;
        Cell touchedGridCell = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (currentItemIndex == -1 && touches.size() < itemList.size()) {
                    int last = event.getPointerCount() - 1;
                    if (last >= 0) {
                        Float xPos = new Float(event.getX(last));
                        Float yPos = new Float(event.getY(last));
                        int x = xPos.intValue();
                        int y = yPos.intValue();


                        for (int i = 0; i < gridCells.size(); i++) {
                            if (gridCells.get(i).rect.contains(x, y)) {
                                touchedGridCell = gridCells.get(i);
                                returnValue = true;
                                break;
                            }
                        }

                        if (touchedGridCell != null && !touches.values().contains(touchedGridCell)) {
                            touches.put(touchCounter, touchedGridCell);


                            Double distance = -1.0;
// Sprawdzamy tylko odpowiedni indeks wyświetlonego obrazka dla danego kliknięcia
                            distance = calculateDistance(touchedGridCell, gridCells.get(usedFields.get(touchCounter)));

                            calculatedDistancesFromClosestProperPosition.add(distance);
                            touchCounter++;
                            m_animateHandler.sendEmptyMessageDelayed(currentItemIndex, 0);
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (!nextTaskRequested) {
                    if (taskMode == TaskMode.normal && touches.size() == itemList.size()) {
                        ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 500);
                        nextTaskRequested = true;
                        LogUtils.d(TAG, "next task request: touches = " + touches + "  itemList = " + itemList + " tutorial = " + taskMode);
                    } else if ((taskMode == TaskMode.tutorial || taskMode == TaskMode.demo) && touches.size() == itemList.size()) {
                        currentItemIndex = -2;
                        m_animateHandler.sendEmptyMessageDelayed(currentItemIndex, 250);
                        ((TaskAct) getContext()).getTaskHandler().sendEmptyMessageDelayed(BaseTask.TASK_MESS_NEXT_TASK, 750);
                        LogUtils.d(TAG, "next task request: touches = " + touches + "  itemList = " + itemList + " tutorial = " + taskMode);
                        nextTaskRequested = true;
                    }
                }
            }
            break;
        }
        return returnValue;
    }

    private double calculateDistance(Cell touchedGridCell, Cell usedCells) {
        double x = Math.abs(usedCells.x - touchedGridCell.x);
        double y = Math.abs(usedCells.y - touchedGridCell.y);
        return Math.round(100.0 * (Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0)))) / 100.0;
    }

    public GridTaskResult getData() {
        LogUtils.e(TAG, "GET DATA!");
        GridTaskResult gridTaskResult = new GridTaskResult();
        gridTaskResult.taskStart = startTime;
        gridTaskResult.results = calculatedDistancesFromClosestProperPosition;
        gridTaskResult.answerTime = System.currentTimeMillis() - answerStartTime;
        gridTaskResult.area = area;
        gridTaskResult.isTutorial = TaskMode.tutorial.equals(taskMode);
        gridTaskResult.numOfItems = usedFields.size();
        return gridTaskResult;
    }

    public void setFakeRandomMode(int taskId, boolean isAbstract) {
        if (BuildConfig.ENABLE_FAKE_RANDOM) {
            if (isAbstract) {
                if (taskId == 1) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_TUTORIAL_1_ABSTRACT);
                } else if (taskId == 2) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_TUTORIAL_2_ABSTRACT);
                } else if (taskId == 3) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_PROPER_ABSTRACT);
                }
            } else {
                if (taskId == 1) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_TUTORIAL_1_NORMAL);
                } else if (taskId == 2) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_TUTORIAL_2_NORMAL);
                } else if (taskId == 3) {
                    random = new FakeRandom(FakeRandom.FakeRandomMode.TPR2_TASK_7_PROPER_NORMAL);
                }
            }
        }
    }

    private enum TaskMode {
        normal, demo, tutorial
    }

    private static class ItemHolder {
        public String image;
        public String imageShadow;
        public long timeout;


        @Override
        public String toString() {
            return "ItemHolder{" +
                    "image='" + image + '\'' +
                    ", imageShadow='" + imageShadow + '\'' +
                    ", timeout=" + timeout +
                    '}';
        }
    }

    private class Cell {
        public Rect rect;
        public int x;
        public int y;

        @Override
        public String toString() {
            return "Cell{" +
                    "rect=" + rect +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
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
            allowRedrawRequest = true;
            if (firstRun) {
                currentItemIndex = 0;
                firstRun = false;
            } else if (currentItemIndex != -1 && currentItemIndex != -2) {
                currentItemIndex++;
                if (currentItemIndex >= itemList.size()) {
                    currentItemIndex = -1;
                }
            }

            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
        }
    }


    public class GridTaskResult {
        public long taskStart;
        public long taskEnd;
        public long time;

        public long answerTime;

        public ArrayList<Double> results = new ArrayList<>();
        public int numOfItems;
        public String area;
        public boolean isTutorial;

        @Override
        public String toString() {
            return "GridTaskResult{" +
                    "taskStart=" + taskStart +
                    ", taskEnd=" + taskEnd +
                    ", time=" + time +
                    ", results=" + results +
                    ", numOfItems=" + numOfItems +
                    ", area='" + area + '\'' +
                    ", isTutorial=" + isTutorial +
                    '}';
        }
    }
}