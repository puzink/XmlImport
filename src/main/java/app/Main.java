package app;

import app.dao.RowDao;
import app.dao.RowDaoImpl;
import app.dao.TableDaoImpl;
import app.imports.XmlImporter;
import app.imports.transaction.ThreadConnectionPool;
import app.imports.transaction.ThreadConnectionTransactionManagerImpl;
import app.repository.RowRepositoryImpl;
import app.repository.TableRepositoryImpl;
import app.xml.*;

import java.io.File;

public class Main {

    private static DbConnection dbConnection;
    private static File file;
    private static XmlImporter.Settings settings;

    private static void readSettings(){
        dbConnection = getDbConnection();
        file = getFile();
        settings = getImportSettings();
    }

    private static DbConnection getDbConnection(){
        String url = System.getProperty("url");
        String username = System.getProperty("username");
        String pass = System.getProperty("password");

        return new DbConnection(username, pass, url);
    }


    private static XmlImporter.Settings getImportSettings() {
        String queueSize = System.getProperty("queueSize");
        String maxThreads = System.getProperty("threads");
        String rowSize = System.getProperty("rowSize");

        XmlImporter.Settings defaultSettings = XmlImporter.Settings.builder().build();

        return XmlImporter.Settings.builder()
                .readRowSize(rowSize == null
                        ? defaultSettings.getReadRowSize()
                        : Integer.parseInt(rowSize))
                .taskQueueSize(queueSize == null
                        ? defaultSettings.getTaskQueueSize()
                        : Integer.parseInt(queueSize))
                .threads(maxThreads == null
                        ? defaultSettings.getThreads()
                        : Integer.parseInt(maxThreads))
                .build();
    }

    private static File getFile(){
        System.out.println(System.getProperty("file"));
        return new File(System.getProperty("file"));
    }

    public static void main(String[] args) throws Exception {
        readSettings();

        ThreadConnectionPool connectionPool = new ThreadConnectionPool(dbConnection);
        ThreadConnectionTransactionManagerImpl tx =
                new ThreadConnectionTransactionManagerImpl(connectionPool);

        RowDao simpleRowDao = new RowDaoImpl(connectionPool);
        TableDaoImpl simpleTableDao = new TableDaoImpl(connectionPool);

        RowRepositoryImpl repository = new RowRepositoryImpl(connectionPool, simpleRowDao);
        TableRepositoryImpl tableRepository = new TableRepositoryImpl(connectionPool, simpleTableDao);

        XmlParser parser = new XmlLazyParser(file, new XmlElementParserImpl());
        XmlTableReader tableReader = new XmlTableReaderImpl(parser);

        XmlImporter xmlImporter =
                new XmlImporter(repository, tableRepository, tx, settings);
//        long start = System.currentTimeMillis();
        System.out.println("Inserted row count = " + xmlImporter.importUniqueTableRows(tableReader));
//        System.out.println("Time = " + (System.currentTimeMillis() - start) / 1000);
//        System.out.println();
    }
}
