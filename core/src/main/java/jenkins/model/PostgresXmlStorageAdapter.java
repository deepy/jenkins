package jenkins.model;

import jenkins.StorageAdapter;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresXmlStorageAdapter implements StorageAdapter {
    private static final Logger LOGGER = Logger.getLogger(PostgresXmlStorageAdapter.class.getName());

    private boolean initialized = false;
    private Connection db = null;
    private final String domain;

    protected PostgresXmlStorageAdapter(String domain) throws ClassNotFoundException, SQLException {
        this.domain = domain;
        Class.forName("org.postgresql.Driver");
        db = DriverManager.getConnection("jdbc:postgresql://localhost:5433/jenkins", "jenkins", "See5ceeleepeigui");
        PreparedStatement ps = db.prepareStatement("create table if not exists " + domain + "(id serial, path text unique not null, file xml not null);");
        ps.execute();
    }

    @Override
    public boolean exists(String path) {
        try {
            PreparedStatement ps = db.prepareStatement("SELECT FROM " + domain + " WHERE path = ?");
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
            PreparedStatement ps = db.prepareStatement("DELETE FROM " + domain + " WHERE path = ?");
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
            PreparedStatement ps = db.prepareStatement("INSERT INTO " + domain + " (path, file) values (?, xml(?)) " +
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
            PreparedStatement ps = db.prepareStatement("SELECT file FROM " + domain + " WHERE path = ?");
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
