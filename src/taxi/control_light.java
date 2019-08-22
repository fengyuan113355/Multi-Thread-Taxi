package taxi;

import java.awt.Point;
import java.util.Random;

/*PRESENT_OBJECT:(green[][] c_light,TaxiGui c_gui)
 *AF:AF(c)=(light,gui)where(light==c.c_light,gui==c.c_gui) 
 *INVARIANCE:(c_light!=NULL&&c_gui!=NULL) 
 */

/*OVERVIEW:This is the thread required by the guidebook to control
 *the light. 
 */

public class control_light extends Thread{
    green_red[][] light;
    TaxiGUI gui;
    
    control_light(green_red[][] light,TaxiGUI gui)
    /*@REQUIRES:None
     *@MODIFIES:this.light,this.gui
     *@EFFECTS:(\result this.light=light)
     *(\result this.gui=gui) 
     */
    {
    	this.light = light;
    	this.gui = gui;
    }
    
    public boolean repOK()
    /*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
    {
    	if(light==null){
    		return false;
    	}
    	if(gui==null){
    		return false;
    	}
    	return true;
    }
    
    synchronized public void run()
    /*@REQUIRES:get_the_object_lock
     *@MODIFIES:this.light,this.gui 
     *@EFFECTS:After_waiting_gap
     *If(light[i][j].valid==1&&light[i][j].color_row==0),Then(\result light[i][j].color_row=1&&light[i][j].notifyAll())
     *If(light[i][j].valid==1&&light[i][j].color_row==1),Then(\result light[i][j].color_row=0&&light[i][j].notifyAll())
     *set_light_new_status_in_gui 
     */
    {
    	Random ran = new Random();
    	int gap = 200 + ran.nextInt(301);
    	for(;;){
    		try {
				wait(gap);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.exit(0);
			}
    		for(int i=0;i<80;i++){
    			for(int j=0;j<80;j++){
    				if(light[i][j].valid==0){
    					gui.SetLightStatus(new Point(i,j),0);
    					continue;
    				}
    				if(light[i][j].color_row==0){
    					light[i][j].color_row=1;
    					gui.SetLightStatus(new Point(i,j),1);
    				}else{
    					light[i][j].color_row=0;
    					gui.SetLightStatus(new Point(i,j),2);
    				}
    				//////////////////////////////
    				synchronized(light[i][j]){
    					light[i][j].notifyAll();
    				}
    				//////////////////////////////
    			}
    		}
    	}
    }
}
