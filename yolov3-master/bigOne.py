import argparse
import subprocess
import numpy as np
import os
import io

import requests
from rdflib import Graph
import urllib.request

import glob
from PIL import Image
import numpy as np
import pandas as pd
import time
import math
import os
import shutil
from pandas import DataFrame, read_excel, merge
from pandas import DataFrame


t1=time.time()

endpoint = "https://commons.wikimedia.org/w/api.php"
PARAMS = {
    'action': 'query',
    'list': 'search',
    'srlimit': '10',
    'srnamespace': '6',
    'format': 'json',
    'continue': '-||'
}
if __name__=='__main__':
# if _name_ == '__main__':
    # ?action=query&list=search&srsearch=haswbstatement:P180=Q7378&srnamespace=6&format=json
    inputQid = 'Q7378'  # Qinput("enter Qid:")
    print(inputQid)
    # defining a params dict for the parameters to be sent to the API
    url_list = []
    filename=[]
    i = 10
    while i <= 3000:
        i += 10
        PARAMS['srsearch'] = 'haswbstatement:P180=' + inputQid
        PARAMS['sroffset'] = i
        # sending get request and saving the response as response object
        r = requests.get(url=endpoint, params=PARAMS)
        if r.ok:
            for image in r.json()['query']['search']:
                URL = 'https://commons.wikimedia.org/wiki/Special:EntityData/M' + str(image['pageid']) + '.ttl'
                r = requests.get(url=URL)
                g = Graph()
                g.parse(io.StringIO(r.content.decode("utf-8")), format="ttl")
                for row in g.query("SELECT DISTINCT ?o1 WHERE { ?s1 <http://schema.org/contentUrl> ?o1. }"):
                    # print(row[0])
                    url_list.append(row[0].__str__())
    print(len(url_list))
    os.chdir(r"F:\yolov3-master\test")
    for url in url_list:
        print(url)
        r = requests.get(url)
        if r.ok:
            try:
                with open("/".join([url.split("/")[-1]]), "wb") as f:
                
                    f.write(r.content)
                    filename.append(url.split("/")[-1])
            except FileNotFoundError as oserr:
                pass
downloadingThePhotos2=time.time()

af=pd.DataFrame(data={"URLs":url_list,"Image name":filename})
af.to_csv("F:\\yolov3-master\\excels\\urlsFilenames.csv",sep=',',index=False)

doingYolo1=time.time()

os.chdir(r"F:\yolov3-master")
# subprocess.run('dir', shell=True)
# subprocess.run('python detect.py --cfg cfg\yolov3.cfg --weights weights\yolov3.pt --source test --save-txt', shell=True)
subprocess.run('python detect.py --cfg cfg\yolov3.cfg --weights weights\yolov3.pt --source test --save-txt --conf-thres 0.8', shell=True)
doingYolo2=time.time()

imageDimentionList=[]
ImageFilenameList=[]

images = glob.glob("output/*.*")
imagess = images.copy()
for i in imagess:
    if i.endswith(".txt") or i.endswith(".svg"):
        print(i)
        images.remove(i)
print("ok")
for image in images:
    with open(image, 'rb') as file:
        img = Image.open(file)
        imageSize=img.size
        imageDimentionList.append(imageSize)
        image=image[7:]
        ImageFilenameList.append(image)


lengthDimention = [item[0] for item in imageDimentionList]
breadthDimention=[item[1] for item in imageDimentionList]
wikiDimention = {'dimention': imageDimentionList,'Image name': ImageFilenameList, 'Y-Image Dimentions':lengthDimention, 'X-Image Dimentions':breadthDimention}

df = pd.DataFrame(wikiDimention , columns= ['dimention', 'Image name', 'Y-Image Dimentions', 'X-Image Dimentions'])

print (df)
df.to_csv('F:\\yolov3-master\\excels\\output.csv', encoding='utf-8', index=False)
print(ImageFilenameList,imageDimentionList)

dir_path = 'F:\\yolov3-master\\output'
os.chdir(dir_path)
src_files = os.listdir(dir_path)
for file_name in src_files:
    if file_name.endswith(".txt"):
        full_file_name = os.path.join(dir_path, file_name)
        if os.path.isfile(full_file_name):
            shutil.copy(full_file_name, 'F:\\yolov3-master\\texts')

dir_path = 'F:\\yolov3-master\\texts'  # Put the path to the texts directory here
os.chdir(dir_path)
file_name = []
for f in os.listdir():
    f_name,f_ext=os.path.splitext(f)
    file_name.append(f_name)

