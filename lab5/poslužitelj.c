#include<stdio.h>
#include<pthread.h>
#include<stdlib.h>

void *thread_worker(void *arg){
	printf("Hello from thread!\n");
	return 0;
}

int main(int argc, char *argv[]){
	int thread_count;
	int i;
	int s; //for statuses
	pthread_t *thread_ids;

	
	if (argc < 2){
		fprintf(stderr, "One argument required!\n");
		exit(1);
	}
	
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

	for (i = 0; i < thread_count; ++i){
		s = pthread_join(thread_ids[i], NULL);
		if (s != 0){
			perror("Failed to join thread:\n");
			exit(s);
		}
	}
	
	free(thread_ids);
	return 0;
}


