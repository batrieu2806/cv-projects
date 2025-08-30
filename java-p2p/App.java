import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

public class App {
    
    private static JTextField commandField = new JTextField();
    private static JTextArea outputArea = new JTextArea();
    
    public static void main(String[] args) {
        
        NewFrame frame = new NewFrame(1000, 500, false, "Torrent Server Manager"); //Create Frame

        frame.setLayout(new BorderLayout(0, 0));


        NewButton button0 = new NewButton(50, 50, 100, 100, "Recompile", false); //Create (re)compile button
        NewButton button1 = new NewButton(50, 200, 100, 100, "Run Server", false); //create run server button
        NewButton button2 = new NewButton(200, 200, 150, 50, "Run Client 1", false); //create Run Client 1
        NewButton button3 = new NewButton(200, 300, 150, 50, "Run Client 2", false); //create Run Client 2
        NewButton button4 = new NewButton(200, 400, 150, 50, "Run Client 3", false); //create Run Client 3
        NewButton button5 = new NewButton(50, 350, 100, 100, "Split File", false);  //create Split File
        NewButton buttonRun = new NewButton(887, 0, 100, 40, "RUN", false);
        
        JPanel LeftSide = new JPanel(new BorderLayout());
        LeftSide.setPreferredSize(new Dimension(475,500));
        JPanel RightSide =new JPanel(new BorderLayout());
        RightSide.setPreferredSize(new Dimension(475, 500));
        //----------------------------------------//

        commandField = new JTextField();
        commandField.setPreferredSize(new Dimension(400, 40));

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        JPanel inputPanel = new JPanel(new BorderLayout(0,0));
        inputPanel.add(commandField, BorderLayout.CENTER);
        inputPanel.add(buttonRun, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(350, 350));

        RightSide.add(inputPanel, BorderLayout.NORTH);
        RightSide.add(scrollPane, BorderLayout.CENTER);

        //-------------------------------//
        JPanel panelcompileStatusLabel = new JPanel(new BorderLayout());

        JPanel blackLinePanel = new JPanel(new BorderLayout()); 
        blackLinePanel.setBackground(Color.BLACK);
        blackLinePanel.setPreferredSize(new Dimension(500,10));
        JLabel compileStatusLabel = new JLabel("");
        compileStatusLabel.setPreferredSize(new Dimension(500,40));
        compileStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        compileStatusLabel.setVerticalAlignment(SwingConstants.CENTER);
        compileStatusLabel.setBackground(new Color(153, 255, 153));
        compileStatusLabel.setOpaque(true);

        panelcompileStatusLabel.add(blackLinePanel, BorderLayout.CENTER);
        panelcompileStatusLabel.add(compileStatusLabel, BorderLayout.NORTH);
        panelcompileStatusLabel.setPreferredSize(new Dimension(1000, 50));

        LeftSide.add(panelcompileStatusLabel, BorderLayout.NORTH);

        JPanel buttongrid = new JPanel(new BorderLayout());
        buttongrid.setLayout(new GridLayout(3,2,50,10));
        buttongrid.add(button0);
        buttongrid.add(button2);
        buttongrid.add(button1);
        buttongrid.add(button3);
        buttongrid.add(button5);
        buttongrid.add(button4);
        LeftSide.add(buttongrid, BorderLayout.CENTER);
        buttongrid.setPreferredSize(new Dimension(450,300));
        //-------------------------------//



        frame.add(LeftSide,BorderLayout.WEST);
        frame.add(RightSide,BorderLayout.EAST);

        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();
        
        try {
            compileJavaTask();
            System.out.println("Compiled Program successful.");
            compileStatusLabel.setText("Compiled Program successful.");
            button0.setEnabled(true);
            button1.setEnabled(true);
            button2.setEnabled(true);
            button3.setEnabled(true);
            button4.setEnabled(true);
            button5.setEnabled(true);
            buttonRun.setEnabled(true);
        } catch (IOException ex) {
            System.err.println("Compiled Program failed: " + ex.getMessage());
        }

        buttonRun.addActionListener(e -> {
            String command = commandField.getText().trim();
            if (!command.isEmpty()) {
                try {
                    String currentDirectory = System.getProperty("user.dir");

                    System.out.println("Button Run clicked");
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                    processBuilder.redirectErrorStream(true);
                    processBuilder.directory(new File(currentDirectory)); // Set the working directory
                    Process process = processBuilder.start();
                    try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
                        String output = scanner.hasNext() ? scanner.next() : "";
                        outputArea.append(currentDirectory + ">" + command + "\n");
                        outputArea.append(output + "\n");
                        System.out.println(currentDirectory + ">" + command + "\n");
                        System.out.println(output + "\n");
                    }
                } catch (Exception ex) {
                    outputArea.append("Error executing command: " + command + "\n" + ex.getMessage() + "\n\n");
                    System.err.println("Error executing command: " + command + "\n" + ex.getMessage() + "\n\n");
                }
                commandField.setText("");
            }
        });

