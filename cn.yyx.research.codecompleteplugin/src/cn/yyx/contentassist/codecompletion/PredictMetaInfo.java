package cn.yyx.contentassist.codecompletion;

public class PredictMetaInfo {
	
	public static final double NotExistProbability = 0;
	
	public static final double SequenceSimilarThreshold = 0.5;
	public static final double OneSentenceSimilarThreshold = 0.7;
	
	public static final double TwoStringSimilarThreshold = 0.6;
	
	public static final int PredictMaxSequence = 15;
	public static final int PrePredictWindow = 8;
	public static final int PreTryMaxStep = 8;
	public static final int PreTryNeedSize = 4;
	
	public static final int OneExtendMaxSequence = 2;
	public static final int OneLevelExtendMaxSequence = 1; // must be the power of 2.
	
	public static final int MaxExtendLength = 15;
	
	public static final int NgramMaxSize = 5;
	
	public static final double MethodSimilarityThreshold = 0.7;
	
	public static final int MaxTypeConcateSize = 2;
	public static final int MaxTypeSpecificationSize = 2;
	
	public static final int OneCodeSynthesisTaskValidFinalSize = 2;
	
}