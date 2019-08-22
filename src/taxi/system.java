package taxi;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/*SPECIAL:The static properties don't belong to the object.
 *PRESENT_OBJECT:(int[][] c_map_array,flow[][] c_pluse,green_red[][] c_light,
 *ArrayList<request> c_option,FileOutputStream c_out,TaxiGUI c_gui)
 *AF:AF(c)=(map_array,pluse,light,option,out,gui)where(map_array==c.c_map_array,
 *pluse==c.c_pluse,light==c.c_light,option==c.c_option,out==c.c_out,gui==c.c_gui)
 *INVARIANCE:(c.c_map_array!=NULL)&&(c.c_pluse!=NULL)&&(c.c_light!=NULL)&&
 *(c.c_out!=NULL)&&(c.c_gui!=NULL)&&(c.c_option!=NULL)
 */

/*OVERVIEW:This class is responsible for starting the car thread and distributing
 * the requests. 
 */
public class system extends Thread{
	static car[] kernel = new car[100];
	int[][] map_array = new int[80][80];
	flow[][] pluse = new flow[80][80];
	green_red[][] light = new green_red[80][80];
	ArrayList<request> option = new ArrayList<request>();
	static double time;
	FileOutputStream out;
	TaxiGUI gui;
	
	system(int[][] map_array,ArrayList<request> option,FileOutputStream out,TaxiGUI gui,flow[][] pluse,green_red[][] light)
	/*@REQUIRES:None
	 *@MODIFIES:map_array,pluse,option,time,out,gui
	 *@EFFECTS:(\result this.map_array = map_array)
	 *(\result this.option=option)
	 *(\result time=0)
	 *(\result this.gui=gui)
	 *(\result this.pluse=pluse)
	 *(\result this.light=light)
	 */
	{
		this.map_array = map_array;
		this.option = option;
		time = 0;
		this.out = out;
		this.gui = gui;
		this.pluse = pluse;
		this.light = light;
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(map_array==null){
			return false;
		}
		if(pluse==null){
			return false;
		}
		if(light==null){
			return false;
		}
		if(out==null){
			return false;
		}
		if(gui==null){
			return false;
		}
		if(option==null){
			return false;
		}
		return true;
	}
	
