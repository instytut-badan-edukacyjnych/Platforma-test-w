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


package pl.edu.ibe.loremipsum.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import pl.edu.ibe.loremipsum.db.schema.DaoMaster;
import pl.edu.ibe.loremipsum.db.schema.DaoSession;

/**
 * Created by adam on 10.03.14.
 * Helper class used to open and manage database
 */
public class DbHelper {
    private final Context context;
    private DaoMaster.DevOpenHelper devOpenHelper;
    private SQLiteDatabase database;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    /**
     * Constructor
     *
     * @param context
     */
    public DbHelper(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context
     * @param database
     */
    public DbHelper(Context context, SQLiteDatabase database) {
        this.context = context;
        this.database = database;
        openDb();
    }

    /**
     * Opens database
     */
    private void openDb() {
        if (database == null) {
            devOpenHelper = new LoremIpsumOpenHelper(context, "LoremIpsum_db", null);
            database = devOpenHelper.getWritableDatabase();
        }
        database.execSQL("PRAGMA foreign_keys = ON;");
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    /**
     * closes database
     */
    public void closeDb() {
        database.close();
    }

    /**
     * @return greenDao session
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }


    private static class LoremIpsumOpenHelper extends DaoMaster.DevOpenHelper {
        public LoremIpsumOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onUpgrade(db, oldVersion, newVersion);
        }
    }

}