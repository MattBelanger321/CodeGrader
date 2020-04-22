import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static final int PORT = 55588;
    public ServerSocket serverSocket;
    public Socket client;
    public String name;
    public File submission;
    public File report;
    private String fileName;

    public Server(String args[]) throws IOException {
        while(true){
            DataOutputStream dos;
            FileInputStream fis;
            String name = "Page.class";
            File page = new File(name);
            try {
                serverSocket = new ServerSocket(PORT);
                fis = new FileInputStream(page);
                System.out.println("Awaiting Connection...");
                client = serverSocket.accept(); //returns the client socket that is trying to connect to this server
                System.out.println("Connected");
                dos = new DataOutputStream(client.getOutputStream());    //output stream sends info to client
                long size = page.length();
                byte[] byteArray = new byte[(int) size];
                fis.read(byteArray);
                dos.write(byteArray, 0, byteArray.length);
                dos.flush();
                dos.close();
                client.close();

                getSubmission();
            } catch(Exception e) {
                e.printStackTrace();
            }

            testSubmission(args[0]);
            serverSocket.close();
        }
    }

    private void testSubmission(String testPath) throws IOException {
        File execute = new File("./"+fileName+"/a");
		boolean exe;
        LinkedList<String> commands = new LinkedList<>();
        commands.add("gcc");
        commands.add("-o");
        commands.add("./"+fileName+"/a");
        commands.add("./"+fileName+"/"+fileName+".c");
        if((exe = execute.exists())){
            System.out.println(execute.delete());
        }
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        compReport(exe,runTestCase(new File(testPath)));
    }

    private LinkedList<Boolean> runTestCase(File testCases) throws IOException {
        LinkedList<Boolean> passed = new LinkedList<>();
        boolean pass = true;
        LinkedList<String> commands = new LinkedList<>();
        commands.add("./"+fileName+"/a");
        ProcessBuilder pb = new ProcessBuilder(commands);
        Scanner scan = new Scanner(new FileReader(testCases));
        for(int i = 1;scan.hasNext();i++){
            Process p = pb.start();
            PrintWriter pw;
            pw = new PrintWriter(p.getOutputStream());
            String line;
            while(!(line = scan.nextLine()).equals("*")){
                pw.println(line);
                pw.flush();
            }
            pw.close();

            BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = read.readLine();
            passed.add(line.equals(scan.nextLine()));
            scan.nextLine();
        }
        return passed;
    }

    private void compReport(boolean comp,LinkedList<Boolean> passed) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./"+fileName+"/"+"errReport.txt")));
        if(!comp){
            bw.write(String.format("FILE RECEIVED:\n\nCOMPILATION STATUS: UNSUCCESSFUL\nTEST CASES FAILED\n"));
            bw.close();
            return;
        }
        bw.write(String.format("FILE RECEIVED:\n\nCOMPILATION STATUS: SUCCESSFUL\n"));
        bw.append("AFTER EVALUATION OF TEST CASES THE RESULTS ARE:\n");
        int i = 1;
        for(boolean b: passed){
            bw.append(String.format("TEST CASE %d: YOU %s\n",i++,b?"PASS\n":"FAIL\n"));
        }
        bw.close();
    }

    private void getSubmission() throws IOException {
        client = serverSocket.accept();
        DataInputStream dis = new DataInputStream(client.getInputStream());    //input stream receives info from client
        name = dis.readUTF();
        submission = readFile(dis);
    }

    private File readFile(DataInputStream dis) throws IOException {
        fileName = generateFileName(name);
        new File(fileName).delete();
        new File(fileName).mkdir();
        DataOutputStream submissionWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("./"+fileName+"/"+fileName+".c")));
        byte n;
        byte[] byteArr = new byte[dis.readInt()];
        while(true){
            try{
                n = dis.readByte();
                submissionWriter.write(n);
                submissionWriter.flush();
            }catch(EOFException e){
                break;
            }
        }
        submissionWriter.close();
        System.err.println("FILE RECEIVED");
        return new File(name+"\\"+fileName+".c");
    }

    private String generateFileName(String nameCpy) {
        return nameCpy.replaceAll(" ","_").replaceAll("\n","");
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(args);
    }
}