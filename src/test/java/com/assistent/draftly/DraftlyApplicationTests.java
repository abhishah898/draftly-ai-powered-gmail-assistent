package com.assistent.draftly;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootTest
class DraftlyApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void test() {
		System.out.println("Test Started!");
//		Mono<String> monoPublisher = Mono.just("Abhishek");
//		monoPublisher.subscribe(new CoreSubscriber<String>() {
//			@Override
//			public void onSubscribe(Subscription subscription) {
//				System.out.println("Subscribed!");
//				subscription.request(1);
//			}
//
//			@Override
//			public void onNext(String s) {
//				System.out.println("Data: " + s);
//			}
//
//			@Override
//			public void onError(Throwable throwable) {
//				System.out.println(throwable.getMessage());
//			}
//
//			@Override
//			public void onComplete() {
//				System.out.println("Completed");
//			}
//		});
//
//		Mono<String> m1 = Mono.just("I am data");
//		Mono<String> m2 = Mono.just("I am data 2");
//		Mono<Tuple2<String, String>> monoZip = Mono.zip(m1, m2);
//		monoZip.subscribe(data -> {
//			System.out.println(data);
//		});
//
//		Mono<Tuple2<String, String>> zipWithMono = m1.zipWith(m2);
//		zipWithMono.subscribe(data->{
//			System.out.println(data.getT1());
//			System.out.println(data.getT2());
//		});
		Mono<String> m1 = Mono.just("I am data");
		Mono<String> m2 = Mono.just("I am data 2");
		Mono<Integer> m3 = Mono.just(132456);
		Mono<String> resultMapMono = m1.map(data -> {
			System.out.println("Inside map function");
			System.out.println(data);
			System.out.println(data.toUpperCase());
			return data.toUpperCase();
		});

		resultMapMono.subscribe(System.out::println);

	}

}
