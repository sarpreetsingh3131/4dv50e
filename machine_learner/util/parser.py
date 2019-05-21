'''
this class converts runtime data to datasets
'''

import json


version = 'v1'  # change version here -> v1, v2
cycles = 5  # change here to make big dataset -> 1 to 300

features = []
packet_loss_labels = []
latency_labels = []

for i in range(1, cycles + 1):
    cycle = json.load(open('data/runtime/' + version + '/cycle_' + str(i) + '.json'))
    for snr, power, packet_distribution, load in zip(
            cycle['motes_snr'],
            cycle['motes_power'],
            cycle['motes_packet_distribution'],
            cycle['motes_load']
    ):
        features.append(snr + power + packet_distribution + load)

    for value in cycle['packet_loss']:
        packet_loss_labels.append(1 if value < 10 else 0)

    for value in cycle['latency']:
        latency_labels.append(1 if value < 5 else 0)


for dataset_name, labels in [
    ('packet_loss', packet_loss_labels),
    ('latency', latency_labels)
]:
    with open('data/dataset/' + version + '/' + dataset_name + '.json', 'w') as f:
        json.dump({'features': features, 'labels': labels}, f, indent=2)
