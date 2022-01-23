package com.Server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class Server extends JFrame implements ActionListener {
	//serverUI
	JPanel serverPanel;
	TextArea textArea;
	TextField textField;
	JLabel portNumber;
	JButton serverStart;
	JButton serverStop;
	//serverNetwork
	ServerSocket serverSocket;
	Socket socket;
	
	int port=5000;
	
	
	//
	StringTokenizer stringTokenizer;
	Vector vectorUser = new Vector();
	HashMap<String, String> namelist=new HashMap<>();
	String[] job={"시민1","시민2","시민3","마피아","의사","사회자"};
	String temp="";
	
	public void job(){
		for(int i=0; i<1000; i++){
		int ran=(int)(Math.random()*job.length);
		temp=job[0];
		job[0]=job[ran];
		job[ran]=temp;
		}
	}
	public void serverUI(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		serverPanel=new JPanel();
		setTitle("Server");
		serverPanel.setLayout(null);
		textArea=new TextArea("",0,0,1);
		textArea.setBounds(10,10,365,240);
		serverPanel.add(textArea);
		//port number
		portNumber=new JLabel("port");
		portNumber.setBounds(30,260,40,20);
		serverPanel.add(portNumber);
		textField=new TextField();
		textField.setBounds(70,260,270,20);
		serverPanel.add(textField);
		//server start, stop sign
		serverStart=new JButton("서버 시작");
		serverStop=new JButton("서버 종료");
		serverStart.setBounds(20,285,150,20);
		serverStop.setBounds(220,285,150,20);
		serverPanel.add(serverStart);
		serverPanel.add(serverStop);
		//add panel
		add(serverPanel);
		setBounds(100,100,400,350);
		setVisible(true);
	}
	public void addactions(){
		serverStart.addActionListener(this);
		serverStop.addActionListener(this);
	}
	public void serverNetwork(){//커넥션을 통해 소켓 승인 받아냄
			try {		
				serverSocket=new ServerSocket(port);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "이미 사용중인 포트", "알림", JOptionPane.ERROR_MESSAGE);
			}
			if(serverSocket!=null){
				connection();
			}
	}
	public void connection(){//소켓 스레드 시작
		Thread thread = new Thread(new SocketThread());
		thread.start();
	}
	public Server(){//서버 UI 호출
		serverUI();
		addactions();
	}
		public static void main(String[] args) {
		new Server();//서버 호출 
	}
	public class SocketThread implements Runnable{//서버 소켓 승인
		public void run() {
			while(true){
			try {
				textArea.append("사용자 접속 대기중\n");
				socket = serverSocket.accept();
				UserChatting userChatting=new UserChatting(socket);
				userChatting.start();
			} catch (IOException e) {
				textArea.append("서버가 중지 되었습니다\n");
				break;
			}
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==serverStart){
			port=Integer.parseInt(textField.getText().trim());
			serverNetwork();
			serverStart.setEnabled(false);
			textField.setEnabled(false);
			serverStop.setEnabled(true);
		}else if(e.getSource()==serverStop){
				serverStart.setEnabled(true);
				textField.setEnabled(true);
				serverStop.setEnabled(false);
			try {
				serverSocket.close();
				vectorUser.removeAllElements();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("서버 종료");
		}
	}
	public class UserChatting extends Thread{// oi 클래스 
		InputStream inputStream;
		OutputStream outputStream;
		DataInputStream datainputStream;
		DataOutputStream dataoutputStream;
		Socket socket_user;
		String name="";
		public UserChatting(Socket socket){// 클라이언트의 소켓 
			this.socket_user = socket;
			userConnection();
		}
		public String getname(){
			return name;
		}
		public void userConnection(){// input output stream 연결
			try {
			inputStream = socket_user.getInputStream();
			outputStream = socket_user.getOutputStream();
			datainputStream = new DataInputStream(inputStream);
			dataoutputStream = new DataOutputStream(outputStream);	
			name = datainputStream.readUTF();
			textArea.append(name + " : 사용자 접속\n");
			
			
			
			
			broadCast("NewUser/"+name);
			for(int i=0; i<vectorUser.size();i++){
				UserChatting userChatting=(UserChatting) vectorUser.elementAt(i);
				this.sendMessage("OldUser/"+ userChatting.getname());
			}
			vectorUser.add(this);
			broadCast("userList_update/");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void broadCast(String str){//유저에게 메세지 보냄
			for(int i=0; i<vectorUser.size();i++){
				UserChatting userChatting=(UserChatting)vectorUser.elementAt(i);
				userChatting.sendMessage(str);
			}
		}
		
		public void sendMessage(String message){// dataoutputstream
			try {
				dataoutputStream.writeUTF(message);
			} catch (IOException e) {
				textArea.append("메세지 전송 실패");
			}
		}
		@Override
		public void run() {
			
			while(true){
				try {
					String msg = datainputStream.readUTF();
					textArea.append(name + " : " + msg + "\n");
					InMessage(msg);
				} catch (IOException e) {
					textArea.append(name + " : 사용자 접속 끊어짐\n");
					try {
						datainputStream.close();
						dataoutputStream.close();
						socket_user.close();
						vectorUser.remove(this);
						broadCast("UserOut/"+name);
						broadCast("userList_update/ ");
					} catch (IOException e1) {}
					break;
				}
			}
		}
		public void InMessage(String str){
			stringTokenizer=new StringTokenizer(str,"/");
			String protocol= stringTokenizer.nextToken();
			String message=stringTokenizer.nextToken();
			System.out.println("protocol :"+protocol);
			if(protocol.equals("Chatting")){
				String msg=stringTokenizer.nextToken();
				sendMessage("Chatting/"+message+"/"+msg);
			}
					}
		
	}

}