file_content = {
    'X1': [],
    'Y1': [],
    'X2': [],
    'Y2': [],
    'object name': [],
    'Image name': []
}


for i in os.listdir():
    f_name,f_ext=os.path.splitext(i)
    file1 = open(i, "r+")
    txt = file1.readlines()

    for line in txt:
        l = line.split(' ')
        if l[-1] == '\n':
            l = l[:-1]
        file_content['X1'].append(l[0])
        file_content['Y1'].append(l[1])
        file_content['X2'].append(l[2])
        file_content['Y2'].append(l[3])
        file_content['object name'].append(l[4])
        a=np.array(file_content['X1'], dtype=np.float)
        b=np.array(file_content['X2'], dtype=np.float)
        c=np.array(file_content['Y1'], dtype=np.float)
        d=np.array(file_content['Y2'], dtype=np.float)
        file_content['X-centre']=((a+b)/2)
        file_content['Y-centre']=((c+d)/2)
        file_content['Image name'].append(f_name)

df = DataFrame(data=file_content)
print(df)
df.to_csv('F:\\yolov3-master\\excels\\imageNameBoundingBoxesObjectNameXYcenter.csv',index=False) 



df1 = pd.read_csv('F:\\yolov3-master\\excels\\imageNameBoundingBoxesObjectNameXYcenter.csv')

df2 = pd.read_csv('F:\\yolov3-master\\excels\\urlsFilenames.csv')

df3=df1.merge(df2, on='Image name', how='left')

df3.to_csv('F:\\yolov3-master\\excels\\imageNameBoundingBoxesObjectNameXYcenterURL.csv', encoding='utf-8', index=False)

df4 = pd.read_csv('F:\\yolov3-master\\excels\\imageNameBoundingBoxesObjectNameXYcenterURL.csv')

df5 = pd.read_csv('F:\\yolov3-master\\excels\\output.csv')

df6=df4.merge(df5, on='Image name', how='left')

df6.to_csv('F:\\yolov3-master\\excels\\imageNameBoundingBoxesObjectNameXYcenterURLImageDim.csv', encoding='utf-8', index=False)


dddf = pd.read_csv('F:\yolov3-master\excels\imageNameBoundingBoxesObjectNameXYcenterURLImageDim.csv')

# automationUrl2downloadPhoto2yolo2ioRel&ooRelexcel

hasinthecenter = []
def center_object(image , object, x_dim, y_dim, x_centre, y_centre):
    if x_centre>0.3*x_dim and x_centre<0.66*x_dim and y_centre>0.3*y_dim and y_centre<0.66*y_dim:
        hasinthecenter.append(object)
    else:
        hasinthecenter.append('na')


for _,row in dddf.iterrows():

    center_object(row['Image name'], row['object name'], row['X-Image Dimentions'], row['Y-Image Dimentions'], row['X-centre'], row['Y-centre'])
        

hasintheleft = []
def center_object(image , object, x_dim, y_dim, x_centre, y_centre):
    if x_centre<0.3*x_dim:
        hasintheleft.append(object)
    else:
        hasintheleft.append('na')


for _,row in dddf.iterrows():

    center_object(row['Image name'], row['object name'], row['X-Image Dimentions'], row['Y-Image Dimentions'], row['X-centre'], row['Y-centre'])

hasinthetop= []
def center_object(image , object, x_dim, y_dim, x_centre, y_centre):
    if y_centre<0.3*y_dim:
        hasinthetop.append(object)
    else:
        hasinthetop.append('na')


for _,row in dddf.iterrows():

    center_object(row['Image name'], row['object name'], row['X-Image Dimentions'], row['Y-Image Dimentions'], row['X-centre'], row['Y-centre'])

hasintheright = []
def center_object(image , object, x_dim, y_dim, x_centre, y_centre):
    if x_centre>0.67*x_dim:
        hasintheright.append(object)
    else:
        hasintheright.append('na')



for _,row in dddf.iterrows():

    center_object(row['Image name'], row['object name'], row['X-Image Dimentions'], row['Y-Image Dimentions'], row['X-centre'], row['Y-centre'])

hasinthebottom = []
def center_object(image , object, x_dim, y_dim, x_centre, y_centre):
    if y_centre>0.67*y_dim:
        hasinthebottom.append(object)
    else:
        hasinthebottom.append('na')



for _,row in dddf.iterrows():

    center_object(row['Image name'], row['object name'], row['X-Image Dimentions'], row['Y-Image Dimentions'], row['X-centre'], row['Y-centre'])

