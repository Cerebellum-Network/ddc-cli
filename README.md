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

You can configure ddc-cli (e.g. appPrivKey, cdnUrl, scheme) using next command:

```shell script
ddc-cli configure --appPrivKey APP_PRIV_KEY --cdnUrl http://localhost:8080 --scheme sr25519
```

## Content Addressable Storage

### Read

Read pieces by cid. Returns piece in JSON format or save to file (```-f``` - path to file).

```shell script
ddc-cli ca read -b 123 -c Qmf6mNYKEjYwA82PTJLfA4PjHAEq9QvRf4pTBURjkZYG2o
```

### Store

Store piece with required tags and data (```-f``` - path to file, ```-d``` - string data Base64 format)

```shell script
ddc-cli ca store -d YXNkYXNk -b 123 -t key=value -t key2=value
```

### Search

Search pieces by tags. Return pieces in JSON format.

```shell script
ddc-cli ca search -b 123 -t key=value
```

## Key-Value Storage

### Read

Read piece from Key-Value Storage with requred key value. Return pieces in JSON format.

```shell script
ddc-cli kv read -b 123 -k key
```

### Store

Store piece to Key-Value Storage with key and data (```-f``` - path to file, ```-d``` - string data Base64).

```shell script
ddc-cli kv store -d YXNkYXNk -k key -b 123 -t key=value -t key2=value
```