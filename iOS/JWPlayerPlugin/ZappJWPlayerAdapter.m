//
//  ZappPlayerAdapter.m
//  JWPlayerPlugin
//
//  Created by Liviu Romascanu on 15/10/2018.
//  Copyright © 2018 Applicaster. All rights reserved.
//

@import ZappCore;

#import "ZappJWPlayerAdapter.h"
#import "JWPlayer_iOS_SDK/JWPlayerController.h"

@implementation ZappJWPlayerAdapter

@synthesize screenPluginDelegate;

static NSString *const kPlayableItemsKey = @"playable_items";

#pragma mark - ZPPlayerProtocol

+ (id<ZPPlayerProtocol>)pluggablePlayerInitWithPlayableItems:(NSArray<id<ZPPlayable>> *)items configurationJSON:(NSDictionary *)configurationJSON {
    NSString *playerKey = configurationJSON[@"playerKey"];
    
    if (![playerKey isNotEmptyOrWhiteSpaces]) {
        return nil;
    }
    
    [JWPlayerController setPlayerKey:playerKey];
    
    ZappJWPlayerAdapter *instance = [ZappJWPlayerAdapter new];
    instance.configurationJSON = configurationJSON;
    instance.playerViewController = [JWPlayerViewController new];
    instance.playerViewController.configurationJSON = configurationJSON;
    instance.currentPlayableItem = items.firstObject;
    instance.currentPlayableItems = items;
    
    id airplayValue = configurationJSON[@"airplay"];
    
    if (airplayValue && [airplayValue isKindOfClass:[NSString class]]) {
        instance.allowAirplay = [((NSString*)airplayValue) isEqualToString:@"1"];
    } else if (airplayValue && [airplayValue isKindOfClass:[NSNumber class]]) {
        instance.allowAirplay = [((NSNumber*)airplayValue) boolValue];
    }
    
    return instance;
}

- (void)pluggablePlayerAddInline:(UIViewController * _Nonnull)rootViewController container:(UIView * _Nonnull)container {
    [self pluggablePlayerAddInline:rootViewController
                         container:container
                     configuration:nil];
}

- (void)pluggablePlayerAddInline:(UIViewController *)rootViewController container:(UIView *)container configuration:(ZPPlayerConfiguration *)configuration {
    if ([self.currentPlayableItem isFree] == NO) {
        NSObject<ZPLoginProviderUserDataProtocol> *loginPlugin = [[ZAAppConnector sharedInstance].pluginsDelegate.loginPluginsManager createWithUserData];
        NSDictionary *extensions = [NSDictionary dictionaryWithObject:self.currentPlayableItems
                                                               forKey:kPlayableItemsKey];
        if ([loginPlugin respondsToSelector:@selector(isUserComplyWithPolicies:)]) {
            [self handleUserComply:[loginPlugin isUserComplyWithPolicies:extensions]
                       loginPlugin:loginPlugin
                rootViewController:rootViewController
                         container:container
                     configuration:configuration
                        completion:nil];
        } else if ([loginPlugin respondsToSelector:@selector(isUserComplyWithPolicies:completion:)]) {
            __block typeof(self) blockSelf = self;
            [loginPlugin isUserComplyWithPolicies:extensions
                                       completion:^(BOOL isUserComply) {
                                           [blockSelf handleUserComply:isUserComply
                                                           loginPlugin:loginPlugin
                                                    rootViewController:rootViewController
                                                             container:container
                                                         configuration:configuration
                                                            completion:nil];
                                       }];
        } else {
            // login protocol doesn't handle the checks - let the player go
            [self playInline:rootViewController
                   container:container
               configuration:configuration
                  completion:nil];
        }
    } else {
        // item is free
        [self playInline:rootViewController
               container:container
           configuration:configuration
              completion:nil];
    }
}

- (void)pluggablePlayerRemoveInline {
    [self.playerViewController removeViewFromParentViewController];
}

- (BOOL)pluggablePlayerIsPlaying {
    BOOL isPlaying = [self.playerViewController isPlaying];
    self.playerState = isPlaying ? ZPPlayerStatePlaying : ZPPlayerStateStopped;
    return isPlaying;
}

- (void)pluggablePlayerPause {
    [self.playerViewController pause];
    self.playerState = ZPPlayerStatePaused;
}

- (void)pluggablePlayerPlay:(ZPPlayerConfiguration * _Nullable)configuration {
    [self.playerViewController play];
    self.playerState = ZPPlayerStatePlaying;
    
    NSError *error = nil;
    [AVAudioSession.sharedInstance setCategory:AVAudioSessionCategoryPlayback
                                   withOptions:0
                                         error:&error];
    [AVAudioSession.sharedInstance setActive:YES
                                       error:&error];
}

- (void)pluggablePlayerPlay:(NSArray<id<ZPPlayable>> *)items configuration:(ZPPlayerConfiguration *)configuration {
    [self.playerViewController stop];
    self.currentPlayableItems = items;
    self.currentPlayableItem = items.firstObject;
    [self pluggablePlayerPlay:configuration];
}

