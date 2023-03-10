package io.jenkins.plugins.credentials.secretsmanager.folders.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import io.jenkins.plugins.credentials.secretsmanager.config.PluginConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Allows the plugin to be configured at the folder level (rather than the global level)
 */
// FIXME extract PluginConfiguration to interface, so that GlobalPluginConfiguration and FolderPluginConfiguration can inherit it
public class FolderPluginConfiguration extends AbstractFolderProperty<AbstractFolder<?>> {

    private OurPluginConfiguration pluginConfiguration;

    @DataBoundConstructor
    public FolderPluginConfiguration() {
        // empty DataBoundConstructor for Stapler (= all properties are optional)
    }

    public OurPluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    @DataBoundSetter
    public void setPluginConfiguration(OurPluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }

    public PluginConfiguration build() {
        if (pluginConfiguration == null) {
            return new PluginConfiguration();
        }

        return pluginConfiguration.build();
    }

    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

    }
}
