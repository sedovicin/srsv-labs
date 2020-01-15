#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>
#include<signal.h>
#include<unistd.h>
#include<mqueue.h>
#include<errno.h>
#include<string.h>
#include<time.h>

#define ENV_VAR_NAME "SRSV_LAB5"

short end = 0;

/*
Function for initializing shutdown on received signal.
*/
static void signal_handler(int sig, siginfo_t *info, void *context){
	printf("JOURNAL: SIGTERM received, initializing shutdown...\n");
	end = 1;
}

int main (void){
	struct sigaction sa;
	char* NAME = getenv(ENV_VAR_NAME);
	char* MQF_NAME;
	ssize_t mbytesread;
	void *buffer;
	struct mq_attr mqattr;
	mqd_t mqf;
	int i = 0;

	if (NAME == NULL || strlen(NAME) <= 0){
		fprintf(stderr, "J: No message queue name defined!\n");
		exit(1);
	}

	//Set shutdown on SIGTERM
	sa.sa_flags = SA_SIGINFO;
	sigemptyset(&sa.sa_mask);
	sigaddset(&sa.sa_mask, SIGTERM);
	sa.sa_sigaction = signal_handler;
	if (sigaction(SIGTERM, &sa, NULL) == -1){
		perror("J: Error on setting action for signal SIGTERM");
	}

	//Open log message queue
	MQF_NAME = calloc(strlen(NAME) + 2, sizeof(char));
	strncpy(MQF_NAME, "/\0", 2);
	strncat(MQF_NAME, NAME, strlen(NAME));
	strncat(MQF_NAME, "-log\0", 5);

	mqf = mq_open(MQF_NAME, O_RDONLY);
	if (mqf == (mqd_t) -1){
		while (errno == ENOENT && (++i < 10)){
			mqf = mq_open(MQF_NAME, O_RDONLY);
			if (mqf != (mqd_t) -1){
				break;
			}
			printf("J: Could not open finished message queue. Trying again...\n");
			sleep(1);
		}
		if (mqf == (mqd_t) -1){
			perror("J: Failed to open finished message queue");
			exit(1);
		}
	} else {
		printf("J: Opened finished message queue.\n");
	}

	if (mq_getattr(mqf, &mqattr) == -1){
		perror("J: Failed to get attribute of message queue");
	}

	buffer = malloc(mqattr.mq_msgsize);
	if (buffer == NULL){
		perror("J: Failed to allocate memory for message buffer");
	}

	while(!end){
		mbytesread = mq_receive(mqf, buffer, mqattr.mq_msgsize, NULL);
		if (mbytesread == -1){
			
			perror("J: Error while reading message queue");
		} else {
			printf("J: %s\n", (char *)buffer);
			memset(buffer, 0, strlen(buffer));
		}
	}
}
