package io.jenkins.plugins.credentials.secretsmanager.folders.util.assertions;

import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class CustomAssertions {

    public static StringCredentialsAssert assertThat(StringCredentials actual) {
        return new StringCredentialsAssert(actual);
    }

    public static ListBoxModelAssert assertThat(ListBoxModel actual) {
        return new ListBoxModelAssert(actual);
    }

}
