# This should be the Android SDK root directory
ANDROID ?= ../../../android-sdk-linux_86/

# SDK version
ANDROID_VERSION=1.5

TARGET = Postcode
KEYSTORE=postcode.keystore
PACKAGE=net.tevp.postcode
R_PATH = src/net/tevp/postcode/R.java
SOURCE_FILES=$(wildcard src/net/tevp/*/*.java)

include Makefile.common

test: $(BIN_FILES)
	java -cp bin:json.jar:$(PLATFORM_PATH)/android.jar net.tevp.postcode.PostcodeBackend 42.569261 -71.268311

