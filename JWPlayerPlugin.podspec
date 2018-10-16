Pod::Spec.new do |s|

  s.name             = "JWPlayerPlugin"
  s.version          = '2.0.0'
  s.summary          = 'JWPlayer Player plugin implementation.'
  s.description      = 'An implementation for JWPlayer as a Zapp Player Plugin in Objective C.'
  s.homepage         = "https://github.com/applicaster/JWPlayer-Plugin-iOS"
  s.license          = 'MIT'
  s.author           = { "Liviu Romascanu" => "l.romasca@applicaster.com" }
  s.source           = { :git => "git@github.com:applicaster/JWPlayer-Plugin-iOS.git", :tag => s.version.to_s }

  s.ios.deployment_target   = "9.0"
  s.platform                = :ios, '9.0'
  s.requires_arc            = true
  s.swift_version           = '4.1'

  s.source_files = 'JWPlayerPlugin/**/*.{h,m}'

  s.xcconfig =  { 'CLANG_ALLOW_NON_MODULAR_INCLUDES_IN_FRAMEWORK_MODULES' => 'YES',
                  'ENABLE_BITCODE' => 'YES',
                  'OTHER_LDFLAGS' => '$(inherited)',
                  'FRAMEWORK_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}"/**',
                  'LIBRARY_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}"/**',
                  'HEADER_SEARCH_PATHS' => '$(inherited) "${PODS_ROOT}"/**',
                  'SWIFT_VERSION' => '4.1'
                }

  s.dependency 'ZappPlugins'
  s.dependency 'ApplicasterSDK'
  s.dependency 'JWPlayer-SDK', '~> 3.0'

end
