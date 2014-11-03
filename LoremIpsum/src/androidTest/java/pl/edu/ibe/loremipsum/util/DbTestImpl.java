package pl.edu.ibe.loremipsum.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import pl.edu.ibe.loremipsum.db.DbHelper;
import pl.edu.ibe.loremipsum.db.schema.DaoMaster;
import pl.edu.ibe.loremipsum.tools.DbAccess;

/**
 * Created by mikolaj on 10.04.14.
 */
public class DbTestImpl implements DbTest {

    private Context context;
    private DbHelper dbHelper;

    public DbTestImpl(Context context) {
        this.context = context;
    }

    @Override
    public DbAccess getDb() {
        return getDbAccess();
    }

    @Override
    public void onTearDown() {
        dbHelper = null;
    }

    private DbAccess getDbAccess() {
        if (dbHelper == null) {
            SQLiteDatabase sqliteDatabase = SQLiteDatabase.create(null);
            DaoMaster.createAllTables(sqliteDatabase, false);
            dbHelper = new DbHelper(context, sqliteDatabase);
        }
        return new DbAccess(dbHelper.getDaoSession());
    }
}
