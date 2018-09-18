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
import hudson.model.Item;
import hudson.model.Job;
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
public class XmlKubernetesRun extends XmlKubernetesResource {
    private final NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> runClient;
    private final String namespace;

    public XmlKubernetesRun(XStream xs, File file, NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> runClient, String namespace) {
        super(xs, file);
        this.runClient = runClient;
        this.namespace = namespace;
    }

    @Override
    public boolean exists() {
        Run run = getPipeline();
        return run != null;
    }

    protected Run getPipeline() {
        return runClient.withName(getName()).get();
    }


    @Override
    protected BufferedInputStream createInputStream() throws IOException {
        Run run = getPipeline();
        if (run == null) {
            throw new FileNotFoundException("Run resource not found in namespace " + namespace + " with name " + getName());
        }
        String configXml = null;
        RunSpec spec = run.getSpec();
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
        runClient.withName(getName()).delete();
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

        Run run = new Run();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(getName());
        metadata.setNamespace(namespace);
        run.setMetadata(metadata);
        RunSpec spec = new RunSpec();
        spec.setConfigXml(xml);
        spec.setPath(getPath());
        if (o instanceof hudson.model.Run) {
            hudson.model.Run item = (hudson.model.Run) o;
            Job parent = item.getParent();
            if (parent != null) {
                spec.setJobFullName(parent.getFullName());
            }
            spec.setNumber(item.getNumber());
            spec.setFullDisplayName(item.getFullDisplayName());
            spec.setUrl(item.getUrl());
        }
        run.setSpec(spec);

        runClient.createOrReplace(run);
    }
}
