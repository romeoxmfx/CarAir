package com.android.carair.threadpool2;

import java.util.Comparator;

import com.android.carair.utils.Priority;


/**
 * 优先级比较器
 *
 */
public class PriorityComparator implements Comparator<Priority>{

	@Override
	public int compare(Priority object1, Priority object2) {
		// TODO Auto-generated method stub
		int priority1 = object1.getPriority();
		int priority2 = object2.getPriority();
		if(priority1 > priority2)
			return 1;
		else if(priority1 < priority2)
			return -1;
		else
			return 0;
	}

}
