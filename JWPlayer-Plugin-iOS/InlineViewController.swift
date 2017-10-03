//
//  InlineViewController.swift
//  JWPlayer-Plugin-iOS
//
//  Created by Udi Lumnitz on 02/10/2017.
//  Copyright © 2017 Applicaster. All rights reserved.
//

import Foundation
import ZappPlugins
import ApplicasterSDK

class InlineViewController: UIViewController {
    
    @IBOutlet weak var VideoContainerView: UIView!
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        startPlayVideo()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        // Getting the current player instance and stopping the video player when our view is going to disappear.
        ZPPlayerManager.sharedInstance.lastActiveInstance?.pluggablePlayerStop()
    }
    
    func startPlayVideo() {
        let item:ZPPlayable = APURLPlayable(streamURL: "https://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", name: "Test Video", description: "")
        let pluggablePlayer = ZPPlayerManager.sharedInstance.create(playableItem: item)
        pluggablePlayer.pluggablePlayerAddInline(self, container: VideoContainerView)
        pluggablePlayer.pluggablePlayerPlay(nil)
    }
    
}
