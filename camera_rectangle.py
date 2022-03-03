import cv2
import numpy as np
import requests 

width = 3
height = 5
#cap = cv2.VideoCapture(0)
url = r'http://roborio-6817-frc.local:1181/?action=stream'
while True:
    
    resp = requests.get(url, stream=True).raw
    image = np.asarray(bytearray(resp.read()), dtype="uint8")
    frame = cv2.imdecode(image, cv2.IMREAD_COLOR)
    #ret, frame = cap.read()
    # Convert BGR to HSV
    # hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    # define range of red color in HSV
    lower_red = np.array([80, 120, 80])
    upper_red = np.array([155, 255, 155])

    frame = cv2.boxFilter(frame, -1, (10,10))
    frame = cv2.GaussianBlur(frame, (11,11), 11, 11)

    mask = cv2.inRange (frame, lower_red, upper_red)
    contours = cv2.findContours(mask.copy(),
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
    cv2.imshow('mask', mask)

    if cv2.waitKey(1) == 27: #if exit, break loop
        break

cap.release()
cv2.destroyAllWindows()