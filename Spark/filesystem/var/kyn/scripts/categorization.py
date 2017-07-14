#!/usr/bin/python

# Copyright (C) 2017 e-ito Technology Services GmbH
# e-mail: info@e-ito.de

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.



################################################################
# Exit codes
# 0: success
# 1: no data return from elasticsearch
# 2: commandline option error
# 3: runtime error (e.g. not enough data for calculation)
################################################################

from __future__ import print_function
import sys
import getopt
import datetime
import re
import json
import math
import numpy
import elasticsearch
from time import strftime
from sklearn.cluster import MeanShift, estimate_bandwidth, KMeans, AgglomerativeClustering, DBSCAN

################################################################

'''
Elasticsearch parameter
'''
nodes = ['http://elasticsearch:9200']
index = "kyn-netflow-*"

################################################################

'''
Clustering parameters
'''
meanshift_bin_seeding=False
meanshift_cluster_all=False
meanshift_n_jobs=-1
meanshift_sample_size=3000
bandwidth_quantile=0.2
bandwidth_n_samples=20000
bandwidth_random_state=0
bandwidth_n_jobs=-1
kmeans_n_clusters=10
agglomerative_n_clusters=10
agglomerative_linkage="ward"
agglomerative_affinity="euclidean"
dbscan_eps=0.3
dbscan_min_samples=10

################################################################

'''
File names and paths
'''
meanshift_outputfile     = '/tmp/meanshift.json'
kmeans_outputfile        = '/tmp/kmeans.json'
agglomerative_outputfile = '/tmp/agglomerative.json'
dbscan_outputfile        = '/tmp/dbscan.json'

################################################################

'''
internals
'''
chatty = True

################################################################

def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

def print_or_quiet(*args, **kwargs):
    global chatty
    if chatty:
        print(*args, **kwargs)

def get_elasticsearch_major_version(es):
    response = es.info()
    try:
        elasticsearch_version = int(response['version']['number'][0])
        if elasticsearch_version != 2 and elasticsearch_version != 5:
            eprint("Unknown Elasticsearch version %d. Exiting ..." % elasticsearch_version)
            sys.exit(3)
        return elasticsearch_version
    except (ValueError, KeyError) as err:
        eprint("Cannot get Elasticsearch version. Exiting ...")
        sys.exit(3)

def query_into_dictionary(es, es_version, start, end, main_dictionary, dictionary, aggregation1, field1, aggregation2, field2):
    query = {
      "size": 0,
      "query": {
        "bool": {
          "must": [
            {
              "wildcard": {
                "TCPFlags": "*F"
              }
            },
            {
              "range": {
                "@timestamp": {
                  "gte": start,
                  "lt": end
                }
              }
            }
          ]
        }
      },
      "aggregations": {
        aggregation1: {
          "terms": {
            "field": field1,
            "size": 2147483647
          },
          "aggregations": {
            aggregation2: {
              "terms": {
                "field": field2,
                "size": 2147483647
              },
              "aggregations": {
                "summe": {
                  "sum": {
                    "field": "Bytes"
                  }
                },
                "pakete": {
                  "sum": {
                    "field": "InputPackets"
                  }
                },
                "sessions": {
                  "value_count": {
                    "field": "Bytes"
                  }
                }
              }
            }
          }
        }
      }
    }

    # Exception will be caught in calling function to adjust steps
    res = es.search(index=index, body = query, request_timeout = 60)

    for i, set1 in enumerate(res['aggregations'][aggregation1]['buckets']):
        key1 = set1['key']
        if key1 not in main_dictionary:
            main_dictionary[key1] = {}
            if es_version == 2:
                main_dictionary[key1]['ip_address']=set1['key_as_string']
            elif es_version == 5:
                main_dictionary[key1]['ip_address']=set1['key']
            else:
                eprint("Unknown Elasticsearch version %d. Exiting ..." % es_version)
                sys.exit(3)
            main_dictionary[key1]['entropy_peer_sessions']=0.0
            main_dictionary[key1]['entropy_peer_bytes']=0.0
            main_dictionary[key1]['entropy_peer_packets']=0.0
            main_dictionary[key1]['entropy_sport_sessions']=0.0
            main_dictionary[key1]['entropy_sport_bytes']=0.0
            main_dictionary[key1]['entropy_sport_packets']=0.0
            main_dictionary[key1]['entropy_dport_sessions']=0.0
            main_dictionary[key1]['entropy_dport_bytes']=0.0
            main_dictionary[key1]['entropy_dport_packets']=0.0

        if key1 not in dictionary:
            dictionary[key1] = {}

        for j, set2 in enumerate(set1[aggregation2]['buckets']):
            key2 = set2['key']
            if key2 not in dictionary[key1]:
                dictionary[key1][key2] = {}
                dictionary[key1][key2]['sessions'] = set2['sessions']['value']
                dictionary[key1][key2]['bytes'] = set2['summe']['value']
                dictionary[key1][key2]['packets'] = set2['pakete']['value']
            else:
                dictionary[key1][key2]['sessions'] += set2['sessions']['value']
                dictionary[key1][key2]['bytes'] += set2['summe']['value']
                dictionary[key1][key2]['packets'] += set2['pakete']['value']

