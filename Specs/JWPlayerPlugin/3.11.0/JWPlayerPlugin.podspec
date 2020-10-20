Pod::Spec.new do |s|

  s.name             = "JWPlayerPlugin"
  s.version          = '3.11.0'
  s.summary          = 'JWPlayer Player plugin implementation.'
  s.description      = 'An implementation for JWPlayer as a Zapp Player Plugin in Objective C.'
  s.homepage         = "https://github.com/applicaster/zapp-player-plugin-jw"
  s.license          = 'MIT'
  s.author           = { "Jesus De Meyer" => "j.demeyer@applicaster.com" }
  s.source           = { :git => "git@github.com:applicaster/zapp-player-plugin-jw.git", :tag => 'ios-' + s.version.to_s }

  s.ios.deployment_target   = "10.0"
  s.platform                = :ios, '10.0'
  s.requires_arc            = true

  s.source_files = 'iOS/JWPlayerPlugin/**/*.{swift,h,m}'
  s.public_header_files = 'iOS/JWPlayerPlugin/**/*.h'
  s.resources = 'iOS/JWPlayerPlugin/**/*.{storyboard,png}'

  s.xcconfig =  { 'CLANG_ALLOW_NON_MODULAR_INCLUDES_IN_FRAMEWORK_MODULES' => 'YES',
                  'ENABLE_BITCODE' => 'YES',
                  #  Workaround until this will be released https://github.com/CocoaPods/CocoaPods/pull/9045
                  'OTHER_LDFLAGS' => '$(inherited) -framework "JWPlayer_iOS_SDK"',
                  'SWIFT_VERSION' => '5.1'
               }

  s.dependency 'ZappCore'
  s.dependency 'ApplicasterSDK'
  s.dependency 'JWPlayer-SDK', '~> 3.14.0'
  s.dependency 'GoogleAds-IMA-iOS-SDK', '= 3.12.1'
  s.dependency 'google-cast-sdk-no-bluetooth'
end
