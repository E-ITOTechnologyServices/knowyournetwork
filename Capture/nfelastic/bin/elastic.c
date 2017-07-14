/*
 * elastic.c
 *
 *  Created on: Feb 16, 2017
 *      Author: zzh3x2
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <inttypes.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <curl/curl.h>
#include <jansson.h>
#include <pthread.h>

#include "util.h"
#include "elastic.h"

char *elasticsearch_url = NULL;
char *elasticsearch_index = NULL;
char *elasticsearch_whitelistindex = NULL;
char *elasticsearch_knownnetworksindex = NULL;
char *elasticsearch_searchurl = NULL;
char *elasticsearch_whitelisturl = NULL;
char *elasticsearch_knownnetworksurl = NULL;
char *elasticsearch_bulkurl = NULL;

whitelist_t *whitelist = NULL;
int whitelist_counter = 0;
known_network_t *knownnetworks = NULL;
int knownnetworks_counter = 0;

int output_to_elastic_db = 0;

struct curl_data {
	char *memory;
	size_t size;
};

struct thread_memory {
	pthread_t thread;
	int *thread_return_value;
	char * bulkdata;
	size_t bulksize;
};

struct thread_memory *tm;
struct thread_memory next_thread;
int current_thread = 0;
long max_threads = 8;

void LogError(char *format, ...);

void init_thread(struct thread_memory * thread);
size_t write_callback(char *content, size_t size, size_t nmemb, void *userdata);
void * send_elasticsearch_bulk_data(void *arg);

size_t write_callback(char *content, size_t size, size_t nmemb, void *userdata) {
	size_t realsize = size * nmemb;
	struct curl_data *mem = (struct curl_data *)userdata;

	mem->memory = realloc(mem->memory, mem->size + realsize + 1);
	if(mem->memory == NULL) {
		/* out of memory! */
		LogError("not enough memory (realloc returned NULL)\n");
		return 0;
	}

	memcpy(&(mem->memory[mem->size]), content, realsize);
	mem->size += realsize;
	mem->memory[mem->size] = 0;

	return realsize;
}

void init_thread(struct thread_memory * thread) {
	thread->thread = 0;
	thread->bulkdata = NULL;
	thread->bulksize = 0;
	thread->thread_return_value = NULL;
}

void init_elasticsearch(void) {
	int i, errno;
	long nprocs = -1;

#ifdef _WIN32
#ifndef _SC_NPROCESSORS_ONLN
	SYSTEM_INFO info;
	GetSystemInfo(&info);
#define sysconf(a) info.dwNumberOfProcessors
#define _SC_NPROCESSORS_ONLN
#endif
#endif
#ifdef _SC_NPROCESSORS_ONLN
	nprocs = sysconf(_SC_NPROCESSORS_ONLN);
	if (nprocs < 1)
		fprintf(stderr, "Could not determine number of CPUs online:\n%s\n", strerror (errno));
#endif

	if (nprocs > 2)
		max_threads = (nprocs-1)*2;

	tm = (struct thread_memory *)malloc(max_threads * sizeof(struct thread_memory));
	for (i = 0; i < max_threads; i++)
		init_thread(&(tm[i]));
	init_thread(&next_thread);

	curl_global_init(CURL_GLOBAL_ALL);
}

void cleanup_elasticsearch(void)
{
	curl_global_cleanup();
}

