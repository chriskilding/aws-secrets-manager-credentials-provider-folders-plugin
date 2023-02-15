package io.jenkins.plugins.credentials.secretsmanager.folders.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class FolderPluginConfigurationIT {

    @Rule
    public final JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode(value = "/folders/single.yml")
    public void shouldHaveFolderLevelConfiguration() {
        // When
        final var folderPluginConfiguration = getFolderConfiguration("foo");
        final var actual = folderPluginConfiguration.build().getClient().getEndpointConfiguration();

        // Then
        assertThat(actual.getServiceEndpoint()).isEqualTo("https://example.com");
    }

    @Test
    @ConfiguredWithCode(value = "/folders/multiple.yml")
    public void shouldSupportMultipleFolders() {
        // When
        final var foo = getFolderConfiguration("foo");
        final var fooServiceEndpoint = foo.build().getClient().getEndpointConfiguration().getServiceEndpoint();

        final var bar = getFolderConfiguration("bar");
        final var barServiceEndpoint = bar.build().getClient().getEndpointConfiguration().getServiceEndpoint();

        // Then
        assertSoftly(s -> {
            s.assertThat(fooServiceEndpoint).as("Foo").isEqualTo("https://example.com/foo");
            s.assertThat(barServiceEndpoint).as("Bar").isEqualTo("https://example.com/bar");
        });
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
