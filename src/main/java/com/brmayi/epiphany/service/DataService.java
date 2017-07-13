package com.brmayi.epiphany.service;

import java.util.List;

import com.brmayi.epiphany.exception.EpiphanyException;
/**
 * 
 * 	通用文件处理类：这是业务代码的核心类
 *            
 *            .==.       .==.
 *           //'^\\     //^'\\
 *          // ^^\(\__/)/^ ^^\\
 *         //^ ^^ ^/6  6\ ^^^ \\
 *        //^ ^^ ^/( .. )\^ ^^ \\
 *       // ^^  ^/\|v""v|/\^^ ^ \\
 *      // ^^/\/  / '~~' \ \/\^ ^\\
 *      ----------------------------------------
 *      HERE BE DRAGONS WHICH CAN CREATE MIRACLE
 *       
 *      @author 静儿(987489055@qq.com)
 *
 */
public interface DataService {
	/**
	 * 根据ID进行业务数据处理
	 * @param dealIds 处理ID
	 * @param path 要保存到的磁盘路径，不需要保存磁盘，可以为null
	 * @throws EpiphanyException 抛出通用异常
	 */
	public void dealDataByIds(List<Long> dealIds, String path) throws EpiphanyException;
	
	/**
	 * 根据时间区间获取id列表
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return id列表
	 * @throws EpiphanyException 抛出通用异常
	 */
	public List<Long> getIds(String beginTime, String endTime) throws EpiphanyException;
	
	/**
	 * 根据开始结束ID处理数据
	 * @param beginId 开始ID 
	 * @param endId 结束ID
	 * @param path 要保存到的磁盘路径，不需要保存磁盘，可以为null
	 * @throws EpiphanyException 抛出通用异常
	 */
	public void dealDataByBeginEnd(long beginId, long endId, String path) throws EpiphanyException;
	
	/**
	 * 取得最大ID
	 * @return 最大ID
	 * @throws EpiphanyException 抛出通用异常
	 */
	public long getMaxId() throws EpiphanyException;
	
	
	/**
	 * 取得最小ID
	 * @return 最小ID
	 * @throws EpiphanyException 抛出通用异常
	 */
	public long getMinId() throws EpiphanyException;
}
