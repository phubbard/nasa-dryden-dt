#include<sys/socket.h>
#include<netinet/in.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <string.h>
struct sockaddr_in target_host_address;	/*the receiver's address*/
struct sockaddr_in host_address;
int hst_addr_size = sizeof(host_address);
unsigned char * target_address_holder;

int sendInit(int s, int * address, int WritePort) 
{

  if (s == -1) { printf("BAD SOCKET\n");}
  target_host_address.sin_family=PF_INET;
  target_host_address.sin_port=htons(WritePort);
  target_address_holder=(unsigned char*)&target_host_address.sin_addr.s_addr;
  target_address_holder[0]=address[0];
  target_address_holder[1]=address[1];
  target_address_holder[2]=address[2];
  target_address_holder[3]=address[3];
  return 0;
}


int initSocket(int * destination, char * protocol, int ReadPort) 
{
  int s;
  s=socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
  fcntl(s,F_SETFL, O_NONBLOCK);
  //if (s < 0) peform some error handling
  memset((void*)&host_address, 0, sizeof(host_address));
  host_address.sin_family=PF_INET;
  host_address.sin_addr.s_addr=INADDR_ANY;
  host_address.sin_port=htons(ReadPort);
  if(bind(s, (struct sockaddr*)&host_address, sizeof(host_address)) < 0) 
  {
    printf("error binding the socket\n");
  }
  return s;
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
  memset(buffer,0,bufferSize); 
  int length;
  /*wait for incoming message*/
  //length = recvfrom(s, buffer, bufferSize, 0, 
	//(struct sockaddr*)&host_address, &hst_addr_size);
  length = recv(s, buffer, bufferSize, 0);
  return length;

}





int main()
{
int s;
int RC;
int destination[3];
char buffer[2000];
destination[0]=127;
destination[1]=0;
destination[2]=0;
destination[3]=1;
s=initSocket(destination, "UDP", 5001);
sendInit(s, destination, 5000);
sendData(s, "FIU VERSION 3 TEST", strlen("FIU VERSION 3 TEST"));
while(1)
{
RC=readData(s, buffer, sizeof(buffer));
if(RC>0)
{
	printf("%s\n",buffer);

}




}
return 0;
}
