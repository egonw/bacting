# bacting
Bacting := acting as the Bioclipse TNG

The installation is described in the [INSTALL.md](INSTALL.md) file.

## Install

```shell
mvn clean install
```

## Usage

It can be used in Groovy by including the Bacting managers you need:

```groovy
@Grab(group='net.bioclipse.managers', module='bioclipse-cdk', version='0.0.1-SNAPSHOT')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

println cdk.fromSMILES("COC")
```

For the time being, the API is *incomplete*. You may need to occassionally delete the
modules cached by Groovy, by doing something like:

```shell
\rm -Rf ~/.groovy/grapes/net.bioclipse.managers/
```

## License

Eclipse Public License v1.0 + GPL exception.
