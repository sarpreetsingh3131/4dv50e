import traceback
from v1 import Repository as V1Repository
from v1 import ANN as V1ANN
from flask import Flask, request, jsonify

app = Flask(__name__)

V1Repository.initialize()


@app.route('/', methods=['GET', 'POST', 'OPTIONS'])
def training_testing():
    try:
        if request.method == 'POST':
            dataset = request.get_json()

            if request.args.get('mode') == 'training':
                if request.args.get('version') == 'v1':
                    return jsonify(V1ANN.training(dataset))

            elif request.args.get('mode') == 'testing':
                if request.args.get('version') == 'v1':
                    return jsonify(V1ANN.training(dataset))

            else:
                return jsonify({'message': 'invalid mode and/or version'})
        else:
            return jsonify({'message': 'only POST request is allowed'})
    except Exception as e:
        traceback.print_tb(e.__traceback__)
        return jsonify({'message': 'internal server error'})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
