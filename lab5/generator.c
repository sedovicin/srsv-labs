#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>
#include<sys/mman.h>
#

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

void create_job(int id, int max_job_duration);

void shutdown(void);

int main(int argc, char *argv[]){
	int jobs_count;
	int max_job_duration;
	
	char* NAME = getenv(ENV_VAR_NAME);

	int id_start = 0;
	int i;
	struct timespec t;

	clock_gettime(CLOCK_REALTIME, &t);
	srand(t.tv_nsec);

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

	//mq_unlink(MQ_NAME);
	//shm_unlink(SHM_NAME);
	//exit(0);

	id_start = reserve_ids(jobs_count);

	for (i = id_start; i < id_start + jobs_count; ++i){
		create_job(i, max_job_duration);
	}

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

void create_job(int id, int max_job_duration) {
	int job_duration;
	int i;
	char *JOB_NAME;
	char *descriptor;
	int *random_numbers;

	int shm_job;
	int *shm_job_content;

	job_duration = rand() % (max_job_duration - 1) + 1;

	JOB_NAME = calloc(strlen(SHM_NAME) + 2 + 10, sizeof(char));
	snprintf(JOB_NAME, strlen(SHM_NAME) + 2 + 10, "%s-%d", SHM_NAME, id);

	descriptor = calloc(10 + 10 + strlen(JOB_NAME) + 3, sizeof(char));
	snprintf(descriptor, 10 + 10 + strlen(JOB_NAME) + 3, "%d %d %s", id, job_duration, JOB_NAME);

	random_numbers = calloc(job_duration, sizeof(int));
	for (i = 0; i < job_duration; ++i){
		random_numbers[i] = rand() % 100000;
	}

	shm_job = shm_open(JOB_NAME, O_RDWR | O_CREAT | O_EXCL, 00600);
	if (shm_job == -1){
		perror("Error while creating shared memory for job:\n");
	}
	ftruncate(shm_job, sizeof(int) * job_duration);
	shm_job_content = (int *)mmap(NULL, sizeof(int) * job_duration, PROT_READ | PROT_WRITE, MAP_SHARED, shm_job, 0);

	for (i = 0; i < job_duration; ++i){
		shm_job_content[i] = random_numbers[i];
	}

	if (mq_send(mq, descriptor, strlen(descriptor), 0) == -1){
		perror("Error while sending descriptor to message queue:\n");
	}

	if (munmap(shm_job_content, sizeof(int) * job_duration) == -1){
		perror("Failed to unmap shared memory for job:\n");
	}
	sleep(1);

	free(JOB_NAME);
	free(descriptor);
	free(random_numbers);
	free(shm_job_content);
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

	free(mq_buffer);
	free(MQ_NAME);
	free(SHM_NAME);
}
