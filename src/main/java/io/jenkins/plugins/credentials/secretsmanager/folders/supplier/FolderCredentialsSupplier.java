package io.jenkins.plugins.credentials.secretsmanager.folders.supplier;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import io.jenkins.plugins.credentials.secretsmanager.folders.config.FolderPluginConfiguration;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;

public class FolderCredentialsSupplier {

    private final CredentialsSupplier credentialsSupplier = CredentialsSupplier.standard();

    public Collection<StandardCredentials> get(String folderName) {
        final var jenkins = Jenkins.get();

        final AbstractFolder<?> folder = jenkins.getItemByFullName(folderName, AbstractFolder.class);

        if (folder != null) {
            final var folderProperties = folder.getProperties();

            final var folderPluginConfiguration = folderProperties.get(FolderPluginConfiguration.class);

            // the Jenkins user may not have configured this plugin on the folder
            if (folderPluginConfiguration != null) {
                final var ourPluginConfiguration = folderPluginConfiguration.getPluginConfiguration();

                if (ourPluginConfiguration != null) {
                    return credentialsSupplier.get(ourPluginConfiguration.build());
                }
            }
        }

        return Collections.emptyList();
    }
}
