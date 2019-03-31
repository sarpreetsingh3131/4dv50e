import os
import shutil
from sklearn.externals import joblib
from sklearn.neural_network import MLPClassifier, MLPRegressor
from sklearn.preprocessing import MinMaxScaler, StandardScaler


PACKET_LOSS_ANN = 'PacketLossANN'
LATENCY_ANN = 'LatencyANN'
ENERGY_CONSUMPTION_ANN = 'EnergyConsumptionANN'
STANDARD_SCALER = 'StandardScaler'
MIN_MAX_SCALER = 'MinMaxScaler'
DIR_NAME = 'v1-trained-learning-models'


models = {
    PACKET_LOSS_ANN: MLPClassifier(
        hidden_layer_sizes=(204, 288, 10, 69, 74, 96, 172),
        activation='tanh',
        max_iter=200,
        random_state=1
    ),
    LATENCY_ANN: MLPClassifier(
        hidden_layer_sizes=(92),
        activation='relu',
        max_iter=200,
        random_state=1
    ),
    ENERGY_CONSUMPTION_ANN: MLPRegressor(
        hidden_layer_sizes=(126,),
        activation='relu',
        max_iter=1000,
        random_state=1
    ),
    STANDARD_SCALER: StandardScaler(),
    MIN_MAX_SCALER: MinMaxScaler()
}


def load(model_name):
    try:
        return joblib.load(DIR_NAME + '/' + model_name + '.pkl')
    except Exception as e:
        print('unable to load model: ' + model_name)
        return models[model_name]


def save(model, model_name):
    try:
        joblib.dump(model, DIR_NAME + '/' + model_name + '.pkl')
    except Exception as e:
        print('unable to save model: ' + model_name)


def initialize():
    if os.path.exists(DIR_NAME):
        shutil.rmtree(DIR_NAME)

    os.makedirs(DIR_NAME)

