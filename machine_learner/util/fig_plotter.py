import matplotlib.pyplot as plt
import pandas as pd
from ast import literal_eval
import json
import numpy as np
import matplotlib.patches as mpatches
from sklearn.preprocessing import StandardScaler, MinMaxScaler, MaxAbsScaler


version = 'v1'  # v1, v2


def plot_learning_vs_no_learning():
    plt.figure()
    plt_index = 1

    for title in [
        'Packet Loss (%)',
        'Latency (%)',
        'Energy Consumption (coulomb)',
        'Adaptation Space',
        'Analysis Time (sec)',
        'Adaptation Time (sec)'
    ]:
        data = {
            'learning': [],
            'no_learning': []
        }

        for file_name in ['learning', 'no_learning']:
            path = 'data/controlled_experiment/' + version + '/'
            file_data = open(path + file_name + '.txt').readlines()
            file_data = [x.strip() for x in file_data]
            learning_size = 11
            no_learning_size = 8

            for line in file_data:
                content = line.split(';')

                if len(content) > 1 and int(content[0]) == 1:  # skip training cycle
                    continue

                if title == 'Packet Loss (%)':
                    if len(content) == no_learning_size:
                        data[file_name].append(float(content[4]))
                    elif len(content) == learning_size:
                        data[file_name].append(float(content[7]))

                elif title == 'Latency (%)':
                    if len(content) == no_learning_size:
                        data[file_name].append(float(content[5]))
                    elif len(content) == learning_size:
                        data[file_name].append(float(content[8]))

                elif title == 'Energy Consumption (coulomb)':
                    if len(content) == no_learning_size:
                        data[file_name].append(float(content[6]))
                    elif len(content) == learning_size:
                        data[file_name].append(float(content[9]))

                elif title == 'Adaptation Space':
                    if len(content) == no_learning_size:
                        data[file_name].append(int(content[2]))
                    elif len(content) == learning_size:
                        data[file_name].append(int(content[4]))

                elif title == 'Analysis Time (sec)':
                    if len(content) == no_learning_size:
                        data[file_name].append(float(content[3]) / 1000)
                    elif len(content) == learning_size:
                        data[file_name].append(float(content[5]) / 1000)

                elif title == 'Adaptation Time (sec)':
                    if len(content) == 8:
                        data[file_name].append((float(content[7]) - float(content[1])) / 1000)
                    elif len(content) == 11:
                        data[file_name].append((float(content[10]) - float(content[1])) / 1000)

        print({
            'title': title,
            'learning_avg': np.average(data['learning']),
            'no_learning_avg': np.average(data['no_learning'])
        })

        plt.subplot(2, 3, plt_index)

        boxplot = plt.boxplot(
            [data[x] for x in ['learning', 'no_learning']],
            positions=[1, 2],
            widths=.3,
            labels=['Learning', 'No Learning'],
            patch_artist=True,
            medianprops={'color': 'black', 'linewidth': 2}
        )

        for index, box in enumerate(boxplot['boxes']):
            box.set(facecolor=['orange', 'dodgerblue'][index])

        plt.ylabel(title)
        plt_index += 1

    plt.show()


def plot_learning_models_time():
    plt.figure()

    for plt_index, v in enumerate(['v1', 'v2']):
        initial_training_time = []
        training_time = []
        prediction_time = []
        file_data = open('data/controlled_experiment/' + v + '/learning.txt').readlines()
        file_data = [x.strip() for x in file_data]

        for line in file_data:
            content = line.split(';')

            if len(content) == 9:
                print('ini')
                initial_training_time.append(float(content[4]) / 1000)
            elif len(content) == 11:
                prediction_time.append(float(content[2]) / 1000)
                training_time.append(float(content[6]) / 1000)

        print({
            'version': v,
            'avg_initial_training_time': np.average(initial_training_time),
            'avg_training_time': np.average(training_time),
            'avg_prediction_time': np.average(prediction_time)
        })

        plt.subplot(1, 2, plt_index + 1)

        boxplot = plt.boxplot(
            [initial_training_time, prediction_time, training_time],
            positions=[1, 2, 3],
            widths=.3,
            labels=['Initial Training', 'Prediction', 'Training After Prediction'],
            patch_artist=True,
            medianprops={'color': 'black', 'linewidth': 2}
        )

        for index, box in enumerate(boxplot['boxes']):
            box.set(facecolor=['orange', 'dodgerblue', 'green'][index])

        plt.ylabel('Time (sec)')
    plt.show()


