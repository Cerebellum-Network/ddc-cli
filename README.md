# ddc-cli, the DDC command line interface

The ddc-cli allows to create app, produce and consume data in DDC.

## Building the CLI

To build native executable:

```
./gradlew build -Dquarkus.package.type=native 
```

## Installing the CLI

Download a binary file from [releases](https://github.com/Cerebellum-Network/ddc-cli/releases).

## General Commands

### generate-keys

To generate a secret phrase (mnemonic code) and keypair (public and private keys):

```shell script
ddc-cli generate-keys
```

### extract-keys

To extracts keypair (public and private keys) from a secret phrase (mnemonic code):

```shell script
ddc-cli extract-keys --secret-phrase 'ivory immense card before water diesel illness soccer icon garbage exit claw '
```

### configure

You can configure ddc-cli (e.g. appPrivKey, gatewayUrl, scheme) using next
command:

```shell script
ddc-cli configure --appPrivKey APP_PRIV_KEY --gatewayUrl http://localhost:8080 --scheme sr25519
```

## Content Addressable Storage

### Read

```shell script
ddc-cli ca read -b 123 -c Qmf6mNYKEjYwA82PTJLfA4PjHAEq9QvRf4pTBURjkZYG2o
```

### Store

```shell script
ddc-cli ca store -d YXNkYXNk -b 123 -t key=value -t key2=value
```

### Search

```shell script
ddc-cli ca search -b 123 -t key=value
```

## Key-Value Storage

### Read

```shell script
ddc-cli kv read -b 123 -k key
```

### Store

```shell script
ddc-cli kv store -d YXNkYXNk -k key -b 123 -t key=value -t key2=value
```