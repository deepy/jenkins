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
import hudson.model.TopLevelItem;
import hudson.XmlFileFactory;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import jenkins.model.Jenkins;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 */
public class KuberentesXmlFileFactory implements XmlFileFactory {
    private static final KubernetesClient kubernetesClient = new DefaultKubernetesClient();
    private static NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> pipelineClient;
    private static NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> runClient;

    public KuberentesXmlFileFactory() {
        watchResources();
    }

    protected static String getNamespace() {
        String namespace = kubernetesClient.getNamespace();
        if (namespace == null) {
            namespace = System.getenv("KUBERNETES_NAMESPACE");
        }
        if (namespace == null) {
            namespace = "default";
        }
        return namespace;
    }

    protected void watchResources() {
        String ns = getNamespace();
        NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> pipelineClient = getPipelineClient(ns);
        PipelineList bcList = pipelineClient.list();
        if (bcList != null) {
            List<Pipeline> items = bcList.getItems();
            if (items != null) {
                for (Pipeline item : items) {
                    onPipeline(item);
                }
            }
        }
        Watch pipelineWatcher = pipelineClient.watch(new Watcher<Pipeline>() {
            @Override
            public void eventReceived(Action action, Pipeline item) {
                onPipeline(item);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                // ignore
            }
        });


        NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> runs = getRunClient(ns);
        RunList runList = runs.list();
        if (runList != null) {
            List<Run> items = runList.getItems();
            if (items != null) {
                for (Run item : items) {
                    onRun(item);
                }
            }
        }
        Watch runWatcher = runs.watch(new Watcher<Run>() {
            @Override
            public void eventReceived(Action action, Run item) {
                onRun(item);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                // ignore
            }
        });
    }

    protected void onRun(Run item) {
        RunSpec spec = item.getSpec();
        ObjectMeta metadata = item.getMetadata();
        if (spec != null && metadata != null) {
            String path = spec.getPath();
            String configXml = spec.getConfigXml();
            if (configXml != null) {
                String name = metadata.getName();
                Jenkins jenkins = Jenkins.getInstance();
                List<TopLevelItem> items = jenkins.getItems();
                boolean found = false;
                for (TopLevelItem topLevelItem : items) {
                    if (name.equals(topLevelItem.getName())) {
                        System.out.println("Updated item " + name + " so reloading");
                        // TODO find better reload...
                        try {
                            jenkins.reload();
                        } catch (Exception e) {
                            // TODO
                        }
                        return;
                    }
                }
                if (!found) {
                    try {
                        jenkins.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes()));
                    } catch (IOException e) {
                        System.out.println("Failed to create Item: " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected void onPipeline(Pipeline item) {
        PipelineSpec spec = item.getSpec();
        ObjectMeta metadata = item.getMetadata();
        if (spec != null && metadata != null) {
            String path = spec.getPath();
            String configXml = spec.getConfigXml();
            if (configXml != null) {
                String name = metadata.getName();
                Jenkins jenkins = Jenkins.getInstance();
                List<TopLevelItem> items = jenkins.getItems();
                boolean found = false;
                for (TopLevelItem topLevelItem : items) {
                    if (name.equals(topLevelItem.getName())) {
                        System.out.println("Updated item " + name + " so reloading");
                        // TODO find better reload...
                        try {
                            jenkins.reload();
                        } catch (Exception e) {
                            // TODO
                        }
                        return;
                    }
                }
                if (!found) {
                    try {
                        jenkins.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes()));
                    } catch (IOException e) {
                        System.out.println("Failed to create Item: " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public XmlFile createItemFile(XStream xstream, File file) {
        String namespace = getNamespace();
        getPipelineClient(namespace);
        return new XmlKubernetesPipeline(xstream, file, pipelineClient, namespace);
    }

    @Override
    public XmlFile createRunFile(XStream xstream, File file) {
        String namespace = getNamespace();
        getRunClient(namespace);
        return new XmlKubernetesRun(xstream, file, runClient, namespace);
    }

    @Override
    public XmlFile createConfigXmlFile(File file) {
        String namespace = getNamespace();
        return new XmlKubernetesConfigFile(file, kubernetesClient, namespace);
    }

    @Override
    public XmlFile createQueueConfigXmlFile(File file) {
        return new XmlFile(file);
    }

    protected NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> getPipelineClient(String namespace) {
        if (pipelineClient == null) {
            pipelineClient = ClientHelper.pipelineClient(kubernetesClient, namespace);
        }
        return pipelineClient;
    }

    protected NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> getRunClient(String namespace) {
        if (runClient == null) {
            runClient = ClientHelper.runClient(kubernetesClient, namespace);
        }
        return runClient;
    }
}