void set_elasticsearch_parameter(const char * url, const char * searchindex, const char * whitelistindex,
		const char * knownnetworksindex, const int elastic_db) {
	char *index_default = "kyn-netflow";

	if (url != NULL) {
		size_t needed;
		elasticsearch_url = (char *)url;

		if (searchindex == NULL)
			searchindex = index_default;

		elasticsearch_index = (char *)searchindex;

		needed = snprintf(NULL, 0, "%s/%s-*/_search",url,searchindex) + 1;
		elasticsearch_searchurl = malloc(needed);
		snprintf(elasticsearch_searchurl, needed, "%s/%s-*/_search",url,searchindex);

		needed = snprintf(NULL, 0, "%s/_bulk",url) + 1;
		elasticsearch_bulkurl = malloc(needed);
		snprintf(elasticsearch_bulkurl, needed, "%s/_bulk",url);

		output_to_elastic_db = elastic_db;

		if (knownnetworksindex) {
			elasticsearch_knownnetworksindex = (char *)knownnetworksindex;

			needed = snprintf(NULL, 0, "%s/%s/_search",url,knownnetworksindex) + 1;
			elasticsearch_knownnetworksurl = malloc(needed);
			snprintf(elasticsearch_knownnetworksurl, needed, "%s/%s/_search",url,knownnetworksindex);
		}
		if (whitelistindex) {
			elasticsearch_whitelistindex = (char *)whitelistindex;

			needed = snprintf(NULL, 0, "%s/%s/_search",url,whitelistindex) + 1;
			elasticsearch_whitelisturl = malloc(needed);
			snprintf(elasticsearch_whitelisturl, needed, "%s/%s/_search",url,whitelistindex);
		}
	} else {
		elasticsearch_url = NULL;
		elasticsearch_index = NULL;
		elasticsearch_whitelistindex = NULL;
		elasticsearch_knownnetworksindex = NULL;
		if (elasticsearch_searchurl) {
			free(elasticsearch_searchurl);
			elasticsearch_searchurl = NULL;
		}
		if (elasticsearch_whitelisturl) {
			free(elasticsearch_whitelisturl);
			elasticsearch_whitelisturl = NULL;
		}
		if (elasticsearch_knownnetworksurl) {
			free(elasticsearch_knownnetworksurl);
			elasticsearch_knownnetworksurl = NULL;
		}
		if (elasticsearch_bulkurl) {
			free(elasticsearch_bulkurl);
			elasticsearch_bulkurl = NULL;
		}
		output_to_elastic_db = 0;
	}
}

char * get_elasticsearch_index (void) {
	return elasticsearch_index;
}

int get_output_to_elastic_db(void) {
	return output_to_elastic_db;
}

int get_elasticsearch_major_version(void) {
	json_t *root, *version, *number;
	json_error_t error;
	char *number_str, *token;

	int ret = 0;

	CURL *curl;
	CURLcode res;

	struct curl_data chunk;

	if (elasticsearch_url != NULL) {
		chunk.memory = malloc(1);
		chunk.size = 0;

		curl = curl_easy_init();
		if (curl) {
			curl_easy_setopt(curl, CURLOPT_URL, elasticsearch_url);
			curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
			curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
			curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

			res = curl_easy_perform(curl);
			if(res == CURLE_OK) {
				root = json_loads(chunk.memory, 0, &error);

				free(chunk.memory);

				if (root) {
					if(json_is_object(root)) {
						version = json_object_get(root, "version");
						if(json_is_object(version)){
							number = json_object_get(version, "number");
							if(json_is_string(number)) {
								number_str = (char *)json_string_value(number);
								token = strtok(number_str, ".");
								ret = atoi(token);
							} else {
								LogError("number isn't a string.\n");
							}
						} else {
							LogError("version isn't an object.\n");
						}
					} else {
						LogError("root isn't an object.\n");
					}
					json_decref(root);
				} else {
					LogError("Couldn't load JSON.\n");
				}
			} else {
				LogError("%s. URL = %s\n", curl_easy_strerror(res), elasticsearch_url);
			}
			curl_easy_cleanup(curl);
		} else {
			LogError("Cannot setup curl object.\n");
		}
	}
	return ret;
}

