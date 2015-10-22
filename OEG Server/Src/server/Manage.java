package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import shared.Messages;
import shared.action.Action;

/**
 * Manage is the GUI that is used by the person managing the server and
 * gameplay. It allows the manager to view what actions each Operator is taking,
 * and a log of everything that has happened in the past. It displays current
 * round, timer, and game status.
 * 
 * @author jweber2, ndickrel
 */
public class Manage {
    /** The frame that contains everything */
    private JFrame frame;
    /** List model to display the list of operators in the game */
    private DefaultListModel operatorModel;
    /** List view to display the operators in operatorModel */
    private JList operatorList;
    /** List view to display the actions for the selected operator */
    private JList actionQueueList;
    /** List view to display the history of the selected operator */
    private JList historyList;
    /** The text field to hold the IP address of the server */
    private JTextField IPTextField;
    /** The label to display the current round number */
    private JLabel roundLabel;
    /** The label to display the current status of the game/server */
    private JLabel statusLabel;
    /** The label to display the current balance for the selected op */
    private JLabel bankLabel;
    /** The label to display the current income for the selected op */
    private JLabel incomeLabel;
    /** The label to display the remaining time in the current round */
    private JLabel timerLabel;

    /** The Director that has the Manage - this is a circular reference!! */
    Director director;
    /** How many milliseconds remain in the UI count down. */
    private long remaining;
    /** The current round number */
    private int curRound;
    /** When countdown was last updated */
    private long lastUpdate;
    /** The timer object to handle the count down UI. */
    private RoundTimer roundTimer;
    /** Format that the timer field is set to use */
    private NumberFormat format;

    /**
     * Create the application.
     * 
     * @param IP
     *            The IP address of the local computer.
     */
    public Manage(String IP, Director directorIn) {
        // this.manage = this;
        director = directorIn;
        initialize();
        // Timer
        format = NumberFormat.getNumberInstance();
        format.setMinimumIntegerDigits(2); // pad with 0 if necessary
        lastUpdate = System.currentTimeMillis();
        // setting the text for the IP
        IPTextField.setText(IP);
        statusLabel.setText("Not Running");
        // new timer started
        roundTimer = new RoundTimer();
        roundTimer.resume();

        // if in testing mode, start server automatically
        if (Messages.TESTMODE) {
            Server server = new Server(this, null);
            server.start();
        }
    }

    /** Update the Director to avoid null pointers */
    public void setDirector(Director director) {
        this.director = director;
    }

    public boolean registerDisabled;

    /** Initialize all the contents of the frame with swing objects. */
    private void initialize() {
        // creates new jframe
        final JFrame frmOegDirector = frame = new JFrame();
        frmOegDirector.setTitle("Oil Director");
        frmOegDirector.setBounds(100, 100, 700, 550);
        frmOegDirector.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 3, 55, 13, 73, 14, 50, -46, 76,
                        60, 0, 54, 0, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 37, 28, 109, 33, 25, 0, 22, 160,
                        0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 1.0,
                        0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
                        Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                        0.0, 1.0, 0.0, Double.MIN_VALUE };
        frmOegDirector.getContentPane().setLayout(gridBagLayout);
        // Timer Label
        JLabel lblTimer = new JLabel("Timer:");
        lblTimer.setFont(new Font("Dialog", Font.BOLD, 16));
        GridBagConstraints gbc_lblTimer = new GridBagConstraints();
        gbc_lblTimer.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblTimer.insets = new Insets(0, 0, 5, 5);
        gbc_lblTimer.gridx = 6;
        gbc_lblTimer.gridy = 0;
        frmOegDirector.getContentPane().add(lblTimer, gbc_lblTimer);

