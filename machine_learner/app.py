import traceback
import repository
import classification
from flask import Flask, request, jsonify


app = Flask(__name__)

repository.initialize()


@app.route('/', methods=['GET', 'POST', 'OPTIONS'])
def training_testing():
    try:
        if request.method == 'POST':
            dataset = request.get_json()
            mode = request.args.get('mode')
            version = request.args.get('version')

            if mode == 'training':
                if version == 'v1':
                    return jsonify(classification.v1_training(dataset, repository))

                elif version == 'v2':
                    return jsonify(classification.v2_training(dataset, repository))

            elif mode == 'testing':
                if version == 'v1':
                    return jsonify(classification.v1_testing(dataset, repository))

                elif version == 'v2':
                    return jsonify(classification.v2_testing(dataset, repository))

            else:
                return jsonify({'message': 'invalid mode and/or version'})

        else:
            return jsonify({'message': 'only POST request is allowed'})
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return jsonify({'message': 'internal server error'})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')

