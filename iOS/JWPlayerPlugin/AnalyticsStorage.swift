//
//  AnalyticsStorage.swift
//  JWPlayerPlugin
//
//  Created by Roman Karpievich on 3/3/20.
//

import Foundation
import ZappPlugins

@objc public enum AnalyticsEvents: Int {
    case tapCast
    case castStart
    case castStop
    
    var key: String {
        switch self {
        case .tapCast:
            return "Tap Cast"
        case .castStart:
            return "Cast Start"
        case .castStop:
            return "Cast Stop"
        }
    }
}

@objc public enum PlayerViewType: Int {
    case fullScreen
    case inline
    case cast
    
    var stringValue: String {
        switch self {
        case .fullScreen:
            return "Full Screen"
        case .inline:
            return "Inline"
        case .cast:
            return "Cast"
        }
    }
}

@objc public class AnalyticsStorage: NSObject {
    
    @objc public var duration: TimeInterval = 0.0 {
        didSet {
            parameters["Item Duration"] = String.create(fromInterval: duration)
        }
    }
    
    @objc public var playerViewType: PlayerViewType = .fullScreen {
        didSet {
            parameters["View"] = playerViewType.stringValue
        }
    }
    
    @objc public var isCompleted: Bool = false {
        didSet {
            parameters["Completed"] = isCompleted ? "Yes" : "No"
        }
    }
    
    @objc public var castingDevice: String = "" {
        didSet {
            parameters["Casting Device"] = castingDevice
        }
    }
    
    @objc public var videoProgress: TimeInterval = 0.0 {
        didSet {
            parameters["Timecode"] = String.create(fromInterval: videoProgress)
        }
    }
    
    @objc public var isCasting: Bool = false {
        didSet {
            parameters["Previous State"] = oldValue ? "On" : "Off"
        }
    }
    
    private var parameters: Dictionary<String, String> = [:]
    
    public override init() {
        super.init()
        
        parameters["Completed"] = "No"
        parameters["VOD Type"] = "ATOM"
        parameters["Previous State"] = "Off"
        parameters["Casting Device"] = "None Provided"
        parameters["Item Duration"] = "None Provided"
        parameters["Timecode"] = String.create(fromInterval: 0.0)
        parameters["View"] = PlayerViewType.fullScreen.stringValue
    }
    
    @objc public func parseParameters(from video: ZPPlayable) {
        parameters["Item ID"] = (video.identifier ?? "") as String
        parameters["Item Name"] = video.playableName()
        parameters["Free or Paid"] = video.isFree() ? "Free" : "Paid"
        parameters["Video Type"] = video.isLive() ? "Live" : "VOD"
    }
    
    @objc public func send(analyticsEvent: AnalyticsEvents, timed: Bool = false) {
        var parameters = Dictionary<String, String>()
        
        switch analyticsEvent {
        case .tapCast:
            parameters = playableItemProperties()
            parameters = parameters.merge(castProperties())
            parameters["Previous State"] = self.parameters["Previous State"]
        case .castStart:
            parameters = playableItemProperties()
            parameters = parameters.merge(castProperties())
        case .castStop:
            parameters = playableItemProperties()
            parameters = parameters.merge(castProperties())
        }
        
        ZAAppConnector.sharedInstance().analyticsDelegate.trackEvent(name: analyticsEvent.key,
                                                                     parameters: parameters,
                                                                     timed: timed)
    }
    
    @objc public func setLiveProperties() {
        parameters["Video Type"] = "Live"
        parameters["Item Duration"] = "None Provided"
        parameters["Timecode"] = "None Provided"
    }
    
    // MARK: - Private methods
    
    private func playableItemProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Video Type"] = parameters["Video Type"]
        properties["Item Duration"] = parameters["Item Duration"]
        properties["VOD Type"] = parameters["VOD Type"]
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Timecode"] = parameters["Timecode"]
        
        return properties
    }
    
    private func castProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Casting Device"] = parameters["Casting Device"]
        properties["View"] = parameters["View"]
        
        return properties
    }
}

private extension String {
    static func create(fromInterval interval: TimeInterval) -> String {
        guard interval.isNormal == true else {
            return ""
        }
        
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.hour, .minute, .second]
        formatter.unitsStyle = .positional
        
        return formatter.string(from: interval) ?? ""
    }
}
