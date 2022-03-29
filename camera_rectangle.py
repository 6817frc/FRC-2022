#!/usr/bin/env python3

# Copyright (c) FIRST and other WPILib contributors.
# Open Source Software; you can modify and/or share it under the terms of
# the WPILib BSD license file in the root directory of this project.

import json
import time
import sys
import cv2
import numpy as np

from cscore import CameraServer, VideoSource, UsbCamera, MjpegServer
from networktables import NetworkTables, NetworkTablesInstance

#   JSON format:
#   {
#       "team": <team number>,
#       "ntmode": <"client" or "server", "client" if unspecified>
#       "cameras": [
#           {
#               "name": <camera name>
#               "path": <path, e.g. "/dev/video0">
#               "pixel format": <"MJPEG", "YUYV", etc>   // optional
#               "width": <video mode width>              // optional
#               "height": <video mode height>            // optional
#               "fps": <video mode fps>                  // optional
#               "brightness": <percentage brightness>    // optional
#               "white balance": <"auto", "hold", value> // optional
#               "exposure": <"auto", "hold", value>      // optional
#               "properties": [                          // optional
#                   {
#                       "name": <property name>
#                       "value": <property value>
#                   }
#               ],
#               "stream": {                              // optional
#                   "properties": [
#                       {
#                           "name": <stream property name>
#                           "value": <stream property value>
#                       }
#                   ]
#               }
#           }
#       ]
#       "switched cameras": [
#           {
#               "name": <virtual camera name>
#               "key": <network table key used for selection>
#               // if NT value is a string, it's treated as a name
#               // if NT value is a double, it's treated as an integer index
#           }
#       ]
#   }

configFile = "/boot/frc.json"

class CameraConfig: pass

width = 3
height = 5
center=(0,0)
__resize_image_width = 640
__resize_image_height = 480
__resize_image_interpolation = cv2.INTER_CUBIC
red = [0.0, 235.68181818181816]
green = [231.6097122302158, 255.0]
blue = [57.32913669064748, 255.0]

inst = None

team = None
server = False
_server=None
frame = None
cameraConfigs = []
switchedCameraConfigs = []
cameras = []
def initialize():
    NetworkTables.initialize(server="roboRIO-6817-frc.local")
    table = NetworkTables.getTable('SmartDashboard')
    table.putBoolean("initialize", True)
    return table
def parseError(str):
    """Report parse error."""
    print("config error in '" + configFile + "': " + str, file=sys.stderr)

def readCameraConfig(config):
    """Read single camera configuration."""
    cam = CameraConfig()

    # name
    try:
        cam.name = config["name"]
    except KeyError:
        parseError("could not read camera name")
        return False

    # path
    try:
        cam.path = config["path"]
    except KeyError:
        parseError("camera '{}': could not read path".format(cam.name))
        return False

    # stream properties
    cam.streamConfig = config.get("stream")

    cam.config = config

    cameraConfigs.append(cam)
    return True

def readSwitchedCameraConfig(config):
    """Read single switched camera configuration."""
    cam = CameraConfig()

    # name
    try:
        cam.name = config["name"]
    except KeyError:
        parseError("could not read switched camera name")
        return False

    # path
    try:
        cam.key = config["key"]
    except KeyError:
        parseError("switched camera '{}': could not read key".format(cam.name))
        return False

    switchedCameraConfigs.append(cam)
    return True

def readConfig():
    """Read configuration file."""
    global team
    global server

    # parse file
    try:
        with open(configFile, "rt", encoding="utf-8") as f:
            j = json.load(f)
    except OSError as err:
        print("could not open '{}': {}".format(configFile, err), file=sys.stderr)
        return False

    # top level must be an object
    if not isinstance(j, dict):
        parseError("must be JSON object")
        return False

    # team number
    try:
        team = j["team"]
    except KeyError:
        parseError("could not read team number")
        return False

    # ntmode (optional)
    if "ntmode" in j:
        str = j["ntmode"]
        if str.lower() == "client":
            server = False
        elif str.lower() == "server":
            server = True
        else:
            parseError("could not understand ntmode value '{}'".format(str))

    # cameras
    try:
        cameras = j["cameras"]
    except KeyError:
        parseError("could not read cameras")
        return False
    for camera in cameras:
        if not readCameraConfig(camera):
            return False

    # switched cameras
    if "switched cameras" in j:
        for camera in j["switched cameras"]:
            if not readSwitchedCameraConfig(camera):
                return False

    return True

