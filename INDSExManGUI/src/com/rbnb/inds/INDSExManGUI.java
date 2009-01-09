
/*
	INDSExManGUI.java

	Copyright 2009 Creare Inc.

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
	01/07/09  JPW  Created.
*/

package com.rbnb.inds;

import com.rbnb.inds.exec.Remote;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class INDSExManGUI extends JFrame implements ListSelectionListener {
    
    private javax.swing.JList commandLB;
    private javax.swing.JButton connectB;
    private javax.swing.JTextField hostNameTF;
    private javax.swing.JCheckBox showCompletedCommandsCB;
    private javax.swing.JTextArea stderrTA;
    private javax.swing.JTextArea stdoutTA;
    private javax.swing.JTextArea xmlTA;
    
    private Remote remoteObj = null;
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new INDSExManGUI().setVisible(true);
            }
        });
    }

    /*
     * Constructor
     */
    public INDSExManGUI() {
        createGUI();
    }

    /*
     * Initialize the GUI
     */
    private void createGUI() {

        setTitle("INDS Execution Manager");
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        JPanel guiPanel = new JPanel(gbl);

        //
        // Host and connect button
        //
        GridBagLayout tempgbl = new GridBagLayout();
        JPanel tempP = new JPanel(tempgbl);
        JLabel tempL = new JLabel("Host");
        hostNameTF = new JTextField(20);
        connectB = new JButton("Connect");
        connectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchCommandList(evt);
            }
        });
        gbc.insets = new Insets(0,0,0,5);
        add(tempP, tempL, tempgbl, gbc, 0, 0, 1, 1);
        gbc.insets = new Insets(0,0,0,5);
        add(tempP, hostNameTF, tempgbl, gbc, 1, 0, 1, 1);
        gbc.insets = new Insets(0,0,0,5);
        add(tempP, connectB, tempgbl, gbc, 2, 0, 1, 1);
        // Add tempP to guiPanel
        gbc.insets = new Insets(15,15,0,15);
        add(guiPanel, tempP, gbl, gbc, 0, 0, 2, 1);

        //
        // Command listbox
        //
        commandLB = new JList();
        commandLB.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        commandLB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                commandListMouseClickCallback(evt);
            }
        });
        commandLB.addListSelectionListener(this);
        JScrollPane tempSP =
            new JScrollPane(
                commandLB,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(200, 600));
        gbc.insets = new Insets(15,15,15,15);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0;
        gbc.weighty = 100;
        add(guiPanel, tempSP, gbl, gbc, 0, 1, 1, 7);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // Checkbox for displaying/not displaying completed commands
        //
        showCompletedCommandsCB = new JCheckBox("Show completed commands?");
        showCompletedCommandsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Use the same callback as for the "Connect" button
                fetchCommandList(evt);
            }
        });
        gbc.insets = new Insets(15,0,0,15);
        add(guiPanel, showCompletedCommandsCB, gbl, gbc, 1, 1, 1, 1);

        //
        // Std out text area
        //
        tempL = new JLabel("Std out");
        gbc.insets = new Insets(15,0,0,15);
        add(guiPanel, tempL, gbl, gbc, 1, 2, 1, 1);
        stdoutTA = new JTextArea();
        stdoutTA.setEditable(false);
        tempSP =
            new JScrollPane(
                stdoutTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(600, 150));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(5,0,0,15);
        add(guiPanel, tempSP, gbl, gbc, 1, 3, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // Std err text area
        //
        tempL = new JLabel("Std err");
        gbc.insets = new Insets(15,0,0,15);
        add(guiPanel, tempL, gbl, gbc, 1, 4, 1, 1);
        stderrTA = new JTextArea();
        // stderrTA.setColumns(30);
        stderrTA.setEditable(false);
        // stderrTA.setRows(8);
        tempSP =
            new JScrollPane(
                stderrTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(600, 150));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(5,0,0,15);
        add(guiPanel, tempSP, gbl, gbc, 1, 5, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // XML text area
        //
        tempL = new JLabel("XML");
        gbc.insets = new Insets(15,0,0,15);
        add(guiPanel, tempL, gbl, gbc, 1, 6, 1, 1);
        xmlTA = new JTextArea();
        // xmlTA.setColumns(30);
        xmlTA.setEditable(false);
        // xmlTA.setRows(5);
        tempSP =
            new JScrollPane(
                xmlTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(600, 100));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets = new Insets(5,0,15,15);
        add(guiPanel, tempSP, gbl, gbc, 1, 7, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // Now add guiPanel to the frame
        //
        // gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,0,0,0);
        GridBagLayout framegbl = new GridBagLayout();
        setLayout(framegbl);
        add(this.getContentPane(),guiPanel,framegbl,gbc,0,0,1,1);
        
        pack();
        
    }

    private void commandListMouseClickCallback(java.awt.event.MouseEvent evt) {
        fetchCommandData();
    }

    public void valueChanged(ListSelectionEvent e) {
        fetchCommandData();
    }

    private void fetchCommandData() {
        try {
            // User clicked on a command in the list box; get associated
            // information via the RMI interface.
            // int index = commandLB.locationToIndex(evt.getPoint());
            String command = (String) commandLB.getSelectedValue();
            stdoutTA.setText(remoteObj.getCommandOut(command));
            stderrTA.setText(remoteObj.getCommandError(command));
            xmlTA.setText(remoteObj.getConfiguration(command));
        } catch (RemoteException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fetchCommandList(java.awt.event.ActionEvent evt) {
        String[] commands = null;
        String hostName = hostNameTF.getText().trim();
        DefaultListModel commandModel = new DefaultListModel();
        try {
            // Connect using RMI:
            java.rmi.registry.Registry reg =
                java.rmi.registry.LocateRegistry.getRegistry(hostName);
            String[] names = reg.list();
            int index = 0;
            remoteObj = (Remote) reg.lookup(names[index]);
            commands = remoteObj.getCommandList();
        } catch (NotBoundException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (commands != null) {
            // Clear out the text boxes
            stderrTA.setText("");
            stdoutTA.setText("");
            xmlTA.setText("");
            boolean bShowCompletedCommands = showCompletedCommandsCB.isSelected();
            for (String cmd : commands) {
                if (!bShowCompletedCommands) {
                    try {
                        if (!remoteObj.isComplete(cmd)) {
                            commandModel.addElement(cmd);
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    commandModel.addElement(cmd);
                }
            }
            commandLB.setModel(commandModel);
        }
    }

    /**************************************************************************
     * Add a component to a contain in the GUI using the GridBagLayout manager
     * <p>
     *
     * @author John P. Wilson
     *
     * @param container		Container to add Component to
     * @param c			Component to add to the Container
     * @param gbl		GridBagLayout manager to use
     * @param gbc		GridBagConstrains to use to add ther Component
     * @param x			Desired row position of the Component
     * @param y			Desired column position of the Component
     * @param w			Num of columns (width) Component should occupy
     * @param h			Num of rows (height) Component should occupy
     *
     * @version 01/08/2009
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/08/2009  JPW  Created. Taken from com.rbnb.utility.Utility
     *
     */

    public static void add(
        Container container,
        Component c,
        GridBagLayout gbl,
        GridBagConstraints gbc,
        int x, int y, int w, int h)
    {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        gbl.setConstraints(c,gbc);
        container.add(c);
    }
    
}
