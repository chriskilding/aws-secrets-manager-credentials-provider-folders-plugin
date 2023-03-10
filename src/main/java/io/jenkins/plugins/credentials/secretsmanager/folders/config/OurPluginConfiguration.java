package io.jenkins.plugins.credentials.secretsmanager.folders.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

// FIXME put this into parent plugin
public class OurPluginConfiguration extends AbstractDescribableImpl<OurPluginConfiguration> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean cache;

    private Client client;

    private ListSecrets listSecrets;

    private Transformations transformations;

    @DataBoundConstructor
    public OurPluginConfiguration() {
        // empty DataBoundConstructor for Stapler (= all properties are optional)
    }

    public Boolean getCache() {
        return cache;
    }

    @DataBoundSetter
    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public Client getClient() {
        return client;
    }

    @DataBoundSetter
    public void setClient(Client client) {
        this.client = client;
    }

    public ListSecrets getListSecrets() {
        return listSecrets;
    }

    @DataBoundSetter
    public void setListSecrets(ListSecrets listSecrets) {
        this.listSecrets = listSecrets;
    }

    public Transformations getTransformations() {
        return transformations;
    }

    @DataBoundSetter
    public void setTransformations(Transformations transformations) {
        this.transformations = transformations;
    }

    public PluginConfiguration build() {
//         nulls are handled by the CredentialsSupplier
//         FIXME need to create a new InternalConfiguration POJO (to be used by the CredentialsSupplier)
//         FIXME need to avoid the unwanted XML load() and save() calls that the global PluginConfiguration does
        final var configuration = new PluginConfiguration();

        configuration.setCache(cache);
        configuration.setClient(client);
        configuration.setListSecrets(listSecrets);
        configuration.setTransformations(transformations);

        return configuration;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<OurPluginConfiguration> {

    }
}

