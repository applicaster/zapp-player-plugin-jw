platform :ios, '9.0'
use_frameworks!
install! 'cocoapods', :deterministic_uuids => false

source 'git@github.com:applicaster/CocoaPods.git'
source 'git@github.com:applicaster/PluginsBuilderCocoaPods.git'
source 'git@github.com:CocoaPods/Specs.git'

def shared_pods
pod 'ZappPlugins'
pod 'ApplicasterSDK'

end

target 'JWPlayer-Plugin-iOS' do
    shared_pods
    pod 'JWPlayer-SDK'
end
