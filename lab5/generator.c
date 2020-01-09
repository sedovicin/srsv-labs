#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>

#define MQ_ENV_VAR_NAME "SRSV_LAB5"

int main(int argc, char *argv[]){
	int jobs_count;
	int max_job_duration;
	mqd_t mq;
	struct mq_attr mqattr;
	void *buffer;
	char* MQ_NAME = getenv(MQ_ENV_VAR_NAME);

	//Get arguments values
	if (argc < 3){
		fprintf(stderr, "Two arguments required!\n");
		exit(1);
	}
	jobs_count = atoi(argv[1]);
	max_job_duration = atoi(argv[2]);

	//Setup message queue link
	if (MQ_NAME == NULL || strlen(MQ_NAME) <= 0){
		fprintf(stderr, "No message queue name defined!\n");
		exit(1);
	}
	//mq = mq_open(MQ_NAME, O_RDONLY | O_NONBLOCK);
	mq = mq_open(MQ_NAME, O_RDONLY | O_NONBLOCK | O_CREAT, 00600, NULL);
	if (mq == (mqd_t) -1){
		perror("Failed to open message queue:\n");
		exit(1);
	}
	if (mq_getattr(mq, &mqattr) == -1){
		perror("Failed to get attribute of message queue:\n");
	}
	buffer = malloc(mqattr.mq_msgsize);
	if (buffer == NULL){
		perror("Failed to allocate memory for message buffer:\n");
	}

	//Do what you need to do
	printf("%d %d\n", jobs_count, max_job_duration);
	sleep(1);

	if (mq_unlink(MQ_NAME) == -1){
		perror("Failed to initialize closing message queue:\n");
	}
	printf("Bye!\n");
	return 0;
}
