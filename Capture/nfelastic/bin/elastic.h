/*
 * elastic.h
 *
 *  Created on: Feb 16, 2017
 *      Author: zzh3x2
 */

#ifndef ELASTIC_H_
#define ELASTIC_H_

#define ELASTICSEARCH_v2 2
#define ELASTICSEARCH_v5 5
#define ELASTICSEARCH_MAX_BULK_SIZE 5242880 // 5MB

typedef struct whitelist_s {
	union {
		struct {
#ifdef WORDS_BIGENDIAN
			uint32_t	fill1[3];
			uint32_t	startaddr;
			uint32_t	fill2[3];
			uint32_t	endaddr;
#else
			uint32_t	fill1[2];
			uint32_t	startaddr;
			uint32_t	fill2;
			uint32_t	fill3[2];
			uint32_t	endaddr;
			uint32_t	fill4;
#endif
		} _v4;
		struct {
			uint64_t	startaddr[2];
			uint64_t	endaddr[2];
		} _v6;
	} ip_union;
	uint16_t	startport;
	uint16_t	endport;
	uint8_t		protocol;
	char 		*text;
} whitelist_t;

typedef struct known_network_s {
	char * description;
	union {
		struct {
#ifdef WORDS_BIGENDIAN
			uint32_t	fill1[3];
			uint32_t	startaddr;
			uint32_t	fill2[3];
			uint32_t	endaddr;
#else
			uint32_t	fill1[2];
			uint32_t	startaddr;
			uint32_t	fill2;
			uint32_t	fill3[2];
			uint32_t	endaddr;
			uint32_t	fill4;
#endif
		} _v4;
		struct {
			uint64_t	startaddr[2];
			uint64_t	endaddr[2];
		} _v6;
	} ip_union;
} known_network_t;

extern whitelist_t *whitelist;
extern int whitelist_counter;
extern known_network_t *knownnetworks;
extern int knownnetworks_counter;

void init_elasticsearch(void);
void cleanup_elasticsearch(void);
void set_elasticsearch_parameter(const char * url, const char * searchindex, const char * whitelistindex,
		const char * knownnetworksindex, const int elastic_db);
int get_elasticsearch_major_version(void);
int get_elasticsearch_peer_count(const char *ip, const int keep_connection);
int get_elasticsearch_whitelist(void);
int get_elasticsearch_knownnetworks(void);
char * get_elasticsearch_index(void);
int get_output_to_elastic_db(void);
size_t collect_and_send_elasticsearch_bulk_data(const char * data);
void wait_for_elasticsearch_threads(void);

#endif /* ELASTIC_H_ */
