package taxi;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Point;

/*PRESENT_OBJECT&AF&INVARIANCE:Except the static properties,there is no other property so
 *I ignore the PRESENT_OBJECT, the ABSTRACT FUNCTION and the INVARIANCE.  
 */

/*OVERVIEW:This is the class responsible for handling input and starting other
 * thread. And this can be thought as main class, you should run this ".java".
 */

public class input {
    static double inittime; 
    static int[][] map_array = new int[80][80];
    static green_red[][] light = new green_red[80][80];///////////////////////////////////
	public static void main(String[] args) throws FileNotFoundException 
	/*@REQUIRES:size == 80&&\exist "map.txt"
     *@MODIFIES:inittime,map_array,option,System.out
     *@EFFECTS:(\old(map_array==NULL)==>\new(map_array=="map.txt"))
     *(\old(option==NULL)==>\new(option.add(request))
     *(\old(map_array))==>(\new(map_array))(open/close the path)
     */
	{
		// TODO Auto-generated method stub
    	Date time = new Date();
		inittime = time.getTime();
		int i,j;
		String s;
		try {
			BufferedReader in = new BufferedReader(new FileReader("map.txt"));
			for(i=0;i<80;i++){
				try {
					s=in.readLine();
					s=s.replaceAll(" ","");
					s=s.replaceAll("\t","");
					for(j=0;j<80;j++){
						map_array[i][j] = s.charAt(j)-'0';
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
		//get map_array
		/////////////////////////////////
		try{
			BufferedReader in_light = new BufferedReader(new FileReader("light.txt"));
			for(i=0;i<80;i++){
				try {
					s=in_light.readLine();
					s=s.replaceAll(" ","");
					s=s.replaceAll("\t","");
					for(j=0;j<80;j++){
						light[i][j] = new green_red(s.charAt(j)-'0');
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
			}
		}catch(FileNotFoundException e){
			// TODO Auto-generated catch block
			System.exit(0);
		}
		/////////////////////////////////
		//get light
		/////////////////////////////////
		ArrayList<request> option = new ArrayList<request>();
		Pattern dil=Pattern.compile("\\[CR,\\(\\+?0*\\d+,\\+?0*\\d+\\),\\(\\+?0*\\d+,\\+?0*\\d+\\)\\]");
		Pattern dil_carcode=Pattern.compile("\\d+");
		Pattern dil_path = Pattern.compile("\\[[0|1],\\(\\+?0*\\d+,\\+?0*\\d+\\),\\(\\+?0*\\d+,\\+?0*\\d+\\)\\]");
		Pattern dil_flow = Pattern.compile("\\[flow,\\(\\+?0*\\d+,\\+?0*\\d+\\),\\(\\+?0*\\d+,\\+?0*\\d+\\)\\]");
		Scanner sc = new Scanner(System.in);
		FileOutputStream out = new FileOutputStream("result.txt");
		TaxiGUI gui = new TaxiGUI();
		mapInfo mi = new mapInfo();
		mi.readmap("map.txt");//set_map_path
		gui.LoadMap(mi.map, 80);
		///////
		flow[][] pluse = new flow[80][80];
		for(int p=0;p<80;p++){
			for(int q=0;q<80;q++){
				pluse[p][q] = new flow();
			}
		}
		//////
		system main_system = new system(map_array,option,out,gui,pluse,light);
		control_light cont = new control_light(light,gui);
		cont.start();
		main_system.start();
		for(;;){
			String m = sc.nextLine();
			if(m.equals("end")){
				System.exit(0);
			}
			Matcher dil_temp = dil.matcher(m);
			Matcher dil_carcode_temp = dil_carcode.matcher(m);
			Matcher dil_path_temp = dil_path.matcher(m);
			Matcher dil_flow_temp = dil_flow.matcher(m);
			boolean a = dil_temp.matches();
			boolean b = dil_carcode_temp.matches();
			boolean c = dil_path_temp.matches();
			boolean d = dil_flow_temp.matches();
			if(!a){
				if(m.equals("status0")){
					for(int car=0;car<100;car++){
						if(system.kernel[car].state==0){
							System.out.print("car:"+system.kernel[car].code+" ");
						}
					}
					System.out.print("\n");
				}else if(m.equals("status1")){
					for(int car=0;car<100;car++){
						if(system.kernel[car].state==1){
							System.out.print("car:"+system.kernel[car].code+" ");
						}
					}
					System.out.print("\n");
				}else if(m.equals("status2")){
					for(int car=0;car<100;car++){
						if(system.kernel[car].state==2){
							System.out.print("car:"+system.kernel[car].code+" ");
						}
					}
					System.out.print("\n");
				}else if(m.equals("status3")){
					for(int car=0;car<100;car++){
						/////////////////////////////////
						///////////////////////////
						if(system.kernel[car].state==3){
							System.out.print("car:"+system.kernel[car].code+" ");
						}
					}
					System.out.print("\n");
				}else if(b){
					int car;
					try{
					    car = Integer.parseInt(m);
					}catch(NumberFormatException e){
						System.out.println("INVALID INPUT:"+m);
						continue;
					}
					if(car<1||car>100){
						System.out.println("INVALID INPUT:"+m);
						continue;
					}else{
						Date _time = new Date();
						System.out.println("Time:"+_time.getTime()+"("+system.kernel[car-1].car_i+","+system.kernel[car-1].car_j+")state:"+system.kernel[car-1].state);
					    if(system.kernel[car-1] instanceof specialcar){
					    	System.out.println("special_car!");
					    	((specialcar)system.kernel[car-1]).dd();
					    }
					}
				}else if(c){
					String[] fetch = m.split("[\\[(,#)]");
					int init_i = Integer.parseInt(fetch[3]);
					int init_j = Integer.parseInt(fetch[4]);
					int end_i = Integer.parseInt(fetch[7]);
					int end_j = Integer.parseInt(fetch[8]);
					int op_cl = Integer.parseInt(fetch[1]);
					if(init_i<0||init_i>79||init_j<0||init_j>79||end_i<0||end_i>79||end_j<0||end_j>79||(op_cl!=0&&op_cl!=1)){
						System.out.println("INVALID INPUT:"+m);
						continue;
					}
					if(path_bridge(init_i,init_j,end_i,end_j)&&op_cl==0){
						if(init_i<end_i){
							map_array[init_i][init_j] = map_array[init_i][init_j]-2;
							re_compute.path_compute();
						}else if(init_i>end_i){
							map_array[end_i][end_j] = map_array[end_i][end_j]-2;
							re_compute.path_compute();
						}else{
							if(init_j<end_j){
								map_array[init_i][init_j] = map_array[init_i][init_j]-1;
								re_compute.path_compute();
							}else{
								map_array[end_i][end_j] = map_array[end_i][end_j]-1;
								re_compute.path_compute();
							}
						}
						gui.SetRoadStatus(new Point(init_i,init_j), new Point(end_i,end_j),0);
					}else if(op_cl==1){
						if(end_i==init_i&&end_j-init_j==-1&&(map_array[end_i][end_j]==0||map_array[end_i][end_j]==2)){
							map_array[end_i][end_j] = map_array[end_i][end_j]+1;
							gui.SetRoadStatus(new Point(init_i,init_j), new Point(end_i,end_j),1);
							re_compute.path_compute();
						}else if(end_i==init_i&&end_j-init_j==1&&(map_array[init_i][init_j]==0||map_array[init_i][init_j]==2)){
							map_array[init_i][init_j] = map_array[init_i][init_j]+1;
							gui.SetRoadStatus(new Point(init_i,init_j), new Point(end_i,end_j),1);
							re_compute.path_compute();
						}else if(init_j==end_j&&end_i-init_i==-1&&(map_array[end_i][end_j]==0||map_array[end_i][end_j]==1)){
							map_array[end_i][end_j] = map_array[end_i][end_j]+2;
							gui.SetRoadStatus(new Point(init_i,init_j), new Point(end_i,end_j),1);
							re_compute.path_compute();
						}else if(init_j==end_j&&end_i-init_i==1&&(map_array[init_i][init_j]==0||map_array[init_i][init_j]==1)){
							map_array[init_i][init_j] = map_array[init_i][init_j]+2;
							gui.SetRoadStatus(new Point(init_i,init_j), new Point(end_i,end_j),1);
							re_compute.path_compute();
						}else{
							System.out.println("INVALID"+m);
						}
					}else{
						System.out.println("INVALID:"+m);
					}
				}else if(d){
					String[] fetch = m.split("[\\[(,#)]");
					int init_i = Integer.parseInt(fetch[3]);
					int init_j = Integer.parseInt(fetch[4]);
					int end_i = Integer.parseInt(fetch[7]);
					int end_j = Integer.parseInt(fetch[8]);
					if(bridge(init_i, init_j, end_i, end_j)){
					System.out.println("Flow:"+guigv.GetFlow(init_i, init_j, end_i, end_j));
					}else{
						System.out.println("INVALID INPUT(Not Connected):"+m);
					}
				}
				else{
					System.out.println("INVALID INPUT:"+m);
				}
				continue;
			}else{
				String[] fetch = m.split("[(,#)]");
				int init_i = Integer.parseInt(fetch[2]);
				int init_j = Integer.parseInt(fetch[3]);
				int end_i = Integer.parseInt(fetch[6]);
				int end_j = Integer.parseInt(fetch[7]);
				if(init_i>79||init_j>79||end_i>79||end_j>79){
					System.out.println("INVALID INPUT:"+m);
					continue;
				}
				if(init_i==end_i&&init_j==end_j){
					System.out.println("INVALID INPUT:"+m);
					continue;
				}
				//gui.RequestTaxi(new Point(init_i,init_j),new Point(end_i,end_j));
				request rq = new request(m);
				option.add(rq);
				try {
					out.write((rq+"\r\n4*4:\r\n").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				for(int k=0;k<100;k++){
					if(first_pd(rq,system.kernel[k])){
						try {
							out.write(("CAR:"+system.kernel[k].code+"("+system.kernel[k].car_i+","+system.kernel[k].car_j+")"+"status:"+system.kernel[k].state+"\r\n").getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.exit(0);
						}
					}
				}
			}
		} 	
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
	    if(inittime<0){
	    	return false;
	    }
	    if(map_array==null){
	    	return false;
	    }
	    if(light==null){
	    	return false;
	    }
	    return true;
	}
	
	static boolean bridge(int init1,int init2,int end1,int end2)
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
				if(map_array[init1][init2]==2||map_array[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}
		}else if(init1-end1==1){
			if(init2!=end2){
				return false;
			}else{
				if(map_array[end1][end2]==2||map_array[end1][end2]==3){
					return true;
				}else{
					return false;
				}
			}
			
		}else if(init1==end1){
			if(init2-end2==-1){
				if(map_array[init1][init2]==1||map_array[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}else if(init2-end2==1){
				if(map_array[end1][end2]==1||map_array[end1][end2]==3){
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
	
	static boolean first_pd(request rq,car take)
	/*@REQUIRES:take instanceof car
	 *rq instanceof request
     *@MODIFIES:None
     *@EFFECTS:(take in 4*4field centered by rq)==>(\result==true)
     *(take out of 4*4field centered by rq)==>(\result==false)
     */
	{
		if((take.car_i-rq.init_i<=2)&&(take.car_i-rq.init_i>=-2)){
			if((take.car_j-rq.init_j<=2)&&(take.car_j-rq.init_j>=-2)){
					return true;
			}
		}
		return false;
	}
	static boolean path_bridge(int init1,int init2,int end1,int end2)
	/*@REQUIRES:\exist(map_array)
     *@MODIFIES:None
     *@EFFECTS:((init1,init2)linked to(end1,end2)in map_array)==>(\result==true)
     *((init1,init2)not linked to(end1,end2)||(init1,init2)==(end1,end2))==>(\result==false)
     */
	{
		if(init1-end1==-1){
			if(init2!=end2){
				return false;
			}else{
				if(map_array[init1][init2]==2||map_array[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}
		}else if(init1-end1==1){
			if(init2!=end2){
				return false;
			}else{
				if(map_array[end1][end2]==2||map_array[end1][end2]==3){
					return true;
				}else{
					return false;
				}
			}
			
		}else if(init1==end1){
			if(init2-end2==-1){
				if(map_array[init1][init2]==1||map_array[init1][init2]==3){
					return true;
				}else{
					return false;
				}
			}else if(init2-end2==1){
				if(map_array[end1][end2]==1||map_array[end1][end2]==3){
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
}

/*PRESENT_OBJECT:(int[][] c_map)
 *AF:AF(c)=(map)where(map==c.c_map)
 *INVARIANCE:(c.c_map!=NULL) 
 *SPECIAL:This class is copied from GUI_PACKAGE.
 */

/*OVERVIEW:This class is copied from GUI_PACKAGE to set up map.
 */
class mapInfo{
	int[][] map=new int[80][80];
	public void readmap(String path)
	/*@REQUIRES:String path,System.in
     *@MODIFIES:System.out,map[][]
     *@EFFECTS:(path is right&&file exist&&file is right)==>(read file to map)
     *else(exit(1))
     */
	{
		Scanner scan=null;
		File file=new File(path);
		if(file.exists()==false){
			System.out.println("锟斤拷图锟侥硷拷锟斤拷锟斤拷锟斤拷,锟斤拷锟斤拷锟剿筹拷");
			System.exit(1);
			return;
		}
		try {
			scan = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			
		}
		for(int i=0;i<80;i++){
			String[] strArray = null;
			try{
				strArray=scan.nextLine().split("");
			}catch(Exception e){
				System.out.println("锟斤拷图锟侥硷拷锟斤拷息锟斤拷锟襟，筹拷锟斤拷锟剿筹拷");
				System.exit(1);
			}
			for(int j=0;j<80;j++){
				try{
					this.map[i][j]=Integer.parseInt(strArray[j]);
				}catch(Exception e){
					System.out.println("锟斤拷图锟侥硷拷锟斤拷息锟斤拷锟襟，筹拷锟斤拷锟剿筹拷");
					System.exit(1);
				}
			}
		}
		scan.close();
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(map==null){
			return false;
		}
		return true;
	}
}

