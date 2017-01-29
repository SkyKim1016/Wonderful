package com.onethefull.recipe.res;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.NumberUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onethefull.recipe.type.RecommendType;


public class RcmdRecipeBase {
	private final static int INT_SCALE = 0;
	private final static BigDecimal BD_DIVISION_NUMBER = new BigDecimal("2");
	private String recipeId;
	private String recipeName;
	private String recipeImageUrl;	
	private String recipeImageOriginalUrl;	
	private String recipeImageThumbUrl;	
	private String recipeBaseOn;
	private String categoryName;
	private String cookingTime;
	private int cookDifficulty;
	private int healthLevel;
	private int isRcmdbyHeath;
	private int isPopular;
	private int isTaste;
	private char isAIRecipe;
	private String rateHealth = "0";
	private String rateTaste = "0";
	private String ratePopular = "0";
	
	public RcmdRecipeBase() {
		
	}

	public String getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(String recipeId) {
		this.recipeId = recipeId;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getRecipeImageUrl() {
		return recipeImageUrl;
	}

	public void setRecipeImageUrl(String recipeImageUrl) {
		this.recipeImageUrl = recipeImageUrl;
	}

	public String getRecipeImageOriginalUrl() {
		return recipeImageOriginalUrl;
	}

	public void setRecipeImageOriginalUrl(String recipeImageOriginalUrl) {
		this.recipeImageOriginalUrl = recipeImageOriginalUrl;
	}

	public String getRecipeImageThumbUrl() {
		return recipeImageThumbUrl;
	}

	public void setRecipeImageThumbUrl(String recipeImageThumbUrl) {
		this.recipeImageThumbUrl = recipeImageThumbUrl;
	}

	public String getRecipeBaseOn() {
		return recipeBaseOn;
	}

	public void setRecipeBaseOn(String recipeBaseOn) {
		this.recipeBaseOn = recipeBaseOn;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getCookingTime() {
		return cookingTime;
	}

	public void setCookingTime(String cookingTime) {
		this.cookingTime = cookingTime;
	}

	public int getCookDifficulty() {
		return this.cookDifficulty;
//		return CommonUtil.getCookDifficulty(String.valueOf(cookDifficulty)); 
	}

	public void setCookDifficulty(int cookDifficulty) {
		this.cookDifficulty = cookDifficulty;
	}

	@JsonIgnore
	public int getHealthLevel() {
		return healthLevel;
	}

	@JsonIgnore
	public void setHealthLevel(int healthLevel) {
		this.healthLevel = healthLevel;
	}

	@JsonIgnore
	public int getIsRcmdbyHeath() {
		return isRcmdbyHeath;
	}

	@JsonIgnore
	public void setIsRcmdbyHeath(int isRcmdbyHeath) {
		this.isRcmdbyHeath = isRcmdbyHeath;
	}

	@JsonIgnore
	public int getIsPopular() {
		return isPopular;
	}

	@JsonIgnore
	public void setIsPopular(int isPopular) {
		this.isPopular = isPopular;
	}

	@JsonIgnore
	public int getIsTaste() {
		return isTaste;
	}

	@JsonIgnore
	public void setIsTaste(int isTaste) {
		this.isTaste = isTaste;
	}

	@JsonIgnore
	public char getIsAIRecipe() {
		return isAIRecipe;
	}

	@JsonIgnore
	public void setIsAIRecipe(char isAIRecipe) {
		this.isAIRecipe = isAIRecipe;
	}
	
	public String getRateHealth() {
		return rateHealth;
	}

	public void setRateHealth(String rateHealth) {
		this.rateHealth = rateHealth;
	}

	public String getRateTaste() {
		return rateTaste;
	}

	public void setRateTaste(String rateTaste) {
		this.rateTaste = rateTaste;
	}

	public String getRatePopular() {
		return ratePopular;
	}

	public void setRatePopular(String ratePopular) {
		this.ratePopular = ratePopular;
	}

	public String getRateTotal() {
		BigDecimal rateTotal = BigDecimal.ZERO;
		rateTotal = rateTotal.add(NumberUtils.parseNumber(this.rateHealth, BigDecimal.class));
		rateTotal = rateTotal.add(NumberUtils.parseNumber(this.rateTaste, BigDecimal.class));
		return rateTotal.divide(BD_DIVISION_NUMBER, INT_SCALE, BigDecimal.ROUND_UP).toString();
	}
	
	public List<RecommendType> getRecommendType() {
		List<RecommendType> list = new ArrayList<RecommendType>();
		
		if(this.isAIRecipe == 'Y') list.add(RecommendType.CREATED);
		if(this.isRcmdbyHeath == 1 || this.healthLevel < 5) {
			list.add(RecommendType.HEALTH);
		}
		
		if(this.isPopular == 1 ) {
			list.add(RecommendType.POPULAR);			
		}
		
		if(this.isTaste == 1) {
			list.add(RecommendType.TASTE);			
		}
		return list;
	}

}