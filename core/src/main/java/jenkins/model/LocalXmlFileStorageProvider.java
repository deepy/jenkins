package jenkins.model;

import hudson.XmlFile;

import java.io.File;

import com.thoughtworks.xstream.XStream;

public class LocalXmlFileStorageProvider implements StorageProvider {

    @Override
    public XmlFile createItemFile(XStream xStream, File file) {
        return getXmlFile(xStream, file);
    }

    @Override
    public XmlFile createRunFile(XStream xStream, File file) {
        return getXmlFile(xStream, file);
    }

    @Override
    public XmlFile createConfigXmlFile(File file) {
        return getXmlFile(file);
    }

    @Override
    public XmlFile createConfigXmlFile(XStream xStream, File file) {
        return getXmlFile(xStream, file);
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
        return true;
    }

    private XmlFile getXmlFile(File file) {
        return new XmlFile(file);
    }

    private XmlFile getXmlFile(XStream xs, File file) {
        return new XmlFile(xs, file);
    }
}
