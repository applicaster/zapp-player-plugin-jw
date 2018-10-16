//
//  ZappPlayerAdapter.m
//  JWPlayerPlugin
//
//  Created by Liviu Romascanu on 15/10/2018.
//  Copyright © 2018 Applicaster. All rights reserved.
//

#import "ZappJWPlayerAdapter.h"
#import "JWPlayer_iOS_SDK/JWPlayerController.h"

@implementation ZappJWPlayerAdapter

#pragma mark - ZPPlayerProtocol

+ (id<ZPPlayerProtocol>)pluggablePlayerInitWithPlayableItems:(NSArray<id<ZPPlayable>> *)items configurationJSON:(NSDictionary *)configurationJSON {
    NSString *playerKey = configurationJSON[@"playerKey"];
    
    if (![playerKey isNotEmptyOrWhiteSpaces]) {
        return nil;
    }
    
    [JWPlayerController setPlayerKey:playerKey];
    
    ZappJWPlayerAdapter *instance = [ZappJWPlayerAdapter new];
    instance.playerViewController = [JWPlayerViewController new];
    instance.currentPlayableItem = items.firstObject;
    instance.configurationJSON = configurationJSON;
    instance.currentPlayableItems = items;
    
    return instance;
}

- (void)pluggablePlayerAddInline:(UIViewController * _Nonnull)rootViewController container:(UIView * _Nonnull)container {
    self.playerViewController.isPresentedFullScreen = NO;
    [rootViewController addChildViewController:self.playerViewController toView:container];
    [self.playerViewController.view matchParent];
}

- (void)pluggablePlayerRemoveInline {
    UIView *container = self.playerViewController.view.superview;
    [self.playerViewController removeViewFromParentViewController];
    [container removeFromSuperview];
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
    if (self.currentPlayableItem == nil) {
        return nil;
    }
    
    [self.playerViewController setupPlayerWithPlayableItem:self.currentPlayableItem];
    return self.playerViewController;
}

- (void)presentPlayerFullScreen:(UIViewController * _Nonnull)rootViewController configuration:(ZPPlayerConfiguration * _Nullable)configuration {
    [self presentPlayerFullScreen:rootViewController configuration:configuration completion:nil];
}

- (void)presentPlayerFullScreen:(UIViewController *)rootViewController configuration:(ZPPlayerConfiguration *)configuration completion:(void (^)(void))completion {
    self.playerViewController.isPresentedFullScreen = YES;
    [[rootViewController topmostModalViewController] presentViewController:self.playerViewController
                                                                  animated:configuration.animated
                                                                completion:^{
                                                                    if (completion) {
                                                                        completion();
                                                                    }
                                                                }];
}

- (NSObject<ZPPlayable> *)currentPlayableItem {
    return self.currentPlayableItem;
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

@end
