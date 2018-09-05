package jenkins;

import java.io.InputStream;

public interface StorageAdapter {
    void delete(String o);

    void save(String path, String data);

    InputStream readStream(String path);

    boolean exists(String path);
}
