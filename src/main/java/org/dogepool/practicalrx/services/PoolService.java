package org.dogepool.practicalrx.services;

import org.dogepool.practicalrx.domain.User;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.HashSet;
import java.util.Set;

/**
 * Service to retrieve information on the current status of the mining pool
 */
@Service
public class PoolService {

    private final Set<User> connectedUsers = new HashSet<>();

    public String poolName() {
        return "Wow Such Pool!";
    }

    public Observable<User> miningUsers() {
        return Observable.from(connectedUsers);
    }

    public Observable<Boolean> connectUser(User user) {
        //for use onNext with same Obs have to add <T> reference
        return Observable.<Boolean>create(subscriber -> {
            connectedUsers.add(user);
            subscriber.onNext(Boolean.TRUE);
            subscriber.onCompleted();
        }).doOnNext(value -> System.out.println(user.nickname + "connected : " + value));
    }

    public Observable<Boolean> disconnectUser(User user) {
        //for use onNext with same Obs have to add <T> reference
        return Observable.<Boolean>create(subscriber -> {
            connectedUsers.remove(user);
            System.out.println(user.nickname + " disconnected");

            subscriber.onNext(Boolean.TRUE);
            subscriber.onCompleted();
        }).doOnNext(value -> System.out.println(user.nickname + "disconnected : " + value));
    }
}
