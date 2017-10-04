#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
PyFingerprint
Copyright (C) 2015 Bastian Raschke <bastian.raschke@posteo.de>
All rights reserved.

"""
import hashlib
import time
from pyfingerprint.pyfingerprint import PyFingerprint


## Enrolls new finger
##

## Tries to initialize the sensor
try:
    f = PyFingerprint('/dev/ttyUSB0', 57600, 0xFFFFFFFF, 0x00000000)

    if ( f.verifyPassword() == False ):
        raise ValueError('El password d-access al sensor es incorrecte!!')

except Exception as e:
    print('El sensor no ha pogut ser inicialitzat!')
    print('Missatge error: ' + str(e))
    exit(1)

## Gets some sensor information
## print('Currently used templates: ' + str(f.getTemplateCount()) +'/'+ str(f.getStorageCapacity()))

## Tries to enroll new finger
try:
    print('Waiting Finger')

    ## Wait that finger is read
    while ( f.readImage() == False ):
        pass

    ## Converts read image to characteristics and stores it in charbuffer 1
    f.convertImage(0x01)

    ## Checks if finger is already enrolled
    result = f.searchTemplate()
    positionNumber = result[0]

    if ( positionNumber >= 0 ):
        raise Exception('Emprempta ja existent!')

    print('Remove Finger')
    time.sleep(2)

    print('Put Finger again')

    ## Wait that finger is read again
    while ( f.readImage() == False ):
        pass

    ## Converts read image to characteristics and stores it in charbuffer 2
    f.convertImage(0x02)

    ## Compares the charbuffers
    if ( f.compareCharacteristics() == 0 ):
        raise Exception('Les dues empremtes no son coincidents.')

    ## Creates a template
    f.createTemplate()

    ## Saves template at new position number
    positionNumber = f.storeTemplate()
    ## print('Finger enrolled successfully!')
    ## print('New template position #' + str(positionNumber))
    
    ## OPTIONAL stuff
    ##

    ## Loads the found template to charbuffer 1
    f.loadTemplate(positionNumber, 0x01)

    ## Downloads the characteristics of template loaded in charbuffer 1
    characterics = str(f.downloadCharacteristics(0x01))

	

    ## Hashes characteristics of template
    ## print('SHA-2 hash of template: ' + hashlib.sha256(characterics).hexdigest())
    print('Characterics: ' + characterics)
    print('Hash: ' + hashlib.sha256(characterics).hexdigest())
    print('End enrollment')
    if ( f.deleteTemplate(positionNumber) == True ):
        print('Template deleted')

except Exception as e:
    print('El proces ha fallat')
    print('Missatge error: ' + str(e))
    exit(1)