ddf=dddf.assign(hasintheleft=hasintheleft,hasintheright=hasintheright,hasinthetop=hasinthetop,hasinthebottom=hasinthebottom,hasinthecenter=hasinthecenter)
ddf.to_csv("F:\\yolov3-master\\excels\\imageHasOnLeftRightCenterTopBottom.csv",sep=',',index=False)

df=pd.read_csv('F:\\yolov3-master\\excels\\imageHasOnLeftRightCenterTopBottom.csv')
variable2 = df.groupby('Image name').agg({'object name': ', '.join}).reset_index()
df['count'] = 1

value=df.groupby(['Image name']).count()['count']

numberOfObjects=[]
for i in value:

    numberOfObjects.append(i)

ImageName=[]
objectName=[]
for _,row in variable2.iterrows():
    ImageName.append(row['Image name'])
    objectName.append(row['object name'])
    
aaff= pd.DataFrame(data={'Image name':ImageName,'object name':objectName,'no. of objects':numberOfObjects})
aaff.to_csv("F:\\yolov3-master\\excels\\imageNameObjectsNoOfObjects.csv",sep=',',index=False)

df7 = pd.read_csv('F:\\yolov3-master\\excels\\imageHasOnLeftRightCenterTopBottom.csv')

df8 = pd.read_csv('F:\\yolov3-master\\excels\\imageNameObjectsNoOfObjects.csv')

df9=df7.merge(df8, on='Image name', how='left')

df9.to_csv('F:\\yolov3-master\\excels\\imageHasOnLeftRightCenterTopBottomNOofObjects.csv', encoding='utf-8', index=False)



dff = pd.read_csv('F:\\yolov3-master\\excels\\imageHasOnLeftRightCenterTopBottomNOofObjects.csv')



diagonal_object_values=[]
X_Image_Dimentions_values=[]
Y_Image_Dimentions_values=[]
x1_values=[]
x2_values=[]
y1_values=[]
y2_values=[] 
x_values = [] 
object_name_list=[]
y_values=[]
Image_name_list=[]




for _,row in df.iterrows():

    x_values.append(row['X-centre'])
    object_name_list.append(row['object name'])
    Image_name_list.append(row['Image name'])
    y_values.append(row['Y-centre'])
#     diagonal_object_valuess.append(row['diagonalObject'])
    
    x1_values.append(row['X1'])
    x2_values.append(row['X2'])
    y1_values.append(row['Y1'])
    y2_values.append(row['Y2'])
    X_Image_Dimentions_values.append(row['X-Image Dimentions'])
    Y_Image_Dimentions_values.append(row['Y-Image Dimentions'])
   

import numpy as np
a=np.array(x1_values, dtype=np.float)
b=np.array(x2_values, dtype=np.float)
c=np.array(y1_values, dtype=np.float)
d=np.array(y2_values, dtype=np.float)

diagonal_object_values=(((a+b)**0.5)+((c+d)**0.5))
ddf=df.assign(diagonalObject=diagonal_object_values)



# 3
relations=["is close to","is far from","is greater than","is smaller than","overlaps","doesn't overlap","is in the right of",
           "is in the left of","is in the bottom of","is in the top of","in between"]
predicate = []
object_1 = []
object_2_far=[]
object_2_close=[]
object_2_is_greater_than=[]
object_2_is_smaller_than=[]
object_2_overlaps=[]
object_2_doesnt_overlap=[]
object_2_is_in_the_right_of=[]
object_2_is_in_the_left_of=[]
object_2_is_in_the_bottom_of=[]
object_2_is_in_the_top_of=[]


imageName = []
for i in range(len(y_values)):
    for j in range(i + 1, len(y_values)):
        if ((diagonal_object_values[i]+diagonal_object_values[j])/2)>(math.sqrt((x_values[i]-x_values[j])**2)+(y_values[i]-y_values[j])**2) and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i])
            imageName.append(Image_name_list[i])
            object_2_far.append(object_name_list[j])
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_far.append(object_name_list[i])
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")

        elif ((diagonal_object_values[i]+diagonal_object_values[j])/2)<(math.sqrt((x_values[i]-x_values[j])**2)+(y_values[i]-y_values[j])**2) and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i]) 
            imageName.append(Image_name_list[i])
            object_2_close.append(object_name_list[j])
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_close.append(object_name_list[i])
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")

            
# object_2_close.append("na")
# object_2_far.append("na")



