package es.zaroz.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VoidTask extends Task {

    public static final Task CompletedTask = VoidTask.run(() -> {});

    private final Runnable _work;
    private boolean _isWorking;
    private boolean _isCompleted;

    private Exception _exception = null;

    private final ArrayList<Runnable> _then0Callbacks = new ArrayList<>();
    private final ArrayList<Consumer<Task>> _then1Callbacks = new ArrayList<>();

    private final ArrayList<Consumer<Exception>> _errorCallbacks = new ArrayList<>();

    private Thread _thread;

    public VoidTask(Runnable work){
        this._work = work;
    }

    private void work_thread(){
        _thread = new Thread(() -> {
            try {
                _isWorking = true;
                _work.run();
            }catch(Exception exception){
                _exception = exception;
            }finally {
                _isWorking = false;
                _isCompleted = true;
                dispatch();
            }
        });
        _thread.start();
    }

    public static Task run(Runnable work) {
        return new VoidTask(work).start();
    }

    @Override
    public boolean isDone() {
        return _isCompleted;
    }

    @Override
    public boolean isWorking() {
        return _isWorking;
    }

    @Override
    public boolean hasFailed() {
        return _exception != null;
    }

    @Override
    public Task start() {
        if (_isCompleted) return this;
        work_thread();
        return this;
    }

    public Task then(Runnable action) {
        if (hasFailed()) return this;

        if (isDone()) {
            action.run();
            return this;
        }

        _then0Callbacks.add(action);

        return this;
    }

    @Override
    public Task then(Consumer<Task> action) {
        if (hasFailed()) return this;

        if (isDone()) {
            action.accept(this);
            return this;
        }

        _then1Callbacks.add(action);

        return this;
    }

    @Override
    public Task error(Consumer<Exception> action) {

        if (isDone() && hasFailed()) {
            action.accept(_exception);
            return this;
        }

        if (isDone()) return this;

        _errorCallbacks.add(action);

        return this;
    }

    @Override
    public void await() {
        if (_isCompleted) return;
        try {
            _thread.join();
        }catch (Exception error){
            if (_exception == null) {
                _exception = error;
            }

            _isWorking = false;
            _isCompleted = true;
        }
    }

    private void dispatch(){
        if (hasFailed()){
            for (Consumer<Exception> cb : _errorCallbacks) {
                cb.accept(_exception);
            }

            return;
        }

        for (Runnable cb : _then0Callbacks) {
            cb.run();
        }

        for (Consumer<Task> cb : _then1Callbacks) {
            cb.accept(this);
        }
    }
}
