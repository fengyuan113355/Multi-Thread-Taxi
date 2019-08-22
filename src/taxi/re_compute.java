package taxi;

import java.util.ArrayList;

/*As for this class, I don't set any property because I only need to use the
 * static method. So this class doesn't have any property sightful to the user
 * so there is no need to write PRESENT_OBJECT, AF and INVARIANCE.
 */

/*OVERVIEW:This class creates the method to recompute the route after opening a path
 * or closing a path.
 */

public class re_compute {
	static void path_compute()
	/*@REQUIRES:system.kernel
	 *@MODIFIES:system.kernel[i].sum
	 *system.kernel[i].path1
	 *system.kernel[i].path2
	 *system.kernel[i].path_to_sum
	 *@EFFECTS:(car in state(0|1|3) change their conditions)
	 *(special:state0 has two circumstances)
	 */
	{
		for(int i=0;i<100;i++){
			if(system.kernel[i] instanceof specialcar){
				continue;
			}
			synchronized(system.kernel[i]){
			if(system.kernel[i].state==0){
				if(system.kernel[i].path_to_sum==0){
					continue;
				}else{
					system.kernel[i].path2 = new ArrayList<_node>();
					_node[] quene = new _node[6400];
					for(int j=0;j<6400;j++){
						quene[j] = new _node();
					}
					system.kernel[i].path_to_sum = system.kernel[i].bfs(system.kernel[i].rq.init_i,system.kernel[i].rq.init_j,system.kernel[i].rq.end_i,system.kernel[i].rq.end_j,quene);
					int k;
		    		for(k=6399;k>=0;k--){
		    			if(quene[k].i==system.kernel[i].rq.end_i&&quene[k].j==system.kernel[i].rq.end_j){
		    				break;
		    			}
		    		}
		    		system.kernel[i].record_path(quene,k,system.kernel[i].path2);
		    		system.kernel[i].path2.remove(system.kernel[i].path2.size()-1);
				}
			}else if(system.kernel[i].state==1){
				system.kernel[i].sum = 0;
				system.kernel[i].path2 = new ArrayList<_node>();
				_node[] quene = new _node[6400];
				for(int j=0;j<6400;j++){
					quene[j] = new _node();
				}
				system.kernel[i].path_to_sum = system.kernel[i].bfs(system.kernel[i].car_i,system.kernel[i].car_j,system.kernel[i].rq.end_i,system.kernel[i].rq.end_j,quene);
				int k;
				for(k=6399;k>=0;k--){
					if(quene[k].i==system.kernel[i].rq.end_i&&quene[k].j==system.kernel[i].rq.end_j){
						break;
					}
				}
				system.kernel[i].record_path(quene,k,system.kernel[i].path2);
				system.kernel[i].path2.remove(system.kernel[i].path2.size()-1);
			}else if(system.kernel[i].state==2){
				continue;
			}else{
				system.kernel[i].sum = 0;
				system.kernel[i].path1 = new ArrayList<_node>();
				system.kernel[i].path2 = new ArrayList<_node>();
				system.kernel[i].init();
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
		return true;
	}
}
