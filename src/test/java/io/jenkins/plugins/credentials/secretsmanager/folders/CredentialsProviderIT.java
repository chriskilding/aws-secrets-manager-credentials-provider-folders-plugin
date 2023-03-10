package io.jenkins.plugins.credentials.secretsmanager.folders;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.AwsTags;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.CredentialNames;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.Rules;
import jenkins.model.Jenkins;
import org.assertj.core.api.Assertions;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static io.jenkins.plugins.credentials.secretsmanager.folders.util.assertions.CustomAssertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CredentialsProviderIT {

    private static final String SECRET = "supersecret";

    public final JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/default.yml")
    public void shouldBeEmptyWhenProviderNotConfigured() {
        // Given
        final var folder = getFolder("foo");

        // When
        final var credentials = lookupCredentials(StringCredentials.class, folder);

        // Then
        Assertions.assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldBeEmptyWhenAwsHasNoSecrets() {
        // Given
        final var folder = getFolder("foo");

        // When
        final var credentials = lookupCredentials(StringCredentials.class, folder);

        // Then
        Assertions.assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportLookupCredentials() {
        // Given
        final var secret = createStringSecret(SECRET);
        // And
        final var folder = getFolder("foo");

        // When
        final var credentials = lookupCredentials(StringCredentials.class, folder);
        final var c = credentials.get(0);

        // Then
        assertThat(c)
                .hasId(secret.getName())
                .hasSecret(SECRET);
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportListCredentials() {
        // Given
        final var secret = createStringSecret(SECRET);
        // And
        final var folder = getFolder("foo");

        // When
        final var credentialNames = listCredentials(StringCredentials.class, folder);

        // Then
        assertThat(credentialNames)
                .containsOption(secret.getName(), secret.getName());
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldFolderScopeItsCredentials() {
        // Given
        createStringSecret(SECRET);
        // And
        final var folder = getFolder("foo");

        // When
        final var globalCredentials = lookupCredentials(StringCredentials.class, Jenkins.get());
        final var folderScopedCredentials = lookupCredentials(StringCredentials.class, folder);

        // Then
        assertSoftly(s -> {
            s.assertThat(globalCredentials).as("Global").isEmpty();
            s.assertThat(folderScopedCredentials).as("Folder-scoped").hasSize(1);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/nested.yml")
    public void shouldSupportNestedFolders() {
        // Given
        final var secret = createStringSecret(SECRET);
        // And
        final var folder = getFolder("foo/bar");

        // When
        final var credentials = lookupCredentials(StringCredentials.class, folder);
        final var c = credentials.get(0);

        // Then
        assertThat(c)
                .hasId(secret.getName())
                .hasSecret(SECRET);
    }

    private static <C extends StandardCredentials> List<C> lookupCredentials(Class<C> type, ItemGroup<?> itemGroup) {
        return CredentialsProvider.lookupCredentials(type, itemGroup, ACL.SYSTEM, List.of());
    }

    private static <C extends StandardCredentials> ListBoxModel listCredentials(Class<C> type, ItemGroup<?> itemGroup) {
        return CredentialsProvider.listCredentials(type, itemGroup, null, null, null);
    }

    private CreateSecretResult createStringSecret(String secretString) {
        final var tags = List.of(AwsTags.type(Type.string));

        return createSecret(secretString, tags);
    }

    private CreateSecretResult createSecret(String secretString, List<Tag> tags) {
        final var request = new CreateSecretRequest()
                .withName(CredentialNames.random())
                .withSecretString(secretString)
                .withTags(tags);

        return secretsManager.getClient().createSecret(request);
    }

    private AbstractFolder<?> getFolder(String name) {
        var folder = jenkins.jenkins.getItemByFullName(name);

        if (folder instanceof AbstractFolder) {
            return (AbstractFolder<?>) folder;
        }
        return null;
    }
}
