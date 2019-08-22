package taxi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.Random;
import java.awt.Point;

/*SPECIAL:static property belongs to the class but doesn't belong to the
 *object,so I don't include it in the present object.
 *PRESENT_OBJECT:(int c_code,int c_car_i,int c_car_j,int c_credit,int c_state,
 *int c_function,int c_sum,int c_path_sum,int c_path_to_sum,request c_rq,
 *double c_time,ArrayList<_node> c_path1,ArrayList<_node> c_path2,FileOutputStream c_out,
 *TaxiGUI c_gui,int c_temp_i,int c_temp_j,int c_record_i,int c_record_j)
 *AF:AF(c)=(code,car_i,car_j,credit,state,function,sum,path_sum,path_to_sum,
 *rq,time,path1,path2,out,gui,temp_i,temp_j,record_i,record_j)where(code==c.c_code,
 *car_i==c.c_car_i,car_j==c.c_car_j,credit==c.c_credit,state==c.c_state
 *function==c.c_function,sum==c.c_sum,path_sum==c.c_path_sum,path_to_sum==c.c_path_to_sum,
 *rq==c.c_rq,time==c.c_time,path1==c.c_path1,path2==c.c_path2,out==c.c_out,
 *gui==c.c_gui,temp_i==c.c_temp_i,temp_j==c.c_temp_j,record_i==c.c_record_i,record_j==c.c_record_j)
 *INVARIANCE:(1<=c.c_code<=100)&&(0<=c.c_car_i<80)&&(0<=c.c_car_j<80)&&(c.c_credit>=0)&&
 *(0<=c.c_state<=3)&&(0<=c.c_function<=3)&&(c.c_sum>=0)&&(c.c_path_sum>=0)&&(c.c_path_to_sum>=0)&&
 *(0<=c.c_temp_i<80&&0<=c.c_temp_j<80&&0<=c.c_record_i<80&&0<=c.c_record_j<80)
 */

/*OVERVIEW:This class controls the action of the car according to
 * the relevant rules and generates information to output.
 */
