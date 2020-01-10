#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>
#include<sys/mman.h>

#define ENV_VAR_NAME "SRSV_LAB5"

mqd_t mq;
struct mq_attr mq_attr;
char* MQ_NAME;
void *mq_buffer;
void setup_message_queue(char *NAME);

struct Shm_shared {
	pthread_mutex_t mutex;
	int last_given_id;
};
int shm;
struct Shm_shared *shm_id;
char* SHM_NAME;
void setup_shared_memory_for_id(char *NAME);

int reserve_ids(int jobs_count);

void shutdown(void);

int main(int argc, char *argv[]){
	int jobs_count;
	int max_job_duration;
	
	char* NAME = getenv(ENV_VAR_NAME);

	int id_start = 0;

	//Get arguments values
	if (argc < 3){
		fprintf(stderr, "Two arguments required!\n");
		exit(1);
	}
	jobs_count = atoi(argv[1]);
	max_job_duration = atoi(argv[2]);
	
	if (NAME == NULL || strlen(NAME) <= 0){
		fprintf(stderr, "No message queue name defined!\n");
		exit(1);
	}
	
	setup_message_queue(NAME);
	setup_shared_memory_for_id(NAME);

	id_start = reserve_ids(jobs_count);

	printf("%d %d %d\n", jobs_count, max_job_duration, id_start);
	sleep(1);

	shutdown();
	
	printf("Bye!\n");
	return 0;
}

/*
Does setup of message queue. Creates one if not exists, does linking only if exists.
*/
void setup_message_queue(char *NAME){
	MQ_NAME = calloc(strlen(NAME) + 2, sizeof(char));
	strncpy(MQ_NAME, "/\0", 2);
	strncat(MQ_NAME, NAME, strlen(NAME));

	mq = mq_open(MQ_NAME, O_RDWR | O_NONBLOCK | O_CREAT | O_EXCL, 00600, NULL);
	if (mq == (mqd_t) -1){ //Already exists, open it.
		mq = mq_open(MQ_NAME, O_RDWR | O_NONBLOCK);
		if (mq == (mqd_t) -1){
			perror("Failed to open message queue:\n");
			exit(1);
		} else {
			printf("Opened message queue.\n");
		}
	} else { //Not exists, create one
		if (mq_getattr(mq, &mq_attr) == -1){
			perror("Failed to get attribute of message queue:\n");
		}
		mq_buffer = malloc(mq_attr.mq_msgsize);
		if (mq_buffer == NULL){
			perror("Failed to allocate memory for message buffer:\n");
		}
		printf("Created message queue.\n");
	}
}

/*
Does setup of shared memory for getting IDs. Creates one if not exists, does linking only if exists.
*/
void setup_shared_memory_for_id(char *NAME) {
	SHM_NAME = calloc(strlen(NAME) + 2, sizeof(char));
	strncpy(SHM_NAME, "/\0", 2);
	strncat(SHM_NAME, NAME, strlen(NAME));
	shm = shm_open(SHM_NAME, O_RDWR | O_CREAT | O_EXCL, 00600);
	if (shm == -1){ //Already exists, try to open it
		shm = shm_open(SHM_NAME, O_RDWR, 00600);
		if (shm == -1){
			perror("Failed to open shared memory:\n");
			exit(1);
		} else {
			shm_id = mmap(NULL, sizeof(struct Shm_shared), PROT_READ | PROT_WRITE, MAP_SHARED, shm, 0);
		}
	} else { //Not exists, create it and initialize it
		ftruncate(shm, sizeof(struct Shm_shared));
		shm_id = mmap(NULL, sizeof(struct Shm_shared), PROT_READ | PROT_WRITE, MAP_SHARED, shm, 0);
		pthread_mutex_init(&(shm_id->mutex), NULL);
		shm_id->last_given_id = 0;
	}
}

/*
Returns value of first available ID.
*/
int reserve_ids(int jobs_count) {
	int id_start;

	pthread_mutex_lock(&(shm_id->mutex));

	id_start = shm_id->last_given_id + 1;
	shm_id->last_given_id += jobs_count;

	pthread_mutex_unlock(&(shm_id->mutex));

	return id_start;
}

void shutdown(void){
	if (munmap(shm_id, sizeof(struct Shm_shared)) == -1){
		perror("Failed to unmap shared memory:\n");
	}
	if (shm_unlink(SHM_NAME) == -1){
		if (errno != ENOENT){
			perror("Failed to initialize closing shared memory:\n");
		}
	}
}
