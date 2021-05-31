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
ddc-cli create-app --appPubKey APP_PUB_KEY --appPrivKey APP_PRIV_KEY --tierId 2
```

> **_NOTE:_**  Subscription in SC required (except dev environment where SC is mocked). If appPubkey and appPrivKey are not present - new app will be generated (dev only).

### configure

You can configure ddc-cli (e.g. bootstrapNodes, appPubKey and appPrivKey for data producing/consuming) using next command:
```shell script
ddc-cli configure --bootstrapNodes http://localhost:8080 --appPubKey APP_PUB_KEY --appPrivKey APP_PRIV_KEY
```

### produce

To produce data to DDC (bootstrapNodes, appPubKey and appPrivKey from configuration are used):
```shell script
ddc-cli produce -d test_data -u test_user
```

### consume

To consume data from DDC (bootstrapNodes and appPubKey from configuration are used):
```shell script
ddc-cli consume --stream-id filtered_stream --from 2021-01-01T00:00:00.000Z --to 2021-01-01T12:00:00.000Z --fields=field1,field2
```