        button0.addActionListener(e -> {
            try {
                compileJavaTask();
                //button1.setEnabled(true);
                //button2.setEnabled(true);
                //button3.setEnabled(true);
                System.out.println("Recompiled Java successful.");
                compileStatusLabel.setText("Recompiled Java successful.");

            } catch (IOException ex) {
                System.err.println("Recompiled Java failed: " + ex.getMessage());
                compileStatusLabel.setText("Recompiled Java failed: " + ex.getMessage());
            }
            
        });
        button1.addActionListener(e -> {
            try {
                runServerTask();
                System.out.println("Run Server successful.");
                compileStatusLabel.setText("Run Server successful.");
            } catch (IOException ex) {
                System.err.println("Run Server failed: " + ex.getMessage());
                compileStatusLabel.setText("Run Server failed: " + ex.getMessage());
            }
        });

        button2.addActionListener(e -> {
            try {
                runClient1();
                System.out.println("Run Client 1 successful.");
                compileStatusLabel.setText("Run Client 1 successful.");
                NewFrame frame1 = new NewFrame(500, 500, false, "Client 1"); //Create Frame
                JTextField commandField1 = new JTextField();
                JButton runButton1 = new JButton("Run");
                JTextArea outputArea1 = new JTextArea();
                outputArea1.setEditable(false);
                JPanel inputPanel1 = new JPanel(new BorderLayout());
                inputPanel1.add(commandField1, BorderLayout.CENTER);
                inputPanel1.add(runButton1, BorderLayout.EAST);

                JScrollPane scrollPane1 = new JScrollPane(outputArea);
                frame1.getContentPane().setLayout(new BorderLayout());
                frame1.getContentPane().add(inputPanel1, BorderLayout.NORTH);
                frame1.getContentPane().add(scrollPane1, BorderLayout.CENTER);
                runButton1.addActionListener(event -> {
                    String command = commandField.getText().trim();
                    if (!command.isEmpty()) {
                        try {
                            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
                            processBuilder.redirectErrorStream(true);
                            Process process = processBuilder.start();
                            try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
                                String output = scanner.hasNext() ? scanner.next() : "";
                                outputArea.append("Command: " + command + "\n");
                                outputArea.append("Output:\n" + output + "\n");
                                outputArea.append("\n");
                            }
                        } catch (Exception ex) {
                            outputArea.append("Error executing command: " + command + "\n" + ex.getMessage() + "\n\n");
                        }
                        commandField.setText("");
                    }
                });
            } catch (IOException ex) {
                System.err.println("Run Client 1 failed: " + ex.getMessage());
                compileStatusLabel.setText("Run Client 1 failed: " + ex.getMessage());
            }
        });

        button3.addActionListener(e -> {
            try {
                runClient2();
                System.out.println("Run Client 2 successful.");
                compileStatusLabel.setText("Run Client 2 successful.");
                NewFrame frame2 = new NewFrame(500, 500, false, "Client 2"); //Create Frame
            } catch (IOException ex) {
                System.err.println("Run Client 2 failed: " + ex.getMessage());
                compileStatusLabel.setText("Run Client 2 failed: " + ex.getMessage());
            }
        });

        button4.addActionListener(e -> {
            try {
                runClient3();
                System.out.println("Run Client 3 successful.");
                compileStatusLabel.setText("Run Client 3 successful.");
                NewFrame frame3 = new NewFrame(500, 500, false, "Client 3"); //Create Frame
                
            } catch (IOException ex) {
                System.err.println("Run Client 3 failed: " + ex.getMessage());
                compileStatusLabel.setText("Run Client 3 failed: " + ex.getMessage());
            }
        });
        
        button5.addActionListener(e -> {
            try {
                runFileSplitTask();
                System.out.println("Run FileSplit successful.");
                compileStatusLabel.setText("Run FileSplit successful.");
            } catch (IOException ex) {
                System.err.println("Run FileSplit failed: " + ex.getMessage());
                compileStatusLabel.setText("Run FileSplit failed: " + ex.getMessage());
            }
        });
    }

    private static void compileJavaTask() throws IOException {
        String[] command = {"javac", "Server.java", "Client.java", "Testing/FileSplit.java"};
    
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    private static void runServerTask() throws IOException {
        compileJavaTask();
        String[] command = {"java", "Server"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    private static void runFileSplitTask() throws IOException {
        //String[] command = {"java", "-cp", ".", "Testing.FileSplit", "./Testing/1.txt"};
        compileJavaTask();
        String[] command = {"java", "-cp", ".", "Testing.FileSplit", "./Testing/2.txt"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    private static void runClient1() throws IOException {
        compileJavaTask();
        String[] command = {"java", "-cp", ".", "Client", "./Client1/Meta.txt"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }
    
    private static void runClient2() throws IOException {
        compileJavaTask();
        String[] command = {"java", "-cp", ".", "Client", "./Client2/Meta.txt"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    private static void runClient3() throws IOException {
        compileJavaTask();
        String[] command = {"java", "-cp", ".", "Client", "./Client3/Meta.txt"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        processBuilder.start();
    }

    // private static void executeCommand(String command) {
    //     try {
    //         ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
    //         processBuilder.redirectErrorStream(true);
    //         Process process = processBuilder.start();
    //         try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
    //             String output = scanner.hasNext() ? scanner.next() : "";
    //             outputArea.append("Command: " + command + "\n");
    //             outputArea.append("Output:\n" + output + "\n");
    //         }
    //     } catch (Exception ex) {
    //         outputArea.append("Error executing command: " + command + "\n" + ex.getMessage() + "\n\n");
    //     }
    //     commandField.setText("");
    // }
}
