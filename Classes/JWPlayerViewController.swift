//
//  JWPlayerViewController.swift
//  JWPlayer-Plugin-iOS
//
//  Created by Roi Mulia on 10/1/17.
//  Copyright © 2017 Applicaster. All rights reserved.
//

import UIKit
import ZappPlugins
import ApplicasterSDK
import JWPlayerSDKWrapper

final class JWPlayerViewController: UIViewController, JWPlayerDelegate {
    
    var isPresentedFullScreen = false {
        didSet {
            closeButton.isHidden = !isPresentedFullScreen
        }
    }
    
    private let closeButton: UIButton = {
        
        let inset: CGFloat = 8
        let size = CGSize(width: 32, height: 32)
        let path = UIBezierPath()
        path.move(to: CGPoint(x: inset, y: inset))
        path.addLine(to: CGPoint(x: size.width - inset, y: size.height - inset))
        path.move(to: CGPoint(x: size.width - inset, y: inset))
        path.addLine(to: CGPoint(x: inset, y: size.height - inset))
        path.lineCapStyle = .round
        path.lineJoinStyle = .round
        path.lineWidth = 3
        
        UIGraphicsBeginImageContext(CGSize(width: 32, height: 32))
        let context = UIGraphicsGetCurrentContext()!
        UIColor.white.setStroke()
        path.stroke()
        let image = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()
        
        let btn = UIButton()
        btn.setImage(image, for: .normal)
        btn.setTitleColor(UIColor.white, for: .normal)
        return btn
    }()
    
    override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
        return .landscapeRight
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        if (player.isInFullscreen || isPresentedFullScreen) && UI_USER_INTERFACE_IDIOM() == .phone {
            return [.landscapeLeft, .landscapeRight]
        }
        return .all
    }
    
    func onComplete() {
        if isPresentedFullScreen {
            DispatchQueue.main.async {
                self.player?.stop()
                self.dismiss(animated: true, completion: nil)
                UIViewController.attemptRotationToDeviceOrientation()
            }
        }
    }
    
    func onControlBarVisible(_ isVisible: Bool) {
        if Thread.isMainThread {
            closeButton.alpha = isVisible ? 1 : 0
        } else {
            DispatchQueue.main.async {
                self.closeButton.alpha = isVisible ? 1 : 0
            }
        }
    }
    
    @objc private func dismissMe(_ sender: Any?) {
        if Thread.isMainThread {
            self.player?.stop()
            dismiss(animated: true, completion: nil)
            UIViewController.attemptRotationToDeviceOrientation()
        } else {
            DispatchQueue.main.async {
                self.dismissMe(sender)
            }
        }
    }
    
    var player: JWPlayerController! {
        didSet {
            player.delegate = self
            
            player.view.frame = view.bounds
            player.view.autoresizingMask = [
                UIViewAutoresizing.flexibleBottomMargin,
                UIViewAutoresizing.flexibleHeight,
                UIViewAutoresizing.flexibleLeftMargin,
                UIViewAutoresizing.flexibleRightMargin,
                UIViewAutoresizing.flexibleTopMargin,
                UIViewAutoresizing.flexibleWidth]
            
            if closeButton.allTargets.isEmpty {
                closeButton.addTarget(self, action: #selector(JWPlayerViewController.dismissMe(_:)), for: .touchUpInside)
            }
            
            closeButton.removeFromSuperview()
            closeButton.alpha = 1
            closeButton.isHidden = !isPresentedFullScreen
            player.view.addSubview(closeButton)
            closeButton.frame = CGRect(origin: CGPoint(x: 16, y: 16), size: closeButton.intrinsicContentSize)
            closeButton.autoresizingMask = [.flexibleBottomMargin, .flexibleRightMargin]
            player.forceLandscapeOnFullScreen = true
            view.addSubview(player.view)
        }
    }
}
