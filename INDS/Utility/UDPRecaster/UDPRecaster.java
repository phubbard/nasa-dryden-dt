// UDPRecaster - receives UDP packets, resends them to specified hosts/ports
// EMF
// 6/6/2005
// for IOScan
// Copyright 2005 Creare Incorporated

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPRecaster {
	int serversocket=5000;
	DatagramPacket[] echoHostsP=new DatagramPacket[0];
	DatagramPacket[] noechoHostsP=new DatagramPacket[0];
	DatagramSocket ds=null;

	
	public static void main(String[] arg) {
		new UDPRecaster(arg).start();
	}
	
	public UDPRecaster(String[] arg) {
		try {
			if (arg!=null) for (int i=0;i<arg.length;i++) {
				if (arg[i].equals("-h")) {
					arg=null;
					break;
				} else if (arg[i].equals("-s")) {
					serversocket=Integer.parseInt(arg[++i]);
					System.err.println("Listening for UDP packets on serversocket "+serversocket);
				} else if (arg[i].equals("-e")) {
					echoHostsP=parseHosts(arg[++i]);
				} else if (arg[i].equals("-n")) {
					noechoHostsP=parseHosts(arg[++i]);
				} else {
					arg=null;
					break;
				}
			}
		} catch (Exception e) {
			arg=null;
		}
		if (arg==null || arg.length==0) {
			System.err.println("UDPRecaster");
			System.err.println(" -h                  : print this usage info");
			System.err.println(" -s <server socket>  : socket number to listen for UDP packets on");
			System.err.println("             default : 5000");
			System.err.println(" -e <host:port,...>  : echo packets from these hosts");
			System.err.println(" -n <host:port,...>  : do not echo packets from these hosts");
			System.exit(0);
		}
		
		if (echoHostsP.length==0 && noechoHostsP.length==0) {
			System.err.println("no hosts specified to forward UDP packets to, exiting");
			System.exit(0);
		}
		
		System.err.println("Forwarding UDP packets to:");
		for (int i=0;i<echoHostsP.length;i++) {
			System.err.println(echoHostsP[i].getSocketAddress()+" with echo");
		}
		for (int i=0;i<noechoHostsP.length;i++) {
			System.err.println(noechoHostsP[i].getSocketAddress()+" without echo");
		}
		
		try {
			ds=new DatagramSocket(serversocket);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	} //end UDPRecaster constructor
	
	private DatagramPacket[] parseHosts(String in) {
		String[] hp=in.split(",");
		DatagramPacket[] hostsP=new DatagramPacket[hp.length];
		for (int i=0;i<hp.length;i++) {
			int n=hp[i].indexOf(':');
			String name=hp[i].substring(0,n);
			int port=Integer.parseInt(hp[i].substring(n+1));
			InetSocketAddress isa=new InetSocketAddress(name,port);
			hostsP[i]=new DatagramPacket(new byte[65536],65536);
			hostsP[i].setSocketAddress(isa);
		}
		return hostsP;
	} //end parseHosts method
	
	public void start() {
		try {
			DatagramPacket dpin=new DatagramPacket(new byte[65536],65536);
			while (true) {
				ds.receive(dpin);
				System.err.println("\nreceived "+dpin.getLength()+" bytes from "+dpin.getSocketAddress());
				if (dpin.getLength()>0) {
					SocketAddress sa=dpin.getSocketAddress();
					for (int i=0;i<echoHostsP.length;i++) {
						echoHostsP[i].setData(dpin.getData(),dpin.getOffset(),dpin.getLength());
						ds.send(echoHostsP[i]);
System.err.println("forwarded to "+echoHostsP[i].getSocketAddress());
					}
					for (int i=0;i<noechoHostsP.length;i++) {
						if (!((InetSocketAddress)noechoHostsP[i].getSocketAddress()).equals(dpin.getSocketAddress())) {
							noechoHostsP[i].setData(dpin.getData(),dpin.getOffset(),dpin.getLength());
							ds.send(noechoHostsP[i]);
System.err.println("forwarded to "+noechoHostsP[i].getSocketAddress());
						} else {
System.err.println("not forwarded to "+noechoHostsP[i].getSocketAddress());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	} //end start method
	
} //end UDPRecaster class

