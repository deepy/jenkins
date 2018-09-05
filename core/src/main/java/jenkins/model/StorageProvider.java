package jenkins.model;

import hudson.XmlFile;

import java.io.File;

import com.thoughtworks.xstream.XStream;

public interface StorageProvider {
    XmlFile createItemFile(XStream xstream, File file);

    XmlFile createRunFile(XStream xstream, File file);

    XmlFile createConfigXmlFile(File file);

    XmlFile createConfigXmlFile(XStream xStream, File file);

    XmlFile createQueueConfigXmlFile(File file);

    XmlFile createQueueConfigXmlFile(XStream xStream, File file);

    boolean preFlightCheck();
}