# 4
for i in range(len(y_values)):
    for j in range(i + 1, len(y_values)):    
        if ((y1_values[i]-y2_values[i])*(x1_values[i]-x2_values[i]))>((y1_values[j]-y2_values[j])*(x1_values[j]-x2_values[j])) and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i])
            imageName.append(Image_name_list[i])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append(object_name_list[j])
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append(object_name_list[i])
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            
        elif ((y1_values[i]-y2_values[i])*(x1_values[i]-x2_values[i]))<((y1_values[j]-y2_values[j])*(x1_values[j]-x2_values[j])) and Image_name_list[i]==Image_name_list[j]:

            
            object_1.append(object_name_list[i]) 
            imageName.append(Image_name_list[i])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append(object_name_list[j])
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append(object_name_list[i])
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")



# 5
from shapely.geometry import Polygon
for i in range(len(y_values)):
    for j in range(i + 1, len(y_values)): 
        p1=Polygon([(x1_values[i],y1_values[i]),(x2_values[i],y2_values[i]),(x1_values[i],y2_values[i]),(x2_values[i],y1_values[i])])
        p2=Polygon([(x1_values[j],y1_values[j]),(x2_values[j],y2_values[j]),(x1_values[j],y2_values[j]),(x2_values[j],y1_values[j])])
        if p1.intersects(p2)==True and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i])
            imageName.append(Image_name_list[i])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append(object_name_list[j])
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append(object_name_list[i])     
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            
        elif p1.intersects(p2)==False and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i]) 
            imageName.append(Image_name_list[i])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append(object_name_list[j])
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append(object_name_list[i])        
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")                                            

            
           
            
            


for i in range(len(x_values)):
    for j in range(i + 1, len(x_values)):
        if x_values[i]>x_values[j] and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i])
            imageName.append(Image_name_list[i])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append(object_name_list[j])
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append(object_name_list[i])
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")


        elif x_values[i]<x_values[j] and Image_name_list[i]==Image_name_list[j]:
    
            object_1.append(object_name_list[i]) 
            imageName.append(Image_name_list[i])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append(object_name_list[j])
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append(object_name_list[i])
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append("na")                                              



# 7
for i in range(len(x_values)):
    for j in range(i + 1, len(x_values)):
        if y_values[i]>y_values[j] and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i])
            imageName.append(Image_name_list[i])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append(object_name_list[j])
            object_2_is_in_the_top_of.append("na")
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_far.append("na")
            object_2_close.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append(object_name_list[i]) 
           
        elif y_values[i]<y_values[j] and Image_name_list[i]==Image_name_list[j]:
            object_1.append(object_name_list[i]) 
            imageName.append(Image_name_list[i])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append("na")
            object_2_is_in_the_top_of.append(object_name_list[j])
            object_1.append(object_name_list[j]) 
            imageName.append(Image_name_list[j])
            object_2_close.append("na")
            object_2_far.append("na")
            object_2_is_greater_than.append("na")
            object_2_is_smaller_than.append("na")
            object_2_overlaps.append("na")
            object_2_doesnt_overlap.append("na")
            object_2_is_in_the_right_of.append("na")
            object_2_is_in_the_left_of.append("na")
            object_2_is_in_the_bottom_of.append(object_name_list[i])      
            object_2_is_in_the_top_of.append("na") 
          



# 8
aaf = pd.DataFrame(data={ "image_Name":imageName, "object_1": object_1, "is close to":object_2_close, "is far from":object_2_far,"is_smaller_than":object_2_is_smaller_than,"is_greater_than":object_2_is_greater_than,
'overlaps':object_2_overlaps,
'doesnt_overlap':object_2_doesnt_overlap,
'is_in_the_right_of':object_2_is_in_the_right_of,
'is_in_the_left_of':object_2_is_in_the_left_of,
'is_in_the_bottom_of':object_2_is_in_the_bottom_of,
'is_in_the_top_of':object_2_is_in_the_top_of,
})
aaf.to_csv("F:\\yolov3-master\\excels\\OORelation.csv", sep=',',index=False)

t2=time.time()
print("we got the URLs in",getTheUrl-t1,"seconds which is",(getTheUrl-t1)/60,"minutes")
print("we got the uncompressed photos downloaded from the urls in",downloadingThePhotos2-downloadingThePhotos1,"seconds which is",((downloadingThePhotos2-downloadingThePhotos1)/60),"minutes")
print("we got the yolo implemented in",doingYolo2-doingYolo1,"seconds which is",((doingYolo2-doingYolo1)/60),"minutes")
print("completed the program in",t2-t1,"seconds")
print("completed the program in",((t2-t1)/60),"minutes")





