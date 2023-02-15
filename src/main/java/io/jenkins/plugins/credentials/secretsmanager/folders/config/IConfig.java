package io.jenkins.plugins.credentials.secretsmanager.folders.config;

import io.jenkins.plugins.credentials.secretsmanager.config.Client;
import io.jenkins.plugins.credentials.secretsmanager.config.ListSecrets;
import io.jenkins.plugins.credentials.secretsmanager.config.Transformations;

public interface IConfig {
    Boolean getCache();

    void setCache(Boolean cache);

    Client getClient();

    void setClient(Client client);

    ListSecrets getListSecrets();

    void setListSecrets(ListSecrets listSecrets);

    Transformations getTransformations();

    void setTransformations(Transformations transformations);
}