def plot_uncertainties_profiles():
    path = 'data/uncertainties_profiles'
    index = 1

    plt.figure()
    for label, file in ([
        ('Packets Load', open(path + '/packets_load_1.txt')),
        ('Packets Load', open(path + '/packets_load_2.txt')),
        ('SNR', open(path + '/snr_1.txt')),
        ('SNR', open(path + '/snr_2.txt'))
    ]):
        file_data = file.readlines()
        data = [float(x.strip()) for x in file_data]
        plt.subplot(2, 2, index)
        plt.plot(np.arange(0, len(data[:76]), 1), data[:76], c='red')
        plt.xticks(
            np.arange(0, len(data[:76]), 25),
            ['08:00', '12:00', '16:00', '20:00']
        )
        plt.xlabel('Time')
        plt.ylabel(label)
        index += 1
    plt.show()


def plot_training_selection():
    data = json.load(open('data/training_selection/' + version + '_training_selection.json'))
    labels = {
        'packet_loss': 'Packet Loss Model',
        'latency': 'Latency Model'
    }

    plt.figure()
    for item in data:
        plt.plot(item['training_samples'], item['accuracy'], label=labels[item['target']])

    plt.ylabel('Accuracy (%)')
    plt.xlabel('Training Samples\n(Total Samples = ' + str(data[0]['total_samples']) + ')')
    plt.xticks(data[0]['training_samples'])
    plt.ylim(top=1.0, bottom=0.0)
    plt.grid()
    plt.legend()
    plt.show()


def plot_offline_activities_time():
    plt.figure()
    for index, v in enumerate(['v1', 'v2']):
        training_cycles_selection_time = 0
        activation_function_selection_time = 0
        hidden_layers_selection_time = 0

        for item in json.load(open('data/training_selection/' + v + '_training_selection.json')):
            training_cycles_selection_time += item['execution_time_in_sec']

        for dir_name in ['packet_loss', 'latency']:
            file_data = open('data/params_scaler_selection/' + v + '/' + dir_name + '/execution_time.txt').readlines()
            file_data = [x.strip() for x in file_data]
            
            for line in file_data:
                content = line.split(',')
                
                if len(content) == 2:
                    if int(content[0].split('=')[1]) == 1:
                        activation_function_selection_time += float(content[1].split('=')[1])

                    hidden_layers_selection_time += float(content[1].split('=')[1])

        values = [
            activation_function_selection_time,
            hidden_layers_selection_time,
            training_cycles_selection_time
        ]
        plt.subplot(1, 2, index + 1)
        p = plt.pie(
            values,
            colors=['dodgerblue', 'orange', 'c'],
            autopct=lambda pct: '{:.1f}\nmin'.format((pct / 100. * np.sum(values)) / 60),
            startangle=90
        )
        print(plt)
        plt.axis('equal')
    
    plt.legend(
        bbox_to_anchor=(0., 1.02, 1.2, .102),
        ncol=3,
        borderaxespad=0.2,
        handles=[
            mpatches.Patch(color='dodgerblue', label='Activation Function Selection'),
            mpatches.Patch(color='orange', label='Hidden Layers Selection'),
            mpatches.Patch(color='c', label='Training Cycles Selection'),
        ]
    )
    plt.show()


def plot_offline_activities():
    data = {
        'activation_function': {
            'packet_loss': [],
            'latency': []
        },
        'hidden_layers': {
            'packet_loss': [],
            'latency': []  
        }
    }
    activation_functions = [
        'Tanh\n(With Standard Scaling)',
        'Relu\n(With Min Max Scaling)',
        'Tanh\n(With Max Abs Scaling)',
        'Logistic\n(With Min Max Scaling)'
    ]

    for dir_path in ['/packet_loss', '/latency']:
        csv = pd.read_csv('data/params_selection/' + version + dir_path + '/hidden_layer_1.csv')
        for item in activation_functions:
            accuracy = []
            activation = item.split('\n')[0].lower()
            scaler = item.split('With ')[1].split(' Scaling')[0].replace(' ', '_').lower()

            for record in csv.to_dict('records'):
                if record['scaler'] == scaler and literal_eval(record['params'])['activation'] == activation:
                    accuracy.append(record['accuracy'])

            if dir_path.endswith('packet_loss'):
                data['activation_function']['packet_loss'].append(max(accuracy))
            else:
                data['activation_function']['latency'].append(max(accuracy))
        
        for i in range(1, 11):
            csv = pd.read_csv('data/params_selection/' + version + dir_path + '/hidden_layer_' + str(i) + '.csv')
            records = csv.to_dict('records')

            if dir_path.endswith('packet_loss'):
                data['hidden_layers']['packet_loss'].append(records[0]['accuracy'])
            else:
                data['hidden_layers']['latency'].append(records[0]['accuracy'])
    
    plt.figure()
    index = 1
    for x, x_label, key in [
        (activation_functions, 'Activation Function', 'activation_function'),
        ([i for i in range(1, 11)], 'Hidden Layers', 'hidden_layers')
    ]:
        plt.subplot(2, 1, index)
        plot(
            x, 
            data[key]['packet_loss'], 
            data[key]['latency'],
            x_label
        )
        
        if index == 1:
            plt.legend(
                bbox_to_anchor=(0.0, 1.2, 0.8, .102),
                ncol=2,
                borderaxespad=0.2,
                handles=[
                    mpatches.Patch(color='b', label='Packet Loss Model'),
                    mpatches.Patch(color='g', label='Latency Model')
                ]
            )
        index += 1

    plt.show()


