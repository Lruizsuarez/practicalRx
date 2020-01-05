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
        return Observable.create(subscriber -> {
            connectedUsers.add(user);
            System.out.println(user.nickname + " connected");

            subscriber.onNext(Boolean.TRUE);
            subscriber.onCompleted();
        });
    }

    public Observable<Boolean> disconnectUser(User user) {
        return Observable.create(subscriber -> {
            connectedUsers.remove(user);
            System.out.println(user.nickname + " disconnected");

            subscriber.onNext(Boolean.TRUE);
            subscriber.onCompleted();
        });
    }
}
