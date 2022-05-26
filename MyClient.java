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
    String lstj_servertype;
    String lstj_data;
    String lstj_serverid;
    int est_runtime;
    int est_runtime_temp;
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

    this.sendRequest("OK");
    this.rcvRequest();

    }

    public void schdJob(String job, String servertype, String serverid)throws IOException{
    this.sendRequest("SCHD " + job + " " + servertype+ " " + serverid); 
    return;
    }

    public void lstj()throws IOException{
        this.est_runtime=9999999;
        for(int p = 0; p<this.server_list.length; p++){
            this.est_runtime_temp=0;
            String temp_servertype = this.server_list[p].split(" ")[0];
            String temp_serverid=this.server_list[p].split(" ")[1];
            this.sendRequest("LSTJ "+temp_servertype+" " +temp_serverid);
            lstj_data=this.rcvRequest();
            this.sendRequest("OK");
            int numJobs=(Integer.parseInt(lstj_data.split(" ")[1]));

            if (numJobs==0){
                this.lstj_servertype=temp_servertype;
                this.lstj_serverid=temp_serverid;
                break;
            }
            
            for(int q=0;q<Integer.parseInt(lstj_data.split(" ")[1]);q++){
                int est_runtime_add=Integer.parseInt(this.rcvRequest().split(" ")[4]);
                this.est_runtime_temp=this.est_runtime_temp+est_runtime_add;
                }
            
            if (this.est_runtime_temp<this.est_runtime){
                this.est_runtime=this.est_runtime_temp;
                this.lstj_servertype=temp_servertype;
                this.lstj_serverid=temp_serverid;
            }
            this.sendRequest("OK");
            this.rcvRequest();
        }
        
        this.schdJob(this.job, this.lstj_servertype, this.lstj_serverid);
    }

    public void jobnHandle(String[] response)throws IOException{
    job = response[2];
    this.sendRequest(String.format("GETS Capable %s %s %s", response[4], response[5], response[6]));
    this.handleResponse(this.rcvRequest());
    lstj();
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
    
       
    public void sendRequest(String request)throws IOException{
    this.dout.write((request+"\n").getBytes());
    }
    
    
    public String rcvRequest()throws IOException{
    String strf = "";
    
    strf=din.readLine();
    //System.out.println(strf+"\n");
    return(strf);
    }}
    
    

    
