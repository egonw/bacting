# Bacting

[![License](https://img.shields.io/badge/License-EPL%201.0-red.svg)](https://opensource.org/licenses/EPL-1.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.2638709.svg)](https://doi.org/10.5281/zenodo.2638709)
[![build](https://github.com/egonw/bacting/workflows/build/badge.svg)](https://github.com/egonw/bacting/actions?query=workflow%3Abuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.egonw.bacting/bacting.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.egonw.bacting%22%20AND%20a:%22bacting%22)
[![codecov](https://codecov.io/gh/egonw/bacting/branch/main/graph/badge.svg?token=E1NGWVWL04)](https://codecov.io/gh/egonw/bacting)

Bacting := acting as the Bioclipse TNG (The Next Generation)

Bacting is an open-source platform for chemo- and bioinformatics based on [Bioclipse](https://scholia.toolforge.org/topic/Q1769726)
that defines a number of common domain objects and wraps common functionality, providing a toolkit-independent, scriptable solution to
handle data from the life sciences. Like Bioclipse, Bacting is written in the Java language, making use in Java-derived
languages like [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) easy, but also accessible to Python. Deposition of the Bacting package on
[Maven Central](https://search.maven.org/search?q=g:%22io.github.egonw.bacting%22%20AND%20a:%22bacting%22) allows it
to be easily used in Groovy scripts with `@Grab` instructions.

## How to Cite

If you use this software, please cite the article in JOSS:

[![DOI](https://joss.theoj.org/papers/10.21105/joss.02558/status.svg)](https://doi.org/10.21105/joss.02558)

## Prerequisites

This project is built based on Java and supports Java versions 11, 17, and 19. Ensure you have the following installed before proceeding:

- [Maven](https://www.google.nl/search?q=install+maven)
- Java Runtime Environment

## Installation Steps

### Option 1: On demand Installation

For the below use cases, Bacting is actually installed on demand. In Groovy this is done with
`@Grab` and in Python with `from pybacting import cdk` (see [pybacting](https://github.com/cthoyt/pybacting))
or `scyjava.config`.

### Option 2: Manual Installation

To install Bacting from the source code:

```shell
mvn clean install -Dgpg.skip -Dmaven.javadoc.skip=true
```

### Verification

To verify the successful installation, run:

```shell
mvn clean test
```

## Making a release

Before making a release, update the version number in this `README.md` and in `CITATION.cff`.

Releases are created by the release manager and requires permission to submit the release to Maven Central
(using an approved Sonatype ([oss.sonatype.org](http://oss.sonatype.org/)) account).
If these requirements are fulfilled then the following commands to the job:


```shell
export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
mvn versions:set -DnewVersion=1.0.3
git commit -m "New release" -a
mvn deploy -P release
```



## Usage

### Groovy

It can be used in [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) by including the
Bacting managers you need. The following example tells Groovy to download the `CDKManager`
and instantiate it for the given workspace location (as it if was running in Bioclipse
itself), and then converts a [SMILES](https://en.wikipedia.org/wiki/Simplified_molecular-input_line-entry_system)
string to a Bioclipse `IMolecule` data object:

```groovy
@Grab(group='io.github.egonw.bacting', module='managers-cdk', version='1.0.3')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

println cdk.fromSMILES("COC")
```

### Python

Bacting can also be used in Python 3.7 with [pybacting](https://github.com/cthoyt/pybacting) and
[scyjava](https://github.com/scijava/scyjava).

#### Using Pybacting

Install using pip:

```shell
pip install pybacting
```

Example:

```python
from pybacting import cdk

print(cdk.fromSMILES("COC"))
```

#### Using Scyjava

Install Scyjava:

```shell
pip install scyjava
```

Example:

```python
from scyjava import config, jimport

config.add_endpoints('io.github.egonw.bacting:managers-cdk:1.0.3')
workspaceRoot = "."
cdkClass = jimport("net.bioclipse.managers.CDKManager")
cdk = cdkClass(workspaceRoot)

print(cdk.fromSMILES("COC"))
```

## Code Examples

Detailed examples can be found in the following sources:

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


## External Documents

- [Bioclipse Scripting Language Examples](https://bioclipse.github.io/bioclipse.scripting/)
- [JavaDoc API](https://egonw.github.io/bacting-api/)

## Version History

For changes and API updates, refer to [GitHub Projects](https://github.com/egonw/bacting/projects/2).

## Help and Support

Refer to the [GitHub repository issues](https://github.com/egonw/bacting/issues) for FAQs and support.

## Copyright and Authors

Code in this repository contains mostly code that originated from Bioclipse
and the headers of the individual source code files describe who contributed to that code of that class, but unfortunately this code
ownership is not always clear. I refer to the various [Bioclipse code repositories](https://github.com/bioclipse)
for the git history for detailed information.

