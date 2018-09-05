package jenkins;

import java.io.InputStream;

abstract class PostgresqlJsonFile implements StorageAdapter {
    public Object unmarshal(Object o) {
        return o;
    }

    @Override
    public void delete() {

    }

    @Override
    public void save(Object o) {

    }

    @Override
    public InputStream readStream() {
        return null;
    }
}