- (void)pluggablePlayerStop {
    [self.playerViewController stop];
    self.playerState = ZPPlayerStateStopped;
}

- (UIViewController * _Nullable)pluggablePlayerViewController {
    return self.playerViewController;
}

- (void)presentPlayerFullScreen:(UIViewController * _Nonnull)rootViewController configuration:(ZPPlayerConfiguration * _Nullable)configuration {
    [self presentPlayerFullScreen:rootViewController configuration:configuration completion:nil];
}

- (void)presentPlayerFullScreen:(UIViewController *)rootViewController configuration:(ZPPlayerConfiguration *)configuration completion:(void (^)(void))completion {
    if ([self.currentPlayableItem isFree] == NO) {
        NSObject<ZPLoginProviderUserDataProtocol> *loginPlugin = [[ZAAppConnector sharedInstance].pluginsDelegate.loginPluginsManager createWithUserData];
        NSDictionary *extensions = [NSDictionary dictionaryWithObject:self.currentPlayableItems
                                                               forKey:kPlayableItemsKey];
        if ([loginPlugin respondsToSelector:@selector(isUserComplyWithPolicies:)]) {
            [self handleUserComply:[loginPlugin isUserComplyWithPolicies:extensions]
                       loginPlugin:loginPlugin
                rootViewController:rootViewController
                         container:nil
                     configuration:configuration
                        completion:completion];
        } else if ([loginPlugin respondsToSelector:@selector(isUserComplyWithPolicies:completion:)]) {
            __block typeof(self) blockSelf = self;
            [loginPlugin isUserComplyWithPolicies:extensions
                                       completion:^(BOOL isUserComply) {
                                           [blockSelf handleUserComply:isUserComply
                                                           loginPlugin:loginPlugin
                                                    rootViewController:rootViewController
                                                             container:nil
                                                         configuration:configuration
                                                            completion:completion];
                                       }];
        } else {
            // login protocol doesn't handle the checks - let the player go
            [self playFullScreen:rootViewController
                   configuration:configuration
                      completion:completion];
        }
    } else {
        // item is free
        [self playFullScreen:rootViewController
               configuration:configuration
                  completion:completion];
    }
}

- (enum ZPPlayerType)pluggablePlayerType {
    return ZappJWPlayerAdapter.pluggablePlayerType;
}

+ (enum ZPPlayerType)pluggablePlayerType {
    return ZPPlayerTypeUndefined;
}

- (id<ZPPlayable>)pluggablePlayerFirstPlayableItem {
    return self.currentPlayableItem;
}

- (BOOL)pluggablePlayerCanHandleSubscription {
    return YES;
}

#pragma mark - public

- (NSURL * _Nullable)pluggablePlayerCurrentUrl {
    return [[NSURL alloc] initWithString:self.currentPlayableItem.contentVideoURLPath];
}

- (void)setIsOnHold:(BOOL)isOnHold {
    _isOnHold = isOnHold;
    self.currentPlayerState = ZPPlayerStateOnHold;
}

#pragma mark - private

- (void)setPlayerState:(ZPPlayerState)state {
    if (!self.isOnHold) {
        self.currentPlayerState = state;
    }
}

- (enum ZPPlayerState)playerState {
    return self.currentPlayerState;
}

- (void)setupPlayerWithCurrentPlayableItemUsingInlinePlayer:(BOOL)inlinePlayer {
    self.playerViewController.allowAirplay = self.allowAirplay;
    self.playerViewController.isInlinePlayer = inlinePlayer;
    [self.playerViewController setupPlayerWithPlayableItem:self.currentPlayableItem];
    self.playerViewController.isLive = [self.currentPlayableItem isKindOfClass:[APChannel class]];
    
    NSArray *ads = self.currentPlayableItem.extensionsDictionary[@"videoAds"];
    if (ads == nil) {
        ads = self.currentPlayableItem.extensionsDictionary[@"video_ads"];
    }
    
    // Setup Ads
    [self.playerViewController setupPlayerAdvertisingWithConfiguration:ads];

    // Setup Subtitle Tracks if provided
    NSArray *subtitleTracksArray = self.currentPlayableItem.extensionsDictionary[@"sideCarCaptions"];
    if (subtitleTracksArray == nil) {
        subtitleTracksArray = self.currentPlayableItem.extensionsDictionary[@"text_tracks"][@"tracks"];
    }
    if (subtitleTracksArray != nil) {
        [self.playerViewController setupPlayerSubtitleTracksWithConfiguration:subtitleTracksArray];
    }
}

