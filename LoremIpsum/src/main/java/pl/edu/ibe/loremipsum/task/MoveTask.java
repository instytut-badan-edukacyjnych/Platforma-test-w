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


package pl.edu.ibe.loremipsum.task;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;


/**
 * Klasa obsługi zadania polegajacego na przesunieciu określonej liczby
 * elementów na pole docelowe.
 *
 *
 */
public class MoveTask extends MoveBaseTask {
    private static final String TAG = MoveTask.class.toString();

    /**
     * Constructor.
     *
     * @param context Android context.
     */
    public MoveTask(Context context) {
        super(context);
    }

    /*
     * (non-Javadoc)
     * @see pl.edu.ibe.loremipsum.task.MoveBaseTask#Create(pl.edu.ibe.loremipsum.task.TaskInfo, android.os.Handler, java.lang.String)
     */
    @Override
    public boolean Create(TaskInfo a_info, Handler a_handler, String a_dir) throws IOException {

        boolean valid = super.Create(a_info, a_handler, a_dir);


        if (m_document != null) {
            Bitmap item = null;
            int width = 0;
            int height = 0;

            NodeList list = m_document.getElementsByTagName(XML_DETAILS_TASK);
            if (list != null) {
                if (list.getLength() > a_info.m_groupIndex) {
                    NamedNodeMap map = list.item(a_info.m_groupIndex).getAttributes();
                    if (map != null) {
                        Node node = map.getNamedItem(XML_DETAILS_TASK_ITEM);
                        if (node != null) {
                            String fileName = node.getNodeValue();

                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                // INFO_RESIZE pomniejszamy pliki graficzne ze wzgledu na limit wilekości aplikacji 64MB
                                // jest mała więc nie pomniejszamy
                                item = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                height = (item.getHeight() * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                width = item.getWidth();
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                item = null;
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                        } else {
                            item = null;
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ITEM);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_PLACE);
                        if (node != null) {
                            String fileName = node.getNodeValue();

                            VirtualFile bmpFile = a_info.getDirectory().getChildFile(fileName);

                            if (bmpFile.exists()) {
                                Bitmap bmp = BitmapFactory.decodeStream(bmpFile.getInputStream());
                                TargetField tar = new TargetField();
                                tar.m_mask = bmp;
                                tar.m_answer = null;
                                m_targetList.add(tar);
                                LogUtils.d(TAG, "BitmapFactory: " + fileName);
                            } else {
                                // nie ma obrazka
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_FILE + bmpFile.getAbsolutePath());
                            }
                        } else {
                            valid = false;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_PLACE);
                        }
                        node = null;

                        node = map.getNamedItem(XML_DETAILS_TASK_ANSWER);
                        if (node != null) {
                            m_answer = node.getNodeValue();
                        } else {
                            m_answer = null;
                            LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_ANSWER);
                        }
                        node = null;

                        if (m_property.baseLine) {
                            node = map.getNamedItem(XML_DETAILS_TASK_SX);
                            if (node != null) {
                                try {
                                    m_horBase = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                    ;
                                } catch (NumberFormatException e) {
                                    m_horBase = 0;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                m_horBase = 0;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_TASK_SX);
                            }
                        }
                    }
                }
            } else {
                valid = false;
            }

            list = m_document.getElementsByTagName(XML_DETAILS_FIELD);
            if (list != null) {
                if (list.getLength() > 0) {
                    for (int index = 0; index < list.getLength(); ++index) {
                        NamedNodeMap map = list.item(index).getAttributes();
                        if (map != null) {
                            Rect rec = new Rect();
                            String name = null;
                            int px = 0;
                            int py = 0;

                            Node node = map.getNamedItem(XML_DETAILS_FIELD_NAME);
                            if (node != null) {
                                name = node.getNodeValue();
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_NAME);
                            }
                            node = null;

                            node = map.getNamedItem(XML_DETAILS_FIELD_X);
                            if (node != null) {
                                try {
                                    rec.left = Integer.parseInt(node.getNodeValue());
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_X);
                            }
                            node = null;

                            node = map.getNamedItem(XML_DETAILS_FIELD_Y);
                            if (node != null) {
                                try {
                                    rec.top = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                } catch (NumberFormatException e) {
                                    valid = false;
                                    LogUtils.e(TAG, e);
                                }
                            } else {
                                valid = false;
                                LogUtils.e(TAG, ERR_MESS_NO_ATTRIBUTE + XML_DETAILS_FIELD_Y);
                            }
                            node = null;

                            if (m_property.pull) {
                                // pola moga nie wystepowac pomimo ustawienia flagi
                                node = map.getNamedItem(XML_DETAILS_FIELD_PX);
                                if (node != null) {
                                    try {
                                        px = Integer.parseInt(node.getNodeValue());
                                    } catch (NumberFormatException e) {
                                        valid = false;
                                        LogUtils.e(TAG, e);
                                    }
                                }
                                node = null;

                                node = map.getNamedItem(XML_DETAILS_FIELD_PY);
                                if (node != null) {
                                    try {
                                        py = (Integer.parseInt(node.getNodeValue()) * VIEW_SIZE_CORECTION_FACTOR_MUL) / VIEW_SIZE_CORECTION_FACTOR_DIV;
                                    } catch (NumberFormatException e) {
                                        valid = false;
                                        LogUtils.e(TAG, e);
                                    }
                                }
                            }

                            if (valid) {
                                rec.right = rec.left + width;
                                rec.bottom = rec.top + height;

                                FieldSelect sel = new FieldSelect();
                                sel.m_name = name;
                                sel.m_srce = rec;
                                sel.m_dest = new Rect(rec);
                                sel.m_mask = item;
                                sel.m_xPlace = rec.left + width / 2;
                                sel.m_yPlace = rec.top + height / 2;
                                sel.m_selected = false;

                                m_items.add(sel);

                                if (px != 0 && py != 0) {
                                    PlaceFiled f = new PlaceFiled();
                                    f.m_occupied = false;
                                    f.m_xPlace = px;
                                    f.m_yPlace = py;
                                    m_pullList.add(f);
                                }
                            }
                        }
                    }
                }
            } else {
                valid = false;
            }
        }

        return valid;
    }

}


