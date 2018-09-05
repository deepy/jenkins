package jenkins;

import java.io.InputStream;

public interface StorageAdapter {
    void delete();

    void save(Object o);

    InputStream readStream();
}
