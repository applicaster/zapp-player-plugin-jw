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

#import <GoogleCast/GoogleCast.h>
#import "JWPlayerPlugin/JWPlayerPlugin-Swift.h"

@interface JWPlayerViewController () <JWPlayerDelegate, JWCastingDelegate> {
    BOOL isViewHidden;
}

@property (nonatomic, strong) JWPlayerController *player;
@property (nonatomic, strong) JWCastController *castController;
@property (nonatomic, strong) JWAdConfig *adConfig;

@property (nonatomic, strong) NSDictionary *extensionsDictionary;

@property (nonatomic) UIStackView *buttonsStackView;
@property (nonatomic, strong) UIButton *closeButton;
@property (nonatomic, strong) UIView *airplayButton;
@property (nonatomic) UIButton *castingButton;

@property (nonatomic, strong) NSArray *availableCastDevices;
@property (nonatomic) BOOL casting;

@end

@implementation JWPlayerViewController

#pragma mark - UIViewController

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.analyticsStorage = [AnalyticsStorage new];
    }
    return self;
}

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

- (void)setAllowChromecast:(BOOL)allowChromecast {
    _allowChromecast = allowChromecast;
    
    if (allowChromecast) {
        if (self.castController == nil) {
            self.castController = [[JWCastController alloc] initWithPlayer:self.player];
            //self.castController.chromeCastReceiverAppID = kGCKDefaultMediaReceiverApplicationID;
            [self.castController scanForDevices];
        }
        
        self.castController.delegate = self;
    } else {
        self.castController.delegate = nil;
    }
}

#pragma mark - UI Stuff

- (UIStackView *)buttonsStackView {
    if (_buttonsStackView) {
        return _buttonsStackView;
    }
    
    _buttonsStackView = [[UIStackView alloc] initWithArrangedSubviews:@[]];
    _buttonsStackView.axis = UILayoutConstraintAxisHorizontal;
    _buttonsStackView.alignment = UIStackViewAlignmentTrailing;
    _buttonsStackView.distribution = UIStackViewDistributionFill;
    
    return _buttonsStackView;
}

- (UIButton *)closeButton {
    if (_closeButton) {
        return _closeButton;
    }
    
    UIButton *button = [UIButton new];
    button.titleLabel.font = [UIFont systemFontOfSize:25];
    [button setTitle:@"X" forState:UIControlStateNormal];
    [button setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    
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
    
    return buttonView;
}

- (UIButton*)castingButton {
    if (_castingButton) {
        return _castingButton;
    }
    
    CGRect frame = CGRectMake(0, 0, 22, 22);
    _castingButton = [[UIButton alloc]initWithFrame:frame];
    
    [_castingButton addTarget:self
                           action:@selector(castButtonTapped:)
                 forControlEvents:UIControlEventTouchUpInside];
    [_castingButton setImage:[[self imageForCastOff] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate] forState:UIControlStateNormal];
    _castingButton.imageView.contentMode = UIViewContentModeScaleAspectFit;
    _castingButton.tintColor = [UIColor whiteColor];
    
    return _castingButton;
}

// MARK: -

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
    [self.analyticsStorage parseParametersFrom:playableItem];
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

- (void)setButtonStackViewContraints:(UIView*)parentView {
    [self.buttonsStackView.topAnchor constraintEqualToAnchor:parentView.topAnchor constant:36.0].active = YES;
    [self.buttonsStackView.leadingAnchor constraintGreaterThanOrEqualToAnchor:self.closeButton.trailingAnchor constant:16.0].active = YES;
    [self.buttonsStackView.trailingAnchor constraintEqualToAnchor:parentView.trailingAnchor constant:-16.0].active = YES;
    [self.buttonsStackView.heightAnchor constraintEqualToConstant:32.0].active = YES;
}

- (void)setCloseButtonConstraints:(UIView *) parentView {
    [self.closeButton.topAnchor constraintEqualToAnchor: parentView.topAnchor constant:36].active = YES;
    [self.closeButton.leadingAnchor constraintEqualToAnchor: parentView.leadingAnchor constant:16].active = YES;
    [self.closeButton.heightAnchor constraintEqualToConstant: 32].active = YES;
    [self.closeButton.widthAnchor constraintEqualToConstant:  32].active = YES;
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
    
    [self.view addSubview:player.view];
    [player.view matchParent];
    
    if (self.closeButton.allTargets.count == 0) {
        [self.closeButton addTarget:self
                             action:@selector(dismiss:)
                   forControlEvents:UIControlEventTouchUpInside];
    }
    
    [self.closeButton removeFromSuperview];
    [player.view addSubview:self.closeButton];
    self.closeButton.translatesAutoresizingMaskIntoConstraints = NO;
    self.closeButton.alpha = self.isInlinePlayer ? 0.0 : 1.0;
    [self setCloseButtonConstraints:player.view];

    [self.buttonsStackView removeFromSuperview];
    [player.view addSubview:self.buttonsStackView];
    self.buttonsStackView.translatesAutoresizingMaskIntoConstraints = NO;
    [self setButtonStackViewContraints:player.view];
    
    self.player.fullscreen                 = NO;
    self.player.forceFullScreenOnLandscape = NO;
    self.player.forceLandscapeOnFullScreen = NO;
    
    _player = player;
}

- (void)addCastButtons {
    if (self.allowAirplay) {
        [self.airplayButton removeFromSuperview];
        self.airplayButton.alpha = 1.0;
        [self.buttonsStackView addArrangedSubview:self.airplayButton];
    }
    
//    if (self.allowChromecast) {
        [self.castingButton removeFromSuperview];
        self.castingButton.alpha = 1.0;
        [self.buttonsStackView addArrangedSubview:self.castingButton];
//    }
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
    self.castingButton.alpha = visible ? 1.0 : 0.0;
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
    self.analyticsStorage.isCompleted = true;
}

- (void)onControlBarVisible:(JWEvent<JWControlsEvent> *)event {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self adjustButtonAlpha:event.controls];
    });
}

