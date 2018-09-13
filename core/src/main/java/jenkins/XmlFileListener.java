package jenkins;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import jenkins.util.SystemProperties;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Backwards compatibility, awaits a file being saved and then writes that specific file to disk.
 * Enable with io.jenkins.storage.syncFilesToDisk
 *
 * @see <a href="https://wiki.jenkins-ci.org/display/JENKINS/Architecture#Architecture-Persistence">Architecture » Persistence</a>
 * @author Alex Nordlund
 */
@Extension(ordinal = Integer.MAX_VALUE - 10)
public class XmlFileListener extends SaveableListener {
    private static final Logger LOGGER = Logger.getLogger(XmlFileListener.class.getName());

    @Override
    public void onChange(Saveable o, XmlFile file) {
        if (SystemProperties.getBoolean("io.jenkins.storage.syncFilesToDisk", false)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getFile()));
                 BufferedReader br = new BufferedReader(file.readRaw())) {

                int b;
                while ((b = br.read()) != -1) {
                    bos.write(b);
                }

                bos.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error writing file to disk", e);
            }
        }
    }
}
