//
//  DGGStream.m
//  Destiny
//

#import "DGGStream.h"

@interface DGGStream ()

@property (nonatomic, readwrite) NSNumber *viewers;
@property (nonatomic, readwrite) DGGChannel *channel;

@end

@implementation DGGStream

- (id)initWithDictionary:(NSDictionary *)dict
{
    self = [super init];
    if (self)
    {
        self.viewers = dict[@"viewers"];
        self.channel = [[DGGChannel alloc] initWithDictionary:dict[@"channel"]];
    }
    return self;
}

@end
