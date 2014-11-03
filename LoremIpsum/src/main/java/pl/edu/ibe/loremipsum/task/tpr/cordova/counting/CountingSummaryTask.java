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

package pl.edu.ibe.loremipsum.task.tpr.cordova.counting;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;

import pl.edu.ibe.loremipsum.task.LookTask;
import pl.edu.ibe.loremipsum.task.TaskInfo;
import pl.edu.ibe.loremipsum.task.tpr.cordova.counting.CountingTaskGroup;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.loremipsum.tpr.views.CountList;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 08.08.14.
 */
public class CountingSummaryTask extends LookTask {
    private static final String TAG = CountingSummaryTask.class.getSimpleName();

    private static final String ATTR_SHOW_CORRECT = "showCorrect";
    private static final String ATTR_MARK_RANGE = "markRange";
    boolean showCorrect;
    private ArrayList<CountList> boards;
    private LinearLayout container;
    private View button;
    private LinearLayout view;
    private int minRange;
    private int maxRange;


    private CountingTaskGroup.SummaryTaskResults countingTaskResultes;


    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public CountingSummaryTask(Context context) {
        super(context);
    }

    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {
        boolean valid = super.Create(a_info, a_handler, a_dir);
        boards = new ArrayList<>();
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
                            showCorrect = getString(map, ATTR_SHOW_CORRECT, "false").equals("true");

                            String[] range = getString(map, ATTR_MARK_RANGE, "1,5").split(",");


                            minRange = Integer.valueOf(range[0]);
                            maxRange = Integer.valueOf(range[1]);

                        } catch (XMLFileException e) {
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE, e);
                        }
                    }
                }
            }

        }

        return valid;
    }

    public void setData(CountingTaskGroup.SummaryTaskResults countingTaskResultses) {
        this.countingTaskResultes = countingTaskResultses;
        if (view != null) {
            CountList countList;
            for (int i = 0; i < countingTaskResultses.resultses.size(); i++) {
                countList = new CountList(getContext());
                countList.setRange(minRange, maxRange);
                boards.add(countList);
                container.addView(countList);
            }
        }
        if (showCorrect) {
            button.setOnClickListener(v -> {
                for (int i = 0; i < boards.size(); i++) {
                    boards.get(i).showCorrect(countingTaskResultses.resultses.get(i).correctBallsCount);
                }
            });
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
            button.setOnClickListener(null);
        }
    }

    public void setView(LinearLayout view) {
        this.view = view;
        this.container = (LinearLayout) view.findViewById(R.id.item_counts);
        view.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);
        button = view.findViewById(R.id.show_marks);
    }

    public void requestStatisticUpdate() {
        for (int i = 0; i < boards.size(); i++) {
            countingTaskResultes.resultses.get(i).answer = boards.get(i).getSelectedNumber();
        }
    }

    @Override
    public void Destory() throws IOException {
        for (int i = 0; i < boards.size(); i++) {
            countingTaskResultes.resultses.get(i).answer = boards.get(i).getSelectedNumber();
        }

        button.setOnClickListener(null);
        if (container != null) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
        view.setVisibility(View.GONE);
        super.Destory();
    }


}
