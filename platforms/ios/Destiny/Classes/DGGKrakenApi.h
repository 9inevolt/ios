//
//  DGGKrakenApi.h
//  Destiny
//

#import <Foundation/Foundation.h>

@interface DGGKrakenApi : NSObject

+ (void)getChannelAccessToken:(NSString *)channel
                   completion:(void (^)(NSDictionary *))completion;
+ (void)getPlaylist:(NSString *)channel
         completion:(void (^)(NSString *))completion;
+ (NSURL *)getPlaylistURL:(NSString *)channel token:(NSDictionary *)token;

@end
