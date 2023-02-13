package io.jenkins.plugins.credentials.secretsmanager.folders;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import io.jenkins.plugins.credentials.secretsmanager.folders.config.FolderPluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.folders.supplier.CredentialsSupplier;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Extension
public class AwsSecretsManagerFolderCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AwsSecretsManagerFolderCredentialsProvider.class.getName());

    private final AwsSecretsManagerFolderCredentialsStore store = new AwsSecretsManagerFolderCredentialsStore(this);

    // FIXME re-enable memoizer / caching
    private final CredentialsSupplier credentialsSupplier = CredentialsSupplier.standard();

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          ItemGroup itemGroup,
                                                          Authentication authentication) {


        final var creds = new ArrayList<C>();

        if (ACL.SYSTEM.equals(authentication)) {
            LOG.info(() -> "AWS Credentials Provider folder-scoped lookup");

            // Reverse hierarchical traversal - if no config found in this folder, go to the parent
            // FIXME does not work yet, because the passed-in itemGroup is not an AbstractFolder
            for (ItemGroup g = itemGroup; g instanceof AbstractFolder; g = (AbstractFolder.class.cast(g)).getParent()) {
                var folder = ((AbstractFolder<?>) g);
                final var folderProperties = folder.getProperties();
                final var folderPluginConfiguration = folderProperties.get(FolderPluginConfiguration.class);
                final var folderName = folder.getName();

                LOG.info(() -> "Checking plugin configuration for folder " + folderName);

                final var pluginConfiguration = folderPluginConfiguration.getConfiguration();

                if (pluginConfiguration == null) {
                    LOG.info(() -> "No plugin configuration found for folder " + folderName);
                    continue;
                }

                final var folderCredentials = credentialsSupplier.get(pluginConfiguration);

                // FIXME Associate the credential object with the folder
//                for (StandardCredentials c : folderCreds) {
//                    ((AbstractVaultBaseStandardCredentials) c).setContext(g);
//                }

                folderCredentials.stream()
                        .filter(c -> type.isAssignableFrom(c.getClass()))
                        .map(type::cast)
                        .forEach(creds::add);
            }
        }

        return Collections.unmodifiableList(creds);
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        return object == Jenkins.get() ? store : null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }

    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Supplier<Duration> duration) {
        return CustomSuppliers.memoizeWithExpiration(base, duration);
    }
}