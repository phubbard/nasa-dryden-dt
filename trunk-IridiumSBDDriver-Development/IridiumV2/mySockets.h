#include<string.h>
#include<stdio.h>
#include<sys/socket.h>
#include<netinet/in.h>
#define BUF_SIZE 2000
#define LOCAL_PORT 5000
#define REMOTE_PORT 5000

//DEFINE prototypes:
//DEFINE FUNCTION DEFINITIONS

//int s; /*the socket descriptor*/
struct sockaddr_in target_host_address;	/*the receiver's address*/
struct sockaddr_in host_address;
int hst_addr_size = sizeof(host_address);
unsigned char* target_address_holder;						
int initSocket(int * destination, char * protocol) 
{
  int s;
  /*socketdescriptor*/
  /*struct used for binding the socket to a local address*/
  //struct sockaddr_in host_address;	
  /*create the socket*/
  s=socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
  fcntl(s,F_SETFL, O_NONBLOCK);
  //if (s < 0) peform some error handling
  /*init the host_address the socket is beeing bound to*/
  memset((void*)&host_address, 0, sizeof(host_address));
  /*set address family*/
  host_address.sin_family=PF_INET;
  /*accept any incoming messages:*/
  host_address.sin_addr.s_addr=INADDR_ANY;
  /*the port the socket i to be bound to:*/
  host_address.sin_port=htons(LOCAL_PORT);
  if(bind(s, (struct sockaddr*)&host_address, sizeof(host_address)) < 0) 
  {
    printf("error binding the socket\n");
  }
  return s;
}

int sendInit(int s, int * address) 
{
  //unsigned char* target_address_holder;	/*a pointer to the ip address*/
  /*create the socket*/
  if (s == -1) { printf("BAD SOCKET\n");}
  /*init target address structure*/
  target_host_address.sin_family=PF_INET;
  target_host_address.sin_port=htons(REMOTE_PORT);
  target_address_holder=(unsigned char*)&target_host_address.sin_addr.s_addr;
  //address of target will be from the array structure passed into this function
  target_address_holder[0]=address[0];
  target_address_holder[1]=address[1];
  target_address_holder[2]=address[2];
  target_address_holder[3]=address[3];
  return 0;
}

int sendData(int s, char * buffer, int bufferSize)
{
  /*send it*/
  int rc;
 
  rc=sendto(s, buffer, bufferSize, 0, 
    (struct sockaddr*)&target_host_address, sizeof(struct sockaddr));
 //rc=send(s, buffer, bufferSize,0);

 return 0;
}


int readData(int s, char * buffer, int bufferSize)
{
  int length;
  /*wait for incoming message*/
  //length = recvfrom(s, buffer, bufferSize, 0, 
	//(struct sockaddr*)&host_address, &hst_addr_size);
  length = recv(s, buffer, bufferSize, 0);
  return length;

}



