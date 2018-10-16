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

@interface JWPlayerViewController () <JWPlayerDelegate> {
    
}

@property (nonatomic, strong) JWPlayerController *player;

@end

@implementation JWPlayerViewController
@synthesize isPresentedFullScreen = _isPresentedFullScreen;
@synthesize player = _player;
@synthesize closeButton = _closeButton;

#pragma mark - UIViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationLandscapeRight;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAll;
}

#pragma mark - public

- (void)setIsPresentedFullScreen:(BOOL)isPresentedFullScreen {
    _isPresentedFullScreen = isPresentedFullScreen;
    self.closeButton.hidden = !_isPresentedFullScreen;
}

- (UIButton *)closeButton {
    if (_closeButton) {
        return _closeButton;
    }
    
    CGFloat inset = 8.0;
    CGSize size = CGSizeMake(32.0, 32.0);
    UIBezierPath *path = [UIBezierPath new];
    [path moveToPoint:CGPointMake(inset, inset)];
    [path addLineToPoint:CGPointMake(size.width - inset, size.height - inset)];
    [path moveToPoint:CGPointMake(size.width - inset, size.height - inset)];
    [path addLineToPoint:CGPointMake(inset, size.height - inset)];
    path.lineCapStyle = kCGLineCapRound;
    path.lineJoinStyle = kCGLineJoinRound;
    path.lineWidth = 3.0;
    
    UIGraphicsBeginImageContext(size);
//    CGContextRef context = UIGraphicsGetCurrentContext();
    [UIColor.whiteColor setStroke];
    [path stroke];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    UIButton *button = [UIButton new];
    [button setImage:image forState:UIControlStateNormal];
    [button setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    
    _closeButton = button;
    return button;
}

- (void)setupPlayerWithPlayableItem:(NSObject <ZPPlayable> *)playableItem
{
    JWConfig *config = [JWConfig new];
    config.sources = [NSArray arrayWithObject:[[JWSource alloc] initWithFile:[playableItem contentVideoURLPath]
                                                                       label:@""
                                                                   isDefault:YES]];
    
    config.title = playableItem.playableName;
    config.controls = YES;
    config.repeat = NO;
    config.autostart = YES;
    
    self.player = [[JWPlayerController alloc] initWithConfig:config];
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

- (void)setPlayer:(JWPlayerController *)player {
    player.delegate = self;
    player.view.frame = self.view.bounds;
    player.view.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin |
                                    UIViewAutoresizingFlexibleTopMargin |
                                    UIViewAutoresizingFlexibleLeftMargin |
                                    UIViewAutoresizingFlexibleRightMargin |
                                    UIViewAutoresizingFlexibleWidth |
                                    UIViewAutoresizingFlexibleHeight;
    
    if (self.closeButton.allTargets.count == 0) {
        [self.closeButton addTarget:self
                             action:@selector(dismiss:)
                   forControlEvents:UIControlEventTouchUpInside];
    }
    
    [self.closeButton removeFromSuperview];
    self.closeButton.alpha = 1.0;
    self.closeButton.hidden = !self.isPresentedFullScreen;
    [player.view addSubview:self.closeButton];
    self.closeButton.frame = CGRectMake(16.0, 16.0, 32.0 , 32.0);
    self.closeButton.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleRightMargin;
    [self.view addSubview:player.view];
    
    _player = player;
}

- (void)dismiss:(NSObject *)sender {
    if ([NSThread isMainThread]) {
        [self.player stop];
        UIViewController *vc = self.presentingViewController;
        
        if (vc) {
            [vc.view.window makeKeyAndVisible];
            [vc dismissViewControllerAnimated:YES completion:nil];
            [vc setNeedsStatusBarAppearanceUpdate];
            [UIViewController attemptRotationToDeviceOrientation];
        }
        
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self dismiss:sender];
        });
    }
}

- (void)adjustButtonAlpha:(BOOL)visible {
    self.closeButton.alpha = visible ? 1.0 : 0.0;
}

#pragma mark - JWPlayerDelegate

- (void)onComplete {
    if (self.isPresentedFullScreen) {
        [self dismiss:nil];
    }
}

- (void)onControlBarVisible:(JWEvent<JWControlsEvent> *)event {
    if ([NSThread isMainThread]) {
        [self adjustButtonAlpha:event.controls];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self adjustButtonAlpha:event.controls];
        });
    }
}

@end
