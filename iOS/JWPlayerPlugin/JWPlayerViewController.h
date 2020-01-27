//
//  JWPlayerViewController.h
//  JWPlayerPlugin
//
//  Created by Liviu Romascanu on 15/10/2018.
//  Copyright © 2018 Applicaster. All rights reserved.
//

#import <UIKit/UIKit.h>
@import ZappPlugins;

NS_ASSUME_NONNULL_BEGIN

@interface JWPlayerViewController : UIViewController

@property (nonatomic, strong) NSDictionary *configurationJSON;
@property (nonatomic, assign) BOOL isLive;
@property (nonatomic, assign) BOOL allowAirplay;
@property (nonatomic, assign) BOOL allowChromecast;
@property (nonatomic, nullable) NSString *chromecastButtonOnPath;
@property (nonatomic, nullable) NSString *chromecastButtonOffPath;

@property (nonatomic, assign) BOOL isInlinePlayer;

- (void)setupPlayerWithPlayableItem:(NSObject <ZPPlayable> *)playableItem;
- (void)setupPlayerAdvertisingWithConfiguration:(NSArray *)ads;
- (void)setupPlayerSubtitleTracksWithConfiguration:(NSArray *)subtitleTracks;
- (void)play;
- (void)pause;
- (void)stop;
- (BOOL)isPlaying;

@end

NS_ASSUME_NONNULL_END