def calculate_sums(main_dictionary, dictionary_name, dictionary, ignore_hosts):
    '''
    there are way better methods to do this in python,
    but I keep it this way for easy change to other language
    '''
    itxt='sum_'+dictionary_name
    itxtsessions = itxt+'_sessions'
    itxtbytes = itxt+'_bytes'
    itxtpackets = itxt+'_packets'
    for a, h in enumerate(main_dictionary):
        try:
            for b, d in enumerate(dictionary[h]):
                try:
                    main_dictionary[h][itxtsessions] += dictionary[h][d]['sessions']
                    main_dictionary[h][itxtbytes]    += dictionary[h][d]['bytes']
                    main_dictionary[h][itxtpackets]  += dictionary[h][d]['packets']
                except KeyError:
                    main_dictionary[h][itxtsessions] = dictionary[h][d]['sessions']
                    main_dictionary[h][itxtbytes]    = dictionary[h][d]['bytes']
                    main_dictionary[h][itxtpackets]  = dictionary[h][d]['packets']
        except KeyError:
            ignore_hosts.append(h);

def calculate_entropy(main_dictionary, dictionaries):
    '''
    there are way better methods to do this in python,
    but I keep it this way for easy change to other language
    '''
    for a, h in enumerate(main_dictionary):
        for b, d in enumerate(dictionaries):
            for c, p in enumerate(d[0][h]):
                try:
                    pi_sessions = float(d[0][h][p]['sessions']) / float(main_dictionary[h]['sum_'+d[1]+'_sessions'])
                except ZeroDivisionError:
                    pi_sessions = 0.0
                try:
                    pi_bytes    = float(d[0][h][p]['bytes'])    / float(main_dictionary[h]['sum_'+d[1]+'_bytes'])
                except ZeroDivisionError:
                    pi_bytes    = 0.0
                try:
                    pi_packets  = float(d[0][h][p]['packets'])  / float(main_dictionary[h]['sum_'+d[1]+'_packets'])
                except ZeroDivisionError:
                    pi_packets  = 0.0
                try:
                    pi_log2pi_sessions = pi_sessions * (math.log(pi_sessions,2))
                except ValueError:
                    pi_log2pi_sessions = 0.0
                try:
                    pi_log2pi_bytes    = pi_bytes    * (math.log(pi_bytes,2))
                except ValueError:
                    pi_log2pi_bytes    = 0.0
                try:
                    pi_log2pi_packets  = pi_packets  * (math.log(pi_packets,2))
                except ValueError:
                    pi_log2pi_packets  = 0.0
                main_dictionary[h]['entropy_'+d[1]+'_sessions'] -= pi_log2pi_sessions
                main_dictionary[h]['entropy_'+d[1]+'_bytes']    -= pi_log2pi_bytes
                main_dictionary[h]['entropy_'+d[1]+'_packets']  -= pi_log2pi_packets

def create_np(main_dictionary, columns):
    '''
    there are way better methods to do this in python,
    but I keep it this way for easy change to other language
    '''
    x = []
    for b, h in enumerate(main_dictionary):
        y = []
        for c in columns:
            y.append(main_dictionary[h][c])
        x.append(y)
    ret = numpy.asarray(x)
    return ret

