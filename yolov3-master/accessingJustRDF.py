import argparse
import subprocess
import numpy as np
import os
import io
#download2yolo2excel
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

if __name__ == "__main__":
    parser=argparse.ArgumentParser() #ArgumentParser is the class of the object(module)argparse
    parser.add_argument("QID",help="please enter a valid qid, you can refer here, https://query.wikidata.org/")


    args=parser.parse_args()






t1=time.time()

url_list = []

#?action=query&list=search&srsearch=haswbstatement:P180=Q7378&srnamespace=6&format=json
URL = "https://commons.wikimedia.org/w/api.php"

# defining a params dict for the parameters to be sent to the API
PARAMS = {
    'action' :'query',
    'list': 'search',
    'srsearch': 'haswbstatement:P180='+args.QID,
    'srnamespace': '6',
    'format': 'json',
    }

# sending get request and saving the response as response object
r = requests.get(url = URL, params = PARAMS)

os.chdir(r"F:\yolov3-master\texts")
for image in r.json()['query']['search']:
    # print(image['pageid'])
    URL = 'https://commons.wikimedia.org/wiki/Special:EntityData/M'+str(image['pageid'])+'.ttl'
    r = requests.get(url=URL)
    # print(r.content)

    g = Graph()
    # print(io.StringIO(r.content.decode("utf-8") ))
    g.parse(io.StringIO(r.content.decode("utf-8") ), format="ttl")

    # print(len(g))  # prints 2

    import pprint

    for stmt in g:
        text_file = open("gparsed.ttl", "wb")
        n = text_file.write(r.content)
        text_file.close()

        if 'http://schema.org/contentUrl' == str(stmt[1]):
            imageURL = (stmt[2])
            url_list.append(imageURL)

getTheUrl=time.time()

downloadingThePhotos1=time.time()

fileName=[]
for url in url_list:
    filename = url.split('/')[-1]
    try:
        os.chdir(r"F:\yolov3-master\test")
        a=urllib.request.urlretrieve(url, filename)
        fileName.append(filename)
  

    except OSError as oserr:
      if oserr.errno ==36:
        pass
      else:
        raise

downloadingThePhotos2=time.time()

af=pd.DataFrame(data={"URLs":url_list,"Image name":fileName})
af.to_csv("F:\\yolov3-master\\excels\\urlsFilenames.csv",sep=',',index=False)