        timerLabel = new JLabel("");
        timerLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        GridBagConstraints gbc_timerLabel = new GridBagConstraints();
        gbc_timerLabel.insets = new Insets(0, 0, 5, 5);
        gbc_timerLabel.gridx = 7;
        gbc_timerLabel.gridy = 0;
        frmOegDirector.getContentPane().add(timerLabel, gbc_timerLabel);
        // Round Label
        JLabel lblRound = new JLabel("Round: ");
        lblRound.setFont(new Font("Dialog", Font.BOLD, 16));
        GridBagConstraints gbc_lblRound = new GridBagConstraints();
        gbc_lblRound.anchor = GridBagConstraints.EAST;
        gbc_lblRound.insets = new Insets(0, 0, 5, 5);
        gbc_lblRound.gridx = 9;
        gbc_lblRound.gridy = 0;
        frmOegDirector.getContentPane().add(lblRound, gbc_lblRound);

        roundLabel = new JLabel("");
        roundLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        GridBagConstraints gbc_roundLabel = new GridBagConstraints();
        gbc_roundLabel.anchor = GridBagConstraints.WEST;
        gbc_roundLabel.insets = new Insets(0, 0, 5, 5);
        gbc_roundLabel.gridx = 10;
        gbc_roundLabel.gridy = 0;
        frmOegDirector.getContentPane().add(roundLabel, gbc_roundLabel);

        // Second row labels
        JLabel lblTeamName = new JLabel("Teams");
        lblTeamName.setHorizontalAlignment(SwingConstants.CENTER);
        lblTeamName.setFont(new Font("Dialog", Font.BOLD, 20));
        GridBagConstraints gbc_lblTeamName = new GridBagConstraints();
        gbc_lblTeamName.gridwidth = 5;
        gbc_lblTeamName.anchor = GridBagConstraints.SOUTH;
        gbc_lblTeamName.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblTeamName.insets = new Insets(0, 0, 5, 5);
        gbc_lblTeamName.gridx = 1;
        gbc_lblTeamName.gridy = 1;
        frmOegDirector.getContentPane().add(lblTeamName, gbc_lblTeamName);

        JScrollPane operatorScrollPane = new JScrollPane();
        GridBagConstraints gbc_operatorScrollPane = new GridBagConstraints();
        gbc_operatorScrollPane.gridwidth = 5;
        gbc_operatorScrollPane.fill = GridBagConstraints.BOTH;
        gbc_operatorScrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_operatorScrollPane.gridheight = 2;
        gbc_operatorScrollPane.gridx = 1;
        gbc_operatorScrollPane.gridy = 2;
        frmOegDirector.getContentPane().add(operatorScrollPane,
                        gbc_operatorScrollPane);

