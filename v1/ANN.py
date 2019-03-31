import traceback
from v1 import Repository
import operator


def training(dataset):
    try:
        packet_loss_ann = Repository.load(Repository.PACKET_LOSS_ANN)
        latency_ann = Repository.load(Repository.LATENCY_ANN)
        energy_consumption_ann = Repository.load(Repository.ENERGY_CONSUMPTION_ANN)
        standard_scaler = Repository.load(Repository.STANDARD_SCALER)
        min_max_scaler = Repository.load(Repository.MIN_MAX_SCALER)

        features = dataset['features']
        standard_scaler.partial_fit(features)
        standard_transformed_features = standard_scaler.transform(features)
        min_max_scaler.partial_fit(features)
        min_max_transformed_features = min_max_scaler.transform(features)

        packet_loss_ann.partial_fit(standard_transformed_features, dataset['packet_loss'], classes=[0, 1])
        latency_ann.partial_fit(min_max_transformed_features, dataset['latency'], classes=[0, 1])
        energy_consumption_ann.partial_fit(min_max_transformed_features, dataset['energy_consumption'])

        Repository.save(packet_loss_ann, Repository.PACKET_LOSS_ANN)
        Repository.save(latency_ann, Repository.LATENCY_ANN)
        Repository.save(energy_consumption_ann, Repository.ENERGY_CONSUMPTION_ANN)
        Repository.save(standard_scaler, Repository.STANDARD_SCALER)
        Repository.save(min_max_scaler, Repository.MIN_MAX_SCALER)
        return {'message': 'training successful'}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'training failed'}


def testing(dataset):
    try:
        packet_loss_ann = Repository.load(Repository.PACKET_LOSS_ANN)
        latency_ann = Repository.load(Repository.LATENCY_ANN)
        energy_consumption_ann = Repository.load(Repository.ENERGY_CONSUMPTION_ANN)
        standard_scaler = Repository.load(Repository.STANDARD_SCALER)
        min_max_scaler = Repository.load(Repository.MIN_MAX_SCALER)

        features = dataset['features']
        standard_transformed_features = standard_scaler.transform(features)
        min_max_transformed_features = min_max_scaler.transform(features)

        packet_loss_predictions = packet_loss_ann.predict(standard_transformed_features)
        packet_loss_valid_options = find_valid_options(
            packet_loss_predictions,
            standard_transformed_features,
            [i for i in range(len(standard_transformed_features))]  # original_indexes
        )

        latency_features = [
            min_max_transformed_features[index] for index in packet_loss_valid_options['original_indexes']
        ]

        latency_predictions = latency_ann.predict(latency_features)
        latency_valid_options = find_valid_options(
            latency_predictions,
            latency_features,
            packet_loss_valid_options['original_indexes']
        )

        energy_consumption_predictions = energy_consumption_ann.predict(latency_valid_options['features'])

        Repository.save(packet_loss_ann, Repository.PACKET_LOSS_ANN)
        Repository.save(latency_ann, Repository.LATENCY_ANN)
        Repository.save(energy_consumption_ann, Repository.ENERGY_CONSUMPTION_ANN)
        Repository.save(standard_scaler, Repository.STANDARD_SCALER)
        Repository.save(min_max_scaler, Repository.MIN_MAX_SCALER)

        final_valid_options = []

        for index, prediction in enumerate(energy_consumption_predictions):
            final_valid_options.append({
                'energy_consumption': prediction,
                'index': latency_valid_options['original_indexes'][index]
            })

        final_valid_options.sort(key=operator.itemgetter('energy_consumption'))

        final_valid_options_indexes = []

        for option in final_valid_options:
            final_valid_options_indexes.append(option['index'])

        return {'indexes': final_valid_options_indexes}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'testing failed'}


def find_valid_options(predictions, previous_features, previous_features_indexes):
    valid_options = {
        'features': [],
        'original_indexes': []
    }

    for index, prediction in enumerate(predictions):
        if prediction == 1:
            valid_options['features'].append(previous_features[index])
            valid_options['original_indexes'].append(previous_features_indexes[index])

    if len(valid_options['features']) > 0:
        return valid_options

    else:
        return {
            'features': previous_features,
            'original_indexes': previous_features_indexes
        }
