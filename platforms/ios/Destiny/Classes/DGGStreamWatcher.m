//
//  DGGStreamWatcher.m
//  Destiny
//

#import "DGGStream.h"
#import "DGGStreamWatcher.h"
#import "DGGKrakenApi.h"

static double INTERVAL = 30;

@interface DGGStreamWatcher () {
    NSTimer *_timer;
    DGGStreamStatus _status;
}

@property (nonatomic, readwrite) NSString *channel;
@property (nonatomic, readwrite) BOOL isRunning;
@property (nonatomic, weak, readwrite) id<DGGStreamWatcherDelegate> delegate;

@end

@implementation DGGStreamWatcher

- (id)initWithChannel:(NSString *)channel delegate:(id<DGGStreamWatcherDelegate>)delegate
{
    self = [super init];
    if (self)
    {
        self.channel = channel;
        self.delegate = delegate;
        self.isRunning = NO;
        _timer = nil;
        _status = DGGStreamStatusOffline;
    }
    return self;
}

- (void)start
{
    if (!self.isRunning)
    {
        [self checkStream];
        [self reschedule];
        self.isRunning = YES;
    }
}

- (void)stop
{
    if (self.isRunning)
    {
        [self cancel];
        _timer = nil;
        self.isRunning = NO;
    }
}

- (void)tryGoOnline
{
    if (_status == DGGStreamStatusOnline)
        return;
    
    _status = DGGStreamStatusOnline;
    if (self.delegate)
        [self.delegate online];
    
    [self cancel];
}

- (void)goOffline
{
    if (_status == DGGStreamStatusOffline)
        return;
    
    _status = DGGStreamStatusOffline;
    if (self.delegate)
        [self.delegate offline];
    
    [self reschedule];
}

- (void)cancel
{
    if (_timer != nil)
    {
        [_timer invalidate];
    }
}

- (void)reschedule
{
    [self cancel];
    _timer = [NSTimer scheduledTimerWithTimeInterval:INTERVAL target:self
                                            selector:@selector(checkStream) userInfo:nil repeats:YES];
}

- (void)checkStream
{
    [DGGKrakenApi getStream:self.channel completion:^(NSDictionary *dict) {
        if (dict != nil)
        {
            NSLog(@"Channel %@ is online", self.channel);
            //DGGStream *stream = [[DGGStream alloc] initWithDictionary:dict];
            [self tryGoOnline];
        }
        else
        {
            NSLog(@"Channel %@ is offline", self.channel);
            [self goOffline];
        }
    }];
}

- (void)fetchPlaylist
{
    //TODO
}

@end
