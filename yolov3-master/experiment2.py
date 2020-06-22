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
from pathlib import Path

from pandas import DataFrame

t1=time.time()

url_list = []
filename=[]
#?action=query&list=search&srsearch=haswbstatement:P180=Q7378&srnamespace=6&format=json
URL = "https://commons.wikimedia.org/w/api.php"

# defining a params dict for the parameters to be sent to the API
PARAMS = {
    'action' :'query',
    'list': 'search',
    'srsearch': 'haswbstatement:P180=Q7378',
    'srnamespace': '6',
    'format': 'json',
    }

# sending get request and saving the response as response object
r = requests.get(url = URL, params = PARAMS)


for image in r.json()['query']['search']:
    # print(image['pageid'])
    URL = 'https://commons.wikimedia.org/wiki/Special:EntityData/M'+str(image['pageid'])+'.ttl'
    r = requests.get(url=URL)
    # print(r.content)

    g = Graph()
    # print(io.StringIO(r.content.decode("utf-8") ))
    g.parse(io.StringIO(r.content.decode("utf-8") ), format="ttl")
    text_file = open("gparsed.ttl", "wb")
    n = text_file.write(r.content)
    text_file.close()
    for row in g.query("SELECT DISTINCT ?o1 WHERE { ?s1 <http://schema.org/contentUrl> ?o1. }"):
                    # print(row[0])
                    url_list.append(row[0].__str__())
    print(len(url_list))
    os.chdir(r"F:\yolov3-master\test")
    for url in url_list:
        print(url)
        r = requests.get(url)
        if r.ok:
            p = Path("/".join([url.split("/")[-1]]))
            if p.exists():
                p.write_text()
            try:
                with open("/".join([url.split("/")[-1]]), "wb") as f:
                
                    f.write(r.content)
                    filename.append(url.split("/")[-1])
            except FileNotFoundError as oserr:
                pass
downloadingThePhotos2=time.time()

af=pd.DataFrame(data={"URLs":url_list,"Image name":filename})
af.to_csv("F:\\yolov3-master\\excels\\urlsFilenames.csv",sep=',',index=False)

    # print(len(g))  # prints 2