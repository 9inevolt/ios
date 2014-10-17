//
//  DGGChannel.m
//  Destiny
//

#import "DGGChannel.h"

@interface DGGChannel ()

@property (nonatomic, readwrite) NSString *name;
@property (nonatomic, readwrite) NSString *displayName;
@property (nonatomic, readwrite) NSString *status;
@property (nonatomic, readwrite) NSString *logo;
@property (nonatomic, readwrite) NSString *videoBanner;

@end

@implementation DGGChannel

- (id)initWithDictionary:(NSDictionary *)dict
{
    self = [super init];
    if (self)
    {
        self.name = dict[@"name"];
        self.displayName = dict[@"display_name"];
        self.status = dict[@"status"];
        self.logo = dict[@"logo"];
        self.videoBanner = dict[@"video_banner"];
    }
    return self;
}

@end
