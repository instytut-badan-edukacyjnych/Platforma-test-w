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

package pl.edu.ibe.loremipsum.tablet.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pl.edu.ibe.loremipsum.RowImageView;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 31.03.14.
 * Common adapter for lists in app. Simplifies creating lists with one @link{TextView}
 */
public abstract class LoremIpsumSimpleAdapter<T> extends ArrayAdapter<T> {

    private final LayoutInflater inflater;
    private final int resource;

    /**
     * Root must be @link{TextView} or if layout is named "row_simple_list_item_1" must contain @link{TextView} with id text1 and @link{RowImageView} with id row_image
     *
     * @param context
     * @param resource list row layout.
     */
    public LoremIpsumSimpleAdapter(Context context, int resource) {
        super(context, resource);
        inflater = LayoutInflater.from(context);
        this.resource = resource;
    }


    @Override
    public long getItemId(int position) {
        return extractId(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            try {
                view = inflater.inflate(resource, parent, false);
            } catch (Exception e) {
                try {
                    throw new InflateException(getContext().getResources().getResourceName(resource), e);
                } catch (Exception e1) {
                    throw new IllegalStateException("Could not get resource name: " + resource, e1);
                }
            }

        }
        if (view == null)
            throw new IllegalStateException("Could not inflate resource: " + resource);

        if (resource == R.layout.row_simple_list_item_1) {
            TextView textView = (TextView) view.findViewById(R.id.text1);
            textView.setText(populateItem(getItem(position)));
            RowImageView rowImageView = (RowImageView) view.findViewById(R.id.row_image);
            rowImageView.setImage(position);
        } else {
            ((TextView) view).setText(populateItem(getItem(position)));
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(resource, parent, false);
        if (view == null)
            throw new IllegalStateException("Could not inflate resource: " + resource);

        TextView textView = (TextView) view;
        textView.setText(populateItem(getItem(position)));
        return view;
    }

    /**
     * Clears adapter and populate it with new data
     *
     * @param data
     */
    public void populate(List<T> data) {
        clear();
        addAll(data);
        notifyDataSetChanged();
    }

    /**
     * Adds new item to list
     *
     * @param item
     */
    public void addItem(T item) {
        add(item);
        notifyDataSetChanged();
    }

    /**
     * @param item
     * @return item id
     */
    protected abstract long extractId(T item);

    /**
     * @param item
     * @return String displayed in @link{TextView}
     */
    protected abstract String populateItem(T item);


    public static class InflateException extends RuntimeException {


        public InflateException(String resourceName, Exception e) {
            super(resourceName, e);

        }


    }

}
