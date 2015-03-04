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

package pl.edu.ibe.loremipsum.task.tpr.cordova.grid;

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
import android.view.MotionEvent;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import pl.edu.ibe.loremipsum.tablet.task.TaskAct;
import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.CopyStream;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.BuildConfig;

/**
 * Created by adam on 07.08.14.
 */
public class GridTask extends LookTask {
    private static final String TAG = GridTask.class.getSimpleName();

    private static final String ATTR_GRID_POSITION = "gridPosition";
    private static final String ATTR_GRID_COLUMNS = "gridColumns";
    private static final String ATTR_GRID_ROWS = "girdRows";
    private static final String ATTR_INFO_SOUND = "infoSound";

    private static final String ATTR_TIMEOUT = "timeout";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_SHOW_CORRECT = "showCorrectAnswer";

    private static final String TAG_ENTRY = "entry";
    private static final String ATTR_IMAGE = "image";
    private static final String ATTR_IMAGE_SHADOW = "imageShadow";


    private Rect gridContainer;
    private int gridColumns;
    private int gridRows;

    private Integer containerLeft;
    private Integer containerTop;
    private Integer containerRight;
    private Integer containerBottom;

    private ArrayList<Cell> gridCells;
    private ArrayList<Rect> hintList;

    private boolean tutorial;
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

    private String infoSound;

    private ArrayList<Double> calculatedDistancesFromClosestProperPosition;
    private String area;

    private HashMap<String, Bitmap> bitmapCache;

    private ArrayList<ItemHolder> itemList;
    private MediaPlayer player;
    private Paint transparentPaint;


    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public GridTask(Context context) {
        super(context);
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

                            infoSound = GetString(map, ATTR_INFO_SOUND);

                            tutorial = getString(map, ATTR_MODE, "randomString").equals("tutorial");
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

        random = new Random();

        m_animateHandler = new AnimateHandler();
        m_animateHandler.sendEmptyMessageDelayed(0, 1000);

        usedFields = new HashMap<>();

        player = ((TaskAct) getContext()).getPlayer();

        transparentPaint = new Paint();
        transparentPaint.setStyle(Paint.Style.FILL);
        transparentPaint.setColor(Color.argb(100, 0, 255, 255));


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

        if (BuildConfig.ENABLE_GRID_POSITIONS) {
            for (Integer integer : usedFields.values()) {
                canvas.drawRect(gridCells.get(integer).rect, transparentPaint);
            }
        }

        if (!firstRun) {
            int randomPosition;
            if (currentItemIndex == -1) {

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
            } else {
                if (usedFields.containsKey(currentItemIndex)) {
                    randomPosition = usedFields.get(currentItemIndex);
                } else {
                    do {
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

    private Rect shrinkImageToFitCell(Rect cell) {
        Rect rect = new Rect(cell.left + 7, cell.top + 7, cell.right - 7, cell.bottom - 7);
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
                            touchCounter++;

                            ArrayList<Double> distances = new ArrayList<>();

                            for (Integer integer : usedFields.values()) {
                                distances.add(calculateDistance(touchedGridCell, gridCells.get(integer)));
                            }

                            calculatedDistancesFromClosestProperPosition.add(Collections.min(distances));

                            m_animateHandler.sendEmptyMessageDelayed(currentItemIndex, 0);
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {

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
        GridTaskResult gridTaskResult = new GridTaskResult();
        gridTaskResult.taskStart = startTime;

        while (calculatedDistancesFromClosestProperPosition.size() < itemList.size()) {
            calculatedDistancesFromClosestProperPosition.add(10d);
        }

        gridTaskResult.results = calculatedDistancesFromClosestProperPosition;
        gridTaskResult.area = area;
        gridTaskResult.numOfItems = itemList.size();
        return gridTaskResult;
    }


    private static class ItemHolder {
        public String image;
        public String imageShadow;
        public long timeout;
    }

    private class Cell {
        public Rect rect;
        public int x;
        public int y;
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
            } else if (currentItemIndex != -1) {
                currentItemIndex++;
                if (currentItemIndex >= itemList.size()) {
                    currentItemIndex = -1;
                    playSound(infoSound);
                }
            }
            m_actHandler.sendEmptyMessage(TASK_MESS_REFRESH_VIEW);
        }
    }


    public class GridTaskResult {
        public long taskStart;
        public long taskEnd;
        public long time;
        public ArrayList<Double> results = new ArrayList<>();
        public int numOfItems;
        public String area;
    }
}