	void init_taxi()
	/*REQUIRES:The file "set_taxi.txt" exists
	 *MODIFIES:kernel,gui
	 *EFFECTS:create new object for kernel and start the thread. 
	 */
	{
		int i;
		Random random = new Random();
		String s=null;
		////////////////////////////////////////////////////////////////////
		try {
			BufferedReader in = new BufferedReader(new FileReader("set_taxi.txt"));
			try {
				s = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.exit(0);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.exit(0);
		}
		////////////////////////////////////////////////////////////////////////
		try{
			for(i=0;i<100;i++){
				if(s.charAt(i)-'0'==0){
					kernel[i] = new car(i+1,random.nextInt(80),random.nextInt(80),0,0,map_array,gui,pluse,light);
					gui.SetTaxiStatus(i,new Point(kernel[i].car_i,kernel[i].car_j),kernel[i].state);////////////////////////
				    gui.SetTaxiType(i,0);
				}else{
					kernel[i] = new specialcar(i+1,random.nextInt(80),random.nextInt(80),0,0,map_array,gui,pluse,light);
					gui.SetTaxiStatus(i,new Point(kernel[i].car_i,kernel[i].car_j),kernel[i].state);////////////////////////
					gui.SetTaxiType(i,1);
				}
				kernel[i].start();
			}
	    }catch(Exception e){
	    	System.exit(0);
	    }
	}
	synchronized public void run()
	/*@REQUIRES:None
	 *@MODIFIES:kernel,pluse,option
	 *@EFFECTS:thread run;control and change kernel;add pluse
	 */
	{
		init_taxi();
		int i;
		for(;;){
			int tocar;
			if(option.size()==0){
				try {
					wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
				continue;
			}else{
				for(i=0;i<option.size();i++){
					if(option.get(i).sum==29){
						tocar = whichcar(option.get(i));
						if(tocar==101){
							//System.out.println("NO CAR TO TAKE");
							try {
								out.write((option.get(i)+"NO CAR TAKE\r\n").getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
						}else{
							addrq(option.get(i),kernel[tocar]);
							try {
								out.write((option.get(i)+"DONE BY CAR"+kernel[tocar].code+"\r\n").getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.exit(0);
							}
							for(int n=0;n<100;n++){
								if(option.get(i).carry[n]==1){
									try {
										out.write(("3s_window_send_apply"+"CAR"+kernel[n].code+"-("+kernel[n].car_i+","+kernel[n].car_j+")"+"credit:"+kernel[n].credit+"\r\n").getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										System.exit(0);
									}
								}
							}
						}
						option.remove(i);
						i--;
						continue;
					}
					option.get(i).sum = option.get(i).sum+1;
					for(int j=0;j<100;j++){
						if(pd(option.get(i),kernel[j])){
							if(option.get(i).carry[j]==0){
								option.get(i).carry[j] = 1;
								kernel[j].credit = kernel[j].credit+1;
							}
						}
					}
				}
				try {
					wait(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
			}
		}
		
	}
	
	void init(int[] carry)
	/*@REQUIRES:None
	 *@MODIFIES:carry
	 *@EFFECTS:(\all int i;0<=i&&i<100;carry[i]=0)
	 */
	{
		int i;
		for(i=0;i<100;i++){
			carry[i] = 0;
		}
	}
	
	boolean pd(request rq,car take)
	/*@REQUIRES:(rq instanceof request)&&(take instanceof car)
	 *@MODIFIES:None
	 *@EFFECTS:((take.car_i,take.car_j) in the 4*4 field centered by (rq.init_i,rq.init_j))==>(\result return true)
	 *ELSE==>(\result return false)
	 */
	{
		if((take.car_i-rq.init_i<=2)&&(take.car_i-rq.init_i>=-2)){
			if((take.car_j-rq.init_j<=2)&&(take.car_j-rq.init_j>=-2)){
				if(take.state==2){
					return true;
				}
			}
		}
		return false;
	}
	int whichcar(request rq)
	/*@REQUIRES:(3s window is closing)
	 *@MODIFIES:None
	 *@EFFECTS:(\all car take;take response to rq&&take.state==2&&(take has the highest credit)&&
	 *(in the cars which have the highest credit take has the shortest path))==>(\result return take.code)
	 *ELSE==>(\result return 101)
	 */
	{
		/*rq.carry*/
		int i,j,k;
		ArrayList<fit> func = new ArrayList<fit>();
		for(i=0;i<100;i++){
			if(rq.carry[i]==1&&kernel[i].state==2){
				_node[] quene = new _node[6400];
				for(k=0;k<6400;k++){
					quene[k] = new _node();
				}
				func.add(new fit(i,kernel[i].credit,kernel[i].bfs(kernel[i].car_i, kernel[i].car_j, rq.init_i, rq.init_j, quene)));
			}
		}
		if(func.size()==0){
			return 101;
		}else{
			int best;
			for(best=0,i=0;i<func.size();i++){
				if(func.get(i).credit>func.get(best).credit){
					best = i;
					continue;
				}
				if(func.get(i).credit==func.get(best).credit&&func.get(i).path<func.get(best).path){
					best = i;
					continue;
				}
			}
			return func.get(best).biao;
		}
		
	}
	
	
	
	void addrq(request rq,car thiscar)
	/*@REQUIRES:(rq instanceof request)&&(thiscar instanceof car)
	 *@MODIFIES:thiscar.rq,thiscar.state,thiscar.sum
	 *@EFFECTS:(\result thiscar.rq=rq)
	 *(\result thiscar.state=3)
	 *(\result thiscar.sum=0)
	 *(\result thiscar.init())
	 *SPECIAL:(in the method init(), it'll change the car's state.But that doesn't
	 *belong to this method)
	 */
	{   
		synchronized(thiscar){
			thiscar.rq = rq;
			thiscar.state = 3;///
			thiscar.sum = 0;
			thiscar.init();
		}
	}
}


