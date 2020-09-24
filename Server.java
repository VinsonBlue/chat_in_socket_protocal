package serverWITHclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.NullPointerException;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class Server {
	private JFrame frame;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JTextField txt_max;
    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JSplitPane centerSplit;
    private JList userList;
    private DefaultListModel listModel;

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private ArrayList<ClientThread> clients;
    private boolean isStart = false;

    // ������,����ִ�����
    public static void main(String[] args) {
            new Server();
    }

    // ִ����Ϣ����
    public void send() {
            if (!isStart) {
                    JOptionPane.showMessageDialog(frame, "��������δ����,���ܷ�����Ϣ��", "����",
                                    JOptionPane.ERROR_MESSAGE);
                    return;
            }
            if (clients.size() == 0) {
                    JOptionPane.showMessageDialog(frame, "û���û�����,���ܷ�����Ϣ��", "����",
                                    JOptionPane.ERROR_MESSAGE);
                    return;
            }
            String message = txt_message.getText().trim();
            if (message == null || message.equals("")) {
                    JOptionPane.showMessageDialog(frame, "��Ϣ����Ϊ�գ�", "����",
                                    JOptionPane.ERROR_MESSAGE);
                    return;
            }
            
            sendServerMessage(message);// Ⱥ����������Ϣ
            contentArea.append("������˵��" + txt_message.getText() + "\r\n");
            txt_message.setText(null);
    }

    // ����ŷ�
    public Server() {
            frame = new JFrame("������");
            // ����JFrame��ͼ�꣺
            //frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
            frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Server.class.getResource("qq.png")));
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setForeground(Color.blue);
            txt_message = new JTextField();
            txt_max = new JTextField("30");
            txt_port = new JTextField("6666");
            btn_start = new JButton("����");
            btn_stop = new JButton("ֹͣ");
            btn_send = new JButton("����");
            btn_stop.setEnabled(false);
            listModel = new DefaultListModel();
            userList = new JList(listModel);

            southPanel = new JPanel(new BorderLayout());
            southPanel.setBorder(new TitledBorder("д��Ϣ"));
            southPanel.add(txt_message, "Center");
            southPanel.add(btn_send, "East");
            leftPanel = new JScrollPane(userList);
            leftPanel.setBorder(new TitledBorder("�����û�"));

            rightPanel = new JScrollPane(contentArea);
            rightPanel.setBorder(new TitledBorder("��Ϣ��ʾ��"));

            centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
                            rightPanel);
            centerSplit.setDividerLocation(100);
            northPanel = new JPanel();
            northPanel.setLayout(new GridLayout(1, 6));
            northPanel.add(new JLabel("��������"));
            northPanel.add(txt_max);
            northPanel.add(new JLabel("�˿�"));
            northPanel.add(txt_port);
            northPanel.add(btn_start);
            northPanel.add(btn_stop);
            northPanel.setBorder(new TitledBorder("������Ϣ"));

            frame.setLayout(new BorderLayout());
            frame.add(northPanel, "North");
            frame.add(centerSplit, "Center");
            frame.add(southPanel, "South");
            frame.setSize(700, 400);
            //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//����ȫ��
            int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
            frame.setLocation((screen_width - frame.getWidth()) / 2,
                            (screen_height - frame.getHeight()) / 2);
            frame.setVisible(true);

            // �رմ���ʱ�¼�
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                            if (isStart) {
                                    closeServer();// �رշ�����
                            }
                            System.exit(0);// �˳�����
                    }
            });

            // �ı��򰴻س���ʱ�¼�
            txt_message.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            send();
                    }
            });

            // �������Ͱ�ťʱ�¼�
            btn_send.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                            send();
                    }
            });

            // ����������������ťʱ�¼�
            btn_start.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            if (isStart) {
                                    JOptionPane.showMessageDialog(frame, "�������Ѵ�������״̬����Ҫ�ظ�������",
                                                    "����", JOptionPane.ERROR_MESSAGE);
                                    return;
                            }
                            int max;
                            int port;
                            try {
                                    try {
                                            max = Integer.parseInt(txt_max.getText());
                                    } catch (Exception e1) {
                                            throw new Exception("��������Ϊ��������");
                                    }
                                    if (max <= 0) {
                                            throw new Exception("��������Ϊ��������");
                                    }
                                    try {
                                            port = Integer.parseInt(txt_port.getText());
                                    } catch (Exception e1) {
                                            throw new Exception("�˿ں�Ϊ��������");
                                    }
                                    if (port <= 0) {
                                            throw new Exception("�˿ں� Ϊ��������");
                                    }
                                    serverStart(max, port);
                                    contentArea.append("�������ѳɹ�����!��"  + ",�˿ڣ�" + port
                                                    + "\r\n");
                                    JOptionPane.showMessageDialog(frame, "�������ɹ�����!");
                                    btn_start.setEnabled(false);
                                    txt_max.setEnabled(false);
                                    txt_port.setEnabled(false);
                                    btn_stop.setEnabled(true);
                            } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                                                    "����", JOptionPane.ERROR_MESSAGE);
                            }
                    }
            });

            // ����ֹͣ��������ťʱ�¼�
            btn_stop.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            if (!isStart) {
                                    JOptionPane.showMessageDialog(frame, "��������δ����������ֹͣ��", "����",
                                                    JOptionPane.ERROR_MESSAGE);
                                    return;
                            }
                            try {
                                    closeServer();
                                    btn_start.setEnabled(true);
                                    txt_max.setEnabled(true);
                                    txt_port.setEnabled(true);
                                    btn_stop.setEnabled(false);
                                    contentArea.append("�������ɹ�ֹͣ!\r\n");
                                    JOptionPane.showMessageDialog(frame, "�������ɹ�ֹͣ��");
                            } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(frame, "ֹͣ�����������쳣��", "����",
                                                    JOptionPane.ERROR_MESSAGE);
                            }
                    }
            });
    }

    // ����������
    public void serverStart(int max, int port) throws java.net.BindException {
            try {
                    clients = new ArrayList<ClientThread>();
                    serverSocket = new ServerSocket(port);
                    serverThread = new ServerThread(serverSocket, max);
                    serverThread.start();
                    isStart = true;
            } catch (BindException e) {
                    isStart = false;
                    throw new BindException("�˿ں��ѱ�ռ�ã��뻻һ����");
            } catch (Exception e1) {
                    e1.printStackTrace();
                    isStart = false;
                    throw new BindException("�����������쳣��");
            }
    }

    // �رշ�����
    @SuppressWarnings("deprecation")
    public void closeServer() {
            try {
                    if (serverThread != null)
                            serverThread.stop();// ֹͣ�������߳�

                    for (int i = clients.size() - 1; i >= 0; i--) {
                            // �����������û����͹ر�����
                            clients.get(i).getWriter().println("CLOSE");
                            clients.get(i).getWriter().flush();
                            // �ͷ���Դ
                            clients.get(i).stop();// ֹͣ����Ϊ�ͻ��˷�����߳�
                            clients.get(i).reader.close();
                            clients.get(i).writer.close();
                            clients.get(i).socket.close();
                            clients.remove(i);
                    }
                    if (serverSocket != null) {
                            serverSocket.close();// �رշ�����������
                    }
                    listModel.removeAllElements();// ����û��б�
                    isStart = false;
            } catch (IOException e) {
                    e.printStackTrace();
                    isStart = true;
            }
    }

    // Ⱥ����������Ϣ
    public void sendServerMessage(String message) {
            for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println("��������" + message + "(��������Ϣ)");
                    clients.get(i).getWriter().flush();
            }
    }

    // �������߳�
    class ServerThread extends Thread {
            private ServerSocket serverSocket;
            private int max;// ��������

            // �������̵߳Ĺ��췽��
            public ServerThread(ServerSocket serverSocket, int max) {
                    this.serverSocket = serverSocket;
                    this.max = max;
            }

            public void run() {
                    while (true) {// ��ͣ�ĵȴ��ͻ��˵�����
                            try {
                                    Socket socket = serverSocket.accept();

                                    if (clients.size() == max) {// ����Ѵ���������
                                            BufferedReader r = new BufferedReader(
                                                            new InputStreamReader(socket.getInputStream()));
                                            PrintWriter w = new PrintWriter(socket
                                                            .getOutputStream());
                                            // ���տͻ��˵Ļ����û���Ϣ
                                            String inf = r.readLine();
                                            StringTokenizer st = new StringTokenizer(inf, "@");
                                            User user = new User(st.nextToken(), st.nextToken());
                                            // �������Ӳ��ɹ���Ϣ
                                            w.println("MAX@���������Բ���" + user.getName()
                                                            + user.getIp() + "�����������������Ѵ����ޣ����Ժ������ӣ�");
                                            w.flush();
                                            // �ͷ���Դ
                                            r.close();
                                            w.close();
                                            socket.close();

                                            continue;
                                    	}
                                    ClientThread client = new ClientThread(socket);
                                    client.start();// �����Դ˿ͻ��˷�����߳�
                                    
                                    listModel.addElement(client.getUser().getName());// ���������б�
                                    contentArea.append(client.getUser().getName()
                                                    + client.getUser().getIp() + "����!\r\n");
                                    clients.add(client);
                            } catch (IOException e) {
                                    e.printStackTrace();
                            }
                            catch (NullPointerException e){
                            	e.getMessage();
                            }
                    }
            }
    }

    // Ϊһ���ͻ��˷�����߳�
    class ClientThread extends Thread {
            private Socket socket;
            private BufferedReader reader;
            private PrintWriter writer;
            private User user;

            public BufferedReader getReader() {
                    return reader;
            }

            public PrintWriter getWriter() {
                    return writer;
            }

            public User getUser() {
                    return user;
            }

            // �ͻ����̵߳Ĺ��췽��
            public ClientThread(Socket socket) {
                    try {
                            this.socket = socket;
                            reader = new BufferedReader(new InputStreamReader(socket
                                            .getInputStream()));
                            writer = new PrintWriter(socket.getOutputStream());
                            // ���տͻ��˵Ļ����û���Ϣ
                            String inf = reader.readLine();
                            StringTokenizer st = new StringTokenizer(inf, "@");
                            user = new User(st.nextToken(), st.nextToken());
                            // �������ӳɹ���Ϣ
                            writer.println(user.getName() + user.getIp() + "����������ӳɹ�!");
                            writer.flush();
                            // ������ǰ�����û���Ϣ
                            if (clients.size() > 0) {
                                    String temp = "";
                                    for (int i = clients.size() - 1; i >= 0; i--) {
                                            temp += (clients.get(i).getUser().getName() + "/" + clients
                                                            .get(i).getUser().getIp())
                                                            + "@";
                                    }
                                    writer.println("USERLIST@" + clients.size() + "@" + temp);
                                    writer.flush();
                            }
                            // �����������û����͸��û���������
                            for (int i = clients.size() - 1; i >= 0; i--) {
                                    clients.get(i).getWriter().println(
                                                    "ADD@" + user.getName() + user.getIp());
                                    clients.get(i).getWriter().flush();
                            }
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
            }

            @SuppressWarnings("deprecation")
            public void run() {// ���Ͻ��տͻ��˵���Ϣ�����д���
                    String message = null;
                    while (true) {
                            try {
                                    message = reader.readLine();// ���տͻ�����Ϣ
                                    if (message.equals("CLOSE"))// ��������
                                    {
                                            contentArea.append(this.getUser().getName()
                                                            + this.getUser().getIp() + "����!\r\n");
                                            // �Ͽ������ͷ���Դ
                                            reader.close();
                                            writer.close();
                                            socket.close();
                                            

                                            // �����������û����͸��û�����������
                                            for (int i = clients.size() - 1; i >= 0; i--) {
                                                    clients.get(i).getWriter().println(
                                                                    "DELETE@" + user.getName());
                                                    clients.get(i).getWriter().flush();
                                            }

                                            listModel.removeElement(user.getName());// ���������б�

                                            // ɾ�������ͻ��˷����߳�
                                            for (int i = clients.size() - 1; i >= 0; i--) {
                                                    if (clients.get(i).getUser() == user) {
                                                            ClientThread temp = clients.get(i);
                                                            clients.remove(i);// ɾ�����û��ķ����߳�
                                                            temp.stop();// ֹͣ���������߳�
                                                           
                                                            return;
                                                    }
                                            }
                                    } else {
                                            dispatcherMessage(message);// ת����Ϣ
                                    }
                            } catch (IOException e) {
                                    e.printStackTrace();
                            }
                    }
            }

            // ת����Ϣ
            public void dispatcherMessage(String message) throws IOException {
                    String[] tokens = message.split("@");
                    String source = tokens[0];
                    String owner = tokens[1];
                    String content = tokens[2];
                    message = source + "˵��" + content;
                    contentArea.append(message + "\r\n");
                     // Ⱥ��
                    if(owner.equals("all")){
                    	for(int i=0;i<clients.size();i++){
                    		clients.get(i).getWriter().println(message + "(���˷���)");
                    		clients.get(i).getWriter().flush();
                    	}
                    }
                    else {
                    	int i=0;
                    	ArrayList<String>name = new ArrayList<>();
                    	for(i=clients.size()-1;i>=0;i--){
                    		
                    		name.add(clients.get(i).user.getName().toString()) ;
                    		if(owner.equals(clients.get(i).user.getName().toString())){
                    			clients.get(i).getWriter().println(message+"(������)");
                    			clients.get(i).getWriter().flush();
                    			PrintWriter w = new PrintWriter(socket.getOutputStream());
                    			w.println(message);
                    			w.flush();
                    				}
                    		
                    		
                    		}
                    	if(!name.contains(owner)){
                    		PrintWriter w = new PrintWriter(socket.getOutputStream());
                    		w.println("�Բ���û�д��û���");
                    		w.flush();
                    	}
                    	
               }
         }
    }
}
