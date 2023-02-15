package io.jenkins.plugins.credentials.secretsmanager.folders;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.AWSSecretsManagerRule;
import io.jenkins.plugins.credentials.secretsmanager.folders.util.Rules;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CredentialsStoreIT {

    private static final String SECRET = "supersecret";

    public final JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();
    public final AWSSecretsManagerRule secretsManager = new AWSSecretsManagerRule();

    @Rule
    public final TestRule chain = Rules.jenkinsWithSecretsManager(jenkins, secretsManager);

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldSupportGets() {
        // Given
        final var store = lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        // When
        final var credentials = store.getCredentials(Domain.global());

        // Then
        assertThat(credentials).isEmpty();
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportUpdates() {
        final var credential = new StringCredentialsImpl(CredentialsScope.GLOBAL,"foo", "desc", Secret.fromString(SECRET));

        final var store = lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.updateCredentials(Domain.global(), credential, credential))
                .withMessage("Jenkins may not update credentials in AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportInserts() {
        final var store = lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.addCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not add credentials to AWS Secrets Manager");
    }

    @Test
    @ConfiguredWithCode(value = "/integration.yml")
    public void shouldNotSupportDeletes() {
        final var store = lookupStore(AwsSecretsManagerFolderCredentialsStore.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> store.removeCredentials(Domain.global(), new StringCredentialsImpl(CredentialsScope.GLOBAL, "foo", "desc", Secret.fromString(SECRET))))
                .withMessage("Jenkins may not remove credentials from AWS Secrets Manager");
    }

    private <C extends CredentialsStore> CredentialsStore lookupStore(Class<C> type) {
        final var itStores = CredentialsProvider.lookupStores(jenkins);

        return StreamSupport.stream(itStores.spliterator(), false)
                .filter(store -> store.getClass().equals(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find the specified CredentialsStore"));
    }
}
