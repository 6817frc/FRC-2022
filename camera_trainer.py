import cv2
import numpy as np


def mouseRGB(event,x,y,flags,param):
    if event == cv2.EVENT_LBUTTONDOWN: #checks mouse left button down condition
        colorsB = frame[y,x,0] #grabs coordinate of Blue
        colorsG = frame[y,x,1] #grabs coordinate of Green
        colorsR = frame[y,x,2] #grabs coordinate of Red
        colors = frame[y,x] #grabs coordinate of mouse click
        print("Red: ",colorsR) #telemetry/feedback for value gathered at mouse coordinate lines 11-15
        print("Green: ",colorsG)
        print("Blue: ",colorsB)
        print("BRG Format: ",colors)
        print("Coordinates of pixel: X: ",x,"Y: ",y)


cv2.namedWindow('mouseRGB')
cv2.setMouseCallback('mouseRGB',mouseRGB)

capture = cv2.VideoCapture(0)

while(True):

    ret, frame = capture.read()

    cv2.imshow('mouseRGB', frame) #displays video

    if cv2.waitKey(1) == 27: #if exit, break loop
        break

capture.release()
cv2.destroyAllWindows()