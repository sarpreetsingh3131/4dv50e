import json
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler, MaxAbsScaler
import time

version = 'v1'  # v1, v2

total_samples = 4000 if version == 'v2' else 1000  # any desired value
test_sizes = [0.9, 0.7, 0.5, 0.3]  # any desired value

models = {
    'v1': {
        'packet_loss': {
            'model': MLPClassifier(
                hidden_layer_sizes=(47, 161, 17, 105, 194, 276, 285, 12, 225),
                activation='tanh',
                random_state=1
            ),
            'scaler': StandardScaler()
        },
        'latency': {
            'model': MLPClassifier(
                hidden_layer_sizes=(245,),
                activation='tanh',
                random_state=1
            ),
            'scaler': MaxAbsScaler()
        }
    },
    'v2': {
        'packet_loss': {
            'model': MLPClassifier(
                hidden_layer_sizes=(274, 177, 129, 94),
                activation='tanh',
                random_state=1
            ),
            'scaler': StandardScaler()
        },
        'latency': {
            'model': MLPClassifier(
                hidden_layer_sizes=(215, 294, 230, 168, 136, 53),
                activation='tanh',
                random_state=1
            ),
            'scaler': StandardScaler()
        }
    }
}

results = []

for dataset_name, dataset in [
    ('packet_loss', json.load(open('data/dataset/' + version + '/packet_loss.json'))),
    ('latency', json.load(open('data/dataset/' + version + '/latency.json'))),
]:
    start_time = time.time()
    accuracy = []
    training_samples = []
    features = list(dataset['features'][:total_samples])
    target = list(dataset['target'][:total_samples])

    for test_size in test_sizes:
        training_features, testing_features, training_target, testing_target = train_test_split(
            features,
            target,
            test_size=test_size,
            random_state=1,
        )

        training_samples.append(len(training_target))

        model = models[version][dataset_name]['model']
        scaler = models[version][dataset_name]['scaler']

        scaler.fit(training_features)
        training_features = scaler.transform(training_features)
        testing_features = scaler.transform(testing_features)

        model.fit(training_features, training_target)
        accuracy.append(model.score(testing_features, testing_target))

    end_time = time.time() - start_time

    print({
        'version': version,
        'target': dataset_name,
        'accuracy': accuracy,
        'training_samples': training_samples,
        'total_samples': total_samples,
        'execution_time_in_sec': end_time
    })

    results.append({
        'target': dataset_name,
        'accuracy': accuracy,
        'training_samples': training_samples,
        'total_samples': total_samples,
        'execution_time_in_sec': end_time
    })

with open('data/training_selection/' + version + '_training_selection.json', 'w') as f:
    json.dump(results, f, indent=2)
