package com.brmayi.epiphany.strategy;

import java.util.ArrayList;
import java.util.List;

import com.brmayi.epiphany.business.FullBusinessTask;

public enum ThreadDistributionStrategy {
	DEGREE {
		@Override
        public List<FullBusinessTask> getStrategy(int threadCount, String fullPath, Object[] idsObject) {
			List<Long> largerIds = (List<Long>) idsObject[0];
			List<Long> smallerIds = (List<Long>) idsObject[0];
			List<FullBusinessTask> taskMap = new ArrayList<FullBusinessTask>(threadCount);
	    	int threadNo = 1;
	    	int idsSize = smallerIds.size();
	    	int dealSum = idsSize/(threadCount/2);
	    	int leftCount = idsSize%(threadCount/2);
	    	int i=0;
	    	while(i<idsSize) {
	    		FullBusinessTask fullTask = taskMap.get(threadNo);
	        	int end = leftCount>0?(i+dealSum+1):(threadNo==(threadCount/2)?idsSize:(i+dealSum));
	        	--leftCount;
	        	fullTask.setIds(smallerIds.subList(i, end));
	        	fullTask.setFullPath(fullPath);
	        	fullTask.setThreadNo(threadNo);
	    		i = end;
	    		threadNo++;
	    	}
	        idsSize = largerIds.size();
	    	dealSum = idsSize/(threadCount-threadCount/2);
	    	leftCount = idsSize%(threadCount-threadCount/2);
	    	i=0;
	        while(i<idsSize) {
	        	FullBusinessTask fullTask = taskMap.get(threadNo);
	        	int end = leftCount>0?(i+dealSum+1):(threadNo==threadCount?idsSize:(i+dealSum));
	        	--leftCount;
	        	fullTask.setIds(largerIds.subList(i, end));
	        	fullTask.setFullPath(fullPath);
	        	fullTask.setThreadNo(threadNo);
	    		i = end;
	    		threadNo++;
	    	}
			return taskMap;
        }
	},
	AVERAGE {
        public List<FullBusinessTask> getStrategy(int threadCount, String fullPath, Object[] idsObject) {
        	Long beginId = (Long) idsObject[0], maxId=(Long) idsObject[1];
        	List<FullBusinessTask> taskMap = new ArrayList<FullBusinessTask>(threadCount);
	    	long interval = (maxId-beginId)/threadCount;
	    	int threadNo = 1;
	        while(threadNo<=threadCount) {
	        	//应对新增情况，实际作用应该不是很大，但是至少加1是必须的，因为最后一条不包含在检索条件里
	        	if(threadNo==threadCount) {
	        		interval += 100;
	        	}
	        	long endId = beginId + interval;
	        	FullBusinessTask fullTask = taskMap.get(threadNo);
	        	List<Long> ids = new ArrayList<Long>((int)interval);
	        	while(beginId<endId) {
	        		ids.add(beginId++);
	        	}
	        	fullTask.setIds(ids);
	        	fullTask.setFullPath(fullPath);
	        	fullTask.setThreadNo(threadNo);
	    		threadNo++;
	    		
	    	}
			return taskMap;
        }
	},
	EVEN {
        public List<FullBusinessTask> getStrategy(int threadCount, String fullPath, Object[] idsObject) {
        	List<Long> ids = (List<Long>) idsObject[0];
        	List<FullBusinessTask> taskMap = new ArrayList<FullBusinessTask>(threadCount);
	    	int threadNo = 1;
	    	int idsSize = ids.size();
	    	int i=0;
	    	int dealSum = idsSize/(threadCount);
	    	int leftCount = idsSize%(threadCount);
	    	while(i<idsSize) {
	    		FullBusinessTask fullTask = taskMap.get(threadNo);
	    		int end = leftCount>0?(i+dealSum+1):(threadNo==threadCount?idsSize:(i+dealSum));
	        	fullTask.setIds(ids.subList(i, end));
	        	fullTask.setFullPath(fullPath);
	        	fullTask.setThreadNo(threadNo);
	    		i = end;
	    		threadNo++;
	    	}
			return taskMap;
        }
	};
	
	public abstract List<FullBusinessTask> getStrategy(int threadCount, String fullPath, Object[] objects);
}
