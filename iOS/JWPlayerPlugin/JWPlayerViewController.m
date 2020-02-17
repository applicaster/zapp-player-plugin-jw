//
//  JWPlayerViewController.m
//  JWPlayerPlugin
//
//  Created by Liviu Romascanu on 15/10/2018.
//  Copyright © 2018 Applicaster. All rights reserved.
//

#import "JWPlayerViewController.h"
#import "JWPlayer_iOS_SDK/JWPlayerController.h"
@import UIKit;
@import AVKit;
@import AVFoundation;
@import MediaPlayer;

// Accessibility IDs

NSString * const kJWPlayerCloseButton = @"jw_player_close_button";
NSString * const kJWPlayerAirplayButton = @"jw_player_airplay_button";
NSString * const kJWPlayerScreen = @"jw_player_screen";
NSString * const kJWPlayerSeekBackButton = @"jw_player_seek_back_button";
NSString * const kJWPlayerRestartButton = @"jw_player_restart_button";
NSString * const kJWPlayerPauseButton = @"jw_player_pause_button";

@interface JWPlayerViewController () <JWPlayerDelegate> {
    BOOL isViewHidden;
}

@property (nonatomic, strong) JWPlayerController *player;
@property (nonatomic, strong) JWAdConfig *adConfig;

@property (nonatomic) CGFloat trackedPercentage;
@property (nonatomic, strong) NSDictionary *extensionsDictionary;

@end

@implementation JWPlayerViewController

#pragma mark - UIViewController

- (void)loadView {
    [super loadView];
    
    isViewHidden = true;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(pause)
                                                 name:UIApplicationDidEnterBackgroundNotification
                                               object:nil];     // Fix for JP-5 task
    
    [self.view setAccessibilityIdentifier:kJWPlayerScreen];
    
    isViewHidden = false;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];

    [self.player pause];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIApplicationDidEnterBackgroundNotification
                                                  object:nil]; // Fix for JP-5 task
    
    isViewHidden = true;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

#pragma mark - public

- (void)setAllowAirplay:(BOOL)allowAirplay {
    _allowAirplay = allowAirplay;
    
    if (allowAirplay) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleScreenConnected:) name:UIScreenDidConnectNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleScreenDisconnected:) name:UIScreenDidDisconnectNotification object:nil];
    } else {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIScreenDidConnectNotification object:nil];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIScreenDidDisconnectNotification object:nil];
    }
}

#pragma mark -

- (UIButton *)closeButton {
    if (_closeButton) {
        return _closeButton;
    }
    
    UIButton *button = [UIButton new];
    button.titleLabel.font = [UIFont systemFontOfSize:25];
    [button setTitle:@"X" forState:UIControlStateNormal];
    [button setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    [button setAccessibilityIdentifier:kJWPlayerCloseButton];
    
    _closeButton = button;
    return button;
}

- (UIView*)airplayButton {
    if (_airplayButton) {
        return _airplayButton;
    }
    
    UIView *buttonView = nil;
    CGRect buttonFrame = CGRectMake(0, 0, 44, 44);
    
    // It's highly recommended to use the AVRoutePickerView in order to avoid AirPlay issues after iOS 11.
    if (@available(iOS 11.0, *)) {
        AVRoutePickerView *airplayButton = [[AVRoutePickerView alloc] initWithFrame:buttonFrame];
        airplayButton.activeTintColor = [UIColor grayColor];
        airplayButton.tintColor = [UIColor whiteColor];
        buttonView = airplayButton;
    } else {
        // If you still support previous iOS versions, you can use MPVolumeView
        MPVolumeView *airplayButton = [[MPVolumeView alloc] initWithFrame:buttonFrame];
        airplayButton.showsVolumeSlider = NO;
        airplayButton.tintColor = [UIColor whiteColor];
        buttonView = airplayButton;
    }
    
    _airplayButton = buttonView;
    
    _airplayButton.userInteractionEnabled = YES;
    [_airplayButton addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(airplayButtonTapped:)]];
    [_airplayButton setAccessibilityIdentifier:kJWPlayerAirplayButton];
    
    return buttonView;
}

