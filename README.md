# JWPlayer-Plugin-iOS

The JWPlayer-Plugin-iOS is a player plugin for the Applicaster Zapp Platform that uses [JWPlayer-SDK](https://www.jwplayer.com/)

## Note
This is a rebuild of the previous library - rewritten in obj-c due to limitations with the way JWPlayer SDK is written.
Since JWPlayer SDK moved to be a framework with no proper module files and problematic import names - this had to be done.

Please do not introduce Swift code to this implementation - keep it obj-c until JW changes their library.

The old implementation and history can be found on a side branch of this repository.
This will de-facto replace the master branch.