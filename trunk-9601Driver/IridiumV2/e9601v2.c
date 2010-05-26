#include<string.h>
#include <stdlib.h>
#include<stdio.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include <fcntl.h>   
#include <errno.h>   
#include <termios.h>
#define PATH "/home/lappy/REVEAL/Iridium9601/9601.cfg"
#define LOCAL_PORT 5000
#define REMOTE_PORT 5000
#define Retry 3
#define BufferSize 1960
#define ModemPathDefault "/dev/ttyUSB0"
#define SBDWBMax 1960  //Maximum Mobile originated SBD Binary write w/o checksum
#define SBDWBMin 1     //Minimum Mobile originated SBD...
#define SBDWTMAX 1960  //Max text message  
#define SBDWTMIN 1     //Min text message

struct sockaddr_in target_host_address;	/*the receiver's address*/
struct sockaddr_in host_address;
struct Iridium9601
{

  int BaudRate;
  int Port;
  int SerialDescriptor;
  int SocketDescriptor;
  int Destination[4];
  char Protocol[10];
  char ModemPath[20];
};

int hst_addr_size = sizeof(host_address);
unsigned char* target_address_holder;						
long BAUDRATE=B19200;

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

//for debug: sometimes seeing raw hex can help with jumbled ascii nonsense
void print_str_to_hex(unsigned char *char_string)
{
  int i;
  printf("hex representation:\n");
  for(i=0;char_string[i]!=0x00;i++)
  {
    printf("%02X ",char_string[i]);	
  }
  printf("\n");
}
int readSerPort(int fd, char * buffer, int bufferSize)
{
  int res;
  char buf[200];
  buffer[0]=0;
  while ((res = read(fd,buf, sizeof(buf))) > 0) 
  { 
    buf[res]=0;  
    //strcat(interimBuff,buf);
   strcat(buffer,buf);
  }
  return 0;
}

int writeSerPort(int fd, char *message, int messageSize)   
{
  write(fd,message,messageSize);
  return 0;
}

int initSerPort(char * modemPath, int baud)
{
  int fd;
  fd=open(modemPath, O_RDWR | O_NOCTTY); 
  struct termios oldtio,newtio;
  switch (baud) 
  {
 	case 9600: BAUDRATE=B9600; break;	
 	case 19200: BAUDRATE=B19200; break;	 
 	case 38400: BAUDRATE=B38400; break;	
    default: BAUDRATE=B19200; break; 
  }
  tcgetattr(fd,&oldtio); // save current serial port settings 
  newtio.c_cflag = BAUDRATE | CRTSCTS | CS8 | CLOCAL | CREAD;
  newtio.c_iflag = IGNPAR | ICRNL;
  newtio.c_oflag = 0;
  newtio.c_lflag = 0;
  newtio.c_cc[VTIME]    = 1;   // inter-character timer unused, necessary to prevent READ BLOCK CONDITION!!!!!!!!
  newtio.c_cc[VMIN]     = 0;   
  tcflush(fd, TCIFLUSH);
  tcsetattr(fd,TCSANOW,&newtio);
  return fd;	
}

