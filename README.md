# ddc-cli, the DDC command line interface

The ddc-cli allows to create app, produce and consume data in DDC.

## Building the CLI

To build native executable:

```
./gradlew build -Dquarkus.package.type=native 
```

## Installing the CLI

Download a binary file from [releases](https://github.com/Cerebellum-Network/ddc-cli/releases).

## Commands

### create-app

You can create application in DDC using next command (subscription in SC required):

```shell script
ddc-cli create-app --appPubKey APP_PUB_KEY --appPrivKey APP_PRIV_KEY
```

> **_NOTE:_**  Subscription in SC required (except dev environment where SC is mocked). If appPubkey and appPrivKey are not present - new app will be generated (dev only).

### configure

You can configure ddc-cli (e.g. bootstrapNodes, appPubKey and appPrivKey for data producing/consuming) using next
command:

```shell script
ddc-cli configure --bootstrapNodes http://localhost:8080 --appPubKey APP_PUB_KEY --appPrivKey APP_PRIV_KEY
```

### produce

To produce data to DDC (bootstrapNodes, appPubKey and appPrivKey from configuration are used):

```shell script
ddc-cli produce -d test_data -u test_user
```

### consume

To consume data from DDC (bootstrapNodes and appPubKey from configuration are used). Offset reset values are earliest and latest. Where 'earliest' means consume from beginning and 'latest' is a real-time (old data isn't consumed):

```shell script
ddc-cli consume --stream-id test_stream --fields=field1,field2 --offset-reset latest
```

### get-app-pieces

To get application pieces from DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-app-pieces --from 2021-07-22T09:56:06.849030Z --to 2021-07-22T09:56:49.849030Z --fields=field1,field2
```

### get-user-pieces

To get user pieces from DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-user-pieces -u aceba9c5-617e-4422-9520-c98fe66eb6e2 --from 2021-07-22T09:56:06.849030Z --to 2021-07-22T09:56:49.849030Z --fields=field1,field2
```

### get-piece

To get piece from DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-by-cid -u aceba9c5-617e-4422-9520-c98fe66eb6e2 -c Qmf6mNYKEjYwA82PTJLfA4PjHAEq9QvRf4pTBURjkZYG2o
```

### generate-load

To generate random load to DDC:

```shell script
ddc-cli generate-load -u 100 -n 30 -i pt5s -s 1000
```

### benchmark

To benchmark DDC node (define WCU and RCU parameters):

```shell script
ddc-cli benchmark
```