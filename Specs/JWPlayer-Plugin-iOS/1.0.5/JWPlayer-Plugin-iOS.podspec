Pod::Spec.new do |s|

  s.name             = "JWPlayer-Plugin-iOS"
  s.version          = '1.0.5'
  s.summary          = 'An EXample of video player framework for Zapp iOS.'
  s.description      = 'An EXample of video player framework for Zapp iOS.'
  s.homepage         = "https://github.com/applicaster/JWPlayer-Plugin-iOS"
  s.license          = 'MIT'
  s.author           = { "Udi Lumnitz" => "u.lumnitz@applicaster.com" }
  s.source           = { :git => "git@github.com:applicaster/JWPlayer-Plugin-iOS.git", :tag => s.version.to_s }

  s.ios.deployment_target   = "9.0"
  s.platform                = :ios, '9.0'
  s.requires_arc            = true
  s.swift_version           = '4.1'

  s.source_files = 'Classes/**/*.{swift}'

  s.xcconfig =  { 'CLANG_ALLOW_NON_MODULAR_INCLUDES_IN_FRAMEWORK_MODULES' => 'YES',
                  'ENABLE_BITCODE' => 'YES',
                  'OTHER_LDFLAGS' => '$(inherited)',
                  'FRAMEWORK_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}"/**',
                  'LIBRARY_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}"/**',
                  'SWIFT_VERSION' => '4.1'
                }

  s.dependency 'ZappPlugins'
  s.dependency 'ApplicasterSDK'
  s.dependency 'JWPlayerSDKWrapper', '~> 0.1.4'

end
