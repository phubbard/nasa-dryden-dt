/**
Iridium 9601 code as part of NASA GSRP 2010 and Erigo 
Chris Rodriguez crodr021@fiu.edu
Florida International University 
Miami, FL
**/

/** 
Changelog:   
V3 4/14/2010 CR
1. Serial reader was changed
2. The ability to read SBDIX output correctly (this was in the collection of files, but buggy), now it works.
	a. Using this output one can see if a message was downloaded.
	b. If there are messages in the gateway
	c. How many to go fetch
3. The -v flag was added for verbose
4. The -l flag was added for log, currently disabled. Will be enabled for V4
5. Proper initializing of buffers.
6. Others to be listed in doc file.
***reminder to discuss with John source/destination question

Todo for V4:
1. SBDRING interrupt driven recognition. I have the code somewhat working, but not integrated into this file.
  As of V3, the messages from the gateway will only be retrieved after an SBDIX session.
2. Cleanup the verbose/log feature by putting into its own function
	i.e.
	int debug(char * string)
	{
		if(v){ verbose string }
		if(l){ log string }
		return 0;
	}
3. Reset MO and MT buffers after respective operations. 
4. Enable Retries again.
5. Clean up of code.


Todo for V5:
1. Once a "perfect" program is up and running, we can talk about migrating to multiple threads working with each other since the functionality
	is already in place.
2. More robust error handling...for example, an SBDIX return code can indicate RF drop, no connection or the failure to retrive an MT message
	or even location update. 

**/

#include<sys/socket.h>
#include<netinet/in.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <string.h>
#define PATH "9601.cfg"
#define LOCAL_PORT 5000
#define REMOTE_PORT 5000
#define BAUDRATE B19200            
#define ModemPathDefault "/dev/ttyUSB0"
#define FALSE 0
#define TRUE 1
#define logfile "9601.log"
#define SBDWTMAX 1960  
#define SBDWTMIN 1     

struct sockaddr_in target_host_address;	/*the receiver's address*/
struct sockaddr_in host_address;
struct termios oldtio,newtio;

struct Iridium9601
{

int BaudRate;
int ReadPort;
int WritePort;
int SerialDescriptor;
int SocketDescriptor;
int Destination[4];
char Protocol[10];
char ModemPath[20];
};


volatile int STOP=FALSE; 
int hst_addr_size = sizeof(host_address);
unsigned char * target_address_holder;
char output[SBDWTMAX]={0}; 
char udpIn[SBDWTMAX]={0}; 
char udpSend[SBDWTMAX]={0}; 
int verboseon=0;
int logon=0;
FILE * logfp;

/** 
Function Prototypes for 9601
more to be listed as part of cleanup.
**/
int initSerPort(char * modemPath, int baud); /* Initiates the serial port for the 9601 */
int sendATCommand(int fd, char * message, char * returnCode);
int init9601(char * path, struct Iridium9601 * my9601, int maxModems);
int printStructs(struct Iridium9601 *my9601, int maxModems);

//convenient way of holding SBDIX return variables
struct SBDIXReturn
{
int MOSTATUS;
int MOMSN;
int MTSTATUS;
int MTMSN;
int MTLENGTH;
int MTQUEUED;
} mySBDI;

/**
start of functions for 9601 program

**/

//cleaned
int printStructs(struct Iridium9601 *my9601, int maxModems) 
{
	int index;
	for(index=0; index<maxModems; index++)
	{
		 printf("Modem %i config:\n BaudRate: %i\n ReadPort: %i\n WritePort: %i\n ", index+1,my9601[index].BaudRate, my9601[index].ReadPort,my9601[index].WritePort);
		 printf("Destination %i.%i.%i.%i\n ", my9601[index].Destination[0], my9601[index].Destination[1], my9601[index].Destination[2], my9601[index].Destination[3]);
		 printf("Protocol: %s\n ModemPath: %s\n \n",my9601[index].Protocol, my9601[index].ModemPath);
		 printf("SerialDescriptor: %i\n SocketDescriptor: %i\n \n",my9601[index].SerialDescriptor, my9601[index].SocketDescriptor);
	}
	return 0;
}

