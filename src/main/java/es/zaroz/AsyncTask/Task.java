package es.zaroz.AsyncTask;

import java.util.function.Consumer;

public abstract class Task {
    public abstract boolean isDone();
    public abstract boolean isWorking();
    public abstract boolean hasFailed();
    public abstract Task start();
    public abstract Task then(Runnable action);
    public abstract Task then(Consumer<Task> action);
    public abstract Task error(Consumer<Exception> action);

    public abstract void await();

    public static Task whenAll(Task... tasks){
        return VoidTask.run(() -> {
            for (Task t : tasks) {
                t.await();
            }
        });
    }

    public static Task whenAny(Task... tasks){
        return VoidTask.run(() -> {
            while(true){
                for (Task t : tasks) {
                    if (t.isDone()) return;
                }
            }
        });
    }
}
