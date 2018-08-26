package jenkins.model;

import hudson.XmlFile;
import jenkins.XmlFilePretender;

import java.io.File;

import com.thoughtworks.xstream.XStream;

public class XmlFileFallbackStorageProvider implements StorageProvider {

    @Override
    public XmlFile getXmlFile(File file) {
        return new XmlFilePretender(file);
    }

    @Override
    public XmlFile getXmlFile(XStream xs, File file) {
        return new XmlFilePretender(xs, file);
    }

    @Override
    public boolean preFlightCheck() {
        return true;
    }
}
