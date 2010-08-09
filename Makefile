# This should be the Android SDK root directory
ANDROID ?= ../../../android-sdk-linux_86/

# SDK version
ANDROID_VERSION=1.5

TARGET = Postcode
KEYSTORE=.debug.keystore
PACKAGE=net.tevp.postcode
R_PATH = src/net/tevp/postcode/R.java
SOURCE_FILES=$(wildcard src/net/tevp/*/*.java)

include Makefile.common
