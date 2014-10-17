//
//  DGGChannel.h
//  Destiny
//

#import <Foundation/Foundation.h>

@interface DGGChannel : NSObject

@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSString *displayName;
@property (nonatomic, readonly) NSString *status;
@property (nonatomic, readonly) NSString *logo;
@property (nonatomic, readonly) NSString *videoBanner;

- (id)initWithDictionary:(NSDictionary *)dict;

@end
