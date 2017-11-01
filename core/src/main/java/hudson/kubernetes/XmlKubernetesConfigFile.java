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

import hudson.XmlFile;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class XmlKubernetesConfigFile extends XmlFile {
    private final KubernetesClient kubernetesClient;
    private final String namespace;
    private final String name;
    private final String key;

    public XmlKubernetesConfigFile(File file, KubernetesClient kubernetesClient, String namespace) {
        super(file);
        this.kubernetesClient = kubernetesClient;
        this.namespace = namespace;
        this.key = file.getName();
        this.name = "jenkins-config";
    }


    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    protected BufferedInputStream createInputStream() throws IOException {
        String xml = getXml();
        if (xml == null) {
            xml = "";
        }
        return new BufferedInputStream(new ByteArrayInputStream(xml.getBytes()));
    }

    private String getXml() {
        ConfigMap configMap = configMapClient().get();
        String xml = null;
        if (configMap != null) {
            Map<String, String> data = configMap.getData();
            if (data != null) {
                xml = data.get(getKey());
            }
        }
        return xml;
    }

    protected Resource<ConfigMap, DoneableConfigMap> configMapClient() {
        return getKubernetesClient().configMaps().inNamespace(namespace).withName(name);
    }

    @Override
    public boolean exists() {
        String xml = getXml();
        return xml != null;
    }

    @Override
    public void delete() {
        configMapClient().delete();
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

        ConfigMap configMap = configMapClient().get();
        if (configMap == null) {
            configMap = new ConfigMapBuilder().withNewMetadata().withName(name).endMetadata().build();
        }
        Map<String, String> data = configMap.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, xml);
        configMap.setData(data);
        configMapClient().createOrReplace(configMap);
    }
}
