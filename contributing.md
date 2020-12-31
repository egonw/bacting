# Contributions

There are various ways to contribute to Bacting, and this does not only involve development, but can also be
checking of an existing Bioclipse scripts can already be run with Bacting (the Bioclipse 2.6 API is
[only partially ported](https://github.com/egonw/bacting/projects/2)).

## User experience

We love to hear from you about the Bioclipse scripts you have written in the past. We like to learn
from this to see what functionality needs to be reported. You can use [this issue template](https://github.com/egonw/bacting/issues/new?assignees=&labels=enhancement&template=feature-request--bioclipse-api-method.md&title=)
for that.

## Code development

Developing Bioclipse is not trivial. Developing Bacting is easier but still has a learning curve.
It is recommended to do all code development in branches, allowing others to suggest improvements
and generally do peer review. If you fork the repository, it is suggested to keep your `master`
branch identical to the upstream version.

### Porting Bioclipse managers

There are still many unported managers. Some are easy to port, some are hard. Some managers focus on
the Bioclipse graphical user interface, which in Bacting is not available. Porting such a manager will
be a lot harder. Other managers cannot be ported yet because the used libraries are not yet available
from Maven Central. However, there is plenty of work that can still be done.

Once you decided it is needed to port a manager, one easy way
to start a new manager is to just copy/paste the full folder of a simple manager, like that of OPSIN
found [here](https://github.com/egonw/bacting/tree/master/managers-cheminfo/net.bioclipse.managers.opsin).
The copied files can then be updated with a text editor to have unique names.

With the folder structure in place, you can start copy/pasting manager content from the Bioclipse project.
Make sure to always:

* keep copyright and license information for all copied Bioclipse source code
* ideally, separate copy/pasting the code and making any updates
* the API should match that of the manager interface, not of the Bioclipse implementation

The latter means you should be aware that Bacting does not have:

* a `IProgressMonitor` and the Bacting API should exclude those method parameters
* use `String file` instead of `IFile file`

#### Testing

Testing for ported managers is welcome buit not required. Bioclipse itself has a test suite.

### Developing a new manager

Bacting is not limited to existing managers and new managers are welcome, wrapping around a yet unused
bioinformatics of cheminformatics Java library. It is recommended to use the
[Issue tracker](https://github.com/egonw/bacting/issues) first to discuss the plans and to make sure
the Java library is indeed not used yet.

New managers can be developed in this repository, but do not have to be. The section
about starting with the folder structure of a simple manager in "Porting Bioclipse managers"
can be used here too.

#### Testing

Testing for new managers is recommended. 
