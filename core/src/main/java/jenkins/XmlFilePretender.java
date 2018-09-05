/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins;

import hudson.XmlFile;
import hudson.util.XStream2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.InvalidPathException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Backwards compatibility, pretends to represents an XML data file that Jenkins uses as a data file.
 *
 * @see <a href="https://wiki.jenkins-ci.org/display/JENKINS/Architecture#Architecture-Persistence">Architecture » Persistence</a>
 * @author Kohsuke Kawaguchi
 */
public class XmlFilePretender extends XmlFile {
    private static final Logger LOGGER = Logger.getLogger(XmlFilePretender.class.getName());
    private StorageAdapter proxy = null;


    public XmlFilePretender(File file) {
        super(file);
    }

    public XmlFilePretender(XStream xs, File file) {
        super(xs, file);
    }

    public XmlFilePretender(StorageAdapter proxy, File file) {
        super(file);
        this.proxy = proxy;
    }

    public XmlFilePretender(StorageAdapter proxy, XStream xs, File file) {
        super(xs, file);
        this.proxy = proxy;
    }

    public File getFile() {
        return super.getFile();
    }

    public XStream getXStream() {
        return super.getXStream();
    }

    /**
     * Loads the contents of this file into a new object.
     */
    public Object read() throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Reading "+super.getFile());
        }

        if (proxy == null) {
            return super.read();
        } else {
            try (InputStream in = new BufferedInputStream(proxy.readStream())) {
                return super.getXStream().fromXML(in);
            } catch (RuntimeException | Error e) {
                throw new IOException("Unable to read "+proxy.toString(), e);
            }
        }
    }

    /**
     * Loads the contents of this file into an existing object.
     *
     * @return
     *      The unmarshalled object. Usually the same as <tt>o</tt>, but would be different
     *      if the XML representation is completely new.
     */
    public Object unmarshal( Object o ) throws IOException {
        return unmarshal(o, false);
    }

    /**
     * Variant of {@link #unmarshal(Object)} applying {@link XStream2#unmarshal(HierarchicalStreamReader, Object, DataHolder, boolean)}.
     * @since 2.99
     */
    public Object unmarshalNullingOut(Object o) throws IOException {
        return unmarshal(o, true);
    }


    private Object unmarshal(Object o, boolean nullOut) throws IOException {
        try (InputStream in = new BufferedInputStream(proxy.readStream())) {
            if (nullOut) {
                return ((XStream2) super.getXStream()).unmarshal(XStream2.getDefaultDriver().createReader(in), o, null, true);
            } else {
                return super.getXStream().unmarshal(XStream2.getDefaultDriver().createReader(in), o);
            }
        } catch (RuntimeException | Error e) {
            throw new IOException("Unable to read ",e);
        }
    }

    @Override
    public void write( Object o ) throws IOException {
        if (proxy == null) {
            super.write(o);
        } else {
            proxy.save(o);
        }
    }

    @Override
    public boolean exists() {
        return super.exists();
    }

    public void delete() {
        super.delete();
    }
    
    public void mkdirs() {
        super.mkdirs();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Opens a {@link Reader} that loads XML.
     * This method uses {@link #sniffEncoding() the right encoding},
     * not just the system default encoding.
     * @throws IOException Encoding issues
     * @return Reader for the file. should be close externally once read.
     */
    public Reader readRaw() throws IOException {
        if (proxy == null) {
            return super.readRaw();
        } else {
            try {
                InputStream fileInputStream = proxy.readStream();
                return new InputStreamReader(fileInputStream, sniffEncoding());
            } catch (InvalidPathException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Returns the XML file read as a string.
     */
    public String asString() throws IOException {
        return super.asString();
    }

    /**
     * Writes the raw XML to the given {@link Writer}.
     * Writer will not be closed by the implementation.
     */
    public void writeRawTo(Writer w) throws IOException {
        super.writeRawTo(w);
    }

    /**
     * Parses the beginning of the file and determines the encoding.
     *
     * @throws IOException
     *      if failed to detect encoding.
     * @return
     *      always non-null.
     */
    public String sniffEncoding() throws IOException {
        return super.sniffEncoding();
    }
}
