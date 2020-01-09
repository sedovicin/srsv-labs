#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>

#define MQ_ENV_VAR_NAME "SRSV_LAB5"

short end = 0;

/*
Function that represents worker thread.
*/
static void *thread_worker(void *arg){
	while(!end){	
		printf("Hello from thread!\n");
		sleep(1);
	}
	return NULL;
}

/*
Function for initializing shutdown on received signal.
*/
static void signal_handler(int sig, siginfo_t *info, void *context){
	printf("SIGTERM received, initializing shutdown...\n");
	end = 1;
}

int main(int argc, char *argv[]){
	int thread_count;
	int i;
	int s; //for statuses
	pthread_t *thread_ids;
	struct sigaction sa;
	mqd_t mq;
	struct mq_attr mqattr;
	void *buffer;
	ssize_t mbytesread;
	char* MQ_NAME = getenv(MQ_ENV_VAR_NAME);

	//Check if arguments are here
	if (argc < 2){
		fprintf(stderr, "One argument required!\n");
		exit(1);
	}

	//Set shutdown on SIGTERM
	sa.sa_flags = SA_SIGINFO;
	sigemptyset(&sa.sa_mask);
	sigaddset(&sa.sa_mask, SIGTERM);
	sa.sa_sigaction = signal_handler;
	if (sigaction(SIGTERM, &sa, NULL) == -1){
		perror("error on setting action for signal SIGTERM:\n");
	}


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

	//Create needed amount of threads
	thread_count = atoi(argv[1]);
	printf("Dretvi: %d\n", thread_count);
	thread_ids = calloc(thread_count, sizeof(pthread_t));	
	if (thread_ids == NULL){
		perror("Failed to allocate memory for threads!\n");
		exit(1);
	}
	for (i = 0; i < thread_count; ++i){
		s = pthread_create(&thread_ids[i], NULL, &thread_worker, NULL);
		if (s != 0){
			perror("Failed to create thread:\n");
			exit(s);
		}
	}

	//Do what you need to do
	while(!end){
		//Read message
		mbytesread = mq_receive(mq, buffer, mqattr.mq_msgsize, NULL);
		if (mbytesread == -1){
			if (errno == EAGAIN){
				 printf("Nothing to read...\n");
			}
		}
		//Do the rest
		sleep(1);
	}

	//Wait for all threads to finish
	for (i = 0; i < thread_count; ++i){
		s = pthread_join(thread_ids[i], NULL);
		if (s != 0){
			perror("Failed to join thread:\n");
			exit(s);
		}
	}
	
	if (mq_unlink(MQ_NAME) == -1){
		perror("Failed to initialize closing message queue:\n");
	}
	free(thread_ids);

	printf("Bye!\n");
	return 0;
}
