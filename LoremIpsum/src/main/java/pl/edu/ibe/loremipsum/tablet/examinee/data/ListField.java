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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;

import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;

/**
 * Created by adam on 22.07.14.
 */
public class ListField extends Field {
    @ElementList(inline = true)
    public ArrayList<Item> items;

    @Override
    public View generateFieldView(Context context) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Spinner spinner = new Spinner(context);
        spinner.setLayoutParams(lp);
        spinner.setTag(this);
        Adapter adapter = new Adapter(context, android.R.layout.simple_list_item_1);
        adapter.addAll(items);
        spinner.setAdapter(adapter);
        return spinner;
    }


    public static class Item extends Field {

        @Override
        public View generateFieldView(Context context) {
            return null;
        }
    }

    public static class Adapter extends LoremIpsumSimpleAdapter<Item> {

        /**
         * Root must be @link{TextView} or if layout is named "row_simple_list_item_1" must contain @link{TextView} with id text1 and @link{RowImageView} with id row_image
         *
         * @param context
         * @param resource list row layout.
         */
        public Adapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        protected long extractId(Item item) {
            return item.getViewId();
        }

        @Override
        protected String populateItem(Item item) {
            return item.getLocalizedName();
        }
    }
}
