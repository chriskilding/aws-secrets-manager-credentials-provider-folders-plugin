package io.jenkins.plugins.credentials.secretsmanager.folders.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Allows the plugin to be configured at the folder level (rather than the global level)
 */
public class FolderPluginConfiguration extends AbstractFolderProperty<AbstractFolder<?>> {

    private Boolean cache;

    private Client client;

    private ListSecrets listSecrets;

    private Transformations transformations;

    public FolderPluginConfiguration() {
    }

    @DataBoundConstructor
    public FolderPluginConfiguration(Boolean cache, Client client, ListSecrets listSecrets, Transformations transformations) {
        this.cache = cache;
        this.client = client;
        this.listSecrets = listSecrets;
        this.transformations = transformations;
    }

    public Boolean getCache() {
        return cache;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public Client getClient() {
        return client;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setClient(Client client) {
        this.client = client;
    }

    public ListSecrets getListSecrets() {
        return listSecrets;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setListSecrets(ListSecrets listSecrets) {
        this.listSecrets = listSecrets;
    }

    public Transformations getTransformations() {
        return transformations;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setTransformations(Transformations transformations) {
        this.transformations = transformations;
    }


    public PluginConfiguration getConfiguration() {
        var configuration = new PluginConfiguration();
        configuration.setCache(cache);
        configuration.setClient(client);
        configuration.setListSecrets(listSecrets);
        configuration.setTransformations(transformations);
        return configuration;
    }

    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

    }
}
