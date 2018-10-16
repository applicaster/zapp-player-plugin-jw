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

@property (nonatomic, assign) BOOL isPresentedFullScreen;
@property (nonatomic, strong) UIButton *closeButton;

- (void)setupPlayerWithPlayableItem:(NSObject <ZPPlayable> *)playableItem;
- (void)play;
- (void)pause;
- (void)stop;
- (BOOL)isPlaying;

@end

NS_ASSUME_NONNULL_END
