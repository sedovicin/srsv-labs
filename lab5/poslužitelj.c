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
#define WAIT_TIME 10

short end = 0;

/*
#########################
NODE BEGIN
#########################
*/
typedef struct node {
	char *message;
	struct node *next;
} Node;

Node *head = NULL;
Node *tail = NULL;
int job_count = 0;
int total_jobs_duration = 0;

/*
Adds a copy of messageToAdd to the end of the list.
*/
void add_node(char *message_to_add){
	Node *new_node = malloc(sizeof(Node));
	char *new = calloc(strlen(message_to_add), sizeof(char));
	strncpy(new, message_to_add, strlen(message_to_add));

	new_node -> message = new;
	if (head == NULL){ //list empty
		head = new_node;
		tail = new_node;
	}
	else { //list contains at least one element
		tail -> next = new_node;
		tail = new_node;
	}
}

/*
Removes node from beginning of the list and returns element that was in removed node.
*/
char* remove_node(){
	char *message;
	Node *first_node = head;

	if (head == NULL){ //list empty
		return NULL;
	}

	head = first_node -> next;
	if (first_node -> next == NULL){ //only one (this) entry in list
		tail = NULL;
	}

	message = first_node -> message;

	free(first_node);
	return message;
}
/*
#########################
NODE END
#########################
*/


pthread_cond_t msgs_avail;
pthread_mutex_t mutex_mq;

void do_work(pthread_t thread_id, char* message){
	int id;
	int job_duration;
	char* shm_job_name;
	char* split;
	//struct timespec t, thread_wake_limit;

	split = strtok(message, " ");
	id = atoi(split);
	split = strtok(NULL, " ");
	job_duration = atoi(split);

	shm_job_name = strtok(NULL, " ");

	printf("R%lu: ID: %d; DURATION: %d; NAME: %s\n", thread_id, id, job_duration, shm_job_name);

}

/*
Function that represents worker thread.
*/
static void *thread_worker(void *arg){
	pthread_t thread = pthread_self();
	char* message;
	char* buf_cpy;
	char* duration_ptr;
	int duration;

	printf("Hello from thread %lu\n", thread);
	while(1){
		pthread_mutex_lock(&mutex_mq);
		if (end && (job_count == 0)){
			printf("R%lu: no jobs and end\n", thread);
			pthread_mutex_unlock(&mutex_mq);
			break;
		}
		while (job_count == 0 && !end){
			printf("R%lu: Wait started\n", thread);
			pthread_cond_wait(&msgs_avail, &mutex_mq);
			printf("R%lu: Wait finished\n", thread);
		}
		if (end && (job_count == 0)){
			printf("%lu no jobs and end\n", thread);
			pthread_mutex_unlock(&mutex_mq);
			break;
		}
		message = remove_node();
		--job_count;
		buf_cpy = calloc(strlen(message) + 1, sizeof(char));
		strncpy(buf_cpy, message, strlen(message));
		duration_ptr = strtok(buf_cpy, " ");
		duration_ptr = strtok(NULL, " ");
		duration = atoi(duration_ptr);
		total_jobs_duration -= duration;

		pthread_mutex_unlock(&mutex_mq);
		printf("R%lu: got message\n", thread);
		free(buf_cpy);

		do_work(thread, message);
	}
	return NULL;
}

/*
Function for initializing shutdown on received signal.
*/
static void signal_handler(int sig, siginfo_t *info, void *context){
	printf("SIGTERM received, initializing shutdown...\n");
	end = 1;
	pthread_cond_broadcast(&msgs_avail);
}

