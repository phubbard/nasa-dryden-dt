/**9601 header file for the Dryden GSRP 
 AUTHOR: Christian J Rodriguez
 EMAIL: Crodr021@FIU.edu
**/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "mySer.h"
#include "mySockets.h"
#define Retry 3
#define BufferSize 1960
#define ModemPathDefault "/dev/ttyUSB0"
#define SBDWBMax 1960  //Maximum Mobile originated SBD Binary write w/o checksum
#define SBDWBMin 1     //Minimum Mobile originated SBD...
#define SBDWTMAX 1960  //Max text message  
#define SBDWTMIN 1     //Min text message

struct Iridium9601
{

  int BaudRate;
  int Port;
  int SerialDescriptor;
  int SocketDescriptor;
  int Destination[4];
  char Protocol[10];
  char ModemPath[20];
  char ReturnBuffer[BufferSize];
  char MessageBuffer[BufferSize];
};

//for debug:
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

/**
maxModems will be changed to size of Iridium9601...i.e. number of modems in config file
As it stands now, I have a hard limit for testing purposes because I've yet to decided
how I want to handle the config file format.

**/
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
//test code for checksum
int checkSum(char * message, int size)
{
	unsigned short checksum; 
        checksum= 0; 
	int i;
	unsigned char c;
        char data[] = "hello";
	int length;
        length=sizeof(data);
	for (i=0;i<length;i++) {
	       c = data[i];
	       checksum += c;
	}
	//Print out the 2 byte checksum
	printf("%x\n",checksum/256);
	printf("%x\n",checksum%256);
        return 0;
}



int sendMessage(struct Iridium9601 *IridiumPTR, char * message, int messageSize,  char * RV)
{
  int index;
  for(index=0; index<Retry; index++)
  {
     
     writeSerPort(IridiumPTR->SerialDescriptor, message, messageSize);
     readSerPort(IridiumPTR->SerialDescriptor,IridiumPTR->ReturnBuffer, BufferSize);
     if(strpbrk(IridiumPTR->ReturnBuffer, RV) != NULL)
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

int sendAT(struct Iridium9601 *IridiumPTR)
{
  char message[] = "AT\r";
  char returnValue[]="OK";
  int messageSize = strlen(message);
  return sendMessage(IridiumPTR, message, messageSize, returnValue);

}


int getRegisters(struct Iridium9601 *IridiumPTR)
{
  char message[] = "AT%R\r";
  char returnValue[]="OK";
  int messageSize = strlen(message);
  return sendMessage(IridiumPTR, message, messageSize, returnValue);

}

int initSBDIX(struct Iridium9601 *IridiumPTR)
{
  char message[] = "AT+SBDIX\r";
   //and other things...these have to be parsed on return
  int messageSize = strlen(message);
  return sendMessage(IridiumPTR, message, messageSize, "NONE");

}
/**
int checkRing(struct Iridium9601 *IridiumPTR)
{
    readSerPort(IridiumPTR->SerialDescriptor,IridiumPTR->ReturnBuffer, BufferSize);
    if(strpbrk(IridiumPTR->ReturnBuffer, "SBDRING") != NULL)
    {
      printf("RING RING RING:\n");
      initSBDIX(IridiumPTR);
      sleep(7);
      //readText(IridiumPTR);
      //sleep(3);
      char message[] = "AT+SBDRT\r";
      int messageSize = strlen(message);
      sendMessage(IridiumPTR, message, messageSize, "+SBDRT:");
      printf("Message: %s\n", IridiumPTR->ReturnBuffer);
      sendData(IridiumPTR->SocketDescriptor, IridiumPTR->ReturnBuffer);


      
    }
    return 0;
}
**/
int writeText(struct Iridium9601 *IridiumPTR)
{
  int RC;
  RC=sendMessage(IridiumPTR, "AT+SBDWT\r", strlen("AT+SBDWT\r"), "READY");
  if(RC==-1)
  {
    return -1; //message failed to write text
  }
  RC=sendMessage(IridiumPTR, IridiumPTR->MessageBuffer, BufferSize , "OK");
  if(RC==-1)
  {
    return -1; //message failed to load
  }
  return sendMessage(IridiumPTR, "AT+SBDIX\r", strlen("AT+SBDIX\r"), "NONE");
}








