import io

import requests
from rdflib import Graph

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
    i = 10
    while i <= 10:
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

    for url in url_list:
        print(url)
        r = requests.get(url)
        if r.ok:
            with open("/".join([url.split("/")[-1]]), "wb") as f:
                f.write(r.content)