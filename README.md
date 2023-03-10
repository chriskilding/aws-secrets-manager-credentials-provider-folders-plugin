# aws-secrets-manager-credentials-provider-folders-plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/aws-secrets-manager-credentials-provider-folders-plugin/main)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Faws-secrets-manager-credentials-provider-folders-plugin/activity/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/aws-secrets-manager-credentials-provider-folders.svg)](https://plugins.jenkins.io/aws-secrets-manager-credentials-provider-folders)

Folders support for the [AWS Secrets Manager Credentials Provider](https://github.com/jenkinsci/aws-secrets-manager-credentials-provider-plugin) plugin.

:warning: This plugin is **experimental**. It should not be used in a production environment. If you want to try it, you will need to build it from source.

## Overview

This plugin extends the AWS Secrets Manager Credentials Provider plugin to provide folder-scoped credentials. These are credentials which are only visible to jobs in a particular folder (and its subfolders).

This is primarily helpful in the following use cases:

- **Multi-tenancy.** In this use case, you have multiple products (or product teams) on a shared Jenkins. Each product is assigned its own folder on Jenkins. This plugin allows product-scoped credentials, which are only visible to jobs in the product team's folder.
- **Multi-environment.** In this use case, you have one Jenkins server that orchestrates jobs for multiple environments (e.g. staging, production). Each environment is assigned its own folder on Jenkins. This plugin allows environment-scoped credentials, so for example the staging credentials are only visible to jobs in the staging folder, and likewise for production.

In these use cases, the fundamental unit of organization is normally the AWS account. (E.g. each product has its own AWS account, with its own secrets in Secrets Manager.) In each Jenkins folder, you will likely want to configure the credentials provider to assume a role in the corresponding AWS account.

## Setup

1. Build the plugin from source (see the [Development](#Development) instructions below).
2. Install the built plugin `.hpi` on Jenkins.
3. Configure IAM.
4. Configure the plugin on the relevant Jenkins folders (see the [Configuration](#Configuration) instructions below).

Setup is very similar to the AWS Secrets Manager Credentials Provider plugin. The main differences are:

- This plugin is folder-scoped, so you will need to configure it on each folder where you want to have folder-scoped credentials.
- If you use this plugin to connect to Secrets Manager in multiple AWS accounts, you will need to do the IAM configuration in each of the remote AWS accounts.

## Usage

The plugin allows secrets from Secrets Manager to be used as Jenkins credentials. It behaves the same as the upstream AWS Secrets Manager Credentials Provider plugin, except that credentials are scoped to folders (i.e. they are only available to jobs in their respective folder).

Please see the AWS Secrets Manager Credentials Provider plugin README for usage instructions.

## Configuration

The plugin has the same configuration options as the AWS Secrets Manager Credentials Provider plugin. The only difference is that the configuration is set at the folder level, not at the global level.

### Web UI

You can set plugin configuration at the folder level using the Web UI.

Go to `Jenkins` > `<folder>` > `Configure` > `AWS Secrets Manager Credentials Provider`, tick the box to enable it for `<folder>`, and change the settings.

### Configuration As Code (CasC)

You can set plugin configuration at the folder level using Jenkins [Configuration As Code](https://github.com/jenkinsci/configuration-as-code-plugin).

To use CasC with folders, you MUST also have the Job DSL Plugin installed.

Example:

```yaml
jobs:
  - script: >
      folder('<folder>') {
        description('This folder is configured as code')
        configure {
          it / 'properties' / 'io.jenkins.plugins.credentials.secretsmanager.folders.config.FolderPluginConfiguration' / 'pluginConfiguration' / 'client' / 'endpointConfiguration' / serviceEndpoint << 'https://example.com'
          it / 'properties' / 'io.jenkins.plugins.credentials.secretsmanager.folders.config.FolderPluginConfiguration' / 'pluginConfiguration' / 'client' / 'endpointConfiguration' / signingRegion << 'us-east-1'
        }
      }
```

## Versioning

This plugin is currently **experimental**, so it does not have any stable releases. When it does, a versioning policy will be added here.

## Development

### Git

Start by cloning the project.

**Note for Windows users:** some file paths in this project may exceed the legacy Win32 path length limit. This may cause an error when cloning the project on Windows. If you see this error, enable Git's Windows longpaths support with `git config --system core.longpaths true` (you might need to run Git as Administrator for this to work). Then try to clone the project again.

### Dependencies

- Docker
- Java
- Maven

### Build

In Maven:

```shell script
mvn clean verify
```

In your IDE:

1. Generate translations: `mvn localizer:generate`. (This is a one-off task. You only need to re-run this if you change the translations, or if you clean the Maven `target` directory. If the IDE still cannot find the translation symbols after running `mvn localizer:generate`, use a one-off `mvn compile` instead.)
2. Compile.
3. Run tests.

### Run

You can explore how the plugin works by running it locally with [Moto](https://github.com/getmoto/moto) (the AWS mock)...

Start Moto:

```shell
docker run -it -p 5000:5000 motoserver/moto:3.1.18
```

Upload some fake secrets to Moto (like these):

```shell
aws --endpoint-url http://localhost:5000 secretsmanager create-secret --name 'example-api-key' --secret-string '123456' --tags 'Key=jenkins:credentials:type,Value=string' --description 'Example API key'
```

Start Jenkins with the plugin:

```shell
mvn hpi:run
```

Create a new folder `<folder>`.

Edit the folder's Secrets Manager configuration (go to `Jenkins` -> `<folder>` -> `Configure`) to use Moto:

1. Tick the box to enable the Secrets Manager folder-scoped provider on `<folder>`
2. Enable the `Endpoint Configuration` option
3. Set `Service Endpoint` to `http://localhost:5000`
4. Set `Signing Region` to `us-east-1`
5. Click `Save`
6. Try loading the folder-scoped credentials that have come from Moto, or using them in Jenkins jobs.