def plot(x, packet_loss, latency, x_label):
    plt.plot(x, packet_loss, label='Packet Loss Model', c='b')
    plt.plot(x, latency, label='Latency Model', c='g')
    plt.xticks(x)
    plt.xlabel(x_label)
    plt.ylim(top=1.0, bottom=0.0)
    plt.ylabel('Accuracy (%)\n(Total Samples = 1000,\nTraining Samples = 100)\n')
    plt.grid()


def plot_selected_adaptation_options(cycle):
    data = json.load(open('data/selected_adaptation_options/' + version + '/cycle_' + str(cycle) + '.json'))
    selected_options = {
        'packet_loss': [],
        'latency': []
    }

    for index in data['indexes_of_selected_adaptation_options']:
        selected_options['packet_loss'].append(data['packet_loss'][index])
        selected_options['latency'].append(data['latency'][index])

    plt.figure()
    plt.scatter(data['packet_loss'], data['latency'], color='orange')
    plt.scatter(selected_options['packet_loss'], selected_options['latency'], color='green')
    plt.plot([10, 10], [0, max(data['latency'])], color='red')
    plt.plot([0, max(data['packet_loss'])], [5, 5], color='red')
    plt.xlabel('Packet Loss (%)')
    plt.ylabel('Latency (%)')
    plt.legend(
        bbox_to_anchor=(0., 1.02, 1., .102),
        ncol=2,
        mode='expand',
        borderaxespad=0.,
        handles=[
            mpatches.Patch(color='orange', label='Adaptation Options'),
            mpatches.Patch(color='green', label='Selected Adaptation Options')
        ]
    )
    plt.show()


def plot_feature_scaling():
    plt_index = 1
    dataset = json.load(open('data/dataset/' + version + '/packet_loss.json'))
    features = dataset['features']

    for scaler_name, scaler in [
        ('No Scaling', None),
        ('With Min Max Scaler', MinMaxScaler),
        ('With Standard Scaler', StandardScaler),
        ('With Max Abs Scaler', MaxAbsScaler)
    ]:
        plt.subplot(2, 2, plt_index)
        f = list(features)

        if scaler is not None:
            scaler = scaler()
            scaler.partial_fit(f)
            f = scaler.transform(f)

        plt.bar(range(1, 18), f[0][:17], label='SNR', color='red')
        plt.bar(range(18, 34), f[0][17:33], label='Power', color='blue')
        plt.bar(range(34, 51), f[0][33:50], label='Distribution', color='green')
        plt.bar(range(51, 66), f[0][50:], label='Load', color='orange')

        plt.title(scaler_name)
        plt.xlabel('Features')
        plt.ylabel('Scaled Value')
        plt.xticks([1, 17, 34, 51, 65])
        plt_index += 1

        if scaler is None:
            plt.legend(
                bbox_to_anchor=(0., 1.2, 2.2, 0.),
                loc=3,
                ncol=4,
                mode='expand',
                borderaxespad=0.,
                handles=[
                    mpatches.Patch(color='red', label='SNR'),
                    mpatches.Patch(color='blue', label='Power'),
                    mpatches.Patch(color='green', label='Distribution'),
                    mpatches.Patch(color='orange', label='Load')
                ]
            )
            plt.title(scaler_name)
            plt.ylabel('Original Value')

    plt.show()


plot_feature_scaling()
#plot_learning_vs_no_learning()
#plot_learning_models_time()
#plot_uncertainties_profiles()
#plot_training_selection()
#plot_offline_activities_time()
#plot_offline_activities()
#for i in range(300, 0, -1):
#    plot_selected_adaptation_options(i)