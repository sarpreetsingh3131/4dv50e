'''
this class converts runtime data to datasets
'''

import json


version = 'v1'  # change version here -> v1, v2
adaptation_cycles = 50  # change here to make big dataset -> 1 to 300

features = []
packet_loss_target = []
latency_target = []

for i in range(1, adaptation_cycles + 1):
    adaptation_cycle = json.load(open('data/runtime/' + version + '/adaptation_cycle_' + str(i) + '.json'))
    for snr, power, packet_distribution, load in zip(
            adaptation_cycle['motes_snr'],
            adaptation_cycle['motes_power'],
            adaptation_cycle['motes_packet_distribution'],
            adaptation_cycle['motes_load']
    ):
        features.append(snr + power + packet_distribution + load)

    for value in adaptation_cycle['packet_loss']:
        packet_loss_target.append(1 if value < 10 else 0)

    for value in adaptation_cycle['latency']:
        latency_target.append(1 if value < 5 else 0)


for dataset_name, target in [
    ('packet_loss', packet_loss_target),
    ('latency', latency_target)
]:
    with open('data/dataset/' + version + '/' + dataset_name + '.json', 'w') as f:
        json.dump({'features': features, 'target': target}, f, indent=2)
