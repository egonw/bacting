# Some porting basics

Porting Bioclipse functionality is not trivial, but once you picked up the pattern, not too hard. Some key differences:

- Bacting does not have IFile, but uses String instead (matching the API of Bioclipse, instead of the implementations)
- Bacting does not have IProgressMonitor (just remove it)
- code should be copied as exact as possible
- copyright ownership must be kept; copying is non-trvial so adding yourself as copyright owner is acceptable
- 