- (void)setupPlayerWithPlayableItem:(NSObject <ZPPlayable> *)playableItem
{
    //JW Config
    JWConfig *config = [JWConfig new];
    config.sources = [NSArray arrayWithObject:[[JWSource alloc] initWithFile:[playableItem contentVideoURLPath]
                                                                       label:@""
                                                                   isDefault:YES]];
    
    config.title = playableItem.playableName;
    config.controls = YES;
    config.repeat = NO;
    config.autostart = YES;

    self.extensionsDictionary = playableItem.extensionsDictionary;

    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Play VOD Item"
                                                                 parameters:self.extensionsDictionary];

    if (self.adConfig) {
        config.advertising = self.adConfig;
        self.adConfig = nil;
    }
    
    self.player = [[JWPlayerController alloc] initWithConfig:config];
}

- (void)setupPlayerAdvertisingWithConfiguration:(NSArray *)ads {
    JWAdConfig *adConfig = [self createBaseAdConfiguration];
    NSMutableArray *scheduleArray = [NSMutableArray new];
    
    if (ads != nil) {
        // ad configuration dictionary scheduling
        for (NSDictionary *adConfiguration in ads)
        {
            NSString *type = adConfiguration[@"type"];
            
            if ([type  isEqual: @"vmap"]) {
                // we are using a vmap configuration that includes scheduling inside one url
                adConfig.adVmap = adConfiguration[@"ad_url"];
                break;
            } else {
                NSObject *rawOffset = adConfiguration[@"offset"];
                NSString *convertedOffset = nil;

                if ([rawOffset isKindOfClass:NSString.class]) {
                    NSString *offset = (NSString *)rawOffset;
                    if ([offset isEqualToString:@"preroll"]) {
                        convertedOffset = @"pre";
                    }
                    else if ([offset isEqualToString:@"postroll"]) {
                        convertedOffset = @"post";
                    }
                }
                else if ([rawOffset isKindOfClass:NSNumber.class]) {
                    convertedOffset = [(NSNumber *)rawOffset stringValue];
                }

                JWAdBreak *adBreak = [self createAdBreakWithTag:adConfiguration[@"ad_url"] offset:convertedOffset];
                
                if (adBreak) {
                    [scheduleArray addObject:adBreak];
                    adConfig.client = JWAdClientGoogima;
                }
            }
        }
    } else {
        // configure fallback ads according to configuration json
        if (self.isLive) {
            // Grab live ad fallbackconfiguration
            JWAdBreak *preroll = [self createAdBreakWithTag:self.configurationJSON[@"live_preroll_ad_url"]
                                                     offset:@"pre"];
            JWAdBreak *midRoll = [self createAdBreakWithTag:self.configurationJSON[@"live_midroll_ad_url"]
                                                     offset:self.configurationJSON[@"live_midroll_offset"]];
            
            if ([self.configurationJSON[@"live_ad_type"]  isEqual: @"googleima"]) {
                adConfig.client = JWAdClientGoogima;
            } else {
                adConfig.client = JWAdClientVast;
            }
            
            if (preroll != nil) {
                [scheduleArray addObject:preroll];
            }
            
            if (midRoll != nil) {
                [scheduleArray addObject:midRoll];
            }
        } else {
            // Grab live ad fallbackconfiguration
            JWAdBreak *preroll = [self createAdBreakWithTag:self.configurationJSON[@"vod_preroll_ad_url"]
                                                     offset:@"pre"];
            JWAdBreak *midRoll = [self createAdBreakWithTag:self.configurationJSON[@"vod_midroll_ad_url"]
                                                     offset:self.configurationJSON[@"vod_midroll_offset"]];
            
            if ([self.configurationJSON[@"vod_ad_type"]  isEqual: @"googleima"]) {
                adConfig.client = JWAdClientGoogima;
            } else {
                adConfig.client = JWAdClientVast;
            }
            
            if (preroll != nil) {
                [scheduleArray addObject:preroll];
            }
            
            if (midRoll != nil) {
                [scheduleArray addObject:midRoll];
            }
        }
    }
    
    // Set up the schedule if needed
    if ([scheduleArray count] > 0) {
        adConfig.schedule = scheduleArray;
    }
    
    if (self.player) {
        self.player.config.advertising = adConfig;
    } else {
        self.adConfig = adConfig;
    }
}