def main(argv):
    global chatty

    start=""
    end=""
    duration=""
    
    bisection=0
    bisection_max=10
    min_step = datetime.timedelta(minutes=5)

    try:
        opts, args = getopt.getopt(argv,"hqs:d:t:",["help","quiet","start=","duration=","steps="])
    except getopt.GetoptError:
        eprint('Error: Unrecognized option!')
        eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
        eprint('Use -h for help.')
        sys.exit(2)
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            print('categorization.py [-h] [-s <start> -d <duration>]')
            print('  -h:                    print this text')
            print('  -s <start>:            start date and time in elasticsearch time format')
            print('  -d <duration>:         duration format <number>[mhdw]')
            print('  -t <steps>:            steps format <number>[mhdw]')
            print('                         If Elasticsearch query isn\'t responed in time, step size will be automatically adjusted by bisection.')
            print('  -q:                    no output except errors')
            print('  --help:                same as -h')
            print('  --start <start>:       same as -s')
            print('  --duration <duration>: same as -d')
            print('  --steps <steps>:       same as -t')
            print('  --quiet:               same as -q')
            print('')
            print(' If start and duration are omitted, the last 24 hours will be used.')
            sys.exit(0);
        elif opt in ("-q", "--quiet"):
            chatty = False
        elif opt in ("-s", "--start"):
            try:
                start_dt = datetime.datetime.strptime(arg,"%Y-%m-%dT%H:%M")
                start = arg
            except ValueError:
                eprint('Error: Invalid option start!')
                eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                eprint('<start> format: yyyy-mm-ddThh:mm')
                eprint('Use -h for help.')
                sys.exit(2)
        elif opt in ("-d", "--duration"):
            match = re.match("(\d+)([mhdw])$", arg)
            if match:
                (x, c) = match.groups()
                try:
                    y=int(x)
                except:
                    eprint('Error: Invalid option duration!')
                    eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                    eprint('<duration> format: <number>[mhdw]')
                    eprint('Use -h for help.')
                    sys.exit(2)
                if c == 'm':
                    delta = datetime.timedelta(minutes=y)
                elif c == 'h':
                    delta = datetime.timedelta(hours=y)
                elif c == 'd':
                    delta = datetime.timedelta(days=y)
                elif c == 'w':
                    delta = datetime.timedelta(days=(7*y))
                else:
                    eprint('Error: Invalid option duration!')
                    eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                    eprint('<duration> format: <number>[mhdw]')
                    eprint('Use -h for help.')
                    sys.exit(2)
                duration = arg
            else:
                eprint('Error: Invalid option duration!')
                eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                eprint('<duration> format: <number>[mhdw]')
                eprint('Use -h for help.')
                sys.exit(2)
        elif opt in ("-t", "--steps"):
            match = re.match("(\d+)([mhdw])$", arg)
            if match:
                (x, c) = match.groups()
                try:
                    y=int(x)
                except:
                    eprint('Error: Invalid option steps!')
                    eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                    eprint('<duration> format: <number>[mhdw]')
                    eprint('Use -h for help.')
                    sys.exit(2)
                if c == 'm':
                    step = datetime.timedelta(minutes=y)
                elif c == 'h':
                    step = datetime.timedelta(hours=y)
                elif c == 'd':
                    step = datetime.timedelta(days=y)
                elif c == 'w':
                    step = datetime.timedelta(days=(7*y))
                else:
                    eprint('Error: Invalid option steps!')
                    eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                    eprint('<duration> format: <number>[mhdw]')
                    eprint('Use -h for help.')
                    sys.exit(2)
            else:
                eprint('Error: Invalid option steps!')
                eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
                eprint('<steps> format: <number>[mhdw]')
                eprint('Use -h for help.')
                sys.exit(2)

    if ((start == "") and (duration == "")):
        end_dt = datetime.datetime.now()
        end = end_dt.strftime("%Y-%m-%dT%H:%M")
        delta = datetime.timedelta(days=1);
        start_dt = end_dt - delta
        start = start_dt.strftime("%Y-%m-%dT%H:%M")
    elif ((start == "") or (duration == "")):
        eprint('Error: Invalid option combination!')
        eprint('Start and duration must both be specified or omitted.')
        eprint('categorization.py [-h] [-q] [-s <start> -d <duration>] [-t <steps]')
        eprint('Use -h for help.')
        sys.exit(2)
    else:
        end_dt = start_dt + delta
        end = end_dt.strftime("%Y-%m-%dT%H:%M")

    if ('step' not in vars()) :
        step = delta

    if (end_dt - start_dt) < step:
        step = end_dt - start_dt

    es = elasticsearch.Elasticsearch(nodes)

    elasticsearch_version = get_elasticsearch_major_version(es)

    axes = ['entropy_peer_sessions','entropy_peer_packets','entropy_peer_bytes','entropy_sport_sessions','entropy_sport_packets','entropy_sport_bytes','entropy_dport_sessions','entropy_dport_packets','entropy_dport_bytes']

    host = {}
    peer = {}
    sport = {}
    dport = {}

    '''
    During data generation slight differences in timing
    may cause some hosts not to be listed in all dictionaries.
    These hosts will be ignored.
    '''
    ignore_hosts = []

    sets = [['peer (1)', peer, "src", "SourceAddress", "dst", "DestinationAddress"],
            ['peer (2)', peer, "dst", "DestinationAddress", "src", "SourceAddress"],
            ['sport (1)', sport, "src", "SourceAddress", "sport", "SourcePort"],
            ['sport (2)', sport, "dst", "DestinationAddress", "dport", "DestinationPort"],
            ['dport (1)', dport, "src", "SourceAddress", "dport", "DestinationPort"],
            ['dport (2)', dport, "dst", "DestinationAddress", "sport", "SourcePort"]]

    for s in sets:
        moving_start_dt = start_dt
        moving_end_dt = min(start_dt + step, end_dt)
        while True:
            try:
                start = moving_start_dt.strftime("%Y-%m-%dT%H:%M:%S")
                end = moving_end_dt.strftime("%Y-%m-%dT%H:%M:%S")
                print_or_quiet('%s Fetching %s data ... %s - %s' % (strftime('%H:%M:%S'),s[0],start,end))
                query_into_dictionary(es, elasticsearch_version, start, end, host, s[1], s[2], s[3], s[4], s[5])
                moving_start_dt = moving_end_dt
                moving_end_dt += step
                if moving_start_dt >= end_dt:
                    break
                if moving_end_dt > end_dt:
                    moving_end_dt = end_dt
            except elasticsearch.exceptions.ConnectionTimeout as esect:
                bisection += 1
                if bisection <= bisection_max:
                    step = step // 2
                    if step < min_step:
                        eprint('Elasticsearch Connection Timeout. Minimum timeframe reached. Exiting ...')
                        sys.exit(3)
                    eprint('Elasticsearch Connection Timeout. Halving step size.')
                    moving_end_dt = moving_start_dt + step
                else:
                    eprint('%d. time Elasticsearch Connection Timeout. Exiting ...' % bisection)
                    sys.exit(3)

    print_or_quiet('%s Calculating sums ...' % (strftime('%H:%M:%S')))
    calculate_sums(host, 'peer', peer, ignore_hosts)
    calculate_sums(host, 'sport', sport, ignore_hosts)
    calculate_sums(host, 'dport', dport, ignore_hosts)

    print_or_quiet('%s Removing incomplete hosts ...' % (strftime('%H:%M:%S')))
    for h in ignore_hosts:
        try:
            del host[h]
        except KeyError:
            # host may appear more than once
            pass

    print_or_quiet('%s Calculating entropy ...' % (strftime('%H:%M:%S')))
    calculate_entropy(host, [[peer,'peer'],[dport,'dport'],[sport,'sport']])

    print_or_quiet('%s Removing dictionaries ...' % (strftime('%H:%M:%S')))
    peer.clear()
    sport.clear()
    dport.clear()

    if (len(host) == 0):
        eprint("No data found. Exiting ...")
        sys.exit(1)

    print_or_quiet('%s Creating sample set ...' % (strftime('%H:%M:%S')))
    labels = host.keys();
    npa = create_np(host, axes);
    n_samples, n_features = npa.shape
    print_or_quiet('          samples: %d features:%d' % (n_samples, n_features))

    #########################
    # MeanShift
    #########################
    print_or_quiet('%s Calculating bandwidth ...' % (strftime('%H:%M:%S')))
    bandwidth = estimate_bandwidth(npa, quantile=bandwidth_quantile, n_samples=bandwidth_n_samples,
            random_state=bandwidth_random_state, n_jobs=bandwidth_n_jobs)

    if (bandwidth == 0.00000000):
        eprint('Useless bandwith. Exiting ....')
        sys.exit(3);

    print_or_quiet('%s Calculating MeanShift ...' % (strftime('%H:%M:%S')))
    ms = MeanShift(bandwidth=bandwidth, bin_seeding=meanshift_bin_seeding, cluster_all=meanshift_cluster_all, n_jobs=meanshift_n_jobs)
    ms.fit(npa, npa.shape)
    n_clusters = len(numpy.unique(ms.labels_))

    print_or_quiet('%s Getting predictions ...' % (strftime('%H:%M:%S')))
    prediction = ms.predict(npa)
    ip = [y['ip_address'] for y in host.values()]
    ip_prediction = zip(ip, prediction)

    meanshift_output = {}
    for (ip,prediction) in ip_prediction:
        meanshift_output.setdefault(prediction,list()).append(ip)

    print_or_quiet('%s Writing MeanShift output file ...' % (strftime('%H:%M:%S')))
    with open(meanshift_outputfile, 'w') as fp:
        json.dump({str(key): value for key, value in meanshift_output.iteritems()}, fp)

    #########################
    # KMeans
    #########################
    print_or_quiet('%s Calculating KMeans ...' % (strftime('%H:%M:%S')))
    km = KMeans(n_clusters=kmeans_n_clusters)
    km.fit(npa, npa.shape)

    print_or_quiet('%s Getting predictions ...' % (strftime('%H:%M:%S')))
    prediction = km.predict(npa)
    ip = [y['ip_address'] for y in host.values()]
    ip_prediction = zip(ip, prediction)

    kmeans_output = {}
    for (ip,prediction) in ip_prediction:
        kmeans_output.setdefault(prediction,list()).append(ip)

    print_or_quiet('%s Writing KMeans output file ...' % (strftime('%H:%M:%S')))
    with open(kmeans_outputfile, 'w') as fp:
        json.dump({str(key): value for key, value in kmeans_output.iteritems()}, fp)

    #########################
    # AgglomerativeClustering
    #########################
    print_or_quiet('%s Calculating Agglomerative Clustering ...' % (strftime('%H:%M:%S')))
    ac = AgglomerativeClustering(n_clusters=agglomerative_n_clusters, affinity=agglomerative_affinity,
            linkage=agglomerative_linkage)

    prediction = ac.fit_predict(npa)
    ip = [y['ip_address'] for y in host.values()]
    ip_prediction = zip(ip, prediction)

    agglomerative_output = {}
    for (ip,prediction) in ip_prediction:
        agglomerative_output.setdefault(prediction,list()).append(ip)

    print_or_quiet('%s Writing Agglomerative Clustering output file ...' % (strftime('%H:%M:%S')))
    with open(agglomerative_outputfile, 'w') as fp:
        json.dump({str(key): value for key, value in agglomerative_output.iteritems()}, fp)

    #########################
    # DBSCAN
    #########################
    print_or_quiet('%s Calculating DBSCAN ...' % (strftime('%H:%M:%S')))
    db = DBSCAN(eps=dbscan_eps, min_samples=dbscan_min_samples)

    prediction = db.fit_predict(npa)
    ip = [y['ip_address'] for y in host.values()]
    ip_prediction = zip(ip, prediction)

    dbscan_output = {}
    for (ip,prediction) in ip_prediction:
        dbscan_output.setdefault(prediction,list()).append(ip)

    print_or_quiet('%s Writing DBSCAN output file ...' % (strftime('%H:%M:%S')))
    with open(dbscan_outputfile, 'w') as fp:
        json.dump({str(key): value for key, value in dbscan_output.iteritems()}, fp)

if __name__ == "__main__":
    main(sys.argv[1:])
