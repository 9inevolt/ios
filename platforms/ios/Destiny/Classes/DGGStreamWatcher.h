//
//  DGGStreamWatcher.h
//  Destiny
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, DGGStreamStatus) {
    DGGStreamStatusOffline,
    DGGStreamStatusOnline
};

@protocol DGGStreamWatcherDelegate <NSObject>

- (void)online;
- (void)offline;

@end

@interface DGGStreamWatcher : NSObject

@property (nonatomic, readonly) NSString *channel;
@property (nonatomic, readonly) BOOL isRunning;

- (id)initWithChannel:(NSString *)channel
             delegate:(id<DGGStreamWatcherDelegate>)delegate;
- (void)start;
- (void)stop;

@end
