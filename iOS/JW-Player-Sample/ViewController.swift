//
//  ViewController.swift
//  JW-Player-Sample
//
//  Created by Roman Karpievich on 2/27/20.
//  Copyright © 2020 Applicaster. All rights reserved.
//

import UIKit
import JWPlayerPlugin
import ApplicasterSDK
import ZappPlugins

class ViewController: UIViewController {
    
    private let playButton = UIButton()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        playButton.setTitle("Play", for: .normal)
        playButton.backgroundColor = .black
        playButton.frame = CGRect(x: 150, y: 150, width: 100, height: 100)
        playButton.addTarget(self, action: #selector(playButtonPressed), for: .touchDown)
        
        view.addSubview(playButton)
    }
    
    @objc private func playButtonPressed() {
        let video = createPlayableItem()
        let configuration = ["playerKey": "N6yue4xdCfG31DL/TW+XY3X8kyI/3Ly0RP1cyeA2H0VcY4UX",
                             "chromecast": "1"]
        let player = ZappJWPlayerAdapter.pluggablePlayerInit(withPlayableItems: [video],
                                                             configurationJSON: configuration)
        player.presentPlayerFullScreen(self, configuration: nil)
//        let containerView = UIView()
//        containerView.frame = CGRect(x: 300, y: 300, width: 200, height: 200)
//        view.addSubview(containerView)
//        player.pluggablePlayerAddInline(self, container: containerView)
    }
    
    private func createPlayableItem() -> ZPPlayable {
        let item = Playable()
        //        item.videoURL = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
        //        item.videoURL = "http://besttv61.aoslive.it.best-tv.com/reshet/applicaster/index.m3u8"
        item.videoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        item.name = "Test Video"
        item.free = false
        item.identifier = "123235245"
        item.extensionsDictionary = ["duration" : 12345]
        item.live = false
        let extensionsDictionary: NSDictionary = ["free": "true"]
        item.extensionsDictionary = extensionsDictionary
        
        return item
    }
    
}

