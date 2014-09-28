//
//  DGGKrakenApi.m
//  Destiny
//

#import "DGGKrakenApi.h"

static NSURLSessionConfiguration *sharedSession;

@implementation DGGKrakenApi

+ (void)initialize
{
    sharedSession = [NSURLSessionConfiguration defaultSessionConfiguration];
}

+ (void)getChannelAccessToken:(NSString *)channel completion:(void (^)(NSDictionary*))completion
{
    NSString *urlChannel = [channel stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSString *url = [NSString stringWithFormat:@"http://api.twitch.tv/api/channels/%@/access_token", urlChannel];
    [[[NSURLSession sharedSession] dataTaskWithURL:[NSURL URLWithString:url]
                                completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                                      NSDictionary *tokenData = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
                                      if (tokenData) {
                                          completion(tokenData);
                                      }
                                }] resume];
}

+ (NSURL *)getPlaylistURL:(NSString *)channel token:(NSDictionary *)token
{
    NSString *urlChannel = [channel stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSString *urlSig = [[token objectForKey:@"sig"] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSString *urlToken = [[token objectForKey:@"token"] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSString *url = [NSString stringWithFormat:@"http://usher.twitch.tv/select/%@.json?nauthsig=%@&nauth=%@&allow_source=true&allow_audio_only=true",
                     urlChannel, urlSig, urlToken];
    return [NSURL URLWithString:url];
}

+ (void)getPlaylist:(NSString *)channel completion:(void (^)(NSString *))completion
{
    [self getChannelAccessToken:channel completion:^(NSDictionary *token) {
//        NSLog(@"Token received: %@", token);
        if (token && [token objectForKey:@"sig"] && [token objectForKey:@"token"]) {
            NSURL *url = [self getPlaylistURL:channel token:token];
//            NSLog(@"Requesting playlist from %@", url);
            [[[NSURLSession sharedSession] dataTaskWithURL:url
                                         completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
//                                             NSLog(@"Playlist data: %@", data);
                                             NSString *playlist = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                                             completion(playlist);
                                         }] resume];
        }
    }];
}

@end
