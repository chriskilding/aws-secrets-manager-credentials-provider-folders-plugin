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
import io.jenkins.plugins.credentials.secretsmanager.folders.supplier.FolderCredentialsSupplier;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Extension
public class AwsSecretsManagerFolderCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AwsSecretsManagerFolderCredentialsProvider.class.getName());

    // FIXME re-enable memoizer / caching
    // Note that the cache structure must be THREAD-SAFE because multiple threads could call CredentialsProvider#getCredentials below
    private final FolderCredentialsSupplier credentialsSupplier = new FolderCredentialsSupplier();

    @Override
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@Nonnull Class<C> type,
                                                          ItemGroup itemGroup,
                                                          Authentication authentication) {
        if (ACL.SYSTEM.equals(authentication)) {
            if (itemGroup instanceof AbstractFolder<?>) {
                LOG.fine(() -> "AWS Secrets Manager Credentials Provider folder-scoped lookup");

                final var folderName = itemGroup.getFullName();

                final var folderCredentials = credentialsSupplier.get(folderName);

                return folderCredentials.stream()
                        .filter(c -> type.isAssignableFrom(c.getClass()))
                        .map(type::cast)
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public CredentialsStore getStore(ModelObject object) {
        if (object instanceof ItemGroup<?>) {
            final var itemGroup = (ItemGroup<?>) object;
            return new AwsSecretsManagerFolderCredentialsStore(this, itemGroup);
        }

        return null;
    }

    @Override
    public String getIconClassName() {
        return "icon-aws-secrets-manager-credentials-store";
    }

    private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Supplier<Duration> duration) {
        return CustomSuppliers.memoizeWithExpiration(base, duration);
    }
}