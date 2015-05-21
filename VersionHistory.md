# Introduction #

This page summarizes the Daisy Diff releases.

## Daisy Diff Trunk ##

Changes are:

  1. Proper closing of readers and streams (by firecreeper, patch from  [issue 41](https://code.google.com/p/daisydiff/issues/detail?id=41) )
  1. Running of unit tests via Ant (by Gregor Petrin, see [issue 48](https://code.google.com/p/daisydiff/issues/detail?id=48))



## Daisy Diff 1.2 (October 2011) ##

Changes are:

  1. Html mode performance tuning (By Don Willis, patch from [issue 33](https://code.google.com/p/daisydiff/issues/detail?id=33))
  1. Support for 3-way comparison (By Carsten Pfeiffer, see [issue 27](https://code.google.com/p/daisydiff/issues/detail?id=27))
  1. Some initial unit tests (By Carsten Pfeiffer,Don Willis and Kostis Kapelonis)
  1. Improved treatment of the visual document structure (By Carsten Pfeiffer, see [issue 28](https://code.google.com/p/daisydiff/issues/detail?id=28))
  1. Fixed an out of bounds exception (By Beton, see [issue 22](https://code.google.com/p/daisydiff/issues/detail?id=22))

## Daisy Diff 1.1 (February 2010) ##

This is mainly a Bug fix release. If you are happy with the way version 1.0 works
then it is not imperative to upgrade. If you find a regression (something that worked
in 1.0 but not in 1.1) please open an issue with the details.

Changes are:

  1. Ignore order of attributes when comparing HTML tags (By Karol Kraskiewicz)
  1. Quiet mode command line option (By Peter Dibble)
  1. Update of CyberNeko HML Parser to version 1.9.11 (By Hudak Rastislav)
  1. Several Fixes by yaacovCR
  1. Several Fixes by Anastassia Sytnik (Such as [issue 10](https://code.google.com/p/daisydiff/issues/detail?id=10) and [issue 12](https://code.google.com/p/daisydiff/issues/detail?id=12))
  1. Fixed an exception with long HTML (By Peter Dibble. Fixes [issue 20](https://code.google.com/p/daisydiff/issues/detail?id=20))
  1. Allow alternative output formats and some unit tests (By Kostis Kapelonis. Fixes [issue 16](https://code.google.com/p/daisydiff/issues/detail?id=16))

## Daisy Diff 1.0 (October 2008) ##

This is the original version by Guy Van den Broeck and Daniel Dickison. The release
is the result of a Google Summer of Code project.

Version 1.0 is a stable release that is considered production ready. Most of its issues
have to do with HTML tables that contain complex formatting.

See also DaisyDiffModes.