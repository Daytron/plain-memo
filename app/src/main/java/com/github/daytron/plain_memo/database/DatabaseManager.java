package com.github.daytron.plain_memo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ryan on 27/10/15.
 */
public abstract class DatabaseManager {

    abstract public void onCreate(SQLiteDatabase db);
    abstract public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    static private class DBOpenHelper extends SQLiteOpenHelper {

        DatabaseManager databaseManager;
        private AtomicInteger counter = new AtomicInteger(0);

        public DBOpenHelper(Context appContext, String dbName, int dbVersion,
                            DatabaseManager dbManager) {
            super(appContext, dbName, null, dbVersion);
            this.databaseManager = dbManager;
        }

        public void addConnection(){
            counter.incrementAndGet();
        }
        public void removeConnection(){
            counter.decrementAndGet();
        }
        public int getCounter() {
            return counter.get();
        }

        /**
         * Called when the database is created for the first time. This is where the
         * creation of tables and the initial population of the tables should happen.
         *
         * @param db The database.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            databaseManager.onCreate(db);
        }

        /**
         * Called when the database needs to be upgraded. The implementation
         * should use this method to drop tables, add tables, or do anything else it
         * needs to upgrade to the new schema version.
         * <p/>
         * <p>
         * The SQLite ALTER TABLE documentation can be found
         * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
         * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
         * you can use ALTER TABLE to rename the old table, then create the new table and then
         * populate the new table with the contents of the old table.
         * </p><p>
         * This method executes within a transaction.  If an exception is thrown, all changes
         * will automatically be rolled back.
         * </p>
         *
         * @param db         The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            databaseManager.onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static final ConcurrentHashMap<String,DBOpenHelper> dbMap =
            new ConcurrentHashMap<>();

    private static final Object lockObject = new Object();


    private DBOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase db;
    private Context context;


    /** Instantiate a new DB Helper.
     * <br> SQLiteOpenHelpers are statically cached so they (and their internally cached
     * SQLiteDatabases) will be reused for concurrency
     *
     * @param appContext Any {@link android.content.Context} belonging to your package.
     * @param dbName The database name. This may be anything you like. Adding a file extension
     *               is not required and any file extension you would like to use is fine.
     * @param dbVersion the database version.
     */
    public DatabaseManager(Context appContext, String dbName, int dbVersion) {
        String dbPath = appContext.getApplicationContext().getDatabasePath(dbName).getAbsolutePath();
        synchronized (lockObject) {
            sqLiteOpenHelper = dbMap.get(dbPath);
            if (sqLiteOpenHelper == null) {
                sqLiteOpenHelper = new DBOpenHelper(appContext, dbName, dbVersion, this);
                dbMap.put(dbPath,sqLiteOpenHelper);
            }
            //SQLiteOpenHelper class caches the SQLiteDatabase, so this will be the same SQLiteDatabase object every time
            db = sqLiteOpenHelper.getWritableDatabase();
        }
        this.context = appContext.getApplicationContext();
    }

    /**Get the writable SQLiteDatabase
     */
    public SQLiteDatabase getDatabase(){
        return db;
    }


    /** Check if the underlying SQLiteDatabase is open
     *
     * @return whether the DB is open or not
     */
    public boolean isOpen(){
        return (db != null && db.isOpen());
    }

    /** Lowers the DB counter by 1 for any {@link DatabaseManager}s referencing the same DB on disk
     *  <br />If the new counter is 0, then the database will be closed.
     *  <br /><br />This needs to be called before application exit.
     * <br />If the counter is 0, then the underlying SQLiteDatabase is <b>null</b> until another
     * DatabaseManager is instantiated or you call {@link #open()}
     *
     * @return true if the underlying {@link android.database.sqlite.SQLiteDatabase} is closed
     * (counter is 0), and false otherwise (counter > 0)
     */
    public boolean close(){
        sqLiteOpenHelper.removeConnection();
        if (sqLiteOpenHelper.getCounter()==0){
            synchronized (lockObject){
                if (db.inTransaction())db.endTransaction();
                if (db.isOpen())db.close();
                db = null;
            }
            return true;
        }
        return false;
    }

    /** Increments the internal db counter by one and opens the db if needed
     *
     */
    public void open(){
        sqLiteOpenHelper.addConnection();
        if (db==null||!db.isOpen()){
            synchronized (lockObject){
                db = sqLiteOpenHelper.getWritableDatabase();
            }
        }
    }
}
