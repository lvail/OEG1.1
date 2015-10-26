package client;

import java.awt.Font;

// import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClientGUI extends JFrame {
    final int SHELLHEIGHT = 500;
    final int SHELLWIDTH = 800;
    // Labels
    private JLabel teamLabel, fixedTeamLabel, teamFundsLabel, fixedBankLabel,
                    midLabel, roundLabel;
    // panel
    JPanel topInfoL, teamPanel, bankPanel, topInfoR, roundCountPanel;
    //
    Font standardBoldFont = new Font("Tahoma", Font.BOLD, 15);

    public static void main(String[] args) {
        Requests request = new Requests();
    }

}
