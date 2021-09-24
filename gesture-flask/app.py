import os
from flask import Flask, flash, request, redirect, url_for
from flask import send_file
from werkzeug.utils import secure_filename
import random
import cv2
import pickle
import tensorflow as tf

from frameextractor import frameExtractor
from handshape_feature_extractor import HandShapeFeatureExtractor

UPLOAD_FOLDER = 'uploads/'
ALLOWED_EXTENSIONS = {'mp4'}

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

extractor = HandShapeFeatureExtractor().get_instance()


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/upload', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        # if user does not select file, browser also
        # submit a empty part without filename
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.root_path, app.config['UPLOAD_FOLDER'], filename))
            return {"status": "success", "message": "file_uploaded"}


@app.route('/videos/<path:filename>', methods=['GET'])
def download(filename):
    return send_file("videos/" + filename + ".mp4", as_attachment=True)


@app.route('/predict', methods=['GET', 'POST'])
def predict_gesture():
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        # if user does not select file, browser also
        # submit a empty part without filename
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file_path = os.path.join(app.root_path, app.config['UPLOAD_FOLDER'], filename)
            file.save(os.path.join(file_path))
            count = random.randint(-1, 100)
            image_name = "/%#05d.png" % (count + 1)
            frameExtractor(file_path, "frames", count)
            f = extractor.extract_feature(cv2.imread('frames/' + image_name))
            featureMap = pickle.load(open("train_set", "rb"))
            l = []
            for fmap in featureMap:
                m = tf.keras.metrics.CosineSimilarity(axis=1)
                m.update_state(fmap[1], f)
                cosine_similarity = m.result().numpy()
                l.append([cosine_similarity, fmap[0]])
            l.sort(key=lambda it: it[0], reverse=True)
            recognized_gesture = l[0][1]
            return {"status": "success", "gesture": recognized_gesture, "percentage": str(l[0][0]*100.0)}


app.run(debug=True)
