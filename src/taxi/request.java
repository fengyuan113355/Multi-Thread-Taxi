package taxi;

import java.util.Date;
import java.util.Random;

/*PRESENT_OBJECT:(double c_time,int c_init_i,int c_init_j,int c_end_i
 * int c_end_j,int c_sum,int[] c_carry)
 *AF:AF(c)=(time,init_i,init_j,end_i,end_j,sum,carry)where(time==c.c_time,
 *init_i==c.c_init_i,init_j==c.c_init_j,end_i==c.c_end_i,end_j==c.c_end_j,
 *sum==c.c_sum,carry==c.c_carry)
 *INVARIANCE:(c.c_time>=0)&&(0<=c.c_init_i<80)&&(0<=c.c_init_j<80)&&
 *(0<=c.c_end_i<80)&&(0<=c.c_end_j<80)&&(0<=c.c_sum<=30)&&(c.c_carry!=NULL) 
 */

/*OVERVIEW:This class saves the information of request and redefines the toString
 * method. 
 */
public class request {
	double time;
	int init_i;
	int init_j;
	int end_i;
	int end_j;
	int sum;
	int[] carry = new int[100]; 
	request(){}
	request(String m)
	/*@REQUIRES:dil.matcher(m).matches()==true
	 *@MODIFIES:init_i,init_j,end_i,end_j,time,carry
	 *@EFFECTS:(\all int k;k>=0&&k<100)==>(\result carry[k]=0)
	 * (dil.matcher(m).matches()==true)==>(\result init_i = Integer.parseInt(fetch[2]))
	 * (dil.matcher(m).matches()==true)==>(\result init_j = Integer.parseInt(fetch[3]))
	 * (dil.matcher(m).matches()==true)==>(\result end_i = Integer.parseInt(fetch[6]))
	 * (dil.matcher(m).matches()==true)==>(\result end_j = Integer.parseInt(fetch[7]))
	 * (\result time=system.time)
	 */
	{
		String[] fetch = m.split("[(,#)]");
		init_i = Integer.parseInt(fetch[2]);
		init_j = Integer.parseInt(fetch[3]);
		end_i = Integer.parseInt(fetch[6]);
		end_j = Integer.parseInt(fetch[7]);
		Date time = new Date();
		//this.time = system.time;
		//this.time = Math.round(this.time/100)*100;/*base_on_100ms*/
		this.time = time.getTime();
		for(int i=0;i<100;i++){
			carry[i] = 0;
		}
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
	    if(time<0){
	    	return false;
	    }
	    if(init_i<0||init_i>=80){
	    	return false;
	    }
	    if(init_j<0||init_j>=80){
	    	return false;
	    }
	    if(end_i<0||end_i>=80){
	    	return false;
	    }
	    if(end_j<0||end_j>=80){
	    	return false;
	    }
	    if(sum<0||sum>=31){
	    	return false;
	    }
	    if(carry==null){
	    	return false;
	    }
	    return true;
	}
	
	public String toString()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:(\result==String)(change request to String)
	 */
	{
		return "request:("+init_i+","+init_j+")"+"------"+"("+end_i+","+end_j+")"+"-time-"+time+"ms";
	}
}

/*PRESENT_OBJECT:(int c_i,int c_j,int c_valid,int c_path,int c_quan)
 *AF:AF(c)=(i,j,valid,path,quan)where(i==c.c_i,j==c.c_j,valid==c.c_valid
 *path==c.c_path,quan==c.c_quan)
 *INVARIANCE:(0<=c.c_i<80)&&(0<=c.c_j<80)&&(c.c_valid>=0)&&(c.c_path>=0)&&(c.c_quan>=0) 
 */

/*OVERVIEW:This class is responsible for calculating the route.
 */
class _node{
	int i;
	int j;
	int valid;
	int path;
	int quan;
	_node(){}
	_node(int a,int b)
	/*REQUIRES:(0<=a<80)&&(0<=b<80)
	 *MODIFIES:i,j
	 *EFFECTS:Constructor 
	 */
	{
		this.i = a;
		this.j = b;
	}
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(i<0||i>=80){
			return false;
		}
		if(j<0||j>=80){
			return false;
		}
		if(valid<0){
			return false;
		}
		if(path<0){
			return false;
		}
		if(quan<0){
			return false;
		}
		return true;
	}
	public String toString()
	/*REQUIRES:None
	 *MODIFIES:None 
	 *EFFECTS:(\result==String)(change _node to String) 
	 */
	{
		return "("+i+","+j+")";
	}
}