-(void)onTime:(JWEvent<JWTimeEvent> *)event {
    CGFloat pos = [event position];
    CGFloat dur = [event duration];
    CGFloat percentage = (pos / dur) * 100;
    
    self.analyticsStorage.duration = dur;
    self.analyticsStorage.videoProgress = percentage;
}

-(void)onSeek:(JWEvent<JWSeekEvent> *)event {
    self.analyticsStorage.videoProgress = 0;
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
    [self.buttonsStackView removeFromSuperview];
    [NSLayoutConstraint deactivateConstraints:self.buttonsStackView.constraints];
    
    self.isInlinePlayer = !event.fullscreen;
    if (event.fullscreen) {
        self.analyticsStorage.playerViewType = PlayerViewTypeFullScreen;
    } else {
        self.analyticsStorage.playerViewType = PlayerViewTypeInline;
    }
    
    [self adjustButtonAlpha:NO];
    
    if (event.fullscreen) {
        self.player.forceFullScreenOnLandscape = YES;
        if ([[UIDevice currentDevice]orientation] == UIInterfaceOrientationPortrait){
            NSNumber *value = [NSNumber numberWithInt:UIInterfaceOrientationLandscapeRight];
            [[UIDevice currentDevice] setValue:value forKey:@"orientation"];
        }
        
        UIWindow *keyWindow = [UIApplication sharedApplication].keyWindow;
        [keyWindow addSubview:self.buttonsStackView];

        [self.buttonsStackView addArrangedSubview:self.closeButton];
        if (self.allowAirplay) {
            [self.buttonsStackView addArrangedSubview:self.airplayButton];
        }
        if (self.allowChromecast) {
            [self.buttonsStackView addArrangedSubview:self.castingButton];
        }
        
        [self setButtonStackViewContraints:keyWindow];
   }
    else {
        self.player.forceFullScreenOnLandscape = NO;
        [[UIDevice currentDevice] setValue:[NSNumber numberWithInt:UIInterfaceOrientationPortrait] forKey:@"orientation"];
        
        [self.player.view addSubview:self.buttonsStackView];
        [self setButtonStackViewContraints:self.player.view];
        
        if (self.allowAirplay) {
            [self.buttonsStackView addArrangedSubview:self.airplayButton];
        }
        if (self.allowChromecast) {
            [self.buttonsStackView addArrangedSubview:self.castingButton];
        }
    }
}

// MARK: - Chromecast support

-(void)onCastingDevicesAvailable:(NSArray *)devices
{
    self.availableCastDevices = devices;
    
    if (devices.count > 0 && self.allowChromecast) {
        [self.castingButton setHidden:NO];
        [self updateForCastDeviceDisconnection];
    } else if (devices.count == 0) {
        [self.castingButton setHidden:YES];
    }
}

-(void)onUserSelectedDevice:(NSInteger)index
{
    JWCastingDevice *chosenDevice = self.availableCastDevices[index];
    [self.castController connectToDevice:chosenDevice];
}

-(void)onConnectedToCastingDevice:(JWCastingDevice *)device
{
    [self updateForCastDeviceConnection];
    [self.castController cast];
}

-(void)onDisconnectedFromCastingDevice:(NSError *)error
{
    [self updateForCastDeviceDisconnection];
}

-(void)onConnectionTemporarilySuspended
{
    [self updateWhenConnectingToCastDevice];
}

-(void)onConnectionRecovered
{
    [self updateForCastDeviceConnection];
}

-(void)onConnectionFailed:(NSError *)error
{
    if(error) {
        NSLog(@"Connection Error: %@", error);
    }
    [self updateForCastDeviceDisconnection];
}

-(void)onCasting
{
    [self updateForCasting];
}

