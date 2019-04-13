# bacting

[![License](https://img.shields.io/badge/License-EPL%201.0-red.svg)](https://opensource.org/licenses/EPL-1.0)

Bacting := acting as the Bioclipse TNG

If you use this software, please cite the [Bioclipse 2 paper](https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-397).

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

## Copyright and authors

All code in the `/bioclipse/` folder contains Bioclipse 2 code and the headers of the individual
source code files describe who contributed to that code of that class, but unfortunately this code
ownership is not always clear. I refer to the various [Bioclipse code repositories](https://github.com/bioclipse)
for the git history for more information.
