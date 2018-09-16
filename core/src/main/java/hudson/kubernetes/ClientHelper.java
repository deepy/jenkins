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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

import java.util.List;

/**
 */
public class ClientHelper {

    public static String JENKINS_CRD_GROUP = "jenkins.io";
    public static String PIPELINE_CRD_NAME = "pipelines." + JENKINS_CRD_GROUP;
    public static String RUN_CRD_NAME = "runs." + JENKINS_CRD_GROUP;

    public static NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> pipelineClient(KubernetesClient client, String namespace) {
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        CustomResourceDefinition pipelineCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (PIPELINE_CRD_NAME.equals(name)) {
                    pipelineCRD = crd;
                }
            }
        }
        if (pipelineCRD == null) {
            pipelineCRD = new CustomResourceDefinitionBuilder().
                    withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(PIPELINE_CRD_NAME).endMetadata().
                    withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
                    withNewNames().withKind("Pipeline").withShortNames("pipeline").withPlural("pipelines").endNames().endSpec().
                    build();

            client.customResourceDefinitions().create(pipelineCRD);
        }
        return client.customResource(pipelineCRD, Pipeline.class, PipelineList.class, DoneablePipeline.class).inNamespace(namespace);
    }

    public static NonNamespaceOperation<Run, RunList, DoneableRun, Resource<Run, DoneableRun>> runClient(KubernetesClient client, String namespace) {
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        CustomResourceDefinition runCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (RUN_CRD_NAME.equals(name)) {
                    runCRD = crd;
                }
            }
        }
        if (runCRD == null) {
            runCRD = new CustomResourceDefinitionBuilder().
                    withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(RUN_CRD_NAME).endMetadata().
                    withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
                    withNewNames().withKind("Run").withShortNames("run").withPlural("runs").endNames().endSpec().
                    build();

            client.customResourceDefinitions().create(runCRD);
        }
        return client.customResource(runCRD, Run.class, RunList.class, DoneableRun.class).inNamespace(namespace);
    }

}