int get_elasticsearch_peer_count(const char *ip, const int keep_connection) {
	json_t *root, *aggregations, *count, *value, *status_object, *error_object, *type;
	json_error_t error;

	const char * type_text;

	int ret = 0;

	static char *field[2];
	field[0] = "SourceAddress";
	field[1] = "DestinationAddress";
	int step = 0;

	static CURL *curl = NULL;
	CURLcode res;
	static char *datatemplate = "{\"size\":0,"
			"\"query\":{\"bool\":{\"must\":[{\"term\":{\"%s\":\"%s\"}}]}},"
			"\"aggs\":{\"dest_count\":{\"cardinality\":{\"field\":\"%s\"}}}}";
	char data[512];

	struct curl_data chunk;

	int retry = 60;
	int status;

	if (elasticsearch_searchurl) {
		if (!curl) {
			curl = curl_easy_init();
		}

		if (curl) {
			while (retry > 0) {
				for (step = 0; step < 2; step++){
					chunk.memory = malloc(1);
					chunk.size = 0;

					sprintf(data, datatemplate, field[step], ip, field[(step+1)%2]);
					curl_easy_setopt(curl, CURLOPT_URL, elasticsearch_searchurl);
					curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
					curl_easy_setopt(curl, CURLOPT_POST, 1L);
					curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
					curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
					curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

					res = curl_easy_perform(curl);
					if(res == CURLE_OK) {
						root = json_loads(chunk.memory, 0, &error);

						free(chunk.memory);

						if (root) {
							if(json_is_object(root)) {
								aggregations = json_object_get(root, "aggregations");
								if(json_is_object(aggregations)) {
									count = json_object_get(aggregations, "dest_count");
									if(json_is_object(count)) {
										value = json_object_get(count, "value");
										if(json_is_integer(value)) {
											ret += json_integer_value(value);
											retry = 0;
										} else {
											LogError("value isn't an integer.\n");
										}
									} else {
										LogError("count isn't an object.\n");
									}
								} else {
									error_object = json_object_get(root, "error");
									if (json_is_object(error_object)) {
										status_object = json_object_get(root, "status");
										if (json_is_integer(status_object)) {
											status = json_integer_value(status_object);

											if (status == 503) {
												LogError("Elasticsearch aggregations not ready yet.\n");
												LogError("Retry in 1 second. (%d)\n", retry);
												retry--;
												sleep(1);
												continue;
											} else if (status == 404) {
												type = json_object_get(error_object, "type");
												if (json_is_string(type)) {
													type_text = json_string_value(type);
													if (strncmp(type_text,"index_not_found_exception", 25)) {
														LogError("Unhandled Elasticsearch error: %s.\n", type_text);
													}
													ret = -1;
													retry = 0;
													break;
												}
											}
										}
									}
									LogError("aggregations isn't an object.\n");
									retry = 0;
									ret = -1;
								}
							} else {
								LogError("root isn't an object.\n");
							}
							json_decref(root);
						} else {
							LogError("Couldn't load JSON. %d.%d: %s\n", error.line, error.position, error.text);
						}
					} else {
						LogError("%s. URL = %s\n", curl_easy_strerror(res), elasticsearch_url);
					}
				}
			}
			if (!keep_connection) {
				curl_easy_cleanup(curl);
				curl = NULL;
			}
		} else {
			LogError("Cannot setup curl object.\n");
		}
	}
	return ret;
}

