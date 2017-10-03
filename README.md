# JWPlayer-Plugin-iOS

The JWPlayer-Plugin-iOS is a player plugin for the Applicaster Zapp Platform that uses [JWPlayer-SDK](https://www.jwplayer.com/)


#### Using the Zapp Plugin Configuration JSON
When creating a plugin in Zapp we can create custom configuration fields. This enable the Zapp user to fill relevant details for the specific plugin. More details can found in the [Zapp Plugin Manifest Format](http://zapp-tech-book.herokuapp.com/zappifest/plugins-manifest-format.html).
You can use that on the plugin level like that:
``` swift
    guard let customParam = configurationJSON?["customParam"] as? String else {
        APLoggerError("Failed to create customParam from the plugin configuration JSON.")
        return nil
    }
```

## Debug the Example app

Edit the JWPlayer-Plugin-iOS/Resources/plugin_configurations.json and add a license key from JWPlayer.

    "configuration_json": {"playerKey": "<Your License Key>"}

# Using the Starter Kit

In order to be able to test and debug your project A->Z, you will need add your plugin to the `zapp-ios-plugins-starter-kit`, which can be cloned [here](https://github.com/applicaster/zapp-ios-plugins-starter-kit).
