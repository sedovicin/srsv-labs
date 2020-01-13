#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>
#include<sys/mman.h>
#include<time.h>

#define ENV_VAR_NAME "SRSV_LAB5"

mqd_t mq;
struct mq_attr mq_attr;
char* MQ_NAME;
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
	struct sched_param param;

	clock_gettime(CLOCK_REALTIME, &t);
	srand(t.tv_nsec);

	//Get arguments values
	if (argc < 3){
		fprintf(stderr, "G: Two arguments required!\n");
		exit(1);
	}
	jobs_count = atoi(argv[1]);
	max_job_duration = atoi(argv[2]);

	param.sched_priority = 50;
	pthread_setschedparam(pthread_self(), SCHED_RR, &param);
	
	if (NAME == NULL || strlen(NAME) <= 0){
		fprintf(stderr, "G: No message queue name defined!\n");
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

	sleep(1);

	shutdown();
	
	printf("G: Bye!\n");
	return 0;
}

/*
Does setup of message queue. Creates one if not exists, does linking only if exists.
*/
void setup_message_queue(char *NAME){
	MQ_NAME = calloc(strlen(NAME) + 2, sizeof(char));
	strncpy(MQ_NAME, "/\0", 2);
	strncat(MQ_NAME, NAME, strlen(NAME));

	mq = mq_open(MQ_NAME, O_RDWR | O_CREAT | O_EXCL, 00600, NULL);
	if (mq == (mqd_t) -1){ //Already exists, open it.
		mq = mq_open(MQ_NAME, O_RDWR);
		if (mq == (mqd_t) -1){
			perror("G: Failed to open message queue");
			exit(1);
		} else {
			printf("G: Opened message queue.\n");
		}
	} else { //Not exists, create one
		printf("G: Created message queue.\n");
	}
	if (mq_getattr(mq, &mq_attr) == -1){
		perror("G: Failed to get attribute of message queue");
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
			perror("G: Failed to open shared memory");
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
		shm_job = shm_open(JOB_NAME, O_RDWR, 00600);
		if (shm_job == -1){
			perror("G: Error while creating shared memory for job:\n");
		}
	} else {
		ftruncate(shm_job, sizeof(int) * job_duration);
	}

	shm_job_content = (int *)mmap(NULL, sizeof(int) * job_duration, PROT_READ | PROT_WRITE, MAP_SHARED, shm_job, 0);
	if (shm_job_content == (void *) -1){
		perror("G: Failed to map job shared memory");
	}

	for (i = 0; i < job_duration; ++i){
		shm_job_content[i] = random_numbers[i];
	}

	printf("G: job: %s [", descriptor);
	for (i = 0; i < job_duration; ++i){
		printf(" %d", shm_job_content[i]);
	}
	printf(" ]\n");

	if (mq_send(mq, descriptor, strlen(descriptor), 0) == -1){
		perror("G: Error while sending descriptor to message queue");
	}

	if (munmap(shm_job_content, sizeof(int) * job_duration) == -1){
		perror("G: Failed to unmap shared memory for job");
	}
	sleep(1);

	free(JOB_NAME);
	free(descriptor);
	free(random_numbers);
}

void shutdown(void){
	if (munmap(shm_id, sizeof(struct Shm_shared)) == -1){
		perror("G: Failed to unmap shared memory");
	}
	//if (shm_unlink(SHM_NAME) == -1){
	//	if (errno != ENOENT){
	//		perror("G: Failed to initialize closing shared memory");
	//	}
	//}

	free(MQ_NAME);
	free(SHM_NAME);
}
