platform :ios, '9.0'
use_frameworks!
install! 'cocoapods', :deterministic_uuids => false

source 'git@github.com:applicaster/JWPlayerSDKWrapper-iOS.git'
source 'git@github.com:applicaster/CocoaPods.git'
source 'git@github.com:applicaster/PluginsBuilderCocoaPods.git'
source 'git@github.com:CocoaPods/Specs.git'

def shared_pods
pod 'ZappPlugins'
pod 'ApplicasterSDK'

end

target 'JWPlayer-Plugin-iOS' do
    #shared_pods
    #pod 'JWPlayer-SDK'
    pod 'JWPlayer-Plugin-iOS', :path => 'JWPlayer-Plugin-iOS.podspec'
end

pre_install do |installer|
    # workaround for https://github.com/CocoaPods/CocoaPods/issues/3289
    Pod::Installer::Xcode::TargetValidator.send(:define_method, :verify_no_static_framework_transitive_dependencies) {}
end
