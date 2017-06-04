package com.bolingcavalry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public void doExecute(){
        logger.debug("start doExecute");

        Observer<String> observer = new Observer<String>() {
            public void onCompleted() {
                logger.debug("start onCompleted");
            }

            public void onError(Throwable throwable) {
                logger.debug("start onError : " + throwable);
            }

            public void onNext(String s) {
                logger.debug("start onNext [" + s + "]");
            }
        };


        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        });

        logger.debug("try subscribe");

        observable.subscribe(observer);


        logger.debug("finish subscribe");


        logger.debug("finish doExecute");
    }


    public void doAction(){
        logger.debug("start doAction");

        Action0 onCompleteAction = new Action0() {
            public void call() {
                logger.debug("start Action0 onCompleteAction");
            }
        };

        Action1<String> onNextAction = new Action1<String>() {
            public void call(String s) {
                logger.debug("start Action1 onNextAction [" + s + "]");
            }
        };


        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("world");
                subscriber.onCompleted();
            }
        });

        logger.debug("try subscribe");

        observable.subscribe(onNextAction);

        logger.debug("finish subscribe");
        logger.debug("finish doAction");
    }


    public void doChain(){
        logger.debug("start doChain");

        String[] array = {"first", "second", "third"};

        Observable.from(array)
                .subscribe(s -> logger.debug("Action1 call invoked [" + s + "]"));



        logger.debug("finish doChain");
    }

    public void doSchedule(){
        logger.debug("start doSchedule");

        Observable.create(subscriber -> {
            logger.debug("OnSubscribe's call invoked");
            subscriber.onNext("OnSubscribe's onNext");
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.newThread())
        .subscribe(s -> logger.debug("Observer's onNext invoked [" + s + "]"));
        logger.debug("finish doSchedule");
    }

    public void doMap(){
        logger.debug("start doMap");

        Observable.create(subscriber -> {
            logger.debug("OnSubscribe's call invoked");
            subscriber.onNext(1001);
            subscriber.onNext(1002);
            subscriber.onNext(1003);
            subscriber.onCompleted();
        })
        .map(intValue -> "int[" + intValue + "]")
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.immediate())
        .subscribe(s -> logger.debug("Action1 call invoked [" + s + "]"));


        logger.debug("finish doMap");
    }

    public void doFlatMap(){
        logger.debug("start doFlatMap");

        Observable.just(101, 102, 103)
                /*
                //这是使用lambda之前的代码
                .flatMap(new Func1<Integer, Observable<String>>() {
                    public Observable<String> call(final Integer integer) {
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            public void call(Subscriber<? super String> subscriber) {
                                subscriber.onNext("after flatMap (" + integer + ")");
                                subscriber.onNext("after flatMap (" + (integer+1000) + ")");
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                */

                .flatMap(integer -> Observable.create(subscriber -> {
                            subscriber.onNext("after flatMap (" + integer + ")");
                            subscriber.onNext("after flatMap (" + (integer+1000) + ")");
                            subscriber.onCompleted();
                        }
                   )
                )
                .observeOn(Schedulers.computation())
                .subscribe(s -> logger.debug("Action1 call invoked [" + s + "]"));

        logger.debug("finish doFlatMap");
    }

}
