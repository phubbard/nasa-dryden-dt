
/*
	INDSExManShutdown.java

	Copyright 2010 Erigo Technologies LLC

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


	---  History  ---
	06/30/2010  JPW  Created.
*/

package com.rbnb.inds;

import com.rbnb.inds.exec.Remote;

public class INDSExManShutdown {
    
    private Remote remoteObj = null;
    
    public static void main(String[] args) {
    	System.err.println("Try to shut down local INDS Execution Manager...");
        try {
            // Connect using RMI
            java.rmi.registry.Registry reg =
                java.rmi.registry.LocateRegistry.getRegistry("localhost");
            String[] names = reg.list();
            Remote remoteObj = (Remote) reg.lookup(names[0]);
            remoteObj.terminateIEM();
            System.err.println("Shut down successful.");
        } catch (Exception e) {
            System.err.println("Caught exception trying to terminate IEM:\n" + e);
        }
    }
    
    /*
     * Constructor
     */
    public INDSExManShutdown() {
        
    }
    
}