int get_elasticsearch_whitelist() {
	json_t *root, *hits, *total, *hits_array, *value, *source, *startip, *endip, *startport, *endport;
	json_t *protocol, *text, *error_object, *status_object, *type;
	json_error_t error;

	uint32_t size = 0;
	uint32_t n_ip4;
	uint64_t n_ip6[2];
	size_t indx, len;

	const char *ipstring;
	const char *text_string;
	const char *type_text;

	CURL *curl = NULL;
	CURLcode res;
	static char *datatemplate = "{\"size\":10000,\"query\":{\"match_all\":{}}}";

	struct curl_data chunk;

	int status;
	int retry = 60;
	int ret = -1;

	if (elasticsearch_whitelisturl) {
		curl = curl_easy_init();

		if (curl) {
			while (retry > 0) {
				chunk.memory = malloc(1);
				chunk.size = 0;

				curl_easy_setopt(curl, CURLOPT_URL, elasticsearch_whitelisturl);
				curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
				curl_easy_setopt(curl, CURLOPT_POST, 1L);
				curl_easy_setopt(curl, CURLOPT_POSTFIELDS, datatemplate);
				curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
				curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

				res = curl_easy_perform(curl);
				if (res == CURLE_OK) {
					root = json_loads(chunk.memory, 0, &error);

					free(chunk.memory);

					if (root) {
						if (json_is_object(root)) {
							hits = json_object_get(root, "hits");
							if (json_is_object(hits)) {
								total = json_object_get(hits, "total");
								if (json_is_integer(total)) {
									size = json_integer_value(total);
									whitelist = (whitelist_t *)malloc(sizeof(whitelist_t)*(size + 1));
									memset(whitelist,0,sizeof(whitelist_t)*(size + 1));
									if (whitelist != NULL) {
										hits_array = json_object_get(hits, "hits");
										if (json_is_array(hits_array)){
											json_array_foreach(hits_array, indx, value) {
												if ((indx < size) && json_is_object(value)) {
													source = json_object_get(value, "_source");
													if (json_is_object(source)) {
														startip = json_object_get(source,"StartIPAddress");
														if (!json_is_string(startip)) {
															LogError("StartIPAddress isn't a string.\n");
															continue;
														}
														endip = json_object_get(source,"EndIPAddress");
														if (!json_is_string(endip)) {
															LogError("EndIPAddress isn't a string.\n");
															continue;
														}
														startport = json_object_get(source,"StartPort");
														if (!json_is_integer(startport)) {
															LogError("StartPort isn't an integer.\n");
															continue;
														}
														endport = json_object_get(source,"EndPort");
														if (!json_is_integer(endport)) {
															LogError("EndPort isn't an integer.\n");
															continue;
														}
														protocol = json_object_get(source,"Protocol");
														if (!json_is_integer(protocol)) {
															LogError("Protocol isn't an integer.\n");
															continue;
														}
														text = json_object_get(source,"Text");
														if (!json_is_string(text)) {
															LogError("Text isn't a string.\n");
															continue;
														}
														ipstring = json_string_value(startip);
														if (strchr(ipstring, ':')) {
															inet_pton(AF_INET6, ipstring, n_ip6);
															whitelist[indx].ip_union._v6.startaddr[0] = ntohll(n_ip6[0]);
															whitelist[indx].ip_union._v6.startaddr[1] = ntohll(n_ip6[1]);
														} else {
															inet_pton(AF_INET, ipstring, &n_ip4);
															whitelist[indx].ip_union._v4.startaddr = ntohl(n_ip4);
														}
														ipstring = json_string_value(endip);
														if (strchr(ipstring, ':')) {
															inet_pton(AF_INET6, ipstring, n_ip6);
															whitelist[indx].ip_union._v6.endaddr[0] = ntohll(n_ip6[0]);
															whitelist[indx].ip_union._v6.endaddr[1] = ntohll(n_ip6[1]);
														} else {
															inet_pton(AF_INET, ipstring, &n_ip4);
															whitelist[indx].ip_union._v4.endaddr = ntohl(n_ip4);
														}
														whitelist[indx].startport = json_integer_value(startport);
														whitelist[indx].endport = json_integer_value(endport);
														whitelist[indx].protocol = json_integer_value(protocol);
														text_string = json_string_value(text);
														len = strlen(text_string);
														whitelist[indx].text = malloc(len + 1);
														if (whitelist[indx].text != NULL) {
															strncpy(whitelist[indx].text, text_string, len);
															whitelist[indx].text[len] = '\0';
														} else {
															LogError("not enough memory (malloc returned NULL)\n");
															break;
														}
														whitelist_counter++;
													} else {
														LogError("_source isn't an object.\n");
													}
												} else {
													LogError("Too many/Not enough entries in whitelist array.\n");
													break;
												}
											}
											retry = 0;
											ret = size;
										} else {
											LogError("hits isn't an array.\n");
										}
									} else {
										LogError("not enough memory (malloc returned NULL)\n");
									}
								} else {
									LogError("total isn't an integer.\n");
								}
							} else {
								error_object = json_object_get(root, "error");
								if (json_is_object(error_object)) {
									status_object = json_object_get(root, "status");
									if (json_is_integer(status_object)) {
										status = json_integer_value(status_object);

										if (status == 503) {
											LogError("Elasticsearch query not ready yet.\n");
											LogError("Retry in 1 second. (%d)\n", retry);
											retry--;
											sleep(1);
											continue;
										} else if (status == 404) {
											type = json_object_get(error_object, "type");
											if (json_is_string(type)) {
												type_text = json_string_value(type);
												if (strncmp(type_text,"index_not_found_exception", 25)) {
													LogError("Unhandled Elasticsearch error: %s.\n", type_text);
												}
												ret = -1;
												retry = 0;
												break;
											}
										}
									}
								}
								LogError("hits isn't an object.\n");
								retry = 0;
							}
						} else {
							LogError("root isn't an object.\n");
						}
						json_decref(root);
					} else {
						LogError("Couldn't load JSON. %d.%d: %s\n", error.line, error.position, error.text);
					}
				} else {
					LogError("%s. URL = %s\n", curl_easy_strerror(res), elasticsearch_url);
				}
			}
			curl_easy_cleanup(curl);
		} else {
			LogError("Cannot setup curl object.\n");
		}
	}

	return ret;
}

