    import java.net.*;  
    import java.io.*;  
    class MyClient{  
    Socket s;  
    BufferedReader din;
    DataOutputStream dout;
    String[] server_list;
    String biggestServer_type;
    int biggestServer_num;
    int serverCounter = 0;
    String job;
    int dataGet = 0;
    public MyClient(){
    try{
    this.s = new Socket("localhost",50000);
    this.din = new BufferedReader(new InputStreamReader(s.getInputStream())); 
    this.dout = new DataOutputStream(s.getOutputStream());
    }
    catch(Exception e){
    System.out.println(e);
    }}
    
    public static void main(String args[])throws IOException{  
    MyClient client = new MyClient();
    client.handshake();
    client.sendRequest("REDY");
    while(true){
    client.handleResponse(client.rcvRequest());
    }}
    
    public void handleResponse(String response)throws IOException{
    String[] responsesplit = response.split(" ");
    if(responsesplit[0].equals("JOBN")){
    jobnHandle(responsesplit);
    }
    else if(responsesplit[0].equals("DATA")){
    dataHandle(responsesplit);   
    }
    else if(responsesplit[0].equals("JCPL")){
    this.sendRequest("REDY");
    }
    else if(responsesplit[0].equals("NONE")){
    this.closeConnection();
    System.exit(0);
    }
    else if(responsesplit[0].equals("OK")){
    this.sendRequest("REDY");
    }}
    
    public void dataHandle(String[] response)throws IOException{
    int no_servers = Integer.parseInt(response[1]);
    this.server_list = new String[no_servers]; 
    this.sendRequest("OK");
    for(int p = 0; p < no_servers; p++){
    this.server_list[p]=this.rcvRequest();
    }
    
    this.getBiggest(this.server_list);
    this.sendRequest("OK");
    this.rcvRequest();
    this.schdJob(job);
    }


    public void schdJob(String job)throws IOException{
    this.sendRequest("SCHD " + job + " " + biggestServer_type + " " + serverCounter); 
    if(this.serverCounter<biggestServer_num){
        this.serverCounter++;
    }
    else if(this.serverCounter==biggestServer_num){
        this.serverCounter=0;
    }
    return;
    }
    
    public void jobnHandle(String[] response)throws IOException{
    job = response[2];
    if(this.dataGet==0){
        this.sendRequest(String.format("GETS All"));
        dataGet=1;
    }
    else{
        schdJob(job);
    }
    // this.sendRequest(String.format("GETS Capable %s %s %s", response[4], response[5], response[6]));
    
    return;
    }
    
    public void closeConnection()throws IOException{
    this.sendRequest("QUIT");
    this.rcvRequest();
    this.din.close();  
    this.s.close();  
    this.dout.close();
    }
    
    public void handshake()throws IOException{
    this.sendRequest("HELO");
    this.rcvRequest();
    this.sendRequest("AUTH "+System.getProperty("user.name"));
    this.rcvRequest();
    }
    
    public void getBiggest(String[] list)throws IOException{
    int max = 0;
    
    for(int i = 0; i<list.length; i++){
    String[] listsplit = list[i].split(" ");
    int cores = Integer.parseInt(listsplit[4]);
    if(cores==max){
        if(listsplit[0].equals(biggestServer_type)){
            biggestServer_num++;
        }
    }
    else if(cores>max){
    biggestServer_type = listsplit[0];
    biggestServer_num = 0;
    max = cores;
    }}
    return;
    }
    
       
    public void sendRequest(String request)throws IOException{
    this.dout.write((request+"\n").getBytes());
    }
    
    
    public String rcvRequest()throws IOException{
    String strf = "";
    
    strf=din.readLine();
    // System.out.println(strf+"\n");
    return(strf);
    }}
    
    

    