        operatorModel = new DefaultListModel();
        operatorList = new JList(operatorModel);
        operatorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                Manage.this.refreshInfo();
                if (SwingUtilities.isRightMouseButton(arg0)) {
                    final ServerOperator operator = (ServerOperator) operatorList
                                    .getSelectedValue();
                    JPopupMenu menu = new JPopupMenu();
                    // View Password
                    JMenuItem pass = new JMenuItem("Copy password");
                    pass.setEnabled(true);
                    pass.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            StringSelection selection = new StringSelection(
                                            operator.password);
                            Clipboard clipboard = Toolkit.getDefaultToolkit()
                                            .getSystemClipboard();
                            clipboard.setContents(selection, selection);
                        }
                    });
                    menu.add(pass);
                    // Send a PM
                    JMenuItem sendMessage = new JMenuItem("Send Message");
                    sendMessage.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            String message = JOptionPane.showInputDialog(frame,
                                            "Enter message:",
                                            "Message " + operator.getName(),
                                            JOptionPane.PLAIN_MESSAGE);
                            operator.sendInfo(message);
                        }
                    });
                    menu.add(sendMessage);
                    // Change Password
                    JMenuItem changePassword = new JMenuItem("Change Password");
                    changePassword.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            String message = JOptionPane.showInputDialog(frame,
                                            "Changes will reflect at the beginning of the next round.\n"
                                                            + "Enter New Password:",
                                            "Change Password for " /*
                                                                    * +
                                                                    * operator.
                                                                    * getName()
                                                                    */,
                                            JOptionPane.PLAIN_MESSAGE);
                            if (message != null)
                                operator.password = message;
                        }
                    });
                    menu.add(changePassword);

                    // Change bank balance
                    JMenuItem changeBank = new JMenuItem("Modify Bank Balance");
                    changeBank.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            String message = JOptionPane.showInputDialog(frame,
                                            "Changes will reflect at the beginning of the next round.\n"
                                                            + "Enter Amount to add:",
                                            "Add to " + operator.getName()
                                                            + "'s Balance",
                                            JOptionPane.PLAIN_MESSAGE);
                            int changeBy = Integer.parseInt(message);
                            String operatorAlert = String.format(
                                            "The Bank has decided to %s $%d.00 to your account."
                                                            + "The changes will reflect on the next round.",
                                            (changeBy > 0 ? "credit" : "bill"),
                                            changeBy);
                            operator.sendInfo(operatorAlert);
                            operator.setBalance(
                                            operator.getBalance() + changeBy);
                        }
                    });
                    menu.add(changeBank);
                    // Delete team
                    JMenuItem delete = new JMenuItem("Delete Team");
                    delete.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            operator.sendWarning(
                                            "You have been deleted by the director.");
                            operator.closeSocket();
                            operatorModel.removeElement(operator);
                        }
                    });
                    menu.add(delete);

                    menu.show(arg0.getComponent(), 30, 20);
                }
            }
        });
        operatorScrollPane.setViewportView(operatorList);
        // history log of game
        Label historyLabel = new Label("History");
        historyLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        historyLabel.setAlignment(Label.CENTER);
        GridBagConstraints gbc_historyLabel = new GridBagConstraints();
        gbc_historyLabel.anchor = GridBagConstraints.SOUTH;
        gbc_historyLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_historyLabel.insets = new Insets(0, 0, 5, 5);
        gbc_historyLabel.gridwidth = 4;
        gbc_historyLabel.gridx = 7;
        gbc_historyLabel.gridy = 1;
        frmOegDirector.getContentPane().add(historyLabel, gbc_historyLabel);

        JPanel moneyPanel = new JPanel();
        GridBagConstraints gbc_moneyPanel = new GridBagConstraints();
        gbc_moneyPanel.gridwidth = 5;
        gbc_moneyPanel.insets = new Insets(0, 0, 5, 5);
        gbc_moneyPanel.fill = GridBagConstraints.BOTH;
        gbc_moneyPanel.gridx = 1;
        gbc_moneyPanel.gridy = 4;
        frmOegDirector.getContentPane().add(moneyPanel, gbc_moneyPanel);
        moneyPanel.setLayout(new BorderLayout(0, 0));

        JPanel bankPanel = new JPanel();
        moneyPanel.add(bankPanel, BorderLayout.WEST);
        bankPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, -5));

        JLabel bankTitleLabel = new JLabel("Bank:");
        bankPanel.add(bankTitleLabel);
        bankTitleLabel.setFont(new Font("Dialog", Font.BOLD, 18));

        bankLabel = new JLabel("");
        bankPanel.add(bankLabel);
        bankLabel.setFont(new Font("Dialog", Font.BOLD, 16));

        JPanel incomePanel = new JPanel();
        moneyPanel.add(incomePanel, BorderLayout.EAST);
        FlowLayout fl_incomePanel = new FlowLayout(FlowLayout.RIGHT, 2, -5);
        incomePanel.setLayout(fl_incomePanel);

        JLabel lblIncome = new JLabel("Income:");
        lblIncome.setFont(new Font("Dialog", Font.BOLD, 18));
        lblIncome.setHorizontalAlignment(SwingConstants.RIGHT);
        incomePanel.add(lblIncome);

        incomeLabel = new JLabel("");
        incomeLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        incomePanel.add(incomeLabel);

        JScrollPane scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.gridwidth = 4;
        gbc_scrollPane_1.gridheight = 6;
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 7;
        gbc_scrollPane_1.gridy = 2;
        frmOegDirector.getContentPane().add(scrollPane_1, gbc_scrollPane_1);

        historyList = new JList();
        historyList.setCellRenderer(new MyCellRenderer());
        scrollPane_1.setViewportView(historyList);

        Label actionQLabel = new Label("Action Queue");
        actionQLabel.setAlignment(Label.CENTER);
        actionQLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        GridBagConstraints gbc_actionQLabel = new GridBagConstraints();
        gbc_actionQLabel.gridwidth = 5;
        gbc_actionQLabel.fill = GridBagConstraints.BOTH;
        gbc_actionQLabel.insets = new Insets(0, 0, 5, 5);
        gbc_actionQLabel.gridx = 1;
        gbc_actionQLabel.gridy = 6;
        frmOegDirector.getContentPane().add(actionQLabel, gbc_actionQLabel);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 5;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 7;
        frmOegDirector.getContentPane().add(scrollPane, gbc_scrollPane);

        actionQueueList = new JList();
        actionQueueList.setCellRenderer(new MyCellRenderer());
        actionQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionQueueList.setSelectionForeground(Color.BLACK);
        actionQueueList.setSelectionBackground(Color.WHITE);
        actionQueueList.setRequestFocusEnabled(false);
        actionQueueList.setForeground(Color.BLACK);
        actionQueueList.setFocusable(false);
        scrollPane.setViewportView(actionQueueList);
        GridBagConstraints gbc_IPLabel = new GridBagConstraints();
        gbc_IPLabel.anchor = GridBagConstraints.WEST;
        gbc_IPLabel.insets = new Insets(0, 0, 0, 5);
        gbc_IPLabel.gridx = 1;
        gbc_IPLabel.gridy = 8;
        JLabel IPLabel = new JLabel("IP Address: ");
        IPLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        frmOegDirector.getContentPane().add(IPLabel, gbc_IPLabel);

        IPTextField = new JTextField();
        IPTextField.setFont(new Font("Dialog", Font.PLAIN, 11));
        IPTextField.setBorder(null);
        IPTextField.setEditable(false);
        GridBagConstraints gbc_IPTextField = new GridBagConstraints();
        gbc_IPTextField.gridwidth = 2;
        gbc_IPTextField.insets = new Insets(0, 0, 0, 5);
        gbc_IPTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_IPTextField.gridx = 2;
        gbc_IPTextField.gridy = 8;
        frmOegDirector.getContentPane().add(IPTextField, gbc_IPTextField);
        IPTextField.setColumns(10);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("Dialog", Font.PLAIN, 11));
        GridBagConstraints gbc_lblStatus = new GridBagConstraints();
        gbc_lblStatus.anchor = GridBagConstraints.EAST;
        gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
        gbc_lblStatus.gridx = 4;
        gbc_lblStatus.gridy = 8;
        frmOegDirector.getContentPane().add(lblStatus, gbc_lblStatus);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        GridBagConstraints gbc_statusLabel = new GridBagConstraints();
        gbc_statusLabel.anchor = GridBagConstraints.WEST;
        gbc_statusLabel.gridwidth = 5;
        gbc_statusLabel.insets = new Insets(0, 0, 0, 5);
        gbc_statusLabel.gridx = 5;
        gbc_statusLabel.gridy = 8;
        frmOegDirector.getContentPane().add(statusLabel, gbc_statusLabel);

        JLabel statusL = new JLabel("");
        GridBagConstraints gbc_statusL = new GridBagConstraints();
        gbc_statusL.anchor = GridBagConstraints.WEST;
        gbc_statusL.insets = new Insets(0, 0, 0, 5);
        gbc_statusL.gridx = 6;
        gbc_statusL.gridy = 8;
        frmOegDirector.getContentPane().add(statusL, gbc_statusL);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);
        frmOegDirector.setJMenuBar(menuBar);

        JMenu mnCopy = new JMenu("Copy");
        JMenu mnTeam = new JMenu("All Teams");
        JMenu mnRndCtrl = new JMenu("Control");

        menuBar.add(mnRndCtrl);
        menuBar.add(mnTeam);
        menuBar.add(mnCopy);

        // Control -- Select XML
        final JMenuItem mntmLoadXMLFile = new JMenuItem("Load Simulation");
        mntmLoadXMLFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                File file = new File(".");
                File tmp = new File(file, "trunk");
                if (tmp.isDirectory())
                    file = tmp;
                tmp = new File(tmp, "datafiles");
                if (tmp.isDirectory())
                    file = tmp;
                JFileChooser chooser = new JFileChooser(file);
                FileFilter xmlFilter = new FileNameExtensionFilter(
                                "XML File (.xml)", "xml");
                chooser.addChoosableFileFilter(xmlFilter);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(xmlFilter);
                int returnVal = chooser.showOpenDialog(frmOegDirector);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    // This is where a real application would open the
                    // file.//HELP
                    Server server = new Server(Manage.this, selectedFile);
                    server.start();
                    mntmLoadXMLFile.setEnabled(false);
                }
                /*
                 * else { }
                 */

            }
        });
        mnRndCtrl.add(mntmLoadXMLFile);
        frmOegDirector.setVisible(true);
        // Control -- Registration Enabled [Checkbox]
        final JCheckBoxMenuItem mntmRegEnabled = new JCheckBoxMenuItem(
                        "Enable Registration");
        mntmRegEnabled.setSelected(true);
        ;
        mntmRegEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                registerDisabled = !mntmRegEnabled.isSelected();
            }
        });
        mnRndCtrl.add(mntmRegEnabled);
        // Control -- Team -- Message All
        JMenuItem mntmMessageAll = new JMenuItem("Message");
        mntmMessageAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = JOptionPane.showInputDialog(null,
                                "Enter message:", "Message All Teams",
                                JOptionPane.PLAIN_MESSAGE);
                for (Object operator : operatorModel.toArray()) {
                    if (operator == null)
                        continue;
                    ServerOperator selected = (ServerOperator) operator;
                    System.out.printf("Sending message %s to %s\n", message,
                                    selected.getName());
                    selected.sendInfo(message);
                }
            }
        });
        mnTeam.add(mntmMessageAll);
        // Control -- Team -- Bulk Update Bank
        JMenuItem mntmBankAll = new JMenuItem("Change Balance");
        mntmBankAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = JOptionPane.showInputDialog(frame,
                                "Changes will reflect at the beginning of the next round.\n"
                                                + "Enter Amount to add:",
                                "Add to every Team's Bank",
                                JOptionPane.PLAIN_MESSAGE);
                for (Object operator : operatorModel.toArray()) {
                    ServerOperator selected = (ServerOperator) operator;
                    int changeBy = Integer.parseInt(message);
                    String operatorAlert = String.format(
                                    "The Bank has decided to %s $%d.00 to your account."
                                                    + "The changes will reflect on the next round.",
                                    (changeBy > 0 ? "credit" : "bill"),
                                    changeBy);
                    selected.sendInfo(operatorAlert);
                    selected.setBalance(selected.getBalance() + changeBy);
                }
            }
        });
        mnTeam.add(mntmBankAll);
        // Menu -- Control -- Set Round Time
        JMenuItem mnSetRndTime = new JMenuItem("Change Round Time");
        mnSetRndTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // director.unscheduledEndRound();
                long length = Long.parseLong(JOptionPane.showInputDialog(null,
                                "This will end the current round!\n"
                                                + "Enter new Round Time in minutes:",
                                "Change Round Time",
                                JOptionPane.PLAIN_MESSAGE));
                director.changeRoundLength(length * 60000l);
            }
        });
        mnRndCtrl.add(mnSetRndTime);
        // Menu -- Control -- End Round
        JMenuItem mnEndRound = new JMenuItem("End Round");
        mnEndRound.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                director.unscheduledEndRound();
            }
        });
        mnRndCtrl.add(mnEndRound);
        // Menu -- Control -- End Simulation
        JMenuItem mntmEndGame = new JMenuItem("End Simulation");
        mntmEndGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                director.endGame();
                // Prep for new game:
                operatorModel.clear();
                // actionQueueList needs to be cleared!
            }
        });
        mnRndCtrl.add(mntmEndGame);
        // Menu -- Copy -- Copy Action Queue
        JMenuItem mntmCopyActionQueue = new JMenuItem("Copy Action Queue");
        mntmCopyActionQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int index = operatorList.getSelectedIndex();
                ServerOperator selected = (ServerOperator) operatorModel
                                .get(index);
                copyActionList(selected);
            }
        });
        mnCopy.add(mntmCopyActionQueue);
        // Menu -- Copy -- Copy History List
        JMenuItem mntmCopyHistoryList = new JMenuItem("Copy History List");
        mntmCopyHistoryList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = operatorList.getSelectedIndex();
                ServerOperator selected = (ServerOperator) operatorModel
                                .get(index);
                copyHistoryList(selected);
            }
        });
        mnCopy.add(mntmCopyHistoryList);
        frmOegDirector.setVisible(true);

    }

    public void refreshInfo() {
        int index = operatorList.getSelectedIndex();
        if (index != -1) {
            ServerOperator selected = (ServerOperator) operatorModel.get(index);
            setCurrentOperator(selected);
        }
    }

    /**
     * Set the UI elements to display information about the newly selected
     * operator.
     * 
     * @param selected
     *            The selected operator
     */
    protected void setCurrentOperator(ServerOperator selected) {
        setActionList(selected);
        setHistoryList(selected);
        setBankBalance(selected);
    }

    /**
     * Set the bank balance to reflect the selected operator's balance.
     * 
     * @param selected
     *            The selected operator.
     */
    private void setBankBalance(ServerOperator selected) {
        DecimalFormat myFormatter = new DecimalFormat("$###,###,###");
        String output = myFormatter.format(selected.getBank().getBalance());
        bankLabel.setText(output);
        output = myFormatter.format(selected.getBank().getIncome());
        incomeLabel.setText(output);
    }

    /**
     * Gather a list of all current actions for the selected operator
     * 
     * @param selected
     *            The selected operator.
     * @return A Vector collection of actions the operator is currently planning
     *         to take.
     */
    private Vector<Action> getActionList(ServerOperator selected) {
        Vector<Action> combine = new Vector<Action>();

        combine.addAll(selected.getBidQueue());
        combine.addAll(selected.getDrillQueue());
        combine.addAll(selected.getSeismicQueue());

        return combine;
    }

    /**
     * Set the action list UI element to display the current action queue for
     * the selected operator.
     * 
     * @param selected
     *            The selected operator.
     */
    private void setActionList(ServerOperator selected) {
        Vector<Action> combine = getActionList(selected);
        actionQueueList.setListData(combine);
    }

    /**
     * Place the action queue of the selected operator in the clipboard
     * 
     * @param selected
     *            The selected operator.
     */
    private void copyActionList(ServerOperator selected) {
        Vector<Action> combine = getActionList(selected);
        setClipboard(combine);
    }

    /**
     * Place the history list of the selected operator in the clipboard
     * 
     * @param selected
     *            The selected operator.
     */
    private void copyHistoryList(ServerOperator selected) {
        Vector<Action> combine = getHistoryList(selected);
        setClipboard(combine);
    }

    /**
     * Set the system clipboard to reflect the collection of actions that are
     * passed. Uses the toString() method of each Action object.
     * 
     * @param actions
     *            The Vector of actions that will be placed on the clipboard.
     */
    private void setClipboard(Vector<Action> actions) {
        String copy = "";
        for (Action a : actions) {
            copy += a.toString() + "\n";
        }
        StringSelection ss = new StringSelection(copy);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    /**
     * Get the history log for the selected operator.
     * 
     * @param selected
     *            The selected operator.
     * @return A Vector collection of Actions that is filled with the entire
     *         history log for this operator.
     */
    private Vector<Action> getHistoryList(ServerOperator selected) {
        Vector<Action> combine = new Vector<Action>();

        combine.addAll(selected.getHistory());
        return combine;
    }

    /**
     * Set the history UI element to display the log of the selected Operator.
     * 
     * @param selected
     *            The selected operator.
     */
    private void setHistoryList(ServerOperator selected) {
        Vector<Action> combine = getHistoryList(selected);
        historyList.setListData(combine);
    }

    /**
     * Add an operator to the UI operator's list.
     * 
     * @param operator
     *            The operator to add to the list.
     */
    public void addOperator(ServerOperator operator) {
        operatorModel.addElement(operator);
    }

    /**
     * Set the round and timer UI labels to the current values
     * 
     * @param curRound2
     *            The current game round value.
     * @param string
     *            The current formatted time remaining in the round clock.
     */
    public void setTimer(int curRound2, int numRounds, String string) {
        roundLabel.setText(curRound2 + " / " + numRounds);
        timerLabel.setText(string);
    }

    /**
     * Reset the UI round timer information to new values.
     * 
     * @param timeRemaining
     *            The time remaining in the current round timer.
     * @param curRound
     *            The current round number.
     */
    public void setRoundTimer(long timeRemaining, int curRound) {
        remaining = timeRemaining;
        this.curRound = curRound;
        lastUpdate = System.currentTimeMillis();
        roundTimer.resume();
    }

    /**
     * Set the status UI label to the input string.
     * 
     * @param in
     *            The new contents of the status UI label.
     */
    public void setStatus(String in) {
        statusLabel.setText(in);
    }

    /** Manages the UI round timer by updating it every second. */
    class RoundTimer implements ActionListener {
        /** Timer to update the count down every second */
        private Timer timer;

        /** Starts the next 1 second timer */
        void resume() {
            // Restore the time we're counting down from and restart the timer.
            timer = new Timer(1000, roundTimer);
            timer.setInitialDelay(0); // First timer is immediate.
            timer.start(); // Start the timer
        }

        // Prematurely end the timer
        public void stop() {
            timer.stop();
        }

        /**
         * Callback method called when the 1 second timer expires. Calls
         * updateDisplay().
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            updateDisplay();
        }

        /**
         * Update the displayed time in the UI. This method is called from
         * actionPerformed() which is itself invoked by the timer.
         */
        private void updateDisplay() {
            long now = System.currentTimeMillis(); // current time in ms
            long elapsed = now - lastUpdate; // ms elapsed since last update
            remaining -= elapsed; // adjust remaining time
            lastUpdate = now; // remember this update time

            // Convert remaining milliseconds to mm:ss format and display
            if (remaining <= 0) {
                remaining = 0; // roundLength;
            }

            int hours = (int) remaining / 60000 / 60;
            int minutes = (int) (remaining / 60000) % 60;
            int seconds = (int) ((remaining % 60000) / 1000);
            String days = "";
            if (hours >= 24) {
                days = (hours / 24)
                                + ((hours / 24 == 1) ? " Day, " : " Days - ");
                hours = hours % 24;
            }
            setTimer(curRound,
                            director.getNumRounds(), days
                                            + ((hours >= 1) ? (format.format(
                                                            hours) + ":") : "")
                                            + format.format(minutes) + ":"
                                            + format.format(seconds));
        }

    }

    /**
     * The JTextArea UI element that is placed in every Action JList element.
     * Implements ListCellRenderer to get the value of the object placed in the
     * JList using the toString() method of the object. All objects added to the
     * two Action JLists in the UI use this class for displaying their values.
     * This is done so that JTextAreas can be used to have multi-line JList
     * elements
     */
    class MyCellRenderer extends JTextArea implements ListCellRenderer {

        /** Serializable UID */
        private static final long serialVersionUID = -6586536098689832377L;

        /** Constructor for the JTextArea */
        public MyCellRenderer() {
            super();
            setOpaque(true);
        }

        /**
         * Called to render the List element. Uses the Action object's .toString
         * to fill this JTextArea.
         * 
         * @param list
         *            The JList that is calling this method.
         * @param value
         *            The Action that is being inserted into list.
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {

            setText(value.toString());

            Color background = Color.WHITE;
            Color foreground = Color.BLACK;

            setBackground(background);
            setForeground(foreground);

            return this;
        }

    }
}
