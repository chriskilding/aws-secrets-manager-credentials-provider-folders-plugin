package io.jenkins.plugins.credentials.secretsmanager.folders.util.assertions;

import org.assertj.core.api.SoftAssertions;

public class CustomSoftAssertions extends SoftAssertions implements KeyStoreSoftAssertionsProvider,
        StandardCredentialsSoftAssertionsProvider, StandardCertificateCredentialsSoftAssertionsProvider {

}
