#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
PyFingerprint
Copyright (C) 2015 Bastian Raschke <bastian.raschke@posteo.de>
All rights reserved.

"""
import sys
import hashlib
from pyfingerprint.pyfingerprint import PyFingerprint


## Search for a finger
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
print('Currently used templates: ' + str(f.getTemplateCount()) +'/'+ str(f.getStorageCapacity()))

## Tries to search the finger and calculate hash
try:
    ##print('Waiting for finger...')

    ## Wait that finger is read
    ##while ( f.readImage() == False ):
    ##    pass

    ## Converts read image to characteristics and stores it in charbuffer 1
    ##f.convertImage(0x01)

    ## Searchs template
    ##result = f.searchTemplate()

    ##positionNumber = result[0]
    ##accuracyScore = result[1]

    ##if ( positionNumber == -1 ):
    ##    print('No match found!')
    ##    exit(0)
    ##else:
    ##    print('Found template at position #' + str(positionNumber))
    ##    print('The accuracy score is: ' + str(accuracyScore))

    ## OPTIONAL stuff
    ##

    ## Loads the found template to charbuffer 1
    ## f.loadTemplate(positionNumber, 0x01)

    ## Downloads the characteristics of template loaded in charbuffer 1
    ## characterics = str(f.downloadCharacteristics(0x01)).encode('utf-8')
	
    #characterics = '[3, 1, 90, 11, 163, 0, 128, 14, 128, 14, 128, 6, 0, 6, 0, 6, 0, 2, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 128, 6, 192, 14, 224, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 54, 4, 13, 190, 82, 155, 230, 30, 49, 36, 170, 158, 95, 37, 103, 158, 37, 40, 215, 254, 50, 180, 86, 190, 63, 33, 84, 31, 52, 59, 235, 255, 73, 188, 21, 63, 46, 137, 227, 186, 37, 9, 161, 123, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 88, 18, 161, 0, 128, 6, 128, 6, 0, 2, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 0, 128, 0, 128, 0, 128, 2, 192, 2, 192, 2, 224, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 144, 204, 254, 12, 17, 33, 62, 31, 145, 163, 62, 22, 29, 157, 126, 24, 164, 27, 254, 107, 40, 229, 190, 73, 49, 170, 222, 58, 181, 216, 94, 75, 193, 22, 158, 56, 134, 101, 63, 26, 170, 154, 255, 88, 174, 84, 31, 101, 142, 142, 124, 36, 60, 153, 28, 36, 191, 193, 92, 95, 14, 165, 29, 69, 151, 35, 144, 65, 28, 36, 79, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]'
    num = f.getTemplateCount()
    characterics = sys.argv[1]
    if(f.uploadCharacteristics(0x01, eval(characterics)) == True):
        print('Template uploaded')
    else:
        raise Exception('Error during upload')

    if(f.storeTemplate(num) == True):
        print('Template stored')
    else:
        raise Exception('Error during template storing')
	

    ## Hashes characteristics of template
    ## print('SHA-2 hash of template: ' + hashlib.sha256(characterics).hexdigest())

except Exception as e:
    print('Operation failed!')
    print('Exception message: ' + str(e))
    exit(1)
