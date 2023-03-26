package hse.agents.util;

import java.io.Serializable;

public class MyPair<T, U> implements Serializable {

    private static final long serialVersionUID = 1L;
    private T left;
    private U right;
    
    public MyPair(T left, U right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MyPair) {
            MyPair p = (MyPair) o;
            return p.getKey().equals(this.left) && p.getValue().equals(this.right);
        }
        else
            return false;
    }

    public T getKey() {
        return left;
    }

    public U getValue() {
        return right;
    }

    public void setValue(U newValue) {
        right = newValue;
    }
}