def startCamera(config):
    global _server, inst
    """Start running the camera."""
    print("Starting camera '{}' on {}".format(config.name, config.path))
    inst = CameraServer.getInstance()
    camera = UsbCamera(config.name, config.path)
    _server = inst.startAutomaticCapture(camera=camera, return_server=True)

    camera.setConfigJson(json.dumps(config.config))
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen)

    if config.streamConfig is not None:
        _server.setConfigJson(json.dumps(config.streamConfig))

    return camera

def startSwitchedCamera(config):
    """Start running the switched camera."""
    print("Starting switched camera '{}' on {}".format(config.name, config.key))
    server = CameraServer.getInstance().addSwitchedCamera(config.name)

    def listener(fromobj, key, value, isNew):
        if isinstance(value, float):
            i = int(value)
            if i >= 0 and i < len(cameras):
              server.setSource(cameras[i])
        elif isinstance(value, str):
            for i in range(len(cameraConfigs)):
                if value == cameraConfigs[i].name:
                    server.setSource(cameras[i])
                    break

    # NetworkTablesInstance.getDefault().getEntry(config.key).addListener(
    #     listener,
    #     NetworkTablesInstance.NotifyFlags.IMMEDIATE |
    #     NetworkTablesInstance.NotifyFlags.NEW |
    #     NetworkTablesInstance.NotifyFlags.UPDATE)

    return server

def filter(frame):
        __resize_image_width = 640
        __resize_image_height = 480
        __resize_image_interpolation = cv2.INTER_CUBIC
        red = [0.0, 235.68181818181816]
        green = [231.6097122302158, 255.0]
        blue = [57.32913669064748, 255.0]
        resize = cv2.resize(frame, ((int)(__resize_image_width), (int)(__resize_image_height)), interpolation= __resize_image_interpolation)
        # blur = cv2.bilateralFilter(resize, -1, round(0.0), round(0.0))
        out = cv2.cvtColor(resize, cv2.COLOR_BGR2RGB)
        final = cv2.inRange(out, (red[0], green[0], blue[0]),  (red[1], green[1], blue[1]))
        return final
def findContours(frame):
        global center
        contours = cv2.findContours(frame.copy(),
                                cv2.RETR_TREE,
                                cv2.CHAIN_APPROX_SIMPLE) [-2]
    
        """  for contour in contours:
            x, y, w, h = cv2.boundingRect(contour)
        
            if w % width < 3 and h % height < 5 and w * h > 50:
                cv2.rectangle(frame,(x, y),(x+w, y+h),(0, 255, 0), 2) """
        
        if len(contours) > 0:
            red_area = max(contours, key=cv2.contourArea)
            x, y, w, h = cv2.boundingRect(red_area)
            cv2.rectangle(frame,(x, y),(x+w, y+h),(0, 0, 255), 6)
            center = (x+w, y+h)

        return frame
def sendCenter(center, table):
    table.putNumber("DB/Slider 0", center[0])
    table.putNumber("DB/Slider 1", center[1])
if __name__ == "__main__":
    if len(sys.argv) >= 2:
        configFile = sys.argv[1]

    # read configuration
    if not readConfig():
        sys.exit(1)

    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    if server:
        print("Setting up NetworkTables server")
        ntinst.startServer()
    else:
        print("Setting up NetworkTables client for team {}".format(team))
        ntinst.startClientTeam(team)
        ntinst.startDSClient()

    # start cameras
    for config in cameraConfigs:
        cameras.append(startCamera(config))

    # start switched cameras
    for config in switchedCameraConfigs:
        startSwitchedCamera(config)

    # loop forever
     # (optional) Setup a CvSource. This will send images back to the Dashboard
    outputStream = inst.putVideo("Vision Processing", 320, 240)
    table = initialize()
    # Allocating new images is very expensive, always try to preallocate
    frame = np.zeros(shape=(240, 320, 3), dtype=np.uint8)
    while True:
        cvsink= inst.getVideo()
        _, frame=cvsink.grabFrame(frame)
        frame = filter(np.array(frame))
        frame = findContours(frame)
        sendCenter(center, table)
        outputStream.putFrame(frame)
        time.sleep(0.01)