package jenkins.model;

import hudson.XmlFile;
import jenkins.StorageAdapter;
import jenkins.XmlFilePretender;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

public class PostgresXmlStorageProvider implements StorageProvider, StorageAdapter {
    private static final Logger LOGGER = Logger.getLogger(PostgresXmlStorageProvider.class.getName());

    private boolean initialized = false;
    private Connection db = null;
    private StorageAdapter jobs = null;
    private StorageAdapter config = null;

    @Override
    public XmlFile createItemFile(XStream xStream, File file) {
        return getXmlFile(xStream, file);
    }

    @Override
    public XmlFile createRunFile(XStream xStream, File file) {
        return getXmlFile(jobs, xStream, file);
    }

    @Override
    public XmlFile createConfigXmlFile(File file) {
        return getXmlFile(config, file);
    }

    @Override
    public XmlFile createConfigXmlFile(XStream xStream, File file) {
        return getXmlFile(config, xStream, file);
    }

    @Override
    public XmlFile createQueueConfigXmlFile(File file) {
        return getXmlFile(file);
    }

    @Override
    public XmlFile createQueueConfigXmlFile(XStream xStream, File file) {
        return getXmlFile(xStream, file);
    }

    @Override
    public boolean preFlightCheck() {
        if (initialized) {
            return true;
        } else {
            try {
                initialize();
                initialized = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void initialize() throws ClassNotFoundException, SQLException {
        jobs = new PostgresXmlStorageAdapter("jobs");
        config = new PostgresXmlStorageAdapter("config");
        Class.forName("org.postgresql.Driver");
        db = DriverManager.getConnection("jdbc:postgresql://localhost:5433/jenkins", "jenkins", "See5ceeleepeigui");
//        db.setAutoCommit(false);
    }

    private XmlFile getXmlFile(File file) {
        return new XmlFilePretender(this, file);
    }

    private XmlFile getXmlFile(XStream xs, File file) {
        return new XmlFilePretender(this, xs, file);
    }

    private XmlFile getXmlFile(StorageAdapter proxy, File file) {
        return new XmlFilePretender(proxy, file);
    }

    private XmlFile getXmlFile(StorageAdapter proxy, XStream xs, File file) {
        return new XmlFilePretender(proxy, xs, file);
    }

    @Override
    public boolean exists(String path) {
        try {
            PreparedStatement ps = db.prepareStatement("SELECT FROM config WHERE path = ?");
            ps.setString(1, path);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            return rs.next();
//            db.commit();
        } catch (Exception e) {
            // pass
            LOGGER.info(path);
            LOGGER.log(Level.SEVERE, "Failed to lookup!", e);
        }

        return false;
    }

    @Override
    public void delete(String path) {
        try {
            PreparedStatement ps = db.prepareStatement("DELETE FROM config WHERE path = ?");
            ps.setString(1, path);
            ps.execute();
//            db.commit();
        } catch (Exception e) {
            // pass
            LOGGER.info(path);
            LOGGER.log(Level.SEVERE, "Failed to save!", e);
        }
    }

    @Override
    public void save(String path, String data) {
        try {
            PreparedStatement ps = db.prepareStatement("INSERT INTO config (path, file) values (?, xml(?)) " +
                    "ON CONFLICT (path) DO UPDATE SET file = EXCLUDED.file");
            ps.setString(1, path);
            ps.setString(2, data);
            ps.execute();
//            db.commit();
        } catch (Exception e) {
//            try {
                LOGGER.info(data);
                LOGGER.log(Level.SEVERE, "Failed to save!", e);
//                db.rollback();
//            } catch (SQLException e1) {
                // pass
//            }
        }
    }

    @Override
    public InputStream readStream(String path) {
        try {
            PreparedStatement ps = db.prepareStatement("SELECT file FROM config WHERE path = ?");
            ps.setString(1, path);
            ps.execute();
            ResultSet rs = ps.getResultSet();

            if (rs.next()) {
                return rs.getBinaryStream(1);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to read!", e);
        }

        return null;
    }
}
