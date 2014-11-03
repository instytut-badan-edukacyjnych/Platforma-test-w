package pl.edu.ibe.loremipsum.util;

import pl.edu.ibe.loremipsum.tools.DbAccess;

/**
 * Created by mikolaj on 10.04.14.
 */
public interface DbTest {

    DbAccess getDb();
    void onTearDown();
}