//cleaned
int init9601(char * path, struct Iridium9601 * my9601, int maxModems)
{
	FILE *file;
	int rc;
	int digitValue;
	int modemCount;
	char index[100]={0};
	char value[100]={0};
	struct Iridium9601 * ptr;
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
		else if(!(strcmp(index, "READPORT")))
		{
			digitValue=atoi(value);
			ptr->ReadPort=digitValue;
		}	
		else if(!(strcmp(index, "WRITEPORT")))
		{
			digitValue=atoi(value);
			ptr->WritePort=digitValue;
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
	ptr->SocketDescriptor=initSocket(ptr->Destination, ptr->Protocol, ptr->ReadPort);
	sendInit(ptr->Destination, ptr->Destination, ptr->WritePort);
	return 0;
}


//cleaned
int initSBDIXReturn()
{
	mySBDI.MOSTATUS=-1;
	mySBDI.MOMSN=-1;
	mySBDI.MTSTATUS=-1;
	mySBDI.MTMSN=-1;
	mySBDI.MTLENGTH=-1;
	mySBDI.MTQUEUED=-1;
	return 0;
}
//cleaned
int ParseSBDRT(char * output)
{
	char * token;
	if(verboseon)
	{
		printf("To parse: %s\n",output);

	}		
	token=strtok(output, ":"); 
	token=strtok(NULL, "\n");   
	if(verboseon)
	{
		printf("Token: %s\n",token);
	
	}

	//strncpy(udpSend,token,24);
	sprintf(udpSend,"%s",token);
	return 0;
}

//cleaned
int ParseSBDIX(char * output, struct SBDIXReturn * mySBDI)
{
	char * token;
	token=strtok(output, ": ");
	token=strtok(NULL, ", ");
	mySBDI->MOSTATUS=atoi(token);
	token=strtok(NULL, ", ");
	mySBDI->MOMSN=atoi(token);
	token=strtok(NULL, ", ");
	mySBDI->MTSTATUS=atoi(token);
	token=strtok(NULL, ", ");
	mySBDI->MTMSN=atoi(token);
	token=strtok(NULL, ", ");
	mySBDI->MTLENGTH=atoi(token);
	token=strtok(NULL, "\n");
	mySBDI->MTQUEUED=atoi(token);
	//basic error handling
	if(verboseon)
	{
		printf("SBDIX Return: +SBDIX: %i, %i, %i, %i, %i, %i\n",mySBDI->MOSTATUS, mySBDI->MOMSN, mySBDI->MTSTATUS, mySBDI->MTMSN, mySBDI->MTLENGTH, mySBDI->MTQUEUED);
	}
	/**
	if(logon)
	{
		fprintf(logfp, "SBDIX Return: +SBDIX: %i, %i, %i, %i, %i, %i\n",mySBDI.MOSTATUS,mySBDI.MOMSN, mySBDI.MTSTATUS, mySBDI.MTMSN, mySBDI.MTLENGTH, mySBDI.MTQUEUED);
	}  
	**/       
	if(mySBDI->MOSTATUS == 0)
	{
	//complete success
		return 0;
	}
	else if(1 <= mySBDI->MOSTATUS <=4) 
	{
		//sucess for MO but not for other aspects of SBDIX
		//handle said aspects, if applicable
		return 0;
	}
	else if(5 <= mySBDI->MOSTATUS <= 36)
	{
		//some of these errors are recoverable from
		//go into error recovery
		return -1;	
	}	
	else 
	{
		return -1;
	}
	return 0;		
}

//cleaned
int initSerPort(char * modemPath, int baud)
{
	int fd;
	fd = open(modemPath, O_RDWR | O_NOCTTY ); 
	if (fd <0) {perror(modemPath); return(-1); }
	tcgetattr(fd,&oldtio);
	newtio.c_cflag = BAUDRATE | CRTSCTS | CS8 | CLOCAL | CREAD;
	newtio.c_iflag = IGNPAR | ICRNL;
	newtio.c_oflag = 0;
	newtio.c_lflag = ICANON;
	newtio.c_cc[VINTR]    = 0;     /* Ctrl-c */ 
	newtio.c_cc[VQUIT]    = 0;     /* Ctrl-\ */
	newtio.c_cc[VERASE]   = 0;     /* del */
	newtio.c_cc[VKILL]    = 0;     /* @ */
	newtio.c_cc[VEOF]     = 4;     /* Ctrl-d */
	newtio.c_cc[VTIME]    = 0;     /* inter-character timer unused */
	newtio.c_cc[VMIN]     = 1;     /* blocking read until 1 character arrives */
	newtio.c_cc[VSWTC]    = 0;     /* '\0' */
	newtio.c_cc[VSTART]   = 0;     /* Ctrl-q */ 
	newtio.c_cc[VSTOP]    = 0;     /* Ctrl-s */
	newtio.c_cc[VSUSP]    = 0;     /* Ctrl-z */
	newtio.c_cc[VEOL]     = 0;     /* '\0' */
	newtio.c_cc[VREPRINT] = 0;     /* Ctrl-r */
	newtio.c_cc[VDISCARD] = 0;     /* Ctrl-u */
	newtio.c_cc[VWERASE]  = 0;     /* Ctrl-w */
	newtio.c_cc[VLNEXT]   = 0;     /* Ctrl-v */
	newtio.c_cc[VEOL2]    = 0;     /* '\0' */
	tcflush(fd, TCIFLUSH);
	tcsetattr(fd,TCSANOW,&newtio);
	return fd;
}

//cleaned
int sendInit(int s, int * address, int WritePort) 
{
	if (s == -1) 
	{ 
		printf("BAD SOCKET\n");
		return -1;
	}
	target_host_address.sin_family=PF_INET;
	target_host_address.sin_port=htons(WritePort);
	target_address_holder=(unsigned char*)&target_host_address.sin_addr.s_addr;
	target_address_holder[0]=address[0];
	target_address_holder[1]=address[1];
	target_address_holder[2]=address[2];
	target_address_holder[3]=address[3];
	return 0;
}

//cleaned
int initSocket(int * destination, char * protocol, int ReadPort) 
{
	int s;
	s=socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
	fcntl(s,F_SETFL, O_NONBLOCK);
	if (s == -1) 
	{ 
		printf("BAD SOCKET\n");
		return -1;
	}	
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

//cleaned
int sendData(int s, char * buffer, int bufferSize)
{
	/*send it*/
	int rc;
	rc=sendto(s, buffer, bufferSize, 0, (struct sockaddr*)&target_host_address, sizeof(struct sockaddr));
	return 0;
}

//cleaned
int readData(int s, char * buffer, int bufferSize)
{
	int length;
	/*wait for incoming message*/
	//length = recvfrom(s, buffer, bufferSize, 0, 
	//(struct sockaddr*)&host_address, &hst_addr_size);
	length = recv(s, buffer, bufferSize, 0);
	return length;
}

//cleaned
int sendATCommand(int fd, char * message, char * returnCode)
{
	/** 4/14/2010
	at%R returns a lot of data so the buffer might seem a big.
	Also the operator ={0} initializes the buffer (see global). This was causing
	issues. in V2
	**/
	memset(output,0,sizeof(output)); 
	int res;
	char buf[255]={0};
	write(fd,message,strlen(message));
	while (1) 
	{  
		res = read(fd,buf,255); 
		buf[res]=0;        
		strcat(output,buf);
		if ((strstr(output, returnCode) != NULL))
		{
			break;
		}
		else if((strstr(output, "ERROR") != NULL))
		{
			printf("ERROR with command\n");
			return -1;
		}
	}
	return 0;
}
//cleaned
int main(int argc, char *argv[])
{
	int fd;
	int rc;
	int count;
	if (argc > 1)
	{
		for (count = 1; count < argc; count++)
		{
			if(!strcmp("-v", argv[count]) || !strcmp("-V", argv[count]))
			{
				printf("Running with verbose on\n");
				verboseon =1;
			}
			if(!strcmp(argv[count],"-l") | !strcmp(argv[count],"-L"))
			{
				if((logfp=fopen(logfile,"a+"))==NULL)
				{
					printf("LOG FILE OPENING FAILED\n");
				} 
				else
				{
					printf("Running with log file: 9601.log\n");
					logon=1;
					fprintf(logfp, "_________ NEW LOG ________\n");
				}

			}
		}
	}
	//START of old e9601v2
	int ReadLength=0;
	int index=0;
	struct Iridium9601 my9601[1];
	struct Iridium9601 *ptr; //reference to my9601
	ptr=&my9601[0];
	init9601(PATH, my9601, 1);
	if(verboseon)
	{
		printStructs(my9601, 1);
	}
	initSBDIXReturn();
	// use ptr->ModemPath instead of ModemPathDefault
	fd=initSerPort(ptr->ModemPath, BAUDRATE);
	//init of stuff is done, lets get to work
	//begin loop for now until interrupts from socket enabled.
	while(1)
	{
		ReadLength=readData(ptr->SocketDescriptor, udpIn, sizeof(udpIn)-1);
		// if nothing is read, sleep for a bit
		if (ReadLength <= 0) {
			usleep(100000);
		} else {
			sprintf(udpIn,"%s\r",udpIn); 
			if(verboseon)
			{
				printf("UDP READ: %s\n", udpIn);
			}
			rc=sendATCommand(fd,"AT+SBDWT\r", "READY");
			if(verboseon)
			{
				printf("AT+SBDWT ReturnCode: %s\n", output);
			}
			/**
			if(logon)
			{
				fprintf(logfp, "AT+SBDWT ReturnCode: %s\n", output);
			}
			**/
			//do stuff with return code...i.e. retry
			rc=sendATCommand(fd, udpIn, "OK");
			if(verboseon)
			{
				printf("MESSAGE LOADING ReturnCode: %s\n", output);
			}
			/**
			if(logon)
			{
				fprintf(logfp, "MESSAGE LOADING ReturnCode: %s\n", output);
			}
			**/
			rc=sendATCommand(fd, "AT+SBDIX\r","OK");
			if(verboseon)
			{
				printf("AT+SBIX ReturnCode: %s\n", output);
			}
			/**
			if(logon)
			{
				fprintf(logfp, "AT+SBDIX ReturnCode: %s\n", output);
			} 
			**/
			ParseSBDIX(output, &mySBDI); //time to parse output of SBDIX
			if(mySBDI.MTSTATUS==1)
			{
				/** 
				return code of 0,1,2. 0
				1 ok, 2 are error. 
				0 no message, 1 means message waiting.
				SBDRING will take care of error of 2...maybe?
				**/
				if(verboseon)
				{
					printf("One message Downloaded.\n Messages waiting: %i \n", mySBDI.MTQUEUED);
				}
				/**
				if(logon)
				{
					fprintf(logfp, "One message Downloaded.\n Messages waiting: %i \n", mySBDI.MTQUEUED);
				}
				**/
				//reading of MT messages at gateway      	        
				rc=sendATCommand(fd, "AT+SBDRT\r","OK");
				ParseSBDRT(output);
				sendData(ptr->SocketDescriptor, udpSend, strlen(udpSend));
				if(verboseon)
				{
					printf("Sending UDPData: %s\n", udpSend);
				}
				for(index=1; index < mySBDI.MTQUEUED; index++)
				{
					if(verboseon)
					{
						printf("Retrieving messages from gateway.\n");
					}
					rc=sendATCommand(fd, "AT+SBDIX\r","OK");
					if(verboseon)
					{
						printf("AT+SBIX ReturnCode: %s\n", output);
					}
					/**
					There is a MAX number of messages that may be
					QUEUED. Retrieve messages if available and send to
					destination/port in config file.
					**/
					rc=sendATCommand(fd, "AT+SBDRT\r","OK");
					ParseSBDRT(output);
					sendData(ptr->SocketDescriptor, udpSend, strlen(udpSend));
					if(verboseon)
					{
						printf("Sending UDPData: %s\n", udpSend);
					}
		
				}
			} 
			else
			{
				/**
				retry to do SBDIX if RC is not zero? This case would mean MO was sent
				but MT wasn't received because of bad link conditions.
				**/
			}

		} //end of if readlength > 0
	} //end of main while loop
	return 0;
}


