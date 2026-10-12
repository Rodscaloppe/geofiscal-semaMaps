#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class SharedCameraStatus, SharedCaptureResult, SharedGeoCalculations, SharedKotlinArray<T>, SharedKotlinByteArray, SharedKotlinByteIterator, SharedKotlinEnum<E>, SharedKotlinEnumCompanion, SharedKotlinException, SharedKotlinIllegalStateException, SharedKotlinPair<__covariant A, __covariant B>, SharedKotlinRuntimeException, SharedKotlinThrowable, SharedLocationData, SharedMapLayer, SharedMapMarker, SharedMapRegion, SharedMarkerSeverity, SharedMarkerType, SharedSensorTelemetryData;

@protocol SharedKotlinComparable, SharedKotlinIterator, SharedKotlinx_coroutines_coreFlow, SharedKotlinx_coroutines_coreFlowCollector, SharedKotlinx_coroutines_coreSharedFlow, SharedKotlinx_coroutines_coreStateFlow;

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
@interface SharedBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface SharedBase (SharedBaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface SharedMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface SharedMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorSharedKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface SharedNumber : NSNumber
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
@interface SharedByte : SharedNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface SharedUByte : SharedNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface SharedShort : SharedNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface SharedUShort : SharedNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface SharedInt : SharedNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface SharedUInt : SharedNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface SharedLong : SharedNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface SharedULong : SharedNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface SharedFloat : SharedNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface SharedDouble : SharedNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface SharedBoolean : SharedNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("GeoCalculations")))
@interface SharedGeoCalculations : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)geoCalculations __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedGeoCalculations *shared __attribute__((swift_name("shared")));
- (double)bearingBetweenLat1:(double)lat1 lon1:(double)lon1 lat2:(double)lat2 lon2:(double)lon2 __attribute__((swift_name("bearingBetween(lat1:lon1:lat2:lon2:)")));
- (double)calculatePolygonAreaHectaresVertices:(NSArray<SharedKotlinPair<SharedDouble *, SharedDouble *> *> *)vertices __attribute__((swift_name("calculatePolygonAreaHectares(vertices:)")));
- (double)calculateTrajectoryDistanceKmPoints:(NSArray<SharedKotlinPair<SharedDouble *, SharedDouble *> *> *)points __attribute__((swift_name("calculateTrajectoryDistanceKm(points:)")));
- (double)haversineDistanceKmLat1:(double)lat1 lon1:(double)lon1 lat2:(double)lat2 lon2:(double)lon2 __attribute__((swift_name("haversineDistanceKm(lat1:lon1:lat2:lon2:)")));
- (double)haversineDistanceMetersLat1:(double)lat1 lon1:(double)lon1 lat2:(double)lat2 lon2:(double)lon2 __attribute__((swift_name("haversineDistanceMeters(lat1:lon1:lat2:lon2:)")));
- (BOOL)isValidCarNumberCar:(NSString *)car __attribute__((swift_name("isValidCarNumber(car:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CameraStatus")))
@interface SharedCameraStatus : SharedBase
- (instancetype)initWithIsInitialized:(BOOL)isInitialized isTakingPicture:(BOOL)isTakingPicture isFrontCamera:(BOOL)isFrontCamera hasFlash:(BOOL)hasFlash isFlashOn:(BOOL)isFlashOn errorMessage:(NSString * _Nullable)errorMessage __attribute__((swift_name("init(isInitialized:isTakingPicture:isFrontCamera:hasFlash:isFlashOn:errorMessage:)"))) __attribute__((objc_designated_initializer));
- (SharedCameraStatus *)doCopyIsInitialized:(BOOL)isInitialized isTakingPicture:(BOOL)isTakingPicture isFrontCamera:(BOOL)isFrontCamera hasFlash:(BOOL)hasFlash isFlashOn:(BOOL)isFlashOn errorMessage:(NSString * _Nullable)errorMessage __attribute__((swift_name("doCopy(isInitialized:isTakingPicture:isFrontCamera:hasFlash:isFlashOn:errorMessage:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable errorMessage __attribute__((swift_name("errorMessage")));
@property (readonly) BOOL hasFlash __attribute__((swift_name("hasFlash")));
@property (readonly) BOOL isFlashOn __attribute__((swift_name("isFlashOn")));
@property (readonly) BOOL isFrontCamera __attribute__((swift_name("isFrontCamera")));
@property (readonly) BOOL isInitialized __attribute__((swift_name("isInitialized")));
@property (readonly) BOOL isTakingPicture __attribute__((swift_name("isTakingPicture")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CaptureResult")))
@interface SharedCaptureResult : SharedBase
- (instancetype)initWithImageBytes:(SharedKotlinByteArray *)imageBytes width:(int32_t)width height:(int32_t)height rotationDegrees:(int32_t)rotationDegrees __attribute__((swift_name("init(imageBytes:width:height:rotationDegrees:)"))) __attribute__((objc_designated_initializer));
- (SharedCaptureResult *)doCopyImageBytes:(SharedKotlinByteArray *)imageBytes width:(int32_t)width height:(int32_t)height rotationDegrees:(int32_t)rotationDegrees __attribute__((swift_name("doCopy(imageBytes:width:height:rotationDegrees:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t height __attribute__((swift_name("height")));
@property (readonly) SharedKotlinByteArray *imageBytes __attribute__((swift_name("imageBytes")));
@property (readonly) int32_t rotationDegrees __attribute__((swift_name("rotationDegrees")));
@property (readonly) int32_t width __attribute__((swift_name("width")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LocationData")))
@interface SharedLocationData : SharedBase
- (instancetype)initWithLatitude:(double)latitude longitude:(double)longitude altitude:(double)altitude accuracyMeters:(float)accuracyMeters speedKmh:(float)speedKmh bearingDegrees:(float)bearingDegrees timestamp:(int64_t)timestamp isGpsFixed:(BOOL)isGpsFixed isMockLocation:(BOOL)isMockLocation provider:(NSString *)provider distanceTraveledMeters:(double)distanceTraveledMeters fixAgeMs:(int64_t)fixAgeMs __attribute__((swift_name("init(latitude:longitude:altitude:accuracyMeters:speedKmh:bearingDegrees:timestamp:isGpsFixed:isMockLocation:provider:distanceTraveledMeters:fixAgeMs:)"))) __attribute__((objc_designated_initializer));
- (SharedLocationData *)doCopyLatitude:(double)latitude longitude:(double)longitude altitude:(double)altitude accuracyMeters:(float)accuracyMeters speedKmh:(float)speedKmh bearingDegrees:(float)bearingDegrees timestamp:(int64_t)timestamp isGpsFixed:(BOOL)isGpsFixed isMockLocation:(BOOL)isMockLocation provider:(NSString *)provider distanceTraveledMeters:(double)distanceTraveledMeters fixAgeMs:(int64_t)fixAgeMs __attribute__((swift_name("doCopy(latitude:longitude:altitude:accuracyMeters:speedKmh:bearingDegrees:timestamp:isGpsFixed:isMockLocation:provider:distanceTraveledMeters:fixAgeMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float accuracyMeters __attribute__((swift_name("accuracyMeters")));
@property (readonly) double altitude __attribute__((swift_name("altitude")));
@property (readonly) float bearingDegrees __attribute__((swift_name("bearingDegrees")));
@property (readonly) double distanceTraveledMeters __attribute__((swift_name("distanceTraveledMeters")));
@property (readonly) int64_t fixAgeMs __attribute__((swift_name("fixAgeMs")));
@property (readonly) BOOL isGpsFixed __attribute__((swift_name("isGpsFixed")));
@property (readonly) BOOL isMockLocation __attribute__((swift_name("isMockLocation")));
@property (readonly) double latitude __attribute__((swift_name("latitude")));
@property (readonly) double longitude __attribute__((swift_name("longitude")));
@property (readonly) NSString *provider __attribute__((swift_name("provider")));
@property (readonly) float speedKmh __attribute__((swift_name("speedKmh")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@end

__attribute__((swift_name("MapController")))
@protocol SharedMapController
@required
- (void)addMarkerMarker:(SharedMapMarker *)marker __attribute__((swift_name("addMarker(marker:)")));
- (void)addPolylinePoints:(NSArray<SharedKotlinPair<SharedDouble *, SharedDouble *> *> *)points colorHex:(int64_t)colorHex __attribute__((swift_name("addPolyline(points:colorHex:)")));
- (void)animateToRegionRegion:(SharedMapRegion *)region durationMs:(int64_t)durationMs __attribute__((swift_name("animateToRegion(region:durationMs:)")));
- (void)clearMarkers __attribute__((swift_name("clearMarkers()")));
- (void)fitAllMarkersPaddingPx:(int32_t)paddingPx __attribute__((swift_name("fitAllMarkers(paddingPx:)")));
- (void)removeMarkerId:(NSString *)id __attribute__((swift_name("removeMarker(id:)")));
- (void)setMapLayerLayer:(SharedMapLayer *)layer __attribute__((swift_name("setMapLayer(layer:)")));
@end

__attribute__((swift_name("KotlinComparable")))
@protocol SharedKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end

__attribute__((swift_name("KotlinEnum")))
@interface SharedKotlinEnum<E> : SharedBase <SharedKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) SharedKotlinEnumCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MapLayer")))
@interface SharedMapLayer : SharedKotlinEnum<SharedMapLayer *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedMapLayer *normal __attribute__((swift_name("normal")));
@property (class, readonly) SharedMapLayer *satellite __attribute__((swift_name("satellite")));
@property (class, readonly) SharedMapLayer *hybrid __attribute__((swift_name("hybrid")));
@property (class, readonly) SharedMapLayer *terrain __attribute__((swift_name("terrain")));
+ (SharedKotlinArray<SharedMapLayer *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedMapLayer *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MapMarker")))
@interface SharedMapMarker : SharedBase
- (instancetype)initWithId:(NSString *)id latitude:(double)latitude longitude:(double)longitude title:(NSString *)title snippet:(NSString *)snippet severity:(SharedMarkerSeverity *)severity type:(SharedMarkerType *)type __attribute__((swift_name("init(id:latitude:longitude:title:snippet:severity:type:)"))) __attribute__((objc_designated_initializer));
- (SharedMapMarker *)doCopyId:(NSString *)id latitude:(double)latitude longitude:(double)longitude title:(NSString *)title snippet:(NSString *)snippet severity:(SharedMarkerSeverity *)severity type:(SharedMarkerType *)type __attribute__((swift_name("doCopy(id:latitude:longitude:title:snippet:severity:type:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) double latitude __attribute__((swift_name("latitude")));
@property (readonly) double longitude __attribute__((swift_name("longitude")));
@property (readonly) SharedMarkerSeverity *severity __attribute__((swift_name("severity")));
@property (readonly) NSString *snippet __attribute__((swift_name("snippet")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@property (readonly) SharedMarkerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MapRegion")))
@interface SharedMapRegion : SharedBase
- (instancetype)initWithCenterLatitude:(double)centerLatitude centerLongitude:(double)centerLongitude latitudeDelta:(double)latitudeDelta longitudeDelta:(double)longitudeDelta __attribute__((swift_name("init(centerLatitude:centerLongitude:latitudeDelta:longitudeDelta:)"))) __attribute__((objc_designated_initializer));
- (SharedMapRegion *)doCopyCenterLatitude:(double)centerLatitude centerLongitude:(double)centerLongitude latitudeDelta:(double)latitudeDelta longitudeDelta:(double)longitudeDelta __attribute__((swift_name("doCopy(centerLatitude:centerLongitude:latitudeDelta:longitudeDelta:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) double centerLatitude __attribute__((swift_name("centerLatitude")));
@property (readonly) double centerLongitude __attribute__((swift_name("centerLongitude")));
@property (readonly) double latitudeDelta __attribute__((swift_name("latitudeDelta")));
@property (readonly) double longitudeDelta __attribute__((swift_name("longitudeDelta")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MarkerSeverity")))
@interface SharedMarkerSeverity : SharedKotlinEnum<SharedMarkerSeverity *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedMarkerSeverity *critical __attribute__((swift_name("critical")));
@property (class, readonly) SharedMarkerSeverity *high __attribute__((swift_name("high")));
@property (class, readonly) SharedMarkerSeverity *medium __attribute__((swift_name("medium")));
@property (class, readonly) SharedMarkerSeverity *normal __attribute__((swift_name("normal")));
+ (SharedKotlinArray<SharedMarkerSeverity *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedMarkerSeverity *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MarkerType")))
@interface SharedMarkerType : SharedKotlinEnum<SharedMarkerType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) SharedMarkerType *denuncia __attribute__((swift_name("denuncia")));
@property (class, readonly) SharedMarkerType *inspection __attribute__((swift_name("inspection")));
@property (class, readonly) SharedMarkerType *liveGps __attribute__((swift_name("liveGps")));
+ (SharedKotlinArray<SharedMarkerType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<SharedMarkerType *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformCameraService")))
@interface SharedPlatformCameraService : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)capturePhotoWithCompletionHandler:(void (^)(SharedCaptureResult * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("capturePhoto(completionHandler:)")));
- (void)initializeSessionUseFrontCamera:(BOOL)useFrontCamera __attribute__((swift_name("initializeSession(useFrontCamera:)")));
- (void)release_ __attribute__((swift_name("release()")));
- (void)toggleCamera __attribute__((swift_name("toggleCamera()")));
- (void)toggleFlash __attribute__((swift_name("toggleFlash()")));
@property (readonly) id<SharedKotlinx_coroutines_coreStateFlow> cameraStatus __attribute__((swift_name("cameraStatus")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformLocationService")))
@interface SharedPlatformLocationService : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)getCurrentPrecisionFixWithCompletionHandler:(void (^)(SharedLocationData * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("getCurrentPrecisionFix(completionHandler:)")));
- (void)resetDistanceCounter __attribute__((swift_name("resetDistanceCounter()")));
- (void)startLocationUpdatesIntervalMs:(int64_t)intervalMs minDisplacementMeters:(float)minDisplacementMeters __attribute__((swift_name("startLocationUpdates(intervalMs:minDisplacementMeters:)")));
- (void)stopLocationUpdates __attribute__((swift_name("stopLocationUpdates()")));
@property (readonly) id<SharedKotlinx_coroutines_coreStateFlow> isTracking __attribute__((swift_name("isTracking")));
@property (readonly) id<SharedKotlinx_coroutines_coreStateFlow> locationData __attribute__((swift_name("locationData")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformSensorManager")))
@interface SharedPlatformSensorManager : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)startListening __attribute__((swift_name("startListening()")));
- (void)stopListening __attribute__((swift_name("stopListening()")));
@property (readonly) id<SharedKotlinx_coroutines_coreStateFlow> telemetry __attribute__((swift_name("telemetry")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SensorTelemetryData")))
@interface SharedSensorTelemetryData : SharedBase
- (instancetype)initWithLatitude:(double)latitude longitude:(double)longitude altitude:(double)altitude accuracy:(float)accuracy speed:(float)speed azimuthBearing:(float)azimuthBearing pitch:(float)pitch roll:(float)roll pressureHpa:(float)pressureHpa isGpsFixed:(BOOL)isGpsFixed isMockLocation:(BOOL)isMockLocation distanceTraveledKm:(double)distanceTraveledKm __attribute__((swift_name("init(latitude:longitude:altitude:accuracy:speed:azimuthBearing:pitch:roll:pressureHpa:isGpsFixed:isMockLocation:distanceTraveledKm:)"))) __attribute__((objc_designated_initializer));
- (SharedSensorTelemetryData *)doCopyLatitude:(double)latitude longitude:(double)longitude altitude:(double)altitude accuracy:(float)accuracy speed:(float)speed azimuthBearing:(float)azimuthBearing pitch:(float)pitch roll:(float)roll pressureHpa:(float)pressureHpa isGpsFixed:(BOOL)isGpsFixed isMockLocation:(BOOL)isMockLocation distanceTraveledKm:(double)distanceTraveledKm __attribute__((swift_name("doCopy(latitude:longitude:altitude:accuracy:speed:azimuthBearing:pitch:roll:pressureHpa:isGpsFixed:isMockLocation:distanceTraveledKm:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) float accuracy __attribute__((swift_name("accuracy")));
@property (readonly) double altitude __attribute__((swift_name("altitude")));
@property (readonly) float azimuthBearing __attribute__((swift_name("azimuthBearing")));
@property (readonly) double distanceTraveledKm __attribute__((swift_name("distanceTraveledKm")));
@property (readonly) BOOL isGpsFixed __attribute__((swift_name("isGpsFixed")));
@property (readonly) BOOL isMockLocation __attribute__((swift_name("isMockLocation")));
@property (readonly) double latitude __attribute__((swift_name("latitude")));
@property (readonly) double longitude __attribute__((swift_name("longitude")));
@property (readonly) float pitch __attribute__((swift_name("pitch")));
@property (readonly) float pressureHpa __attribute__((swift_name("pressureHpa")));
@property (readonly) float roll __attribute__((swift_name("roll")));
@property (readonly) float speed __attribute__((swift_name("speed")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinPair")))
@interface SharedKotlinPair<__covariant A, __covariant B> : SharedBase
- (instancetype)initWithFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("init(first:second:)"))) __attribute__((objc_designated_initializer));
- (SharedKotlinPair<A, B> *)doCopyFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("doCopy(first:second:)")));
- (BOOL)equalsOther:(id _Nullable)other __attribute__((swift_name("equals(other:)")));
- (int32_t)hashCode __attribute__((swift_name("hashCode()")));
- (NSString *)toString __attribute__((swift_name("toString()")));
@property (readonly) A _Nullable first __attribute__((swift_name("first")));
@property (readonly) B _Nullable second __attribute__((swift_name("second")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinByteArray")))
@interface SharedKotlinByteArray : SharedBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(SharedByte *(^)(SharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int8_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (SharedKotlinByteIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int8_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinEnumCompanion")))
@interface SharedKotlinEnumCompanion : SharedBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) SharedKotlinEnumCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface SharedKotlinArray<T> : SharedBase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(SharedInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<SharedKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((swift_name("KotlinThrowable")))
@interface SharedKotlinThrowable : SharedBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));

/**
 * @note annotations
 *   kotlin.experimental.ExperimentalNativeApi
*/
- (SharedKotlinArray<NSString *> *)getStackTrace __attribute__((swift_name("getStackTrace()")));
- (void)printStackTrace __attribute__((swift_name("printStackTrace()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) SharedKotlinThrowable * _Nullable cause __attribute__((swift_name("cause")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
- (NSError *)asError __attribute__((swift_name("asError()")));
@end

__attribute__((swift_name("KotlinException")))
@interface SharedKotlinException : SharedKotlinThrowable
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinRuntimeException")))
@interface SharedKotlinRuntimeException : SharedKotlinException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinIllegalStateException")))
@interface SharedKotlinIllegalStateException : SharedKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.4")
*/
__attribute__((swift_name("KotlinCancellationException")))
@interface SharedKotlinCancellationException : SharedKotlinIllegalStateException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(SharedKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreFlow")))
@protocol SharedKotlinx_coroutines_coreFlow
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)collectCollector:(id<SharedKotlinx_coroutines_coreFlowCollector>)collector completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("collect(collector:completionHandler:)")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreSharedFlow")))
@protocol SharedKotlinx_coroutines_coreSharedFlow <SharedKotlinx_coroutines_coreFlow>
@required
@property (readonly) NSArray<id> *replayCache __attribute__((swift_name("replayCache")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreStateFlow")))
@protocol SharedKotlinx_coroutines_coreStateFlow <SharedKotlinx_coroutines_coreSharedFlow>
@required
@property (readonly) id _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("KotlinIterator")))
@protocol SharedKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

__attribute__((swift_name("KotlinByteIterator")))
@interface SharedKotlinByteIterator : SharedBase <SharedKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (SharedByte *)next __attribute__((swift_name("next()")));
- (int8_t)nextByte __attribute__((swift_name("nextByte()")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreFlowCollector")))
@protocol SharedKotlinx_coroutines_coreFlowCollector
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitValue:(id _Nullable)value completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(value:completionHandler:)")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
