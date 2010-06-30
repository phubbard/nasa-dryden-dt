
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class INDSExManGUI extends JFrame implements ListSelectionListener, ListCellRenderer  {

    private JTextField hostNameTF;
    private JButton connectB;
    private JButton terminateB;
    private JList commandLB;
    private JCheckBox showCompletedCommandsCB;
    private JLabel processIDLabel;
    private JLabel classificationLabel;
    private JTextArea stderrTA;
    private JTextArea stdoutTA;
    private JTextArea xmlTA;
    private JTextArea configTA;
    
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
        // JPW 06/30/2010: Add terminate button
        //
        GridBagLayout tempgbl = new GridBagLayout();
        JPanel tempP = new JPanel(tempgbl);
        JLabel tempL = new JLabel("Host");
        hostNameTF = new JTextField(20);
        hostNameTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchCommandList(evt);
            }
        });
        connectB = new JButton("Connect");
        connectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchCommandList(evt);
            }
        });
        terminateB = new JButton("Terminate IEM");
        terminateB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if (remoteObj == null) {
            	    return;
            	}
            	try {
            	    remoteObj.terminateIEM();
            	} catch (RemoteException re) {
            	    System.err.println("Caught exception trying to terminate IEM:\n" + re);
            	}
            }
        });
        gbc.insets = new Insets(0,0,0,5);
        add(tempP, tempL, tempgbl, gbc, 0, 0, 1, 1);
        gbc.insets = new Insets(0,0,0,5);
        add(tempP, hostNameTF, tempgbl, gbc, 1, 0, 1, 1);
        gbc.insets = new Insets(0,0,0,30);
        add(tempP, connectB, tempgbl, gbc, 2, 0, 1, 1);
        gbc.insets = new Insets(0,0,0,0);
        add(tempP, terminateB, tempgbl, gbc, 3, 0, 1, 1);
        // Add tempP to guiPanel
        gbc.insets = new Insets(15,15,0,15);
        add(guiPanel, tempP, gbl, gbc, 0, 0, 2, 1);

        //
        // We will construct a JSplitPane where the left panel contains the
        // command listbox and the right panel contains the display of
        // isCompleted, stdout, stderr, and XML snippet.

        //
        // Command listbox
        //
        commandLB = new JList();
        commandLB.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        /*
         * Use the list selection listener instead of mouse listener
         *
        commandLB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                commandListMouseClickCallback(evt);
            }
        });
        */
        commandLB.addListSelectionListener(this);
        commandLB.setCellRenderer(this);
        JScrollPane commandSP =
            new JScrollPane(
                commandLB,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        commandSP.setMinimumSize(new Dimension(0,0));

        //
        // Construct the right hand panel which contains:
        // processID, classification string, isCompleted, stdout, stderr, and XML snippet.
        //
        tempgbl = new GridBagLayout();
        JPanel rightPanel = new JPanel(tempgbl);

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
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, showCompletedCommandsCB, tempgbl, gbc, 0, 0, 1, 1);

        //
        // label for displaying the process ID
        //
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempL = new JLabel("Process ID:  ");
        labelPanel.add(tempL);
        processIDLabel = new JLabel();
        labelPanel.add(processIDLabel);
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, labelPanel, tempgbl, gbc, 0, 1, 1, 1);
        
        //
        // label for displaying the process classification
        //
        labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempL = new JLabel("Classification:  ");
        labelPanel.add(tempL);
        classificationLabel = new JLabel();
        labelPanel.add(classificationLabel);
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, labelPanel, tempgbl, gbc, 0, 2, 1, 1);

        //
        // Std out text area
        //
        tempL = new JLabel("Std out");
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, tempL, tempgbl, gbc, 0, 3, 1, 1);
        stdoutTA = new JTextArea();
        stdoutTA.setEditable(false);
        JScrollPane tempSP =
            new JScrollPane(
                stdoutTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(300, 75));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,5,0,5);
        add(rightPanel, tempSP, tempgbl, gbc, 0, 4, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // Std err text area
        //
        tempL = new JLabel("Std err");
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, tempL, tempgbl, gbc, 0, 5, 1, 1);
        stderrTA = new JTextArea();
        stderrTA.setEditable(false);
        tempSP =
            new JScrollPane(
                stderrTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(300, 75));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,5,0,5);
        add(rightPanel, tempSP, tempgbl, gbc, 0, 6, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //
        // XML text area
        //
        tempL = new JLabel("XML");
        gbc.insets = new Insets(5,5,0,5);
        add(rightPanel, tempL, tempgbl, gbc, 0, 7, 1, 1);
        xmlTA = new JTextArea();
        xmlTA.setEditable(false);
        tempSP =
            new JScrollPane(
                xmlTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(300, 75));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets = new Insets(0,5,5,5);
        add(rightPanel, tempSP, tempgbl, gbc, 0, 8, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        //Create the split pane to contain commandSP and rightPanel
        JSplitPane topSplitPane =
            new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                commandSP,
                rightPanel);
        // To avoid the problem of multiple borders on nested frames
        // (see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4131528)
        topSplitPane.setBorder(null);
        topSplitPane.setOneTouchExpandable(true);
        topSplitPane.setDividerLocation(150);

        //
        // Construct the bottom panel which contains the config file output
        //
        tempgbl = new GridBagLayout();
        JPanel bottomPanel = new JPanel(tempgbl);

        // Config file text area
        tempL = new JLabel("Config file");
        gbc.insets = new Insets(5,5,0,5);
        add(bottomPanel, tempL, tempgbl, gbc, 0, 0, 1, 1);
        configTA = new JTextArea();
        configTA.setEditable(false);
        tempSP =
            new JScrollPane(
                configTA,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tempSP.setPreferredSize(new Dimension(450, 75));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0,5,5,5);
        add(bottomPanel, tempSP, tempgbl, gbc, 0, 1, 1, 1);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        
        //
        // Create another split pane which contains:
        //     topSplitPane
        //     bottomPanel
        JSplitPane parentSplitPane =
            new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topSplitPane,
                bottomPanel);
        parentSplitPane.setOneTouchExpandable(true);
        parentSplitPane.setDividerLocation(350);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(15,15,15,15);
        add(guiPanel, parentSplitPane, gbl, gbc, 0, 1, 1, 1);
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
    
    /*
    private void commandListMouseClickCallback(java.awt.event.MouseEvent evt) {
        fetchCommandData();
    }
    */

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            // wait until no more adjusting and then fetch the command data
            return;
        }
        fetchCommandData();
    }

    private void fetchCommandData() {
        try {
            // User clicked on a command in the list box; get associated
            // information via the RMI interface.
            // int index = commandLB.locationToIndex(evt.getPoint());
            String command = (String) commandLB.getSelectedValue();
            // System.err.println("command = " + command);
            if ( (command == null) || (command.trim().equals("")) ) {
                return;
            }
            processIDLabel.setText(command);
            classificationLabel.setText(remoteObj.getCommandClassification(command));
            stdoutTA.setText(remoteObj.getCommandOut(command));
            stderrTA.setText(remoteObj.getCommandError(command));
            xmlTA.setText(remoteObj.getConfiguration(command));
            configTA.setText(remoteObj.getChildConfiguration(command));
        } catch (RemoteException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fetchCommandList(java.awt.event.ActionEvent evt) {
        String[] processIDs = null;
        String hostName = hostNameTF.getText().trim();
        DefaultListModel commandModel = new DefaultListModel();
        try {
            // Connect using RMI:
            java.rmi.registry.Registry reg =
                java.rmi.registry.LocateRegistry.getRegistry(hostName);
            String[] names = reg.list();
            int index = 0;
            remoteObj = (Remote) reg.lookup(names[index]);
            processIDs = remoteObj.getCommandList();
        } catch (NotBoundException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (processIDs != null) {
            // Clear out the labels and text boxes
            processIDLabel.setText("");
            classificationLabel.setText("");
            stderrTA.setText("");
            stdoutTA.setText("");
            xmlTA.setText("");
            configTA.setText("");
            boolean bShowCompletedCommands = showCompletedCommandsCB.isSelected();
            for (String id : processIDs) {
                if (!bShowCompletedCommands) {
                    try {
                        if (!remoteObj.isComplete(id)) {
                            commandModel.addElement(id);
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    commandModel.addElement(id);
                }
            }
            commandLB.setModel(commandModel);
        }
    }

    /*
     * A renderer for the JList - we will display the nicely formatted node
     * name rather than the process ID.
     */
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
        JLabel listBoxLabel = new JLabel();
        String processIDStr = value.toString();
        // Get the nicely formatted string associated with this processID
        String niceName = null;
        try {
            niceName = remoteObj.getName(processIDStr);
        } catch (RemoteException ex) {
            Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        listBoxLabel.setText(niceName);
        if (isSelected) {
            listBoxLabel.setBackground(list.getSelectionBackground());
            listBoxLabel.setForeground(list.getSelectionForeground());
        } else {
            listBoxLabel.setBackground(list.getBackground());
            try {
                if (remoteObj.isComplete(processIDStr)) {
                    listBoxLabel.setForeground(Color.RED);
                } else {
                    listBoxLabel.setForeground(list.getForeground());
                }
            } catch (RemoteException ex) {
                Logger.getLogger(INDSExManGUI.class.getName()).log(Level.SEVERE, null, ex);
                listBoxLabel.setForeground(list.getForeground());
            }
        }
        listBoxLabel.setEnabled(list.isEnabled());
        listBoxLabel.setFont(list.getFont());
        listBoxLabel.setOpaque(true);
        return listBoxLabel;
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
