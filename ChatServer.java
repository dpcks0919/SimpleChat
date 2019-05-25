//git hub : https://github.com/dpcks0919/SimpleChat


import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection....");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id; 
	private HashMap hm;
	private BufferedReader br;
	private PrintWriter pw;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
			 if(!line.contains("lol") && !line.contains("brb")&& !line.contains("btw")&& !line.contains("lmk")&& !line.contains("g2g") ) {	
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				else if(line.equals("/userlist")) {
					send_userlist();
				}
				else
					broadcast(id + " : " + line);
			 }
			 else {
					pw.println("Your sentence contains banned words. (lol , brb, btw, lmk, g2g) ");
					pw.flush();
			 }
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run 
	// line으로 읽어들이는 문자에 lol , brb, btw, lmk, g2g라는 단어가 포함되어 있는지 contains라는 함수로 확인하고 전부 다 없으면
	//예전과 같은 기능을 수행하고, 있다면 자신에게 경고문을 보낸다.
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw != this.pw) { 
				pw.println(msg);
				pw.flush();
				}
			}
		}
	} // broadcast 
	// iterator로 해쉬맵의 모든 정보를 돌 때, 그 값이 문자를 보낸 본인의 pw인지 확인하고 아니라면 값을 보내준다
	public void send_userlist() {	
		synchronized(hm){
			Set key = hm.keySet(); 
			  for (Iterator iterator = key.iterator(); iterator.hasNext();) { 
                  String keyName = (String) iterator.next();
                  pw.print(keyName+" ");
                  pw.println();
			  }
			}
		int x =java.lang.Thread.activeCount()-1; 
		pw.println("user's number"+"= "+ x );
		pw.flush();
		}  // send_userlist
		   //iterator로 키값을 받아와서 다음 값이 없을때까지 돌리는데 
		   //String에 값을 저장해서 본인에게 출력해준다.  
		   //현재 사용하고 있는 thread의 값은 java.lang.Thread.activeCount()를 통해 받아오는데 서버를 제외하기 위해 -1을 해준다.
	}