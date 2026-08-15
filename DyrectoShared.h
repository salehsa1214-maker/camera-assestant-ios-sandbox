#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class DyrectoSharedAlertSeverity, DyrectoSharedAlertPattern, DyrectoSharedAlertStore, DyrectoSharedAlert, DyrectoSharedKotlinEnumCompanion, DyrectoSharedKotlinEnum<E>, DyrectoSharedBleConnectionState, DyrectoSharedKotlinArray<T>, DyrectoSharedDiscoveredDevice, DyrectoSharedBondState, DyrectoSharedBleStatus, DyrectoSharedCameraBrand, DyrectoSharedPtpOperation, DyrectoSharedCameraDeviceInfo, DyrectoSharedPtpOpcodes, DyrectoSharedCameraInfo, DyrectoSharedCameraProperty, DyrectoSharedCameraFeature, DyrectoSharedCameraCapabilitiesCompanion, DyrectoSharedCameraCapabilities, DyrectoSharedCameraCommand, DyrectoSharedCameraControlValidator, DyrectoSharedControlRejection, DyrectoSharedCameraEventBatteryChanged, DyrectoSharedCameraEventConnectionLost, DyrectoSharedCameraEventFocusChanged, DyrectoSharedCameraEventLensChanged, DyrectoSharedCameraEventMediaInserted, DyrectoSharedCameraEventMediaRemoved, DyrectoSharedCameraEventPropertyChanged, DyrectoSharedCameraEventRecordingStarted, DyrectoSharedCameraEventRecordingStopped, DyrectoSharedCameraEventStorageChanged, DyrectoSharedCameraEventThermalWarning, DyrectoSharedKotlinByteArray, DyrectoSharedCameraEventUnknown, DyrectoSharedCameraEventDiffer, DyrectoSharedCameraTransport, DyrectoSharedCameraInfoCompanion, DyrectoSharedCameraPropertyCompanion, DyrectoSharedCapabilityMapper, DyrectoSharedRawPropertyRecord, DyrectoSharedCapabilityReportCompanion, DyrectoSharedCapabilityReport, DyrectoSharedCapabilityReporter, DyrectoSharedCapabilitySchema, DyrectoSharedControlResultFailed, DyrectoSharedControlResultNotEnabled, DyrectoSharedControlResultRejected, DyrectoSharedControlResultSuccess, DyrectoSharedFeatureResolver, DyrectoSharedPropertyCatalog, DyrectoSharedPropertyValueSetCompanion, DyrectoSharedPropertyValueSetEnumCompanion, DyrectoSharedPropertyValueSetEnum, DyrectoSharedPropertyValueSetNone, DyrectoSharedPropertyValueSetRangeCompanion, DyrectoSharedPropertyValueSetRange, DyrectoSharedRawFormEnum, DyrectoSharedRawFormNone, DyrectoSharedRawFormRange, DyrectoSharedSonyPropValueCodec, DyrectoSharedSshInfoTlv, DyrectoSharedSshInfoTlvResult, DyrectoSharedSshInfoTlvSshState, DyrectoSharedSshInfoTlvSshStateCompanion, DyrectoSharedCameraBrandCompanion, DyrectoSharedConnectionPhase, DyrectoSharedSshStatus, DyrectoSharedPtpStatus, DyrectoSharedCameraTelemetry, DyrectoSharedTimelineEvent, DyrectoSharedCameraConnectionState, DyrectoSharedTelemetryProp, DyrectoSharedCameraTelemetryCompanion, DyrectoSharedTelemetryDecoder, DyrectoSharedAlertType, DyrectoSharedAlertCategory, DyrectoSharedAlertConfigCompanion, DyrectoSharedAlertConfig, DyrectoSharedAlertIdGenerator, DyrectoSharedAlertEngineAlertInputs, DyrectoSharedAlertPatternCompanion, DyrectoSharedExposureAlertRules, DyrectoSharedExposureAlertRulesResult, DyrectoSharedExposureAlertState, DyrectoSharedSceneContext, DyrectoSharedSceneAlertRules, DyrectoSharedSceneAlertRulesResult, DyrectoSharedSceneAlertState, NSData, DyrectoSharedKotlinIntArray, DyrectoSharedRgbaFramePixelsCompanion, DyrectoSharedRgbaFramePixels, DyrectoSharedSwiftEnums, DyrectoSharedVoiceMode, DyrectoSharedVoiceSpeechRate, DyrectoSharedAdaptiveSamplerCompanion, DyrectoSharedAnalysisColorSpace, DyrectoSharedAnalysisRegion, DyrectoSharedAnalysisRegionRegistry, DyrectoSharedExposureAnalyzer, DyrectoSharedExposureResult, DyrectoSharedHistogramResult, DyrectoSharedZebraResult, DyrectoSharedExposureConfig, DyrectoSharedFalseColorResult, DyrectoSharedLumaField, DyrectoSharedFalseColorScale, DyrectoSharedFalseColorBand, DyrectoSharedFocusPeakingResult, DyrectoSharedHistogramEngineCompanion, DyrectoSharedIreScale, DyrectoSharedLuminanceAnalyzer, DyrectoSharedColorAccumulator, DyrectoSharedKotlinFloatArray, DyrectoSharedLut3DCompanion, DyrectoSharedLut3D, DyrectoSharedOverlayConfig, DyrectoSharedRec709Transform, DyrectoSharedRegionLumaStats, DyrectoSharedSubjectExposureStats, DyrectoSharedSubjectExposureStatsCompanion, DyrectoSharedWaveformResult, DyrectoSharedZebraEngineCompanion, DyrectoSharedZebraSpecCompanion, DyrectoSharedZebraSpecLevel, DyrectoSharedZebraSpecRange, DyrectoSharedAssistantAction, DyrectoSharedAssistantInstruction, DyrectoSharedInstructionFormatter, DyrectoSharedPerceptualSeverity, DyrectoSharedInstructionProgress, DyrectoSharedReferenceSignalResult, DyrectoSharedInstructionSelector, DyrectoSharedReferenceSignal, DyrectoSharedComparisonStrategy, DyrectoSharedReferenceMatchResult, DyrectoSharedInstructionTemplates, DyrectoSharedInstructionTranslator, DyrectoSharedPerceptualObservation, DyrectoSharedPerceptualContext, DyrectoSharedCompositionEvaluator, DyrectoSharedCreativePriorityMapper, DyrectoSharedCreativeSceneModel, DyrectoSharedCurveSpecCurveType, DyrectoSharedKotlinPair<__covariant A, __covariant B>, DyrectoSharedCurveSpec, DyrectoSharedExposureEvaluator, DyrectoSharedHeadroomEvaluator, DyrectoSharedHistogramSignature, DyrectoSharedReferenceProfile, DyrectoSharedCurrentReferenceInput, DyrectoSharedPerceptualTuning, DyrectoSharedHysteresisState, DyrectoSharedPerceivedMatchBucket, DyrectoSharedPerceptualApplicability, DyrectoSharedPerceptualConfidence, DyrectoSharedPerceptualThresholdsProfile, DyrectoSharedPerceptualDifference, DyrectoSharedPerceptualMessages, DyrectoSharedReferenceDriftDirection, DyrectoSharedPerceptualObservationCompanion, DyrectoSharedPerceptualTrend, DyrectoSharedPerceptualSignal, DyrectoSharedPerceptualThresholds, DyrectoSharedReferenceTolerance, DyrectoSharedPerceptualThresholdsExposureWeights, DyrectoSharedPerceptualThresholdsSignalBand, DyrectoSharedPerceptualTuningCompanion, DyrectoSharedPositionEvaluator, DyrectoSharedSimilarityEvaluator, DyrectoSharedSizeEvaluator, DyrectoSharedWhiteBalanceEvaluator, DyrectoSharedFaceDetectionResult, DyrectoSharedEyeDetectionResult, DyrectoSharedColorStatsResult, DyrectoSharedSceneSnapshot, DyrectoSharedReferenceEmbedding, DyrectoSharedNormalizedRectCompanion, DyrectoSharedNormalizedRect, DyrectoSharedSceneMode, DyrectoSharedPrimarySubjectType, DyrectoSharedSubjectCategory, DyrectoSharedReferenceAiSubject, DyrectoSharedReferenceAiProfileCompanion, DyrectoSharedReferenceAiProfile, DyrectoSharedReferenceAiSubjectCompanion, DyrectoSharedReferenceAlertRules, DyrectoSharedReferenceAlertRulesResult, DyrectoSharedReferenceAlertState, DyrectoSharedReferenceCameraSettingsCompanion, DyrectoSharedReferenceCameraSettings, DyrectoSharedReferenceColorProfileCompanion, DyrectoSharedReferenceColorProfile, DyrectoSharedReferenceConfig, DyrectoSharedReferenceConfigReferenceThresholds, DyrectoSharedReferenceEmbeddingCompanion, DyrectoSharedReferenceExposureProfileCompanion, DyrectoSharedReferenceExposureProfile, DyrectoSharedReferenceFaceProfileCompanion, DyrectoSharedReferenceFaceProfile, DyrectoSharedReferenceMonitorOptionsCompanion, DyrectoSharedReferenceMonitorOptions, DyrectoSharedReferenceSubjectProfile, DyrectoSharedShotCompletion, DyrectoSharedReferenceProfileCompanion, DyrectoSharedReferenceProfileCodec, DyrectoSharedReferenceSession, DyrectoSharedStoryboardCompletionRule, DyrectoSharedReferenceSessionCompanion, DyrectoSharedReferenceSessionState, DyrectoSharedStoryboardProgress, DyrectoSharedReferenceSignalApplicability, DyrectoSharedReferenceSignalState, DyrectoSharedReferenceSubjectProfileCompanion, DyrectoSharedSceneComparator, DyrectoSharedSettingDrift, DyrectoSharedSettingsDriftRules, DyrectoSharedShotCompletionCompanion, DyrectoSharedStoryboardAlertRules, DyrectoSharedStoryboardCompletion, DyrectoSharedStoryboardCompletionUpdate, DyrectoSharedStoryboardCompletionRuntime, DyrectoSharedStoryboardCompletionRuleCompanion, DyrectoSharedAiCadenceConfig, DyrectoSharedAiCapability, DyrectoSharedAiCapabilities, DyrectoSharedEngineStatus, DyrectoSharedComparisonStrategySelector, DyrectoSharedPrimarySubject, DyrectoSharedCompositionAnalyzer, DyrectoSharedSceneSubject, DyrectoSharedDetectedObject, DyrectoSharedEmbeddingMath, DyrectoSharedPrimarySubjectSelector, DyrectoSharedFeatureValue<T>, DyrectoSharedSceneModeClassifier, DyrectoSharedSegmentationSource, DyrectoSharedSegmentationResult, DyrectoSharedSubjectMatcher, DyrectoSharedSubjectMatcherMatch, DyrectoSharedSubjectMatcherExpected, DyrectoSharedSubjectMatcherMatchWeights, DyrectoSharedVisualEmbeddingResult, DyrectoSharedPlatformImage, DyrectoSharedCocoSemanticMapper, DyrectoSharedFeatureValueCompanion, DyrectoSharedNormalizedPoint, DyrectoSharedSegmentationSummary, DyrectoSharedSnapshotSource, DyrectoSharedSceneSnapshotCompanion, DyrectoSharedSceneSubjectCompanion, DyrectoSharedSceneClassification, DyrectoSharedDetectorTrigger, DyrectoSharedLumaFrame, DyrectoSharedTrackUpdate, DyrectoSharedTrackPolicy, DyrectoSharedTrackPolicyInput, DyrectoSharedTrackPolicyConfig, DyrectoSharedCameraAngle, DyrectoSharedColorTemperature, DyrectoSharedCreativeAspect, DyrectoSharedShotType, DyrectoSharedCreativeCameraCompanion, DyrectoSharedCreativeCamera, DyrectoSharedTintCast, DyrectoSharedCreativeColorCompanion, DyrectoSharedCreativeColor, DyrectoSharedSubjectPlacement, DyrectoSharedNegativeSpace, DyrectoSharedHeadroomLevel, DyrectoSharedSymmetry, DyrectoSharedCreativeCompositionCompanion, DyrectoSharedCreativeComposition, DyrectoSharedDepthOfField, DyrectoSharedCreativeDepthCompanion, DyrectoSharedCreativeDepth, DyrectoSharedLightingKey, DyrectoSharedLightingContrast, DyrectoSharedLightingDirection, DyrectoSharedCreativeLightingCompanion, DyrectoSharedCreativeLighting, DyrectoSharedCreativePriorityCompanion, DyrectoSharedCreativePriority, DyrectoSharedPriorityLevel, DyrectoSharedCreativeSceneAnalyzer, DyrectoSharedSemanticObservation, DyrectoSharedCreativeSubject, DyrectoSharedCreativeStyle, DyrectoSharedSceneRelationship, DyrectoSharedCreativeSignature, DyrectoSharedSceneSemantics, DyrectoSharedShotIdentity, DyrectoSharedCreativeSceneModelCompanion, DyrectoSharedSignatureElement, DyrectoSharedCreativeSignatureCompanion, DyrectoSharedMood, DyrectoSharedCreativeStyleCompanion, DyrectoSharedCreativeSubjectKind, DyrectoSharedCreativeSubjectCompanion, DyrectoSharedRelationshipKind, DyrectoSharedSceneRelationshipCompanion, DyrectoSharedSemanticConcept, DyrectoSharedSceneSemanticsCompanion, DyrectoSharedSemanticConceptCompanion, DyrectoSharedSignatureSource, DyrectoSharedSignatureElementCompanion, DyrectoSharedSignaturePresenter, DyrectoSharedSignaturePresenterInsight, DyrectoSharedCreativeContribution, DyrectoSharedCreativeAnalysisContext, DyrectoSharedColorExpert, DyrectoSharedCompositionExpert, DyrectoSharedShotTrait, DyrectoSharedPriorityHint, DyrectoSharedConceptScore, DyrectoSharedCreativeContributionCompanion, DyrectoSharedGeometryExpert, DyrectoSharedLightingExpert, DyrectoSharedSegmentationExpert, DyrectoSharedSemanticExpert, DyrectoSharedShotIdentityCompanion, DyrectoSharedShotTraitDimension, DyrectoSharedShotIdentityExtractor, DyrectoSharedShotIdentityThresholds, DyrectoSharedShotIdentityThresholdsCompanion, DyrectoSharedTraitDirection, DyrectoSharedShotTraitCompanion, DyrectoSharedConceptVocabularyCompanion, DyrectoSharedConceptVocabularyEntry, DyrectoSharedConceptVocabulary, DyrectoSharedNoSemanticSceneEngine, DyrectoSharedBatteryState, DyrectoSharedExposureState, DyrectoSharedFaceState, DyrectoSharedRecordingState, DyrectoSharedSceneAnalyzer, DyrectoSharedFrameContext, DyrectoSharedVisionContext, DyrectoSharedSceneConnectionState, DyrectoSharedStorageState, DyrectoSharedSceneState, DyrectoSharedJpegValidation, DyrectoSharedVericFrame, DyrectoSharedVericFrameRef, DyrectoSharedVericFrameExtractorCompanion, DyrectoSharedExposureChannelState, DyrectoSharedExposureVerdict, DyrectoSharedPixelRect, DyrectoSharedEyeBox, DyrectoSharedFaceBox, DyrectoSharedSceneSnapshotResult, DyrectoSharedSubjectExposureResult, DyrectoSharedVoiceCooldowns, DyrectoSharedVoiceDebugState, DyrectoSharedVoiceSource, DyrectoSharedVoicePriority, DyrectoSharedVoiceEvent, DyrectoSharedVoiceKeyAssistant, DyrectoSharedVoiceKeyReferenceMatched, DyrectoSharedVoiceKeyShotCompleted, DyrectoSharedVoiceKeyTelemetry, DyrectoSharedVoiceSettings, DyrectoSharedVoiceTemplates, DyrectoSharedChannelState, DyrectoSharedPtpStatusCompanion, DyrectoSharedSshEnabled, DyrectoSharedTimelineStage, DyrectoSharedTimelineStageCompanion, DyrectoSharedKotlinCancellationException, DyrectoSharedKotlinThrowable, DyrectoSharedJobSupport, DyrectoSharedCoroutineStart, DyrectoSharedCoroutineDispatcher, DyrectoSharedKotlinAbstractCoroutineContextElement, DyrectoSharedCoroutineDispatcherKey, DyrectoSharedKotlinException, DyrectoSharedKotlinRuntimeException, DyrectoSharedKotlinAbstractCoroutineContextKey<B, E>, DyrectoSharedCoroutineExceptionHandlerKey, DyrectoSharedCoroutineNameKey, DyrectoSharedCoroutineName, DyrectoSharedDispatchers, DyrectoSharedMainCoroutineDispatcher, DyrectoSharedGlobalScope, DyrectoSharedJobKey, DyrectoSharedNonCancellable, DyrectoSharedNonDisposableHandle, DyrectoSharedKotlinIllegalStateException, DyrectoSharedTimeoutCancellationException, DyrectoSharedBufferOverflow, DyrectoSharedChannelFactory, DyrectoSharedKotlinNoSuchElementException, DyrectoSharedSharingCommand, DyrectoSharedSharingStartedCompanion, DyrectoSharedChannelFlow<T>, DyrectoSharedAtomicOp<__contravariant T>, DyrectoSharedOpDescriptor, DyrectoSharedLockFreeLinkedListNode, DyrectoSharedAtomicfuSynchronizedObjectLockState, DyrectoSharedKotlinAtomicReference<T>, DyrectoSharedAtomicfuSynchronizedObject, DyrectoSharedThreadSafeHeap<T>, DyrectoSharedKotlinUnit, DyrectoSharedKotlinIntIterator, DyrectoSharedKotlinLongIterator, DyrectoSharedKotlinLongArray, DyrectoSharedKotlinIntProgressionCompanion, DyrectoSharedKotlinIntProgression, DyrectoSharedKotlinIntRangeCompanion, DyrectoSharedKotlinIntRange, DyrectoSharedKotlinLongProgressionCompanion, DyrectoSharedKotlinLongProgression, DyrectoSharedKotlinLongRangeCompanion, DyrectoSharedKotlinLongRange, DyrectoSharedKotlinNothing, DyrectoSharedCloseableCoroutineDispatcher, DyrectoSharedKotlinByteIterator, DyrectoSharedKotlinFloatIterator, DyrectoSharedAtomicfuSynchronizedObjectStatus, DyrectoSharedKotlinx_serialization_coreSerializersModule, DyrectoSharedKotlinx_serialization_coreSerialKind;

@protocol DyrectoSharedStateFlow, DyrectoSharedKotlinComparable, DyrectoSharedCameraControl, DyrectoSharedFlow, DyrectoSharedKotlinx_serialization_coreKSerializer, DyrectoSharedControlResult, DyrectoSharedCameraEvent, DyrectoSharedPropertyValueSet, DyrectoSharedRawForm, DyrectoSharedCameraAdapter, DyrectoSharedVisionResult, DyrectoSharedFramePixels, DyrectoSharedAnalysisColorTransform, DyrectoSharedZebraSpec, DyrectoSharedPerceptualEvaluator, DyrectoSharedObjectDetectorEngine, DyrectoSharedSemanticMapper, DyrectoSharedEmbeddingEngine, DyrectoSharedSegmentationEngine, DyrectoSharedTrackHandle, DyrectoSharedObjectTracker, DyrectoSharedSceneExpert, DyrectoSharedSemanticSceneEngine, DyrectoSharedVoiceKey, DyrectoSharedVoiceSpeechEngine, DyrectoSharedChildHandle, DyrectoSharedChildJob, DyrectoSharedDisposableHandle, DyrectoSharedJob, DyrectoSharedKotlinSequence, DyrectoSharedSelectClause0, DyrectoSharedKotlinCoroutineContextKey, DyrectoSharedKotlinCoroutineContextElement, DyrectoSharedKotlinCoroutineContext, DyrectoSharedParentJob, DyrectoSharedSelectClause1, DyrectoSharedKotlinContinuation, DyrectoSharedCoroutineScope, DyrectoSharedKotlinSuspendFunction1, DyrectoSharedKotlinContinuationInterceptor, DyrectoSharedRunnable, DyrectoSharedDeferred, DyrectoSharedCancellableContinuation, DyrectoSharedCopyableThrowable, DyrectoSharedReceiveChannel, DyrectoSharedSelectClause2, DyrectoSharedSendChannel, DyrectoSharedChannelIterator, DyrectoSharedBroadcastChannel, DyrectoSharedFlowCollector, DyrectoSharedSharedFlow, DyrectoSharedMutableSharedFlow, DyrectoSharedSharingStarted, DyrectoSharedFusibleFlow, DyrectoSharedProducerScope, DyrectoSharedMainDispatcherFactory, DyrectoSharedKotlinSuspendFunction0, DyrectoSharedSelectInstance, DyrectoSharedSelectClause, DyrectoSharedKotlinIterator, DyrectoSharedKotlinIterable, DyrectoSharedKotlinClosedRange, DyrectoSharedKotlinOpenEndRange, DyrectoSharedChannel, DyrectoSharedKotlinSuspendFunction2, DyrectoSharedCompletableDeferred, DyrectoSharedCoroutineExceptionHandler, DyrectoSharedKotlinSuspendFunction3, DyrectoSharedCompletableJob, DyrectoSharedKotlinSuspendFunction4, DyrectoSharedKotlinSuspendFunction5, DyrectoSharedMutex, DyrectoSharedSelectBuilder, DyrectoSharedSemaphore, DyrectoSharedMutableStateFlow, DyrectoSharedKotlinKClass, DyrectoSharedKotlinSuspendFunction6, DyrectoSharedKotlinx_serialization_coreEncoder, DyrectoSharedKotlinx_serialization_coreSerialDescriptor, DyrectoSharedKotlinx_serialization_coreSerializationStrategy, DyrectoSharedKotlinx_serialization_coreDecoder, DyrectoSharedKotlinx_serialization_coreDeserializationStrategy, DyrectoSharedKotlinFunction, DyrectoSharedKotlinKDeclarationContainer, DyrectoSharedKotlinKAnnotatedElement, DyrectoSharedKotlinKClassifier, DyrectoSharedKotlinx_serialization_coreCompositeEncoder, DyrectoSharedKotlinAnnotation, DyrectoSharedKotlinx_serialization_coreCompositeDecoder, DyrectoSharedKotlinx_serialization_coreSerializersModuleCollector;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((swift_name("KotlinBase")))
@interface DyrectoSharedBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface DyrectoSharedBase (DyrectoSharedBaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface DyrectoSharedMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface DyrectoSharedMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorDyrectoSharedKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface DyrectoSharedNumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end

__attribute__((swift_name("KotlinByte")))
@interface DyrectoSharedByte : DyrectoSharedNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface DyrectoSharedUByte : DyrectoSharedNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface DyrectoSharedShort : DyrectoSharedNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface DyrectoSharedUShort : DyrectoSharedNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface DyrectoSharedInt : DyrectoSharedNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface DyrectoSharedUInt : DyrectoSharedNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface DyrectoSharedLong : DyrectoSharedNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface DyrectoSharedULong : DyrectoSharedNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface DyrectoSharedFloat : DyrectoSharedNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface DyrectoSharedDouble : DyrectoSharedNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface DyrectoSharedBoolean : DyrectoSharedNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end

__attribute__((swift_name("AlertFeedback")))
@protocol DyrectoSharedAlertFeedback
@required
- (void)deliverSeverity:(DyrectoSharedAlertSeverity *)severity sound:(DyrectoSharedAlertPattern *)sound vibration:(DyrectoSharedAlertPattern *)vibration __attribute__((swift_name("deliver(severity:sound:vibration:)")));
@property (readonly) id<DyrectoSharedStateFlow> playing __attribute__((swift_name("playing")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertStore")))
@interface DyrectoSharedAlertStore : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)alertStore __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedAlertStore *shared __attribute__((swift_name("shared")));
- (void)clear __attribute__((swift_name("clear()")));
- (void)recordAlert:(DyrectoSharedAlert *)alert __attribute__((swift_name("record(alert:)")));
@property (readonly) id<DyrectoSharedStateFlow> alerts __attribute__((swift_name("alerts")));
@end

__attribute__((swift_name("KotlinComparable")))
@protocol DyrectoSharedKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end

__attribute__((swift_name("KotlinEnum")))
@interface DyrectoSharedKotlinEnum<E> : DyrectoSharedBase <DyrectoSharedKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedKotlinEnumCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BleConnectionState")))
@interface DyrectoSharedBleConnectionState : DyrectoSharedKotlinEnum<DyrectoSharedBleConnectionState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedBleConnectionState *disconnected __attribute__((swift_name("disconnected")));
@property (class, readonly) DyrectoSharedBleConnectionState *connecting __attribute__((swift_name("connecting")));
@property (class, readonly) DyrectoSharedBleConnectionState *connected __attribute__((swift_name("connected")));
@property (class, readonly) DyrectoSharedBleConnectionState *disconnecting __attribute__((swift_name("disconnecting")));
+ (DyrectoSharedKotlinArray<DyrectoSharedBleConnectionState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedBleConnectionState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BleStatus")))
@interface DyrectoSharedBleStatus : DyrectoSharedBase
- (instancetype)initWithScanning:(BOOL)scanning devices:(NSArray<DyrectoSharedDiscoveredDevice *> *)devices cameraName:(NSString * _Nullable)cameraName cameraAddress:(NSString * _Nullable)cameraAddress rssi:(DyrectoSharedInt * _Nullable)rssi connectionState:(DyrectoSharedBleConnectionState *)connectionState bondState:(DyrectoSharedBondState *)bondState mtu:(DyrectoSharedInt * _Nullable)mtu model:(NSString * _Nullable)model firmware:(NSString * _Nullable)firmware cameraSsid:(NSString * _Nullable)cameraSsid serviceFound:(DyrectoSharedBoolean * _Nullable)serviceFound error:(NSString * _Nullable)error wifiApSsid:(NSString * _Nullable)wifiApSsid wifiApPassword:(NSString * _Nullable)wifiApPassword wifiApBssid:(NSString * _Nullable)wifiApBssid __attribute__((swift_name("init(scanning:devices:cameraName:cameraAddress:rssi:connectionState:bondState:mtu:model:firmware:cameraSsid:serviceFound:error:wifiApSsid:wifiApPassword:wifiApBssid:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedBleStatus *)doCopyScanning:(BOOL)scanning devices:(NSArray<DyrectoSharedDiscoveredDevice *> *)devices cameraName:(NSString * _Nullable)cameraName cameraAddress:(NSString * _Nullable)cameraAddress rssi:(DyrectoSharedInt * _Nullable)rssi connectionState:(DyrectoSharedBleConnectionState *)connectionState bondState:(DyrectoSharedBondState *)bondState mtu:(DyrectoSharedInt * _Nullable)mtu model:(NSString * _Nullable)model firmware:(NSString * _Nullable)firmware cameraSsid:(NSString * _Nullable)cameraSsid serviceFound:(DyrectoSharedBoolean * _Nullable)serviceFound error:(NSString * _Nullable)error wifiApSsid:(NSString * _Nullable)wifiApSsid wifiApPassword:(NSString * _Nullable)wifiApPassword wifiApBssid:(NSString * _Nullable)wifiApBssid __attribute__((swift_name("doCopy(scanning:devices:cameraName:cameraAddress:rssi:connectionState:bondState:mtu:model:firmware:cameraSsid:serviceFound:error:wifiApSsid:wifiApPassword:wifiApBssid:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedBondState *bondState __attribute__((swift_name("bondState")));
@property (readonly) NSString * _Nullable cameraAddress __attribute__((swift_name("cameraAddress")));
@property (readonly) NSString * _Nullable cameraName __attribute__((swift_name("cameraName")));
@property (readonly) NSString * _Nullable cameraSsid __attribute__((swift_name("cameraSsid")));
@property (readonly) BOOL connected __attribute__((swift_name("connected")));
@property (readonly) DyrectoSharedBleConnectionState *connectionState __attribute__((swift_name("connectionState")));
@property (readonly) NSArray<DyrectoSharedDiscoveredDevice *> *devices __attribute__((swift_name("devices")));
@property (readonly) NSString * _Nullable error __attribute__((swift_name("error")));
@property (readonly) NSString * _Nullable firmware __attribute__((swift_name("firmware")));
@property (readonly) NSString * _Nullable model __attribute__((swift_name("model")));
@property (readonly) DyrectoSharedInt * _Nullable mtu __attribute__((swift_name("mtu")));
@property (readonly) DyrectoSharedInt * _Nullable rssi __attribute__((swift_name("rssi")));
@property (readonly) BOOL scanning __attribute__((swift_name("scanning")));
@property (readonly) DyrectoSharedBoolean * _Nullable serviceFound __attribute__((swift_name("serviceFound")));
@property (readonly) NSString * _Nullable wifiApBssid __attribute__((swift_name("wifiApBssid")));
@property (readonly) NSString * _Nullable wifiApPassword __attribute__((swift_name("wifiApPassword")));
@property (readonly) NSString * _Nullable wifiApSsid __attribute__((swift_name("wifiApSsid")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BondState")))
@interface DyrectoSharedBondState : DyrectoSharedKotlinEnum<DyrectoSharedBondState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedBondState *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedBondState *bonding __attribute__((swift_name("bonding")));
@property (class, readonly) DyrectoSharedBondState *bonded __attribute__((swift_name("bonded")));
+ (DyrectoSharedKotlinArray<DyrectoSharedBondState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedBondState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DiscoveredDevice")))
@interface DyrectoSharedDiscoveredDevice : DyrectoSharedBase
- (instancetype)initWithName:(NSString *)name address:(NSString *)address rssi:(int32_t)rssi matched:(BOOL)matched brand:(DyrectoSharedCameraBrand *)brand __attribute__((swift_name("init(name:address:rssi:matched:brand:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedDiscoveredDevice *)doCopyName:(NSString *)name address:(NSString *)address rssi:(int32_t)rssi matched:(BOOL)matched brand:(DyrectoSharedCameraBrand *)brand __attribute__((swift_name("doCopy(name:address:rssi:matched:brand:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *address __attribute__((swift_name("address")));
@property (readonly) DyrectoSharedCameraBrand *brand __attribute__((swift_name("brand")));
@property (readonly) BOOL matched __attribute__((swift_name("matched")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t rssi __attribute__((swift_name("rssi")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraDeviceInfo")))
@interface DyrectoSharedCameraDeviceInfo : DyrectoSharedBase
- (instancetype)initWithManufacturer:(NSString *)manufacturer model:(NSString *)model firmwareVersion:(NSString *)firmwareVersion serialNumber:(NSString *)serialNumber standardVersion:(int32_t)standardVersion vendorExtensionId:(int64_t)vendorExtensionId vendorExtensionVersion:(int32_t)vendorExtensionVersion vendorExtensionDescription:(NSString *)vendorExtensionDescription functionalMode:(int32_t)functionalMode operations:(NSArray<DyrectoSharedPtpOperation *> *)operations supportedEventCount:(int32_t)supportedEventCount supportedPropertyCount:(int32_t)supportedPropertyCount captureFormatCount:(int32_t)captureFormatCount imageFormatCount:(int32_t)imageFormatCount __attribute__((swift_name("init(manufacturer:model:firmwareVersion:serialNumber:standardVersion:vendorExtensionId:vendorExtensionVersion:vendorExtensionDescription:functionalMode:operations:supportedEventCount:supportedPropertyCount:captureFormatCount:imageFormatCount:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraDeviceInfo *)doCopyManufacturer:(NSString *)manufacturer model:(NSString *)model firmwareVersion:(NSString *)firmwareVersion serialNumber:(NSString *)serialNumber standardVersion:(int32_t)standardVersion vendorExtensionId:(int64_t)vendorExtensionId vendorExtensionVersion:(int32_t)vendorExtensionVersion vendorExtensionDescription:(NSString *)vendorExtensionDescription functionalMode:(int32_t)functionalMode operations:(NSArray<DyrectoSharedPtpOperation *> *)operations supportedEventCount:(int32_t)supportedEventCount supportedPropertyCount:(int32_t)supportedPropertyCount captureFormatCount:(int32_t)captureFormatCount imageFormatCount:(int32_t)imageFormatCount __attribute__((swift_name("doCopy(manufacturer:model:firmwareVersion:serialNumber:standardVersion:vendorExtensionId:vendorExtensionVersion:vendorExtensionDescription:functionalMode:operations:supportedEventCount:supportedPropertyCount:captureFormatCount:imageFormatCount:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t captureFormatCount __attribute__((swift_name("captureFormatCount")));
@property (readonly) NSString *firmwareVersion __attribute__((swift_name("firmwareVersion")));
@property (readonly) int32_t functionalMode __attribute__((swift_name("functionalMode")));
@property (readonly) int32_t imageFormatCount __attribute__((swift_name("imageFormatCount")));
@property (readonly) NSString *manufacturer __attribute__((swift_name("manufacturer")));
@property (readonly) NSString *model __attribute__((swift_name("model")));
@property (readonly) NSArray<DyrectoSharedPtpOperation *> *operations __attribute__((swift_name("operations")));
@property (readonly) NSString *serialNumber __attribute__((swift_name("serialNumber")));
@property (readonly) int32_t standardVersion __attribute__((swift_name("standardVersion")));
@property (readonly) NSString *standardVersionText __attribute__((swift_name("standardVersionText")));
@property (readonly) int32_t supportedEventCount __attribute__((swift_name("supportedEventCount")));
@property (readonly) int32_t supportedOperationCount __attribute__((swift_name("supportedOperationCount")));
@property (readonly) int32_t supportedPropertyCount __attribute__((swift_name("supportedPropertyCount")));
@property (readonly) NSString *vendorExtensionDescription __attribute__((swift_name("vendorExtensionDescription")));
@property (readonly) int64_t vendorExtensionId __attribute__((swift_name("vendorExtensionId")));
@property (readonly) NSString *vendorExtensionIdHex __attribute__((swift_name("vendorExtensionIdHex")));
@property (readonly) int32_t vendorExtensionVersion __attribute__((swift_name("vendorExtensionVersion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PtpOpcodes")))
@interface DyrectoSharedPtpOpcodes : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)ptpOpcodes __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPtpOpcodes *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPtpOperation *)describeCode:(int32_t)code __attribute__((swift_name("describe(code:)")));
- (NSString *)hexCode:(int32_t)code __attribute__((swift_name("hex(code:)")));
- (NSString *)nameCode:(int32_t)code __attribute__((swift_name("name(code:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PtpOperation")))
@interface DyrectoSharedPtpOperation : DyrectoSharedBase
- (instancetype)initWithCode:(int32_t)code name:(NSString *)name __attribute__((swift_name("init(code:name:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPtpOperation *)doCopyCode:(int32_t)code name:(NSString *)name __attribute__((swift_name("doCopy(code:name:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) NSString *hex __attribute__((swift_name("hex")));
@property (readonly) BOOL isVendor __attribute__((swift_name("isVendor")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) NSString *searchText __attribute__((swift_name("searchText")));
@end

__attribute__((swift_name("CameraAdapter")))
@protocol DyrectoSharedCameraAdapter
@required
@property (readonly) id<DyrectoSharedStateFlow> capabilities __attribute__((swift_name("capabilities")));
@property (readonly) id<DyrectoSharedCameraControl> control __attribute__((swift_name("control")));
@property (readonly) id<DyrectoSharedFlow> events __attribute__((swift_name("events")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraCapabilities")))
@interface DyrectoSharedCameraCapabilities : DyrectoSharedBase
- (instancetype)initWithSchemaVersion:(int32_t)schemaVersion info:(DyrectoSharedCameraInfo *)info properties:(NSArray<DyrectoSharedCameraProperty *> *)properties features:(NSSet<DyrectoSharedCameraFeature *> *)features __attribute__((swift_name("init(schemaVersion:info:properties:features:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCameraCapabilitiesCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCameraCapabilities *)doCopySchemaVersion:(int32_t)schemaVersion info:(DyrectoSharedCameraInfo *)info properties:(NSArray<DyrectoSharedCameraProperty *> *)properties features:(NSSet<DyrectoSharedCameraFeature *> *)features __attribute__((swift_name("doCopy(schemaVersion:info:properties:features:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (DyrectoSharedCameraProperty * _Nullable)propertyCode:(int32_t)code __attribute__((swift_name("property(code:)")));
- (BOOL)supportsFeature:(DyrectoSharedCameraFeature *)feature __attribute__((swift_name("supports(feature:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSSet<DyrectoSharedCameraFeature *> *features __attribute__((swift_name("features")));
@property (readonly) DyrectoSharedCameraInfo *info __attribute__((swift_name("info")));
@property (readonly) NSArray<DyrectoSharedCameraProperty *> *properties __attribute__((swift_name("properties")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@property (readonly) NSArray<DyrectoSharedInt *> *unknownPropertyCodes __attribute__((swift_name("unknownPropertyCodes")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraCapabilities.Companion")))
@interface DyrectoSharedCameraCapabilitiesCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraCapabilitiesCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@property (readonly) DyrectoSharedCameraCapabilities *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraCommand")))
@interface DyrectoSharedCameraCommand : DyrectoSharedKotlinEnum<DyrectoSharedCameraCommand *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCameraCommand *recordStart __attribute__((swift_name("recordStart")));
@property (class, readonly) DyrectoSharedCameraCommand *recordStop __attribute__((swift_name("recordStop")));
@property (class, readonly) DyrectoSharedCameraCommand *afPush __attribute__((swift_name("afPush")));
@property (class, readonly) DyrectoSharedCameraCommand *zoomIn __attribute__((swift_name("zoomIn")));
@property (class, readonly) DyrectoSharedCameraCommand *zoomOut __attribute__((swift_name("zoomOut")));
@property (class, readonly) DyrectoSharedCameraCommand *zoomStop __attribute__((swift_name("zoomStop")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCameraCommand *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCameraCommand *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("CameraControl")))
@protocol DyrectoSharedCameraControl
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeCommandCommand:(DyrectoSharedCameraCommand *)command completionHandler:(void (^)(id<DyrectoSharedControlResult> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("invokeCommand(command:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)setPropertyCode:(int32_t)code value:(int64_t)value completionHandler:(void (^)(id<DyrectoSharedControlResult> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("setProperty(code:value:completionHandler:)")));
@property (readonly) DyrectoSharedCameraCapabilities * _Nullable capabilities __attribute__((swift_name("capabilities")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraControlValidator")))
@interface DyrectoSharedCameraControlValidator : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)cameraControlValidator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraControlValidator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedControlRejection * _Nullable)validateCaps:(DyrectoSharedCameraCapabilities * _Nullable)caps code:(int32_t)code value:(int64_t)value __attribute__((swift_name("validate(caps:code:value:)")));
@end

__attribute__((swift_name("CameraEvent")))
@protocol DyrectoSharedCameraEvent
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventBatteryChanged")))
@interface DyrectoSharedCameraEventBatteryChanged : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithPercent:(DyrectoSharedInt * _Nullable)percent minutesRemaining:(DyrectoSharedInt * _Nullable)minutesRemaining __attribute__((swift_name("init(percent:minutesRemaining:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventBatteryChanged *)doCopyPercent:(DyrectoSharedInt * _Nullable)percent minutesRemaining:(DyrectoSharedInt * _Nullable)minutesRemaining __attribute__((swift_name("doCopy(percent:minutesRemaining:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedInt * _Nullable minutesRemaining __attribute__((swift_name("minutesRemaining")));
@property (readonly) DyrectoSharedInt * _Nullable percent __attribute__((swift_name("percent")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventConnectionLost")))
@interface DyrectoSharedCameraEventConnectionLost : DyrectoSharedBase <DyrectoSharedCameraEvent>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)connectionLost __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraEventConnectionLost *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventFocusChanged")))
@interface DyrectoSharedCameraEventFocusChanged : DyrectoSharedBase <DyrectoSharedCameraEvent>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)focusChanged __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraEventFocusChanged *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventLensChanged")))
@interface DyrectoSharedCameraEventLensChanged : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithInfo:(NSString * _Nullable)info __attribute__((swift_name("init(info:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventLensChanged *)doCopyInfo:(NSString * _Nullable)info __attribute__((swift_name("doCopy(info:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable info __attribute__((swift_name("info")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventMediaInserted")))
@interface DyrectoSharedCameraEventMediaInserted : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithSlot:(int32_t)slot __attribute__((swift_name("init(slot:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventMediaInserted *)doCopySlot:(int32_t)slot __attribute__((swift_name("doCopy(slot:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t slot __attribute__((swift_name("slot")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventMediaRemoved")))
@interface DyrectoSharedCameraEventMediaRemoved : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithSlot:(int32_t)slot __attribute__((swift_name("init(slot:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventMediaRemoved *)doCopySlot:(int32_t)slot __attribute__((swift_name("doCopy(slot:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t slot __attribute__((swift_name("slot")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventPropertyChanged")))
@interface DyrectoSharedCameraEventPropertyChanged : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithCode:(int32_t)code oldRaw:(DyrectoSharedLong * _Nullable)oldRaw newRaw:(DyrectoSharedLong * _Nullable)newRaw __attribute__((swift_name("init(code:oldRaw:newRaw:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventPropertyChanged *)doCopyCode:(int32_t)code oldRaw:(DyrectoSharedLong * _Nullable)oldRaw newRaw:(DyrectoSharedLong * _Nullable)newRaw __attribute__((swift_name("doCopy(code:oldRaw:newRaw:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly, getter=doNewRaw) DyrectoSharedLong * _Nullable newRaw __attribute__((swift_name("newRaw")));
@property (readonly) DyrectoSharedLong * _Nullable oldRaw __attribute__((swift_name("oldRaw")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventRecordingStarted")))
@interface DyrectoSharedCameraEventRecordingStarted : DyrectoSharedBase <DyrectoSharedCameraEvent>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)recordingStarted __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraEventRecordingStarted *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventRecordingStopped")))
@interface DyrectoSharedCameraEventRecordingStopped : DyrectoSharedBase <DyrectoSharedCameraEvent>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)recordingStopped __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraEventRecordingStopped *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventStorageChanged")))
@interface DyrectoSharedCameraEventStorageChanged : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithSlot:(int32_t)slot __attribute__((swift_name("init(slot:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventStorageChanged *)doCopySlot:(int32_t)slot __attribute__((swift_name("doCopy(slot:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t slot __attribute__((swift_name("slot")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventThermalWarning")))
@interface DyrectoSharedCameraEventThermalWarning : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithLevel:(DyrectoSharedInt * _Nullable)level __attribute__((swift_name("init(level:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventThermalWarning *)doCopyLevel:(DyrectoSharedInt * _Nullable)level __attribute__((swift_name("doCopy(level:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedInt * _Nullable level __attribute__((swift_name("level")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventUnknown")))
@interface DyrectoSharedCameraEventUnknown : DyrectoSharedBase <DyrectoSharedCameraEvent>
- (instancetype)initWithCode:(int32_t)code raw:(DyrectoSharedKotlinByteArray * _Nullable)raw __attribute__((swift_name("init(code:raw:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraEventUnknown *)doCopyCode:(int32_t)code raw:(DyrectoSharedKotlinByteArray * _Nullable)raw __attribute__((swift_name("doCopy(code:raw:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) DyrectoSharedKotlinByteArray * _Nullable raw __attribute__((swift_name("raw")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraEventDiffer")))
@interface DyrectoSharedCameraEventDiffer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)cameraEventDiffer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraEventDiffer *shared __attribute__((swift_name("shared")));
- (NSArray<id<DyrectoSharedCameraEvent>> *)diffOld:(DyrectoSharedCameraCapabilities * _Nullable)old new:(DyrectoSharedCameraCapabilities *)new_ __attribute__((swift_name("diff(old:new:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraFeature")))
@interface DyrectoSharedCameraFeature : DyrectoSharedKotlinEnum<DyrectoSharedCameraFeature *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCameraFeature *liveView __attribute__((swift_name("liveView")));
@property (class, readonly) DyrectoSharedCameraFeature *audio __attribute__((swift_name("audio")));
@property (class, readonly) DyrectoSharedCameraFeature *waveform __attribute__((swift_name("waveform")));
@property (class, readonly) DyrectoSharedCameraFeature *touchFocus __attribute__((swift_name("touchFocus")));
@property (class, readonly) DyrectoSharedCameraFeature *eyeAf __attribute__((swift_name("eyeAf")));
@property (class, readonly) DyrectoSharedCameraFeature *faceAf __attribute__((swift_name("faceAf")));
@property (class, readonly) DyrectoSharedCameraFeature *subjectRecognition __attribute__((swift_name("subjectRecognition")));
@property (class, readonly) DyrectoSharedCameraFeature *isoControl __attribute__((swift_name("isoControl")));
@property (class, readonly) DyrectoSharedCameraFeature *shutterControl __attribute__((swift_name("shutterControl")));
@property (class, readonly) DyrectoSharedCameraFeature *irisControl __attribute__((swift_name("irisControl")));
@property (class, readonly) DyrectoSharedCameraFeature *ndControl __attribute__((swift_name("ndControl")));
@property (class, readonly) DyrectoSharedCameraFeature *whiteBalanceControl __attribute__((swift_name("whiteBalanceControl")));
@property (class, readonly) DyrectoSharedCameraFeature *focusControl __attribute__((swift_name("focusControl")));
@property (class, readonly) DyrectoSharedCameraFeature *zoomControl __attribute__((swift_name("zoomControl")));
@property (class, readonly) DyrectoSharedCameraFeature *ptz __attribute__((swift_name("ptz")));
@property (class, readonly) DyrectoSharedCameraFeature *pictureProfile __attribute__((swift_name("pictureProfile")));
@property (class, readonly) DyrectoSharedCameraFeature *recordControl __attribute__((swift_name("recordControl")));
@property (class, readonly) DyrectoSharedCameraFeature *recFormat __attribute__((swift_name("recFormat")));
@property (class, readonly) DyrectoSharedCameraFeature *sandq __attribute__((swift_name("sandq")));
@property (class, readonly) DyrectoSharedCameraFeature *proxyRecording __attribute__((swift_name("proxyRecording")));
@property (class, readonly) DyrectoSharedCameraFeature *markers __attribute__((swift_name("markers")));
@property (class, readonly) DyrectoSharedCameraFeature *multiCamera __attribute__((swift_name("multiCamera")));
@property (class, readonly) DyrectoSharedCameraFeature *usb __attribute__((swift_name("usb")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCameraFeature *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCameraFeature *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *displayName __attribute__((swift_name("displayName")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraInfo")))
@interface DyrectoSharedCameraInfo : DyrectoSharedBase
- (instancetype)initWithManufacturer:(NSString * _Nullable)manufacturer model:(NSString * _Nullable)model firmwareVersion:(NSString * _Nullable)firmwareVersion protocolVersion:(NSString * _Nullable)protocolVersion serialNumber:(NSString * _Nullable)serialNumber lensInfo:(NSString * _Nullable)lensInfo transport:(DyrectoSharedCameraTransport *)transport __attribute__((swift_name("init(manufacturer:model:firmwareVersion:protocolVersion:serialNumber:lensInfo:transport:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCameraInfoCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCameraInfo *)doCopyManufacturer:(NSString * _Nullable)manufacturer model:(NSString * _Nullable)model firmwareVersion:(NSString * _Nullable)firmwareVersion protocolVersion:(NSString * _Nullable)protocolVersion serialNumber:(NSString * _Nullable)serialNumber lensInfo:(NSString * _Nullable)lensInfo transport:(DyrectoSharedCameraTransport *)transport __attribute__((swift_name("doCopy(manufacturer:model:firmwareVersion:protocolVersion:serialNumber:lensInfo:transport:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable firmwareVersion __attribute__((swift_name("firmwareVersion")));
@property (readonly) NSString * _Nullable lensInfo __attribute__((swift_name("lensInfo")));
@property (readonly) NSString * _Nullable manufacturer __attribute__((swift_name("manufacturer")));
@property (readonly) NSString * _Nullable model __attribute__((swift_name("model")));
@property (readonly) NSString * _Nullable protocolVersion __attribute__((swift_name("protocolVersion")));
@property (readonly) NSString * _Nullable serialNumber __attribute__((swift_name("serialNumber")));
@property (readonly) DyrectoSharedCameraTransport *transport __attribute__((swift_name("transport")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraInfo.Companion")))
@interface DyrectoSharedCameraInfoCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraInfoCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@property (readonly) DyrectoSharedCameraInfo *UNKNOWN __attribute__((swift_name("UNKNOWN")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraProperty")))
@interface DyrectoSharedCameraProperty : DyrectoSharedBase
- (instancetype)initWithCode:(int32_t)code dataType:(int32_t)dataType currentRaw:(DyrectoSharedLong * _Nullable)currentRaw currentText:(NSString * _Nullable)currentText writable:(BOOL)writable available:(BOOL)available valueSet:(id<DyrectoSharedPropertyValueSet>)valueSet label:(NSString *)label known:(BOOL)known getSetRaw:(int32_t)getSetRaw availabilityRaw:(int32_t)availabilityRaw __attribute__((swift_name("init(code:dataType:currentRaw:currentText:writable:available:valueSet:label:known:getSetRaw:availabilityRaw:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCameraPropertyCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCameraProperty *)doCopyCode:(int32_t)code dataType:(int32_t)dataType currentRaw:(DyrectoSharedLong * _Nullable)currentRaw currentText:(NSString * _Nullable)currentText writable:(BOOL)writable available:(BOOL)available valueSet:(id<DyrectoSharedPropertyValueSet>)valueSet label:(NSString *)label known:(BOOL)known getSetRaw:(int32_t)getSetRaw availabilityRaw:(int32_t)availabilityRaw __attribute__((swift_name("doCopy(code:dataType:currentRaw:currentText:writable:available:valueSet:label:known:getSetRaw:availabilityRaw:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t availabilityRaw __attribute__((swift_name("availabilityRaw")));
@property (readonly) BOOL available __attribute__((swift_name("available")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) DyrectoSharedLong * _Nullable currentRaw __attribute__((swift_name("currentRaw")));
@property (readonly) NSString * _Nullable currentText __attribute__((swift_name("currentText")));
@property (readonly) int32_t dataType __attribute__((swift_name("dataType")));
@property (readonly) int32_t getSetRaw __attribute__((swift_name("getSetRaw")));
@property (readonly) BOOL known __attribute__((swift_name("known")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) id<DyrectoSharedPropertyValueSet> valueSet __attribute__((swift_name("valueSet")));
@property (readonly) BOOL writable __attribute__((swift_name("writable")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraProperty.Companion")))
@interface DyrectoSharedCameraPropertyCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraPropertyCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraTransport")))
@interface DyrectoSharedCameraTransport : DyrectoSharedKotlinEnum<DyrectoSharedCameraTransport *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCameraTransport *wifi __attribute__((swift_name("wifi")));
@property (class, readonly) DyrectoSharedCameraTransport *usb __attribute__((swift_name("usb")));
@property (class, readonly) DyrectoSharedCameraTransport *ble __attribute__((swift_name("ble")));
@property (class, readonly) DyrectoSharedCameraTransport *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCameraTransport *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCameraTransport *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CapabilityMapper")))
@interface DyrectoSharedCapabilityMapper : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)capabilityMapper __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCapabilityMapper *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCameraCapabilities *)buildInfo:(DyrectoSharedCameraInfo *)info records:(NSArray<DyrectoSharedRawPropertyRecord *> *)records __attribute__((swift_name("build(info:records:)")));
- (DyrectoSharedCameraProperty *)toPropertyRecord:(DyrectoSharedRawPropertyRecord *)record __attribute__((swift_name("toProperty(record:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CapabilityReport")))
@interface DyrectoSharedCapabilityReport : DyrectoSharedBase
- (instancetype)initWithSchemaVersion:(int32_t)schemaVersion generatedAtMs:(int64_t)generatedAtMs info:(DyrectoSharedCameraInfo *)info properties:(NSArray<DyrectoSharedCameraProperty *> *)properties features:(NSArray<NSString *> *)features observedEvents:(NSArray<NSString *> *)observedEvents unknownPropertyCodes:(NSArray<DyrectoSharedInt *> *)unknownPropertyCodes unknownEvents:(NSArray<NSString *> *)unknownEvents __attribute__((swift_name("init(schemaVersion:generatedAtMs:info:properties:features:observedEvents:unknownPropertyCodes:unknownEvents:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCapabilityReportCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCapabilityReport *)doCopySchemaVersion:(int32_t)schemaVersion generatedAtMs:(int64_t)generatedAtMs info:(DyrectoSharedCameraInfo *)info properties:(NSArray<DyrectoSharedCameraProperty *> *)properties features:(NSArray<NSString *> *)features observedEvents:(NSArray<NSString *> *)observedEvents unknownPropertyCodes:(NSArray<DyrectoSharedInt *> *)unknownPropertyCodes unknownEvents:(NSArray<NSString *> *)unknownEvents __attribute__((swift_name("doCopy(schemaVersion:generatedAtMs:info:properties:features:observedEvents:unknownPropertyCodes:unknownEvents:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<NSString *> *features __attribute__((swift_name("features")));
@property (readonly) int64_t generatedAtMs __attribute__((swift_name("generatedAtMs")));
@property (readonly) DyrectoSharedCameraInfo *info __attribute__((swift_name("info")));
@property (readonly) NSArray<NSString *> *observedEvents __attribute__((swift_name("observedEvents")));
@property (readonly) NSArray<DyrectoSharedCameraProperty *> *properties __attribute__((swift_name("properties")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@property (readonly) NSArray<NSString *> *unknownEvents __attribute__((swift_name("unknownEvents")));
@property (readonly) NSArray<DyrectoSharedInt *> *unknownPropertyCodes __attribute__((swift_name("unknownPropertyCodes")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CapabilityReport.Companion")))
@interface DyrectoSharedCapabilityReportCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCapabilityReportCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CapabilityReporter")))
@interface DyrectoSharedCapabilityReporter : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)capabilityReporter __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCapabilityReporter *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCapabilityReport *)buildCaps:(DyrectoSharedCameraCapabilities *)caps generatedAtMs:(int64_t)generatedAtMs observedEvents:(NSArray<NSString *> *)observedEvents unknownEvents:(NSArray<NSString *> *)unknownEvents __attribute__((swift_name("build(caps:generatedAtMs:observedEvents:unknownEvents:)")));
- (NSString *)toJsonReport:(DyrectoSharedCapabilityReport *)report __attribute__((swift_name("toJson(report:)")));
- (NSString *)toMarkdownReport:(DyrectoSharedCapabilityReport *)report __attribute__((swift_name("toMarkdown(report:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CapabilitySchema")))
@interface DyrectoSharedCapabilitySchema : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)capabilitySchema __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCapabilitySchema *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t VERSION __attribute__((swift_name("VERSION")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ControlRejection")))
@interface DyrectoSharedControlRejection : DyrectoSharedKotlinEnum<DyrectoSharedControlRejection *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedControlRejection *unknownProperty __attribute__((swift_name("unknownProperty")));
@property (class, readonly) DyrectoSharedControlRejection *notWritable __attribute__((swift_name("notWritable")));
@property (class, readonly) DyrectoSharedControlRejection *notAvailable __attribute__((swift_name("notAvailable")));
@property (class, readonly) DyrectoSharedControlRejection *outOfRange __attribute__((swift_name("outOfRange")));
+ (DyrectoSharedKotlinArray<DyrectoSharedControlRejection *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedControlRejection *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("ControlResult")))
@protocol DyrectoSharedControlResult
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ControlResultFailed")))
@interface DyrectoSharedControlResultFailed : DyrectoSharedBase <DyrectoSharedControlResult>
- (instancetype)initWithReason:(NSString *)reason __attribute__((swift_name("init(reason:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedControlResultFailed *)doCopyReason:(NSString *)reason __attribute__((swift_name("doCopy(reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ControlResultNotEnabled")))
@interface DyrectoSharedControlResultNotEnabled : DyrectoSharedBase <DyrectoSharedControlResult>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)notEnabled __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedControlResultNotEnabled *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ControlResultRejected")))
@interface DyrectoSharedControlResultRejected : DyrectoSharedBase <DyrectoSharedControlResult>
- (instancetype)initWithRejection:(DyrectoSharedControlRejection *)rejection __attribute__((swift_name("init(rejection:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedControlResultRejected *)doCopyRejection:(DyrectoSharedControlRejection *)rejection __attribute__((swift_name("doCopy(rejection:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedControlRejection *rejection __attribute__((swift_name("rejection")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ControlResultSuccess")))
@interface DyrectoSharedControlResultSuccess : DyrectoSharedBase <DyrectoSharedControlResult>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)success __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedControlResultSuccess *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FeatureResolver")))
@interface DyrectoSharedFeatureResolver : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)featureResolver __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedFeatureResolver *shared __attribute__((swift_name("shared")));
- (NSSet<DyrectoSharedCameraFeature *> *)resolveProperties:(NSArray<DyrectoSharedCameraProperty *> *)properties transport:(DyrectoSharedCameraTransport *)transport __attribute__((swift_name("resolve(properties:transport:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyCatalog")))
@interface DyrectoSharedPropertyCatalog : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)propertyCatalog __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPropertyCatalog *shared __attribute__((swift_name("shared")));
- (BOOL)isKnownCode:(int32_t)code __attribute__((swift_name("isKnown(code:)")));
- (NSString * _Nullable)knownLabelCode:(int32_t)code __attribute__((swift_name("knownLabel(code:)")));
- (NSString *)labelCode:(int32_t)code __attribute__((swift_name("label(code:)")));
@property (readonly) NSSet<DyrectoSharedInt *> *knownCodes __attribute__((swift_name("knownCodes")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((swift_name("PropertyValueSet")))
@protocol DyrectoSharedPropertyValueSet
@required
- (BOOL)permitsValue:(int64_t)value __attribute__((swift_name("permits(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetCompanion")))
@interface DyrectoSharedPropertyValueSetCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPropertyValueSetCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializerTypeParamsSerializers:(DyrectoSharedKotlinArray<id<DyrectoSharedKotlinx_serialization_coreKSerializer>> *)typeParamsSerializers __attribute__((swift_name("serializer(typeParamsSerializers:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetEnum")))
@interface DyrectoSharedPropertyValueSetEnum : DyrectoSharedBase <DyrectoSharedPropertyValueSet>
- (instancetype)initWithValues:(NSArray<DyrectoSharedLong *> *)values __attribute__((swift_name("init(values:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedPropertyValueSetEnumCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(int64_t)value __attribute__((swift_name("contains(value:)")));
- (DyrectoSharedPropertyValueSetEnum *)doCopyValues:(NSArray<DyrectoSharedLong *> *)values __attribute__((swift_name("doCopy(values:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedLong *> *values __attribute__((swift_name("values")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetEnum.Companion")))
@interface DyrectoSharedPropertyValueSetEnumCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPropertyValueSetEnumCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetNone")))
@interface DyrectoSharedPropertyValueSetNone : DyrectoSharedBase <DyrectoSharedPropertyValueSet>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)none __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPropertyValueSetNone *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializerTypeParamsSerializers:(DyrectoSharedKotlinArray<id<DyrectoSharedKotlinx_serialization_coreKSerializer>> *)typeParamsSerializers __attribute__((swift_name("serializer(typeParamsSerializers:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetRange")))
@interface DyrectoSharedPropertyValueSetRange : DyrectoSharedBase <DyrectoSharedPropertyValueSet>
- (instancetype)initWithMin:(int64_t)min max:(int64_t)max step:(int64_t)step __attribute__((swift_name("init(min:max:step:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedPropertyValueSetRangeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(int64_t)value __attribute__((swift_name("contains(value:)")));
- (DyrectoSharedPropertyValueSetRange *)doCopyMin:(int64_t)min max:(int64_t)max step:(int64_t)step __attribute__((swift_name("doCopy(min:max:step:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t max __attribute__((swift_name("max")));
@property (readonly) int64_t min __attribute__((swift_name("min")));
@property (readonly) int64_t step __attribute__((swift_name("step")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PropertyValueSetRange.Companion")))
@interface DyrectoSharedPropertyValueSetRangeCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPropertyValueSetRangeCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((swift_name("RawForm")))
@protocol DyrectoSharedRawForm
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RawFormEnum")))
@interface DyrectoSharedRawFormEnum : DyrectoSharedBase <DyrectoSharedRawForm>
- (instancetype)initWithValues:(NSArray<DyrectoSharedLong *> *)values __attribute__((swift_name("init(values:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedRawFormEnum *)doCopyValues:(NSArray<DyrectoSharedLong *> *)values __attribute__((swift_name("doCopy(values:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedLong *> *values __attribute__((swift_name("values")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RawFormNone")))
@interface DyrectoSharedRawFormNone : DyrectoSharedBase <DyrectoSharedRawForm>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)none __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedRawFormNone *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RawFormRange")))
@interface DyrectoSharedRawFormRange : DyrectoSharedBase <DyrectoSharedRawForm>
- (instancetype)initWithMin:(int64_t)min max:(int64_t)max step:(int64_t)step __attribute__((swift_name("init(min:max:step:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedRawFormRange *)doCopyMin:(int64_t)min max:(int64_t)max step:(int64_t)step __attribute__((swift_name("doCopy(min:max:step:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t max __attribute__((swift_name("max")));
@property (readonly) int64_t min __attribute__((swift_name("min")));
@property (readonly) int64_t step __attribute__((swift_name("step")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RawPropertyRecord")))
@interface DyrectoSharedRawPropertyRecord : DyrectoSharedBase
- (instancetype)initWithCode:(int32_t)code dataType:(int32_t)dataType getSet:(int32_t)getSet availability:(int32_t)availability currentRaw:(DyrectoSharedLong * _Nullable)currentRaw currentText:(NSString * _Nullable)currentText form:(id<DyrectoSharedRawForm>)form __attribute__((swift_name("init(code:dataType:getSet:availability:currentRaw:currentText:form:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedRawPropertyRecord *)doCopyCode:(int32_t)code dataType:(int32_t)dataType getSet:(int32_t)getSet availability:(int32_t)availability currentRaw:(DyrectoSharedLong * _Nullable)currentRaw currentText:(NSString * _Nullable)currentText form:(id<DyrectoSharedRawForm>)form __attribute__((swift_name("doCopy(code:dataType:getSet:availability:currentRaw:currentText:form:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t availability __attribute__((swift_name("availability")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) DyrectoSharedLong * _Nullable currentRaw __attribute__((swift_name("currentRaw")));
@property (readonly) NSString * _Nullable currentText __attribute__((swift_name("currentText")));
@property (readonly) int32_t dataType __attribute__((swift_name("dataType")));
@property (readonly) id<DyrectoSharedRawForm> form __attribute__((swift_name("form")));
@property (readonly) int32_t getSet __attribute__((swift_name("getSet")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SonyPropValueCodec")))
@interface DyrectoSharedSonyPropValueCodec : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sonyPropValueCodec __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSonyPropValueCodec *shared __attribute__((swift_name("shared")));
- (DyrectoSharedKotlinByteArray *)encodeValue:(int64_t)value dataType:(int32_t)dataType __attribute__((swift_name("encode(value:dataType:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshInfoTlv")))
@interface DyrectoSharedSshInfoTlv : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sshInfoTlv __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSshInfoTlv *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSshInfoTlvResult *)decodeB:(DyrectoSharedKotlinByteArray *)b __attribute__((swift_name("decode(b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshInfoTlv.Result")))
@interface DyrectoSharedSshInfoTlvResult : DyrectoSharedBase
- (instancetype)initWithState:(DyrectoSharedSshInfoTlvSshState *)state sshId:(NSString *)sshId sshPass:(NSString *)sshPass fingerprint:(NSString *)fingerprint notes:(NSArray<NSString *> *)notes __attribute__((swift_name("init(state:sshId:sshPass:fingerprint:notes:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSshInfoTlvResult *)doCopyState:(DyrectoSharedSshInfoTlvSshState *)state sshId:(NSString *)sshId sshPass:(NSString *)sshPass fingerprint:(NSString *)fingerprint notes:(NSArray<NSString *> *)notes __attribute__((swift_name("doCopy(state:sshId:sshPass:fingerprint:notes:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *fingerprint __attribute__((swift_name("fingerprint")));
@property (readonly) NSArray<NSString *> *notes __attribute__((swift_name("notes")));
@property (readonly) NSString *sshId __attribute__((swift_name("sshId")));
@property (readonly) NSString *sshPass __attribute__((swift_name("sshPass")));
@property (readonly) DyrectoSharedSshInfoTlvSshState *state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshInfoTlv.SshState")))
@interface DyrectoSharedSshInfoTlvSshState : DyrectoSharedKotlinEnum<DyrectoSharedSshInfoTlvSshState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) DyrectoSharedSshInfoTlvSshStateCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) DyrectoSharedSshInfoTlvSshState *unknown __attribute__((swift_name("unknown")));
@property (class, readonly) DyrectoSharedSshInfoTlvSshState *off __attribute__((swift_name("off")));
@property (class, readonly) DyrectoSharedSshInfoTlvSshState *on __attribute__((swift_name("on")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSshInfoTlvSshState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSshInfoTlvSshState *> *entries __attribute__((swift_name("entries")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshInfoTlv.SshStateCompanion")))
@interface DyrectoSharedSshInfoTlvSshStateCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSshInfoTlvSshStateCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSshInfoTlvSshState *)fromB:(int32_t)b __attribute__((swift_name("from(b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraBrand")))
@interface DyrectoSharedCameraBrand : DyrectoSharedKotlinEnum<DyrectoSharedCameraBrand *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) DyrectoSharedCameraBrandCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) DyrectoSharedCameraBrand *sony __attribute__((swift_name("sony")));
@property (class, readonly) DyrectoSharedCameraBrand *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCameraBrand *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCameraBrand *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *displayName __attribute__((swift_name("displayName")));
@property (readonly) NSArray<NSString *> *filters __attribute__((swift_name("filters")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraBrand.Companion")))
@interface DyrectoSharedCameraBrandCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraBrandCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCameraBrand *)detectName:(NSString *)name __attribute__((swift_name("detect(name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraConnectionState")))
@interface DyrectoSharedCameraConnectionState : DyrectoSharedBase
- (instancetype)initWithPhase:(DyrectoSharedConnectionPhase *)phase running:(BOOL)running ble:(DyrectoSharedBleStatus *)ble ssh:(DyrectoSharedSshStatus *)ssh ptp:(DyrectoSharedPtpStatus *)ptp deviceInfo:(DyrectoSharedCameraDeviceInfo * _Nullable)deviceInfo telemetry:(DyrectoSharedCameraTelemetry * _Nullable)telemetry timeline:(NSArray<DyrectoSharedTimelineEvent *> *)timeline lastSuccessfulCommunicationAt:(DyrectoSharedLong * _Nullable)lastSuccessfulCommunicationAt lastTelemetryUpdateAt:(DyrectoSharedLong * _Nullable)lastTelemetryUpdateAt fatalError:(NSString * _Nullable)fatalError wifiCredentialsFailed:(BOOL)wifiCredentialsFailed discoveredCameraIp:(NSString * _Nullable)discoveredCameraIp __attribute__((swift_name("init(phase:running:ble:ssh:ptp:deviceInfo:telemetry:timeline:lastSuccessfulCommunicationAt:lastTelemetryUpdateAt:fatalError:wifiCredentialsFailed:discoveredCameraIp:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCameraConnectionState *)doCopyPhase:(DyrectoSharedConnectionPhase *)phase running:(BOOL)running ble:(DyrectoSharedBleStatus *)ble ssh:(DyrectoSharedSshStatus *)ssh ptp:(DyrectoSharedPtpStatus *)ptp deviceInfo:(DyrectoSharedCameraDeviceInfo * _Nullable)deviceInfo telemetry:(DyrectoSharedCameraTelemetry * _Nullable)telemetry timeline:(NSArray<DyrectoSharedTimelineEvent *> *)timeline lastSuccessfulCommunicationAt:(DyrectoSharedLong * _Nullable)lastSuccessfulCommunicationAt lastTelemetryUpdateAt:(DyrectoSharedLong * _Nullable)lastTelemetryUpdateAt fatalError:(NSString * _Nullable)fatalError wifiCredentialsFailed:(BOOL)wifiCredentialsFailed discoveredCameraIp:(NSString * _Nullable)discoveredCameraIp __attribute__((swift_name("doCopy(phase:running:ble:ssh:ptp:deviceInfo:telemetry:timeline:lastSuccessfulCommunicationAt:lastTelemetryUpdateAt:fatalError:wifiCredentialsFailed:discoveredCameraIp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedBleStatus *ble __attribute__((swift_name("ble")));
@property (readonly) DyrectoSharedCameraDeviceInfo * _Nullable deviceInfo __attribute__((swift_name("deviceInfo")));
@property (readonly) NSString * _Nullable discoveredCameraIp __attribute__((swift_name("discoveredCameraIp")));
@property (readonly) NSString * _Nullable fatalError __attribute__((swift_name("fatalError")));
@property (readonly) BOOL isFullyConnected __attribute__((swift_name("isFullyConnected")));
@property (readonly) DyrectoSharedLong * _Nullable lastSuccessfulCommunicationAt __attribute__((swift_name("lastSuccessfulCommunicationAt")));
@property (readonly) DyrectoSharedLong * _Nullable lastTelemetryUpdateAt __attribute__((swift_name("lastTelemetryUpdateAt")));
@property (readonly) DyrectoSharedConnectionPhase *phase __attribute__((swift_name("phase")));
@property (readonly) DyrectoSharedPtpStatus *ptp __attribute__((swift_name("ptp")));
@property (readonly) BOOL running __attribute__((swift_name("running")));
@property (readonly) DyrectoSharedSshStatus *ssh __attribute__((swift_name("ssh")));
@property (readonly) DyrectoSharedCameraTelemetry * _Nullable telemetry __attribute__((swift_name("telemetry")));
@property (readonly) NSArray<DyrectoSharedTimelineEvent *> *timeline __attribute__((swift_name("timeline")));
@property (readonly) BOOL wifiCredentialsFailed __attribute__((swift_name("wifiCredentialsFailed")));
@end

__attribute__((swift_name("CameraRepository")))
@protocol DyrectoSharedCameraRepository
@required
- (void)connect __attribute__((swift_name("connect()")));
- (void)connectToCameraAddress:(NSString *)address __attribute__((swift_name("connectToCamera(address:)")));
- (void)disconnect __attribute__((swift_name("disconnect()")));
- (void)dumpEeState __attribute__((swift_name("dumpEeState()")));
- (void)readEe02 __attribute__((swift_name("readEe02()")));
- (void)readEe04 __attribute__((swift_name("readEe04()")));
- (void)runOneTimePairing __attribute__((swift_name("runOneTimePairing()")));
- (void)setCameraIpIp:(NSString *)ip __attribute__((swift_name("setCameraIp(ip:)")));
- (void)startScan __attribute__((swift_name("startScan()")));
- (void)stopScan __attribute__((swift_name("stopScan()")));
@property (readonly) id<DyrectoSharedCameraAdapter> camera __attribute__((swift_name("camera")));
@property (readonly) id<DyrectoSharedStateFlow> state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraTelemetry")))
@interface DyrectoSharedCameraTelemetry : DyrectoSharedBase
- (instancetype)initWithProps:(NSDictionary<DyrectoSharedInt *, DyrectoSharedTelemetryProp *> *)props __attribute__((swift_name("init(props:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCameraTelemetryCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCameraTelemetry *)doCopyProps:(NSDictionary<DyrectoSharedInt *, DyrectoSharedTelemetryProp *> *)props __attribute__((swift_name("doCopy(props:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (DyrectoSharedTelemetryProp * _Nullable)getCode:(int32_t)code __attribute__((swift_name("get(code:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<DyrectoSharedInt *, DyrectoSharedTelemetryProp *> *props __attribute__((swift_name("props")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraTelemetry.Companion")))
@interface DyrectoSharedCameraTelemetryCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCameraTelemetryCompanion *shared __attribute__((swift_name("shared")));
- (NSString *)labelForCode:(int32_t)code __attribute__((swift_name("labelFor(code:)")));
@property (readonly) int32_t AUTO_POWER_OFF_TEMP __attribute__((swift_name("AUTO_POWER_OFF_TEMP")));
@property (readonly) int32_t BATTERY __attribute__((swift_name("BATTERY")));
@property (readonly) int32_t BATTERY_MINUTES __attribute__((swift_name("BATTERY_MINUTES")));
@property (readonly) int32_t BATTERY_TOTAL __attribute__((swift_name("BATTERY_TOTAL")));
@property (readonly) int32_t BATTERY_VOLTAGE __attribute__((swift_name("BATTERY_VOLTAGE")));
@property (readonly) int32_t COLOR_TEMP __attribute__((swift_name("COLOR_TEMP")));
@property (readonly) int32_t FILE_FORMAT_MOVIE __attribute__((swift_name("FILE_FORMAT_MOVIE")));
@property (readonly) int32_t FNUMBER __attribute__((swift_name("FNUMBER")));
@property (readonly) int32_t FOCUS_MODE __attribute__((swift_name("FOCUS_MODE")));
@property (readonly) int32_t FOCUS_MODE_SETTING __attribute__((swift_name("FOCUS_MODE_SETTING")));
@property (readonly) int32_t FOCUS_TOUCH_SPOT __attribute__((swift_name("FOCUS_TOUCH_SPOT")));
@property (readonly) int32_t FOCUS_TRACKING __attribute__((swift_name("FOCUS_TRACKING")));
@property (readonly) int32_t ISO __attribute__((swift_name("ISO")));
@property (readonly) int32_t LIVE_VIEW __attribute__((swift_name("LIVE_VIEW")));
@property (readonly) int32_t MONITORING_DELIVERING __attribute__((swift_name("MONITORING_DELIVERING")));
@property (readonly) int32_t MONITOR_CODEC __attribute__((swift_name("MONITOR_CODEC")));
@property (readonly) int32_t MONITOR_FPS __attribute__((swift_name("MONITOR_FPS")));
@property (readonly) int32_t MONITOR_LUT __attribute__((swift_name("MONITOR_LUT")));
@property (readonly) int32_t MONITOR_RESOLUTION __attribute__((swift_name("MONITOR_RESOLUTION")));
@property (readonly) int32_t MOVIE_REC __attribute__((swift_name("MOVIE_REC")));
@property (readonly) int32_t OVERHEATING __attribute__((swift_name("OVERHEATING")));
@property (readonly) int32_t PROXY_REC __attribute__((swift_name("PROXY_REC")));
@property (readonly) int32_t REC_FPS __attribute__((swift_name("REC_FPS")));
@property (readonly) int32_t REC_RESOLUTION __attribute__((swift_name("REC_RESOLUTION")));
@property (readonly) int32_t REC_SETTING_MOVIE __attribute__((swift_name("REC_SETTING_MOVIE")));
@property (readonly) int32_t REC_TIME __attribute__((swift_name("REC_TIME")));
@property (readonly) int32_t SANDQ_FPS __attribute__((swift_name("SANDQ_FPS")));
@property (readonly) int32_t SANDQ_MODE __attribute__((swift_name("SANDQ_MODE")));
@property (readonly) int32_t SHUTTER __attribute__((swift_name("SHUTTER")));
@property (readonly) int32_t SLOT1_REMAIN __attribute__((swift_name("SLOT1_REMAIN")));
@property (readonly) int32_t SLOT1_SHOTS __attribute__((swift_name("SLOT1_SHOTS")));
@property (readonly) int32_t SLOT1_STATUS __attribute__((swift_name("SLOT1_STATUS")));
@property (readonly) int32_t SLOT2_REMAIN __attribute__((swift_name("SLOT2_REMAIN")));
@property (readonly) int32_t SLOT2_SHOTS __attribute__((swift_name("SLOT2_SHOTS")));
@property (readonly) int32_t SLOT2_STATUS __attribute__((swift_name("SLOT2_STATUS")));
@property (readonly) int32_t SUBJECT_AF __attribute__((swift_name("SUBJECT_AF")));
@property (readonly) int32_t WHITE_BALANCE __attribute__((swift_name("WHITE_BALANCE")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConnectionPhase")))
@interface DyrectoSharedConnectionPhase : DyrectoSharedKotlinEnum<DyrectoSharedConnectionPhase *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedConnectionPhase *idle __attribute__((swift_name("idle")));
@property (class, readonly) DyrectoSharedConnectionPhase *scanning __attribute__((swift_name("scanning")));
@property (class, readonly) DyrectoSharedConnectionPhase *bleFound __attribute__((swift_name("bleFound")));
@property (class, readonly) DyrectoSharedConnectionPhase *connecting __attribute__((swift_name("connecting")));
@property (class, readonly) DyrectoSharedConnectionPhase *bonding __attribute__((swift_name("bonding")));
@property (class, readonly) DyrectoSharedConnectionPhase *connected __attribute__((swift_name("connected")));
@property (class, readonly) DyrectoSharedConnectionPhase *readingInfo __attribute__((swift_name("readingInfo")));
@property (class, readonly) DyrectoSharedConnectionPhase *smartphoneMode __attribute__((swift_name("smartphoneMode")));
@property (class, readonly) DyrectoSharedConnectionPhase *apCreating __attribute__((swift_name("apCreating")));
@property (class, readonly) DyrectoSharedConnectionPhase *wifiCredentials __attribute__((swift_name("wifiCredentials")));
@property (class, readonly) DyrectoSharedConnectionPhase *wifiJoining __attribute__((swift_name("wifiJoining")));
@property (class, readonly) DyrectoSharedConnectionPhase *ipDiscovery __attribute__((swift_name("ipDiscovery")));
@property (class, readonly) DyrectoSharedConnectionPhase *cc17Read __attribute__((swift_name("cc17Read")));
@property (class, readonly) DyrectoSharedConnectionPhase *sshConnecting __attribute__((swift_name("sshConnecting")));
@property (class, readonly) DyrectoSharedConnectionPhase *sshAuthenticated __attribute__((swift_name("sshAuthenticated")));
@property (class, readonly) DyrectoSharedConnectionPhase *ptpInit __attribute__((swift_name("ptpInit")));
@property (class, readonly) DyrectoSharedConnectionPhase *sessionOpen __attribute__((swift_name("sessionOpen")));
@property (class, readonly) DyrectoSharedConnectionPhase *deviceInfo __attribute__((swift_name("deviceInfo")));
@property (class, readonly) DyrectoSharedConnectionPhase *error __attribute__((swift_name("error")));
+ (DyrectoSharedKotlinArray<DyrectoSharedConnectionPhase *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedConnectionPhase *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TelemetryDecoder")))
@interface DyrectoSharedTelemetryDecoder : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)telemetryDecoder __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedTelemetryDecoder *shared __attribute__((swift_name("shared")));
- (NSString * _Nullable)decodeCode:(int32_t)code raw:(DyrectoSharedLong * _Nullable)raw __attribute__((swift_name("decode(code:raw:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TelemetryProp")))
@interface DyrectoSharedTelemetryProp : DyrectoSharedBase
- (instancetype)initWithCode:(int32_t)code label:(NSString *)label rawValue:(NSString * _Nullable)rawValue dataType:(int32_t)dataType rawNumber:(DyrectoSharedLong * _Nullable)rawNumber __attribute__((swift_name("init(code:label:rawValue:dataType:rawNumber:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedTelemetryProp *)doCopyCode:(int32_t)code label:(NSString *)label rawValue:(NSString * _Nullable)rawValue dataType:(int32_t)dataType rawNumber:(DyrectoSharedLong * _Nullable)rawNumber __attribute__((swift_name("doCopy(code:label:rawValue:dataType:rawNumber:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) int32_t dataType __attribute__((swift_name("dataType")));
@property (readonly) NSString * _Nullable decoded __attribute__((swift_name("decoded")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) DyrectoSharedLong * _Nullable rawNumber __attribute__((swift_name("rawNumber")));
@property (readonly) NSString * _Nullable rawValue __attribute__((swift_name("rawValue")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Alert")))
@interface DyrectoSharedAlert : DyrectoSharedBase
- (instancetype)initWithId:(int64_t)id type:(DyrectoSharedAlertType *)type severity:(DyrectoSharedAlertSeverity *)severity title:(NSString *)title message:(NSString *)message timestamp:(int64_t)timestamp __attribute__((swift_name("init(id:type:severity:title:message:timestamp:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAlert *)doCopyId:(int64_t)id type:(DyrectoSharedAlertType *)type severity:(DyrectoSharedAlertSeverity *)severity title:(NSString *)title message:(NSString *)message timestamp:(int64_t)timestamp __attribute__((swift_name("doCopy(id:type:severity:title:message:timestamp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t id __attribute__((swift_name("id")));
@property (readonly) NSString *message __attribute__((swift_name("message")));
@property (readonly) DyrectoSharedAlertSeverity *severity __attribute__((swift_name("severity")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@property (readonly) DyrectoSharedAlertType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertCategory")))
@interface DyrectoSharedAlertCategory : DyrectoSharedKotlinEnum<DyrectoSharedAlertCategory *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAlertCategory *recording __attribute__((swift_name("recording")));
@property (class, readonly) DyrectoSharedAlertCategory *battery __attribute__((swift_name("battery")));
@property (class, readonly) DyrectoSharedAlertCategory *media __attribute__((swift_name("media")));
@property (class, readonly) DyrectoSharedAlertCategory *thermal __attribute__((swift_name("thermal")));
@property (class, readonly) DyrectoSharedAlertCategory *connection __attribute__((swift_name("connection")));
@property (class, readonly) DyrectoSharedAlertCategory *exposure __attribute__((swift_name("exposure")));
@property (class, readonly) DyrectoSharedAlertCategory *face __attribute__((swift_name("face")));
@property (class, readonly) DyrectoSharedAlertCategory *reference __attribute__((swift_name("reference")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAlertCategory *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAlertCategory *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertConfig")))
@interface DyrectoSharedAlertConfig : DyrectoSharedBase
- (instancetype)initWithAlertType:(DyrectoSharedAlertType *)alertType enabled:(BOOL)enabled severity:(DyrectoSharedAlertSeverity *)severity soundPattern:(DyrectoSharedAlertPattern *)soundPattern vibrationPattern:(DyrectoSharedAlertPattern *)vibrationPattern __attribute__((swift_name("init(alertType:enabled:severity:soundPattern:vibrationPattern:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedAlertConfigCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedAlertConfig *)doCopyAlertType:(DyrectoSharedAlertType *)alertType enabled:(BOOL)enabled severity:(DyrectoSharedAlertSeverity *)severity soundPattern:(DyrectoSharedAlertPattern *)soundPattern vibrationPattern:(DyrectoSharedAlertPattern *)vibrationPattern __attribute__((swift_name("doCopy(alertType:enabled:severity:soundPattern:vibrationPattern:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAlertType *alertType __attribute__((swift_name("alertType")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));
@property (readonly) DyrectoSharedAlertSeverity *severity __attribute__((swift_name("severity")));
@property (readonly) DyrectoSharedAlertPattern *soundPattern __attribute__((swift_name("soundPattern")));
@property (readonly) DyrectoSharedAlertPattern *vibrationPattern __attribute__((swift_name("vibrationPattern")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertConfig.Companion")))
@interface DyrectoSharedAlertConfigCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedAlertConfigCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedAlertConfig *)defaultType:(DyrectoSharedAlertType *)type __attribute__((swift_name("default(type:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertEngine")))
@interface DyrectoSharedAlertEngine : DyrectoSharedBase
- (instancetype)initWithIdGen:(DyrectoSharedAlertIdGenerator *)idGen __attribute__((swift_name("init(idGen:)"))) __attribute__((objc_designated_initializer));
- (NSArray<DyrectoSharedAlert *> *)evaluateState:(DyrectoSharedCameraConnectionState *)state __attribute__((swift_name("evaluate(state:)")));
- (void)reset __attribute__((swift_name("reset()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertEngine.AlertInputs")))
@interface DyrectoSharedAlertEngineAlertInputs : DyrectoSharedBase
- (instancetype)initWithRecording:(DyrectoSharedBoolean * _Nullable)recording batteryPct:(DyrectoSharedInt * _Nullable)batteryPct batteryMinutes:(DyrectoSharedInt * _Nullable)batteryMinutes slot1Status:(DyrectoSharedInt * _Nullable)slot1Status slot2Status:(DyrectoSharedInt * _Nullable)slot2Status overheating:(DyrectoSharedBoolean * _Nullable)overheating connected:(BOOL)connected __attribute__((swift_name("init(recording:batteryPct:batteryMinutes:slot1Status:slot2Status:overheating:connected:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAlertEngineAlertInputs *)doCopyRecording:(DyrectoSharedBoolean * _Nullable)recording batteryPct:(DyrectoSharedInt * _Nullable)batteryPct batteryMinutes:(DyrectoSharedInt * _Nullable)batteryMinutes slot1Status:(DyrectoSharedInt * _Nullable)slot1Status slot2Status:(DyrectoSharedInt * _Nullable)slot2Status overheating:(DyrectoSharedBoolean * _Nullable)overheating connected:(BOOL)connected __attribute__((swift_name("doCopy(recording:batteryPct:batteryMinutes:slot1Status:slot2Status:overheating:connected:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedInt * _Nullable batteryMinutes __attribute__((swift_name("batteryMinutes")));
@property (readonly) DyrectoSharedInt * _Nullable batteryPct __attribute__((swift_name("batteryPct")));
@property (readonly) BOOL connected __attribute__((swift_name("connected")));
@property (readonly) DyrectoSharedBoolean * _Nullable overheating __attribute__((swift_name("overheating")));
@property (readonly) DyrectoSharedBoolean * _Nullable recording __attribute__((swift_name("recording")));
@property (readonly) DyrectoSharedInt * _Nullable slot1Status __attribute__((swift_name("slot1Status")));
@property (readonly) DyrectoSharedInt * _Nullable slot2Status __attribute__((swift_name("slot2Status")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertIdGenerator")))
@interface DyrectoSharedAlertIdGenerator : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (int64_t)next __attribute__((swift_name("next()")));
- (void)reset __attribute__((swift_name("reset()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertPattern")))
@interface DyrectoSharedAlertPattern : DyrectoSharedBase
- (instancetype)initWithCount:(int32_t)count intervalMs:(int64_t)intervalMs __attribute__((swift_name("init(count:intervalMs:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedAlertPatternCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedAlertPattern *)doCopyCount:(int32_t)count intervalMs:(int64_t)intervalMs __attribute__((swift_name("doCopy(count:intervalMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t count __attribute__((swift_name("count")));
@property (readonly) int64_t intervalMs __attribute__((swift_name("intervalMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertPattern.Companion")))
@interface DyrectoSharedAlertPatternCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedAlertPatternCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedAlertPattern *)ofCount:(int32_t)count intervalMs:(int64_t)intervalMs __attribute__((swift_name("of(count:intervalMs:)")));
@property (readonly) int64_t DEFAULT_INTERVAL_MS __attribute__((swift_name("DEFAULT_INTERVAL_MS")));
@property (readonly) int32_t MAX_COUNT __attribute__((swift_name("MAX_COUNT")));
@property (readonly) int64_t MAX_INTERVAL_MS __attribute__((swift_name("MAX_INTERVAL_MS")));
@property (readonly) int32_t MIN_COUNT __attribute__((swift_name("MIN_COUNT")));
@property (readonly) int64_t MIN_INTERVAL_MS __attribute__((swift_name("MIN_INTERVAL_MS")));
@property (readonly) DyrectoSharedAlertPattern *NONE __attribute__((swift_name("NONE")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertSeverity")))
@interface DyrectoSharedAlertSeverity : DyrectoSharedKotlinEnum<DyrectoSharedAlertSeverity *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAlertSeverity *info __attribute__((swift_name("info")));
@property (class, readonly) DyrectoSharedAlertSeverity *warning __attribute__((swift_name("warning")));
@property (class, readonly) DyrectoSharedAlertSeverity *critical __attribute__((swift_name("critical")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAlertSeverity *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAlertSeverity *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AlertType")))
@interface DyrectoSharedAlertType : DyrectoSharedKotlinEnum<DyrectoSharedAlertType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAlertType *recordingStarted __attribute__((swift_name("recordingStarted")));
@property (class, readonly) DyrectoSharedAlertType *recordingStopped __attribute__((swift_name("recordingStopped")));
@property (class, readonly) DyrectoSharedAlertType *batteryLow20 __attribute__((swift_name("batteryLow20")));
@property (class, readonly) DyrectoSharedAlertType *batteryLow10 __attribute__((swift_name("batteryLow10")));
@property (class, readonly) DyrectoSharedAlertType *batteryLow5 __attribute__((swift_name("batteryLow5")));
@property (class, readonly) DyrectoSharedAlertType *batteryTime10Min __attribute__((swift_name("batteryTime10Min")));
@property (class, readonly) DyrectoSharedAlertType *batteryTime5Min __attribute__((swift_name("batteryTime5Min")));
@property (class, readonly) DyrectoSharedAlertType *cardInserted __attribute__((swift_name("cardInserted")));
@property (class, readonly) DyrectoSharedAlertType *cardRemoved __attribute__((swift_name("cardRemoved")));
@property (class, readonly) DyrectoSharedAlertType *overheating __attribute__((swift_name("overheating")));
@property (class, readonly) DyrectoSharedAlertType *connectionLost __attribute__((swift_name("connectionLost")));
@property (class, readonly) DyrectoSharedAlertType *highlightClipping __attribute__((swift_name("highlightClipping")));
@property (class, readonly) DyrectoSharedAlertType *shadowClipping __attribute__((swift_name("shadowClipping")));
@property (class, readonly) DyrectoSharedAlertType *faceLost __attribute__((swift_name("faceLost")));
@property (class, readonly) DyrectoSharedAlertType *eyesNotVisible __attribute__((swift_name("eyesNotVisible")));
@property (class, readonly) DyrectoSharedAlertType *highlightRecovered __attribute__((swift_name("highlightRecovered")));
@property (class, readonly) DyrectoSharedAlertType *shadowRecovered __attribute__((swift_name("shadowRecovered")));
@property (class, readonly) DyrectoSharedAlertType *referenceExposureDrift __attribute__((swift_name("referenceExposureDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceWhiteBalanceDrift __attribute__((swift_name("referenceWhiteBalanceDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceSubjectPositionDrift __attribute__((swift_name("referenceSubjectPositionDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceSubjectSizeDrift __attribute__((swift_name("referenceSubjectSizeDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceHeadroomDrift __attribute__((swift_name("referenceHeadroomDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceFaceMissing __attribute__((swift_name("referenceFaceMissing")));
@property (class, readonly) DyrectoSharedAlertType *referenceEyesMissing __attribute__((swift_name("referenceEyesMissing")));
@property (class, readonly) DyrectoSharedAlertType *referenceRecovered __attribute__((swift_name("referenceRecovered")));
@property (class, readonly) DyrectoSharedAlertType *referenceSubjectMissing __attribute__((swift_name("referenceSubjectMissing")));
@property (class, readonly) DyrectoSharedAlertType *referenceCompositionDrift __attribute__((swift_name("referenceCompositionDrift")));
@property (class, readonly) DyrectoSharedAlertType *referenceVisualMismatch __attribute__((swift_name("referenceVisualMismatch")));
@property (class, readonly) DyrectoSharedAlertType *storyboardIncomplete __attribute__((swift_name("storyboardIncomplete")));
@property (class, readonly) DyrectoSharedAlertType *settingsDrift __attribute__((swift_name("settingsDrift")));
@property (class, readonly) DyrectoSharedAlertType *pictureProfileChanged __attribute__((swift_name("pictureProfileChanged")));
@property (class, readonly) DyrectoSharedAlertType *frameRateMismatch __attribute__((swift_name("frameRateMismatch")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAlertType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAlertType *> *entries __attribute__((swift_name("entries")));
@property (readonly) DyrectoSharedAlertCategory *category __attribute__((swift_name("category")));
@property (readonly) DyrectoSharedAlertSeverity *defaultSeverity __attribute__((swift_name("defaultSeverity")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureAlertRules")))
@interface DyrectoSharedExposureAlertRules : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)exposureAlertRules __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedExposureAlertRules *shared __attribute__((swift_name("shared")));
- (DyrectoSharedExposureAlertRulesResult *)evaluateState:(DyrectoSharedExposureAlertState *)state scene:(DyrectoSharedSceneContext *)scene idGen:(DyrectoSharedAlertIdGenerator *)idGen __attribute__((swift_name("evaluate(state:scene:idGen:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureAlertRules.Result")))
@interface DyrectoSharedExposureAlertRulesResult : DyrectoSharedBase
- (instancetype)initWithAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedExposureAlertState *)state __attribute__((swift_name("init(alerts:state:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedExposureAlertRulesResult *)doCopyAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedExposureAlertState *)state __attribute__((swift_name("doCopy(alerts:state:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedAlert *> *alerts __attribute__((swift_name("alerts")));
@property (readonly) DyrectoSharedExposureAlertState *state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureAlertState")))
@interface DyrectoSharedExposureAlertState : DyrectoSharedBase
- (instancetype)initWithSeeded:(BOOL)seeded highlightClipFired:(BOOL)highlightClipFired shadowClipFired:(BOOL)shadowClipFired __attribute__((swift_name("init(seeded:highlightClipFired:shadowClipFired:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedExposureAlertState *)doCopySeeded:(BOOL)seeded highlightClipFired:(BOOL)highlightClipFired shadowClipFired:(BOOL)shadowClipFired __attribute__((swift_name("doCopy(seeded:highlightClipFired:shadowClipFired:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL highlightClipFired __attribute__((swift_name("highlightClipFired")));
@property (readonly) BOOL seeded __attribute__((swift_name("seeded")));
@property (readonly) BOOL shadowClipFired __attribute__((swift_name("shadowClipFired")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneAlertRules")))
@interface DyrectoSharedSceneAlertRules : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sceneAlertRules __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneAlertRules *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSceneAlertRulesResult *)evaluateState:(DyrectoSharedSceneAlertState *)state scene:(DyrectoSharedSceneContext *)scene idGen:(DyrectoSharedAlertIdGenerator *)idGen __attribute__((swift_name("evaluate(state:scene:idGen:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneAlertRules.Result")))
@interface DyrectoSharedSceneAlertRulesResult : DyrectoSharedBase
- (instancetype)initWithAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedSceneAlertState *)state __attribute__((swift_name("init(alerts:state:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneAlertRulesResult *)doCopyAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedSceneAlertState *)state __attribute__((swift_name("doCopy(alerts:state:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedAlert *> *alerts __attribute__((swift_name("alerts")));
@property (readonly) DyrectoSharedSceneAlertState *state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneAlertState")))
@interface DyrectoSharedSceneAlertState : DyrectoSharedBase
- (instancetype)initWithPreviousScene:(DyrectoSharedSceneContext * _Nullable)previousScene faceTrackingActive:(BOOL)faceTrackingActive eyesNotVisibleFired:(BOOL)eyesNotVisibleFired __attribute__((swift_name("init(previousScene:faceTrackingActive:eyesNotVisibleFired:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneAlertState *)doCopyPreviousScene:(DyrectoSharedSceneContext * _Nullable)previousScene faceTrackingActive:(BOOL)faceTrackingActive eyesNotVisibleFired:(BOOL)eyesNotVisibleFired __attribute__((swift_name("doCopy(previousScene:faceTrackingActive:eyesNotVisibleFired:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL eyesNotVisibleFired __attribute__((swift_name("eyesNotVisibleFired")));
@property (readonly) BOOL faceTrackingActive __attribute__((swift_name("faceTrackingActive")));
@property (readonly) DyrectoSharedSceneContext * _Nullable previousScene __attribute__((swift_name("previousScene")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FlowWatcher")))
@interface DyrectoSharedFlowWatcher<T> : DyrectoSharedBase
- (instancetype)initWithFlow:(id<DyrectoSharedFlow>)flow __attribute__((swift_name("init(flow:)"))) __attribute__((objc_designated_initializer));
- (void)close __attribute__((swift_name("close()")));
- (void)watchBlock:(void (^)(T _Nullable))block __attribute__((swift_name("watch(block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IosExposureModule")))
@interface DyrectoSharedIosExposureModule : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSArray<id<DyrectoSharedVisionResult>> *)analyzeRgbaData:(NSData *)rgbaData width:(int32_t)width height:(int32_t)height __attribute__((swift_name("analyze(rgbaData:width:height:)")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@end

__attribute__((swift_name("FramePixels")))
@protocol DyrectoSharedFramePixels
@required
- (void)readArgbDest:(DyrectoSharedKotlinIntArray *)dest __attribute__((swift_name("readArgb(dest:)")));
@property (readonly) int32_t height __attribute__((swift_name("height")));
@property (readonly) BOOL isAvailable __attribute__((swift_name("isAvailable")));
@property (readonly) int32_t width __attribute__((swift_name("width")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RgbaFramePixels")))
@interface DyrectoSharedRgbaFramePixels : DyrectoSharedBase <DyrectoSharedFramePixels>
- (instancetype)initWithRgba:(DyrectoSharedKotlinByteArray *)rgba width:(int32_t)width height:(int32_t)height __attribute__((swift_name("init(rgba:width:height:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedRgbaFramePixelsCompanion *companion __attribute__((swift_name("companion")));
- (void)readArgbDest:(DyrectoSharedKotlinIntArray *)dest __attribute__((swift_name("readArgb(dest:)")));
@property (readonly) int32_t height __attribute__((swift_name("height")));
@property (readonly) BOOL isAvailable __attribute__((swift_name("isAvailable")));
@property (readonly) int32_t width __attribute__((swift_name("width")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RgbaFramePixels.Companion")))
@interface DyrectoSharedRgbaFramePixelsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedRgbaFramePixelsCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedRgbaFramePixels *)fromNSDataData:(NSData *)data width:(int32_t)width height:(int32_t)height __attribute__((swift_name("fromNSData(data:width:height:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SwiftEnums")))
@interface DyrectoSharedSwiftEnums : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)swiftEnums __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSwiftEnums *shared __attribute__((swift_name("shared")));
- (DyrectoSharedAlertPattern *)alertPatternCount:(int32_t)count __attribute__((swift_name("alertPattern(count:)")));
- (DyrectoSharedAlertSeverity * _Nullable)alertSeverityOrNullName:(NSString *)name __attribute__((swift_name("alertSeverityOrNull(name:)")));
- (NSArray<DyrectoSharedAlertType *> *)alertTypes __attribute__((swift_name("alertTypes()")));
- (DyrectoSharedVoiceMode * _Nullable)voiceModeOrNullName:(NSString *)name __attribute__((swift_name("voiceModeOrNull(name:)")));
- (DyrectoSharedVoiceSpeechRate * _Nullable)voiceSpeechRateOrNullName:(NSString *)name __attribute__((swift_name("voiceSpeechRateOrNull(name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdaptiveSampler")))
@interface DyrectoSharedAdaptiveSampler : DyrectoSharedBase
- (instancetype)initWithHighBudgetNs:(int64_t)highBudgetNs lowBudgetNs:(int64_t)lowBudgetNs __attribute__((swift_name("init(highBudgetNs:lowBudgetNs:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedAdaptiveSamplerCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)recordElapsedNs:(int64_t)elapsedNs __attribute__((swift_name("record(elapsedNs:)")));
- (void)reset __attribute__((swift_name("reset()")));
@property (readonly) int32_t stride __attribute__((swift_name("stride")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdaptiveSampler.Companion")))
@interface DyrectoSharedAdaptiveSamplerCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedAdaptiveSamplerCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t MAX_STRIDE __attribute__((swift_name("MAX_STRIDE")));
@property (readonly) int32_t OVER_STREAK __attribute__((swift_name("OVER_STREAK")));
@property (readonly) int32_t UNDER_STREAK __attribute__((swift_name("UNDER_STREAK")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AnalysisColorSpace")))
@interface DyrectoSharedAnalysisColorSpace : DyrectoSharedKotlinEnum<DyrectoSharedAnalysisColorSpace *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAnalysisColorSpace *auto_ __attribute__((swift_name("auto_")));
@property (class, readonly) DyrectoSharedAnalysisColorSpace *rec709 __attribute__((swift_name("rec709")));
@property (class, readonly) DyrectoSharedAnalysisColorSpace *sLog3 __attribute__((swift_name("sLog3")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAnalysisColorSpace *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAnalysisColorSpace *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("AnalysisColorTransform")))
@protocol DyrectoSharedAnalysisColorTransform
@required
- (int32_t)toLumaR:(int32_t)r g:(int32_t)g b:(int32_t)b __attribute__((swift_name("toLuma(r:g:b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AnalysisRegion")))
@interface DyrectoSharedAnalysisRegion : DyrectoSharedBase
- (instancetype)initWithLeft:(float)left top:(float)top right:(float)right bottom:(float)bottom __attribute__((swift_name("init(left:top:right:bottom:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAnalysisRegion *)doCopyLeft:(float)left top:(float)top right:(float)right bottom:(float)bottom __attribute__((swift_name("doCopy(left:top:right:bottom:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float bottom __attribute__((swift_name("bottom")));
@property (readonly) float left __attribute__((swift_name("left")));
@property (readonly) float right __attribute__((swift_name("right")));
@property (readonly) float top __attribute__((swift_name("top")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AnalysisRegionRegistry")))
@interface DyrectoSharedAnalysisRegionRegistry : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)analysisRegionRegistry __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedAnalysisRegionRegistry *shared __attribute__((swift_name("shared")));
- (void)clear __attribute__((swift_name("clear()")));
@property int32_t highlightLumaMin __attribute__((swift_name("highlightLumaMin")));
@property int32_t shadowLumaMax __attribute__((swift_name("shadowLumaMax")));
@property DyrectoSharedAnalysisRegion * _Nullable subjectRegion __attribute__((swift_name("subjectRegion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ColorAccumulator")))
@interface DyrectoSharedColorAccumulator : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (readonly) float avgB __attribute__((swift_name("avgB")));
@property (readonly) float avgG __attribute__((swift_name("avgG")));
@property (readonly) float avgR __attribute__((swift_name("avgR")));
@property (readonly) int32_t sampleCount __attribute__((swift_name("sampleCount")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureAnalyzer")))
@interface DyrectoSharedExposureAnalyzer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)exposureAnalyzer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedExposureAnalyzer *shared __attribute__((swift_name("shared")));
- (DyrectoSharedExposureResult *)analyzeHistogram:(DyrectoSharedHistogramResult *)histogram zebra:(DyrectoSharedZebraResult *)zebra __attribute__((swift_name("analyze(histogram:zebra:)")));
@property (readonly) float DETECT_PCT __attribute__((swift_name("DETECT_PCT")));
@property (readonly) float HIGHLIGHT_CLIP_PCT __attribute__((swift_name("HIGHLIGHT_CLIP_PCT")));
@property (readonly) float HIGHLIGHT_RECOVERY_PCT __attribute__((swift_name("HIGHLIGHT_RECOVERY_PCT")));
@property (readonly) int32_t PERSISTENCE_FRAMES __attribute__((swift_name("PERSISTENCE_FRAMES")));
@property (readonly) int32_t RECOVERY_FRAMES __attribute__((swift_name("RECOVERY_FRAMES")));
@property (readonly) float SHADOW_CLIP_PCT __attribute__((swift_name("SHADOW_CLIP_PCT")));
@property (readonly) float SHADOW_RECOVERY_PCT __attribute__((swift_name("SHADOW_RECOVERY_PCT")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureConfig")))
@interface DyrectoSharedExposureConfig : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)exposureConfig __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedExposureConfig *shared __attribute__((swift_name("shared")));
- (void)registerSLog3TransformTransform:(id<DyrectoSharedAnalysisColorTransform>)transform lutName:(NSString *)lutName __attribute__((swift_name("registerSLog3Transform(transform:lutName:)")));
- (id<DyrectoSharedAnalysisColorTransform>)resolveTransform __attribute__((swift_name("resolveTransform()")));
- (void)setAnalysisColorSpaceSpace:(DyrectoSharedAnalysisColorSpace *)space __attribute__((swift_name("setAnalysisColorSpace(space:)")));
- (void)setZebraLevelIre:(int32_t)ire __attribute__((swift_name("setZebraLevel(ire:)")));
- (void)setZebraSpecSpec:(id<DyrectoSharedZebraSpec>)spec __attribute__((swift_name("setZebraSpec(spec:)")));
@property (readonly) NSArray<DyrectoSharedInt *> *PRESET_LEVELS __attribute__((swift_name("PRESET_LEVELS")));
@property (readonly) id<DyrectoSharedStateFlow> analysisColorSpace __attribute__((swift_name("analysisColorSpace")));
@property (readonly) NSString * _Nullable sLog3LutName __attribute__((swift_name("sLog3LutName")));
@property (readonly) id<DyrectoSharedStateFlow> zebraSpec __attribute__((swift_name("zebraSpec")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureStateMachine")))
@interface DyrectoSharedExposureStateMachine : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)reset __attribute__((swift_name("reset()")));
- (DyrectoSharedExposureResult *)updateRaw:(DyrectoSharedExposureResult *)raw __attribute__((swift_name("update(raw:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FalseColorEngine")))
@interface DyrectoSharedFalseColorEngine : DyrectoSharedBase
- (instancetype)initWithModuleId:(NSString *)moduleId __attribute__((swift_name("init(moduleId:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFalseColorResult *)computeLuma:(DyrectoSharedLumaField *)luma __attribute__((swift_name("compute(luma:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FalseColorScale")))
@interface DyrectoSharedFalseColorScale : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)falseColorScale __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedFalseColorScale *shared __attribute__((swift_name("shared")));
@property (readonly) NSArray<DyrectoSharedFalseColorBand *> *bands __attribute__((swift_name("bands")));
@property (readonly) DyrectoSharedKotlinIntArray *lumaToBand __attribute__((swift_name("lumaToBand")));
@property (readonly) DyrectoSharedKotlinIntArray *lumaToColor __attribute__((swift_name("lumaToColor")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FocusPeakingEngine")))
@interface DyrectoSharedFocusPeakingEngine : DyrectoSharedBase
- (instancetype)initWithModuleId:(NSString *)moduleId threshold:(int32_t)threshold __attribute__((swift_name("init(moduleId:threshold:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFocusPeakingResult *)computeLuma:(DyrectoSharedLumaField *)luma generateMask:(BOOL)generateMask __attribute__((swift_name("compute(luma:generateMask:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HistogramEngine")))
@interface DyrectoSharedHistogramEngine : DyrectoSharedBase
- (instancetype)initWithModuleId:(NSString *)moduleId __attribute__((swift_name("init(moduleId:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedHistogramEngineCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedHistogramResult *)computeLuma:(DyrectoSharedLumaField *)luma __attribute__((swift_name("compute(luma:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HistogramEngine.Companion")))
@interface DyrectoSharedHistogramEngineCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedHistogramEngineCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t BIN_COUNT __attribute__((swift_name("BIN_COUNT")));
@property (readonly) int32_t HIGHLIGHT_CLIP_BIN __attribute__((swift_name("HIGHLIGHT_CLIP_BIN")));
@property (readonly) int32_t SHADOW_CLIP_BIN __attribute__((swift_name("SHADOW_CLIP_BIN")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IreScale")))
@interface DyrectoSharedIreScale : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)ireScale __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedIreScale *shared __attribute__((swift_name("shared")));
- (int32_t)ireToLumaIre:(int32_t)ire __attribute__((swift_name("ireToLuma(ire:)")));
- (int32_t)lumaToIreLuma:(int32_t)luma __attribute__((swift_name("lumaToIre(luma:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LumaField")))
@interface DyrectoSharedLumaField : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (readonly) int32_t count __attribute__((swift_name("count")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) DyrectoSharedKotlinIntArray *luma __attribute__((swift_name("luma")));
@property (readonly) int32_t sourceHeight __attribute__((swift_name("sourceHeight")));
@property (readonly) int32_t sourceWidth __attribute__((swift_name("sourceWidth")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LuminanceAnalyzer")))
@interface DyrectoSharedLuminanceAnalyzer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)luminanceAnalyzer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedLuminanceAnalyzer *shared __attribute__((swift_name("shared")));
- (DyrectoSharedLumaField *)analyzeArgb:(DyrectoSharedKotlinIntArray *)argb width:(int32_t)width height:(int32_t)height stride:(int32_t)stride out:(DyrectoSharedLumaField *)out transform:(id<DyrectoSharedAnalysisColorTransform>)transform colorAccumulator:(DyrectoSharedColorAccumulator * _Nullable)colorAccumulator __attribute__((swift_name("analyze(argb:width:height:stride:out:transform:colorAccumulator:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Lut3D")))
@interface DyrectoSharedLut3D : DyrectoSharedBase
- (instancetype)initWithSize:(int32_t)size table:(DyrectoSharedKotlinFloatArray *)table __attribute__((swift_name("init(size:table:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedLut3DCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedKotlinFloatArray *)sampleR:(float)r g:(float)g b:(float)b __attribute__((swift_name("sample(r:g:b:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Lut3D.Companion")))
@interface DyrectoSharedLut3DCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedLut3DCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedLut3D *)parseText:(NSString *)text __attribute__((swift_name("parse(text:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Lut3DColorTransform")))
@interface DyrectoSharedLut3DColorTransform : DyrectoSharedBase <DyrectoSharedAnalysisColorTransform>
- (instancetype)initWithLut:(DyrectoSharedLut3D *)lut __attribute__((swift_name("init(lut:)"))) __attribute__((objc_designated_initializer));
- (int32_t)toLumaR:(int32_t)r g:(int32_t)g b:(int32_t)b __attribute__((swift_name("toLuma(r:g:b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("OverlayConfig")))
@interface DyrectoSharedOverlayConfig : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)overlayConfig __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedOverlayConfig *shared __attribute__((swift_name("shared")));
- (BOOL)anyEnabled __attribute__((swift_name("anyEnabled()")));
- (void)setFalseColorOn:(BOOL)on __attribute__((swift_name("setFalseColor(on:)")));
- (void)setFocusPeakingOn:(BOOL)on __attribute__((swift_name("setFocusPeaking(on:)")));
- (void)setWaveformOn:(BOOL)on __attribute__((swift_name("setWaveform(on:)")));
@property (readonly) id<DyrectoSharedStateFlow> falseColor __attribute__((swift_name("falseColor")));
@property (readonly) id<DyrectoSharedStateFlow> focusPeaking __attribute__((swift_name("focusPeaking")));
@property (readonly) id<DyrectoSharedStateFlow> waveform __attribute__((swift_name("waveform")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Rec709Transform")))
@interface DyrectoSharedRec709Transform : DyrectoSharedBase <DyrectoSharedAnalysisColorTransform>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)rec709Transform __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedRec709Transform *shared __attribute__((swift_name("shared")));
- (int32_t)toLumaR:(int32_t)r g:(int32_t)g b:(int32_t)b __attribute__((swift_name("toLuma(r:g:b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RegionLumaStats")))
@interface DyrectoSharedRegionLumaStats : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)regionLumaStats __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedRegionLumaStats *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSubjectExposureStats * _Nullable)computeArgb:(DyrectoSharedKotlinIntArray *)argb width:(int32_t)width height:(int32_t)height region:(DyrectoSharedAnalysisRegion *)region stride:(int32_t)stride transform:(id<DyrectoSharedAnalysisColorTransform>)transform highlightLumaMin:(int32_t)highlightLumaMin shadowLumaMax:(int32_t)shadowLumaMax __attribute__((swift_name("compute(argb:width:height:region:stride:transform:highlightLumaMin:shadowLumaMax:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectExposureStats")))
@interface DyrectoSharedSubjectExposureStats : DyrectoSharedBase
- (instancetype)initWithMean:(float)mean median:(float)median highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage sampleCount:(int32_t)sampleCount __attribute__((swift_name("init(mean:median:highlightCoverage:shadowCoverage:sampleCount:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSubjectExposureStatsCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSubjectExposureStats *)doCopyMean:(float)mean median:(float)median highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage sampleCount:(int32_t)sampleCount __attribute__((swift_name("doCopy(mean:median:highlightCoverage:shadowCoverage:sampleCount:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float highlightCoverage __attribute__((swift_name("highlightCoverage")));
@property (readonly) float mean __attribute__((swift_name("mean")));
@property (readonly) float median __attribute__((swift_name("median")));
@property (readonly) int32_t sampleCount __attribute__((swift_name("sampleCount")));
@property (readonly) float shadowCoverage __attribute__((swift_name("shadowCoverage")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectExposureStats.Companion")))
@interface DyrectoSharedSubjectExposureStatsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSubjectExposureStatsCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WaveformEngine")))
@interface DyrectoSharedWaveformEngine : DyrectoSharedBase
- (instancetype)initWithModuleId:(NSString *)moduleId targetColumns:(int32_t)targetColumns bins:(int32_t)bins cellCap:(int32_t)cellCap __attribute__((swift_name("init(moduleId:targetColumns:bins:cellCap:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedWaveformResult *)computeLuma:(DyrectoSharedLumaField *)luma __attribute__((swift_name("compute(luma:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraEngine")))
@interface DyrectoSharedZebraEngine : DyrectoSharedBase
- (instancetype)initWithModuleId:(NSString *)moduleId __attribute__((swift_name("init(moduleId:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedZebraEngineCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedZebraResult *)computeLuma:(DyrectoSharedLumaField *)luma spec:(id<DyrectoSharedZebraSpec>)spec generateMask:(BOOL)generateMask __attribute__((swift_name("compute(luma:spec:generateMask:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraEngine.Companion")))
@interface DyrectoSharedZebraEngineCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedZebraEngineCompanion *shared __attribute__((swift_name("shared")));
- (int32_t)lumaForIreIre:(int32_t)ire __attribute__((swift_name("lumaForIre(ire:)")));
@end

__attribute__((swift_name("ZebraSpec")))
@protocol DyrectoSharedZebraSpec
@required
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraSpecCompanion")))
@interface DyrectoSharedZebraSpecCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedZebraSpecCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) id<DyrectoSharedZebraSpec> PRESET_100 __attribute__((swift_name("PRESET_100")));
@property (readonly) id<DyrectoSharedZebraSpec> PRESET_70 __attribute__((swift_name("PRESET_70")));
@property (readonly) id<DyrectoSharedZebraSpec> PRESET_95 __attribute__((swift_name("PRESET_95")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraSpecLevel")))
@interface DyrectoSharedZebraSpecLevel : DyrectoSharedBase <DyrectoSharedZebraSpec>
- (instancetype)initWithLevel:(int32_t)level __attribute__((swift_name("init(level:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedZebraSpecLevel *)doCopyLevel:(int32_t)level __attribute__((swift_name("doCopy(level:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) int32_t level __attribute__((swift_name("level")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraSpecRange")))
@interface DyrectoSharedZebraSpecRange : DyrectoSharedBase <DyrectoSharedZebraSpec>
- (instancetype)initWithCenter:(int32_t)center range:(int32_t)range __attribute__((swift_name("init(center:range:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedZebraSpecRange *)doCopyCenter:(int32_t)center range:(int32_t)range __attribute__((swift_name("doCopy(center:range:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t center __attribute__((swift_name("center")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) int32_t range __attribute__((swift_name("range")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AssistantAction")))
@interface DyrectoSharedAssistantAction : DyrectoSharedKotlinEnum<DyrectoSharedAssistantAction *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAssistantAction *panLeft __attribute__((swift_name("panLeft")));
@property (class, readonly) DyrectoSharedAssistantAction *panRight __attribute__((swift_name("panRight")));
@property (class, readonly) DyrectoSharedAssistantAction *tiltUp __attribute__((swift_name("tiltUp")));
@property (class, readonly) DyrectoSharedAssistantAction *tiltDown __attribute__((swift_name("tiltDown")));
@property (class, readonly) DyrectoSharedAssistantAction *moveCloser __attribute__((swift_name("moveCloser")));
@property (class, readonly) DyrectoSharedAssistantAction *moveBack __attribute__((swift_name("moveBack")));
@property (class, readonly) DyrectoSharedAssistantAction *zoomIn __attribute__((swift_name("zoomIn")));
@property (class, readonly) DyrectoSharedAssistantAction *zoomOut __attribute__((swift_name("zoomOut")));
@property (class, readonly) DyrectoSharedAssistantAction *increaseExposure __attribute__((swift_name("increaseExposure")));
@property (class, readonly) DyrectoSharedAssistantAction *reduceExposure __attribute__((swift_name("reduceExposure")));
@property (class, readonly) DyrectoSharedAssistantAction *coolWhiteBalance __attribute__((swift_name("coolWhiteBalance")));
@property (class, readonly) DyrectoSharedAssistantAction *warmWhiteBalance __attribute__((swift_name("warmWhiteBalance")));
@property (class, readonly) DyrectoSharedAssistantAction *waitForSubject __attribute__((swift_name("waitForSubject")));
@property (class, readonly) DyrectoSharedAssistantAction *reframe __attribute__((swift_name("reframe")));
@property (class, readonly) DyrectoSharedAssistantAction *matchReference __attribute__((swift_name("matchReference")));
@property (class, readonly) DyrectoSharedAssistantAction *none __attribute__((swift_name("none")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAssistantAction *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAssistantAction *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AssistantInstruction")))
@interface DyrectoSharedAssistantInstruction : DyrectoSharedBase
- (instancetype)initWithAction:(DyrectoSharedAssistantAction *)action message:(NSString *)message confidence:(float)confidence progress:(float)progress reason:(NSString *)reason __attribute__((swift_name("init(action:message:confidence:progress:reason:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAssistantInstruction *)doCopyAction:(DyrectoSharedAssistantAction *)action message:(NSString *)message confidence:(float)confidence progress:(float)progress reason:(NSString *)reason __attribute__((swift_name("doCopy(action:message:confidence:progress:reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAssistantAction *action __attribute__((swift_name("action")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) NSString *message __attribute__((swift_name("message")));
@property (readonly) float progress __attribute__((swift_name("progress")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InstructionFormatter")))
@interface DyrectoSharedInstructionFormatter : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)instructionFormatter __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedInstructionFormatter *shared __attribute__((swift_name("shared")));
- (NSString *)confidenceLabelInstruction:(DyrectoSharedAssistantInstruction *)instruction __attribute__((swift_name("confidenceLabel(instruction:)")));
- (NSString *)messageAction:(DyrectoSharedAssistantAction *)action severity:(DyrectoSharedPerceptualSeverity *)severity __attribute__((swift_name("message(action:severity:)")));
- (NSString *)progressPercentInstruction:(DyrectoSharedAssistantInstruction *)instruction __attribute__((swift_name("progressPercent(instruction:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InstructionProgress")))
@interface DyrectoSharedInstructionProgress : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)instructionProgress __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedInstructionProgress *shared __attribute__((swift_name("shared")));
- (float)ofResult:(DyrectoSharedReferenceSignalResult *)result __attribute__((swift_name("of(result:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InstructionSelector")))
@interface DyrectoSharedInstructionSelector : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)instructionSelector __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedInstructionSelector *shared __attribute__((swift_name("shared")));
- (NSArray<DyrectoSharedReferenceSignal *> *)priorityForStrategy:(DyrectoSharedComparisonStrategy *)strategy __attribute__((swift_name("priorityFor(strategy:)")));
- (DyrectoSharedAssistantInstruction * _Nullable)selectMatch:(DyrectoSharedReferenceMatchResult *)match __attribute__((swift_name("select(match:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InstructionTemplates")))
@interface DyrectoSharedInstructionTemplates : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)instructionTemplates __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedInstructionTemplates *shared __attribute__((swift_name("shared")));
- (NSString *)adverbSeverity:(DyrectoSharedPerceptualSeverity *)severity __attribute__((swift_name("adverb(severity:)")));
- (NSString *)templateAction:(DyrectoSharedAssistantAction *)action __attribute__((swift_name("template(action:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InstructionTranslator")))
@interface DyrectoSharedInstructionTranslator : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)instructionTranslator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedInstructionTranslator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceMatchResult *)annotateMatch:(DyrectoSharedReferenceMatchResult *)match __attribute__((swift_name("annotate(match:)")));
- (DyrectoSharedAssistantInstruction * _Nullable)translateSignal:(DyrectoSharedReferenceSignal *)signal result:(DyrectoSharedReferenceSignalResult *)result __attribute__((swift_name("translate(signal:result:)")));
@property (readonly) float LOW_DIRECTION_CONFIDENCE __attribute__((swift_name("LOW_DIRECTION_CONFIDENCE")));
@end

__attribute__((swift_name("PerceptualEvaluator")))
@protocol DyrectoSharedPerceptualEvaluator
@required
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompositionEvaluator")))
@interface DyrectoSharedCompositionEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)compositionEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCompositionEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativePriorityMapper")))
@interface DyrectoSharedCreativePriorityMapper : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)creativePriorityMapper __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativePriorityMapper *shared __attribute__((swift_name("shared")));
- (float)multiplierForSignal:(DyrectoSharedReferenceSignal *)signal model:(DyrectoSharedCreativeSceneModel *)model __attribute__((swift_name("multiplierFor(signal:model:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CurveSpec")))
@interface DyrectoSharedCurveSpec : DyrectoSharedBase
- (instancetype)initWithType:(DyrectoSharedCurveSpecCurveType *)type gamma:(float)gamma breakpoints:(NSArray<DyrectoSharedKotlinPair<DyrectoSharedFloat *, DyrectoSharedFloat *> *> *)breakpoints __attribute__((swift_name("init(type:gamma:breakpoints:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCurveSpec *)doCopyType:(DyrectoSharedCurveSpecCurveType *)type gamma:(float)gamma breakpoints:(NSArray<DyrectoSharedKotlinPair<DyrectoSharedFloat *, DyrectoSharedFloat *> *> *)breakpoints __attribute__((swift_name("doCopy(type:gamma:breakpoints:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedKotlinPair<DyrectoSharedFloat *, DyrectoSharedFloat *> *> *breakpoints __attribute__((swift_name("breakpoints")));
@property (readonly) float gamma __attribute__((swift_name("gamma")));
@property (readonly) DyrectoSharedCurveSpecCurveType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CurveSpec.CurveType")))
@interface DyrectoSharedCurveSpecCurveType : DyrectoSharedKotlinEnum<DyrectoSharedCurveSpecCurveType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCurveSpecCurveType *linear __attribute__((swift_name("linear")));
@property (class, readonly) DyrectoSharedCurveSpecCurveType *smoothstep __attribute__((swift_name("smoothstep")));
@property (class, readonly) DyrectoSharedCurveSpecCurveType *power __attribute__((swift_name("power")));
@property (class, readonly) DyrectoSharedCurveSpecCurveType *piecewise __attribute__((swift_name("piecewise")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCurveSpecCurveType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCurveSpecCurveType *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureEvaluator")))
@interface DyrectoSharedExposureEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)exposureEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedExposureEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HeadroomEvaluator")))
@interface DyrectoSharedHeadroomEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)headroomEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedHeadroomEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HistogramSignature")))
@interface DyrectoSharedHistogramSignature : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)histogramSignature __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedHistogramSignature *shared __attribute__((swift_name("shared")));
- (DyrectoSharedFloat * _Nullable)divergenceA:(NSArray<DyrectoSharedFloat *> *)a b:(NSArray<DyrectoSharedFloat *> *)b __attribute__((swift_name("divergence(a:b:)")));
- (NSArray<DyrectoSharedFloat *> *)fromBinsBins:(DyrectoSharedKotlinIntArray *)bins signatureBins:(int32_t)signatureBins __attribute__((swift_name("fromBins(bins:signatureBins:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HumanPerceptionEngine")))
@interface DyrectoSharedHumanPerceptionEngine : DyrectoSharedBase
- (instancetype)initWithEvaluators:(NSDictionary<DyrectoSharedReferenceSignal *, id<DyrectoSharedPerceptualEvaluator>> *)evaluators __attribute__((swift_name("init(evaluators:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceMatchResult *)evaluateRaw:(DyrectoSharedReferenceMatchResult *)raw reference:(DyrectoSharedReferenceProfile *)reference input:(DyrectoSharedCurrentReferenceInput *)input liveSubjectExposure:(DyrectoSharedSubjectExposureStats * _Nullable)liveSubjectExposure tuning:(DyrectoSharedPerceptualTuning *)tuning __attribute__((swift_name("evaluate(raw:reference:input:liveSubjectExposure:tuning:)")));
- (void)reset __attribute__((swift_name("reset()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HysteresisState")))
@interface DyrectoSharedHysteresisState : DyrectoSharedKotlinEnum<DyrectoSharedHysteresisState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedHysteresisState *inside __attribute__((swift_name("inside")));
@property (class, readonly) DyrectoSharedHysteresisState *entered __attribute__((swift_name("entered")));
@property (class, readonly) DyrectoSharedHysteresisState *holdingLeave __attribute__((swift_name("holdingLeave")));
+ (DyrectoSharedKotlinArray<DyrectoSharedHysteresisState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedHysteresisState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceivedMatchBucket")))
@interface DyrectoSharedPerceivedMatchBucket : DyrectoSharedKotlinEnum<DyrectoSharedPerceivedMatchBucket *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *perfect __attribute__((swift_name("perfect")));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *identical __attribute__((swift_name("identical")));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *veryClose __attribute__((swift_name("veryClose")));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *slightlyDifferent __attribute__((swift_name("slightlyDifferent")));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *noticeablyDifferent __attribute__((swift_name("noticeablyDifferent")));
@property (class, readonly) DyrectoSharedPerceivedMatchBucket *different __attribute__((swift_name("different")));
+ (DyrectoSharedKotlinArray<DyrectoSharedPerceivedMatchBucket *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedPerceivedMatchBucket *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualApplicability")))
@interface DyrectoSharedPerceptualApplicability : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)perceptualApplicability __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualApplicability *shared __attribute__((swift_name("shared")));
- (float)strategyWeightStrategy:(DyrectoSharedComparisonStrategy *)strategy signal:(DyrectoSharedReferenceSignal *)signal weights:(NSDictionary<DyrectoSharedComparisonStrategy *, NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *> *)weights __attribute__((swift_name("strategyWeight(strategy:signal:weights:)")));
@property (readonly) NSDictionary<DyrectoSharedComparisonStrategy *, NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *> *DEFAULT_WEIGHTS __attribute__((swift_name("DEFAULT_WEIGHTS")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualConfidence")))
@interface DyrectoSharedPerceptualConfidence : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)perceptualConfidence __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualConfidence *shared __attribute__((swift_name("shared")));
- (float)combineFeatureConfidences:(DyrectoSharedKotlinArray<DyrectoSharedFloat *> *)featureConfidences __attribute__((swift_name("combine(featureConfidences:)")));
- (BOOL)isUsableConfidence:(float)confidence profile:(DyrectoSharedPerceptualThresholdsProfile *)profile __attribute__((swift_name("isUsable(confidence:profile:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualContext")))
@interface DyrectoSharedPerceptualContext : DyrectoSharedBase
- (instancetype)initWithSignal:(DyrectoSharedReferenceSignal *)signal raw:(DyrectoSharedReferenceSignalResult *)raw reference:(DyrectoSharedReferenceProfile *)reference input:(DyrectoSharedCurrentReferenceInput *)input liveSubjectExposure:(DyrectoSharedSubjectExposureStats * _Nullable)liveSubjectExposure strategy:(DyrectoSharedComparisonStrategy *)strategy tuning:(DyrectoSharedPerceptualTuning *)tuning __attribute__((swift_name("init(signal:raw:reference:input:liveSubjectExposure:strategy:tuning:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualContext *)doCopySignal:(DyrectoSharedReferenceSignal *)signal raw:(DyrectoSharedReferenceSignalResult *)raw reference:(DyrectoSharedReferenceProfile *)reference input:(DyrectoSharedCurrentReferenceInput *)input liveSubjectExposure:(DyrectoSharedSubjectExposureStats * _Nullable)liveSubjectExposure strategy:(DyrectoSharedComparisonStrategy *)strategy tuning:(DyrectoSharedPerceptualTuning *)tuning __attribute__((swift_name("doCopy(signal:raw:reference:input:liveSubjectExposure:strategy:tuning:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCurrentReferenceInput *input __attribute__((swift_name("input")));
@property (readonly) DyrectoSharedSubjectExposureStats * _Nullable liveSubjectExposure __attribute__((swift_name("liveSubjectExposure")));
@property (readonly) DyrectoSharedReferenceSignalResult *raw __attribute__((swift_name("raw")));
@property (readonly) DyrectoSharedReferenceProfile *reference __attribute__((swift_name("reference")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@property (readonly) DyrectoSharedComparisonStrategy *strategy __attribute__((swift_name("strategy")));
@property (readonly) DyrectoSharedPerceptualTuning *tuning __attribute__((swift_name("tuning")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualDifference")))
@interface DyrectoSharedPerceptualDifference : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)perceptualDifference __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualDifference *shared __attribute__((swift_name("shared")));
- (float)applyCurveP:(float)p curve:(DyrectoSharedCurveSpec *)curve __attribute__((swift_name("applyCurve(p:curve:)")));
- (float)importanceOfNoticeability:(float)noticeability strategyWeight:(float)strategyWeight salience:(float)salience gamma:(float)gamma referenceWeight:(float)referenceWeight __attribute__((swift_name("importanceOf(noticeability:strategyWeight:salience:gamma:referenceWeight:)")));
- (float)normalizeRaw:(float)raw deadZone:(float)deadZone saturation:(float)saturation __attribute__((swift_name("normalize(raw:deadZone:saturation:)")));
- (DyrectoSharedPerceptualSeverity *)severityOfNoticeability:(float)noticeability cutoffs:(DyrectoSharedKotlinFloatArray *)cutoffs __attribute__((swift_name("severityOf(noticeability:cutoffs:)")));
- (float)weightedCompositeComponents:(NSArray<DyrectoSharedKotlinPair<DyrectoSharedFloat *, DyrectoSharedFloat *> *> *)components __attribute__((swift_name("weightedComposite(components:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualMessages")))
@interface DyrectoSharedPerceptualMessages : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)perceptualMessages __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualMessages *shared __attribute__((swift_name("shared")));
- (NSString *)messageSignal:(DyrectoSharedReferenceSignal *)signal direction:(DyrectoSharedReferenceDriftDirection *)direction severity:(DyrectoSharedPerceptualSeverity *)severity __attribute__((swift_name("message(signal:direction:severity:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualObservation")))
@interface DyrectoSharedPerceptualObservation : DyrectoSharedBase
- (instancetype)initWithAvailable:(BOOL)available rawDifference:(float)rawDifference perceptualDifference:(float)perceptualDifference noticeability:(float)noticeability confidence:(float)confidence directionConfidence:(float)directionConfidence deadZoneApplied:(float)deadZoneApplied direction:(DyrectoSharedReferenceDriftDirection *)direction matchBucket:(DyrectoSharedPerceivedMatchBucket * _Nullable)matchBucket components:(NSDictionary<NSString *, DyrectoSharedFloat *> *)components __attribute__((swift_name("init(available:rawDifference:perceptualDifference:noticeability:confidence:directionConfidence:deadZoneApplied:direction:matchBucket:components:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedPerceptualObservationCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedPerceptualObservation *)doCopyAvailable:(BOOL)available rawDifference:(float)rawDifference perceptualDifference:(float)perceptualDifference noticeability:(float)noticeability confidence:(float)confidence directionConfidence:(float)directionConfidence deadZoneApplied:(float)deadZoneApplied direction:(DyrectoSharedReferenceDriftDirection *)direction matchBucket:(DyrectoSharedPerceivedMatchBucket * _Nullable)matchBucket components:(NSDictionary<NSString *, DyrectoSharedFloat *> *)components __attribute__((swift_name("doCopy(available:rawDifference:perceptualDifference:noticeability:confidence:directionConfidence:deadZoneApplied:direction:matchBucket:components:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL available __attribute__((swift_name("available")));
@property (readonly) NSDictionary<NSString *, DyrectoSharedFloat *> *components __attribute__((swift_name("components")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) float deadZoneApplied __attribute__((swift_name("deadZoneApplied")));
@property (readonly) DyrectoSharedReferenceDriftDirection *direction __attribute__((swift_name("direction")));
@property (readonly) float directionConfidence __attribute__((swift_name("directionConfidence")));
@property (readonly) DyrectoSharedPerceivedMatchBucket * _Nullable matchBucket __attribute__((swift_name("matchBucket")));
@property (readonly) float noticeability __attribute__((swift_name("noticeability")));
@property (readonly) float perceptualDifference __attribute__((swift_name("perceptualDifference")));
@property (readonly) float rawDifference __attribute__((swift_name("rawDifference")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualObservation.Companion")))
@interface DyrectoSharedPerceptualObservationCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualObservationCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedPerceptualObservation *UNAVAILABLE __attribute__((swift_name("UNAVAILABLE")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualSeverity")))
@interface DyrectoSharedPerceptualSeverity : DyrectoSharedKotlinEnum<DyrectoSharedPerceptualSeverity *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedPerceptualSeverity *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedPerceptualSeverity *subtle __attribute__((swift_name("subtle")));
@property (class, readonly) DyrectoSharedPerceptualSeverity *noticeable __attribute__((swift_name("noticeable")));
@property (class, readonly) DyrectoSharedPerceptualSeverity *obvious __attribute__((swift_name("obvious")));
@property (class, readonly) DyrectoSharedPerceptualSeverity *severe __attribute__((swift_name("severe")));
+ (DyrectoSharedKotlinArray<DyrectoSharedPerceptualSeverity *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedPerceptualSeverity *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualSignal")))
@interface DyrectoSharedPerceptualSignal : DyrectoSharedBase
- (instancetype)initWithRawDifference:(float)rawDifference perceptualDifference:(float)perceptualDifference humanNoticeability:(float)humanNoticeability importance:(float)importance confidence:(float)confidence directionConfidence:(float)directionConfidence trend:(DyrectoSharedPerceptualTrend *)trend deadZoneApplied:(float)deadZoneApplied hysteresisState:(DyrectoSharedHysteresisState *)hysteresisState severity:(DyrectoSharedPerceptualSeverity *)severity strategyWeight:(float)strategyWeight perceptuallyDrifting:(BOOL)perceptuallyDrifting direction:(DyrectoSharedReferenceDriftDirection *)direction matchBucket:(DyrectoSharedPerceivedMatchBucket * _Nullable)matchBucket components:(NSDictionary<NSString *, DyrectoSharedFloat *> *)components __attribute__((swift_name("init(rawDifference:perceptualDifference:humanNoticeability:importance:confidence:directionConfidence:trend:deadZoneApplied:hysteresisState:severity:strategyWeight:perceptuallyDrifting:direction:matchBucket:components:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualSignal *)doCopyRawDifference:(float)rawDifference perceptualDifference:(float)perceptualDifference humanNoticeability:(float)humanNoticeability importance:(float)importance confidence:(float)confidence directionConfidence:(float)directionConfidence trend:(DyrectoSharedPerceptualTrend *)trend deadZoneApplied:(float)deadZoneApplied hysteresisState:(DyrectoSharedHysteresisState *)hysteresisState severity:(DyrectoSharedPerceptualSeverity *)severity strategyWeight:(float)strategyWeight perceptuallyDrifting:(BOOL)perceptuallyDrifting direction:(DyrectoSharedReferenceDriftDirection *)direction matchBucket:(DyrectoSharedPerceivedMatchBucket * _Nullable)matchBucket components:(NSDictionary<NSString *, DyrectoSharedFloat *> *)components __attribute__((swift_name("doCopy(rawDifference:perceptualDifference:humanNoticeability:importance:confidence:directionConfidence:trend:deadZoneApplied:hysteresisState:severity:strategyWeight:perceptuallyDrifting:direction:matchBucket:components:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<NSString *, DyrectoSharedFloat *> *components __attribute__((swift_name("components")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) float deadZoneApplied __attribute__((swift_name("deadZoneApplied")));
@property (readonly) DyrectoSharedReferenceDriftDirection *direction __attribute__((swift_name("direction")));
@property (readonly) float directionConfidence __attribute__((swift_name("directionConfidence")));
@property (readonly) float humanNoticeability __attribute__((swift_name("humanNoticeability")));
@property (readonly) DyrectoSharedHysteresisState *hysteresisState __attribute__((swift_name("hysteresisState")));
@property (readonly) float importance __attribute__((swift_name("importance")));
@property (readonly) DyrectoSharedPerceivedMatchBucket * _Nullable matchBucket __attribute__((swift_name("matchBucket")));
@property (readonly) float perceptualDifference __attribute__((swift_name("perceptualDifference")));
@property (readonly) BOOL perceptuallyDrifting __attribute__((swift_name("perceptuallyDrifting")));
@property (readonly) float rawDifference __attribute__((swift_name("rawDifference")));
@property (readonly) DyrectoSharedPerceptualSeverity *severity __attribute__((swift_name("severity")));
@property (readonly) float strategyWeight __attribute__((swift_name("strategyWeight")));
@property (readonly) DyrectoSharedPerceptualTrend *trend __attribute__((swift_name("trend")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualThresholds")))
@interface DyrectoSharedPerceptualThresholds : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)perceptualThresholds __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualThresholds *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualThresholdsProfile *)profileForTolerance:(DyrectoSharedReferenceTolerance *)tolerance __attribute__((swift_name("profileFor(tolerance:)")));
@property (readonly) NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedPerceptualThresholdsExposureWeights *> *DEFAULT_EXPOSURE_WEIGHTS __attribute__((swift_name("DEFAULT_EXPOSURE_WEIGHTS")));
@property (readonly) int32_t HISTOGRAM_SIGNATURE_BINS __attribute__((swift_name("HISTOGRAM_SIGNATURE_BINS")));
@property (readonly) DyrectoSharedPerceptualThresholdsProfile *LOOSE __attribute__((swift_name("LOOSE")));
@property (readonly) DyrectoSharedPerceptualThresholdsProfile *MEDIUM __attribute__((swift_name("MEDIUM")));
@property (readonly) DyrectoSharedPerceptualThresholdsProfile *STRICT __attribute__((swift_name("STRICT")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualThresholds.ExposureWeights")))
@interface DyrectoSharedPerceptualThresholdsExposureWeights : DyrectoSharedBase
- (instancetype)initWithMean:(float)mean median:(float)median p95:(float)p95 p99:(float)p99 highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage histogramDivergence:(float)histogramDivergence subjectExposure:(float)subjectExposure __attribute__((swift_name("init(mean:median:p95:p99:highlightCoverage:shadowCoverage:histogramDivergence:subjectExposure:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualThresholdsExposureWeights *)doCopyMean:(float)mean median:(float)median p95:(float)p95 p99:(float)p99 highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage histogramDivergence:(float)histogramDivergence subjectExposure:(float)subjectExposure __attribute__((swift_name("doCopy(mean:median:p95:p99:highlightCoverage:shadowCoverage:histogramDivergence:subjectExposure:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float highlightCoverage __attribute__((swift_name("highlightCoverage")));
@property (readonly) float histogramDivergence __attribute__((swift_name("histogramDivergence")));
@property (readonly) float mean __attribute__((swift_name("mean")));
@property (readonly) float median __attribute__((swift_name("median")));
@property (readonly) float p95 __attribute__((swift_name("p95")));
@property (readonly) float p99 __attribute__((swift_name("p99")));
@property (readonly) float shadowCoverage __attribute__((swift_name("shadowCoverage")));
@property (readonly) float subjectExposure __attribute__((swift_name("subjectExposure")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualThresholds.Profile")))
@interface DyrectoSharedPerceptualThresholdsProfile : DyrectoSharedBase
- (instancetype)initWithExposureMean:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureMean exposureMedian:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureMedian exposureP95:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureP95 exposureP99:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureP99 highlightCoverage:(DyrectoSharedPerceptualThresholdsSignalBand *)highlightCoverage shadowCoverage:(DyrectoSharedPerceptualThresholdsSignalBand *)shadowCoverage histogramDivergence:(DyrectoSharedPerceptualThresholdsSignalBand *)histogramDivergence warmth:(DyrectoSharedPerceptualThresholdsSignalBand *)warmth position:(DyrectoSharedPerceptualThresholdsSignalBand *)position size:(DyrectoSharedPerceptualThresholdsSignalBand *)size headroom:(DyrectoSharedPerceptualThresholdsSignalBand *)headroom composition:(DyrectoSharedPerceptualThresholdsSignalBand *)composition similarityCosineBounds:(NSArray<DyrectoSharedFloat *> *)similarityCosineBounds similarityBucketNoticeability:(NSArray<DyrectoSharedFloat *> *)similarityBucketNoticeability enterNoticeability:(float)enterNoticeability leaveNoticeability:(float)leaveNoticeability minConfidence:(float)minConfidence severityCutoffs:(DyrectoSharedKotlinFloatArray *)severityCutoffs presenceNoticeability:(float)presenceNoticeability salience:(NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *)salience importanceGamma:(float)importanceGamma curves:(NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedCurveSpec *> *)curves compositionDirectionConfidence:(float)compositionDirectionConfidence similarityDirectionConfidence:(float)similarityDirectionConfidence historyWindow:(int32_t)historyWindow spikeDeltaThreshold:(float)spikeDeltaThreshold spikeDampingFactor:(float)spikeDampingFactor trendDelta:(float)trendDelta oscillationMinFlips:(int32_t)oscillationMinFlips oscillationDirectionDecay:(float)oscillationDirectionDecay positionDeadZoneFactors:(NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedFloat *> *)positionDeadZoneFactors subjectHighlightLumaMin:(int32_t)subjectHighlightLumaMin subjectShadowLumaMax:(int32_t)subjectShadowLumaMax __attribute__((swift_name("init(exposureMean:exposureMedian:exposureP95:exposureP99:highlightCoverage:shadowCoverage:histogramDivergence:warmth:position:size:headroom:composition:similarityCosineBounds:similarityBucketNoticeability:enterNoticeability:leaveNoticeability:minConfidence:severityCutoffs:presenceNoticeability:salience:importanceGamma:curves:compositionDirectionConfidence:similarityDirectionConfidence:historyWindow:spikeDeltaThreshold:spikeDampingFactor:trendDelta:oscillationMinFlips:oscillationDirectionDecay:positionDeadZoneFactors:subjectHighlightLumaMin:subjectShadowLumaMax:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualThresholdsProfile *)doCopyExposureMean:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureMean exposureMedian:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureMedian exposureP95:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureP95 exposureP99:(DyrectoSharedPerceptualThresholdsSignalBand *)exposureP99 highlightCoverage:(DyrectoSharedPerceptualThresholdsSignalBand *)highlightCoverage shadowCoverage:(DyrectoSharedPerceptualThresholdsSignalBand *)shadowCoverage histogramDivergence:(DyrectoSharedPerceptualThresholdsSignalBand *)histogramDivergence warmth:(DyrectoSharedPerceptualThresholdsSignalBand *)warmth position:(DyrectoSharedPerceptualThresholdsSignalBand *)position size:(DyrectoSharedPerceptualThresholdsSignalBand *)size headroom:(DyrectoSharedPerceptualThresholdsSignalBand *)headroom composition:(DyrectoSharedPerceptualThresholdsSignalBand *)composition similarityCosineBounds:(NSArray<DyrectoSharedFloat *> *)similarityCosineBounds similarityBucketNoticeability:(NSArray<DyrectoSharedFloat *> *)similarityBucketNoticeability enterNoticeability:(float)enterNoticeability leaveNoticeability:(float)leaveNoticeability minConfidence:(float)minConfidence severityCutoffs:(DyrectoSharedKotlinFloatArray *)severityCutoffs presenceNoticeability:(float)presenceNoticeability salience:(NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *)salience importanceGamma:(float)importanceGamma curves:(NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedCurveSpec *> *)curves compositionDirectionConfidence:(float)compositionDirectionConfidence similarityDirectionConfidence:(float)similarityDirectionConfidence historyWindow:(int32_t)historyWindow spikeDeltaThreshold:(float)spikeDeltaThreshold spikeDampingFactor:(float)spikeDampingFactor trendDelta:(float)trendDelta oscillationMinFlips:(int32_t)oscillationMinFlips oscillationDirectionDecay:(float)oscillationDirectionDecay positionDeadZoneFactors:(NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedFloat *> *)positionDeadZoneFactors subjectHighlightLumaMin:(int32_t)subjectHighlightLumaMin subjectShadowLumaMax:(int32_t)subjectShadowLumaMax __attribute__((swift_name("doCopy(exposureMean:exposureMedian:exposureP95:exposureP99:highlightCoverage:shadowCoverage:histogramDivergence:warmth:position:size:headroom:composition:similarityCosineBounds:similarityBucketNoticeability:enterNoticeability:leaveNoticeability:minConfidence:severityCutoffs:presenceNoticeability:salience:importanceGamma:curves:compositionDirectionConfidence:similarityDirectionConfidence:historyWindow:spikeDeltaThreshold:spikeDampingFactor:trendDelta:oscillationMinFlips:oscillationDirectionDecay:positionDeadZoneFactors:subjectHighlightLumaMin:subjectShadowLumaMax:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *composition __attribute__((swift_name("composition")));
@property (readonly) float compositionDirectionConfidence __attribute__((swift_name("compositionDirectionConfidence")));
@property (readonly) NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedCurveSpec *> *curves __attribute__((swift_name("curves")));
@property (readonly) float enterNoticeability __attribute__((swift_name("enterNoticeability")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *exposureMean __attribute__((swift_name("exposureMean")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *exposureMedian __attribute__((swift_name("exposureMedian")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *exposureP95 __attribute__((swift_name("exposureP95")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *exposureP99 __attribute__((swift_name("exposureP99")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *headroom __attribute__((swift_name("headroom")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *highlightCoverage __attribute__((swift_name("highlightCoverage")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *histogramDivergence __attribute__((swift_name("histogramDivergence")));
@property (readonly) int32_t historyWindow __attribute__((swift_name("historyWindow")));
@property (readonly) float importanceGamma __attribute__((swift_name("importanceGamma")));
@property (readonly) float leaveNoticeability __attribute__((swift_name("leaveNoticeability")));
@property (readonly) float minConfidence __attribute__((swift_name("minConfidence")));
@property (readonly) float oscillationDirectionDecay __attribute__((swift_name("oscillationDirectionDecay")));
@property (readonly) int32_t oscillationMinFlips __attribute__((swift_name("oscillationMinFlips")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *position __attribute__((swift_name("position")));
@property (readonly) NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedFloat *> *positionDeadZoneFactors __attribute__((swift_name("positionDeadZoneFactors")));
@property (readonly) float presenceNoticeability __attribute__((swift_name("presenceNoticeability")));
@property (readonly) NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *salience __attribute__((swift_name("salience")));
@property (readonly) DyrectoSharedKotlinFloatArray *severityCutoffs __attribute__((swift_name("severityCutoffs")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *shadowCoverage __attribute__((swift_name("shadowCoverage")));
@property (readonly) NSArray<DyrectoSharedFloat *> *similarityBucketNoticeability __attribute__((swift_name("similarityBucketNoticeability")));
@property (readonly) NSArray<DyrectoSharedFloat *> *similarityCosineBounds __attribute__((swift_name("similarityCosineBounds")));
@property (readonly) float similarityDirectionConfidence __attribute__((swift_name("similarityDirectionConfidence")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *size __attribute__((swift_name("size")));
@property (readonly) float spikeDampingFactor __attribute__((swift_name("spikeDampingFactor")));
@property (readonly) float spikeDeltaThreshold __attribute__((swift_name("spikeDeltaThreshold")));
@property (readonly) int32_t subjectHighlightLumaMin __attribute__((swift_name("subjectHighlightLumaMin")));
@property (readonly) int32_t subjectShadowLumaMax __attribute__((swift_name("subjectShadowLumaMax")));
@property (readonly) float trendDelta __attribute__((swift_name("trendDelta")));
@property (readonly) DyrectoSharedPerceptualThresholdsSignalBand *warmth __attribute__((swift_name("warmth")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualThresholds.SignalBand")))
@interface DyrectoSharedPerceptualThresholdsSignalBand : DyrectoSharedBase
- (instancetype)initWithDeadZone:(float)deadZone saturation:(float)saturation __attribute__((swift_name("init(deadZone:saturation:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualThresholdsSignalBand *)doCopyDeadZone:(float)deadZone saturation:(float)saturation __attribute__((swift_name("doCopy(deadZone:saturation:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float deadZone __attribute__((swift_name("deadZone")));
@property (readonly) float saturation __attribute__((swift_name("saturation")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualTrend")))
@interface DyrectoSharedPerceptualTrend : DyrectoSharedKotlinEnum<DyrectoSharedPerceptualTrend *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedPerceptualTrend *stable __attribute__((swift_name("stable")));
@property (class, readonly) DyrectoSharedPerceptualTrend *increasing __attribute__((swift_name("increasing")));
@property (class, readonly) DyrectoSharedPerceptualTrend *decreasing __attribute__((swift_name("decreasing")));
@property (class, readonly) DyrectoSharedPerceptualTrend *oscillating __attribute__((swift_name("oscillating")));
+ (DyrectoSharedKotlinArray<DyrectoSharedPerceptualTrend *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedPerceptualTrend *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualTuning")))
@interface DyrectoSharedPerceptualTuning : DyrectoSharedBase
- (instancetype)initWithProfile:(DyrectoSharedPerceptualThresholdsProfile *)profile strategyWeights:(NSDictionary<DyrectoSharedComparisonStrategy *, NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *> *)strategyWeights exposureWeights:(NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedPerceptualThresholdsExposureWeights *> *)exposureWeights __attribute__((swift_name("init(profile:strategyWeights:exposureWeights:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedPerceptualTuningCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedPerceptualTuning *)doCopyProfile:(DyrectoSharedPerceptualThresholdsProfile *)profile strategyWeights:(NSDictionary<DyrectoSharedComparisonStrategy *, NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *> *)strategyWeights exposureWeights:(NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedPerceptualThresholdsExposureWeights *> *)exposureWeights __attribute__((swift_name("doCopy(profile:strategyWeights:exposureWeights:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<DyrectoSharedComparisonStrategy *, DyrectoSharedPerceptualThresholdsExposureWeights *> *exposureWeights __attribute__((swift_name("exposureWeights")));
@property (readonly) DyrectoSharedPerceptualThresholdsProfile *profile __attribute__((swift_name("profile")));
@property (readonly) NSDictionary<DyrectoSharedComparisonStrategy *, NSDictionary<DyrectoSharedReferenceSignal *, DyrectoSharedFloat *> *> *strategyWeights __attribute__((swift_name("strategyWeights")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualTuning.Companion")))
@interface DyrectoSharedPerceptualTuningCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPerceptualTuningCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualTuning *)forToleranceTolerance:(DyrectoSharedReferenceTolerance *)tolerance __attribute__((swift_name("forTolerance(tolerance:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PositionEvaluator")))
@interface DyrectoSharedPositionEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)positionEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPositionEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PresenceEvaluator")))
@interface DyrectoSharedPresenceEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
- (instancetype)initWithSignal:(DyrectoSharedReferenceSignal *)signal __attribute__((swift_name("init(signal:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SimilarityEvaluator")))
@interface DyrectoSharedSimilarityEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)similarityEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSimilarityEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SizeEvaluator")))
@interface DyrectoSharedSizeEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sizeEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSizeEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WhiteBalanceEvaluator")))
@interface DyrectoSharedWhiteBalanceEvaluator : DyrectoSharedBase <DyrectoSharedPerceptualEvaluator>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)whiteBalanceEvaluator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedWhiteBalanceEvaluator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedPerceptualObservation *)evaluateCtx:(DyrectoSharedPerceptualContext *)ctx __attribute__((swift_name("evaluate(ctx:)")));
@property (readonly) DyrectoSharedReferenceSignal *signal __attribute__((swift_name("signal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CurrentReferenceInput")))
@interface DyrectoSharedCurrentReferenceInput : DyrectoSharedBase
- (instancetype)initWithExposure:(DyrectoSharedExposureResult * _Nullable)exposure histogram:(DyrectoSharedHistogramResult * _Nullable)histogram zebra:(DyrectoSharedZebraResult * _Nullable)zebra faces:(DyrectoSharedFaceDetectionResult * _Nullable)faces eyes:(DyrectoSharedEyeDetectionResult * _Nullable)eyes colorStats:(DyrectoSharedColorStatsResult * _Nullable)colorStats scene:(DyrectoSharedSceneContext * _Nullable)scene sceneSnapshot:(DyrectoSharedSceneSnapshot * _Nullable)sceneSnapshot referenceEmbedding:(DyrectoSharedReferenceEmbedding * _Nullable)referenceEmbedding __attribute__((swift_name("init(exposure:histogram:zebra:faces:eyes:colorStats:scene:sceneSnapshot:referenceEmbedding:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCurrentReferenceInput *)doCopyExposure:(DyrectoSharedExposureResult * _Nullable)exposure histogram:(DyrectoSharedHistogramResult * _Nullable)histogram zebra:(DyrectoSharedZebraResult * _Nullable)zebra faces:(DyrectoSharedFaceDetectionResult * _Nullable)faces eyes:(DyrectoSharedEyeDetectionResult * _Nullable)eyes colorStats:(DyrectoSharedColorStatsResult * _Nullable)colorStats scene:(DyrectoSharedSceneContext * _Nullable)scene sceneSnapshot:(DyrectoSharedSceneSnapshot * _Nullable)sceneSnapshot referenceEmbedding:(DyrectoSharedReferenceEmbedding * _Nullable)referenceEmbedding __attribute__((swift_name("doCopy(exposure:histogram:zebra:faces:eyes:colorStats:scene:sceneSnapshot:referenceEmbedding:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedColorStatsResult * _Nullable colorStats __attribute__((swift_name("colorStats")));
@property (readonly) DyrectoSharedExposureResult * _Nullable exposure __attribute__((swift_name("exposure")));
@property (readonly) DyrectoSharedEyeDetectionResult * _Nullable eyes __attribute__((swift_name("eyes")));
@property (readonly) DyrectoSharedFaceDetectionResult * _Nullable faces __attribute__((swift_name("faces")));
@property (readonly) DyrectoSharedHistogramResult * _Nullable histogram __attribute__((swift_name("histogram")));
@property (readonly) DyrectoSharedReferenceEmbedding * _Nullable referenceEmbedding __attribute__((swift_name("referenceEmbedding")));
@property (readonly) DyrectoSharedSceneContext * _Nullable scene __attribute__((swift_name("scene")));
@property (readonly) DyrectoSharedSceneSnapshot * _Nullable sceneSnapshot __attribute__((swift_name("sceneSnapshot")));
@property (readonly) DyrectoSharedZebraResult * _Nullable zebra __attribute__((swift_name("zebra")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NormalizedRect")))
@interface DyrectoSharedNormalizedRect : DyrectoSharedBase
- (instancetype)initWithLeft:(float)left top:(float)top right:(float)right bottom:(float)bottom __attribute__((swift_name("init(left:top:right:bottom:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedNormalizedRectCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedNormalizedRect *)doCopyLeft:(float)left top:(float)top right:(float)right bottom:(float)bottom __attribute__((swift_name("doCopy(left:top:right:bottom:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float area __attribute__((swift_name("area")));
@property (readonly) float bottom __attribute__((swift_name("bottom")));
@property (readonly) float centerX __attribute__((swift_name("centerX")));
@property (readonly) float centerY __attribute__((swift_name("centerY")));
@property (readonly) float height __attribute__((swift_name("height")));
@property (readonly) float left __attribute__((swift_name("left")));
@property (readonly) float right __attribute__((swift_name("right")));
@property (readonly) float top __attribute__((swift_name("top")));
@property (readonly) float width __attribute__((swift_name("width")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NormalizedRect.Companion")))
@interface DyrectoSharedNormalizedRectCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedNormalizedRectCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAiProfile")))
@interface DyrectoSharedReferenceAiProfile : DyrectoSharedBase
- (instancetype)initWithSceneMode:(DyrectoSharedSceneMode *)sceneMode sceneModeConfidence:(float)sceneModeConfidence strategy:(DyrectoSharedComparisonStrategy *)strategy primarySubjectType:(DyrectoSharedPrimarySubjectType *)primarySubjectType primarySubjectCategory:(DyrectoSharedSubjectCategory *)primarySubjectCategory primarySubjectRawLabel:(NSString * _Nullable)primarySubjectRawLabel primarySubjectConfidence:(float)primarySubjectConfidence primarySubjectBox:(DyrectoSharedNormalizedRect * _Nullable)primarySubjectBox subjects:(NSArray<DyrectoSharedReferenceAiSubject *> *)subjects compositionSignature:(NSArray<DyrectoSharedFloat *> *)compositionSignature segmentationCoverage:(DyrectoSharedFloat * _Nullable)segmentationCoverage segmentationPixelAccurate:(BOOL)segmentationPixelAccurate embeddingId:(NSString * _Nullable)embeddingId embeddingModelId:(NSString * _Nullable)embeddingModelId schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("init(sceneMode:sceneModeConfidence:strategy:primarySubjectType:primarySubjectCategory:primarySubjectRawLabel:primarySubjectConfidence:primarySubjectBox:subjects:compositionSignature:segmentationCoverage:segmentationPixelAccurate:embeddingId:embeddingModelId:schemaVersion:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceAiProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceAiProfile *)doCopySceneMode:(DyrectoSharedSceneMode *)sceneMode sceneModeConfidence:(float)sceneModeConfidence strategy:(DyrectoSharedComparisonStrategy *)strategy primarySubjectType:(DyrectoSharedPrimarySubjectType *)primarySubjectType primarySubjectCategory:(DyrectoSharedSubjectCategory *)primarySubjectCategory primarySubjectRawLabel:(NSString * _Nullable)primarySubjectRawLabel primarySubjectConfidence:(float)primarySubjectConfidence primarySubjectBox:(DyrectoSharedNormalizedRect * _Nullable)primarySubjectBox subjects:(NSArray<DyrectoSharedReferenceAiSubject *> *)subjects compositionSignature:(NSArray<DyrectoSharedFloat *> *)compositionSignature segmentationCoverage:(DyrectoSharedFloat * _Nullable)segmentationCoverage segmentationPixelAccurate:(BOOL)segmentationPixelAccurate embeddingId:(NSString * _Nullable)embeddingId embeddingModelId:(NSString * _Nullable)embeddingModelId schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("doCopy(sceneMode:sceneModeConfidence:strategy:primarySubjectType:primarySubjectCategory:primarySubjectRawLabel:primarySubjectConfidence:primarySubjectBox:subjects:compositionSignature:segmentationCoverage:segmentationPixelAccurate:embeddingId:embeddingModelId:schemaVersion:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedFloat *> *compositionSignature __attribute__((swift_name("compositionSignature")));
@property (readonly) NSString * _Nullable embeddingId __attribute__((swift_name("embeddingId")));
@property (readonly) NSString * _Nullable embeddingModelId __attribute__((swift_name("embeddingModelId")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable primarySubjectBox __attribute__((swift_name("primarySubjectBox")));
@property (readonly) DyrectoSharedSubjectCategory *primarySubjectCategory __attribute__((swift_name("primarySubjectCategory")));
@property (readonly) float primarySubjectConfidence __attribute__((swift_name("primarySubjectConfidence")));
@property (readonly) NSString * _Nullable primarySubjectRawLabel __attribute__((swift_name("primarySubjectRawLabel")));
@property (readonly) DyrectoSharedPrimarySubjectType *primarySubjectType __attribute__((swift_name("primarySubjectType")));
@property (readonly) DyrectoSharedSceneMode *sceneMode __attribute__((swift_name("sceneMode")));
@property (readonly) float sceneModeConfidence __attribute__((swift_name("sceneModeConfidence")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@property (readonly) DyrectoSharedFloat * _Nullable segmentationCoverage __attribute__((swift_name("segmentationCoverage")));
@property (readonly) BOOL segmentationPixelAccurate __attribute__((swift_name("segmentationPixelAccurate")));
@property (readonly) DyrectoSharedComparisonStrategy *strategy __attribute__((swift_name("strategy")));
@property (readonly) NSArray<DyrectoSharedReferenceAiSubject *> *subjects __attribute__((swift_name("subjects")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAiProfile.Companion")))
@interface DyrectoSharedReferenceAiProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceAiProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAiSubject")))
@interface DyrectoSharedReferenceAiSubject : DyrectoSharedBase
- (instancetype)initWithCategory:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("init(category:rawLabel:confidence:box:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceAiSubjectCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceAiSubject *)doCopyCategory:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("doCopy(category:rawLabel:confidence:box:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect *box __attribute__((swift_name("box")));
@property (readonly) DyrectoSharedSubjectCategory *category __attribute__((swift_name("category")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) NSString * _Nullable rawLabel __attribute__((swift_name("rawLabel")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAiSubject.Companion")))
@interface DyrectoSharedReferenceAiSubjectCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceAiSubjectCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAlertRules")))
@interface DyrectoSharedReferenceAlertRules : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)referenceAlertRules __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceAlertRules *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceAlertRulesResult *)evaluateState:(DyrectoSharedReferenceAlertState *)state match:(DyrectoSharedReferenceMatchResult *)match idGen:(DyrectoSharedAlertIdGenerator *)idGen __attribute__((swift_name("evaluate(state:match:idGen:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAlertRules.Result")))
@interface DyrectoSharedReferenceAlertRulesResult : DyrectoSharedBase
- (instancetype)initWithAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedReferenceAlertState *)state __attribute__((swift_name("init(alerts:state:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceAlertRulesResult *)doCopyAlerts:(NSArray<DyrectoSharedAlert *> *)alerts state:(DyrectoSharedReferenceAlertState *)state __attribute__((swift_name("doCopy(alerts:state:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedAlert *> *alerts __attribute__((swift_name("alerts")));
@property (readonly) DyrectoSharedReferenceAlertState *state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceAlertState")))
@interface DyrectoSharedReferenceAlertState : DyrectoSharedBase
- (instancetype)initWithSeeded:(BOOL)seeded referenceId:(NSString * _Nullable)referenceId fired:(NSSet<DyrectoSharedReferenceSignal *> *)fired __attribute__((swift_name("init(seeded:referenceId:fired:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceAlertState *)doCopySeeded:(BOOL)seeded referenceId:(NSString * _Nullable)referenceId fired:(NSSet<DyrectoSharedReferenceSignal *> *)fired __attribute__((swift_name("doCopy(seeded:referenceId:fired:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSSet<DyrectoSharedReferenceSignal *> *fired __attribute__((swift_name("fired")));
@property (readonly) NSString * _Nullable referenceId __attribute__((swift_name("referenceId")));
@property (readonly) BOOL seeded __attribute__((swift_name("seeded")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceCameraSettings")))
@interface DyrectoSharedReferenceCameraSettings : DyrectoSharedBase
- (instancetype)initWithIso:(NSString * _Nullable)iso shutter:(NSString * _Nullable)shutter aperture:(NSString * _Nullable)aperture whiteBalance:(NSString * _Nullable)whiteBalance colorTemp:(NSString * _Nullable)colorTemp frameRate:(NSString * _Nullable)frameRate codec:(NSString * _Nullable)codec pictureProfile:(NSString * _Nullable)pictureProfile __attribute__((swift_name("init(iso:shutter:aperture:whiteBalance:colorTemp:frameRate:codec:pictureProfile:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceCameraSettingsCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceCameraSettings *)doCopyIso:(NSString * _Nullable)iso shutter:(NSString * _Nullable)shutter aperture:(NSString * _Nullable)aperture whiteBalance:(NSString * _Nullable)whiteBalance colorTemp:(NSString * _Nullable)colorTemp frameRate:(NSString * _Nullable)frameRate codec:(NSString * _Nullable)codec pictureProfile:(NSString * _Nullable)pictureProfile __attribute__((swift_name("doCopy(iso:shutter:aperture:whiteBalance:colorTemp:frameRate:codec:pictureProfile:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)summary __attribute__((swift_name("summary()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable aperture __attribute__((swift_name("aperture")));
@property (readonly) NSString * _Nullable codec __attribute__((swift_name("codec")));
@property (readonly) NSString * _Nullable colorTemp __attribute__((swift_name("colorTemp")));
@property (readonly) NSString * _Nullable frameRate __attribute__((swift_name("frameRate")));
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) NSString * _Nullable iso __attribute__((swift_name("iso")));
@property (readonly) NSString * _Nullable pictureProfile __attribute__((swift_name("pictureProfile")));
@property (readonly) NSString * _Nullable shutter __attribute__((swift_name("shutter")));
@property (readonly) NSString * _Nullable whiteBalance __attribute__((swift_name("whiteBalance")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceCameraSettings.Companion")))
@interface DyrectoSharedReferenceCameraSettingsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceCameraSettingsCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceCameraSettings *)fromTelemetryT:(DyrectoSharedCameraTelemetry *)t __attribute__((swift_name("fromTelemetry(t:)")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceColorProfile")))
@interface DyrectoSharedReferenceColorProfile : DyrectoSharedBase
- (instancetype)initWithAvgR:(float)avgR avgG:(float)avgG avgB:(float)avgB warmthScore:(float)warmthScore tintScore:(float)tintScore __attribute__((swift_name("init(avgR:avgG:avgB:warmthScore:tintScore:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceColorProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceColorProfile *)doCopyAvgR:(float)avgR avgG:(float)avgG avgB:(float)avgB warmthScore:(float)warmthScore tintScore:(float)tintScore __attribute__((swift_name("doCopy(avgR:avgG:avgB:warmthScore:tintScore:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float avgB __attribute__((swift_name("avgB")));
@property (readonly) float avgG __attribute__((swift_name("avgG")));
@property (readonly) float avgR __attribute__((swift_name("avgR")));
@property (readonly) float tintScore __attribute__((swift_name("tintScore")));
@property (readonly) float warmthScore __attribute__((swift_name("warmthScore")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceColorProfile.Companion")))
@interface DyrectoSharedReferenceColorProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceColorProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceConfig")))
@interface DyrectoSharedReferenceConfig : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)referenceConfig __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceConfig *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceConfigReferenceThresholds *)thresholdsForTolerance:(DyrectoSharedReferenceTolerance *)tolerance __attribute__((swift_name("thresholdsFor(tolerance:)")));
@property (readonly) int64_t AI_STALE_AFTER_MS __attribute__((swift_name("AI_STALE_AFTER_MS")));
@property (readonly) DyrectoSharedReferenceConfigReferenceThresholds *LOOSE __attribute__((swift_name("LOOSE")));
@property (readonly) DyrectoSharedReferenceConfigReferenceThresholds *MEDIUM __attribute__((swift_name("MEDIUM")));
@property (readonly) DyrectoSharedReferenceConfigReferenceThresholds *STRICT __attribute__((swift_name("STRICT")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceConfig.ReferenceThresholds")))
@interface DyrectoSharedReferenceConfigReferenceThresholds : DyrectoSharedBase
- (instancetype)initWithExposureMeanDelta:(float)exposureMeanDelta exposureP95Delta:(float)exposureP95Delta warmthDelta:(float)warmthDelta subjectPositionDelta:(float)subjectPositionDelta subjectSizeDelta:(float)subjectSizeDelta headroomDelta:(float)headroomDelta compositionDelta:(float)compositionDelta visualSimilarityMin:(float)visualSimilarityMin minFeatureConfidence:(float)minFeatureConfidence requiredPersistenceFrames:(int32_t)requiredPersistenceFrames requiredRecoveryFrames:(int32_t)requiredRecoveryFrames __attribute__((swift_name("init(exposureMeanDelta:exposureP95Delta:warmthDelta:subjectPositionDelta:subjectSizeDelta:headroomDelta:compositionDelta:visualSimilarityMin:minFeatureConfidence:requiredPersistenceFrames:requiredRecoveryFrames:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceConfigReferenceThresholds *)doCopyExposureMeanDelta:(float)exposureMeanDelta exposureP95Delta:(float)exposureP95Delta warmthDelta:(float)warmthDelta subjectPositionDelta:(float)subjectPositionDelta subjectSizeDelta:(float)subjectSizeDelta headroomDelta:(float)headroomDelta compositionDelta:(float)compositionDelta visualSimilarityMin:(float)visualSimilarityMin minFeatureConfidence:(float)minFeatureConfidence requiredPersistenceFrames:(int32_t)requiredPersistenceFrames requiredRecoveryFrames:(int32_t)requiredRecoveryFrames __attribute__((swift_name("doCopy(exposureMeanDelta:exposureP95Delta:warmthDelta:subjectPositionDelta:subjectSizeDelta:headroomDelta:compositionDelta:visualSimilarityMin:minFeatureConfidence:requiredPersistenceFrames:requiredRecoveryFrames:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float compositionDelta __attribute__((swift_name("compositionDelta")));
@property (readonly) float exposureMeanDelta __attribute__((swift_name("exposureMeanDelta")));
@property (readonly) float exposureP95Delta __attribute__((swift_name("exposureP95Delta")));
@property (readonly) float headroomDelta __attribute__((swift_name("headroomDelta")));
@property (readonly) float minFeatureConfidence __attribute__((swift_name("minFeatureConfidence")));
@property (readonly) int32_t requiredPersistenceFrames __attribute__((swift_name("requiredPersistenceFrames")));
@property (readonly) int32_t requiredRecoveryFrames __attribute__((swift_name("requiredRecoveryFrames")));
@property (readonly) float subjectPositionDelta __attribute__((swift_name("subjectPositionDelta")));
@property (readonly) float subjectSizeDelta __attribute__((swift_name("subjectSizeDelta")));
@property (readonly) float visualSimilarityMin __attribute__((swift_name("visualSimilarityMin")));
@property (readonly) float warmthDelta __attribute__((swift_name("warmthDelta")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceDriftDirection")))
@interface DyrectoSharedReferenceDriftDirection : DyrectoSharedKotlinEnum<DyrectoSharedReferenceDriftDirection *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *brighter __attribute__((swift_name("brighter")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *darker __attribute__((swift_name("darker")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *warmer __attribute__((swift_name("warmer")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *cooler __attribute__((swift_name("cooler")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *left __attribute__((swift_name("left")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *right __attribute__((swift_name("right")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *up __attribute__((swift_name("up")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *down __attribute__((swift_name("down")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *larger __attribute__((swift_name("larger")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *smaller __attribute__((swift_name("smaller")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *missing __attribute__((swift_name("missing")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *different __attribute__((swift_name("different")));
@property (class, readonly) DyrectoSharedReferenceDriftDirection *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedReferenceDriftDirection *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedReferenceDriftDirection *> *entries __attribute__((swift_name("entries")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceEmbedding")))
@interface DyrectoSharedReferenceEmbedding : DyrectoSharedBase
- (instancetype)initWithId:(NSString *)id modelId:(NSString *)modelId dimensions:(int32_t)dimensions values:(NSArray<DyrectoSharedFloat *> *)values createdAtMs:(int64_t)createdAtMs schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("init(id:modelId:dimensions:values:createdAtMs:schemaVersion:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceEmbeddingCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceEmbedding *)doCopyId:(NSString *)id modelId:(NSString *)modelId dimensions:(int32_t)dimensions values:(NSArray<DyrectoSharedFloat *> *)values createdAtMs:(int64_t)createdAtMs schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("doCopy(id:modelId:dimensions:values:createdAtMs:schemaVersion:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
- (DyrectoSharedKotlinFloatArray *)vector __attribute__((swift_name("vector()")));
@property (readonly) int64_t createdAtMs __attribute__((swift_name("createdAtMs")));
@property (readonly) int32_t dimensions __attribute__((swift_name("dimensions")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@property (readonly) NSArray<DyrectoSharedFloat *> *values __attribute__((swift_name("values")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceEmbedding.Companion")))
@interface DyrectoSharedReferenceEmbeddingCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceEmbeddingCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceExposureProfile")))
@interface DyrectoSharedReferenceExposureProfile : DyrectoSharedBase
- (instancetype)initWithMean:(float)mean median:(float)median p95:(float)p95 p99:(float)p99 highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage histogramSignature:(NSArray<DyrectoSharedFloat *> *)histogramSignature subjectExposure:(DyrectoSharedSubjectExposureStats * _Nullable)subjectExposure __attribute__((swift_name("init(mean:median:p95:p99:highlightCoverage:shadowCoverage:histogramSignature:subjectExposure:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceExposureProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceExposureProfile *)doCopyMean:(float)mean median:(float)median p95:(float)p95 p99:(float)p99 highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage histogramSignature:(NSArray<DyrectoSharedFloat *> *)histogramSignature subjectExposure:(DyrectoSharedSubjectExposureStats * _Nullable)subjectExposure __attribute__((swift_name("doCopy(mean:median:p95:p99:highlightCoverage:shadowCoverage:histogramSignature:subjectExposure:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float highlightCoverage __attribute__((swift_name("highlightCoverage")));
@property (readonly) NSArray<DyrectoSharedFloat *> *histogramSignature __attribute__((swift_name("histogramSignature")));
@property (readonly) float mean __attribute__((swift_name("mean")));
@property (readonly) float median __attribute__((swift_name("median")));
@property (readonly) float p95 __attribute__((swift_name("p95")));
@property (readonly) float p99 __attribute__((swift_name("p99")));
@property (readonly) float shadowCoverage __attribute__((swift_name("shadowCoverage")));
@property (readonly) DyrectoSharedSubjectExposureStats * _Nullable subjectExposure __attribute__((swift_name("subjectExposure")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceExposureProfile.Companion")))
@interface DyrectoSharedReferenceExposureProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceExposureProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceFaceProfile")))
@interface DyrectoSharedReferenceFaceProfile : DyrectoSharedBase
- (instancetype)initWithFaceDetected:(BOOL)faceDetected eyesDetected:(BOOL)eyesDetected faceBox:(DyrectoSharedNormalizedRect * _Nullable)faceBox eyeCount:(int32_t)eyeCount __attribute__((swift_name("init(faceDetected:eyesDetected:faceBox:eyeCount:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceFaceProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceFaceProfile *)doCopyFaceDetected:(BOOL)faceDetected eyesDetected:(BOOL)eyesDetected faceBox:(DyrectoSharedNormalizedRect * _Nullable)faceBox eyeCount:(int32_t)eyeCount __attribute__((swift_name("doCopy(faceDetected:eyesDetected:faceBox:eyeCount:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t eyeCount __attribute__((swift_name("eyeCount")));
@property (readonly) BOOL eyesDetected __attribute__((swift_name("eyesDetected")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable faceBox __attribute__((swift_name("faceBox")));
@property (readonly) BOOL faceDetected __attribute__((swift_name("faceDetected")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceFaceProfile.Companion")))
@interface DyrectoSharedReferenceFaceProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceFaceProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceMatchResult")))
@interface DyrectoSharedReferenceMatchResult : DyrectoSharedBase
- (instancetype)initWithActive:(BOOL)active referenceId:(NSString * _Nullable)referenceId strategy:(DyrectoSharedComparisonStrategy *)strategy overallScore:(float)overallScore creativeAware:(BOOL)creativeAware exposureMatch:(DyrectoSharedReferenceSignalResult *)exposureMatch subjectPositionMatch:(DyrectoSharedReferenceSignalResult *)subjectPositionMatch subjectSizeMatch:(DyrectoSharedReferenceSignalResult *)subjectSizeMatch whiteBalanceMatch:(DyrectoSharedReferenceSignalResult *)whiteBalanceMatch headroomMatch:(DyrectoSharedReferenceSignalResult *)headroomMatch facePresenceMatch:(DyrectoSharedReferenceSignalResult *)facePresenceMatch eyeVisibilityMatch:(DyrectoSharedReferenceSignalResult *)eyeVisibilityMatch subjectPresenceMatch:(DyrectoSharedReferenceSignalResult *)subjectPresenceMatch compositionMatch:(DyrectoSharedReferenceSignalResult *)compositionMatch visualSimilarityMatch:(DyrectoSharedReferenceSignalResult *)visualSimilarityMatch updatedAtMs:(int64_t)updatedAtMs __attribute__((swift_name("init(active:referenceId:strategy:overallScore:creativeAware:exposureMatch:subjectPositionMatch:subjectSizeMatch:whiteBalanceMatch:headroomMatch:facePresenceMatch:eyeVisibilityMatch:subjectPresenceMatch:compositionMatch:visualSimilarityMatch:updatedAtMs:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceMatchResult *)doCopyActive:(BOOL)active referenceId:(NSString * _Nullable)referenceId strategy:(DyrectoSharedComparisonStrategy *)strategy overallScore:(float)overallScore creativeAware:(BOOL)creativeAware exposureMatch:(DyrectoSharedReferenceSignalResult *)exposureMatch subjectPositionMatch:(DyrectoSharedReferenceSignalResult *)subjectPositionMatch subjectSizeMatch:(DyrectoSharedReferenceSignalResult *)subjectSizeMatch whiteBalanceMatch:(DyrectoSharedReferenceSignalResult *)whiteBalanceMatch headroomMatch:(DyrectoSharedReferenceSignalResult *)headroomMatch facePresenceMatch:(DyrectoSharedReferenceSignalResult *)facePresenceMatch eyeVisibilityMatch:(DyrectoSharedReferenceSignalResult *)eyeVisibilityMatch subjectPresenceMatch:(DyrectoSharedReferenceSignalResult *)subjectPresenceMatch compositionMatch:(DyrectoSharedReferenceSignalResult *)compositionMatch visualSimilarityMatch:(DyrectoSharedReferenceSignalResult *)visualSimilarityMatch updatedAtMs:(int64_t)updatedAtMs __attribute__((swift_name("doCopy(active:referenceId:strategy:overallScore:creativeAware:exposureMatch:subjectPositionMatch:subjectSizeMatch:whiteBalanceMatch:headroomMatch:facePresenceMatch:eyeVisibilityMatch:subjectPresenceMatch:compositionMatch:visualSimilarityMatch:updatedAtMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (DyrectoSharedReferenceSignalResult *)signalSignal:(DyrectoSharedReferenceSignal *)signal __attribute__((swift_name("signal(signal:)")));
- (DyrectoSharedKotlinPair<DyrectoSharedReferenceSignal *, DyrectoSharedReferenceSignalResult *> * _Nullable)strongestDrift __attribute__((swift_name("strongestDrift()")));
- (NSString *)description __attribute__((swift_name("description()")));
- (DyrectoSharedReferenceMatchResult *)withSignalSignal:(DyrectoSharedReferenceSignal *)signal result:(DyrectoSharedReferenceSignalResult *)result __attribute__((swift_name("withSignal(signal:result:)")));
@property (readonly) BOOL active __attribute__((swift_name("active")));
@property (readonly) DyrectoSharedReferenceSignalResult *compositionMatch __attribute__((swift_name("compositionMatch")));
@property (readonly) BOOL creativeAware __attribute__((swift_name("creativeAware")));
@property (readonly) DyrectoSharedReferenceSignalResult *exposureMatch __attribute__((swift_name("exposureMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *eyeVisibilityMatch __attribute__((swift_name("eyeVisibilityMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *facePresenceMatch __attribute__((swift_name("facePresenceMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *headroomMatch __attribute__((swift_name("headroomMatch")));
@property (readonly) float overallScore __attribute__((swift_name("overallScore")));
@property (readonly) NSString * _Nullable referenceId __attribute__((swift_name("referenceId")));
@property (readonly) DyrectoSharedComparisonStrategy *strategy __attribute__((swift_name("strategy")));
@property (readonly) DyrectoSharedReferenceSignalResult *subjectPositionMatch __attribute__((swift_name("subjectPositionMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *subjectPresenceMatch __attribute__((swift_name("subjectPresenceMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *subjectSizeMatch __attribute__((swift_name("subjectSizeMatch")));
@property (readonly) int64_t updatedAtMs __attribute__((swift_name("updatedAtMs")));
@property (readonly) DyrectoSharedReferenceSignalResult *visualSimilarityMatch __attribute__((swift_name("visualSimilarityMatch")));
@property (readonly) DyrectoSharedReferenceSignalResult *whiteBalanceMatch __attribute__((swift_name("whiteBalanceMatch")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceMonitorOptions")))
@interface DyrectoSharedReferenceMonitorOptions : DyrectoSharedBase
- (instancetype)initWithMonitorExposure:(BOOL)monitorExposure monitorSubjectPosition:(BOOL)monitorSubjectPosition monitorSubjectSize:(BOOL)monitorSubjectSize monitorWhiteBalance:(BOOL)monitorWhiteBalance monitorFraming:(BOOL)monitorFraming monitorHeadroom:(BOOL)monitorHeadroom monitorFacePresence:(BOOL)monitorFacePresence monitorEyeVisibility:(BOOL)monitorEyeVisibility monitorSubjectPresence:(BOOL)monitorSubjectPresence monitorComposition:(BOOL)monitorComposition monitorVisualSimilarity:(BOOL)monitorVisualSimilarity tolerance:(DyrectoSharedReferenceTolerance *)tolerance __attribute__((swift_name("init(monitorExposure:monitorSubjectPosition:monitorSubjectSize:monitorWhiteBalance:monitorFraming:monitorHeadroom:monitorFacePresence:monitorEyeVisibility:monitorSubjectPresence:monitorComposition:monitorVisualSimilarity:tolerance:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceMonitorOptionsCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceMonitorOptions *)doCopyMonitorExposure:(BOOL)monitorExposure monitorSubjectPosition:(BOOL)monitorSubjectPosition monitorSubjectSize:(BOOL)monitorSubjectSize monitorWhiteBalance:(BOOL)monitorWhiteBalance monitorFraming:(BOOL)monitorFraming monitorHeadroom:(BOOL)monitorHeadroom monitorFacePresence:(BOOL)monitorFacePresence monitorEyeVisibility:(BOOL)monitorEyeVisibility monitorSubjectPresence:(BOOL)monitorSubjectPresence monitorComposition:(BOOL)monitorComposition monitorVisualSimilarity:(BOOL)monitorVisualSimilarity tolerance:(DyrectoSharedReferenceTolerance *)tolerance __attribute__((swift_name("doCopy(monitorExposure:monitorSubjectPosition:monitorSubjectSize:monitorWhiteBalance:monitorFraming:monitorHeadroom:monitorFacePresence:monitorEyeVisibility:monitorSubjectPresence:monitorComposition:monitorVisualSimilarity:tolerance:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL monitorComposition __attribute__((swift_name("monitorComposition")));
@property (readonly) BOOL monitorExposure __attribute__((swift_name("monitorExposure")));
@property (readonly) BOOL monitorEyeVisibility __attribute__((swift_name("monitorEyeVisibility")));
@property (readonly) BOOL monitorFacePresence __attribute__((swift_name("monitorFacePresence")));
@property (readonly) BOOL monitorFraming __attribute__((swift_name("monitorFraming")));
@property (readonly) BOOL monitorHeadroom __attribute__((swift_name("monitorHeadroom")));
@property (readonly) BOOL monitorSubjectPosition __attribute__((swift_name("monitorSubjectPosition")));
@property (readonly) BOOL monitorSubjectPresence __attribute__((swift_name("monitorSubjectPresence")));
@property (readonly) BOOL monitorSubjectSize __attribute__((swift_name("monitorSubjectSize")));
@property (readonly) BOOL monitorVisualSimilarity __attribute__((swift_name("monitorVisualSimilarity")));
@property (readonly) BOOL monitorWhiteBalance __attribute__((swift_name("monitorWhiteBalance")));
@property (readonly) DyrectoSharedReferenceTolerance *tolerance __attribute__((swift_name("tolerance")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceMonitorOptions.Companion")))
@interface DyrectoSharedReferenceMonitorOptionsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceMonitorOptionsCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceProfile")))
@interface DyrectoSharedReferenceProfile : DyrectoSharedBase
- (instancetype)initWithId:(NSString *)id name:(NSString *)name createdAtMs:(int64_t)createdAtMs imageUri:(NSString * _Nullable)imageUri width:(int32_t)width height:(int32_t)height exposure:(DyrectoSharedReferenceExposureProfile *)exposure color:(DyrectoSharedReferenceColorProfile *)color subject:(DyrectoSharedReferenceSubjectProfile * _Nullable)subject face:(DyrectoSharedReferenceFaceProfile * _Nullable)face options:(DyrectoSharedReferenceMonitorOptions *)options ai:(DyrectoSharedReferenceAiProfile * _Nullable)ai creativeScene:(DyrectoSharedCreativeSceneModel * _Nullable)creativeScene completion:(DyrectoSharedShotCompletion *)completion cameraSettings:(DyrectoSharedReferenceCameraSettings * _Nullable)cameraSettings __attribute__((swift_name("init(id:name:createdAtMs:imageUri:width:height:exposure:color:subject:face:options:ai:creativeScene:completion:cameraSettings:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceProfile *)doCopyId:(NSString *)id name:(NSString *)name createdAtMs:(int64_t)createdAtMs imageUri:(NSString * _Nullable)imageUri width:(int32_t)width height:(int32_t)height exposure:(DyrectoSharedReferenceExposureProfile *)exposure color:(DyrectoSharedReferenceColorProfile *)color subject:(DyrectoSharedReferenceSubjectProfile * _Nullable)subject face:(DyrectoSharedReferenceFaceProfile * _Nullable)face options:(DyrectoSharedReferenceMonitorOptions *)options ai:(DyrectoSharedReferenceAiProfile * _Nullable)ai creativeScene:(DyrectoSharedCreativeSceneModel * _Nullable)creativeScene completion:(DyrectoSharedShotCompletion *)completion cameraSettings:(DyrectoSharedReferenceCameraSettings * _Nullable)cameraSettings __attribute__((swift_name("doCopy(id:name:createdAtMs:imageUri:width:height:exposure:color:subject:face:options:ai:creativeScene:completion:cameraSettings:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedReferenceAiProfile * _Nullable ai __attribute__((swift_name("ai")));
@property (readonly) DyrectoSharedReferenceCameraSettings * _Nullable cameraSettings __attribute__((swift_name("cameraSettings")));
@property (readonly) DyrectoSharedReferenceColorProfile *color __attribute__((swift_name("color")));
@property (readonly) DyrectoSharedShotCompletion *completion __attribute__((swift_name("completion")));
@property (readonly) int64_t createdAtMs __attribute__((swift_name("createdAtMs")));
@property (readonly) DyrectoSharedCreativeSceneModel * _Nullable creativeScene __attribute__((swift_name("creativeScene")));
@property (readonly) DyrectoSharedReferenceExposureProfile *exposure __attribute__((swift_name("exposure")));
@property (readonly) DyrectoSharedReferenceFaceProfile * _Nullable face __attribute__((swift_name("face")));
@property (readonly) int32_t height __attribute__((swift_name("height")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) NSString * _Nullable imageUri __attribute__((swift_name("imageUri")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) DyrectoSharedReferenceMonitorOptions *options __attribute__((swift_name("options")));
@property (readonly) DyrectoSharedReferenceSubjectProfile * _Nullable subject __attribute__((swift_name("subject")));
@property (readonly) int32_t width __attribute__((swift_name("width")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceProfile.Companion")))
@interface DyrectoSharedReferenceProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceProfileCodec")))
@interface DyrectoSharedReferenceProfileCodec : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)referenceProfileCodec __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceProfileCodec *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceEmbedding * _Nullable)decodeEmbeddingText:(NSString *)text __attribute__((swift_name("decodeEmbedding(text:)")));
- (DyrectoSharedReferenceSession * _Nullable)decodeSessionText:(NSString *)text __attribute__((swift_name("decodeSession(text:)")));
- (NSString *)encodeEmbeddingEmbedding:(DyrectoSharedReferenceEmbedding *)embedding __attribute__((swift_name("encodeEmbedding(embedding:)")));
- (NSString *)encodeSessionSession:(DyrectoSharedReferenceSession *)session __attribute__((swift_name("encodeSession(session:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSession")))
@interface DyrectoSharedReferenceSession : DyrectoSharedBase
- (instancetype)initWithId:(NSString *)id name:(NSString * _Nullable)name createdAtMs:(int64_t)createdAtMs profiles:(NSArray<DyrectoSharedReferenceProfile *> *)profiles activeProfileId:(NSString * _Nullable)activeProfileId completionRule:(DyrectoSharedStoryboardCompletionRule *)completionRule schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("init(id:name:createdAtMs:profiles:activeProfileId:completionRule:schemaVersion:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceSessionCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceSession *)addProfileProfile:(DyrectoSharedReferenceProfile *)profile __attribute__((swift_name("addProfile(profile:)")));
- (DyrectoSharedReferenceSession *)doCopyId:(NSString *)id name:(NSString * _Nullable)name createdAtMs:(int64_t)createdAtMs profiles:(NSArray<DyrectoSharedReferenceProfile *> *)profiles activeProfileId:(NSString * _Nullable)activeProfileId completionRule:(DyrectoSharedStoryboardCompletionRule *)completionRule schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("doCopy(id:name:createdAtMs:profiles:activeProfileId:completionRule:schemaVersion:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (DyrectoSharedReferenceSession *)removeProfileId:(NSString *)id __attribute__((swift_name("removeProfile(id:)")));
- (NSString *)description __attribute__((swift_name("description()")));
- (DyrectoSharedReferenceSession *)withCompletionRuleRule:(DyrectoSharedStoryboardCompletionRule *)rule __attribute__((swift_name("withCompletionRule(rule:)")));
- (DyrectoSharedReferenceSession *)withOptionsForAllOptions:(DyrectoSharedReferenceMonitorOptions *)options __attribute__((swift_name("withOptionsForAll(options:)")));
- (DyrectoSharedReferenceSession *)withProfileProfile:(DyrectoSharedReferenceProfile *)profile __attribute__((swift_name("withProfile(profile:)")));
- (DyrectoSharedReferenceSession *)withProgressReset __attribute__((swift_name("withProgressReset()")));
- (DyrectoSharedReferenceSession *)withReplacedProfileOldId:(NSString *)oldId profile:(DyrectoSharedReferenceProfile *)profile __attribute__((swift_name("withReplacedProfile(oldId:profile:)")));
- (DyrectoSharedReferenceSession *)withUpdatedActiveProfileProfile:(DyrectoSharedReferenceProfile *)profile __attribute__((swift_name("withUpdatedActiveProfile(profile:)")));
- (DyrectoSharedReferenceSession *)withUpdatedProfileProfile:(DyrectoSharedReferenceProfile *)profile __attribute__((swift_name("withUpdatedProfile(profile:)")));
@property (readonly) DyrectoSharedReferenceProfile * _Nullable activeProfile __attribute__((swift_name("activeProfile")));
@property (readonly) NSString * _Nullable activeProfileId __attribute__((swift_name("activeProfileId")));
@property (readonly) int32_t completedCount __attribute__((swift_name("completedCount")));
@property (readonly) DyrectoSharedStoryboardCompletionRule *completionRule __attribute__((swift_name("completionRule")));
@property (readonly) int64_t createdAtMs __attribute__((swift_name("createdAtMs")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) NSString * _Nullable name __attribute__((swift_name("name")));
@property (readonly) NSArray<DyrectoSharedReferenceProfile *> *profiles __attribute__((swift_name("profiles")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSession.Companion")))
@interface DyrectoSharedReferenceSessionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceSessionCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSessionState")))
@interface DyrectoSharedReferenceSessionState : DyrectoSharedBase
- (instancetype)initWithSession:(DyrectoSharedReferenceSession * _Nullable)session monitoringRequested:(BOOL)monitoringRequested analyzing:(BOOL)analyzing error:(NSString * _Nullable)error __attribute__((swift_name("init(session:monitoringRequested:analyzing:error:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceSessionState *)doCopySession:(DyrectoSharedReferenceSession * _Nullable)session monitoringRequested:(BOOL)monitoringRequested analyzing:(BOOL)analyzing error:(NSString * _Nullable)error __attribute__((swift_name("doCopy(session:monitoringRequested:analyzing:error:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL analyzing __attribute__((swift_name("analyzing")));
@property (readonly) int32_t completedCount __attribute__((swift_name("completedCount")));
@property (readonly) DyrectoSharedStoryboardCompletionRule *completionRule __attribute__((swift_name("completionRule")));
@property (readonly) NSString * _Nullable error __attribute__((swift_name("error")));
@property (readonly) BOOL monitoringRequested __attribute__((swift_name("monitoringRequested")));
@property (readonly) DyrectoSharedReferenceProfile * _Nullable profile __attribute__((swift_name("profile")));
@property (readonly) DyrectoSharedStoryboardProgress *progress __attribute__((swift_name("progress")));
@property (readonly) DyrectoSharedReferenceSession * _Nullable session __attribute__((swift_name("session")));
@property (readonly) int32_t totalCount __attribute__((swift_name("totalCount")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSignal")))
@interface DyrectoSharedReferenceSignal : DyrectoSharedKotlinEnum<DyrectoSharedReferenceSignal *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedReferenceSignal *exposure __attribute__((swift_name("exposure")));
@property (class, readonly) DyrectoSharedReferenceSignal *subjectPosition __attribute__((swift_name("subjectPosition")));
@property (class, readonly) DyrectoSharedReferenceSignal *subjectSize __attribute__((swift_name("subjectSize")));
@property (class, readonly) DyrectoSharedReferenceSignal *whiteBalance __attribute__((swift_name("whiteBalance")));
@property (class, readonly) DyrectoSharedReferenceSignal *headroom __attribute__((swift_name("headroom")));
@property (class, readonly) DyrectoSharedReferenceSignal *facePresence __attribute__((swift_name("facePresence")));
@property (class, readonly) DyrectoSharedReferenceSignal *eyeVisibility __attribute__((swift_name("eyeVisibility")));
@property (class, readonly) DyrectoSharedReferenceSignal *subjectPresence __attribute__((swift_name("subjectPresence")));
@property (class, readonly) DyrectoSharedReferenceSignal *composition __attribute__((swift_name("composition")));
@property (class, readonly) DyrectoSharedReferenceSignal *visualSimilarity __attribute__((swift_name("visualSimilarity")));
+ (DyrectoSharedKotlinArray<DyrectoSharedReferenceSignal *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedReferenceSignal *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSignalApplicability")))
@interface DyrectoSharedReferenceSignalApplicability : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)referenceSignalApplicability __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceSignalApplicability *shared __attribute__((swift_name("shared")));
- (BOOL)appliesSignal:(DyrectoSharedReferenceSignal *)signal strategy:(DyrectoSharedComparisonStrategy *)strategy __attribute__((swift_name("applies(signal:strategy:)")));
- (NSSet<DyrectoSharedReferenceSignal *> *)signalsForStrategy:(DyrectoSharedComparisonStrategy *)strategy __attribute__((swift_name("signalsFor(strategy:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSignalResult")))
@interface DyrectoSharedReferenceSignalResult : DyrectoSharedBase
- (instancetype)initWithEnabled:(BOOL)enabled available:(BOOL)available matched:(BOOL)matched score:(float)score delta:(float)delta direction:(DyrectoSharedReferenceDriftDirection *)direction message:(NSString *)message state:(DyrectoSharedReferenceSignalState *)state persistenceFrames:(int32_t)persistenceFrames recoveryFrames:(int32_t)recoveryFrames confirmedDrift:(BOOL)confirmedDrift perception:(DyrectoSharedPerceptualSignal * _Nullable)perception instruction:(DyrectoSharedAssistantInstruction * _Nullable)instruction __attribute__((swift_name("init(enabled:available:matched:score:delta:direction:message:state:persistenceFrames:recoveryFrames:confirmedDrift:perception:instruction:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedReferenceSignalResult *)doCopyEnabled:(BOOL)enabled available:(BOOL)available matched:(BOOL)matched score:(float)score delta:(float)delta direction:(DyrectoSharedReferenceDriftDirection *)direction message:(NSString *)message state:(DyrectoSharedReferenceSignalState *)state persistenceFrames:(int32_t)persistenceFrames recoveryFrames:(int32_t)recoveryFrames confirmedDrift:(BOOL)confirmedDrift perception:(DyrectoSharedPerceptualSignal * _Nullable)perception instruction:(DyrectoSharedAssistantInstruction * _Nullable)instruction __attribute__((swift_name("doCopy(enabled:available:matched:score:delta:direction:message:state:persistenceFrames:recoveryFrames:confirmedDrift:perception:instruction:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL available __attribute__((swift_name("available")));
@property (readonly) BOOL confirmedDrift __attribute__((swift_name("confirmedDrift")));
@property (readonly) float delta __attribute__((swift_name("delta")));
@property (readonly) DyrectoSharedReferenceDriftDirection *direction __attribute__((swift_name("direction")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));
@property (readonly) DyrectoSharedAssistantInstruction * _Nullable instruction __attribute__((swift_name("instruction")));
@property (readonly) BOOL matched __attribute__((swift_name("matched")));
@property (readonly) NSString *message __attribute__((swift_name("message")));
@property (readonly) DyrectoSharedPerceptualSignal * _Nullable perception __attribute__((swift_name("perception")));
@property (readonly) int32_t persistenceFrames __attribute__((swift_name("persistenceFrames")));
@property (readonly) int32_t recoveryFrames __attribute__((swift_name("recoveryFrames")));
@property (readonly) float score __attribute__((swift_name("score")));
@property (readonly) DyrectoSharedReferenceSignalState *state __attribute__((swift_name("state")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSignalState")))
@interface DyrectoSharedReferenceSignalState : DyrectoSharedKotlinEnum<DyrectoSharedReferenceSignalState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedReferenceSignalState *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedReferenceSignalState *drifting __attribute__((swift_name("drifting")));
@property (class, readonly) DyrectoSharedReferenceSignalState *confirmedDrift __attribute__((swift_name("confirmedDrift")));
@property (class, readonly) DyrectoSharedReferenceSignalState *recovering __attribute__((swift_name("recovering")));
+ (DyrectoSharedKotlinArray<DyrectoSharedReferenceSignalState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedReferenceSignalState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceStateMachine")))
@interface DyrectoSharedReferenceStateMachine : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)reset __attribute__((swift_name("reset()")));
- (DyrectoSharedReferenceMatchResult *)updateRaw:(DyrectoSharedReferenceMatchResult *)raw thresholds:(DyrectoSharedReferenceConfigReferenceThresholds *)thresholds __attribute__((swift_name("update(raw:thresholds:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSubjectProfile")))
@interface DyrectoSharedReferenceSubjectProfile : DyrectoSharedBase
- (instancetype)initWithNormalizedCenterX:(float)normalizedCenterX normalizedCenterY:(float)normalizedCenterY normalizedWidth:(float)normalizedWidth normalizedHeight:(float)normalizedHeight normalizedArea:(float)normalizedArea __attribute__((swift_name("init(normalizedCenterX:normalizedCenterY:normalizedWidth:normalizedHeight:normalizedArea:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedReferenceSubjectProfileCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedReferenceSubjectProfile *)doCopyNormalizedCenterX:(float)normalizedCenterX normalizedCenterY:(float)normalizedCenterY normalizedWidth:(float)normalizedWidth normalizedHeight:(float)normalizedHeight normalizedArea:(float)normalizedArea __attribute__((swift_name("doCopy(normalizedCenterX:normalizedCenterY:normalizedWidth:normalizedHeight:normalizedArea:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float normalizedArea __attribute__((swift_name("normalizedArea")));
@property (readonly) float normalizedCenterX __attribute__((swift_name("normalizedCenterX")));
@property (readonly) float normalizedCenterY __attribute__((swift_name("normalizedCenterY")));
@property (readonly) float normalizedHeight __attribute__((swift_name("normalizedHeight")));
@property (readonly) float normalizedWidth __attribute__((swift_name("normalizedWidth")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceSubjectProfile.Companion")))
@interface DyrectoSharedReferenceSubjectProfileCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedReferenceSubjectProfileCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReferenceTolerance")))
@interface DyrectoSharedReferenceTolerance : DyrectoSharedKotlinEnum<DyrectoSharedReferenceTolerance *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedReferenceTolerance *strict __attribute__((swift_name("strict")));
@property (class, readonly) DyrectoSharedReferenceTolerance *medium __attribute__((swift_name("medium")));
@property (class, readonly) DyrectoSharedReferenceTolerance *loose __attribute__((swift_name("loose")));
+ (DyrectoSharedKotlinArray<DyrectoSharedReferenceTolerance *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedReferenceTolerance *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneComparator")))
@interface DyrectoSharedSceneComparator : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sceneComparator __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneComparator *shared __attribute__((swift_name("shared")));
- (DyrectoSharedReferenceMatchResult *)compareReference:(DyrectoSharedReferenceProfile *)reference current:(DyrectoSharedCurrentReferenceInput *)current nowMs:(int64_t)nowMs __attribute__((swift_name("compare(reference:current:nowMs:)")));
- (DyrectoSharedNormalizedRect * _Nullable)largestFaceNormalizedFaces:(DyrectoSharedFaceDetectionResult * _Nullable)faces __attribute__((swift_name("largestFaceNormalized(faces:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SettingDrift")))
@interface DyrectoSharedSettingDrift : DyrectoSharedBase
- (instancetype)initWithLabel:(NSString *)label referenceValue:(NSString *)referenceValue liveValue:(NSString *)liveValue alertType:(DyrectoSharedAlertType *)alertType __attribute__((swift_name("init(label:referenceValue:liveValue:alertType:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSettingDrift *)doCopyLabel:(NSString *)label referenceValue:(NSString *)referenceValue liveValue:(NSString *)liveValue alertType:(DyrectoSharedAlertType *)alertType __attribute__((swift_name("doCopy(label:referenceValue:liveValue:alertType:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAlertType *alertType __attribute__((swift_name("alertType")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) NSString *liveValue __attribute__((swift_name("liveValue")));
@property (readonly) NSString *referenceValue __attribute__((swift_name("referenceValue")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SettingsDriftRules")))
@interface DyrectoSharedSettingsDriftRules : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)settingsDriftRules __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSettingsDriftRules *shared __attribute__((swift_name("shared")));
- (NSArray<DyrectoSharedSettingDrift *> *)evaluateReference:(DyrectoSharedReferenceCameraSettings *)reference live:(DyrectoSharedReferenceCameraSettings *)live __attribute__((swift_name("evaluate(reference:live:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotCompletion")))
@interface DyrectoSharedShotCompletion : DyrectoSharedBase
- (instancetype)initWithCompleted:(BOOL)completed confirmations:(int32_t)confirmations __attribute__((swift_name("init(completed:confirmations:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedShotCompletionCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedShotCompletion *)doCopyCompleted:(BOOL)completed confirmations:(int32_t)confirmations __attribute__((swift_name("doCopy(completed:confirmations:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL completed __attribute__((swift_name("completed")));
@property (readonly) int32_t confirmations __attribute__((swift_name("confirmations")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotCompletion.Companion")))
@interface DyrectoSharedShotCompletionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedShotCompletionCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardAlertRules")))
@interface DyrectoSharedStoryboardAlertRules : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)storyboardAlertRules __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedStoryboardAlertRules *shared __attribute__((swift_name("shared")));
- (DyrectoSharedAlert * _Nullable)onSessionEndedState:(DyrectoSharedReferenceSessionState *)state now:(int64_t)now idGen:(DyrectoSharedAlertIdGenerator *)idGen __attribute__((swift_name("onSessionEnded(state:now:idGen:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardCompletion")))
@interface DyrectoSharedStoryboardCompletion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)storyboardCompletion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedStoryboardCompletion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedStoryboardCompletionUpdate *)updateCompletion:(DyrectoSharedShotCompletion *)completion runtime:(DyrectoSharedStoryboardCompletionRuntime *)runtime aboveThreshold:(BOOL)aboveThreshold nowMs:(int64_t)nowMs rule:(DyrectoSharedStoryboardCompletionRule *)rule __attribute__((swift_name("update(completion:runtime:aboveThreshold:nowMs:rule:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardCompletion.Runtime")))
@interface DyrectoSharedStoryboardCompletionRuntime : DyrectoSharedBase
- (instancetype)initWithHoldStartMs:(DyrectoSharedLong * _Nullable)holdStartMs streakConfirmed:(BOOL)streakConfirmed __attribute__((swift_name("init(holdStartMs:streakConfirmed:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedStoryboardCompletionRuntime *)doCopyHoldStartMs:(DyrectoSharedLong * _Nullable)holdStartMs streakConfirmed:(BOOL)streakConfirmed __attribute__((swift_name("doCopy(holdStartMs:streakConfirmed:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedLong * _Nullable holdStartMs __attribute__((swift_name("holdStartMs")));
@property (readonly) BOOL streakConfirmed __attribute__((swift_name("streakConfirmed")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardCompletion.Update")))
@interface DyrectoSharedStoryboardCompletionUpdate : DyrectoSharedBase
- (instancetype)initWithCompletion:(DyrectoSharedShotCompletion *)completion runtime:(DyrectoSharedStoryboardCompletionRuntime *)runtime justCompleted:(BOOL)justCompleted __attribute__((swift_name("init(completion:runtime:justCompleted:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedStoryboardCompletionUpdate *)doCopyCompletion:(DyrectoSharedShotCompletion *)completion runtime:(DyrectoSharedStoryboardCompletionRuntime *)runtime justCompleted:(BOOL)justCompleted __attribute__((swift_name("doCopy(completion:runtime:justCompleted:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedShotCompletion *completion __attribute__((swift_name("completion")));
@property (readonly) BOOL justCompleted __attribute__((swift_name("justCompleted")));
@property (readonly) DyrectoSharedStoryboardCompletionRuntime *runtime __attribute__((swift_name("runtime")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardCompletionRule")))
@interface DyrectoSharedStoryboardCompletionRule : DyrectoSharedBase
- (instancetype)initWithHoldSeconds:(int32_t)holdSeconds confirmCount:(int32_t)confirmCount matchThreshold:(float)matchThreshold __attribute__((swift_name("init(holdSeconds:confirmCount:matchThreshold:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedStoryboardCompletionRuleCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedStoryboardCompletionRule *)doCopyHoldSeconds:(int32_t)holdSeconds confirmCount:(int32_t)confirmCount matchThreshold:(float)matchThreshold __attribute__((swift_name("doCopy(holdSeconds:confirmCount:matchThreshold:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t confirmCount __attribute__((swift_name("confirmCount")));
@property (readonly) int64_t holdMs __attribute__((swift_name("holdMs")));
@property (readonly) int32_t holdSeconds __attribute__((swift_name("holdSeconds")));
@property (readonly) float matchThreshold __attribute__((swift_name("matchThreshold")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardCompletionRule.Companion")))
@interface DyrectoSharedStoryboardCompletionRuleCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedStoryboardCompletionRuleCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StoryboardProgress")))
@interface DyrectoSharedStoryboardProgress : DyrectoSharedBase
- (instancetype)initWithCompleted:(int32_t)completed total:(int32_t)total __attribute__((swift_name("init(completed:total:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedStoryboardProgress *)doCopyCompleted:(int32_t)completed total:(int32_t)total __attribute__((swift_name("doCopy(completed:total:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL allComplete __attribute__((swift_name("allComplete")));
@property (readonly) int32_t completed __attribute__((swift_name("completed")));
@property (readonly) float fraction __attribute__((swift_name("fraction")));
@property (readonly) int32_t total __attribute__((swift_name("total")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AiCadenceConfig")))
@interface DyrectoSharedAiCadenceConfig : DyrectoSharedBase
- (instancetype)initWithTrackerEveryNFrames:(int32_t)trackerEveryNFrames embeddingEveryNFrames:(int32_t)embeddingEveryNFrames staleAfterMs:(int64_t)staleAfterMs inferenceMaxEdgePx:(int32_t)inferenceMaxEdgePx trackingMaxEdgePx:(int32_t)trackingMaxEdgePx __attribute__((swift_name("init(trackerEveryNFrames:embeddingEveryNFrames:staleAfterMs:inferenceMaxEdgePx:trackingMaxEdgePx:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAiCadenceConfig *)doCopyTrackerEveryNFrames:(int32_t)trackerEveryNFrames embeddingEveryNFrames:(int32_t)embeddingEveryNFrames staleAfterMs:(int64_t)staleAfterMs inferenceMaxEdgePx:(int32_t)inferenceMaxEdgePx trackingMaxEdgePx:(int32_t)trackingMaxEdgePx __attribute__((swift_name("doCopy(trackerEveryNFrames:embeddingEveryNFrames:staleAfterMs:inferenceMaxEdgePx:trackingMaxEdgePx:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t embeddingEveryNFrames __attribute__((swift_name("embeddingEveryNFrames")));
@property (readonly) int32_t inferenceMaxEdgePx __attribute__((swift_name("inferenceMaxEdgePx")));
@property (readonly) int64_t staleAfterMs __attribute__((swift_name("staleAfterMs")));
@property (readonly) int32_t trackerEveryNFrames __attribute__((swift_name("trackerEveryNFrames")));
@property (readonly) int32_t trackingMaxEdgePx __attribute__((swift_name("trackingMaxEdgePx")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AiCapabilities")))
@interface DyrectoSharedAiCapabilities : DyrectoSharedBase
- (instancetype)initWithDetection:(DyrectoSharedAiCapability *)detection embedding:(DyrectoSharedAiCapability *)embedding segmentation:(DyrectoSharedAiCapability *)segmentation __attribute__((swift_name("init(detection:embedding:segmentation:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAiCapabilities *)doCopyDetection:(DyrectoSharedAiCapability *)detection embedding:(DyrectoSharedAiCapability *)embedding segmentation:(DyrectoSharedAiCapability *)segmentation __attribute__((swift_name("doCopy(detection:embedding:segmentation:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAiCapability *detection __attribute__((swift_name("detection")));
@property (readonly) DyrectoSharedAiCapability *embedding __attribute__((swift_name("embedding")));
@property (readonly) DyrectoSharedAiCapability *segmentation __attribute__((swift_name("segmentation")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AiCapability")))
@interface DyrectoSharedAiCapability : DyrectoSharedBase
- (instancetype)initWithModelId:(NSString *)modelId status:(DyrectoSharedEngineStatus *)status error:(NSString * _Nullable)error __attribute__((swift_name("init(modelId:status:error:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedAiCapability *)doCopyModelId:(NSString *)modelId status:(DyrectoSharedEngineStatus *)status error:(NSString * _Nullable)error __attribute__((swift_name("doCopy(modelId:status:error:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL available __attribute__((swift_name("available")));
@property (readonly) NSString * _Nullable error __attribute__((swift_name("error")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@property (readonly) DyrectoSharedEngineStatus *status __attribute__((swift_name("status")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ComparisonStrategy")))
@interface DyrectoSharedComparisonStrategy : DyrectoSharedKotlinEnum<DyrectoSharedComparisonStrategy *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedComparisonStrategy *humanStrategy __attribute__((swift_name("humanStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *animalStrategy __attribute__((swift_name("animalStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *vehicleStrategy __attribute__((swift_name("vehicleStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *productStrategy __attribute__((swift_name("productStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *architectureStrategy __attribute__((swift_name("architectureStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *landscapeStrategy __attribute__((swift_name("landscapeStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *multiObjectStrategy __attribute__((swift_name("multiObjectStrategy")));
@property (class, readonly) DyrectoSharedComparisonStrategy *genericSceneStrategy __attribute__((swift_name("genericSceneStrategy")));
+ (DyrectoSharedKotlinArray<DyrectoSharedComparisonStrategy *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedComparisonStrategy *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ComparisonStrategySelector")))
@interface DyrectoSharedComparisonStrategySelector : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)comparisonStrategySelector __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedComparisonStrategySelector *shared __attribute__((swift_name("shared")));
- (DyrectoSharedComparisonStrategy *)selectSceneMode:(DyrectoSharedSceneMode *)sceneMode primarySubject:(DyrectoSharedPrimarySubject * _Nullable)primarySubject detectionAvailable:(BOOL)detectionAvailable __attribute__((swift_name("select(sceneMode:primarySubject:detectionAvailable:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompositionAnalyzer")))
@interface DyrectoSharedCompositionAnalyzer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)compositionAnalyzer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCompositionAnalyzer *shared __attribute__((swift_name("shared")));
- (float)deltaA:(DyrectoSharedKotlinFloatArray *)a b:(DyrectoSharedKotlinFloatArray *)b __attribute__((swift_name("delta(a:b:)")));
- (DyrectoSharedKotlinFloatArray *)gridSignatureSubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects __attribute__((swift_name("gridSignature(subjects:)")));
@property (readonly) int32_t CELLS __attribute__((swift_name("CELLS")));
@property (readonly) int32_t GRID __attribute__((swift_name("GRID")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DetectedObject")))
@interface DyrectoSharedDetectedObject : DyrectoSharedBase
- (instancetype)initWithClassName:(NSString *)className classId:(int32_t)classId confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect *)boundingBox __attribute__((swift_name("init(className:classId:confidence:boundingBox:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedDetectedObject *)doCopyClassName:(NSString *)className classId:(int32_t)classId confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect *)boundingBox __attribute__((swift_name("doCopy(className:classId:confidence:boundingBox:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect *boundingBox __attribute__((swift_name("boundingBox")));
@property (readonly) int32_t classId __attribute__((swift_name("classId")));
@property (readonly) NSString *className __attribute__((swift_name("className")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) float normalizedArea __attribute__((swift_name("normalizedArea")));
@property (readonly) float normalizedCenterX __attribute__((swift_name("normalizedCenterX")));
@property (readonly) float normalizedCenterY __attribute__((swift_name("normalizedCenterY")));
@property (readonly) float normalizedHeight __attribute__((swift_name("normalizedHeight")));
@property (readonly) float normalizedWidth __attribute__((swift_name("normalizedWidth")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EmbeddingMath")))
@interface DyrectoSharedEmbeddingMath : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)embeddingMath __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedEmbeddingMath *shared __attribute__((swift_name("shared")));
- (float)cosineSimilarityA:(DyrectoSharedKotlinFloatArray *)a b:(DyrectoSharedKotlinFloatArray *)b __attribute__((swift_name("cosineSimilarity(a:b:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EngineStatus")))
@interface DyrectoSharedEngineStatus : DyrectoSharedKotlinEnum<DyrectoSharedEngineStatus *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedEngineStatus *notLoaded __attribute__((swift_name("notLoaded")));
@property (class, readonly) DyrectoSharedEngineStatus *ready __attribute__((swift_name("ready")));
@property (class, readonly) DyrectoSharedEngineStatus *failed __attribute__((swift_name("failed")));
+ (DyrectoSharedKotlinArray<DyrectoSharedEngineStatus *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedEngineStatus *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PrimarySubject")))
@interface DyrectoSharedPrimarySubject : DyrectoSharedBase
- (instancetype)initWithType:(DyrectoSharedPrimarySubjectType *)type category:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect * _Nullable)boundingBox maskAvailable:(BOOL)maskAvailable normalizedCenterX:(float)normalizedCenterX normalizedCenterY:(float)normalizedCenterY normalizedArea:(float)normalizedArea __attribute__((swift_name("init(type:category:rawLabel:confidence:boundingBox:maskAvailable:normalizedCenterX:normalizedCenterY:normalizedArea:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPrimarySubject *)doCopyType:(DyrectoSharedPrimarySubjectType *)type category:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect * _Nullable)boundingBox maskAvailable:(BOOL)maskAvailable normalizedCenterX:(float)normalizedCenterX normalizedCenterY:(float)normalizedCenterY normalizedArea:(float)normalizedArea __attribute__((swift_name("doCopy(type:category:rawLabel:confidence:boundingBox:maskAvailable:normalizedCenterX:normalizedCenterY:normalizedArea:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable boundingBox __attribute__((swift_name("boundingBox")));
@property (readonly) DyrectoSharedSubjectCategory *category __attribute__((swift_name("category")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) BOOL maskAvailable __attribute__((swift_name("maskAvailable")));
@property (readonly) float normalizedArea __attribute__((swift_name("normalizedArea")));
@property (readonly) float normalizedCenterX __attribute__((swift_name("normalizedCenterX")));
@property (readonly) float normalizedCenterY __attribute__((swift_name("normalizedCenterY")));
@property (readonly) NSString * _Nullable rawLabel __attribute__((swift_name("rawLabel")));
@property (readonly) DyrectoSharedPrimarySubjectType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PrimarySubjectSelector")))
@interface DyrectoSharedPrimarySubjectSelector : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)primarySubjectSelector __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPrimarySubjectSelector *shared __attribute__((swift_name("shared")));
- (DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)selectSubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects __attribute__((swift_name("select(subjects:)")));
- (DyrectoSharedPrimarySubjectType *)typeForCategory:(DyrectoSharedSubjectCategory *)category __attribute__((swift_name("typeFor(category:)")));
@property (readonly) float DOMINANCE_RATIO __attribute__((swift_name("DOMINANCE_RATIO")));
@property (readonly) float IMPORTANCE_RATIO __attribute__((swift_name("IMPORTANCE_RATIO")));
@property (readonly) float MIN_CONFIDENCE __attribute__((swift_name("MIN_CONFIDENCE")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PrimarySubjectType")))
@interface DyrectoSharedPrimarySubjectType : DyrectoSharedKotlinEnum<DyrectoSharedPrimarySubjectType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedPrimarySubjectType *person __attribute__((swift_name("person")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *animal __attribute__((swift_name("animal")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *vehicle __attribute__((swift_name("vehicle")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *product __attribute__((swift_name("product")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *food __attribute__((swift_name("food")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *building __attribute__((swift_name("building")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *multiple __attribute__((swift_name("multiple")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedPrimarySubjectType *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedPrimarySubjectType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedPrimarySubjectType *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneMode")))
@interface DyrectoSharedSceneMode : DyrectoSharedKotlinEnum<DyrectoSharedSceneMode *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSceneMode *human __attribute__((swift_name("human")));
@property (class, readonly) DyrectoSharedSceneMode *animal __attribute__((swift_name("animal")));
@property (class, readonly) DyrectoSharedSceneMode *vehicle __attribute__((swift_name("vehicle")));
@property (class, readonly) DyrectoSharedSceneMode *product __attribute__((swift_name("product")));
@property (class, readonly) DyrectoSharedSceneMode *food __attribute__((swift_name("food")));
@property (class, readonly) DyrectoSharedSceneMode *architecture __attribute__((swift_name("architecture")));
@property (class, readonly) DyrectoSharedSceneMode *landscape __attribute__((swift_name("landscape")));
@property (class, readonly) DyrectoSharedSceneMode *street __attribute__((swift_name("street")));
@property (class, readonly) DyrectoSharedSceneMode *interior __attribute__((swift_name("interior")));
@property (class, readonly) DyrectoSharedSceneMode *multiSubject __attribute__((swift_name("multiSubject")));
@property (class, readonly) DyrectoSharedSceneMode *genericScene __attribute__((swift_name("genericScene")));
@property (class, readonly) DyrectoSharedSceneMode *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSceneMode *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSceneMode *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneModeClassifier")))
@interface DyrectoSharedSceneModeClassifier : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sceneModeClassifier __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneModeClassifier *shared __attribute__((swift_name("shared")));
- (DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *)classifySubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects primary:(DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)primary embeddingAvailable:(BOOL)embeddingAvailable __attribute__((swift_name("classify(subjects:primary:embeddingAvailable:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SegmentationResult")))
@interface DyrectoSharedSegmentationResult : DyrectoSharedBase
- (instancetype)initWithMaskAvailable:(BOOL)maskAvailable coverage:(float)coverage maskBox:(DyrectoSharedNormalizedRect * _Nullable)maskBox source:(DyrectoSharedSegmentationSource *)source __attribute__((swift_name("init(maskAvailable:coverage:maskBox:source:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSegmentationResult *)doCopyMaskAvailable:(BOOL)maskAvailable coverage:(float)coverage maskBox:(DyrectoSharedNormalizedRect * _Nullable)maskBox source:(DyrectoSharedSegmentationSource *)source __attribute__((swift_name("doCopy(maskAvailable:coverage:maskBox:source:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float coverage __attribute__((swift_name("coverage")));
@property (readonly) BOOL maskAvailable __attribute__((swift_name("maskAvailable")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable maskBox __attribute__((swift_name("maskBox")));
@property (readonly) DyrectoSharedSegmentationSource *source __attribute__((swift_name("source")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SegmentationSource")))
@interface DyrectoSharedSegmentationSource : DyrectoSharedKotlinEnum<DyrectoSharedSegmentationSource *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSegmentationSource *model __attribute__((swift_name("model")));
@property (class, readonly) DyrectoSharedSegmentationSource *bboxFallback __attribute__((swift_name("bboxFallback")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSegmentationSource *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSegmentationSource *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectCategory")))
@interface DyrectoSharedSubjectCategory : DyrectoSharedKotlinEnum<DyrectoSharedSubjectCategory *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSubjectCategory *human __attribute__((swift_name("human")));
@property (class, readonly) DyrectoSharedSubjectCategory *animal __attribute__((swift_name("animal")));
@property (class, readonly) DyrectoSharedSubjectCategory *vehicle __attribute__((swift_name("vehicle")));
@property (class, readonly) DyrectoSharedSubjectCategory *product __attribute__((swift_name("product")));
@property (class, readonly) DyrectoSharedSubjectCategory *building __attribute__((swift_name("building")));
@property (class, readonly) DyrectoSharedSubjectCategory *food __attribute__((swift_name("food")));
@property (class, readonly) DyrectoSharedSubjectCategory *furniture __attribute__((swift_name("furniture")));
@property (class, readonly) DyrectoSharedSubjectCategory *electronics __attribute__((swift_name("electronics")));
@property (class, readonly) DyrectoSharedSubjectCategory *nature __attribute__((swift_name("nature")));
@property (class, readonly) DyrectoSharedSubjectCategory *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSubjectCategory *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSubjectCategory *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectMatcher")))
@interface DyrectoSharedSubjectMatcher : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)subjectMatcher __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSubjectMatcher *shared __attribute__((swift_name("shared")));
- (float)iouA:(DyrectoSharedNormalizedRect *)a b:(DyrectoSharedNormalizedRect *)b __attribute__((swift_name("iou(a:b:)")));
- (DyrectoSharedSubjectMatcherMatch * _Nullable)matchExpected:(DyrectoSharedSubjectMatcherExpected *)expected candidates:(NSArray<DyrectoSharedSceneSubject *> *)candidates weights:(DyrectoSharedSubjectMatcherMatchWeights *)weights appearanceScore:(DyrectoSharedFloat *(^ _Nullable)(DyrectoSharedSceneSubject *))appearanceScore __attribute__((swift_name("match(expected:candidates:weights:appearanceScore:)")));
@property (readonly) DyrectoSharedSubjectMatcherMatchWeights *DEFAULT_WEIGHTS __attribute__((swift_name("DEFAULT_WEIGHTS")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectMatcher.Expected")))
@interface DyrectoSharedSubjectMatcherExpected : DyrectoSharedBase
- (instancetype)initWithCategory:(DyrectoSharedSubjectCategory *)category boundingBox:(DyrectoSharedNormalizedRect *)boundingBox rawLabel:(NSString * _Nullable)rawLabel trackId:(int32_t)trackId __attribute__((swift_name("init(category:boundingBox:rawLabel:trackId:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSubjectMatcherExpected *)doCopyCategory:(DyrectoSharedSubjectCategory *)category boundingBox:(DyrectoSharedNormalizedRect *)boundingBox rawLabel:(NSString * _Nullable)rawLabel trackId:(int32_t)trackId __attribute__((swift_name("doCopy(category:boundingBox:rawLabel:trackId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect *boundingBox __attribute__((swift_name("boundingBox")));
@property (readonly) DyrectoSharedSubjectCategory *category __attribute__((swift_name("category")));
@property (readonly) NSString * _Nullable rawLabel __attribute__((swift_name("rawLabel")));
@property (readonly) int32_t trackId __attribute__((swift_name("trackId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectMatcher.Match")))
@interface DyrectoSharedSubjectMatcherMatch : DyrectoSharedBase
- (instancetype)initWithSubject:(DyrectoSharedSceneSubject *)subject score:(float)score __attribute__((swift_name("init(subject:score:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSubjectMatcherMatch *)doCopySubject:(DyrectoSharedSceneSubject *)subject score:(float)score __attribute__((swift_name("doCopy(subject:score:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float score __attribute__((swift_name("score")));
@property (readonly) DyrectoSharedSceneSubject *subject __attribute__((swift_name("subject")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectMatcher.MatchWeights")))
@interface DyrectoSharedSubjectMatcherMatchWeights : DyrectoSharedBase
- (instancetype)initWithIou:(float)iou centerDistance:(float)centerDistance appearance:(float)appearance trackIdentity:(float)trackIdentity labelAffinity:(float)labelAffinity minScore:(float)minScore __attribute__((swift_name("init(iou:centerDistance:appearance:trackIdentity:labelAffinity:minScore:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSubjectMatcherMatchWeights *)doCopyIou:(float)iou centerDistance:(float)centerDistance appearance:(float)appearance trackIdentity:(float)trackIdentity labelAffinity:(float)labelAffinity minScore:(float)minScore __attribute__((swift_name("doCopy(iou:centerDistance:appearance:trackIdentity:labelAffinity:minScore:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float appearance __attribute__((swift_name("appearance")));
@property (readonly) float centerDistance __attribute__((swift_name("centerDistance")));
@property (readonly) float iou __attribute__((swift_name("iou")));
@property (readonly) float labelAffinity __attribute__((swift_name("labelAffinity")));
@property (readonly) float minScore __attribute__((swift_name("minScore")));
@property (readonly) float trackIdentity __attribute__((swift_name("trackIdentity")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VisualEmbeddingResult")))
@interface DyrectoSharedVisualEmbeddingResult : DyrectoSharedBase
- (instancetype)initWithEmbedding:(DyrectoSharedKotlinFloatArray *)embedding modelId:(NSString *)modelId confidence:(float)confidence __attribute__((swift_name("init(embedding:modelId:confidence:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVisualEmbeddingResult *)doCopyEmbedding:(DyrectoSharedKotlinFloatArray *)embedding modelId:(NSString *)modelId confidence:(float)confidence __attribute__((swift_name("doCopy(embedding:modelId:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) int32_t dimensions __attribute__((swift_name("dimensions")));
@property (readonly) DyrectoSharedKotlinFloatArray *embedding __attribute__((swift_name("embedding")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DetectionManager")))
@interface DyrectoSharedDetectionManager : DyrectoSharedBase
- (instancetype)initWithEngine:(id<DyrectoSharedObjectDetectorEngine>)engine mapper:(id<DyrectoSharedSemanticMapper>)mapper __attribute__((swift_name("init(engine:mapper:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)detectBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(NSArray<DyrectoSharedSceneSubject *> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("detect(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@property (readonly) int64_t lastDurationMs __attribute__((swift_name("lastDurationMs")));
@property (readonly) int64_t runCount __attribute__((swift_name("runCount")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EmbeddingManager")))
@interface DyrectoSharedEmbeddingManager : DyrectoSharedBase
- (instancetype)initWithEngine:(id<DyrectoSharedEmbeddingEngine>)engine __attribute__((swift_name("init(engine:)"))) __attribute__((objc_designated_initializer));
- (BOOL)canCompareStoredModelId:(NSString * _Nullable)storedModelId storedDimensions:(int32_t)storedDimensions live:(DyrectoSharedVisualEmbeddingResult * _Nullable)live __attribute__((swift_name("canCompare(storedModelId:storedDimensions:live:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)embedBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedVisualEmbeddingResult * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("embed(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@property (readonly) int64_t lastDurationMs __attribute__((swift_name("lastDurationMs")));
@property (readonly) int64_t runCount __attribute__((swift_name("runCount")));
@end

__attribute__((swift_name("EmbeddingEngine")))
@protocol DyrectoSharedEmbeddingEngine
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)embedBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedVisualEmbeddingResult * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("embed(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@end

__attribute__((swift_name("ObjectDetectorEngine")))
@protocol DyrectoSharedObjectDetectorEngine
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)detectBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(NSArray<DyrectoSharedDetectedObject *> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("detect(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@end

__attribute__((swift_name("SegmentationEngine")))
@protocol DyrectoSharedSegmentationEngine
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)segmentBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedSegmentationResult * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("segment(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SegmentationManager")))
@interface DyrectoSharedSegmentationManager : DyrectoSharedBase
- (instancetype)initWithEngine:(id<DyrectoSharedSegmentationEngine>)engine __attribute__((swift_name("init(engine:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSegmentationResult *)bboxFallbackBox:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("bboxFallback(box:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)segmentBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedSegmentationResult * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("segment(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@property (readonly) int64_t lastDurationMs __attribute__((swift_name("lastDurationMs")));
@end

__attribute__((swift_name("SemanticMapper")))
@protocol DyrectoSharedSemanticMapper
@required
- (DyrectoSharedSubjectCategory *)mapRawLabel:(NSString *)rawLabel classId:(int32_t)classId __attribute__((swift_name("map(rawLabel:classId:)")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CocoSemanticMapper")))
@interface DyrectoSharedCocoSemanticMapper : DyrectoSharedBase <DyrectoSharedSemanticMapper>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)cocoSemanticMapper __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCocoSemanticMapper *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSubjectCategory *)mapRawLabel:(NSString *)rawLabel classId:(int32_t)classId __attribute__((swift_name("map(rawLabel:classId:)")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FeatureValue")))
@interface DyrectoSharedFeatureValue<T> : DyrectoSharedBase
- (instancetype)initWithValue:(T _Nullable)value confidence:(float)confidence __attribute__((swift_name("init(value:confidence:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedFeatureValueCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedFeatureValue<T> *)doCopyValue:(T _Nullable)value confidence:(float)confidence __attribute__((swift_name("doCopy(value:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) BOOL present __attribute__((swift_name("present")));
@property (readonly) T _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FeatureValueCompanion")))
@interface DyrectoSharedFeatureValueCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedFeatureValueCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedFeatureValue<id> *)absent __attribute__((swift_name("absent()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NormalizedPoint")))
@interface DyrectoSharedNormalizedPoint : DyrectoSharedBase
- (instancetype)initWithX:(float)x y:(float)y __attribute__((swift_name("init(x:y:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedNormalizedPoint *)doCopyX:(float)x y:(float)y __attribute__((swift_name("doCopy(x:y:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float x __attribute__((swift_name("x")));
@property (readonly) float y __attribute__((swift_name("y")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSnapshot")))
@interface DyrectoSharedSceneSnapshot : DyrectoSharedBase
- (instancetype)initWithSubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects primarySubject:(DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)primarySubject sceneMode:(DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *)sceneMode subjectPosition:(DyrectoSharedFeatureValue<DyrectoSharedNormalizedPoint *> *)subjectPosition subjectSize:(DyrectoSharedFeatureValue<DyrectoSharedFloat *> *)subjectSize layoutSignature:(DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *)layoutSignature embedding:(DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *)embedding embeddingModelId:(NSString * _Nullable)embeddingModelId segmentationSummary:(DyrectoSharedSegmentationSummary * _Nullable)segmentationSummary source:(DyrectoSharedSnapshotSource *)source analyzedAtMs:(int64_t)analyzedAtMs __attribute__((swift_name("init(subjects:primarySubject:sceneMode:subjectPosition:subjectSize:layoutSignature:embedding:embeddingModelId:segmentationSummary:source:analyzedAtMs:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSceneSnapshotCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSceneSnapshot *)doCopySubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects primarySubject:(DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)primarySubject sceneMode:(DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *)sceneMode subjectPosition:(DyrectoSharedFeatureValue<DyrectoSharedNormalizedPoint *> *)subjectPosition subjectSize:(DyrectoSharedFeatureValue<DyrectoSharedFloat *> *)subjectSize layoutSignature:(DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *)layoutSignature embedding:(DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *)embedding embeddingModelId:(NSString * _Nullable)embeddingModelId segmentationSummary:(DyrectoSharedSegmentationSummary * _Nullable)segmentationSummary source:(DyrectoSharedSnapshotSource *)source analyzedAtMs:(int64_t)analyzedAtMs __attribute__((swift_name("doCopy(subjects:primarySubject:sceneMode:subjectPosition:subjectSize:layoutSignature:embedding:embeddingModelId:segmentationSummary:source:analyzedAtMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t analyzedAtMs __attribute__((swift_name("analyzedAtMs")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *embedding __attribute__((swift_name("embedding")));
@property (readonly) NSString * _Nullable embeddingModelId __attribute__((swift_name("embeddingModelId")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedKotlinFloatArray *> *layoutSignature __attribute__((swift_name("layoutSignature")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *primarySubject __attribute__((swift_name("primarySubject")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *sceneMode __attribute__((swift_name("sceneMode")));
@property (readonly) DyrectoSharedSegmentationSummary * _Nullable segmentationSummary __attribute__((swift_name("segmentationSummary")));
@property (readonly) DyrectoSharedSnapshotSource *source __attribute__((swift_name("source")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedNormalizedPoint *> *subjectPosition __attribute__((swift_name("subjectPosition")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedFloat *> *subjectSize __attribute__((swift_name("subjectSize")));
@property (readonly) NSArray<DyrectoSharedSceneSubject *> *subjects __attribute__((swift_name("subjects")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSnapshot.Companion")))
@interface DyrectoSharedSceneSnapshotCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneSnapshotCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSceneSnapshot *)emptyNowMs:(int64_t)nowMs __attribute__((swift_name("empty(nowMs:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSubject")))
@interface DyrectoSharedSceneSubject : DyrectoSharedBase
- (instancetype)initWithTrackId:(int32_t)trackId category:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect *)boundingBox tracked:(BOOL)tracked maskAvailable:(BOOL)maskAvailable __attribute__((swift_name("init(trackId:category:rawLabel:confidence:boundingBox:tracked:maskAvailable:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSceneSubjectCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSceneSubject *)doCopyTrackId:(int32_t)trackId category:(DyrectoSharedSubjectCategory *)category rawLabel:(NSString * _Nullable)rawLabel confidence:(float)confidence boundingBox:(DyrectoSharedNormalizedRect *)boundingBox tracked:(BOOL)tracked maskAvailable:(BOOL)maskAvailable __attribute__((swift_name("doCopy(trackId:category:rawLabel:confidence:boundingBox:tracked:maskAvailable:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect *boundingBox __attribute__((swift_name("boundingBox")));
@property (readonly) DyrectoSharedSubjectCategory *category __attribute__((swift_name("category")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) BOOL maskAvailable __attribute__((swift_name("maskAvailable")));
@property (readonly) float normalizedArea __attribute__((swift_name("normalizedArea")));
@property (readonly) float normalizedCenterX __attribute__((swift_name("normalizedCenterX")));
@property (readonly) float normalizedCenterY __attribute__((swift_name("normalizedCenterY")));
@property (readonly) NSString * _Nullable rawLabel __attribute__((swift_name("rawLabel")));
@property (readonly) int32_t trackId __attribute__((swift_name("trackId")));
@property (readonly) BOOL tracked __attribute__((swift_name("tracked")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSubject.Companion")))
@interface DyrectoSharedSceneSubjectCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneSubjectCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t NO_TRACK __attribute__((swift_name("NO_TRACK")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SegmentationSummary")))
@interface DyrectoSharedSegmentationSummary : DyrectoSharedBase
- (instancetype)initWithCoverage:(float)coverage maskBox:(DyrectoSharedNormalizedRect * _Nullable)maskBox pixelAccurate:(BOOL)pixelAccurate __attribute__((swift_name("init(coverage:maskBox:pixelAccurate:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSegmentationSummary *)doCopyCoverage:(float)coverage maskBox:(DyrectoSharedNormalizedRect * _Nullable)maskBox pixelAccurate:(BOOL)pixelAccurate __attribute__((swift_name("doCopy(coverage:maskBox:pixelAccurate:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float coverage __attribute__((swift_name("coverage")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable maskBox __attribute__((swift_name("maskBox")));
@property (readonly) BOOL pixelAccurate __attribute__((swift_name("pixelAccurate")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SnapshotSource")))
@interface DyrectoSharedSnapshotSource : DyrectoSharedKotlinEnum<DyrectoSharedSnapshotSource *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSnapshotSource *tracker __attribute__((swift_name("tracker")));
@property (class, readonly) DyrectoSharedSnapshotSource *detector __attribute__((swift_name("detector")));
@property (class, readonly) DyrectoSharedSnapshotSource *embeddingOnly __attribute__((swift_name("embeddingOnly")));
@property (class, readonly) DyrectoSharedSnapshotSource *none __attribute__((swift_name("none")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSnapshotSource *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSnapshotSource *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneClassification")))
@interface DyrectoSharedSceneClassification : DyrectoSharedBase
- (instancetype)initWithSceneMode:(DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *)sceneMode primarySubject:(DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)primarySubject strategy:(DyrectoSharedComparisonStrategy *)strategy __attribute__((swift_name("init(sceneMode:primarySubject:strategy:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneClassification *)doCopySceneMode:(DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *)sceneMode primarySubject:(DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *)primarySubject strategy:(DyrectoSharedComparisonStrategy *)strategy __attribute__((swift_name("doCopy(sceneMode:primarySubject:strategy:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedPrimarySubject *> *primarySubject __attribute__((swift_name("primarySubject")));
@property (readonly) DyrectoSharedFeatureValue<DyrectoSharedSceneMode *> *sceneMode __attribute__((swift_name("sceneMode")));
@property (readonly) DyrectoSharedComparisonStrategy *strategy __attribute__((swift_name("strategy")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StrategyManager")))
@interface DyrectoSharedStrategyManager : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedSceneClassification *)classifySubjects:(NSArray<DyrectoSharedSceneSubject *> *)subjects embeddingAvailable:(BOOL)embeddingAvailable detectionAvailable:(BOOL)detectionAvailable __attribute__((swift_name("classify(subjects:embeddingAvailable:detectionAvailable:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DetectorTrigger")))
@interface DyrectoSharedDetectorTrigger : DyrectoSharedKotlinEnum<DyrectoSharedDetectorTrigger *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedDetectorTrigger *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedDetectorTrigger *theInit __attribute__((swift_name("theInit")));
@property (class, readonly) DyrectoSharedDetectorTrigger *lossRecovery __attribute__((swift_name("lossRecovery")));
@property (class, readonly) DyrectoSharedDetectorTrigger *sceneChange __attribute__((swift_name("sceneChange")));
@property (class, readonly) DyrectoSharedDetectorTrigger *periodicVerify __attribute__((swift_name("periodicVerify")));
+ (DyrectoSharedKotlinArray<DyrectoSharedDetectorTrigger *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedDetectorTrigger *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LumaFrame")))
@interface DyrectoSharedLumaFrame : DyrectoSharedBase
- (instancetype)initWithWidth:(int32_t)width height:(int32_t)height luma:(DyrectoSharedKotlinIntArray *)luma __attribute__((swift_name("init(width:height:luma:)"))) __attribute__((objc_designated_initializer));
- (float)meanLuma __attribute__((swift_name("meanLuma()")));
@property (readonly) int32_t height __attribute__((swift_name("height")));
@property (readonly) DyrectoSharedKotlinIntArray *luma __attribute__((swift_name("luma")));
@property (readonly) int32_t width __attribute__((swift_name("width")));
@end

__attribute__((swift_name("ObjectTracker")))
@protocol DyrectoSharedObjectTracker
@required
- (id<DyrectoSharedTrackHandle> _Nullable)doInitFrame:(DyrectoSharedLumaFrame *)frame box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("doInit(frame:box:)")));
- (void)refreshHandle:(id<DyrectoSharedTrackHandle>)handle frame:(DyrectoSharedLumaFrame *)frame box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("refresh(handle:frame:box:)")));
- (void)releaseHandle:(id<DyrectoSharedTrackHandle>)handle __attribute__((swift_name("release(handle:)")));
- (DyrectoSharedTrackUpdate *)updateHandle:(id<DyrectoSharedTrackHandle>)handle frame:(DyrectoSharedLumaFrame *)frame __attribute__((swift_name("update(handle:frame:)")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NccTemplateTracker")))
@interface DyrectoSharedNccTemplateTracker : DyrectoSharedBase <DyrectoSharedObjectTracker>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (id<DyrectoSharedTrackHandle> _Nullable)doInitFrame:(DyrectoSharedLumaFrame *)frame box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("doInit(frame:box:)")));
- (void)refreshHandle:(id<DyrectoSharedTrackHandle>)handle frame:(DyrectoSharedLumaFrame *)frame box:(DyrectoSharedNormalizedRect *)box __attribute__((swift_name("refresh(handle:frame:box:)")));
- (void)releaseHandle:(id<DyrectoSharedTrackHandle>)handle __attribute__((swift_name("release(handle:)")));
- (DyrectoSharedTrackUpdate *)updateHandle:(id<DyrectoSharedTrackHandle>)handle frame:(DyrectoSharedLumaFrame *)frame __attribute__((swift_name("update(handle:frame:)")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@end

__attribute__((swift_name("TrackHandle")))
@protocol DyrectoSharedTrackHandle
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TrackPolicy")))
@interface DyrectoSharedTrackPolicy : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)trackPolicy __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedTrackPolicy *shared __attribute__((swift_name("shared")));
- (DyrectoSharedDetectorTrigger *)decideInput:(DyrectoSharedTrackPolicyInput *)input config:(DyrectoSharedTrackPolicyConfig *)config __attribute__((swift_name("decide(input:config:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TrackPolicy.Input")))
@interface DyrectoSharedTrackPolicyInput : DyrectoSharedBase
- (instancetype)initWithMonitoringActive:(BOOL)monitoringActive hasActiveTrack:(BOOL)hasActiveTrack consecutiveLowConfidenceFrames:(int32_t)consecutiveLowConfidenceFrames msSinceLastVerify:(int64_t)msSinceLastVerify embeddingSimilarityToVerified:(DyrectoSharedFloat * _Nullable)embeddingSimilarityToVerified meanLumaDeltaSinceVerified:(DyrectoSharedFloat * _Nullable)meanLumaDeltaSinceVerified __attribute__((swift_name("init(monitoringActive:hasActiveTrack:consecutiveLowConfidenceFrames:msSinceLastVerify:embeddingSimilarityToVerified:meanLumaDeltaSinceVerified:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedTrackPolicyInput *)doCopyMonitoringActive:(BOOL)monitoringActive hasActiveTrack:(BOOL)hasActiveTrack consecutiveLowConfidenceFrames:(int32_t)consecutiveLowConfidenceFrames msSinceLastVerify:(int64_t)msSinceLastVerify embeddingSimilarityToVerified:(DyrectoSharedFloat * _Nullable)embeddingSimilarityToVerified meanLumaDeltaSinceVerified:(DyrectoSharedFloat * _Nullable)meanLumaDeltaSinceVerified __attribute__((swift_name("doCopy(monitoringActive:hasActiveTrack:consecutiveLowConfidenceFrames:msSinceLastVerify:embeddingSimilarityToVerified:meanLumaDeltaSinceVerified:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t consecutiveLowConfidenceFrames __attribute__((swift_name("consecutiveLowConfidenceFrames")));
@property (readonly) DyrectoSharedFloat * _Nullable embeddingSimilarityToVerified __attribute__((swift_name("embeddingSimilarityToVerified")));
@property (readonly) BOOL hasActiveTrack __attribute__((swift_name("hasActiveTrack")));
@property (readonly) DyrectoSharedFloat * _Nullable meanLumaDeltaSinceVerified __attribute__((swift_name("meanLumaDeltaSinceVerified")));
@property (readonly) BOOL monitoringActive __attribute__((swift_name("monitoringActive")));
@property (readonly) int64_t msSinceLastVerify __attribute__((swift_name("msSinceLastVerify")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TrackPolicyConfig")))
@interface DyrectoSharedTrackPolicyConfig : DyrectoSharedBase
- (instancetype)initWithLossConfidenceThreshold:(float)lossConfidenceThreshold lossFramesToDeclare:(int32_t)lossFramesToDeclare verifyIntervalMs:(int64_t)verifyIntervalMs sceneChangeSimilarityFloor:(float)sceneChangeSimilarityFloor sceneChangeLumaDelta:(float)sceneChangeLumaDelta __attribute__((swift_name("init(lossConfidenceThreshold:lossFramesToDeclare:verifyIntervalMs:sceneChangeSimilarityFloor:sceneChangeLumaDelta:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedTrackPolicyConfig *)doCopyLossConfidenceThreshold:(float)lossConfidenceThreshold lossFramesToDeclare:(int32_t)lossFramesToDeclare verifyIntervalMs:(int64_t)verifyIntervalMs sceneChangeSimilarityFloor:(float)sceneChangeSimilarityFloor sceneChangeLumaDelta:(float)sceneChangeLumaDelta __attribute__((swift_name("doCopy(lossConfidenceThreshold:lossFramesToDeclare:verifyIntervalMs:sceneChangeSimilarityFloor:sceneChangeLumaDelta:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float lossConfidenceThreshold __attribute__((swift_name("lossConfidenceThreshold")));
@property (readonly) int32_t lossFramesToDeclare __attribute__((swift_name("lossFramesToDeclare")));
@property (readonly) float sceneChangeLumaDelta __attribute__((swift_name("sceneChangeLumaDelta")));
@property (readonly) float sceneChangeSimilarityFloor __attribute__((swift_name("sceneChangeSimilarityFloor")));
@property (readonly) int64_t verifyIntervalMs __attribute__((swift_name("verifyIntervalMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TrackUpdate")))
@interface DyrectoSharedTrackUpdate : DyrectoSharedBase
- (instancetype)initWithBox:(DyrectoSharedNormalizedRect *)box confidence:(float)confidence __attribute__((swift_name("init(box:confidence:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedTrackUpdate *)doCopyBox:(DyrectoSharedNormalizedRect *)box confidence:(float)confidence __attribute__((swift_name("doCopy(box:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedNormalizedRect *box __attribute__((swift_name("box")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TrackingManager")))
@interface DyrectoSharedTrackingManager : DyrectoSharedBase
- (instancetype)initWithTracker:(id<DyrectoSharedObjectTracker>)tracker policyConfig:(DyrectoSharedTrackPolicyConfig *)policyConfig __attribute__((swift_name("init(tracker:policyConfig:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedDetectorTrigger *)detectorTriggerMonitoringActive:(BOOL)monitoringActive nowMs:(int64_t)nowMs embeddingSimilarityToVerified:(DyrectoSharedFloat * _Nullable)embeddingSimilarityToVerified meanLumaDeltaSinceVerified:(DyrectoSharedFloat * _Nullable)meanLumaDeltaSinceVerified __attribute__((swift_name("detectorTrigger(monitoringActive:nowMs:embeddingSimilarityToVerified:meanLumaDeltaSinceVerified:)")));
- (void)expectSubjectExpected:(DyrectoSharedSubjectMatcherExpected * _Nullable)expected __attribute__((swift_name("expectSubject(expected:)")));
- (DyrectoSharedSceneSubject * _Nullable)onDetectionsFrame:(DyrectoSharedLumaFrame *)frame detections:(NSArray<DyrectoSharedSceneSubject *> *)detections nowMs:(int64_t)nowMs __attribute__((swift_name("onDetections(frame:detections:nowMs:)")));
- (DyrectoSharedSceneSubject * _Nullable)onTrackerFrameFrame:(DyrectoSharedLumaFrame *)frame __attribute__((swift_name("onTrackerFrame(frame:)")));
- (void)reset __attribute__((swift_name("reset()")));
@property (readonly) float currentConfidence __attribute__((swift_name("currentConfidence")));
@property (readonly) int32_t currentTrackId __attribute__((swift_name("currentTrackId")));
@property (readonly) int64_t detectorRuns __attribute__((swift_name("detectorRuns")));
@property (readonly) BOOL hasActiveTrack __attribute__((swift_name("hasActiveTrack")));
@property (readonly) NSString *trackerId __attribute__((swift_name("trackerId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraAngle")))
@interface DyrectoSharedCameraAngle : DyrectoSharedKotlinEnum<DyrectoSharedCameraAngle *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCameraAngle *eyeLevel __attribute__((swift_name("eyeLevel")));
@property (class, readonly) DyrectoSharedCameraAngle *highAngle __attribute__((swift_name("highAngle")));
@property (class, readonly) DyrectoSharedCameraAngle *lowAngle __attribute__((swift_name("lowAngle")));
@property (class, readonly) DyrectoSharedCameraAngle *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCameraAngle *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCameraAngle *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ColorTemperature")))
@interface DyrectoSharedColorTemperature : DyrectoSharedKotlinEnum<DyrectoSharedColorTemperature *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedColorTemperature *warm __attribute__((swift_name("warm")));
@property (class, readonly) DyrectoSharedColorTemperature *neutral __attribute__((swift_name("neutral")));
@property (class, readonly) DyrectoSharedColorTemperature *cool __attribute__((swift_name("cool")));
@property (class, readonly) DyrectoSharedColorTemperature *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedColorTemperature *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedColorTemperature *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeAspect")))
@interface DyrectoSharedCreativeAspect : DyrectoSharedKotlinEnum<DyrectoSharedCreativeAspect *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCreativeAspect *subject __attribute__((swift_name("subject")));
@property (class, readonly) DyrectoSharedCreativeAspect *subjectPlacement __attribute__((swift_name("subjectPlacement")));
@property (class, readonly) DyrectoSharedCreativeAspect *subjectScale __attribute__((swift_name("subjectScale")));
@property (class, readonly) DyrectoSharedCreativeAspect *headroom __attribute__((swift_name("headroom")));
@property (class, readonly) DyrectoSharedCreativeAspect *composition __attribute__((swift_name("composition")));
@property (class, readonly) DyrectoSharedCreativeAspect *negativeSpace __attribute__((swift_name("negativeSpace")));
@property (class, readonly) DyrectoSharedCreativeAspect *lighting __attribute__((swift_name("lighting")));
@property (class, readonly) DyrectoSharedCreativeAspect *color __attribute__((swift_name("color")));
@property (class, readonly) DyrectoSharedCreativeAspect *depthOfField __attribute__((swift_name("depthOfField")));
@property (class, readonly) DyrectoSharedCreativeAspect *background __attribute__((swift_name("background")));
@property (class, readonly) DyrectoSharedCreativeAspect *mood __attribute__((swift_name("mood")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCreativeAspect *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCreativeAspect *> *entries __attribute__((swift_name("entries")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeCamera")))
@interface DyrectoSharedCreativeCamera : DyrectoSharedBase
- (instancetype)initWithShotType:(DyrectoSharedShotType *)shotType angle:(DyrectoSharedCameraAngle *)angle shotTypeConfidence:(float)shotTypeConfidence angleConfidence:(float)angleConfidence __attribute__((swift_name("init(shotType:angle:shotTypeConfidence:angleConfidence:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeCameraCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeCamera *)doCopyShotType:(DyrectoSharedShotType *)shotType angle:(DyrectoSharedCameraAngle *)angle shotTypeConfidence:(float)shotTypeConfidence angleConfidence:(float)angleConfidence __attribute__((swift_name("doCopy(shotType:angle:shotTypeConfidence:angleConfidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCameraAngle *angle __attribute__((swift_name("angle")));
@property (readonly) float angleConfidence __attribute__((swift_name("angleConfidence")));
@property (readonly) DyrectoSharedShotType *shotType __attribute__((swift_name("shotType")));
@property (readonly) float shotTypeConfidence __attribute__((swift_name("shotTypeConfidence")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeCamera.Companion")))
@interface DyrectoSharedCreativeCameraCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeCameraCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeColor")))
@interface DyrectoSharedCreativeColor : DyrectoSharedBase
- (instancetype)initWithTemperature:(DyrectoSharedColorTemperature *)temperature tint:(DyrectoSharedTintCast *)tint confidence:(float)confidence __attribute__((swift_name("init(temperature:tint:confidence:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeColorCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeColor *)doCopyTemperature:(DyrectoSharedColorTemperature *)temperature tint:(DyrectoSharedTintCast *)tint confidence:(float)confidence __attribute__((swift_name("doCopy(temperature:tint:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedColorTemperature *temperature __attribute__((swift_name("temperature")));
@property (readonly) DyrectoSharedTintCast *tint __attribute__((swift_name("tint")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeColor.Companion")))
@interface DyrectoSharedCreativeColorCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeColorCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeComposition")))
@interface DyrectoSharedCreativeComposition : DyrectoSharedBase
- (instancetype)initWithPlacement:(DyrectoSharedSubjectPlacement *)placement negativeSpace:(DyrectoSharedNegativeSpace *)negativeSpace headroom:(DyrectoSharedHeadroomLevel *)headroom confidence:(float)confidence symmetry:(DyrectoSharedSymmetry * _Nullable)symmetry leadingLines:(DyrectoSharedBoolean * _Nullable)leadingLines __attribute__((swift_name("init(placement:negativeSpace:headroom:confidence:symmetry:leadingLines:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeCompositionCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeComposition *)doCopyPlacement:(DyrectoSharedSubjectPlacement *)placement negativeSpace:(DyrectoSharedNegativeSpace *)negativeSpace headroom:(DyrectoSharedHeadroomLevel *)headroom confidence:(float)confidence symmetry:(DyrectoSharedSymmetry * _Nullable)symmetry leadingLines:(DyrectoSharedBoolean * _Nullable)leadingLines __attribute__((swift_name("doCopy(placement:negativeSpace:headroom:confidence:symmetry:leadingLines:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedHeadroomLevel *headroom __attribute__((swift_name("headroom")));
@property (readonly) DyrectoSharedBoolean * _Nullable leadingLines __attribute__((swift_name("leadingLines")));
@property (readonly) DyrectoSharedNegativeSpace *negativeSpace __attribute__((swift_name("negativeSpace")));
@property (readonly) DyrectoSharedSubjectPlacement *placement __attribute__((swift_name("placement")));
@property (readonly) DyrectoSharedSymmetry * _Nullable symmetry __attribute__((swift_name("symmetry")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeComposition.Companion")))
@interface DyrectoSharedCreativeCompositionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeCompositionCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeDepth")))
@interface DyrectoSharedCreativeDepth : DyrectoSharedBase
- (instancetype)initWithDepthOfField:(DyrectoSharedDepthOfField *)depthOfField confidence:(float)confidence lensCharacter:(NSString * _Nullable)lensCharacter __attribute__((swift_name("init(depthOfField:confidence:lensCharacter:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeDepthCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeDepth *)doCopyDepthOfField:(DyrectoSharedDepthOfField *)depthOfField confidence:(float)confidence lensCharacter:(NSString * _Nullable)lensCharacter __attribute__((swift_name("doCopy(depthOfField:confidence:lensCharacter:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedDepthOfField *depthOfField __attribute__((swift_name("depthOfField")));
@property (readonly) NSString * _Nullable lensCharacter __attribute__((swift_name("lensCharacter")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeDepth.Companion")))
@interface DyrectoSharedCreativeDepthCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeDepthCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeLighting")))
@interface DyrectoSharedCreativeLighting : DyrectoSharedBase
- (instancetype)initWithKey:(DyrectoSharedLightingKey *)key contrast:(DyrectoSharedLightingContrast *)contrast confidence:(float)confidence backlightHint:(DyrectoSharedBoolean * _Nullable)backlightHint direction:(DyrectoSharedLightingDirection * _Nullable)direction __attribute__((swift_name("init(key:contrast:confidence:backlightHint:direction:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeLightingCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeLighting *)doCopyKey:(DyrectoSharedLightingKey *)key contrast:(DyrectoSharedLightingContrast *)contrast confidence:(float)confidence backlightHint:(DyrectoSharedBoolean * _Nullable)backlightHint direction:(DyrectoSharedLightingDirection * _Nullable)direction __attribute__((swift_name("doCopy(key:contrast:confidence:backlightHint:direction:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedBoolean * _Nullable backlightHint __attribute__((swift_name("backlightHint")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedLightingContrast *contrast __attribute__((swift_name("contrast")));
@property (readonly) DyrectoSharedLightingDirection * _Nullable direction __attribute__((swift_name("direction")));
@property (readonly) DyrectoSharedLightingKey *key __attribute__((swift_name("key")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeLighting.Companion")))
@interface DyrectoSharedCreativeLightingCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeLightingCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativePriority")))
@interface DyrectoSharedCreativePriority : DyrectoSharedBase
- (instancetype)initWithWeight:(float)weight reason:(NSString *)reason __attribute__((swift_name("init(weight:reason:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativePriorityCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativePriority *)doCopyWeight:(float)weight reason:(NSString *)reason __attribute__((swift_name("doCopy(weight:reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedPriorityLevel *level __attribute__((swift_name("level")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@property (readonly) float weight __attribute__((swift_name("weight")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativePriority.Companion")))
@interface DyrectoSharedCreativePriorityCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativePriorityCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSceneAnalyzer")))
@interface DyrectoSharedCreativeSceneAnalyzer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)creativeSceneAnalyzer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeSceneAnalyzer *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeSceneModel *)analyzeExposure:(DyrectoSharedReferenceExposureProfile *)exposure color:(DyrectoSharedReferenceColorProfile *)color ai:(DyrectoSharedReferenceAiProfile * _Nullable)ai faceBox:(DyrectoSharedNormalizedRect * _Nullable)faceBox semantic:(DyrectoSharedSemanticObservation * _Nullable)semantic __attribute__((swift_name("analyze(exposure:color:ai:faceBox:semantic:)")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSceneModel")))
@interface DyrectoSharedCreativeSceneModel : DyrectoSharedBase
- (instancetype)initWithSubject:(DyrectoSharedCreativeSubject *)subject camera:(DyrectoSharedCreativeCamera *)camera composition:(DyrectoSharedCreativeComposition *)composition lighting:(DyrectoSharedCreativeLighting *)lighting color:(DyrectoSharedCreativeColor *)color depth:(DyrectoSharedCreativeDepth *)depth style:(DyrectoSharedCreativeStyle *)style importance:(NSDictionary<DyrectoSharedCreativeAspect *, DyrectoSharedCreativePriority *> *)importance relationships:(NSArray<DyrectoSharedSceneRelationship *> *)relationships signature:(DyrectoSharedCreativeSignature * _Nullable)signature semantics:(DyrectoSharedSceneSemantics * _Nullable)semantics identity:(DyrectoSharedShotIdentity * _Nullable)identity schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("init(subject:camera:composition:lighting:color:depth:style:importance:relationships:signature:semantics:identity:schemaVersion:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeSceneModelCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeSceneModel *)doCopySubject:(DyrectoSharedCreativeSubject *)subject camera:(DyrectoSharedCreativeCamera *)camera composition:(DyrectoSharedCreativeComposition *)composition lighting:(DyrectoSharedCreativeLighting *)lighting color:(DyrectoSharedCreativeColor *)color depth:(DyrectoSharedCreativeDepth *)depth style:(DyrectoSharedCreativeStyle *)style importance:(NSDictionary<DyrectoSharedCreativeAspect *, DyrectoSharedCreativePriority *> *)importance relationships:(NSArray<DyrectoSharedSceneRelationship *> *)relationships signature:(DyrectoSharedCreativeSignature * _Nullable)signature semantics:(DyrectoSharedSceneSemantics * _Nullable)semantics identity:(DyrectoSharedShotIdentity * _Nullable)identity schemaVersion:(int32_t)schemaVersion __attribute__((swift_name("doCopy(subject:camera:composition:lighting:color:depth:style:importance:relationships:signature:semantics:identity:schemaVersion:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCreativeCamera *camera __attribute__((swift_name("camera")));
@property (readonly) DyrectoSharedCreativeColor *color __attribute__((swift_name("color")));
@property (readonly) DyrectoSharedCreativeComposition *composition __attribute__((swift_name("composition")));
@property (readonly) DyrectoSharedCreativeDepth *depth __attribute__((swift_name("depth")));
@property (readonly) DyrectoSharedShotIdentity * _Nullable identity __attribute__((swift_name("identity")));
@property (readonly) NSDictionary<DyrectoSharedCreativeAspect *, DyrectoSharedCreativePriority *> *importance __attribute__((swift_name("importance")));
@property (readonly) DyrectoSharedCreativeLighting *lighting __attribute__((swift_name("lighting")));
@property (readonly) NSArray<DyrectoSharedSceneRelationship *> *relationships __attribute__((swift_name("relationships")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));
@property (readonly) DyrectoSharedSceneSemantics * _Nullable semantics __attribute__((swift_name("semantics")));
@property (readonly) DyrectoSharedCreativeSignature * _Nullable signature __attribute__((swift_name("signature")));
@property (readonly) DyrectoSharedCreativeStyle *style __attribute__((swift_name("style")));
@property (readonly) DyrectoSharedCreativeSubject *subject __attribute__((swift_name("subject")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSceneModel.Companion")))
@interface DyrectoSharedCreativeSceneModelCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeSceneModelCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSignature")))
@interface DyrectoSharedCreativeSignature : DyrectoSharedBase
- (instancetype)initWithElements:(NSArray<DyrectoSharedSignatureElement *> *)elements __attribute__((swift_name("init(elements:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeSignatureCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeSignature *)doCopyElements:(NSArray<DyrectoSharedSignatureElement *> *)elements __attribute__((swift_name("doCopy(elements:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedSignatureElement *> *elements __attribute__((swift_name("elements")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSignature.Companion")))
@interface DyrectoSharedCreativeSignatureCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeSignatureCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeStyle")))
@interface DyrectoSharedCreativeStyle : DyrectoSharedBase
- (instancetype)initWithMood:(DyrectoSharedMood *)mood confidence:(float)confidence __attribute__((swift_name("init(mood:confidence:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeStyleCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeStyle *)doCopyMood:(DyrectoSharedMood *)mood confidence:(float)confidence __attribute__((swift_name("doCopy(mood:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedMood *mood __attribute__((swift_name("mood")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeStyle.Companion")))
@interface DyrectoSharedCreativeStyleCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeStyleCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSubject")))
@interface DyrectoSharedCreativeSubject : DyrectoSharedBase
- (instancetype)initWithKind:(DyrectoSharedCreativeSubjectKind *)kind count:(int32_t)count confidence:(float)confidence __attribute__((swift_name("init(kind:count:confidence:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeSubjectCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeSubject *)doCopyKind:(DyrectoSharedCreativeSubjectKind *)kind count:(int32_t)count confidence:(float)confidence __attribute__((swift_name("doCopy(kind:count:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) int32_t count __attribute__((swift_name("count")));
@property (readonly) DyrectoSharedCreativeSubjectKind *kind __attribute__((swift_name("kind")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSubject.Companion")))
@interface DyrectoSharedCreativeSubjectCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeSubjectCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeSubjectKind")))
@interface DyrectoSharedCreativeSubjectKind : DyrectoSharedKotlinEnum<DyrectoSharedCreativeSubjectKind *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *portrait __attribute__((swift_name("portrait")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *group __attribute__((swift_name("group")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *animal __attribute__((swift_name("animal")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *vehicle __attribute__((swift_name("vehicle")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *product __attribute__((swift_name("product")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *food __attribute__((swift_name("food")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *scene __attribute__((swift_name("scene")));
@property (class, readonly) DyrectoSharedCreativeSubjectKind *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCreativeSubjectKind *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCreativeSubjectKind *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DepthOfField")))
@interface DyrectoSharedDepthOfField : DyrectoSharedKotlinEnum<DyrectoSharedDepthOfField *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedDepthOfField *shallow __attribute__((swift_name("shallow")));
@property (class, readonly) DyrectoSharedDepthOfField *deep __attribute__((swift_name("deep")));
@property (class, readonly) DyrectoSharedDepthOfField *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedDepthOfField *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedDepthOfField *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HeadroomLevel")))
@interface DyrectoSharedHeadroomLevel : DyrectoSharedKotlinEnum<DyrectoSharedHeadroomLevel *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedHeadroomLevel *tight __attribute__((swift_name("tight")));
@property (class, readonly) DyrectoSharedHeadroomLevel *balanced __attribute__((swift_name("balanced")));
@property (class, readonly) DyrectoSharedHeadroomLevel *generous __attribute__((swift_name("generous")));
@property (class, readonly) DyrectoSharedHeadroomLevel *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedHeadroomLevel *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedHeadroomLevel *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LightingContrast")))
@interface DyrectoSharedLightingContrast : DyrectoSharedKotlinEnum<DyrectoSharedLightingContrast *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedLightingContrast *low __attribute__((swift_name("low")));
@property (class, readonly) DyrectoSharedLightingContrast *medium __attribute__((swift_name("medium")));
@property (class, readonly) DyrectoSharedLightingContrast *high __attribute__((swift_name("high")));
@property (class, readonly) DyrectoSharedLightingContrast *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedLightingContrast *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedLightingContrast *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LightingDirection")))
@interface DyrectoSharedLightingDirection : DyrectoSharedKotlinEnum<DyrectoSharedLightingDirection *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedLightingDirection *front __attribute__((swift_name("front")));
@property (class, readonly) DyrectoSharedLightingDirection *side __attribute__((swift_name("side")));
@property (class, readonly) DyrectoSharedLightingDirection *back __attribute__((swift_name("back")));
@property (class, readonly) DyrectoSharedLightingDirection *top __attribute__((swift_name("top")));
@property (class, readonly) DyrectoSharedLightingDirection *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedLightingDirection *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedLightingDirection *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LightingKey")))
@interface DyrectoSharedLightingKey : DyrectoSharedKotlinEnum<DyrectoSharedLightingKey *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedLightingKey *lowKey __attribute__((swift_name("lowKey")));
@property (class, readonly) DyrectoSharedLightingKey *balanced __attribute__((swift_name("balanced")));
@property (class, readonly) DyrectoSharedLightingKey *highKey __attribute__((swift_name("highKey")));
@property (class, readonly) DyrectoSharedLightingKey *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedLightingKey *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedLightingKey *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Mood")))
@interface DyrectoSharedMood : DyrectoSharedKotlinEnum<DyrectoSharedMood *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedMood *warmCinematic __attribute__((swift_name("warmCinematic")));
@property (class, readonly) DyrectoSharedMood *brightAiry __attribute__((swift_name("brightAiry")));
@property (class, readonly) DyrectoSharedMood *darkMoody __attribute__((swift_name("darkMoody")));
@property (class, readonly) DyrectoSharedMood *neutral __attribute__((swift_name("neutral")));
@property (class, readonly) DyrectoSharedMood *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedMood *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedMood *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NegativeSpace")))
@interface DyrectoSharedNegativeSpace : DyrectoSharedKotlinEnum<DyrectoSharedNegativeSpace *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedNegativeSpace *low __attribute__((swift_name("low")));
@property (class, readonly) DyrectoSharedNegativeSpace *medium __attribute__((swift_name("medium")));
@property (class, readonly) DyrectoSharedNegativeSpace *high __attribute__((swift_name("high")));
@property (class, readonly) DyrectoSharedNegativeSpace *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedNegativeSpace *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedNegativeSpace *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PriorityLevel")))
@interface DyrectoSharedPriorityLevel : DyrectoSharedKotlinEnum<DyrectoSharedPriorityLevel *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedPriorityLevel *low __attribute__((swift_name("low")));
@property (class, readonly) DyrectoSharedPriorityLevel *medium __attribute__((swift_name("medium")));
@property (class, readonly) DyrectoSharedPriorityLevel *high __attribute__((swift_name("high")));
+ (DyrectoSharedKotlinArray<DyrectoSharedPriorityLevel *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedPriorityLevel *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RelationshipKind")))
@interface DyrectoSharedRelationshipKind : DyrectoSharedKotlinEnum<DyrectoSharedRelationshipKind *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedRelationshipKind *subjectToFrame __attribute__((swift_name("subjectToFrame")));
@property (class, readonly) DyrectoSharedRelationshipKind *subjectToBackground __attribute__((swift_name("subjectToBackground")));
@property (class, readonly) DyrectoSharedRelationshipKind *negativeSpaceDominant __attribute__((swift_name("negativeSpaceDominant")));
@property (class, readonly) DyrectoSharedRelationshipKind *foregroundBackgroundSeparation __attribute__((swift_name("foregroundBackgroundSeparation")));
@property (class, readonly) DyrectoSharedRelationshipKind *visualHierarchy __attribute__((swift_name("visualHierarchy")));
@property (class, readonly) DyrectoSharedRelationshipKind *dominantAnchor __attribute__((swift_name("dominantAnchor")));
@property (class, readonly) DyrectoSharedRelationshipKind *secondaryAnchor __attribute__((swift_name("secondaryAnchor")));
@property (class, readonly) DyrectoSharedRelationshipKind *frameBalance __attribute__((swift_name("frameBalance")));
@property (class, readonly) DyrectoSharedRelationshipKind *subjectIsolation __attribute__((swift_name("subjectIsolation")));
@property (class, readonly) DyrectoSharedRelationshipKind *backgroundSimplicity __attribute__((swift_name("backgroundSimplicity")));
@property (class, readonly) DyrectoSharedRelationshipKind *subjectOrientation __attribute__((swift_name("subjectOrientation")));
@property (class, readonly) DyrectoSharedRelationshipKind *eyeLine __attribute__((swift_name("eyeLine")));
@property (class, readonly) DyrectoSharedRelationshipKind *layerSeparation __attribute__((swift_name("layerSeparation")));
@property (class, readonly) DyrectoSharedRelationshipKind *edgeAnchoring __attribute__((swift_name("edgeAnchoring")));
@property (class, readonly) DyrectoSharedRelationshipKind *directionalFlow __attribute__((swift_name("directionalFlow")));
@property (class, readonly) DyrectoSharedRelationshipKind *visualTension __attribute__((swift_name("visualTension")));
+ (DyrectoSharedKotlinArray<DyrectoSharedRelationshipKind *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedRelationshipKind *> *entries __attribute__((swift_name("entries")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneRelationship")))
@interface DyrectoSharedSceneRelationship : DyrectoSharedBase
- (instancetype)initWithKind:(DyrectoSharedRelationshipKind *)kind strength:(float)strength confidence:(float)confidence reason:(NSString *)reason __attribute__((swift_name("init(kind:strength:confidence:reason:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSceneRelationshipCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSceneRelationship *)doCopyKind:(DyrectoSharedRelationshipKind *)kind strength:(float)strength confidence:(float)confidence reason:(NSString *)reason __attribute__((swift_name("doCopy(kind:strength:confidence:reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedRelationshipKind *kind __attribute__((swift_name("kind")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@property (readonly) float strength __attribute__((swift_name("strength")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneRelationship.Companion")))
@interface DyrectoSharedSceneRelationshipCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneRelationshipCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSemantics")))
@interface DyrectoSharedSceneSemantics : DyrectoSharedBase
- (instancetype)initWithConcepts:(NSArray<DyrectoSharedSemanticConcept *> *)concepts modelId:(NSString *)modelId __attribute__((swift_name("init(concepts:modelId:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSceneSemanticsCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSceneSemantics *)doCopyConcepts:(NSArray<DyrectoSharedSemanticConcept *> *)concepts modelId:(NSString *)modelId __attribute__((swift_name("doCopy(concepts:modelId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedSemanticConcept *> *concepts __attribute__((swift_name("concepts")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSemantics.Companion")))
@interface DyrectoSharedSceneSemanticsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneSemanticsCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemanticConcept")))
@interface DyrectoSharedSemanticConcept : DyrectoSharedBase
- (instancetype)initWithLabel:(NSString *)label score:(float)score aspect:(DyrectoSharedCreativeAspect * _Nullable)aspect __attribute__((swift_name("init(label:score:aspect:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSemanticConceptCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSemanticConcept *)doCopyLabel:(NSString *)label score:(float)score aspect:(DyrectoSharedCreativeAspect * _Nullable)aspect __attribute__((swift_name("doCopy(label:score:aspect:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCreativeAspect * _Nullable aspect __attribute__((swift_name("aspect")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) float score __attribute__((swift_name("score")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemanticConcept.Companion")))
@interface DyrectoSharedSemanticConceptCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSemanticConceptCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotType")))
@interface DyrectoSharedShotType : DyrectoSharedKotlinEnum<DyrectoSharedShotType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedShotType *extremeCloseUp __attribute__((swift_name("extremeCloseUp")));
@property (class, readonly) DyrectoSharedShotType *closeUp __attribute__((swift_name("closeUp")));
@property (class, readonly) DyrectoSharedShotType *mediumCloseUp __attribute__((swift_name("mediumCloseUp")));
@property (class, readonly) DyrectoSharedShotType *medium __attribute__((swift_name("medium")));
@property (class, readonly) DyrectoSharedShotType *wide __attribute__((swift_name("wide")));
@property (class, readonly) DyrectoSharedShotType *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedShotType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedShotType *> *entries __attribute__((swift_name("entries")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignatureElement")))
@interface DyrectoSharedSignatureElement : DyrectoSharedBase
- (instancetype)initWithLabel:(NSString *)label salience:(float)salience source:(DyrectoSharedSignatureSource *)source __attribute__((swift_name("init(label:salience:source:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedSignatureElementCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedSignatureElement *)doCopyLabel:(NSString *)label salience:(float)salience source:(DyrectoSharedSignatureSource *)source __attribute__((swift_name("doCopy(label:salience:source:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) float salience __attribute__((swift_name("salience")));
@property (readonly) DyrectoSharedSignatureSource *source __attribute__((swift_name("source")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignatureElement.Companion")))
@interface DyrectoSharedSignatureElementCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSignatureElementCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignaturePresenter")))
@interface DyrectoSharedSignaturePresenter : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)signaturePresenter __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSignaturePresenter *shared __attribute__((swift_name("shared")));
- (NSArray<DyrectoSharedSignaturePresenterInsight *> *)insightsModel:(DyrectoSharedCreativeSceneModel * _Nullable)model __attribute__((swift_name("insights(model:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignaturePresenter.Insight")))
@interface DyrectoSharedSignaturePresenterInsight : DyrectoSharedBase
- (instancetype)initWithTitle:(NSString *)title text:(NSString *)text __attribute__((swift_name("init(title:text:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSignaturePresenterInsight *)doCopyTitle:(NSString *)title text:(NSString *)text __attribute__((swift_name("doCopy(title:text:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *text __attribute__((swift_name("text")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignatureSource")))
@interface DyrectoSharedSignatureSource : DyrectoSharedKotlinEnum<DyrectoSharedSignatureSource *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSignatureSource *geometry __attribute__((swift_name("geometry")));
@property (class, readonly) DyrectoSharedSignatureSource *composition __attribute__((swift_name("composition")));
@property (class, readonly) DyrectoSharedSignatureSource *lighting __attribute__((swift_name("lighting")));
@property (class, readonly) DyrectoSharedSignatureSource *color __attribute__((swift_name("color")));
@property (class, readonly) DyrectoSharedSignatureSource *segmentation __attribute__((swift_name("segmentation")));
@property (class, readonly) DyrectoSharedSignatureSource *semantic __attribute__((swift_name("semantic")));
@property (class, readonly) DyrectoSharedSignatureSource *relationship __attribute__((swift_name("relationship")));
@property (class, readonly) DyrectoSharedSignatureSource *identity __attribute__((swift_name("identity")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSignatureSource *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSignatureSource *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectPlacement")))
@interface DyrectoSharedSubjectPlacement : DyrectoSharedKotlinEnum<DyrectoSharedSubjectPlacement *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSubjectPlacement *leftThird __attribute__((swift_name("leftThird")));
@property (class, readonly) DyrectoSharedSubjectPlacement *center __attribute__((swift_name("center")));
@property (class, readonly) DyrectoSharedSubjectPlacement *rightThird __attribute__((swift_name("rightThird")));
@property (class, readonly) DyrectoSharedSubjectPlacement *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSubjectPlacement *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSubjectPlacement *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Symmetry")))
@interface DyrectoSharedSymmetry : DyrectoSharedKotlinEnum<DyrectoSharedSymmetry *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSymmetry *symmetric __attribute__((swift_name("symmetric")));
@property (class, readonly) DyrectoSharedSymmetry *asymmetric __attribute__((swift_name("asymmetric")));
@property (class, readonly) DyrectoSharedSymmetry *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSymmetry *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSymmetry *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TintCast")))
@interface DyrectoSharedTintCast : DyrectoSharedKotlinEnum<DyrectoSharedTintCast *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedTintCast *green __attribute__((swift_name("green")));
@property (class, readonly) DyrectoSharedTintCast *neutral __attribute__((swift_name("neutral")));
@property (class, readonly) DyrectoSharedTintCast *magenta __attribute__((swift_name("magenta")));
@property (class, readonly) DyrectoSharedTintCast *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedTintCast *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedTintCast *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("SceneExpert")))
@protocol DyrectoSharedSceneExpert
@required
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ColorExpert")))
@interface DyrectoSharedColorExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)colorExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedColorExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompositionExpert")))
@interface DyrectoSharedCompositionExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)compositionExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCompositionExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeAnalysisContext")))
@interface DyrectoSharedCreativeAnalysisContext : DyrectoSharedBase
- (instancetype)initWithExposure:(DyrectoSharedReferenceExposureProfile *)exposure color:(DyrectoSharedReferenceColorProfile *)color ai:(DyrectoSharedReferenceAiProfile * _Nullable)ai faceBox:(DyrectoSharedNormalizedRect * _Nullable)faceBox subjectBox:(DyrectoSharedNormalizedRect * _Nullable)subjectBox boxIsFace:(BOOL)boxIsFace semantic:(DyrectoSharedSemanticObservation * _Nullable)semantic __attribute__((swift_name("init(exposure:color:ai:faceBox:subjectBox:boxIsFace:semantic:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedCreativeAnalysisContext *)doCopyExposure:(DyrectoSharedReferenceExposureProfile *)exposure color:(DyrectoSharedReferenceColorProfile *)color ai:(DyrectoSharedReferenceAiProfile * _Nullable)ai faceBox:(DyrectoSharedNormalizedRect * _Nullable)faceBox subjectBox:(DyrectoSharedNormalizedRect * _Nullable)subjectBox boxIsFace:(BOOL)boxIsFace semantic:(DyrectoSharedSemanticObservation * _Nullable)semantic __attribute__((swift_name("doCopy(exposure:color:ai:faceBox:subjectBox:boxIsFace:semantic:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedReferenceAiProfile * _Nullable ai __attribute__((swift_name("ai")));
@property (readonly) BOOL boxIsFace __attribute__((swift_name("boxIsFace")));
@property (readonly) DyrectoSharedReferenceColorProfile *color __attribute__((swift_name("color")));
@property (readonly) DyrectoSharedReferenceExposureProfile *exposure __attribute__((swift_name("exposure")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable faceBox __attribute__((swift_name("faceBox")));
@property (readonly) DyrectoSharedSemanticObservation * _Nullable semantic __attribute__((swift_name("semantic")));
@property (readonly) DyrectoSharedNormalizedRect * _Nullable subjectBox __attribute__((swift_name("subjectBox")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeContribution")))
@interface DyrectoSharedCreativeContribution : DyrectoSharedBase
- (instancetype)initWithSubject:(DyrectoSharedCreativeSubject * _Nullable)subject camera:(DyrectoSharedCreativeCamera * _Nullable)camera placement:(DyrectoSharedSubjectPlacement * _Nullable)placement negativeSpace:(DyrectoSharedNegativeSpace * _Nullable)negativeSpace headroom:(DyrectoSharedHeadroomLevel * _Nullable)headroom compositionConfidence:(DyrectoSharedFloat * _Nullable)compositionConfidence lighting:(DyrectoSharedCreativeLighting * _Nullable)lighting color:(DyrectoSharedCreativeColor * _Nullable)color relationships:(NSArray<DyrectoSharedSceneRelationship *> *)relationships signatureElements:(NSArray<DyrectoSharedSignatureElement *> *)signatureElements traits:(NSArray<DyrectoSharedShotTrait *> *)traits priorityHints:(NSArray<DyrectoSharedPriorityHint *> *)priorityHints concepts:(NSArray<DyrectoSharedConceptScore *> *)concepts __attribute__((swift_name("init(subject:camera:placement:negativeSpace:headroom:compositionConfidence:lighting:color:relationships:signatureElements:traits:priorityHints:concepts:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedCreativeContributionCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCreativeContribution *)doCopySubject:(DyrectoSharedCreativeSubject * _Nullable)subject camera:(DyrectoSharedCreativeCamera * _Nullable)camera placement:(DyrectoSharedSubjectPlacement * _Nullable)placement negativeSpace:(DyrectoSharedNegativeSpace * _Nullable)negativeSpace headroom:(DyrectoSharedHeadroomLevel * _Nullable)headroom compositionConfidence:(DyrectoSharedFloat * _Nullable)compositionConfidence lighting:(DyrectoSharedCreativeLighting * _Nullable)lighting color:(DyrectoSharedCreativeColor * _Nullable)color relationships:(NSArray<DyrectoSharedSceneRelationship *> *)relationships signatureElements:(NSArray<DyrectoSharedSignatureElement *> *)signatureElements traits:(NSArray<DyrectoSharedShotTrait *> *)traits priorityHints:(NSArray<DyrectoSharedPriorityHint *> *)priorityHints concepts:(NSArray<DyrectoSharedConceptScore *> *)concepts __attribute__((swift_name("doCopy(subject:camera:placement:negativeSpace:headroom:compositionConfidence:lighting:color:relationships:signatureElements:traits:priorityHints:concepts:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCreativeCamera * _Nullable camera __attribute__((swift_name("camera")));
@property (readonly) DyrectoSharedCreativeColor * _Nullable color __attribute__((swift_name("color")));
@property (readonly) DyrectoSharedFloat * _Nullable compositionConfidence __attribute__((swift_name("compositionConfidence")));
@property (readonly) NSArray<DyrectoSharedConceptScore *> *concepts __attribute__((swift_name("concepts")));
@property (readonly) DyrectoSharedHeadroomLevel * _Nullable headroom __attribute__((swift_name("headroom")));
@property (readonly) DyrectoSharedCreativeLighting * _Nullable lighting __attribute__((swift_name("lighting")));
@property (readonly) DyrectoSharedNegativeSpace * _Nullable negativeSpace __attribute__((swift_name("negativeSpace")));
@property (readonly) DyrectoSharedSubjectPlacement * _Nullable placement __attribute__((swift_name("placement")));
@property (readonly) NSArray<DyrectoSharedPriorityHint *> *priorityHints __attribute__((swift_name("priorityHints")));
@property (readonly) NSArray<DyrectoSharedSceneRelationship *> *relationships __attribute__((swift_name("relationships")));
@property (readonly) NSArray<DyrectoSharedSignatureElement *> *signatureElements __attribute__((swift_name("signatureElements")));
@property (readonly) DyrectoSharedCreativeSubject * _Nullable subject __attribute__((swift_name("subject")));
@property (readonly) NSArray<DyrectoSharedShotTrait *> *traits __attribute__((swift_name("traits")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CreativeContribution.Companion")))
@interface DyrectoSharedCreativeContributionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCreativeContributionCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedCreativeContribution *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("GeometryExpert")))
@interface DyrectoSharedGeometryExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)geometryExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedGeometryExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LightingExpert")))
@interface DyrectoSharedLightingExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)lightingExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedLightingExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PriorityHint")))
@interface DyrectoSharedPriorityHint : DyrectoSharedBase
- (instancetype)initWithAspect:(DyrectoSharedCreativeAspect *)aspect boost:(float)boost reason:(NSString *)reason __attribute__((swift_name("init(aspect:boost:reason:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPriorityHint *)doCopyAspect:(DyrectoSharedCreativeAspect *)aspect boost:(float)boost reason:(NSString *)reason __attribute__((swift_name("doCopy(aspect:boost:reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCreativeAspect *aspect __attribute__((swift_name("aspect")));
@property (readonly) float boost __attribute__((swift_name("boost")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SegmentationExpert")))
@interface DyrectoSharedSegmentationExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)segmentationExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSegmentationExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemanticExpert")))
@interface DyrectoSharedSemanticExpert : DyrectoSharedBase <DyrectoSharedSceneExpert>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)semanticExpert __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSemanticExpert *shared __attribute__((swift_name("shared")));
- (DyrectoSharedCreativeContribution *)observeCtx:(DyrectoSharedCreativeAnalysisContext *)ctx __attribute__((swift_name("observe(ctx:)")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotIdentity")))
@interface DyrectoSharedShotIdentity : DyrectoSharedBase
- (instancetype)initWithTraits:(NSArray<DyrectoSharedShotTrait *> *)traits __attribute__((swift_name("init(traits:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedShotIdentityCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedShotIdentity *)doCopyTraits:(NSArray<DyrectoSharedShotTrait *> *)traits __attribute__((swift_name("doCopy(traits:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (DyrectoSharedShotTrait * _Nullable)ofDimension:(DyrectoSharedShotTraitDimension *)dimension __attribute__((swift_name("of(dimension:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float peakDistinctiveness __attribute__((swift_name("peakDistinctiveness")));
@property (readonly) DyrectoSharedShotTrait * _Nullable primary __attribute__((swift_name("primary")));
@property (readonly) NSArray<DyrectoSharedShotTrait *> *traits __attribute__((swift_name("traits")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotIdentity.Companion")))
@interface DyrectoSharedShotIdentityCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedShotIdentityCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotIdentityExtractor")))
@interface DyrectoSharedShotIdentityExtractor : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)shotIdentityExtractor __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedShotIdentityExtractor *shared __attribute__((swift_name("shared")));
- (DyrectoSharedShotIdentity *)extractCtx:(DyrectoSharedCreativeAnalysisContext *)ctx t:(DyrectoSharedShotIdentityThresholds *)t __attribute__((swift_name("extract(ctx:t:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotIdentityThresholds")))
@interface DyrectoSharedShotIdentityThresholds : DyrectoSharedBase
- (instancetype)initWithPositionAnchorX:(float)positionAnchorX positionAnchorY:(float)positionAnchorY positionSaturation:(float)positionSaturation scaleAnchor:(float)scaleAnchor scaleSaturation:(float)scaleSaturation edgeTensionGap:(float)edgeTensionGap negSpaceAnchor:(float)negSpaceAnchor negSpaceSaturation:(float)negSpaceSaturation separationSaturation:(float)separationSaturation tonalKeyAnchor:(float)tonalKeyAnchor tonalKeySaturation:(float)tonalKeySaturation contrastAnchor:(float)contrastAnchor contrastSaturation:(float)contrastSaturation balanceSaturation:(float)balanceSaturation spreadSaturation:(float)spreadSaturation densityAnchor:(float)densityAnchor densitySaturation:(float)densitySaturation warmthSaturation:(float)warmthSaturation castSaturation:(float)castSaturation semanticMarginSaturation:(float)semanticMarginSaturation traitFloor:(float)traitFloor strongDistinctiveness:(float)strongDistinctiveness __attribute__((swift_name("init(positionAnchorX:positionAnchorY:positionSaturation:scaleAnchor:scaleSaturation:edgeTensionGap:negSpaceAnchor:negSpaceSaturation:separationSaturation:tonalKeyAnchor:tonalKeySaturation:contrastAnchor:contrastSaturation:balanceSaturation:spreadSaturation:densityAnchor:densitySaturation:warmthSaturation:castSaturation:semanticMarginSaturation:traitFloor:strongDistinctiveness:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedShotIdentityThresholdsCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedShotIdentityThresholds *)doCopyPositionAnchorX:(float)positionAnchorX positionAnchorY:(float)positionAnchorY positionSaturation:(float)positionSaturation scaleAnchor:(float)scaleAnchor scaleSaturation:(float)scaleSaturation edgeTensionGap:(float)edgeTensionGap negSpaceAnchor:(float)negSpaceAnchor negSpaceSaturation:(float)negSpaceSaturation separationSaturation:(float)separationSaturation tonalKeyAnchor:(float)tonalKeyAnchor tonalKeySaturation:(float)tonalKeySaturation contrastAnchor:(float)contrastAnchor contrastSaturation:(float)contrastSaturation balanceSaturation:(float)balanceSaturation spreadSaturation:(float)spreadSaturation densityAnchor:(float)densityAnchor densitySaturation:(float)densitySaturation warmthSaturation:(float)warmthSaturation castSaturation:(float)castSaturation semanticMarginSaturation:(float)semanticMarginSaturation traitFloor:(float)traitFloor strongDistinctiveness:(float)strongDistinctiveness __attribute__((swift_name("doCopy(positionAnchorX:positionAnchorY:positionSaturation:scaleAnchor:scaleSaturation:edgeTensionGap:negSpaceAnchor:negSpaceSaturation:separationSaturation:tonalKeyAnchor:tonalKeySaturation:contrastAnchor:contrastSaturation:balanceSaturation:spreadSaturation:densityAnchor:densitySaturation:warmthSaturation:castSaturation:semanticMarginSaturation:traitFloor:strongDistinctiveness:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float balanceSaturation __attribute__((swift_name("balanceSaturation")));
@property (readonly) float castSaturation __attribute__((swift_name("castSaturation")));
@property (readonly) float contrastAnchor __attribute__((swift_name("contrastAnchor")));
@property (readonly) float contrastSaturation __attribute__((swift_name("contrastSaturation")));
@property (readonly) float densityAnchor __attribute__((swift_name("densityAnchor")));
@property (readonly) float densitySaturation __attribute__((swift_name("densitySaturation")));
@property (readonly) float edgeTensionGap __attribute__((swift_name("edgeTensionGap")));
@property (readonly) float negSpaceAnchor __attribute__((swift_name("negSpaceAnchor")));
@property (readonly) float negSpaceSaturation __attribute__((swift_name("negSpaceSaturation")));
@property (readonly) float positionAnchorX __attribute__((swift_name("positionAnchorX")));
@property (readonly) float positionAnchorY __attribute__((swift_name("positionAnchorY")));
@property (readonly) float positionSaturation __attribute__((swift_name("positionSaturation")));
@property (readonly) float scaleAnchor __attribute__((swift_name("scaleAnchor")));
@property (readonly) float scaleSaturation __attribute__((swift_name("scaleSaturation")));
@property (readonly) float semanticMarginSaturation __attribute__((swift_name("semanticMarginSaturation")));
@property (readonly) float separationSaturation __attribute__((swift_name("separationSaturation")));
@property (readonly) float spreadSaturation __attribute__((swift_name("spreadSaturation")));
@property (readonly) float strongDistinctiveness __attribute__((swift_name("strongDistinctiveness")));
@property (readonly) float tonalKeyAnchor __attribute__((swift_name("tonalKeyAnchor")));
@property (readonly) float tonalKeySaturation __attribute__((swift_name("tonalKeySaturation")));
@property (readonly) float traitFloor __attribute__((swift_name("traitFloor")));
@property (readonly) float warmthSaturation __attribute__((swift_name("warmthSaturation")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotIdentityThresholds.Companion")))
@interface DyrectoSharedShotIdentityThresholdsCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedShotIdentityThresholdsCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedShotIdentityThresholds *DEFAULT __attribute__((swift_name("DEFAULT")));
@property (readonly) DyrectoSharedShotIdentityThresholds *LOOSE __attribute__((swift_name("LOOSE")));
@property (readonly) DyrectoSharedShotIdentityThresholds *MEDIUM __attribute__((swift_name("MEDIUM")));
@property (readonly) DyrectoSharedShotIdentityThresholds *STRICT __attribute__((swift_name("STRICT")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.Serializable
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotTrait")))
@interface DyrectoSharedShotTrait : DyrectoSharedBase
- (instancetype)initWithDimension:(DyrectoSharedShotTraitDimension *)dimension value:(float)value distinctiveness:(float)distinctiveness descriptor:(NSString *)descriptor direction:(DyrectoSharedTraitDirection *)direction confidence:(float)confidence reason:(NSString *)reason __attribute__((swift_name("init(dimension:value:distinctiveness:descriptor:direction:confidence:reason:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedShotTraitCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedShotTrait *)doCopyDimension:(DyrectoSharedShotTraitDimension *)dimension value:(float)value distinctiveness:(float)distinctiveness descriptor:(NSString *)descriptor direction:(DyrectoSharedTraitDirection *)direction confidence:(float)confidence reason:(NSString *)reason __attribute__((swift_name("doCopy(dimension:value:distinctiveness:descriptor:direction:confidence:reason:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) NSString *descriptor __attribute__((swift_name("descriptor")));
@property (readonly) DyrectoSharedShotTraitDimension *dimension __attribute__((swift_name("dimension")));
@property (readonly) DyrectoSharedTraitDirection *direction __attribute__((swift_name("direction")));
@property (readonly) float distinctiveness __attribute__((swift_name("distinctiveness")));
@property (readonly) NSString *reason __attribute__((swift_name("reason")));
@property (readonly) float value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotTrait.Companion")))
@interface DyrectoSharedShotTraitCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedShotTraitCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShotTraitDimension")))
@interface DyrectoSharedShotTraitDimension : DyrectoSharedKotlinEnum<DyrectoSharedShotTraitDimension *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedShotTraitDimension *subjectFramePosition __attribute__((swift_name("subjectFramePosition")));
@property (class, readonly) DyrectoSharedShotTraitDimension *subjectScale __attribute__((swift_name("subjectScale")));
@property (class, readonly) DyrectoSharedShotTraitDimension *edgeAnchoring __attribute__((swift_name("edgeAnchoring")));
@property (class, readonly) DyrectoSharedShotTraitDimension *negativeSpaceVolume __attribute__((swift_name("negativeSpaceVolume")));
@property (class, readonly) DyrectoSharedShotTraitDimension *negativeSpaceDirection __attribute__((swift_name("negativeSpaceDirection")));
@property (class, readonly) DyrectoSharedShotTraitDimension *subjectBackgroundSeparation __attribute__((swift_name("subjectBackgroundSeparation")));
@property (class, readonly) DyrectoSharedShotTraitDimension *tonalKey __attribute__((swift_name("tonalKey")));
@property (class, readonly) DyrectoSharedShotTraitDimension *dominantContrast __attribute__((swift_name("dominantContrast")));
@property (class, readonly) DyrectoSharedShotTraitDimension *backgroundBrightness __attribute__((swift_name("backgroundBrightness")));
@property (class, readonly) DyrectoSharedShotTraitDimension *visualBalance __attribute__((swift_name("visualBalance")));
@property (class, readonly) DyrectoSharedShotTraitDimension *visualWeightSpread __attribute__((swift_name("visualWeightSpread")));
@property (class, readonly) DyrectoSharedShotTraitDimension *colorWarmthMagnitude __attribute__((swift_name("colorWarmthMagnitude")));
@property (class, readonly) DyrectoSharedShotTraitDimension *colorCastStrength __attribute__((swift_name("colorCastStrength")));
@property (class, readonly) DyrectoSharedShotTraitDimension *subjectIsolation __attribute__((swift_name("subjectIsolation")));
@property (class, readonly) DyrectoSharedShotTraitDimension *frameDensity __attribute__((swift_name("frameDensity")));
@property (class, readonly) DyrectoSharedShotTraitDimension *semanticDistinction __attribute__((swift_name("semanticDistinction")));
+ (DyrectoSharedKotlinArray<DyrectoSharedShotTraitDimension *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedShotTraitDimension *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TraitDirection")))
@interface DyrectoSharedTraitDirection : DyrectoSharedKotlinEnum<DyrectoSharedTraitDirection *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedTraitDirection *left __attribute__((swift_name("left")));
@property (class, readonly) DyrectoSharedTraitDirection *right __attribute__((swift_name("right")));
@property (class, readonly) DyrectoSharedTraitDirection *up __attribute__((swift_name("up")));
@property (class, readonly) DyrectoSharedTraitDirection *down __attribute__((swift_name("down")));
@property (class, readonly) DyrectoSharedTraitDirection *none __attribute__((swift_name("none")));
@property (class, readonly) DyrectoSharedTraitDirection *unknown __attribute__((swift_name("unknown")));
+ (DyrectoSharedKotlinArray<DyrectoSharedTraitDirection *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedTraitDirection *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConceptScore")))
@interface DyrectoSharedConceptScore : DyrectoSharedBase
- (instancetype)initWithLabel:(NSString *)label score:(float)score aspect:(DyrectoSharedCreativeAspect * _Nullable)aspect family:(NSString *)family __attribute__((swift_name("init(label:score:aspect:family:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedConceptScore *)doCopyLabel:(NSString *)label score:(float)score aspect:(DyrectoSharedCreativeAspect * _Nullable)aspect family:(NSString *)family __attribute__((swift_name("doCopy(label:score:aspect:family:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedCreativeAspect * _Nullable aspect __attribute__((swift_name("aspect")));
@property (readonly) NSString *family __attribute__((swift_name("family")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) float score __attribute__((swift_name("score")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConceptVocabulary")))
@interface DyrectoSharedConceptVocabulary : DyrectoSharedBase
@property (class, readonly, getter=companion) DyrectoSharedConceptVocabularyCompanion *companion __attribute__((swift_name("companion")));
- (NSArray<DyrectoSharedConceptScore *> *)scoreEmbedding:(DyrectoSharedKotlinFloatArray * _Nullable)embedding __attribute__((swift_name("score(embedding:)")));
@property (readonly) int32_t dimensions __attribute__((swift_name("dimensions")));
@property (readonly) NSArray<DyrectoSharedConceptVocabularyEntry *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConceptVocabulary.Companion")))
@interface DyrectoSharedConceptVocabularyCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedConceptVocabularyCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedConceptVocabulary * _Nullable)fromJsonText:(NSString *)text __attribute__((swift_name("fromJson(text:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConceptVocabulary.Entry")))
@interface DyrectoSharedConceptVocabularyEntry : DyrectoSharedBase
- (instancetype)initWithLabel:(NSString *)label aspect:(DyrectoSharedCreativeAspect * _Nullable)aspect family:(NSString *)family vector:(DyrectoSharedKotlinFloatArray *)vector __attribute__((swift_name("init(label:aspect:family:vector:)"))) __attribute__((objc_designated_initializer));
@property (readonly) DyrectoSharedCreativeAspect * _Nullable aspect __attribute__((swift_name("aspect")));
@property (readonly) NSString *family __attribute__((swift_name("family")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) DyrectoSharedKotlinFloatArray *vector __attribute__((swift_name("vector")));
@end

__attribute__((swift_name("SemanticSceneEngine")))
@protocol DyrectoSharedSemanticSceneEngine
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)observeBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedSemanticObservation * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("observe(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NoSemanticSceneEngine")))
@interface DyrectoSharedNoSemanticSceneEngine : DyrectoSharedBase <DyrectoSharedSemanticSceneEngine>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)noSemanticSceneEngine __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedNoSemanticSceneEngine *shared __attribute__((swift_name("shared")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)observeBitmap:(DyrectoSharedPlatformImage *)bitmap completionHandler:(void (^)(DyrectoSharedSemanticObservation * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("observe(bitmap:completionHandler:)")));
@property (readonly) DyrectoSharedAiCapability *capability __attribute__((swift_name("capability")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemanticObservation")))
@interface DyrectoSharedSemanticObservation : DyrectoSharedBase
- (instancetype)initWithConcepts:(NSArray<DyrectoSharedConceptScore *> *)concepts embedding:(DyrectoSharedKotlinFloatArray * _Nullable)embedding modelId:(NSString *)modelId __attribute__((swift_name("init(concepts:embedding:modelId:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedConceptScore * _Nullable)bestForAspect:(DyrectoSharedCreativeAspect *)aspect minScore:(float)minScore __attribute__((swift_name("bestFor(aspect:minScore:)")));
- (DyrectoSharedConceptScore * _Nullable)bestInFamilyFamily:(NSString *)family minScore:(float)minScore __attribute__((swift_name("bestInFamily(family:minScore:)")));
- (DyrectoSharedSemanticObservation *)doCopyConcepts:(NSArray<DyrectoSharedConceptScore *> *)concepts embedding:(DyrectoSharedKotlinFloatArray * _Nullable)embedding modelId:(NSString *)modelId __attribute__((swift_name("doCopy(concepts:embedding:modelId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
- (NSArray<DyrectoSharedConceptScore *> *)topConceptsMinScore:(float)minScore limit:(int32_t)limit __attribute__((swift_name("topConcepts(minScore:limit:)")));
@property (readonly) NSArray<DyrectoSharedConceptScore *> *concepts __attribute__((swift_name("concepts")));
@property (readonly) DyrectoSharedKotlinFloatArray * _Nullable embedding __attribute__((swift_name("embedding")));
@property (readonly) NSString *modelId __attribute__((swift_name("modelId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BatteryState")))
@interface DyrectoSharedBatteryState : DyrectoSharedBase
- (instancetype)initWithBatteryLevel:(DyrectoSharedInt * _Nullable)batteryLevel isBatteryLow:(BOOL)isBatteryLow __attribute__((swift_name("init(batteryLevel:isBatteryLow:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedBatteryState *)doCopyBatteryLevel:(DyrectoSharedInt * _Nullable)batteryLevel isBatteryLow:(BOOL)isBatteryLow __attribute__((swift_name("doCopy(batteryLevel:isBatteryLow:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedInt * _Nullable batteryLevel __attribute__((swift_name("batteryLevel")));
@property (readonly) BOOL isBatteryLow __attribute__((swift_name("isBatteryLow")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureState")))
@interface DyrectoSharedExposureState : DyrectoSharedBase
- (instancetype)initWithHighlightPercentage:(float)highlightPercentage shadowPercentage:(float)shadowPercentage isHighlightClipped:(BOOL)isHighlightClipped isShadowClipped:(BOOL)isShadowClipped __attribute__((swift_name("init(highlightPercentage:shadowPercentage:isHighlightClipped:isShadowClipped:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedExposureState *)doCopyHighlightPercentage:(float)highlightPercentage shadowPercentage:(float)shadowPercentage isHighlightClipped:(BOOL)isHighlightClipped isShadowClipped:(BOOL)isShadowClipped __attribute__((swift_name("doCopy(highlightPercentage:shadowPercentage:isHighlightClipped:isShadowClipped:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float highlightPercentage __attribute__((swift_name("highlightPercentage")));
@property (readonly) BOOL isHighlightClipped __attribute__((swift_name("isHighlightClipped")));
@property (readonly) BOOL isShadowClipped __attribute__((swift_name("isShadowClipped")));
@property (readonly) float shadowPercentage __attribute__((swift_name("shadowPercentage")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FaceState")))
@interface DyrectoSharedFaceState : DyrectoSharedBase
- (instancetype)initWithFacesDetected:(int32_t)facesDetected eyesDetected:(int32_t)eyesDetected hasVisibleFace:(BOOL)hasVisibleFace hasVisibleEyes:(BOOL)hasVisibleEyes __attribute__((swift_name("init(facesDetected:eyesDetected:hasVisibleFace:hasVisibleEyes:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFaceState *)doCopyFacesDetected:(int32_t)facesDetected eyesDetected:(int32_t)eyesDetected hasVisibleFace:(BOOL)hasVisibleFace hasVisibleEyes:(BOOL)hasVisibleEyes __attribute__((swift_name("doCopy(facesDetected:eyesDetected:hasVisibleFace:hasVisibleEyes:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t eyesDetected __attribute__((swift_name("eyesDetected")));
@property (readonly) int32_t facesDetected __attribute__((swift_name("facesDetected")));
@property (readonly) BOOL hasVisibleEyes __attribute__((swift_name("hasVisibleEyes")));
@property (readonly) BOOL hasVisibleFace __attribute__((swift_name("hasVisibleFace")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RecordingState")))
@interface DyrectoSharedRecordingState : DyrectoSharedBase
- (instancetype)initWithIsRecording:(BOOL)isRecording recordingDurationAvailable:(BOOL)recordingDurationAvailable __attribute__((swift_name("init(isRecording:recordingDurationAvailable:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedRecordingState *)doCopyIsRecording:(BOOL)isRecording recordingDurationAvailable:(BOOL)recordingDurationAvailable __attribute__((swift_name("doCopy(isRecording:recordingDurationAvailable:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL isRecording __attribute__((swift_name("isRecording")));
@property (readonly) BOOL recordingDurationAvailable __attribute__((swift_name("recordingDurationAvailable")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneAnalyzer")))
@interface DyrectoSharedSceneAnalyzer : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sceneAnalyzer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSceneAnalyzer *shared __attribute__((swift_name("shared")));
- (DyrectoSharedSceneContext *)analyzeFrame:(DyrectoSharedFrameContext *)frame vision:(DyrectoSharedVisionContext *)vision nowMs:(int64_t)nowMs __attribute__((swift_name("analyze(frame:vision:nowMs:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneConnectionState")))
@interface DyrectoSharedSceneConnectionState : DyrectoSharedBase
- (instancetype)initWithPhase:(DyrectoSharedConnectionPhase *)phase liveViewActive:(BOOL)liveViewActive __attribute__((swift_name("init(phase:liveViewActive:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneConnectionState *)doCopyPhase:(DyrectoSharedConnectionPhase *)phase liveViewActive:(BOOL)liveViewActive __attribute__((swift_name("doCopy(phase:liveViewActive:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL liveViewActive __attribute__((swift_name("liveViewActive")));
@property (readonly) DyrectoSharedConnectionPhase *phase __attribute__((swift_name("phase")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneContext")))
@interface DyrectoSharedSceneContext : DyrectoSharedBase
- (instancetype)initWithUpdatedAtMs:(int64_t)updatedAtMs exposure:(DyrectoSharedExposureState *)exposure face:(DyrectoSharedFaceState *)face recording:(DyrectoSharedRecordingState *)recording connection:(DyrectoSharedSceneConnectionState *)connection battery:(DyrectoSharedBatteryState *)battery storage:(DyrectoSharedStorageState *)storage overallState:(DyrectoSharedSceneState *)overallState __attribute__((swift_name("init(updatedAtMs:exposure:face:recording:connection:battery:storage:overallState:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneContext *)doCopyUpdatedAtMs:(int64_t)updatedAtMs exposure:(DyrectoSharedExposureState *)exposure face:(DyrectoSharedFaceState *)face recording:(DyrectoSharedRecordingState *)recording connection:(DyrectoSharedSceneConnectionState *)connection battery:(DyrectoSharedBatteryState *)battery storage:(DyrectoSharedStorageState *)storage overallState:(DyrectoSharedSceneState *)overallState __attribute__((swift_name("doCopy(updatedAtMs:exposure:face:recording:connection:battery:storage:overallState:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedBatteryState *battery __attribute__((swift_name("battery")));
@property (readonly) DyrectoSharedSceneConnectionState *connection __attribute__((swift_name("connection")));
@property (readonly) DyrectoSharedExposureState *exposure __attribute__((swift_name("exposure")));
@property (readonly) DyrectoSharedFaceState *face __attribute__((swift_name("face")));
@property (readonly) DyrectoSharedSceneState *overallState __attribute__((swift_name("overallState")));
@property (readonly) DyrectoSharedRecordingState *recording __attribute__((swift_name("recording")));
@property (readonly) DyrectoSharedStorageState *storage __attribute__((swift_name("storage")));
@property (readonly) int64_t updatedAtMs __attribute__((swift_name("updatedAtMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneState")))
@interface DyrectoSharedSceneState : DyrectoSharedKotlinEnum<DyrectoSharedSceneState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSceneState *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedSceneState *recording __attribute__((swift_name("recording")));
@property (class, readonly) DyrectoSharedSceneState *faceVisible __attribute__((swift_name("faceVisible")));
@property (class, readonly) DyrectoSharedSceneState *lowBattery __attribute__((swift_name("lowBattery")));
@property (class, readonly) DyrectoSharedSceneState *storageLow __attribute__((swift_name("storageLow")));
@property (class, readonly) DyrectoSharedSceneState *highlightPresent __attribute__((swift_name("highlightPresent")));
@property (class, readonly) DyrectoSharedSceneState *shadowPresent __attribute__((swift_name("shadowPresent")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSceneState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSceneState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StorageState")))
@interface DyrectoSharedStorageState : DyrectoSharedBase
- (instancetype)initWithRemainingStatus:(NSString * _Nullable)remainingStatus isStorageCritical:(BOOL)isStorageCritical __attribute__((swift_name("init(remainingStatus:isStorageCritical:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedStorageState *)doCopyRemainingStatus:(NSString * _Nullable)remainingStatus isStorageCritical:(BOOL)isStorageCritical __attribute__((swift_name("doCopy(remainingStatus:isStorageCritical:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL isStorageCritical __attribute__((swift_name("isStorageCritical")));
@property (readonly) NSString * _Nullable remainingStatus __attribute__((swift_name("remainingStatus")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FrameContext")))
@interface DyrectoSharedFrameContext : DyrectoSharedBase
- (instancetype)initWithRenderSequence:(int64_t)renderSequence observedAtMs:(int64_t)observedAtMs frameAgeMs:(int64_t)frameAgeMs frameFps:(double)frameFps latencyMs:(double)latencyMs renderActive:(BOOL)renderActive telemetry:(DyrectoSharedCameraTelemetry * _Nullable)telemetry telemetryTimestampMs:(int64_t)telemetryTimestampMs connectionPhase:(DyrectoSharedConnectionPhase *)connectionPhase liveViewActive:(BOOL)liveViewActive __attribute__((swift_name("init(renderSequence:observedAtMs:frameAgeMs:frameFps:latencyMs:renderActive:telemetry:telemetryTimestampMs:connectionPhase:liveViewActive:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFrameContext *)doCopyRenderSequence:(int64_t)renderSequence observedAtMs:(int64_t)observedAtMs frameAgeMs:(int64_t)frameAgeMs frameFps:(double)frameFps latencyMs:(double)latencyMs renderActive:(BOOL)renderActive telemetry:(DyrectoSharedCameraTelemetry * _Nullable)telemetry telemetryTimestampMs:(int64_t)telemetryTimestampMs connectionPhase:(DyrectoSharedConnectionPhase *)connectionPhase liveViewActive:(BOOL)liveViewActive __attribute__((swift_name("doCopy(renderSequence:observedAtMs:frameAgeMs:frameFps:latencyMs:renderActive:telemetry:telemetryTimestampMs:connectionPhase:liveViewActive:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedConnectionPhase *connectionPhase __attribute__((swift_name("connectionPhase")));
@property (readonly) int64_t frameAgeMs __attribute__((swift_name("frameAgeMs")));
@property (readonly) double frameFps __attribute__((swift_name("frameFps")));
@property (readonly) double latencyMs __attribute__((swift_name("latencyMs")));
@property (readonly) BOOL liveViewActive __attribute__((swift_name("liveViewActive")));
@property (readonly) int64_t observedAtMs __attribute__((swift_name("observedAtMs")));
@property (readonly) BOOL renderActive __attribute__((swift_name("renderActive")));
@property (readonly) int64_t renderSequence __attribute__((swift_name("renderSequence")));
@property (readonly) DyrectoSharedCameraTelemetry * _Nullable telemetry __attribute__((swift_name("telemetry")));
@property (readonly) int64_t telemetryTimestampMs __attribute__((swift_name("telemetryTimestampMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("JpegValidation")))
@interface DyrectoSharedJpegValidation : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)jpegValidation __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedJpegValidation *shared __attribute__((swift_name("shared")));
- (BOOL)isCompleteJpegBuf:(DyrectoSharedKotlinByteArray *)buf start:(int32_t)start endExclusive:(int32_t)endExclusive __attribute__((swift_name("isCompleteJpeg(buf:start:endExclusive:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VericFrame")))
@interface DyrectoSharedVericFrame : DyrectoSharedBase
- (instancetype)initWithIndex:(int32_t)index timestampMs:(int64_t)timestampMs streamOffset:(int64_t)streamOffset jpeg:(DyrectoSharedKotlinByteArray *)jpeg header:(DyrectoSharedKotlinByteArray *)header __attribute__((swift_name("init(index:timestampMs:streamOffset:jpeg:header:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVericFrame *)doCopyIndex:(int32_t)index timestampMs:(int64_t)timestampMs streamOffset:(int64_t)streamOffset jpeg:(DyrectoSharedKotlinByteArray *)jpeg header:(DyrectoSharedKotlinByteArray *)header __attribute__((swift_name("doCopy(index:timestampMs:streamOffset:jpeg:header:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedKotlinByteArray *header __attribute__((swift_name("header")));
@property (readonly) int32_t index __attribute__((swift_name("index")));
@property (readonly) DyrectoSharedKotlinByteArray *jpeg __attribute__((swift_name("jpeg")));
@property (readonly) int64_t streamOffset __attribute__((swift_name("streamOffset")));
@property (readonly) int64_t timestampMs __attribute__((swift_name("timestampMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VericFrameExtractor")))
@interface DyrectoSharedVericFrameExtractor : DyrectoSharedBase
- (instancetype)initWithOnFrame:(void (^)(DyrectoSharedVericFrameRef *))onFrame onError:(void (^)(NSString *))onError clock:(DyrectoSharedLong *(^)(void))clock acceptFrame:(DyrectoSharedBoolean *(^)(DyrectoSharedKotlinByteArray *, DyrectoSharedInt *, DyrectoSharedInt *))acceptFrame __attribute__((swift_name("init(onFrame:onError:clock:acceptFrame:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedVericFrameExtractorCompanion *companion __attribute__((swift_name("companion")));
- (void)appendChunk:(DyrectoSharedKotlinByteArray *)chunk len:(int32_t)len __attribute__((swift_name("append(chunk:len:)")));
- (void)appendChunk:(DyrectoSharedKotlinByteArray *)chunk off:(int32_t)off len:(int32_t)len __attribute__((swift_name("append(chunk:off:len:)")));
@property (readonly) int32_t bufferedBytes __attribute__((swift_name("bufferedBytes")));
@property (readonly) int64_t bytesConsumed __attribute__((swift_name("bytesConsumed")));
@property (readonly) int32_t errors __attribute__((swift_name("errors")));
@property (readonly) int32_t framesEmitted __attribute__((swift_name("framesEmitted")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VericFrameExtractor.Companion")))
@interface DyrectoSharedVericFrameExtractorCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedVericFrameExtractorCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t MAX_JPEG_BYTES __attribute__((swift_name("MAX_JPEG_BYTES")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VericFrameRef")))
@interface DyrectoSharedVericFrameRef : DyrectoSharedBase
- (instancetype)initWithIndex:(int32_t)index timestampMs:(int64_t)timestampMs streamOffset:(int64_t)streamOffset buf:(DyrectoSharedKotlinByteArray *)buf jpegOffset:(int32_t)jpegOffset jpegLength:(int32_t)jpegLength headerOffset:(int32_t)headerOffset headerLength:(int32_t)headerLength __attribute__((swift_name("init(index:timestampMs:streamOffset:buf:jpegOffset:jpegLength:headerOffset:headerLength:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVericFrame *)materialize __attribute__((swift_name("materialize()")));
@property (readonly) DyrectoSharedKotlinByteArray *buf __attribute__((swift_name("buf")));
@property (readonly) int32_t headerLength __attribute__((swift_name("headerLength")));
@property (readonly) int32_t headerOffset __attribute__((swift_name("headerOffset")));
@property (readonly) int32_t index __attribute__((swift_name("index")));
@property (readonly) int32_t jpegLength __attribute__((swift_name("jpegLength")));
@property (readonly) int32_t jpegOffset __attribute__((swift_name("jpegOffset")));
@property (readonly) int64_t streamOffset __attribute__((swift_name("streamOffset")));
@property (readonly) int64_t timestampMs __attribute__((swift_name("timestampMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VericParser")))
@interface DyrectoSharedVericParser : DyrectoSharedBase
- (instancetype)initWithOnFrame:(void (^)(DyrectoSharedVericFrameRef *))onFrame verbose:(BOOL)verbose clock:(DyrectoSharedLong *(^)(void))clock logLine:(void (^)(NSString *))logLine __attribute__((swift_name("init(onFrame:verbose:clock:logLine:)"))) __attribute__((objc_designated_initializer));
- (void)parseChunk:(DyrectoSharedKotlinByteArray *)chunk len:(int32_t)len __attribute__((swift_name("parse(chunk:len:)")));
- (void)parseChunk:(DyrectoSharedKotlinByteArray *)chunk off:(int32_t)off len:(int32_t)len __attribute__((swift_name("parse(chunk:off:len:)")));
@property (readonly) int32_t errors __attribute__((swift_name("errors")));
@property (readonly) int32_t framesEmitted __attribute__((swift_name("framesEmitted")));
@end

__attribute__((swift_name("VisionResult")))
@protocol DyrectoSharedVisionResult
@required
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ColorStatsResult")))
@interface DyrectoSharedColorStatsResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId avgR:(float)avgR avgG:(float)avgG avgB:(float)avgB sampleCount:(int32_t)sampleCount __attribute__((swift_name("init(moduleId:avgR:avgG:avgB:sampleCount:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedColorStatsResult *)doCopyModuleId:(NSString *)moduleId avgR:(float)avgR avgG:(float)avgG avgB:(float)avgB sampleCount:(int32_t)sampleCount __attribute__((swift_name("doCopy(moduleId:avgR:avgG:avgB:sampleCount:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float avgB __attribute__((swift_name("avgB")));
@property (readonly) float avgG __attribute__((swift_name("avgG")));
@property (readonly) float avgR __attribute__((swift_name("avgR")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t sampleCount __attribute__((swift_name("sampleCount")));
@property (readonly) float tintScore __attribute__((swift_name("tintScore")));
@property (readonly) float warmthScore __attribute__((swift_name("warmthScore")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureChannelState")))
@interface DyrectoSharedExposureChannelState : DyrectoSharedKotlinEnum<DyrectoSharedExposureChannelState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedExposureChannelState *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedExposureChannelState *clipping __attribute__((swift_name("clipping")));
@property (class, readonly) DyrectoSharedExposureChannelState *confirmed __attribute__((swift_name("confirmed")));
@property (class, readonly) DyrectoSharedExposureChannelState *recovering __attribute__((swift_name("recovering")));
+ (DyrectoSharedKotlinArray<DyrectoSharedExposureChannelState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedExposureChannelState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureResult")))
@interface DyrectoSharedExposureResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId highlightDetected:(BOOL)highlightDetected shadowDetected:(BOOL)shadowDetected highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage exposureState:(DyrectoSharedExposureVerdict *)exposureState exposureConfidence:(float)exposureConfidence highlightState:(DyrectoSharedExposureChannelState *)highlightState shadowState:(DyrectoSharedExposureChannelState *)shadowState highlightPersistenceFrames:(int32_t)highlightPersistenceFrames shadowPersistenceFrames:(int32_t)shadowPersistenceFrames highlightRecoveryFrames:(int32_t)highlightRecoveryFrames shadowRecoveryFrames:(int32_t)shadowRecoveryFrames highlightConfirmed:(BOOL)highlightConfirmed shadowConfirmed:(BOOL)shadowConfirmed __attribute__((swift_name("init(moduleId:highlightDetected:shadowDetected:highlightCoverage:shadowCoverage:exposureState:exposureConfidence:highlightState:shadowState:highlightPersistenceFrames:shadowPersistenceFrames:highlightRecoveryFrames:shadowRecoveryFrames:highlightConfirmed:shadowConfirmed:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedExposureResult *)doCopyModuleId:(NSString *)moduleId highlightDetected:(BOOL)highlightDetected shadowDetected:(BOOL)shadowDetected highlightCoverage:(float)highlightCoverage shadowCoverage:(float)shadowCoverage exposureState:(DyrectoSharedExposureVerdict *)exposureState exposureConfidence:(float)exposureConfidence highlightState:(DyrectoSharedExposureChannelState *)highlightState shadowState:(DyrectoSharedExposureChannelState *)shadowState highlightPersistenceFrames:(int32_t)highlightPersistenceFrames shadowPersistenceFrames:(int32_t)shadowPersistenceFrames highlightRecoveryFrames:(int32_t)highlightRecoveryFrames shadowRecoveryFrames:(int32_t)shadowRecoveryFrames highlightConfirmed:(BOOL)highlightConfirmed shadowConfirmed:(BOOL)shadowConfirmed __attribute__((swift_name("doCopy(moduleId:highlightDetected:shadowDetected:highlightCoverage:shadowCoverage:exposureState:exposureConfidence:highlightState:shadowState:highlightPersistenceFrames:shadowPersistenceFrames:highlightRecoveryFrames:shadowRecoveryFrames:highlightConfirmed:shadowConfirmed:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float exposureConfidence __attribute__((swift_name("exposureConfidence")));
@property (readonly) DyrectoSharedExposureVerdict *exposureState __attribute__((swift_name("exposureState")));
@property (readonly) BOOL highlightConfirmed __attribute__((swift_name("highlightConfirmed")));
@property (readonly) float highlightCoverage __attribute__((swift_name("highlightCoverage")));
@property (readonly) BOOL highlightDetected __attribute__((swift_name("highlightDetected")));
@property (readonly) int32_t highlightPersistenceFrames __attribute__((swift_name("highlightPersistenceFrames")));
@property (readonly) int32_t highlightRecoveryFrames __attribute__((swift_name("highlightRecoveryFrames")));
@property (readonly) DyrectoSharedExposureChannelState *highlightState __attribute__((swift_name("highlightState")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) BOOL shadowConfirmed __attribute__((swift_name("shadowConfirmed")));
@property (readonly) float shadowCoverage __attribute__((swift_name("shadowCoverage")));
@property (readonly) BOOL shadowDetected __attribute__((swift_name("shadowDetected")));
@property (readonly) int32_t shadowPersistenceFrames __attribute__((swift_name("shadowPersistenceFrames")));
@property (readonly) int32_t shadowRecoveryFrames __attribute__((swift_name("shadowRecoveryFrames")));
@property (readonly) DyrectoSharedExposureChannelState *shadowState __attribute__((swift_name("shadowState")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExposureVerdict")))
@interface DyrectoSharedExposureVerdict : DyrectoSharedKotlinEnum<DyrectoSharedExposureVerdict *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedExposureVerdict *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedExposureVerdict *highlightClip __attribute__((swift_name("highlightClip")));
@property (class, readonly) DyrectoSharedExposureVerdict *shadowClip __attribute__((swift_name("shadowClip")));
@property (class, readonly) DyrectoSharedExposureVerdict *mixed __attribute__((swift_name("mixed")));
+ (DyrectoSharedKotlinArray<DyrectoSharedExposureVerdict *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedExposureVerdict *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EyeBox")))
@interface DyrectoSharedEyeBox : DyrectoSharedBase
- (instancetype)initWithRect:(DyrectoSharedPixelRect *)rect confidence:(float)confidence __attribute__((swift_name("init(rect:confidence:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedEyeBox *)doCopyRect:(DyrectoSharedPixelRect *)rect confidence:(float)confidence __attribute__((swift_name("doCopy(rect:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedPixelRect *rect __attribute__((swift_name("rect")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EyeDetectionResult")))
@interface DyrectoSharedEyeDetectionResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId eyesDetected:(int32_t)eyesDetected boundingBoxes:(NSArray<DyrectoSharedEyeBox *> *)boundingBoxes averageConfidence:(float)averageConfidence analysisTimeMs:(int64_t)analysisTimeMs __attribute__((swift_name("init(moduleId:eyesDetected:boundingBoxes:averageConfidence:analysisTimeMs:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedEyeDetectionResult *)doCopyModuleId:(NSString *)moduleId eyesDetected:(int32_t)eyesDetected boundingBoxes:(NSArray<DyrectoSharedEyeBox *> *)boundingBoxes averageConfidence:(float)averageConfidence analysisTimeMs:(int64_t)analysisTimeMs __attribute__((swift_name("doCopy(moduleId:eyesDetected:boundingBoxes:averageConfidence:analysisTimeMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t analysisTimeMs __attribute__((swift_name("analysisTimeMs")));
@property (readonly) float averageConfidence __attribute__((swift_name("averageConfidence")));
@property (readonly) NSArray<DyrectoSharedEyeBox *> *boundingBoxes __attribute__((swift_name("boundingBoxes")));
@property (readonly) int32_t eyesDetected __attribute__((swift_name("eyesDetected")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FaceBox")))
@interface DyrectoSharedFaceBox : DyrectoSharedBase
- (instancetype)initWithRect:(DyrectoSharedPixelRect *)rect confidence:(float)confidence __attribute__((swift_name("init(rect:confidence:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFaceBox *)doCopyRect:(DyrectoSharedPixelRect *)rect confidence:(float)confidence __attribute__((swift_name("doCopy(rect:confidence:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float confidence __attribute__((swift_name("confidence")));
@property (readonly) DyrectoSharedPixelRect *rect __attribute__((swift_name("rect")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FaceDetectionResult")))
@interface DyrectoSharedFaceDetectionResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId facesDetected:(int32_t)facesDetected boundingBoxes:(NSArray<DyrectoSharedFaceBox *> *)boundingBoxes averageConfidence:(float)averageConfidence analysisTimeMs:(int64_t)analysisTimeMs sourceWidth:(int32_t)sourceWidth sourceHeight:(int32_t)sourceHeight __attribute__((swift_name("init(moduleId:facesDetected:boundingBoxes:averageConfidence:analysisTimeMs:sourceWidth:sourceHeight:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFaceDetectionResult *)doCopyModuleId:(NSString *)moduleId facesDetected:(int32_t)facesDetected boundingBoxes:(NSArray<DyrectoSharedFaceBox *> *)boundingBoxes averageConfidence:(float)averageConfidence analysisTimeMs:(int64_t)analysisTimeMs sourceWidth:(int32_t)sourceWidth sourceHeight:(int32_t)sourceHeight __attribute__((swift_name("doCopy(moduleId:facesDetected:boundingBoxes:averageConfidence:analysisTimeMs:sourceWidth:sourceHeight:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t analysisTimeMs __attribute__((swift_name("analysisTimeMs")));
@property (readonly) float averageConfidence __attribute__((swift_name("averageConfidence")));
@property (readonly) NSArray<DyrectoSharedFaceBox *> *boundingBoxes __attribute__((swift_name("boundingBoxes")));
@property (readonly) int32_t facesDetected __attribute__((swift_name("facesDetected")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t sourceHeight __attribute__((swift_name("sourceHeight")));
@property (readonly) int32_t sourceWidth __attribute__((swift_name("sourceWidth")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FalseColorBand")))
@interface DyrectoSharedFalseColorBand : DyrectoSharedBase
- (instancetype)initWithIndex:(int32_t)index label:(NSString *)label loIre:(int32_t)loIre hiIre:(int32_t)hiIre colorRgb:(int32_t)colorRgb pixels:(int32_t)pixels percentage:(float)percentage __attribute__((swift_name("init(index:label:loIre:hiIre:colorRgb:pixels:percentage:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFalseColorBand *)doCopyIndex:(int32_t)index label:(NSString *)label loIre:(int32_t)loIre hiIre:(int32_t)hiIre colorRgb:(int32_t)colorRgb pixels:(int32_t)pixels percentage:(float)percentage __attribute__((swift_name("doCopy(index:label:loIre:hiIre:colorRgb:pixels:percentage:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t colorRgb __attribute__((swift_name("colorRgb")));
@property (readonly) int32_t hiIre __attribute__((swift_name("hiIre")));
@property (readonly) int32_t index __attribute__((swift_name("index")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) int32_t loIre __attribute__((swift_name("loIre")));
@property (readonly) float percentage __attribute__((swift_name("percentage")));
@property (readonly) int32_t pixels __attribute__((swift_name("pixels")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FalseColorResult")))
@interface DyrectoSharedFalseColorResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId bands:(NSArray<DyrectoSharedFalseColorBand *> *)bands totalSamples:(int32_t)totalSamples effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("init(moduleId:bands:totalSamples:effectiveStride:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFalseColorResult *)doCopyModuleId:(NSString *)moduleId bands:(NSArray<DyrectoSharedFalseColorBand *> *)bands totalSamples:(int32_t)totalSamples effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("doCopy(moduleId:bands:totalSamples:effectiveStride:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<DyrectoSharedFalseColorBand *> *bands __attribute__((swift_name("bands")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t totalSamples __attribute__((swift_name("totalSamples")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FocusPeakingResult")))
@interface DyrectoSharedFocusPeakingResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId peakedSamples:(int32_t)peakedSamples totalSamples:(int32_t)totalSamples coveragePercentage:(float)coveragePercentage threshold:(int32_t)threshold cols:(int32_t)cols rows:(int32_t)rows mask:(DyrectoSharedKotlinByteArray * _Nullable)mask effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("init(moduleId:peakedSamples:totalSamples:coveragePercentage:threshold:cols:rows:mask:effectiveStride:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedFocusPeakingResult *)doCopyModuleId:(NSString *)moduleId peakedSamples:(int32_t)peakedSamples totalSamples:(int32_t)totalSamples coveragePercentage:(float)coveragePercentage threshold:(int32_t)threshold cols:(int32_t)cols rows:(int32_t)rows mask:(DyrectoSharedKotlinByteArray * _Nullable)mask effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("doCopy(moduleId:peakedSamples:totalSamples:coveragePercentage:threshold:cols:rows:mask:effectiveStride:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t cols __attribute__((swift_name("cols")));
@property (readonly) float coveragePercentage __attribute__((swift_name("coveragePercentage")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) DyrectoSharedKotlinByteArray * _Nullable mask __attribute__((swift_name("mask")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t peakedSamples __attribute__((swift_name("peakedSamples")));
@property (readonly) int32_t rows __attribute__((swift_name("rows")));
@property (readonly) int32_t threshold __attribute__((swift_name("threshold")));
@property (readonly) int32_t totalSamples __attribute__((swift_name("totalSamples")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HistogramResult")))
@interface DyrectoSharedHistogramResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId bins:(DyrectoSharedKotlinIntArray *)bins totalPixels:(int32_t)totalPixels mean:(float)mean median:(int32_t)median percentile95:(int32_t)percentile95 percentile99:(int32_t)percentile99 clippedHighlightPixels:(int32_t)clippedHighlightPixels clippedShadowPixels:(int32_t)clippedShadowPixels clippedHighlightPercentage:(float)clippedHighlightPercentage clippedShadowPercentage:(float)clippedShadowPercentage effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("init(moduleId:bins:totalPixels:mean:median:percentile95:percentile99:clippedHighlightPixels:clippedShadowPixels:clippedHighlightPercentage:clippedShadowPercentage:effectiveStride:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedHistogramResult *)doCopyModuleId:(NSString *)moduleId bins:(DyrectoSharedKotlinIntArray *)bins totalPixels:(int32_t)totalPixels mean:(float)mean median:(int32_t)median percentile95:(int32_t)percentile95 percentile99:(int32_t)percentile99 clippedHighlightPixels:(int32_t)clippedHighlightPixels clippedShadowPixels:(int32_t)clippedShadowPixels clippedHighlightPercentage:(float)clippedHighlightPercentage clippedShadowPercentage:(float)clippedShadowPercentage effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("doCopy(moduleId:bins:totalPixels:mean:median:percentile95:percentile99:clippedHighlightPixels:clippedShadowPixels:clippedHighlightPercentage:clippedShadowPercentage:effectiveStride:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedKotlinIntArray *bins __attribute__((swift_name("bins")));
@property (readonly) float clippedHighlightPercentage __attribute__((swift_name("clippedHighlightPercentage")));
@property (readonly) int32_t clippedHighlightPixels __attribute__((swift_name("clippedHighlightPixels")));
@property (readonly) float clippedShadowPercentage __attribute__((swift_name("clippedShadowPercentage")));
@property (readonly) int32_t clippedShadowPixels __attribute__((swift_name("clippedShadowPixels")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) float mean __attribute__((swift_name("mean")));
@property (readonly) int32_t median __attribute__((swift_name("median")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t percentile95 __attribute__((swift_name("percentile95")));
@property (readonly) int32_t percentile99 __attribute__((swift_name("percentile99")));
@property (readonly) int32_t totalPixels __attribute__((swift_name("totalPixels")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PixelRect")))
@interface DyrectoSharedPixelRect : DyrectoSharedBase
- (instancetype)initWithLeft:(int32_t)left top:(int32_t)top right:(int32_t)right bottom:(int32_t)bottom __attribute__((swift_name("init(left:top:right:bottom:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedPixelRect *)doCopyLeft:(int32_t)left top:(int32_t)top right:(int32_t)right bottom:(int32_t)bottom __attribute__((swift_name("doCopy(left:top:right:bottom:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t bottom __attribute__((swift_name("bottom")));
@property (readonly) int32_t left __attribute__((swift_name("left")));
@property (readonly) int32_t right __attribute__((swift_name("right")));
@property (readonly) int32_t top __attribute__((swift_name("top")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SceneSnapshotResult")))
@interface DyrectoSharedSceneSnapshotResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId snapshot:(DyrectoSharedSceneSnapshot *)snapshot skippedInferences:(int64_t)skippedInferences __attribute__((swift_name("init(moduleId:snapshot:skippedInferences:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSceneSnapshotResult *)doCopyModuleId:(NSString *)moduleId snapshot:(DyrectoSharedSceneSnapshot *)snapshot skippedInferences:(int64_t)skippedInferences __attribute__((swift_name("doCopy(moduleId:snapshot:skippedInferences:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int64_t skippedInferences __attribute__((swift_name("skippedInferences")));
@property (readonly) DyrectoSharedSceneSnapshot *snapshot __attribute__((swift_name("snapshot")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SubjectExposureResult")))
@interface DyrectoSharedSubjectExposureResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId stats:(DyrectoSharedSubjectExposureStats *)stats __attribute__((swift_name("init(moduleId:stats:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSubjectExposureResult *)doCopyModuleId:(NSString *)moduleId stats:(DyrectoSharedSubjectExposureStats *)stats __attribute__((swift_name("doCopy(moduleId:stats:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) DyrectoSharedSubjectExposureStats *stats __attribute__((swift_name("stats")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VisionContext")))
@interface DyrectoSharedVisionContext : DyrectoSharedBase
- (instancetype)initWithHistogram:(DyrectoSharedHistogramResult * _Nullable)histogram zebra:(DyrectoSharedZebraResult * _Nullable)zebra exposure:(DyrectoSharedExposureResult * _Nullable)exposure faces:(DyrectoSharedFaceDetectionResult * _Nullable)faces eyes:(DyrectoSharedEyeDetectionResult * _Nullable)eyes colorStats:(DyrectoSharedColorStatsResult * _Nullable)colorStats aiScene:(DyrectoSharedSceneSnapshotResult * _Nullable)aiScene subjectExposure:(DyrectoSharedSubjectExposureResult * _Nullable)subjectExposure waveform:(DyrectoSharedWaveformResult * _Nullable)waveform falseColor:(DyrectoSharedFalseColorResult * _Nullable)falseColor focusPeaking:(DyrectoSharedFocusPeakingResult * _Nullable)focusPeaking updatedAtMs:(int64_t)updatedAtMs __attribute__((swift_name("init(histogram:zebra:exposure:faces:eyes:colorStats:aiScene:subjectExposure:waveform:falseColor:focusPeaking:updatedAtMs:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVisionContext *)doCopyHistogram:(DyrectoSharedHistogramResult * _Nullable)histogram zebra:(DyrectoSharedZebraResult * _Nullable)zebra exposure:(DyrectoSharedExposureResult * _Nullable)exposure faces:(DyrectoSharedFaceDetectionResult * _Nullable)faces eyes:(DyrectoSharedEyeDetectionResult * _Nullable)eyes colorStats:(DyrectoSharedColorStatsResult * _Nullable)colorStats aiScene:(DyrectoSharedSceneSnapshotResult * _Nullable)aiScene subjectExposure:(DyrectoSharedSubjectExposureResult * _Nullable)subjectExposure waveform:(DyrectoSharedWaveformResult * _Nullable)waveform falseColor:(DyrectoSharedFalseColorResult * _Nullable)falseColor focusPeaking:(DyrectoSharedFocusPeakingResult * _Nullable)focusPeaking updatedAtMs:(int64_t)updatedAtMs __attribute__((swift_name("doCopy(histogram:zebra:exposure:faces:eyes:colorStats:aiScene:subjectExposure:waveform:falseColor:focusPeaking:updatedAtMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedSceneSnapshotResult * _Nullable aiScene __attribute__((swift_name("aiScene")));
@property (readonly) DyrectoSharedColorStatsResult * _Nullable colorStats __attribute__((swift_name("colorStats")));
@property (readonly) DyrectoSharedExposureResult * _Nullable exposure __attribute__((swift_name("exposure")));
@property (readonly) DyrectoSharedEyeDetectionResult * _Nullable eyes __attribute__((swift_name("eyes")));
@property (readonly) DyrectoSharedFaceDetectionResult * _Nullable faces __attribute__((swift_name("faces")));
@property (readonly) DyrectoSharedFalseColorResult * _Nullable falseColor __attribute__((swift_name("falseColor")));
@property (readonly) DyrectoSharedFocusPeakingResult * _Nullable focusPeaking __attribute__((swift_name("focusPeaking")));
@property (readonly) DyrectoSharedHistogramResult * _Nullable histogram __attribute__((swift_name("histogram")));
@property (readonly) DyrectoSharedSubjectExposureResult * _Nullable subjectExposure __attribute__((swift_name("subjectExposure")));
@property (readonly) int64_t updatedAtMs __attribute__((swift_name("updatedAtMs")));
@property (readonly) DyrectoSharedWaveformResult * _Nullable waveform __attribute__((swift_name("waveform")));
@property (readonly) DyrectoSharedZebraResult * _Nullable zebra __attribute__((swift_name("zebra")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WaveformResult")))
@interface DyrectoSharedWaveformResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId columns:(int32_t)columns bins:(int32_t)bins intensity:(DyrectoSharedKotlinIntArray *)intensity maxIntensity:(int32_t)maxIntensity totalSamples:(int32_t)totalSamples effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("init(moduleId:columns:bins:intensity:maxIntensity:totalSamples:effectiveStride:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedWaveformResult *)doCopyModuleId:(NSString *)moduleId columns:(int32_t)columns bins:(int32_t)bins intensity:(DyrectoSharedKotlinIntArray *)intensity maxIntensity:(int32_t)maxIntensity totalSamples:(int32_t)totalSamples effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("doCopy(moduleId:columns:bins:intensity:maxIntensity:totalSamples:effectiveStride:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t bins __attribute__((swift_name("bins")));
@property (readonly) int32_t columns __attribute__((swift_name("columns")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) DyrectoSharedKotlinIntArray *intensity __attribute__((swift_name("intensity")));
@property (readonly) int32_t maxIntensity __attribute__((swift_name("maxIntensity")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t totalSamples __attribute__((swift_name("totalSamples")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZebraResult")))
@interface DyrectoSharedZebraResult : DyrectoSharedBase <DyrectoSharedVisionResult>
- (instancetype)initWithModuleId:(NSString *)moduleId spec:(id<DyrectoSharedZebraSpec>)spec pixelsMatched:(int32_t)pixelsMatched totalPixels:(int32_t)totalPixels coveragePercentage:(float)coveragePercentage mask:(DyrectoSharedKotlinByteArray * _Nullable)mask effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("init(moduleId:spec:pixelsMatched:totalPixels:coveragePercentage:mask:effectiveStride:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedZebraResult *)doCopyModuleId:(NSString *)moduleId spec:(id<DyrectoSharedZebraSpec>)spec pixelsMatched:(int32_t)pixelsMatched totalPixels:(int32_t)totalPixels coveragePercentage:(float)coveragePercentage mask:(DyrectoSharedKotlinByteArray * _Nullable)mask effectiveStride:(int32_t)effectiveStride __attribute__((swift_name("doCopy(moduleId:spec:pixelsMatched:totalPixels:coveragePercentage:mask:effectiveStride:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float coveragePercentage __attribute__((swift_name("coveragePercentage")));
@property (readonly) int32_t effectiveStride __attribute__((swift_name("effectiveStride")));
@property (readonly) DyrectoSharedKotlinByteArray * _Nullable mask __attribute__((swift_name("mask")));
@property (readonly) NSString *moduleId __attribute__((swift_name("moduleId")));
@property (readonly) int32_t pixelsMatched __attribute__((swift_name("pixelsMatched")));
@property (readonly) id<DyrectoSharedZebraSpec> spec __attribute__((swift_name("spec")));
@property (readonly) int32_t totalPixels __attribute__((swift_name("totalPixels")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CooldownTracker")))
@interface DyrectoSharedCooldownTracker : DyrectoSharedBase
- (instancetype)initWithClock:(DyrectoSharedLong *(^)(void))clock __attribute__((swift_name("init(clock:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedLong * _Nullable)lastSpokenAtKey:(id<DyrectoSharedVoiceKey>)key __attribute__((swift_name("lastSpokenAt(key:)")));
- (void)markDisappearedKey:(id<DyrectoSharedVoiceKey>)key __attribute__((swift_name("markDisappeared(key:)")));
- (void)markSpokenKey:(id<DyrectoSharedVoiceKey>)key __attribute__((swift_name("markSpoken(key:)")));
- (BOOL)readyKey:(id<DyrectoSharedVoiceKey>)key __attribute__((swift_name("ready(key:)")));
- (void)reset __attribute__((swift_name("reset()")));
- (NSDictionary<id<DyrectoSharedVoiceKey>, DyrectoSharedLong *> *)snapshotRemaining __attribute__((swift_name("snapshotRemaining()")));
@property int64_t assistantCooldownMs __attribute__((swift_name("assistantCooldownMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceCooldowns")))
@interface DyrectoSharedVoiceCooldowns : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)voiceCooldowns __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedVoiceCooldowns *shared __attribute__((swift_name("shared")));
- (int32_t)clampReminderSecondsSeconds:(int32_t)seconds __attribute__((swift_name("clampReminderSeconds(seconds:)")));
- (int64_t)forTelemetryType:(DyrectoSharedAlertType *)type __attribute__((swift_name("forTelemetry(type:)")));
@property (readonly) int32_t ASSISTANT_REMINDER_DEFAULT_SECONDS __attribute__((swift_name("ASSISTANT_REMINDER_DEFAULT_SECONDS")));
@property (readonly) int32_t ASSISTANT_REMINDER_MAX_SECONDS __attribute__((swift_name("ASSISTANT_REMINDER_MAX_SECONDS")));
@property (readonly) int32_t ASSISTANT_REMINDER_MIN_SECONDS __attribute__((swift_name("ASSISTANT_REMINDER_MIN_SECONDS")));
@property (readonly) int64_t CRITICAL_MS __attribute__((swift_name("CRITICAL_MS")));
@property (readonly) int64_t MIN_SPEECH_GAP_MS __attribute__((swift_name("MIN_SPEECH_GAP_MS")));
@property (readonly) float PROGRESS_EPSILON __attribute__((swift_name("PROGRESS_EPSILON")));
@property (readonly) int64_t PROGRESS_STALL_WINDOW_MS __attribute__((swift_name("PROGRESS_STALL_WINDOW_MS")));
@property (readonly) int64_t TELEMETRY_DEFAULT_MS __attribute__((swift_name("TELEMETRY_DEFAULT_MS")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceDebugState")))
@interface DyrectoSharedVoiceDebugState : DyrectoSharedBase
- (instancetype)initWithEnabled:(BOOL)enabled speaking:(NSString * _Nullable)speaking queued:(NSArray<NSString *> *)queued cooldownsRemainingMs:(NSDictionary<NSString *, DyrectoSharedLong *> *)cooldownsRemainingMs suppressedCount:(int32_t)suppressedCount lastSuppressedReason:(NSString * _Nullable)lastSuppressedReason lastSpoken:(NSString * _Nullable)lastSpoken spokenCount:(int32_t)spokenCount __attribute__((swift_name("init(enabled:speaking:queued:cooldownsRemainingMs:suppressedCount:lastSuppressedReason:lastSpoken:spokenCount:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVoiceDebugState *)doCopyEnabled:(BOOL)enabled speaking:(NSString * _Nullable)speaking queued:(NSArray<NSString *> *)queued cooldownsRemainingMs:(NSDictionary<NSString *, DyrectoSharedLong *> *)cooldownsRemainingMs suppressedCount:(int32_t)suppressedCount lastSuppressedReason:(NSString * _Nullable)lastSuppressedReason lastSpoken:(NSString * _Nullable)lastSpoken spokenCount:(int32_t)spokenCount __attribute__((swift_name("doCopy(enabled:speaking:queued:cooldownsRemainingMs:suppressedCount:lastSuppressedReason:lastSpoken:spokenCount:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<NSString *, DyrectoSharedLong *> *cooldownsRemainingMs __attribute__((swift_name("cooldownsRemainingMs")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));
@property (readonly) NSString * _Nullable lastSpoken __attribute__((swift_name("lastSpoken")));
@property (readonly) NSString * _Nullable lastSuppressedReason __attribute__((swift_name("lastSuppressedReason")));
@property (readonly) NSArray<NSString *> *queued __attribute__((swift_name("queued")));
@property (readonly) NSString * _Nullable speaking __attribute__((swift_name("speaking")));
@property (readonly) int32_t spokenCount __attribute__((swift_name("spokenCount")));
@property (readonly) int32_t suppressedCount __attribute__((swift_name("suppressedCount")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceEvent")))
@interface DyrectoSharedVoiceEvent : DyrectoSharedBase
- (instancetype)initWithSource:(DyrectoSharedVoiceSource *)source priority:(DyrectoSharedVoicePriority *)priority key:(id<DyrectoSharedVoiceKey>)key text:(NSString *)text interruptible:(BOOL)interruptible repeatable:(BOOL)repeatable timestamp:(int64_t)timestamp __attribute__((swift_name("init(source:priority:key:text:interruptible:repeatable:timestamp:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVoiceEvent *)doCopySource:(DyrectoSharedVoiceSource *)source priority:(DyrectoSharedVoicePriority *)priority key:(id<DyrectoSharedVoiceKey>)key text:(NSString *)text interruptible:(BOOL)interruptible repeatable:(BOOL)repeatable timestamp:(int64_t)timestamp __attribute__((swift_name("doCopy(source:priority:key:text:interruptible:repeatable:timestamp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL interruptible __attribute__((swift_name("interruptible")));
@property (readonly) id<DyrectoSharedVoiceKey> key __attribute__((swift_name("key")));
@property (readonly) DyrectoSharedVoicePriority *priority __attribute__((swift_name("priority")));
@property (readonly) BOOL repeatable __attribute__((swift_name("repeatable")));
@property (readonly) DyrectoSharedVoiceSource *source __attribute__((swift_name("source")));
@property (readonly) NSString *text __attribute__((swift_name("text")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@end

__attribute__((swift_name("VoiceKey")))
@protocol DyrectoSharedVoiceKey
@required
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceKeyAssistant")))
@interface DyrectoSharedVoiceKeyAssistant : DyrectoSharedBase <DyrectoSharedVoiceKey>
- (instancetype)initWithAction:(DyrectoSharedAssistantAction *)action __attribute__((swift_name("init(action:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVoiceKeyAssistant *)doCopyAction:(DyrectoSharedAssistantAction *)action __attribute__((swift_name("doCopy(action:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAssistantAction *action __attribute__((swift_name("action")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceKeyReferenceMatched")))
@interface DyrectoSharedVoiceKeyReferenceMatched : DyrectoSharedBase <DyrectoSharedVoiceKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)referenceMatched __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedVoiceKeyReferenceMatched *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceKeyShotCompleted")))
@interface DyrectoSharedVoiceKeyShotCompleted : DyrectoSharedBase <DyrectoSharedVoiceKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)shotCompleted __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedVoiceKeyShotCompleted *shared __attribute__((swift_name("shared")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceKeyTelemetry")))
@interface DyrectoSharedVoiceKeyTelemetry : DyrectoSharedBase <DyrectoSharedVoiceKey>
- (instancetype)initWithType:(DyrectoSharedAlertType *)type __attribute__((swift_name("init(type:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedVoiceKeyTelemetry *)doCopyType:(DyrectoSharedAlertType *)type __attribute__((swift_name("doCopy(type:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) DyrectoSharedAlertType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceMode")))
@interface DyrectoSharedVoiceMode : DyrectoSharedKotlinEnum<DyrectoSharedVoiceMode *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedVoiceMode *assistantOnly __attribute__((swift_name("assistantOnly")));
@property (class, readonly) DyrectoSharedVoiceMode *telemetryOnly __attribute__((swift_name("telemetryOnly")));
@property (class, readonly) DyrectoSharedVoiceMode *both __attribute__((swift_name("both")));
+ (DyrectoSharedKotlinArray<DyrectoSharedVoiceMode *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedVoiceMode *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoicePriority")))
@interface DyrectoSharedVoicePriority : DyrectoSharedKotlinEnum<DyrectoSharedVoicePriority *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedVoicePriority *critical __attribute__((swift_name("critical")));
@property (class, readonly) DyrectoSharedVoicePriority *high __attribute__((swift_name("high")));
@property (class, readonly) DyrectoSharedVoicePriority *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedVoicePriority *low __attribute__((swift_name("low")));
+ (DyrectoSharedKotlinArray<DyrectoSharedVoicePriority *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedVoicePriority *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceQueue")))
@interface DyrectoSharedVoiceQueue : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)clear __attribute__((swift_name("clear()")));
- (void)offerEvent:(DyrectoSharedVoiceEvent *)event __attribute__((swift_name("offer(event:)")));
- (DyrectoSharedVoiceEvent * _Nullable)peek __attribute__((swift_name("peek()")));
- (DyrectoSharedVoiceEvent * _Nullable)poll __attribute__((swift_name("poll()")));
- (void)removeAllPredicate:(DyrectoSharedBoolean *(^)(DyrectoSharedVoiceEvent *))predicate __attribute__((swift_name("removeAll(predicate:)")));
- (void)removeByKeyKey:(id<DyrectoSharedVoiceKey>)key __attribute__((swift_name("removeByKey(key:)")));
- (NSArray<DyrectoSharedVoiceEvent *> *)snapshot __attribute__((swift_name("snapshot()")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceScheduler")))
@interface DyrectoSharedVoiceScheduler : DyrectoSharedBase
- (instancetype)initWithEngine:(id<DyrectoSharedVoiceSpeechEngine>)engine clock:(DyrectoSharedLong *(^)(void))clock __attribute__((swift_name("init(engine:clock:)"))) __attribute__((objc_designated_initializer));
- (void)onAssistantInstructionInstruction:(DyrectoSharedAssistantInstruction * _Nullable)instruction monitoringActive:(BOOL)monitoringActive __attribute__((swift_name("onAssistantInstruction(instruction:monitoringActive:)")));
- (void)onGapElapsed __attribute__((swift_name("onGapElapsed()")));
- (void)onShotCompleted __attribute__((swift_name("onShotCompleted()")));
- (void)onTelemetryAlertAlert:(DyrectoSharedAlert *)alert __attribute__((swift_name("onTelemetryAlert(alert:)")));
- (void)onUtteranceFinished __attribute__((swift_name("onUtteranceFinished()")));
- (void)reset __attribute__((swift_name("reset()")));
- (void)updateSettingsS:(DyrectoSharedVoiceSettings *)s __attribute__((swift_name("updateSettings(s:)")));
@property (readonly) id<DyrectoSharedStateFlow> debug __attribute__((swift_name("debug")));
@property void (^ _Nullable onScheduleRecheck)(DyrectoSharedLong *) __attribute__((swift_name("onScheduleRecheck")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceSettings")))
@interface DyrectoSharedVoiceSettings : DyrectoSharedBase
- (instancetype)initWithEnabled:(BOOL)enabled mode:(DyrectoSharedVoiceMode *)mode speechRate:(DyrectoSharedVoiceSpeechRate *)speechRate assistantReminderSeconds:(int32_t)assistantReminderSeconds __attribute__((swift_name("init(enabled:mode:speechRate:assistantReminderSeconds:)"))) __attribute__((objc_designated_initializer));
- (BOOL)allowsSource:(DyrectoSharedVoiceSource *)source __attribute__((swift_name("allows(source:)")));
- (DyrectoSharedVoiceSettings *)doCopyEnabled:(BOOL)enabled mode:(DyrectoSharedVoiceMode *)mode speechRate:(DyrectoSharedVoiceSpeechRate *)speechRate assistantReminderSeconds:(int32_t)assistantReminderSeconds __attribute__((swift_name("doCopy(enabled:mode:speechRate:assistantReminderSeconds:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t assistantReminderMs __attribute__((swift_name("assistantReminderMs")));
@property (readonly) int32_t assistantReminderSeconds __attribute__((swift_name("assistantReminderSeconds")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));
@property (readonly) DyrectoSharedVoiceMode *mode __attribute__((swift_name("mode")));
@property (readonly) DyrectoSharedVoiceSpeechRate *speechRate __attribute__((swift_name("speechRate")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceSource")))
@interface DyrectoSharedVoiceSource : DyrectoSharedKotlinEnum<DyrectoSharedVoiceSource *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedVoiceSource *assistant __attribute__((swift_name("assistant")));
@property (class, readonly) DyrectoSharedVoiceSource *telemetry __attribute__((swift_name("telemetry")));
+ (DyrectoSharedKotlinArray<DyrectoSharedVoiceSource *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedVoiceSource *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("VoiceSpeechEngine")))
@protocol DyrectoSharedVoiceSpeechEngine
@required
- (void)setOnUtteranceFinishedListener:(void (^)(void))listener __attribute__((swift_name("setOnUtteranceFinished(listener:)")));
- (void)setSpeechRateRate:(DyrectoSharedVoiceSpeechRate *)rate __attribute__((swift_name("setSpeechRate(rate:)")));
- (void)shutdown __attribute__((swift_name("shutdown()")));
- (void)speakEvent:(DyrectoSharedVoiceEvent *)event __attribute__((swift_name("speak(event:)")));
- (void)stop __attribute__((swift_name("stop()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceSpeechRate")))
@interface DyrectoSharedVoiceSpeechRate : DyrectoSharedKotlinEnum<DyrectoSharedVoiceSpeechRate *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedVoiceSpeechRate *slow __attribute__((swift_name("slow")));
@property (class, readonly) DyrectoSharedVoiceSpeechRate *normal __attribute__((swift_name("normal")));
@property (class, readonly) DyrectoSharedVoiceSpeechRate *fast __attribute__((swift_name("fast")));
+ (DyrectoSharedKotlinArray<DyrectoSharedVoiceSpeechRate *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedVoiceSpeechRate *> *entries __attribute__((swift_name("entries")));
@property (readonly) float ttsRate __attribute__((swift_name("ttsRate")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("VoiceTemplates")))
@interface DyrectoSharedVoiceTemplates : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)voiceTemplates __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedVoiceTemplates *shared __attribute__((swift_name("shared")));
- (NSString * _Nullable)forActionAction:(DyrectoSharedAssistantAction *)action __attribute__((swift_name("forAction(action:)")));
- (NSString * _Nullable)forAlertType:(DyrectoSharedAlertType *)type __attribute__((swift_name("forAlert(type:)")));
- (BOOL)isCriticalTelemetryType:(DyrectoSharedAlertType *)type __attribute__((swift_name("isCriticalTelemetry(type:)")));
- (DyrectoSharedVoicePriority *)telemetryPriorityType:(DyrectoSharedAlertType *)type __attribute__((swift_name("telemetryPriority(type:)")));
@property (readonly) NSString *REFERENCE_MATCHED __attribute__((swift_name("REFERENCE_MATCHED")));
@property (readonly) NSString *SHOT_COMPLETED __attribute__((swift_name("SHOT_COMPLETED")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicLong")))
@interface DyrectoSharedAtomicLong : DyrectoSharedBase
- (instancetype)initWithInitial:(int64_t)initial __attribute__((swift_name("init(initial:)"))) __attribute__((objc_designated_initializer));
- (int64_t)getAndIncrement __attribute__((swift_name("getAndIncrement()")));
- (void)setValue:(int64_t)value __attribute__((swift_name("set(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformImage")))
@interface DyrectoSharedPlatformImage : DyrectoSharedBase
- (instancetype)initWithHandle:(id)handle __attribute__((swift_name("init(handle:)"))) __attribute__((objc_designated_initializer));
@property (readonly) id handle __attribute__((swift_name("handle")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelState")))
@interface DyrectoSharedChannelState : DyrectoSharedKotlinEnum<DyrectoSharedChannelState *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedChannelState *idle __attribute__((swift_name("idle")));
@property (class, readonly) DyrectoSharedChannelState *open __attribute__((swift_name("open")));
@property (class, readonly) DyrectoSharedChannelState *failed __attribute__((swift_name("failed")));
+ (DyrectoSharedKotlinArray<DyrectoSharedChannelState *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedChannelState *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PtpStatus")))
@interface DyrectoSharedPtpStatus : DyrectoSharedBase
- (instancetype)initWithTunnelUp:(BOOL)tunnelUp commandChannel:(DyrectoSharedChannelState *)commandChannel eventChannel:(DyrectoSharedChannelState *)eventChannel sessionOpen:(BOOL)sessionOpen connectionNumber:(DyrectoSharedInt * _Nullable)connectionNumber responderName:(NSString * _Nullable)responderName initCommandAck:(DyrectoSharedBoolean * _Nullable)initCommandAck initEventAck:(DyrectoSharedBoolean * _Nullable)initEventAck openSessionResponse:(DyrectoSharedInt * _Nullable)openSessionResponse getDeviceInfoResponse:(DyrectoSharedInt * _Nullable)getDeviceInfoResponse error:(NSString * _Nullable)error __attribute__((swift_name("init(tunnelUp:commandChannel:eventChannel:sessionOpen:connectionNumber:responderName:initCommandAck:initEventAck:openSessionResponse:getDeviceInfoResponse:error:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedPtpStatusCompanion *companion __attribute__((swift_name("companion")));
- (DyrectoSharedPtpStatus *)doCopyTunnelUp:(BOOL)tunnelUp commandChannel:(DyrectoSharedChannelState *)commandChannel eventChannel:(DyrectoSharedChannelState *)eventChannel sessionOpen:(BOOL)sessionOpen connectionNumber:(DyrectoSharedInt * _Nullable)connectionNumber responderName:(NSString * _Nullable)responderName initCommandAck:(DyrectoSharedBoolean * _Nullable)initCommandAck initEventAck:(DyrectoSharedBoolean * _Nullable)initEventAck openSessionResponse:(DyrectoSharedInt * _Nullable)openSessionResponse getDeviceInfoResponse:(DyrectoSharedInt * _Nullable)getDeviceInfoResponse error:(NSString * _Nullable)error __attribute__((swift_name("doCopy(tunnelUp:commandChannel:eventChannel:sessionOpen:connectionNumber:responderName:initCommandAck:initEventAck:openSessionResponse:getDeviceInfoResponse:error:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedChannelState *commandChannel __attribute__((swift_name("commandChannel")));
@property (readonly) DyrectoSharedInt * _Nullable connectionNumber __attribute__((swift_name("connectionNumber")));
@property (readonly) NSString * _Nullable error __attribute__((swift_name("error")));
@property (readonly) DyrectoSharedChannelState *eventChannel __attribute__((swift_name("eventChannel")));
@property (readonly) DyrectoSharedInt * _Nullable getDeviceInfoResponse __attribute__((swift_name("getDeviceInfoResponse")));
@property (readonly, getter=doInitCommandAck) DyrectoSharedBoolean * _Nullable initCommandAck __attribute__((swift_name("initCommandAck")));
@property (readonly, getter=doInitEventAck) DyrectoSharedBoolean * _Nullable initEventAck __attribute__((swift_name("initEventAck")));
@property (readonly) DyrectoSharedInt * _Nullable openSessionResponse __attribute__((swift_name("openSessionResponse")));
@property (readonly) NSString * _Nullable responderName __attribute__((swift_name("responderName")));
@property (readonly) BOOL sessionOpen __attribute__((swift_name("sessionOpen")));
@property (readonly) BOOL tunnelUp __attribute__((swift_name("tunnelUp")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PtpStatus.Companion")))
@interface DyrectoSharedPtpStatusCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedPtpStatusCompanion *shared __attribute__((swift_name("shared")));
- (NSString *)rcTextRc:(DyrectoSharedInt * _Nullable)rc __attribute__((swift_name("rcText(rc:)")));
@property (readonly) int32_t RC_OK __attribute__((swift_name("RC_OK")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshEnabled")))
@interface DyrectoSharedSshEnabled : DyrectoSharedKotlinEnum<DyrectoSharedSshEnabled *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSshEnabled *unknown __attribute__((swift_name("unknown")));
@property (class, readonly) DyrectoSharedSshEnabled *off __attribute__((swift_name("off")));
@property (class, readonly) DyrectoSharedSshEnabled *on __attribute__((swift_name("on")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSshEnabled *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSshEnabled *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SshStatus")))
@interface DyrectoSharedSshStatus : DyrectoSharedBase
- (instancetype)initWithSshState:(DyrectoSharedSshEnabled *)sshState sshId:(NSString * _Nullable)sshId sshPassword:(NSString * _Nullable)sshPassword fingerprint:(NSString * _Nullable)fingerprint cameraIp:(NSString * _Nullable)cameraIp notes:(NSArray<NSString *> *)notes connected:(BOOL)connected authenticated:(BOOL)authenticated keyboardInteractiveSupported:(BOOL)keyboardInteractiveSupported connectMillis:(DyrectoSharedLong * _Nullable)connectMillis authMillis:(DyrectoSharedLong * _Nullable)authMillis lastConnectedAt:(DyrectoSharedLong * _Nullable)lastConnectedAt error:(NSString * _Nullable)error __attribute__((swift_name("init(sshState:sshId:sshPassword:fingerprint:cameraIp:notes:connected:authenticated:keyboardInteractiveSupported:connectMillis:authMillis:lastConnectedAt:error:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedSshStatus *)doCopySshState:(DyrectoSharedSshEnabled *)sshState sshId:(NSString * _Nullable)sshId sshPassword:(NSString * _Nullable)sshPassword fingerprint:(NSString * _Nullable)fingerprint cameraIp:(NSString * _Nullable)cameraIp notes:(NSArray<NSString *> *)notes connected:(BOOL)connected authenticated:(BOOL)authenticated keyboardInteractiveSupported:(BOOL)keyboardInteractiveSupported connectMillis:(DyrectoSharedLong * _Nullable)connectMillis authMillis:(DyrectoSharedLong * _Nullable)authMillis lastConnectedAt:(DyrectoSharedLong * _Nullable)lastConnectedAt error:(NSString * _Nullable)error __attribute__((swift_name("doCopy(sshState:sshId:sshPassword:fingerprint:cameraIp:notes:connected:authenticated:keyboardInteractiveSupported:connectMillis:authMillis:lastConnectedAt:error:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedLong * _Nullable authMillis __attribute__((swift_name("authMillis")));
@property (readonly) BOOL authenticated __attribute__((swift_name("authenticated")));
@property (readonly) NSString * _Nullable cameraIp __attribute__((swift_name("cameraIp")));
@property (readonly) DyrectoSharedLong * _Nullable connectMillis __attribute__((swift_name("connectMillis")));
@property (readonly) BOOL connected __attribute__((swift_name("connected")));
@property (readonly) NSString * _Nullable error __attribute__((swift_name("error")));
@property (readonly) NSString * _Nullable fingerprint __attribute__((swift_name("fingerprint")));
@property (readonly) BOOL hasCredentials __attribute__((swift_name("hasCredentials")));
@property (readonly) BOOL keyboardInteractiveSupported __attribute__((swift_name("keyboardInteractiveSupported")));
@property (readonly) DyrectoSharedLong * _Nullable lastConnectedAt __attribute__((swift_name("lastConnectedAt")));
@property (readonly) NSArray<NSString *> *notes __attribute__((swift_name("notes")));
@property (readonly) NSString * _Nullable sshId __attribute__((swift_name("sshId")));
@property (readonly) NSString * _Nullable sshPassword __attribute__((swift_name("sshPassword")));
@property (readonly) DyrectoSharedSshEnabled *sshState __attribute__((swift_name("sshState")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimelineEvent")))
@interface DyrectoSharedTimelineEvent : DyrectoSharedBase
- (instancetype)initWithStage:(DyrectoSharedTimelineStage *)stage timestamp:(int64_t)timestamp durationFromPreviousMs:(DyrectoSharedLong * _Nullable)durationFromPreviousMs ok:(BOOL)ok detail:(NSString * _Nullable)detail __attribute__((swift_name("init(stage:timestamp:durationFromPreviousMs:ok:detail:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedTimelineEvent *)doCopyStage:(DyrectoSharedTimelineStage *)stage timestamp:(int64_t)timestamp durationFromPreviousMs:(DyrectoSharedLong * _Nullable)durationFromPreviousMs ok:(BOOL)ok detail:(NSString * _Nullable)detail __attribute__((swift_name("doCopy(stage:timestamp:durationFromPreviousMs:ok:detail:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable detail __attribute__((swift_name("detail")));
@property (readonly) DyrectoSharedLong * _Nullable durationFromPreviousMs __attribute__((swift_name("durationFromPreviousMs")));
@property (readonly) BOOL ok __attribute__((swift_name("ok")));
@property (readonly) DyrectoSharedTimelineStage *stage __attribute__((swift_name("stage")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimelineStage")))
@interface DyrectoSharedTimelineStage : DyrectoSharedKotlinEnum<DyrectoSharedTimelineStage *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) DyrectoSharedTimelineStageCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) DyrectoSharedTimelineStage *bleFound __attribute__((swift_name("bleFound")));
@property (class, readonly) DyrectoSharedTimelineStage *connected __attribute__((swift_name("connected")));
@property (class, readonly) DyrectoSharedTimelineStage *apCreated __attribute__((swift_name("apCreated")));
@property (class, readonly) DyrectoSharedTimelineStage *wifiJoined __attribute__((swift_name("wifiJoined")));
@property (class, readonly) DyrectoSharedTimelineStage *ipDiscovered __attribute__((swift_name("ipDiscovered")));
@property (class, readonly) DyrectoSharedTimelineStage *cc17Read __attribute__((swift_name("cc17Read")));
@property (class, readonly) DyrectoSharedTimelineStage *sshAuthenticated __attribute__((swift_name("sshAuthenticated")));
@property (class, readonly) DyrectoSharedTimelineStage *ptpInit __attribute__((swift_name("ptpInit")));
@property (class, readonly) DyrectoSharedTimelineStage *openSession __attribute__((swift_name("openSession")));
@property (class, readonly) DyrectoSharedTimelineStage *getDeviceInfo __attribute__((swift_name("getDeviceInfo")));
+ (DyrectoSharedKotlinArray<DyrectoSharedTimelineStage *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedTimelineStage *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) int32_t order __attribute__((swift_name("order")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimelineStage.Companion")))
@interface DyrectoSharedTimelineStageCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedTimelineStageCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) NSArray<DyrectoSharedTimelineStage *> *ordered __attribute__((swift_name("ordered")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinCoroutineContext")))
@protocol DyrectoSharedKotlinCoroutineContext
@required
- (id _Nullable)foldInitial:(id _Nullable)initial operation:(id _Nullable (^)(id _Nullable, id<DyrectoSharedKotlinCoroutineContextElement>))operation __attribute__((swift_name("fold(initial:operation:)")));
- (id<DyrectoSharedKotlinCoroutineContextElement> _Nullable)getKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("get(key:)")));
- (id<DyrectoSharedKotlinCoroutineContext>)minusKeyKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("minusKey(key:)")));
- (id<DyrectoSharedKotlinCoroutineContext>)plusContext:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("plus(context:)")));
@end

__attribute__((swift_name("KotlinCoroutineContextElement")))
@protocol DyrectoSharedKotlinCoroutineContextElement <DyrectoSharedKotlinCoroutineContext>
@required
@property (readonly) id<DyrectoSharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end

__attribute__((swift_name("Job")))
@protocol DyrectoSharedJob <DyrectoSharedKotlinCoroutineContextElement>
@required
- (id<DyrectoSharedChildHandle>)attachChildChild:(id<DyrectoSharedChildJob>)child __attribute__((swift_name("attachChild(child:)")));
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (DyrectoSharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)")));
- (id<DyrectoSharedJob>)plusOther:(id<DyrectoSharedJob>)other __attribute__((swift_name("plus(other:)"))) __attribute__((unavailable("Operator '+' on two Job objects is meaningless. Job is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The job to the right of `+` just replaces the job the left of `+`.")));
- (BOOL)start __attribute__((swift_name("start()")));
@property (readonly) id<DyrectoSharedKotlinSequence> children __attribute__((swift_name("children")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@property (readonly) id<DyrectoSharedSelectClause0> onJoin __attribute__((swift_name("onJoin")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
@property (readonly) id<DyrectoSharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end

__attribute__((swift_name("ChildJob")))
@protocol DyrectoSharedChildJob <DyrectoSharedJob>
@required
- (void)parentCancelledParentJob:(id<DyrectoSharedParentJob>)parentJob __attribute__((swift_name("parentCancelled(parentJob:)")));
@end

__attribute__((swift_name("ParentJob")))
@protocol DyrectoSharedParentJob <DyrectoSharedJob>
@required
- (DyrectoSharedKotlinCancellationException *)getChildJobCancellationCause __attribute__((swift_name("getChildJobCancellationCause()")));
@end

__attribute__((swift_name("JobSupport")))
@interface DyrectoSharedJobSupport : DyrectoSharedBase <DyrectoSharedJob, DyrectoSharedChildJob, DyrectoSharedParentJob>
- (instancetype)initWithActive:(BOOL)active __attribute__((swift_name("init(active:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable("This is internal API and may be removed in the future releases")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)afterCompletionState:(id _Nullable)state __attribute__((swift_name("afterCompletion(state:)")));
- (id<DyrectoSharedChildHandle>)attachChildChild:(id<DyrectoSharedChildJob>)child __attribute__((swift_name("attachChild(child:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)awaitInternalWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitInternal(completionHandler:)")));
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (BOOL)cancelCoroutineCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancelCoroutine(cause:)")));
- (void)cancelInternalCause:(DyrectoSharedKotlinThrowable *)cause __attribute__((swift_name("cancelInternal(cause:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString *)cancellationExceptionMessage __attribute__((swift_name("cancellationExceptionMessage()")));
- (BOOL)childCancelledCause:(DyrectoSharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
- (DyrectoSharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()")));
- (DyrectoSharedKotlinCancellationException *)getChildJobCancellationCause __attribute__((swift_name("getChildJobCancellationCause()")));
- (DyrectoSharedKotlinThrowable * _Nullable)getCompletionExceptionOrNull __attribute__((swift_name("getCompletionExceptionOrNull()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (BOOL)handleJobExceptionException:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("handleJobException(exception:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)doInitParentJobParent:(id<DyrectoSharedJob> _Nullable)parent __attribute__((swift_name("doInitParentJob(parent:)")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCancellingCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("onCancelling(cause:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletionInternalState:(id _Nullable)state __attribute__((swift_name("onCompletionInternal(state:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onStart __attribute__((swift_name("onStart()")));
- (void)parentCancelledParentJob:(id<DyrectoSharedParentJob>)parentJob __attribute__((swift_name("parentCancelled(parentJob:)")));
- (BOOL)start __attribute__((swift_name("start()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (DyrectoSharedKotlinCancellationException *)toCancellationException:(DyrectoSharedKotlinThrowable *)receiver message:(NSString * _Nullable)message __attribute__((swift_name("toCancellationException(_:message:)")));
- (NSString *)toDebugString __attribute__((swift_name("toDebugString()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<DyrectoSharedKotlinSequence> children __attribute__((swift_name("children")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) DyrectoSharedKotlinThrowable * _Nullable completionCause __attribute__((swift_name("completionCause")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) BOOL completionCauseHandled __attribute__((swift_name("completionCauseHandled")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@property (readonly) BOOL isCompletedExceptionally __attribute__((swift_name("isCompletedExceptionally")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) BOOL isScopedCoroutine __attribute__((swift_name("isScopedCoroutine")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) id<DyrectoSharedSelectClause1> onAwaitInternal __attribute__((swift_name("onAwaitInternal")));
@property (readonly) id<DyrectoSharedSelectClause0> onJoin __attribute__((swift_name("onJoin")));
@property (readonly) id<DyrectoSharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuation")))
@protocol DyrectoSharedKotlinContinuation
@required
- (void)resumeWithResult:(id _Nullable)result __attribute__((swift_name("resumeWith(result:)")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end

__attribute__((swift_name("CoroutineScope")))
@protocol DyrectoSharedCoroutineScope
@required
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end

__attribute__((swift_name("AbstractCoroutine")))
@interface DyrectoSharedAbstractCoroutine<__contravariant T> : DyrectoSharedJobSupport <DyrectoSharedJob, DyrectoSharedKotlinContinuation, DyrectoSharedCoroutineScope>
- (instancetype)initWithParentContext:(id<DyrectoSharedKotlinCoroutineContext>)parentContext initParentJob:(BOOL)initParentJob active:(BOOL)active __attribute__((swift_name("init(parentContext:initParentJob:active:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithActive:(BOOL)active __attribute__((swift_name("init(active:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)afterResumeState:(id _Nullable)state __attribute__((swift_name("afterResume(state:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString *)cancellationExceptionMessage __attribute__((swift_name("cancellationExceptionMessage()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCancelledCause:(DyrectoSharedKotlinThrowable *)cause handled:(BOOL)handled __attribute__((swift_name("onCancelled(cause:handled:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletedValue:(T _Nullable)value __attribute__((swift_name("onCompleted(value:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)onCompletionInternalState:(id _Nullable)state __attribute__((swift_name("onCompletionInternal(state:)")));
- (void)resumeWithResult:(id _Nullable)result __attribute__((swift_name("resumeWith(result:)")));
- (void)startStart:(DyrectoSharedCoroutineStart *)start receiver:(id _Nullable)receiver block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("start(start:receiver:block:)")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@end

__attribute__((swift_name("CancellableContinuation")))
@protocol DyrectoSharedCancellableContinuation <DyrectoSharedKotlinContinuation>
@required
- (BOOL)cancelCause_:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(cause_:)")));
- (void)completeResumeToken:(id)token __attribute__((swift_name("completeResume(token:)")));
- (void)doInitCancellability __attribute__((swift_name("doInitCancellability()")));
- (void)invokeOnCancellationHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCancellation(handler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resumeValue:(id _Nullable)value onCancellation:(void (^ _Nullable)(DyrectoSharedKotlinThrowable *))onCancellation __attribute__((swift_name("resume(value:onCancellation:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resumeUndispatched:(DyrectoSharedCoroutineDispatcher *)receiver value:(id _Nullable)value __attribute__((swift_name("resumeUndispatched(_:value:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resumeUndispatchedWithException:(DyrectoSharedCoroutineDispatcher *)receiver exception:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("resumeUndispatchedWithException(_:exception:)")));
- (id _Nullable)tryResumeValue:(id _Nullable)value idempotent:(id _Nullable)idempotent __attribute__((swift_name("tryResume(value:idempotent:)")));
- (id _Nullable)tryResumeValue:(id _Nullable)value idempotent:(id _Nullable)idempotent onCancellation:(void (^ _Nullable)(DyrectoSharedKotlinThrowable *))onCancellation __attribute__((swift_name("tryResume(value:idempotent:onCancellation:)")));
- (id _Nullable)tryResumeWithExceptionException:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("tryResumeWithException(exception:)")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@end

__attribute__((swift_name("DisposableHandle")))
@protocol DyrectoSharedDisposableHandle
@required
- (void)dispose __attribute__((swift_name("dispose()")));
@end

__attribute__((swift_name("ChildHandle")))
@protocol DyrectoSharedChildHandle <DyrectoSharedDisposableHandle>
@required
- (BOOL)childCancelledCause:(DyrectoSharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
@property (readonly) id<DyrectoSharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextElement")))
@interface DyrectoSharedKotlinAbstractCoroutineContextElement : DyrectoSharedBase <DyrectoSharedKotlinCoroutineContextElement>
- (instancetype)initWithKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer));
@property (readonly) id<DyrectoSharedKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuationInterceptor")))
@protocol DyrectoSharedKotlinContinuationInterceptor <DyrectoSharedKotlinCoroutineContextElement>
@required
- (id<DyrectoSharedKotlinContinuation>)interceptContinuationContinuation:(id<DyrectoSharedKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (void)releaseInterceptedContinuationContinuation:(id<DyrectoSharedKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
@end

__attribute__((swift_name("CoroutineDispatcher")))
@interface DyrectoSharedCoroutineDispatcher : DyrectoSharedKotlinAbstractCoroutineContextElement <DyrectoSharedKotlinContinuationInterceptor>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) DyrectoSharedCoroutineDispatcherKey *companion __attribute__((swift_name("companion")));
- (void)dispatchContext:(id<DyrectoSharedKotlinCoroutineContext>)context block:(id<DyrectoSharedRunnable>)block __attribute__((swift_name("dispatch(context:block:)")));
- (void)dispatchYieldContext:(id<DyrectoSharedKotlinCoroutineContext>)context block:(id<DyrectoSharedRunnable>)block __attribute__((swift_name("dispatchYield(context:block:)")));
- (id<DyrectoSharedKotlinContinuation>)interceptContinuationContinuation:(id<DyrectoSharedKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (BOOL)isDispatchNeededContext:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("isDispatchNeeded(context:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (DyrectoSharedCoroutineDispatcher *)limitedParallelismParallelism:(int32_t)parallelism __attribute__((swift_name("limitedParallelism(parallelism:)")));
- (DyrectoSharedCoroutineDispatcher *)plusOther_:(DyrectoSharedCoroutineDispatcher *)other __attribute__((swift_name("plus(other_:)"))) __attribute__((unavailable("Operator '+' on two CoroutineDispatcher objects is meaningless. CoroutineDispatcher is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The dispatcher to the right of `+` just replaces the dispatcher to the left.")));
- (void)releaseInterceptedContinuationContinuation:(id<DyrectoSharedKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((swift_name("CloseableCoroutineDispatcher")))
@interface DyrectoSharedCloseableCoroutineDispatcher : DyrectoSharedCoroutineDispatcher
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)close __attribute__((swift_name("close()")));
@end

__attribute__((swift_name("Deferred")))
@protocol DyrectoSharedDeferred <DyrectoSharedJob>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)awaitWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("await(completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (id _Nullable)getCompleted __attribute__((swift_name("getCompleted()")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (DyrectoSharedKotlinThrowable * _Nullable)getCompletionExceptionOrNull __attribute__((swift_name("getCompletionExceptionOrNull()")));
@property (readonly) id<DyrectoSharedSelectClause1> onAwait __attribute__((swift_name("onAwait")));
@end

__attribute__((swift_name("CompletableDeferred")))
@protocol DyrectoSharedCompletableDeferred <DyrectoSharedDeferred>
@required
- (BOOL)completeValue:(id _Nullable)value __attribute__((swift_name("complete(value:)")));
- (BOOL)completeExceptionallyException:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("completeExceptionally(exception:)")));
@end

__attribute__((swift_name("CompletableJob")))
@protocol DyrectoSharedCompletableJob <DyrectoSharedJob>
@required
- (BOOL)complete __attribute__((swift_name("complete()")));
- (BOOL)completeExceptionallyException:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("completeExceptionally(exception:)")));
@end

__attribute__((swift_name("KotlinThrowable")))
@interface DyrectoSharedKotlinThrowable : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));

/**
 * @note annotations
 *   kotlin.experimental.ExperimentalNativeApi
*/
- (DyrectoSharedKotlinArray<NSString *> *)getStackTrace __attribute__((swift_name("getStackTrace()")));
- (void)printStackTrace __attribute__((swift_name("printStackTrace()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedKotlinThrowable * _Nullable cause __attribute__((swift_name("cause")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
- (NSError *)asError __attribute__((swift_name("asError()")));
@end

__attribute__((swift_name("KotlinException")))
@interface DyrectoSharedKotlinException : DyrectoSharedKotlinThrowable
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinRuntimeException")))
@interface DyrectoSharedKotlinRuntimeException : DyrectoSharedKotlinException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompletionHandlerException")))
@interface DyrectoSharedCompletionHandlerException : DyrectoSharedKotlinRuntimeException
- (instancetype)initWithMessage:(NSString *)message cause:(DyrectoSharedKotlinThrowable *)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
__attribute__((swift_name("CopyableThrowable")))
@protocol DyrectoSharedCopyableThrowable
@required
- (DyrectoSharedKotlinThrowable * _Nullable)createCopy __attribute__((swift_name("createCopy()")));
@end

__attribute__((swift_name("KotlinCoroutineContextKey")))
@protocol DyrectoSharedKotlinCoroutineContextKey
@required
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextKey")))
@interface DyrectoSharedKotlinAbstractCoroutineContextKey<B, E> : DyrectoSharedBase <DyrectoSharedKotlinCoroutineContextKey>
- (instancetype)initWithBaseKey:(id<DyrectoSharedKotlinCoroutineContextKey>)baseKey safeCast:(E _Nullable (^)(id<DyrectoSharedKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineDispatcher.Key")))
@interface DyrectoSharedCoroutineDispatcherKey : DyrectoSharedKotlinAbstractCoroutineContextKey<id<DyrectoSharedKotlinContinuationInterceptor>, DyrectoSharedCoroutineDispatcher *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithBaseKey:(id<DyrectoSharedKotlinCoroutineContextKey>)baseKey safeCast:(id<DyrectoSharedKotlinCoroutineContextElement> _Nullable (^)(id<DyrectoSharedKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCoroutineDispatcherKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("CoroutineExceptionHandler")))
@protocol DyrectoSharedCoroutineExceptionHandler <DyrectoSharedKotlinCoroutineContextElement>
@required
- (void)handleExceptionContext:(id<DyrectoSharedKotlinCoroutineContext>)context exception:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("handleException(context:exception:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineExceptionHandlerKey")))
@interface DyrectoSharedCoroutineExceptionHandlerKey : DyrectoSharedBase <DyrectoSharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCoroutineExceptionHandlerKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineName")))
@interface DyrectoSharedCoroutineName : DyrectoSharedKotlinAbstractCoroutineContextElement
- (instancetype)initWithName:(NSString *)name __attribute__((swift_name("init(name:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) DyrectoSharedCoroutineNameKey *companion __attribute__((swift_name("companion")));
- (DyrectoSharedCoroutineName *)doCopyName:(NSString *)name __attribute__((swift_name("doCopy(name:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineName.Key")))
@interface DyrectoSharedCoroutineNameKey : DyrectoSharedBase <DyrectoSharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedCoroutineNameKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineStart")))
@interface DyrectoSharedCoroutineStart : DyrectoSharedKotlinEnum<DyrectoSharedCoroutineStart *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedCoroutineStart *default_ __attribute__((swift_name("default_")));
@property (class, readonly) DyrectoSharedCoroutineStart *lazy __attribute__((swift_name("lazy")));
@property (class, readonly) DyrectoSharedCoroutineStart *atomic __attribute__((swift_name("atomic")));
@property (class, readonly) DyrectoSharedCoroutineStart *undispatched __attribute__((swift_name("undispatched")));
+ (DyrectoSharedKotlinArray<DyrectoSharedCoroutineStart *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedCoroutineStart *> *entries __attribute__((swift_name("entries")));
- (void)invokeBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block receiver:(id _Nullable)receiver completion:(id<DyrectoSharedKotlinContinuation>)completion __attribute__((swift_name("invoke(block:receiver:completion:)")));
@property (readonly) BOOL isLazy __attribute__((swift_name("isLazy")));
@end

__attribute__((swift_name("Delay")))
@protocol DyrectoSharedDelay
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)delayTime:(int64_t)time completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(time:completionHandler:)"))) __attribute__((unavailable("Deprecated without replacement as an internal method never intended for public use")));
- (id<DyrectoSharedDisposableHandle>)invokeOnTimeoutTimeMillis:(int64_t)timeMillis block:(id<DyrectoSharedRunnable>)block context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("invokeOnTimeout(timeMillis:block:context:)")));
- (void)scheduleResumeAfterDelayTimeMillis:(int64_t)timeMillis continuation:(id<DyrectoSharedCancellableContinuation>)continuation __attribute__((swift_name("scheduleResumeAfterDelay(timeMillis:continuation:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Dispatchers")))
@interface DyrectoSharedDispatchers : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)dispatchers __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedDispatchers *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedCoroutineDispatcher *Default __attribute__((swift_name("Default")));
@property (readonly) DyrectoSharedMainCoroutineDispatcher *Main __attribute__((swift_name("Main")));
@property (readonly) DyrectoSharedCoroutineDispatcher *Unconfined __attribute__((swift_name("Unconfined")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("GlobalScope")))
@interface DyrectoSharedGlobalScope : DyrectoSharedBase <DyrectoSharedCoroutineScope>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)globalScope __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedGlobalScope *shared __attribute__((swift_name("shared")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("JobKey")))
@interface DyrectoSharedJobKey : DyrectoSharedBase <DyrectoSharedKotlinCoroutineContextKey>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedJobKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("MainCoroutineDispatcher")))
@interface DyrectoSharedMainCoroutineDispatcher : DyrectoSharedCoroutineDispatcher
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedCoroutineDispatcher *)limitedParallelismParallelism:(int32_t)parallelism __attribute__((swift_name("limitedParallelism(parallelism:)")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString * _Nullable)toStringInternalImpl __attribute__((swift_name("toStringInternalImpl()")));
@property (readonly) DyrectoSharedMainCoroutineDispatcher *immediate __attribute__((swift_name("immediate")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NonCancellable")))
@interface DyrectoSharedNonCancellable : DyrectoSharedKotlinAbstractCoroutineContextElement <DyrectoSharedJob>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithKey:(id<DyrectoSharedKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)nonCancellable __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedNonCancellable *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedChildHandle>)attachChildChild:(id<DyrectoSharedChildJob>)child __attribute__((swift_name("attachChild(child:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (DyrectoSharedKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (id<DyrectoSharedDisposableHandle>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (BOOL)start __attribute__((swift_name("start()"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<DyrectoSharedKotlinSequence> children __attribute__((swift_name("children"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) id<DyrectoSharedSelectClause0> onJoin __attribute__((swift_name("onJoin"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@property (readonly) id<DyrectoSharedJob> _Nullable parent __attribute__((swift_name("parent"))) __attribute__((deprecated("NonCancellable can be used only as an argument for 'withContext', direct usages of its API are prohibited")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NonDisposableHandle")))
@interface DyrectoSharedNonDisposableHandle : DyrectoSharedBase <DyrectoSharedDisposableHandle, DyrectoSharedChildHandle>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)nonDisposableHandle __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedNonDisposableHandle *shared __attribute__((swift_name("shared")));
- (BOOL)childCancelledCause:(DyrectoSharedKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
- (void)dispose __attribute__((swift_name("dispose()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<DyrectoSharedJob> _Nullable parent __attribute__((swift_name("parent")));
@end

__attribute__((swift_name("Runnable")))
@protocol DyrectoSharedRunnable
@required
- (void)run __attribute__((swift_name("run()")));
@end

__attribute__((swift_name("KotlinIllegalStateException")))
@interface DyrectoSharedKotlinIllegalStateException : DyrectoSharedKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.4")
*/
__attribute__((swift_name("KotlinCancellationException")))
@interface DyrectoSharedKotlinCancellationException : DyrectoSharedKotlinIllegalStateException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeoutCancellationException")))
@interface DyrectoSharedTimeoutCancellationException : DyrectoSharedKotlinCancellationException <DyrectoSharedCopyableThrowable>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (DyrectoSharedTimeoutCancellationException *)createCopy __attribute__((swift_name("createCopy()")));
@end

__attribute__((swift_name("SendChannel")))
@protocol DyrectoSharedSendChannel
@required
- (BOOL)closeCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("close(cause:)")));
- (void)invokeOnCloseHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnClose(handler:)")));
- (BOOL)offerElement:(id _Nullable)element __attribute__((swift_name("offer(element:)"))) __attribute__((unavailable("Deprecated in the favour of 'trySend' method")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)sendElement:(id _Nullable)element completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("send(element:completionHandler:)")));
- (id _Nullable)trySendElement:(id _Nullable)element __attribute__((swift_name("trySend(element:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForSend __attribute__((swift_name("isClosedForSend")));
@property (readonly) id<DyrectoSharedSelectClause2> onSend __attribute__((swift_name("onSend")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
__attribute__((swift_name("BroadcastChannel")))
@protocol DyrectoSharedBroadcastChannel <DyrectoSharedSendChannel>
@required
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (id<DyrectoSharedReceiveChannel>)openSubscription __attribute__((swift_name("openSubscription()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BufferOverflow")))
@interface DyrectoSharedBufferOverflow : DyrectoSharedKotlinEnum<DyrectoSharedBufferOverflow *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedBufferOverflow *suspend __attribute__((swift_name("suspend")));
@property (class, readonly) DyrectoSharedBufferOverflow *dropOldest __attribute__((swift_name("dropOldest")));
@property (class, readonly) DyrectoSharedBufferOverflow *dropLatest __attribute__((swift_name("dropLatest")));
+ (DyrectoSharedKotlinArray<DyrectoSharedBufferOverflow *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedBufferOverflow *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("ReceiveChannel")))
@protocol DyrectoSharedReceiveChannel
@required
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (id<DyrectoSharedChannelIterator>)iterator __attribute__((swift_name("iterator()")));
- (id _Nullable)poll __attribute__((swift_name("poll()"))) __attribute__((unavailable("Deprecated in the favour of 'tryReceive'. Please note that the provided replacement does not rethrow channel's close cause as 'poll' did, for the precise replacement please refer to the 'poll' documentation")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receive(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveCatchingWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receiveCatching(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)receiveOrNullWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("receiveOrNull(completionHandler:)"))) __attribute__((unavailable("Deprecated in favor of 'receiveCatching'. Please note that the provided replacement does not rethrow channel's close cause as 'receiveOrNull' did, for the detailed replacement please refer to the 'receiveOrNull' documentation")));
- (id _Nullable)tryReceive __attribute__((swift_name("tryReceive()")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForReceive __attribute__((swift_name("isClosedForReceive")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) id<DyrectoSharedSelectClause1> onReceive __attribute__((swift_name("onReceive")));
@property (readonly) id<DyrectoSharedSelectClause1> onReceiveCatching __attribute__((swift_name("onReceiveCatching")));
@property (readonly) id<DyrectoSharedSelectClause1> onReceiveOrNull __attribute__((swift_name("onReceiveOrNull"))) __attribute__((unavailable("Deprecated in favor of onReceiveCatching extension")));
@end

__attribute__((swift_name("Channel")))
@protocol DyrectoSharedChannel <DyrectoSharedSendChannel, DyrectoSharedReceiveChannel>
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelFactory")))
@interface DyrectoSharedChannelFactory : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)factory __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedChannelFactory *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t BUFFERED __attribute__((swift_name("BUFFERED")));
@property (readonly) int32_t CONFLATED __attribute__((swift_name("CONFLATED")));
@property (readonly) NSString *DEFAULT_BUFFER_PROPERTY_NAME __attribute__((swift_name("DEFAULT_BUFFER_PROPERTY_NAME")));
@property (readonly) int32_t RENDEZVOUS __attribute__((swift_name("RENDEZVOUS")));
@property (readonly) int32_t UNLIMITED __attribute__((swift_name("UNLIMITED")));
@end

__attribute__((swift_name("ChannelIterator")))
@protocol DyrectoSharedChannelIterator
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)hasNextWithCompletionHandler:(void (^)(DyrectoSharedBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("hasNext(completionHandler:)")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

__attribute__((swift_name("KotlinNoSuchElementException")))
@interface DyrectoSharedKotlinNoSuchElementException : DyrectoSharedKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClosedReceiveChannelException")))
@interface DyrectoSharedClosedReceiveChannelException : DyrectoSharedKotlinNoSuchElementException
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClosedSendChannelException")))
@interface DyrectoSharedClosedSendChannelException : DyrectoSharedKotlinIllegalStateException
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
- (instancetype)initWithCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ConflatedBroadcastChannel")))
@interface DyrectoSharedConflatedBroadcastChannel<E> : DyrectoSharedBase <DyrectoSharedBroadcastChannel>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer)) __attribute__((deprecated("ConflatedBroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithValue:(E _Nullable)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer)) __attribute__((deprecated("ConflatedBroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
- (void)cancelCause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (BOOL)closeCause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("close(cause:)")));
- (void)invokeOnCloseHandler:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnClose(handler:)")));
- (BOOL)offerElement:(E _Nullable)element __attribute__((swift_name("offer(element:)"))) __attribute__((unavailable("Deprecated in the favour of 'trySend' method")));
- (id<DyrectoSharedReceiveChannel>)openSubscription __attribute__((swift_name("openSubscription()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)sendElement:(E _Nullable)element completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("send(element:completionHandler:)")));
- (id _Nullable)trySendElement:(E _Nullable)element __attribute__((swift_name("trySend(element:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
@property (readonly) BOOL isClosedForSend __attribute__((swift_name("isClosedForSend")));
@property (readonly) id<DyrectoSharedSelectClause2> onSend __attribute__((swift_name("onSend")));
@property (readonly) E _Nullable value __attribute__((swift_name("value")));
@property (readonly) E _Nullable valueOrNull __attribute__((swift_name("valueOrNull")));
@end

__attribute__((swift_name("ProducerScope")))
@protocol DyrectoSharedProducerScope <DyrectoSharedCoroutineScope, DyrectoSharedSendChannel>
@required
@property (readonly) id<DyrectoSharedSendChannel> channel __attribute__((swift_name("channel")));
@end

__attribute__((swift_name("Flow")))
@protocol DyrectoSharedFlow
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<DyrectoSharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));
@end


/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
__attribute__((swift_name("AbstractFlow")))
@interface DyrectoSharedAbstractFlow<T> : DyrectoSharedBase <DyrectoSharedFlow>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<DyrectoSharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectSafelyCollector:(id<DyrectoSharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectSafely(collector:completionHandler:)")));
@end

__attribute__((swift_name("FlowCollector")))
@protocol DyrectoSharedFlowCollector
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitValue:(id _Nullable)value completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(value:completionHandler:)")));
@end

__attribute__((swift_name("SharedFlow")))
@protocol DyrectoSharedSharedFlow <DyrectoSharedFlow>
@required
@property (readonly) NSArray<id> *replayCache __attribute__((swift_name("replayCache")));
@end

__attribute__((swift_name("MutableSharedFlow")))
@protocol DyrectoSharedMutableSharedFlow <DyrectoSharedSharedFlow, DyrectoSharedFlowCollector>
@required

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)resetReplayCache __attribute__((swift_name("resetReplayCache()")));
- (BOOL)tryEmitValue:(id _Nullable)value __attribute__((swift_name("tryEmit(value:)")));
@property (readonly) id<DyrectoSharedStateFlow> subscriptionCount __attribute__((swift_name("subscriptionCount")));
@end

__attribute__((swift_name("StateFlow")))
@protocol DyrectoSharedStateFlow <DyrectoSharedSharedFlow>
@required
@property (readonly) id _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("MutableStateFlow")))
@protocol DyrectoSharedMutableStateFlow <DyrectoSharedStateFlow, DyrectoSharedMutableSharedFlow>
@required
- (void)setValue:(id _Nullable)value __attribute__((swift_name("setValue(_:)")));
- (BOOL)compareAndSetExpect:(id _Nullable)expect update:(id _Nullable)update __attribute__((swift_name("compareAndSet(expect:update:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharingCommand")))
@interface DyrectoSharedSharingCommand : DyrectoSharedKotlinEnum<DyrectoSharedSharingCommand *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedSharingCommand *start __attribute__((swift_name("start")));
@property (class, readonly) DyrectoSharedSharingCommand *stop __attribute__((swift_name("stop")));
@property (class, readonly) DyrectoSharedSharingCommand *stopAndResetReplayCache __attribute__((swift_name("stopAndResetReplayCache")));
+ (DyrectoSharedKotlinArray<DyrectoSharedSharingCommand *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedSharingCommand *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("SharingStarted")))
@protocol DyrectoSharedSharingStarted
@required
- (id<DyrectoSharedFlow>)commandSubscriptionCount:(id<DyrectoSharedStateFlow>)subscriptionCount __attribute__((swift_name("command(subscriptionCount:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharingStartedCompanion")))
@interface DyrectoSharedSharingStartedCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedSharingStartedCompanion *shared __attribute__((swift_name("shared")));
- (id<DyrectoSharedSharingStarted>)WhileSubscribedStopTimeoutMillis:(int64_t)stopTimeoutMillis replayExpirationMillis:(int64_t)replayExpirationMillis __attribute__((swift_name("WhileSubscribed(stopTimeoutMillis:replayExpirationMillis:)")));
@property (readonly) id<DyrectoSharedSharingStarted> Eagerly __attribute__((swift_name("Eagerly")));
@property (readonly) id<DyrectoSharedSharingStarted> Lazily __attribute__((swift_name("Lazily")));
@end

__attribute__((swift_name("FusibleFlow")))
@protocol DyrectoSharedFusibleFlow <DyrectoSharedFlow>
@required
- (id<DyrectoSharedFlow>)fuseContext:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("fuse(context:capacity:onBufferOverflow:)")));
@end

__attribute__((swift_name("ChannelFlow")))
@interface DyrectoSharedChannelFlow<T> : DyrectoSharedBase <DyrectoSharedFusibleFlow>
- (instancetype)initWithContext:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("init(context:capacity:onBufferOverflow:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (NSString * _Nullable)additionalToStringProps __attribute__((swift_name("additionalToStringProps()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<DyrectoSharedFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)collectToScope:(id<DyrectoSharedProducerScope>)scope completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectTo(scope:completionHandler:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (DyrectoSharedChannelFlow<T> *)createContext:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("create(context:capacity:onBufferOverflow:)")));
- (id<DyrectoSharedFlow> _Nullable)dropChannelOperators __attribute__((swift_name("dropChannelOperators()")));
- (id<DyrectoSharedFlow>)fuseContext:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("fuse(context:capacity:onBufferOverflow:)")));
- (id<DyrectoSharedReceiveChannel>)produceImplScope:(id<DyrectoSharedCoroutineScope>)scope __attribute__((swift_name("produceImpl(scope:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t capacity __attribute__((swift_name("capacity")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@property (readonly) DyrectoSharedBufferOverflow *onBufferOverflow __attribute__((swift_name("onBufferOverflow")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SendingCollector")))
@interface DyrectoSharedSendingCollector<T> : DyrectoSharedBase <DyrectoSharedFlowCollector>
- (instancetype)initWithChannel:(id<DyrectoSharedSendChannel>)channel __attribute__((swift_name("init(channel:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitValue:(T _Nullable)value completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(value:completionHandler:)")));
@end

__attribute__((swift_name("OpDescriptor")))
@interface DyrectoSharedOpDescriptor : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (id _Nullable)performAffected:(id _Nullable)affected __attribute__((swift_name("perform(affected:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) DyrectoSharedAtomicOp<id> * _Nullable atomicOp __attribute__((swift_name("atomicOp")));
@end

__attribute__((swift_name("AtomicOp")))
@interface DyrectoSharedAtomicOp<__contravariant T> : DyrectoSharedOpDescriptor
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)completeAffected:(T _Nullable)affected failure:(id _Nullable)failure __attribute__((swift_name("complete(affected:failure:)")));
- (id _Nullable)performAffected:(id _Nullable)affected __attribute__((swift_name("perform(affected:)")));
- (id _Nullable)prepareAffected:(T _Nullable)affected __attribute__((swift_name("prepare(affected:)")));
@property (readonly) DyrectoSharedAtomicOp<id> *atomicOp __attribute__((swift_name("atomicOp")));
@end

__attribute__((swift_name("LockFreeLinkedListNode")))
@interface DyrectoSharedLockFreeLinkedListNode : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)addLastNode:(DyrectoSharedLockFreeLinkedListNode *)node __attribute__((swift_name("addLast(node:)")));
- (BOOL)addLastIfNode:(DyrectoSharedLockFreeLinkedListNode *)node condition:(DyrectoSharedBoolean *(^)(void))condition __attribute__((swift_name("addLastIf(node:condition:)")));
- (BOOL)addOneIfEmptyNode:(DyrectoSharedLockFreeLinkedListNode *)node __attribute__((swift_name("addOneIfEmpty(node:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (DyrectoSharedLockFreeLinkedListNode * _Nullable)nextIfRemoved __attribute__((swift_name("nextIfRemoved()")));
- (BOOL)remove __attribute__((swift_name("remove()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL isRemoved __attribute__((swift_name("isRemoved")));
@property (readonly, getter=next_) id next __attribute__((swift_name("next")));
@property (readonly) DyrectoSharedLockFreeLinkedListNode *nextNode __attribute__((swift_name("nextNode")));
@property (readonly) DyrectoSharedLockFreeLinkedListNode *prevNode __attribute__((swift_name("prevNode")));
@end

__attribute__((swift_name("LockFreeLinkedListHead")))
@interface DyrectoSharedLockFreeLinkedListHead : DyrectoSharedLockFreeLinkedListNode
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)forEachBlock:(void (^)(DyrectoSharedLockFreeLinkedListNode *))block __attribute__((swift_name("forEach(block:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (DyrectoSharedLockFreeLinkedListNode * _Nullable)nextIfRemoved __attribute__((swift_name("nextIfRemoved()")));
- (BOOL)remove __attribute__((swift_name("remove()")));
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) BOOL isRemoved __attribute__((swift_name("isRemoved")));
@end

__attribute__((swift_name("MainDispatcherFactory")))
@protocol DyrectoSharedMainDispatcherFactory
@required
- (DyrectoSharedMainCoroutineDispatcher *)createDispatcherAllFactories:(NSArray<id<DyrectoSharedMainDispatcherFactory>> *)allFactories __attribute__((swift_name("createDispatcher(allFactories:)")));
- (NSString * _Nullable)hintOnError __attribute__((swift_name("hintOnError()")));
@property (readonly) int32_t loadPriority __attribute__((swift_name("loadPriority")));
@end

__attribute__((swift_name("AtomicfuSynchronizedObject")))
@interface DyrectoSharedAtomicfuSynchronizedObject : DyrectoSharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)lock __attribute__((swift_name("lock()")));
- (BOOL)tryLock __attribute__((swift_name("tryLock()")));
- (void)unlock __attribute__((swift_name("unlock()")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly, getter=lock_) DyrectoSharedKotlinAtomicReference<DyrectoSharedAtomicfuSynchronizedObjectLockState *> *lock __attribute__((swift_name("lock")));
@end

__attribute__((swift_name("ThreadSafeHeap")))
@interface DyrectoSharedThreadSafeHeap<T> : DyrectoSharedAtomicfuSynchronizedObject
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)addLastNode:(T)node __attribute__((swift_name("addLast(node:)")));
- (BOOL)addLastIfNode:(T)node cond:(DyrectoSharedBoolean *(^)(T _Nullable))cond __attribute__((swift_name("addLastIf(node:cond:)")));
- (T _Nullable)findPredicate:(DyrectoSharedBoolean *(^)(T))predicate __attribute__((swift_name("find(predicate:)")));
- (T _Nullable)peek __attribute__((swift_name("peek()")));
- (BOOL)removeNode:(T)node __attribute__((swift_name("remove(node:)")));
- (T _Nullable)removeFirstIfPredicate:(DyrectoSharedBoolean *(^)(T))predicate __attribute__((swift_name("removeFirstIf(predicate:)")));
- (T _Nullable)removeFirstOrNull __attribute__((swift_name("removeFirstOrNull()")));
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((swift_name("ThreadSafeHeapNode")))
@protocol DyrectoSharedThreadSafeHeapNode
@required
@property DyrectoSharedThreadSafeHeap<id> * _Nullable heap __attribute__((swift_name("heap")));
@property int32_t index __attribute__((swift_name("index")));
@end

__attribute__((swift_name("SelectBuilder")))
@protocol DyrectoSharedSelectBuilder
@required
- (void)invoke:(id<DyrectoSharedSelectClause0>)receiver block:(id<DyrectoSharedKotlinSuspendFunction0>)block __attribute__((swift_name("invoke(_:block:)")));
- (void)invoke:(id<DyrectoSharedSelectClause1>)receiver block_:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:block_:)")));
- (void)invoke:(id<DyrectoSharedSelectClause2>)receiver block__:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:block__:)")));
- (void)invoke:(id<DyrectoSharedSelectClause2>)receiver param:(id _Nullable)param block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("invoke(_:param:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (void)onTimeoutTimeMillis:(int64_t)timeMillis block:(id<DyrectoSharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(timeMillis:block:)"))) __attribute__((unavailable("Replaced with the same extension function")));
@end

__attribute__((swift_name("SelectClause")))
@protocol DyrectoSharedSelectClause
@required
@property (readonly) id clauseObject __attribute__((swift_name("clauseObject")));
@property (readonly) DyrectoSharedKotlinUnit *(^(^ _Nullable onCancellationConstructor)(id<DyrectoSharedSelectInstance>, id _Nullable, id _Nullable))(DyrectoSharedKotlinThrowable *) __attribute__((swift_name("onCancellationConstructor")));
@property (readonly) id _Nullable (^processResFunc)(id, id _Nullable, id _Nullable) __attribute__((swift_name("processResFunc")));
@property (readonly) void (^regFunc)(id, id<DyrectoSharedSelectInstance>, id _Nullable) __attribute__((swift_name("regFunc")));
@end

__attribute__((swift_name("SelectClause0")))
@protocol DyrectoSharedSelectClause0 <DyrectoSharedSelectClause>
@required
@end

__attribute__((swift_name("SelectClause1")))
@protocol DyrectoSharedSelectClause1 <DyrectoSharedSelectClause>
@required
@end

__attribute__((swift_name("SelectClause2")))
@protocol DyrectoSharedSelectClause2 <DyrectoSharedSelectClause>
@required
@end

__attribute__((swift_name("SelectInstance")))
@protocol DyrectoSharedSelectInstance
@required
- (void)disposeOnCompletionDisposableHandle:(id<DyrectoSharedDisposableHandle>)disposableHandle __attribute__((swift_name("disposeOnCompletion(disposableHandle:)")));
- (void)selectInRegistrationPhaseInternalResult:(id _Nullable)internalResult __attribute__((swift_name("selectInRegistrationPhase(internalResult:)")));
- (BOOL)trySelectClauseObject:(id)clauseObject result:(id _Nullable)result __attribute__((swift_name("trySelect(clauseObject:result:)")));
@property (readonly) id<DyrectoSharedKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end

__attribute__((swift_name("Mutex")))
@protocol DyrectoSharedMutex
@required
- (BOOL)holdsLockOwner:(id)owner __attribute__((swift_name("holdsLock(owner:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)lockOwner:(id _Nullable)owner completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("lock(owner:completionHandler:)")));
- (BOOL)tryLockOwner:(id _Nullable)owner __attribute__((swift_name("tryLock(owner:)")));
- (void)unlockOwner:(id _Nullable)owner __attribute__((swift_name("unlock(owner:)")));
@property (readonly) BOOL isLocked __attribute__((swift_name("isLocked")));
@property (readonly) id<DyrectoSharedSelectClause2> onLock __attribute__((swift_name("onLock"))) __attribute__((deprecated("Mutex.onLock deprecated without replacement. For additional details please refer to #2794")));
@end

__attribute__((swift_name("Semaphore")))
@protocol DyrectoSharedSemaphore
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)acquireWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("acquire(completionHandler:)")));
- (void)release_ __attribute__((swift_name("release()")));
- (BOOL)tryAcquire __attribute__((swift_name("tryAcquire()")));
@property (readonly) int32_t availablePermits __attribute__((swift_name("availablePermits")));
@end

@interface DyrectoSharedVericFrameRef (Extensions)
- (NSData *)jpegNSData __attribute__((swift_name("jpegNSData()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface DyrectoSharedKotlinArray<T> : DyrectoSharedBase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(DyrectoSharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<DyrectoSharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface DyrectoSharedKotlinArray (Extensions)
- (id<DyrectoSharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntArray")))
@interface DyrectoSharedKotlinIntArray : DyrectoSharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(DyrectoSharedInt *(^)(DyrectoSharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int32_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (DyrectoSharedKotlinIntIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int32_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface DyrectoSharedKotlinIntArray (Extensions)
- (id<DyrectoSharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongArray")))
@interface DyrectoSharedKotlinLongArray : DyrectoSharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(DyrectoSharedLong *(^)(DyrectoSharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int64_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (DyrectoSharedKotlinLongIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int64_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

@interface DyrectoSharedKotlinLongArray (Extensions)
- (id<DyrectoSharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((swift_name("KotlinIterable")))
@protocol DyrectoSharedKotlinIterable
@required
- (id<DyrectoSharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
@end

__attribute__((swift_name("KotlinIntProgression")))
@interface DyrectoSharedKotlinIntProgression : DyrectoSharedBase <DyrectoSharedKotlinIterable>
@property (class, readonly, getter=companion) DyrectoSharedKotlinIntProgressionCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (DyrectoSharedKotlinIntIterator *)iterator __attribute__((swift_name("iterator()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t first __attribute__((swift_name("first")));
@property (readonly) int32_t last __attribute__((swift_name("last")));
@property (readonly) int32_t step __attribute__((swift_name("step")));
@end

__attribute__((swift_name("KotlinClosedRange")))
@protocol DyrectoSharedKotlinClosedRange
@required
- (BOOL)containsValue:(id)value __attribute__((swift_name("contains(value:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
@property (readonly) id endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) id start __attribute__((swift_name("start")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
__attribute__((swift_name("KotlinOpenEndRange")))
@protocol DyrectoSharedKotlinOpenEndRange
@required
- (BOOL)containsValue_:(id)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
@property (readonly) id endExclusive __attribute__((swift_name("endExclusive")));
@property (readonly, getter=start_) id start __attribute__((swift_name("start")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntRange")))
@interface DyrectoSharedKotlinIntRange : DyrectoSharedKotlinIntProgression <DyrectoSharedKotlinClosedRange, DyrectoSharedKotlinOpenEndRange>
- (instancetype)initWithStart:(int32_t)start endInclusive:(int32_t)endInclusive __attribute__((swift_name("init(start:endInclusive:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedKotlinIntRangeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(DyrectoSharedInt *)value __attribute__((swift_name("contains(value:)")));
- (BOOL)containsValue_:(DyrectoSharedInt *)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
@property (readonly) DyrectoSharedInt *endExclusive __attribute__((swift_name("endExclusive"))) __attribute__((deprecated("Can throw an exception when it's impossible to represent the value with Int type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")));
@property (readonly) DyrectoSharedInt *endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) DyrectoSharedInt *start __attribute__((swift_name("start")));
@end

@interface DyrectoSharedKotlinIntRange (Extensions)
- (id<DyrectoSharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

__attribute__((swift_name("KotlinLongProgression")))
@interface DyrectoSharedKotlinLongProgression : DyrectoSharedBase <DyrectoSharedKotlinIterable>
@property (class, readonly, getter=companion) DyrectoSharedKotlinLongProgressionCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (DyrectoSharedKotlinLongIterator *)iterator __attribute__((swift_name("iterator()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t first __attribute__((swift_name("first")));
@property (readonly) int64_t last __attribute__((swift_name("last")));
@property (readonly) int64_t step __attribute__((swift_name("step")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongRange")))
@interface DyrectoSharedKotlinLongRange : DyrectoSharedKotlinLongProgression <DyrectoSharedKotlinClosedRange, DyrectoSharedKotlinOpenEndRange>
- (instancetype)initWithStart:(int64_t)start endInclusive:(int64_t)endInclusive __attribute__((swift_name("init(start:endInclusive:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) DyrectoSharedKotlinLongRangeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)containsValue:(DyrectoSharedLong *)value __attribute__((swift_name("contains(value:)")));
- (BOOL)containsValue_:(DyrectoSharedLong *)value __attribute__((swift_name("contains(value_:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
@property (readonly) DyrectoSharedLong *endExclusive __attribute__((swift_name("endExclusive"))) __attribute__((deprecated("Can throw an exception when it's impossible to represent the value with Long type, for example, when the range includes MAX_VALUE. It's recommended to use 'endInclusive' property that doesn't throw.")));
@property (readonly) DyrectoSharedLong *endInclusive __attribute__((swift_name("endInclusive")));
@property (readonly, getter=start_) DyrectoSharedLong *start __attribute__((swift_name("start")));
@end

@interface DyrectoSharedKotlinLongRange (Extensions)
- (id<DyrectoSharedFlow>)asFlow __attribute__((swift_name("asFlow()")));
@end

@interface DyrectoSharedCoroutineDispatcher (Extensions)

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(block:completionHandler:)")));
@end

@interface DyrectoSharedDispatchers (Extensions)
@property (readonly) DyrectoSharedCoroutineDispatcher *IO __attribute__((swift_name("IO")));
@end

@interface DyrectoSharedSharingStartedCompanion (Extensions)
- (id<DyrectoSharedSharingStarted>)WhileSubscribedStopTimeout:(int64_t)stopTimeout replayExpiration:(int64_t)replayExpiration __attribute__((swift_name("WhileSubscribed(stopTimeout:replayExpiration:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AwaitKt")))
@interface DyrectoSharedAwaitKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitAll:(id)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitAll(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitAllDeferreds:(DyrectoSharedKotlinArray<id<DyrectoSharedDeferred>> *)deferreds completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitAll(deferreds:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)joinAll:(id)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("joinAll(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)joinAllJobs:(DyrectoSharedKotlinArray<id<DyrectoSharedJob>> *)jobs completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("joinAll(jobs:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BroadcastKt")))
@interface DyrectoSharedBroadcastKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<DyrectoSharedBroadcastChannel>)broadcast:(id<DyrectoSharedReceiveChannel>)receiver capacity:(int32_t)capacity start:(DyrectoSharedCoroutineStart *)start __attribute__((swift_name("broadcast(_:capacity:start:)"))) __attribute__((deprecated("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<DyrectoSharedBroadcastChannel>)broadcast:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity start:(DyrectoSharedCoroutineStart *)start onCompletion:(void (^ _Nullable)(DyrectoSharedKotlinThrowable * _Nullable))onCompletion block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("broadcast(_:context:capacity:start:onCompletion:block:)"))) __attribute__((deprecated("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BroadcastChannelKt")))
@interface DyrectoSharedBroadcastChannelKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id<DyrectoSharedBroadcastChannel>)BroadcastChannelCapacity:(int32_t)capacity __attribute__((swift_name("BroadcastChannel(capacity:)"))) __attribute__((deprecated("BroadcastChannel is deprecated in the favour of SharedFlow and StateFlow, and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Builders_commonKt")))
@interface DyrectoSharedBuilders_commonKt : DyrectoSharedBase
+ (id<DyrectoSharedDeferred>)async:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context start:(DyrectoSharedCoroutineStart *)start block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("async(_:context:start:block:)")));
+ (id<DyrectoSharedJob>)launch:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context start:(DyrectoSharedCoroutineStart *)start block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("launch(_:context:start:block:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withContextContext:(id<DyrectoSharedKotlinCoroutineContext>)context block:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withContext(context:block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("BuildersKt")))
@interface DyrectoSharedBuildersKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)asFlow:(id _Nullable (^)(void))receiver __attribute__((swift_name("asFlow(_:)")));
+ (id<DyrectoSharedFlow>)asFlow_:(id)receiver __attribute__((swift_name("asFlow(__:)")));
+ (id<DyrectoSharedFlow>)asFlow__:(id<DyrectoSharedKotlinIterator>)receiver __attribute__((swift_name("asFlow(___:)")));
+ (id<DyrectoSharedFlow>)asFlow___:(id<DyrectoSharedKotlinSuspendFunction0>)receiver __attribute__((swift_name("asFlow(____:)")));
+ (id<DyrectoSharedFlow>)asFlow____:(id<DyrectoSharedKotlinSequence>)receiver __attribute__((swift_name("asFlow(_____:)")));
+ (id<DyrectoSharedFlow>)callbackFlowBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("callbackFlow(block:)")));
+ (id<DyrectoSharedFlow>)channelFlowBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("channelFlow(block:)")));
+ (id<DyrectoSharedFlow>)emptyFlow __attribute__((swift_name("emptyFlow()")));
+ (id<DyrectoSharedFlow>)flowBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("flow(block:)")));
+ (id<DyrectoSharedFlow>)flowOfValue:(id _Nullable)value __attribute__((swift_name("flowOf(value:)")));
+ (id<DyrectoSharedFlow>)flowOfElements:(DyrectoSharedKotlinArray<id> *)elements __attribute__((swift_name("flowOf(elements:)")));
+ (id _Nullable)runBlockingContext:(id<DyrectoSharedKotlinCoroutineContext>)context block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("runBlocking(context:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CancellableKt")))
@interface DyrectoSharedCancellableKt : DyrectoSharedBase
+ (void)startCoroutineCancellable:(id<DyrectoSharedKotlinSuspendFunction0>)receiver completion:(id<DyrectoSharedKotlinContinuation>)completion __attribute__((swift_name("startCoroutineCancellable(_:completion:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CancellableContinuationKt")))
@interface DyrectoSharedCancellableContinuationKt : DyrectoSharedBase
+ (void)disposeOnCancellation:(id<DyrectoSharedCancellableContinuation>)receiver handle:(id<DyrectoSharedDisposableHandle>)handle __attribute__((swift_name("disposeOnCancellation(_:handle:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)suspendCancellableCoroutineBlock:(void (^)(id<DyrectoSharedCancellableContinuation>))block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("suspendCancellableCoroutine(block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelKt")))
@interface DyrectoSharedChannelKt : DyrectoSharedBase
+ (id<DyrectoSharedChannel>)ChannelCapacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow onUndeliveredElement:(void (^ _Nullable)(id _Nullable))onUndeliveredElement __attribute__((swift_name("Channel(capacity:onBufferOverflow:onUndeliveredElement:)")));
+ (id _Nullable)getOrElse:(id _Nullable)receiver onFailure:(id _Nullable (^)(DyrectoSharedKotlinThrowable * _Nullable))onFailure __attribute__((swift_name("getOrElse(_:onFailure:)")));
+ (id _Nullable)onClosed:(id _Nullable)receiver action:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))action __attribute__((swift_name("onClosed(_:action:)")));
+ (id _Nullable)onFailure:(id _Nullable)receiver action:(void (^)(DyrectoSharedKotlinThrowable * _Nullable))action __attribute__((swift_name("onFailure(_:action:)")));
+ (id _Nullable)onSuccess:(id _Nullable)receiver action:(void (^)(id _Nullable))action __attribute__((swift_name("onSuccess(_:action:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Channels_commonKt")))
@interface DyrectoSharedChannels_commonKt : DyrectoSharedBase
+ (id _Nullable)consume:(id<DyrectoSharedReceiveChannel>)receiver block:(id _Nullable (^)(id<DyrectoSharedReceiveChannel>))block __attribute__((swift_name("consume(_:block:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)consumeEach:(id<DyrectoSharedReceiveChannel>)receiver action:(void (^)(id _Nullable))action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("consumeEach(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<DyrectoSharedReceiveChannel>)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ChannelsKt")))
@interface DyrectoSharedChannelsKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)asFlow:(id<DyrectoSharedBroadcastChannel>)receiver __attribute__((swift_name("asFlow(_:)"))) __attribute__((unavailable("'BroadcastChannel' is obsolete and all corresponding operators are deprecated in the favour of StateFlow and SharedFlow")));
+ (id<DyrectoSharedFlow>)consumeAsFlow:(id<DyrectoSharedReceiveChannel>)receiver __attribute__((swift_name("consumeAsFlow(_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)emitAll:(id<DyrectoSharedFlowCollector>)receiver channel:(id<DyrectoSharedReceiveChannel>)channel completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emitAll(_:channel:completionHandler:)")));
+ (id<DyrectoSharedReceiveChannel>)produceIn:(id<DyrectoSharedFlow>)receiver scope:(id<DyrectoSharedCoroutineScope>)scope __attribute__((swift_name("produceIn(_:scope:)")));
+ (id<DyrectoSharedFlow>)receiveAsFlow:(id<DyrectoSharedReceiveChannel>)receiver __attribute__((swift_name("receiveAsFlow(_:)")));
+ (id _Nullable)trySendBlocking:(id<DyrectoSharedSendChannel>)receiver element:(id _Nullable)element __attribute__((swift_name("trySendBlocking(_:element:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CollectKt")))
@interface DyrectoSharedCollectKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collect:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collectIndexed:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction2>)action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectIndexed(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)collectLatest:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collectLatest(_:action:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)emitAll:(id<DyrectoSharedFlowCollector>)receiver flow:(id<DyrectoSharedFlow>)flow completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emitAll(_:flow:completionHandler:)")));
+ (id<DyrectoSharedJob>)launchIn:(id<DyrectoSharedFlow>)receiver scope:(id<DyrectoSharedCoroutineScope>)scope __attribute__((swift_name("launchIn(_:scope:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CollectionKt")))
@interface DyrectoSharedCollectionKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toCollection:(id<DyrectoSharedFlow>)receiver destination:(id)destination completionHandler:(void (^)(id _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toCollection(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<DyrectoSharedFlow>)receiver destination:(NSMutableArray<id> *)destination completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<DyrectoSharedFlow>)receiver destination:(DyrectoSharedMutableSet<id> *)destination completionHandler:(void (^)(NSSet<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:destination:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CompletableDeferredKt")))
@interface DyrectoSharedCompletableDeferredKt : DyrectoSharedBase
+ (id<DyrectoSharedCompletableDeferred>)CompletableDeferredValue:(id _Nullable)value __attribute__((swift_name("CompletableDeferred(value:)")));
+ (id<DyrectoSharedCompletableDeferred>)CompletableDeferredParent:(id<DyrectoSharedJob> _Nullable)parent __attribute__((swift_name("CompletableDeferred(parent:)")));
+ (BOOL)completeWith:(id<DyrectoSharedCompletableDeferred>)receiver result:(id _Nullable)result __attribute__((swift_name("completeWith(_:result:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ContextKt")))
@interface DyrectoSharedContextKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)buffer:(id<DyrectoSharedFlow>)receiver capacity:(int32_t)capacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("buffer(_:capacity:onBufferOverflow:)")));
+ (id<DyrectoSharedFlow>)cancellable:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("cancellable(_:)")));
+ (id<DyrectoSharedFlow>)conflate:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("conflate(_:)")));
+ (id<DyrectoSharedFlow>)flowOn:(id<DyrectoSharedFlow>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("flowOn(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineContextKt")))
@interface DyrectoSharedCoroutineContextKt : DyrectoSharedBase
+ (id<DyrectoSharedKotlinCoroutineContext>)doNewCoroutineContext:(id<DyrectoSharedKotlinCoroutineContext>)receiver addedContext:(id<DyrectoSharedKotlinCoroutineContext>)addedContext __attribute__((swift_name("doNewCoroutineContext(_:addedContext:)")));
+ (id<DyrectoSharedKotlinCoroutineContext>)doNewCoroutineContext:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("doNewCoroutineContext(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineExceptionHandlerKt")))
@interface DyrectoSharedCoroutineExceptionHandlerKt : DyrectoSharedBase
+ (id<DyrectoSharedCoroutineExceptionHandler>)CoroutineExceptionHandlerHandler:(void (^)(id<DyrectoSharedKotlinCoroutineContext>, DyrectoSharedKotlinThrowable *))handler __attribute__((swift_name("CoroutineExceptionHandler(handler:)")));
+ (void)handleCoroutineExceptionContext:(id<DyrectoSharedKotlinCoroutineContext>)context exception:(DyrectoSharedKotlinThrowable *)exception __attribute__((swift_name("handleCoroutineException(context:exception:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CoroutineScopeKt")))
@interface DyrectoSharedCoroutineScopeKt : DyrectoSharedBase
+ (BOOL)isActive:(id<DyrectoSharedCoroutineScope>)receiver __attribute__((swift_name("isActive(_:)")));
+ (id<DyrectoSharedCoroutineScope>)CoroutineScopeContext:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("CoroutineScope(context:)")));
+ (id<DyrectoSharedCoroutineScope>)MainScope __attribute__((swift_name("MainScope()")));
+ (void)cancel:(id<DyrectoSharedCoroutineScope>)receiver cause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)")));
+ (void)cancel:(id<DyrectoSharedCoroutineScope>)receiver message:(NSString *)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(_:message:cause:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)coroutineScopeBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("coroutineScope(block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)currentCoroutineContextWithCompletionHandler:(void (^)(id<DyrectoSharedKotlinCoroutineContext> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("currentCoroutineContext(completionHandler:)")));
+ (void)ensureActive:(id<DyrectoSharedCoroutineScope>)receiver __attribute__((swift_name("ensureActive(_:)")));
+ (id<DyrectoSharedCoroutineScope>)plus:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("plus(_:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CountKt")))
@interface DyrectoSharedCountKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(DyrectoSharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(DyrectoSharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:predicate:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DelayKt")))
@interface DyrectoSharedDelayKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitCancellationWithCompletionHandler:(void (^)(DyrectoSharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("awaitCancellation(completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)debounce:(id<DyrectoSharedFlow>)receiver timeoutMillis:(DyrectoSharedLong *(^)(id _Nullable))timeoutMillis __attribute__((swift_name("debounce(_:timeoutMillis:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
 *   kotlin.jvm.JvmName(name="debounceDuration")
*/
+ (id<DyrectoSharedFlow>)debounce:(id<DyrectoSharedFlow>)receiver timeout:(id (^)(id _Nullable))timeout __attribute__((swift_name("debounce(_:timeout:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)debounce:(id<DyrectoSharedFlow>)receiver timeoutMillis_:(int64_t)timeoutMillis __attribute__((swift_name("debounce(_:timeoutMillis_:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)debounce:(id<DyrectoSharedFlow>)receiver timeout_:(int64_t)timeout __attribute__((swift_name("debounce(_:timeout_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)delayTimeMillis:(int64_t)timeMillis completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(timeMillis:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)delayDuration:(int64_t)duration completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("delay(duration:completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)sample:(id<DyrectoSharedFlow>)receiver periodMillis:(int64_t)periodMillis __attribute__((swift_name("sample(_:periodMillis:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)sample:(id<DyrectoSharedFlow>)receiver period:(int64_t)period __attribute__((swift_name("sample(_:period:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
+ (id<DyrectoSharedFlow>)timeout:(id<DyrectoSharedFlow>)receiver timeout:(int64_t)timeout __attribute__((swift_name("timeout(_:timeout:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DeprecatedKt")))
@interface DyrectoSharedDeprecatedKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ObsoleteCoroutinesApi
*/
+ (id _Nullable)consume:(id<DyrectoSharedBroadcastChannel>)receiver block:(id _Nullable (^)(id<DyrectoSharedReceiveChannel>))block __attribute__((swift_name("consume(_:block:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)consumeEach:(id<DyrectoSharedBroadcastChannel>)receiver action:(void (^)(id _Nullable))action completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("consumeEach(_:action:completionHandler:)"))) __attribute__((unavailable("BroadcastChannel is deprecated in the favour of SharedFlow and is no longer supported")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DispatchedContinuationKt")))
@interface DyrectoSharedDispatchedContinuationKt : DyrectoSharedBase
+ (void)resumeCancellableWith:(id<DyrectoSharedKotlinContinuation>)receiver result:(id _Nullable)result onCancellation:(void (^ _Nullable)(DyrectoSharedKotlinThrowable *))onCancellation __attribute__((swift_name("resumeCancellableWith(_:result:onCancellation:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DistinctKt")))
@interface DyrectoSharedDistinctKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)distinctUntilChanged:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("distinctUntilChanged(_:)")));
+ (id<DyrectoSharedFlow>)distinctUntilChanged:(id<DyrectoSharedFlow>)receiver areEquivalent:(DyrectoSharedBoolean *(^)(id _Nullable, id _Nullable))areEquivalent __attribute__((swift_name("distinctUntilChanged(_:areEquivalent:)")));
+ (id<DyrectoSharedFlow>)distinctUntilChangedBy:(id<DyrectoSharedFlow>)receiver keySelector:(id _Nullable (^)(id _Nullable))keySelector __attribute__((swift_name("distinctUntilChangedBy(_:keySelector:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EmittersKt")))
@interface DyrectoSharedEmittersKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)onCompletion:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction2>)action __attribute__((swift_name("onCompletion(_:action:)")));
+ (id<DyrectoSharedFlow>)onEmpty:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action __attribute__((swift_name("onEmpty(_:action:)")));
+ (id<DyrectoSharedFlow>)onStart:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action __attribute__((swift_name("onStart(_:action:)")));
+ (id<DyrectoSharedFlow>)transform:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transform(_:transform:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ErrorsKt")))
@interface DyrectoSharedErrorsKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)catch:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction2>)action __attribute__((swift_name("catch(_:action:)")));
+ (id<DyrectoSharedFlow>)retry:(id<DyrectoSharedFlow>)receiver retries:(int64_t)retries predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("retry(_:retries:predicate:)")));
+ (id<DyrectoSharedFlow>)retryWhen:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction3>)predicate __attribute__((swift_name("retryWhen(_:predicate:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ExceptionsKt")))
@interface DyrectoSharedExceptionsKt : DyrectoSharedBase
+ (DyrectoSharedKotlinCancellationException *)CancellationExceptionMessage:(NSString * _Nullable)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("CancellationException(message:cause:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IosFactoriesKt")))
@interface DyrectoSharedIosFactoriesKt : DyrectoSharedBase
+ (DyrectoSharedCameraConnectionState *)emptyCameraConnectionState __attribute__((swift_name("emptyCameraConnectionState()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("JobKt")))
@interface DyrectoSharedJobKt : DyrectoSharedBase
+ (BOOL)isActive:(id<DyrectoSharedKotlinCoroutineContext>)receiver __attribute__((swift_name("isActive(_:)")));
+ (id<DyrectoSharedJob>)job:(id<DyrectoSharedKotlinCoroutineContext>)receiver __attribute__((swift_name("job(_:)")));
+ (id<DyrectoSharedCompletableJob>)JobParent:(id<DyrectoSharedJob> _Nullable)parent __attribute__((swift_name("Job(parent:)")));
+ (void)cancel:(id<DyrectoSharedKotlinCoroutineContext>)receiver cause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)")));
+ (void)cancel:(id<DyrectoSharedJob>)receiver message:(NSString *)message cause:(DyrectoSharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(_:message:cause:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)cancelAndJoin:(id<DyrectoSharedJob>)receiver completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("cancelAndJoin(_:completionHandler:)")));
+ (void)cancelChildren:(id<DyrectoSharedKotlinCoroutineContext>)receiver cause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancelChildren(_:cause:)")));
+ (void)cancelChildren:(id<DyrectoSharedJob>)receiver cause_:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancelChildren(_:cause_:)")));
+ (void)ensureActive:(id<DyrectoSharedKotlinCoroutineContext>)receiver __attribute__((swift_name("ensureActive(_:)")));
+ (void)ensureActive_:(id<DyrectoSharedJob>)receiver __attribute__((swift_name("ensureActive(__:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LimitKt")))
@interface DyrectoSharedLimitKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)drop:(id<DyrectoSharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("drop(_:count:)")));
+ (id<DyrectoSharedFlow>)dropWhile:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("dropWhile(_:predicate:)")));
+ (id<DyrectoSharedFlow>)take:(id<DyrectoSharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("take(_:count:)")));
+ (id<DyrectoSharedFlow>)takeWhile:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("takeWhile(_:predicate:)")));
+ (id<DyrectoSharedFlow>)transformWhile:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transformWhile(_:transform:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LintKt")))
@interface DyrectoSharedLintKt : DyrectoSharedBase
+ (id<DyrectoSharedKotlinCoroutineContext>)coroutineContext:(id<DyrectoSharedFlowCollector>)receiver __attribute__((swift_name("coroutineContext(_:)"))) __attribute__((unavailable("coroutineContext is resolved into the property of outer CoroutineScope which is likely to be an error.Use currentCoroutineContext() instead or specify the receiver of coroutineContext explicitly")));
+ (BOOL)isActive:(id<DyrectoSharedFlowCollector>)receiver __attribute__((swift_name("isActive(_:)"))) __attribute__((unavailable("isActive is resolved into the extension of outer CoroutineScope which is likely to be an error.Use currentCoroutineContext().isActive or cancellable() operator instead or specify the receiver of isActive explicitly. Additionally, flow {} builder emissions are cancellable by default.")));
+ (void)cancel:(id<DyrectoSharedFlowCollector>)receiver cause:(DyrectoSharedKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(_:cause:)"))) __attribute__((unavailable("cancel() is resolved into the extension of outer CoroutineScope which is likely to be an error.Use currentCoroutineContext().cancel() instead or specify the receiver of cancel() explicitly")));
+ (id<DyrectoSharedFlow>)cancellable:(id<DyrectoSharedSharedFlow>)receiver __attribute__((swift_name("cancellable(_:)"))) __attribute__((unavailable("Applying 'cancellable' to a SharedFlow has no effect. See the SharedFlow documentation on Operator Fusion.")));
+ (id<DyrectoSharedFlow>)catch:(id<DyrectoSharedSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction2>)action __attribute__((swift_name("catch(_:action:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator typically has not effect, it can only catch exceptions from 'onSubscribe' operator")));
+ (id<DyrectoSharedFlow>)conflate:(id<DyrectoSharedStateFlow>)receiver __attribute__((swift_name("conflate(_:)"))) __attribute__((unavailable("Applying 'conflate' to StateFlow has no effect. See the StateFlow documentation on Operator Fusion.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)count:(id<DyrectoSharedSharedFlow>)receiver completionHandler:(void (^)(DyrectoSharedInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("count(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));
+ (id<DyrectoSharedFlow>)distinctUntilChanged:(id<DyrectoSharedStateFlow>)receiver __attribute__((swift_name("distinctUntilChanged(_:)"))) __attribute__((unavailable("Applying 'distinctUntilChanged' to StateFlow has no effect. See the StateFlow documentation on Operator Fusion.")));
+ (id<DyrectoSharedFlow>)flowOn:(id<DyrectoSharedSharedFlow>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("flowOn(_:context:)"))) __attribute__((unavailable("Applying 'flowOn' to SharedFlow has no effect. See the SharedFlow documentation on Operator Fusion.")));
+ (id<DyrectoSharedFlow>)retry:(id<DyrectoSharedSharedFlow>)receiver retries:(int64_t)retries predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("retry(_:retries:predicate:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator has no effect.")));
+ (id<DyrectoSharedFlow>)retryWhen:(id<DyrectoSharedSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction3>)predicate __attribute__((swift_name("retryWhen(_:predicate:)"))) __attribute__((deprecated("SharedFlow never completes, so this operator has no effect.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<DyrectoSharedSharedFlow>)receiver completionHandler:(void (^)(NSArray<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toList:(id<DyrectoSharedSharedFlow>)receiver destination:(NSMutableArray<id> *)destination completionHandler:(void (^)(DyrectoSharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toList(_:destination:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<DyrectoSharedSharedFlow>)receiver completionHandler:(void (^)(NSSet<id> * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:completionHandler:)"))) __attribute__((deprecated("SharedFlow never completes, so this terminal operation never completes.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)toSet:(id<DyrectoSharedSharedFlow>)receiver destination:(DyrectoSharedMutableSet<id> *)destination completionHandler:(void (^)(DyrectoSharedKotlinNothing * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("toSet(_:destination:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MergeKt")))
@interface DyrectoSharedMergeKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)flatMapConcat:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapConcat(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)flatMapLatest:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapLatest(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)flatMapMerge:(id<DyrectoSharedFlow>)receiver concurrency:(int32_t)concurrency transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("flatMapMerge(_:concurrency:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)flattenConcat:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("flattenConcat(_:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)flattenMerge:(id<DyrectoSharedFlow>)receiver concurrency:(int32_t)concurrency __attribute__((swift_name("flattenMerge(_:concurrency:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)mapLatest:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("mapLatest(_:transform:)")));
+ (id<DyrectoSharedFlow>)merge:(id)receiver __attribute__((swift_name("merge(_:)")));
+ (id<DyrectoSharedFlow>)mergeFlows:(DyrectoSharedKotlinArray<id<DyrectoSharedFlow>> *)flows __attribute__((swift_name("merge(flows:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedFlow>)transformLatest:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("transformLatest(_:transform:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
@property (class, readonly) int32_t DEFAULT_CONCURRENCY __attribute__((swift_name("DEFAULT_CONCURRENCY")));

/**
 * @note annotations
 *   kotlinx.coroutines.FlowPreview
*/
@property (class, readonly) NSString *DEFAULT_CONCURRENCY_PROPERTY_NAME __attribute__((swift_name("DEFAULT_CONCURRENCY_PROPERTY_NAME")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MigrationKt")))
@interface DyrectoSharedMigrationKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)cache:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("cache(_:)"))) __attribute__((unavailable("Flow analogue of 'cache()' is 'shareIn' with unlimited replay and 'started = SharingStared.Lazily' argument'")));
+ (id<DyrectoSharedFlow>)combineLatest:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineLatest(_:other:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<DyrectoSharedFlow>)combineLatest:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other other2:(id<DyrectoSharedFlow>)other2 transform:(id<DyrectoSharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineLatest(_:other:other2:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<DyrectoSharedFlow>)combineLatest:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other other2:(id<DyrectoSharedFlow>)other2 other3:(id<DyrectoSharedFlow>)other3 transform:(id<DyrectoSharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combineLatest(_:other:other2:other3:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<DyrectoSharedFlow>)combineLatest:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other other2:(id<DyrectoSharedFlow>)other2 other3:(id<DyrectoSharedFlow>)other3 other4:(id<DyrectoSharedFlow>)other4 transform:(id<DyrectoSharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combineLatest(_:other:other2:other3:other4:transform:)"))) __attribute__((unavailable("Flow analogue of 'combineLatest' is 'combine'")));
+ (id<DyrectoSharedFlow>)compose:(id<DyrectoSharedFlow>)receiver transformer:(id<DyrectoSharedFlow> (^)(id<DyrectoSharedFlow>))transformer __attribute__((swift_name("compose(_:transformer:)"))) __attribute__((unavailable("Flow analogue of 'compose' is 'let'")));
+ (id<DyrectoSharedFlow>)concatMap:(id<DyrectoSharedFlow>)receiver mapper:(id<DyrectoSharedFlow> (^)(id _Nullable))mapper __attribute__((swift_name("concatMap(_:mapper:)"))) __attribute__((unavailable("Flow analogue of 'concatMap' is 'flatMapConcat'")));
+ (id<DyrectoSharedFlow>)concatWith:(id<DyrectoSharedFlow>)receiver value:(id _Nullable)value __attribute__((swift_name("concatWith(_:value:)"))) __attribute__((unavailable("Flow analogue of 'concatWith' is 'onCompletion'. Use 'onCompletion { emit(value) }'")));
+ (id<DyrectoSharedFlow>)concatWith:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other __attribute__((swift_name("concatWith(_:other:)"))) __attribute__((unavailable("Flow analogue of 'concatWith' is 'onCompletion'. Use 'onCompletion { if (it == null) emitAll(other) }'")));
+ (id<DyrectoSharedFlow>)delayEach:(id<DyrectoSharedFlow>)receiver timeMillis:(int64_t)timeMillis __attribute__((swift_name("delayEach(_:timeMillis:)"))) __attribute__((unavailable("Use 'onEach { delay(timeMillis) }'")));
+ (id<DyrectoSharedFlow>)delayFlow:(id<DyrectoSharedFlow>)receiver timeMillis:(int64_t)timeMillis __attribute__((swift_name("delayFlow(_:timeMillis:)"))) __attribute__((unavailable("Use 'onStart { delay(timeMillis) }'")));
+ (id<DyrectoSharedFlow>)flatMap:(id<DyrectoSharedFlow>)receiver mapper:(id<DyrectoSharedKotlinSuspendFunction1>)mapper __attribute__((swift_name("flatMap(_:mapper:)"))) __attribute__((unavailable("Flow analogue is 'flatMapConcat'")));
+ (id<DyrectoSharedFlow>)flatten:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("flatten(_:)"))) __attribute__((unavailable("Flow analogue of 'flatten' is 'flattenConcat'")));
+ (void)forEach:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action __attribute__((swift_name("forEach(_:action:)"))) __attribute__((unavailable("Flow analogue of 'forEach' is 'collect'")));
+ (id<DyrectoSharedFlow>)merge:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("merge(_:)"))) __attribute__((unavailable("Flow analogue of 'merge' is 'flattenConcat'")));
+ (id<DyrectoSharedFlow>)observeOn:(id<DyrectoSharedFlow>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("observeOn(_:context:)"))) __attribute__((unavailable("Collect flow in the desired context instead")));
+ (id<DyrectoSharedFlow>)onErrorResume:(id<DyrectoSharedFlow>)receiver fallback:(id<DyrectoSharedFlow>)fallback __attribute__((swift_name("onErrorResume(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emitAll(fallback) }'")));
+ (id<DyrectoSharedFlow>)onErrorResumeNext:(id<DyrectoSharedFlow>)receiver fallback:(id<DyrectoSharedFlow>)fallback __attribute__((swift_name("onErrorResumeNext(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emitAll(fallback) }'")));
+ (id<DyrectoSharedFlow>)onErrorReturn:(id<DyrectoSharedFlow>)receiver fallback:(id _Nullable)fallback __attribute__((swift_name("onErrorReturn(_:fallback:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { emit(fallback) }'")));
+ (id<DyrectoSharedFlow>)onErrorReturn:(id<DyrectoSharedFlow>)receiver fallback:(id _Nullable)fallback predicate:(DyrectoSharedBoolean *(^)(DyrectoSharedKotlinThrowable *))predicate __attribute__((swift_name("onErrorReturn(_:fallback:predicate:)"))) __attribute__((unavailable("Flow analogue of 'onErrorXxx' is 'catch'. Use 'catch { e -> if (predicate(e)) emit(fallback) else throw e }'")));
+ (id<DyrectoSharedFlow>)publish:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("publish(_:)"))) __attribute__((unavailable("Flow analogue of 'publish()' is 'shareIn'. \npublish().connect() is the default strategy (no extra call is needed), \npublish().autoConnect() translates to 'started = SharingStared.Lazily' argument, \npublish().refCount() translates to 'started = SharingStared.WhileSubscribed()' argument.")));
+ (id<DyrectoSharedFlow>)publish:(id<DyrectoSharedFlow>)receiver bufferSize:(int32_t)bufferSize __attribute__((swift_name("publish(_:bufferSize:)"))) __attribute__((unavailable("Flow analogue of 'publish(bufferSize)' is 'buffer' followed by 'shareIn'. \npublish().connect() is the default strategy (no extra call is needed), \npublish().autoConnect() translates to 'started = SharingStared.Lazily' argument, \npublish().refCount() translates to 'started = SharingStared.WhileSubscribed()' argument.")));
+ (id<DyrectoSharedFlow>)publishOn:(id<DyrectoSharedFlow>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("publishOn(_:context:)"))) __attribute__((unavailable("Collect flow in the desired context instead")));
+ (id<DyrectoSharedFlow>)replay:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("replay(_:)"))) __attribute__((unavailable("Flow analogue of 'replay()' is 'shareIn' with unlimited replay. \nreplay().connect() is the default strategy (no extra call is needed), \nreplay().autoConnect() translates to 'started = SharingStared.Lazily' argument, \nreplay().refCount() translates to 'started = SharingStared.WhileSubscribed()' argument.")));
+ (id<DyrectoSharedFlow>)replay:(id<DyrectoSharedFlow>)receiver bufferSize:(int32_t)bufferSize __attribute__((swift_name("replay(_:bufferSize:)"))) __attribute__((unavailable("Flow analogue of 'replay(bufferSize)' is 'shareIn' with the specified replay parameter. \nreplay().connect() is the default strategy (no extra call is needed), \nreplay().autoConnect() translates to 'started = SharingStared.Lazily' argument, \nreplay().refCount() translates to 'started = SharingStared.WhileSubscribed()' argument.")));
+ (id<DyrectoSharedFlow>)scanFold:(id<DyrectoSharedFlow>)receiver initial:(id _Nullable)initial operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scanFold(_:initial:operation:)"))) __attribute__((unavailable("Flow has less verbose 'scan' shortcut")));
+ (id<DyrectoSharedFlow>)scanReduce:(id<DyrectoSharedFlow>)receiver operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scanReduce(_:operation:)"))) __attribute__((unavailable("'scanReduce' was renamed to 'runningReduce' to be consistent with Kotlin standard library")));
+ (id<DyrectoSharedFlow>)skip:(id<DyrectoSharedFlow>)receiver count:(int32_t)count __attribute__((swift_name("skip(_:count:)"))) __attribute__((unavailable("Flow analogue of 'skip' is 'drop'")));
+ (id<DyrectoSharedFlow>)startWith:(id<DyrectoSharedFlow>)receiver value:(id _Nullable)value __attribute__((swift_name("startWith(_:value:)"))) __attribute__((unavailable("Flow analogue of 'startWith' is 'onStart'. Use 'onStart { emit(value) }'")));
+ (id<DyrectoSharedFlow>)startWith:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other __attribute__((swift_name("startWith(_:other:)"))) __attribute__((unavailable("Flow analogue of 'startWith' is 'onStart'. Use 'onStart { emitAll(other) }'")));
+ (void)subscribe:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("subscribe(_:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (void)subscribe:(id<DyrectoSharedFlow>)receiver onEach:(id<DyrectoSharedKotlinSuspendFunction1>)onEach __attribute__((swift_name("subscribe(_:onEach:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (void)subscribe:(id<DyrectoSharedFlow>)receiver onEach:(id<DyrectoSharedKotlinSuspendFunction1>)onEach onError:(id<DyrectoSharedKotlinSuspendFunction1>)onError __attribute__((swift_name("subscribe(_:onEach:onError:)"))) __attribute__((unavailable("Use 'launchIn' with 'onEach', 'onCompletion' and 'catch' instead")));
+ (id<DyrectoSharedFlow>)subscribeOn:(id<DyrectoSharedFlow>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context __attribute__((swift_name("subscribeOn(_:context:)"))) __attribute__((unavailable("Use 'flowOn' instead")));
+ (id<DyrectoSharedFlow>)switchMap:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("switchMap(_:transform:)"))) __attribute__((unavailable("Flow analogues of 'switchMap' are 'transformLatest', 'flatMapLatest' and 'mapLatest'")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MultithreadedDispatchers_commonKt")))
@interface DyrectoSharedMultithreadedDispatchers_commonKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
 *   kotlinx.coroutines.DelicateCoroutinesApi
*/
+ (DyrectoSharedCloseableCoroutineDispatcher *)doNewSingleThreadContextName:(NSString *)name __attribute__((swift_name("doNewSingleThreadContext(name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MultithreadedDispatchersKt")))
@interface DyrectoSharedMultithreadedDispatchersKt : DyrectoSharedBase
+ (DyrectoSharedCloseableCoroutineDispatcher *)doNewFixedThreadPoolContextNThreads:(int32_t)nThreads name:(NSString *)name __attribute__((swift_name("doNewFixedThreadPoolContext(nThreads:name:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MutexKt")))
@interface DyrectoSharedMutexKt : DyrectoSharedBase
+ (id<DyrectoSharedMutex>)MutexLocked:(BOOL)locked __attribute__((swift_name("Mutex(locked:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withLock:(id<DyrectoSharedMutex>)receiver owner:(id _Nullable)owner action:(id _Nullable (^)(void))action completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withLock(_:owner:action:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("OnTimeoutKt")))
@interface DyrectoSharedOnTimeoutKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (void)onTimeout:(id<DyrectoSharedSelectBuilder>)receiver timeMillis:(int64_t)timeMillis block:(id<DyrectoSharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(_:timeMillis:block:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (void)onTimeout:(id<DyrectoSharedSelectBuilder>)receiver timeout:(int64_t)timeout block:(id<DyrectoSharedKotlinSuspendFunction0>)block __attribute__((swift_name("onTimeout(_:timeout:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PerceptualEvaluatorKt")))
@interface DyrectoSharedPerceptualEvaluatorKt : DyrectoSharedBase
+ (NSDictionary<DyrectoSharedReferenceSignal *, id<DyrectoSharedPerceptualEvaluator>> *)defaultPerceptualEvaluators __attribute__((swift_name("defaultPerceptualEvaluators()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ProduceKt")))
@interface DyrectoSharedProduceKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)awaitClose:(id<DyrectoSharedProducerScope>)receiver block:(void (^)(void))block completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("awaitClose(_:block:completionHandler:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
+ (id<DyrectoSharedReceiveChannel>)produce:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("produce(_:context:capacity:block:)")));
+ (id<DyrectoSharedReceiveChannel>)produce:(id<DyrectoSharedCoroutineScope>)receiver context:(id<DyrectoSharedKotlinCoroutineContext>)context capacity:(int32_t)capacity start:(DyrectoSharedCoroutineStart *)start onCompletion:(void (^ _Nullable)(DyrectoSharedKotlinThrowable * _Nullable))onCompletion block:(id<DyrectoSharedKotlinSuspendFunction1>)block __attribute__((swift_name("produce(_:context:capacity:start:onCompletion:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ReduceKt")))
@interface DyrectoSharedReduceKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)first:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("first(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)first:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("first(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)firstOrNull:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("firstOrNull(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)firstOrNull:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("firstOrNull(_:predicate:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)fold:(id<DyrectoSharedFlow>)receiver initial:(id _Nullable)initial operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("fold(_:initial:operation:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)last:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("last(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)lastOrNull:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("lastOrNull(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)reduce:(id<DyrectoSharedFlow>)receiver operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("reduce(_:operation:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)single:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("single(_:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)singleOrNull:(id<DyrectoSharedFlow>)receiver completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("singleOrNull(_:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RunnableKt")))
@interface DyrectoSharedRunnableKt : DyrectoSharedBase
+ (id<DyrectoSharedRunnable>)RunnableBlock:(void (^)(void))block __attribute__((swift_name("Runnable(block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SelectKt")))
@interface DyrectoSharedSelectKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)selectBuilder:(void (^)(id<DyrectoSharedSelectBuilder>))builder completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("select(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SelectUnbiasedKt")))
@interface DyrectoSharedSelectUnbiasedKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)selectUnbiasedBuilder:(void (^)(id<DyrectoSharedSelectBuilder>))builder completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("selectUnbiased(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SemaphoreKt")))
@interface DyrectoSharedSemaphoreKt : DyrectoSharedBase
+ (id<DyrectoSharedSemaphore>)SemaphorePermits:(int32_t)permits acquiredPermits:(int32_t)acquiredPermits __attribute__((swift_name("Semaphore(permits:acquiredPermits:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withPermit:(id<DyrectoSharedSemaphore>)receiver action:(id _Nullable (^)(void))action completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withPermit(_:action:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShareKt")))
@interface DyrectoSharedShareKt : DyrectoSharedBase
+ (id<DyrectoSharedSharedFlow>)asSharedFlow:(id<DyrectoSharedMutableSharedFlow>)receiver __attribute__((swift_name("asSharedFlow(_:)")));
+ (id<DyrectoSharedStateFlow>)asStateFlow:(id<DyrectoSharedMutableStateFlow>)receiver __attribute__((swift_name("asStateFlow(_:)")));
+ (id<DyrectoSharedSharedFlow>)onSubscription:(id<DyrectoSharedSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action __attribute__((swift_name("onSubscription(_:action:)")));
+ (id<DyrectoSharedSharedFlow>)shareIn:(id<DyrectoSharedFlow>)receiver scope:(id<DyrectoSharedCoroutineScope>)scope started:(id<DyrectoSharedSharingStarted>)started replay:(int32_t)replay __attribute__((swift_name("shareIn(_:scope:started:replay:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)stateIn:(id<DyrectoSharedFlow>)receiver scope:(id<DyrectoSharedCoroutineScope>)scope completionHandler:(void (^)(id<DyrectoSharedStateFlow> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("stateIn(_:scope:completionHandler:)")));
+ (id<DyrectoSharedStateFlow>)stateIn:(id<DyrectoSharedFlow>)receiver scope:(id<DyrectoSharedCoroutineScope>)scope started:(id<DyrectoSharedSharingStarted>)started initialValue:(id _Nullable)initialValue __attribute__((swift_name("stateIn(_:scope:started:initialValue:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SharedFlowKt")))
@interface DyrectoSharedSharedFlowKt : DyrectoSharedBase
+ (id<DyrectoSharedMutableSharedFlow>)MutableSharedFlowReplay:(int32_t)replay extraBufferCapacity:(int32_t)extraBufferCapacity onBufferOverflow:(DyrectoSharedBufferOverflow *)onBufferOverflow __attribute__((swift_name("MutableSharedFlow(replay:extraBufferCapacity:onBufferOverflow:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StateFlowKt")))
@interface DyrectoSharedStateFlowKt : DyrectoSharedBase
+ (id<DyrectoSharedMutableStateFlow>)MutableStateFlowValue:(id _Nullable)value __attribute__((swift_name("MutableStateFlow(value:)")));
+ (id _Nullable)getAndUpdate:(id<DyrectoSharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("getAndUpdate(_:function:)")));
+ (void)update:(id<DyrectoSharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("update(_:function:)")));
+ (id _Nullable)updateAndGet:(id<DyrectoSharedMutableStateFlow>)receiver function:(id _Nullable (^)(id _Nullable))function __attribute__((swift_name("updateAndGet(_:function:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("StateFlowFactoryKt")))
@interface DyrectoSharedStateFlowFactoryKt : DyrectoSharedBase
+ (id<DyrectoSharedMutableStateFlow>)mutableStateFlowInitial:(BOOL)initial __attribute__((swift_name("mutableStateFlow(initial:)")));
+ (id<DyrectoSharedMutableStateFlow>)mutableStateFlowAnyInitial:(id _Nullable)initial __attribute__((swift_name("mutableStateFlowAny(initial:)")));
+ (id<DyrectoSharedMutableStateFlow>)mutableStateFlowIntInitial:(int32_t)initial __attribute__((swift_name("mutableStateFlowInt(initial:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SupervisorKt")))
@interface DyrectoSharedSupervisorKt : DyrectoSharedBase
+ (id<DyrectoSharedCompletableJob>)SupervisorJobParent:(id<DyrectoSharedJob> _Nullable)parent __attribute__((swift_name("SupervisorJob(parent:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)supervisorScopeBlock:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("supervisorScope(block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Synchronized_commonKt")))
@interface DyrectoSharedSynchronized_commonKt : DyrectoSharedBase
+ (id _Nullable)synchronizedLock:(DyrectoSharedAtomicfuSynchronizedObject *)lock block:(id _Nullable (^)(void))block __attribute__((swift_name("synchronized(lock:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SynchronizedKt")))
@interface DyrectoSharedSynchronizedKt : DyrectoSharedBase
+ (id _Nullable)synchronizedImplLock:(DyrectoSharedAtomicfuSynchronizedObject *)lock block:(id _Nullable (^)(void))block __attribute__((swift_name("synchronizedImpl(lock:block:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeKt")))
@interface DyrectoSharedTimeKt : DyrectoSharedBase
+ (int64_t)epochMillis __attribute__((swift_name("epochMillis()")));
+ (int64_t)nanoTime __attribute__((swift_name("nanoTime()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeoutKt")))
@interface DyrectoSharedTimeoutKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutTimeMillis:(int64_t)timeMillis block:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeout(timeMillis:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutTimeout:(int64_t)timeout block:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeout(timeout:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutOrNullTimeMillis:(int64_t)timeMillis block:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeoutOrNull(timeMillis:block:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)withTimeoutOrNullTimeout:(int64_t)timeout block:(id<DyrectoSharedKotlinSuspendFunction1>)block completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("withTimeoutOrNull(timeout:block:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TransformKt")))
@interface DyrectoSharedTransformKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)filter:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("filter(_:predicate:)")));
+ (id<DyrectoSharedFlow>)filterIsInstance:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("filterIsInstance(_:)")));
+ (id<DyrectoSharedFlow>)filterIsInstance:(id<DyrectoSharedFlow>)receiver klass:(id<DyrectoSharedKotlinKClass>)klass __attribute__((swift_name("filterIsInstance(_:klass:)")));
+ (id<DyrectoSharedFlow>)filterNot:(id<DyrectoSharedFlow>)receiver predicate:(id<DyrectoSharedKotlinSuspendFunction1>)predicate __attribute__((swift_name("filterNot(_:predicate:)")));
+ (id<DyrectoSharedFlow>)filterNotNull:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("filterNotNull(_:)")));
+ (id<DyrectoSharedFlow>)map:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("map(_:transform:)")));
+ (id<DyrectoSharedFlow>)mapNotNull:(id<DyrectoSharedFlow>)receiver transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("mapNotNull(_:transform:)")));
+ (id<DyrectoSharedFlow>)onEach:(id<DyrectoSharedFlow>)receiver action:(id<DyrectoSharedKotlinSuspendFunction1>)action __attribute__((swift_name("onEach(_:action:)")));
+ (id<DyrectoSharedFlow>)runningFold:(id<DyrectoSharedFlow>)receiver initial:(id _Nullable)initial operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation __attribute__((swift_name("runningFold(_:initial:operation:)")));
+ (id<DyrectoSharedFlow>)runningReduce:(id<DyrectoSharedFlow>)receiver operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation __attribute__((swift_name("runningReduce(_:operation:)")));
+ (id<DyrectoSharedFlow>)scan:(id<DyrectoSharedFlow>)receiver initial:(id _Nullable)initial operation:(id<DyrectoSharedKotlinSuspendFunction2>)operation __attribute__((swift_name("scan(_:initial:operation:)")));
+ (id<DyrectoSharedFlow>)withIndex:(id<DyrectoSharedFlow>)receiver __attribute__((swift_name("withIndex(_:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WhileSelectKt")))
@interface DyrectoSharedWhileSelectKt : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)whileSelectBuilder:(void (^)(id<DyrectoSharedSelectBuilder>))builder completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("whileSelect(builder:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("YieldKt")))
@interface DyrectoSharedYieldKt : DyrectoSharedBase

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
+ (void)yieldWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("yield(completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ZipKt")))
@interface DyrectoSharedZipKt : DyrectoSharedBase
+ (id<DyrectoSharedFlow>)combineFlows:(DyrectoSharedKotlinArray<id<DyrectoSharedFlow>> *)flows transform:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("combine(flows:transform:)")));
+ (id<DyrectoSharedFlow>)combineFlows:(id)flows transform_:(id<DyrectoSharedKotlinSuspendFunction1>)transform __attribute__((swift_name("combine(flows:transform_:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmName(name="flowCombine")
*/
+ (id<DyrectoSharedFlow>)combine:(id<DyrectoSharedFlow>)receiver flow:(id<DyrectoSharedFlow>)flow transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combine(_:flow:transform:)")));
+ (id<DyrectoSharedFlow>)combineFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combine(flow:flow2:transform:)")));
+ (id<DyrectoSharedFlow>)combineFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 transform:(id<DyrectoSharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combine(flow:flow2:flow3:transform:)")));
+ (id<DyrectoSharedFlow>)combineFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 flow4:(id<DyrectoSharedFlow>)flow4 transform:(id<DyrectoSharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combine(flow:flow2:flow3:flow4:transform:)")));
+ (id<DyrectoSharedFlow>)combineFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 flow4:(id<DyrectoSharedFlow>)flow4 flow5:(id<DyrectoSharedFlow>)flow5 transform:(id<DyrectoSharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combine(flow:flow2:flow3:flow4:flow5:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlows:(DyrectoSharedKotlinArray<id<DyrectoSharedFlow>> *)flows transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineTransform(flows:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlows:(id)flows transform_:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("combineTransform(flows:transform_:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmName(name="flowCombineTransform")
*/
+ (id<DyrectoSharedFlow>)combineTransform:(id<DyrectoSharedFlow>)receiver flow:(id<DyrectoSharedFlow>)flow transform:(id<DyrectoSharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineTransform(_:flow:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 transform:(id<DyrectoSharedKotlinSuspendFunction3>)transform __attribute__((swift_name("combineTransform(flow:flow2:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 transform:(id<DyrectoSharedKotlinSuspendFunction4>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 flow4:(id<DyrectoSharedFlow>)flow4 transform:(id<DyrectoSharedKotlinSuspendFunction5>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:flow4:transform:)")));
+ (id<DyrectoSharedFlow>)combineTransformFlow:(id<DyrectoSharedFlow>)flow flow2:(id<DyrectoSharedFlow>)flow2 flow3:(id<DyrectoSharedFlow>)flow3 flow4:(id<DyrectoSharedFlow>)flow4 flow5:(id<DyrectoSharedFlow>)flow5 transform:(id<DyrectoSharedKotlinSuspendFunction6>)transform __attribute__((swift_name("combineTransform(flow:flow2:flow3:flow4:flow5:transform:)")));
+ (id<DyrectoSharedFlow>)zip:(id<DyrectoSharedFlow>)receiver other:(id<DyrectoSharedFlow>)other transform:(id<DyrectoSharedKotlinSuspendFunction2>)transform __attribute__((swift_name("zip(_:other:transform:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinEnumCompanion")))
@interface DyrectoSharedKotlinEnumCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinEnumCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreSerializationStrategy")))
@protocol DyrectoSharedKotlinx_serialization_coreSerializationStrategy
@required
- (void)serializeEncoder:(id<DyrectoSharedKotlinx_serialization_coreEncoder>)encoder value:(id _Nullable)value __attribute__((swift_name("serialize(encoder:value:)")));
@property (readonly) id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor> descriptor __attribute__((swift_name("descriptor")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreDeserializationStrategy")))
@protocol DyrectoSharedKotlinx_serialization_coreDeserializationStrategy
@required
- (id _Nullable)deserializeDecoder:(id<DyrectoSharedKotlinx_serialization_coreDecoder>)decoder __attribute__((swift_name("deserialize(decoder:)")));
@property (readonly) id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor> descriptor __attribute__((swift_name("descriptor")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreKSerializer")))
@protocol DyrectoSharedKotlinx_serialization_coreKSerializer <DyrectoSharedKotlinx_serialization_coreSerializationStrategy, DyrectoSharedKotlinx_serialization_coreDeserializationStrategy>
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinByteArray")))
@interface DyrectoSharedKotlinByteArray : DyrectoSharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(DyrectoSharedByte *(^)(DyrectoSharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int8_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (DyrectoSharedKotlinByteIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int8_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinFloatArray")))
@interface DyrectoSharedKotlinFloatArray : DyrectoSharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(DyrectoSharedFloat *(^)(DyrectoSharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (float)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (DyrectoSharedKotlinFloatIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(float)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinPair")))
@interface DyrectoSharedKotlinPair<__covariant A, __covariant B> : DyrectoSharedBase
- (instancetype)initWithFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("init(first:second:)"))) __attribute__((objc_designated_initializer));
- (DyrectoSharedKotlinPair<A, B> *)doCopyFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("doCopy(first:second:)")));
- (BOOL)equalsOther:(id _Nullable)other __attribute__((swift_name("equals(other:)")));
- (int32_t)hashCode __attribute__((swift_name("hashCode()")));
- (NSString *)toString __attribute__((swift_name("toString()")));
@property (readonly) A _Nullable first __attribute__((swift_name("first")));
@property (readonly) B _Nullable second __attribute__((swift_name("second")));
@end

__attribute__((swift_name("KotlinSequence")))
@protocol DyrectoSharedKotlinSequence
@required
- (id<DyrectoSharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
@end

__attribute__((swift_name("KotlinFunction")))
@protocol DyrectoSharedKotlinFunction
@required
@end

__attribute__((swift_name("KotlinSuspendFunction1")))
@protocol DyrectoSharedKotlinSuspendFunction1 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicfuSynchronizedObject.LockState")))
@interface DyrectoSharedAtomicfuSynchronizedObjectLockState : DyrectoSharedBase
- (instancetype)initWithStatus:(DyrectoSharedAtomicfuSynchronizedObjectStatus *)status nestedLocks:(int32_t)nestedLocks waiters:(int32_t)waiters ownerThreadId:(void * _Nullable)ownerThreadId mutex:(void * _Nullable)mutex __attribute__((swift_name("init(status:nestedLocks:waiters:ownerThreadId:mutex:)"))) __attribute__((objc_designated_initializer));
@property (readonly) void * _Nullable mutex __attribute__((swift_name("mutex")));
@property (readonly) int32_t nestedLocks __attribute__((swift_name("nestedLocks")));
@property (readonly) void * _Nullable ownerThreadId __attribute__((swift_name("ownerThreadId")));
@property (readonly) DyrectoSharedAtomicfuSynchronizedObjectStatus *status __attribute__((swift_name("status")));
@property (readonly) int32_t waiters __attribute__((swift_name("waiters")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.9")
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinAtomicReference")))
@interface DyrectoSharedKotlinAtomicReference<T> : DyrectoSharedBase
- (instancetype)initWithValue:(T _Nullable)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer));
- (T _Nullable)compareAndExchangeExpected:(T _Nullable)expected newValue:(T _Nullable)newValue __attribute__((swift_name("compareAndExchange(expected:newValue:)")));
- (BOOL)compareAndSetExpected:(T _Nullable)expected newValue:(T _Nullable)newValue __attribute__((swift_name("compareAndSet(expected:newValue:)")));
- (T _Nullable)getAndSetNewValue:(T _Nullable)newValue __attribute__((swift_name("getAndSet(newValue:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property T _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("KotlinSuspendFunction0")))
@protocol DyrectoSharedKotlinSuspendFunction0 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeWithCompletionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinUnit")))
@interface DyrectoSharedKotlinUnit : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)unit __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinUnit *shared __attribute__((swift_name("shared")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((swift_name("KotlinIterator")))
@protocol DyrectoSharedKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

__attribute__((swift_name("KotlinIntIterator")))
@interface DyrectoSharedKotlinIntIterator : DyrectoSharedBase <DyrectoSharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedInt *)next __attribute__((swift_name("next()")));
- (int32_t)nextInt __attribute__((swift_name("nextInt()")));
@end

__attribute__((swift_name("KotlinLongIterator")))
@interface DyrectoSharedKotlinLongIterator : DyrectoSharedBase <DyrectoSharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedLong *)next __attribute__((swift_name("next()")));
- (int64_t)nextLong __attribute__((swift_name("nextLong()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntProgression.Companion")))
@interface DyrectoSharedKotlinIntProgressionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinIntProgressionCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedKotlinIntProgression *)fromClosedRangeRangeStart:(int32_t)rangeStart rangeEnd:(int32_t)rangeEnd step:(int32_t)step __attribute__((swift_name("fromClosedRange(rangeStart:rangeEnd:step:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinIntRange.Companion")))
@interface DyrectoSharedKotlinIntRangeCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinIntRangeCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedKotlinIntRange *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongProgression.Companion")))
@interface DyrectoSharedKotlinLongProgressionCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinLongProgressionCompanion *shared __attribute__((swift_name("shared")));
- (DyrectoSharedKotlinLongProgression *)fromClosedRangeRangeStart:(int64_t)rangeStart rangeEnd:(int64_t)rangeEnd step:(int64_t)step __attribute__((swift_name("fromClosedRange(rangeStart:rangeEnd:step:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinLongRange.Companion")))
@interface DyrectoSharedKotlinLongRangeCompanion : DyrectoSharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) DyrectoSharedKotlinLongRangeCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) DyrectoSharedKotlinLongRange *EMPTY __attribute__((swift_name("EMPTY")));
@end

__attribute__((swift_name("KotlinSuspendFunction2")))
@protocol DyrectoSharedKotlinSuspendFunction2 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinNothing")))
@interface DyrectoSharedKotlinNothing : DyrectoSharedBase
@end

__attribute__((swift_name("KotlinSuspendFunction3")))
@protocol DyrectoSharedKotlinSuspendFunction3 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:completionHandler:)")));
@end

__attribute__((swift_name("KotlinSuspendFunction4")))
@protocol DyrectoSharedKotlinSuspendFunction4 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:completionHandler:)")));
@end

__attribute__((swift_name("KotlinSuspendFunction5")))
@protocol DyrectoSharedKotlinSuspendFunction5 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 p5:(id _Nullable)p5 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:p5:completionHandler:)")));
@end

__attribute__((swift_name("KotlinKDeclarationContainer")))
@protocol DyrectoSharedKotlinKDeclarationContainer
@required
@end

__attribute__((swift_name("KotlinKAnnotatedElement")))
@protocol DyrectoSharedKotlinKAnnotatedElement
@required
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
__attribute__((swift_name("KotlinKClassifier")))
@protocol DyrectoSharedKotlinKClassifier
@required
@end

__attribute__((swift_name("KotlinKClass")))
@protocol DyrectoSharedKotlinKClass <DyrectoSharedKotlinKDeclarationContainer, DyrectoSharedKotlinKAnnotatedElement, DyrectoSharedKotlinKClassifier>
@required

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
- (BOOL)isInstanceValue:(id _Nullable)value __attribute__((swift_name("isInstance(value:)")));
@property (readonly) NSString * _Nullable qualifiedName __attribute__((swift_name("qualifiedName")));
@property (readonly) NSString * _Nullable simpleName __attribute__((swift_name("simpleName")));
@end

__attribute__((swift_name("KotlinSuspendFunction6")))
@protocol DyrectoSharedKotlinSuspendFunction6 <DyrectoSharedKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 p3:(id _Nullable)p3 p4:(id _Nullable)p4 p5:(id _Nullable)p5 p6:(id _Nullable)p6 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:p3:p4:p5:p6:completionHandler:)")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreEncoder")))
@protocol DyrectoSharedKotlinx_serialization_coreEncoder
@required
- (id<DyrectoSharedKotlinx_serialization_coreCompositeEncoder>)beginCollectionDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor collectionSize:(int32_t)collectionSize __attribute__((swift_name("beginCollection(descriptor:collectionSize:)")));
- (id<DyrectoSharedKotlinx_serialization_coreCompositeEncoder>)beginStructureDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("beginStructure(descriptor:)")));
- (void)encodeBooleanValue:(BOOL)value __attribute__((swift_name("encodeBoolean(value:)")));
- (void)encodeByteValue:(int8_t)value __attribute__((swift_name("encodeByte(value:)")));
- (void)encodeCharValue:(unichar)value __attribute__((swift_name("encodeChar(value:)")));
- (void)encodeDoubleValue:(double)value __attribute__((swift_name("encodeDouble(value:)")));
- (void)encodeEnumEnumDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)enumDescriptor index:(int32_t)index __attribute__((swift_name("encodeEnum(enumDescriptor:index:)")));
- (void)encodeFloatValue:(float)value __attribute__((swift_name("encodeFloat(value:)")));
- (id<DyrectoSharedKotlinx_serialization_coreEncoder>)encodeInlineDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("encodeInline(descriptor:)")));
- (void)encodeIntValue:(int32_t)value __attribute__((swift_name("encodeInt(value:)")));
- (void)encodeLongValue:(int64_t)value __attribute__((swift_name("encodeLong(value:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (void)encodeNotNullMark __attribute__((swift_name("encodeNotNullMark()")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (void)encodeNull __attribute__((swift_name("encodeNull()")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (void)encodeNullableSerializableValueSerializer:(id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeNullableSerializableValue(serializer:value:)")));
- (void)encodeSerializableValueSerializer:(id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeSerializableValue(serializer:value:)")));
- (void)encodeShortValue:(int16_t)value __attribute__((swift_name("encodeShort(value:)")));
- (void)encodeStringValue:(NSString *)value __attribute__((swift_name("encodeString(value:)")));
@property (readonly) DyrectoSharedKotlinx_serialization_coreSerializersModule *serializersModule __attribute__((swift_name("serializersModule")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreSerialDescriptor")))
@protocol DyrectoSharedKotlinx_serialization_coreSerialDescriptor
@required

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (NSArray<id<DyrectoSharedKotlinAnnotation>> *)getElementAnnotationsIndex:(int32_t)index __attribute__((swift_name("getElementAnnotations(index:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)getElementDescriptorIndex:(int32_t)index __attribute__((swift_name("getElementDescriptor(index:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (int32_t)getElementIndexName:(NSString *)name __attribute__((swift_name("getElementIndex(name:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (NSString *)getElementNameIndex:(int32_t)index __attribute__((swift_name("getElementName(index:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (BOOL)isElementOptionalIndex:(int32_t)index __attribute__((swift_name("isElementOptional(index:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
@property (readonly) NSArray<id<DyrectoSharedKotlinAnnotation>> *annotations __attribute__((swift_name("annotations")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
@property (readonly) int32_t elementsCount __attribute__((swift_name("elementsCount")));
@property (readonly) BOOL isInline __attribute__((swift_name("isInline")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
@property (readonly) BOOL isNullable __attribute__((swift_name("isNullable")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
@property (readonly) DyrectoSharedKotlinx_serialization_coreSerialKind *kind __attribute__((swift_name("kind")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
@property (readonly) NSString *serialName __attribute__((swift_name("serialName")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreDecoder")))
@protocol DyrectoSharedKotlinx_serialization_coreDecoder
@required
- (id<DyrectoSharedKotlinx_serialization_coreCompositeDecoder>)beginStructureDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("beginStructure(descriptor:)")));
- (BOOL)decodeBoolean __attribute__((swift_name("decodeBoolean()")));
- (int8_t)decodeByte __attribute__((swift_name("decodeByte()")));
- (unichar)decodeChar __attribute__((swift_name("decodeChar()")));
- (double)decodeDouble __attribute__((swift_name("decodeDouble()")));
- (int32_t)decodeEnumEnumDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)enumDescriptor __attribute__((swift_name("decodeEnum(enumDescriptor:)")));
- (float)decodeFloat __attribute__((swift_name("decodeFloat()")));
- (id<DyrectoSharedKotlinx_serialization_coreDecoder>)decodeInlineDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("decodeInline(descriptor:)")));
- (int32_t)decodeInt __attribute__((swift_name("decodeInt()")));
- (int64_t)decodeLong __attribute__((swift_name("decodeLong()")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (BOOL)decodeNotNullMark __attribute__((swift_name("decodeNotNullMark()")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (DyrectoSharedKotlinNothing * _Nullable)decodeNull __attribute__((swift_name("decodeNull()")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id _Nullable)decodeNullableSerializableValueDeserializer:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy>)deserializer __attribute__((swift_name("decodeNullableSerializableValue(deserializer:)")));
- (id _Nullable)decodeSerializableValueDeserializer:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy>)deserializer __attribute__((swift_name("decodeSerializableValue(deserializer:)")));
- (int16_t)decodeShort __attribute__((swift_name("decodeShort()")));
- (NSString *)decodeString __attribute__((swift_name("decodeString()")));
@property (readonly) DyrectoSharedKotlinx_serialization_coreSerializersModule *serializersModule __attribute__((swift_name("serializersModule")));
@end

__attribute__((swift_name("KotlinByteIterator")))
@interface DyrectoSharedKotlinByteIterator : DyrectoSharedBase <DyrectoSharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedByte *)next __attribute__((swift_name("next()")));
- (int8_t)nextByte __attribute__((swift_name("nextByte()")));
@end

__attribute__((swift_name("KotlinFloatIterator")))
@interface DyrectoSharedKotlinFloatIterator : DyrectoSharedBase <DyrectoSharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (DyrectoSharedFloat *)next __attribute__((swift_name("next()")));
- (float)nextFloat __attribute__((swift_name("nextFloat()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AtomicfuSynchronizedObject.Status")))
@interface DyrectoSharedAtomicfuSynchronizedObjectStatus : DyrectoSharedKotlinEnum<DyrectoSharedAtomicfuSynchronizedObjectStatus *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) DyrectoSharedAtomicfuSynchronizedObjectStatus *unlocked __attribute__((swift_name("unlocked")));
@property (class, readonly) DyrectoSharedAtomicfuSynchronizedObjectStatus *thin __attribute__((swift_name("thin")));
@property (class, readonly) DyrectoSharedAtomicfuSynchronizedObjectStatus *fat __attribute__((swift_name("fat")));
+ (DyrectoSharedKotlinArray<DyrectoSharedAtomicfuSynchronizedObjectStatus *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<DyrectoSharedAtomicfuSynchronizedObjectStatus *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreCompositeEncoder")))
@protocol DyrectoSharedKotlinx_serialization_coreCompositeEncoder
@required
- (void)encodeBooleanElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(BOOL)value __attribute__((swift_name("encodeBooleanElement(descriptor:index:value:)")));
- (void)encodeByteElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(int8_t)value __attribute__((swift_name("encodeByteElement(descriptor:index:value:)")));
- (void)encodeCharElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(unichar)value __attribute__((swift_name("encodeCharElement(descriptor:index:value:)")));
- (void)encodeDoubleElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(double)value __attribute__((swift_name("encodeDoubleElement(descriptor:index:value:)")));
- (void)encodeFloatElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(float)value __attribute__((swift_name("encodeFloatElement(descriptor:index:value:)")));
- (id<DyrectoSharedKotlinx_serialization_coreEncoder>)encodeInlineElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("encodeInlineElement(descriptor:index:)")));
- (void)encodeIntElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(int32_t)value __attribute__((swift_name("encodeIntElement(descriptor:index:value:)")));
- (void)encodeLongElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(int64_t)value __attribute__((swift_name("encodeLongElement(descriptor:index:value:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (void)encodeNullableSerializableElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index serializer:(id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeNullableSerializableElement(descriptor:index:serializer:value:)")));
- (void)encodeSerializableElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index serializer:(id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeSerializableElement(descriptor:index:serializer:value:)")));
- (void)encodeShortElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(int16_t)value __attribute__((swift_name("encodeShortElement(descriptor:index:value:)")));
- (void)encodeStringElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index value:(NSString *)value __attribute__((swift_name("encodeStringElement(descriptor:index:value:)")));
- (void)endStructureDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("endStructure(descriptor:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (BOOL)shouldEncodeElementDefaultDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("shouldEncodeElementDefault(descriptor:index:)")));
@property (readonly) DyrectoSharedKotlinx_serialization_coreSerializersModule *serializersModule __attribute__((swift_name("serializersModule")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreSerializersModule")))
@interface DyrectoSharedKotlinx_serialization_coreSerializersModule : DyrectoSharedBase

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (void)dumpToCollector:(id<DyrectoSharedKotlinx_serialization_coreSerializersModuleCollector>)collector __attribute__((swift_name("dumpTo(collector:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id<DyrectoSharedKotlinx_serialization_coreKSerializer> _Nullable)getContextualKClass:(id<DyrectoSharedKotlinKClass>)kClass typeArgumentsSerializers:(NSArray<id<DyrectoSharedKotlinx_serialization_coreKSerializer>> *)typeArgumentsSerializers __attribute__((swift_name("getContextual(kClass:typeArgumentsSerializers:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy> _Nullable)getPolymorphicBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass value:(id)value __attribute__((swift_name("getPolymorphic(baseClass:value:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy> _Nullable)getPolymorphicBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass serializedClassName:(NSString * _Nullable)serializedClassName __attribute__((swift_name("getPolymorphic(baseClass:serializedClassName:)")));
@end

__attribute__((swift_name("KotlinAnnotation")))
@protocol DyrectoSharedKotlinAnnotation
@required
@end


/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
__attribute__((swift_name("Kotlinx_serialization_coreSerialKind")))
@interface DyrectoSharedKotlinx_serialization_coreSerialKind : DyrectoSharedBase
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((swift_name("Kotlinx_serialization_coreCompositeDecoder")))
@protocol DyrectoSharedKotlinx_serialization_coreCompositeDecoder
@required
- (BOOL)decodeBooleanElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeBooleanElement(descriptor:index:)")));
- (int8_t)decodeByteElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeByteElement(descriptor:index:)")));
- (unichar)decodeCharElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeCharElement(descriptor:index:)")));
- (int32_t)decodeCollectionSizeDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("decodeCollectionSize(descriptor:)")));
- (double)decodeDoubleElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeDoubleElement(descriptor:index:)")));
- (int32_t)decodeElementIndexDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("decodeElementIndex(descriptor:)")));
- (float)decodeFloatElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeFloatElement(descriptor:index:)")));
- (id<DyrectoSharedKotlinx_serialization_coreDecoder>)decodeInlineElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeInlineElement(descriptor:index:)")));
- (int32_t)decodeIntElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeIntElement(descriptor:index:)")));
- (int64_t)decodeLongElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeLongElement(descriptor:index:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (id _Nullable)decodeNullableSerializableElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy>)deserializer previousValue:(id _Nullable)previousValue __attribute__((swift_name("decodeNullableSerializableElement(descriptor:index:deserializer:previousValue:)")));

/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
- (BOOL)decodeSequentially __attribute__((swift_name("decodeSequentially()")));
- (id _Nullable)decodeSerializableElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy>)deserializer previousValue:(id _Nullable)previousValue __attribute__((swift_name("decodeSerializableElement(descriptor:index:deserializer:previousValue:)")));
- (int16_t)decodeShortElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeShortElement(descriptor:index:)")));
- (NSString *)decodeStringElementDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeStringElement(descriptor:index:)")));
- (void)endStructureDescriptor:(id<DyrectoSharedKotlinx_serialization_coreSerialDescriptor>)descriptor __attribute__((swift_name("endStructure(descriptor:)")));
@property (readonly) DyrectoSharedKotlinx_serialization_coreSerializersModule *serializersModule __attribute__((swift_name("serializersModule")));
@end


/**
 * @note annotations
 *   kotlinx.serialization.ExperimentalSerializationApi
*/
__attribute__((swift_name("Kotlinx_serialization_coreSerializersModuleCollector")))
@protocol DyrectoSharedKotlinx_serialization_coreSerializersModuleCollector
@required
- (void)contextualKClass:(id<DyrectoSharedKotlinKClass>)kClass provider:(id<DyrectoSharedKotlinx_serialization_coreKSerializer> (^)(NSArray<id<DyrectoSharedKotlinx_serialization_coreKSerializer>> *))provider __attribute__((swift_name("contextual(kClass:provider:)")));
- (void)contextualKClass:(id<DyrectoSharedKotlinKClass>)kClass serializer:(id<DyrectoSharedKotlinx_serialization_coreKSerializer>)serializer __attribute__((swift_name("contextual(kClass:serializer:)")));
- (void)polymorphicBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass actualClass:(id<DyrectoSharedKotlinKClass>)actualClass actualSerializer:(id<DyrectoSharedKotlinx_serialization_coreKSerializer>)actualSerializer __attribute__((swift_name("polymorphic(baseClass:actualClass:actualSerializer:)")));
- (void)polymorphicDefaultBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass defaultDeserializerProvider:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy> _Nullable (^)(NSString * _Nullable))defaultDeserializerProvider __attribute__((swift_name("polymorphicDefault(baseClass:defaultDeserializerProvider:)"))) __attribute__((deprecated("Deprecated in favor of function with more precise name: polymorphicDefaultDeserializer")));
- (void)polymorphicDefaultDeserializerBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass defaultDeserializerProvider:(id<DyrectoSharedKotlinx_serialization_coreDeserializationStrategy> _Nullable (^)(NSString * _Nullable))defaultDeserializerProvider __attribute__((swift_name("polymorphicDefaultDeserializer(baseClass:defaultDeserializerProvider:)")));
- (void)polymorphicDefaultSerializerBaseClass:(id<DyrectoSharedKotlinKClass>)baseClass defaultSerializerProvider:(id<DyrectoSharedKotlinx_serialization_coreSerializationStrategy> _Nullable (^)(id))defaultSerializerProvider __attribute__((swift_name("polymorphicDefaultSerializer(baseClass:defaultSerializerProvider:)")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
