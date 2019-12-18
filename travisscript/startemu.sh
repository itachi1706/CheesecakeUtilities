#!/bin/bash

if [ $ESPRESSO -eq 1 ]
then
    echo no | avdmanager create avd --force -n test -k "system-images;android-$API;$EMU_FLAVOR;$ABI" -c 10M
    EMU_PARAMS="-verbose -no-snapshot -no-window -camera-back none -camera-front none -selinux permissive -qemu -m 2048"
    EMU_COMMAND="emulator"
    sudo -E sudo -u $USER -E bash -c "${ANDROID_HOME}/emulator/${EMU_COMMAND} -avd test ${AUDIO} ${EMU_PARAMS} &"

    ./travisscript/android-wait-for-emulator
    adb shell input keyevent 82 &

    # Old Code
    #echo no | android create avd --force -n test -t $ANDROID_TARGET --abi $ANDROID_ABI --tag $ANDROID_TAG
    #emulator -avd test -no-window &
    #android-wait-for-emulator
    #adb shell input keyevent 82 &
fi
