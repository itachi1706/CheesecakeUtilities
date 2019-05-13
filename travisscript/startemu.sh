#!/bin/bash

if [ $ESPRESSO -eq 1 ]
then
    echo no | android create avd --force -n test -t $ANDROID_TARGET --abi $ANDROID_ABI --tag $ANDROID_TAG
    emulator -avd test -no-window &
    android-wait-for-emulator
fi
