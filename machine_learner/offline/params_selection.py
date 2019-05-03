import csv
import json
import operator
import time

from sklearn.model_selection import ParameterGrid, train_test_split
from sklearn.neural_network import MLPClassifier, MLPRegressor
from sklearn.preprocessing import MaxAbsScaler, MinMaxScaler, StandardScaler


version = 'v1'  # change version here -> v1, v2


'''
see results of best hidden layers in /data/params_selection
after each iteration, update hidden_layers_sizes variable MANUALLY 
with best accuracy hidden layers size. the best accuracy is the first one in the results
eg: hidden_layer_sizes = [(256, 49, i,) for i in range(1, max_neurons + 1)]
'''
max_neurons = 300  # any desired value
hidden_layer_sizes = [(i,) for i in range(1, max_neurons + 1)]


'''
tanh needs feature range from (-1,1)
relu needs feature range from (0, infinity)
logistic needs feature range from (0, 1)

after first iteration, MANUALLY remove those activation functions that are worse.
only keep the best one. see results in /data/params_selection
'''
param_grid = {
    'standard': list(
        ParameterGrid({
            'activation': ['tanh'],
            'hidden_layer_sizes': hidden_layer_sizes
        })
    ),
    'min_max': list(
        ParameterGrid({
            'activation': ['relu', 'logistic'],
            'hidden_layer_sizes': hidden_layer_sizes
        })
    ),
    'max_abs': list(
        ParameterGrid({
            'activation': ['tanh'],
            'hidden_layer_sizes': hidden_layer_sizes
        })
    )
}


testing_samples = 1000  # any desired value
test_size = 0.9  # any desired value


'''
only use one dataset at a time because models will have different best parameters
'''
for dataset_name, dataset in [
    #('packet_loss', json.load(open('data/dataset/' + version + '/packet_loss.json'))),
    ('latency', json.load(open('data/dataset/' + version + '/latency.json'))),
]:
    start_time = time.time()

    result = []
    features = dataset['features'][:testing_samples]
    target = dataset['target'][:testing_samples]
    dataset = None  # save runtime memory

    training_features, testing_features, training_target, testing_target = train_test_split(
        features,
        target,
        test_size=test_size,
        random_state=1
    )

    '''
    after 1st iteration, MANUALLY remove the worst scalers and only
    keep the best one, see results in /data/params_selection
    '''
    for scaler_name, scaler in [
        #('standard', StandardScaler),
        #('min_max', MinMaxScaler),
        ('max_abs', MaxAbsScaler)
    ]:
        for params in param_grid[scaler_name]:
            model = MLPClassifier(random_state=1, early_stopping=True)
            model.set_params(**params)

            a_scaler = scaler()
            a_scaler.fit(training_features)
            training_features = a_scaler.transform(training_features)
            testing_features = a_scaler.transform(testing_features)
            model.fit(training_features, training_target)

            accuracy = model.score(testing_features, testing_target)

            print({
                'version': version,
                'dataset': dataset_name,
                'scaler': scaler_name,
                'params': params,
                'accuracy': accuracy
            })

            result.append({
                'scaler': scaler_name,
                'params': params,
                'accuracy': accuracy
            })

    result.sort(key=operator.itemgetter('accuracy'), reverse=True)

    end_time = time.time() - start_time

    path = 'data/params_selection/' + version + '/' + dataset_name
    hidden_layers =  str(len(hidden_layer_sizes[0])) 

    with open(path + '/hidden_layer_' + hidden_layers + '.csv', 'w') as f:
        dict_writer = csv.DictWriter(f, result[0].keys())
        dict_writer.writeheader()
        dict_writer.writerows(result)

    with open(path + '/execution_time.txt', 'a') as f:
        f.write('\n' + 'hidden_layer=' + hidden_layers + ',' + 'execution_time_in_sec=' + str(end_time))