- (void)setupPlayerSubtitleTracksWithConfiguration:(NSArray *)subtitleTracks {
    if (self.player) {
        NSMutableArray *subtitleTracksArray = [NSMutableArray array];
        
        for (NSDictionary* currentSubtitleTrack in subtitleTracks)
        {
            NSDictionary *currentTrack;
            currentTrack = currentSubtitleTrack;
            NSString *subtitleTrackSource = currentTrack[@"source"];
            NSString *subtitleTrackLabel = currentTrack[@"label"];
            
            if (subtitleTrackSource.isNotEmpty && subtitleTrackLabel.isNotEmpty) {
                JWTrack *validSubtitleTrack = [JWTrack trackWithFile:subtitleTrackSource label:subtitleTrackLabel];
                
                [subtitleTracksArray addObject:validSubtitleTrack];
            }
        }
        
        if ([subtitleTracksArray count] > 0) {
            self.player.config.tracks = subtitleTracksArray;
        }
    }
}

- (void)play
{
    [self.player play];
}

- (void)pause {
    [self.player pause];
}

- (void)stop
{
    
}

- (BOOL)isPlaying {
    if (self.player.state == JWPlayerStatePlaying) {
        return YES;
    } else {
        return NO;
    }
}

#pragma mark - private

- (JWAdConfig *)createBaseAdConfiguration {
    JWAdConfig *adConfig = [JWAdConfig new];
    adConfig.adMessage = (self.configurationJSON[@"ad_message"] ? self.configurationJSON[@"ad_message"] : @"Ad duration countdown xx");
    adConfig.skipMessage = (self.configurationJSON[@"skip_message"] ? self.configurationJSON[@"skip_message"] : @"Skip in xx");
    adConfig.skipText = (self.configurationJSON[@"skip_text"] ? self.configurationJSON[@"skip_text"] : @"Move on");
    adConfig.skipOffset = (self.configurationJSON[@"skip_offset"] ? [self.configurationJSON[@"skip_offset"] intValue] : 3);
    
    return adConfig;
}

- (JWAdBreak *)createAdBreakWithTag:(NSString *)tag
                             offset:(NSString *)offset
{
    if ([tag isNotEmptyOrWhiteSpaces] && [offset isNotEmptyOrWhiteSpaces]) {
        return [JWAdBreak adBreakWithTag:tag offset:offset];
    } else {
        return nil;
    }
}

- (void) setCloseButtonConstraints:(UIView *) parentView {
    [self.closeButton.topAnchor constraintEqualToAnchor: parentView.topAnchor constant:36].active = YES;
    [self.closeButton.leadingAnchor constraintEqualToAnchor: parentView.leadingAnchor constant:16].active = YES;
    [self.closeButton.heightAnchor constraintEqualToConstant: 32].active = YES;
    [self.closeButton.widthAnchor constraintEqualToConstant:  32].active = YES;
}

- (void) setAirplayButtonConstraints:(UIView *) parentView {
    [self.airplayButton.topAnchor constraintEqualToAnchor: parentView.topAnchor constant:36].active = YES;
    [self.airplayButton.trailingAnchor constraintEqualToAnchor: parentView.trailingAnchor constant:-16].active = YES;
    [self.airplayButton.heightAnchor constraintEqualToConstant: 32].active = YES;
    [self.airplayButton.widthAnchor constraintEqualToConstant:  32].active = YES;
}

