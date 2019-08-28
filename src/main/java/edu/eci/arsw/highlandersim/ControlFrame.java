package edu.eci.arsw.highlandersim;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ControlFrame extends JFrame {

    private static final int DEFAULT_IMMORTAL_HEALTH = 100;
    private static final int DEFAULT_DAMAGE_VALUE = 10;

    private JPanel contentPane;
    private List<Immortal> immortals;
    private static boolean pause = false;
    private static boolean terminated = false;
    private JTextArea output;
    private JTextField numOfImmortals;
    public static AtomicInteger counterPause=new AtomicInteger(0);;
    public static Object lock =new Object();
    public static ControlFrame frame=new ControlFrame();
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //ControlFrame frame = new ControlFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public ControlFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 647, 248);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        //counter=new AtomicInteger(0);
        JToolBar toolBar = new JToolBar();
        contentPane.add(toolBar, BorderLayout.NORTH);
        //lock=new Object();
        final JButton btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                immortals = setupInmortals();
                if (immortals != null) {
                    for (Immortal im : immortals) {
                        im.start();
                    }
                }
                btnStart.setEnabled(false);
            }
        });
        toolBar.add(btnStart);

        JButton btnPauseAndCheck = new JButton("Pause and check");
        btnPauseAndCheck.addActionListener((ActionEvent e) -> {
            if (this.counterPause.get() != immortals.size()) {
                synchronized (this) {
                    try {
                        setPause(true);
                        this.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ControlFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int sum = 0;
                    for (Immortal im : immortals) {
                        sum += im.getHealth();
                    }
                    output.setText(immortals.toString() + ".\nSum:" + sum);
                }
            }
        });
        toolBar.add(btnPauseAndCheck);

        JButton btnResume = new JButton("Resume");

        btnResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setPause(false);
                synchronized(immortals){
                    immortals.notifyAll();
                }
            }
        });

        toolBar.add(btnResume);
        
        JLabel lblNumOfImmortals = new JLabel("num. of immortals:");
        toolBar.add(lblNumOfImmortals);

        numOfImmortals = new JTextField();
        numOfImmortals.setText("100");
        toolBar.add(numOfImmortals);
        numOfImmortals.setColumns(10);
        
        JButton btnStop = new JButton("STOP");
        btnStop.setForeground(Color.RED);
        toolBar.add(btnStop);
        
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTerminated(true);
                JOptionPane.showMessageDialog(null, "Has finalizado el Juego. ");
            }
        });      
       
        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        output = new JTextArea();
        output.setEditable(false);
        scrollPane.setViewportView(output);

    }
    public List<Immortal> setupInmortals() {
        try {
            int ni = Integer.parseInt(numOfImmortals.getText());

            List<Immortal> il = new LinkedList<Immortal>();

            for (int i = 0; i < ni; i++) {
                Immortal i1 = new Immortal("im" + i, il, DEFAULT_IMMORTAL_HEALTH, DEFAULT_DAMAGE_VALUE);
                il.add(i1);
            }
            return il;
        } catch (NumberFormatException e) {
            JOptionPane.showConfirmDialog(null, "Número inválido.");
            return null;
        }

    }
    
    private void setPause(boolean t) {
        pause = t;
    }
    public static boolean getPause(){
        return pause;
    }
    private void setTerminated(boolean t) {
        terminated = t;
    }
    public static boolean getTerminated(){
        return terminated;
    }
}