int main(int argc, char *argv[]){
	int thread_count;
	int min_jobs_duration;
	int i = 0;
	int s; //for statuses
	pthread_t *thread_ids;
	struct sigaction sa;
	mqd_t mq;
	struct mq_attr mqattr;
	void *buffer;
	ssize_t mbytesread;
	char* NAME = getenv(ENV_VAR_NAME);
	char* MQ_NAME;
	char* buf_cpy;
	char* duration_ptr;

	struct timespec t, thread_wake_limit;

	//Check if arguments are here
	if (argc < 3){
		fprintf(stderr, "P: Two arguments required!\n");
		exit(1);
	}

	min_jobs_duration = atoi(argv[2]);
	pthread_cond_init(&msgs_avail, NULL);
	pthread_mutex_init(&mutex_mq, NULL);

	//Set shutdown on SIGTERM
	sa.sa_flags = SA_SIGINFO;
	sigemptyset(&sa.sa_mask);
	sigaddset(&sa.sa_mask, SIGTERM);
	sa.sa_sigaction = signal_handler;
	if (sigaction(SIGTERM, &sa, NULL) == -1){
		perror("P: Error on setting action for signal SIGTERM");
	}

	if (NAME == NULL || strlen(NAME) <= 0){
		fprintf(stderr, "No message queue name defined!\n");
		exit(1);
	}
	MQ_NAME = calloc(strlen(NAME) + 2, sizeof(char));
	strncpy(MQ_NAME, "/\0", 2);
	strncat(MQ_NAME, NAME, strlen(NAME));
	//Setup message queue link


	mq = mq_open(MQ_NAME, O_RDONLY | O_NONBLOCK);
	//mq = mq_open(MQ_NAME, O_RDONLY | O_NONBLOCK | O_CREAT, 00600, NULL);
	if (mq == (mqd_t) -1){
		while (errno == ENOENT && (++i < 10)){
			mq = mq_open(MQ_NAME, O_RDONLY | O_NONBLOCK);
			if (mq != (mqd_t) -1){
				break;
			}
			printf("Could not open message queue. Trying again...\n");
			sleep(1);
		}
		if (mq == (mqd_t) -1){
			perror("P: Failed to open message queue");
			exit(1);
		}
	}
	if (mq_getattr(mq, &mqattr) == -1){
		perror("P: Failed to get attribute of message queue");
	}
	buffer = malloc(mqattr.mq_msgsize);
	if (buffer == NULL){
		perror("P: Failed to allocate memory for message buffer");
	}

	//Create needed amount of threads
	thread_count = atoi(argv[1]);
	printf("Dretvi: %d\n", thread_count);
	thread_ids = calloc(thread_count, sizeof(pthread_t));	
	if (thread_ids == NULL){
		fprintf(stderr, "P: Failed to allocate memory for threads!\n");
		exit(1);
	}
	for (i = 0; i < thread_count; ++i){
		s = pthread_create(&thread_ids[i], NULL, &thread_worker, NULL);
		if (s != 0){
			perror("P: Failed to create thread");
			exit(s);
		}
	}

	clock_gettime(CLOCK_REALTIME, &t);
	thread_wake_limit.tv_sec = t.tv_sec + WAIT_TIME;
	thread_wake_limit.tv_nsec = t.tv_nsec;

	//Do what you need to do
	while(!end){
		//Read message
		mbytesread = mq_receive(mq, buffer, mqattr.mq_msgsize, NULL);
		if (mbytesread == -1){
			if (errno == EAGAIN){
				//printf("Nothing to read...\n");
				clock_gettime(CLOCK_REALTIME, &t);
				sleep(1);
			}
			else {
				perror("P: Error while reading message queue");
			}
		} else {
			printf("MEssage read\n");
			pthread_mutex_lock(&mutex_mq);
			add_node(buffer);
			clock_gettime(CLOCK_REALTIME, &t);
			thread_wake_limit.tv_sec = t.tv_sec + WAIT_TIME;
			thread_wake_limit.tv_nsec = t.tv_nsec;

			++job_count;
			buf_cpy = calloc(strlen(buffer) + 1, sizeof(char));
			strncpy(buf_cpy, buffer, strlen(buffer));
			duration_ptr = strtok(buf_cpy, " ");
			duration_ptr = strtok(NULL, " ");
			total_jobs_duration += atoi(duration_ptr);

			pthread_mutex_unlock(&mutex_mq);
			free(buf_cpy);
			printf("Message added\n");
		}
		if ((job_count >= thread_count && total_jobs_duration >= min_jobs_duration)
			 || (t.tv_sec >= thread_wake_limit.tv_sec && t.tv_nsec >= thread_wake_limit.tv_nsec)) {

			printf("Condition met\n");
			pthread_cond_broadcast(&msgs_avail);

			clock_gettime(CLOCK_REALTIME, &t);
			thread_wake_limit.tv_sec = t.tv_sec + 30;
			thread_wake_limit.tv_nsec = t.tv_nsec;
		}
		//Do the rest
		//sleep(1);
	}

	//Wait for all threads to finish
	for (i = 0; i < thread_count; ++i){
		s = pthread_join(thread_ids[i], NULL);
		if (s != 0){
			perror("P: Failed to join thread");
			exit(s);
		}
	}

	pthread_cond_destroy(&msgs_avail);
	
	if (mq_unlink(MQ_NAME) == -1){
		perror("P: Failed to initialize closing message queue");
	}
	free(thread_ids);

	printf("P: Bye!\n");
	return 0;
}
