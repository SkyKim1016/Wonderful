package com.onethefull.recipe.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.onethefull.recipe.comm.vo.PageInfoVO;
import com.onethefull.recipe.req.CognizeReq;
import com.onethefull.recipe.req.ConditionCognizeReq;
import com.onethefull.recipe.vo.ConditionCognizeVO;

@Repository("cognizeMapper")
public interface CognizeMapper {

	public PageInfoVO getCondtionCognizeRequestListPageInfo(ConditionCognizeReq conditionCognizeReq);
	public List<ConditionCognizeVO> getCondtionCognizeRequestList(ConditionCognizeReq conditionCognizeReq);
	public ConditionCognizeVO getConditionCognize(CognizeReq cognizeReq);
	public int insertCondition(ConditionCognizeReq conditionCognizeReq);
	public int updateConditionCognizeForReceipt(ConditionCognizeReq conditionCognizeReq);
	public int updateConditionCognizeForContent(ConditionCognizeReq conditionCognizeReq);
}
