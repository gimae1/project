package com.Client;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

public class Client extends JFrame implements ActionListener,KeyListener ,MouseListener{
	//loginUI
	JFrame logIn; 
	JPanel loginPanel;
	JLabel labelIp,labelPort,labelName;
	TextField ipField,portField,nameField;
	JButton loginButton;
	//mainUI
	JPanel mainPanel;
	TextArea chattingArea;
	TextField typingField;
	JLabel nameLabel,timerLabel;
	JList nameList = new JList();
	JButton voteButton,sendButton;
	
	//network
	Socket socket;
	OutputStream outputStream;
	InputStream inputStream;
	DataOutputStream dataoutputStream;
	DataInputStream datainputStream;
	
	StringTokenizer stringTokenizer;
	Vector vectorUserList = new Vector();
	
	int port=5000;
	String ip="127,0,0,1";
	String name="";
	
	public void loginUI(){
		logIn=new JFrame("login");
		logIn.setDefaultCloseOperation(EXIT_ON_CLOSE);
		loginPanel=new JPanel();
		loginPanel.setLayout(null);
		//�������ּ�
		labelIp=new JLabel("IP Address");
		labelIp.setBounds(30,220,80,20);
		loginPanel.add(labelIp);
		ipField=new TextField();
		ipField.setBounds(110,220,130,20);
		loginPanel.add(ipField);
		//��Ʈ��ȣ 
		labelPort=new JLabel("Port Number");
		labelPort.setBounds(30,250,80,20);
		loginPanel.add(labelPort);
		portField=new TextField();
		portField.setBounds(110,250,130,20);
		loginPanel.add(portField);
		//�����̸�
		labelName=new JLabel("UserName");
		labelName.setBounds(30,280,80,20);
		loginPanel.add(labelName);
		//�����̸��Է�
		nameField=new TextField();
		nameField.setBounds(110,280,130,20);
		loginPanel.add(nameField);
		//�α��� ��ư
		loginButton=new JButton("�α���");
		loginButton.setBounds(40,350,200,30);
		loginButton.addActionListener(this);
		loginPanel.add(loginButton);
		//�г� �ޱ�	
		logIn.add(loginPanel);
		logIn.setBounds(100,100,300,500);
		logIn.setVisible(true);
	}
	public void mainUI(){
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Mafia Game(ver 0.1.0)");
		mainPanel=new JPanel();
		mainPanel.setLayout(null);
		//ä�ö�
		chattingArea=new TextArea("",0,0,1);
		chattingArea.setBounds(120,10,550,450);
		mainPanel.add(chattingArea);
		//������Ȳ
		nameLabel=new JLabel("�� �� �� Ȳ");
		nameLabel.setBounds(30,10,100,20);
		mainPanel.add(nameLabel);
		//���� ����Ʈ
		nameList.setBounds(10,50,100,410);
		nameList.setListData(vectorUserList);
		mainPanel.add(nameList);
		//��ǥ��ư
		voteButton=new JButton("�� ��");
		voteButton.setBounds(10,470,100,40);
		mainPanel.add(voteButton);
		//���۹�ư
		sendButton=new JButton("�� ��");
		sendButton.setBounds(575,470,95,80);
		mainPanel.add(sendButton);
		//Ÿ�̸� 
		timerLabel=new JLabel("60 ��");
		timerLabel.setBounds(45,515,70,40);
		mainPanel.add(timerLabel);
		//���� �Է�ĭ
		typingField=new TextField();
		typingField.setBounds(120,470,450,80);
		mainPanel.add(typingField);
		add(mainPanel);
		setBounds(100,100,700,600);
		setVisible(false);
	}
	public Client(){
		loginUI();//�α���UI
		mainUI();//main ui
		actionAdd();//action
	}
	

	
	public void actionAdd(){
		voteButton.addActionListener(this);//vote button
		sendButton.addActionListener(this);//send button
		typingField.addKeyListener(this);//chat field 
		nameList.addMouseListener(this);//list
	}
	public void clientNetwork(){//client socket network 
			try {
				socket=new Socket(ip,port);
				if(socket!=null){
					connection();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	public void connection(){//send message to server
		try {
			outputStream=socket.getOutputStream();
			inputStream=socket.getInputStream();
			dataoutputStream=new DataOutputStream(outputStream);
			datainputStream=new DataInputStream(inputStream);
			} catch (IOException e) {
			e.printStackTrace();
		}
		this.setVisible(true);
		this.logIn.setVisible(false);
		sendMessage(name);
		vectorUserList.add(name);//add my name in userList
		Thread thread =new Thread(new SocketThread());
		thread.start();
	}
	public class SocketThread implements Runnable{
		@Override
		public void run() {
			while(true){
			try {
				InMessage(datainputStream.readUTF());
			} catch (IOException e) {
				try {
					dataoutputStream.close();
					datainputStream.close();
					inputStream.close();
					outputStream.close();
					socket.close();
					JOptionPane.showMessageDialog(null, "������ ���� ������", "�˸�", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {}
				break;
				}
			}
		}
	}
//	public class Timer extends Thread{
//		public Timer(){
//			Thread thr=new Thread();
//			while(true){
//				int i=60;
//			timerLabel.setText("");
//			timerLabel.setText(i+"��");	
//				i--;
//			try {
//				thr.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			}
//		}
//		
//	}
	public void InMessage(String str){
		
		stringTokenizer=new StringTokenizer(str,"/");// ������ ������ protocol/message/ ������ ����
		String [] arr= new String[3];
		int i=0;
		while(stringTokenizer.hasMoreTokens()){
			arr[i]=stringTokenizer.nextToken();
			i++;
		}
		
		String protocol=arr[0];
		String message=arr[1];
		
		System.out.println("���� : "+protocol);
		System.out.println("���� : "+message);
	
		if(protocol.equals("NewUser")){
			vectorUserList.add(message);
		}else if(protocol.equals("OldUser")){
			vectorUserList.add(message);
		}
		else if(protocol.equals("userList_update/")){
			nameList.setListData(vectorUserList);
		}
		 if(protocol.equals("Chatting")){
			String msg = arr[2];
			chattingArea.append(message + " : " + msg + "\n");// message==name, msg==chatting message
		}
	}
	public void sendMessage(String message){
		try {
			dataoutputStream.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new Client();
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void keyPressed(KeyEvent e) {
	if(e.getKeyCode()==10){
		System.out.println("sendMessage");
		sendMessage("Chatting/"+name+"/"+typingField.getText().trim());
		typingField.setText("");
		typingField.requestFocus();
	}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==loginButton){
			if(ipField.getText().length()==0){
				JOptionPane.showMessageDialog(null, "IP�� �Է����ּ���", "�˸�", JOptionPane.ERROR_MESSAGE);
				ipField.requestFocus();
			}else if(portField.getText().length()==0){
				JOptionPane.showMessageDialog(null, "Port��ȣ�� �Է����ּ���", "�˸�",JOptionPane.ERROR_MESSAGE);
				portField.requestFocus();
			}else if(nameField.getText().length()==0){
				JOptionPane.showMessageDialog(null, "UserName�� �Է����ּ���", "�˸�",JOptionPane.ERROR_MESSAGE);
				nameField.requestFocus();
			}else{
				ip=ipField.getText().trim();
				port=Integer.parseInt(portField.getText().trim());
				name=nameField.getText().trim();
				clientNetwork();
			}
		}else if(e.getSource()==voteButton){
			JOptionPane.showMessageDialog(null, "�����Ͻðڽ��ϱ�?", "VOTE", JOptionPane.YES_NO_CANCEL_OPTION);
		}else if(e.getSource()==sendButton){
			System.out.println("sendMessage");
			sendMessage("Chatting/"+name+"/"+typingField.getText().trim());
			typingField.setText("");
			typingField.requestFocus();
		}else if(e.getSource()==nameList){
			System.out.println(e.getSource());
		}
		typingField.requestFocus();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource()==nameList){
			System.out.println(e.getSource());
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
}