- (void)setPlayer:(JWPlayerController *)player {
    
    if (_player) {
        // If we already have a player - first dismiss it
        _player.delegate = nil;
        [_player.view removeFromSuperview];
        _player = nil;
    }
    
    player.delegate = self;
    player.view.frame = self.view.bounds;
    
    if (self.closeButton.allTargets.count == 0) {
        [self.closeButton addTarget:self
                             action:@selector(dismiss:)
                   forControlEvents:UIControlEventTouchUpInside];
    }
    
    [self.closeButton removeFromSuperview];
    self.closeButton.alpha = self.isInlinePlayer ? 0.0 : 1.0;
    
    [player.view addSubview:self.closeButton];
    self.closeButton.frame = CGRectZero;
    self.closeButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self setCloseButtonConstraints:player.view];

    [self.view addSubview:player.view];
    [player.view matchParent];
    
    self.player.fullscreen                 = NO;
    self.player.forceFullScreenOnLandscape = NO;
    self.player.forceLandscapeOnFullScreen = NO;
    
    if (self.allowAirplay) {
        [self.airplayButton removeFromSuperview];
        self.airplayButton.alpha = 1.0;
        
        self.airplayButton.translatesAutoresizingMaskIntoConstraints = NO;
    
        [player.view addSubview:self.airplayButton];
        
        [self setAirplayButtonConstraints:player.view];
    }
    
    _player = player;
}

- (void)dismiss:(NSObject *)sender {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.closeButton removeFromSuperview];
        if (self.allowAirplay) {
            [self.airplayButton removeFromSuperview];
        }
        
        self.player.fullscreen = NO;
        [self.player pauseAd:YES];
        UIViewController *vc = self.presentingViewController;
        
        if (vc) {
            [self.player stop];
            
            [vc.view.window makeKeyAndVisible];
            [vc dismissViewControllerAnimated:YES completion:^ {
                
            }];
            [vc setNeedsStatusBarAppearanceUpdate];
            [UIViewController attemptRotationToDeviceOrientation];
        }
    });
}

- (void)adjustButtonAlpha:(BOOL)visible {
    self.closeButton.alpha = visible && !self.isInlinePlayer ? 1.0 : 0.0;
    self.airplayButton.alpha = visible ? 1.0 : 0.0;
}

#pragma mark - Notifications

- (void)airplayButtonTapped:(UIGestureRecognizer *)sender {
    if (self.allowAirplay) {
        [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Tap Cast" parameters:self.extensionsDictionary];
    }
}
- (void)handleScreenConnected:(NSNotification *)sender {
    if ([UIScreen screens].count > 1 && self.allowAirplay) {
        [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Cast Start" parameters:self.extensionsDictionary];
    }
}

- (void)handleScreenDisconnected:(NSNotification *)sender {
    if ([UIScreen screens].count == 1 && self.allowAirplay) {
        [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Cast Stop" parameters:self.extensionsDictionary];
    }
}

#pragma mark - JWPlayerDelegate

- (void)onComplete {
    
}

- (void)onControlBarVisible:(JWEvent<JWControlsEvent> *)event {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self adjustButtonAlpha:event.controls];
    });
}

-(void)onTime:(JWEvent<JWTimeEvent> *)event {
    CGFloat pos = [event position];
    CGFloat dur = [event duration];
    CGFloat per = (pos/dur)*100;

    if (per >= 25.0 && per < 26.0 && self.trackedPercentage < 25) {
        self.trackedPercentage = 25;
    } else if (per >= 50.0 && per < 51.0 && self.trackedPercentage < 50) {
        self.trackedPercentage = 50;
    } else if (per >= 75.0 && per < 76.0 && self.trackedPercentage < 75) {
        self.trackedPercentage = 75;
    } else if (per >= 95.0 && per < 96.0 && self.trackedPercentage < 95) {
        self.trackedPercentage = 95;
    } else {
        return;
    }
    NSMutableDictionary *extensions = self.extensionsDictionary.mutableCopy;
    [extensions setObject:[NSNumber numberWithDouble:self.trackedPercentage]
                   forKey:@"percentage"];
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Watch VOD Percentage"
                                                                 parameters:extensions];
}

