/************************************
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
 ************************************/
package pl.edu.ibe.loremipsum.tablet.examinee.data;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;

/**
 * Created by adam on 22.07.14.
 */
public class ExamineeAdditionalFieldsManager {
    public static final String ADDITIONAL_EXAMINEE_INFO_FILE_NAME = "additional_examinee_info.xml";
    private AdditionalFieldsHolder additionalFieldsHolder;

    public static ExamineeAdditionalFieldsManager createInstance(VirtualFile rootVirtualFile) {
        ExamineeAdditionalFieldsManager manager = new ExamineeAdditionalFieldsManager();
        try {
            manager.additionalFieldsHolder = LoremIpsumApp.obtain().getServiceProvider().getXMLPersister().read(AdditionalFieldsHolder.class, rootVirtualFile.getChildFile(ADDITIONAL_EXAMINEE_INFO_FILE_NAME).getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return manager;
    }

    public List<View> generateViews(Context activity) {
        if (additionalFieldsHolder != null) {
            ArrayList<Field> fields = new ArrayList<>();
            fields.addAll(additionalFieldsHolder.listFields);
            fields.addAll(additionalFieldsHolder.numberFields);
            fields.addAll(additionalFieldsHolder.stringFields);
            Collections.sort(fields, (lhs, rhs) -> {
                if (lhs.order < rhs.order) {
                    return -1;
                } else if (lhs.order > rhs.order) {
                    return 1;
                }
                return 0;
            });
            ArrayList<View> views = new ArrayList<>();
            for (Field field : fields) {
                views.add(field.generateFieldView(activity));
            }

            return views;
        }
        return new ArrayList<>();
    }


    public String handleFilledViews(List<View> views, ArrayList<AdditionalDataItem> savedAdditionalInfo) {
        ArrayList<AdditionalDataItem> additionalDataItems = new ArrayList<>();
        AdditionalDataItem additionalDataItem;
        for (View view : views) {
            Field field = (Field) view.getTag();
            if (view instanceof EditText) {
                additionalDataItem = new AdditionalDataItem(field, ((EditText) view).getText().toString(),
                        field instanceof StringField ? AdditionalDataItem.DataType.STRING : AdditionalDataItem.DataType.NUMBER);
            } else if (view instanceof Spinner) {
                additionalDataItem = new AdditionalDataItem(field, ((Field) ((Spinner) view).getSelectedItem()).id, AdditionalDataItem.DataType.LIST);
            } else {
                throw new RuntimeException("Unknown view type: " + view.toString());
            }
            additionalDataItems.add(additionalDataItem);
        }
        HashMap<String, AdditionalDataItem> mergedData = new HashMap<>();
        if (savedAdditionalInfo != null && savedAdditionalInfo.size() > 0) {
            for (AdditionalDataItem dataItem : savedAdditionalInfo) {
                mergedData.put(dataItem.id, dataItem);
            }
        }
        for (AdditionalDataItem dataItem : additionalDataItems) {
            mergedData.put(dataItem.id, dataItem);
        }


        return LoremIpsumApp.obtain().getServiceProvider().getGson().toJson(mergedData.values());
    }

    public ArrayList<Field> getRequiredFields() {
        ArrayList<Field> fields = new ArrayList<>();
        if (additionalFieldsHolder != null) {
            fields.addAll(additionalFieldsHolder.listFields);
            fields.addAll(additionalFieldsHolder.numberFields);
            fields.addAll(additionalFieldsHolder.stringFields);
        }
        return fields;
    }
}