int get_elasticsearch_knownnetworks() {
	json_t *root, *hits, *total, *hits_array, *value, *source, *startip, *endip, *description, *error_object, *status_object, *type;
	json_error_t error;

	uint32_t size = 0;
	uint32_t n_ip4;
	uint64_t n_ip6[2];
	size_t index, len;

	const char *ipstring;
	const char *text_string;
	const char *type_text;

	CURL *curl = NULL;
	CURLcode res;
	static char *datatemplate = "{\"size\":10000,\"query\":{\"match_all\":{}}}";

	struct curl_data chunk;

	int status;
	int retry = 60;
	int ret = 1;

	if (elasticsearch_knownnetworksurl) {
		curl = curl_easy_init();

		if (curl) {
			while (retry > 0) {
				chunk.memory = malloc(1);
				chunk.size = 0;

				curl_easy_setopt(curl, CURLOPT_URL, elasticsearch_knownnetworksurl);
				curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
				curl_easy_setopt(curl, CURLOPT_POST, 1L);
				curl_easy_setopt(curl, CURLOPT_POSTFIELDS, datatemplate);
				curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
				curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

				res = curl_easy_perform(curl);
				if (res == CURLE_OK) {
					root = json_loads(chunk.memory, 0, &error);

					free(chunk.memory);

					if (root) {
						if (json_is_object(root)) {
							hits = json_object_get(root, "hits");
							if (json_is_object(hits)) {
								total = json_object_get(hits, "total");
								if (json_is_integer(total)) {
									size = json_integer_value(total);
									knownnetworks = (known_network_t *)malloc(sizeof(known_network_t)*(size + 1));
									memset(knownnetworks,0,sizeof(known_network_t)*(size + 1));
									if (knownnetworks != NULL) {
										hits_array = json_object_get(hits, "hits");
										if (json_is_array(hits_array)){
											json_array_foreach(hits_array, index, value) {
												if ((index < size) && json_is_object(value)) {
													source = json_object_get(value, "_source");
													if (json_is_object(source)) {
														startip = json_object_get(source,"StartIPAddress");
														if (!json_is_string(startip)) {
															LogError("StartIPAddress isn't a string.\n");
															continue;
														}
														endip = json_object_get(source,"EndIPAddress");
														if (!json_is_string(endip)) {
															LogError("EndIPAddress isn't a string.\n");
															continue;
														}
														description = json_object_get(source,"Description");
														if (!json_is_string(description)) {
															LogError("Description isn't a string.\n");
															continue;
														}
														ipstring = json_string_value(startip);
														if (strchr(ipstring, ':')) {
															inet_pton(AF_INET6, ipstring, n_ip6);
															knownnetworks[index].ip_union._v6.startaddr[0] = ntohll(n_ip6[0]);
															knownnetworks[index].ip_union._v6.startaddr[1] = ntohll(n_ip6[1]);
														} else {
															inet_pton(AF_INET, ipstring, &n_ip4);
															knownnetworks[index].ip_union._v4.startaddr = ntohl(n_ip4);
														}
														ipstring = json_string_value(endip);
														if (strchr(ipstring, ':')) {
															inet_pton(AF_INET6, ipstring, n_ip6);
															knownnetworks[index].ip_union._v6.endaddr[0] = ntohll(n_ip6[0]);
															knownnetworks[index].ip_union._v6.endaddr[1] = ntohll(n_ip6[1]);
														} else {
															inet_pton(AF_INET, ipstring, &n_ip4);
															knownnetworks[index].ip_union._v4.endaddr = ntohl(n_ip4);
														}
														text_string = json_string_value(description);
														len = strlen(text_string);
														knownnetworks[index].description = malloc(len + 1);
														if (knownnetworks[index].description != NULL) {
															strncpy(knownnetworks[index].description, text_string, len);
															knownnetworks[index].description[len] = '\0';
														} else {
															LogError("not enough memory (malloc returned NULL)\n");
															break;
														}
														knownnetworks_counter++;
													} else {
														LogError("_source isn't an object.\n");
													}
												} else {
													LogError("Too many/Not enough entries in knownnetworks array.\n");
													break;
												}
											}
											retry = 0;
											ret = size;
										} else {
											LogError("hits isn't an array.\n");
										}
									} else {
										LogError("not enough memory (malloc returned NULL)\n");
									}
								} else {
									LogError("total isn't an integer.\n");
								}
							} else {
								error_object = json_object_get(root, "error");
								if (json_is_object(error_object)) {
									status_object = json_object_get(root, "status");
									if (json_is_integer(status_object)) {
										status = json_integer_value(status_object);

										if (status == 503) {
											LogError("Elasticsearch query not ready yet.\n");
											LogError("Retry in 1 second. (%d)\n", retry);
											retry--;
											sleep(1);
											continue;
										} else if (status == 404) {
											type = json_object_get(error_object, "type");
											if (json_is_string(type)) {
												type_text = json_string_value(type);
												if (strncmp(type_text,"index_not_found_exception", 25)) {
													LogError("Unhandled Elasticsearch error: %s.\n", type_text);
												}
												ret = -1;
												retry = 0;
												break;
											}
										}
									}
								}
								LogError("hits isn't an object.\n");
								retry = 0;
							}
						} else {
							LogError("root isn't an object.\n");
						}
						json_decref(root);
					} else {
						LogError("Couldn't load JSON. %d.%d: %s\n", error.line, error.position, error.text);
					}
				} else {
					LogError("%s. URL = %s\n", curl_easy_strerror(res), elasticsearch_url);
				}
			}
			curl_easy_cleanup(curl);
		} else {
			LogError("Cannot setup curl object.\n");
		}
	}

	return ret;
}

