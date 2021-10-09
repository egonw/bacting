# Some porting basics

Porting [Bioclipse](https://github.com/bioclipse) functionality is not trivial, but once you picked up the pattern, not too hard. Some key differences:

- Bacting does not have `IFile`, but uses `String` instead (matching the API of Bioclipse, instead of the implementations)
- Bacting does not have `IProgressMonitor` (just remove it)
- code should be copied as exact as possible
- copyright ownership must be kept; copying is non-trvial so adding yourself as copyright owner is acceptable
- the test method must be copied too, and [code coverage](https://app.codecov.io/gh/egonw/bacting) should be taken into account
