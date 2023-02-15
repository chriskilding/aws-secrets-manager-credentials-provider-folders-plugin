package io.jenkins.plugins.credentials.secretsmanager.folders.util;

import com.amazonaws.services.secretsmanager.model.Tag;
import io.jenkins.plugins.credentials.secretsmanager.factory.Tags;

/**
 * Tags that the Jenkins plugin looks for.
 */
public abstract class AwsTags {
    private AwsTags() {

    }

    public static Tag type(String type) {
        return tag(Tags.type, type);
    }

    public static Tag tag(String key, String value) {
        return new Tag().withKey(key).withValue(value);
    }
}
