//
//  DGGStream.h
//  Destiny
//

#import <Foundation/Foundation.h>
#import "DGGChannel.h"

@interface DGGStream : NSObject

@property (nonatomic, readonly) NSNumber *viewers;
@property (nonatomic, readonly) DGGChannel *channel;

- (id)initWithDictionary:(NSDictionary *)dictionary;

@end
