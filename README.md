# Bacting

[![License](https://img.shields.io/badge/License-EPL%201.0-red.svg)](https://opensource.org/licenses/EPL-1.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.2638709.svg)](https://doi.org/10.5281/zenodo.2638709)
[![build](https://github.com/egonw/bacting/workflows/build/badge.svg)](https://github.com/egonw/bacting/actions?query=workflow%3Abuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.egonw.bacting/bacting.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.egonw.bacting%22%20AND%20a:%22bacting%22)
[![codecov](https://codecov.io/gh/egonw/bacting/branch/master/graph/badge.svg?token=E1NGWVWL04)](https://codecov.io/gh/egonw/bacting)

Bacting := acting as the Bioclipse TNG (The Next Generation)

Bacting is an open-source platform for chemo- and bioinformatics based on [Bioclipse](https://scholia.toolforge.org/topic/Q1769726)
that defines a number of common domain objects and wraps common functionality, providing a toolkit independent, scriptable solution to
handle data from the life sciences. Like Bioclipse, Bacting is written in the Java language, making use in Java-derived
languages like [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) easy, but also accessible to Python. Deposition of the Bacting package on
[Maven Central](https://search.maven.org/search?q=g:%22io.github.egonw.bacting%22%20AND%20a:%22bacting%22) allows it
to be easily used in Groovy scripts with `@Grab` instructions.

## How to cite

If you use this software, please cite the article in JOSS:

[![DOI](https://joss.theoj.org/papers/10.21105/joss.02558/status.svg)](https://doi.org/10.21105/joss.02558)

# Install

For the below use cases, Bacting is actually installed on demand. In Groovy this is done with
`@Grab` and in Python with `from pybacting import cdk` (see [pybacting](https://github.com/cthoyt/pybacting))
or `scyjava.config`. This section explains how Bacting can be installed from
the source code.

## From the source code

First, you need a working [Maven installation](https://www.google.nl/search?q=install+maven) and the code is tested with
Java 11, and 14, and can be installed with:

```shell
mvn clean install -Dgpg.skip -Dmaven.javadoc.skip=true
```

## Making releases

Before making a release, update the version number in this `README.md` and in `CITATION.cff`.

Releases are created by the release manager and requires permission to submit the release to Maven Central
(using an approved Sonatype ([oss.sonatype.org](http://oss.sonatype.org/)) account).
If these requirements are fulfilled then the following commands to the job:

```shell
export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
mvn versions:set -DnewVersion=0.0.33
git commit -m "New release" -a
mvn deploy -P release
```

### Making snapshots

```shell
mvn versions:set -DnewVersion=0.0.34-SNAPSHOT
mvn deploy
```

### Updating the JavaDoc

After the release is avaiable on Maven Central, the [JavaDoc](https://egonw.github.io/bacting-api/)
needs to be updated. The JavaDoc is generated with the below command, and the results are stored
in the `target/site/apidocs/` folder:

```shell
mvn clean javadoc:javadoc javadoc:aggregate
```

That created content needs to be copied into the `docs/` folder of
[this git repository](https://github.com/egonw/bacting-api/).

# Usage

## Groovy

It can be used in [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) by including the
Bacting managers you need. The following example tells Groovy to download the `CDKManager`
and instantiate it for the given workspace location (as it if was running in Bioclipse
itself), and then converts a [SMILES](https://en.wikipedia.org/wiki/Simplified_molecular-input_line-entry_system)
string to a Bioclipse `IMolecule` data object:

```groovy
@Grab(group='io.github.egonw.bacting', module='managers-cdk', version='0.0.33')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

println cdk.fromSMILES("COC")
```

## Python

Bacting can also be used in Python 3.7 with [pybacting](https://github.com/cthoyt/pybacting) and
[scyjava](https://github.com/scijava/scyjava).

### Pybacting

Pybacting can be installed with `pip install pybacting` (or `pip3`, depending on your platform).
The above code example looks like:

```python
from pybacting import cdk

print(cdk.fromSMILES("COC"))
```

Pybacting uses a specific Bacting version, so check the [website](https://github.com/cthoyt/pybacting)
to see which Bacting version you are using.

### Scyjava

Scyjava can be installed with `pip install scyjava` (or `pip3`, depending on your platform).
The code example looks like:

```python
from scyjava import config, jimport
config.add_endpoints('io.github.egonw.bacting:managers-cdk:0.0.33')

workspaceRoot = "."
cdkClass = jimport("net.bioclipse.managers.CDKManager")
cdk = cdkClass(workspaceRoot)

print(cdk.fromSMILES("COC"))
```

# Code examples

Full code examples can be found in the following sources:

* Open Notebooks for Wikidata, including:
  * [script that compares a SMILES string with Wikidata, and creates QuickStatements for missing information](https://github.com/egonw/ons-wikidata/blob/master/Wikidata/createWDitemsFromSMILES.groovy)
  * [script that reads melting points from an Excel spreadsheet to enter into Wikidata](https://github.com/egonw/ons-wikidata/blob/master/MeltingPoints/createQuickStatements.groovy)
* Open Notebooks for WikiPathways:
  * [script to recognize IUPAC names in WikiPathways](https://github.com/egonw/ons-wikipathways/blob/master/WikiPathways/getLabelsWithIUPACNames.groovy)
* Some examples from [A lot of Bioclipse Scripting Language examples](https://bioclipse.github.io/bioclipse.scripting/):
  * [FullPathWikiPathways.groovy](https://bioclipse.github.io/bioclipse.scripting/code/FullPathWikiPathways.code.html)
  * [XMLIsWellFormed.groovy](https://bioclipse.github.io/bioclipse.scripting/code/XMLIsWellFormed.code.html)
  * [XMLListNamespaces.groovy](https://bioclipse.github.io/bioclipse.scripting/code/XMLListNamespaces.code.html)
  * [PerceiveCDKAtomTypes.groovy](https://bioclipse.github.io/bioclipse.scripting/code/PerceiveCDKAtomTypes.code.html)
  * [InChIKeyGenerate.groovy](https://bioclipse.github.io/bioclipse.scripting/code/InChIKeyGenerate.code.html)
  * [ParseIUPACName.groovy](https://bioclipse.github.io/bioclipse.scripting/code/ParseIUPACName.code.html)
  * [ChemSpiderResolve.groovy](https://bioclipse.github.io/bioclipse.scripting/code/ChemSpiderResolve.code.html)

## API Coverage

For the time being, the coverage of the original API is *incomplete*.
Particularly, manager functionality around graphical UX
in the original Bioclipse may never be implemented. Each Bacting release will implement more APIs and
the release notes will mention which managers and which methods have been added.
An overview of the supports APIs can be found in [this overview](https://github.com/egonw/bacting/projects/2).

For a description of the API, I refer to the book
[A lot of Bioclipse Scripting Language examples](https://bioclipse.github.io/bioclipse.scripting/) that
Jonathan and I compiled. However, a [JavaDoc API](https://egonw.github.io/bacting-api/) is also available.

All Bacting scripts will be backwards compatible with Bioclipse. If you want to install Bioclipse
and see its wonderful UX in actions, [download Bioclipse 2.6.2 here](https://sourceforge.net/projects/bioclipse/files/bioclipse2/bioclipse2.6.2/).

## Using SNAPSHOT versions

You may need to occassionally delete the
modules cached by Groovy, by doing something like, to remove earlier SNAPSHOT versions:

```shell
\rm -Rf ~/.groovy/grapes/io.github.egonw.bacting/
```

# Copyright and authors

Code in this repository contains mostly code that originated from Bioclipse
and the headers of the individual source code files describe who contributed to that code of that class, but unfortunately this code
ownership is not always clear. I refer to the various [Bioclipse code repositories](https://github.com/bioclipse)
for the git history for detailed information.
