/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, Yahoo! Inc., CloudBees, Inc.
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
package hudson.kubernetes;

import com.thoughtworks.xstream.XStream;
import hudson.XmlFile;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;

/**
 */
public abstract class XmlKubernetesResource extends XmlFile {
    private String path;
    private String name;

    public XmlKubernetesResource(File file) {
        super(file);
        init();
    }

    public XmlKubernetesResource(XStream xs, File file) {
        super(xs, file);
        init();
    }

    public static String getRelativePath(File rootDir, File file) throws IOException {
        String rootPath = rootDir.getCanonicalPath();
        String fullPath = file.getCanonicalPath();
        if (fullPath.startsWith(rootPath)) {
            return fullPath.substring(rootPath.length());
        } else {
            return fullPath;
        }
    }

    public static String stripPrefix(String value, String suffix) {
        if (!value.startsWith(suffix)) {
            return value;
        } else {
            return value.substring(suffix.length());
        }
    }

    public static String stripSuffix(String value, String suffix) {
        if (!value.endsWith(suffix)) {
            return value;
        } else {
            return value.substring(0, value.length() - suffix.length());
        }
    }

    private void init() {
        try {
            path = getRelativePath(Jenkins.getInstance().getRootDir(), getFile());
        } catch (IOException e) {
            throw new RuntimeException("Cannot calculate relative path " + e, e);
        }
        path = stripSuffix(path, "/build.xml");
        path = stripSuffix(path, "/config.xml");
        path = stripPrefix(path, "/jobs/");
        name = KubernetesNames.convertToKubernetesName(path, true);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }


}
