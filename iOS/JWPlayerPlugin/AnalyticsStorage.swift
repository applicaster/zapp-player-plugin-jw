//
//  AnalyticsStorage.swift
//  JWPlayerPlugin
//
//  Created by Roman Karpievich on 3/3/20.
//

import Foundation
import ZappPlugins

@objc public class SeekEvent: NSObject {
    private(set) var from: Double
    private(set) var to: Double
    var seekDirection: String {
        let value = to > from ? "Fast Forward" : "Rewind"
        return value
    }
    
    @objc public init(from: Double, to: Double) {
        self.from = from
        self.to = to
        
        super.init()
    }
}

@objc public enum AnalyticsEvents: Int {
    case tapCast
    case castStart
    case castStop
    case seek
    case pause
    case switchPlayerView
    case play
    case playError
    case adError
    case adPlayed
    
    var key: String {
        switch self {
        case .tapCast:
            return "Tap Cast"
        case .castStart:
            return "Cast Start"
        case .castStop:
            return "Cast Stop"
        case .seek:
            return "Seek"
        case .pause:
            return "Pause"
        case .switchPlayerView:
            return "Switch Player View"
        case .play:
            return "None provided"
        case .playError:
            return "Video Play Error"
        case .adError:
            return "Video Ad Error"
        case .adPlayed:
            return "Watch Video Advertisement"
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
            parameters["Original View"] = oldValue.stringValue
            parameters["New View"] = playerViewType.stringValue
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
    
    @objc public var seek: SeekEvent = SeekEvent(from: 0, to: 0) {
        didSet {
            parameters["Seek Direction"] = seek.seekDirection
            parameters["Timecode From"] = String.create(fromInterval: seek.from)
            parameters["Timecode To"] = String.create(fromInterval: seek.to)
        }
    }
    
    @objc public var videoStartTime: Date?
    @objc public var playerViewSwitchCounter: Int = 0
    
    @objc public var playError: NSError? {
        didSet {
            if let error = playError {
                parameters["Error Code"] = String(error.code)
                parameters["Error Message"] = error.localizedDescription
                parameters["Error Domain"] = error.domain
            }
        }
    }
    
    @objc public var adError: NSError? {
        didSet {
            if let error = adError {
                parameters["Error Code"] = String(error.code)
                parameters["Advertising Provider"] = "IMA"
                parameters["Error Domain"] = error.domain
            }
        }
    }
    
    @objc public var adPosition: String = "" {
        didSet {
            var analyticsValue = ""
            switch adPosition {
            case "pre":
                analyticsValue = "Preroll"
            case "mid":
                analyticsValue = "Midroll"
            case "post":
                analyticsValue = "Postroll"
            default:
                break
            }
            
            parameters["Video Ad Type"] = analyticsValue
        }
    }
    
    @objc public var adURL: String = "" {
        didSet {
            parameters["Ad Unit"] = adURL
        }
    }
    
    @objc public var isAdSkipped: Bool = false {
        didSet {
            parameters["Skipped"] = isAdSkipped ? "Yes" : "No"
        }
    }
    
    @objc public var isAdClicked: Bool = false {
        didSet {
            parameters["Clicked"] = isAdClicked ? "Yes" : "No"
        }
    }
    
    private var isLive: Bool = false
    private var wasPlayEventSend: Bool = false
    private var adBreakStarted: Date?
    
    private var parameters: Dictionary<String, String> = [:]
    
    public override init() {
        super.init()
        
        parameters["Completed"] = "No"
        parameters["VOD Type"] = "ATOM"
        parameters["Previous State"] = "Off"
        parameters["Casting Device"] = "None Provided"
        parameters["Item Duration"] = "None Provided"
        parameters["Timecode"] = "None Provided"
        parameters["View"] = PlayerViewType.fullScreen.stringValue
        parameters["Video Type"] = "VOD"
        parameters["Skipped"] = "No"
        parameters["Clicked"] = "No"
    }
    
    @objc public func parseParameters(from video: ZPPlayable) {
        parameters["Item ID"] = (video.identifier ?? "") as String
        parameters["Item Name"] = video.playableName()
        parameters["Free or Paid"] = video.isFree() ? "Free" : "Paid"
        parameters["Item Link"] = video.contentVideoURLPath()
        if video.isLive() {
            setLiveProperties()
        }
    }
    
    @objc public func send(analyticsEvent: AnalyticsEvents, timed: Bool = false) {
        if analyticsEvent == .play, wasPlayEventSend == true {
            return
        }
        
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
        case .seek:
            parameters = seekProperties()
        case .pause:
            parameters = pauseProperties()
        case .switchPlayerView:
            playerViewSwitchCounter += 1
            parameters = switchPlayerViewProperties()
        case .play:
            if isLive {
                parameters = playLiveProperties()
            } else {
                parameters = playVodProperties()
            }
        case .playError:
            parameters = playErrorProperties()
        case .adError:
            parameters = adErrorProperties()
        case .adPlayed:
            parameters = adPlayProperies()
        }
        
        var name = analyticsEvent.key
        
        if analyticsEvent == .play {
            if isLive {
                name = "Play Live Stream"
            } else {
                name = "Play VOD Item"
            }
            wasPlayEventSend = true
        }
        
        ZAAppConnector.sharedInstance().analyticsDelegate.trackEvent(name: name,
                                                                     parameters: parameters,
                                                                     timed: timed)
    }
    
    @objc public func setLiveProperties() {
        parameters["Video Type"] = "Live"
        parameters["Item Duration"] = "None Provided"
        parameters["Timecode"] = "None Provided"
        isLive = true
    }
    
    @objc public func adStart() {
        if parameters["Video Ad Type"] == "Preroll" {
            parameters["Ad Break Time"] = "00:00:00"
        } else {
            parameters["Ad Break Time"] = String.create(fromInterval: videoProgress)
        }
        adBreakStarted = Date()
    }
    
    @objc public func adEnd() {
        var adExitMethod = "Completed"
        
        if isAdSkipped {
            adExitMethod = "Skipped"
        }
        
        if isAdClicked {
            adExitMethod = "Clicked"
        }
        
        parameters["Ad Exit Method"] = adExitMethod
        if let adBreakStartTime = adBreakStarted {
            parameters["Ad Break Duration"] = String.create(fromInterval: Date().timeIntervalSince(adBreakStartTime))
        }
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
    
    private func seekProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Seek Direction"] = parameters["Seek Direction"]
        properties["View"] = parameters["View"]
        if isLive == false {
            properties["Item Duration"] = parameters["Item Duration"]
            properties["VOD Type"] = parameters["VOD Type"]
        }
        properties["Timecode From"] = parameters["Timecode From"]
        properties["Timecode To"] = parameters["Timecode To"]
        
        return properties
    }
    
    private func pauseProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Video Type"] = parameters["Video Type"]
        properties["View"] = parameters["View"]
        if isLive == false {
            properties["Item Duration"] = parameters["Item Duration"]
            properties["VOD Type"] = parameters["VOD Type"]
        }
        properties["Timecode"] = parameters["Timecode"]
        if let videoStartTime = videoStartTime {
            properties["Duration In Video"] = String.create(fromInterval: Date().timeIntervalSince(videoStartTime))
        }
        
        return properties
    }
    
