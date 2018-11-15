package com.yifanwu.examples.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * R must impelment euqals and hashcode
 * @author Yifan.Wu on 10/2/2017
 */
public class EqualableRecords<R> extends LinkedBlockingQueue<R> {
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EqualableRecords)) {
            return false;
        }
        EqualableRecords that = (EqualableRecords) obj;
        if (this.size() != that.size()) {
            return false;
        }
        List thisList = new ArrayList<>(this);
        List thatList = new ArrayList<>(that);
        Collections.sort(thisList);
        Collections.sort(thatList);
        return thisList.equals(thatList);

    }


    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof EqualableRecords)) return false;
//        EqualableRecords that = (EqualableRecords) o;
//
//        if (this.size() != that.size()) {
//            return false;
//        }
//        return this.containsAll(that);
//    }

}
