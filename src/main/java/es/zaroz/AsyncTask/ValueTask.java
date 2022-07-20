package es.zaroz.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ValueTask<T> extends Task {
    private final Callable<T> _work;
    private boolean _isWorking;
    private boolean _isCompleted;

    private Exception _exception = null;
    private T _result = null;

    private final ArrayList<Runnable> _then0Callbacks = new ArrayList<>();
    private final ArrayList<Consumer<Task>> _then1Callbacks = new ArrayList<>();
    private final ArrayList<BiConsumer<ValueTask<T>, T>> _then2Callbacks = new ArrayList<>();

    private final ArrayList<Consumer<Exception>> _errorCallbacks = new ArrayList<>();

    private Thread _thread;

    public ValueTask(Callable<T> work){
        this._work = work;
    }

    public ValueTask<T> vtStart(){
        if (_isCompleted) return this;
        work_thread();
        return this;
    }

    private void work_thread(){
        _thread = new Thread(() -> {
            try{
                _isWorking = true;
                _result = _work.call();
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

    public static <V> ValueTask<V> run(Callable<V> work){
        try {
            return new ValueTask<V>(work).vtStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        return vtStart();
    }

    @Override
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

    public Task then(BiConsumer<ValueTask<T>, T> action){
        if (hasFailed()) return this;

        if (isDone()) {
            action.accept(this, _result);
            return this;
        }

        _then2Callbacks.add(action);

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

        for (BiConsumer<ValueTask<T>, T> cb : _then2Callbacks) {
            cb.accept(this, _result);
        }
    }
}