/*PRESENT_OBJECT:(int c_biao,int c_credit,int c_path)
 *AF:AF(c)=(biao,credit,path)where(biao==c.c_biao,credit==c.c_credit,path==c.c_path)
 *INVARIANCE:(c.c_biao>=0)&&(c.c_credit>=0)&&(c.c_path>=0) 
 */

/*OVERVIEW:This class is responsible for calculating the route.
 */
class fit{
	int biao;
	int credit;
	int path;
	fit(int biao,int credit,int path)
	/*@REQUIRES:None
	 *@MODIFIES:biao,credit,path
	 *@EFFECTS:(\result this.biao=biao)
	 *(\result this.credit=credit)
	 *(\result this.path=path)
	 */
	{
		this.biao = biao;
		this.credit = credit;
		this.path = path;
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(biao<0){
			return false;
		}
		if(credit<0){
			return false;
		}
		if(path<0){
			return false;
		}
		return true;
	}
}

/*PRESENT_OBJECT:(int c_you,int c_xia)
 *AF:AF(c)=(you,xia)where(you==c.c_you,xia==c.c_xia)
 *INVARIANCE:(c.c_you>=0)&&(c.c_xia>=0) 
 */

/*OVERVIEW:This class records the flow of special node.
 */
class flow{
	int you;
	int xia;
	flow()
	/*@REQUIRES:None
	 *@MODIFIES:you,xia
	 *@EFFECTS:(\result you=0)
	 *(\result xia=0)
	 */
	{
		this.you = 0;
		this.xia = 0;
	}
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(you<0){
			return false;
		}
		if(xia<0){
			return false;
		}
		return true;
	}
}

/*PRESENT_OBJECT:(_node c_node,int c_quan,int c_father)
 *AF:AF(c)=(node,quan,father)where(node==c.c_node,quan==c.c_quan,father==c.c_father) 
 *INVARIANCE:(c.c_node!=NULL)&&(c.c_quan>=0)&&(c.c_father>=0) 
 */

/*OVERVIEW:This class is responsible for calculating the route.
 */
class fun_node{
	_node node;
	int quan;
	int father;
	fun_node(_node node,int a,int b)
	/*@REQUIRES:None
	 *@MODIFIES:node,quan,father
	 *@EFFECTS:(\result this.node=node)
	 *(\result quan=a)
	 *(\result father=b)
	 */
	{
		this.node = node;
		this.quan = a;
		this.father = b;
	}
	
	public boolean repOK()
	/*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
	{
		if(node==null){
			return false;
		}
		if(quan<0){
			return false;
		}
		if(father<0){
			return false;
		}
		return true;
	}
}

/*PRESENT_OBJECT:(int c_valid,int c_color_row,int c_time)
 *AF:AF(c)=(valid,color_row,time)where(valid==c.c_valid,color_row==c.c_color_row,
 *time==c.c_time) 
 *INVARIANCE:(c.c_valid==0||c.c_valid==1)&&(c.c_color_row==0||c.c_color_row==1)&&
 *(c.c_time>=0) 
 */

/*OVERVIEW:This class records the information of green-red lights
 * aimed at special node.
 */
class green_red{
    int valid;
    int color_row;//red 0   green 1   in row
    int time;
    green_red(int a)
    /*@REQUIRES:None
     *@MODIFIES:valid,color_row 
     *@EFFECTS:(\result this.valid=a)
     *(\result color_row=ran.nextInt(2)) 
     */
    {
    	Random ran = new Random();
    	this.valid = a;
    	this.color_row = ran.nextInt(2);
    }
    
    public boolean repOK()
    /*@REQUIRES:None
	 *@MODIFIES:None
	 *@EFFECTS:If the object fits with the INVARIANCE, then(\result return true)
	 *Else(\result return false) 
	 */
    {
    	if(valid!=0&&valid!=1){
    		return false;
    	}
    	if(color_row!=0&&color_row!=1){
    		return false;
    	}
    	if(time<0){
    		return false;
    	}
    	return true;
    }
}