-(void)onSeek:(JWEvent<JWSeekEvent> *)event {
    self.trackedPercentage = 0;
}

-(void)onAdPlay:(JWAdEvent<JWAdStateChangeEvent> *)event {
    NSMutableDictionary *extensions = self.extensionsDictionary.mutableCopy;
    [extensions setObject:@"Start"
                   forKey:@"advertisement_position"];
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Watch Video Advertisement"
                                                                 parameters:extensions];
}

-(void)onAdPause:(JWAdEvent<JWAdStateChangeEvent> *)event {
    NSMutableDictionary *extensions = self.extensionsDictionary.mutableCopy;
    [extensions setObject:@"Pause"
                   forKey:@"advertisement_position"];
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Watch Video Advertisement"
                                                                 parameters:extensions];
}

-(void)onAdComplete:(JWAdEvent<JWAdDetailEvent> *)event {
    NSMutableDictionary *extensions = self.extensionsDictionary.mutableCopy;
    [extensions setObject:@"End"
                   forKey:@"advertisement_position"];
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Watch Video Advertisement"
                                                                 parameters:extensions];
}

- (void)onBeforePlay {
    if (isViewHidden == true) {
        [self.player stop];
    }
}

-(void)onPlay:(JWEvent<JWStateChangeEvent> *)event {
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Start Video"
                                                                 parameters:self.extensionsDictionary];
}

-(void)onPause:(JWEvent<JWStateChangeEvent> *)event {
    [[[ZAAppConnector sharedInstance] analyticsDelegate] trackEventWithName:@"Pause Video"
                                                                 parameters:self.extensionsDictionary];
}

- (void)onFullscreen:(JWEvent<JWFullscreenEvent> *)event {
    [self.closeButton removeFromSuperview];
    [NSLayoutConstraint deactivateConstraints:self.closeButton.constraints];
    
    if (self.allowAirplay) {
        [self.airplayButton removeFromSuperview];
        [NSLayoutConstraint deactivateConstraints:self.airplayButton.constraints];
    }
    
    self.isInlinePlayer = !event.fullscreen;
    
    [self adjustButtonAlpha:NO];
    
    if (event.fullscreen) {
        self.player.forceFullScreenOnLandscape = YES;
        if ([[UIDevice currentDevice]orientation] == UIInterfaceOrientationPortrait){
            NSNumber *value = [NSNumber numberWithInt:UIInterfaceOrientationLandscapeRight];
            [[UIDevice currentDevice] setValue:value forKey:@"orientation"];
        }
        
        UIWindow *keyWindow = [UIApplication sharedApplication].keyWindow;
        [keyWindow addSubview:self.closeButton];
        [self setCloseButtonConstraints:keyWindow];
        if (self.allowAirplay) {
            [keyWindow addSubview:self.airplayButton];
            [self setAirplayButtonConstraints:keyWindow];
        }
   }
    else {
        self.player.forceFullScreenOnLandscape = NO;
        [[UIDevice currentDevice] setValue:[NSNumber numberWithInt:UIInterfaceOrientationPortrait] forKey:@"orientation"];
        [self.player.view addSubview:self.closeButton];
        [self setCloseButtonConstraints:self.player.view];
        if (self.allowAirplay) {
            [self.player.view addSubview:self.airplayButton];
            [self setAirplayButtonConstraints:self.player.view];
        }
    }
}

@end