public class car extends Thread{
	int code;
	int car_i;
	int car_j;
	int credit;
	int state;//0 stop   1 action   2 wait   3 jiedan
	int function;
	int [][] map_car;
	static flow[][] pluse;
	static green_red[][] light;
	int sum;
	int path_sum;
	int path_to_sum;
	request rq = new request();
	double time;
	ArrayList<_node> path1 = new ArrayList<_node>();
	ArrayList<_node> path2 = new ArrayList<_node>();
	FileOutputStream out;
	TaxiGUI gui;
	int temp_i,temp_j;
	int record_i,record_j;
	car(){}
	car(int code,int car_i,int car_j,int credit,int state,int[][] map_array,TaxiGUI gui,flow[][] pluse,green_red[][] light)
	/*@REQUIRES:size==80
	 *@MODIFIES:code,car_i,car_j,credit,state,map_car,gui,pluse
	 *@EFFECTS:(\result this.code = code)
	 *(\result this.car_i = car_i)
	 *(\result this.car_j = car_j)
	 *(\result this.credit = credit)
	 *(\result this.state = state)
	 *(\result this.map_car = map_array)
	 *(\result this.pluse = pluse)
	 *(\result this.path_sum=0)
	 *(\result this.sum=0)
	 *(\result this.path_to_sum=0)
	 *(\result this.gui=gui)
	 */
	{
		this.code = code;
		this.car_i = car_i;
		this.car_j = car_j;
		this.credit = credit;
		this.state = state;
		this.map_car = map_array;
		this.pluse = pluse;
		this.light = light;
		this.sum = 0;/*��������*/
		this.path_sum = 0;/*��state=1ʱ������*/
		this.path_to_sum = 0;
		this.gui = gui;
		try {
			this.out = new FileOutputStream("car"+code+".txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
		try {
			out.write(("("+car_i+","+car_j+")"+"\r\n").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(code>=1&&code<=100&&car_i>=0&&car_i<80&&car_j>=0&&car_j<80&&credit>=0&&
		 state>=0&&state<=3&&function>=0&&function<=3&&sum>=0&&path_sum>=0&&path_to_sum>=0&&
		 temp_i>=0&&temp_i<80&&temp_j>=0&&temp_j<80&&record_i>=0&&record_i<80&&record_j>=0&&record_j<80){
			return true;
		}
		return false;
	}
	
	void record_path(_node[] quene,int k,ArrayList<_node> path)
	/*@REQUIRES:(path has pointer)&&(\all int i;i>=0&&i<6400;quene[i] instanceof _node)
	 *@MODIFIES:path
	 *@EFFECTS:(\all int i,j;0<=i&&i<j&&j<=k&&quene[i].path+1==quene[j].path)==>(\result path.add(quene[i])&&j=i&&i--)
	 */
	{
		 path.add(quene[k]);
		 for(int i=k-1;i>=0;i--){
			 if(quene[i].path==quene[k].path){
				 continue;
			 }else{
				 if(bridge(quene[i].i,quene[i].j,quene[k].i,quene[k].j)){
					 path.add(quene[i]);
					 k=i;
					 continue;
				 }else{
					 continue;
				 }
			 }
		 }
	}
	
	
	boolean bridge(int init1,int init2,int end1,int end2)
	/*@REQUIRES:(0<=init1&&init1<80)&&(0<=init2&&init2<80)&&(0<=end1&&end1<80)&&(0<=end2&&end2<80)
	 *@MODIFIES:None
	 *@EFFECTS:IF((init1,init2) is linked to (end1,end2))==>(\result return true)
	 *ELSE==>(\result return false)
	 */
	{
		if(init1<0||init1>79||init2<0||init2>79||end1<0||end1>79||end2<0||end2>79){
			return false;
		}
		if(init1-end1==-1){
			if(init2!=end2){
				return false;
			}else{
				if(map_car[init1][init2]==2||map_car[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}
		}else if(init1-end1==1){
			if(init2!=end2){
				return false;
			}else{
				if(map_car[end1][end2]==2||map_car[end1][end2]==3){
					return true;
				}else{
					return false;
				}
			}
			
		}else if(init1==end1){
			if(init2-end2==-1){
				if(map_car[init1][init2]==1||map_car[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}else if(init2-end2==1){
				if(map_car[end1][end2]==1||map_car[end1][end2]==3){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	synchronized void init()
	/*@REQUIRES:get the object lock
	 *@MODIFIES:path_sum,path1,path_to_sum,path2,pluse
	 *@EFFECTS:(\result path_sum=minpath((car_i,car_j),(rq.init_i,rq.init_j)))
	 *(\result path_to_sum=minpath((rq.init_i,rq.init_j),(rq.end_i,rq.end_j)))
	 *(\result path1 records minpath((car_i,car_j),(rq.init_i,rq.init_j)))
	 *(\result path2 records minpath((rq.init_i,rq.init_j),(rq.end_i,rq.end_j)))
	 *(\result pluse add in the 200ms window)
	 */
	{
		_node[] quene1 = new _node[6400];
		for(int k=0;k<6400;k++){
			quene1[k] = new _node();
		}
		path_sum = bfs(car_i,car_j,rq.init_i,rq.init_j,quene1);
		int k;
		for(k=6399;k>=0;k--){
			if(quene1[k].i==rq.init_i&&quene1[k].j==rq.init_j){
				break;
			}
		}
		//System.out.println("K:"+k);
		record_path(quene1,k,path1);
		path1.remove(path1.size()-1);
		
		_node[] quene2 = new _node[6400];
		for(int m=0;m<6400;m++){
			quene2[m] = new _node();
		}
		path_to_sum = bfs(rq.init_i,rq.init_j,rq.end_i,rq.end_j,quene2);
		int m;
		for(m=6399;m>=0;m--){
			if(quene2[m].i==rq.end_i&&quene2[m].j==rq.end_j){
				break;
			}
		}
		//System.out.println("M:"+m);
		record_path(quene2,m,path2);
		path2.remove(path2.size()-1);
		if(path1.size()!=0){
			////////////////////////////////////////////////////////
			//addflow(car_i,car_j,path1.get(path1.size()-1).i,path1.get(path1.size()-1).j);
		    ////////////////////////////////////////////////////////
		}
		//System.out.println("pp"+car_i+","+car_j);
	}
	
    public synchronized void run()
    /*@REQUIRES:get the object lock
	 *@MODIFIES:sum,time,car_i,car_j,temp_i,temp_j,path1,path2,state,pluse,credit
	 *@EFFECTS:(\result sum++)
	 *(\result time=time+100)
	 *((state==0&&sum==10)||(state==2))==>(\result car_i=temp_i&&car_j=temp_j&&temp_i=Random_a&&temp_j=Random_b&&bridge((car_i,car_j),(Random_a,Random_b))==true)
	 *(state==1)==>(\result car_i=path2.get(path2.size()-1).i&&car_j=path2.get(path2.size()-1).j&&path2.remove())
	 *(state==3)==>(\result car_i=path1.get(path1.size()-1).i&&car_j=path1.get(path1.size()-1).j&&path1.remove())
	 *(state==1&&sum==2*path_to_sum)==>(\result credit=credit+3&&state=0&&sum=0)
	 */
    {  
    	for(;;){
	    	if(state==3){
	    		if(car_i==rq.init_i&&car_j==rq.init_j){
	    			state = 0;
					sum = 1;
					continue;
	    		}
	    		///////////////////////////////////
	    		//System.out.println("car_i/j,rq.init.i/j,rq.end.i/j:"+car_i+","+car_j+","+rq.init_i+","+rq.init_j+","+rq.end_i+","+rq.end_j+","+path1.size());
				//subflow(car_i,car_j,path1.get(path1.size()-1).i,path1.get(path1.size()-1).j);///////
				record_i = car_i;
				record_j = car_j;
				///////////////////////////////////////////////////////////////////////////////////////////////
				car_i = path1.get(path1.size()-1).i;
				car_j = path1.get(path1.size()-1).j;
				function = get_function(record_i,record_j,car_i,car_j);
				//////////////////////////////////////////////////////////////////
				path1.remove(path1.size()-1);
				if(car_i==rq.init_i&&car_j==rq.init_j){
					gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
					state = 0;
					sum = 0;
					try {
						Date sys_time = new Date();
						out.write(("("+car_i+","+car_j+")"+"get customer"+"*TIME*"+sys_time.getTime()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
				}else{
					//addflow(car_i,car_j,path1.get(path1.size()-1).i,path1.get(path1.size()-1).j);//////
					gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
					try {
						Date sys_time = new Date();
						out.write(("("+car_i+","+car_j+")"+"*TIME*"+sys_time.getTime()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
					if(wait(function,car_i,car_j,path1.get(path1.size()-1).i,path1.get(path1.size()-1).j)){
						/////////////////////////
						///////////////////////////
						///////////////////////
						//System.out.println("wait1");
						synchronized(light[car_i][car_j]){
							try {
								out.write(("wait for the light!\r\n").getBytes());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
							try {
								light[car_i][car_j].wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
						}
						/////////////////////////////
						/////////////////////////////////
						////////////////////////////////////
					}
				}
				try {
					wait(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				continue;
	    	}
	    	if(state==1){
				//System.out.println("sum:"+sum+"code:"+code+"path1:"+path_sum+"path2:"+path_to_sum+"time:"+time);
				//System.out.println("path1:"+path1.size()+"path_sum"+path_sum+"sum:"+sum);////////
				//subflow(car_i,car_j,path2.get(path2.size()-1).i,path2.get(path2.size()-1).j);
				record_i = car_i;
				record_j = car_j;
				/////////////////////////////////////////////////////////////////////////////////////////////
				car_i = path2.get(path2.size()-1).i;
				car_j = path2.get(path2.size()-1).j;
				function = get_function(record_i,record_j,car_i,car_j);
				///////////////////////////////////////////////////////////////////////////////
				path2.remove(path2.size()-1);
				if(car_i==rq.end_i&&car_j==rq.end_j){
					gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
					state = 0;
					credit = credit+3;
					sum = 0;
					path_sum = 0;
					path_to_sum = 0;
					try {
						Date sys_time = new Date();
						out.write(("("+car_i+","+car_j+")"+"arrive"+"*TIME*"+sys_time.getTime()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
				}else{
					//addflow(car_i,car_j,path2.get(path2.size()-1).i,path2.get(path2.size()-1).j);
					gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
					try {
						Date sys_time = new Date();
						out.write(("("+car_i+","+car_j+")"+"*TIME*"+sys_time.getTime()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
					if(wait(function,car_i,car_j,path2.get(path2.size()-1).i,path2.get(path2.size()-1).j)){
						/////////////////////////////////////////
						///////////////////////////////////////////
						//System.out.println("wait2");
						synchronized(light[car_i][car_j]){
							try {
								out.write(("wait for the light!\r\n").getBytes());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
							try {
								light[car_i][car_j].wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
						}
						////////////////////////////////////////////////
						/////////////////////////////////////////
					}
				}
				try {
					wait(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				continue;
			}
			if(state==0){
				//System.out.println("car"+code+":is stopping");
				if(path_to_sum!=0){
					sum++;
					if(sum==5){
						state = 1;
						//addflow(car_i,car_j,path2.get(path2.size()-1).i,path2.get(path2.size()-1).j);
						gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
						sum = 0;
						try {
							wait(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
						continue;
					}else{
						gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
						try {
							wait(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
						continue;
					}
				}else{
					sum++;
					gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
					if(sum==5){
						state = 2;
						sum = 0;
						Random random = new Random();
						temp_i = car_i;
						temp_j = car_j;
						int[] func = new int[4];
						min(func,temp_i,temp_j);
						for(;;){
							int function = random.nextInt(4);
							if(function==0&&func[0]==1&&car_i!=0&&(map_car[car_i-1][car_j]==2||map_car[car_i-1][car_j]==3)){
								temp_i = car_i-1;
								break;
							}
							if(function==1&&func[1]==1&&car_j!=79&&(map_car[car_i][car_j]==1||map_car[car_i][car_j]==3)){
								temp_j = car_j+1;
								break;
							}
							if(function==2&&func[2]==1&&car_i!=79&&(map_car[car_i][car_j]==2||map_car[car_i][car_j]==3)){
								temp_i = car_i+1;
								break;
							}
							if(function==3&&func[3]==1&&car_j!=0&&(map_car[car_i][car_j-1]==1||map_car[car_i][car_j-1]==3)){
								temp_j = car_j-1;
								break;
							}
						}
						//addflow(car_i,car_j,temp_i,temp_j);
						if(wait(function,car_i,car_j,temp_i,temp_j)){
							//////////////////////////////////////
							/////////////////////////////////////////
							//System.out.println("wait3");
							synchronized(light[car_i][car_j]){
								try {
									out.write(("wait for the light!\r\n").getBytes());
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									System.exit(0);
								}
								try {
									light[car_i][car_j].wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									System.exit(0);
								}
							}
							//////////////////////////////////////////
							/////////////////////////////////////////
						}
						try {
							wait(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
						continue;	
					}else{
						try {
							wait(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
						continue;	
					}	
				}
				
			}
		    if(state==2){
				//System.out.println("car"+code+":is waiting");
				sum++;
				Random random = new Random();
				//subflow(car_i,car_j,temp_i,temp_j);
				record_i = car_i;
				record_j = car_j;
				///////////////////////////////////////////////////////////
				car_i = temp_i;
				car_j = temp_j;
				function = get_function(record_i,record_j,car_i,car_j);
				gui.SetTaxiStatus(code-1,new Point(car_i,car_j),state);
				try {
					Date sys_time = new Date();
					out.write(("("+car_i+","+car_j+")"+"*TIME*"+sys_time.getTime()+"\r\n").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				int[] func = new int[4];
				min(func,temp_i,temp_j);
				for(;;){
					int function = random.nextInt(4);
					if(function==0&&func[0]==1&&car_i!=0&&(map_car[car_i-1][car_j]==2||map_car[car_i-1][car_j]==3)){
						temp_i = car_i-1;
						break;
					}
					if(function==1&&func[1]==1&&car_j!=79&&(map_car[car_i][car_j]==1||map_car[car_i][car_j]==3)){
						temp_j = car_j+1;
						break;
					}
					if(function==2&&func[2]==1&&car_i!=79&&(map_car[car_i][car_j]==2||map_car[car_i][car_j]==3)){
						temp_i = car_i+1;
						break;
					}
					if(function==3&&func[3]==1&&car_j!=0&&(map_car[car_i][car_j-1]==1||map_car[car_i][car_j-1]==3)){
						temp_j = car_j-1;
						break;
					}
				}
				if(sum==100){
					state = 0;
					sum = 0;
				}else{
					//addflow(car_i,car_j,temp_i,temp_j);
					if(wait(function,car_i,car_j,temp_i,temp_j)){
						////////////////////////////////////////
						/////////////////////////////////////////////
						//System.out.println("wait4");
						synchronized(light[car_i][car_j]){
							try {
								out.write(("wait for the light!\r\n").getBytes());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
							try {
								light[car_i][car_j].wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
						}
						///////////////////////////////////////////////////
						///////////////////////////////////////////////////
					}
				}
				try {
					wait(200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				continue;
			}
        }
    }
/*190,216,237,274,298,336,386,425,464row_is_the_function_of_flow*/
    
    void min(int[] biao,int temp_i,int temp_j)
    /*@REQUIRES:sizeof(biao)==4
	 *@MODIFIES:biao
	 *@EFFECTS:(\all int i;0<=i<4)==>(\result biao[i]=pluse((temp_i,temp_j),i))
	 *special(pluse((temp_i,temp_j),i).exist==false==>biao[i]=101)
	 */
    {
    	if(temp_i!=0&&bridge(temp_i,temp_j,temp_i-1,temp_j)){
    		//biao[0] = pluse[temp_i-1][temp_j].xia;
    		biao[0] = guigv.GetFlow(temp_i,temp_j,temp_i-1,temp_j);
    	}else{
    		biao[0] = 101;
    	}
    	if(temp_j==79){
    		biao[1] = 101;
    	}else if(map_car[temp_i][temp_j]==1||map_car[temp_i][temp_j]==3){
    		//biao[1] = pluse[temp_i][temp_j].you;
    		biao[1] = guigv.GetFlow(temp_i,temp_j,temp_i,temp_j+1);
    	}else{
    		biao[1] = 101;
    	}
    	if(temp_i==79){
    		biao[2] = 101;
    	}else if(map_car[temp_i][temp_j]==2||map_car[temp_i][temp_j]==3){
    		//biao[2] = pluse[temp_i][temp_j].xia;
    		biao[2] = guigv.GetFlow(temp_i, temp_j, temp_i+1, temp_j);
    	}else{
    		biao[2] = 101;
    	}
    	if(temp_j!=0&&bridge(temp_i,temp_j,temp_i,temp_j-1)){
    		//biao[3] = pluse[temp_i][temp_j-1].you;
    		biao[3] = guigv.GetFlow(temp_i, temp_j, temp_i, temp_j-1);
    	}else{
    		biao[3] = 101;
    	}
    	int minxe = 200;
    	for(int i=0;i<4;i++){
    		if(biao[i]<minxe){
    			minxe = biao[i];
    		}
    	}
    	for(int i=0;i<4;i++){
    		if(biao[i]==minxe){
    			biao[i] = 1;
    		}else{
    			biao[i] = 0;
    		}
    	}
    }
    
    void addflow(int init1,int init2,int end1,int end2)
    /*@REQUIRES:(0<=init1<80)&&(0<=init2<80)&&(0<=end1<80)&&(0<=end2<80)
	 *@MODIFIES:pluse
	 *@EFFECTS:(\result pluse((init1,init2),(end1,end2))++)
	 */
    {
        if(init1>end1){
        	pluse[end1][end2].xia++;
        }else if(init1<end1){
        	pluse[init1][init2].xia++;
        }else{
        	if(end2<init2){
        		pluse[end1][end2].you++;
        	}else if(end2>init2){
        		pluse[init1][init2].you++;
        	}
        }
    }
    void subflow(int init1,int init2,int end1,int end2)
    /*@REQUIRES:(0<=init1<80)&&(0<=init2<80)&&(0<=end1<80)&&(0<=end2<80)
	 *@MODIFIES:pluse
	 *@EFFECTS:(\result pluse((init1,init2),(end1,end2))--)
	 */
    {
        if(init1>end1){
        	pluse[end1][end2].xia--;
        }else if(init1<end1){
        	pluse[init1][init2].xia--;
        }else{
        	if(end2<init2){
        		pluse[end1][end2].you--;
        	}else if(end2>init2){
        		pluse[init1][init2].you--;
        	}
        }
    }
    static int _getflow(int init1,int init2,int end1,int end2)
    /*@REQUIRES:(0<=init1<80)&&(0<=init2<80)&&(0<=end1<80)&&(0<=end2<80)
	 *@MODIFIES:None
	 *@EFFECTS:(\result return pluse((init1,init2),(end1,end2)))
	 */
    {
    	if(init1>end1){
        	return pluse[end1][end2].xia;
        }else if(init1<end1){
        	return pluse[init1][init2].xia;
        }else{
        	if(end2<init2){
        		return pluse[end1][end2].you;
        	}else if(end2>init2){
        		return pluse[init1][init2].you;
        	}
        }
    	return 0;
    }
    
	int bfs(int init1,int init2,int end1,int end2,_node[] quene)
	/*@REQUIRES:(map is linked)&&(0<=init1<80)&&(0<=init2<80)&&(0<=end1<80)&&(0<=end2<80)
	 *@MODIFIES:quene
	 *@EFFECTS:from (init1,init2) on,add the dots which are linked to (init1,init2) to
	 *quene;then add the dots which are linked to these new added dots to the quene;until
	 *add the dot (end1,end2) then over
	 */
	{
		int temp=0,b;
		//node[] quene = new node[6400];
		quene[temp].i = init1;
		quene[temp].j = init2;
		quene[temp].valid = 1;
		quene[temp].path = 0;
		//quene.add(new _node(init1,init2,1,0));
		temp = 1;
		b = add(0,0,quene,end1,end2);
		//////////////////////////////////////////////
		for(int i=0;i<6400;i++){
			if(quene[i].i==end1&&quene[i].j==end2){
				return quene[i].path;
			}
		}
		////////////////////////////////////////////////////
		for(;b<6400;){
			int fu = b;
			b = add(temp,b,quene,end1,end2);
			temp = fu+1;
			for(int i=0;i<6400;i++){
				if(quene[i].i==end1&&quene[i].j==end2){
					return quene[i].path;
				}
			}
		}
		//////////////////////////////////////////////
		return 0;
	}
	
	int add(int a,int b,_node[] quene,int end1,int end2)
	/*@REQUIRES:(0<=a&&a<b&&b<6400)&&((0<=end1<80)&&(0<=end2<80))
	 *@MODIFIES:quene
	 *@EFFECTS:(\all int i;a<=i<=b)==>(\result add the dots which is linked to quene[i] to the quene
	 *until find the dot (end1,end2))
	 */
	{
		int i=a,temp=b;
		a = b+1;
		if(temp==6399){
			return 6399;
		}
		for(;i<=temp;i++){
			if(quene[i].i!=0&&(map_car[quene[i].i-1][quene[i].j]==2||map_car[quene[i].i-1][quene[i].j]==3)){
				if(addone(quene[i].i-1,quene[i].j,quene[i].path+1,quene)==1){
					b++;
				}
				if(quene[i].i-1==end1&&quene[i].j==end2){
					return b;
				}
			}/*shang*/
			if(quene[i].j!=0&&(map_car[quene[i].i][quene[i].j-1]==1||map_car[quene[i].i][quene[i].j-1]==3)){
				if(addone(quene[i].i,quene[i].j-1,quene[i].path+1,quene)==1){
					b++;
				}
				if(quene[i].i==end1&&quene[i].j-1==end2){
					return b;
				}
			}/*zuo*/
			if(quene[i].j!=79&&(map_car[quene[i].i][quene[i].j]==1||map_car[quene[i].i][quene[i].j]==3)){
				if(addone(quene[i].i,quene[i].j+1,quene[i].path+1,quene)==1){
					b++;
				}
				if(quene[i].i==end1&&quene[i].j+1==end2){
					return b;
				}
			}/*you*/
			if(quene[i].i!=79&&(map_car[quene[i].i][quene[i].j]==2||map_car[quene[i].i][quene[i].j]==3)){
				if(addone(quene[i].i+1,quene[i].j,quene[i].path+1,quene)==1){
					b++;
				}
				if(quene[i].i+1==end1&&quene[i].j==end2){
					return b;
				}
			}/*xia*/
		}
		//add(a,b,quene,end1,end2);
		return b;
	}
	
	static int addone(int init1,int init2,int _path,_node[] quene)
	/*@REQUIRES:(0<=init1<80)&&(0<=init2<80)&&(quene has the pointer)
	 *@MODIFIES:quene
	 *@EFFECTS:(\result add new _node((init1,init2),_path,0) to quene)
	 */
	{
		for(int i=0;;i++){
			if(quene[i].valid==1){
				if(quene[i].i==init1&&quene[i].j==init2){
					return 0;
				}else{
					continue;
				}
			}else{
				quene[i].i = init1;
				quene[i].j = init2;
				quene[i].valid = 1;
				quene[i].path = _path;
				return 1;
 			}
		}
	}
	
	int get_function(int past_i,int past_j,int now_i,int now_j)
	/*@REQUIRES:(0<=past_i<80)&&(0<=past_j<80)&&(0<=now_i<80)&&(0<=now_j<80)
	 *@MODIFIES:None 
	 *@EFFECTS:If(north),Then(\result return 0)
	 *If(earth),Then(\result return 1)
	 *If(south),Then(\result return 2)
	 *If(west),Then(\result return 3) 
	 */
	{
		if(past_i-now_i==-1){
			return 2;
		}else if(past_i-now_i==1){
			return 0;
		}else if(past_j-now_j==-1){
			return 1;
		}else{
			return 3;
		}
	}
	 boolean wait(int _function,int now_i,int now_j,int will_i,int will_j)
	 /*@REQUIRES:((_function==0)||(_function==1)||(_function==2)||(_function==3))&&
	  *(0<=now_i<80)&&(0<=now_j<80)&&(0<=will_i<80)&&(0<=will_j<80) 
	  *@MODIFIES:None
	  *@EFFECTS:According to the _function,now_i,now_j,will_i,will_j and
	  *the static property "light",judge whether the car need to wait for the
	  *light.If need,return true;Else return false. 
	  */
	 {
	    	//use light[][] false not_wait   true wait
	    	switch(_function){
		    	case 0:{
		    		if(will_i-now_i==1){
		    			return false;
		    		}else if(will_i-now_i==-1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==1){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else if(will_j-now_j==-1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==0){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else{
		    			return false;
		    		}
		    	}
		    	case 1:{
		    		if(will_j-now_j==-1){
		    			return false;
		    		}else if(will_j-now_j==1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==0){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else if(will_i-now_i==-1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==1){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else{
		    			return false;
		    		}
		    	}
		    	case 2:{
		    		if(will_i-now_i==-1){
		    			return false;
		    		}else if(will_i-now_i==1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==1){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else if(will_j-now_j==1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==0){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else{
		    			return false;
		    		}
		    	}
		    	case 3:{
		    		if(will_j-now_j==1){
		    			return false;
		    		}else if(will_j-now_j==-1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==0){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else if(will_i-now_i==1){
		    			if(light[now_i][now_j].valid==1&&light[now_i][now_j].color_row==1){
		    				return true;
		    			}else{
		    				return false;
		    			}
		    		}else{
		    			return false;
		    		}
		    	}
	    	}
			return false;
	    }
	 ListIterator<String> get_iterator()
		/*REQUIRES:None
		 *MODIFIES:None 
		 *EFFECTS:(\result return listiterator) 
		 */
	 {
		 return null;
	 }
}

/*PRESENT_OBJECT:(ArrayList<ArrayList<_node>> c_record1,ArrayList<ArrayList<_node>> c_record2,
 * ArrayList<request> c_record_rq,FileOutputStream c_specialout)
 *AF:AF(c)=(record1,record2,record_rq,specialout)where(record1==c_record1,record2==c_record2,
 *record_rq==c_record_rq,specialout==c_specialout)
 *INVARIANCE:(c_record1!=NULL)&&(c_record2!=NULL)&&(c_record_rq!=NULL)&&(c_specialout!=NULL) 
 */

/*OVERVIEW:This is the class required by the guidebook to define the special
 *car. 
 */
class specialcar extends car{
	ArrayList<ArrayList<_node>> record1 = new ArrayList<ArrayList<_node>>();
	ArrayList<ArrayList<_node>> record2 = new ArrayList<ArrayList<_node>>();
	ArrayList<request> record_rq = new ArrayList<request>();
	FileOutputStream specialout;
	ArrayList<String> goal;
	specialcar(int code,int car_i,int car_j,int credit,int state,int[][] map_array,TaxiGUI gui,flow[][] pluse,green_red[][] light)
	/*@REQUIRES:size==80
	 *@MODIFIES:code,car_i,car_j,credit,state,map_car,gui,pluse,out,specialout
	 *@EFFECTS:(\result this.code = code)
	 *(\result this.car_i = car_i)
	 *(\result this.car_j = car_j)
	 *(\result this.credit = credit)
	 *(\result this.state = state)
	 *(\result this.map_car = map_array)
	 *(\result this.pluse = pluse)
	 *(\result this.path_sum=0)
	 *(\result this.sum=0)
	 *(\result this.path_to_sum=0)
	 *(\result this.gui=gui)
	 *(\result this.out=new(out))
	 *(\result this.specialout=new(specialout))
	 */
	{
		this.code = code;
		this.car_i = car_i;
		this.car_j = car_j;
		this.credit = credit;
		this.state = state;
		/*this.map_car = map_array;*/
		map_car = new int[80][80];
		for(int i=0;i<80;i++){
			for(int j=0;j<80;j++){
				map_car[i][j] = map_array[i][j];
			}
		}/*copy the map*/
		this.pluse = pluse;
		this.light = light;
		this.sum = 0;/*��������*/
		this.path_sum = 0;/*��state=1ʱ������*/
		this.path_to_sum = 0;
		this.gui = gui;
		try {
			this.out = new FileOutputStream("car"+code+".txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
		try {
			this.specialout = new FileOutputStream("Iterator_car"+code+".txt");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
		try {
			out.write(("("+car_i+","+car_j+")"+"\r\n").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
	}
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(!super.repOK()){
			return false;
		}else{
			if(specialout==null){
				return false;
			}else{
				return true;
			}
		}
	}
	synchronized void init()
	/*REQUIRES:get the object lock
	 *MODIFIES:path1,path2,record1,record2,record_rq 
	 *EFFECTS:path1,path2 will be modified by the new route and record1,record2
	 *and record_rq will record the information which will be needed by iterator. 
	 */
	{
		super.init();/*only has a child class,and the system will think it apply this(child) lock*/
		ArrayList<_node> fu1 = new ArrayList<_node>();
		for(int i=path1.size()-1;i>=0;i--){
			fu1.add(new _node(path1.get(i).i,path1.get(i).j));
		}
		record1.add(fu1);
		ArrayList<_node> fu2 = new ArrayList<_node>();
		for(int i=path2.size()-1;i>=0;i--){
			fu2.add(new _node(path2.get(i).i,path2.get(i).j));
		}
		record2.add(fu2);
		record_rq.add(rq);
	}
	
	void dd()
	/*SPECIAL:This method is prepared for you to test.You can change this method
	 *to use the get_iterator() to generate some outputs.I only write some simple
	 *outputs for you to display how to test. 
	 */
	/*REQUIRES:None
	 *MODIFIES:None 
	 *EFFECTS:print the recorded information to relevant txt
	 *This is the method which can be changed by you to test the iterator. 
	 */
	{/*
		ListIterator it1 = record1.listIterator();
		ListIterator it2 = record2.listIterator();
		ListIterator it3 = record_rq.listIterator();
		while(it3.hasNext()){
			try {
				specialout.write(("Iterator:"+it3.next()+"\r\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.exit(0);
			}
			while(it1.hasNext()){
				ListIterator func_it1 = ((ArrayList<_node>)it1.next()).listIterator();
			    while(func_it1.hasNext()){
			    	try {
						specialout.write(("path1:"+(_node)func_it1.next()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
			    }
			    break;
			}
			while(it2.hasNext()){
				ListIterator func_it2 = ((ArrayList<_node>)it2.next()).listIterator();
			    while(func_it2.hasNext()){
			    	try {
						specialout.write(("path2"+(_node)func_it2.next()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
			    }
			    break;
			}
		}*/
		ListIterator<String> mem = get_iterator();
		if(mem.hasNext()){
			try {
				specialout.write((mem.next()+"\r\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.exit(0);
			}
			if(mem.hasNext()){
				try {
					specialout.write((mem.next()+"\r\n").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				if(mem.hasPrevious()){
					try {
						specialout.write((mem.previous()+"\r\n").getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.exit(0);
					}
					if(mem.hasPrevious()){
						try {
							specialout.write((mem.previous()+"\r\n").getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
					}
				}
			}
		}
	}
	
	ListIterator<String> get_iterator()
	/*REQUIRES:None
	 *MODIFIES:None 
	 *EFFECTS:(\result return listiterator) 
	 */
	{
		goal = new ArrayList<String>();
		for(int i=0;i<record_rq.size();i++){
			String m = record_rq.get(i).toString()+"\r\n";
			for(int j=0;j<record1.get(i).size();j++){
				m = m+"path1:"+record1.get(i).get(j).toString()+"\r\n";
			}
			for(int j=0;j<record2.get(i).size();j++){
				m = m+"path2:"+record2.get(i).get(j).toString()+"\r\n";
			}
			goal.add(m);
		}
		return goal.listIterator();
	}
}












