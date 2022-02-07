import cv2
import numpy as np

clickone = False

frame = None

# Using above first method to create a
# 2D array
rows, cols = (2, 2)
point = [[0]*cols]*rows

def mouseRGB(event,x,y,flags,param):
    global clickone
    global frame
    if event == cv2.EVENT_LBUTTONDOWN: #checks mouse left button down condition
        if not clickone:
            #collect first point
            point[0] = [x,y]
            clickone = True
        else:
            #collect second point
            point[1] = [x,y]
            clickone = False

            width = point[1][0] - point[0][0]
            height = point[1][1] - point[0][1]

          
            g = frame[y :y + height, x :x + width, 1:2]
            #colorsB = frame[y,x,0] #grabs coordinate of Blue
            #colorsG = frame[y,x,1] #grabs coordinate of Green
            # colorsR = frame[y,x,2] #grabs coordinate of Red
            #colors = frame[y,x] #grabs coordinate of mouse click   
        
           # g = frame[0:7, :, 1:2]
            g_mean = np.mean(g)
            g_min = np.amin(g)
            g_max = np.amax(g)

            #print("Red: ",colorsR) #telemetry/feedback for value gathered at mouse coordinate lines 17-21
            # print("Green: ",colorsG)
            # print("Blue: ",colorsB)
            # print("BRG Format: ",colors)
            print("Coordinates of pixel: X: ",x,"Y: ",y)
            print("Green Min Value: ", g_min)
            print("Green Average Value: ", g_mean)
            print("Green Max Value: ", g_max)
            #wait for a key to be pressed to do anything
            cv2.waitKey()
    if event == cv2.EVENT_MOUSEMOVE and clickone:
        #draw rectangle using point one and the continously updating point 2
        pass

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