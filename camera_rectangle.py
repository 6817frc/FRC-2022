from tkinter import Frame
import cv2
import numpy as np
import requests

width = 3
height = 5
__resize_image_width = 640
__resize_image_height = 480
__resize_image_interpolation = cv2.INTER_CUBIC
red = [0.0, 235.68181818181816]
green = [231.6097122302158, 255.0]
blue = [57.32913669064748, 255.0]
#cap = cv2.VideoCapture(0)
url = r'http://roborio-6817-frc.local:1181/stream.mjpg'
while True:
    
    resp = requests.get(url, stream=True).raw
    image = np.asarray(bytearray(resp.read()), dtype="uint8")
    frame = cv2.imdecode(image, cv2.IMREAD_COLOR)
    # #ret, frame = cap.read()
    # # Convert BGR to HSV
    # # hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    # # define range of red color in HSV
    # lower_red = np.array([80, 120, 80])
    # upper_red = np.array([155, 255, 155])

    # frame = cv2.boxFilter(frame, -1, (10,10))
    # frame = cv2.GaussianBlur(frame, (11,11), 11, 11)

    # mask = cv2.inRange (frame, lower_red, upper_red)
    resize = cv2.resize(frame, ((int)(__resize_image_width), (int)(__resize_image_height)), 0, 0, __resize_image_interpolation)
    blur = cv2.bilateralFilter(resize, -1, round(0.0), round(0.0))
    out = cv2.cvtColor(input, cv2.COLOR_BGR2RGB)
    final = cv2.inRange(out, (red[0], green[0], blue[0]),  (red[1], green[1], blue[1]))
    contours = cv2.findContours(final.copy(),
                            cv2.RETR_TREE,
                            cv2.CHAIN_APPROX_SIMPLE) [-2]
   
    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)
       
        if w % width < 3 and h % height < 5 and w * h > 50:
            cv2.rectangle(frame,(x, y),(x+w, y+h),(0, 255, 0), 2)

    if len(contours) > 0:
        red_area = max(contours, key=cv2.contourArea)
        x, y, w, h = cv2.boundingRect(red_area)
        cv2.rectangle(frame,(x, y),(x+w, y+h),(0, 0, 255), 2)


    cv2.imshow('frame', frame)
    cv2.imshow('mask', final)

    if cv2.waitKey(1) == 27: #if exit, break loop
        break


cv2.destroyAllWindows()