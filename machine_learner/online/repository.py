import os
import shutil
from sklearn.externals import joblib
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler, MaxAbsScaler

dir_name = 'model'

models = {
    'v1_packet_loss': MLPClassifier(
        hidden_layer_sizes=(47, 161, 17, 105, 194, 276, 285, 12, 225),
        activation='tanh',
        random_state=1
    ),
    'v1_latency': MLPClassifier(
        hidden_layer_sizes=(245,),
        activation='tanh',
        random_state=1,
    ),
    'v1_standard_scaler': StandardScaler(),
    'v1_max_abs_scaler': MaxAbsScaler(),

    'v2_packet_loss': MLPClassifier(
        hidden_layer_sizes=(274, 177, 129, 94),
        activation='tanh',
        random_state=1
    ),
    'v2_latency': MLPClassifier(
        hidden_layer_sizes=(215, 294, 230, 168, 136, 53),
        activation='tanh',
        random_state=1,
    ),
    'v2_standard_scaler': StandardScaler(),
}


def get_models(version):
    if version == 'v1':
        return {
            'packet_loss': get_model(version + '_packet_loss'),
            'latency': get_model(version + '_latency'),
            'standard_scaler': get_model(version + '_standard_scaler'),
            'max_abs_scaler': get_model(version + '_max_abs_scaler')
        }
    else:
        return {
            'packet_loss': get_model(version + '_packet_loss'),
            'latency': get_model(version + '_latency'),
            'standard_scaler': get_model(version + '_standard_scaler'),
        }


def save_models(models, version):
    for model_name in models:
        save_model(version + '_' + model_name, models[model_name])


def get_model(model_name):
    try:
        return joblib.load(dir_name + '/' + model_name + '.pkl')
    except Exception as e:
        print('unable to load model: ' + model_name)
        return models[model_name]


def save_model(model_name, model):
    try:
        joblib.dump(model, dir_name + '/' + model_name + '.pkl')
    except Exception as e:
        print('unable to save model: ' + model_name)


def initialize():
    if os.path.exists(dir_name):
        shutil.rmtree(dir_name)

    os.makedirs(dir_name)
