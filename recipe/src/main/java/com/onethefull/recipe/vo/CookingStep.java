package com.onethefull.recipe.vo;

import java.util.List;

public class CookingStep {
	String stepSeq;
	String stepName;
	String actionImgURL;
	List<AiRecipeIngriVO> stepIngr;
	String stepDetal;
	
	public CookingStep(){} 
	public String getStepSeq() {
		return stepSeq;
	}
	public String getActionImgURL() {
		return actionImgURL;
	}
	public void setActionImgURL(String actionImgURL) {
		this.actionImgURL = actionImgURL;
	}
	public void setStepSeq(String stepSeq) {
		this.stepSeq = stepSeq;
	}
	public String getStepName() {
		return stepName;
	}
	public void setStepName(String stepName) {
		this.stepName = stepName;
	}
	public List<AiRecipeIngriVO> getStepIngr() {
		return stepIngr;
	}
	public void setStepIngr(List<AiRecipeIngriVO> stepIngr) {
		this.stepIngr = stepIngr;
	}
	public String getStepDetal() {
		return stepDetal;
	}
	public void setStepDetal(String stepDetal) {
		this.stepDetal = stepDetal;
	}
	public CookingStep(String stepSeq, String stepName, String actionImgURL, List<AiRecipeIngriVO> stepIngr, String stepDetal) {
		super();
		this.stepSeq = stepSeq;
		this.stepName = stepName;
		this.actionImgURL = actionImgURL;
		this.stepIngr = stepIngr;
		this.stepDetal = stepDetal;
	}
}