- (void)handleUserComply:(BOOL)isUserComply
             loginPlugin:(NSObject<ZPLoginProviderUserDataProtocol> *)plugin
      rootViewController:(UIViewController *)rootViewController
               container:(UIView *)container
           configuration:(ZPPlayerConfiguration *)configuration
              completion:(void (^)(void))completion
{
    if (isUserComply) {
        if (container) {
            [self playInline:rootViewController
                   container:container
               configuration:configuration
                  completion:completion];
        } else {
            [self playFullScreen:rootViewController
                   configuration:configuration
                      completion:completion];
        }
    } else {
        __block typeof(self) blockSelf = self;
        NSDictionary *playableItems = [NSDictionary dictionaryWithObject:[self currentPlayableItems] forKey:kPlayableItemsKey];
        [plugin login:playableItems
           completion:^(enum ZPLoginOperationStatus status) {
               if (status == ZPLoginOperationStatusCompletedSuccessfully) {
                   if (container) {
                       [blockSelf playInline:rootViewController
                                   container:container
                               configuration:configuration
                                  completion:completion];
                   } else {
                       [blockSelf playFullScreen:rootViewController
                                   configuration:configuration
                                      completion:completion];
                   }
               }
           }];
    }
}

- (void)playFullScreen:(UIViewController *)rootViewController
         configuration:(ZPPlayerConfiguration *)configuration
            completion:(void (^)(void))completion {
    __block typeof(self) blockSelf = self;
    [self loadItemIfNeeded:^{
        [blockSelf setupPlayerWithCurrentPlayableItemUsingInlinePlayer:NO];

        [[rootViewController topmostModalViewController] presentViewController:blockSelf.playerViewController
                                                                      animated:configuration.animated
                                                                    completion:^{
                                                                        if (completion) {
                                                                            completion();
                                                                        }
                                                                    }];
    }];
}

- (void)playInline:(UIViewController *)rootViewController
         container:(UIView *)container
     configuration:(ZPPlayerConfiguration *)configuration
        completion:(void (^)(void))completion {
    __block typeof(self) blockSelf = self;
    [self loadItemIfNeeded:^{
        [blockSelf setupPlayerWithCurrentPlayableItemUsingInlinePlayer:YES];

        [rootViewController addChildViewController:blockSelf.playerViewController toView:container];
        [blockSelf.playerViewController.view matchParent];
        
        if (completion) {
            completion();
        }
    }];
}

- (void)loadItemIfNeeded:(void (^)(void))completion {
    if (([[self currentPlayableItem] isKindOfClass:[APVodItem class]] || [[self currentPlayableItem] isKindOfClass:[APChannel class]]) &&
        (![(APModel *)[self currentPlayableItem] isLoaded] || ![self.currentPlayableItem.contentVideoURLPath isNotEmptyOrWhiteSpaces])) {
        __block typeof(self) blockSelf = self;
        APModel *model = (APModel *)[self currentPlayableItem];
        [model loadWithCompletionHandler:^(BOOL success, APModel *model) {
            blockSelf.currentPlayableItem = (NSObject <ZPPlayable>*)model;
            completion();
        }];
    } else if ([[self currentPlayableItem] isKindOfClass:[APAtomEntryPlayable class]] && ![self.currentPlayableItem.contentVideoURLPath isNotEmptyOrWhiteSpaces])  {
        APAtomEntryPlayable *model = (APAtomEntryPlayable *)[self currentPlayableItem];
        
        // check if we need to load the streamURL of the model
        if (![self loadVideoURLPathIfNeeded:model completion:completion]) {
            NSNumber *channelID = model.extensionsDictionary[@"applicaster_channel_id"];
            APChannel *channel = [[APApplicasterController sharedInstance].account channelByUniqueID:[channelID stringValue]];
            if (channel) {
                __block typeof(self) blockSelf = self;
                [channel loadWithCompletionHandler:^(BOOL success, APModel *model) {
                    blockSelf.currentPlayableItem = (NSObject <ZPPlayable>*)model;
                    completion();
                }];
            } else {
                completion();
            }
        }
    } else {
        completion();
    }
}

// Update streamURL of the playable
// This fixes an issue when using Applicaster2 and the entry->content has no src field or src is empty
// returns YES if it needed to update the streamURL, NO otherwise
- (BOOL)loadVideoURLPathIfNeeded:(APAtomEntryPlayable*)model completion:(void (^)(void))completion {
    if (model.contentVideoURLPath == nil || [model.contentVideoURLPath isEqualToString:@""]) {
        [model loadAtomEntryContentUrl:^(NSString * newStreamURL) {
            [model updateStreamUrl:newStreamURL];
            dispatch_async(dispatch_get_main_queue(), ^{
                completion();
            });
        }];
        return YES;
    }
    
    return NO;
}

#pragma mark - ZPPluggableScreenProtocol / ZPUIBuilderPluginsProtocol

//Necessary to implement this method to allow prehooks, however this constructor is never called for a player
- (instancetype)initWithPluginModel:(ZPPluginModel *)pluginModel screenModel:(ZLScreenModel *)screenModel dataSourceModel:(NSObject *)dataSourceModel {
    return nil;
}

@end
