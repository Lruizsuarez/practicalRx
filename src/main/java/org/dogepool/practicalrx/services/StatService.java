package org.dogepool.practicalrx.services;

import org.dogepool.practicalrx.domain.User;
import org.dogepool.practicalrx.domain.UserStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

/**
 * Service to get stats on the pool, like top 10 ladders for various criteria.
 */
@Service
public class StatService {

    @Autowired
    private HashrateService hashrateService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private UserService userService;

    public Observable<UserStat> getAllStats() {
        return userService.findAll()
                .flatMap(u -> {
                    Observable<Double> hashRate = hashrateService.hashrateFor(u);
                    Observable<Long> coins = coinService.totalCoinsMinedBy(u);

                    return Observable.zip(hashRate, coins, (rate, coin) -> new UserStat(u, rate, coin));
                });
    }

    public Observable<LocalDateTime> lastBlockFoundDate() {
        Random rng = new Random(System.currentTimeMillis());
        return Observable.just(LocalDateTime.now().minus(rng.nextInt(72), ChronoUnit.HOURS));
    }

    public Observable<User> lastBlockFoundBy() {
        Random rng = new Random(System.currentTimeMillis());
        return Observable.defer(() -> Observable.just(rng.nextInt(10)))
                .doOnNext(System.out::println)
                .flatMap(potentiallyBadIndex -> userService.findAll().elementAt(potentiallyBadIndex))
                .retry();
    }
}
