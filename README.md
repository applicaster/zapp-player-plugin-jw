# JWPlayer-Plugin-iOS

The JWPlayer-Plugin-iOS is a player plugin for the Applicaster Zapp Platform that uses [JWPlayer-SDK](https://www.jwplayer.com/)

## Debug the Example app

Edit the JWPlayer-Plugin-iOS/Resources/plugin_configurations.json and add a license key from JWPlayer.

    "configuration_json": {"playerKey": "<Your License Key>"}

# Using the Starter Kit

In order to be able to test and debug your project A->Z, you will need add your plugin to the `zapp-ios-plugins-starter-kit`, which can be cloned [here](https://github.com/applicaster/zapp-ios-plugins-starter-kit).

# TODO

JWPlayer is a static library without a module. So we use a bridging-header to import `#import <JWPlayer-SDK/JWPlayerController.h>`
A workaround is needed instead of the briding header.
