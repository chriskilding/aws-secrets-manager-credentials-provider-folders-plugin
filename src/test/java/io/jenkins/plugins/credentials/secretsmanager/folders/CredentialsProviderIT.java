package io.jenkins.plugins.credentials.secretsmanager.folders;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.credentials.secretsmanager.factory.Type;
import io.jenkins.plugins.credentials.secretsmanager.folders.config.FolderPluginConfiguration;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * The plugin should support CredentialsProvider usage to list available credentials.
 */
public class CredentialsProviderIT {

    private static final String SECRET = "supersecret";

    public final MyJenkinsConfiguredWithCodeRule jenkins = new MyJenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldStartEmpty() {
        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/folders/single.yml")
    public void shouldHaveFolderLevelConfiguration() {
        // When
        final var folderPluginConfiguration = getFolderConfiguration("foo");
        final var actual = folderPluginConfiguration.getConfiguration().getClient().getEndpointConfiguration();

        // Then
        assertSoftly(s -> {
            s.assertThat(actual.getServiceEndpoint()).isEqualTo("https://example.com");
            s.assertThat(actual.getSigningRegion()).isEqualTo("us-east-1");
        });
    }

    @Test
    @ConfiguredWithCode(value = "/folders/multiple.yml")
    public void shouldSupportMultipleFolders() {
        // When
        final var foo = getFolderConfiguration("foo");
        final var fooServiceEndpoint = foo.getConfiguration().getClient().getEndpointConfiguration().getServiceEndpoint();

        final var bar = getFolderConfiguration("bar");
        final var barServiceEndpoint = bar.getConfiguration().getClient().getEndpointConfiguration().getServiceEndpoint();

        // Then
        assertSoftly(s -> {
            s.assertThat(fooServiceEndpoint).as("Foo").isEqualTo("https://example.com/foo");
            s.assertThat(barServiceEndpoint).as("Bar").isEqualTo("https://example.com/bar");
        });
    }

    @Ignore
    @ConfiguredWithCode(value = "/default.yml")
    public void shouldFailGracefullyWhenSecretsManagerUnavailable() {
        // When
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials).isEmpty();
    }

    @Ignore
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldUseSecretNameAsCredentialName() {
        // Given
        final var secret = createStringSecret(SECRET);

        // When
        final var credentialNames = jenkins.getCredentials().list(StringCredentials.class);

        // Then
        assertThat(credentialNames)
                .extracting("name")
                .containsOnly(secret.getName());
    }

    @Ignore
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateDeletedCredentials() {
        // Given
        final var foo = createStringSecret(SECRET);
        final var bar = createStringSecret(SECRET);

        // When
        deleteSecret(bar.getName());
        final var credentials = lookup(StringCredentials.class);

        // Then
        assertThat(credentials)
                .extracting("id", "secret")
                .containsOnly(tuple(foo.getName(), Secret.fromString(SECRET)));
    }

    @Ignore
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldTolerateRecentlyDeletedCredentials() {
        // Given
        final var foo = createStringSecret(SECRET);
        final var bar = createStringSecret(SECRET);

        // When
        final var credentials = lookup(StringCredentials.class);
        deleteSecret(bar.getName());

        // Then
        final var fooCreds = credentials.stream().filter(c -> c.getId().equals(foo.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));
        final var barCreds = credentials.stream().filter(c -> c.getId().equals(bar.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Needed a credential, but it did not exist"));

        assertSoftly(s -> {
            s.assertThat(fooCreds.getSecret()).as("Foo").isEqualTo(Secret.fromString(SECRET));
            s.assertThatThrownBy(barCreds::getSecret).as("Bar").isInstanceOf(CredentialsUnavailableException.class);
        });
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final var credential = new StringCredentialsImpl(CredentialsScope.GLOBAL,"foo", "desc", Secret.fromString(SECRET));

        final var store = jenkins.getCredentials().lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        final var store = jenkins.getCredentials().lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        final var store = jenkins.getCredentials().lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

    private <C extends StandardCredentials> List<C> lookup(Class<C> type) {
        return jenkins.getCredentials().lookup(type);
    }

    private void deleteSecret(String secretId) {
        final var request = new DeleteSecretRequest().withSecretId(secretId);
        secretsManager.getClient().deleteSecret(request);
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

    private FolderPluginConfiguration getFolderConfiguration(String name) {
        final var folder = getFolder(name);
        final var folderProperties = folder.getProperties();
        return folderProperties.get(FolderPluginConfiguration.class);
    }

    private AbstractFolder<?> getFolder(String name) {
        var folder = jenkins.jenkins.getItem(name);

        if (folder instanceof AbstractFolder) {
            return (AbstractFolder<?>) folder;
        }
        return null;
    }
}
