//
//  DGGKrakenApi.h
//  Destiny
//

#import <Foundation/Foundation.h>

@interface DGGKrakenApi : NSObject

+ (void)getChannel:(NSString *)channel
        completion:(void (^)(NSDictionary *))completion;
+ (void)getChannelAccessToken:(NSString *)channel
        completion:(void (^)(NSDictionary *))completion;
+ (void)getStream:(NSString *)channel
        completion:(void (^)(NSDictionary *))completion;
+ (void)getPlaylist:(NSString *)channel
        completion:(void (^)(NSString *))completion;
+ (NSURL *)getPlaylistURL:(NSString *)channel token:(NSDictionary *)token;

@end
