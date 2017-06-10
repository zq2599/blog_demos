package com.bolingcavalry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
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

        //声明一个观察者，用来响应被观察者发布的事件
        Observer<String> observer = new Observer<String>() {
            /**
             * 被观察者发布结束事件的时候，该方法会被调用
             */
            public void onCompleted() {
                logger.debug("start onCompleted");
            }

            /**
             * 被观察者发布事件期间，和观察者处理事件期间，发生异常的时候，该方法都会被调用
             */
            public void onError(Throwable throwable) {
                logger.debug("start onError : " + throwable);
            }

            /**
             * 被观察者发布事件后，该方法会被调用
             * @param s
             */
            public void onNext(String s) {
                logger.debug("start onNext [" + s + "]");
            }
        };

        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            public void call(Subscriber<? super String> subscriber) {
                //向观察者发布事件
                subscriber.onNext("Hello");
                //再次向观察者发布事件
                subscriber.onNext("world");
                //通知观察者，订阅结束
                subscriber.onCompleted();
            }
        });

        logger.debug("try subscribe");

        //执行订阅
        observable.subscribe(observer);

        logger.debug("finish doExecute");
    }


    public void doAction(){
        logger.debug("start doAction");

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

        logger.debug("finish doAction");
    }


    public void doFromChain(){
        logger.debug("start doFromChain");


        //声明一个观察者，用来响应被观察者发布的事件
        Action1<String> observer = new Action1<String>() {
            /**
             * 被观察者发布事件后，该方法会被调用
             * @param s
             */
            public void call(String s) {
                logger.debug("start onNext [" + s + "]");
            }
        };


        String[] array = {"Hello", "world"};

        //from方法可以直接创建被观察者，并且发布array中的元素对应的事件
        Observable.from(array).subscribe(observer);

        logger.debug("finish doFromChain");
    }

    public void doJustChain(){
        logger.debug("start doJustChain");

        Observable.just("Hello", "world")
                .subscribe(s -> logger.debug("start onNext [" + s + "]"));

        logger.debug("finish doJustChain");
    }

    public void doSchedule(){
        logger.debug("start doSchedule");

        Observable.create(subscriber -> {
            logger.debug("enter subscribe");
            subscriber.onNext("Hello");
            subscriber.onCompleted();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.newThread())
        .flatMap(str -> {
            logger.debug("enter flatMap");
            return Observable.create(
                    subscriber -> subscriber.onNext("after flatMap (" + str + ")")
            );
            }
        )
        .observeOn(Schedulers.newThread())
        .subscribe(s -> logger.debug("Observer's onNext invoked [" + s + "]"));
        logger.debug("finish doSchedule");
    }

    public void doMap(){
        logger.debug("start doMap");


        Observable.just(1001, 1002)
        .map(intValue -> "int[" + intValue + "]")
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
