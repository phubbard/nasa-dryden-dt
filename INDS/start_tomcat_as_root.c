
#include <unistd.h>

main() {
    int returnVal =
	execl("/home/suborbital/INDS/start_tomcat",NULL);
    printf("Started Tomcat; return %d\n",returnVal);
}