-(void)onCastingEnded:(NSError *)error
{
    if(error) {
        NSLog(@"Casting Error: %@", error);
    }
    [self updateForCastingEnd];
}

-(void)onCastingFailed:(NSError *)error
{
    if(error) {
        NSLog(@"Casting Error: %@", error);
    }
    [self updateForCastingEnd];
}

#pragma Mark - Casting Status Helpers

- (void)updateWhenConnectingToCastDevice
{
    [self.castingButton setTintColor:[UIColor whiteColor]];
}

- (void)updateForCastDeviceConnection
{
    [self.castingButton setImage:[[self imageForCastOn] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate]
                        forState:UIControlStateNormal];
    [self.castingButton setTintColor:[UIColor blueColor]];
}

- (void)updateForCastDeviceDisconnection
{
    [self.castingButton setImage:[[self imageForCastOff] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate]
                        forState:UIControlStateNormal];
    [self.castingButton setTintColor:[UIColor whiteColor]];
}

- (void)updateForCasting
{
    self.casting = YES;
    [self.castingButton setTintColor:[UIColor greenColor]];
    self.analyticsStorage.isCasting = true;
    self.analyticsStorage.playerViewType = PlayerViewTypeCast;
    [self.analyticsStorage sendWithAnalyticsEvent:AnalyticsEventsCastStart
                                            timed:true];
}

- (void)updateForCastingEnd
{
    self.casting = NO;
    [self.castingButton setTintColor:[UIColor blueColor]];
    self.analyticsStorage.isCasting = false;
    [self.analyticsStorage sendWithAnalyticsEvent:AnalyticsEventsCastStop
                                            timed:true];
}

#pragma Mark - Cast stuff

- (void)castButtonTapped:(id) sender
{
    [self.analyticsStorage sendWithAnalyticsEvent:AnalyticsEventsTapCast
                                            timed:false];
    __weak JWPlayerViewController *weakSelf = self;
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                             message:nil
                                                                      preferredStyle:UIAlertControllerStyleActionSheet];
    alertController.popoverPresentationController.sourceView = self.castingButton;
    alertController.popoverPresentationController.sourceRect = self.castingButton.frame;
    if (self.castController.connectedDevice == nil) {
        alertController.title = @"Connect to";
        
        [self.castController.availableDevices enumerateObjectsUsingBlock:^(JWCastingDevice  *_Nonnull device,
                                                                           NSUInteger idx,
                                                                           BOOL * _Nonnull stop) {
            UIAlertAction *deviceSelected = [UIAlertAction actionWithTitle:device.name
                                                                     style:UIAlertActionStyleDefault
                                                                   handler:^(UIAlertAction * _Nonnull action) {
                [weakSelf.castController connectToDevice:device];
                [weakSelf updateWhenConnectingToCastDevice];
                weakSelf.analyticsStorage.castingDevice = device.name;
            }];
            [alertController addAction:deviceSelected];
        }];
    } else {
        alertController.title = self.castController.connectedDevice.name;
        alertController.message = @"Select an action";
        
        UIAlertAction *disconnect = [UIAlertAction actionWithTitle:@"Disconnect"
                                                             style:UIAlertActionStyleDestructive
                                                           handler:^(UIAlertAction * _Nonnull action) {
                                                               [weakSelf.castController disconnect];
                                                           }];
        [alertController addAction:disconnect];
        
        UIAlertAction *castControl;
        if (self.casting) {
            castControl = [UIAlertAction actionWithTitle:@"Stop Casting"
                                                   style:UIAlertActionStyleDefault
                                                 handler:^(UIAlertAction * _Nonnull action) {
                                                     [weakSelf.castController stopCasting];
                                                 }];
        } else {
            castControl = [UIAlertAction actionWithTitle:@"Cast"
                                                   style:UIAlertActionStyleDefault
                                                 handler:^(UIAlertAction * _Nonnull action) {
                                                     [weakSelf.castController cast];
                                                 }];
        }
        [alertController addAction:castControl];
    }
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"Cancel"
                            style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {}];
    [alertController addAction:cancel];
    
    [self presentViewController:alertController animated:YES completion:^{}];
}

// MARK: - Chromecast Images

- (UIImage*)imageForCastOn {
    NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:@"cast_on" ofType:@"png"];
    
    if (self.chromecastButtonOnPath && ![self.chromecastButtonOnPath isEqualToString:@""]) {
        path = self.chromecastButtonOnPath;
    }
    
    return [[UIImage alloc] initWithContentsOfFile:path];
}

- (UIImage*)imageForCastOff {
    NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:@"cast_off" ofType:@"png"];
    
    if (self.chromecastButtonOffPath && ![self.chromecastButtonOffPath isEqualToString:@""]) {
        path = self.chromecastButtonOffPath;
    }
    
    return [[UIImage alloc] initWithContentsOfFile:path];
}

@end
