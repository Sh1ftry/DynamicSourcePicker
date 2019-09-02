package view;

import rx.Observable;
import rx.schedulers.Schedulers;
import source.SensorReading;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UserInterface {

    private final Scanner in;
    private final PrintStream out;

    public UserInterface(final InputStream in, final OutputStream out) {
        this.in = new Scanner(in);
        this.out = new PrintStream(out);
    }

    public <A, B> void show(final Function<String, A> firstArgumentParser, final Function<String, B> secondArgumentParser,
                            final BiFunction<A, B, Observable<SensorReading>> logic) {
        Observable.fromCallable(() -> {
            out.print(">> ");
            return in.nextLine().split(" ");
        }).flatMap(input -> logic.apply(
                        firstArgumentParser.apply(input[0]),
                        secondArgumentParser.apply(input[1])))
                .subscribeOn(Schedulers.io())
                .repeat().toBlocking()
                .subscribe(out::println, out::println);
    }
}
