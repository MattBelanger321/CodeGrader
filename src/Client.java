import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

class Client extends JFrame{
    private String fileName;
    private DataOutputStream pageWritter;
    private JTextField textBox;
    private JPanel panel,programPanel;
    private Class cls;
    private DataInputStream in;
    private Socket sock;
    private String name = null;
    private File file = null;
    private JFrame frame;
    private DataOutputStream submissionSender;

    public static void main(String[] args) throws IOException {
        System.out.println("<program launched>");
        new Client();
    }
    public Client(){
        super("Client");
        buildFrames(); // Builds main client frame aswel as a separate frame to display Page
        addTextBoxActionListener();
    }

    //Submits IP Address in the textbox and connects to server
    private void addTextBoxActionListener() {
        textBox.addActionListener(actionEvent -> {
            panel.removeAll();
            try{
                try {
                    sock = new Socket(textBox.getText(), 55588);
                    getServerClass();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    if (in != null)
                        in.close();
                    if(sock != null)
                        sock.close();
                }
                runPage();
                addSend();

            }catch(Exception e){
                System.err.println("OPEN UNSUCCESFUL");
                e.printStackTrace();
            }
        });
    }

    //This method receives the page.java class from the server
    private void getServerClass() throws IOException {
        in = new DataInputStream(new BufferedInputStream(sock.getInputStream())); //Input from server
        fileName = "Page.class";
        pageWritter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        int n;
        byte[] byteArr = new byte[4096];
        while((n = in.read(byteArr)) != -1){
            try{
                pageWritter.write(byteArr,0,n);
                pageWritter.flush();
            }catch(EOFException ignored){}
                pageWritter.close();
        }
    }

    //Creates a UI button that will send information to server
    private void addSend(){
        JButton button1 = new JButton("SEND");
        button1.setSize(100,25);
        frame.add(button1,BorderLayout.SOUTH);
        button1.addActionListener(e->{
            Method[] methods = cls.getDeclaredMethods();
            invokeGetters(methods);
            try {
                send();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    //Sends submission to server
    private void send() throws IOException {
        sock = new Socket(textBox.getText(), 55588);
        submissionSender = new DataOutputStream(sock.getOutputStream());
        submissionSender.writeUTF(name);
        writeFile();
        sock.close();
    }

    //This methods sends file to the server
    private void writeFile() throws IOException {
        FileInputStream fis = new FileInputStream(file);
        long size = file.length();
        byte[] byteArray = new byte[(int) size];
        fis.read(byteArray);
        submissionSender.writeInt((int)size);
        System.err.println("SENDING SUBMISSION...");
        submissionSender.write(byteArray, 0, byteArray.length);
        System.err.println("SUCCESSFUL SUBMISSION");
        submissionSender.flush();
    }

    //this method invokes the Page.java class sent from the server and reflects it in its own frame
    private void runPage() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        cls = Class.forName(fileName.substring(0,fileName.indexOf(".")));
        programPanel = (JPanel) cls.getDeclaredConstructor().newInstance();
        panel.add(programPanel,BorderLayout.CENTER);
        panel.updateUI();
        frame.setVisible(true);
    }

    //This function invokes getFile and getName in Page to get the users name and their submission
    private void invokeGetters(Method[] methods) {
        for(Method m: methods){
            if(m.toString().contains("get")){
                try {
                    if(m.toString().contains("File"))
                        this.file = (File)m.invoke(programPanel);
                    else if(m.toString().contains("Name"))
                        this.name = (String)m.invoke(programPanel);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    //This function builds UI frames
    private void buildFrames() {//Main frame
        setSize(500,500);
        setLayout(new BorderLayout());
        textBox = new JTextField("ENTER THE IP ADDRESS");
        add(textBox, BorderLayout.NORTH);
        setVisible(true);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(this.getBounds());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Page frame
        frame = new JFrame("Page.class");
        frame.setBounds(500,0,500,500);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
    }
}