int init9601(char * path, struct Iridium9601 * my9601, int maxModems)
{
  FILE *file;
  int rc;
  int digitValue;
  int modemCount;
  char index[100];
  char value[100];
  struct Iridium9601 * ptr;
  //do an LS to see if there is a config file, if not, populate with defaults.
  file=fopen(path, "r"); 
  modemCount =-1; //should be read from config file
  while(rc=fscanf(file, "%s %s", index, value) > 0)
  {
      if(!(strcmp(index, "MODEMPATH")))
      {
          modemCount=modemCount+1;
          ptr = &my9601[modemCount]; 
	  sprintf(ptr->ModemPath, "%s", value);
      } 
      else if(!(strcmp(index, "PROTOCOL")))
      {
         sprintf(ptr->Protocol, "%s", value);
      }
      else if(!(strcmp(index, "BAUDRATE")))
      {
	  digitValue=atoi(value);
     	  ptr->BaudRate=digitValue;	  	
      }  
      else if(!(strcmp(index, "PORT")))
      {
	  digitValue=atoi(value);
     	  ptr->Port=digitValue;
      }
      else if(!(strcmp(index, "DESTINATION")))
      {
	  ptr->Destination[0]= atoi(strtok(value, "."));
          ptr->Destination[1]= atoi(strtok(NULL, "."));
          ptr->Destination[2]= atoi(strtok(NULL, "."));
          ptr->Destination[3]= atoi(strtok(NULL, " "));
     }
  }
  fclose(file);
  //time to set the File Descriptor for the Serial port and the socket
  //ptr->SerialDescriptor = open(ptr->ModemPath, O_RDWR | O_NOCTTY); 
  if (ptr->SerialDescriptor <0) {perror("No Serial"); return(-1); }
  ptr->SerialDescriptor=initSerPort(ptr->ModemPath, ptr->BaudRate);
  if (ptr->SocketDescriptor <0) {perror("No Socket"); return(-1); }
  ptr->SocketDescriptor=initSocket(ptr->Destination, ptr->Protocol);
  return 0;
}

int printStructs(struct Iridium9601 *my9601, int maxModems)
{
  int index;
  for(index=0; index<maxModems; index++)
  {
     printf("Modem %i config:\n BaudRate: %i\n Port: %i\n ", index+1,my9601[index].BaudRate, my9601[index].Port);
     printf("Destination %i.%i.%i.%i\n ", my9601[index].Destination[0], my9601[index].Destination[1], my9601[index].Destination[2], my9601[index].Destination[3]);
     printf("Protocol: %s\n ModemPath: %s\n \n",my9601[index].Protocol, my9601[index].ModemPath);
     printf("SerialDescriptor: %i\n SocketDescriptor: %i\n \n",my9601[index].SerialDescriptor, my9601[index].SocketDescriptor);
  }
}

int sendMessage(struct Iridium9601 *IridiumPTR, char * message, int messageSize,  char * RV)
{
  char returnBuffer[SBDWTMAX];
  int index;
  for(index=0; index<Retry; index++)
  {
     
     writeSerPort(IridiumPTR->SerialDescriptor, message, messageSize);
     readSerPort(IridiumPTR->SerialDescriptor,returnBuffer, SBDWTMAX);
     if(strpbrk(returnBuffer, RV) != NULL)
     {
       index=Retry;
       return 0;
     }
     else if(!(strcmp("NONE",RV)))
     {
       index=Retry;
       return 0;
     }
     printf("Retry on %s\n", message);
  }
  return -1;
}

int writeText(struct Iridium9601 *IridiumPTR, char * outMessage)
{
  int RC;
  RC=sendMessage(IridiumPTR, "AT+SBDWT\r", strlen("AT+SBDWT\r"), "READY");
  if(RC==-1)
  {
    return -1; //message failed to write text
  }
  RC=sendMessage(IridiumPTR, outMessage, SBDWTMAX , "OK");
  if(RC==-1)
  {
    return -1; //message failed to load
  }
  return sendMessage(IridiumPTR, "AT+SBDIX\r", strlen("AT+SBDIX\r"), "NONE");
}

int main()
{
  int ReadLength;
  ReadLength=0;
  struct Iridium9601 my9601[1];
  struct Iridium9601 *ptr; //reference to my9601
  ptr=&my9601[0];
  init9601(PATH, my9601, 1);
  printStructs(my9601, 1);
  while(1)
  {
      char outBuffer[SBDWTMAX];
       //get Data from Socket
       ReadLength=readData(ptr->SocketDescriptor, outBuffer, SBDWTMAX); 
       /**if there is data in socket, send. This can be changed to
       1.interrupt from UDP socket
       2.turn off interrupts
       3. Service socket
       4. Turn on interrupts
       **/
       if(ReadLength >0)
       {
         printf("Message Received: %s\n", outBuffer);
         writeText(ptr, outBuffer); //WriteText to SBD
       }
  }  
  return 0; 
}



