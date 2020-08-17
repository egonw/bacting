# Bacting

[![License](https://img.shields.io/badge/License-EPL%201.0-red.svg)](https://opensource.org/licenses/EPL-1.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.2638709.svg)](https://doi.org/10.5281/zenodo.2638709)
[![Build Status](https://travis-ci.org/egonw/bacting.svg?branch=master)](https://travis-ci.org/egonw/bacting)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.egonw.bacting/bacting.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.egonw.bacting%22%20AND%20a:%22bacting%22)

Bacting := acting as the Bioclipse TNG

Bacting is a Java-based, open-source platform for chemo- and bioinformatics based on [Bioclipse](https://scholia.toolforge.org/topic/Q1769726)
that defines a number of common domain objects and wraps common functionality, providing a toolkit independent, scriptable solution to
handle data from the life sciences.

## How to cite

If you use this software, please cite the [Bioclipse 2 paper](https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-397).

A paper about Bacting has been writing and available from [paper/paper.md](paper/paper.md).

[![status](https://joss.theoj.org/papers/c306d8f50a6390d21b43633c99cbe4c3/status.svg)](https://joss.theoj.org/papers/c306d8f50a6390d21b43633c99cbe4c3)

# Install

First, you need a working [Maven installation](https://www.google.nl/search?q=install+maven) and the code is tested with
Java 8, 11, and 14, and can be installed with:

```shell
mvn clean install -Dgpg.skip
```

# Usage

It can be used in [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) by including the
Bacting managers you need:

```groovy
@Grab(group='io.github.egonw.bacting', module='managers-cdk', version='0.0.13')

workspaceRoot = "."
def cdk = new net.bioclipse.managers.CDKManager(workspaceRoot);

println cdk.fromSMILES("COC")
```

## API Coverage

For the time being, the coverage of the original API is *incomplete*.
Particularly, manager functionality around graphical UX
in the original Bioclipse may never be implemented. Each Bacting release will implement more APIs and
the release notes will mention which managers and which methods have been added.

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

All code in the `/bioclipse/` folder contains Bioclipse 2 code and the headers of the individual
source code files describe who contributed to that code of that class, but unfortunately this code
ownership is not always clear. I refer to the various [Bioclipse code repositories](https://github.com/bioclipse)
for the git history for more information.
