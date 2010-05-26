#include <stdio.h>   
#include <string.h>  
#include <unistd.h>  
#include <fcntl.h>   
#include <errno.h>   
#include <termios.h> 
long BAUDRATE=B19200; //default
//function Definitions
void print_str_to_hex(unsigned char *char_string);

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