size_t collect_and_send_elasticsearch_bulk_data(const char * data) {
	void *new_block;
	size_t needed;
	size_t documents;
	int err;

	if (data != NULL) {
		needed = snprintf(NULL, 0, "%s\n",data);
		new_block = realloc((void *)next_thread.bulkdata, next_thread.bulksize + needed + 1);

		if (new_block == NULL) {
			LogError("not enough memory (realloc returned NULL)\n");
			return ULONG_MAX;
		}

		next_thread.bulkdata = (char *)new_block;
		snprintf(next_thread.bulkdata+next_thread.bulksize, needed + 1, "%s\n",data);
		next_thread.bulksize += needed;
	}

	if ((data == NULL) || (next_thread.bulksize >= ELASTICSEARCH_MAX_BULK_SIZE)) {
		if (next_thread.bulkdata != NULL) {
			current_thread = (current_thread + 1) % max_threads;

			if (tm[current_thread].thread != 0) {
				pthread_join(tm[current_thread].thread, (void **)&(tm[current_thread].thread_return_value));
				free(tm[current_thread].bulkdata);
				init_thread(&(tm[current_thread]));
			}

			tm[current_thread].bulkdata = next_thread.bulkdata;
			tm[current_thread].bulksize = next_thread.bulksize;

			err = pthread_create(&(tm[current_thread].thread), NULL, &send_elasticsearch_bulk_data, (void *)&(tm[current_thread]));
			if (err != 0) {
				LogError("Can't create thread: %s\n", strerror(err));
				exit(255);
			}

			init_thread(&next_thread);
		}
	}

	return next_thread.bulksize;
}

