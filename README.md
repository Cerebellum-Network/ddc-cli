# ddc-cli, the DDC command line interface

The ddc-cli allows to create app, produce and consume data in DDC.

## Building the CLI

To build native executable:

```
./gradlew build -Dquarkus.package.type=native 
```

## Installing the CLI

Download a binary file from [releases](https://github.com/Cerebellum-Network/ddc-cli/releases).

##  General Commands

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

You can configure ddc-cli (e.g. bootstrapNodes, appPubKey and appPrivKey for data producing/consuming) using next
command:

```shell script
ddc-cli configure --bootstrapNodes http://localhost:8080 --bootstrapNodeIds 12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT --appPubKey APP_PUB_KEY --appPrivKey APP_PRIV_KEY
```

### create-app

You can create application in Behaviour DDC using next command (bootstrapNodes, appPubKey and appPrivKey from configuration are used) :

```shell script
ddc-cli create-app
```

> **_NOTE:_**  Subscription in SC required (except dev environment where SC is mocked). If appPubkey and appPrivKey are not present - new app will be generated (dev only).

## Behaviour storage

### produce

To produce data to Behaviour DDC (bootstrapNodes, appPubKey and appPrivKey from configuration are used):

```shell script
ddc-cli produce -d test_data -u test_user
```

### consume

To consume data from Behaviour DDC (bootstrapNodes and appPubKey from configuration are used). Offset reset values are earliest and latest. Where 'earliest' means consume from beginning and 'latest' is a real-time (old data isn't consumed):

```shell script
ddc-cli consume --stream-id test_stream --fields=field1,field2 --offset-reset latest
```

### get-app-pieces

To get application pieces from Behaviour DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-app-pieces --from 2021-07-22T09:56:06.849030Z --to 2021-07-22T09:56:49.849030Z --fields=field1,field2
```

### get-user-pieces

To get user pieces from Behaviour DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-user-pieces -u aceba9c5-617e-4422-9520-c98fe66eb6e2 --from 2021-07-22T09:56:06.849030Z --to 2021-07-22T09:56:49.849030Z --fields=field1,field2
```

### get-piece

To get piece from Behaviour DDC (bootstrapNodes and appPubKey from configuration are used):

```shell script
ddc-cli get-by-cid -u aceba9c5-617e-4422-9520-c98fe66eb6e2 -c Qmf6mNYKEjYwA82PTJLfA4PjHAEq9QvRf4pTBURjkZYG2o
```

### generate-load

To generate random load to behaviour DDC:

```shell script
ddc-cli generate-load -u 100 -n 30 -i pt5s -s 1000
```

### benchmark

To benchmark behaviour DDC node (define WCU and RCU parameters):

```shell script
ddc-cli benchmark
```

## NFT Storage

### store-asset

To store asset in Base64 format

```shell script
ddc-cli nft-storage store-asset -i someNftId -d Base64_data -n image.jpeg
```

### read asset

To read asset  in Base64 format

```shell script
ddc-cli nft-storage read-asset -i someNftId -u cns:///someCid/image.jpeg
```

### store metadata

To store metadata ERC-721 format

```shell script
ddc-cli nft-storage store-metadata -i someNftId -s ERC-721 -d '{"name":"metadata","description":"some metadata","image":"cns:///someIMage.jpeg"}'
```

### read metadata

To read metadata

```shell script
ddc-cli nft-storage read-metadata -i someNftId -u cns:///someCid/metadata.json
```

### store EDEK

To store EDEK

```shell script
ddc-cli nft-storage store-edek -i someNftId -u cns:///metadataCid/metadata.json -k 0x37520s8dgy892h890 -v asd23dfh456urytkj567
```

### read EDEK

To read EDEK

```shell script
ddc-cli nft-storage store-edek -i someNftId -u cns:///metadataCid/metadata.json -k 0x37520s8dgy892h890
```

### generate-load

To generate random load to storage:

```shell script
ddc-cli nft-storage generate-load -u 100 -n 30 -int pt5s -s 1000 -i someNftId
```

### benchmark

To benchmark storage:

```shell script
ddc-cli nft-storage benchmark -i someNftId
```