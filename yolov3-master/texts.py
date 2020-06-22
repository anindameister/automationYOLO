# import glob
# import shutil
# from PIL import Image
# for i in glob.glob("output/*.*"):
#     if i.endswith(".txt") or i.endswith(".svg"):
#         f = open(i, "r")
#         reading=f.read()
#         shutil.copy(reading, 'F:\yolov3-master\texts')

import os
import shutil
dir_path = 'F:\yolov3-master\output'
os.chdir(dir_path)
src_files = os.listdir(dir_path)
for file_name in src_files:
    if file_name.endswith(".txt"):
        full_file_name = os.path.join(dir_path, file_name)
        if os.path.isfile(full_file_name):
            shutil.copy(full_file_name, 'F:\\yolov3-master\\texts')