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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

/**
 */
public class XmlKubernetesBuild extends XmlKubernetesResource {
    private final NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;
    private final String namespace;

    public XmlKubernetesBuild(XStream xs, File file, NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient, String namespace) {
        super(xs, file);
        this.buildClient = buildClient;
        this.namespace = namespace;
    }

    @Override
    public boolean exists() {
        Build build = getPipeline();
        return build != null;
    }

    protected Build getPipeline() {
        return buildClient.withName(getName()).get();
    }


    @Override
    protected BufferedInputStream createInputStream() throws IOException {
        Build build = getPipeline();
        if (build == null) {
            throw new FileNotFoundException("Build resource not found in namespace " + namespace + " with name " + getName());
        }
        String configXml = null;
        BuildSpec spec = build.getSpec();
        if (spec != null) {
            configXml = spec.getConfigXml();
        }
        if (configXml == null) {
            configXml = "";
        }
        return new BufferedInputStream(new ByteArrayInputStream(configXml.getBytes()));
    }

    @Override
    public void delete() {
        buildClient.withName(getName()).delete();
    }

    @Override
    public void mkdirs() {
        // ignore
    }

    @Override
    public void write(Object o) throws IOException {
        StringWriter w = new StringWriter();
        w.write("<?xml version='1.0' encoding='UTF-8'?>\n");
        getXStream().toXML(o, w);
        w.close();
        String xml = w.toString();

        Build build = new Build();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(getName());
        metadata.setNamespace(namespace);
        build.setMetadata(metadata);
        BuildSpec spec = new BuildSpec();
        spec.setConfigXml(xml);
        spec.setPath(getPath());
        build.setSpec(spec);

        buildClient.createOrReplace(build);
    }
}
