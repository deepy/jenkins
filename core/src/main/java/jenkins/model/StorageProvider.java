package jenkins.model;

import hudson.XmlFile;

import java.io.File;

import com.thoughtworks.xstream.XStream;

public interface StorageProvider {
    XmlFile getXmlFile(File file);

    XmlFile getXmlFile(XStream xs, File file);

    boolean preFlightCheck();
}
