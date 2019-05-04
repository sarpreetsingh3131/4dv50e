import traceback


def v1_training(dataset, repository):
    try:
        models = repository.get_models('v1')
        features = dataset['features']

        for model, scaler, target in [
            (models['packet_loss'], models['standard_scaler'], dataset['packet_loss']),
            (models['latency'], models['max_abs_scaler'], dataset['latency']),
        ]:
            scaler.partial_fit(features)
            transformed_features = scaler.transform(features)
            model.partial_fit(transformed_features, target, classes=[0, 1])

        repository.save_models(models, 'v1')
        return {'message': 'training successful'}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'training failed'}


def v1_testing(dataset, repository):
    try:
        models = repository.get_models('v1')
        features = dataset['features']
        indexes = [i for i in range(0, len(features))]

        for model, scaler in [
            (models['packet_loss'], models['standard_scaler']),
            (models['latency'], models['max_abs_scaler']),
        ]:
            relevant_features = []
            relevant_indexes = []
            transformed_features = scaler.transform(features)
            predictions = model.predict(transformed_features)

            for index, prediction in enumerate(predictions):
                if prediction == 1:
                    relevant_features.append(features[index])
                    relevant_indexes.append(indexes[index])

            if len(relevant_features) > 0:
                features = relevant_features
                indexes = relevant_indexes

        repository.save_models(models, 'v1')
        return {'indexes': indexes}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'testing failed'}


def v2_training(dataset, repository):
    try:
        models = repository.get_models('v2')
        features = dataset['features']
        models['standard_scaler'].partial_fit(features)
        transformed_features = models['standard_scaler'].transform(features)
        models['packet_loss'].partial_fit(transformed_features, dataset['packet_loss'], classes=[0, 1])
        models['latency'].partial_fit(transformed_features, dataset['latency'], classes=[0, 1])
        repository.save_models(models, 'v2')
        return {'message': 'training successful'}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'training failed'}


def v2_testing(dataset, repository):
    try:
        models = repository.get_models('v2')
        features = dataset['features']
        transformed_features = models['standard_scaler'].transform(features)
        indexes = [i for i in range(0, len(features))]

        for model in [models['packet_loss'], models['latency']]:
            relevant_features = []
            relevant_indexes = []
            predictions = model.predict(transformed_features)

            for index, prediction in enumerate(predictions):
                if prediction == 1:
                    relevant_features.append(transformed_features[index])
                    relevant_indexes.append(indexes[index])

            if len(relevant_features) > 0:
                transformed_features = relevant_features
                indexes = relevant_indexes

        repository.save_models(models, 'v2')
        return {'indexes': indexes}
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return {'message': 'testing failed'}