void * send_elasticsearch_bulk_data(void *arg) {
	size_t documents = 0;

	json_t *root, *errors, *items;
	json_error_t error;

	int ret = 0;
	char * bulkdata = ((struct thread_memory *)arg)->bulkdata;
	size_t bulksize = ((struct thread_memory *)arg)->bulksize;

	CURL *curl = NULL;
	CURLcode res;
	struct curl_data chunk;
	struct curl_slist *list = NULL;

	if (elasticsearch_bulkurl) {
		curl = curl_easy_init();

		if (curl) {
			chunk.memory = malloc(1);
			chunk.size = 0;

			curl_easy_setopt(curl, CURLOPT_URL, elasticsearch_bulkurl);
			list = curl_slist_append(list, "Content-Type: application/x-ndjson");
			curl_easy_setopt(curl, CURLOPT_HTTPHEADER, list);
			curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
			curl_easy_setopt(curl, CURLOPT_POST, 1L);
			curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE_LARGE, bulksize);
			curl_easy_setopt(curl, CURLOPT_POSTFIELDS, bulkdata);
			curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
			curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

			res = curl_easy_perform(curl);
			curl_slist_free_all(list);
			if(res == CURLE_OK) {
				root = json_loads(chunk.memory, 0, &error);

				free(chunk.memory);

				if (root) {
					if (json_is_object(root)) {
						errors = json_object_get(root, "errors");
						if (json_is_boolean(errors)) {
							if (json_is_false(errors)){
								items = json_object_get(root, "items");
								if (json_is_array(items)) {
									documents = json_array_size(items);
								} else {
									LogError("items isn't an array.\n");
								}
							} else {
								LogError("error isn't a boolean object.\n");
							}
						} else {
							LogError("error isn't a boolean object.\n");
						}
					} else {
						LogError("root isn't an object.\n");
					}
					json_decref(root);
				}
			} else {
				LogError("%s. URL = %s\n", curl_easy_strerror(res), elasticsearch_bulkurl);
			}
			curl_easy_cleanup(curl);
		} else {
			LogError("Cannot setup curl object.\n");
		}
	}

	return NULL;
}

void wait_for_elasticsearch_threads(void) {
	int i;

	for(i=0; i<max_threads; i++) {
		if (tm[i].thread != 0) {
			pthread_join(tm[i].thread, (void **)&(tm[i].thread_return_value));
			free(tm[i].bulkdata);
		}
	}
}

