
Iridium driver code

Developed by Christian Rodriguez
NASA-sponsored FIU grad student
crodr@fiu.edu

These notes compiled by John Wilson, Erigo Technologies

This driver communicates with either a 9601 or 9602 SBD modem to send MO or receive MT messages.  It has
a UDP backend for message passing and an Iridium SBD front-end.  The user specifies configuration
properties in a file called "9601.cfg".  Configuration information includes the serial port to use to
communicate with the modem, the communication baudrate, and what UDP ports to use for backend message
passing.

As an example, the user can specify that the driver should receive UDP messages at port 5000.  In this case,
the user can send UDP messages to the driver at this port, and these messages will then be sent off as MO
messages via Iridium.  The user also specifies  what UDP address and port the driver should send MT messages
to; in this case, MT received messages will be sent out to this address:port.

Christian has some notes in his code about future development ideas.  Here are some additional thoughts:

1. Use stderr for all output.

2. Logfile output isn’t currently needed, but what we could have in the code is a general output
   logging function that sends output to stderr and/or logfile based on user command line flags.
   Any output from the program should then go through this function (in other words, the only place
   “printf” or “fprintf” will show up in the code is in this logging function).

3. Cleanup/streamline all output messages

4. Have a timeout waiting for responses from modem.

   - One situation this would be useful for is if there is a modem power glitch.  Say the driver has issued
     the AT+SBDWT command, text is entered, and then the driver issues AT+SBDIX.  Then a power glitch occurs
     and the modem resets.  In this case, the driver just hangs waiting for the response to the last command.

   - Another situation I’ve noticed (although it doesn’t seem to occur all the time); this is more of a
     modem hardware issue, but I’ll include it here for completeness: let’s say the modem has completed
     sending out a message.  Then, before the next message arrives, there is a modem power glitch, but the
     modem comes back up.  Then a new message comes in via UDP to the driver.  The driver works fine, but
     I see error codes in the SBIX return “MO status” field; I’ll get codes 13, 14, 18, or 19.  The modem
     stays in this state for any further messages that come in.  The manual fix to correct this problem is that
     I need to kill the driver, power cycle the modem, then start the driver again.

5. Something to consider: Regardless of whether the UDP input is arriving faster than the driver can send it
   out, the driver still steps through each message one at a time (and thus will get further and further
   behind).  I’m not sure, but it is possible we’d want to skip ahead to the newest message and forget the
   other messages that were piling up.

6. Christian mentioned the following driver hangup issue; his is with USB (I assume he is talking to the
   9601 modem via a USB-to-serial adapter?).
   
   a. The driver starts on USB port 1, lets say.
   b. The modem power cycles
   c. Then the modem gets assigned USB port 2
   d. Drive is still thinking it is on 1
