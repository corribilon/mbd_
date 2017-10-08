#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
PyFingerprint
Copyright (C) 2015 Bastian Raschke <bastian.raschke@posteo.de>
All rights reserved.

"""

from pyfingerprint.pyfingerprint import PyFingerprint


## Deletes a finger from sensor
##


## Tries to initialize the sensor
try:
    f = PyFingerprint('/dev/ttyUSB0', 57600, 0xFFFFFFFF, 0x00000000)

    if ( f.verifyPassword() == False ):
        raise ValueError('The given fingerprint sensor password is wrong!')

except Exception as e:
    print('The fingerprint sensor could not be initialized!')
    print('Exception message: ' + str(e))
    exit(1)

## Gets some sensor information
numTemps = str(f.getTemplateCount())
print('Currently used templates: ' + numTemps +'/'+ str(f.getStorageCapacity()))

## Tries to delete the template of the finger
try:
    #positionNumber = input('Please enter the template position you want to delete: ')
    #positionNumber = int(positionNumber)
    numT=f.getTemplateCount()
    for x in range(0,numT):
        f.deleteTemplate(x)
    numTemps = str(f.getTemplateCount())
    print('Currently used templates: ' + numTemps +'/'+ str(f.getStorageCapacity()))

except Exception as e:
    print('Operation failed!')
    print('Exception message: ' + str(e))
    exit(1)
