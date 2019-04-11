# Getting the Bioclipse Java archive files

To compile and run this software, we're going to use libraries available from Maven Central, but we're also going to need Bioclipse
which we have to get and install in our local Maven repository differently.

For this, download [Bioclipse 2.6.2 from SourceForge](https://sourceforge.net/projects/bioclipse/files/bioclipse2/bioclipse2.6.2/).
We are going to need the Java archive files in one of the distributions.

This archive is extracted, e.g. on GNU/Linux:

```shell
tar zxvf Bioclipse.2.6.2.linux64bit.tar.gz
```

We need to install several Bioclipse plugins into our local Maven repository:

```shell
cd Bioclipse.2.6.2/plugins
mvn install:install-file -Dfile=net.bioclipse.core_2.6.2.v201411181156.jar -DgroupId=net.bioclipse -DartifactId=core -Dversion=2.6.2.v201411181156 -Dpackaging=jar
mvn install:install-file -Dfile=net.bioclipse.cdk.business_2.6.2.v201609010936.jar -DgroupId=net.bioclipse -DartifactId=cdk.business -Dversion=2.6.2.v201609010936 -Dpackaging=jar
```