    private func switchPlayerViewProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Original View"] = parameters["Original View"]
        properties["New View"] = parameters["New View"]
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Video Type"] = parameters["Video Type"]
        if isLive == false {
            properties["Item Duration"] = parameters["Item Duration"]
            properties["VOD Type"] = parameters["VOD Type"]
        }
        properties["Timecode"] = parameters["Timecode"]
        properties["Switch Instance"] = String(playerViewSwitchCounter)
        if let videoStartTime = videoStartTime {
            properties["Duration In Video"] = String.create(fromInterval: Date().timeIntervalSince(videoStartTime))
        }
        
        return properties
    }
    
    private func playVodProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Item Duration"] = parameters["Item Duration"]
        properties["Completed"] = parameters["Completed"]
        properties["VOD Type"] = parameters["VOD Type"]
        properties["View"] = parameters["View"]
        
        return properties
    }
    
    private func playLiveProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["View"] = parameters["View"]
        
        return properties
    }
    
    private func playErrorProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Item Duration"] = parameters["Item Duration"]
        properties["Item Link"] = parameters["Item Link"]
        properties["Completed"] = parameters["Completed"]
        properties["VOD Type"] = parameters["VOD Type"]
        properties["View"] = parameters["View"]
        properties["Error Code"] = parameters["Error Code"]
        properties["Error Message"] = parameters["Error Message"]
        properties["Eror Domain"] = parameters["Error Domain"]
        properties["Video Player Plugin"] = "JW Player"
        
        return properties
    }
    
    private func adErrorProperties() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["Item Duration"] = parameters["Item Duration"]
        properties["Item Link"] = parameters["Item Link"]
        properties["Completed"] = parameters["Completed"]
        properties["VOD Type"] = parameters["VOD Type"]
        properties["View"] = parameters["View"]
        properties["Video Player Plugin"] = "JW Player"
        properties["Error Code"] = parameters["Error Code"]
        properties["Advertising Provider"] = parameters["Advertising Provider"]
        properties["Error Domain"] = parameters["Error Domain"]
        
        return properties
    }
    
    private func adPlayProperies() -> Dictionary<String, String> {
        var properties = Dictionary<String, String>()
        properties["Video Ad Type"] = parameters["Video Ad Type"]
        properties["Ad Provider"] = "DFP"
        properties["Ad Unit"] = parameters["Ad Unit"]
        properties["Skipped"] = parameters["Skipped"]
        properties["Content Video Duration"] = parameters["Item Duration"]
        properties["Ad Break Time"] = parameters["Ad Break Time"]
        properties["Ad Break Duration"] = parameters["Ad Break Duration"]
        properties["Ad Exit Method"] = parameters["Ad Exit Method"]
        if parameters["Video Ad Type"] == "Preroll" {
            properties["Time When Exited"] = "00:00:00"
        } else {
            properties["Time When Exited"] = parameters["Timecode"]
        }
        properties["Free or Paid"] = parameters["Free or Paid"]
        properties["Clicked"] = parameters["Clicked"]
        properties["Item ID"] = parameters["Item ID"]
        properties["Item Name"] = parameters["Item Name"]
        properties["VOD Type"] = parameters["VOD Type"]
        
        
        isAdSkipped = false
        isAdClicked = false
        adBreakStarted = nil
        
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
        formatter.zeroFormattingBehavior = .pad
        
        return formatter.string(from: interval) ?? ""
    }
}
