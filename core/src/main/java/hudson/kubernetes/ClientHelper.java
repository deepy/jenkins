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
    public static String BUILDCONFIG_CRD_NAME = "buildconfigs." + JENKINS_CRD_GROUP;
    public static String BUILD_CRD_NAME = "builds." + JENKINS_CRD_GROUP;

    public static NonNamespaceOperation<BuildConfig, BuildConfigList, DoneableBuildConfig, Resource<BuildConfig, DoneableBuildConfig>> buildConfigClient(KubernetesClient client, String namespace) {
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        CustomResourceDefinition buildConfigCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (BUILDCONFIG_CRD_NAME.equals(name)) {
                    buildConfigCRD = crd;
                }
            }
        }
        if (buildConfigCRD == null) {
            buildConfigCRD = new CustomResourceDefinitionBuilder().
                    withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(BUILDCONFIG_CRD_NAME).endMetadata().
                    withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
                    withNewNames().withKind("BuildConfig").withShortNames("bc").withPlural("buildconfigs").endNames().endSpec().
                    build();

            client.customResourceDefinitions().create(buildConfigCRD);
        }
        return client.customResource(buildConfigCRD, BuildConfig.class, BuildConfigList.class, DoneableBuildConfig.class).inNamespace(namespace);
    }

    public static NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient(KubernetesClient client, String namespace) {
        CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
        List<CustomResourceDefinition> crdsItems = crds.getItems();
        CustomResourceDefinition buildCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
                if (BUILD_CRD_NAME.equals(name)) {
                    buildCRD = crd;
                }
            }
        }
        if (buildCRD == null) {
            buildCRD = new CustomResourceDefinitionBuilder().
                    withApiVersion("apiextensions.k8s.io/v1beta1").
                    withNewMetadata().withName(BUILD_CRD_NAME).endMetadata().
                    withNewSpec().withGroup(JENKINS_CRD_GROUP).withVersion("v1").withScope("Namespaced").
                    withNewNames().withKind("Build").withShortNames("build").withPlural("builds").endNames().endSpec().
                    build();

            client.customResourceDefinitions().create(buildCRD);
        }
        return client.customResource(buildCRD, Build.class, BuildList.class, DoneableBuild.class).inNamespace(namespace);
    }

}
